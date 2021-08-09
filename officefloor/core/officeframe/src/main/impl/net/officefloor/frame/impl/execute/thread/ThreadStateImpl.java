/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.thread;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.impl.execute.flow.FlowImpl;
import net.officefloor.frame.impl.execute.function.AbstractFunctionState;
import net.officefloor.frame.impl.execute.function.LinkedListSetPromise;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.internal.structure.AvoidTeam;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadProfiler;
import net.officefloor.frame.internal.structure.ThreadSafeOperation;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadStateContext;

/**
 * Implementation of the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadStateImpl extends AbstractLinkedListSetEntry<ThreadState, ProcessState> implements ThreadState {

	/**
	 * Reduce object creation as {@link Governance} not always used.
	 */
	private static final GovernanceContainer<?>[] NO_THREAD_GOVERNANCE = new GovernanceContainer[0];

	/**
	 * {@link ActiveThreadState} for the executing {@link Thread}.
	 */
	private static final ThreadLocal<ActiveThreadState> activeThreadState = new ThreadLocal<>();

	/**
	 * Attaches the {@link ThreadState} to the {@link Thread}.
	 * 
	 * @param threadState              {@link ThreadState} to attached to the
	 *                                 {@link Thread}.
	 * @param isRequireThreadStateSafe <code>true</code> to provide {@link Thread}
	 *                                 safety on executing the {@link FunctionState}
	 *                                 instances.
	 * @return {@link ThreadStateContext} for executing the {@link ThreadState}
	 *         attached to the {@link Thread}.
	 */
	public static ThreadStateContext attachThreadStateToThread(ThreadState threadState,
			boolean isRequireThreadStateSafe) {

		// Obtain the possible existing thread state on thread
		ActiveThreadState previous = activeThreadState.get();

		// Suspend the active (soon to be previous) thread state
		if ((previous != null) && (previous.threadState != null)) {
			suspendThread(previous.threadState);
		}

		// Determine if already locked
		ActiveThreadState lockCheck = previous;
		FOUND_PREVIOUS: while (lockCheck != null) {
			if (lockCheck.threadState == threadState) {
				break FOUND_PREVIOUS;
			}
			lockCheck = lockCheck.previousActiveThreadState;
		}

		// Attach the next thread state to the thread
		ActiveThreadState active = lockCheck != null ? new ActiveThreadState(threadState, lockCheck.lockState, previous)
				: new ActiveThreadState(threadState, previous);
		activeThreadState.set(active);

		// Determine if require locking thread state to thread
		if ((isRequireThreadStateSafe) && (!active.lockState.isThreadStateSafe)) {

			// Lock thread state to the thread
			ThreadStateImpl impl = (ThreadStateImpl) threadState;
			impl.lock.lock();
			active.lockState.isThreadStateSafe = true;
		}

		// Resume the thread
		resumeThread(threadState);

		// Return the context
		return active;
	}

	/**
	 * Detaches the {@link ThreadState} from the {@link Thread}.
	 */
	public static void detachThreadStateFromThread() {

		// Obtain the active thread state
		ActiveThreadState active = activeThreadState.get();
		if (active == null) {
			throw new IllegalStateException(
					"No " + ThreadState.class.getSimpleName() + " attached to " + Thread.class.getSimpleName());
		}

		// Suspend the thread
		suspendThread(active.threadState);

		// Reinstate previous thread state (detaching thread state)
		activeThreadState.set(active.previousActiveThreadState);

		// Resume the possible previous thread
		if (active.previousActiveThreadState != null) {
			resumeThread(active.previousActiveThreadState.threadState);
		}

		// Determine if unlock thread state from thread
		if ((active.lockState.isThreadStateSafe) && (active == active.lockState.initialActiveThreadState)) {

			// Unlock thread state
			ThreadStateImpl impl = (ThreadStateImpl) active.threadState;
			impl.lock.unlock();
		}
	}

	/**
	 * Obtains the current {@link ProcessState} {@link ProcessIdentifier}.
	 * 
	 * @return Current {@link ProcessState} {@link ProcessIdentifier} or
	 *         <code>null</code> if outside management.
	 */
	public static ProcessIdentifier currentProcessIdentifier() {

		// Obtain the context attached to the thread
		ActiveThreadState current = activeThreadState.get();

		// Obtain the possible process identifier
		return current == null ? null : current.threadState.getProcessState().getProcessIdentifier();
	}

	/**
	 * Obtains the current {@link ThreadStateContext}.
	 * 
	 * @param fallbackThreadState Fall back {@link ThreadState} if no
	 *                            {@link ThreadState} bound to {@link Thread}.
	 * @return Current {@link ThreadStateContext}.
	 */
	public static ThreadStateContext currentThreadContext(ThreadState fallbackThreadState) {

		// Obtain the context attached to the thread
		ActiveThreadState current = activeThreadState.get();

		// Ensure have current (even if temporary before loop)
		if (current == null) {
			current = new ActiveThreadState(fallbackThreadState, null);
		}

		// Return the current thread state
		return current;
	}

	/**
	 * Resumes the {@link Thread}.
	 * 
	 * @param threadState {@link ThreadState} to resume.
	 */
	private static void resumeThread(ThreadState threadState) {
		ThreadStateImpl impl = (ThreadStateImpl) threadState;

		// Determine if first thread (or no state to resume)
		if (impl.synchronisers == null) {
			return; // nothing to resume
		}

		// Resume the thread
		ThreadSynchroniser[] synchronisers = impl.synchronisers.pop();
		for (int i = 0; i < synchronisers.length; i++) {
			synchronisers[i].resumeThread();
		}
	}

	/**
	 * Suspends the {@link Thread}.
	 * 
	 * @param threadState {@link ThreadState} to suspend.
	 */
	private static void suspendThread(ThreadState threadState) {
		ThreadStateImpl impl = (ThreadStateImpl) threadState;

		// Obtain the factories
		ThreadSynchroniserFactory[] factories = impl.threadMetaData.getThreadSynchronisers();
		if (factories.length == 0) {
			return; // nothing to suspend
		}

		// Suspend the thread
		ThreadSynchroniser[] synchronisers = new ThreadSynchroniser[factories.length];
		for (int i = 0; i < factories.length; i++) {
			ThreadSynchroniser synchroniser = factories[i].createThreadSynchroniser();
			synchroniser.suspendThread();
			synchronisers[i] = synchroniser;
		}

		// Capture state
		if (impl.synchronisers == null) {
			impl.synchronisers = new LinkedList<>();
		}
		impl.synchronisers.push(synchronisers);
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
	 * {@link Lock} for the {@link ThreadState}.
	 */
	private final Lock lock = new ReentrantLock(true);

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
	 * Indicates if this {@link ThreadState} was spawned to handle an
	 * {@link Escalation}.
	 */
	private final boolean isEscalationHandling;

	/**
	 * {@link FlowCompletion}.
	 */
	private final FlowCompletion completion;

	/**
	 * {@link ThreadProfiler}.
	 */
	private final ThreadProfiler profiler;

	/**
	 * {@link ThreadSynchroniser} instances.
	 */
	private Deque<ThreadSynchroniser[]> synchronisers = null;

	/**
	 * {@link EscalationLevel} of this {@link ThreadState}.
	 */
	private EscalationLevel escalationLevel = EscalationLevel.OFFICE;

	/**
	 * {@link ThreadState} {@link Escalation} to potential {@link FlowCompletion}.
	 */
	private Throwable threadEscalation = null;

	/**
	 * {@link ThreadState} {@link EscalationCompletion} to potential
	 * {@link FlowCompletion}.
	 */
	private EscalationCompletion threadEscalationCompletion = null;

	/**
	 * Indicates if this {@link ThreadState} has been completed.
	 */
	private boolean isThreadComplete = false;

	/**
	 * Initiate with {@link ProcessState} {@link FlowCallback}.
	 * 
	 * @param threadMetaData      {@link ThreadMetaData} for this
	 *                            {@link ThreadState}.
	 * @param callback            {@link ProcessState} {@link FlowCallback}.
	 * @param callbackThreadState {@link FlowCallback} {@link ThreadState}.
	 * @param processState        {@link ProcessState} for this {@link ThreadState}.
	 * @param processProfiler     {@link ProcessProfiler}. May be <code>null</code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData, FlowCallback callback, ThreadState callbackThreadState,
			ProcessState processState, ProcessProfiler processProfiler) {

		// Initial thread of process, so never escalation thread state
		this(threadMetaData, null, callback, callbackThreadState, false, processState, processProfiler);
	}

	/**
	 * Initiate.
	 * 
	 * @param threadMetaData                  {@link ThreadMetaData} for this
	 *                                        {@link ThreadState}.
	 * @param completion                      {@link FlowCompletion} for this
	 *                                        {@link ThreadState}.
	 * @param isEscalationHandlingThreadState <code>true</code> if this
	 *                                        {@link ThreadState} was spawned to
	 *                                        handle an {@link Escalation}.
	 *                                        <code>false</code> to indicate a
	 *                                        {@link ThreadState} for normal
	 *                                        execution.
	 * @param processState                    {@link ProcessState} for this
	 *                                        {@link ThreadState}.
	 * @param processProfiler                 {@link ProcessProfiler}. May be
	 *                                        <code>null</code>.
	 */
	public ThreadStateImpl(ThreadMetaData threadMetaData, FlowCompletion completion,
			boolean isEscalationHandlingThreadState, ProcessState processState, ProcessProfiler processProfiler) {

		// Spawned thread state (possibly
		this(threadMetaData, completion, null, null, isEscalationHandlingThreadState, processState, processProfiler);
	}

	/**
	 * Instantiate using appropriate {@link FlowCompletion}.
	 * 
	 * @param threadMetaData                  {@link ThreadMetaData} for this
	 *                                        {@link ThreadState}.
	 * @param completion                      {@link FlowCompletion} for this
	 *                                        {@link ThreadState}.
	 * @param callback                        {@link ProcessState} invoked
	 *                                        {@link FlowCallback}.
	 * @param callbackThreadState             {@link FlowCallback}
	 *                                        {@link ThreadState}.
	 * @param isEscalationHandlingThreadState <code>true</code> if this
	 *                                        {@link ThreadState} was spawned to
	 *                                        handle an {@link Escalation}.
	 *                                        <code>false</code> to indicate a
	 *                                        {@link ThreadState} for normal
	 *                                        execution.
	 * @param processState                    {@link ProcessState} for this
	 *                                        {@link ThreadState}.
	 * @param processProfiler                 {@link ProcessProfiler}. May be
	 *                                        <code>null</code>.
	 */
	private ThreadStateImpl(ThreadMetaData threadMetaData, FlowCompletion completion, FlowCallback callback,
			ThreadState callbackThreadState, boolean isEscalationHandlingThreadState, ProcessState processState,
			ProcessProfiler processProfiler) {
		this.threadMetaData = threadMetaData;
		this.isEscalationHandling = isEscalationHandlingThreadState;
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

			// Specify process flow completion (on this "main" thread state)
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
		if (governanceMetaData.length == 0) {
			this.governanceContainers = NO_THREAD_GOVERNANCE;
		} else {
			this.governanceContainers = new GovernanceContainer[governanceMetaData.length];
		}

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
		return (active != null) ? active.lockState.isThreadStateSafe : false;
	}

	@Override
	public void lockThreadState() {

		// Obtain the active thread state
		ActiveThreadState active = activeThreadState.get();
		if (active == null) {
			throw new IllegalStateException("Must invoke on managed " + Thread.class.getSimpleName());
		} else if (active.threadState != this) {
			throw new IllegalStateException(
					"Another " + ThreadState.class.getSimpleName() + " active on " + Thread.class.getSimpleName());
		}

		// Lock if not already locked
		if (!active.lockState.isThreadStateSafe) {

			// Lock the thread state to the thread
			this.lock.lock();
			active.lockState.isThreadStateSafe = true;
		}
	}

	@Override
	public FunctionState then(FunctionState function, FunctionState thenFunction) {

		// Step down higher level context
		AbstractThenContext thenContext = null;
		AbstractThenContext contextToFunction = null;
		if (function instanceof AvoidTeamFunction) {
			thenContext = (AbstractThenContext) function;

			// Function is last delegated function
			contextToFunction = thenContext;
			while (contextToFunction.delegate instanceof AvoidTeamFunction) {
				contextToFunction = (AvoidTeamFunction) contextToFunction.delegate;
			}
			function = contextToFunction.delegate;
		}

		// Ensure function wrapped
		ThenFunction current;
		if (function instanceof ThenFunction) {
			current = (ThenFunction) function;
		} else {
			current = new ThenFunction(function);
		}

		// Append then function to chain
		ThenFunction append = current;
		while (append.thenFunction != null) {
			append = append.thenFunction;
		}
		append.thenFunction = new ThenFunction(thenFunction);

		// Provide then context below possible higher level context
		if (thenContext != null) {
			contextToFunction.delegate = current;
			return thenContext;

		} else {
			// Return current to continue executing
			return current;
		}
	}

	@Override
	public FunctionState runWithin(FunctionState function) {
		return new RunWithinFunction(function, function.getThreadState());
	}

	@Override
	public AvoidTeam avoidTeam(FunctionState function, TeamManagement team) {
		AvoidTeamFunction avoidFunction = new AvoidTeamFunction(team, function);
		return new AvoidTeamImpl(avoidFunction.avoidTeamTracker, avoidFunction);
	}

	@Override
	public int getMaximumFunctionChainLength() {
		return this.threadMetaData.getMaximumFunctionChainLength();
	}

	@Override
	public <R, T extends Throwable> R runThreadSafeOperation(ThreadSafeOperation<R, T> operation) throws T {

		// Obtain the active thread state
		ActiveThreadState active = activeThreadState.get();
		ThreadState activeThreadState = (active != null ? active.threadState : null);

		// Determine if running safely within this thread
		boolean isActiveThread = this == activeThreadState;
		if ((isActiveThread) && (active.lockState.isThreadStateSafe)) {
			// Safe to run within this thread
			return operation.run();

		} else {
			// Undertake within lock for safety
			this.lock.lock();
			try {

				// Ensure thread safe for next function
				if (isActiveThread) {
					active.flagRequiresThreadStateSafety();
				}

				// Undertake operation
				return operation != null ? operation.run() : null;
			} finally {
				this.lock.unlock();
			}
		}
	}

	@Override
	public <R, T extends Throwable> R runProcessSafeOperation(ProcessSafeOperation<R, T> operation) throws T {

		// Obtain the main thread
		ThreadState mainThreadState = this.processState.getMainThreadState();

		// Obtain the active thread state
		ActiveThreadState active = activeThreadState.get();
		ThreadState activeThreadState = (active != null ? active.threadState : null);

		// Determine if running on same thread state
		if ((mainThreadState == activeThreadState) && (active.lockState.isThreadStateSafe)) {
			// Safe on main thread, so no additional lock required
			return operation.run();

		} else {
			// Not safe as different thread state, so lock on main thread state
			return mainThreadState.runThreadSafeOperation(operation);
		}
	}

	@Override
	public Flow createFlow(FlowCompletion flowCompletion, EscalationCompletion escalationCompletion) {

		// Create and register the activate flow
		Flow flow = new FlowImpl(flowCompletion, escalationCompletion, this);
		this.activeFlows.addEntry(flow);

		// Return the flow
		return flow;
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation, EscalationCompletion escalationCompletion) {

		// Undertake clean up
		FunctionState cleanUpFunctions = null;

		// Ensure existing escalation is completed
		if (this.threadEscalationCompletion != null) {
			cleanUpFunctions = Promise.then(cleanUpFunctions, this.threadEscalationCompletion.escalationComplete());
		}

		// Cancel all remaining flows
		cleanUpFunctions = Promise.then(cleanUpFunctions,
				LinkedListSetPromise.all(this.activeFlows, (flow) -> flow.cancel()));

		// Handle based on escalation level
		switch (this.escalationLevel) {
		case OFFICE:
			EscalationFlow escalationFlow = this.threadMetaData.getOfficeEscalationProcedure()
					.getEscalation(escalation);
			if (escalationFlow != null) {
				// Create new flow, to keep thread alive
				Flow flow = this.createFlow(null, escalationCompletion);

				// Escalation complete on flow completion
				escalationCompletion = null;

				// Next escalation will be flow completion
				this.escalationLevel = EscalationLevel.FLOW_COMPLETION;
				return Promise.then(cleanUpFunctions, flow.createManagedFunction(escalation,
						escalationFlow.getManagedFunctionMetaData(), false, null));
			}

		case FLOW_COMPLETION:
			// Outside thread handling, so clean up thread state

			// Disregard any active governance
			for (int i = 0; i < this.governanceContainers.length; i++) {
				GovernanceContainer<?> container = this.governanceContainers[i];
				if ((container != null) && (container.isGovernanceActive())) {
					cleanUpFunctions = Promise.then(cleanUpFunctions, container.disregardGovernance());
				}
			}

			// Complete the thread (as thread escalation)
			this.escalationLevel = EscalationLevel.OFFICE_FLOOR;
			cleanUpFunctions = Promise.then(cleanUpFunctions, this.complete());

			// Capture the escalation for the thread
			// (all further escalations are for clean up of thread state)
			if (this.threadEscalation == null) {
				this.threadEscalation = escalation;
				this.threadEscalationCompletion = escalationCompletion;

				// Determine if completion (as handles thread escalation)
				if (this.completion != null) {
					return cleanUpFunctions;
				}
			}

		case OFFICE_FLOOR:
			escalationFlow = this.threadMetaData.getOfficeFloorEscalation();
			if (escalationFlow != null) {

				// Any further failure, log (not happen most applications)
				// (avoids infinite loop of escalation handling)
				this.escalationLevel = EscalationLevel.LOG;

				// Escalation threads can not spawn further escalations threads
				if (!this.isEscalationHandling) {
					// Spawn escalation thread state to handle
					return Promise.then(cleanUpFunctions, this.processState
							.spawnThreadState(escalationFlow.getManagedFunctionMetaData(), escalation, null, true));
				}
			}

		case LOG:
			OfficeFloorImpl.getFrameworkLogger().log(Level.SEVERE, "Unhandle escalation", escalation);
		}

		// Logged error
		return cleanUpFunctions;
	}

	@Override
	public FunctionState flowComplete(Flow flow, Throwable threadEscalation,
			EscalationCompletion escalationCompletion) {

		// Remove the flow
		boolean isThreadComplete = this.activeFlows.removeEntry(flow);

		// Handle escalation
		if (threadEscalation != null) {
			// Will handle completion of thread (if necessary)
			return this.handleEscalation(threadEscalation, escalationCompletion);
		}

		// Determine if handle completing thread
		if (isThreadComplete) {

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

			// Complete the thread
			return this.complete();
		}

		// Thread complete
		return null;
	}

	/**
	 * Completes the {@link ThreadState}.
	 * 
	 * @return {@link FunctionState} to complete the {@link ThreadState}.
	 */
	private FunctionState complete() {
		return new ThreadStateOperation() {

			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Easy access to thread state
				final ThreadStateImpl threadState = ThreadStateImpl.this;

				// Clean up thread state
				FunctionState cleanUpFunctions = null;

				// Deactivate the governance
				for (int i = 0; i < threadState.governanceContainers.length; i++) {
					GovernanceContainer<?> container = threadState.governanceContainers[i];
					if (container != null) {
						cleanUpFunctions = Promise.then(cleanUpFunctions, container.deactivateGovernance());
					}
				}

				// Unload managed objects (some may not have been used)
				for (int i = 0; i < threadState.managedObjectContainers.length; i++) {
					ManagedObjectContainer container = threadState.managedObjectContainers[i];
					if (container != null) {
						cleanUpFunctions = Promise.then(cleanUpFunctions, container.unloadManagedObject());
					}
				}

				// Ensure escalation completion
				if (threadState.threadEscalationCompletion != null) {
					cleanUpFunctions = Promise.then(cleanUpFunctions,
							threadState.threadEscalationCompletion.escalationComplete());
				}

				// Must handle completion at end of chain
				// (clean up escalations can cause re-attempts until complete)
				cleanUpFunctions = Promise.then(cleanUpFunctions, new ThreadStateOperation() {

					@Override
					public FunctionState execute(FunctionStateContext context) throws Throwable {

						// Do nothing if thread is complete
						if (threadState.isThreadComplete) {
							return null;
						}

						// Thread is now considered complete
						threadState.isThreadComplete = true;

						// Flag thread complete (escalation now to OfficeFloor)
						threadState.escalationLevel = EscalationLevel.OFFICE_FLOOR;

						// Notify complete thread state (with thread escalation)
						FunctionState threadCompletion = null;
						if (threadState.completion != null) {
							threadCompletion = threadState.completion.flowComplete(threadState.threadEscalation);
						}

						// Complete the thread state (thread is now cleaned up)
						return threadState.processState.threadComplete(threadState, threadCompletion);
					}
				});

				// Return the clean up
				return cleanUpFunctions;
			}
		};
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
	 * Abstract {@link FunctionState} operation for the {@link ThreadState}.
	 */
	private abstract class ThreadStateOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public ThreadState getThreadState() {
			return ThreadStateImpl.this;
		}
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
		 * @param threadState {@link ThreadState}.
		 * @param callback    {@link FlowCallback}.
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
		public FunctionState flowComplete(Throwable escalation) {
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
			 * @param escalation       {@link Escalation}.
			 * @param avoidTeamTracker {@link EscalationCompletion}.
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
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Undertake the callback
				ProcessFlowCompletion.this.callback.run(this.escalation);

				// Process now complete
				return null;
			}
		}
	}

	/**
	 * State of locking for the {@link ThreadState} on current {@link Thread}.
	 */
	private static class LockState {

		/**
		 * Top level {@link ActiveThreadState} that initiated this {@link LockState}.
		 */
		private final ActiveThreadState initialActiveThreadState;

		/**
		 * Flag indicating if the {@link ThreadState} is safe on the current
		 * {@link Thread}.
		 */
		private boolean isThreadStateSafe = false;

		/**
		 * Flag requires {@link ThreadState} safety.
		 */
		private boolean isRequireThreadStateSafety = false;

		/**
		 * Instantiate.
		 * 
		 * @param initialActiveThreadState Top level {@link ActiveThreadState} that
		 *                                 initiated this {@link LockState}.
		 */
		private LockState(ActiveThreadState initialActiveThreadState) {
			this.initialActiveThreadState = initialActiveThreadState;
		}
	}

	/**
	 * Active {@link ThreadState}.
	 */
	private static class ActiveThreadState implements ThreadStateContext, FunctionStateContext {

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * {@link LockState} for this {@link ActiveThreadState}.
		 */
		private final LockState lockState;

		/**
		 * Previous {@link ActiveThreadState}. This enables {@link FunctionLogic} to be
		 * executed within the context of another {@link FunctionLogic}. Will be
		 * <code>null</code> for top level {@link ThreadState} of {@link Thread}.
		 */
		private final ActiveThreadState previousActiveThreadState;

		/**
		 * Instantiate.
		 * 
		 * @param threadState               Active {@link ThreadState}.
		 * @param lockState                 {@link LockState} for this
		 *                                  {@link ActiveThreadState}.
		 * @param previousActiveThreadState Previous {@link ActiveThreadState} on the
		 *                                  {@link Thread}. May be <code>null</code>.
		 */
		private ActiveThreadState(ThreadState threadState, LockState lockState,
				ActiveThreadState previousActiveThreadState) {
			this.threadState = threadState;
			this.lockState = lockState;
			this.previousActiveThreadState = previousActiveThreadState;
		}

		/**
		 * Instantiate for initial {@link LockState}.
		 * 
		 * @param threadState               Active {@link ThreadState}.
		 * @param previousActiveThreadState Previous {@link ActiveThreadState} on the
		 *                                  {@link Thread}. May be <code>null</code>.
		 */
		private ActiveThreadState(ThreadState threadState, ActiveThreadState previousActiveThreadState) {
			this.threadState = threadState;
			this.lockState = new LockState(this);
			this.previousActiveThreadState = previousActiveThreadState;
		}

		/*
		 * ======================== ThreadStateContext ========================
		 */

		@Override
		public boolean isThreadStateSafe() {
			return this.lockState.isThreadStateSafe;
		}

		@Override
		public boolean isRequireThreadStateSafety() {
			return this.lockState.isRequireThreadStateSafety;
		}

		@Override
		public void flagRequiresThreadStateSafety() {
			this.lockState.isRequireThreadStateSafety = true;
		}

		@Override
		public FunctionState createFunction(FunctionLogic logic, ThreadState fallbackThreadState) {

			// Determine if active thread state
			if (this.threadState != null) {
				// Create function on active thread state
				Flow flow = this.threadState.createFlow(null, null);
				FunctionState logicFunction = flow.createFunction(logic);
				FunctionState completeFlow = new AbstractFunctionState(this.threadState) {
					@Override
					public FunctionState execute(FunctionStateContext context) throws Throwable {
						return ActiveThreadState.this.threadState.flowComplete(flow, null, null);
					}
				};
				return Promise.then(logicFunction, completeFlow);

			} else {
				// External thread, so use fall back thread state
				return fallbackThreadState.runThreadSafeOperation(() -> {
					Flow flow = fallbackThreadState.createFlow(null, null);
					FunctionState logicFunction = flow.createFunction(logic);
					FunctionState completeFlow = new AbstractFunctionState(fallbackThreadState) {

						@Override
						public FunctionState execute(FunctionStateContext context) throws Throwable {
							return fallbackThreadState.flowComplete(flow, null, null);
						}
					};
					return Promise.then(logicFunction, completeFlow);
				});
			}
		}

		@Override
		public FunctionState executeFunction(FunctionState function) throws Throwable {
			return function.execute(this);
		}

		@Override
		public ManagedObjectContainer getManagedObject(ManagedObjectIndex index) {

			// Obtain the scope index
			int scopeIndex = index.getIndexOfManagedObjectWithinScope();

			// Obtain the managed object container
			switch (index.getManagedObjectScope()) {

			case THREAD:
				// Obtain the container from the thread state
				return this.threadState.getManagedObjectContainer(scopeIndex);

			case PROCESS:
				// Obtain the container from the process state
				return this.threadState.getProcessState().getManagedObjectContainer(scopeIndex);

			default:
				throw new IllegalStateException(
						"Illegal managed object scope " + index.getManagedObjectScope() + " for thread local access");
			}
		}

		/*
		 * ========================= FunctionContext =========================
		 */

		@Override
		public FunctionState executeDelegate(FunctionState delegate) throws Throwable {
			return delegate.execute(this);
		}
	}

	/**
	 * Then {@link FunctionState}.
	 */
	private class ThenFunction extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

		/**
		 * Current {@link FunctionState}.
		 */
		protected final FunctionState currentFunction;

		/**
		 * Next {@link ThenFunction}.
		 */
		protected ThenFunction thenFunction = null;

		/**
		 * Instantiate.
		 * 
		 * @param delegate Delegate {@link FunctionState} to complete it and all
		 *                 produced {@link FunctionState} instances before continuing.
		 */
		private ThenFunction(FunctionState delegate) {
			this.currentFunction = delegate;
		}

		@Override
		public String toString() {
			return this.currentFunction.toString();
		}

		/**
		 * Flatten if wrapping a {@link ThenFunction}.
		 * 
		 * @param next Next {@link FunctionState}.
		 * @return Flattened {@link FunctionState}.
		 */
		private FunctionState flatten(FunctionState next) {

			// Determine if next
			if (next == null) {
				return this.thenFunction;
			}

			// Determine if flatten
			if (next instanceof ThenFunction) {
				ThenFunction flatten = (ThenFunction) next;

				// Append this then function to current function
				ThenFunction append = flatten;
				while (append.thenFunction != null) {
					append = append.thenFunction;
				}
				append.thenFunction = this.thenFunction;

				// Return flattened
				return flatten;

			} else {
				// No flattening required, so just next function
				ThenFunction nextFunction = new ThenFunction(next);
				nextFunction.thenFunction = this.thenFunction;
				return nextFunction;
			}
		}

		/*
		 * =================== FunctionState ==============================
		 */

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {
			return this.flatten(context.executeDelegate(this.currentFunction));
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion escalationCompletion) {
			return this.flatten(this.currentFunction.handleEscalation(escalation, escalationCompletion));
		}

		@Override
		public FunctionState cancel() {
			return Promise.then(this.currentFunction.cancel(), this.thenFunction.cancel());
		}

		@Override
		public Flow getLinkedListSetOwner() {
			return this.currentFunction.getLinkedListSetOwner();
		}

		@Override
		public TeamManagement getResponsibleTeam() {
			return this.currentFunction.getResponsibleTeam();
		}

		@Override
		public ThreadState getThreadState() {
			return this.currentFunction.getThreadState();
		}

		@Override
		public boolean isRequireThreadStateSafety() {
			return this.currentFunction.isRequireThreadStateSafety();
		}
	}

	/**
	 * Abstract higher context {@link FunctionState} than {@link ThenFunction}.
	 */
	private class AbstractThenContext extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

		/**
		 * Delegate {@link FunctionState}.
		 */
		protected FunctionState delegate;

		/**
		 * Instantiate.
		 * 
		 * @param delegate Delegate {@link FunctionState}.
		 */
		public AbstractThenContext(FunctionState delegate) {
			this.delegate = delegate;
		}

		/*
		 * ======================= Object ==========================
		 */

		@Override
		public String toString() {
			return this.delegate.toString();
		}

		/*
		 * ======================= FunctionState ==========================
		 */

		@Override
		public Flow getLinkedListSetOwner() {
			return this.delegate.getLinkedListSetOwner();
		}

		@Override
		public TeamManagement getResponsibleTeam() {
			return this.delegate.getResponsibleTeam();
		}

		@Override
		public ThreadState getThreadState() {
			return this.delegate.getThreadState();
		}

		@Override
		public boolean isRequireThreadStateSafety() {
			return this.delegate.isRequireThreadStateSafety();
		}

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {
			return context.executeDelegate(this.delegate);
		}

		@Override
		public FunctionState cancel() {
			return this.delegate.cancel();
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
			return this.delegate.handleEscalation(escalation, completion);
		}

	}

	/**
	 * Runs the {@link FunctionState} and all its subsequent {@link FunctionState}
	 * instances in the specified {@link ThreadState}.
	 */
	private class RunWithinFunction extends AbstractThenContext {

		/**
		 * {@link ThreadState} to override with this {@link ThreadState}.
		 */
		private final ThreadState overriddenThreadState;

		/**
		 * Instantiate.
		 * 
		 * @param delegate              Delegate {@link FunctionState}.
		 * @param overriddenThreadState {@link ThreadState} to override with this
		 *                              {@link ThreadState}.
		 */
		public RunWithinFunction(FunctionState delegate, ThreadState overriddenThreadState) {
			super(delegate);
			this.overriddenThreadState = overriddenThreadState;
		}

		/**
		 * Runs {@link FunctionState} within this {@link ThreadState}.
		 * 
		 * @param function {@link FunctionState} to run within this {@link ThreadState}.
		 * @return {@link FunctionState} running within this {@link ThreadState}.
		 */
		private FunctionState runWithin(FunctionState function) {
			return function == null ? null : new RunWithinFunction(function, this.overriddenThreadState);
		}

		/*
		 * =================== FunctionState ==============================
		 */

		@Override
		public ThreadState getThreadState() {

			// Override the thread state (but only if the specified thread)
			ThreadState delegateThreadState = this.delegate.getThreadState();
			return (delegateThreadState == this.overriddenThreadState) ? ThreadStateImpl.this : delegateThreadState;
		}

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {
			return this.runWithin(this.delegate.execute(context));
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
			return this.runWithin(this.delegate.handleEscalation(escalation, completion));
		}

		@Override
		public FunctionState cancel() {
			return this.runWithin(this.delegate.cancel());
		}
	}

	/**
	 * {@link AvoidTeam} implementation.
	 */
	private static class AvoidTeamImpl implements AvoidTeam {

		/**
		 * {@link AvoidTeamTracker}.
		 */
		private final AvoidTeamTracker avoidTeamTracker;

		/**
		 * Initial {@link FunctionState} to start avoiding the {@link Team}.
		 */
		private final FunctionState functionState;

		/**
		 * Instantiate.
		 * 
		 * @param avoidTeamTracker {@link AvoidTeamTracker}.
		 * @param functionState    Initial {@link FunctionState} to start avoiding the
		 *                         {@link Team}.
		 */
		private AvoidTeamImpl(AvoidTeamTracker avoidTeamTracker, FunctionState functionState) {
			this.avoidTeamTracker = avoidTeamTracker;
			this.functionState = functionState;
		}

		/*
		 * ==================== AvoidTeam =========================
		 */

		@Override
		public FunctionState getFunctionState() {
			return this.functionState;
		}

		@Override
		public void stopAvoidingTeam() {
			this.avoidTeamTracker.isContinueAvoidingTeam = false;
		}
	}

	/**
	 * Tracks whether to continue avoiding the {@link Team}.
	 */
	private static class AvoidTeamTracker {

		/**
		 * {@link TeamManagement} of {@link Team} to avoid.
		 */
		private final TeamManagement team;

		/**
		 * Indicates whether to continue avoiding the {@link Team}.
		 */
		private boolean isContinueAvoidingTeam = true;

		/**
		 * Instantiate.
		 * 
		 * @param team {@link TeamManagement} of {@link Team} to avoid.
		 */
		private AvoidTeamTracker(TeamManagement team) {
			this.team = team;
		}
	}

	/**
	 * Avoids executing {@link FunctionState} by a particular {@link Team}.
	 */
	private class AvoidTeamFunction extends AbstractThenContext {

		/**
		 * {@link AvoidTeamTracker}.
		 */
		private final AvoidTeamTracker avoidTeamTracker;

		/**
		 * Instantiate to initiate avoiding the {@link Team}.
		 *
		 * @param team     {@link TeamManagement} of {@link Team} to avoid.
		 * @param delegate Delegate {@link FunctionState}.
		 */
		private AvoidTeamFunction(TeamManagement team, FunctionState delegate) {
			super(delegate);
			this.avoidTeamTracker = new AvoidTeamTracker(team);
		}

		/**
		 * Instantiate to continue to avoid the {@link Team}.
		 * 
		 * @param avoidTeamTracker {@link AvoidTeamTracker}.
		 * @param delegate         Delegate {@link FunctionState}.
		 */
		private AvoidTeamFunction(AvoidTeamTracker avoidTeamTracker, FunctionState delegate) {
			super(delegate);
			this.avoidTeamTracker = avoidTeamTracker;
		}

		/**
		 * Continues to avoid the overloaded {@link Team}.
		 * 
		 * @param functionState {@link FunctionState}.
		 * @return {@link FunctionState} to avoid the overloaded {@link Team}.
		 */
		private FunctionState avoidOverloadedTeam(FunctionState functionState) {

			// Ensure have function
			if (functionState == null) {
				return null;
			}

			// Determine if continue avoiding team
			if (this.avoidTeamTracker.isContinueAvoidingTeam) {
				// Continue to avoid the overloaded team
				return new AvoidTeamFunction(this.avoidTeamTracker, functionState);

			} else {
				// Allow using the team again
				return functionState;
			}
		}

		/*
		 * ===================== FunctionState ========================
		 */

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {
			return this.avoidOverloadedTeam(this.delegate.execute(context));
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
			return this.avoidOverloadedTeam(this.delegate.handleEscalation(escalation, completion));
		}

		@Override
		public FunctionState cancel() {
			return this.avoidOverloadedTeam(this.delegate.cancel());
		}

		@Override
		public TeamManagement getResponsibleTeam() {

			// Obtain the required team
			TeamManagement requiredTeam = this.delegate.getResponsibleTeam();

			// Allow other threads to attempt team
			if (ThreadStateImpl.this != this.delegate.getThreadState()) {
				return requiredTeam;
			}

			// Determine if override team
			if (!this.avoidTeamTracker.isContinueAvoidingTeam) {
				return requiredTeam; // no further overriding team
			}

			// If the team to avoid, then allow any team to process.
			// This causes back pressure on these teams to also slow.
			Object requiredTeamIdentifier = (requiredTeam != null) ? requiredTeam.getIdentifier() : null;
			if (requiredTeamIdentifier == this.avoidTeamTracker.team.getIdentifier()) {
				return null; // continue with existing team
			}

			// Allow required team to be attempted
			return requiredTeam;
		}
	}

}
