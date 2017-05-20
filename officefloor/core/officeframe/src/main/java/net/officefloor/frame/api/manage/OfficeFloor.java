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
package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * {@link OfficeFloor} where {@link ManagedFunction} instances are executed
 * within {@link Office} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloor {

	/**
	 * Opens the OfficeFloor and starts necessary {@link ManagedFunction}
	 * instances.
	 * 
	 * @throws Exception
	 *             If fails to open the OfficeFloor.
	 */
	void openOfficeFloor() throws Exception;

	/**
	 * Closes the OfficeFloor. This stops all {@link ManagedFunction} instances
	 * executing within the {@link Office} instances and releases all resources.
	 * 
	 * @throws Exception
	 *             If fails to close the {@link OfficeFloor}.
	 */
	void closeOfficeFloor() throws Exception;

	/**
	 * <p>
	 * Obtains the names of the {@link Office} instances within this
	 * {@link OfficeFloor}.
	 * <p>
	 * This allows to dynamically manage this {@link OfficeFloor}.
	 * 
	 * @return Names of the {@link Office} instances within this
	 *         {@link OfficeFloor}.
	 */
	String[] getOfficeNames();

	/**
	 * Obtains the {@link Office} for the input office name.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return Specified {@link Office}.
	 * @throws UnknownOfficeException
	 *             If no {@link Office} by the name within this
	 *             {@link OfficeFloor}.
	 */
	Office getOffice(String officeName) throws UnknownOfficeException;

}