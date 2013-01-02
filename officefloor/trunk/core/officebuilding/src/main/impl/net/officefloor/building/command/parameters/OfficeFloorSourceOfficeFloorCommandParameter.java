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
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;

/**
 * {@link OfficeFloorCommandParameter} for the {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSourceOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Parameter name for the {@link OfficeFloorSource}.
	 */
	public static final String PARAMETER_OFFICE_FLOOR_SOURCE = "officefloorsource";

	/**
	 * Initiate.
	 */
	public OfficeFloorSourceOfficeFloorCommandParameter() {
		super(PARAMETER_OFFICE_FLOOR_SOURCE, "ofs", "OfficeFloorSource");
	}

	/**
	 * Obtains the {@link OfficeFloorSource} class name.
	 * 
	 * @return {@link OfficeFloorSource} class name.
	 */
	public String getOfficeFloorSourceClassName() {
		return this.getValue();
	}

}