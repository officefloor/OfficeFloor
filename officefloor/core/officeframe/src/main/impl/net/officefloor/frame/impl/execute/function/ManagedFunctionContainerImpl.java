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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

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
	private FunctionState setupFunction;

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
	 * {@link ManagedObjectContainer} instances for the respective
	 * {@link ManagedObject} instances bound to this
	 * {@link ManagedFunctionContainer}.
	 */
	private final ManagedObjectContainer[] managedObjects;

	/**
	 * {@link ManagedFunctionContainer} {@link ManagedObjectIndex} instances to
	 * the {@link ManagedObject} instances that must be loaded before the
	 * {@link ManagedFunction} may be executed.
	 */
	private final ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * <p>
	 * Array identifying which {@link Governance} instances are required to be
	 * active for this {@link ManagedFunctionLogic}. The {@link Governance} is
	 * identified by the index into the array.For each {@link Governance}:
	 * <ol>
	 * <li><code>true</code> indicates the {@link Governance} is to be activated
	 * (if not already activated)</li>
	 * <li><code>false</code> indicates to deactivate the {@link Governance}
	 * should it be active.</li>
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
	private ManagedFunctionState containerState = ManagedFunctionState.SETUP;

	/**
	 * Index of the next {@link ManagedObject} to load.
	 */
	private int loadManagedObjectIndex = 0;

	/**
	 * <p>
	 * Owner if this {@link ManagedFunctionContainer} is a parallel
	 * {@link ManagedFunctionContainer}.
	 * <p>
	 * This is the {@link ManagedFunctionContainer} that is executed once the
	 * graph from this {@link ManagedFunctionContainer} is complete.
	 */
	private ManagedFunctionContainerImpl<?> parallelOwner;

	/**
	 * Parallel {@link ManagedFunctionContainer} that must be executed before
	 * this {@link ManagedFunctionContainer} may be executed.
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
	 * @param setupFunction
	 *            Optional {@link FunctionState} to be executed to setup this
	 *            {@link ManagedObjectContainer}. May be <code>null</code>.
	 * @param managedFunctionLogic
	 *            {@link ManagedFunctionLogic} to be executed by this
	 *            {@link ManagedFunctionContainer}.
	 * @param functionBoundManagedObjects
	 *            {@link ManagedObjectContainer} instances for the
	 *            {@link ManagedObject} instances bound to the
	 *            {@link ManagedFunction}.
	 * @param requiredManagedObjects
	 *            {@link ManagedObjectIndex} instances to the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link ManagedFunction} may be executed.
	 * @param requiredGovernance
	 *            Identifies the required activation state of the
	 *            {@link Governance} for this {@link ManagedFunction}.
	 * @param isEnforceGovernance
	 *            <code>true</code> to enforce {@link Governance} on
	 *            deactivation. <code>false</code> to disregard
	 *            {@link Governance} on deactivation.
	 * @param functionLogicMetaData
	 *            {@link ManagedFunctionLogicMetaData} for this node.
	 * @param parallelOwner
	 *            Parallel owner of this {@link ManagedFunctionContainer}. May
	 *            be <code>null</code> if no owner.
	 * @param flow
	 *            {@link Flow} containing this {@link ManagedFunctionContainer}.
	 * @param isUnloadManagedObjects
	 *            Indicates whether this {@link ManagedObjectContainer} is
	 *            responsible for unloading the {@link ManagedObject} instances.
	 */
	public ManagedFunctionContainerImpl(FunctionState setupFunction, ManagedFunctionLogic managedFunctionLogic,
			ManagedObjectContainer[] functionBoundManagedObjects, ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance, boolean isEnforceGovernance, M functionLogicMetaData,
			ManagedFunctionContainer parallelOwner, Flow flow, boolean isUnloadManagedObjects) {
		this.setupFunction = setupFunction;
		this.managedFunctionLogic = managedFunctionLogic;
		this.managedObjects = functionBoundManagedObjects;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.isEnforceGovernance = isEnforceGovernance;
		this.functionLogicMetaData = functionLogicMetaData;
		this.parallelOwner = (ManagedFunctionContainerImpl<?>) parallelOwner;
		this.flow = flow;
		this.isUnloadManagedObjects = isUnloadManagedObjects;
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
	public void setNextManagedFunctionContainer(ManagedFunctionContainer container) {
		this.sequentialFunction = (ManagedFunctionContainerImpl<?>) container;
	}

	@Override
	public ManagedObjectContainer getManagedObjectContainer(int index) {
		return this.managedObjects[index];
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
	public FunctionState execute() throws Throwable {

		// Obtain the thread and process state (as used throughout method)
		ThreadState threadState = this.flow.getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Profile function being executed
		threadState.profile(this.functionLogicMetaData);

		switch (this.containerState) {
		case SETUP:

			// Setup
			this.containerState = ManagedFunctionState.LOAD_MANAGED_OBJECTS;
			if (this.setupFunction != null) {
				return Promise.then(this.setupFunction, this);
			}

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
									? governance.enforceGovernance() : governance.disregardGovernance());
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

			// Synchronise process state to this thread (if required)
			if (threadState != processState.getMainThreadState()) {
				// Must synchronise, so execute function when executing again
				this.containerState = ManagedFunctionState.EXECUTE_FUNCTION;
				return Promise.then(this.flow.createFunction(new SynchroniseProcessStateFunctionLogic()), this);
			}

		case EXECUTE_FUNCTION:

			// Execute the managed function
			ManagedFunctionLogicContextImpl context = new ManagedFunctionLogicContextImpl();
			this.nextManagedFunctionParameter = this.managedFunctionLogic.execute(context);

			// Function executed, so now await flow completions
			this.containerState = ManagedFunctionState.AWAIT_FLOW_COMPLETIONS;

		case AWAIT_FLOW_COMPLETIONS:

			// Undertake execute functions (may be invoked by callback)
			FunctionState executeFunctions = null;

			// Spawn any thread states
			if (this.spawnThreadStateFunction != null) {
				FunctionState spawn = this.spawnThreadStateFunction;
				this.spawnThreadStateFunction = null;
				executeFunctions = Promise.then(executeFunctions, spawn);
			}

			// Determine if next function registered
			if (this.nextFunction != null) {
				executeFunctions = Promise.then(executeFunctions, this.flow.createFunction(this.nextFunction));
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

			// Complete this function (flags state complete)
			FunctionState completeFunction = this.complete();
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
	public FunctionState handleEscalation(final Throwable escalation) {
		return new ManagedFunctionOperation() {
			@Override
			public FunctionState execute() throws Throwable {

				// Easy access to container
				ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Function failure
				container.containerState = ManagedFunctionState.FAILED;

				// Escalation from this node, so nothing further
				FunctionState clearFunctions = container.clear();

				// Obtain the escalation flow from this function
				EscalationFlow escalationFlow = container.functionLogicMetaData.getEscalationProcedure()
						.getEscalation(escalation);
				if (escalationFlow != null) {
					// Escalation handled by this function
					ThreadState threadState = container.flow.getThreadState();
					Flow parallelFlow = threadState.createFlow(null);
					FunctionState escalationFunction = parallelFlow.createManagedFunction(escalation,
							escalationFlow.getManagedFunctionMetaData(), false, container.parallelOwner);
					return Promise.then(clearFunctions, escalationFunction);
				}

				// Not handled by this function, so escalate to flow
				return Promise.then(clearFunctions, container.flow.handleEscalation(escalation));
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
		ManagedFunctionContainerImpl<?> currentTask = this;
		ManagedFunctionContainerImpl<?> nextTask = null;
		while ((nextTask = currentTask.parallelFunction) != null) {
			currentTask = nextTask;
		}

		// Determine if a parallel task
		if (currentTask == this) {
			// No parallel task
			return null;
		} else {
			// Return the furthest parallel task
			return currentTask;
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
			return this.sequentialFunction;
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
			ManagedObjectContainer moContainer = null;
			int scopeIndex = index.getIndexOfManagedObjectWithinScope();
			switch (index.getManagedObjectScope()) {
			case FUNCTION:
				moContainer = container.managedObjects[scopeIndex];
				break;

			case THREAD:
				moContainer = container.getThreadState().getManagedObjectContainer(scopeIndex);
				break;

			case PROCESS:
				moContainer = container.getThreadState().getProcessState().getManagedObjectContainer(scopeIndex);
				break;

			default:
				throw new IllegalStateException(
						"Unknown " + ManagedObject.class.getSimpleName() + " scope " + index.getManagedObjectScope());
			}

			// Return the object from the container
			return moContainer.getObject();
		}

		@Override
		public final void doFlow(FlowMetaData flowMetaData, Object parameter, FlowCallback callback) {

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// Obtain the task meta-data for instigating the flow
			@SuppressWarnings("rawtypes")
			ManagedFunctionMetaData initialFunctionMetaData = flowMetaData.getInitialFunctionMetaData();

			// Create the flow completion
			FlowCompletion completion = null;
			if (callback != null) {
				completion = new FlowCompletionImpl(callback);
				container.awaitingFlowCompletions.addEntry(completion);
			}

			// Determine if spawn thread
			if (flowMetaData.isSpawnThreadState()) {
				// Register to spawn the thread state
				FunctionLogic spawnFunctionLogic = new SpawnThreadFunctionLogic(flowMetaData, parameter, completion);
				container.spawnThreadStateFunction = Promise.then(container.spawnThreadStateFunction,
						container.flow.createFunction(spawnFunctionLogic));

			} else if (callback != null) {
				// Have callback, so execute in parallel in own flow
				Flow parallelFlow = container.flow.getThreadState().createFlow(completion);

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
		 * {@link FlowCallback}.
		 */
		private final FlowCallback callback;

		/**
		 * Instantiate.
		 * 
		 * @param callback
		 *            {@link FlowCallback}.
		 */
		public FlowCompletionImpl(FlowCallback callback) {
			this.callback = callback;
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
		public FunctionState complete(final Throwable escalation) {
			return new ManagedFunctionOperation() {
				@Override
				public FunctionState execute() throws Throwable {

					// Remove callback
					ManagedFunctionContainerImpl.this.awaitingFlowCompletions.removeEntry(FlowCompletionImpl.this);

					// Undertake the callback
					FlowCompletionImpl.this.callback.run(escalation);

					// Continue execution of this managed function
					return ManagedFunctionContainerImpl.this;
				}
			};
		}
	}

	/**
	 * Loads a sequential {@link ManagedFunctionContainer} relative to this
	 * {@link ManagedFunctionContainer} within the tree of
	 * {@link ManagedFunctionContainer} instances.
	 * 
	 * @param sequentialFunction
	 *            {@link ManagedFunctionContainer} to load to tree.
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
	 * @param parallelFunction
	 *            {@link ManagedFunctionContainer} to load to tree.
	 */
	private final void loadParallelFunction(ManagedFunctionContainerImpl<?> parallelFunction) {

		// Move possible next parallel function out
		if (this.parallelFunction != null) {
			parallelFunction.parallelFunction = this.parallelFunction;
			this.parallelFunction.parallelOwner = parallelFunction;
		}

		// Set next parallel node
		this.parallelFunction = parallelFunction;
		parallelFunction.parallelOwner = this;
	}

	/**
	 * Clears this {@link FunctionState}.
	 */
	private final FunctionState clear() {
		return new ManagedFunctionOperation() {
			@Override
			public FunctionState execute() throws Throwable {

				// Easy access to container
				final ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Create string of clean up functions
				FunctionState cleanUpFunctions = null;

				// Clear all the parallel functions from this node
				if (container.parallelFunction != null) {
					cleanUpFunctions = Promise.then(container.parallelFunction.clear(), cleanUpFunctions);
				}

				// Clear all the sequential functions from this node
				if (container.sequentialFunction != null) {
					cleanUpFunctions = Promise.then(container.sequentialFunction.clear(), cleanUpFunctions);
				}

				// Clear this function
				return Promise.then(cleanUpFunctions, container.complete());
			}
		};
	}

	/**
	 * Completes this {@link FunctionState}.
	 */
	private FunctionState complete() {
		return new ManagedFunctionOperation() {
			@Override
			public FunctionState execute() throws Throwable {

				// Easy access to container
				final ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Do nothing if already complete
				if (container.containerState == ManagedFunctionState.COMPLETED) {
					return null;
				}

				// Clean up functions
				FunctionState cleanUpFunctions = null;

				// Clean up the managed objects
				if (container.isUnloadManagedObjects) {
					for (int i = 0; i < container.managedObjects.length; i++) {
						ManagedObjectContainer managedObject = container.managedObjects[i];
						cleanUpFunctions = Promise.then(cleanUpFunctions, managedObject.unloadManagedObject());
					}
				}

				// Complete this function
				cleanUpFunctions = Promise.then(cleanUpFunctions, container.flow.managedFunctionComplete(container));

				// Function complete
				return Promise.then(cleanUpFunctions, new ManagedFunctionOperation() {
					@Override
					public FunctionState execute() throws Throwable {
						container.containerState = ManagedFunctionState.COMPLETED;
						return null;
					}
				});
			}
		};
	}

	/**
	 * {@link ManagedFunction} operation.
	 */
	private abstract class ManagedFunctionOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public Flow getLinkedListSetOwner() {
			return ManagedFunctionContainerImpl.this.getLinkedListSetOwner();
		}

		@Override
		public TeamManagement getResponsibleTeam() {
			return ManagedFunctionContainerImpl.this.getResponsibleTeam();
		}

		@Override
		public ThreadState getThreadState() {
			return ManagedFunctionContainerImpl.this.getThreadState();
		}

		@Override
		public boolean isRequireThreadStateSafety() {
			return ManagedFunctionContainerImpl.this.isRequireThreadStateSafety();
		}

		@Override
		public FunctionState cancel(Throwable cause) {
			return ManagedFunctionContainerImpl.this.cancel(cause);
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation) {
			return ManagedFunctionContainerImpl.this.handleEscalation(escalation);
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
		 * Initial state requiring the {@link ManagedObject} instances to be
		 * loaded.
		 */
		LOAD_MANAGED_OBJECTS,

		/**
		 * {@link ManagedObject} instances loaded and requiring
		 * {@link Governance}.
		 */
		GOVERN_MANAGED_OBJECTS,

		/**
		 * {@link Governance} in place. Need to ensure the {@link ProcessState}
		 * is synchronised to this {@link ThreadState} for executing the
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