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
package net.officefloor.frame.integrate.jobnode;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import junit.framework.TestCase;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.impl.execute.task.TaskJob;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

import org.easymock.internal.AlwaysMatcher;

/**
 * Used by the {@link AbstractTaskNodeTestCase} to initiate an execution path.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionNode<W extends Work> implements
		TaskMetaData<W, Indexed, Indexed>, TaskFactory<W, Indexed, Indexed>,
		Task<W, Indexed, Indexed> {

	/**
	 * Test case utilising this {@link ExecutionNode} to test execution of a
	 * {@link Task} tree.
	 */
	protected final AbstractTaskNodeTestCase<W> testCase;

	/**
	 * Responsible {@link TeamManagement} for this {@link Job}.
	 */
	private final TeamManagement responsibleTeam;

	/**
	 * Continue {@link Team}.
	 */
	private final Team continueTeam;

	/**
	 * {@link Team} expected to execute this {@link Task}.
	 */
	private final ExecutionTeam expectedExecutionTeam;

	/**
	 * Listing of the {@link TaskProcessItem}.
	 */
	private final List<TaskProcessItem<W>> taskProcessing = new LinkedList<TaskProcessItem<W>>();

	/**
	 * {@link WorkMetaData}.
	 */
	private final WorkMetaData<W> workMetaData;

	/**
	 * Unique Id of this {@link ExecutionNode}.
	 */
	private final int executionNodeId;

	/**
	 * Value to return from {@link Task#doTask(TaskContext)}.
	 */
	private Object taskReturnValue;

	/**
	 * {@link TaskMetaData} of the next {@link Task}.
	 */
	private TaskMetaData<?, ?, ?> nextTask = null;

	/**
	 * Initiate.
	 * 
	 * @param executionNodeId
	 *            Unique Id of this {@link ExecutionNode}.
	 * @param testCase
	 *            {@link AbstractTaskNodeTestCase}.
	 * @param responsibleTeam
	 *            Responsible {@link TeamManagement}.
	 * @param continueTeam
	 *            Continue {@link Team}.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @param expectedExecutionTeam
	 *            {@link Team} expected to execute this {@link Task}.
	 */
	public ExecutionNode(int executionNodeId,
			AbstractTaskNodeTestCase<W> testCase,
			TeamManagement responsibleTeam, Team continueTeam,
			WorkMetaData<W> workMetaData, ExecutionTeam expectedExecutionTeam) {
		this.executionNodeId = executionNodeId;
		this.testCase = testCase;
		this.responsibleTeam = responsibleTeam;
		this.continueTeam = continueTeam;
		this.workMetaData = workMetaData;
		this.expectedExecutionTeam = expectedExecutionTeam;
	}

	/**
	 * Obtains the unique Id for this {@link ExecutionNode}.
	 * 
	 * @return Unique Id for this {@link ExecutionNode}.
	 */
	public int getExecutionNodeId() {
		return this.executionNodeId;
	}

	/**
	 * Indicates if this {@link ExecutionNode} was executed.
	 * 
	 * @return <code>true</code> if this {@link ExecutionNode} was executed,
	 *         <code>false</code> otherwise.
	 */
	public boolean isExecuted() {
		return this.testCase.isExecuted(this);
	}

	/**
	 * Link in {@link ManagedObject} to be processed.
	 * 
	 * @param moScope
	 *            {@link ManagedObjectScope} for the {@link ManagedObject}.
	 * @param objectOfManagedObject
	 *            Object to be managed by the {@link ManagedObject}.
	 * @param processer
	 *            {@link ManagedObjectProcesser} to process the
	 *            {@link ManagedObject}.
	 */
	public <O> void processManagedObject(ManagedObjectScope moScope,
			O objectOfManagedObject, ManagedObjectProcesser<O> processer) {

		ManagedObjectSource<?, ?> source;
		final ManagedObject mo;
		boolean isAsynchronous;

		// Obtain the source and create the returned Managed Object
		switch (moScope) {
		case PROCESS:
			source = this.testCase.getProcessManagedObjectSource();
			mo = this.testCase.createMock(AsynchronousManagedObject.class);
			isAsynchronous = true;
			break;

		case THREAD:
			source = this.testCase.getThreadManagedObjectSource();
			mo = this.testCase.createMock(ManagedObject.class);
			isAsynchronous = false;
			break;

		case WORK:
			source = this.testCase.getWorkManagedObjectSource();
			mo = this.testCase.createMock(ManagedObject.class);
			isAsynchronous = false;
			break;

		default:
			throw new IllegalStateException("Unknown managed object scope "
					+ moScope);
		}

		// Record obtaining the Managed Object
		source.sourceManagedObject(null);
		this.testCase.control(source).setDefaultMatcher(new AlwaysMatcher() {
			public boolean matches(Object[] expected, Object[] actual) {
				// Obtain the ManagedObjectUser
				ManagedObjectUser user = (ManagedObjectUser) actual[0];

				// Return the Managed Object
				user.setManagedObject(mo);

				// Matching
				return true;
			}
		});

		// If asynchronous must be registered with container
		if (isAsynchronous) {
			// Register with container
			AsynchronousManagedObject amo = (AsynchronousManagedObject) mo;
			amo.registerAsynchronousCompletionListener(null);
			this.testCase.control(mo).setDefaultMatcher(new AlwaysMatcher());
		}

		// Record obtaining the object of the Managed Object
		try {
			mo.getObject();
			this.testCase.control(mo).setReturnValue(objectOfManagedObject);
		} catch (Throwable ex) {
			Assert.fail("Recording - should not fail: " + ex.getMessage());
		}

		// Add to listing of processing for the Task
		this.taskProcessing.add(new ManagedObjectTaskProcessItem<O>(
				new ManagedObjectIndexImpl(moScope, 0), processer));
	}

	/**
	 * Set {@link TaskMetaData} of the next {@link Task}.
	 * 
	 * @param nextTask
	 *            {@link TaskMetaData} of the next {@link Task}.
	 */
	void setNextTask(TaskMetaData<W, ?, ?> nextTask) {
		this.nextTask = nextTask;
	}

	/**
	 * Add a {@link JobSequence} to be executed.
	 * 
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param initialTask
	 *            {@link TaskMetaData} of the initial {@link Task}.
	 */
	void addFlow(FlowInstigationStrategyEnum instigationStrategy,
			TaskMetaData<W, ?, ?> initialTask) {
		// Add to listing of processing for the Task
		this.taskProcessing.add(new FlowTaskProcessItem(instigationStrategy,
				initialTask, this.testCase));
	}

	/**
	 * Adds a {@link ExecutionNode} to join.
	 * 
	 * @param futureNode
	 *            {@link ExecutionNode} to join.
	 */
	void addJoin(ExecutionNode<W> futureNode) {
		// Add to listing of processing for the Task
		this.taskProcessing.add(new JoinTaskProcessItem(futureNode,
				this.testCase));
	}

	/*
	 * ===================== TaskMetaData =================================
	 */

	@Override
	public String getJobName() {
		return String.valueOf(this.executionNodeId);
	}

	@Override
	public String getTaskName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public JobNodeActivatableSet createJobActivableSet() {
		return new JobNodeActivatableSetImpl();
	}

	@Override
	public TaskFactory<W, Indexed, Indexed> getTaskFactory() {
		return this;
	}

	@Override
	public Object getDifferentiator() {
		return null; // no differentiator
	}

	@Override
	public Class<?> getParameterType() {
		return Object.class;
	}

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.responsibleTeam;
	}

	@Override
	public Team getContinueTeam() {
		return this.continueTeam;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ManagedObjectIndex[] getRequiredManagedObjects() {
		// Create the listing of required managed objects
		List<ManagedObjectIndex> moListing = new LinkedList<ManagedObjectIndex>();
		for (TaskProcessItem<W> item : this.taskProcessing) {
			if (item instanceof ExecutionNode<?>.ManagedObjectTaskProcessItem<?>) {
				ManagedObjectTaskProcessItem<?> moItem = (ManagedObjectTaskProcessItem<?>) item;
				moListing.add(moItem.getManagedObjectIndex());
			}
		}

		// Return the required managed objects
		return moListing.toArray(new ManagedObjectIndex[0]);
	}

	@Override
	public boolean[] getRequiredGovernance() {
		return null; // no governance
	}

	@Override
	@SuppressWarnings("unchecked")
	public ManagedObjectIndex translateManagedObjectIndexForWork(int taskMoIndex) {
		return ((ManagedObjectTaskProcessItem<?>) this.taskProcessing
				.get(taskMoIndex)).getManagedObjectIndex();
	}

	@Override
	public FlowMetaData<?> getFlow(int flowIndex) {
		return (FlowMetaData<?>) this.taskProcessing.get(flowIndex);
	}

	@Override
	public WorkMetaData<W> getWorkMetaData() {
		return this.workMetaData;
	}

	@Override
	public EscalationProcedure getEscalationProcedure() {
		return new EscalationProcedureImpl();
	}

	@Override
	public TaskMetaData<?, ?, ?> getNextTaskInFlow() {
		return this.nextTask;
	}

	@Override
	public TaskDutyAssociation<?>[] getPreAdministrationMetaData() {
		return new TaskDutyAssociation[0];
	}

	@Override
	public TaskDutyAssociation<?>[] getPostAdministrationMetaData() {
		return new TaskDutyAssociation[0];
	}

	@Override
	public JobNode createTaskNode(JobSequence flow,
			WorkContainer<W> workContainer, JobNode parallelJobNodeOwner,
			Object parameter,
			GovernanceDeactivationStrategy governanceDeactivationStrategy) {
		return new TaskJob<W, Indexed, Indexed>(flow, workContainer, this,
				governanceDeactivationStrategy, parallelJobNodeOwner, parameter);
	}

	/*
	 * ===================== TaskFactory ======================================
	 */

	@Override
	public Task<W, Indexed, Indexed> createTask(W work) {

		// Flag this as latest execution task node
		this.testCase.setLatestTaskNode(this);

		// Return this as Task
		return this;
	}

	/*
	 * ====================== Task ============================================
	 */

	/**
	 * Indicates if this {@link Task} has had the {@link TaskProcessItem}
	 * instances run.
	 */
	protected boolean isTaskProcessed = false;

	@Override
	public Object doTask(TaskContext<W, Indexed, Indexed> context)
			throws Exception {

		// Flag that this task is being executed
		this.testCase.addExecutedNode(this);

		// Process items only on first execution
		if (!this.isTaskProcessed) {

			// Ensure being executed by the correct team
			ExecutionTeam executingTeam = ExecutionTeam.threadTeam.get();
			TestCase.assertEquals("Incorrect execution team",
					this.expectedExecutionTeam, executingTeam);

			// Execute the task item processors
			boolean isComplete = true;
			int i = 0;
			for (TaskProcessItem<W> item : this.taskProcessing) {
				isComplete &= item.process(i++, context);
			}

			// Flag whether task is complete
			context.setComplete(isComplete);

			// Task processed
			this.isTaskProcessed = true;
		}

		// Return value
		return this.taskReturnValue;
	}

	/**
	 * Contract for a process item of a {@link Task}.
	 */
	private interface TaskProcessItem<W extends Work> {

		/**
		 * Process.
		 * 
		 * @param itemIndex
		 *            Index of this item.
		 * @param context
		 *            {@link TaskContext} in which to process.
		 * @return Flag indicating whether the {@link Task} should be
		 *         re-executed.
		 */
		boolean process(int itemIndex, TaskContext<W, Indexed, Indexed> context);

	}

	/**
	 * {@link TaskProcessItem} to process a {@link ManagedObject}.
	 */
	private class ManagedObjectTaskProcessItem<O> implements TaskProcessItem<W> {

		/**
		 * {@link ManagedObjectIndex}.
		 */
		protected final ManagedObjectIndex moIndex;

		/**
		 * {@link ManagedObjectProcesser}.
		 */
		protected final ManagedObjectProcesser<O> processer;

		/**
		 * Initiate.
		 * 
		 * @param moIndex
		 *            {@link ManagedObjectIndex}.
		 * @param processer
		 *            {@link ManagedObjectProcesser} for the
		 *            {@link ManagedObject}.
		 */
		public ManagedObjectTaskProcessItem(ManagedObjectIndex moIndex,
				ManagedObjectProcesser<O> processer) {
			this.moIndex = moIndex;
			this.processer = processer;
		}

		/**
		 * Obtains the {@link ManagedObjectIndex} of the {@link ManagedObject}.
		 * 
		 * @return {@link ManagedObjectIndex} of the {@link ManagedObject}.
		 */
		public ManagedObjectIndex getManagedObjectIndex() {
			return this.moIndex;
		}

		/*
		 * ==================== TaskProcessItem ==========================
		 */

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public boolean process(int itemIndex, TaskContext context) {
			// Obtain the object of the Managed Object
			O object = (O) context.getObject(itemIndex);

			// Process the item
			this.processer.process(object);

			// Processing complete
			return true;
		}

	}

	/**
	 * {@link TaskProcessItem} to instigate a {@link JobSequence}.
	 */
	private class FlowTaskProcessItem implements TaskProcessItem<W>,
			FlowMetaData<W> {

		/**
		 * {@link FlowInstigationStrategyEnum}.
		 */
		protected final FlowInstigationStrategyEnum instigationStrategy;

		/**
		 * {@link TaskMetaData}.
		 */
		protected final TaskMetaData<W, ?, ?> taskMetaData;

		/**
		 * {@link AbstractTaskNodeTestCase}.
		 */
		protected final AbstractTaskNodeTestCase<W> testCase;

		/**
		 * {@link AssetManager} to managed the {@link JobSequence}.
		 */
		protected final AssetManager flowManager;

		/**
		 * Initiate.
		 * 
		 * @param instigationStrategy
		 *            {@link FlowInstigationStrategyEnum}.
		 * @param taskMetaData
		 *            {@link TaskMetaData}.
		 * @param testCase
		 *            {@link AbstractTaskNodeTestCase}.
		 */
		public FlowTaskProcessItem(
				FlowInstigationStrategyEnum instigationStrategy,
				TaskMetaData<W, ?, ?> taskMetaData,
				AbstractTaskNodeTestCase<W> testCase) {
			this.instigationStrategy = instigationStrategy;
			this.taskMetaData = taskMetaData;
			this.testCase = testCase;

			// Create the flow manager (only if asynchronous)
			if (this.instigationStrategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
				this.flowManager = new AssetManagerImpl(null);
			} else {
				// No need for flow management
				this.flowManager = null;
			}
		}

		/*
		 * =================== TaskProcessItem ===============================
		 */

		@Override
		public boolean process(int itemIndex,
				TaskContext<W, Indexed, Indexed> context) {
			// Do the particular flow
			FlowFuture future = context.doFlow(itemIndex, null);

			// Obtain the execution node just created for flow
			ExecutionNode<?> flowNode = this.testCase.getLatestTaskNode();

			// Register the flow future
			this.testCase.registerFlowFuture(flowNode, future);

			// Only complete if not parallel flow instigated
			return (this.instigationStrategy != FlowInstigationStrategyEnum.PARALLEL);
		}

		/*
		 * ==================== FlowMetaData ===============================
		 */

		@Override
		public FlowInstigationStrategyEnum getInstigationStrategy() {
			return this.instigationStrategy;
		}

		@Override
		public TaskMetaData<W, ?, ?> getInitialTaskMetaData() {
			return this.taskMetaData;
		}

		@Override
		public AssetManager getFlowManager() {
			return this.flowManager;
		}

	}

	/**
	 * {@link TaskProcessItem} to join a {@link FlowFuture}.
	 */
	private class JoinTaskProcessItem implements TaskProcessItem<W> {

		/**
		 * {@link ExecutionNode} for completion.
		 */
		protected final ExecutionNode<W> futureNode;

		/**
		 * {@link AbstractTaskNodeTestCase}.
		 */
		protected final AbstractTaskNodeTestCase<W> testCase;

		/**
		 * Initiate.
		 * 
		 * @param futureNode
		 *            {@link ExecutionNode} for completion.
		 * @param testCase
		 *            {@link AbstractTaskNodeTestCase}.
		 */
		public JoinTaskProcessItem(ExecutionNode<W> futureNode,
				AbstractTaskNodeTestCase<W> testCase) {
			this.futureNode = futureNode;
			this.testCase = testCase;
		}

		/*
		 * ================= TaskProcessItem ==================================
		 */

		@Override
		public boolean process(int itemIndex,
				TaskContext<W, Indexed, Indexed> context) {

			// Obtain the flow future for the completing node
			FlowFuture future = this.testCase.getFlowFuture(this.futureNode);

			// Wait on flow future
			context.join(future, 1000, null);

			// Not complete (execute again)
			return false;
		}
	}

}