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

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Builds details of a {@link ManagedObjectSource} being managed by an
 * {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagingOfficeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the name to bind the input {@link ManagedObject} within the
	 * {@link ProcessState} of the {@link Office}.
	 *
	 * @param inputManagedObjectName Name to bind the input {@link ManagedObject}
	 *                               within the {@link ProcessState} of the
	 *                               {@link Office}.
	 * @return {@link ThreadDependencyMappingBuilder} to map the dependencies of the
	 *         {@link ManagedObject} and possible
	 *         {@link ManagedObjectFunctionDependency} instances.
	 */
	ThreadDependencyMappingBuilder setInputManagedObjectName(String inputManagedObjectName);

	/**
	 * Specifies the {@link ManagedObject} for the
	 * {@link ManagedObjectFunctionDependency}.
	 * 
	 * @param functionObjectName     Name of the
	 *                               {@link ManagedObjectFunctionDependency}.
	 * @param scopeManagedObjectName Name of the {@link ManagedObject}.
	 */
	void mapFunctionDependency(String functionObjectName, String scopeManagedObjectName);

	/**
	 * Links the {@link Flow} for the {@link ManagedObjectSource} to a
	 * {@link ManagedFunction} within the managing {@link Office}.
	 *
	 * @param key          Key identifying the {@link Flow} instigated by the
	 *                     {@link ManagedObjectSource}.
	 * @param functionName Name of the {@link ManagedFunction}.
	 */
	void linkFlow(F key, String functionName);

	/**
	 * Links the {@link Flow} for the {@link ManagedObjectSource} to a
	 * {@link ManagedFunction} within the managing {@link Office}.
	 *
	 * @param flowIndex    Index identifying the {@link Flow} instigated by the
	 *                     {@link ManagedObjectSource}.
	 * @param functionName Name of the {@link ManagedFunction}.
	 */
	void linkFlow(int flowIndex, String functionName);

	/**
	 * Links strategy to its {@link ExecutionStrategy}.
	 * 
	 * @param strategyIndex         Index identifying the dependent
	 *                              {@link ExecutionStrategy} by the
	 *                              {@link ManagedObjectSource}.
	 * @param executionStrategyName Name of the {@link ExecutionStrategy}.
	 */
	void linkExecutionStrategy(int strategyIndex, String executionStrategyName);

}
