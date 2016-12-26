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

import net.officefloor.frame.impl.execute.function.FlowImpl;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
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
	private static class ActiveThreadState {

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
		 * Previous {@link ActiveThreadState}. This enables
		 * {@link FunctionLogic} to be executed within the context of another
		 * {@link FunctionLogic}.
		 */
		private final ActiveThreadState previousActiveThreadState;

		/**
		 * Instantiate.
		 * 
		 * @param threadState
		 *            Active {@link ThreadState}.
		 * @param isThreadStateSafe
		 *            Flag indicating if the {@link ThreadState} is safe on the
		 *            current {@link Thread}.
		 */
		public ActiveThreadState(ThreadState threadState, boolean isThreadStateSafe,
				ActiveThreadState previousActiveThreadState) {
			this.threadState = threadState;
			this.isThreadStateSafe = isThreadStateSafe;
			this.previousActiveThreadState = previousActiveThreadState;
		}
	}

	/**
	 * Attaches the {@link ThreadState} to the {@link Thread}.
	 * 
	 * @param threadState
	 *            {@link ThreadState} to attached to the {@link Thread}.
	 * @param isThreadStateSafe
	 *            Indicates if the execution is {@link ThreadState} safe.
	 */
	public static void attachThreadStateToThread(ThreadState threadState, boolean isThreadStateSafe) {
		ActiveThreadState previous = activeThreadState.get();
		activeThreadState.set(new ActiveThreadState(threadState, isThreadStateSafe, previous));
	}

	/**
	 * Detaches the {@link ThreadState} from the {@link Thread}.
	 */
	public static void detachThreadStateFromThread() {
		ActiveThreadState active = activeThreadState.get();
		if (active == null) {
			throw new IllegalStateException(
					"No " + ThreadState.class.getSimpleName() + " attached to " + Thread.class.getSimpleName());
		}
		activeThreadState.set(active.previousActiveThreadState);
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
	private final GovernanceContainer<?>[] governanceContainers;

	/**
	 * {@link AdministratorContainer} instances for this {@link ThreadState}.
	 */
	private final AdministratorContainer<?>[] administratorContainers;

	/**
	 * {@link ProcessState} for this {@link ThreadState}.
	 */
	private final ProcessState processState;

	/**
	 * {@link AssetManager} for this {@link ThreadState}.
	 */
	private final AssetManager assetManager;

	/**
	 * {@link FlowCompletion}.
	 */
	private final FlowCompletion completion;

	/**
	 * {@link ThreadProfiler}.
	 */
	private final ThreadProfiler profiler;

	/**
	 * Initiate.
	 * 
	 * @param threadMetaData
	 *            {@link ThreadMetaData} for this {@link ThreadState}.
	 * @param assetManager
	 *            {@link AssetManager} for this {@link ThreadState}.
	 * @param completion
	 *            {@link FlowCompletion} for this {@link ThreadState}.
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}. May be <code>null</code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData, AssetManager assetManager, FlowCompletion completion,
			ProcessState processState, ProcessProfiler processProfiler) {
		this.threadMetaData = threadMetaData;
		this.processState = processState;
		this.assetManager = assetManager;
		this.completion = completion;

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
	public ThreadMetaData getThreadMetaData() {
		return this.threadMetaData;
	}

	@Override
	public Flow createFlow(FlowCompletion completion) {

		// Create and register the activate flow
		Flow flow = new FlowImpl(completion, this);
		this.activeFlows.addEntry(flow);

		// Return the flow
		return flow;
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation) {

		// Clean up functions
		FunctionState cleanUpFunctions = null;

		// Deactivate any active governance
		for (int i = 0; i < this.governanceContainers.length; i++) {
			GovernanceContainer<?> container = this.governanceContainers[i];
			if ((container != null) && (container.isGovernanceActive())) {
				cleanUpFunctions = Promise.then(cleanUpFunctions, container.disregardGovernance());
			}
		}

		// Cancel all flows
		Flow flow = this.activeFlows.purgeEntries();
		while (flow != null) {
			cleanUpFunctions = Promise.then(cleanUpFunctions, flow.cancel(escalation));
			flow = flow.getNext();
		}

		// Unload managed objects (some may not have been used)
		for (int i = 0; i < this.managedObjectContainers.length; i++) {
			ManagedObjectContainer container = this.managedObjectContainers[i];
			if (container != null) {
				cleanUpFunctions = Promise.then(cleanUpFunctions, container.unloadManagedObject());
			}
		}

		// Determine if handle by completion
		if (this.completion != null) {
			EscalationFlow escalationFlow = this.completion.getFlowEscalationProcedure().getEscalation(escalation);
			if (escalationFlow != null) {
				// Undertake escalation (that escalates to main thread)
				return Promise.then(cleanUpFunctions, this.processState
						.spawnThreadState(escalationFlow.getManagedFunctionMetaData(), escalation, null));
			}

		}

		// Not handled by completion so handle by thread escalation procedures
		// Determine if require a global escalation
		if (escalationNode == null) {
			// No escalation, so use global escalation
			EscalationFlow globalEscalation = null;
			switch (threadState.getEscalationLevel()) {
			case OFFICE:
				// Tried office, now at invocation
				globalEscalation = processState.getInvocationEscalation();
				if (globalEscalation != null) {
					threadState.setEscalationLevel(EscalationLevel.INVOCATION_HANDLER);
					break;
				}

			case OFFICE_FLOOR:
				// Should not be escalating at office floor.
				// Allow stderr failure to pick up issue.
				throw escalationCause;

			default:
				throw new IllegalStateException("Should not be in state " + threadState.getEscalationLevel());
			}

			// Create the global escalation
			escalationNode = this.createEscalationFunction(globalEscalation.getManagedFunctionMetaData(),
					escalationCause, null);
		}

		// Activate escalation node
		return escalationNode;
	}

	@Override
	public FunctionState flowComplete(Flow flow) {

		// Clean up functions
		FunctionState cleanUpFunctions = null;

		// Remove flow from active flow listing
		if (this.activeFlows.removeEntry(flow)) {

			// Last flow, so thread state is now complete

			// Deactivate any active governance
			GovernanceDeactivationStrategy deactivationStrategy = this.threadMetaData
					.getGovernanceDeactivationStrategy();
			switch (deactivationStrategy) {
			case ENFORCE:
				// Enforce any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?> container = this.governanceContainers[i];
					if ((container != null) && (container.isGovernanceActive())) {
						cleanUpFunctions = Promise.then(cleanUpFunctions, container.enforceGovernance());
					}
				}
				break;

			case DISREGARD:
				// Disregard any active governance
				for (int i = 0; i < this.governanceContainers.length; i++) {
					GovernanceContainer<?> container = this.governanceContainers[i];
					if ((container != null) && (container.isGovernanceActive())) {
						cleanUpFunctions = Promise.then(cleanUpFunctions, container.disregardGovernance());
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
					cleanUpFunctions = Promise.then(cleanUpFunctions, container.unloadManagedObject());
				}
			}

			// Notify completion of thread state
			if (this.completion != null) {
				cleanUpFunctions = Promise.then(cleanUpFunctions, this.completion.getFlowCompletionFunction());
			}

			// Thread complete
			return Promise.then(cleanUpFunctions, this.processState.threadComplete(this));
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
	public GovernanceContainer<?> getGovernanceContainer(int index) {
		// Lazy load the Governance Container
		GovernanceContainer<?> container = this.governanceContainers[index];
		if (container == null) {
			container = this.threadMetaData.getGovernanceMetaData()[index].createGovernanceContainer(this);
			this.governanceContainers[index] = container;
		}
		return container;
	}

	@Override
	public boolean isGovernanceActive(int index) {
		GovernanceContainer<?> container = this.governanceContainers[index];
		return (container != null) ? container.isGovernanceActive() : false;
	}

	@Override
	public AdministratorContainer<?> getAdministratorContainer(int index) {
		// Lazy load the Administrator Container
		AdministratorContainer<?> container = this.administratorContainers[index];
		if (container == null) {
			container = this.threadMetaData.getAdministratorMetaData()[index].createAdministratorContainer(this);
			this.administratorContainers[index] = container;
		}
		return container;
	}

	@Override
	public void profile(ManagedFunctionLogicMetaData jobMetaData) {

		// Only profile if have profiler
		if (this.profiler == null) {
			return;
		}

		// Profile the job execution
		this.profiler.profileJob(jobMetaData);
	}

}