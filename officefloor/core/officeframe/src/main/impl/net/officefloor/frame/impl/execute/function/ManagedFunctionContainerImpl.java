/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.escalate.AsynchronousFlowTimedOutEscalation;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.ProcessCancelledEscalation;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.StrictLinkedListSet;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectContainerImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectReadyCheckImpl;
import net.officefloor.frame.internal.structure.ActiveAsynchronousFlow;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.BlockState;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
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
import net.officefloor.frame.internal.structure.OfficeManager;
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
	 * Awaiting {@link AsynchronousFlow} instances.
	 */
	private final LinkedListSet<ActiveAsynchronousFlow, ManagedFunctionContainer> awaitingAsynchronousFlowCompletions = new StrictLinkedListSet<ActiveAsynchronousFlow, ManagedFunctionContainer>() {
		@Override
		protected ManagedFunctionContainer getOwner() {
			return ManagedFunctionContainerImpl.this;
		}
	};

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
	 * {@link BlockState}.
	 * <p>
	 * This is the {@link BlockState} that is executed once the graph from this
	 * {@link ManagedFunctionContainer} is complete.
	 */
	private BlockState parallelOwner;

	/**
	 * Parallel {@link BlockState} that must be executed before this
	 * {@link ManagedFunctionContainer} may be executed.
	 */
	private BlockState parallelBlock = null;

	/**
	 * Next {@link BlockState} in the sequential listing.
	 */
	private BlockState sequentialBlock = null;

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
	 * Indicates if there were instantiated {@link AsynchronousFlow} instances.
	 */
	private boolean isAsynchronousFlows = false;

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
			BlockState parallelOwner, Flow flow, boolean isUnloadManagedObjects) {
		this.setupFunction = setupFunction;
		this.managedFunctionLogic = managedFunctionLogic;
		this.boundManagedObjects = boundManagedObjects;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.isEnforceGovernance = isEnforceGovernance;
		this.functionLogicMetaData = functionLogicMetaData;
		this.parallelOwner = parallelOwner;
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
	 * ======================= BlockState =====================================
	 */

	@Override
	public void setParallelOwner(BlockState parallelOwner) {
		this.parallelOwner = parallelOwner;
	}

	@Override
	public BlockState getParallelOwner() {
		return this.parallelOwner;
	}

	@Override
	public void setParallelBlock(BlockState parallelBlock) {
		this.parallelBlock = parallelBlock;
	}

	@Override
	public BlockState getParallelBlock() {
		return this.parallelBlock;
	}

	@Override
	public void setSequentialBlock(BlockState sequentialBlock) {
		this.sequentialBlock = sequentialBlock;
	}

	@Override
	public BlockState getSequentialBlock() {
		return this.sequentialBlock;
	}

	/*
	 * ================= ManagedFunctionContainer ============================
	 */

	@Override
	public Flow getFlow() {
		return this.flow;
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
		if (this.parallelBlock != null) {
			return this.parallelBlock;
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
				for (int i = 0; i < this.requiredGovernance.length; i++) {

					// Determine if governance in correct state
					boolean isGovernanceRequired = this.requiredGovernance[i];
					if (isGovernanceRequired != threadState.isGovernanceActive(i)) {

						// Incorrect state, so correct
						GovernanceContainer<?> governance = threadState.getGovernanceContainer(i);
						if (isGovernanceRequired) {
							// Activate the governance
							this.loadParallelBlock(governance.activateGovernance());

						} else {
							// De-activate the governance
							BlockState deactivateGovernance = (this.isEnforceGovernance ? governance.enforceGovernance()
									: governance.disregardGovernance());
							this.loadParallelBlock(deactivateGovernance);
						}
					}
				}
			}

			// Governance setup, so must synchronise state afterwards
			this.containerState = ManagedFunctionState.SYNCHRONISE_PROCESS_STATE;

			// Determine if require updating governance
			FunctionState updateGovernance = this.getParallelBlockToExecute();
			if (updateGovernance != null) {
				return updateGovernance;
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

			// Synchronise state happening next, so must setup afterwards
			this.containerState = ManagedFunctionState.SETUP;

			// Synchronise process state to this thread (if required)
			if (threadState != processState.getMainThreadState()) {
				// Must synchronise, so execute function when executing again
				return Promise.then(this.flow.createFunction(new SynchroniseProcessStateFunctionLogic(threadState)),
						this);
			}

		case SETUP:

			// As now synchronised to process state, ensure not cancelled
			if (processState.isCancelled()) {
				throw new ProcessCancelledEscalation();
			}

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
			this.managedFunctionLogic.execute(logicContext, this.getThreadState());

			// Must recheck managed objects
			this.check = null;

			// Determine if complete (failure in asynchronous flow)
			if (this.containerState == ManagedFunctionState.COMPLETED) {
				// Completed so undertake next
				return this.getNextBlockToExecute();
			}

			// Function executed, so now await flow completions
			this.containerState = ManagedFunctionState.AWAIT_FLOW_COMPLETIONS;

		case AWAIT_FLOW_COMPLETIONS:

			// Capture execute follow up functions
			FunctionState executeFunctions = null;

			// Only check on asynchronous flows if any instantiated (avoiding lock)
			// Note: as may not be under lock safety, must check under safety
			if (this.isAsynchronousFlows) {
				synchronized (this.getThreadState()) {

					// Wait on any new asynchronous flows
					ActiveAsynchronousFlow asynchronousFlow = this.awaitingAsynchronousFlowCompletions.getHead();
					while (asynchronousFlow != null) {
						if (!asynchronousFlow.isWaiting()) {
							executeFunctions = Promise.then(executeFunctions, asynchronousFlow.waitOnCompletion());
						}
						asynchronousFlow = asynchronousFlow.getNext();
					}

					// Ensure asynchronous flows are complete
					if (this.awaitingAsynchronousFlowCompletions.getHead() != null) {
						return executeFunctions;
					}
				}
			}

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
				return this.getParallelBlockToExecute();
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
					this.loadSequentialBlock(nextContainer);
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
			return this.getNextBlockToExecute();

		case FAILED:
			throw new IllegalStateException(
					"Should not attempt to execute " + ManagedFunctionContainer.class.getSimpleName()
							+ " when in failed state (function: " + this.functionLogicMetaData.getFunctionName() + ")");

		default:
			throw new IllegalStateException("Should not be in state " + this.containerState + " (function: "
					+ this.functionLogicMetaData.getFunctionName() + ")");
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
	 * Cancels this and downstream {@link BlockState} instances.
	 * 
	 * @param isCancelThis Indicates whether to cancel this
	 *                     {@link ManagedFunctionContainer}. Note all downstream
	 *                     {@link BlockState} instances are still cancelled.
	 * @return Next {@link FunctionState} to handle cancel.
	 */
	private FunctionState cancel(boolean isCancelThis) {
		return new ManagedFunctionOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Easy access to container
				final ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Create string of clean up functions
				FunctionState cleanUpFunctions = null;

				// Clear all the parallel functions from this node
				if (container.parallelBlock != null) {
					cleanUpFunctions = Promise.then(container.parallelBlock.cancel(), cleanUpFunctions);
				}

				// Clear all the sequential functions from this node
				if (container.sequentialBlock != null) {
					cleanUpFunctions = Promise.then(container.sequentialBlock.cancel(), cleanUpFunctions);
				}

				// Clean up this function state (if required to do so)
				if (isCancelThis) {
					cleanUpFunctions = Promise.then(cleanUpFunctions, container.complete(null, null));
				}

				// Return clean up functions
				return cleanUpFunctions;
			}
		};
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

			case FAILED:
				return; // ignore if failed

			default:
				throw new IllegalStateException(
						"Can not invoke flow outside function/callback execution (state: " + container.containerState
								+ ", function: " + container.functionLogicMetaData.getFunctionName() + ")");
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
				container.loadParallelBlock(parallelFunction);

			} else {
				// Flag sequential function invoked
				container.isSequentialFunctionInvoked = true;

				// Create the function on the same flow as this function
				@SuppressWarnings("unchecked")
				ManagedFunctionContainerImpl<?> sequentialFunction = (ManagedFunctionContainerImpl<?>) container.flow
						.createManagedFunction(parameter, initialFunctionMetaData, true, container.parallelOwner);

				// Load the sequential function
				container.loadSequentialBlock(sequentialFunction);
			}
		}

		@Override
		public AsynchronousFlow createAsynchronousFlow() {

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// May not be locked on thread state, so ensure locked
			synchronized (container.getThreadState()) {

				// Flag having asynchronous flow
				container.isAsynchronousFlows = true;

				// Ensure in appropriate state to invoke flows
				switch (container.containerState) {
				case EXECUTE_FUNCTION:
				case AWAIT_FLOW_COMPLETIONS:
					break; // correct states to invoke flow

				default:
					throw new IllegalStateException(
							"Can not invoke asynchronous flow outside function/callback execution (state: "
									+ container.containerState + ", function: "
									+ container.functionLogicMetaData.getFunctionName() + ")");
				}

				// Create and register the asynchronous flow
				AsynchronousFlowImpl flow = new AsynchronousFlowImpl();
				container.awaitingAsynchronousFlowCompletions.addEntry(flow);

				// Return the asynchronous flow
				return flow;
			}
		}

		@Override
		public void setNextFunctionArgument(Object argument) {

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// Ensure to overwrite the next function argument
			switch (container.containerState) {
			case EXECUTE_FUNCTION:
			case AWAIT_FLOW_COMPLETIONS:
				break; // correct states to invoke flow

			default:
				throw new IllegalStateException(
						"Can not override next function argument outside function/callback execution (state: "
								+ container.containerState + ", function: "
								+ container.functionLogicMetaData.getFunctionName() + ")");
			}

			// Ensure the appropriate type (null always appropriate)
			if (argument != null) {
				Class<?> argumentType = argument.getClass();
				ManagedFunctionMetaData<?, ?> nextMetaData = container.functionLogicMetaData
						.getNextManagedFunctionMetaData();
				if (nextMetaData != null) {
					Class<?> expectedParameterType = nextMetaData.getParameterType();
					if ((expectedParameterType != null) && (!expectedParameterType.isAssignableFrom(argumentType))) {
						throw new IllegalArgumentException("Next expecting " + expectedParameterType.getName()
								+ " (provided " + argumentType.getName() + ")");
					}
				}
			}

			// Specify the next function argument
			container.nextManagedFunctionParameter = argument;
		}
	}

	/**
	 * {@link AsynchronousFlow} implementation.
	 */
	private class AsynchronousFlowImpl
			extends AbstractLinkedListSetEntry<ActiveAsynchronousFlow, ManagedFunctionContainer>
			implements ActiveAsynchronousFlow, AsynchronousFlow, Asset {

		/**
		 * {@link AssetLatch} for this {@link AsynchronousFlow}.
		 */
		private final AssetLatch assetLatch;

		/**
		 * Time the {@link AsynchronousFlow} was triggered.
		 */
		private final long startTime;

		/**
		 * Indicates if waiting on the {@link AsynchronousFlow}.
		 */
		private boolean isWaiting = false;

		/**
		 * Indicates if the {@link AsynchronousFlow} is complete.
		 */
		private boolean isComplete = false;

		/**
		 * Instantiate.
		 */
		private AsynchronousFlowImpl() {

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// Create the asset latch for asynchronous flow
			AssetManagerReference flowManagerReference = container.functionLogicMetaData
					.getAsynchronousFlowManagerReference();
			OfficeManager officeManager = container.getThreadState().getProcessState().getOfficeManager();
			this.assetLatch = officeManager.getAssetManager(flowManagerReference).createAssetLatch(this);

			// Capture the start time
			this.startTime = container.functionLogicMetaData.getOfficeMetaData().getMonitorClock().currentTimeMillis();
		}

		/*
		 * ================== LinkedListSetEntry =================
		 */

		@Override
		public ManagedFunctionContainer getLinkedListSetOwner() {
			return ManagedFunctionContainerImpl.this;
		}

		/*
		 * ================= ActiveAsynchronousFlow ===============
		 */

		@Override
		public boolean isWaiting() {
			return this.isWaiting;
		}

		@Override
		public FunctionState waitOnCompletion() {

			// Flag that will be now waiting
			this.isWaiting = true;

			// Handle waiting on the asynchronous flow
			return this.assetLatch.awaitOnAsset(ManagedFunctionContainerImpl.this);
		}

		/*
		 * ==================== AsynchronousFlow ==================
		 */

		@Override
		public void complete(AsynchronousFlowCompletion completion) {

			// Undertake completion of asynchronous flow
			this.assetLatch.releaseFunctions(true, new ManagedFunctionOperation() {

				@Override
				public boolean isRequireThreadStateSafety() {
					return true; // majority of time invoked by async callback thread
				}

				@Override
				public FunctionState execute(FunctionStateContext context) throws Throwable {

					// Easy access to flow and function
					AsynchronousFlowImpl flow = AsynchronousFlowImpl.this;
					ManagedFunctionContainerImpl<M> function = ManagedFunctionContainerImpl.this;

					// Determine if complete
					if (flow.isComplete) {
						return null; // already complete
					}

					// Remove from listing to allow progression
					function.awaitingAsynchronousFlowCompletions.removeEntry(flow);

					// Flag now complete
					flow.isComplete = true;

					// Complete the flow (if available)
					if (completion != null) {
						try {
							completion.run();
						} catch (Throwable ex) {
							// Only propagate if still valid processing
							switch (function.containerState) {
							case EXECUTE_FUNCTION:
							case AWAIT_FLOW_COMPLETIONS:
								// Propagate exception
								throw ex;

							default:
								// Do nothing as already complete
							}
						}
					}
					return null; // nothing further
				}
			});
		}

		/*
		 * ==================== Asset ==================
		 */

		@Override
		public ThreadState getOwningThreadState() {
			return ManagedFunctionContainerImpl.this.getThreadState();
		}

		@Override
		public void checkOnAsset(CheckAssetContext context) {

			// Do nothing if completes while checks are being undertaken
			if (this.isComplete) {
				context.releaseFunctions(true); // ensure released
				return;
			}

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// Determine if asynchronous operation has timed out
			long idleTime = container.functionLogicMetaData.getOfficeMetaData().getMonitorClock().currentTimeMillis()
					- this.startTime;
			if (idleTime > container.functionLogicMetaData.getAsynchronousFlowTimeout()) {

				// Remove from listing and consider complete
				ManagedFunctionContainerImpl.this.awaitingAsynchronousFlowCompletions.removeEntry(this);
				this.isComplete = true;

				// Timed out, so escalation failure
				context.failFunctions(
						new AsynchronousFlowTimedOutEscalation(container.functionLogicMetaData.getFunctionName()),
						true);
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
		public FunctionState flowComplete(final Throwable escalation) {
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

					// Undertake the callback
					FlowCompletionImpl.this.callback.run(escalation);

					// Must recheck managed objects
					container.check = null;

					// Continue execution of this managed function
					return container;
				}
			};
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
