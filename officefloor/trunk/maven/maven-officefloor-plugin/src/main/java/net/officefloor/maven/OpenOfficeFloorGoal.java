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

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.building.manager.ArtifactReference;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.manager.UploadArtifact;
import net.officefloor.building.process.ProcessShell;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Maven goal to open the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "open", requiresDependencyResolution = ResolutionScope.COMPILE)
public class OpenOfficeFloorGoal extends AbstractGoal {

	/**
	 * Default process name.
	 */
	public static final String DEFAULT_PROCESS_NAME = "maven-officefloor-plugin";

	/**
	 * Creates the {@link OpenOfficeFloorGoal} with the required parameters.
	 * 
	 * @param defaultProcessName
	 *            Default {@link ProcessShell} name.
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
	 */
	@Parameter(defaultValue = "${project}", required = true)
	private MavenProject project;

	/**
	 * Plug-in dependencies.
	 */
	@Parameter(defaultValue = "${plugin.artifacts}", required = true)
	private List<Artifact> pluginDependencies;

	/**
	 * Port that {@link OfficeBuilding} is running on.
	 */
	@Parameter
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
	 */
	@Parameter(required = true)
	private String officeFloorLocation;

	/**
	 * {@link OfficeFloorSource} class name.
	 */
	@Parameter
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
	 * Process name to open the {@link OfficeFloor} within. s
	 */
	@Parameter
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
	 */
	@Parameter
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

	/**
	 * Listing of additional class path entries to include.
	 */
	private List<String> classPathEntries = new LinkedList<String>();

	/**
	 * Adds a class path entry.
	 * 
	 * @param classPathEntry
	 *            Class path entry.
	 */
	public void addClassPathEntry(String classPathEntry) {
		this.classPathEntries.add(classPathEntry);
	}

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have required values
		ensureNotNull("Must have project", this.project);
		ensureNotNull("Must have plug-in dependencies", this.pluginDependencies);
		ensureNotNull(
				"Port not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.port);
		ensureNotNull(OfficeFloor.class.getSimpleName()
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
			throw newMojoExecutionException("Failed accessing the "
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
			List<String> elements = this.project.getRuntimeClasspathElements();
			for (String element : elements) {
				configuration.addClassPathEntry(element);
			}
		} catch (Throwable ex) {
			throw newMojoExecutionException(
					"Failed creating class path for the "
							+ OfficeFloor.class.getSimpleName(), ex);
		}

		// Add additional class path entries
		for (String classPathEntry : this.classPathEntries) {
			configuration.addClassPathEntry(classPathEntry);
		}

		// Indicate the configuration
		Log log = this.getLog();
		log.debug(OfficeFloorSource.class.getSimpleName() + " configuration:");
		log.debug("\tProcess name = " + configuration.getProcessName());
		log.debug("\t" + OfficeFloorSource.class.getSimpleName() + " class = "
				+ configuration.getOfficeFloorSourceClassName());
		log.debug("\t" + OfficeFloorSource.class.getSimpleName()
				+ " location = " + configuration.getOfficeFloorLocation());
		log.debug("\tProperties:");
		Properties configurationOfficeFloorProperties = configuration
				.getOfficeFloorProperties();
		for (String propertyName : configurationOfficeFloorProperties
				.stringPropertyNames()) {
			log.debug("\t\t"
					+ propertyName
					+ " = "
					+ configurationOfficeFloorProperties
							.getProperty(propertyName));
		}
		log.debug("\tClass path entries:");
		for (String classPathEntry : configuration.getClassPathEntries()) {
			log.debug("\t\t" + classPathEntry);
		}
		log.debug("\tUpload Artifacts:");
		for (UploadArtifact uploadArtifact : configuration.getUploadArtifacts()) {
			log.debug("\t\t" + uploadArtifact.getName());
		}
		log.debug("\tArtifact References:");
		for (ArtifactReference artifactReference : configuration
				.getArtifactReferences()) {
			log.debug("\t\t" + artifactReference.getId());
		}
		log.debug("\tRemote repository URLs:");
		for (String remoteRepositoryUrl : configuration
				.getRemoteRepositoryUrls()) {
			log.debug("\t\t" + remoteRepositoryUrl);
		}
		log.debug("\tJVM options:");
		for (String jvmOption : configuration.getJvmOptions()) {
			log.debug("\t\t" + jvmOption);
		}
		log.debug("\tInitial task = " + configuration.getOfficeName() + " "
				+ configuration.getWorkName() + "."
				+ configuration.getTaskName() + "("
				+ configuration.getParameter() + ")");

		// Open the OfficeFloor
		String processNameSpace;
		try {
			processNameSpace = officeBuildingManager
					.openOfficeFloor(configuration);
		} catch (Throwable ex) {
			throw newMojoExecutionException("Failed opening the "
					+ OfficeFloor.class.getSimpleName(), ex);
		}

		// Log opened the OfficeFloor
		this.getLog().info(
				"Opened " + OfficeFloor.class.getSimpleName()
						+ " under process name space '" + processNameSpace
						+ "' for " + this.officeFloorLocation);
	}

}