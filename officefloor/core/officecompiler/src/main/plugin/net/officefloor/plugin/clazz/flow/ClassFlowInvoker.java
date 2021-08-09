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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Invokes the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassFlowInvoker {

	/**
	 * Invokes the {@link Flow}.
	 * 
	 * @param flowIndex Index identifying the {@link Flow} to instigate.
	 * @param parameter Parameter for the first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param callback  Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(int flowIndex, Object parameter, FlowCallback callback);

}
