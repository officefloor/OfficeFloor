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

import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.AssetManagerImpl;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.Team;

import org.easymock.internal.AlwaysMatcher;

/**
 * Used by the
 * {@link net.officefloor.frame.impl.execute.AbstractTaskNodeTestCase} to
 * initiate an execution path.
 * 
 * @author Daniel
 */
public class ExecutionNode<W extends Work> implements
		TaskMetaData<Object, W, Indexed, Indexed>,
		TaskFactory<Object, W, Indexed, Indexed>,
		Task<Object, W, Indexed, Indexed>, EscalationProcedure {

	/**
	 * Test case utilising this {@link ExecutionNode} to test execution of a
	 * {@link Task} tree.
	 */
	protected final AbstractTaskNodeTestCase testCase;

	/**
	 * Listing of the {@link TaskProcessItem}.
	 */
	private final List<TaskProcessItem> taskProcessing = new LinkedList<TaskProcessItem>();

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
	private TaskMetaData nextTask = null;

	/**
	 * Initiate.
	 * 
	 * @param executionNodeId
	 *            Unique Id of this {@link ExecutionNode}.
	 * @param testCase
	 *            {@link AbstractTaskNodeTestCase}.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 */
	public ExecutionNode(int executionNodeId,
			AbstractTaskNodeTestCase testCase, WorkMetaData<W> workMetaData) {
		this.executionNodeId = executionNodeId;
		this.testCase = testCase;
		this.workMetaData = workMetaData;
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
	 * @param workMoIndex
	 *            Index of the {@link ManagedObject} within the {@link Work}.
	 * @param objectOfManagedObject
	 *            Object to be managed by the {@link ManagedObject}.
	 * @param processer
	 *            {@link ManagedObjectProcesser} to process the
	 *            {@link ManagedObject}.
	 */
	public <O extends Object> void processManagedObject(int workMoIndex,
			O objectOfManagedObject, ManagedObjectProcesser<O> processer) {

		ManagedObjectSource source;
		final ManagedObject mo;
		boolean isAsynchronous;

		// Obtain the source and create the returned Managed Object
		switch (workMoIndex) {
		case AbstractTaskNodeTestCase.PROCESS_MO_INDEX:
			source = this.testCase.getProcessManagedObjectSource();
			mo = this.testCase.createMock(AsynchronousManagedObject.class);
			isAsynchronous = true;
			break;

		case AbstractTaskNodeTestCase.WORK_MO_INDEX:
			source = this.testCase.getWorkManagedObjectSource();
			mo = this.testCase.createMock(ManagedObject.class);
			isAsynchronous = false;
			break;

		default:
			throw new IllegalStateException("Unknown managed object index "
					+ workMoIndex);
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

		// Record obtaining the object of the Managed Object
		try {
			mo.getObject();
			this.testCase.control(mo).setReturnValue(objectOfManagedObject);
		} catch (Exception ex) {
			Assert.fail("Recording - should not fail: " + ex.getMessage());
		}

		// If asynchronous must be registered with container
		if (isAsynchronous) {
			// Register with container
			AsynchronousManagedObject amo = (AsynchronousManagedObject) mo;
			amo.registerAsynchronousCompletionListener(null);
			this.testCase.control(mo).setDefaultMatcher(new AlwaysMatcher());
		}

		// Add to listing of processing for the Task
		this.taskProcessing.add(new ManagedObjectTaskProcessItem<W, O>(
				workMoIndex, isAsynchronous, processer));
	}

	/**
	 * Set {@link TaskMetaData} of the next {@link Task}.
	 * 
	 * @param nextTask
	 *            {@link TaskMetaData} of the next {@link Task}.
	 */
	void setNextTask(TaskMetaData nextTask) {
		this.nextTask = nextTask;
	}

	/**
	 * Add a {@link net.officefloor.frame.internal.structure.Flow} to be
	 * executed.
	 * 
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param initialTask
	 *            {@link TaskMetaData} of the initial {@link Task}.
	 */
	void addFlow(FlowInstigationStrategyEnum instigationStrategy,
			TaskMetaData initialTask) {
		// Add to listing of processing for the Task
		this.taskProcessing.add(new FlowTaskProcessItem<W>(instigationStrategy,
				initialTask, this.testCase));
	}

	/**
	 * Adds a {@link ExecutionNode} to join.
	 * 
	 * @param futureNode
	 *            {@link ExecutionNode} to join.
	 */
	void addJoin(ExecutionNode futureNode) {
		// Add to listing of processing for the Task
		this.taskProcessing.add(new JoinTaskProcessItem<W>(futureNode,
				this.testCase));
	}

	/*
	 * ====================================================================
	 * TaskMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getTaskFactory()
	 */
	public TaskFactory<Object, W, Indexed, Indexed> getTaskFactory() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getTeam()
	 */
	public Team getTeam() {
		return this.testCase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getRequiredManagedObjects()
	 */
	public int[] getRequiredManagedObjects() {
		// Create the listing of required managed objects
		List<Integer> moListing = new LinkedList<Integer>();
		int i = 0;
		for (TaskProcessItem<W> item : this.taskProcessing) {
			if (item instanceof ManagedObjectTaskProcessItem) {
				ManagedObjectTaskProcessItem moItem = (ManagedObjectTaskProcessItem) item;
				moListing.add(moItem.getWorkManagedObjectIndex());
			}
			i++;
		}

		// Create the required managed objects
		int[] requiredMo = new int[moListing.size()];
		i = 0;
		for (Integer integer : moListing) {
			requiredMo[i++] = integer.intValue();
		}

		// Return the required managed objects
		return requiredMo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getCheckManagedObjects()
	 */
	public int[] getCheckManagedObjects() {
		// Create the listing of checked managed objects
		List<Integer> moListing = new LinkedList<Integer>();
		int i = 0;
		for (TaskProcessItem<W> item : this.taskProcessing) {
			if (item instanceof ManagedObjectTaskProcessItem) {
				ManagedObjectTaskProcessItem moItem = (ManagedObjectTaskProcessItem) item;

				// Only add if asynchronous
				if (moItem.isAsynchronous()) {
					moListing.add(moItem.getWorkManagedObjectIndex());
				}
			}
			i++;
		}

		// Create the check managed objects
		int[] checkMo = new int[moListing.size()];
		i = 0;
		for (Integer integer : moListing) {
			checkMo[i++] = integer.intValue();
		}

		// Return the check managed objects
		return checkMo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#translateManagedObjectIndexForWork(int)
	 */
	public int translateManagedObjectIndexForWork(int taskMoIndex) {
		return ((ManagedObjectTaskProcessItem) this.taskProcessing
				.get(taskMoIndex)).getWorkManagedObjectIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getFlow(int)
	 */
	public FlowMetaData<?> getFlow(int flowIndex) {
		return (FlowMetaData) this.taskProcessing.get(flowIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getWorkMetaData()
	 */
	public WorkMetaData<W> getWorkMetaData() {
		return this.workMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getEscalationProcedure()
	 */
	public EscalationProcedure getEscalationProcedure() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getNextTaskInFlow()
	 */
	public TaskMetaData<?, ?, ?, ?> getNextTaskInFlow() {
		return this.nextTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getPreAdministrationMetaData()
	 */
	public TaskDutyAssociation<?>[] getPreAdministrationMetaData() {
		return new TaskDutyAssociation[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.TaskMetaData#getPostAdministrationMetaData()
	 */
	public TaskDutyAssociation<?>[] getPostAdministrationMetaData() {
		return new TaskDutyAssociation[0];
	}

	/*
	 * ========================================================================
	 * TaskFactory
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(W)
	 */
	public Task<Object, W, Indexed, Indexed> createTask(W work) {

		// Flag this as latest execution task node
		this.testCase.setLatestTaskNode(this);

		// Return this as Task
		return this;
	}

	/*
	 * ========================================================================
	 * Task
	 * ========================================================================
	 */

	/**
	 * Indicates if this {@link Task} has had the {@link TaskProcessItem}
	 * instances run.
	 */
	protected boolean isTaskProcessed = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Task#doTask(net.officefloor.frame.api.execute.TaskContext)
	 */
	public Object doTask(TaskContext<Object, W, Indexed, Indexed> context)
			throws Exception {

		// Flag that this task is being executed
		this.testCase.addExecutedNode(this);

		// Process items only on first execution
		if (!this.isTaskProcessed) {

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

	/*
	 * ====================================================================
	 * EscalationProcedure
	 * ====================================================================
	 */

	/**
	 * Index to tie stack trace to failure.
	 */
	private static int escalationIndex = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.EscalationProcedure#escalate(java.lang.Throwable,
	 *      net.officefloor.frame.internal.structure.ThreadState)
	 */
	public void escalate(Throwable cause, ThreadState threadState) {
		// Determine if escalate Assertion failure
		if (cause instanceof AssertionError) {
			throw (AssertionError) cause;
		} else if (cause instanceof AssertionFailedError) {
			throw (AssertionFailedError) cause;
		} else if (cause != null) {

			// Output stack trace to find location of failure
			System.err.println("Escalated failure");
			cause.printStackTrace();

			// Output failure
			String msg = "Escalated failure (" + (escalationIndex++) + "): "
					+ cause.getMessage() + " ["
					+ cause.getClass().getSimpleName() + "]";
			System.err.println(msg);
			cause.printStackTrace(System.err);

			// Propagate failure
			Assert.fail(msg);

		} else {
			Assert.fail("Null escalation failure");
		}
	}

}

/**
 * Contract for a process item of a
 * {@link net.officefloor.frame.api.execute.Task}.
 */
interface TaskProcessItem<W extends Work> {

	/**
	 * Process.
	 * 
	 * @param itemIndex
	 *            Index of this item.
	 * @param context
	 *            {@link TaskContext} in which to process.
	 * @return Flag indicating whether the {@link Task} should be re-executed.
	 */
	boolean process(int itemIndex,
			TaskContext<Object, W, Indexed, Indexed> context);

}

/**
 * {@link TaskProcessItem} to process a
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
 */
class ManagedObjectTaskProcessItem<W extends Work, O extends Object> implements
		TaskProcessItem<W> {

	/**
	 * Index of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	protected final int workMoIndex;

	/**
	 * Specifies whether the {@link ManagedObject} is
	 * {@link net.officefloor.frame.spi.managedobject.AsynchronousManagedObject}.
	 * 
	 * @return Whether the {@link ManagedObject} is
	 *         {@link net.officefloor.frame.spi.managedobject.AsynchronousManagedObject}.
	 */
	protected final boolean isAsynchronous;

	/**
	 * {@link ManagedObjectProcesser}.
	 */
	protected final ManagedObjectProcesser<O> processer;

	/**
	 * Initiate.
	 * 
	 * @param workMoIndex
	 *            Index of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @param isAsynchronous
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link net.officefloor.frame.spi.managedobject.AsynchronousManagedObject}.
	 * @param processer
	 *            {@link ManagedObjectProcesser} for the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	public ManagedObjectTaskProcessItem(int workMoIndex,
			boolean isAsynchronous, ManagedObjectProcesser<O> processer) {
		this.workMoIndex = workMoIndex;
		this.isAsynchronous = isAsynchronous;
		this.processer = processer;
	}

	/**
	 * Obtains the index of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} to be
	 * processed for the {@link Work}.
	 * 
	 * @return Index of the
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject} to
	 *         be processed for the {@link Work}.
	 */
	public int getWorkManagedObjectIndex() {
		return this.workMoIndex;
	}

	/**
	 * Obtains whether the {@link ManagedObject} is
	 * {@link net.officefloor.frame.spi.managedobject.AsynchronousManagedObject}.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} is
	 *         {@link net.officefloor.frame.spi.managedobject.AsynchronousManagedObject}.
	 */
	public boolean isAsynchronous() {
		return this.isAsynchronous;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.execute.impl.TaskProcessItem#process(net.officefloor.frame.api.execute.TaskContext)
	 */
	@SuppressWarnings("unchecked")
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
 * {@link TaskProcessItem} to instigate a
 * {@link net.officefloor.frame.internal.structure.Flow}.
 */
class FlowTaskProcessItem<W extends Work> implements TaskProcessItem<W>,
		FlowMetaData {

	/**
	 * {@link FlowInstigationStrategyEnum}.
	 */
	protected final FlowInstigationStrategyEnum instigationStrategy;

	/**
	 * {@link TaskMetaData}.
	 */
	protected final TaskMetaData taskMetaData;

	/**
	 * {@link AbstractTaskNodeTestCase}.
	 */
	protected final AbstractTaskNodeTestCase testCase;

	/**
	 * {@link AssetManager} to managed the
	 * {@link net.officefloor.frame.internal.structure.Flow}.
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
	public FlowTaskProcessItem(FlowInstigationStrategyEnum instigationStrategy,
			TaskMetaData taskMetaData, AbstractTaskNodeTestCase testCase) {
		this.instigationStrategy = instigationStrategy;
		this.taskMetaData = taskMetaData;
		this.testCase = testCase;

		// Create the flow manager (only if asynchronous)
		if (this.instigationStrategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
			this.flowManager = new AssetManagerImpl();
		} else {
			// No need for flow management
			this.flowManager = null;
		}
	}

	/*
	 * ====================================================================
	 * TaskProcessItem
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.execute.impl.TaskProcessItem#process(int,
	 *      net.officefloor.frame.api.execute.TaskContext)
	 */
	public boolean process(int itemIndex,
			TaskContext<Object, W, Indexed, Indexed> context) {
		// Do the particular flow
		FlowFuture future = context.doFlow(itemIndex, null);

		// Obtain the execution node just created for flow
		ExecutionNode flowNode = this.testCase.getLatestTaskNode();

		// Register the flow future
		this.testCase.registerFlowFuture(flowNode, future);

		// Only complete if not parallel flow instigated
		return (this.instigationStrategy != FlowInstigationStrategyEnum.PARALLEL);
	}

	/*
	 * ====================================================================
	 * FlowMetaData
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowMetaData#getInstigationStrategy()
	 */
	public FlowInstigationStrategyEnum getInstigationStrategy() {
		return this.instigationStrategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowMetaData#getInitialTaskMetaData()
	 */
	public TaskMetaData getInitialTaskMetaData() {
		return this.taskMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowMetaData#getFlowManager()
	 */
	public AssetManager getFlowManager() {
		return this.flowManager;
	}

}

/**
 * {@link TaskProcessItem} to join a
 * {@link net.officefloor.frame.api.execute.FlowFuture}.
 */
class JoinTaskProcessItem<W extends Work> implements TaskProcessItem<W> {

	/**
	 * {@link ExecutionNode} for completion.
	 */
	protected final ExecutionNode futureNode;

	/**
	 * {@link AbstractTaskNodeTestCase}.
	 */
	protected final AbstractTaskNodeTestCase testCase;

	/**
	 * Initiate.
	 * 
	 * @param futureNode
	 *            {@link ExecutionNode} for completion.
	 * @param testCase
	 *            {@link AbstractTaskNodeTestCase}.
	 */
	public JoinTaskProcessItem(ExecutionNode futureNode,
			AbstractTaskNodeTestCase testCase) {
		this.futureNode = futureNode;
		this.testCase = testCase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.execute.impl.TaskProcessItem#process(int,
	 *      net.officefloor.frame.api.execute.TaskContext)
	 */
	public boolean process(int itemIndex,
			TaskContext<Object, W, Indexed, Indexed> context) {

		// Obtain the flow future for the completing node
		FlowFuture future = this.testCase.getFlowFuture(this.futureNode);

		// Wait on flow future
		context.join(future);

		// Not complete (execute again)
		return false;
	}

}