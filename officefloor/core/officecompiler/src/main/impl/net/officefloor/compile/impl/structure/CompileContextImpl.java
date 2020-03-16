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

package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.OfficeFloorMBeanRegistrator;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.InitialSupplierType;
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
	 * {@link SuppliedManagedObjectSourceType} by
	 * {@link SuppliedManagedObjectSourceNode} instances.
	 */
	private final Map<SuppliedManagedObjectSourceNode, TypeHolder<SuppliedManagedObjectSourceType>> suppliedManagedObjectSourceTypes = new HashMap<>();

	/**
	 * {@link InitialSupplierType} by {@link SupplierNode} instances.
	 */
	private final Map<SupplierNode, TypeHolder<InitialSupplierType>> supplierTypes = new HashMap<>();

	/**
	 * {@link FunctionNamespaceType} by {@link FunctionNamespaceNode} instances.
	 */
	private final Map<FunctionNamespaceNode, TypeHolder<FunctionNamespaceType>> namespaceTypes = new HashMap<>();

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
	 * @param node
	 *            {@link Node}.
	 * @param types
	 *            Types by {@link Node}.
	 * @param typeLoader
	 *            {@link Function} to load the type.
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
	 * @param <T>
	 *            Type.
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
	 * @param officeFloorMBeanRegistrator
	 *            {@link OfficeFloorMBeanRegistrator}. May be <code>null</code>.
	 */
	public CompileContextImpl(OfficeFloorMBeanRegistrator officeFloorMBeanRegistrator) {
		this.officeFloorMBeanRegistrator = officeFloorMBeanRegistrator;
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
	public FunctionNamespaceType getOrLoadFunctionNamespaceType(FunctionNamespaceNode namespaceNode) {
		return getOrLoadType(namespaceNode, this.namespaceTypes, (node) -> node.loadFunctionNamespaceType());
	}

	@Override
	public SuppliedManagedObjectSourceType getOrLoadSuppliedManagedObjectSourceType(
			SuppliedManagedObjectSourceNode suppliedManagedObjectSourceNode) {
		return getOrLoadType(suppliedManagedObjectSourceNode, this.suppliedManagedObjectSourceTypes,
				(node) -> node.loadSuppliedManagedObjectSourceType(this));
	}

	@Override
	public InitialSupplierType getOrLoadSupplierType(SupplierNode supplierNode) {
		return getOrLoadType(supplierNode, this.supplierTypes, (node) -> node.loadInitialSupplierType());
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
				(node) -> node.loadAdministrationType());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I, F extends Enum<F>> GovernanceType<I, F> getOrLoadGovernanceType(GovernanceNode governanceNode) {
		return (GovernanceType<I, F>) getOrLoadType(governanceNode, this.governanceTypes,
				(node) -> node.loadGovernanceType());
	}

}
