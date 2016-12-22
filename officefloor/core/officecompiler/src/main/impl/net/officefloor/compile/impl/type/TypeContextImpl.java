/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.type;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.team.TeamType;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link TypeContext} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class TypeContextImpl implements TypeContext {

	/**
	 * {@link ManagedObjectType} by {@link ManagedObjectSourceNode} instances.
	 */
	private final Map<ManagedObjectSourceNode, TypeHolder<ManagedObjectType<?>>> managedObjectTypes = new HashMap<ManagedObjectSourceNode, TypeContextImpl.TypeHolder<ManagedObjectType<?>>>();

	/**
	 * {@link FunctionNamespaceType} by {@link WorkNode} instances.
	 */
	private final Map<WorkNode, TypeHolder<FunctionNamespaceType<?>>> workTypes = new HashMap<WorkNode, TypeContextImpl.TypeHolder<FunctionNamespaceType<?>>>();

	/**
	 * {@link TeamType} by {@link TeamNode} instances.
	 */
	private final Map<TeamNode, TypeHolder<TeamType>> teamTypes = new HashMap<TeamNode, TypeContextImpl.TypeHolder<TeamType>>();

	/**
	 * {@link AdministratorType} by {@link AdministratorNode} instances.
	 */
	private final Map<AdministratorNode, TypeHolder<AdministratorType<?, ?>>> administratorTypes = new HashMap<AdministratorNode, TypeContextImpl.TypeHolder<AdministratorType<?, ?>>>();

	/**
	 * {@link GovernanceType} by {@link GovernanceNode} instances.
	 */
	private final Map<GovernanceNode, TypeHolder<GovernanceType<?, ?>>> governanceTypes = new HashMap<GovernanceNode, TypeContextImpl.TypeHolder<GovernanceType<?, ?>>>();

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
	private static <N extends Node, T> T getOrLoadType(N node,
			Map<N, TypeHolder<T>> types, Function<N, T> typeLoader) {

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

	/*
	 * ====================== TypeContext ============================
	 */

	@Override
	public ManagedObjectType<?> getOrLoadManagedObjectType(
			ManagedObjectSourceNode managedObjectSourceNode) {
		return getOrLoadType(managedObjectSourceNode, this.managedObjectTypes,
				(node) -> node.loadManagedObjectType());
	}

	@Override
	public FunctionNamespaceType<?> getOrLoadWorkType(WorkNode workNode) {
		return getOrLoadType(workNode, this.workTypes,
				(node) -> node.loadWorkType());
	}

	@Override
	public TeamType getOrLoadTeamType(TeamNode teamNode) {
		return getOrLoadType(teamNode, this.teamTypes,
				(node) -> node.loadTeamType());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I, A extends Enum<A>> AdministratorType<I, A> getOrLoadAdministratorType(
			AdministratorNode administratorNode) {
		return (AdministratorType<I, A>) getOrLoadType(administratorNode,
				this.administratorTypes, (node) -> node.loadAdministratorType());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I, F extends Enum<F>> GovernanceType<I, F> getOrLoadGovernanceType(
			GovernanceNode governanceNode) {
		return (GovernanceType<I, F>) getOrLoadType(governanceNode,
				this.governanceTypes, (node) -> node.loadGovernanceType());
	}

}