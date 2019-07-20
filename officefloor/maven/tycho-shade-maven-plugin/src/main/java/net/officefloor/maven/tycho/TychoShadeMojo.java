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
package net.officefloor.maven.tycho;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.eclipse.tycho.classpath.ClasspathEntry;
import org.eclipse.tycho.compiler.AbstractOsgiCompilerMojo;

/**
 * {@link Mojo} for shading a tycho project.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "shade", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class TychoShadeMojo extends AbstractOsgiCompilerMojo {

	@Component
	private Shader shader;

	@Component(hint = "jar")
	private Archiver archiver;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject mavenProject;

	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File target;

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

		// Create the exclude patterns
		List<Pattern> excludePatterns = new ArrayList<>(this.excludes.size());
		for (String exclude : this.excludes) {
			excludePatterns.add(Pattern.compile(exclude));
		}

		// Load the jars for shading
		Set<File> jars = new HashSet<>();
		File directoryArchive = null;
		for (ClasspathEntry entry : this.getClasspath()) {
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
		File shadeJar = new File(this.target, this.mavenProject.getBuild().getFinalName() + ".jar");
		this.getLog().info("Shading jar: " + shadeJar.getAbsolutePath());

		// Log the inclusions
		this.getLog().debug("Tycho shade resources:");

		// Shade the jar
		ShadeRequest request = new ShadeRequest();
		request.setJars(jars);
		request.setUberJar(shadeJar);
		request.setFilters(Arrays.asList(new ResourceFilter()));
		request.setResourceTransformers(Collections.emptyList());
		request.setRelocators(Collections.emptyList());
		try {
			this.shader.shade(request);
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