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
package net.officefloor.building.command.parameters;

import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.console.OfficeBuilding;

/**
 * {@link OfficeFloorCommandParameter} for the {@link OfficeBuilding} port.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingPortOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Parameter name for the {@link OfficeBuilding} port.
	 */
	public static final String PARAMETER_OFFICE_BUILDING_PORT = "office_building_port";

	/**
	 * Default {@link OfficeBuilding} port.
	 */
	public static final int DEFAULT_OFFICE_BUILDING_PORT = 13778;

	/**
	 * Initiate.
	 */
	public OfficeBuildingPortOfficeFloorCommandParameter() {
		super(PARAMETER_OFFICE_BUILDING_PORT, null,
				"Port for the OfficeBuilding. Default is "
						+ DEFAULT_OFFICE_BUILDING_PORT);
	}

	/**
	 * Obtains the {@link OfficeBuilding} port.
	 * 
	 * @return {@link OfficeBuilding} port.
	 */
	public int getOfficeBuildingPort() {

		// Obtain the port
		int port = DEFAULT_OFFICE_BUILDING_PORT;
		String portValue = this.getValue();
		if (portValue != null) {
			port = Integer.parseInt(portValue);
		}

		// Return the port
		return port;
	}

}