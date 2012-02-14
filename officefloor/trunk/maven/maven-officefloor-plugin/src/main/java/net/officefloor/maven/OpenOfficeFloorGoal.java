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

import net.officefloor.building.command.CommandLineBuilder;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
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

	/**
	 * Indicates whether to provide verbose output.
	 * 
	 * @parameter
	 */
	private Boolean verbose = Boolean.valueOf(false);

	/**
	 * Specifies whether verbose.
	 * 
	 * @param isVerbose
	 *            <code>true</code> if verbose.
	 */
	public void setVerbose(boolean isVerbose) {
		this.verbose = Boolean.valueOf(isVerbose);
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
					.getOfficeBuildingManager(null, this.port.intValue());
		} catch (Throwable ex) {
			throw this.newMojoExecutionException("Failed accessing the "
					+ OfficeBuilding.class.getSimpleName(), ex);
		}

		// Create the arguments for class path of project
		CommandLineBuilder arguments = new CommandLineBuilder();
		try {
			List<String> elements = this.project.getCompileClasspathElements();
			for (String element : elements) {
				arguments.addClassPathEntry(element);
			}
		} catch (Throwable ex) {
			throw this.newMojoExecutionException(
					"Failed creating class path for the "
							+ OfficeFloor.class.getSimpleName(), ex);
		}

		// Add plug-in dependencies (makes OfficeFloor functionality available)
		for (Artifact artifact : this.pluginDependencies) {
			arguments.addClassPathEntry(artifact.getFile().getAbsolutePath());
		}

		// Specify the OfficeFloorSource
		arguments.addOfficeFloorSource(this.officeFloorSource);

		// Specify location of OfficeFloor
		arguments.addOfficeFloor(this.officeFloorLocation);

		// Specify the process name
		arguments.addProcessName(this.processName);

		// Provide JVM options (if specified)
		if (this.jvmOptions != null) {
			for (String jvmOption : this.jvmOptions) {
				arguments.addJvmOption(jvmOption);
			}
		}

		// Open the OfficeFloor
		String processNameSpace;
		try {
			processNameSpace = officeBuildingManager.openOfficeFloor(arguments
					.getCommandLine());
		} catch (Throwable ex) {
			throw this.newMojoExecutionException("Failed opening the "
					+ OfficeFloor.class.getSimpleName(), ex);
		}

		// Log opened the OfficeFloor
		this.getLog().info(
				"Opened " + OfficeFloor.class.getSimpleName()
						+ " under process name space '" + processNameSpace
						+ "' for " + this.officeFloorLocation);

		// Determine if provide verbose output
		if (this.verbose.booleanValue()) {
			StringBuilder message = new StringBuilder();
			message.append("ARGUMENTS: ");
			for (String argument : arguments.getCommandLine()) {
				message.append(" ");
				message.append(argument);
			}
			this.getLog().info(message.toString());
		}
	}

}