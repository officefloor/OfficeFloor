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

package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectType<D extends Enum<D>> {

	/**
	 * Obtains the {@link Class} of the object returned from {@link ManagedObject}.
	 * 
	 * @return The {@link Class} of the object being managed by the
	 *         {@link ManagedObject}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the {@link ManagedObjectDependencyType} definitions of the required
	 * dependencies for the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectDependencyType} definitions of the required
	 *         dependencies for the {@link ManagedObject}.
	 */
	ManagedObjectDependencyType<D>[] getDependencyTypes();

	/**
	 * Obtains the {@link ManagedObjectFunctionDependencyType} definitions of the
	 * required dependencies of the added {@link ManagedFunction} instances of the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectFunctionDependencyType} definitions of the
	 *         required dependencies of the added {@link ManagedFunction} instances
	 *         of the {@link ManagedObjectSource}.
	 */
	ManagedObjectFunctionDependencyType[] getFunctionDependencyTypes();

	/**
	 * <p>
	 * Indicates if the {@link ManagedObjectSource} may trigger a {@link Flow}.
	 * <p>
	 * Note that a {@link ManagedObjectSource} can provide no
	 * {@link ManagedObjectFlowType} instances yet still be input (as
	 * {@link ManagedObjectSource} provides the {@link ManagedFunction} for the
	 * {@link Flow}).
	 * 
	 * @return <code>true</code> if input.
	 */
	boolean isInput();

	/**
	 * Obtains the {@link ManagedObjectFlowType} definitions of the {@link Flow}
	 * instances required to be linked for the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectFlowType} definitions of the {@link Flow}
	 *         instances required to be linked for the {@link ManagedObjectSource}.
	 */
	ManagedObjectFlowType<?>[] getFlowTypes();

	/**
	 * Obtains the {@link ManagedObjectTeamType} definitions of {@link Team}
	 * instances required by the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectTeamType} definitions of {@link Team} instances
	 *         required by the {@link ManagedObject}.
	 */
	ManagedObjectTeamType[] getTeamTypes();

	/**
	 * Obtains the {@link ManagedObjectExecutionStrategyType} definitions of the
	 * {@link ExecutionStrategy} instances required by the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectExecutionStrategyType} definitions of the
	 *         {@link ExecutionStrategy} instances required by the
	 *         {@link ManagedObject}.
	 */
	ManagedObjectExecutionStrategyType[] getExecutionStrategyTypes();

	/**
	 * Obtains the extension types supported by the {@link ManagedObject}.
	 * 
	 * @return Extension types supported by the {@link ManagedObject}.
	 */
	Class<?>[] getExtensionTypes();

}
