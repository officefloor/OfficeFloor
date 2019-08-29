/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.maven.woof;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;

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
	 * <a href="http://openjfx.org">OpenJFX</a> install.
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
		try {
			try (URLClassLoader classLoader = WarAwareUrlClassLoader
					.create(classPathUrls.toArray(new URL[classPathUrls.size()]))) {

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

			} catch (IOException ex) {
				throw new MojoExecutionException("Failed to close class loader", ex);
			}
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed class loader", ex);
		}
	}

	/**
	 * {@link URLClassLoader} that is aware of WAR files.
	 */
	private static class WarAwareUrlClassLoader extends URLClassLoader {

		/**
		 * Creates the {@link WarAwareUrlClassLoader}.
		 * 
		 * @param urls {@link URL} instances.
		 * @return {@link WarAwareUrlClassLoader}.
		 * @throws Exception If fails to create {@link WarAwareUrlClassLoader}.
		 */
		public static WarAwareUrlClassLoader create(URL[] urls) throws Exception {

			// Create the listing of URLs
			List<URL> classPathUrls = new ArrayList<URL>();

			// Load the URLs
			NEXT_URL: for (URL url : urls) {

				// Determine if WAR file
				if (!url.getPath().toLowerCase().endsWith(".war")) {
					classPathUrls.add(url);
					continue NEXT_URL; // not war file
				}

				// Create functions to support extracting jars
				Path tempDirectory = Files.createTempDirectory(url.getPath().replace('.', '_'));
				registerCleanupPath(tempDirectory);
				Files.createTempFile(tempDirectory, prefix, suffix, attrs)

				// Decompose the WAR file into parts to enable URL class loader to work
				ZipFile war = new ZipFile(new File(url.toURI()));
				war.stream().forEach((entry) -> {

					// TODO REMOVE
					System.out.println("TODO war entry: " + entry.getName());

				});
			}

			// Return the class loader
			return new WarAwareUrlClassLoader(classPathUrls.toArray(new URL[classPathUrls.size()]));
		}

		private static List<Path> pathsToDelete = null;

		private static synchronized void registerCleanupPath(Path path) {

			// Determine if hook to clean up
			if (pathsToDelete == null) {
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					// Delete files then directories
					Collections.reverse(pathsToDelete);
					for (Path deletePath : pathsToDelete) {
						try {
							Files.deleteIfExists(deletePath);
						} catch (IOException ex) {
							// Best effort to delete
						}
					}
				}));
				pathsToDelete = new LinkedList<>();
			}

			// Add path for deletion
			pathsToDelete.add(path);
		}

		/**
		 * Instantiate.
		 * 
		 * @param urls {@link URL} instances.
		 */
		private WarAwareUrlClassLoader(URL[] urls) {
			super(urls, null);
		}
	}

}