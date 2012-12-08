/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Provides control over the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceControl<I, F extends Enum<F>> {

	/**
	 * Activates the {@link Governance}.
	 * 
	 * @param governanceContext
	 *            {@link GovernanceContext}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param context
	 *            ContainerContext.
	 * @return <code>true</code> if the {@link Governance} was activated.
	 * @throws Throwable
	 *             If fails to activate the {@link Governance}.
	 */
	boolean activateGovernance(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context)
			throws Throwable;

	/**
	 * Initiates {@link Governance} over the {@link ManagedObject}.
	 * 
	 * @param extension
	 *            Extension of the {@link ManagedObject} to provide
	 *            {@link Governance} over it.
	 * @param governanceManager
	 *            {@link ActiveGovernanceManager}.
	 * @param governanceContext
	 *            {@link GovernanceContext}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param context
	 *            ContainerContext.
	 * @return <code>true</code> if {@link ManagedObject} is under
	 *         {@link Governance}.
	 * @throws Throwable
	 *             If fails to initiate {@link Governance} over the
	 *             {@link ManagedObject}.
	 */
	boolean governManagedObject(I extension,
			ActiveGovernanceManager<I, F> governanceManager,
			GovernanceContext<F> governanceContext, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context) throws Throwable;

	/**
	 * Enforce the {@link Governance}.
	 * 
	 * @param governanceContext
	 *            {@link GovernanceContext}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} enforcing
	 *            the {@link Governance}.
	 * @param context
	 *            ContainerContext.
	 * @return <code>true</code> if the {@link Governance} was enforced.
	 * @throws Throwable
	 *             If fails to enforce the {@link Governance}.
	 */
	boolean enforceGovernance(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext context) throws Throwable;

	/**
	 * Disregards the {@link Governance}.
	 * 
	 * @param governanceContext
	 *            {@link GovernanceContext}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team}
	 *            disregarding the {@link Governance}.
	 * @param context
	 *            ContainerContext.
	 * @return <code>true</code> if the {@link Governance} was disregarded.
	 * @throws Throwable
	 *             If fails to disregard the {@link Governance}.
	 */
	boolean disregardGovernance(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext context) throws Throwable;

}