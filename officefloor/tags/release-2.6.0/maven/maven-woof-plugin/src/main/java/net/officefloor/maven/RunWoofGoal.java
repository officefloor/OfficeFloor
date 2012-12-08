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

import java.io.File;
import java.util.List;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.war.WarOfficeFloorDecorator;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
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
	 * {@link PlexusContainer}.
	 * 
	 * @component
	 */
	private PlexusContainer plexusContainer;

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

		// Determine if OfficeBuilding is running
		if (OfficeBuildingManager
				.isOfficeBuildingAvailable(null,
						StartOfficeBuildingGoal.DEFAULT_OFFICE_BUILDING_PORT
								.intValue(), StartOfficeBuildingGoal
								.getKeyStoreFile(),
						StartOfficeBuildingGoal.KEY_STORE_PASSWORD,
						StartOfficeBuildingGoal.USER_NAME,
						StartOfficeBuildingGoal.PASSWORD)) {

			// OfficeBuilding running, so shutdown (allow clean start)
			StopOfficeBuildingGoal.createStopOfficeBuildingGoal(
					StartOfficeBuildingGoal.DEFAULT_OFFICE_BUILDING_PORT, null,
					this.getLog()).execute();
		}

		// Create goal to start the OfficeBuilding
		StartOfficeBuildingGoal startGoal = StartOfficeBuildingGoal
				.createStartOfficeBuildingGoal(this.project,
						this.pluginDependencies, this.localRepository,
						this.plexusContainer, this.wagonProvider, this.getLog());

		// Include officeplugin_war on class path (handle WoOF resources)
		startGoal.includePluginDependencyToOfficeBuildingClassPath(
				WarOfficeFloorDecorator.PLUGIN_WAR_GROUP_ID,
				WarOfficeFloorDecorator.PLUGIN_WAR_ARTIFACT_ID, "jar", null);

		// Start the OfficeBuilding
		startGoal.execute();

		// Create the open goal and configure for WoOF
		OpenOfficeFloorGoal openGoal = OpenOfficeFloorGoal
				.createOfficeFloorGoal("maven-woof-plugin", this.project,
						this.pluginDependencies, this.officeFloorLocation,
						this.getLog());
		openGoal.setOfficeFloorSource(WoofOfficeFloorSource.class.getName());

		/*
		 * Include the web app directory as class path entry so that may run
		 * without requiring packaging.
		 */
		File baseDir = this.project.getBasedir();
		File webAppDir = new File(baseDir, "src/main/webapp");
		openGoal.addClassPathEntry(webAppDir.getAbsolutePath());

		/*
		 * Include resulting pre-WAR directory. This allows inclusion of GWT
		 * generated content.
		 */
		Build build = this.project.getBuild();
		String assemblePath = build.getDirectory() + "/" + build.getFinalName();
		File assembleDir = new File(assemblePath);
		if (!(assembleDir.exists())) {
			this.getLog()
					.warn("No GWT functionality will be available.  Please package project to create content in "
							+ assembleDir.getAbsolutePath());
		}
		try {
			File assembleContent = WarOfficeFloorDecorator
					.generateJarMinusMetaAndWebInf(assembleDir);
			if (assembleContent == null) {
				this.getLog()
						.warn("No GWT functionality will be available.  Packaged content is not in a WAR structure");

			} else {
				// Include the assemble content (GWT content)
				openGoal.addClassPathEntry(assembleContent.getAbsolutePath());
			}
		} catch (Exception ex) {
			this.getLog()
					.warn("No GWT functionality will be available.  Failed to generate package JAR content",
							ex);
		}

		// Indicate WoOF application running
		this.getLog().info("Starting WoOF application");

		// Open the WoOF
		openGoal.execute();

		// Indicate WoOF application running
		this.getLog().info("WoOF application running");
	}

}