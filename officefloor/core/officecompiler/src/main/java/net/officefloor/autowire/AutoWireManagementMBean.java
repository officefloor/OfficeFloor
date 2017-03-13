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
package net.officefloor.autowire;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * MBean for auto-wire {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireManagementMBean {

	/**
	 * Invokes the {@link ManagedFunction} on the {@link OfficeFloor}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @throws Exception
	 *             If fails to invoke the {@link ManagedFunction}.
	 */
	void invokeFunction(String functionName) throws Exception;

	/**
	 * Closes the {@link OfficeFloor}.
	 */
	void closeOfficeFloor();

}