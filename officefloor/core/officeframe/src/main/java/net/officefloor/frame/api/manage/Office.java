/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
