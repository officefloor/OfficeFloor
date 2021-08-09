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

package net.officefloor.frame.util;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Services an invoked {@link ProcessState} from the
 * {@link ManagedObjectSourceStandAlone}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface InvokedProcessServicer {

	/**
	 * Services the invoked {@link ProcessState}.
	 * 
	 * @param processIndex  Index of the invoked {@link ProcessState}. Allows
	 *                      re-using the {@link InvokedProcessServicer} for multiple
	 *                      invocations.
	 * @param parameter     Parameter to the initial {@link ManagedFunction} within
	 *                      the {@link ProcessState}.
	 * @param managedObject {@link ManagedObject} provided for the invoked
	 *                      {@link ProcessState}.
	 * @throws Throwable If failure on servicing.
	 */
	void service(int processIndex, Object parameter, ManagedObject managedObject) throws Throwable;

}
