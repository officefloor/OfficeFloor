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
package net.officefloor.compile.type;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.team.TeamType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Context for loading a type.
 *
 * @author Daniel Sagenschneider
 */
public interface TypeContext {

	/**
	 * Obtains the existing or loads the {@link ManagedObjectType} for the
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceNode
	 *            {@link ManagedObjectSourceNode} to obtain the
	 *            {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	ManagedObjectType<?> getOrLoadManagedObjectType(ManagedObjectSourceNode managedObjectSourceNode);

	/**
	 * Obtains the existing or loads the {@link WorkType} for the
	 * {@link WorkNode}.
	 * 
	 * @param workNode
	 *            {@link WorkNode} to obtain the {@link WorkType}.
	 * @return {@link WorkType} or <code>null</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	WorkType<?> getOrLoadWorkType(WorkNode workNode);

	/**
	 * Obtains the existing or loads the {@link TeamType} for the
	 * {@link TeamNode}.
	 * 
	 * @param teamNode
	 *            {@link TeamNode} to obtain the {@link TeamType}.
	 * @return {@link TeamType} or <code>null</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	TeamType getOrLoadTeamType(TeamNode teamNode);

	/**
	 * Obtains the existing or loads the {@link AdministratorType} for the
	 * {@link AdministratorNode}.
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param <A>
	 *            {@link Duty} key {@link Enum} type.
	 * @param administratorNode
	 *            {@link AdministratorNode} to obtain the
	 *            {@link AdministratorType}.
	 * @return {@link AdministratorType} or <code>null</code> with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	<I, A extends Enum<A>> AdministratorType<I, A> getOrLoadAdministratorType(AdministratorNode administratorNode);

	/**
	 * Obtains the existing or loads the {@link GovernanceType} for the
	 * {@link GovernanceNode}.
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param <F>
	 *            Flow key {@link Enum} type.
	 * @param governanceNode
	 *            {@link GovernanceNode} to obtain the {@link GovernanceType}.
	 * @return {@link GovernanceType} or <code>null</code> with issue report to
	 *         the {@link CompilerIssues}.
	 */
	<I, F extends Enum<F>> GovernanceType<I, F> getOrLoadGovernanceType(GovernanceNode governanceNode);

}