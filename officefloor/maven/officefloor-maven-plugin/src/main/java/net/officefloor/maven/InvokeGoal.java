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

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.process.officefloor.OfficeFloorManagerMBean;
import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven goal to invoke a {@link ManagedFunction} - either directly or as initial
 * {@link ManagedFunction} of {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "invoke")
public class InvokeGoal extends AbstractGoal {

	/**
	 * Port that {@link OfficeBuilding} is running on.
	 */
	@Parameter(property = "port")
	private Integer port = StartOfficeBuildingGoal.DEFAULT_OFFICE_BUILDING_PORT;

	/**
	 * Process name for the {@link OfficeFloor} to invoke the {@link ManagedFunction}
	 * within.
	 */
	@Parameter(property = "processName")
	private String processName;

	/**
	 * Name of {@link Office} containing the {@link ManagedFunction} to invoke.
	 */
	@Parameter(required = true, property = "office")
	private String office;

	/**
	 * Name of {@link Work} containing the {@link ManagedFunction} to invoke.
	 */
	@Parameter(required = true, property = "work")
	private String work;

	/**
	 * Name of {@link ManagedFunction} to invoke. May be <code>null</code> to invoke
	 * initial {@link ManagedFunction} of {@link Work}.
	 */
	@Parameter(property = "task")
	private String task;

	/**
	 * Parameter value for the {@link ManagedFunction}. May be <code>null</code>.
	 */
	@Parameter(property = "parameter")
	private String parameter;

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have required values
		ensureNotNull(
				"Port not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.port);
		ensureNotNull(
				"Office not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.office);
		ensureNotNull(
				"Work not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.office);

		// Ensure default non-required values
		this.processName = defaultValue(this.processName,
				OpenOfficeFloorGoal.DEFAULT_PROCESS_NAME);
		this.task = defaultValue(this.task, null);
		this.parameter = defaultValue(this.parameter, null);

		// Create the reference name
		String referenceName = this.office + ":" + this.work
				+ (this.task == null ? "" : ":" + this.task)
				+ (this.parameter == null ? "" : "(" + this.parameter + ")");

		// Obtain the OfficeFloor manager
		OfficeFloorManagerMBean manager;
		try {
			manager = OfficeBuildingManager.getOfficeFloorManager(null,
					this.port.intValue(), this.processName,
					StartOfficeBuildingGoal.getKeyStoreFile(),
					StartOfficeBuildingGoal.KEY_STORE_PASSWORD,
					StartOfficeBuildingGoal.USER_NAME,
					StartOfficeBuildingGoal.PASSWORD);
		} catch (Throwable ex) {
			throw newMojoExecutionException("Failed accessing the "
					+ OfficeFloor.class.getSimpleName(), ex);
		}

		// Invoke the task
		try {
			manager.invokeTask(this.office, this.work, this.task,
					this.parameter);
		} catch (Throwable ex) {
			throw newMojoExecutionException("Failed invoking task "
					+ referenceName + ": " + ex.getMessage(), ex);
		}

		// Log opened the OfficeFloor
		this.getLog().info(
				"Invoked " + referenceName + " in process name space '"
						+ this.processName + "'");
	}

}