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

package net.officefloor.compile.test.managedobject;

import net.officefloor.compile.executive.ExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Builder for the {@link ManagedObjectType} to validate the loaded
 * {@link ManagedObjectType} from the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTypeBuilder {

	/**
	 * Specifies the {@link Object} class returned from the {@link ManagedObject}.
	 * 
	 * @param objectClass Class of the {@link Object} returned from the
	 *                    {@link ManagedObject}.
	 */
	void setObjectClass(Class<?> objectClass);

	/**
	 * Flags the {@link ManagedObjectSource} as possibly being able to trigger a
	 * {@link Flow}.
	 * 
	 * @param isInput <code>true</code> if can trigger a {@link Flow}.
	 */
	void setInput(boolean isInput);

	/**
	 * Adds a {@link ManagedObjectDependencyType}.
	 * 
	 * @param name            Name of the {@link ManagedObjectDependency}.
	 * @param type            Type of the {@link ManagedObjectDependency}.
	 * @param typeQualifier   Qualifier for the type of
	 *                        {@link ManagedObjectDependency}.
	 * @param index           Index of the {@link ManagedObjectDependency}.
	 * @param key             Key identifying the {@link ManagedObjectDependency}.
	 * @param annotationTypes Types of the annotations for the
	 *                        {@link ManagedObjectDependency}.
	 */
	void addDependency(String name, Class<?> type, String typeQualifier, int index, Enum<?> key,
			Class<?>... annotationTypes);

	/**
	 * Adds a {@link ManagedObjectFunctionDependencyType}.
	 * 
	 * @param name          Name of the {@link ManagedObjectFunctionDependency}.
	 * @param type          Type of the {@link ManagedObjectFunctionDependency}.
	 * @param typeQualifier Qualifier for the type of
	 *                      {@link ManagedObjectFunctionDependency}.
	 */
	void addFunctionDependency(String name, Class<?> type, String typeQualifier);

	/**
	 * <p>
	 * Convenience method to add a {@link ManagedObjectDependencyType} based on the
	 * key.
	 * <p>
	 * Both the <code>name</code> and <code>index</code> are extracted from the key.
	 * 
	 * @param key           Key identifying the {@link ManagedObjectDependency}.
	 * @param type          Type of the {@link ManagedObjectDependency}.
	 * @param typeQualifier Qualifier for the type of
	 *                      {@link ManagedObjectDependency}.
	 */
	void addDependency(Enum<?> key, Class<?> type, String typeQualifier);

	/**
	 * Adds a {@link ManagedObjectFlowType}.
	 * 
	 * @param name         Name of the {@link ManagedObjectFlow}.
	 * @param argumentType Type of argument passed from the
	 *                     {@link ManagedObjectFlow}.
	 * @param index        Index of the {@link ManagedObjectFlow}.
	 * @param key          Key identifying the {@link ManagedObjectFlow}.
	 */
	void addFlow(String name, Class<?> argumentType, int index, Enum<?> key);

	/**
	 * <p>
	 * Convenience method to add a {@link ManagedObjectFlowType} based on the key.
	 * <p>
	 * Both the <code>name</code> and <code>index</code> are extracted from the key.
	 * 
	 * @param key          Key identifying the {@link ManagedObjectFlow}.
	 * @param argumentType Type of argument passed from the
	 *                     {@link ManagedObjectFlow}.
	 */
	void addFlow(Enum<?> key, Class<?> argumentType);

	/**
	 * Adds a {@link ManagedObjectTeamType}.
	 * 
	 * @param teamName Name of the {@link ManagedObjectTeam}.
	 */
	void addTeam(String teamName);

	/**
	 * Adds an {@link ExecutionStrategyType}.
	 * 
	 * @param executionStrategyName Name of the {@link ExecutionStrategyType}.
	 */
	void addExecutionStrategy(String executionStrategyName);

	/**
	 * Adds an extension interface.
	 * 
	 * @param extensionInterface Extension interface.
	 */
	void addExtensionInterface(Class<?> extensionInterface);

	/**
	 * Builds the {@link ManagedObjectType}.
	 * 
	 * @param <D> Dependency keys.
	 * @return {@link ManagedObjectType}.
	 */
	<D extends Enum<D>> ManagedObjectType<D> build();

}
