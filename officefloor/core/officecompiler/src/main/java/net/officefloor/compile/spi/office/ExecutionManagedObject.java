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

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedObject} available to the {@link ExecutionExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionManagedObject {

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	String getManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectType} for the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectType} for the {@link ManagedObject}.
	 */
	ManagedObjectType<?> getManagedObjectType();

	/**
	 * Obtains the {@link ExecutionManagedObject} for the
	 * {@link ManagedObjectDependencyType}.
	 * 
	 * @param dependencyType
	 *            {@link ManagedObjectDependencyType}.
	 * @return {@link ExecutionManagedObject} for the
	 *         {@link ManagedObjectDependencyType}.
	 */
	ExecutionManagedObject getManagedObject(ManagedObjectDependencyType<?> dependencyType);

	/**
	 * Obtains the {@link ExecutionManagedFunction} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param flowType
	 *            {@link ManagedObjectFlowType}.
	 * @return {@link ExecutionManagedFunction} for the
	 *         {@link ManagedObjectFlowType}.
	 */
	ExecutionManagedFunction getManagedFunction(ManagedObjectFlowType<?> flowType);

}
