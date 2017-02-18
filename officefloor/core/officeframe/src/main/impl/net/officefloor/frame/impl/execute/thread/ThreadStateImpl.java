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

import java.util.logging.Level;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.impl.execute.flow.FlowImpl;
import net.officefloor.frame.impl.execute.function.AbstractDelegateFunctionState;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionContext;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadContext;
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
	private static class ActiveThreadState implements ThreadContext, FunctionContext {

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * Flag indicating if the {@link ThreadState} is safe on the current
		 * {@link Thread}.
		 */
		public final boolean isThreadStateSafe;

		/**
		 * Recursive depth of this {@link ActiveThreadState} within the
		 * {@link Thread}.
		 */
		private final int threadStateRecursiveDepth;

		/**
		 * Previous {@link ActiveThreadState}. This enables
		 * {@link FunctionLogic} to be executed within the context of another
		 * {@link FunctionLogic}. Will be <code>null</code> for top level
		 * {@link ThreadState} of {@link Thread}.
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
		 * @param threadStateRecursiveDepth
		 *            Recursive depth of this {@link ActiveThreadState} within
		 *            the {@link Thread}.
		 * @param previousActiveThreadState
		 *            Previous {@link ActiveThreadState} on the {@link Thread}.
		 *            May be <code>null</code>.
		 */
		private ActiveThreadState(ThreadState threadState, boolean isThreadStateSafe, int threadStateRecursiveDepth,
				ActiveThreadState previousActiveThreadState) {
			this.threadState = threadState;
			this.isThreadStateSafe = isThreadStateSafe;
			this.threadStateRecursiveDepth = threadStateRecursiveDepth;
			this.previousActiveThreadState = previousActiveThreadState;
		}

		/*
		 * =========================== ThreadContext ===========================
		 */

		@Override
		public FunctionState executeFunction(FunctionState function) throws Throwable {
			return function.execute(this);
		}

		@Override
		public FunctionState handleEscalation(FunctionState function, Throwable escalation) {
			return function.handleEscalation(escalation, this);
		}

		/*
		 * =========================== FunctionState ===========================
		 */

		@Override
		public FunctionState executeDelegate(FunctionState delegate) throws Throwable {
			return delegate.execute(this);
		}

		@Override
		public FunctionState handleDelegateEscalation(FunctionState delegate, Throwable escalation) {
			return delegate.handleEscalation(escalation, this);
		}
	}

	/**
	 * Attaches the {@link ThreadState} to the {@link Thread}.
	 * 
	 * @param threadState
	 *            {@link ThreadState} to attached to the {@link Thread}.
	 * @param isThreadStateSafe
	 *            Indicates if the execution is {@link ThreadState} safe.
	 * @return {@link ThreadContext} for executing the {@link ThreadState}
	 *         attached to the {@link Thread}.
	 */
	public static ThreadContext attachThreadStateToThread(ThreadState threadState, boolean isThreadStateSafe) {

		// Obtain the possible existing thread state on thread
		ActiveThreadState previous = activeThreadState.get();

		// Determine new depth
		int nextThreadStateDepth = previous != null ? previous.threadStateRecursiveDepth + 1 : 1;

		// Attach the next thread state to the thread
		ActiveThreadState context = new ActiveThreadState(threadState, isThreadStateSafe, nextThreadStateDepth,
				previous);
		activeThreadState.set(context);

		// Return the function context
		return context;
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
	 * Obtains the current {@link ThreadContext}.
	 * 
	 * @return Current {@link ThreadContext}.
	 */
	public static ThreadContext currentThreadContext() {

		// Obtain the context attached to the thread
		ActiveThreadState current = activeThreadState.get();

		// Ensure have current (even if temporary before loop)
		if (current == null) {
			current = new ActiveThreadState(null, false, 0, null);
		}

		// Return the current thread state
		return current;
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
	 * {@link ProcessState} for this {@link ThreadState}.
	 */
	private final ProcessState processState;

	/**
	 * {@link FlowCompletion}.
	 */
	private final FlowCompletion completion;

	/**
	 * {@link ThreadProfiler}.
	 */
	private final ThreadProfiler profiler;

	/**
	 * {@link EscalationLevel} of this {@link ThreadState}.
	 */
	private EscalationLevel escalationLevel = EscalationLevel.OFFICE;

	/**
	 * Initiate with {@link ProcessState} {@link FlowCallback}.
	 * 
	 * @param threadMetaData
	 *            {@link ThreadMetaData} for this {@link ThreadState}.
	 * @param callback
	 *            {@link ProcessState} {@link FlowCallback}.
	 * @param callbackThreadState
	 *            {@link FlowCallback} {@link ThreadState}.
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}. May be <code>null</code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData, FlowCallback callback, ThreadState callbackThreadState,
			ProcessState processState, ProcessProfiler processProfiler) {
		this(threadMetaData, null, callback, callbackThreadState, processState, processProfiler);
	}

	/**
	 * Initiate.
	 * 
	 * @param threadMetaData
	 *            {@link ThreadMetaData} for this {@link ThreadState}.
	 * @param completion
	 *            {@link FlowCompletion} for this {@link ThreadState}.
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}. May be <code>null</code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData, FlowCompletion completion, ProcessState processState,
			ProcessProfiler processProfiler) {
		this(threadMetaData, completion, null, null, processState, processProfiler);
	}

	/**
	 * Instantiate using appropriate {@link FlowCompletion}.
	 * 
	 * @param threadMetaData
	 *            {@link ThreadMetaData} for this {@link ThreadState}.
	 * @param completion
	 *            {@link FlowCompletion} for this {@link ThreadState}.
	 * @param callback
	 *            {@link ProcessState} invoked {@link FlowCallback}.
	 * @param callbackThreadState
	 *            {@link FlowCallback} {@link ThreadState}.
	 * @param processState
	 *            {@link ProcessState} for this {@link ThreadState}.
	 * @param processProfiler
	 *            {@link ProcessProfiler}. May be <code>null</code>.
	 */
	private ThreadStateImpl(ThreadMetaData threadMetaData, FlowCompletion completion, FlowCallback callback,
			ThreadState callbackThreadState, ProcessState processState, ProcessProfiler processProfiler) {
		this.threadMetaData = threadMetaData;
		this.processState = processState;

		// Determine completion
		if (completion != null) {
			// Supplied flow completion
			this.completion = completion;

		} else if (callback != null) {
			// Process invoked callback, so determine callback thread state
			if (callbackThreadState == null) {
				// No provided thread state, so attempt to determine one
				ActiveThreadState active = activeThreadState.get();
				if ((active != null) && (active.threadState != null)) {
					// Use currently active thread state
					callbackThreadState = active.threadState;
				} else {
					// Fall back to this thread state
					callbackThreadState = this;
				}
			}

			// Specify process flow completion
			this.completion = new ProcessFlowCompletion(callbackThreadState, callback);

		} else {
			// No completion
			this.completion = null;
		}

		// Create array to reference the managed objects
		ManagedObjectMetaData<?>[] moMetaData = this.threadMetaData.getManagedObjectMetaData();
		this.managedObjectContainers = new ManagedObjectContainer[moMetaData.length];

		// Create the array to reference the governances
		GovernanceMetaData<?, ?>[] governanceMetaData = this.threadMetaData.getGovernanceMetaData();
		this.governanceContainers = new GovernanceContainer[governanceMetaData.length];

		// Create thread profiler
		this.profiler = (processProfiler == null ? null : processProfiler.addThreadState(this));
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
	public FunctionState then(FunctionState function, FunctionState thenFunction) {
		return new ThenFunction(function, thenFunction);
	}

	@Override
	public <R, T extends Throwable> R runProcessSafeOperation(ProcessSafeOperation<R, T> operation) throws T {

		// Obtain the main thread
		ThreadState mainThreadState = this.processState.getMainThreadState();

		// Obtain the active thread state
		ActiveThreadState active = activeThreadState.get();
		ThreadState activeThreadState = (active != null ? active.threadState : null);

		// Determine if running on same thread state
		if (mainThreadState == activeThreadState) {
			// Safe on main thread, so no additional lock required
			return operation.run();

		} else {
			// Not safe as different thread state, so lock on main thread state
			synchronized (mainThreadState) {
				return operation.run();
			}
		}
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

		// Cancel all flows
		FunctionState cleanUpFunctions = LinkedListSetPromise.all(this.activeFlows, (flow) -> flow.cancel());

		// Handle based on escalation level
		switch (this.escalationLevel) {
		case OFFICE:
			EscalationFlow escalationFlow = this.threadMetaData.getOfficeEscalationProcedure()
					.getEscalation(escalation);
			if (escalationFlow != null) {
				Flow flow = this.createFlow(null);

				// Next escalation will be flow completion
				this.escalationLevel = EscalationLevel.FLOW_COMPLETION;
				return Promise.then(cleanUpFunctions, flow.createManagedFunction(escalation,
						escalationFlow.getManagedFunctionMetaData(), false, null));
			}

		case FLOW_COMPLETION:
			// Outside normal handling, so clean up thread state

			// Disregard any active governance
			for (int i = 0; i < this.governanceContainers.length; i++) {
				GovernanceContainer<?> container = this.governanceContainers[i];
				if ((container != null) && (container.isGovernanceActive())) {
					cleanUpFunctions = Promise.then(cleanUpFunctions, container.disregardGovernance());
				}
			}

			// Notify flow completion (if provided)
			if (this.completion != null) {
				// Next is to escalate to OfficeFloor
				this.escalationLevel = EscalationLevel.OFFICE_FLOOR;
				return Promise.then(cleanUpFunctions, this.completion.complete(escalation));
			}

		case OFFICE_FLOOR:
			escalationFlow = this.threadMetaData.getOfficeFloorEscalation();
			if (escalationFlow != null) {
				Flow flow = this.createFlow(null);

				// Any further failure, log (not happen most applications)
				this.escalationLevel = EscalationLevel.LOG;
				return Promise.then(cleanUpFunctions, flow.createManagedFunction(escalation,
						escalationFlow.getManagedFunctionMetaData(), false, null));
			}

		case LOG:
			OfficeFloorImpl.getFrameworkLogger().log(Level.SEVERE, "Unhandle escalation", escalation);
		}

		// Logged error
		return cleanUpFunctions;
	}

	@Override
	public FunctionState flowComplete(Flow flow, boolean isCancel) {

		// Remove flow from active flow listing
		if (this.activeFlows.removeEntry(flow)) {

			// Enforce any active governance
			for (int i = 0; i < this.governanceContainers.length; i++) {
				GovernanceContainer<?> container = this.governanceContainers[i];
				if (container != null) {

					// Enforce the possible active governance
					if (container.isGovernanceActive()) {
						// New functions of flow (completes thread again)
						return container.enforceGovernance();
					}
				}
			}

			// Last functional flow, so thread state is now complete

			// Deactivate the governance
			FunctionState cleanUpFunctions = null;
			for (int i = 0; i < this.governanceContainers.length; i++) {
				GovernanceContainer<?> container = this.governanceContainers[i];
				if (container != null) {
					cleanUpFunctions = Promise.then(cleanUpFunctions, container.deactivateGovernance());
				}
			}

			// Unload managed objects (some may not have been used)
			for (int i = 0; i < this.managedObjectContainers.length; i++) {
				ManagedObjectContainer container = this.managedObjectContainers[i];
				if (container != null) {
					cleanUpFunctions = Promise.then(cleanUpFunctions, container.unloadManagedObject());
				}
			}

			// Notify completion of thread state
			if (!isCancel && (this.completion != null)) {
				cleanUpFunctions = Promise.then(cleanUpFunctions, this.completion.complete(null));
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
			container = new ManagedObjectContainerImpl(this.threadMetaData.getManagedObjectMetaData()[index], this);
			this.managedObjectContainers[index] = container;
		}
		return container;
	}

	@Override
	public GovernanceContainer<?> getGovernanceContainer(int index) {
		// Lazy load the Governance Container
		GovernanceContainer<?> container = this.governanceContainers[index];
		if (container == null) {
			container = this.threadMetaData.getGovernanceMetaData()[index].createGovernanceContainer(this, index);
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
	public FunctionState registerThreadProfiler() {
		return this.profiler;
	}

	@Override
	public void profile(ManagedFunctionLogicMetaData functionMetaData) {

		// Only profile if have profiler
		if (this.profiler == null) {
			return;
		}

		// Profile the function execution
		this.profiler.profileManagedFunction(functionMetaData);
	}

	/**
	 * {@link Escalation} level of a {@link ThreadState}.
	 */
	private static enum EscalationLevel {
		OFFICE, FLOW_COMPLETION, OFFICE_FLOOR, LOG
	}

	/**
	 * {@link FlowCompletion} for an invoked {@link ProcessState}.
	 */
	private class ProcessFlowCompletion extends AbstractLinkedListSetEntry<FlowCompletion, ManagedFunctionContainer>
			implements FlowCompletion {

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * {@link FlowCallback}.
		 */
		private final FlowCallback callback;

		/**
		 * Instantiate.
		 * 
		 * @param threadState
		 *            {@link ThreadState}.
		 * @param callback
		 *            {@link FlowCallback}.
		 */
		public ProcessFlowCompletion(ThreadState threadState, FlowCallback callback) {
			this.threadState = threadState;
			this.callback = callback;
		}

		@Override
		public ManagedFunctionContainer getLinkedListSetOwner() {
			throw new IllegalStateException("Should never be added to a list");
		}

		@Override
		public FunctionState complete(Throwable escalation) {
			return new CompleteFunctionState(escalation);
		}

		/**
		 * {@link FunctionState} to complete the {@link FlowCallback}.
		 */
		private class CompleteFunctionState extends AbstractLinkedListSetEntry<FunctionState, Flow>
				implements FunctionState {

			/**
			 * {@link Escalation}.
			 */
			private final Throwable escalation;

			/**
			 * Instantiate.
			 * 
			 * @param escalation
			 *            {@link Escalation}.
			 */
			public CompleteFunctionState(Throwable escalation) {
				this.escalation = escalation;
			}

			@Override
			public String toString() {
				return "ThreadState " + Integer.toHexString(ThreadStateImpl.this.hashCode())
						+ " callback with exception " + this.escalation;
			}

			@Override
			public ThreadState getThreadState() {
				return ProcessFlowCompletion.this.threadState;
			}

			@Override
			public FunctionState execute(FunctionContext context) throws Throwable {

				// Undertake callback
				ProcessFlowCompletion.this.callback.run(this.escalation);

				// Process now complete
				return null;
			}
		}
	}

	/**
	 * Then {@link FunctionState}.
	 */
	private static class ThenFunction extends AbstractDelegateFunctionState {

		/**
		 * Then {@link FunctionState}.
		 */
		protected final FunctionState thenFunction;

		/**
		 * Creation by static methods.
		 * 
		 * @param delegate
		 *            Delegate {@link FunctionState} to complete it and all
		 *            produced {@link FunctionState} instances before
		 *            continuing.
		 * @param thenFunction
		 *            Then {@link FunctionState}.
		 */
		private ThenFunction(FunctionState delegate, FunctionState thenFunction) {
			super(delegate);
			this.thenFunction = thenFunction;
		}

		@Override
		public String toString() {
			return this.delegate.toString();
		}

		/*
		 * =================== FunctionState ==============================
		 */

		@Override
		public FunctionState execute(FunctionContext context) throws Throwable {
			FunctionState next = this.delegate.execute(context);
			if (next != null) {
				return new ThenFunction(next, this.thenFunction);
			}
			return this.thenFunction;
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, FunctionContext context) {
			FunctionState handler = this.delegate.handleEscalation(escalation, context);
			if (handler != null) {
				return new ThenFunction(handler, this.thenFunction);
			}
			return this.thenFunction;
		}

		@Override
		public FunctionState cancel() {
			return Promise.then(this.delegate.cancel(), this.thenFunction.cancel());
		}
	}

}