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

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Context for the execution of a {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogicContext {

	/**
	 * Specifies the next {@link FunctionLogic} to be executed before the next
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @param function Next {@link FunctionLogic}.
	 */
	void next(FunctionLogic function);

	/**
	 * Obtains the {@link Object} from a {@link ManagedObject}.
	 * 
	 * @param index {@link ManagedObjectIndex} identifying the
	 *              {@link ManagedObject}.
	 * @return Object from the {@link ManagedObject}.
	 */
	Object getObject(ManagedObjectIndex index);

	/**
	 * Invokes a {@link Flow}.
	 * 
	 * @param flowMetaData {@link FlowMetaData} for the {@link Flow}.
	 * @param parameter    Parameter for the initial {@link ManagedFunction} of the
	 *                     {@link Flow}.
	 * @param callback     Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(FlowMetaData flowMetaData, Object parameter, FlowCallback callback);

	/**
	 * Creates an {@link AsynchronousFlow}.
	 * 
	 * @return {@link AsynchronousFlow}.
	 */
	AsynchronousFlow createAsynchronousFlow();

	/**
	 * Allows to asynchronously overwrite the next {@link ManagedFunction} argument.
	 * 
	 * @param argument Argument for the next {@link ManagedFunction}.
	 */
	void setNextFunctionArgument(Object argument);

}
