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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Interest in a {@link ManagedFunctionContainer}.
 * <p>
 * The {@link ManagedFunctionContainer} will not unload its
 * {@link ManagedFunction} bound {@link ManagedObject} instances until all
 * {@link ManagedFunctionInterest} instances have been unregistered.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionInterest {

	/**
	 * Registers interest in the {@link ManagedFunctionContainer}.
	 * 
	 * @return {@link FunctionState} to register insterest in the
	 *         {@link ManagedFunctionContainer}.
	 */
	FunctionState registerInterest();

	/**
	 * Unregisters interest in the {@link ManagedFunctionContainer}.
	 * 
	 * @return {@link FunctionState} to unregister interest in the
	 *         {@link ManagedFunctionContainer}.
	 */
	FunctionState unregisterInterest();

}