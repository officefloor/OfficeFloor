/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Office within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Office {

	/**
	 * <p>
	 * Obtains the names of the {@link FunctionManager} instances within this
	 * {@link Office}.
	 * <p>
	 * This allows to dynamically manage this {@link Office}.
	 * 
	 * @return Names of the {@link FunctionManager} instances within this
	 *         {@link Office}.
	 */
	String[] getFunctionNames();

	/**
	 * Obtains the {@link FunctionManager} for the named {@link ManagedFunction}.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @return {@link FunctionManager} for the named {@link ManagedFunction}.
	 * @throws UnknownFunctionException If unknown {@link ManagedFunction} name.
	 */
	FunctionManager getFunctionManager(String functionName) throws UnknownFunctionException;

	/**
	 * <p>
	 * Obtains the names of the bound {@link ManagedObject} instances within this
	 * {@link Office}.
	 * <p>
	 * This allows to dynamically managed this {@link Office}.
	 * <p>
	 * Note that only {@link ManagedObjectScope#THREAD} and
	 * {@link ManagedObjectScope#PROCESS} scoped {@link ManagedObject} objects are
	 * available.
	 * 
	 * @return
	 */
	String[] getObjectNames();

	/**
	 * Creates a {@link StateManager}.
	 * 
	 * @return {@link StateManager}.
	 */
	StateManager createStateManager();

}
