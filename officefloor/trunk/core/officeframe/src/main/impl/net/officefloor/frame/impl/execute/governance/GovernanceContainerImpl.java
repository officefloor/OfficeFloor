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

import net.officefloor.frame.api.build.GovernanceFactory;
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
		GovernanceContainer<I>, GovernanceControl<I, F> {

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> metaData;

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState;

	/**
	 * Index of this {@link Governance} registered within the
	 * {@link ProcessState}.
	 */
	private final int registeredIndex;

	/**
	 * {@link ActiveGovernanceManager} instances.
	 */
	private final List<ActiveGovernanceManager> activeGovernances = new LinkedList<ActiveGovernanceManager>();

	/**
	 * {@link Governance}.
	 */
	private Governance<? super I, F> governance = null;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param processState
	 *            {@link ProcessState}.
	 * @param registeredIndex
	 *            Index of this {@link Governance} registered within the
	 *            {@link ProcessState}.
	 */
	public GovernanceContainerImpl(GovernanceMetaData<I, F> metaData,
			ProcessState processState, int registeredIndex) {
		this.metaData = metaData;
		this.processState = processState;
		this.registeredIndex = registeredIndex;
	}

	/**
	 * Unregisters the {@link Governance}.
	 */
	private void unregisterGovernance() {

		// Unregister managed objects from governance
		for (ActiveGovernanceManager activeGovernance : this.activeGovernances) {
			activeGovernance.unregisterManagedObject();
		}

		// Unregister the Governance from Process
		this.processState.governanceComplete(this);

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
	public ActiveGovernance createActiveGovernance(I extensionInterface,
			ManagedObjectContainer managedobjectContainer,
			int managedObjectContainerRegisteredIndex) {

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
				.createActiveGovernance(this, this, extensionInterface,
						managedobjectContainer,
						managedObjectContainerRegisteredIndex);

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

		synchronized (this.processState.getProcessLock()) {

			// Determine if already active governance
			if (this.governance != null) {
				return;
			}

			// Create the governance
			GovernanceFactory<? super I, F> factory = this.metaData
					.getGovernanceFactory();
			this.governance = factory.createGovernance();
		}
	}

	@Override
	public void governManagedObject(I extension,
			TaskContext<?, ?, F> taskContext) throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Create the governance context
		GovernanceContext<F> governanceContext = this.metaData
				.createGovernanceContext(taskContext);

		synchronized (this.processState.getProcessLock()) {

			// Govern the managed object
			this.governance.governManagedObject(extension, governanceContext);
		}
	}

	@Override
	public void enforceGovernance(TaskContext<?, ?, F> taskContext)
			throws Throwable {

		// Access Point: Job
		// Locks: ThreadState

		// Create governance context from task context
		GovernanceContext<F> governanceContext = this.metaData
				.createGovernanceContext(taskContext);

		synchronized (this.processState.getProcessLock()) {

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

		// Create governance context from task context
		GovernanceContext<F> governanceContext = this.metaData
				.createGovernanceContext(taskContext);

		synchronized (this.processState.getProcessLock()) {

			// Disregard the governance
			this.governance.disregardGovernance(governanceContext);

			// Unregister the governance
			this.unregisterGovernance();
		}
	}

}