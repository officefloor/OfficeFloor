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

import net.officefloor.console.OfficeBuilding;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maven goal to stop the {@link WoofOfficeFloorSource}.
 * 
 * @goal stop
 * @requiresDependencyResolution compile
 */
public class StopWoofGoal extends AbstractMojo {

	/**
	 * Port that {@link OfficeBuilding} is running on.
	 * 
	 * @parameter
	 */
	private Integer port;

	/**
	 * Time to wait in stopping the {@link OfficeBuilding}.
	 * 
	 * @parameter
	 */
	private Long waitTime;

	/*
	 * ======================== Mojo ==========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Create the stop goal
		StopOfficeBuildingGoal goal = StopOfficeBuildingGoal
				.createStopOfficeBuildingGoal(this.port, this.waitTime,
						this.getLog());

		// Stop WoOF
		goal.execute();
	}

}