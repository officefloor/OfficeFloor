/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;

/**
 * {@link GovernanceContainer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceContainerImpl<I, F extends Enum<F>> implements
		GovernanceContainer<I>, GovernanceControl<F> {

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> metaData;

	/**
	 * {@link ProcessState} lock.
	 */
	private final Object processLock;

	/**
	 * {@link ActiveGovernanceManager} instances.
	 */
	private final List<ActiveGovernanceManager> activeGovernances = new LinkedList<ActiveGovernanceManager>();

	/**
	 * {@link Governance}.
	 */
	private Governance<I, F> governance = null;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param processLock
	 *            {@link ProcessState} lock.
	 */
	public GovernanceContainerImpl(GovernanceMetaData<I, F> metaData,
			Object processLock) {
		this.metaData = metaData;
		this.processLock = processLock;
	}

	/**
	 * Unregisters the {@link Governance}.
	 */
	private void unregisterGovernance() {

		// Unregister managed objects from governance
		for (ActiveGovernanceManager activeGovernance : this.activeGovernances) {
			activeGovernance.unregisterManagedObject();
		}

		// Disregard the governance
		this.governance = null;
	}

	/*
	 * ==================== GovernanceContainer =========================
	 */

	@Override
	public boolean isActive() {

		// Access Point: Work
		// Locks: ThreadState, ProcessState

		return (this.governance != null);
	}

	@Override
	public ActiveGovernance createActiveGovernance(I extensionInterface,
			ManagedObjectContainer managedobjectContainer) {

		// Access Point: Work
		// Locks: ThreadState, ProcessState

		// Ensure governance active
		if (this.governance == null) {
			throw new IllegalStateException("Can only create "
					+ ActiveGovernance.class.getSimpleName() + " for active "
					+ Governance.class.getSimpleName());
		}

		// Create the active governance
		ActiveGovernanceManager activeGovernance = this.metaData
				.createActiveGovernance(this, this.governance,
						extensionInterface, managedobjectContainer);

		// Register the active governance
		this.activeGovernances.add(activeGovernance);

		// Return the active governance
		return activeGovernance.getActiveGovernance();
	}

	@Override
	public void activateGovernance(ContainerContext context) {
		FlowMetaData<?> flow = this.metaData.getActivateFlowMetaData();
		context.addSetupJob(flow, this);
	}

	@Override
	public void enforceGovernance(ContainerContext context) {
		FlowMetaData<?> flow = this.metaData.getEnforceFlowMetaData();
		context.addSetupJob(flow, this);
	}

	@Override
	public void disregardGovernance(ContainerContext context) {
		FlowMetaData<?> flow = this.metaData.getDisregardFlowMetaData();
		context.addSetupJob(flow, this);
	}

	/*
	 * ==================== GovernanceControl =========================
	 */

	@Override
	public void activateGovernance(TaskContext<?, ?, F> taskContext)
			throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		synchronized (this.processLock) {

			// Determine if already active governance
			if (this.governance != null) {
				return;
			}

			// Create the governance
			this.governance = this.metaData.createGovernance();
		}
	}

	@Override
	public void enforceGovernance(TaskContext<?, ?, F> taskContext)
			throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		synchronized (this.processLock) {

			// Create governance context from task context
			GovernanceContext<F> governanceContext = this.metaData
					.createGovernanceContext(taskContext);

			// Enforce the governance
			this.governance.enforceGovernance(governanceContext);

			// Governance applied, so now unregister governance
			this.unregisterGovernance();
		}
	}

	@Override
	public void disregardGovernance(TaskContext<?, ?, F> taskContext)
			throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		synchronized (this.processLock) {

			// Create governance context from task context
			GovernanceContext<F> governanceContext = this.metaData
					.createGovernanceContext(taskContext);

			// Disregard the governance
			this.governance.disregardGovernance(governanceContext);

			// Unregister the governance
			this.unregisterGovernance();
		}
	}

}