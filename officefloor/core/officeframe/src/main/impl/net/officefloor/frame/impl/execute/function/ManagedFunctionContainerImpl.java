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

import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCompletion;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLogic;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicContext;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.RegisteredGovernance;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * {@link FunctionState} to execute a {@link ManagedFunctionLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionContainerImpl<M extends ManagedFunctionLogicMetaData>
		extends AbstractLinkedListSetEntry<FunctionState, Flow> implements ManagedFunctionContainer {

	/**
	 * Provide logging of {@link OfficeFloor} framework failures.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloor.class.getName());

	/**
	 * {@link Flow}.
	 */
	private final Flow flow;

	/**
	 * {@link ManagedFunctionLogicMetaData}.
	 */
	private final M functionLogicMetaData;

	/**
	 * {@link work} {@link ManagedObjectIndex} instances to the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link ManagedFunction} may be executed.
	 */
	private final ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * {@link ManagedObjectContainer} instances for the respective
	 * {@link ManagedObject} instances bound to this {@link Work}.
	 */
	private final ManagedObjectContainer[] managedObjects;

	/**
	 * {@link AdministratorContainer} instances for the respective
	 * {@link Administrator} instances bound to this {@link Work}.
	 */
	private final AdministratorContainer<?, ?>[] administrators;

	/**
	 * <p>
	 * Array identifying which {@link Governance} instances are required to be
	 * active for this {@link Job}. The {@link Governance} is identified by the
	 * index into the array.For each {@link Governance}:
	 * <ol>
	 * <li><code>true</code> indicates the {@link Governance} is to be activated
	 * (if not already activated)</li>
	 * <li><code>false</code> indicates to deactivate the {@link Governance}
	 * should it be active. The strategy for deactivation is defined by the
	 * {@link GovernanceDeactivationStrategy}.</li>
	 * </ol>
	 * <p>
	 * Should this array be <code>null</code> no change is undertaken with the
	 * {@link Governance} for the {@link Job}.
	 */
	private final boolean[] requiredGovernance;

	/**
	 * {@link GovernanceDeactivationStrategy}.
	 */
	private final GovernanceDeactivationStrategy governanceDeactivationStrategy;

	/**
	 * {@link ManagedFunctionLogic} to be executed by this
	 * {@link ManagedFunctionContainer}.
	 */
	private final ManagedFunctionLogic managedFunctionLogic;

	/**
	 * State of this {@link ManagedFunctionContainerImpl}.
	 */
	private ManagedFunctionState containerState = ManagedFunctionState.LOAD_MANAGED_OBJECTS;

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
	 * Parameter for the next {@link ManagedFunction}.
	 */
	private Object nextManagedFunctionParameter;

	/**
	 * Flag indicating if a sequential {@link FunctionState} was invoked.
	 */
	private boolean isSequentialFunctionInvoked = false;

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
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow} containing this {@link Job}.
	 * @param functionLogicMetaData
	 *            {@link ManagedFunctionLogicMetaData} for this node.
	 * @param parallelOwner
	 *            Parallel owner of this {@link ManagedFunctionContainer}. May
	 *            be <code>null</code> if no owner.
	 * @param requiredManagedObjects
	 *            {@link ManagedObjectIndex} instances to the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link ManagedFunction} may be executed.
	 * @param requiredGovernance
	 *            Identifies the required activation state of the
	 *            {@link Governance} for this {@link ManagedFunction}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy} for
	 *            {@link RegisteredGovernance}.
	 * @param managedFunctionLogic
	 *            {@link ManagedFunctionLogic} to be executed by this
	 *            {@link ManagedFunctionContainer}.
	 */
	public ManagedFunctionContainerImpl(Flow flow, M functionLogicMetaData, ManagedFunctionContainer parallelOwner,
			ManagedObjectIndex[] requiredManagedObjects, boolean[] requiredGovernance,
			GovernanceDeactivationStrategy governanceDeactivationStrategy, ManagedFunctionLogic managedFunctionLogic) {
		this.flow = flow;
		this.functionLogicMetaData = functionLogicMetaData;
		this.parallelOwner = (ManagedFunctionContainerImpl<?>) parallelOwner;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.governanceDeactivationStrategy = governanceDeactivationStrategy;
		this.managedFunctionLogic = managedFunctionLogic;

		// Create the container arrays
		this.managedObjects = new ManagedObjectContainer[this.functionLogicMetaData.getManagedObjectMetaData().length];
		this.administrators = new AdministratorContainer<?, ?>[this.functionLogicMetaData
				.getAdministratorMetaData().length];
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
	public ManagedObjectContainer getManagedObjectContainer(int index) {

		// Lazy load the container
		ManagedObjectContainer container = this.managedObjects[index];
		if (container == null) {
			container = this.functionLogicMetaData.getManagedObjectMetaData()[index]
					.createManagedObjectContainer(this.flow.getThreadState());
			this.managedObjects[index] = container;
		}
		return container;
	}

	@Override
	public AdministratorContainer<?, ?> getAdministratorContainer(int index) {

		// Lazy load the container
		AdministratorContainer<?, ?> container = this.administrators[index];
		if (container == null) {
			container = this.functionLogicMetaData.getAdministratorMetaData()[index]
					.createAdministratorContainer(this.flow.getThreadState());
			this.administrators[index] = container;
		}
		return container;
	}

	/*
	 * ===================== FunctionState ===============================
	 */

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.functionLogicMetaData.getResponsibleTeam();
	}

	@Override
	public Flow getFlow() {
		return this.flow;
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
		case LOAD_MANAGED_OBJECTS:

			// Load the managed objects
			if (this.requiredManagedObjects != null) {
				while (this.loadManagedObjectIndex < this.requiredManagedObjects.length) {
					ManagedObjectIndex index = this.requiredManagedObjects[this.loadManagedObjectIndex];
					this.loadManagedObjectIndex++;

					// Obtain the index of managed object within scope
					int scopeIndex = index.getIndexOfManagedObjectWithinScope();

					// Obtain the managed object container
					ManagedObjectContainer container;
					switch (index.getManagedObjectScope()) {
					case FUNCTION:
						// Obtain the container from this managed function
						container = this.getManagedObjectContainer(scopeIndex);
						break;

					case THREAD:
						// Obtain the container from the thread state
						container = this.flow.getThreadState().getManagedObjectContainer(scopeIndex);
						break;

					case PROCESS:
						// Obtain the container from the process state
						container = this.flow.getThreadState().getProcessState().getManagedObjectContainer(scopeIndex);
						break;

					default:
						throw new IllegalStateException(
								"Unknown managed object scope " + index.getManagedObjectScope());
					}

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
							switch (this.governanceDeactivationStrategy) {
							case ENFORCE:
								updateGovernance = Promise.then(updateGovernance, governance.enforceGovernance());

							case DISREGARD:
								updateGovernance = Promise.then(updateGovernance, governance.disregardGovernance());

							default:
								// Unknown de-activation strategy
								throw new IllegalStateException(
										"Unknown " + GovernanceDeactivationStrategy.class.getSimpleName() + " "
												+ ManagedFunctionContainerImpl.this.governanceDeactivationStrategy);
							}
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
			this.nextManagedFunctionParameter = this.managedFunctionLogic
					.execute(new ManagedFunctionLogicContextImpl());

			// Function executed, so now to activate the next function
			this.containerState = ManagedFunctionState.ACTIVATE_NEXT_FUNCTION;
			FunctionState executeFunctions = null;

			// Spawn any threads
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
				this.containerState = ManagedFunctionState.ACTIVATE_NEXT_FUNCTION;
				return Promise.then(executeFunctions, this);
			}

		case ACTIVATE_NEXT_FUNCTION:

			// Load next function if no sequential function invoked
			if (!this.isSequentialFunctionInvoked) {
				// No sequential function, load next function
				ManagedFunctionMetaData<?, ?> nextFunctionMetaData = this.functionLogicMetaData
						.getNextManagedFunctionMetaData();
				if (nextFunctionMetaData != null) {
					// Create next managed function container
					ManagedFunctionContainerImpl<?> nextContainer = (ManagedFunctionContainerImpl<?>) this.flow
							.createManagedFunction(nextFunctionMetaData, this.parallelOwner,
									this.nextManagedFunctionParameter, this.governanceDeactivationStrategy);

					// Load for sequential execution
					this.loadSequentialFunction(nextContainer);
				}

				// Sequential function now invoked
				this.isSequentialFunctionInvoked = true;
			}

			// Complete this function (flags state complete)
			FunctionState completeFunction = this.completeFunction();
			if (completeFunction != null) {
				return Promise.then(completeFunction, this);
			}

			// Obtain next function to execute
			FunctionState nextFunction = this.getNextFunctionToExecute();
			if (nextFunction != null) {
				return nextFunction;
			}

		case COMPLETED:
			// Now complete (attempt to clean up)
			FunctionState completeJobNode = this.completeFunction();
			if (completeJobNode != null) {
				return Promise.then(completeJobNode, this);
			}
			return null;

		case FAILED:
			throw new IllegalStateException("Should not attempt to execute "
					+ ManagedFunctionContainer.class.getSimpleName() + " when in failed state");

		default:
			throw new IllegalStateException("Should not be in state " + this.containerState);
		}
	}

	@Override
	public FunctionState handleEscalation(final Throwable escalation) {
		return new AbstractFunctionState(this.flow) {
			@Override
			public FunctionState execute() throws Throwable {
				// Easy access to container
				ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Function failure
				container.containerState = ManagedFunctionState.FAILED;

				// Escalation from this node, so nothing further
				FunctionState clearFunctions = container.clearFunctions();

				// Obtain the escalation flow from this function
				EscalationFlow escalationFlow = container.functionLogicMetaData.getEscalationProcedure()
						.getEscalation(escalation);
				if (escalationFlow != null) {
					// Escalation handled by this function
					return Promise.then(clearFunctions, container.createEscalationFunction(
							escalationFlow.getManagedFunctionMetaData(), escalation, container.parallelOwner));
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
		public final void doFlow(FlowMetaData flowMetaData, Object parameter, FlowCallback callback) {

			// Easy access to container
			final ManagedFunctionContainerImpl<?> container = ManagedFunctionContainerImpl.this;

			// Obtain the task meta-data for instigating the flow
			ManagedFunctionMetaData initialFunctionMetaData = flowMetaData.getInitialFunctionMetaData();

			// Determine if spawn thread
			if (flowMetaData.isSpawnThreadState()) {
				// Register to spawn the thread state
				FunctionLogic spawnFunctionLogic = new SpawnThreadFunctionLogic(flowMetaData, parameter, callback,
						container.functionLogicMetaData.getFunctionLoop());
				container.spawnThreadStateFunction = Promise.then(container.spawnThreadStateFunction,
						container.flow.createFunction(spawnFunctionLogic));

			} else if (callback != null) {
				// Have callback, so execute in parallel in own flow
				FlowCompletion completion = new FlowCompletionImpl(callback, container.functionLogicMetaData,
						container);
				Flow parallelFlow = container.flow.getThreadState().createFlow(completion);

				// Create the function
				ManagedFunctionContainerImpl<?> parallelFunction = (ManagedFunctionContainerImpl<?>) parallelFlow
						.createManagedFunction(initialFunctionMetaData, container, parameter,
								GovernanceDeactivationStrategy.ENFORCE);

				// Load the parallel function
				container.loadParallelFunction(parallelFunction);

			} else {
				// Flag sequential function invoked
				container.isSequentialFunctionInvoked = true;

				// Create the function on the same flow as this function
				ManagedFunctionContainerImpl<?> sequentialFunction = (ManagedFunctionContainerImpl<?>) container.flow
						.createManagedFunction(initialFunctionMetaData, container.parallelOwner, parameter,
								GovernanceDeactivationStrategy.ENFORCE);

				// Load the sequential function
				container.loadSequentialFunction(sequentialFunction);
			}
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
	 * Creates an {@link EscalationFlow} {@link FunctionState} from the input
	 * {@link ManagedFunctionMetaData}.
	 * 
	 * @param escalationMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @param parallelOwner
	 *            Parallel owner for the {@link EscalationFlow}
	 *            {@link FunctionState}.
	 * @return {@link FunctionState}.
	 */
	private final FunctionState createEscalationFunction(ManagedFunctionMetaData<?, ?> escalationMetaData,
			Object parameter, ManagedFunctionContainerImpl<?> parallelOwner) {

		// Create a new flow for escalation
		ThreadState threadState = this.flow.getThreadState();
		FlowCompletion completion = new FlowCompletionImpl<>(null, this.functionLogicMetaData, this);
		Flow parallelFlow = threadState.createFlow(completion);

		// Create the job node
		FunctionState escalationJobNode = parallelFlow.createManagedFunction(escalationMetaData, parallelOwner,
				parameter, GovernanceDeactivationStrategy.DISREGARD);

		// Return the escalation job node
		return escalationJobNode;
	}

	/**
	 * Clears this {@link FunctionState}.
	 */
	private final FunctionState clearFunctions() {
		return new AbstractFunctionState(this.flow) {
			@Override
			public FunctionState execute() throws Throwable {
				// Easy access to container
				final ManagedFunctionContainerImpl<M> container = ManagedFunctionContainerImpl.this;

				// Create string of clean up functions
				FunctionState cleanUpFunctions = null;

				// Clear all the parallel functions from this node
				if (container.parallelFunction != null) {
					cleanUpFunctions = Promise.then(container.parallelFunction.clearFunctions(), cleanUpFunctions);
				}

				// Clear all the sequential functions from this node
				if (container.sequentialFunction != null) {
					cleanUpFunctions = Promise.then(container.sequentialFunction.clearFunctions(), cleanUpFunctions);
				}

				// Clear this function
				return Promise.then(cleanUpFunctions, container.completeFunction());
			}
		};
	}

	/**
	 * Completes this {@link FunctionState}.
	 */
	private FunctionState completeFunction() {
		return new AbstractFunctionState(this.flow) {
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
				for (int i = 0; i < container.managedObjects.length; i++) {
					ManagedObjectContainer managedObject = container.managedObjects[i];
					cleanUpFunctions = Promise.then(cleanUpFunctions, managedObject.unloadManagedObject());
				}

				// Complete this function
				cleanUpFunctions = Promise.then(cleanUpFunctions, this.flow.managedFunctionComplete(this));

				// Function complete
				return Promise.then(cleanUpFunctions, new AbstractFunctionState(container.flow) {
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
	 * State of this {@link ManagedFunctionContainer}.
	 */
	private static enum ManagedFunctionState {

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