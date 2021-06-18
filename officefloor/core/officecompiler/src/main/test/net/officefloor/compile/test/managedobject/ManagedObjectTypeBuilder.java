/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
