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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Check that the {@link ManagedObject} is ready.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectReadyCheck {

	/**
	 * Obtains the {@link ManagedFunctionContainer} requiring the check on the
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @return {@link ManagedFunctionContainer} requiring the check on the
	 *         {@link ManagedObject}.
	 */
	ManagedFunctionContainer getManagedJobNode();

	/**
	 * Flags that a {@link ManagedObject} or one of its dependency
	 * {@link ManagedObject} instances is not ready.
	 * 
	 * @return {@link FunctionState} to flag the {@link ManagedObject} as not ready.
	 */
	FunctionState setNotReady();

}