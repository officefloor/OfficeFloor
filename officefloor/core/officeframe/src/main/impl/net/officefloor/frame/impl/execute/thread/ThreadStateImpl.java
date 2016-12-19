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
package net.officefloor.frame.impl.execute.thread;

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.impl.execute.jobnode.FlowImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCallbackJobNodeFactory;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadProfiler;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Implementation of the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadStateImpl extends AbstractLinkedListSetEntry<ThreadState, ProcessState> implements ThreadState {

	/**
	 * {@link ActiveThreadState} for the executing {@link Thread}.
	 */
	private static final ThreadLocal<ActiveThreadState> activeThreadState = new ThreadLocal<>();

	/**
	 * Active {@link ThreadState}.
	 */
	public static class ActiveThreadState {

		/**
		 * {@link ThreadState}.
		 */
		public final ThreadState threadState;

		/**
		 * Flag indicating if the {@link ThreadState} is safe on the current
		 * {@link Thread}.
		 */
		public final boolean isThreadStateSafe;

		/**
		 * Instantiate.
		 * 
		 * @param threadState
		 *            Active {@link ThreadState}.
		 * @param isThreadStateSafe
		 *            Flag indicating if the {@link ThreadState} is safe on the
		 *            current {@link Thread}.
		 */
		public ActiveThreadState(ThreadState threadState, boolean isThreadStateSafe) {
			this.threadState = threadState;
			this.isThreadStateSafe = isThreadStateSafe;
		}
	}

	/**
	 * Obtains the active {@link ThreadState} on the current {@link Thread}.
	 * 
	 * @return Active {@link ThreadState} on the current {@link Thread}. May be
	 *         <code>null</code> if no active {@link ThreadState} on
	 *         {@link Thread}.
	 */
	public static ActiveThreadState getActiveThreadState() {
		return activeThreadState.get();
	}

	/**
	 * Active {@link Flow} instances for this {@link ThreadState}.
	 */
	protected final LinkedListSet<Flow, ThreadState> activeFlows = new StrictLinkedListSet<Flow, ThreadState>() {
		@Override
		protected ThreadState getOwner() {
			return ThreadStateImpl.this;
		}
	};

	/**
	 * {@link ThreadMetaData} for this {@link ThreadState}.
	 */
	private final ThreadMetaData threadMetaData;

	/**
	 * {@link ManagedObjectContainer} instances for this {@link ThreadState}.
	 */
	private final ManagedObjectContainer[] managedObjectContainers;

	/**
	 * {@link GovernanceContainer} instances for the {@link ProcessState}.
	 */
	private final GovernanceContainer<?, ?>[] governanceContainers;

	/**
	 * {@link AdministratorContainer} instances for this {@link ThreadState}.
	 */
	private final AdministratorContainer<?, ?>[] administratorContainers;

	/**
	 * {@link ProcessState} for this {@link ThreadState}.
	 */
	private final ProcessState processState;

	/**
	 * {@link AssetManager} for this {@link ThreadState}.
	 */
	private final AssetManager assetManager;

	/**
	 * {@link FlowCallbackJobNodeFactory}.
	 */
	private final FlowCallbackJobNodeFactory callbackFactory;

	/**
	 * {@link ThreadProfiler}.
	 */
	private final ThreadProfiler profiler;

	/**
	 * Flag indicating that looking for {@link EscalationFlow}.
	 */
	private boolean isEscalating = false;

	/**
	 * Failure of the {@link ThreadState}.
	 */
	private Throwable failure = null;

	/**
	 * {@link EscalationLevel} for this {@link ThreadState}.
	 */
	private EscalationLevel escalationLevel = EscalationLevel.FLOW;

	/**
	 * Initiate.
	 * 
	 * @param threadMetaData
	 *            {@link ThreadMetaData} for this {@link ThreadState}.
	 * @param assetManager
	 *            {@link AssetManager} for this {@link ThreadState}.
	 * @param callbackFactory
	 *            {@link FlowCallbackJobNodeFactory} to create the
	 *            {@link FlowCallback} on completion of this
	 *            {@link ThreadState}. May be <code>null</code>.
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}. May be <code>null</code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData, AssetManager assetManager,
			FlowCallbackJobNodeFactory callbackFactory, ProcessState processState, ProcessProfiler processProfiler) {
		this.threadMetaData = threadMetaData;
		this.processState = processState;
		this.assetManager = assetManager;
		this.callbackFactory = callbackFactory;

		// Create array to reference the managed objects
		ManagedObjectMetaData<?>[] moMetaData = this.threadMetaData.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[moMetaData.length];

		// Create the array to reference the governances
		GovernanceMetaData<?, ?>[] governanceMetaData = this.threadMetaData.getGovernanceMetaData();
		this.governanceContainers = new GovernanceContainer[governanceMetaData.length];

		// Create array to reference the administrators
		AdministratorMetaData<?, ?>[] adminMetaData = this.threadMetaData.getAdministratorMetaData();
		this.administratorContainers = new AdministratorContainer[adminMetaData.length];

		// Create thread profiler
		this.profiler = (processProfiler == null ? null : processProfiler.addThread(this));
	}

	/*
	 * ====================== LinkedListSetEntry ===========================
	 */

	@Override
	public ProcessState getLinkedListSetOwner() {
		return this.processState;
	}

	/*
	 * ===================== ThreadState ==================================
	 */

	@Override
	public void attachThreadStateToThread(boolean isThreadStateSafe) {
		activeThreadState.set(new ActiveThreadState(this, isThreadStateSafe));
	}

	@Override
	public boolean isAttachedToThread() {
		ActiveThreadState active = activeThreadState.get();
		return (active != null) && (active.threadState == this);
	}

	@Override
	public boolean isThreadStateSafe() {
		ActiveThreadState active = activeThreadState.get();
		return (active != null) ? active.isThreadStateSafe : false;
	}

	@Override
	public void detachThreadStateFromThread() {
		activeThreadState.set(null);
	}

	@Override
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

	@Override
	public Throwable getFailure() {
		return this.failure;
	}

	@Override
	public void setFailure(Throwable cause) {
		this.failure = cause;
	}

	@Override
	public Flow createFlow() {

		// Create and register the activate flow
		Flow flow = new FlowImpl(this);
		this.activeFlows.addEntry(flow);

		// Return the flow
		return flow;
	}

	@Override
	public JobNode flowComplete(Flow flow) {

		// Remove Job Sequence from active Job Sequence listing
		if (this.activeFlows.removeEntry(flow)) {

			// Do nothing if searching for escalation
			if (this.isEscalating) {
				return null;
			}

			// Deactivate governance
			GovernanceDeactivationStrategy deactivationStrategy = this.threadMetaData
					.getGovernanceDeactivationStrategy();
			switch (deactivationStrategy) {
			case ENFORCE:
				// Enforce any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?, ?> container = this.governanceContainers[i];
					if (container != null) {
						JobNode enforceJobNode = container.enforceGovernance();
						if (enforceJobNode != null) {
							return enforceJobNode;
						}
					}
				}
				break;

			case DISREGARD:
				// Disregard any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?, ?> container = this.governanceContainers[i];
					if (container != null) {
						JobNode disregardJobNode = container.disregardGovernance();
						if (disregardJobNode != null) {
							return disregardJobNode;
						}
					}
				}
				break;

			default:
				throw new IllegalStateException(
						"Unknown " + GovernanceDeactivationStrategy.class.getSimpleName() + " " + deactivationStrategy);
			}

			// Unload managed objects (some may not have been used)
			for (int i = 0; i < this.managedObjectContainers.length; i++) {
				ManagedObjectContainer container = this.managedObjectContainers[i];
				if (container != null) {
					JobNode unloadJobNode = container.unloadManagedObject();
					if (unloadJobNode != null) {
						return unloadJobNode;
					}
				}
			}

			// Activate all jobs waiting on this thread permanently
			if (this.callbackFactory != null) {
				return this.callbackFactory.createJobNode(null);
			}

			// Thread complete
			return this.processState.threadComplete(this);
		}

		// Thread complete
		return null;
	}

	@Override
	public ProcessState getProcessState() {
		return this.processState;
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		// Lazy load the Managed Object Container
		ManagedObjectContainer container = this.managedObjectContainers[index];
		if (container == null) {
			container = this.threadMetaData.getManagedObjectMetaData()[index].createManagedObjectContainer(this);
			this.managedObjectContainers[index] = container;
		}
		return container;
	}

	@Override
	public boolean isGovernanceActive(int index) {
		// Determine if container is active (not created is not active).
		GovernanceContainer<?, ?> container = this.governanceContainers[index];
		return (container != null) && (container.isActive());
	}

	@Override
	public GovernanceContainer<?, ?> getGovernanceContainer(int index) {
		// Lazy load the Governance Container
		GovernanceContainer<?, ?> container = this.governanceContainers[index];
		if (container == null) {
			container = this.threadMetaData.getGovernanceMetaData()[index].createGovernanceContainer(this, index);
			this.governanceContainers[index] = container;
		}
		return container;
	}

	@Override
	public void governanceComplete(GovernanceContainer<?, ?> governanceContainer) {
		// Unregister the governance
		int index = governanceContainer.getProcessRegisteredIndex();
		this.governanceContainers[index] = null;
	}

	@Override
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {
		// Lazy load the Administrator Container
		AdministratorContainer<?, ?> container = this.administratorContainers[index];
		if (container == null) {
			container = this.threadMetaData.getAdministratorMetaData()[index].createAdministratorContainer();
			this.administratorContainers[index] = container;
		}
		return container;
	}

	@Override
	public void escalationStart(JobNode currentTaskNode) {
		this.isEscalating = true;
	}

	@Override
	public void escalationComplete(JobNode currentTaskNode) {
		this.isEscalating = false;
	}

	@Override
	public EscalationLevel getEscalationLevel() {
		return this.escalationLevel;
	}

	@Override
	public void setEscalationLevel(EscalationLevel escalationLevel) {
		this.escalationLevel = escalationLevel;
	}

	@Override
	public void profile(JobMetaData jobMetaData) {

		// Only profile if have profiler
		if (this.profiler == null) {
			return;
		}

		// Profile the job execution
		this.profiler.profileJob(jobMetaData);
	}

}