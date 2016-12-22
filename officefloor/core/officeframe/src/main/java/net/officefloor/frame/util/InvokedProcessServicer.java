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
package net.officefloor.frame.util;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Services an invoked {@link ProcessState} from the
 * {@link ManagedObjectSourceStandAlone}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InvokedProcessServicer {

	/**
	 * Services the invoked {@link ProcessState}.
	 * 
	 * @param processIndex
	 *            Index of the invoked {@link ProcessState}. Allows re-using the
	 *            {@link InvokedProcessServicer} for multiple invocations.
	 * @param parameter
	 *            Parameter to the initial {@link ManagedFunction} within the
	 *            {@link ProcessState}.
	 * @param managedObject
	 *            {@link ManagedObject} provided for the invoked
	 *            {@link ProcessState}.
	 * @throws Throwable
	 *             If failure on servicing.
	 */
	void service(int processIndex, Object parameter, ManagedObject managedObject)
			throws Throwable;

}