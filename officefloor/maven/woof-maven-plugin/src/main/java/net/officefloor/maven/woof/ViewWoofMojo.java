package net.officefloor.maven.woof;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.web.war.WarAwareClassLoaderFactory;
import net.officefloor.woof.WoofLoaderExtensionService;

/**
 * Enables viewing WoOF configurations.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "view", requiresProject = false, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ViewWoofMojo extends AbstractMojo {

	/**
	 * {@link PluginDescriptor} for {@link ViewWoofMojo}.
	 */
	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor plugin;

	/**
	 * Possible configured {@link Artifact} to be resolved.
	 */
	@Parameter(property = "artifact", required = false, defaultValue = "")
	private String artifact;

	@Component
	private RepositorySystem repositorySystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	private RepositorySystemSession repositorySystemSession;

	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	private List<RemoteRepository> remoteRepositories;

	/**
	 * {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * <p>
	 * Directory containing the JavaFX installed jars. This allows specifying a
	 * different JavaFX version (for a version not included in plugin).
	 * <p>
	 * Typically, this is the lib directory of the
	 * <a href="http://openjfx.io">OpenJFX</a> install.
	 */
	@Parameter(property = "javafx.lib")
	private File javaFxLibDir = null;

	/**
	 * Path to configuration within the {@link MavenProject} / {@link Artifact}.
	 */
	@Parameter(property = "path", required = false, defaultValue = WoofLoaderExtensionService.APPLICATION_WOOF)
	private String path = WoofLoaderExtensionService.APPLICATION_WOOF;

	/*
	 * =================== AbstractMojo =================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Obtain the URLs
		List<URL> classPathUrls = new ArrayList<>();

		// Determine if specified the artifact
		if (!CompileUtil.isBlank(this.artifact)) {

			// Attempt to resolve the artifact
			ArtifactResult result;
			try {
				ArtifactRequest request = new ArtifactRequest();
				request.setArtifact(new DefaultArtifact(this.artifact));
				request.setRepositories(this.remoteRepositories);
				result = this.repositorySystem.resolveArtifact(this.repositorySystemSession, request);
			} catch (ArtifactResolutionException ex) {
				throw new MojoExecutionException("Failed to resolve artifact " + this.artifact, ex);
			}

			// Determine if error
			for (Exception ex : result.getExceptions()) {
				throw new MojoExecutionException("Failed to resolve artifact " + this.artifact, ex);
			}

			// Ensure have artifact
			if (result.isMissing()) {
				throw new MojoExecutionException("Did not find artifact " + this.artifact);
			}

			// Obtain the artifact
			File artifactFile = result.getArtifact().getFile();

			// Load the artifact URL
			URL artifactUrl;
			try {
				artifactUrl = artifactFile.toURI().toURL();
			} catch (MalformedURLException ex) {
				throw new MojoExecutionException(
						"Failed to obtain class path from artifact: " + this.artifact + " (" + artifactFile + ")", ex);
			}

			// Include the URL
			classPathUrls.add(artifactUrl);
		}

		// Determine if load from current project (no artifact specified)
		if (classPathUrls.size() == 0) {

			// Ensure have current project
			if (this.project.getFile() == null) {
				throw new MojoFailureException("Must specify artifact or execute within project");
			}

			// Within project, so find on class path
			List<URL> projectClassPath = new ArrayList<URL>();
			try {
				for (String classPathEntry : this.project.getRuntimeClasspathElements()) {
					projectClassPath.add(new File(classPathEntry).toURI().toURL());
				}
			} catch (DependencyResolutionRequiredException | MalformedURLException ex) {
				throw new MojoExecutionException("Failed to obtain class path from project", ex);
			}

			// Include non-JavaFX project entries
			try {
				classPathUrls.addAll(Arrays.asList(JavaFxFacet
						.getClassPathEntries(projectClassPath.toArray(new URL[projectClassPath.size()]), false, null)));
			} catch (Exception ex) {
				throw new MojoExecutionException("Failed filtering out JavaFX from project class path", ex);
			}
		}

		// Load the plugin dependencies
		try {
			for (URL entryUrl : JavaFxFacet.getClassPathEntries(this.plugin.getClassRealm().getURLs(), true,
					this.javaFxLibDir)) {
				classPathUrls.add(entryUrl);
			}
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed to load JavaFX libraries", ex);
		}

		// Create the class loader
		ClassLoader classLoader;
		try {
			classLoader = new WarAwareClassLoaderFactory()
					.createClassLoader(classPathUrls.toArray(new URL[classPathUrls.size()]));
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed class loader", ex);
		}

		// Obtain the viewer class name
		String viewerClassName = ViewWoofMojo.class.getPackage().getName() + ".Viewer";

		// Load the viewer
		Class<?> viewerClass;
		try {
			viewerClass = classLoader.loadClass(viewerClassName);
		} catch (ClassNotFoundException ex) {
			throw new MojoExecutionException(
					"Failed to find class " + Viewer.class.getName() + " in constructed class path", ex);
		}

		// Obtain run method to Viewer
		final String methodName = "main";
		Method run;
		try {
			run = viewerClass.getMethod(methodName, String[].class);
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new MojoExecutionException(
					"Unable to extract " + methodName + " method from " + Viewer.class.getName(), ex);
		}

		// Ensure context class path configured for JavaFX application launch
		Thread.currentThread().setContextClassLoader(classLoader);

		// Run the Viewer
		try {
			run.invoke(null, (Object) new String[] { this.path });
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new MojoExecutionException("Unable to start " + Viewer.class.getName(), ex);
		}
	}

}