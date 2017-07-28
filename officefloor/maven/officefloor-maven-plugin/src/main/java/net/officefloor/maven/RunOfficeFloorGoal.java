/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.maven;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystem;

import net.officefloor.OfficeFloorMain;

/**
 * Maven goal to run the {@link OfficeFloorMain}.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE)
public class RunOfficeFloorGoal extends AbstractGoal {

	/**
	 * {@link MavenProject}.
	 */
	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	/**
	 * {@link PlexusContainer}.
	 */
	@Component
	private PlexusContainer plexusContainer;

	/**
	 * {@link RepositorySystem}.
	 */
	@Component
	private RepositorySystem repositorySystem;

	/**
	 * Plug-in dependencies.
	 */
	@Parameter(defaultValue = "${plugin.artifacts}", required = true)
	private List<Artifact> pluginDependencies;

	/**
	 * Local repository.
	 */
	@Parameter(defaultValue = "${localRepository}", required = true)
	private ArtifactRepository localRepository;

	/*
	 * ======================= Mojo =========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// Run the OfficeFloor
			OfficeFloorMain.main();

		} catch (Exception ex) {
			throw newMojoExecutionException("Fail to run OfficeFloor", ex);
		}
	}

}