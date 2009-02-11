/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.job;

import junit.framework.AssertionFailedError;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.error.ExecutionError;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowAsset;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobActivatableSet;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;

/**
 * Abstract implementation of the {@link Job} that provides the additional
 * {@link JobNode} functionality.
 * 
 * @author Daniel
 */
public abstract class AbstractJobContainer<W extends Work, N extends JobMetaData>
		implements Job, JobNode, JobExecuteContext {

	/**
	 * {@link Flow}.
	 */
	protected final Flow flow;

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
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow} that this {@link Task} resides within.
	 * @param workContainer
	 *            {@link WorkContainer} of the {@link Work} for this
	 *            {@link Task}.
	 * @param nodeMetaData
	 *            {@link JobMetaData} for this node.
	 * @param parallelOwner
	 *            If this is invoked as or a parallel {@link Task} or from a
	 *            parallel {@link Task} this will be the invokee. If not
	 *            parallel then will be <code>null</code>.
	 */
	public AbstractJobContainer(Flow flow, WorkContainer<W> workContainer,
			N nodeMetaData, JobNode parallelOwner) {
		this.flow = flow;
		this.workContainer = workContainer;
		this.nodeMetaData = nodeMetaData;
		this.parallelOwner = parallelOwner;
	}

	/**
	 * Overridden by specific container to execute the {@link Job}.
	 * 
	 * @return Parameter for the next {@link Job}.
	 * @throws Throwable
	 *             If failure in executing the {@link Job}.
	 */
	protected abstract Object executeJob(JobExecuteContext context)
			throws Throwable;

	/*
	 * ======================== Job ==========================================
	 */

	/**
	 * Next {@link Job} that is managed by the {@link Team}.
	 */
	private Job nextJob = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.team.Job#setNextJob(net.officefloor.frame.spi
	 * .team.Job)
	 */
	@Override
	public final void setNextJob(Job task) {
		this.nextJob = task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Job#getNextJob()
	 */
	@Override
	public final Job getNextJob() {
		return this.nextJob;
	}

	/**
	 * Flag indicating if the {@link Job} is complete. This indicates only when
	 * the {@link Job} is not yet complete.
	 */
	private boolean isComplete;

	/**
	 * {@link FlowAsset} to wait on.
	 */
	private FlowAsset flowAsset = null;

	/**
	 * Parameter for the next {@link Job}.
	 */
	private Object nextJobParameter;

	/**
	 * Flag indicating if a sequential {@link Job} was invoked.
	 */
	private boolean isSequentialJobInvoked = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.team.Job#doJob(net.officefloor.frame.spi.team
	 * .JobContext)
	 */
	@Override
	public final boolean doJob(JobContext executionContext) {

		// Access Point: Team
		// Locks: None

		// Ensure notify and wait on flow
		JobActivatableSet notifySet = this.nodeMetaData.createJobActivableSet();
		FlowAsset waitOnFlowAsset = null;
		try {

			// Obtain the thread and process state (as used throughout method)
			ThreadState threadState = this.flow.getThreadState();
			ProcessState processState = threadState.getProcessState();

			// Only one job per thread at a time
			synchronized (threadState.getThreadLock()) {

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

						// Obtain the required managed object indexes
						int[] managedObjectIndexes = this.nodeMetaData
								.getRequiredManagedObjects();

						// Only take lock if have required managed objects
						if (managedObjectIndexes.length == 0) {
							// Only jump forward if initial state
							if (this.jobState == JobState.LOAD_MANAGED_OBJECTS) {
								// No managed objects required, so execute job
								this.jobState = JobState.EXECUTE_JOB;
							}

						} else {
							// Within process lock, ensure managed objects ready
							synchronized (processState.getProcessLock()) {
								boolean isJustLoaded = false;
								switch (this.jobState) {
								case LOAD_MANAGED_OBJECTS:
									// Load the managed objects
									boolean isAllLoaded = this.workContainer
											.loadManagedObjects(
													managedObjectIndexes,
													executionContext, this,
													notifySet);

									// Flag Managed Objects are being loaded
									this.jobState = JobState.ENSURE_MANAGED_OBJECTS_LOADED;

									// Determine if managed objects are loaded
									if (!isAllLoaded) {
										// Wakened up when loaded
										return true;
									}

									// Flag all just loaded
									isJustLoaded = true;

								case ENSURE_MANAGED_OBJECTS_LOADED:
									// Must check ready if not just loaded
									if (!isJustLoaded) {
										// Ensure managed objects are ready
										if (!this.workContainer
												.isManagedObjectsReady(
														managedObjectIndexes,
														executionContext, this,
														notifySet)) {
											// Wakened up when ready
											return true;
										}
									}

									// Coordinate the managed objects
									this.workContainer
											.coordinateManagedObjects(
													managedObjectIndexes,
													executionContext, this,
													notifySet);

									// Flag Managed Objects are coordinated
									this.jobState = JobState.EXECUTE_JOB;

								default:
									// Ensure managed objects are ready
									if (!this.workContainer
											.isManagedObjectsReady(
													managedObjectIndexes,
													executionContext, this,
													notifySet)) {
										// Wakened up when ready
										return true;
									}
								}
							}
						}

						switch (this.jobState) {
						case EXECUTE_JOB:
							// Flag complete by default and not waiting
							this.isComplete = true;
							this.flowAsset = null;

							// Execute the job
							this.nextJobParameter = this.executeJob(this);

							// Determine if wait on flow
							if (this.flowAsset != null) {
								// Wait on the flow
								waitOnFlowAsset = this.flowAsset;
								this.flowAsset = null;

								// Parallel job done before re-executing
								this.jobState = JobState.ACTIVATE_PARALLEL_JOB;
								return true;
							}

						case ACTIVATE_PARALLEL_JOB:
							// Activate only if job not completed
							if (!this.isComplete) {
								// Execute the job again
								this.jobState = JobState.EXECUTE_JOB;

								// Determine if parallel task to execute
								JobNode parallelJob = this
										.getParallelJobNodeToExecute();
								if (parallelJob != null) {
									// Execute the parallel job (on same thread)
									parallelJob.activateJob();
									if (this.isParallelJobsNotComplete()) {
										// Parallel job wakes up when complete
										return true;
									} else {
										// Parallel job completed, re-run this
										return false;
									}
								} else {
									// Job logic not complete
									return false;
								}
							}

							// Flag job completed
							this.jobState = JobState.ACTIVATE_NEXT_JOB_IN_FLOW;

						case ACTIVATE_NEXT_JOB_IN_FLOW:

							// Load next job if no sequential job invoked
							if (!this.isSequentialJobInvoked) {
								// No sequential node, load next task of flow
								TaskMetaData<?, ?, ?, ?> nextTaskMetaData = this.nodeMetaData
										.getNextTaskInFlow();
								if (nextTaskMetaData != null) {
									// Create next task
									JobNode job = this.flow.createJobNode(
											nextTaskMetaData,
											this.parallelOwner,
											this.nextJobParameter);

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
								parallelJob.activateJob();
								if (this.isParallelJobsNotComplete()) {
									// Parallel job wakes up when complete
									return true;
								}
							}

							// Assign next job to team (same thread)
							JobNode job = this.getNextJobNodeToExecute();
							if (job != null) {
								job.activateJob();
							}

							// Complete this job
							this.completeJob(notifySet);

						case COMPLETED:
							// Already complete, thus return immediately
							return true;

						case FAILED:
						case HANDLING_FAILURE:
							// Carry on to handle the failure
							break;

						default:
							throw new IllegalStateException(
									"Should not be in state " + this.jobState);
						}
					} catch (ExecutionError ex) {

						// Task failed
						this.jobState = JobState.FAILED;

						// Handle the execution error
						switch (ex.getErrorType()) {
						case MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT:
						case MANAGED_OBJECT_FAILED_PROVIDING_OBJECT:
						case MANAGED_OBJECT_NOT_LOADED:
						case MANAGED_OBJECT_SOURCING_FAILURE:
						default:
							// Flag for escalation
							escalationCause = ex.getCause();
							if (escalationCause == null) {
								escalationCause = ex;
							}
						}

						// TODO remove
					} catch (AssertionFailedError ex) {
						throw (AssertionFailedError) ex;

					} catch (Throwable ex) {

						// Task failed
						this.jobState = JobState.FAILED;

						// Flag for escalation
						escalationCause = ex;
					}

					try {
						// Handle completion/failure
						switch (this.jobState) {
						case FAILED:
							// Obtain the escalation to handle the failure
							Escalation escalation = this.nodeMetaData
									.getEscalationProcedure().getEscalation(
											escalationCause);

							// Use catch all escalation, if none provided
							if (escalation == null) {
								escalation = processState
										.getCatchAllEscalation();
							}

							// Start escalation (and ensure complete later)
							boolean isResetThreadState = escalation
									.isResetThreadState();
							threadState.escalationStart(this,
									isResetThreadState, notifySet);
							try {

								// Do the escalation
								FlowMetaData<?> escalationFlowMetaData = escalation
										.getFlowMetaData();
								if (isResetThreadState) {
									// Create and load the sequential flow
									this.createSequentialFlow(
											escalationFlowMetaData,
											escalationCause);

								} else {
									// Flag handling failure
									this.jobState = JobState.HANDLING_FAILURE;

									// Create, load and execute as parallel flow
									this.createParallelFlow(
											escalationFlowMetaData,
											escalationCause);
									JobNode parallelJob = this
											.getNextJobNodeToExecute();
									parallelJob.activateJob();
									if (this.isParallelJobsNotComplete()) {
										// Parallel job wakes up when complete
										return true;
									}
								}
							} finally {
								// Escalation complete
								threadState.escalationComplete(this, notifySet);
							}

						case HANDLING_FAILURE:
							// Assign the next task of flow to its team
							JobNode job = this.getNextJobNodeToExecute();
							if (job != null) {
								// Will be either same thread or new thread
								job.activateJob();
							}

						case COMPLETED:
							// Complete the task
							this.completeJob(notifySet);
							break;

						default:
							throw new IllegalStateException(
									"Should not be in state " + this.jobState);
						}

					} catch (Throwable ex) {
						// Should not receive failure here.
						// If so likely something has corrupted - eg OOM.
						System.err
								.println("FAILURE: please restart office floor as likely become corrupt");
						ex.printStackTrace();
					}

					// Now complete
					return true;

				} finally {
					// Job no longer active
					this.isActive = false;
				}
			}

		} finally {
			// Outside thread lock as may be interacting with other threads

			// Wait on flow
			if (waitOnFlowAsset != null) {
				waitOnFlowAsset.waitOnFlow(this, notifySet);
			}

			// Ensure activate jobs
			notifySet.activateJobs();
		}
	}

	/**
	 * Completes this {@link Job}.
	 * 
	 * @param notifySet
	 *            {@link JobActivateSet}.
	 */
	private void completeJob(JobActivateSet notifySet) {

		// Do nothing if already complete
		if (this.jobState == JobState.COMPLETED) {
			return;
		}

		// Complete the job
		this.jobState = JobState.COMPLETED;

		// Clean up state, may interact with process state
		ProcessState processState = this.flow.getThreadState()
				.getProcessState();
		synchronized (processState.getProcessLock()) {
			this.workContainer.unloadWork();
			this.flow.jobComplete(this, notifySet);
		}
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
	 * ======================= JobContext =================================
	 * 
	 * All methods will be guarded by lock taken in the doJob method.
	 * Furthermore the JobContext methods do not require synchronized
	 * co-ordination between themselves as executing a task is single threaded.
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.impl.execute.JobExecuteContext#setJobComplete(boolean
	 * )
	 */
	@Override
	public final void setJobComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.impl.execute.JobExecuteContext#joinFlow(net.officefloor
	 * .frame.api.execute.FlowFuture)
	 */
	@Override
	public final void joinFlow(FlowFuture flowFuture) {

		// TODO replace below to allow to join more than one flow
		if (this.flowAsset != null) {
			throw new IllegalStateException(
					"TODO implement joining more than one flow");
		}

		// Down cast flow future to its flow asset
		this.flowAsset = (FlowAsset) flowFuture;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.impl.execute.JobExecuteContext#doFlow(net.officefloor
	 * .frame.internal.structure.FlowMetaData, java.lang.Object)
	 */
	@Override
	public final FlowFuture doFlow(FlowMetaData<?> flowMetaData,
			Object parameter) {
		// Instigate the flow
		switch (flowMetaData.getInstigationStrategy()) {
		case ASYNCHRONOUS:
			return this.createAsynchronousFlow(flowMetaData, parameter);

		case PARALLEL:
			return this.createParallelFlow(flowMetaData, parameter);

		case SEQUENTIAL:
			// Flag sequential task invoked
			this.isSequentialJobInvoked = true;
			return this.createSequentialFlow(flowMetaData, parameter);

		default:
			// Unknown instigation strategy
			throw new IllegalStateException("Unknown instigation strategy");
		}
	}

	/**
	 * Creates an asynchronous {@link Flow} from the input {@link FlowMetaData}.
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
		TaskMetaData<?, ?, ?, ?> initTaskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create thread to execute asynchronously
		ProcessState processState = this.flow.getThreadState()
				.getProcessState();
		Flow asyncFlow = processState.createThread(flowMetaData);

		// Create job node for execution
		JobNode asyncJobNode = asyncFlow.createJobNode(initTaskMetaData, null,
				parameter);

		// Asynchronously instigate the job node
		asyncJobNode.activateJob();

		// Specify the thread flow future
		return asyncFlow.getThreadState();
	}

	/**
	 * Creates a parallel {@link Flow} from the input {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @return Parallel {@link Flow}.
	 */
	private FlowFuture createParallelFlow(FlowMetaData<?> flowMetaData,
			Object parameter) {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?, ?> initTaskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		Flow parallelFlow = threadState.createFlow(flowMetaData);

		// Create the job node
		JobNode parallelJobNode = parallelFlow.createJobNode(initTaskMetaData,
				this, parameter);

		// Load the parallel node
		this.loadParallelJobNode(parallelJobNode);

		// Return the flow future
		return parallelFlow;
	}

	/**
	 * Creates a sequential {@link Flow} from the input {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @return Sequential {@link Flow}.
	 */
	private FlowFuture createSequentialFlow(FlowMetaData<?> flowMetaData,
			Object parameter) {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?, ?> initTaskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Create the job node on the same flow as this job node
		JobNode sequentialJobNode = this.flow.createJobNode(initTaskMetaData,
				this.parallelOwner, parameter);

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
	 * This is the {@link JobNode} that is executed once the {@link Flow} that
	 * this {@link JobNode} is involved within is complete.
	 */
	private JobNode parallelOwner;

	/**
	 * Parallel {@link JobNode} that must be complete before this
	 * {@link JobNode} may complete.
	 */
	private JobNode parallelNode;

	/**
	 * Next {@link JobNode} in the sequential listing of {@link Flow}.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#activeTask()
	 */
	@Override
	public final void activateJob() {

		// Access Point: TaskContainer, ManagedObjectSource/Pool, ProjectManager
		// Locks: None (possibly on another ThreadState)

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

			// Flag that queued
			this.isQueuedWithTeam = true;

			// Activate this Task
			this.nodeMetaData.getTeam().assignJob(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#isJobNodeComplete()
	 */
	@Override
	public boolean isJobNodeComplete() {
		// Complete if in complete state
		return (this.jobState == JobState.COMPLETED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.JobNode#getFlow()
	 */
	@Override
	public Flow getFlow() {
		return this.flow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskNode#setParallelOwner(net
	 * .officefloor.frame.internal.structure.TaskNode)
	 */
	@Override
	public final void setParallelOwner(JobNode jobNode) {
		this.parallelOwner = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getParallelOwner()
	 */
	@Override
	public final JobNode getParallelOwner() {
		return this.parallelOwner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskNode#setParallelNode(net
	 * .officefloor.frame.internal.structure.TaskNode)
	 */
	@Override
	public final void setParallelNode(JobNode jobNode) {
		this.parallelNode = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getParallelNode()
	 */
	@Override
	public final JobNode getParallelNode() {
		return this.parallelNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskNode#setNextNode(net.officefloor
	 * .frame.internal.structure.TaskNode)
	 */
	@Override
	public final void setNextNode(JobNode jobNode) {
		this.nextTaskNode = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getNextNode()
	 */
	@Override
	public final JobNode getNextNode() {
		return this.nextTaskNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.TaskNode#clearNodes(net.officefloor
	 * .frame.internal.structure.AssetNotifySet)
	 */
	@Override
	public final void clearNodes(JobActivateSet notifySet) {

		// Complete this task
		this.completeJob(notifySet);

		// Unset the linked nodes
		JobNode parallel = this.getParallelNode();
		this.setParallelNode(null);
		JobNode owner = this.getParallelOwner();
		this.setParallelOwner(null);
		JobNode sequential = this.getNextNode();
		this.setNextNode(null);

		// Clear the linked nodes
		if (parallel != null) {
			parallel.clearNodes(notifySet);
		}
		if (owner != null) {
			owner.clearNodes(notifySet);
		}
		if (sequential != null) {
			sequential.clearNodes(notifySet);
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
		 * Indicates to check that all required {@link ManagedObject} instances
		 * are loaded.
		 */
		ENSURE_MANAGED_OBJECTS_LOADED,

		/**
		 * Indicates the {@link Job} is to be executed.
		 */
		EXECUTE_JOB,

		/**
		 * Indicates to activate the parallel {@link Job}.
		 */
		ACTIVATE_PARALLEL_JOB,

		/**
		 * Indicates to activate the next {@link Task} in the {@link Flow}.
		 */
		ACTIVATE_NEXT_JOB_IN_FLOW,

		/**
		 * Failure in executing.
		 */
		FAILED,

		/**
		 * Currently handling the failure.
		 */
		HANDLING_FAILURE,

		/**
		 * {@link TaskContainer} has completed.
		 */
		COMPLETED,

	}

}
