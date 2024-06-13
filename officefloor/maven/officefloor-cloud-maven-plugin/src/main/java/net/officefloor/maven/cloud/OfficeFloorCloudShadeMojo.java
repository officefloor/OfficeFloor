package net.officefloor.maven.cloud;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.Shader;
import org.apache.maven.plugins.shade.mojo.ShadeMojo;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.apache.maven.plugins.shade.resource.ServicesResourceTransformer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * Builds the additional cloud specific jars attaching them with classifiers.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "shade", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class OfficeFloorCloudShadeMojo extends ShadeMojo {

	/**
	 * Parent {@link ShadeMojo} {@link Class}.
	 */
	private final Class<?> shadeMojoClass = this.getClass().getSuperclass();

	/**
	 * {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject _project;

	/**
	 * {@link Shader}.
	 */
	@Component
	private Shader shader;

	@Inject
	private MavenProjectHelper projectHelper;

	/**
	 * Obtains the {@link Field} from {@link ShadeMojo} parent.
	 * 
	 * @param fieldName Name of {@link Field}.
	 * @return {@link Field}.
	 * @throws MojoExecutionException If {@link Field} no longer in
	 *                                {@link ShadeMojo}.
	 */
	protected Field getShadeField(String fieldName) throws MojoExecutionException {
		Field field;
		try {
			field = this.shadeMojoClass.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new MojoExecutionException(
					this.shadeMojoClass.getSimpleName() + " has changed and no longer has field " + fieldName);
		}
		field.setAccessible(true);
		return field;
	}

	/**
	 * Obtains the {@link ShadeMojo} {@link Field} value.
	 * 
	 * @param <V>       {@link Field} type.
	 * @param fieldName Name of {@link Field}.
	 * @return {@link Field} value.
	 * @throws MojoExecutionException If unable to get {@link Field} value.
	 */
	protected <V> V getShadeFieldValue(String fieldName) throws MojoExecutionException {
		Field field = this.getShadeField(fieldName);
		try {
			return (V) field.get(this);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new MojoExecutionException(
					"Unable to get value for " + this.shadeMojoClass.getSimpleName() + "#" + fieldName, ex);
		}
	}

	/**
	 * Sets the {@link ShadeMojo} {@link Field} value.
	 * 
	 * @param <V>       {@link Field} type.
	 * @param fieldName Name of {@link Field}.
	 * @return {@link Field} value.
	 * @throws MojoExecutionException If unable to get {@link Field} value.
	 */
	protected <V> void setShadeFieldValue(String fieldName, V value) throws MojoExecutionException {
		Field field = this.getShadeField(fieldName);
		try {
			field.set(this, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new MojoExecutionException(
					"Unable to set value for " + this.shadeMojoClass.getSimpleName() + "#" + fieldName, ex);
		}
	}

	@Override
	public void execute() throws MojoExecutionException {

		// Ensure have services resource transformer
		ResourceTransformer[] transformers = this.getShadeFieldValue("transformers");
		if (transformers == null) {
			transformers = new ResourceTransformer[0];
		}
		boolean isIncluded = Arrays.asList(transformers).stream()
				.anyMatch((transformer) -> ServicesResourceTransformer.class.isAssignableFrom(transformer.getClass()));
		if (!isIncluded) {
			// Include services resource transfomer
			transformers = Arrays.copyOf(transformers, transformers.length + 1);
			transformers[transformers.length - 1] = new ServicesResourceTransformer();
			this.setShadeFieldValue("transformers", transformers);
		}

		// Undertake shading of the component
		this.getLog().info("Shading project");
		super.execute();

		// Obtain the project attachments
		for (Artifact artifact : this._project.getAttachedArtifacts()) {
			this.getLog().info("ATTACHED ARTIFACT: " + artifact);
		}

		// Obtain the final jar
		String finalName = this._project.getBuild().getFinalName();
		String targetDirectory = this._project.getBuild().getDirectory();
		File projectJar = new File(targetDirectory + "/" + finalName);

		// Obtain the test artifacts
		List<String> testClassPathEntries;
		try {
			testClassPathEntries = this._project.getTestClasspathElements();
		} catch (Exception ex) {
			throw new MojoExecutionException(ex);
		}

		// Iterate over class path entries finding cloud providers
		for (String testClassPathEntry : testClassPathEntries) {

			// Determine the artifacts for test
			
			
			System.out.println("\t" + testClassPathEntry);
			if (true) continue;

			// Determine the cloud name
			String cloudName = "test";

			// Shade the jar
			ShadeRequest request = new ShadeRequest();
			File shadeJar = null;
			// request.setJars(jars);
			request.setUberJar(shadeJar);
			request.setFilters(Collections.emptyList());
			request.setResourceTransformers(Arrays.asList(new ServicesResourceTransformer()));
			request.setRelocators(Collections.emptyList());
			try {
				this.shader.shade(request);
			} catch (IOException ex) {
				throw new MojoExecutionException(ex.getMessage(), ex);
			}

			// Attach the cloud jar
			this.projectHelper.attachArtifact(this._project, this._project.getArtifact().getType(), cloudName,
					shadeJar);
		}
	}

}
