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

/**
 * {@link OfficeFloorCommandParameter} for a JVM option.
 * 
 * @author Daniel Sagenschneider
 */
public class JvmOptionOfficeFloorCommandParameter extends
		AbstractMultiplePathsOfficeFloorCommandParameter {

	/**
	 * Parameter name for a JVM option.
	 */
	public static final String PARAMETER_JVM_OPTION = "jvm_option";

	/**
	 * Initiate.
	 */
	public JvmOptionOfficeFloorCommandParameter() {
		super(PARAMETER_JVM_OPTION, null, ",", "JVM option");
	}

	/**
	 * Obtains the JVM options.
	 * 
	 * @return JVM options.
	 */
	public String[] getJvmOptions() {
		return this.getPaths();
	}

}