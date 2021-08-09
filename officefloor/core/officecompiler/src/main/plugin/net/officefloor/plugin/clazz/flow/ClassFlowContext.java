/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz.flow;

import java.lang.reflect.Method;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Context for the {@link ClassFlowRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassFlowContext {

	/**
	 * Obtains the Type declaring the {@link Method} of this flow.
	 * 
	 * @return Type declaring the {@link Method} of this flow.
	 */
	Class<?> getFlowInterfaceType();

	/**
	 * Obtains the {@link Method}.
	 * 
	 * @return {@link Method}.
	 */
	Method getMethod();

	/**
	 * Obtains the parameter type for the {@link Flow}. Will be <code>null</code> if
	 * no parameter.
	 * 
	 * @return Parameter type for the {@link Flow}. Will be <code>null</code> if no
	 *         parameter.
	 */
	Class<?> getParameterType();

	/**
	 * Flags if {@link FlowCallback}.
	 * 
	 * @return <code>true</code> if {@link FlowCallback}.
	 */
	boolean isFlowCallback();

}
