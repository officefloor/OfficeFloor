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

import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCallbackFactory;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionContainerContext;
import net.officefloor.frame.internal.structure.ManagedFunctionContainerMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.RegisteredGovernance;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Abstract implementation of the {@link Job} that provides the additional
 * {@link FunctionState} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedFunctionContainer<W extends Work, N extends ManagedFunctionContainerMetaData>
		extends AbstractLinkedListSetEntry<ManagedFunctionContainer, Flow>
		implements ManagedFunctionContainer, ManagedFunctionContainerContext {

	/**
	 * Provide logging of {@link OfficeFloor} framework failures.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloor.class.getName());

	/**
	 * {@link Flow}.
	 */
	protected final Flow flow;

	/**
	 * {@link WorkContainer}.
	 */
	@Deprecated // bind managed objects directly to job
	protected final WorkContainer<W> workContainer;

	/**
	 * {@link ManagedFunctionContainerMetaData}.
	 */
	protected final N functionContainerMetaData;

	/**
	 * {@link work} {@link ManagedObjectIndex} instances to the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link ManagedFunction} may be executed.
	 */
	@Deprecated // make part of job meta-data
	private final ManagedObjectIndex[] requiredManagedObjects;

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
	@Deprecated // make part of job meta-data
	private final boolean[] requiredGovernance;

	/**
	 * {@link GovernanceDeactivationStrategy}.
	 */
	private final GovernanceDeactivationStrategy governanceDeactivationStrategy;

	/**
	 * <p>
	 * {@link JobNode} that is the escalation parent of this {@link JobNode}.
	 * <p>
	 * It is possible for multiple parallel {@link JobNode} instances to be
	 * added. This allows determining which parallel owner is the escalation
	 * parent.
	 */
	// TODO implement with deprecation of WorkContainer
	// private final JobNode escalationParent;

	/**
	 * State of this {@link ManagedFunctionContainer}.
	 */
	private JobState containerState = JobState.LOAD_MANAGED_OBJECTS;

	/**
	 * Next {@link AbstractManagedFunctionContainer} in the sequential listing.
	 */
	private AbstractManagedFunctionContainer<?, ?> nextTaskNode = null;

	/**
	 * Parallel {@link AbstractManagedFunctionContainer} that must be executed
	 * before this {@link AbstractManagedFunctionContainer} may be executed.
	 */
	private AbstractManagedFunctionContainer<?, ?> parallelNode = null;

	/**
	 * <p>
	 * Owner if this {@link ManagedFunctionContainer} is a parallel
	 * {@link ManagedFunctionContainer}.
	 * <p>
	 * This is the {@link ManagedFunctionContainer} that is executed once the
	 * graph from this {@link ManagedFunctionContainer} is complete.
	 */
	private AbstractManagedFunctionContainer<?, ?> parallelOwner;

	/**
	 * Index of the {@link Governance} to be configured.
	 */
	private int index_governance = 0;

	/**
	 * Parameter for the next {@link FunctionState}.
	 */
	private Object nextJobParameter;

	/**
	 * Flag indicating if a sequential {@link FunctionState} was invoked.
	 */
	private boolean isSequentialJobInvoked = false;

	/**
	 * Optional next {@link FunctionState} to be executed once the
	 * {@link ManagedFunctionContainer} has executed.
	 */
	private FunctionState nextFunction = null;

	/**
	 * Spawn {@link ThreadState} {@link FunctionState}.
	 */
	private SpawnThreadStateJobNode spawnThreadStateJobNode = null;

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow} containing this {@link Job}.
	 * @param workContainer
	 *            {@link WorkContainer} of the {@link Work} for this
	 *            {@link ManagedFunction}.
	 * @param functionContainerMetaData
	 *            {@link ManagedFunctionContainerMetaData} for this node.
	 * @param parallelOwner
	 *            Parallel owner of this {@link ManagedFunctionContainer}. May
	 *            be <code>null</code> if no owner.
	 * @param requiredManagedObjects
	 *            {@link Work} {@link ManagedObjectIndex} instances to the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link ManagedFunction} may be executed.
	 * @param requiredGovernance
	 *            Identifies the required activation state of the
	 *            {@link Governance} for this {@link Job}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy} for
	 *            {@link RegisteredGovernance}.
	 */
	public AbstractManagedFunctionContainer(Flow flow, WorkContainer<W> workContainer, N functionContainerMetaData,
			ManagedFunctionContainer parallelOwner, ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance, GovernanceDeactivationStrategy governanceDeactivationStrategy) {
		this.flow = flow;
		this.workContainer = workContainer;
		this.functionContainerMetaData = functionContainerMetaData;
		this.parallelOwner = (AbstractManagedFunctionContainer) parallelOwner;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.governanceDeactivationStrategy = governanceDeactivationStrategy;
	}

	/**
	 * Overridden by specific container to execute the {@link FunctionState}.
	 * 
	 * @param context
	 *            {@link ManagedFunctionContainerContext}.
	 * @return Parameter for the next {@link FunctionState}.
	 * @throws Throwable
	 *             If failure in executing the {@link FunctionState}.
	 */
	protected abstract Object executeFunction(ManagedFunctionContainerContext context) throws Throwable;

	/*
	 * =================== LinkedListSetEntry =================================
	 */

	@Override
	public Flow getLinkedListSetOwner() {
		return this.flow;
	}

	/*
	 * ===================== ManagedJobNode ===================================
	 */

	@Override
	public Flow getFlow() {
		return this.flow;
	}

	@Override
	public void setNextManagedFunction(ManagedFunctionContainer nextJobNode) {
		this.loadSequentialJobNode((AbstractManagedFunctionContainer) nextJobNode);
	}

	/*
	 * ===================== FunctionState ===============================
	 */

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.functionContainerMetaData.getResponsibleTeam();
	}

	@Override
	public ThreadState getThreadState() {
		return this.flow.getThreadState();
	}

	@Override
	public FunctionState execute() {

		// Obtain the thread and process state (as used throughout method)
		ThreadState threadState = this.flow.getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Profile job being executed
		threadState.profile(this.functionContainerMetaData);

		// Escalation cause
		Throwable escalationCause = null;
		try {
			// Handle failure on thread
			// (possibly from waiting for a managed object)
			escalationCause = threadState.getFailure();
			if (escalationCause != null) {
				// Clear failure on the thread, as escalating
				threadState.setFailure(null);

				// Escalate the failure on the thread
				throw escalationCause;
			}

			// Determine if alter governance
			if (this.requiredGovernance != null) {
				// Alter governance for the job
				while (this.index_governance < this.requiredGovernance.length) {
					try {
						// Determine if this governance is required
						boolean isGovernanceRequired = this.requiredGovernance[this.index_governance];

						// Determine if governance in correct state
						if (isGovernanceRequired != threadState.isGovernanceActive(this.index_governance)) {

							// Obtain the governance
							GovernanceContainer<?> governance = threadState
									.getGovernanceContainer(this.index_governance);

							// Incorrect state, so correct
							if (isGovernanceRequired) {
								// Activate the governance
								return governance.activateGovernance().then(this);

							} else {
								// De-activate the governance
								switch (this.governanceDeactivationStrategy) {
								case ENFORCE:
									return governance.enforceGovernance().then(this);

								case DISREGARD:
									return governance.disregardGovernance().then(this);

								default:
									// Unknown de-activation strategy
									throw new IllegalStateException("Unknown "
											+ GovernanceDeactivationStrategy.class.getSimpleName() + " "
											+ AbstractManagedFunctionContainer.this.governanceDeactivationStrategy);
								}
							}
						}
					} finally {
						// Increment for next governance
						this.index_governance++;
					}
				}
			}

			// Load the managed objects
			FunctionState loadJobNode = this.workContainer.loadManagedObjects(this.requiredManagedObjects, this);
			if (loadJobNode != null) {
				// Execute the job once managed objects loaded
				this.containerState = JobState.EXECUTE_JOB;
				return loadJobNode;
			}

			// Synchronise process state to this thread (if required)
			if (threadState != processState.getMainThreadState()) {
				return new SynchroniseProcessStateJobNode(threadState).then(this);
			}

			switch (this.containerState) {
			case EXECUTE_JOB:

				// Execute the job
				this.nextJobParameter = this.executeFunction(this);

				// Job executed, so now to activate the next job
				this.containerState = JobState.ACTIVATE_NEXT_JOB_NODE;

				// Spawn any threads
				if (this.spawnThreadStateJobNode != null) {
					FunctionState spawn = this.spawnThreadStateJobNode;
					this.spawnThreadStateJobNode = null;
					return spawn;
				}

				// Determine if next function registered
				if (this.nextFunction != null) {
					return this.nextFunction.then(this);
				}

			case ACTIVATE_NEXT_JOB_NODE:

				// Load next job if no sequential job invoked
				if (!this.isSequentialJobInvoked) {
					// No sequential node, load next task of flow
					ManagedFunctionMetaData<?, ?, ?> nextFunctionMetaData = this.functionContainerMetaData
							.getNextManagedFunctionMetaData();
					if (nextFunctionMetaData != null) {
						// Create next managed function container
						AbstractManagedFunctionContainer<?, ?> nextContainer = (AbstractManagedFunctionContainer<?, ?>) this.flow
								.createManagedFunction(nextFunctionMetaData, this.parallelOwner, this.nextJobParameter,
										this.governanceDeactivationStrategy);

						// Load for sequential execution
						this.loadSequentialJobNode(nextContainer);
					}

					// Sequential job now invoked
					this.isSequentialJobInvoked = true;
				}

				// Complete this job (flags state complete)
				FunctionState completeJob = this.completeJobNode();
				if (completeJob != null) {
					return completeJob.then(this);
				}

				// Obtain next job to execute
				FunctionState nextJob = this.getNextJobNodeToExecute();
				if (nextJob != null) {
					return nextJob;
				}

			case COMPLETED:
				// Already complete, thus return immediately
				return null;

			case FAILED:
				// Carry on to handle the failure
				break;

			default:
				throw new IllegalStateException("Should not be in state " + this.containerState);
			}
		} catch (PropagateEscalationError ex) {
			// Obtain the cause of the escalation
			escalationCause = ex.getCause();
			if (escalationCause == null) {
				// May have been thrown by application code
				escalationCause = ex;
			}

		} catch (Throwable ex) {
			// Flag for escalation
			escalationCause = ex;
		}

		// Job failure
		this.containerState = JobState.FAILED;
		try {

			/*
			 * FIXME escalation path takes account many sequential.
			 * 
			 * DETAILS: Adding another sequential flow transforms the previous
			 * sequential flow into a parallel flow. This allows the flows to be
			 * executed in order and maintain the sequential nature of
			 * invocation. This however may result in this flow becoming an
			 * escalation handler for the invoked sequential flow (which it
			 * should not). Some identifier is to be provided to know when
			 * actually invoked as parallel rather than sequential transformed
			 * to parallel.
			 * 
			 * MITIGATION: This is an edge case where two sequential flows are
			 * invoked and the first flow throws an Escalation that is handled
			 * by this Node. Require 'real world' example to model tests for
			 * this scenario. Note that in majority of cases that exception
			 * handling is undertaken by input ManagedObjectSource or at
			 * Office/OfficeFloor level.
			 */

			// Obtain the node to handle the escalation
			FunctionState escalationNode = null;

			// Inform thread of escalation search
			threadState.escalationStart(this);
			try {
				// Escalation from this node, so nothing further
				FunctionState clearJobNode = this.clearNodes();
				if (clearJobNode != null) {
					return clearJobNode.then(this);
				}

				// Search upwards for an escalation handler
				AbstractManagedFunctionContainer<?, ?> node = this;
				AbstractManagedFunctionContainer<?, ?> escalationOwnerNode = this.parallelOwner;
				do {
					EscalationFlow escalation = node.functionContainerMetaData.getEscalationProcedure()
							.getEscalation(escalationCause);
					if (escalation == null) {
						// Clear node as not handles escalation
						FunctionState parentClearJobNode = node.clearNodes();
						if (parentClearJobNode != null) {
							return parentClearJobNode.then(this);
						}

					} else {
						// Create the node for the escalation
						escalationNode = this.createEscalationJobNode(escalation.getTaskMetaData(), escalationCause,
								escalationOwnerNode);
					}

					// Move to parallel owner for next try
					if (node != escalationOwnerNode) {
						// Direct parallel owner of job escalating
						node = escalationOwnerNode;
					} else {
						// Ancestor parallel owner
						node = node.parallelOwner;
						escalationOwnerNode = node;
					}

				} while ((escalationNode == null) && (escalationOwnerNode != null));

			} finally {
				// Inform thread escalation search over
				threadState.escalationComplete(this);
			}

			// Determine if require a global escalation
			if (escalationNode == null) {
				// No escalation, so use global escalation
				EscalationFlow globalEscalation = null;
				switch (threadState.getEscalationLevel()) {
				case FLOW:
					// Obtain the Office escalation
					globalEscalation = processState.getOfficeEscalationProcedure().getEscalation(escalationCause);
					if (globalEscalation != null) {
						threadState.setEscalationLevel(EscalationLevel.OFFICE);
						break;
					}

				case OFFICE:
					// Tried office, now at invocation
					globalEscalation = processState.getInvocationEscalation();
					if (globalEscalation != null) {
						threadState.setEscalationLevel(EscalationLevel.INVOCATION_HANDLER);
						break;
					}

				case INVOCATION_HANDLER:
					// Tried invocation, always at office floor
					threadState.setEscalationLevel(EscalationLevel.OFFICE_FLOOR);
					globalEscalation = processState.getOfficeFloorEscalation();
					if (globalEscalation != null) {
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
				escalationNode = this.createEscalationJobNode(globalEscalation.getTaskMetaData(), escalationCause,
						null);
			}

			// Activate escalation node
			return escalationNode;

		} catch (Throwable ex) {
			// Should not receive failure here.
			// If so likely something has corrupted - eg OOM.
			LOGGER.log(Level.SEVERE, "Please restart OfficeFloor as it has become corrupt due to an unhandled failure",
					ex);
		}

		// Now complete
		FunctionState completeJobNode = this.completeJobNode();
		if (completeJobNode != null) {
			return completeJobNode.then(this);
		}

		// Nothing further
		return null;
	}

	/**
	 * Obtains the parallel {@link FunctionState} to execute.
	 * 
	 * @return Parallel {@link FunctionState} to execute.
	 */
	private FunctionState getParallelJobNodeToExecute() {

		// Determine furthest parallel node
		AbstractManagedFunctionContainer<?, ?> currentTask = this;
		AbstractManagedFunctionContainer<?, ?> nextTask = null;
		while ((nextTask = currentTask.parallelNode) != null) {
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
	private FunctionState getNextJobNodeToExecute() {

		// Determine if have parallel node
		FunctionState nextTaskContainer = this.getParallelJobNodeToExecute();
		if (nextTaskContainer != null) {
			// Parallel node
			return nextTaskContainer;
		}

		// Determine if have sequential node
		if (this.nextTaskNode != null) {
			// Sequential node
			return this.nextTaskNode;
		}

		// Determine if have parallel owner
		if (this.parallelOwner != null) {
			// Returning to owner, therefore unlink parallel node
			this.parallelOwner.parallelNode = null;

			// Parallel owner
			return this.parallelOwner;
		}

		// No further tasks
		return null;
	}

	/*
	 * =================== ManagedFunctionContainerContext ===================
	 */

	@Override
	public void next(FunctionState nextFunction) {
		this.nextFunction = nextFunction;
	}

	@Override
	public final void doFlow(FlowMetaData<?> flowMetaData, Object parameter, FlowCallback callback) {

		// Obtain the task meta-data for instigating the flow
		ManagedFunctionMetaData<?, ?, ?> initTaskMetaData = flowMetaData.getInitialTaskMetaData();

		// Instigate the flow
		switch (flowMetaData.getInstigationStrategy()) {

		case SEQUENTIAL:
			// Flag sequential job invoked
			this.isSequentialJobInvoked = true;

			// Create the job node on the same flow as this job node
			AbstractManagedFunctionContainer<?, ?> sequentialJobNode = (AbstractManagedFunctionContainer<?, ?>) this.flow
					.createManagedFunction(initTaskMetaData, this.parallelOwner, parameter,
							GovernanceDeactivationStrategy.ENFORCE);

			// Load the sequential node
			this.loadSequentialJobNode(sequentialJobNode);
			break;

		case PARALLEL:
			// Create a new flow for execution
			Flow parallelFlow = this.flow.getThreadState().createFlow();

			// Create the job node
			AbstractManagedFunctionContainer<?, ?> parallelJobNode = (AbstractManagedFunctionContainer<?, ?>) parallelFlow
					.createManagedFunction(initTaskMetaData, this, parameter, GovernanceDeactivationStrategy.ENFORCE);

			// Load the parallel node
			this.loadParallelJobNode(parallelJobNode);
			break;

		case ASYNCHRONOUS:
			FunctionState continueJobNode = (this.spawnThreadStateJobNode != null) ? this.spawnThreadStateJobNode
					: this;
			this.spawnThreadStateJobNode = new SpawnThreadStateJobNode(this.flow.getThreadState().getProcessState(),
					flowMetaData, parameter, new FlowCallbackFactory() {
						@Override
						public FunctionState createFunction(Throwable exception) {
							// TODO implement Type1481920593530.createJobNode
							throw new UnsupportedOperationException("TODO implement Type1481920593530.createJobNode");

						}
					}, this.functionContainerMetaData.getFunctionLoop(), continueJobNode);
			break;

		default:
			// Unknown instigation strategy
			throw new IllegalStateException("Unknown instigation strategy");
		}
	}

	/**
	 * Loads a sequential {@link FunctionState} relative to this
	 * {@link FunctionState} within the tree of {@link FunctionState} instances.
	 * 
	 * @param sequentialJobNode
	 *            {@link AbstractManagedFunctionContainer} to load to tree.
	 */
	private final void loadSequentialJobNode(AbstractManagedFunctionContainer<?, ?> sequentialJobNode) {

		// Obtain the next sequential node
		if (this.nextTaskNode != null) {
			// Move current sequential node to parallel node
			this.loadParallelJobNode(this.nextTaskNode);
		}

		// Set next sequential node
		this.nextTaskNode = sequentialJobNode;
	}

	/**
	 * Loads a parallel {@link FunctionState} relative to this
	 * {@link FunctionState} within the tree of {@link FunctionState} instances.
	 * 
	 * @param parallelJobNode
	 *            {@link FunctionState} to load to tree.
	 */
	private final void loadParallelJobNode(AbstractManagedFunctionContainer<?, ?> parallelJobNode) {

		// Move possible next parallel node out
		if (this.parallelNode != null) {
			parallelJobNode.parallelNode = this.parallelNode;
			this.parallelNode.parallelOwner = parallelJobNode;
		}

		// Set next parallel node
		this.parallelNode = parallelJobNode;
		parallelJobNode.parallelOwner = this;
	}

	/**
	 * Creates an {@link EscalationFlow} {@link FunctionState} from the input
	 * {@link ManagedFunctionMetaData}.
	 * 
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @param parallelOwner
	 *            Parallel owner for the {@link EscalationFlow}
	 *            {@link FunctionState}.
	 * @return {@link FunctionState}.
	 */
	private final FunctionState createEscalationJobNode(ManagedFunctionMetaData<?, ?, ?> taskMetaData, Object parameter,
			ManagedFunctionContainer parallelOwner) {

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		Flow parallelFlow = threadState.createFlow();

		// Create the job node
		FunctionState escalationJobNode = parallelFlow.createManagedFunction(taskMetaData, parallelOwner, parameter,
				GovernanceDeactivationStrategy.DISREGARD);

		// Return the escalation job node
		return escalationJobNode;
	}

	/**
	 * Clears this {@link FunctionState}.
	 */
	private final FunctionState clearNodes() {

		// Complete this job
		FunctionState completeJobNode = this.completeJobNode();
		if (completeJobNode != null) {
			return completeJobNode.then(this);
		}

		// Clear all the parallel jobs from this node
		if (this.parallelNode != null) {
			FunctionState parallelJobNode = this.parallelNode.clearNodes();
			if (parallelJobNode != null) {
				return parallelJobNode.then(this);
			}
			this.parallelNode = null;
		}

		// Clear all the sequential jobs from this node
		if (this.nextTaskNode != null) {
			FunctionState sequentialJobNode = this.nextTaskNode.clearNodes();
			if (sequentialJobNode != null) {
				return sequentialJobNode.then(this);
			}
			this.nextTaskNode = null;
		}

		// Nodes cleared
		return null;
	}

	/**
	 * Completes this {@link FunctionState}.
	 */
	private FunctionState completeJobNode() {

		// Do nothing if already complete
		if (this.containerState == JobState.COMPLETED) {
			return null;
		}

		// Clean up work container
		FunctionState unloadJob = this.workContainer.unloadWork();
		if (unloadJob != null) {
			return unloadJob.then(this);
		}

		// Clean up job node
		FunctionState flowJob = this.flow.managedFunctionComplete(this);
		if (flowJob != null) {
			return flowJob.then(this);
		}

		// Complete the job
		this.containerState = JobState.COMPLETED;
		return null;
	}

	/**
	 * State of this {@link Job}.
	 */
	private static enum JobState {

		/**
		 * Initial state requiring the {@link ManagedObject} instances to be
		 * loaded.
		 */
		LOAD_MANAGED_OBJECTS,

		/**
		 * Indicates the {@link Job} is to be executed.
		 */
		EXECUTE_JOB,

		/**
		 * Indicates to activate the next {@link ManagedJobNode}.
		 */
		ACTIVATE_NEXT_JOB_NODE,

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