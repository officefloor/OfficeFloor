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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Check that the {@link ManagedObject} is ready.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectReadyCheck {

	/**
	 * Obtains the {@link FunctionState} to use in an {@link AssetLatch} if not
	 * ready.
	 * 
	 * @return {@link FunctionState} to use in an {@link AssetLatch} if not
	 *         ready.
	 */
	FunctionState getLatchFunction();

	/**
	 * Obtains the {@link ManagedFunctionContainer} to access dependent
	 * {@link ManagedObject} instances.
	 * 
	 * @return {@link ManagedFunctionContainer} to access dependent
	 *         {@link ManagedObject} instances.
	 */
	ManagedFunctionContainer getManagedFunctionContainer();

	/**
	 * Flags that a {@link ManagedObject} or one of its dependency
	 * {@link ManagedObject} instances is not ready.
	 * 
	 * @return {@link FunctionState} to flag the {@link ManagedObject} as not
	 *         ready.
	 */
	FunctionState setNotReady();

}
