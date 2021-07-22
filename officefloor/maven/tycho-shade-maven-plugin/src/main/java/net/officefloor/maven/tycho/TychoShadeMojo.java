/*-
 * #%L
 * Tycho creation of uber jar
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.maven.tycho;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.shade.ShadeRequest;
import org.apache.maven.plugins.shade.Shader;
import org.apache.maven.plugins.shade.filter.Filter;
import org.apache.maven.plugins.shade.resource.ServicesResourceTransformer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.eclipse.tycho.classpath.ClasspathEntry;
import org.eclipse.tycho.compiler.AbstractOsgiCompilerMojo;
import org.eclipse.tycho.compiler.OsgiCompilerMojo;
import org.eclipse.tycho.core.TychoProject;

/**
 * {@link Mojo} for shading a tycho project.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "shade", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class TychoShadeMojo extends AbstractMojo {

	/**
	 * {@link OsgiCompilerMojo}.
	 */
	private final OsgiCompilerMojo osgiCompiler = new OsgiCompilerMojo() {
	};

	/**
	 * Sets the {@link Field} on the {@link OsgiCompilerMojo}.
	 * 
	 * @param declaringClass Declaring {@link Class}.
	 * @param fieldName      Name of {@link Field}.
	 * @param value          Value for {@link Field}.
	 * @throws MojoFailureException If fails to load value.
	 */
	private void setOsgiCompilerField(Class<?> declaringClass, String fieldName, Object value)
			throws MojoFailureException {
		try {
			Field field = declaringClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this.osgiCompiler, value);
		} catch (Exception ex) {
			throw new MojoFailureException("Failed to set OSGI Compiler field " + fieldName, ex);
		}
	}

	/**
	 * Tycho.
	 */
	@Parameter(property = "project", readonly = true)
	private MavenProject project;

	@Component(role = TychoProject.class)
	private Map<String, TychoProject> projectTypes;

	/**
	 * {@link Shader}.
	 */
	@Component
	private Shader shader;

	/**
	 * {@link JarArchiver}.
	 */
	@Component(hint = "jar")
	private Archiver archiver;

	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String finalName;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File target;

	@Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
	private File classes;

	/**
	 * List of artifactId's to exclude from shading. This allows avoiding artifacts
	 * that contain jars (that just bloat shaded jar).
	 */
	@Parameter
	private Set<String> excludes = new HashSet<>();

	/**
	 * List of resource names to exclude from shading.
	 */
	@Parameter
	private Set<String> excludeResources = new HashSet<>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Log the shading
		this.getLog().debug("Tycho shade classpath:");

		// Set up the OSGi compiler
		this.setOsgiCompilerField(AbstractOsgiCompilerMojo.class, "project", this.project);
		this.setOsgiCompilerField(AbstractOsgiCompilerMojo.class, "projectTypes", this.projectTypes);

		// Create the exclude patterns
		List<Pattern> excludePatterns = new ArrayList<>(this.excludes.size());
		for (String exclude : this.excludes) {
			excludePatterns.add(Pattern.compile(exclude));
		}

		// Load the jars for shading
		Set<File> jars = new HashSet<>();
		File directoryArchive = null;
		for (ClasspathEntry entry : this.osgiCompiler.getClasspath()) {
			NEXT_LOCACTION: for (File location : entry.getLocations()) {

				// Determine if filter
				for (Pattern excludePattern : excludePatterns) {
					String fileName = location.getName();
					if (excludePattern.matcher(fileName).matches()) {
						// Filter out artifact
						this.getLog().debug("  e " + location.getAbsolutePath() + " (not shading by filter "
								+ excludePattern.pattern() + ")");
						continue NEXT_LOCACTION;
					}
				}

				// Include directory in single archive
				if (location.isDirectory()) {
					this.getLog().debug("  d " + location.getAbsolutePath());

					// Lazy configure the directory archive jar
					if (directoryArchive == null) {
						File tempArea = new File(this.target, "tycho-shade");
						if (!tempArea.exists()) {
							tempArea.mkdirs();
						}
						directoryArchive = new File(tempArea, "DirectoriesArchive.jar");
						this.archiver.setDestFile(directoryArchive);
					}

					// Include the directory
					this.archiver.addFileSet(new DefaultFileSet(location));
					continue NEXT_LOCACTION;
				}

				// Shade the jar
				jars.add(location);
				this.getLog().debug("  + " + location.getAbsolutePath());
			}
		}

		// Determine if create archive
		if (directoryArchive != null) {
			try {
				this.archiver.createArchive();
			} catch (IOException ex) {
				throw new MojoFailureException("Failed to create directory archive for shading", ex);
			}

			// Include the directories into shading
			jars.add(directoryArchive);
		}

		// Determine the shade jar name
		File shadeJar = new File(this.target, this.finalName + "-tychoshade.jar");
		this.getLog().info("Shading jar: " + shadeJar.getAbsolutePath());

		// Log the inclusions
		this.getLog().debug("Tycho shade resources:");

		// Shade the jar
		ShadeRequest request = new ShadeRequest();
		request.setJars(jars);
		request.setUberJar(shadeJar);
		request.setFilters(Arrays.asList(new ResourceFilter()));
		request.setResourceTransformers(Arrays.asList(new ServicesResourceTransformer()));
		request.setRelocators(Collections.emptyList());
		try {
			this.shader.shade(request);
		} catch (IOException ex) {
			throw new MojoFailureException(ex.getMessage(), ex);
		}

		// Ensure have classes directory
		if (this.classes == null) {
			this.classes = new File(this.target, "classes");
		}

		// Extract to classes directory for packaging into jar
		Path classesPath = this.classes.toPath();
		try (JarFile jar = new JarFile(shadeJar)) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					Path entryPath = classesPath.resolve(entry.getName());
					Path parentPath = entryPath.getParent();
					if (!Files.exists(parentPath)) {
						Files.createDirectories(parentPath);
					}
					Files.copy(jar.getInputStream(entry), entryPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (IOException ex) {
			throw new MojoFailureException(ex.getMessage(), ex);
		}
	}

	private class ResourceFilter implements Filter {

		private final List<Pattern> excludePatterns;

		private ResourceFilter() {
			this.excludePatterns = new ArrayList<>(TychoShadeMojo.this.excludeResources.size());
			for (String exclude : TychoShadeMojo.this.excludeResources) {
				this.excludePatterns.add(Pattern.compile(exclude));
			}
		}

		@Override
		public boolean canFilter(File jar) {
			return true;
		}

		@Override
		public boolean isFiltered(String classFile) {

			// Determine if filter out
			for (Pattern exclude : this.excludePatterns) {
				if (exclude.matcher(classFile).matches()) {
					TychoShadeMojo.this.getLog()
							.debug("  - " + classFile + " (filtered out by " + exclude.pattern() + ")");
					return true;
				}
			}

			// Include
			TychoShadeMojo.this.getLog().debug("  + " + classFile);
			return false;
		}

		@Override
		public void finished() {
			// Nothing to tidy up
		}
	}

}
