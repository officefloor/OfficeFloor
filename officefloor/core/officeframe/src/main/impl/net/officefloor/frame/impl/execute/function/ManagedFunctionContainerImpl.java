/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectReadyCheckImpl;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionInterest;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectReadyCheck;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionState} to execute a {@link ManagedFunctionLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionContainerImpl<M extends ManagedFunctionLogicMetaData>
		extends AbstractLinkedListSetEntry<FunctionState, Flow> implements ManagedFunctionContainer {

	/**
	 * Awaiting {@link FlowCompletion} instances.
	 */
	private final LinkedListSet<FlowCompletion, ManagedFunctionContainer> awaitingFlowCompletions = new StrictLinkedListSet<FlowCompletion, ManagedFunctionContainer>() {
		@Override
		protected ManagedFunctionContainer getOwner() {
			return ManagedFunctionContainerImpl.this;
		}
	};

	/**
	 * Optional {@link FunctionState} to be executed to setup this
	 * {@link ManagedObjectContainer}.
	 */
	private final FunctionState setupFunction;

	/**
	 * {@link ManagedFunctionLogic} to be executed by this
	 * {@link ManagedFunctionContainer}.
	 */
	private final ManagedFunctionLogic managedFunctionLogic;

	/**
	 * {@link ManagedFunctionLogicMetaData}.
	 */
	private final M functionLogicMetaData;

	/**
	 * {@link ManagedFunctionBoundManagedObjects} for the
	 * {@link ManagedObjectContainer} instances bound to this
	 * {@link ManagedFunctionContainer}.
	 */
	private final ManagedFunctionBoundManagedObjects boundManagedObjects;

	/**
	 * {@link ManagedFunctionContainer} {@link ManagedObjectIndex} instances to the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link ManagedFunction} may be executed.
	 */
	private final ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * <p>
	 * Array identifying which {@link Governance} instances are required to be
	 * active for this {@link ManagedFunctionLogic}. The {@link Governance} is
	 * identified by the index into the array.For each {@link Governance}:
	 * <ol>
	 * <li><code>true</code> indicates the {@link Governance} is to be activated (if
	 * not already activated)</li>
	 * <li><code>false</code> indicates to deactivate the {@link Governance} should
	 * it be active.</li>
	 * </ol>
	 * <p>
	 * Should this array be <code>null</code> no change is undertaken with the
	 * {@link Governance} for the {@link ManagedFunctionLogic}.
	 */
	private final boolean[] requiredGovernance;

	/**
	 * Indicates whether {@link Governance} is to be enforced.
	 */
	private final boolean isEnforceGovernance;

	/**
	 * {@link Flow}.
	 */
	private final Flow flow;

	/**
	 * Indicates whether this {@link ManagedObjectContainer} is responsible for
	 * unloading the {@link ManagedObject} instances.
	 */
	private final boolean isUnloadManagedObjects;

	/**
	 * State of this {@link ManagedFunctionContainer}.
	 */
	private ManagedFunctionState containerState = ManagedFunctionState.LOAD_MANAGED_OBJECTS;

	/**
	 * Index of the next {@link ManagedObject} to load.
	 */
	private int loadManagedObjectIndex = 0;

	/**
	 * {@link ManagedObjectReadyCheck}.
	 */
	private ManagedObjectReadyCheckImpl check = null;

	/**
	 * <p>
	 * Owner if this {@link ManagedFunctionContainer} is a parallel
	 * {@link ManagedFunctionContainer}.
	 * <p>
	 * This is the {@link ManagedFunctionContainer} that is executed once the graph
	 * from this {@link ManagedFunctionContainer} is complete.
	 */
	private ManagedFunctionContainerImpl<?> parallelOwner;

	/**
	 * Parallel {@link ManagedFunctionContainer} that must be executed before this
	 * {@link ManagedFunctionContainer} may be executed.
	 */
	private ManagedFunctionContainerImpl<?> parallelFunction = null;

	/**
	 * Next {@link ManagedFunctionContainer} in the sequential listing.
	 */
	private ManagedFunctionContainerImpl<?> sequentialFunction = null;

	/**
	 * Optional next {@link FunctionLogic} to be executed once the
	 * {@link ManagedFunction} has executed.
	 */
	private FunctionLogic nextFunction = null;

	/**
	 * Spawn {@link ThreadState} {@link FunctionState}.
	 */
	private FunctionState spawnThreadStateFunction = null;

	/**
	 * Flag indicating if a sequential {@link FunctionState} was invoked.
	 */
	private boolean isSequentialFunctionInvoked = false;

	/**
	 * Parameter for the next {@link ManagedFunction}.
	 */
	private Object nextManagedFunctionParameter;

	/**
	 * Initiate.
	 * 
	 * @param setupFunction          Optional {@link FunctionState} to be executed
	 *                               to setup this {@link ManagedObjectContainer}.
	 *                               May be <code>null</code>.
	 * @param managedFunctionLogic   {@link ManagedFunctionLogic} to be executed by
	 *                               this {@link ManagedFunctionContainer}.
	 * @param boundManagedObjects    {@link ManagedFunctionBoundManagedObjects}
	 *                               containing the {@link ManagedObjectContainer}
	 *                               instances for the {@link ManagedObject}
	 *                               instances bound to the {@link ManagedFunction}.
	 * @param requiredManagedObjects {@link ManagedObjectIndex} instances to the
	 *                               {@link ManagedObject} instances that must be
	 *                               loaded before the {@link ManagedFunction} may
	 *                               be executed.
	 * @param requiredGovernance     Identifies the required activation state of the
	 *                               {@link Governance} for this
	 *                               {@link ManagedFunction}.
	 * @param isEnforceGovernance    <code>true</code> to enforce {@link Governance}
	 *                               on deactivation. <code>false</code> to
	 *                               disregard {@link Governance} on deactivation.
	 * @param functionLogicMetaData  {@link ManagedFunctionLogicMetaData} for this
	 *                               node.
	 * @param parallelOwner          Parallel owner of this
	 *                               {@link ManagedFunctionContainer}. May be
	 *                               <code>null</code> if no owner.
	 * @param flow                   {@link Flow} containing this
	 *                               {@link ManagedFunctionContainer}.
	 * @param isUnloadManagedObjects Indicates whether this
	 *                               {@link ManagedObjectContainer} is responsible
	 *                               for unloading the {@link ManagedObject}
	 *                               instances.
	 */
	public ManagedFunctionContainerImpl(FunctionState setupFunction, ManagedFunctionLogic managedFunctionLogic,
			ManagedFunctionBoundManagedObjects boundManagedObjects, ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance, boolean isEnforceGovernance, M functionLogicMetaData,
			ManagedFunctionContainer parallelOwner, Flow flow, boolean isUnloadManagedObjects) {
		this.setupFunction = setupFunction;
		this.managedFunctionLogic = managedFunctionLogic;
		this.boundManagedObjects = boundManagedObjects;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.isEnforceGovernance = isEnforceGovernance;
		this.functionLogicMetaData = functionLogicMetaData;
		this.parallelOwner = (ManagedFunctionContainerImpl<?>) parallelOwner;
		this.flow = flow;
		this.isUnloadManagedObjects = isUnloadManagedObjects;
	}

	@Override
	public String toString() {
		return "Function " + this.functionLogicMetaData.getFunctionName() + " " + this.containerState.toString();
	}

	/*
	 * =================== LinkedListSetEntry =================================
	 */

	@Override
	public Flow getLinkedListSetOwner() {
		return this.flow;
	}

	/*
	 * ================= ManagedFunctionContainer ============================
	 */

	@Override
	public Flow getFlow() {
		return this.flow;
	}

	@Override
	public void setParallelManagedFunctionContainer(ManagedFunctionContainer container) {
		this.loadParallelFunction((ManagedFunctionContainerImpl<?>) container);
	}

	@Override
	public void setNextManagedFunctionContainer(ManagedFunctionContainer container) {
		this.loadSequentialFunction((ManagedFunctionContainerImpl<?>) container);
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.boundManagedObjects.managedObjects[index];
	}

	@Override
	public ManagedFunctionInterest createInterest() {
		return this.boundManagedObjects.createInterest();
	}

	/*
	 * ===================== FunctionState ===============================
	 */

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.functionLogicMetaData.getResponsibleTeam();
	}

	@Override
	public ThreadState getThreadState() {
		return this.flow.getThreadState();
	}

	@Override
	public boolean isRequireThreadStateSafety() {
		return this.managedFunctionLogic.isRequireThreadStateSafety();
	}

	@Override
	public FunctionState execute(FunctionStateContext context) throws Throwable {

		// Ensure no parallel function to execute first
		if (this.parallelFunction != null) {
			return this.parallelFunction;
		}

		// Obtain the thread and process state (as used throughout method)
		ThreadState threadState = this.flow.getThreadState();
		ProcessState processState = threadState.getProcessState();

		switch (this.containerState) {
		case LOAD_MANAGED_OBJECTS:

			// Load the managed objects
			if (this.requiredManagedObjects != null) {
				while (this.loadManagedObjectIndex < this.requiredManagedObjects.length) {
					ManagedObjectIndex index = this.requiredManagedObjects[this.loadManagedObjectIndex];
					this.loadManagedObjectIndex++;

					// Obtain the managed object container
					ManagedObjectContainer container = ManagedObjectContainerImpl.getManagedObjectContainer(index,
							this);

					// Load the managed object
					return container.loadManagedObject(this);
				}
			}

			// Managed objects loaded
			this.containerState = ManagedFunctionState.GOVERN_MANAGED_OBJECTS;

		case GOVERN_MANAGED_OBJECTS:

			// Ensure appropriate governance in place over managed objects
			// (Does its own check to ensure managed objects are ready)
			if (this.requiredGovernance != null) {
				FunctionState updateGovernance = null;
				for (int i = 0; i < this.requiredGovernance.length; i++) {

					// Determine if governance in correct state
					boolean isGovernanceRequired = this.requiredGovernance[i];
					if (isGovernanceRequired != threadState.isGovernanceActive(i)) {

						// Incorrect state, so correct
						GovernanceContainer<?> governance = threadState.getGovernanceContainer(i);
						if (isGovernanceRequired) {
							// Activate the governance
							updateGovernance = Promise.then(updateGovernance, governance.activateGovernance());

						} else {
							// De-activate the governance
							FunctionState deactivateGovernance = (this.isEnforceGovernance
									? governance.enforceGovernance()
									: governance.disregardGovernance());
							updateGovernance = Promise.then(updateGovernance, deactivateGovernance);
						}
					}
				}
				if (updateGovernance != null) {
					// Governing, so must synchronise when executing again
					this.containerState = ManagedFunctionState.SYNCHRONISE_PROCESS_STATE;
					return Promise.then(updateGovernance, this);
				}
			}

		case SYNCHRONISE_PROCESS_STATE:

			// Check whether managed objects are ready
			if (this.requiredManagedObjects != null) {
				if (this.check == null) {
					// Undertake check to ensure managed objects are ready
					this.check = new ManagedObjectReadyCheckImpl(this, this);
					FunctionState checkFunction = null;
					for (int i = 0; i < this.requiredManagedObjects.length; i++) {
						ManagedObjectIndex index = this.requiredManagedObjects[i];
						ManagedObjectContainer moContainer = ManagedObjectContainerImpl.getManagedObjectContainer(index,
								this);
						checkFunction = Promise.then(checkFunction, moContainer.checkReady(this.check));
					}
					if (checkFunction != null) {
						return Promise.then(checkFunction, this);
					}
				} else if (!this.check.isReady()) {
					// Not ready so wait on latch release and try again
					this.check = null;
					return null;
				}
			}

			// Synchronise process state to this thread (if required)
			if (threadState != processState.getMainThreadState()) {
				// Must synchronise, so execute function when executing again
				this.containerState = ManagedFunctionState.SETUP;
				return Promise.then(this.flow.createFunction(new SynchroniseProcessStateFunctionLogic(threadState)),
						this);
			}

		case SETUP:

			// Setup
			this.containerState = ManagedFunctionState.EXECUTE_FUNCTION;
			if (this.setupFunction != null) {
				return Promise.then(this.setupFunction, this);
			}

		case EXECUTE_FUNCTION:

			// Profile function being executed
			threadState.profile(this.functionLogicMetaData);

			// Execute the managed function
			ManagedFunctionLogicContextImpl logicContext = new ManagedFunctionLogicContextImpl();
			this.nextManagedFunctionParameter = this.managedFunctionLogic.execute(logicContext);

			// Must recheck managed objects
			this.check = null;

			// Function executed, so now await flow completions
			this.containerState = ManagedFunctionState.AWAIT_FLOW_COMPLETIONS;

		case AWAIT_FLOW_COMPLETIONS:

			// Undertake execute functions (may be invoked by callback)
			FunctionState executeFunctions = null;

			// Spawn any thread states
			if (this.spawnThreadStateFunction != null) {
				FunctionState spawn = this.spawnThreadStateFunction;
				this.spawnThreadStateFunction = null; // avoid spawning again
				executeFunctions = Promise.then(executeFunctions, spawn);
			}

			// Determine if next function registered
			if (this.nextFunction != null) {
				executeFunctions = Promise.then(executeFunctions, this.flow.createFunction(this.nextFunction));
				this.nextFunction = null; // avoid infinite loop
			}

			// Undertake execute functions
			if (executeFunctions != null) {
				// Additional functions, active next when executing again
				return Promise.then(executeFunctions, this);
			}

			// Determine if awaiting on flow
			if (this.awaitingFlowCompletions.getHead() != null) {
				return this.getParallelFunctionToExecute();
			}

			// All callbacks completed
			this.containerState = ManagedFunctionState.ACTIVATE_NEXT_FUNCTION;

		case ACTIVATE_NEXT_FUNCTION:

			// Load next function if no sequential function invoked
			if (!this.isSequentialFunctionInvoked) {
				// No sequential function, load next function
				ManagedFunctionMetaData<?, ?> nextFunctionMetaData = this.functionLogicMetaData
						.getNextManagedFunctionMetaData();
				if (nextFunctionMetaData != null) {
					// Create next managed function container
					ManagedFunctionContainerImpl<?> nextContainer = (ManagedFunctionContainerImpl<?>) this.flow
							.createManagedFunction(this.nextManagedFunctionParameter, nextFunctionMetaData, true,
									this.parallelOwner);

					// Load for sequential execution
					this.loadSequentialFunction(nextContainer);
				}

				// Sequential function now invoked
				this.isSequentialFunctionInvoked = true;
			}

			// Complete this function (no escalation as successful)
			FunctionState completeFunction = this.complete(null, null);
			if (completeFunction != null) {
				return Promise.then(completeFunction, this);
			}

		case COMPLETED:
			// Now complete, so undertake next function
			return this.getNextFunctionToExecute();

		case FAILED:
			throw new IllegalStateException("Should not attempt to execute "
					+ ManagedFunctionContainer.class.getSimpleName() + " when in failed state");

		default:
			throw new IllegalStateException("Should not be in state " + this.containerState);
		}
	}

	@Override
	public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {

		// Escalation from this node, so clean up down stream functions
		FunctionState handleFunctions = this.cancel(false);

		switch (this.containerState) {
		case COMPLETED:
			// Handle by thread as this function is complete
			handleFunctions = this.flow.getThreadState().handleEscalation(escalation, completion);
			break;

		default:
			// Function failure
			this.containerState = ManagedFunctionState.FAILED;

			// Obtain the escalation flow from this function
			EscalationFlow escalationFlow = this.functionLogicMetaData.getEscalationProcedure()
					.getEscalation(escalation);
			if (escalationFlow != null) {
				// Escalation handled by this functions escalation procedure
				ThreadState threadState = this.flow.getThreadState();
				Flow parallelFlow = threadState.createFlow(null, completion);
				FunctionState escalationFunction = parallelFlow.createManagedFunction(escalation,
						escalationFlow.getManagedFunctionMetaData(), false, this.parallelOwner);

				// Complete this flow (handling escalating)
				handleFunctions = Promise.then(handleFunctions, this.complete(null, null));
				handleFunctions = Promise.then(handleFunctions, escalationFunction);

			} else {
				// Escalate to flow
				handleFunctions = Promise.then(handleFunctions, this.complete(escalation, completion));
			}
		}

		// Return handling of escalation
		return handleFunctions;
	}

	@Override
	public FunctionState cancel() {
		return this.cancel(true);
	}

	/**
	 * Cancels all downstream {@link FunctionState} instances.
	 * 
	 * @param isCancelThisFunctionState <code>true</code> to cancel this
	 *                                  {@link FunctionState}.
	 * @return {@link FunctionState} to cancel the downstream {@link FunctionState}
	 *         instances.
	 */
	private FunctionState cancel(boolean isCancelThisFunctionState) {
		return new ManagedFunctionOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Easy access to container
				final ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Create string of clean up functions
				FunctionState cleanUpFunctions = null;

				// Clear all the parallel functions from this node
				if (container.parallelFunction != null) {
					cleanUpFunctions = Promise.then(container.parallelFunction.cancel(true), cleanUpFunctions);
				}

				// Clear all the sequential functions from this node
				if (container.sequentialFunction != null) {
					cleanUpFunctions = Promise.then(container.sequentialFunction.cancel(true), cleanUpFunctions);
				}

				// Clean up this function state (if required to do so)
				if (isCancelThisFunctionState) {
					cleanUpFunctions = Promise.then(cleanUpFunctions, container.complete(null, null));
				}

				// Return clean up functions
				return cleanUpFunctions;
			}
		};

	}

	/**
	 * Obtains the parallel {@link FunctionState} to execute.
	 * 
	 * @return Parallel {@link FunctionState} to execute.
	 */
	private FunctionState getParallelFunctionToExecute() {

		// Determine furthest parallel node
		ManagedFunctionContainerImpl<?> currentFunction = this;
		ManagedFunctionContainerImpl<?> nextFunction = null;
		while ((nextFunction = currentFunction.parallelFunction) != null) {
			currentFunction = nextFunction;
		}

		// Determine if a parallel function
		if (currentFunction == this) {
			// No parallel function
			return null;
		} else {
			// Return the furthest parallel function
			return currentFunction;
		}
	}

	/**
	 * Obtains the next {@link FunctionState} to execute.
	 * 
	 * @return Next {@link FunctionState} to execute.
	 */
	private FunctionState getNextFunctionToExecute() {

		// Determine if have parallel function
		FunctionState nextFunction = this.getParallelFunctionToExecute();
		if (nextFunction != null) {
			return nextFunction;
		}

		// Determine if have sequential node
		if (this.sequentialFunction != null) {
			nextFunction = this.sequentialFunction;
			this.sequentialFunction = null; // avoid infinite loop
			return nextFunction;
		}

		// Determine if have parallel owner
		if (this.parallelOwner != null) {
			// Returning to owner, therefore unlink parallel node
			this.parallelOwner.parallelFunction = null;

			// Parallel owner
			return this.parallelOwner;
		}

		// No further tasks
		return null;
	}

	/**
	 * {@link ManagedFunctionLogicContext} implementation.
	 */
	private class ManagedFunctionLogicContextImpl implements ManagedFunctionLogicContext {

		@Override
		public void next(FunctionLogic nextFunction) {
			ManagedFunctionContainerImpl.this.nextFunction = nextFunction;
		}

		@Override
		public Object getObject(ManagedObjectIndex index) {

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// Obtain the managed object container
			ManagedObjectContainer moContainer = ManagedObjectContainerImpl.getManagedObjectContainer(index, container);

			// Return the object from the container
			return moContainer.getObject();
		}

		@Override
		public final void doFlow(FlowMetaData flowMetaData, Object parameter, FlowCallback callback) {

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// Ensure in appropriate state to invoke flows
			switch (container.containerState) {
			case EXECUTE_FUNCTION:
			case AWAIT_FLOW_COMPLETIONS:
				break; // correct states to invoke flow

			default:
				throw new IllegalStateException("Can not invoke flow outside function/callback execution (state: "
						+ container.containerState + ")");
			}

			// Obtain the task meta-data for instigating the flow
			@SuppressWarnings("rawtypes")
			ManagedFunctionMetaData initialFunctionMetaData = flowMetaData.getInitialFunctionMetaData();

			// Create the flow completion
			FlowCompletion completion = null;
			if (callback != null) {
				completion = new FlowCompletionImpl(flowMetaData, callback);
				container.awaitingFlowCompletions.addEntry(completion);
			}

			// Determine if spawn thread
			if (flowMetaData.isSpawnThreadState()) {
				// Register to spawn the thread state
				FunctionLogic spawnFunctionLogic = new SpawnThreadFunctionLogic(flowMetaData, parameter, completion,
						container.flow.getThreadState());
				container.spawnThreadStateFunction = Promise.then(container.spawnThreadStateFunction,
						container.flow.createFunction(spawnFunctionLogic));

			} else if (callback != null) {
				// Have callback, so execute in parallel in new flow
				Flow parallelFlow = container.flow.getThreadState().createFlow(completion, null);

				// Create the function
				@SuppressWarnings("unchecked")
				ManagedFunctionContainerImpl<?> parallelFunction = (ManagedFunctionContainerImpl<?>) parallelFlow
						.createManagedFunction(parameter, initialFunctionMetaData, true, container);

				// Load the parallel function
				container.loadParallelFunction(parallelFunction);

			} else {
				// Flag sequential function invoked
				container.isSequentialFunctionInvoked = true;

				// Create the function on the same flow as this function
				@SuppressWarnings("unchecked")
				ManagedFunctionContainerImpl<?> sequentialFunction = (ManagedFunctionContainerImpl<?>) container.flow
						.createManagedFunction(parameter, initialFunctionMetaData, true, container.parallelOwner);

				// Load the sequential function
				container.loadSequentialFunction(sequentialFunction);
			}
		}
	}

	/**
	 * {@link FlowCompletion} implementation.
	 */
	private class FlowCompletionImpl extends AbstractLinkedListSetEntry<FlowCompletion, ManagedFunctionContainer>
			implements FlowCompletion {

		/**
		 * {@link FlowMetaData}.
		 */
		private final FlowMetaData flowMetaData;

		/**
		 * {@link FlowCallback}.
		 */
		private final FlowCallback callback;

		/**
		 * Instantiate.
		 * 
		 * @param flowMetaData {@link FlowMetaData}.
		 * @param callback     {@link FlowCallback}.
		 */
		public FlowCompletionImpl(FlowMetaData flowMetaData, FlowCallback callback) {
			this.flowMetaData = flowMetaData;
			this.callback = callback;
		}

		@Override
		public String toString() {
			return "FlowComplete " + ManagedFunctionContainerImpl.this.functionLogicMetaData.getFunctionName() + " for "
					+ this.flowMetaData.getInitialFunctionMetaData().getFunctionName();
		}

		/*
		 * ================== LinkedListSetEntry =================
		 */

		@Override
		public ManagedFunctionContainer getLinkedListSetOwner() {
			return ManagedFunctionContainerImpl.this;
		}

		/*
		 * =================== FlowCompletion =====================
		 */

		@Override
		public FunctionState flowComplete(final Throwable escalation, final EscalationCompletion escalationCompletion) {
			return new ManagedFunctionOperation() {
				@Override
				public FunctionState execute(FunctionStateContext context) throws Throwable {

					// Easy access to container
					ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

					// Check whether managed objects are ready
					if (container.requiredManagedObjects != null) {
						if (container.check == null) {
							// Undertake check to ensure objects are ready
							container.check = new ManagedObjectReadyCheckImpl(this, container);
							FunctionState checkFunction = null;
							for (int i = 0; i < container.requiredManagedObjects.length; i++) {
								ManagedObjectIndex index = container.requiredManagedObjects[i];
								ManagedObjectContainer moContainer = ManagedObjectContainerImpl
										.getManagedObjectContainer(index, container);
								checkFunction = Promise.then(checkFunction, moContainer.checkReady(container.check));
							}
							if (checkFunction != null) {
								return Promise.then(checkFunction, this);
							}
						} else if (!container.check.isReady()) {
							// Not ready so wait on latch release and try again
							container.check = null;
							return null;
						}
					}

					// Remove callback
					container.awaitingFlowCompletions.removeEntry(FlowCompletionImpl.this);

					// Notify of escalation completion
					FunctionState escalationCompletionFunction = escalationCompletion != null
							? escalationCompletion.escalationComplete()
							: null;
					FunctionState continueFunction = Promise.then(escalationCompletionFunction, container);

					try {
						// Undertake the callback
						FlowCompletionImpl.this.callback.run(escalation);

					} catch (Throwable ex) {
						// Handle potential failure in the call back
						continueFunction = Promise.then(escalationCompletionFunction, this.handleEscalation(ex, null));
					}

					// Must recheck managed objects
					container.check = null;

					// Continue execution of this managed function
					return continueFunction;
				}
			};
		}
	}

	/**
	 * Loads a sequential {@link ManagedFunctionContainer} relative to this
	 * {@link ManagedFunctionContainer} within the tree of
	 * {@link ManagedFunctionContainer} instances.
	 * 
	 * @param sequentialFunction {@link ManagedFunctionContainer} to load to tree.
	 */
	private final void loadSequentialFunction(ManagedFunctionContainerImpl<?> sequentialFunction) {

		// Obtain the next sequential function
		if (this.sequentialFunction != null) {
			// Move current sequential function to parallel function
			this.loadParallelFunction(this.sequentialFunction);
		}

		// Set next sequential function
		this.sequentialFunction = sequentialFunction;
	}

	/**
	 * Loads a parallel {@link ManagedFunctionContainer} relative to this
	 * {@link ManagedFunctionContainer} within the tree of
	 * {@link ManagedFunctionContainer} instances.
	 * 
	 * @param parallelFunction {@link ManagedFunctionContainer} to load to tree.
	 */
	private final void loadParallelFunction(ManagedFunctionContainerImpl<?> parallelFunction) {

		// Move possible next parallel function out
		if (this.parallelFunction != null) {
			parallelFunction.parallelFunction = this.parallelFunction;
			this.loadParallelOwner(this.parallelFunction, parallelFunction);
		}

		// Set next parallel node
		this.parallelFunction = parallelFunction;
		this.loadParallelOwner(parallelFunction, this);
	}

	/**
	 * Loads the parallel owner to the parallel {@link ManagedFunctionContainer}.
	 * 
	 * @param parallelFunction Parallel {@link ManagedFunctionContainer}.
	 * @param parallelOwner    Parallel owner.
	 */
	private final void loadParallelOwner(ManagedFunctionContainerImpl<?> parallelFunction,
			ManagedFunctionContainerImpl<?> parallelOwner) {
		while (parallelFunction != null) {
			parallelFunction.parallelOwner = parallelOwner;
			parallelFunction = parallelFunction.sequentialFunction;
		}
	}

	/**
	 * Completes this {@link FunctionState}.
	 * 
	 * @param functionEscalation   Possible {@link Escalation} from this
	 *                             {@link FunctionState}.
	 * @param escalationCompletion {@link EscalationCompletion}.
	 */
	private FunctionState complete(Throwable functionEscalation, EscalationCompletion escalationCompletion) {
		return new ManagedFunctionOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Easy access to container
				final ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Attempt to clean up the managed objects
				FunctionState cleanUpFunctions = null;
				if (container.isUnloadManagedObjects) {
					cleanUpFunctions = container.cleanUpManagedObjects();
				}

				// Complete this function
				return Promise.then(cleanUpFunctions, new ManagedFunctionOperation() {
					@Override
					public FunctionState execute(FunctionStateContext context) throws Throwable {

						// Do nothing if already complete
						if (container.containerState == ManagedFunctionState.COMPLETED) {
							return null;
						}

						// Function now complete
						container.containerState = ManagedFunctionState.COMPLETED;

						// Flag function complete
						return container.flow.managedFunctionComplete(container, functionEscalation,
								escalationCompletion);
					}
				});
			}
		};
	}

	/**
	 * Cleans up the {@link ManagedObject} instances.
	 */
	FunctionState cleanUpManagedObjects() {

		// Ensure there is no interest in the managed objects
		if (this.boundManagedObjects.isInterest()) {
			return null;
		}

		// As here, clean up the managed objects
		FunctionState cleanUpFunctions = null;
		for (int i = 0; i < this.boundManagedObjects.managedObjects.length; i++) {
			ManagedObjectContainer managedObject = this.boundManagedObjects.managedObjects[i];
			cleanUpFunctions = Promise.then(cleanUpFunctions, managedObject.unloadManagedObject());
		}
		return cleanUpFunctions;
	}

	/**
	 * {@link ManagedFunction} operation.
	 */
	private abstract class ManagedFunctionOperation extends AbstractDelegateFunctionState {

		/**
		 * Instantiate.
		 */
		public ManagedFunctionOperation() {
			super(ManagedFunctionContainerImpl.this);
		}
	}

	/**
	 * State of this {@link ManagedFunctionContainer}.
	 */
	private static enum ManagedFunctionState {

		/**
		 * Set up of this {@link ManagedFunctionContainer}.
		 */
		SETUP,

		/**
		 * Initial state requiring the {@link ManagedObject} instances to be loaded.
		 */
		LOAD_MANAGED_OBJECTS,

		/**
		 * {@link ManagedObject} instances loaded and requiring {@link Governance}.
		 */
		GOVERN_MANAGED_OBJECTS,

		/**
		 * {@link Governance} in place. Need to ensure the {@link ProcessState} is
		 * synchronised to this {@link ThreadState} for executing the
		 * {@link ManagedFunctionLogic}.
		 */
		SYNCHRONISE_PROCESS_STATE,

		/**
		 * Indicates the {@link ManagedFunctionLogic} is to be executed.
		 */
		EXECUTE_FUNCTION,

		/**
		 * Waiting on the {@link FlowCompletion} instances to be completed.
		 */
		AWAIT_FLOW_COMPLETIONS,

		/**
		 * Indicates to activate the next {@link ManagedFunctionLogic}.
		 */
		ACTIVATE_NEXT_FUNCTION,

		/**
		 * Failure in executing.
		 */
		FAILED,

		/**
		 * Completed.
		 */
		COMPLETED
	}

}