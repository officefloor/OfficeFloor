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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

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

			// Replace the tags
			template = template.replace("ARTIFACT_ID", artifactId);
			template = template.replace("DESCRIPTION",
					((description != null) && (description.trim().length() > 0)) ? description : artifactId);

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
		try {

			// Build the process
			ProcessBuilder builder = new ProcessBuilder("sam", "build");
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

			// Use dummy to avoid maven from re-building project
			// Note: this also avoids infinite loop of 'sam build' triggering this plugin
			Map<String, String> env = builder.environment();
			String path = env.get("PATH");
			String targetFirstPath = mvnExecutable.getParentFile().getAbsolutePath() + ":" + path;
			env.put("PATH", targetFirstPath);

			// Start the process (and direct output to log)
			Process process = builder.start();
			new Thread(() -> {
				StringBuilder line = new StringBuilder();
				try (Reader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					for (int character = reader.read(); character != -1; character = reader.read()) {
						switch (character) {
						case '\n':
							// End of line, so log the line
							this.getLog().info(line.toString());
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
			}).start();

			// Confirm success of process
			int status = process.waitFor();
			if (status != 0) {
				throw new MojoExecutionException("Process exited with status " + status);
			}

		} catch (Exception ex) {
			throw new MojoExecutionException("Failure in process", ex);
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
	}

}