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
package net.officefloor.frame.impl.execute.governance;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link GovernanceContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceContainerImpl<I, F extends Enum<F>> implements
		GovernanceContainer<I, F>, GovernanceControl<I, F> {

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> metaData;

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState;

	/**
	 * Index of this {@link Governance} registered within the
	 * {@link ProcessState}.
	 */
	private final int registeredIndex;

	/**
	 * {@link ActiveGovernanceManager} instances.
	 */
	private final List<ActiveGovernanceManager<I, F>> activeGovernances = new LinkedList<ActiveGovernanceManager<I, F>>();

	/**
	 * {@link Governance}.
	 */
	private Governance<? super I, F> governance = null;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param threadState
	 *            {@link ThreadState}.
	 * @param registeredIndex
	 *            Index of this {@link Governance} registered within the
	 *            {@link ProcessState}.
	 */
	public GovernanceContainerImpl(GovernanceMetaData<I, F> metaData,
			ThreadState threadState, int registeredIndex) {
		this.metaData = metaData;
		this.threadState = threadState;
		this.registeredIndex = registeredIndex;
	}

	/**
	 * Unregisters the {@link Governance}.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team}
	 *            unregistering the {@link Governance}.
	 */
	private void unregisterGovernance(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam) {

		// Unregister managed objects from governance
		for (ActiveGovernanceManager<I, F> activeGovernance : this.activeGovernances) {
			activeGovernance.unregisterManagedObject(activateSet, currentTeam);
		}

		// Unregister the Governance from Process
		this.threadState.governanceComplete(this);

		// Disregard the governance
		this.governance = null;
	}

	/*
	 * ==================== GovernanceContainer =========================
	 * 
	 * All methods are invoked from WorkContainer with a ProcessState lock so no
	 * need to synchronise access on these methods as already thread safe.
	 */

	@Override
	public int getProcessRegisteredIndex() {
		return this.registeredIndex;
	}

	@Override
	public boolean isActive() {

		// Access Point: Work
		// Locks: ThreadState, ProcessState

		return (this.governance != null);
	}

	@Override
	public ActiveGovernance<I, F> createActiveGovernance(I extensionInterface,
			ManagedObjectContainer managedobjectContainer,
			int managedObjectContainerRegisteredIndex,
			WorkContainer<?> workContainer) {

		// Access Point: Work
		// Locks: ThreadState, ProcessState

		// Ensure governance active
		if (this.governance == null) {
			throw new IllegalStateException("Can only create "
					+ ActiveGovernance.class.getSimpleName() + " for active "
					+ Governance.class.getSimpleName());
		}

		// Create the active governance
		ActiveGovernanceManager<I, F> activeGovernance = this.metaData
				.createActiveGovernance(this, this, extensionInterface,
						managedobjectContainer, workContainer,
						managedObjectContainerRegisteredIndex);

		// Register the active governance
		this.activeGovernances.add(activeGovernance);

		// Return the active governance
		return activeGovernance.getActiveGovernance();
	}

	@Override
	public void activateGovernance(ContainerContext context) {
		GovernanceActivity<I, F> activity = this.metaData
				.createActivateActivity(this);
		context.addGovernanceActivity(activity);
	}

	@Override
	public void enforceGovernance(ContainerContext context) {
		GovernanceActivity<I, F> activity = this.metaData
				.createEnforceActivity(this);
		context.addGovernanceActivity(activity);
	}

	@Override
	public void disregardGovernance(ContainerContext context) {
		GovernanceActivity<I, F> activity = this.metaData
				.createDisregardActivity(this);
		context.addGovernanceActivity(activity);
	}

	/*
	 * ==================== GovernanceControl =========================
	 */

	@Override
	public boolean activateGovernance(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context)
			throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Determine if already active governance
		if (this.isActive()) {
			return true; // already active governance
		}

		// Create the governance
		GovernanceFactory<? super I, F> factory = this.metaData
				.getGovernanceFactory();
		this.governance = factory.createGovernance();

		// Successfully activated the governance
		return true;
	}

	@Override
	public boolean governManagedObject(I extension,
			ActiveGovernanceManager<I, F> governanceManager,
			GovernanceContext<F> governanceContext, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context) throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Must lock to check if managed object ready
		ProcessState processState = this.threadState.getProcessState();
		synchronized (processState.getProcessLock()) {

			// Ensure managed object is ready
			if (!governanceManager.isManagedObjectReady(jobContext, jobNode,
					activateSet, context)) {
				return false; // not ready to govern
			}

			// Govern the managed object
			this.governance.governManagedObject(extension, governanceContext);
		}

		// Managed object successfully under governance
		return true;
	}

	@Override
	public boolean enforceGovernance(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext context) throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Must lock to check if managed objects ready
		ProcessState processState = this.threadState.getProcessState();
		synchronized (processState.getProcessLock()) {

			// Determine if active.
			// As Managed Object triggers creation, may not always be active.
			boolean isActive = this.isActive();

			// Ensure managed objects are ready
			if (isActive) {
				if (!this.isManagedObjectsReady(jobContext, jobNode,
						activateSet, context)) {
					return false; // not ready to enforce
				}
			}

			try {
				// Enforce the governance (if activated)
				if (isActive) {
					this.governance.enforceGovernance(governanceContext);
				}

			} finally {
				// Governance enforced (as best), so now unregister
				this.unregisterGovernance(activateSet, currentTeam);
			}
		}

		// Governance successfully enforced
		return true;
	}

	@Override
	public boolean disregardGovernance(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext context) throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Must lock to check if managed objects ready
		ProcessState processState = this.threadState.getProcessState();
		synchronized (processState.getProcessLock()) {

			// Determine if active.
			// As Managed Object triggers creation, may not always be active.
			boolean isActive = this.isActive();

			// Ensure managed objects are ready
			if (isActive) {
				if (!this.isManagedObjectsReady(jobContext, jobNode,
						activateSet, context)) {
					return false; // not ready to disregard
				}
			}

			try {
				// Disregard the governance (if activated)
				if (isActive) {
					this.governance.disregardGovernance(governanceContext);
				}

			} finally {
				// Governance disregarded (as best), so now unregister
				this.unregisterGovernance(activateSet, currentTeam);
			}
		}

		// Governance successfully disregarded
		return true;
	}

	/**
	 * Indicates if the {@link ManagedObject} instances are ready.
	 * 
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param context
	 *            {@link ContainerContext}.
	 * @return <code>true</code> if {@link ManagedObject} instances are ready.
	 */
	private boolean isManagedObjectsReady(JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context) {

		// Ensure managed objects are ready
		for (ActiveGovernanceManager<I, F> governance : this.activeGovernances) {
			if (!governance.isManagedObjectReady(jobContext, jobNode,
					activateSet, context)) {
				return false; // not ready for governance action
			}
		}

		// Ready for governance action
		return true;
	}

}