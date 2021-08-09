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

package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeFloorMBeanRegistrator;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.compile.team.TeamType;

/**
 * {@link CompileContext} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class CompileContextImpl implements CompileContext {

	/**
	 * {@link OfficeFloorMBeanRegistrator}.
	 */
	private OfficeFloorMBeanRegistrator officeFloorMBeanRegistrator;

	/**
	 * {@link ManagedObjectType} by {@link ManagedObjectSourceNode} instances.
	 */
	private final Map<ManagedObjectSourceNode, TypeHolder<ManagedObjectType<?>>> managedObjectTypes = new HashMap<>();

	/**
	 * {@link ManagedObjectPoolType} by {@link ManagedObjectPoolNode} instances.
	 */
	private final Map<ManagedObjectPoolNode, TypeHolder<ManagedObjectPoolType>> managedObjectPoolTypes = new HashMap<>();

	/**
	 * {@link SuppliedManagedObjectSourceType} by
	 * {@link SuppliedManagedObjectSourceNode} instances.
	 */
	private final Map<SuppliedManagedObjectSourceNode, TypeHolder<SuppliedManagedObjectSourceType>> suppliedManagedObjectSourceTypes = new HashMap<>();

	/**
	 * {@link InitialSupplierType} by {@link SupplierNode} instances.
	 */
	private final Map<SupplierNode, TypeHolder<InitialSupplierType>> initialSupplierTypes = new HashMap<>();

	/**
	 * {@link SupplierType} by {@link SupplierNode} instances.
	 */
	private final Map<SupplierNode, TypeHolder<SupplierType>> supplierTypes = new HashMap<>();

	/**
	 * {@link FunctionNamespaceType} by {@link FunctionNamespaceNode} instances.
	 */
	private final Map<FunctionNamespaceNode, TypeHolder<FunctionNamespaceType>> namespaceTypes = new HashMap<>();

	/**
	 * {@link ExecutiveType} by {@link ExecutiveNode} instances.
	 */
	private final Map<ExecutiveNode, TypeHolder<ExecutiveType>> executiveTypes = new HashMap<>();

	/**
	 * {@link TeamType} by {@link TeamNode} instances.
	 */
	private final Map<TeamNode, TypeHolder<TeamType>> teamTypes = new HashMap<>();

	/**
	 * {@link AdministrationType} by {@link AdministrationNode} instances.
	 */
	private final Map<AdministrationNode, TypeHolder<AdministrationType<?, ?, ?>>> administrationTypes = new HashMap<>();

	/**
	 * {@link GovernanceType} by {@link GovernanceNode} instances.
	 */
	private final Map<GovernanceNode, TypeHolder<GovernanceType<?, ?>>> governanceTypes = new HashMap<>();

	/**
	 * Gets or loads the type.
	 * 
	 * @param node       {@link Node}.
	 * @param types      Types by {@link Node}.
	 * @param typeLoader {@link Function} to load the type.
	 */
	private static <N extends Node, T> T getOrLoadType(N node, Map<N, TypeHolder<T>> types, Function<N, T> typeLoader) {

		// Obtain from already registered
		TypeHolder<T> holder = types.get(node);
		if (holder != null) {
			return holder.type;
		}

		// Not registered, so create
		T type = typeLoader.apply(node);

		// Register the result (even if null)
		types.put(node, new TypeHolder<T>(type));

		// Return type
		return type;
	}

	/**
	 * Type holder.
	 * 
	 * @param <T> Type.
	 */
	private static class TypeHolder<T> {

		/**
		 * Type.
		 */
		public final T type;

		public TypeHolder(T type) {
			this.type = type;
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param officeFloorMBeanRegistrator {@link OfficeFloorMBeanRegistrator}. May
	 *                                    be <code>null</code>.
	 */
	public CompileContextImpl(OfficeFloorMBeanRegistrator officeFloorMBeanRegistrator) {
		this.officeFloorMBeanRegistrator = officeFloorMBeanRegistrator;
	}

	/**
	 * Obtains the {@link AvailableType} instances from the {@link SupplierNode}.
	 * 
	 * @param supplierNode {@link SupplierNode}.
	 * @return {@link AvailableType} instances.
	 */
	private AvailableType[] getAvailableTypes(SupplierNode supplierNode) {
		OfficeNode office = supplierNode.getOfficeNode();
		return office != null ? office.getAvailableTypes(this)
				: supplierNode.getOfficeFloorNode().getAvailableTypes(this);
	}

	/*
	 * ====================== CompileContext ============================
	 */

	@Override
	public <T, S extends T> void registerPossibleMBean(Class<T> type, String name, S mbean) {
		if (this.officeFloorMBeanRegistrator != null) {
			this.officeFloorMBeanRegistrator.registerPossibleMBean(type, name, mbean);
		}
	}

	@Override
	public ManagedObjectType<?> getOrLoadManagedObjectType(ManagedObjectSourceNode managedObjectSourceNode) {
		return getOrLoadType(managedObjectSourceNode, this.managedObjectTypes,
				(node) -> node.loadManagedObjectType(this));
	}

	@Override
	public ManagedObjectPoolType getOrLoadManagedObjectPoolType(ManagedObjectPoolNode managedObjectPoolNode) {
		return getOrLoadType(managedObjectPoolNode, this.managedObjectPoolTypes,
				(node) -> node.loadManagedObjectPoolType(false));
	}

	@Override
	public FunctionNamespaceType getOrLoadFunctionNamespaceType(FunctionNamespaceNode namespaceNode) {
		return getOrLoadType(namespaceNode, this.namespaceTypes, (node) -> node.loadFunctionNamespaceType(false));
	}

	@Override
	public SuppliedManagedObjectSourceType getOrLoadSuppliedManagedObjectSourceType(
			SuppliedManagedObjectSourceNode suppliedManagedObjectSourceNode) {
		return getOrLoadType(suppliedManagedObjectSourceNode, this.suppliedManagedObjectSourceTypes,
				(node) -> node.loadSuppliedManagedObjectSourceType(this));
	}

	@Override
	public InitialSupplierType getOrLoadInitialSupplierType(SupplierNode supplierNode) {
		return getOrLoadType(supplierNode, this.initialSupplierTypes, (node) -> node.loadInitialSupplierType(false));
	}

	@Override
	public SupplierType getOrLoadSupplierType(SupplierNode supplierNode) {
		AvailableType[] availableTypes = this.getAvailableTypes(supplierNode);
		return getOrLoadType(supplierNode, this.supplierTypes,
				(node) -> node.loadSupplierType(this, false, availableTypes));
	}

	@Override
	public ExecutiveType getOrLoadExecutiveType(ExecutiveNode executiveNode) {
		return getOrLoadType(executiveNode, this.executiveTypes, (node) -> node.loadExecutiveType());
	}

	@Override
	public TeamType getOrLoadTeamType(TeamNode teamNode) {
		return getOrLoadType(teamNode, this.teamTypes, (node) -> node.loadTeamType());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, F extends Enum<F>, G extends Enum<G>> AdministrationType<E, F, G> getOrLoadAdministrationType(
			AdministrationNode administrationNode) {
		return (AdministrationType<E, F, G>) getOrLoadType(administrationNode, this.administrationTypes,
				(node) -> node.loadAdministrationType(false));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I, F extends Enum<F>> GovernanceType<I, F> getOrLoadGovernanceType(GovernanceNode governanceNode) {
		return (GovernanceType<I, F>) getOrLoadType(governanceNode, this.governanceTypes,
				(node) -> node.loadGovernanceType(false));
	}

}
