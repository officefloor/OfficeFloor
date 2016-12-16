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
package net.officefloor.frame.impl.execute.job;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.linkedlistset.ComparatorLinkedListSet;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.FlowAsset;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetEntry;
import net.officefloor.frame.internal.structure.ManagedJobNodeContext;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.CriticalSection;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Abstract implementation of the {@link Job} that provides the additional
 * {@link JobNode} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedJobNodeContainer<W extends Work, N extends JobMetaData>
		implements JobNode, ManagedJobNodeContext {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFrame.class.getName());

	/**
	 * <p>
	 * Listing {@link FlowAsset} instances for this {@link JobNode} to join on
	 * at the end of each <code>doJob</code> run.
	 * <p>
	 * It will only hold the unique set of {@link FlowAsset} instances to join
	 * on.
	 */
	private final LinkedListSet<JoinFlowAsset, JobNode> joinFlowAssets = new ComparatorLinkedListSet<JoinFlowAsset, JobNode>() {
		@Override
		protected JobNode getOwner() {
			return AbstractManagedJobNodeContainer.this;
		}

		@Override
		protected boolean isEqual(JoinFlowAsset entryA, JoinFlowAsset entryB) {
			// Equal if same flow asset (to maintain unique set to join on)
			return (entryA.flowAsset == entryB.flowAsset);
		}
	};

	/**
	 * {@link Flow}.
	 */
	private final Flow flow;

	/**
	 * {@link WorkContainer}.
	 */
	private final WorkContainer<W> workContainer;

	/**
	 * {@link JobMetaData}.
	 */
	private final N nodeMetaData;

	/**
	 * State of this {@link Job}.
	 */
	private JobState jobState = JobState.LOAD_MANAGED_OBJECTS;

	/**
	 * {@link work} {@link ManagedObjectIndex} instances to the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link Task} may be executed.
	 */
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
	 * Next {@link AbstractManagedJobNodeContainer} in the sequential listing.
	 */
	private AbstractManagedJobNodeContainer<?, ?> nextTaskNode = null;

	/**
	 * Parallel {@link AbstractManagedJobNodeContainer} that must be executed
	 * before this {@link AbstractManagedJobNodeContainer} may be executed.
	 */
	private AbstractManagedJobNodeContainer<?, ?> parallelNode = null;

	/**
	 * <p>
	 * Owner if this {@link AbstractManagedJobNodeContainer} is a parallel
	 * {@link AbstractManagedJobNodeContainer}.
	 * <p>
	 * This is the {@link AbstractManagedJobNodeContainer} that is executed once
	 * the sequence from this {@link AbstractManagedJobNodeContainer} is
	 * complete.
	 */
	private AbstractManagedJobNodeContainer<?, ?> parallelOwner;

	/**
	 * Index of the {@link Governance} to be configured.
	 */
	private int index_governance = 0;

	/**
	 * Parameter for the next {@link JobNode}.
	 */
	private Object nextJobParameter;

	/**
	 * Flag indicating if the {@link JobNode} requires execution.
	 */
	private boolean isRequireExecution = true;

	/**
	 * Flag indicating if a sequential {@link JobNode} was invoked.
	 */
	private boolean isSequentialJobInvoked = false;

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow} containing this {@link Job}.
	 * @param workContainer
	 *            {@link WorkContainer} of the {@link Work} for this
	 *            {@link Task}.
	 * @param nodeMetaData
	 *            {@link JobMetaData} for this node.
	 * @param parallelOwner
	 *            If this is invoked as or a parallel {@link Task} or from a
	 *            parallel {@link Task} this will be the invoker. If not
	 *            parallel then will be <code>null</code>.
	 * @param requiredManagedObjects
	 *            {@link Work} {@link ManagedObjectIndex} instances to the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link Task} may be executed.
	 * @param requiredGovernance
	 *            Identifies the required activation state of the
	 *            {@link Governance} for this {@link Job}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy} for
	 *            {@link ActiveGovernance}.
	 */
	public AbstractManagedJobNodeContainer(Flow flow, WorkContainer<W> workContainer, N nodeMetaData,
			AbstractManagedJobNodeContainer<?, ?> parallelOwner, ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance, GovernanceDeactivationStrategy governanceDeactivationStrategy) {
		this.flow = flow;
		this.workContainer = workContainer;
		this.nodeMetaData = nodeMetaData;
		this.parallelOwner = parallelOwner;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.governanceDeactivationStrategy = governanceDeactivationStrategy;
	}

	/**
	 * Loads the {@link Job} name to the message.
	 * 
	 * @param message
	 *            Message to receive the {@link Job} name.
	 */
	protected abstract void loadJobName(StringBuilder message);

	/**
	 * Overridden by specific container to execute the {@link JobNode}.
	 * 
	 * @param context
	 *            {@link ManagedJobNodeContext}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @return Parameter for the next {@link JobNode}.
	 * @throws Throwable
	 *             If failure in executing the {@link JobNode}.
	 */
	protected abstract Object executeJobNode(ManagedJobNodeContext context, JobContext jobContext) throws Throwable;

	/*
	 * ======================== JobNode =======================================
	 */

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.nodeMetaData.getResponsibleTeam();
	}

	/**
	 * State for {@link CriticalSection} of undertaking the {@link Job}.
	 */
	private static class DoJobCriticalSectionState {

		/**
		 * {@link AbstractManagedJobNodeContainer}.
		 */
		private final AbstractManagedJobNodeContainer<?, ?> job;

		/**
		 * {@link JobContext}.
		 */
		private final JobContext jobContext;

		/**
		 * Instantiate.
		 * 
		 * @param job
		 *            {@link AbstractManagedJobNodeContainer}.
		 * @param jobContext
		 *            {@link JobContext}.
		 * @param currentTeam
		 *            Current {@link TeamIdentifier}.
		 */
		public DoJobCriticalSectionState(AbstractManagedJobNodeContainer<?, ?> job, JobContext jobContext) {
			this.job = job;
			this.jobContext = jobContext;
		}
	}

	/**
	 * {@link CriticalSection} to do the {@link ProcessState} critical aspects
	 * of the {@link Job}.
	 */
	private static final CriticalSection<JobNode, DoJobCriticalSectionState, Throwable> doJobProcessCriticalSection = new CriticalSection<JobNode, DoJobCriticalSectionState, Throwable>() {
		@Override
		public JobNode doCriticalSection(DoJobCriticalSectionState state) throws Exception {

			JobNode jobNode = null;
			switch (state.job.jobState) {
			case LOAD_MANAGED_OBJECTS:
				// Load the managed objects
				jobNode = state.job.workContainer.loadManagedObjects(state.job.requiredManagedObjects, state.jobContext,
						state.job);
				if (jobNode != null) {
					return jobNode;
				}

				// Flag Managed Objects now to be governed
				state.job.jobState = JobState.GOVERN_MANAGED_OBJECTS;

			case GOVERN_MANAGED_OBJECTS:
				// Govern the managed objects.
				// (Handles managed objects not ready)
				jobNode = state.job.workContainer.governManagedObjects(state.job.requiredManagedObjects,
						state.jobContext, state.job);
				if (jobNode != null) {
					return jobNode;
				}

				// Flag Managed Objects are governed
				state.job.jobState = JobState.COORDINATE_MANAGED_OBJECTS;

			case COORDINATE_MANAGED_OBJECTS:
				// Coordinate the managed objects.
				// (Handles managed objects not ready)
				jobNode = state.job.workContainer.coordinateManagedObjects(state.job.requiredManagedObjects,
						state.jobContext, state.job);
				if (jobNode != null) {
					return jobNode;
				}

				// Flag Managed Objects are coordinated
				state.job.jobState = JobState.EXECUTE_JOB;

			default:
				// No managed object operation for state
			}

			// As here, managed objects are ready
			return null;
		}
	};

	@Override
	public final JobNode doJob(JobContext jobContext) {

		// Access Point: Job Loop
		// Locks: Thread (if required)

		// Only one job per thread at a time
		// Obtain the thread and process state (as used throughout method)
		ThreadState threadState = this.flow.getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Profile job being executed
		threadState.profile(this.nodeMetaData);

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
							GovernanceContainer<?, ?> governance = threadState
									.getGovernanceContainer(this.index_governance);

							// Incorrect state, so correct
							if (isGovernanceRequired) {
								// Activate the governance
								return governance.activateGovernance(this);

							} else {
								// De-activate the governance
								switch (this.governanceDeactivationStrategy) {
								case ENFORCE:
									return governance.enforceGovernance(this);

								case DISREGARD:
									return governance.disregardGovernance(this);

								default:
									// Unknown de-activation strategy
									throw new IllegalStateException("Unknown "
											+ GovernanceDeactivationStrategy.class.getSimpleName() + " "
											+ AbstractManagedJobNodeContainer.this.governanceDeactivationStrategy);
								}
							}
						}
					} finally {
						// Increment for next governance
						this.index_governance++;
					}
				}
			}

			// Only take lock if have required managed objects
			if (this.requiredManagedObjects.length == 0) {
				// Only jump forward if initial state
				if (this.jobState == JobState.LOAD_MANAGED_OBJECTS) {
					// No managed objects required, so execute job
					this.jobState = JobState.EXECUTE_JOB;
				}

			} else {
				// Within process lock, ensure managed objects loaded
				DoJobCriticalSectionState state = new DoJobCriticalSectionState(this, jobContext);
				JobNode managedObjectJobNode = threadState.doProcessCriticalSection(state, doJobProcessCriticalSection);
				if (managedObjectJobNode != null) {
					return managedObjectJobNode;
				}
			}

			switch (this.jobState) {
			case EXECUTE_JOB:

				// That now executing the job node
				this.isRequireExecution = false;

				// Log execution of the Job
				if (LOGGER.isLoggable(Level.FINER)) {
					StringBuilder msg = new StringBuilder();
					msg.append("Executing job ");
					this.loadJobName(msg);
					msg.append(" (thread=");
					msg.append(threadState);
					msg.append(" process=");
					msg.append(processState);
					msg.append(", team=");
					msg.append(Thread.currentThread().getName());
					msg.append(")");
					LOGGER.log(Level.FINER, msg.toString());
				}

				// TODO separate out join on flow asset

				// Join should return the next job
				JoinFlowAsset headJoinOnFlowAsset = null;
				try {
					// Execute the job
					this.nextJobParameter = this.executeJobNode(this, jobContext);
				} finally {
					// Ensure always purge join flow assets
					headJoinOnFlowAsset = this.joinFlowAssets.purgeEntries();
				}

				// Now to handle if job is complete
				this.jobState = JobState.HANDLE_JOB_COMPLETION;

				// Join to any required flow assets
				while (headJoinOnFlowAsset != null) {
					//
					// TODO determine how to handle joins
					//
					// if (!(headJoinOnFlowAsset.flowAsset.waitOnFlow(this,
					// headJoinOnFlowAsset.timeout,
					// headJoinOnFlowAsset.token, activateSet))) {
					// // Activate as not waiting on flow
					// activateSet.addJobNode(this);
					// }
					// headJoinOnFlowAsset = headJoinOnFlowAsset.getNext();
				}

			case HANDLE_JOB_COMPLETION:
				// Handle only if job requires re-execution
				if (this.isRequireExecution) {
					// Execute the job again
					this.jobState = JobState.EXECUTE_JOB;

					// Determine if parallel task to execute
					JobNode parallelJob = this.getParallelJobNodeToExecute();
					if (parallelJob != null) {
						// Execute the parallel job before this one
						return parallelJob;
					} else {
						// Job logic not complete (must execute again)
						return this;
					}
				}

				// No re-execution, so activate the next job
				this.jobState = JobState.ACTIVATE_NEXT_JOB_IN_FLOW;

			case ACTIVATE_NEXT_JOB_IN_FLOW:

				// Load next job if no sequential job invoked
				if (!this.isSequentialJobInvoked) {
					// No sequential node, load next task of flow
					TaskMetaData<?, ?, ?> nextTaskMetaData = this.nodeMetaData.getNextTaskInFlow();
					if (nextTaskMetaData != null) {
						// Create next task
						AbstractManagedJobNodeContainer<?, ?> job = (AbstractManagedJobNodeContainer<?, ?>) this.flow
								.createManagedJobNode(nextTaskMetaData, this.parallelOwner, this.nextJobParameter,
										GovernanceDeactivationStrategy.ENFORCE);

						// Load for sequential execution
						this.loadSequentialJobNode(job);
					}

					// Sequential job now invoked
					this.isSequentialJobInvoked = true;
				}

				// Complete this job (flags state complete)
				JobNode completeJob = this.completeJobNode();
				if (completeJob != null) {
					return completeJob;
				}

				// Obtain next job to execute
				JobNode nextJob = this.getNextJobNodeToExecute();
				if (nextJob != null) {
					return null;
				}

			case COMPLETED:
				// Already complete, thus return immediately
				return null;

			case FAILED:
				// Carry on to handle the failure
				break;

			default:
				throw new IllegalStateException("Should not be in state " + this.jobState);
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

			// Log execution of the Job
			if (LOGGER.isLoggable(Level.FINE)) {
				StringBuilder msg = new StringBuilder();
				msg.append("EXCEPTION from job ");
				this.loadJobName(msg);
				msg.append(" (thread=");
				msg.append(threadState);
				msg.append(" process=");
				msg.append(processState);
				msg.append(", team=");
				msg.append(Thread.currentThread().getName());
				msg.append(")");
				LOGGER.log(Level.FINE, msg.toString(), ex);
			}
		}

		// Job failure
		this.jobState = JobState.FAILED;
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
			JobNode escalationNode = null;

			// Inform thread of escalation search
			threadState.escalationStart(this);
			try {
				// Escalation from this node, so nothing further
				JobNode clearJobNode = this.clearNodes();
				if (clearJobNode != null) {
					return clearJobNode;
				}

				// Search upwards for an escalation handler
				AbstractManagedJobNodeContainer<?, ?> node = this;
				AbstractManagedJobNodeContainer<?, ?> escalationOwnerNode = this.parallelOwner;
				do {
					EscalationFlow escalation = node.nodeMetaData.getEscalationProcedure()
							.getEscalation(escalationCause);
					if (escalation == null) {
						// Clear node as not handles escalation
						JobNode parentClearJobNode = node.clearNodes();
						if (parentClearJobNode != null) {
							return parentClearJobNode;
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
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "FAILURE: please restart OfficeFloor as likely become corrupt", ex);
			}
		}

		// Now complete
		return this.completeJobNode();
	}

	/**
	 * Obtains the parallel {@link JobNode} to execute.
	 * 
	 * @return Parallel {@link JobNode} to execute.
	 */
	private JobNode getParallelJobNodeToExecute() {

		// Determine furthest parallel node
		AbstractManagedJobNodeContainer<?, ?> currentTask = this;
		AbstractManagedJobNodeContainer<?, ?> nextTask = null;
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
	 * Obtains the next {@link JobNode} to execute.
	 * 
	 * @return Next {@link JobNode} to execute.
	 */
	private JobNode getNextJobNodeToExecute() {

		// Determine if have parallel node
		JobNode nextTaskContainer = this.getParallelJobNodeToExecute();
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
	 * ======================= ManagedJobNodeContext ==========================
	 */

	@Override
	public final void setJobComplete(boolean isComplete) {
		this.isRequireExecution = !isComplete;
	}

	@Override
	public final void joinFlow(FlowFuture flowFuture, long timeout, Object token) {

		// Flow future must be a FlowFutureToken
		if (!(flowFuture instanceof FlowFutureToken)) {
			throw new IllegalArgumentException(
					"Invalid " + FlowFuture.class.getSimpleName() + " (future=" + flowFuture + ", future type="
							+ flowFuture.getClass().getName() + ", required type=" + FlowFuture.class.getName() + ")");
		}

		// Obtain the flow future token
		FlowFutureToken flowFurtureToken = (FlowFutureToken) flowFuture;

		// Transform actual flow future to its flow asset
		FlowAsset flowAsset = (FlowAsset) flowFurtureToken.flowFuture;

		// Add flow asset to be joined on at completion of this job
		this.joinFlowAssets.addEntry(new JoinFlowAsset(this, flowAsset, timeout, token));
	}

	@Override
	public final FlowFuture doFlow(FlowMetaData<?> flowMetaData, Object parameter) {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?> initTaskMetaData = flowMetaData.getInitialTaskMetaData();

		// Instigate the flow
		FlowFuture flowFuture;
		switch (flowMetaData.getInstigationStrategy()) {

		case SEQUENTIAL:
			// Flag sequential job invoked
			this.isSequentialJobInvoked = true;

			// Create the job node on the same flow as this job node
			AbstractManagedJobNodeContainer<?, ?> sequentialJobNode = (AbstractManagedJobNodeContainer<?, ?>) this.flow
					.createManagedJobNode(initTaskMetaData, this.parallelOwner, parameter,
							GovernanceDeactivationStrategy.ENFORCE);

			// Load the sequential node
			this.loadSequentialJobNode(sequentialJobNode);
			flowFuture = this.flow;
			break;

		case PARALLEL:
			// Create a new flow for execution
			Flow parallelFlow = this.flow.getThreadState().createJobSequence();

			// Create the job node
			AbstractManagedJobNodeContainer<?, ?> parallelJobNode = (AbstractManagedJobNodeContainer<?, ?>) parallelFlow
					.createManagedJobNode(initTaskMetaData, this, parameter, GovernanceDeactivationStrategy.ENFORCE);

			// Load the parallel node
			this.loadParallelJobNode(parallelJobNode);
			flowFuture = parallelFlow;
			break;

		case ASYNCHRONOUS:
			flowFuture = this.flow.getThreadState().spawnThreadState(flowMetaData, parameter);
			break;

		default:
			// Unknown instigation strategy
			throw new IllegalStateException("Unknown instigation strategy");
		}

		// Return the flow future token for joining on
		return new FlowFutureToken(flowFuture);
	}

	/**
	 * Loads a sequential {@link JobNode} relative to this {@link JobNode}
	 * within the tree of {@link JobNode} instances.
	 * 
	 * @param sequentialJobNode
	 *            {@link AbstractManagedJobNodeContainer} to load to tree.
	 */
	private final void loadSequentialJobNode(AbstractManagedJobNodeContainer<?, ?> sequentialJobNode) {

		// Obtain the next sequential node
		if (this.nextTaskNode != null) {
			// Move current sequential node to parallel node
			this.loadParallelJobNode(this.nextTaskNode);
		}

		// Set next sequential node
		this.nextTaskNode = sequentialJobNode;
	}

	/**
	 * Loads a parallel {@link JobNode} relative to this {@link JobNode} within
	 * the tree of {@link JobNode} instances.
	 * 
	 * @param parallelJobNode
	 *            {@link JobNode} to load to tree.
	 */
	private final void loadParallelJobNode(AbstractManagedJobNodeContainer<?, ?> parallelJobNode) {

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
	 * Creates an {@link EscalationFlow} {@link JobNode} from the input
	 * {@link TaskMetaData}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @param parallelOwner
	 *            Parallel owner for the {@link EscalationFlow} {@link JobNode}.
	 * @return {@link JobNode}.
	 */
	private final JobNode createEscalationJobNode(TaskMetaData<?, ?, ?> taskMetaData, Object parameter,
			JobNode parallelOwner) {

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		Flow parallelFlow = threadState.createJobSequence();

		// Create the job node
		JobNode escalationJobNode = parallelFlow.createManagedJobNode(taskMetaData, parallelOwner, parameter,
				GovernanceDeactivationStrategy.DISREGARD);

		// Return the escalation job node
		return escalationJobNode;
	}

	/**
	 * Clears this {@link JobNode}.
	 */
	private final JobNode clearNodes() {

		// Complete this job
		JobNode completeJobNode = this.completeJobNode();
		if (completeJobNode != null) {
			return completeJobNode;
		}

		// Clear all the parallel jobs from this node
		if (this.parallelNode != null) {
			JobNode parallelJobNode = this.parallelNode.clearNodes();
			if (parallelJobNode != null) {
				return parallelJobNode;
			}
			this.parallelNode = null;
		}

		// Clear all the sequential jobs from this node
		if (this.nextTaskNode != null) {
			JobNode sequentialJobNode = this.nextTaskNode.clearNodes();
			if (sequentialJobNode != null) {
				return sequentialJobNode;
			}
			this.nextTaskNode = null;
		}

		// Nodes cleared
		return null;
	}

	/**
	 * Completes this {@link JobNode}.
	 */
	private JobNode completeJobNode() {

		// Do nothing if already complete
		if (this.jobState == JobState.COMPLETED) {
			return null;
		}

		// Clean up work container
		JobNode unloadJob = this.workContainer.unloadWork(this);
		if (unloadJob != null) {
			return unloadJob;
		}

		// Clean up job node
		JobNode flowJob = this.flow.jobNodeComplete(this);
		if (flowJob != null) {
			return flowJob;
		}

		// Complete the job
		this.jobState = JobState.COMPLETED;
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
		 * Indicates the {@link ManagedObject} instances require
		 * {@link Governance}.
		 */
		GOVERN_MANAGED_OBJECTS,

		/**
		 * Indicates the {@link ManagedObject} instances require coordinating.
		 */
		COORDINATE_MANAGED_OBJECTS,

		/**
		 * Indicates the {@link Job} is to be executed.
		 */
		EXECUTE_JOB,

		/**
		 * Handles if the {@link Job} is completed.
		 */
		HANDLE_JOB_COMPLETION,

		/**
		 * Indicates to activate the next {@link Task} in the {@link Flow}.
		 */
		ACTIVATE_NEXT_JOB_IN_FLOW,

		/**
		 * Failure in executing.
		 */
		FAILED,

		/**
		 * {@link TaskContainer} has completed.
		 */
		COMPLETED,

	}

	/**
	 * {@link LinkedListSetEntry} to contain a {@link FlowAsset} that this
	 * {@link JobNode} is to join on.
	 */
	private static final class JoinFlowAsset extends AbstractLinkedListSetEntry<JoinFlowAsset, JobNode> {

		/**
		 * {@link LinkedListSetEntry} {@link JobNode} owner.
		 */
		private final JobNode listOwnerJobNode;

		/**
		 * {@link FlowAsset} that this {@link JobNode} is to join on.
		 */
		public final FlowAsset flowAsset;

		/**
		 * Timeout in milliseconds for the {@link Flow} join.
		 */
		public final long timeout;

		/**
		 * {@link Flow} join token.
		 */
		public final Object token;

		/**
		 * Initiate.
		 * 
		 * @param listOwnerJobNode
		 *            {@link LinkedListSetEntry} {@link JobNode} owner.
		 * @param flowAsset
		 *            {@link FlowAsset} that this {@link JobNode} is to join on.
		 * @param timeout
		 *            Timeout in milliseconds for the {@link Flow} join.
		 * @param token
		 *            {@link Flow} join token.
		 */
		public JoinFlowAsset(JobNode listOwnerJobNode, FlowAsset flowAsset, long timeout, Object token) {
			this.listOwnerJobNode = listOwnerJobNode;
			this.flowAsset = flowAsset;
			this.timeout = timeout;
			this.token = token;
		}

		/*
		 * ==================== LinkedListSetEntry ===========================
		 */

		@Override
		public JobNode getLinkedListSetOwner() {
			return this.listOwnerJobNode;
		}
	}

	/**
	 * <p>
	 * Token class returned from <code>doFlow</code> that is the only input to
	 * <code>joinFlow</code>.
	 * <p>
	 * As application code will be provided a {@link FlowFuture} this wraps the
	 * actual internal framework {@link FlowFuture} to prevent access to
	 * internals of the framework.
	 */
	private static final class FlowFutureToken implements FlowFuture {

		/**
		 * Actual {@link FlowFuture}.
		 */
		private final FlowFuture flowFuture;

		/**
		 * Initiate.
		 * 
		 * @param flowFuture
		 *            Actual {@link FlowFuture}.
		 */
		public FlowFutureToken(FlowFuture flowFuture) {
			this.flowFuture = flowFuture;
		}

		/*
		 * ==================== FlowFuture ==================================
		 */

		@Override
		public boolean isComplete() {
			return this.flowFuture.isComplete();
		}
	}

}