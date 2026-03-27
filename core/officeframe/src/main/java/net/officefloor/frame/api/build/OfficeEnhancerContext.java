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

/**
 * Context for the {@link OfficeEnhancer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeEnhancerContext {

	/**
	 * Obtains the {@link FlowBuilder} registered under the input
	 * {@link ManagedFunction} name.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link FlowBuilder} for the {@link ManagedFunction}.
	 */
	FlowBuilder<?> getFlowBuilder(String functionName);

	/**
	 * Obtains the {@link FlowBuilder} registered by the
	 * {@link ManagedObjectSource} under the input {@link ManagedFunction} name.
	 * 
	 * @param managedObjectSourceName
	 *            {@link ManagedObjectSource} name registered with the
	 *            {@link OfficeFloorBuilder}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link FlowBuilder} for the {@link ManagedFunction}.
	 */
	FlowBuilder<?> getFlowBuilder(String managedObjectSourceName, String functionName);

}
