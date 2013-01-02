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
import net.officefloor.frame.api.execute.Work;

/**
 * {@link OfficeFloorCommandParameter} for the {@link Work} name.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkNameOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {

	/**
	 * Parameter name for the {@link Work} name.
	 */
	public static final String PARAMETER_WORK_NAME = "work";

	/**
	 * Initiate.
	 */
	public WorkNameOfficeFloorCommandParameter() {
		super(PARAMETER_WORK_NAME, "w", "Name of the Work");
	}

	/**
	 * Obtains the {@link Work} name.
	 * 
	 * @return {@link Work} name.
	 */
	public String getWorkName() {
		return this.getValue();
	}

}