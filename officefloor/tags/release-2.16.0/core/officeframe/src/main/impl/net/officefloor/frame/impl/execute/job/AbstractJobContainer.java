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
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowAsset;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.LinkedListSet;
import net.officefloor.frame.internal.structure.LinkedListSetEntry;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
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
public abstract class AbstractJobContainer<W extends Work, N extends JobMetaData>
		extends AbstractLinkedListSetEntry<JobNode, JobSequence> implements
		Job, JobNode, JobExecuteContext, ContainerContext {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFrame.class
			.getName());

	/**
	 * {@link JobSequence}.
	 */
	protected final JobSequence flow;

	/**
	 * {@link WorkContainer}.
	 */
	protected final WorkContainer<W> workContainer;

	/**
	 * {@link JobMetaData}.
	 */
	protected final N nodeMetaData;

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
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link JobSequence} containing this {@link Job}.
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
	public AbstractJobContainer(JobSequence flow,
			WorkContainer<W> workContainer, N nodeMetaData,
			JobNode parallelOwner, ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance,
			GovernanceDeactivationStrategy governanceDeactivationStrategy) {
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
	 * Overridden by specific container to execute the {@link Job}.
	 * 
	 * @param context
	 *            {@link JobExecuteContext}.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @return Parameter for the next {@link Job}.
	 * @throws Throwable
	 *             If failure in executing the {@link Job}.
	 */
	protected abstract Object executeJob(JobExecuteContext context,
			JobContext jobContext, JobNodeActivateSet activateSet)
			throws Throwable;

	/*
	 * =================== LinkedListSetEntry ================================
	 */

	@Override
	public JobSequence getLinkedListSetOwner() {
		return this.flow;
	}

	/*
	 * ===================== ContainerContext ===============================
	 */

	/**
	 * Flag indicating for the {@link Job} to wait to be activated at a later
	 * time.
	 */
	private boolean isJobToWait = false;

	/**
	 * Flag indicating a setup {@link Job} requires executing.
	 */
	private boolean isSetupJob = false;

	@Override
	public void flagJobToWait() {
		// Flag for Job to wait
		this.isJobToWait = true;
	}

	@Override
	public void addSetupTask(TaskMetaData<?, ?, ?> taskMetaData,
			Object parameter) {

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		JobSequence parallelFlow = threadState.createJobSequence();

		// Create the job node
		JobNode parallelJobNode = parallelFlow.createTaskNode(taskMetaData,
				this, parameter, GovernanceDeactivationStrategy.ENFORCE);

		// Load the parallel node
		this.loadParallelJobNode(parallelJobNode);

		// Flag setup job
		this.isSetupJob = true;
	}

	@Override
	public void addGovernanceActivity(GovernanceActivity<?, ?> activity) {

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		JobSequence parallelFlow = threadState.createJobSequence();

		// Create the job node
		JobNode parallelJobNode = parallelFlow.createGovernanceNode(activity,
				this);

		// Load the parallel node
		this.loadParallelJobNode(parallelJobNode);

		// Flag setup job
		this.isSetupJob = true;
	}

	/*
	 * ======================== Job ==========================================
	 */

	/**
	 * Next {@link Job} that is managed by the {@link Team}.
	 */
	private Job nextJob = null;

	@Override
	public final void setNextJob(Job task) {
		this.nextJob = task;
	}

	@Override
	public final Job getNextJob() {
		return this.nextJob;
	}

	@Override
	public Object getProcessIdentifier() {
		return this.flow.getThreadState().getProcessState()
				.getProcessIdentifier();
	}

	/**
	 * Flag indicating if the {@link Job} is complete. This indicates only when
	 * the {@link Job} is not yet complete.
	 */
	private boolean isComplete;

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
			return AbstractJobContainer.this;
		}

		@Override
		protected boolean isEqual(JoinFlowAsset entryA, JoinFlowAsset entryB) {
			// Equal if same flow asset (to maintain unique set to join on)
			return (entryA.flowAsset == entryB.flowAsset);
		}
	};

	/**
	 * Parameter for the next {@link Job}.
	 */
	private Object nextJobParameter;

	/**
	 * Flag indicating if a sequential {@link Job} was invoked.
	 */
	private boolean isSequentialJobInvoked = false;

	@Override
	public void cancelJob(Exception cause) {
		/*
		 * At moment, not seeing loads to require this as a priority.
		 * 
		 * TODO implement after HTTP Security to allow admission control
		 * algorithms.
		 */
		throw new UnsupportedOperationException("TODO implement Job.cancelJob");
	}

	@Override
	public final boolean doJob(JobContext jobContext) {

		// Access Point: Team
		// Locks: None

		// Setup job to be activated
		JobNode setupJob = null;

		// Obtain the current team (as used throughout method)
		TeamIdentifier currentTeam = jobContext.getCurrentTeam();

		// Ensure activate and wait on flow
		JobNodeActivatableSet activateSet = this.nodeMetaData
				.createJobActivableSet();
		JoinFlowAsset headJoinOnFlowAsset = null;
		try {

			// Obtain the thread and process state (as used throughout method)
			ThreadState threadState = this.flow.getThreadState();
			ProcessState processState = threadState.getProcessState();

			// Only one job per thread at a time
			synchronized (threadState.getThreadLock()) {

				// Profile job being executed
				threadState.profile(this.nodeMetaData);

				// Ensure no longer active
				try {
					// Flag active and no longer queued with team
					this.isActive = true;
					this.isQueuedWithTeam = false;

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

						// Reset flags for managed objects readiness
						this.isJobToWait = false;
						this.isSetupJob = false;

						// Determine if alter governance
						if (this.requiredGovernance != null) {
							// Alter governance for the job
							boolean isGovernanceAltered = false;
							for (int i = 0; i < this.requiredGovernance.length; i++) {
								boolean isGovernanceRequired = this.requiredGovernance[i];

								// Determine if governance in correct state
								if (isGovernanceRequired != threadState
										.isGovernanceActive(i)) {

									// Obtain the governance
									GovernanceContainer<?, ?> governance = threadState
											.getGovernanceContainer(i);

									// Incorrect state, so correct
									if (isGovernanceRequired) {
										// Activate the governance
										governance.activateGovernance(this);

									} else {
										// De-activate the governance
										switch (this.governanceDeactivationStrategy) {
										case ENFORCE:
											governance.enforceGovernance(this);
											break;

										case DISREGARD:
											governance
													.disregardGovernance(this);
											break;

										default:
											// Unknown de-activation strategy
											throw new IllegalStateException(
													"Unknown "
															+ GovernanceDeactivationStrategy.class
																	.getSimpleName()
															+ " "
															+ this.governanceDeactivationStrategy);
										}
									}

									// Flag that governance altered
									isGovernanceAltered = true;
								}
							}

							// If governance changed, wait the change
							if (isGovernanceAltered) {
								return true;
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
							// Within process lock, ensure managed objects ready
							synchronized (processState.getProcessLock()) {

								// Only govern once on pass through
								boolean isGovernManagedObjects = false;

								switch (this.jobState) {
								case LOAD_MANAGED_OBJECTS:
									// Load the managed objects
									this.workContainer.loadManagedObjects(
											this.requiredManagedObjects,
											jobContext, this, activateSet,
											currentTeam, this);

									// Flag Managed Objects now to be governed
									this.jobState = JobState.GOVERN_MANAGED_OBJECTS;

								case GOVERN_MANAGED_OBJECTS:
									// Govern the managed objects.
									// (Handles managed objects not ready)
									this.workContainer
											.governManagedObjects(
													this.requiredManagedObjects,
													jobContext, this,
													activateSet, this);

									// Attempted to govern managed objects
									isGovernManagedObjects = true;

									// Determine if Job to wait
									if (this.isJobToWait) {
										// Woken when ready to govern again
										return true;
									}

									// Flag Managed Objects are governed
									this.jobState = JobState.COORDINATE_MANAGED_OBJECTS;

								case COORDINATE_MANAGED_OBJECTS:
									// Coordinate the managed objects.
									// (Handles managed objects not ready)
									this.workContainer
											.coordinateManagedObjects(
													this.requiredManagedObjects,
													jobContext, this,
													activateSet, this);

									// Determine if Job to wait
									if (this.isJobToWait) {
										// Woken when ready to coordinate again
										return true;
									}

									// Flag Managed Objects are coordinated
									this.jobState = JobState.EXECUTE_JOB;

								default:
									// Ensure appropriate governance to execute.
									// May change due to parallel re-execution.
									if ((!isGovernManagedObjects)
											&& (this.jobState == JobState.EXECUTE_JOB)) {
										// Govern managed objects
										this.workContainer
												.governManagedObjects(
														this.requiredManagedObjects,
														jobContext, this,
														activateSet, this);

										// Determine if Job to wait
										if (this.isJobToWait) {
											// Woken when ready to govern again
											return true;
										}
									}

									// Ensure managed objects are ready.
									// Coordinating cause asynchronous operation
									if (!this.workContainer
											.isManagedObjectsReady(
													this.requiredManagedObjects,
													jobContext, this,
													activateSet, this)) {
										// Woken up when ready
										return true;
									}
								}
							}
						}

						switch (this.jobState) {
						case EXECUTE_JOB:

							// Determine if setup job
							if (this.isSetupJob) {
								return true; // setup before execute
							}

							// Flag complete by default and not waiting
							this.isComplete = true;

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

							try {
								// Execute the job
								this.nextJobParameter = this.executeJob(this,
										jobContext, activateSet);
							} finally {
								// Ensure always purge join flow assets
								headJoinOnFlowAsset = this.joinFlowAssets
										.purgeEntries();
							}

							// Will now normally execute setup job as next job
							this.isSetupJob = false;

							// Now to handle if job is complete
							this.jobState = JobState.HANDLE_JOB_COMPLETION;

							// Determine if join to a flow asset
							if (headJoinOnFlowAsset != null) {
								// Finally block handles joins (outside locks)
								return true; // join to wake this job
							}

						case HANDLE_JOB_COMPLETION:
							// Handle only if job not completed
							if (!this.isComplete) {
								// Execute the job again
								this.jobState = JobState.EXECUTE_JOB;

								// Determine if parallel task to execute
								JobNode parallelJob = this
										.getParallelJobNodeToExecute();
								if (parallelJob != null) {
									// Execute the parallel job (on same thread)
									parallelJob.activateJob(currentTeam);
									if (this.isParallelJobsNotComplete()) {
										// Parallel job wakes up when complete
										return true;
									} else {
										// Parallel job completed, re-run this
										this.isQueuedWithTeam = true;
										return false;
									}
								} else {
									// Job logic not complete (still with team)
									this.isQueuedWithTeam = true;
									return false;
								}
							}

							// No parallel job so try for next job
							this.jobState = JobState.ACTIVATE_NEXT_JOB_IN_FLOW;

						case ACTIVATE_NEXT_JOB_IN_FLOW:

							// Load next job if no sequential job invoked
							if (!this.isSequentialJobInvoked) {
								// No sequential node, load next task of flow
								TaskMetaData<?, ?, ?> nextTaskMetaData = this.nodeMetaData
										.getNextTaskInFlow();
								if (nextTaskMetaData != null) {
									// Create next task
									JobNode job = this.flow
											.createTaskNode(
													nextTaskMetaData,
													this.parallelOwner,
													this.nextJobParameter,
													GovernanceDeactivationStrategy.ENFORCE);

									// Load for sequential execution
									this.loadSequentialJobNode(job);
								}

								// Sequential job now invoked
								this.isSequentialJobInvoked = true;
							}

							// Determine if parallel task to execute
							JobNode parallelJob = this
									.getParallelJobNodeToExecute();
							if (parallelJob != null) {
								// Execute the parallel job (on same thread)
								parallelJob.activateJob(currentTeam);
								if (this.isParallelJobsNotComplete()) {
									// Parallel job wakes up when complete
									return true;
								}
							}

							// Assign next job to team (same thread)
							JobNode job = this.getNextJobNodeToExecute();
							if (job != null) {
								job.activateJob(currentTeam);
							}

							// Complete this job (flags state complete)
							this.completeJob(activateSet, currentTeam);

						case COMPLETED:
							// Already complete, thus return immediately
							return true;

						case FAILED:
							// Carry on to handle the failure
							break;

						default:
							throw new IllegalStateException(
									"Should not be in state " + this.jobState);
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
						 * DETAILS: Adding another sequential flow transforms
						 * the previous sequential flow into a parallel flow.
						 * This allows the flows to be executed in order and
						 * maintain the sequential nature of invocation. This
						 * however may result in this flow becoming an
						 * escalation handler for the invoked sequential flow
						 * (which it should not). Some identifier is to be
						 * provided to know when actually invoked as parallel
						 * rather than sequential transformed to parallel.
						 * 
						 * MITIGATION: This is an edge case where two sequential
						 * flows are invoked and the first flow throws an
						 * Escalation that is handled by this Node. Require
						 * 'real world' example to model tests for this
						 * scenario. Note that in majority of cases that
						 * exception handling is undertaken by input
						 * ManagedObjectSource or at Office/OfficeFloor level.
						 */

						// Obtain the node to handle the escalation
						JobNode escalationNode = null;

						// Inform thread of escalation search
						threadState.escalationStart(this, activateSet);
						try {
							// Escalation from this node, so nothing further
							this.clearNodes(activateSet, currentTeam);

							// Search upwards for an escalation handler
							JobNode node = this;
							JobNode escalationOwnerNode = this
									.getParallelOwner();
							do {
								EscalationFlow escalation = node
										.getEscalationProcedure()
										.getEscalation(escalationCause);
								if (escalation == null) {
									// Clear node as not handles escalation
									node.clearNodes(activateSet, currentTeam);
								} else {
									// Create the node for the escalation
									escalationNode = this
											.createEscalationJobNode(escalation
													.getTaskMetaData(),
													escalationCause,
													escalationOwnerNode);
								}

								// Move to parallel owner for next try
								if (node != escalationOwnerNode) {
									// Direct parallel owner of job escalating
									node = escalationOwnerNode;
								} else {
									// Ancestor parallel owner
									node = node.getParallelOwner();
									escalationOwnerNode = node;
								}

							} while ((escalationNode == null)
									&& (escalationOwnerNode != null));

						} finally {
							// Inform thread escalation search over
							threadState.escalationComplete(this, activateSet);
						}

						// Note: invoking escalation node is done outside
						// escalation search as passive teams may complete
						// the thread but in escalation search state, the thread
						// will not be allowed to complete leaving it hanging.

						// Determine if require a global escalation
						if (escalationNode == null) {
							// No escalation, so use global escalation
							EscalationFlow globalEscalation = null;
							switch (threadState.getEscalationLevel()) {
							case FLOW:
								// Obtain the Office escalation
								globalEscalation = processState
										.getOfficeEscalationProcedure()
										.getEscalation(escalationCause);
								if (globalEscalation != null) {
									threadState
											.setEscalationLevel(EscalationLevel.OFFICE);
									break;
								}

							case OFFICE:
								// Tried office, now at invocation
								globalEscalation = processState
										.getInvocationEscalation();
								if (globalEscalation != null) {
									threadState
											.setEscalationLevel(EscalationLevel.INVOCATION_HANDLER);
									break;
								}

							case INVOCATION_HANDLER:
								// Tried invocation, always at office floor
								threadState
										.setEscalationLevel(EscalationLevel.OFFICE_FLOOR);
								globalEscalation = processState
										.getOfficeFloorEscalation();
								if (globalEscalation != null) {
									break;
								}

							case OFFICE_FLOOR:
								// Should not be escalating at office floor.
								// Allow stderr failure to pick up issue.
								throw escalationCause;

							default:
								throw new IllegalStateException(
										"Should not be in state "
												+ threadState
														.getEscalationLevel());
							}

							// Create the global escalation
							escalationNode = this.createEscalationJobNode(
									globalEscalation.getTaskMetaData(),
									escalationCause, null);
						}

						// Activate escalation node
						escalationNode.activateJob(currentTeam);

					} catch (Throwable ex) {
						// Should not receive failure here.
						// If so likely something has corrupted - eg OOM.
						if (LOGGER.isLoggable(Level.SEVERE)) {
							LOGGER.log(
									Level.SEVERE,
									"FAILURE: please restart OfficeFloor as likely become corrupt",
									ex);
						}
					}

					// Now complete
					this.completeJob(activateSet, currentTeam);
					return true;

				} finally {
					// Job no longer active
					this.isActive = false;

					// Set setup job to be activated
					if (this.isSetupJob) {
						// Obtain the setup job to be activated
						setupJob = this.getParallelJobNodeToExecute();
					}
				}
			}

		} finally {
			// Outside thread lock as may be interacting with other threads

			// Join to any required flow assets
			while (headJoinOnFlowAsset != null) {
				if (!(headJoinOnFlowAsset.flowAsset.waitOnFlow(this,
						headJoinOnFlowAsset.timeout, headJoinOnFlowAsset.token,
						activateSet))) {
					// Activate as not waiting on flow
					activateSet.addJobNode(this);
				}
				headJoinOnFlowAsset = headJoinOnFlowAsset.getNext();
			}

			// Ensure activate the necessary jobs
			activateSet.activateJobNodes(currentTeam);

			// Activate setup job
			if (setupJob != null) {
				setupJob.activateJob(currentTeam);
			}
		}
	}

	/**
	 * Completes this {@link Job}.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} completing
	 *            the {@link Job}.
	 */
	private void completeJob(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam) {

		// Do nothing if already complete
		if (this.jobState == JobState.COMPLETED) {
			return;
		}

		// Complete the job
		this.jobState = JobState.COMPLETED;

		// Clean up state
		this.workContainer.unloadWork(activateSet, currentTeam);
		this.flow.jobNodeComplete(this, activateSet, currentTeam);
	}

	/**
	 * Indicates if the graph of parallel {@link JobNode} instances from this
	 * {@link JobNode}are complete.
	 * 
	 * @return <code>true</code> if the {@link JobNode} is not complete and this
	 *         {@link JobNode} should release the
	 *         {@link ThreadState#getThreadLock()} lock to allow it to complete.
	 */
	private boolean isParallelJobsNotComplete() {
		// Obtain the parallel job node
		JobNode parallelJobNode = this.getParallelNode();
		if (parallelJobNode == null) {
			// No parallel jobs, so all complete
			return false; // No non-complete job

		} else {
			// Return whether not complete
			return isParallelJobNotComplete(parallelJobNode);
		}
	}

	/**
	 * <p>
	 * Indicates if the input parallel {@link Job} is complete.
	 * <p>
	 * Passive teams may complete the {@link JobNode} immediately on
	 * {@link Team#assignJob(Job)} and hence processing of this {@link JobNode}
	 * should continue.
	 * 
	 * @param parallelJob
	 *            Parallel {@link JobNode} to check if complete.
	 * @return <code>true</code> if the {@link JobNode} is not complete and this
	 *         {@link JobNode} should release the
	 *         {@link ThreadState#getThreadLock()} lock to allow it to complete.
	 */
	private boolean isParallelJobNotComplete(JobNode parallelJob) {

		// Determine if input job node not is complete
		if (!parallelJob.isJobNodeComplete()) {
			// Not complete, so parallel nodes not yet complete
			return true;
		}

		// Also must check sequential and parallel nodes. Parallel node may
		// create these in its process and potentially either passively complete
		// them or hand them off to another team.
		boolean isNotComplete = false;
		JobNode sequentialNode = parallelJob.getNextNode();
		if (sequentialNode != null) {
			isNotComplete |= this.isParallelJobNotComplete(sequentialNode);
		}
		if (!isNotComplete) {
			JobNode parallelParallelNode = parallelJob.getParallelNode();
			if (parallelParallelNode != null) {
				isNotComplete |= this
						.isParallelJobNotComplete(parallelParallelNode);
			}
		}

		// Return if is not complete
		return isNotComplete;
	}

	/**
	 * Obtains the parallel {@link JobNode} to execute.
	 * 
	 * @return Parallel {@link JobNode} to execute.
	 */
	private JobNode getParallelJobNodeToExecute() {

		// Determine furthest parallel node
		JobNode currentTask = this;
		JobNode nextTask = null;
		while ((nextTask = currentTask.getParallelNode()) != null) {
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
		JobNode nextTask = this.getNextNode();
		if (nextTask != null) {
			// Sequential node
			return nextTask;
		}

		// Determine if have parallel owner
		nextTask = this.getParallelOwner();
		if (nextTask != null) {
			// Returning to owner, therefore unlink parallel node
			nextTask.setParallelNode(null);

			// Parallel owner
			return nextTask;
		}

		// No further tasks
		return null;
	}

	/*
	 * ======================= JobExecuteContext ==========================
	 * 
	 * All methods will be guarded by lock taken in the doJob method.
	 * Furthermore the JobContext methods do not require synchronised
	 * coordination between themselves as executing a task is single threaded.
	 */

	/**
	 * <p>
	 * {@link TeamIdentifier} for decoupling current {@link Team} from invoking
	 * the asynchronous flow.
	 * <p>
	 * Note that asynchronous flows should be queued and not executed
	 * immediately by the current {@link Team}. Should execution be required
	 * immediately please use parallel flow.
	 */
	public static TeamIdentifier ASYNCHRONOUS_FLOW_TEAM_IDENTIFIER = new TeamIdentifier() {
	};

	@Override
	public final void setJobComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	@Override
	public final void joinFlow(FlowFuture flowFuture, long timeout, Object token) {

		// Flow future must be a FlowFutureToken
		if (!(flowFuture instanceof FlowFutureToken)) {
			throw new IllegalArgumentException("Invalid "
					+ FlowFuture.class.getSimpleName() + " (future="
					+ flowFuture + ", future type="
					+ flowFuture.getClass().getName() + ", required type="
					+ FlowFuture.class.getName() + ")");
		}

		// Obtain the flow future token
		FlowFutureToken flowFurtureToken = (FlowFutureToken) flowFuture;

		// Transform actual flow future to its flow asset
		FlowAsset flowAsset = (FlowAsset) flowFurtureToken.flowFuture;

		// Add flow asset to be joined on at completion of this job
		this.joinFlowAssets.addEntry(new JoinFlowAsset(flowAsset, timeout,
				token));
	}

	@Override
	public final FlowFuture doFlow(FlowMetaData<?> flowMetaData,
			Object parameter) {

		// Instigate the flow
		FlowFuture flowFuture;
		switch (flowMetaData.getInstigationStrategy()) {
		case ASYNCHRONOUS:
			flowFuture = this.createAsynchronousFlow(flowMetaData, parameter);
			break;

		case PARALLEL:
			flowFuture = this.createParallelFlow(flowMetaData, parameter);
			break;

		case SEQUENTIAL:
			// Flag sequential job invoked
			this.isSequentialJobInvoked = true;
			flowFuture = this.createSequentialFlow(flowMetaData, parameter);
			break;

		default:
			// Unknown instigation strategy
			throw new IllegalStateException("Unknown instigation strategy");
		}

		// Return the flow future token for joining on
		return new FlowFutureToken(flowFuture);
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
	private JobNode createEscalationJobNode(TaskMetaData<?, ?, ?> taskMetaData,
			Object parameter, JobNode parallelOwner) {

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		JobSequence parallelFlow = threadState.createJobSequence();

		// Create the job node
		JobNode escalationJobNode = parallelFlow.createTaskNode(taskMetaData,
				parallelOwner, parameter,
				GovernanceDeactivationStrategy.DISREGARD);

		// Return the escalation job node
		return escalationJobNode;
	}

	/**
	 * Creates an asynchronous {@link JobSequence} from the input
	 * {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @return Asynchronous {@link FlowFuture}.
	 */
	private FlowFuture createAsynchronousFlow(FlowMetaData<?> flowMetaData,
			Object parameter) {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?> initTaskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create thread to execute asynchronously
		ProcessState processState = this.flow.getThreadState()
				.getProcessState();
		AssetManager flowAssetManager = flowMetaData.getFlowManager();
		JobSequence asyncFlow = processState.createThread(flowAssetManager);

		// Create job node for execution
		JobNode asyncJobNode = asyncFlow.createTaskNode(initTaskMetaData, null,
				parameter, GovernanceDeactivationStrategy.ENFORCE);

		// Asynchronously instigate the job node
		asyncJobNode.activateJob(ASYNCHRONOUS_FLOW_TEAM_IDENTIFIER);

		// Specify the thread flow future
		return asyncFlow.getThreadState();
	}

	/**
	 * Creates a parallel {@link JobSequence} from the input
	 * {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @return Parallel {@link JobSequence}.
	 */
	private FlowFuture createParallelFlow(FlowMetaData<?> flowMetaData,
			Object parameter) {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?> initTaskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		JobSequence parallelFlow = threadState.createJobSequence();

		// Create the job node
		JobNode parallelJobNode = parallelFlow.createTaskNode(initTaskMetaData,
				this, parameter, GovernanceDeactivationStrategy.ENFORCE);

		// Load the parallel node
		this.loadParallelJobNode(parallelJobNode);

		// Return the flow future
		return parallelFlow;
	}

	/**
	 * Creates a sequential {@link JobSequence} from the input
	 * {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @return Sequential {@link JobSequence}.
	 */
	private FlowFuture createSequentialFlow(FlowMetaData<?> flowMetaData,
			Object parameter) {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?> initTaskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create the job node on the same flow as this job node
		JobNode sequentialJobNode = this.flow.createTaskNode(initTaskMetaData,
				this.parallelOwner, parameter,
				GovernanceDeactivationStrategy.ENFORCE);

		// Load the sequential node
		this.loadSequentialJobNode(sequentialJobNode);

		// Return the flow future
		return this.flow;
	}

	/**
	 * Loads a sequential {@link JobNode} relative to this {@link JobNode}
	 * within the tree of {@link JobNode} instances.
	 * 
	 * @param sequentialJobNode
	 *            {@link JobNode} to load to tree.
	 */
	private void loadSequentialJobNode(JobNode sequentialJobNode) {

		// Obtain the next sequential node
		JobNode nextNode = this.getNextNode();
		if (nextNode != null) {

			// Move current sequential node to parallel node
			this.loadParallelJobNode(nextNode);
		}

		// Set next sequential node
		this.setNextNode(sequentialJobNode);
	}

	/**
	 * Loads a parallel {@link JobNode} relative to this {@link JobNode} within
	 * the tree of {@link JobNode} instances.
	 * 
	 * @param parallelJobNode
	 *            {@link JobNode} to load to tree.
	 */
	private void loadParallelJobNode(JobNode parallelJobNode) {

		// Obtain the next parallel node
		JobNode nextNode = this.getParallelNode();
		if (nextNode != null) {

			// Move next parallel node out
			parallelJobNode.setParallelNode(nextNode);
			nextNode.setParallelOwner(parallelJobNode);
		}

		// Set next parallel node
		this.setParallelNode(parallelJobNode);
		parallelJobNode.setParallelOwner(this);
	}

	/*
	 * ====================== JobNode ====================================
	 */

	/**
	 * <p>
	 * Owner if this {@link JobNode} is a parallel {@link JobNode}.
	 * <p>
	 * This is the {@link JobNode} that is executed once the {@link JobSequence}
	 * that this {@link JobNode} is involved within is complete.
	 */
	private JobNode parallelOwner;

	/**
	 * Parallel {@link JobNode} that must be complete before this
	 * {@link JobNode} may complete.
	 */
	private JobNode parallelNode;

	/**
	 * Next {@link JobNode} in the sequential listing of {@link JobSequence}.
	 */
	private JobNode nextTaskNode;

	/**
	 * Flag indicating if this {@link Job} has been assigned to a {@link Team}
	 * to be executed.
	 */
	private boolean isQueuedWithTeam = false;

	/**
	 * Flag indicating if this {@link Task} is active. Passive teams will try to
	 * re-enter active tasks. On doing so, they should not run the {@link Task}
	 * but return and allow earlier invocation to complete.
	 */
	private boolean isActive = false;

	@Override
	public final void activateJob(TeamIdentifier currentTeam) {

		// Access Point: JobContainer (outside ThreadState lock), OfficeManager
		// Locks: None (must be the case to avoid dead-lock)

		// Lock to ensure only one activation
		synchronized (this.flow.getThreadState().getThreadLock()) {

			// Determine if already queued, active or complete
			if (this.isQueuedWithTeam || this.isActive
					|| (this.jobState == JobState.COMPLETED)) {
				return;
			}

			// May not activate if non-complete parallel node
			if (this.isParallelJobsNotComplete()) {
				// Parallel job will activate this job later
				return;
			}

			// Flag that queued.
			// This blocks other job nodes of thread from being queued until
			// this job node is executed.
			this.isQueuedWithTeam = true;
		}

		// Activate this Job (outside thread lock)
		TeamManagement responsible = this.nodeMetaData.getResponsibleTeam();
		if (currentTeam == responsible.getIdentifier()) {
			// Same team, so let worker (thread) continue execution
			this.nodeMetaData.getContinueTeam().assignJob(this, currentTeam);

		} else {
			// Different team, so assign to team to undertake
			responsible.getTeam().assignJob(this, currentTeam);
		}
	}

	@Override
	public boolean isJobNodeComplete() {
		// Complete if in complete state
		return (this.jobState == JobState.COMPLETED);
	}

	@Override
	public JobSequence getJobSequence() {
		return this.flow;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return this.nodeMetaData.getEscalationProcedure();
	}

	@Override
	public final void setParallelOwner(JobNode jobNode) {
		this.parallelOwner = jobNode;
	}

	@Override
	public final JobNode getParallelOwner() {
		return this.parallelOwner;
	}

	@Override
	public final void setParallelNode(JobNode jobNode) {
		this.parallelNode = jobNode;
	}

	@Override
	public final JobNode getParallelNode() {
		return this.parallelNode;
	}

	@Override
	public final void setNextNode(JobNode jobNode) {
		this.nextTaskNode = jobNode;
	}

	@Override
	public final JobNode getNextNode() {
		return this.nextTaskNode;
	}

	@Override
	public final void clearNodes(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam) {

		// Complete this job
		this.completeJob(activateSet, currentTeam);

		// Clear all the parallel jobs from this node
		JobNode parallel = this.getParallelNode();
		this.setParallelNode(null);
		if (parallel != null) {
			parallel.clearNodes(activateSet, currentTeam);
		}

		// Clear all the sequential jobs from this node
		JobNode sequential = this.getNextNode();
		this.setNextNode(null);
		if (sequential != null) {
			sequential.clearNodes(activateSet, currentTeam);
		}
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
	private class JoinFlowAsset extends
			AbstractLinkedListSetEntry<JoinFlowAsset, JobNode> {

		/**
		 * {@link FlowAsset} that this {@link JobNode} is to join on.
		 */
		public final FlowAsset flowAsset;

		/**
		 * Timeout in milliseconds for the {@link JobSequence} join.
		 */
		public final long timeout;

		/**
		 * {@link JobSequence} join token.
		 */
		public final Object token;

		/**
		 * Initiate.
		 * 
		 * @param flowAsset
		 *            {@link FlowAsset} that this {@link JobNode} is to join on.
		 * @param timeout
		 *            Timeout in milliseconds for the {@link JobSequence} join.
		 * @param token
		 *            {@link JobSequence} join token.
		 */
		public JoinFlowAsset(FlowAsset flowAsset, long timeout, Object token) {
			this.flowAsset = flowAsset;
			this.timeout = timeout;
			this.token = token;
		}

		/*
		 * ==================== LinkedListSetEntry ===========================
		 */

		@Override
		public JobNode getLinkedListSetOwner() {
			return AbstractJobContainer.this;
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