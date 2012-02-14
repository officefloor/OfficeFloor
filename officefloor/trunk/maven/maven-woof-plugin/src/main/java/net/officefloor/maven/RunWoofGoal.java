/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.connector.wagon.WagonProvider;

/**
 * Maven goal to run the {@link WoofOfficeFloorSource}.
 * 
 * @goal run
 * @requiresDependencyResolution compile
 * 
 * @author Daniel Sagenschneider
 */
public class RunWoofGoal extends AbstractMojo {

	/**
	 * {@link MavenProject}.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	/**
	 * {@link WagonProvider}.
	 * 
	 * @component
	 */
	private WagonProvider wagonProvider;

	/**
	 * Plug-in dependencies.
	 * 
	 * @parameter expression="${plugin.artifacts}"
	 * @required
	 */
	private List<Artifact> pluginDependencies;

	/**
	 * Local repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @required
	 */
	private ArtifactRepository localRepository;

	/**
	 * Path to the {@link OfficeFloor} configuration.
	 * 
	 * @parameter
	 */
	private String officeFloorLocation = "WoOF";

	/*
	 * ======================= Mojo =========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure the OfficeBuilding is available
		StartOfficeBuildingGoal.ensureDefaultOfficeBuildingAvailable(
				this.project, this.pluginDependencies, this.localRepository,
				this.wagonProvider, this.getLog());

		// Create the open goal and configure for WoOF
		OpenOfficeFloorGoal openGoal = OpenOfficeFloorGoal
				.createOfficeFloorGoal("maven-woof-plugin", this.project,
						this.pluginDependencies, this.officeFloorLocation,
						this.getLog());
		openGoal.setOfficeFloorSource(WoofOfficeFloorSource.class.getName());

		// Indicate WoOF application running
		this.getLog().info("Application running");

		// Open the WoOF
		openGoal.execute();
	}

}