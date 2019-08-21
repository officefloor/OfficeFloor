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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.woof.WoofLoaderExtensionService;

/**
 * Enables viewing WoOF configurations.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "view", requiresProject = false, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ViewWoofMojo extends AbstractMojo {

	/**
	 * {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	/**
	 * Possible configured {@link Artifact} to be resolved.
	 */
	@Parameter(required = false, defaultValue = "")
	private String artifact;

	@Component
	private RepositorySystem repositorySystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	private RepositorySystemSession repositorySystemSession;

	/**
	 * Path to configuration within the {@link MavenProject} / {@link Artifact}.
	 */
	@Parameter(defaultValue = WoofLoaderExtensionService.APPLICATION_WOOF)
	private String path = WoofLoaderExtensionService.APPLICATION_WOOF;

	/*
	 * =================== AbstractMojo =================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Obtain the configuration to view
		ConfigurationItem configuration = null;

		// Determine if specified the artifact
		if (!CompileUtil.isBlank(this.artifact)) {

			// Attempt to resolve the artifact
			
		}

		// Determine if load from current project (no artifact specified)
		if (configuration == null) {

			// Ensure have current project
			if (this.project.getFile() == null) {
				throw new MojoFailureException("Must specify artifact or execute within project");
			}

			// Within project, so find on class path
			ClassLoader classLoader;
			try {
				List<URL> urls = new ArrayList<>();
				for (String classPathEntry : this.project.getRuntimeClasspathElements()) {
					urls.add(new File(classPathEntry).toURI().toURL());
				}
				classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
			} catch (DependencyResolutionRequiredException | MalformedURLException ex) {
				throw new MojoExecutionException("Failed to obtain class path from project", ex);
			}

			// TODO load from class path
		}
	}

}