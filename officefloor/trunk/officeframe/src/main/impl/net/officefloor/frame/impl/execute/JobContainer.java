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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowAsset;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
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
public abstract class JobContainer<W extends Work, N extends JobMetaData>
		implements Job, JobNode, JobExecuteContext {

	/**
	 * {@link ThreadState}.
	 */
	protected final ThreadState threadState;

	/**
	 * {@link Flow}.
	 */
	protected final Flow flow;

	/**
	 * {@link ThreadWorkLink} to access the {@link WorkContainer}.
	 */
	protected final ThreadWorkLink<W> workLink;

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
	 * @param threadState
	 *            {@link ThreadState} for executing this {@link Task}.
	 * @param flow
	 *            {@link Flow} that this {@link Task} resides within.
	 * @param workLink
	 *            {@link ThreadWorkLink} to access the {@link WorkContainer} of
	 *            the {@link Work} for the {@link Task}.
	 * @param nodeMetaData
	 *            {@link JobMetaData} for this node.
	 * @param parallelOwner
	 *            If this is invoked as or a parallel {@link Task} or from a
	 *            parallel {@link Task} this will be the invokee. If not
	 *            parallel then will be <code>null</code>.
	 */
	public JobContainer(ThreadState threadState, Flow flow,
			ThreadWorkLink<W> workLink, N nodeMetaData, JobNode parallelOwner) {
		this.threadState = threadState;
		this.flow = flow;
		this.workLink = workLink;
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
	 * ========================================================================
	 * Job
	 * ========================================================================
	 */

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
	public final void activateJob() {

		// Access Point: TaskContainer, ManagedObjectSource/Pool, ProjectManager
		// Locks: None (possibly on another ThreadState)

		// Lock to ensure only one activation
		synchronized (this.threadState.getThreadLock()) {

			// Determine if already queued, active or complete
			if (this.isQueuedWithTeam || this.isActive
					|| (this.jobState == JobState.COMPLETED)) {
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
	 * @see net.officefloor.frame.spi.team.TaskContainer#getThreadState()
	 */
	public final ThreadState getThreadState() {
		return this.threadState;
	}

	/**
	 * Next {@link Job} that is managed by the {@link Team}.
	 */
	private Job nextJob = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#setNextTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public final void setNextJob(Job task) {
		this.nextJob = task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getNextTask()
	 */
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
	 * @see net.officefloor.frame.spi.team.TaskContainer#doTask(net.officefloor.frame.spi.team.ExecutionContext)
	 */
	public final boolean doJob(JobContext executionContext) {

		// Access Point: Team
		// Locks: None

		// Ensure notify and wait on flow
		JobActivateSetImpl notifySet = new JobActivateSetImpl();
		FlowAsset waitOnFlowAsset = null;
		try {

			// Only one job per thread at a time
			synchronized (this.threadState.getThreadLock()) {

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
						escalationCause = this.threadState.getFailure();
						if (escalationCause != null) {
							// Clear failure on the thread, as escalating
							this.threadState.setFailure(null);

							// Escalate the failure on the thread
							throw escalationCause;
						}

						// Obtain the required managed object indexes
						int[] managedObjectIndexes = this.nodeMetaData
								.getRequiredManagedObjects();

						// Within process lock, ensure managed objects ready
						synchronized (this.threadState.getProcessState()
								.getProcessLock()) {
							switch (this.jobState) {
							case LOAD_MANAGED_OBJECTS:
								// Load the managed objects
								boolean isAllLoaded = this.workLink
										.getWorkContainer().loadManagedObjects(
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

							case ENSURE_MANAGED_OBJECTS_LOADED:
								// Ensure managed objects are ready
								if (!this.workLink.getWorkContainer()
										.isManagedObjectsReady(
												managedObjectIndexes,
												executionContext, this,
												notifySet)) {
									// Wakened up when ready
									return true;
								}

								// Coordinate the managed objects
								this.workLink.getWorkContainer()
										.coordinateManagedObjects(
												managedObjectIndexes,
												executionContext, this,
												notifySet);

								// Flag Managed Objects are coordinated
								this.jobState = JobState.EXECUTE_JOB;

							default:
								// Ensure managed objects are ready
								if (!this.workLink.getWorkContainer()
										.isManagedObjectsReady(
												managedObjectIndexes,
												executionContext, this,
												notifySet)) {
									// Wakened up when ready
									return true;
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
								Job parallelTask = this
										.getParallelJobNodeToExecute();
								if (parallelTask != null) {
									// Execute the parallel job (on same thread)
									parallelTask.activateJob();
									if (this
											.isParallelJobNotComplete(parallelTask)) {
										// Job task is complete (for now)
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
									Job job = this.flow.createJob(
											nextTaskMetaData,
											this.parallelOwner,
											this.nextJobParameter,
											this.workLink);

									// Load for sequential execution
									this.loadSequentialJobNode((JobNode) job);
								}

								// Sequential job now invoked
								this.isSequentialJobInvoked = true;
							}

							// Determine if parallel task to execute
							Job parallelTask = this
									.getParallelJobNodeToExecute();
							if (parallelTask != null) {
								// Execute the parallel job (on same thread)
								parallelTask.activateJob();
								if (this.isParallelJobNotComplete(parallelTask)) {
									// Job is complete (for now)
									return true;
								}
							}

							// Assign next job to team (same thread)
							Job job = this.getNextJobNodeToExecute();
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
								ProcessState processState = this.threadState
										.getProcessState();
								escalation = processState.getCatchAllEscalation();
							}

							try {
								// Start escalation
								this.threadState.escalationStart(this,
										escalation.isResetThreadState(),
										notifySet);

								// Do the escalation
								if (escalation.isResetThreadState()) {
									// Create and load the sequential flow
									this
											.createSequentialFlow(escalation
													.getFlowMetaData(),
													escalationCause);

								} else {
									// Flag handling failure
									this.jobState = JobState.HANDLING_FAILURE;

									// Create, load and execute as parallel flow
									this
											.createParallelFlow(escalation
													.getFlowMetaData(),
													escalationCause);
									Job parallelTask = this
											.getNextJobNodeToExecute();
									parallelTask.activateJob();
									if (this
											.isParallelJobNotComplete(parallelTask)) {
										// Will be reactivated after handling
										return true;
									}
								}
							} finally {
								// Escalation complete
								this.threadState.escalationComplete(this,
										notifySet);
							}

						case HANDLING_FAILURE:
							// Assign the next task of flow to its team
							Job taskContainer = this.getNextJobNodeToExecute();
							if (taskContainer != null) {
								// Same thread, same lock to active so ok
								// Different thread, should be creating it so ok
								taskContainer.activateJob();
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
		this.flow.jobComplete(this, notifySet);
		this.workLink.unregisterJob(this);
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
	 * @return <code>true</code> if the {@link JobNode} is not complete and
	 *         this {@link JobNode} should release the
	 *         {@link ThreadState#getThreadLock()} lock to allow it to complete.
	 */
	private boolean isParallelJobNotComplete(Job parallelJob) {

		// Downcast to implementation
		JobContainer<?, ?> impl = (JobContainer<?, ?>) parallelJob;

		// Determine if input task not is complete
		if (impl.jobState != JobState.COMPLETED) {
			// Not complete
			return true;
		}

		// Also must check sequential and parallel nodes. Parallel node may
		// create these in its process and potentially either passively complete
		// them or hand them off to another team.
		boolean isNotComplete = false;
		Job sequentialNode = (Job) impl.getNextNode();
		if (sequentialNode != null) {
			isNotComplete |= this.isParallelJobNotComplete(sequentialNode);
		}
		if (!isNotComplete) {
			Job parallelParallelNode = (Job) impl.getParallelNode();
			if (parallelParallelNode != null) {
				isNotComplete |= this
						.isParallelJobNotComplete(parallelParallelNode);
			}
		}

		// Return if is not complete
		return isNotComplete;
	}

	/**
	 * Obtains the parallel {@link Job} to execute.
	 * 
	 * @return Parallel {@link Job} to execute.
	 */
	private Job getParallelJobNodeToExecute() {

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
			return (Job) currentTask;
		}
	}

	/**
	 * Obtains the next {@link Job} to execute.
	 * 
	 * @return Next {@link Job} to execute.
	 */
	private Job getNextJobNodeToExecute() {

		// Determine if have parallel node
		Job nextTaskContainer = this.getParallelJobNodeToExecute();
		if (nextTaskContainer != null) {
			// Parallel node
			return nextTaskContainer;
		}

		// Determine if have sequential node
		JobNode nextTask = this.getNextNode();
		if (nextTask != null) {
			// Sequential node
			return (Job) nextTask;
		}

		// Determine if have parallel owner
		nextTask = this.getParallelOwner();
		if (nextTask != null) {
			// Returning to owner, therefore unlink parallel node
			nextTask.setParallelNode(null);

			// Parallel owner
			return (Job) nextTask;
		}

		// No further tasks
		return null;
	}

	/*
	 * ====================================================================
	 * JobContext
	 * 
	 * All methods will be guarded by lock taken in the doJob method.
	 * Furthermore the JobContext methods do not require synchronized
	 * co-ordination between themselves as executing a task is single threaded.
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.NodeContext#setTaskComplete(boolean)
	 */
	public final void setJobComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.NodeContext#joinFlow(net.officefloor.frame.api.execute.FlowFuture)
	 */
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
	 * @see net.officefloor.frame.internal.structure.NodeContext#doFlow(net.officefloor.frame.internal.structure.FlowMetaData,
	 *      java.lang.Object)
	 */
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
		Flow asyncFlow = this.threadState.getProcessState().createThread(
				flowMetaData);

		// Create task for execution
		Job asyncTask = asyncFlow.createJob(initTaskMetaData, null, parameter,
				this.workLink);

		// Asynchronously instigate the task
		asyncTask.activateJob();

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

		// Create a flow for execution
		Flow parallelFlow = this.threadState.createFlow(flowMetaData);

		// Create the Task Container
		Job parallelTaskContainer = parallelFlow.createJob(initTaskMetaData,
				this, parameter, this.workLink);

		// Load the parallel node
		this.loadParallelJobNode((JobNode) parallelTaskContainer);

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

		// Create the Task Container
		Job sequentialTaskContainer = this.flow.createJob(initTaskMetaData,
				this.parallelOwner, parameter, this.workLink);

		// Load the sequential node
		this.loadSequentialJobNode((JobNode) sequentialTaskContainer);

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
	 * ====================================================================
	 * JobNode
	 * ====================================================================
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#setParallelOwner(net.officefloor.frame.internal.structure.TaskNode)
	 */
	public final void setParallelOwner(JobNode jobNode) {
		this.parallelOwner = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getParallelOwner()
	 */
	public final JobNode getParallelOwner() {
		return this.parallelOwner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#setParallelNode(net.officefloor.frame.internal.structure.TaskNode)
	 */
	public final void setParallelNode(JobNode jobNode) {
		this.parallelNode = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getParallelNode()
	 */
	public final JobNode getParallelNode() {
		return this.parallelNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#setNextNode(net.officefloor.frame.internal.structure.TaskNode)
	 */
	public final void setNextNode(JobNode jobNode) {
		this.nextTaskNode = jobNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getNextNode()
	 */
	public final JobNode getNextNode() {
		return this.nextTaskNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#clearNodes(net.officefloor.frame.internal.structure.AssetNotifySet)
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
	private enum JobState {

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
