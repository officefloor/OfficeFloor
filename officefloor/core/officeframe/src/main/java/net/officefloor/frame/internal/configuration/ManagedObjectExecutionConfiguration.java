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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Configuration of an {@link ExecutionStrategy} utilised by a
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecutionConfiguration {

	/**
	 * Obtains the name to identify this {@link ExecutionStrategy}.
	 * 
	 * @return Name identifying this {@link ExecutionStrategy}.
	 */
	String getExecutionStrategyName();

}
