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
 * {@link OfficeFloorCommandParameter} for the {@link OfficeBuilding} host.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingHostOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Parameter name for the {@link OfficeBuilding} host.
	 */
	public static final String PARAMETER_OFFICE_BUILDING_HOST = "office_building_host";

	/**
	 * Initiate.
	 */
	public OfficeBuildingHostOfficeFloorCommandParameter() {
		super(PARAMETER_OFFICE_BUILDING_HOST, null,
				"OfficeBuilding Host. Default is localhost");
	}

	/**
	 * Obtains the {@link OfficeBuilding} host.
	 * 
	 * @return {@link OfficeBuilding} host.
	 */
	public String getOfficeBuildingHost() {
		return this.getValue();
	}

}