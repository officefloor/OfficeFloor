package net.officefloor.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Starts SAM for the integration testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "start")
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

	/*
	 * ================== AbstractMojo ========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure template file exists
		this.ensureTemplateYamlFileExists();

	}

}