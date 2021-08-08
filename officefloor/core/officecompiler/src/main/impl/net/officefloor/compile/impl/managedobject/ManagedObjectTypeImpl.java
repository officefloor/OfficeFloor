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

package net.officefloor.compile.impl.managedobject;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedObjectType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectTypeImpl<D extends Enum<D>> implements ManagedObjectType<D> {

	/**
	 * {@link Class} of the {@link Object} returned from the {@link ManagedObject}.
	 */
	private final Class<?> objectClass;

	/**
	 * Indicates if may trigger a {@link Flow}.
	 */
	private final boolean isInput;

	/**
	 * {@link ManagedObjectDependencyType} instances.
	 */
	private final ManagedObjectDependencyType<D>[] dependencies;

	/**
	 * {@link ManagedObjectFunctionDependencyType} instances.
	 */
	private final ManagedObjectFunctionDependencyType[] functionDependencies;

	/**
	 * {@link ManagedObjectFlowType} instances.
	 */
	private final ManagedObjectFlowType<?>[] flows;

	/**
	 * {@link ManagedObjectTeamType} instances.
	 */
	private final ManagedObjectTeamType[] teams;

	/**
	 * {@link ManagedObjectExecutionStrategyType} instances.
	 */
	private final ManagedObjectExecutionStrategyType[] executionStrategies;

	/**
	 * Extension interfaces supported by the {@link ManagedObject}.
	 */
	private final Class<?>[] extensionTypes;

	/**
	 * Initiate.
	 * 
	 * @param objectClass          {@link Class} of the {@link Object} returned from
	 *                             the {@link ManagedObject}.
	 * @param isInput              Indicates if may trigger a {@link Flow}.
	 * @param dependencies         {@link ManagedObjectDependencyType} instances.
	 * @param functionDependencies {@link ManagedObjectFunctionDependencyType}
	 *                             instances.
	 * @param flows                {@link ManagedObjectFlowType} instances.
	 * @param teams                {@link ManagedObjectTeamType} instances.
	 * @param executionStrategy    {@link ManagedObjectExecutionStrategyType}
	 *                             instances.
	 * @param extensionTypes       Extension types supported by the
	 *                             {@link ManagedObject}.
	 */
	public ManagedObjectTypeImpl(Class<?> objectClass, boolean isInput, ManagedObjectDependencyType<D>[] dependencies,
			ManagedObjectFunctionDependencyType[] functionDependencies, ManagedObjectFlowType<?>[] flows,
			ManagedObjectTeamType[] teams, ManagedObjectExecutionStrategyType[] executionStrategy,
			Class<?>[] extensionTypes) {
		this.objectClass = objectClass;
		this.isInput = isInput;
		this.dependencies = dependencies;
		this.functionDependencies = functionDependencies;
		this.flows = flows;
		this.teams = teams;
		this.executionStrategies = executionStrategy;
		this.extensionTypes = extensionTypes;
	}

	/*
	 * ====================== ManagedObjectType ================================
	 */

	@Override
	public Class<?> getObjectType() {
		return this.objectClass;
	}

	@Override
	public boolean isInput() {
		return this.isInput;
	}

	@Override
	public ManagedObjectDependencyType<D>[] getDependencyTypes() {
		return this.dependencies;
	}

	@Override
	public ManagedObjectFunctionDependencyType[] getFunctionDependencyTypes() {
		return this.functionDependencies;
	}

	@Override
	public ManagedObjectFlowType<?>[] getFlowTypes() {
		return this.flows;
	}

	@Override
	public ManagedObjectTeamType[] getTeamTypes() {
		return this.teams;
	}

	@Override
	public ManagedObjectExecutionStrategyType[] getExecutionStrategyTypes() {
		return this.executionStrategies;
	}

	@Override
	public Class<?>[] getExtensionTypes() {
		return this.extensionTypes;
	}

}
