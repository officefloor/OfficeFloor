/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.command;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ProcessConfiguration;

/**
 * Environment for the {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommandEnvironment {

	/**
	 * Obtains the environment property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Property value for the name.
	 */
	String getProperty(String name);

	/**
	 * Specifies the {@link Process} name for the {@link ProcessConfiguration}.
	 * 
	 * @param processName
	 *            {@link Process} name for the {@link ProcessConfiguration}.
	 */
	void setProcessName(String processName);

}