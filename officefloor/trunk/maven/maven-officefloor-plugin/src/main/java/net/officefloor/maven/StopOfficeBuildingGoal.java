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
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.console.OfficeBuilding;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven goal to stop the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "stop")
public class StopOfficeBuildingGoal extends AbstractGoal {

	/**
	 * Creates the {@link StopOfficeBuildingGoal} with the required parameters.
	 * 
	 * @param port
	 *            Port that {@link OfficeBuilding} is running on. May be
	 *            <code>null</code> to use default port.
	 * @param waitTime
	 *            Time to wait in stopping the {@link OfficeBuilding}. May be
	 *            <code>null</code> to use default time.
	 * @param log
	 *            {@link Log}.
	 * @return {@link StopOfficeBuildingGoal}.
	 */
	public static StopOfficeBuildingGoal createStopOfficeBuildingGoal(
			Integer port, Long waitTime, Log log) {
		StopOfficeBuildingGoal goal = new StopOfficeBuildingGoal();
		goal.port = (port != null ? port
				: StartOfficeBuildingGoal.DEFAULT_OFFICE_BUILDING_PORT);
		goal.waitTime = waitTime;
		goal.setLog(log);
		return goal;
	}

	/**
	 * Port that {@link OfficeBuilding} is running on.
	 */
	@Parameter
	private Integer port = StartOfficeBuildingGoal.DEFAULT_OFFICE_BUILDING_PORT;

	/**
	 * Time to wait in stopping the {@link OfficeBuilding}.
	 */
	@Parameter
	private Long waitTime;

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have required values
		ensureNotNull(
				"Port not configured for the "
						+ OfficeBuilding.class.getSimpleName(), this.port);

		// Ensure default non-required values
		long stopWaitTime = defaultValue(this.waitTime, Long.valueOf(10000))
				.longValue();

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

		// Stop the OfficeBuilding
		try {
			officeBuildingManager.stopOfficeBuilding(stopWaitTime);
		} catch (Throwable ex) {
			throw newMojoExecutionException("Failed stopping the "
					+ OfficeBuilding.class.getSimpleName(), ex);
		}

		// Log started OfficeBuilding
		this.getLog().info(
				"Stopped the " + OfficeBuilding.class.getSimpleName()
						+ " running on port " + this.port.intValue());
	}

}