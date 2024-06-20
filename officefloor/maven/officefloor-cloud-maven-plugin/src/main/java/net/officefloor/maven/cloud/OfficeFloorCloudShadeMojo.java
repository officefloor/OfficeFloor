package net.officefloor.maven.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.inject.Inject;

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
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;

/**
 * Builds the additional cloud specific jars attaching them with classifiers.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "shade", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class OfficeFloorCloudShadeMojo extends ShadeMojo {
	
	/**
	 * Path to cloud artifact properties in the test artifact dependency.
	 */
	public static final String CLOUD_ARTIFACT_PROPERTIES_PATH = "META-INF/cloud/cloud-artifact.properties";

	/**
	 * Path to cloud meta-data properties within the cloud artifact dependency.
	 */
	public static final String CLOUD_META_DATA_PROPERTIES_PATH = "META-INF/cloud/cloud.properties";
	
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

	@Component
	private RepositorySystem repositorySystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	private RepositorySystemSession repositorySystemSession;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	private List<RemoteRepository> remoteRepositories;

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
	@SuppressWarnings("unchecked")
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
		for (org.apache.maven.artifact.Artifact artifact : this._project.getAttachedArtifacts()) {
			this.getLog().info("ATTACHED ARTIFACT: " + artifact);
		}

		// Obtain the final jar
		String finalName = this._project.getBuild().getFinalName();
		String packaging = this._project.getPackaging();
		String targetDirectory = this._project.getBuild().getDirectory();
		File projectJar = new File(targetDirectory + "/" + finalName + "." + packaging);
		if (!projectJar.exists()) {
			throw new MojoExecutionException("After shading project jar, failed to find it at " + projectJar.getAbsolutePath());
		}

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
			Properties cloudArtifactProperties = this.getProperties(new File(testClassPathEntry), CLOUD_ARTIFACT_PROPERTIES_PATH);
			if (cloudArtifactProperties == null) {
				continue; // no artifact properties to create deployment
			}
			
			// Resolve dependency
			String groupId = cloudArtifactProperties.getProperty("groupId");
			String artifactId = cloudArtifactProperties.getProperty("artifactId");
			String version = cloudArtifactProperties.getProperty("version");
			String classifier = cloudArtifactProperties.getProperty("classifier");
			String extension = cloudArtifactProperties.getProperty("extension");
			Artifact artifact = this.resolveArtifact(groupId, artifactId, version, classifier, extension);
			
			// Obtain the cloud properties
			File artifactFile = artifact.getFile();
			Properties cloudMetaDataProperties = this.getProperties(artifactFile, CLOUD_META_DATA_PROPERTIES_PATH);
			String cloudClassifier = cloudMetaDataProperties.getProperty("classifier");
			
			// Resolve dependencies for the cloud artifact
			Set<File> cloudJars = new HashSet<>();
			cloudJars.add(projectJar); // Include the project content
			cloudJars.add(artifactFile); // Include cloud dependencies
			cloudJars.addAll(this.resolveDependencies(artifact)); // Include further dependencies
						
			// Determine the Uber JAR file
			File uberCloudJarFile = new File(targetDirectory, finalName + "-" + classifier + ".jar");

			// Shade the cloud specific jar
			ShadeRequest request = new ShadeRequest();
			request.setUberJar(uberCloudJarFile);
			request.setJars(cloudJars);
			request.setFilters(Collections.emptyList());
			request.setResourceTransformers(Arrays.asList(new ServicesResourceTransformer()));
			request.setRelocators(Collections.emptyList());
			try {
				this.shader.shade(request);
			} catch (IOException ex) {
				throw new MojoExecutionException(ex.getMessage(), ex);
			}

			// Attach the cloud jar
			this.projectHelper.attachArtifact(this._project, this._project.getArtifact().getType(), cloudClassifier,
					uberCloudJarFile);
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
	
	protected Artifact resolveArtifact(String groupId, String artifactId, String version, String classifier, String extension) throws MojoExecutionException {

		// Create the artifact
		DefaultArtifact searchArtifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version);
		
		// Attempt to resolve the artifact
		ArtifactResult result;
		try {
			ArtifactRequest request = new ArtifactRequest();
			request.setArtifact(searchArtifact);
			request.setRepositories(this.remoteRepositories);
			result = this.repositorySystem.resolveArtifact(this.repositorySystemSession, request);
		} catch (ArtifactResolutionException ex) {
			throw new MojoExecutionException("Failed to resolve artifact " + searchArtifact, ex);
		}
		
		// Return the artifact
		return this.getArtifact(result, "artifact");
	}

	protected List<File> resolveDependencies(Artifact artifact) throws MojoExecutionException {
		
		// Attempt to resolve the dependencies
		DependencyResult result;
		try {
			CollectRequest collectRequest = new CollectRequest();
			
			collectRequest.setRepositories(this.remoteRepositories);
			DependencyRequest dependencyRequest = new DependencyRequest();
			dependencyRequest.setCollectRequest(collectRequest);
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
		List<File> dependencies = new ArrayList<>(artifactResults.size());
		for (ArtifactResult artifactResult : artifactResults) {
			dependencies.add(this.getArtifact(artifactResult, "dependency").getFile());
		}
		
		// Return the dependencies
		return dependencies;
	}
	
	protected Artifact getArtifact(ArtifactResult result, String type) throws MojoExecutionException {

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
