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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Interface to manage a particular {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionManager {

	/**
	 * Obtains the annotations for this {@link ManagedFunction}.
	 * 
	 * @return Annotations for this {@link ManagedFunction}.
	 */
	Object[] getAnnotations();

	/**
	 * Obtains the parameter type for invoking this {@link ManagedFunction}.
	 * 
	 * @return Parameter type for invoking the {@link ManagedFunction}. Will be
	 *         <code>null</code> if no parameter to the {@link ManagedFunction}.
	 */
	Class<?> getParameterType();

	/**
	 * Invokes the {@link ManagedFunction} which is executed within a new
	 * {@link ProcessState} of the {@link Office}.
	 * 
	 * @param parameter Parameter for the {@link ManagedFunction}.
	 * @param callback  Optional {@link FlowCallback}. May be <code>null</code>.
	 * @return {@link ProcessManager} to manage the {@link ProcessState}.
	 * @throws InvalidParameterTypeException Should the parameter be of incorrect
	 *                                       type for the {@link ManagedFunction}.
	 */
	ProcessManager invokeProcess(Object parameter, FlowCallback callback) throws InvalidParameterTypeException;

}
