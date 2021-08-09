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

package net.officefloor.frame.api.build;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;

/**
 * Builds a {@link Flow} from a {@link ManagedFunctionContainer} or
 * {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface FlowBuilder<F extends Enum<F>> extends FunctionBuilder<F> {

	/**
	 * Specifies the next {@link ManagedFunction} to be executed.
	 * 
	 * @param functionName
	 *            Name of the next {@link ManagedFunction}.
	 * @param argumentType
	 *            Type of argument passed to the next {@link ManagedFunction}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void setNextFunction(String functionName, Class<?> argumentType);

}
