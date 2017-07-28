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
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorCommandParameter} for the {@link Process} name.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorNameOfficeFloorCommandParameter extends AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Parameter name for the {@link OfficeFloor} name.
	 */
	public static final String PARAMTER_OFFICEFLOOR_NAME = "officefloor_name";

	/**
	 * Initiate.
	 */
	public OfficeFloorNameOfficeFloorCommandParameter() {
		super(PARAMTER_OFFICEFLOOR_NAME, "n", "OfficeFloor name. Default is OfficeFloor");
	}

	/**
	 * Obtains the {@link OfficeFloor} name.
	 * 
	 * @return {@link OfficeFloor} name.
	 */
	public String getOfficeFloorName() {
		return this.getValue();
	}

}