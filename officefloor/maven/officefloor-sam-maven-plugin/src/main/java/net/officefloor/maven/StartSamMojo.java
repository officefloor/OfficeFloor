package net.officefloor.maven;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.function.Function;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;

import net.officefloor.docker.test.DockerContainerInstance;
import net.officefloor.docker.test.DockerNetworkInstance;
import net.officefloor.docker.test.OfficeFloorDockerUtil;
import net.officefloor.nosql.dynamodb.AmazonDynamoDbConnect;

/**
 * Starts SAM for the integration testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "start", requiresDependencyResolution = ResolutionScope.COMPILE)
public class StartSamMojo extends AbstractMojo {

	/**
	 * <code>template.yaml</code> file name.
	 */
	private static final String TEMPLATE_FILE_NAME = "template.yaml";

	/**
	 * {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Base directory.
	 */
	@Parameter(defaultValue = "${project.basedir}", readonly = true)
	private File baseDir;

	/**
	 * Target directory.
	 */
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File target;

	/**
	 * <p>
	 * Port for servicing SAM.
	 * <p>
	 * Must be specified so that project is clear on integration test ports. This
	 * avoids possible changing default port number.
	 */
	@Parameter(required = true, property = "sam.port")
	private int port;

	/**
	 * Name of the docker network.
	 */
	@Parameter(property = "sam.docker.network", defaultValue = "officefloor-sam")
	private String dockerNetworkName;

	/**
	 * Additional environment properties.
	 */
	@Parameter
	private Map<String, String> env;

	/**
	 * Ensures the template.yaml file exists.
	 */
	public void ensureTemplateYamlFileExists() throws MojoExecutionException {

		// Check if template file exists
		File templateFile = new File(this.baseDir, TEMPLATE_FILE_NAME);
		if (templateFile.exists()) {
			this.getLog().debug("Project already has template file " + TEMPLATE_FILE_NAME);
			return; // already exists
		}

		// Write the template file
		this.getLog().info("Creating " + TEMPLATE_FILE_NAME);
		try {
			InputStream input = this.getClass().getResourceAsStream("/" + TEMPLATE_FILE_NAME);

			// Ensure have input
			if (input == null) {
				throw new MojoExecutionException("Unable to locate template file " + TEMPLATE_FILE_NAME);
			}

			// Read the contents of template
			StringWriter buffer = new StringWriter();
			try (Reader reader = new InputStreamReader(input)) {
				for (int character = reader.read(); character != -1; character = reader.read()) {
					buffer.write(character);
				}
			}
			String template = buffer.toString();

			// Obtain project details
			String artifactId = this.project.getArtifactId();
			String description = this.project.getDescription();

			// Generate the environment details
			StringBuilder environment = new StringBuilder("Variables:");
			this.env.forEach((name, value) -> environment.append("\n          " + name + ": " + value));

			// Replace the tags
			template = template.replace("ARTIFACT_ID", artifactId);
			template = template.replace("DESCRIPTION",
					((description != null) && (description.trim().length() > 0)) ? description : artifactId);
			template = template.replace("ENVIRONMENT", environment.toString());

			// Write the template file
			try (Writer writer = new FileWriter(templateFile)) {
				writer.write(template);
			}

		} catch (IOException ex) {
			throw new MojoExecutionException("Failure in reading template file", ex);
		}
	}

	/**
	 * Undertakes SAM build.
	 */
	public void samBuild() throws MojoExecutionException {

		// Start the build
		Process samBuild = this.startProcess((line) -> true, "sam", "build");

		try {
			// Confirm success of build
			int status = samBuild.waitFor();
			if (status != 0) {
				throw new MojoExecutionException("Process exited with status " + status);
			}
		} catch (InterruptedException ex) {
			throw new MojoExecutionException("Failed to wait for process completion", ex);
		}
	}

	/**
	 * Copies in the maven dependencies.
	 */
	public void copyDependencies() throws MojoExecutionException {

		// Obtain function directory (should be only directory under build)
		File awsBuildDir = new File(this.baseDir, ".aws-sam/build");
		File functionDir = null;
		NEXT_FILE: for (File child : awsBuildDir.listFiles()) {

			// Must be directory
			if (!child.isDirectory()) {
				continue NEXT_FILE;
			}

			// Ensure only the one directory
			if (functionDir != null) {
				throw new MojoExecutionException("Found two AWS functions [" + functionDir.getName() + ", "
						+ child.getName() + "] in " + awsBuildDir.getAbsolutePath());
			}

			// Have the function directory
			functionDir = child;
		}

		// Copy over the artifacts (if not existing)
		File libDir = new File(functionDir, "lib");
		if (!libDir.exists()) {
			libDir.mkdirs();
		}
		for (Artifact artifact : this.project.getArtifacts()) {
			File artifactFile = artifact.getFile();
			File libFile = new File(libDir, artifactFile.getName());
			if (!libFile.exists()) {

				// Copy in the artifact
				try {
					try (InputStream input = new BufferedInputStream(new FileInputStream(artifactFile))) {
						try (OutputStream output = new BufferedOutputStream(new FileOutputStream(libFile))) {
							for (int datum = input.read(); datum != -1; datum = input.read()) {
								output.write(datum);
							}
						}
					}
				} catch (IOException ex) {
					throw new MojoExecutionException("Failed to copy " + artifactFile.getName(), ex);
				}
			}
		}
	}

	/**
	 * Ensure the docker network is created.
	 * 
	 * @throws MojoExecutionException If fails to create network.
	 */
	public DockerNetworkInstance dockerNetwork() throws MojoExecutionException {
		try {
			return OfficeFloorDockerUtil.ensureNetworkAvailable(dockerNetworkName);
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed to start docker network", ex);
		}
	}

	/**
	 * Starts DynamoDB.
	 * 
	 * @return {@link DockerContainerInstance} for managing DynamoDB.
	 * @throws MojoExecutionException If fails to start.
	 */
	public DockerContainerInstance dynamoDb() throws MojoExecutionException {
		try {
			final String containerName = AmazonDynamoDbConnect.DYNAMODB_LOCAL;
			final String imageName = "amazon/dynamodb-local:latest";
			return OfficeFloorDockerUtil.ensureContainerAvailable(containerName, imageName,
					(client) -> client.createContainerCmd(imageName).withName(containerName)
							.withHostConfig(new HostConfig().withNetworkMode(this.dockerNetworkName))
							.withExposedPorts(new ExposedPort(8000)));
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed to start DynamoDB", ex);
		}
	}

	/**
	 * Starts the local SAM server.
	 * 
	 * @return {@link Process} for the local SAM server.
	 * @throws MojoExecutionException If fails to start.
	 */
	public Process samLocalStartApi() throws MojoExecutionException {
		return this.startProcess((line) -> line.contains("Running on http"), "sam", "local", "start-api",
				"--docker-network", this.dockerNetworkName, "--port", String.valueOf(this.port));
	}

	/**
	 * Starts a {@link Process}.
	 * 
	 * @param isRunning {@link Function} to receive each line and return when
	 *                  running. For example, bound to port and ready to serve
	 *                  requests.
	 * @param command   Command for {@link Process}.
	 * @return Started {@link Process}.
	 * @throws MojoExecutionException If fails to start {@link Process}.
	 */
	private Process startProcess(Function<String, Boolean> isRunning, String... command) throws MojoExecutionException {
		try {

			// Build the process
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(this.baseDir);
			builder.redirectErrorStream(true);

			// Create dummy mvn that does nothing
			File mvnExecutable = new File(this.target, "mvn");
			if (!mvnExecutable.exists()) {
				try (Writer writer = new FileWriter(mvnExecutable)) {
					writer.write("#!/bin/sh\n");
				}
				mvnExecutable.setExecutable(true);
			}

			// Obtain the process environment
			Map<String, String> environment = builder.environment();

			// Use dummy to avoid maven from re-building project
			// Note: avoids infinite loop of 'sam build', this plugin, 'sam build'
			String path = environment.get("PATH");
			String targetFirstPath = mvnExecutable.getParentFile().getAbsolutePath() + File.pathSeparator + path;
			environment.put("PATH", targetFirstPath);

			// Override the AWS credentials to avoid 'accidentally' connecting to AWS
			final String AWS_ACCESS_KEY = "OFFICEFLOOR_SAM_LOCAL_TEST_ACCESS_KEY";
			final String AWS_SECRET_KEY = "OFFICEFLOOR_SAM_LOCAL_TEST_SECRET_KEY";
			environment.put("AWS_ACCESS_KEY", AWS_ACCESS_KEY);
			environment.put("AWS_ACCESS_KEY_ID", AWS_ACCESS_KEY);
			environment.put("AWS_SECRET_KEY", AWS_SECRET_KEY);
			environment.put("AWS_SECRET_ACCESS_KEY", AWS_SECRET_KEY);

			// Start the process (and direct output to log)
			boolean[] isProcessRunning = new boolean[] { false };
			Process process = builder.start();
			Thread gobblerThread = new Thread(() -> {
				boolean isNotifiedRunning = false; // flag to only notify running once
				StringBuilder line = new StringBuilder();
				try (Reader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					for (int character = reader.read(); character != -1; character = reader.read()) {
						switch (character) {
						case '\n':
							// End of line, so log the line
							String lineText = line.toString();
							this.getLog().info(lineText);

							// Determine if process running
							if ((!isNotifiedRunning) && (isRunning.apply(lineText))) {
								synchronized (isProcessRunning) {
									isProcessRunning[0] = true;
									isProcessRunning.notifyAll();
									isNotifiedRunning = true;
								}
							}

							// Reset for next line
							line.setLength(0);
							break;
						default:
							// Include character for line
							line.append((char) character);
						}
					}
				} catch (IOException ex) {
					this.getLog().warn("Failure in reading process output", ex);
				} finally {
					// Ensure last line is also written
					this.getLog().info(line.toString());
				}
			});
			gobblerThread.setDaemon(true);
			gobblerThread.start();

			// Wait until process is running
			final long START_UP_TIMEOUT = 30; // seconds
			long startTime = System.currentTimeMillis();
			synchronized (isProcessRunning) {
				while (!isProcessRunning[0]) {
					if ((startTime + (START_UP_TIMEOUT * 1000)) < System.currentTimeMillis()) {
						throw new MojoExecutionException(
								"Time out after " + START_UP_TIMEOUT + " seconds waiting on process to start");
					}
					isProcessRunning.wait(10); // wait some time for process to complete
				}
			}

			// Return the process
			return process;

		} catch (Exception ex) {
			throw new MojoExecutionException("Failure in process", ex);
		}
	}

	/*
	 * ================== AbstractMojo ========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure template file exists
		this.ensureTemplateYamlFileExists();

		// Undertake build (that avoids rebuilding maven)
		this.samBuild();

		// As mvn was no-op, need to copy in maven dependencies
		this.copyDependencies();

		// Ensure docker network available
		DockerNetworkInstance network = this.dockerNetwork();

		// Ensure DynamoDB available
		DockerContainerInstance dynamoDb = this.dynamoDb();

		// Start the local SAM server (ensuring shutdown)
		Process samLocalServer = this.samLocalStartApi();

		// Provide means to stop
		Runnable stop = () -> {
			try {
				// Stop SAM
				samLocalServer.destroyForcibly();
			} finally {
				try {
					// Ensure attempt to close DynamoDB
					dynamoDb.close();
				} finally {
					try {
						// Only try to remove network
						// (may still be using network, so avoid tests failing)
						network.close();
					} catch (Exception ex) {
						this.getLog().warn("Failed to remove docker newtork " + this.dockerNetworkName, ex);
					}
				}
			}
		};
		StopSamMojo.setStop(stop);

		// Ensure clean up
		Runtime.getRuntime().addShutdownHook(new Thread(stop));
	}

}