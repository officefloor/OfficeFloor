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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Maven goal to open the {@link OfficeFloor}.
 * 
 * @goal open
 * @requiresDependencyResolution compile
 * 
 * @author Daniel Sagenschneider
 */
public class OpenOfficeFloorGoal extends AbstractGoal {

	/**
	 * Default process name.
	 */
	public static final String DEFAULT_PROCESS_NAME = "maven-officefloor-plugin";

	/**
	 * Creates the {@link OpenOfficeFloorGoal} with the required parameters.
	 * 
	 * @param project
	 *            {@link MavenProject}.
	 * @param pluginDependencies
	 *            Plug-in dependencies.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param log
	 *            {@link Log}.
	 * @return {@link OpenOfficeFloorGoal}.
	 */
	public static OpenOfficeFloorGoal createOfficeFloorGoal(
			String defaultProcessName, MavenProject project,
			List<Artifact> pluginDependencies, String officeFloorLocation,
			Log log) {
		OpenOfficeFloorGoal goal = new OpenOfficeFloorGoal();
		goal.defaultProcessName = defaultProcessName;
		goal.project = project;
		goal.pluginDependencies = pluginDependencies;
		goal.officeFloorLocation = officeFloorLocation;
		goal.setLog(log);
		return goal;
	}

	/**
	 * Default process name.
	 */
	private String defaultProcessName = DEFAULT_PROCESS_NAME;

	/**
	 * {@link MavenProject}.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 */
	private MavenProject project;

	/**
	 * Plug-in dependencies.
	 * 
	 * @parameter expression="${plugin.artifacts}"
	 * @required
	 */
	private List<Artifact> pluginDependencies;

	/**
	 * Port that {@link OfficeBuilding} is running on.
	 * 
	 * @parameter
	 */
	private Integer port = StartOfficeBuildingGoal.DEFAULT_OFFICE_BUILDING_PORT;

	/**
	 * Specifies the port that {@link OfficeBuilding} is running on.
	 * 
	 * @param port
	 *            Port that {@link OfficeBuilding} is running on.
	 */
	public void setOfficeBuildingPort(int port) {
		this.port = port;
	}

	/**
	 * Path to the {@link OfficeFloor} configuration.
	 * 
	 * @parameter
	 * @required
	 */
	private String officeFloorLocation;

	/**
	 * {@link OfficeFloorSource} class name.
	 * 
	 * @parameter
	 */
	private String officeFloorSource;

	/**
	 * Specifies the {@link OfficeFloorSource} class name.
	 * 
	 * @param officeFloorSource
	 *            {@link OfficeFloorSource} class name.
	 */
	public void setOfficeFloorSource(String officeFloorSource) {
		this.officeFloorSource = officeFloorSource;
	}

	/**
	 * Process name to open the {@link OfficeFloor} within.
	 * 
	 * @parameter
	 */
	private String processName;

	/**
	 * Specifies the process name.
	 * 
	 * @param processName
	 *            Process name.
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * JVM options for running the {@link OfficeFloor}.
	 * 
	 * @parameter
	 */
	private String[] jvmOptions;

	/**
	 * Specifies the JVM options.
	 * 
	 * @param jvmOptions
	 *            JVM options.
	 */
	public void setJvmOptions(String... jvmOptions) {
		this.jvmOptions = jvmOptions;
	}

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have required values
		assertNotNull("Must have project", this.project);
		assertNotNull("Must have plug-in dependencies", this.pluginDependencies);
		assertNotNull(
				"Port not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.port);
		assertNotNull(OfficeFloor.class.getSimpleName()
				+ " configuration location not specified",
				this.officeFloorLocation);

		// Ensure default non-required values
		this.processName = defaultValue(this.processName,
				this.defaultProcessName);

		// Obtain the OfficeBuilding manager
		OfficeBuildingManagerMBean officeBuildingManager;
		try {
			officeBuildingManager = OfficeBuildingManager
					.getOfficeBuildingManager(null, this.port.intValue(),
							StartOfficeBuildingGoal.getKeyStoreFile(),
							StartOfficeBuildingGoal.KEY_STORE_PASSWORD,
							StartOfficeBuildingGoal.USER_NAME,
							StartOfficeBuildingGoal.PASSWORD);
		} catch (Throwable ex) {
			throw this.newMojoExecutionException("Failed accessing the "
					+ OfficeBuilding.class.getSimpleName(), ex);
		}

		// Create the open OfficeFloor configuration
		OpenOfficeFloorConfiguration configuration = new OpenOfficeFloorConfiguration(
				this.officeFloorLocation);
		configuration.setOfficeFloorSourceClassName(this.officeFloorSource);
		configuration.setProcessName(this.processName);

		// Provide JVM options (if specified)
		if (this.jvmOptions != null) {
			for (String jvmOption : this.jvmOptions) {
				configuration.addJvmOption(jvmOption);
			}
		}

		// Add class path of project
		try {
			List<String> elements = this.project.getCompileClasspathElements();
			for (String element : elements) {
				configuration.addClassPathEntry(element);
			}
		} catch (Throwable ex) {
			throw this.newMojoExecutionException(
					"Failed creating class path for the "
							+ OfficeFloor.class.getSimpleName(), ex);
		}

		// Add plug-in dependencies (makes OfficeFloor functionality available)
		for (Artifact artifact : this.pluginDependencies) {
			configuration.addClassPathEntry(artifact.getFile()
					.getAbsolutePath());
		}

		// Determine if WAR project
		if ("war".equalsIgnoreCase(this.project.getPackaging())) {

			// WAR project, so make web resources available
			File baseDir = this.project.getBasedir();
			File woofDir = new File(baseDir, "target/woof");

			// Make the woof directory available
			configuration.addClassPathEntry(woofDir.getAbsolutePath());

			// Make the PUBLIC directory available
			File publicDir = new File(woofDir, "PUBLIC");
			publicDir.mkdirs(); // copy will error if not created

			// Obtain the webapp directory
			File webAppDir = new File(baseDir, "src/main/webapp");

			// Update web app resources to be made available
			this.updateResources(webAppDir, publicDir);
		}

		// Open the OfficeFloor
		String processNameSpace;
		try {
			processNameSpace = officeBuildingManager
					.openOfficeFloor(configuration);
		} catch (Throwable ex) {
			throw this.newMojoExecutionException("Failed opening the "
					+ OfficeFloor.class.getSimpleName(), ex);
		}

		// Log opened the OfficeFloor
		this.getLog().info(
				"Opened " + OfficeFloor.class.getSimpleName()
						+ " under process name space '" + processNameSpace
						+ "' for " + this.officeFloorLocation);
	}

	/**
	 * Update the resources in the destination directory from the source
	 * directory.
	 * 
	 * @param srcDir
	 *            Source directory.
	 * @param destDir
	 *            Destination directory.
	 * @throws MojoExecutionException
	 *             If fails to update resources.
	 */
	private void updateResources(File srcDir, File destDir)
			throws MojoExecutionException {
		try {

			// Ensure destination directory exists
			destDir.mkdir(); // not fail as may already exist

			// Copy the files
			for (File srcFile : srcDir.listFiles()) {

				// Determine if directory
				if (srcFile.isDirectory()) {
					// Recursively update the sub directories
					this.updateResources(srcFile,
							new File(destDir, srcFile.getName()));

				} else {
					// Determine if destination file is latest
					File destFile = new File(destDir, srcFile.getName());
					if ((destFile.exists())
							&& (srcFile.lastModified() <= destFile
									.lastModified())) {
						// Have latest file, so do not update
						continue;
					}

					// Update the destination file to latest
					FileInputStream srcContent = new FileInputStream(srcFile);
					FileOutputStream destContent = new FileOutputStream(
							destFile, false);
					for (int value = srcContent.read(); value != -1; value = srcContent
							.read()) {
						destContent.write(value);
					}
					destContent.close();
					srcContent.close();
				}
			}

		} catch (IOException ex) {
			throw this.newMojoExecutionException(
					"Failed making WebApp resources available", ex);
		}
	}

}