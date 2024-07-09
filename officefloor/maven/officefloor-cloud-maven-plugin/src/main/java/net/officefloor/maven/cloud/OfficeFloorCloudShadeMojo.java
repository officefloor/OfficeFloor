package net.officefloor.maven.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.shade.mojo.ShadeMojo;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.apache.maven.plugins.shade.resource.ServicesResourceTransformer;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;

/**
 * Builds the additional cloud specific jars attaching them with classifiers.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "shade", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST)
public class OfficeFloorCloudShadeMojo extends ShadeMojo {

	/**
	 * Path to cloud artifact properties in the test artifact dependency.
	 */
	public static final String CLOUD_ARTIFACT_PROPERTIES_PATH = "META-INF/cloud/cloud-artifact.properties";

	/**
	 * Path to cloud meta-data properties within the cloud artifact dependency.
	 */
	public static final String CLOUD_META_DATA_PROPERTIES_PATH = "META-INF/cloud/cloud.properties";

	@Component
	private RepositorySystem repositorySystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	private RepositorySystemSession repositorySystemSession;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	private List<RemoteRepository> remoteRepositories;

	@Override
	public void execute() throws MojoExecutionException {

		// Obtain the project
		MavenProject project = this.getFieldValue(ShadeMojo.class, "project", this);

		// Ensure have services resource transformer
		ResourceTransformer[] transformers = this.getFieldValue(ShadeMojo.class, "transformers", this);
		if (transformers == null) {
			transformers = new ResourceTransformer[0];
		}
		if (!Arrays.asList(transformers).stream().anyMatch(
				(transformer) -> ServicesResourceTransformer.class.isAssignableFrom(transformer.getClass()))) {
			// Include services resource transformer
			transformers = Arrays.copyOf(transformers, transformers.length + 1);
			transformers[transformers.length - 1] = new ServicesResourceTransformer();
			this.setFieldValue(ShadeMojo.class, "transformers", this, transformers);
		}

		// Obtain the resolved artifacts
		Set<Artifact> originalResolvedArtifacts = this.getFieldValue(MavenProject.class, "resolvedArtifacts", project);
		try {

			// Obtain the runtime resolved artifacts
			ScopeArtifactFilter runtimeFilter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
			List<Artifact> runtimeResolvedArtifacts = originalResolvedArtifacts.stream().filter(runtimeFilter::include)
					.toList();
			this.getLog().debug("Project runtime dependencies " + String.join(", ", runtimeResolvedArtifacts.stream()
					.map((artifact) -> artifact.getFile().getAbsolutePath()).toList()));

			// Iterate over artifacts looking for cloud artifacts
			for (Artifact projectDependency : originalResolvedArtifacts) {

				// Determine the artifacts for test
				File projectDependencyFile = projectDependency.getFile();
				Properties cloudArtifactProperties = this.getProperties(projectDependencyFile,
						CLOUD_ARTIFACT_PROPERTIES_PATH);
				if (cloudArtifactProperties == null) {
					continue; // no artifact properties to create deployment
				}
				this.getLog().debug("Found cloud configuration in " + projectDependencyFile.getAbsolutePath());

				// Resolve dependency
				String groupId = cloudArtifactProperties.getProperty("groupId");
				String artifactId = cloudArtifactProperties.getProperty("artifactId");
				String version = cloudArtifactProperties.getProperty("version");
				String classifier = cloudArtifactProperties.getProperty("classifier");
				String extension = cloudArtifactProperties.getProperty("extension");
				if ((extension == null) || (extension.isEmpty())) {
					extension = "jar";
				}
				DefaultArtifact unresolvedArtifact = new DefaultArtifact(groupId, artifactId, classifier, extension,
						version);
				this.getLog().debug("Providing cloud build from " + unresolvedArtifact);
				org.eclipse.aether.artifact.Artifact cloudArtifact = this.resolveArtifact(unresolvedArtifact);

				// Obtain the cloud properties
				Properties cloudMetaDataProperties = this.getProperties(cloudArtifact.getFile(),
						CLOUD_META_DATA_PROPERTIES_PATH);
				String cloudName = cloudMetaDataProperties.getProperty("name");
				String cloudClassifier = cloudMetaDataProperties.getProperty("classifier");
				this.getLog().info("Shading " + cloudName + " cloud deployment with classifier " + cloudClassifier);

				// Resolve dependencies for the cloud artifact
				List<Artifact> cloudResolvedDependencies = this.resolveDependencies(cloudArtifact).stream()
						.map(RepositoryUtils::toArtifact).toList();
				this.getLog().debug("Cloud resolved dependencies " + String.join(", ", cloudResolvedDependencies
						.stream().map((artifact) -> artifact.getFile().getAbsolutePath()).toList()));

				// Obtain the runtime artifacts for cloud implementation
				Set<Artifact> cloudArtifacts = new HashSet<>();
				cloudArtifacts.addAll(runtimeResolvedArtifacts);
				cloudArtifacts.addAll(cloudResolvedDependencies);

				// Undertaking shading for cloud solution
				this.setFieldValue(MavenProject.class, "resolvedArtifacts", project, cloudArtifacts);
				this.setFieldValue(MavenProject.class, "artifacts", project, null); // clear cache to recalculate
				this.setFieldValue(ShadeMojo.class, "shadedArtifactAttached", this, true);
				this.setFieldValue(ShadeMojo.class, "shadedClassifierName", this, cloudClassifier);
				super.execute();
			}

		} finally {
			// Reset the artifacts
			this.setFieldValue(MavenProject.class, "resolvedArtifacts", project, originalResolvedArtifacts);
		}
	}

	/**
	 * Obtains the {@link Field}.
	 * 
	 * @param fieldName Name of {@link Field}.
	 * @return {@link Field}.
	 * @throws MojoExecutionException If {@link Field} no longer in {@link Class}.
	 */
	protected Field getField(Class<?> clazz, String fieldName) throws MojoExecutionException {
		Field field;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException ex) {
			throw new MojoExecutionException(
					clazz.getSimpleName() + " has changed and no longer has field " + fieldName);
		}
		field.setAccessible(true);
		return field;
	}

	/**
	 * Obtains the {@link Field} value.
	 * 
	 * @param <V>       {@link Field} type.
	 * @param fieldName Name of {@link Field}.
	 * @return {@link Field} value.
	 * @throws MojoExecutionException If unable to get {@link Field} value.
	 */
	@SuppressWarnings("unchecked")
	protected <V, O> V getFieldValue(Class<? super O> clazz, String fieldName, O target) throws MojoExecutionException {
		Field field = this.getField(clazz, fieldName);
		try {
			return (V) field.get(target);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new MojoExecutionException("Unable to get value for " + clazz.getSimpleName() + "#" + fieldName, ex);
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
	protected <V, O> void setFieldValue(Class<? super O> clazz, String fieldName, O target, V value)
			throws MojoExecutionException {
		Field field = this.getField(clazz, fieldName);
		try {
			field.set(target, value);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			throw new MojoExecutionException("Unable to set value for " + clazz.getSimpleName() + "#" + fieldName, ex);
		}
	}

	protected Properties getProperties(File file, String propertiesPath) throws MojoExecutionException {

		// Obtain based on type of path
		if (file.isDirectory()) {

			// Attempt to find in directory
			File cloudArtifactProperties = new File(file, propertiesPath);
			if (!cloudArtifactProperties.exists()) {
				return null; // no properties file
			}

			// Load and return the properties
			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(cloudArtifactProperties));
				return properties;

			} catch (Exception ex) {
				this.getLog().warn("Failed to load properties from " + cloudArtifactProperties.getAbsolutePath(), ex);
				return null; // no properties
			}

		} else if (file.exists()) {
			// Return properties from jar file
			try (JarFile jarFile = new JarFile(file)) {

				// Attempt to find in JAR file
				ZipEntry cloudArtifactPropertiesEntry = jarFile.getEntry(propertiesPath);
				if (cloudArtifactPropertiesEntry == null) {
					return null; // no properties file
				}

				// Load and return the properties
				try {
					Properties properties = new Properties();
					properties.load(jarFile.getInputStream(cloudArtifactPropertiesEntry));
					return properties;
				} catch (Exception ex) {
					this.getLog().warn("Failed to load properties from " + file.getAbsolutePath(), ex);
					return null; // no properties
				}

			} catch (Exception ex) {
				this.getLog().warn("Failed to read JAR file " + file.getAbsolutePath(), ex);
				return null; // no properties
			}

		} else {
			// Path not exist
			return null;
		}
	}

	protected org.eclipse.aether.artifact.Artifact resolveArtifact(
			org.eclipse.aether.artifact.Artifact unresolvedArtifact) throws MojoExecutionException {

		// Attempt to resolve the artifact
		ArtifactResult result;
		try {
			ArtifactRequest request = new ArtifactRequest(unresolvedArtifact, this.remoteRepositories, null);
			result = this.repositorySystem.resolveArtifact(this.repositorySystemSession, request);
		} catch (ArtifactResolutionException ex) {
			throw new MojoExecutionException("Failed to resolve artifact " + unresolvedArtifact, ex);
		}

		// Return the artifact
		return this.getArtifact(result, "artifact");
	}

	protected List<org.eclipse.aether.artifact.Artifact> resolveDependencies(
			org.eclipse.aether.artifact.Artifact artifact) throws MojoExecutionException {

		// Attempt to resolve the dependencies
		DependencyResult result;
		try {
			CollectRequest collectRequest = new CollectRequest(new Dependency(artifact, "runtime"),
					this.remoteRepositories);
			DependencyFilter filter = DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME);
			DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, filter);
			result = this.repositorySystem.resolveDependencies(this.repositorySystemSession, dependencyRequest);
		} catch (DependencyResolutionException ex) {
			throw new MojoExecutionException("Failed to resolve dependencies for " + artifact, ex);
		}

		// Determine if error
		for (Exception ex : result.getCollectExceptions()) {
			throw new MojoExecutionException("Failed to resolve dependencies for " + artifact, ex);
		}

		// Load the list of artifact files
		List<ArtifactResult> artifactResults = result.getArtifactResults();
		List<org.eclipse.aether.artifact.Artifact> dependencies = new ArrayList<>(artifactResults.size());
		for (ArtifactResult artifactResult : artifactResults) {
			dependencies.add(this.getArtifact(artifactResult, "dependency"));
		}

		// Return the dependencies
		return dependencies;
	}

	protected org.eclipse.aether.artifact.Artifact getArtifact(ArtifactResult result, String type)
			throws MojoExecutionException {

		// Ensure have artifact
		if (result.isMissing()) {
			throw new MojoExecutionException("Did not find " + type + " " + result.getArtifact());
		}

		// Ensure resolved
		if (!result.isResolved()) {

			// Log the exceptions in failing to resolve
			for (Exception resolutionException : result.getExceptions()) {
				this.getLog().error("Failure in resolving " + type + " " + result.getArtifact(), resolutionException);
			}

			// Fail as must resolve artifact
			throw new MojoExecutionException("Failed to resolve " + type + " " + result.getArtifact());
		}

		// Return the artifact
		return result.getArtifact();
	}

}
