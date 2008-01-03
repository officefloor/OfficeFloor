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

import junit.framework.AssertionFailedError;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowAsset;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TaskNode;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadWorkLink;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;
import net.officefloor.frame.spi.team.Team;

/**
 * Implementation of the {@link net.officefloor.frame.spi.team.TaskContainer}.
 * 
 * @author Daniel
 */
public class TaskContainerImpl<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>, I extends Enum<I>>
		implements TaskContext<P, W, M, F>, TaskContainer, TaskNode,
		AdministratorContext {

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
	 * Meta-data of the {@link net.officefloor.frame.api.execute.Task} being
	 * managed.
	 */
	protected final TaskMetaData<P, W, M, F> taskMetaData;

	/**
	 * {@link Task} to be done.
	 */
	protected final Task<P, W, M, F> task;

	/**
	 * Parameter of the current {@link net.officefloor.frame.api.execute.Task}
	 * being managed.
	 */
	protected final P parameter;

	/**
	 * State of this {@link TaskContainer}.
	 */
	protected TaskContainerState containerState = TaskContainerState.LOAD_MANAGED_OBJECTS;

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
	 * @param taskMetaData
	 *            Meta-data of the {@link Task}.
	 * @param parameter
	 *            Parameter passed from the doXXXFlow method of the
	 *            {@link TaskContext}.
	 * @param parallelOwner
	 *            If this is invoked as or a parallel {@link Task} or from a
	 *            parallel {@link Task} this will be the invokee. If not
	 *            parallel then will be <code>null</code>.
	 */
	public TaskContainerImpl(ThreadState threadState, Flow flow,
			ThreadWorkLink<W> workLink, TaskMetaData<P, W, M, F> taskMetaData,
			P parameter, TaskNode parallelOwner) {

		// Store state
		this.threadState = threadState;
		this.flow = flow;
		this.workLink = workLink;
		this.taskMetaData = taskMetaData;
		this.parameter = parameter;
		this.parallelOwner = parallelOwner;

		// Obtain the task
		this.task = this.taskMetaData.getTaskFactory().createTask(
				this.workLink.getWorkContainer().getWork(this.threadState));
	}

	/*
	 * ====================================================================
	 * TaskContainer
	 * ====================================================================
	 */

	/**
	 * Flag indicating if this {@link TaskContainer} has been assigned to a
	 * {@link Team} to be executed.
	 */
	protected boolean isQueuedWithTeam = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#activeTask()
	 */
	public void activateTask() {

		// Access Point: TaskContainer, ManagedObjectSource/Pool, ProjectManager
		// Locks: None (possibly on another ThreadState)

		// Lock to ensure only one activation
		synchronized (this.threadState.getThreadLock()) {

			// Determine if already queued or complete
			if (this.isQueuedWithTeam
					|| (this.containerState == TaskContainerState.COMPLETED)) {
				return;
			}

			// Activate this Task
			this.taskMetaData.getTeam().assignTask(this);

			// Flag that queued
			this.isQueuedWithTeam = true;
		}
	}

	/**
	 * <p>
	 * Flag indicating if the {@link Task} is complete. This indicates only when
	 * the {@link Task} is not yet complete.
	 * </p>
	 */
	protected boolean isComplete;

	/**
	 * {@link FlowAsset} to wait on.
	 */
	protected FlowAsset flowAsset = null;

	/**
	 * Parameter for the next {@link Task}.
	 */
	private Object nextTaskParameter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#doTask(net.officefloor.frame.spi.team.ExecutionContext)
	 */
	public boolean doTask(ExecutionContext executionContext) {

		// Access Point: Team
		// Locks: None

		// This should only be executed by one thread at a time
		synchronized (this.threadState.getThreadLock()) {

			// Flag no longer queued with team as now executing
			this.isQueuedWithTeam = false;

			// Escalation cause
			Throwable escalationCause = null;
			try {
				// Handle failure on thread
				// (possibly from waiting for a managed object)
				escalationCause = this.threadState.getFailure();
				if (escalationCause != null) {
					throw escalationCause;
				}

				// Obtain the managed object indexes required/to check
				int[] managedObjectIndexes;
				switch (this.containerState) {
				case LOAD_MANAGED_OBJECTS:
				case ENSURE_MANAGED_OBJECTS_LOADED:
					// Obtain required managed objects
					managedObjectIndexes = this.taskMetaData
							.getRequiredManagedObjects();
					break;
				default:
					// Obtain the check managed objects
					managedObjectIndexes = this.taskMetaData
							.getCheckManagedObjects();
					break;
				}

				// Process based on state of container
				switch (this.containerState) {
				case LOAD_MANAGED_OBJECTS:

					// Load the managed objects
					boolean isAllLoaded = this.workLink.getWorkContainer()
							.loadManagedObjects(managedObjectIndexes,
									executionContext, this);

					// Flag Managed Objects are being loaded
					this.containerState = TaskContainerState.ENSURE_MANAGED_OBJECTS_LOADED;

					// Determine if required managed objects are loaded
					if (!isAllLoaded) {
						// Will be woken when loaded
						return true;
					}

				case ENSURE_MANAGED_OBJECTS_LOADED:

					// Determine if all the managed objects are loaded (/ready)
					if (!this.workLink.getWorkContainer()
							.isManagedObjectsReady(managedObjectIndexes,
									executionContext, this)) {
						// Will be woken when ready
						return true;
					}

					// Coordinate the managed objects
					this.workLink.getWorkContainer().coordinateManagedObjects(
							managedObjectIndexes, executionContext, this);

					// Flag Managed Objects are loaded
					this.containerState = TaskContainerState.DO_PRE_TASK_ADMINISTRATION;

				case DO_PRE_TASK_ADMINISTRATION:

					// Determine if managed objects are ready
					if (!this.workLink.getWorkContainer()
							.isManagedObjectsReady(managedObjectIndexes,
									executionContext, this)) {
						// Will be woken when ready
						return true;
					}

					// Undertake the pre task administration
					for (TaskDutyAssociation<?> duty : this.taskMetaData
							.getPreAdministrationMetaData()) {
						this.workLink.getWorkContainer()
								.administerManagedObjects(duty, this);
					}

					// Flag pre task administration complete
					this.containerState = TaskContainerState.DO_TASK;

					// Determine if parallel task from administration
					TaskContainer preAdminParallelTask = this
							.getParallelTaskNodeToExecute();
					if (preAdminParallelTask != null) {
						// Execute the parallel task
						preAdminParallelTask.activateTask();

						// This task is complete (for now)
						return true;
					}

				case DO_TASK:

					// Determine if managed objects are ready
					if (!this.workLink.getWorkContainer()
							.isManagedObjectsReady(managedObjectIndexes,
									executionContext, this)) {
						// Will be woken when ready
						return true;
					}

					// Flag task to be defaultly complete and not waiting
					this.isComplete = true;
					this.flowAsset = null;

					// Execute the task
					this.nextTaskParameter = this.task.doTask(this);

					// Determine if to wait on flow
					if (this.flowAsset != null) {
						if (this.flowAsset.waitOnFlow(this)) {
							// Waiting on the flow
							return true;
						}
					}

					// Indicate if task completed
					if (!this.isComplete) {

						// Determine if parallel task to execute
						TaskContainer parallelTask = this
								.getParallelTaskNodeToExecute();
						if (parallelTask != null) {
							// Execute the parallel task
							parallelTask.activateTask();

							// This task is complete (for now)
							return true;
						} else {
							// No parallel task but this task logic not complete
							return false;
						}
					}

					// Flag task completed
					this.containerState = TaskContainerState.DO_POST_TASK_ADMINISTRATION;

				case DO_POST_TASK_ADMINISTRATION:

					// Determine if managed objects are ready
					if (!this.workLink.getWorkContainer()
							.isManagedObjectsReady(managedObjectIndexes,
									executionContext, this)) {
						// Will be woken when ready
						return true;
					}

					// Undertake the post task administration
					for (TaskDutyAssociation<?> duty : this.taskMetaData
							.getPostAdministrationMetaData()) {
						this.workLink.getWorkContainer()
								.administerManagedObjects(duty, this);
					}

					// Flag post task administration complete
					this.containerState = TaskContainerState.ACTIVATE_NEXT_TASK_IN_FLOW;

					// Determine if parallel task from administration
					TaskContainer postAdminParallelTask = this
							.getParallelTaskNodeToExecute();
					if (postAdminParallelTask != null) {
						// Execute the parallel task
						postAdminParallelTask.activateTask();

						// This task is complete (for now)
						return true;
					}

				case ACTIVATE_NEXT_TASK_IN_FLOW:

					// Possibly load the next task in the flow
					if (this.getNextNode() == null) {
						// No sequential node, therefore load next task in flow
						TaskMetaData<?, ?, ?, ?> nextTaskMetaData = this.taskMetaData
								.getNextTaskInFlow();
						if (nextTaskMetaData != null) {
							// Create next task
							TaskContainer taskContainer = this.flow
									.createTaskContainer(nextTaskMetaData,
											this.parallelOwner,
											this.nextTaskParameter,
											this.workLink);

							// Load for sequential execution
							this
									.loadSequentialTaskNode((TaskNode) taskContainer);
						}
					}

					// Assign the next task of flow to its team
					TaskContainer taskContainer = this
							.getNextTaskNodeToExecute();
					if (taskContainer != null) {
						taskContainer.activateTask();
					}

					// Flag this task complete
					this.containerState = TaskContainerState.COMPLETED;

					// Do completion work (only once)
					break;

				case COMPLETED:
					// Already complete, thus return immediately
					return true;
				}

			} catch (ExecutionError ex) {

				// Failure immediately completes this task
				this.containerState = TaskContainerState.COMPLETED;

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

				// Failure immediately completes this task
				this.containerState = TaskContainerState.COMPLETED;

				// Flag for escalation
				escalationCause = ex;
			}

			// Handle escalation
			try {
				if (escalationCause == null) {
					// No escalation, task complete
					this.flow.taskContainerComplete(this);
					this.workLink.unregisterTask(this);

				} else {
					try {
						// Start escalation
						this.threadState.escalationStart(this, this.flow);

						// Escalate
						this.taskMetaData.getEscalationProcedure().escalate(
								escalationCause, this.threadState);

						// Assign the next task of flow to its team
						TaskContainer taskContainer = this
								.getNextTaskNodeToExecute();
						if (taskContainer != null) {
							// Same thread, same lock to active so ok
							// Different thread, should be creating it so ok
							taskContainer.activateTask();
						}

					} finally {
						// Escalation complete
						this.threadState.escalationComplete(this, this.flow);
					}
				}
			} catch (Throwable ex) {

				// TODO: remove handle unittest building
				// (replace with top level escalation)
				if (ex instanceof AssertionError) {
					throw (AssertionError) ex;
				} else if (ex instanceof AssertionFailedError) {
					throw (AssertionFailedError) ex;
				} else {
					System.err.print("TaskContainerImpl failure: ");
					ex.printStackTrace();
					System.err
							.println("TODO: implement handling TaskContainer failures");
				}
			}

			// Now complete
			return true;
		}
	}

	/**
	 * Obtains the parallel {@link TaskContainer} to execute.
	 * 
	 * @return Parallel {@link TaskContainer} to execute.
	 */
	protected TaskContainer getParallelTaskNodeToExecute() {

		// Determine furthest parallel node
		TaskNode currentTask = this;
		TaskNode nextTask = null;
		while ((nextTask = currentTask.getParallelNode()) != null) {
			currentTask = nextTask;
		}

		// Determine if a parallel task
		if (currentTask == this) {
			// No parallel task
			return null;
		} else {
			// Return the furthest parallel task
			return (TaskContainer) currentTask;
		}
	}

	/**
	 * Obtains the next {@link TaskContainer} to execute.
	 * 
	 * @return Next {@link TaskContainer} to execute.
	 */
	protected TaskContainer getNextTaskNodeToExecute() {

		// Determine if have parallel node
		TaskContainer nextTaskContainer = this.getParallelTaskNodeToExecute();
		if (nextTaskContainer != null) {
			// Parallel node
			return nextTaskContainer;
		}

		// Determine if have sequential node
		TaskNode nextTask = this.getNextNode();
		if (nextTask != null) {
			// Sequential node
			return (TaskContainer) nextTask;
		}

		// Determine if have parallel owner
		nextTask = this.getParallelOwner();
		if (nextTask != null) {
			// Returning to owner, therefore unlink parallel node
			nextTask.setParallelNode(null);

			// Parallel owner
			return (TaskContainer) nextTask;
		}

		// No further tasks
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getThreadState()
	 */
	public ThreadState getThreadState() {
		return this.threadState;
	}

	/**
	 * <p>
	 * Next {@link TaskContainer} that is managed by the {@link Team}.
	 * </p>
	 */
	protected TaskContainer nextTaskContainer = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#setNextTask(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public void setNextTask(TaskContainer task) {
		this.nextTaskContainer = task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TaskContainer#getNextTask()
	 */
	public TaskContainer getNextTask() {
		return this.nextTaskContainer;
	}

	/*
	 * ====================================================================
	 * TaskContext
	 * 
	 * All methods of the TaskContext will be guarded by lock taken in the
	 * doTask method. Furthermore the TaskContext methods do not require
	 * synchronized co-ordination between themselves as executing a task is
	 * single threaded.
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see TaskContext#getWork()
	 */
	public W getWork() {
		return this.workLink.getWorkContainer().getWork(this.threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TaskContext#getParameter()
	 */
	public P getParameter() {
		return this.parameter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getObject(M)
	 */
	public Object getObject(M key) {
		return this.getObject(key.ordinal());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getObject(int)
	 */
	public Object getObject(int managedObjectIndex) {
		// Obtain the work managed object index
		int workMoIndex = this.taskMetaData
				.translateManagedObjectIndexForWork(managedObjectIndex);

		// Return the Object
		return this.workLink.getWorkContainer().getObject(workMoIndex,
				this.threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#getProcessLock()
	 */
	public Object getProcessLock() {
		return this.threadState.getProcessState().getProcessLock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.TaskContext#join(net.officefloor.frame.api.execute.FlowFuture)
	 */
	public void join(FlowFuture flowFuture) {

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
	 * @see net.officefloor.frame.api.execute.FlowInstigator#doFlow(F,
	 *      java.lang.Object)
	 */
	public FlowFuture doFlow(F key, Object parameter) {
		return this.doFlow(key.ordinal(), parameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.FlowInstigator#doFlow(int,
	 *      java.lang.Object)
	 */
	public FlowFuture doFlow(int flowIndex, Object parameter) {

		// Obtain the Flow meta-data
		FlowMetaData<?> flowMetaData = this.taskMetaData.getFlow(flowIndex);

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?, ?> initTaskMetaData = flowMetaData
				.getInitialTaskMetaData();

		// Instigate the flow
		switch (flowMetaData.getInstigationStrategy()) {
		case ASYNCHRONOUS:
			// Create thread to execute asynchronously
			Flow asyncFlow = this.threadState.getProcessState().createThread(
					flowMetaData);

			// Create task for execution
			TaskContainer asyncTask = asyncFlow.createTaskContainer(
					initTaskMetaData, null, parameter, this.workLink);

			// Asynchronously instigate the task
			asyncTask.activateTask();

			// Specify the thread flow future
			return asyncFlow.getThreadState();

		case PARALLEL:
			// Create a flow for execution
			Flow parallelFlow = this.threadState.createFlow(flowMetaData);

			// Create the Task Container
			TaskContainer parallelTaskContainer = parallelFlow
					.createTaskContainer(initTaskMetaData, this, parameter,
							this.workLink);

			// Load the parallel node
			this.loadParallelTaskNode((TaskNode) parallelTaskContainer);

			// Return the flow future
			return parallelFlow;

		case SEQUENTIAL:
			// Create the Task Container
			TaskContainer sequentialTaskContainer = this.flow
					.createTaskContainer(initTaskMetaData, this.parallelOwner,
							parameter, this.workLink);

			// Load the sequential node
			this.loadSequentialTaskNode((TaskNode) sequentialTaskContainer);

			// Return the flow future
			return this.flow;

		default:
			// Unknown instigation strategy
			throw new IllegalStateException("Unknown instigation strategy");
		}
	}

	/**
	 * Loads a sequential {@link TaskNode} relative to this {@link TaskNode}
	 * within the tree of {@link TaskNode} instances.
	 * 
	 * @param sequentialTaskNode
	 *            {@link TaskNode} to load to tree.
	 */
	protected void loadSequentialTaskNode(TaskNode sequentialTaskNode) {

		// Obtain the next sequential node
		TaskNode nextNode = this.getNextNode();
		if (nextNode != null) {

			// Move current sequential node to parallel node
			this.loadParallelTaskNode(nextNode);
		}

		// Set next sequential node
		this.setNextNode(sequentialTaskNode);
	}

	/**
	 * Loads a parallel {@link TaskNode} relative to this {@link TaskNode}
	 * within the tree of {@link TaskNode} instances.
	 * 
	 * @param parallelTaskNode
	 *            {@link TaskNode} to load to tree.
	 */
	protected void loadParallelTaskNode(TaskNode parallelTaskNode) {

		// Obtain the next parallel node
		TaskNode nextNode = this.getParallelNode();
		if (nextNode != null) {

			// Move next parallel node out
			parallelTaskNode.setParallelNode(nextNode);
			nextNode.setParallelOwner(parallelTaskNode);
		}

		// Set next parallel node
		this.setParallelNode(parallelTaskNode);
		parallelTaskNode.setParallelOwner(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see TaskContext#setComplete(boolean)
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/*
	 * ====================================================================
	 * TaskNode
	 * ====================================================================
	 */

	/**
	 * <p>
	 * Owner if this {@link TaskNode} is a parallel {@link TaskNode}.
	 * <p>
	 * This is the {@link TaskNode} that is executed once the {@link Flow} that
	 * this {@link TaskNode} is involved within is complete.
	 */
	protected TaskNode parallelOwner;

	/**
	 * Parallel {@link TaskNode} that must be complete before this
	 * {@link TaskNode} may complete.
	 */
	protected TaskNode parallelNode;

	/**
	 * Next {@link TaskNode} in the sequential listing of {@link Flow}.
	 */
	protected TaskNode nextTaskNode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#setParallelOwner(net.officefloor.frame.internal.structure.TaskNode)
	 */
	public void setParallelOwner(TaskNode taskNode) {
		this.parallelOwner = taskNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getParallelOwner()
	 */
	public TaskNode getParallelOwner() {
		return this.parallelOwner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#setParallelNode(net.officefloor.frame.internal.structure.TaskNode)
	 */
	public void setParallelNode(TaskNode taskNode) {
		this.parallelNode = taskNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getParallelNode()
	 */
	public TaskNode getParallelNode() {
		return this.parallelNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#setNextNode(net.officefloor.frame.internal.structure.TaskNode)
	 */
	public void setNextNode(TaskNode taskNode) {
		this.nextTaskNode = taskNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskNode#getNextNode()
	 */
	public TaskNode getNextNode() {
		return this.nextTaskNode;
	}

	/*
	 * ====================================================================
	 * AdministratorContext
	 * 
	 * All methods of the AdministratorContext will be guarded by lock taken in
	 * the doTask method. Furthermore the AdministratorContext methods do not
	 * require synchronized co-ordination between themselves as executing a task
	 * and its administration is single threaded.
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorContext#doFlow(net.officefloor.frame.internal.structure.FlowMetaData,
	 *      java.lang.Object)
	 */
	public void doFlow(FlowMetaData<?> flowMetaData, Object parameter) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

	/**
	 * State of this {@link net.officefloor.frame.spi.team.TaskContainer}.
	 */
	private enum TaskContainerState {

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
		 * Indicates the pre {@link Task} administration is to be done.
		 */
		DO_PRE_TASK_ADMINISTRATION,

		/**
		 * Indicates the {@link Task} is to be done.
		 */
		DO_TASK,

		/**
		 * Indicates the post {@link Task} administration is to be done.
		 */
		DO_POST_TASK_ADMINISTRATION,

		/**
		 * Indicates to activate the next {@link Task} in the {@link Flow.
		 */
		ACTIVATE_NEXT_TASK_IN_FLOW,

		/**
		 * {@link TaskContainer} has completed.
		 */
		COMPLETED
	}

}
