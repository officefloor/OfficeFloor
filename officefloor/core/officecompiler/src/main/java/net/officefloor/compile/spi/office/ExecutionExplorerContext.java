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

package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Context for the {@link ExecutionExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionExplorerContext {

	/**
	 * Obtains the initial {@link ExecutionManagedFunction} for the
	 * {@link OfficeSectionInput}.
	 * 
	 * @return Initial {@link ExecutionManagedFunction} for the
	 *         {@link OfficeSectionInput}.
	 */
	ExecutionManagedFunction getInitialManagedFunction();

	/**
	 * <p>
	 * Obtains the {@link ExecutionManagedFunction} by {@link ManagedFunction}
	 * name.
	 * <p>
	 * This enables obtaining dynamically invoked {@link ManagedFunction}
	 * instances via execution.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link ExecutionManagedFunction}.
	 */
	ExecutionManagedFunction getManagedFunction(String functionName);

}
