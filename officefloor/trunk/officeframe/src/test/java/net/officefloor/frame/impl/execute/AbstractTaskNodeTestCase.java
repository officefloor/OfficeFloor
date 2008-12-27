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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract functionality to test execution of
 * {@link net.officefloor.frame.impl.execute.TaskContainerImpl}.
 * 
 * @author Daniel
 */
public abstract class AbstractTaskNodeTestCase<W extends Work> extends
		OfficeFrameTestCase implements Team {

	/**
	 * Index of the {@link ProcessState}
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} on the
	 * {@link Work}.
	 */
	public static final int PROCESS_MO_INDEX = 0;

	/**
	 * Index of the {@link Work}
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} on the
	 * {@link Work}.
	 */
	public static final int WORK_MO_INDEX = 1;

	/**
	 * Initial {@link ExecutionNode}.
	 */
	private ExecutionNode<W> initialNode;

	/**
	 * {@link AssetManager} for managing the sourcing of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} of the
	 * {@link ProcessState}.
	 */
	private AssetManager processSourcingManager;

	/**
	 * {@link AssetManager} for managing the asynchronous operations on the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} of the
	 * {@link ProcessState}.
	 */
	private AssetManager processOperationsManager;

	/**
	 * {@link ManagedObjectSource} for the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} of the
	 * {@link ProcessState}.
	 */
	private ManagedObjectSource<?, ?> processMoSource;

	/**
	 * {@link AssetManager} for managing the sourcing of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} of the
	 * {@link Work}.
	 */
	private AssetManager workSourcingManager;

	/**
	 * {@link AssetManager} for managing the asynchronous operations on the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} of the
	 * {@link Work}.
	 */
	private AssetManager workOperationsManager;

	/**
	 * {@link ManagedObjectSource} for the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} of the
	 * {@link Work}.
	 */
	private ManagedObjectSource<?, ?> workMoSource;

	/**
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} meta-data
	 * for the {@link ProcessState}.
	 */
	private ManagedObjectMetaDataImpl<?> moMetaData;

	/**
	 * Initiate the Test.
	 */
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {

		// TODO: consider testing the recycle tasks

		// Create the Asset Managers for the Managed Objects
		this.processSourcingManager = new AssetManagerImpl();
		this.processOperationsManager = new AssetManagerImpl();
		this.workSourcingManager = new AssetManagerImpl();
		this.workOperationsManager = new AssetManagerImpl();

		// Create the managed object pools
		ManagedObjectPool processMoPool = null;
		ManagedObjectPool workMoPool = null;

		// Create the mock objects
		this.processMoSource = this.createMock(ManagedObjectSource.class);
		this.workMoSource = this.createMock(ManagedObjectSource.class);

		// Create the Managed Object meta-data for the Process
		this.moMetaData = new ManagedObjectMetaDataImpl(this.processMoSource,
				processMoPool, this.processSourcingManager, true,
				this.processOperationsManager, false, null, 1000);
		this.moMetaData.loadRemainingState(null, null);

		// Create the Work Managed Object meta-data
		ManagedObjectMetaDataImpl workMo = new ManagedObjectMetaDataImpl(
				this.workMoSource, workMoPool, this.workSourcingManager, false,
				this.workOperationsManager, false, null, 1000);
		workMo.loadRemainingState(null, null);
		ManagedObjectMetaData[] workMoMetaData = new ManagedObjectMetaData[] {
				new ManagedObjectMetaDataImpl(0), workMo };

		// Create the Work
		final Work work = new Work() {
		};

		// Create the Work Factory
		WorkFactory workFactory = new WorkFactory() {
			public Work createWork() {
				return work;
			}
		};

		// Create the Flow Manager for the initial flow
		AssetManager flowManager = new AssetManagerImpl();

		// Create the Initial Flow
		FlowMetaData initialFlowMetaData = new FlowMetaDataImpl(
				FlowInstigationStrategyEnum.ASYNCHRONOUS,
				this.getInitialNode(), flowManager);

		// TODO consider testing with administrator meta-data
		AdministratorMetaData[] adminMetaData = new AdministratorMetaData[0];

		// Create the Work meta-data
		WorkMetaData workMetaData = new WorkMetaDataImpl(1, workFactory,
				workMoMetaData, adminMetaData, initialFlowMetaData);

		// Initial node
		this.initialNode = new ExecutionNode(this.nextExecutionNodeId(), this,
				workMetaData);
	}

	/**
	 * Obtains the initial {@link ExecutionNode}.
	 * 
	 * @return Initial {@link ExecutionNode}.
	 */
	protected ExecutionNode<W> getInitialNode() {
		return this.initialNode;
	}

	/**
	 * Obtains the {@link ProcessState} bound {@link ManagedObjectSource}.
	 * 
	 * @return {@link ProcessState} bound {@link ManagedObjectSource}.
	 */
	protected ManagedObjectSource<?, ?> getProcessManagedObjectSource() {
		return this.processMoSource;
	}

	/**
	 * Obtains the {@link Work} bound {@link ManagedObjectSource}.
	 * 
	 * @return {@link Work} bound {@link ManagedObjectSource}.
	 */
	protected ManagedObjectSource<?, ?> getWorkManagedObjectSource() {
		return this.workMoSource;
	}

	/**
	 * Binds the next {@link ExecutionNode} to be executed after the input
	 * {@link ExecutionNode}.
	 * 
	 * @param currentNode
	 *            {@link ExecutionNode}.
	 * @return Next {@link ExecutionNode} to be executed after the input
	 *         {@link ExecutionNode}.
	 */
	protected ExecutionNode<?> bindNextNode(ExecutionNode<W> currentNode) {

		// Ensure next node not already bound
		if (currentNode.getNextTaskInFlow() != null) {
			fail("Next node already bound");
		}

		// Create the next execution node
		ExecutionNode<W> nextNode = new ExecutionNode<W>(this
				.nextExecutionNodeId(), this, currentNode.getWorkMetaData());

		// Bind as next execution node
		currentNode.setNextTask(nextNode);

		// Return the next node
		return nextNode;
	}

	/**
	 * Binds a {@link ExecutionNode} to be sequentially executed after the input
	 * {@link ExecutionNode}.
	 * 
	 * @param currentNode
	 *            {@link ExecutionNode}.
	 * @return {@link ExecutionNode} to be sequentially executed after the input
	 *         {@link ExecutionNode}.
	 */
	protected ExecutionNode<W> bindSequentialNode(ExecutionNode<W> currentNode) {
		return this.bindFlow(FlowInstigationStrategyEnum.SEQUENTIAL,
				currentNode);
	}

	/**
	 * Binds a {@link ExecutionNode} to be executed in parallel to the input
	 * {@link ExecutionNode}.
	 * 
	 * @param currentNode
	 *            {@link ExecutionNode}.
	 * @return {@link ExecutionNode} to be executed in parallel to the input
	 *         {@link ExecutionNode}.
	 */
	protected ExecutionNode<W> bindParallelNode(ExecutionNode<W> currentNode) {
		return this.bindFlow(FlowInstigationStrategyEnum.PARALLEL, currentNode);
	}

	/**
	 * Binds a {@link ExecutionNode} to be executed asynchronously to the input
	 * {@link ExecutionNode}.
	 * 
	 * @param currentNode
	 *            {@link ExecutionNode}.
	 * @return {@link ExecutionNode} to be executed asynchronously to the input
	 *         {@link ExecutionNode}.
	 */
	protected ExecutionNode<W> bindAsynchronousNode(ExecutionNode<W> currentNode) {
		return this.bindFlow(FlowInstigationStrategyEnum.ASYNCHRONOUS,
				currentNode);
	}

	/**
	 * Binds an {@link ExecutionNode} to instigated as a {@link Flow}.
	 * 
	 * @param instigationStrategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param currentNode
	 *            {@link ExecutionNode}.
	 * @return Flow {@link ExecutionNode}.
	 */
	private ExecutionNode<W> bindFlow(
			FlowInstigationStrategyEnum instigationStrategy,
			ExecutionNode<W> currentNode) {

		// Create the flow node
		ExecutionNode<W> flowNode = new ExecutionNode<W>(this
				.nextExecutionNodeId(), this, currentNode.getWorkMetaData());

		// Bind as flow node
		currentNode.addFlow(instigationStrategy, flowNode);

		// Return the flow node
		return flowNode;
	}

	/**
	 * Binds an {@link ExecutionNode} to wait on an executing
	 * {@link ExecutionNode}.
	 * 
	 * @param waitNode
	 *            {@link ExecutionNode} to wait.
	 * @param futureNode
	 *            {@link ExecutionNode} executing to completion.
	 */
	protected void joinNode(ExecutionNode<W> waitNode,
			ExecutionNode<W> futureNode) {
		// Wait on completion
		waitNode.addJoin(futureNode);
	}

	/**
	 * Executes the tree.
	 */
	protected void execute() {

		// Executing therefore replay mock objects
		this.replayMockObjects();

		// TODO consider testing with administrators
		AdministratorMetaData<?, ?>[] adminMetaData = new AdministratorMetaData[0];

		// Create Process for executing
		ProcessState processState = new ProcessStateImpl(
				new ManagedObjectMetaData[] { this.moMetaData }, adminMetaData,
				null, null);

		// Obtain the Work meta-data
		WorkMetaData<W> workMetaData = this.getInitialNode().getWorkMetaData();

		// Obtain the initial Flow meta-data
		FlowMetaData<?> flowMetaData = workMetaData.getInitialFlowMetaData();

		// Create Flow for executing
		Flow flow = processState.createThread(flowMetaData);

		// Create the initial job node to execute
		JobNode initialJobNode = flow.createJobNode(this.getInitialNode(),
				null, null);

		// Obtain the job from the job node
		Job initialJob = (Job) initialJobNode;

		// Execute the task tree (until complete)
		boolean isComplete;
		do {
			JobContext context = new MockExecutionContext();
			isComplete = initialJob.doJob(context);
		} while (!isComplete);

		// Verify functionality on mock objects
		this.verifyMockObjects();
	}

	/**
	 * Validates the order of execution.
	 * 
	 * @param nodes
	 *            Listing of {@link ExecutionNode} instances to validate are in
	 *            order of execution.
	 */
	protected void validateExecutionOrder(ExecutionNode<?>... expectedNodes) {

		// Record order of execution for expected
		StringBuilder expected = new StringBuilder();
		for (ExecutionNode<?> node : expectedNodes) {
			expected.append(node.getExecutionNodeId() + " ");
		}

		// Record order of execution for actual
		StringBuilder actual = new StringBuilder();
		for (ExecutionNode<?> node : this.executedNodes) {
			actual.append(node.getExecutionNodeId() + " ");
		}

		// Validate execution order
		assertEquals("Incorrect execution order [expected: "
				+ expected.toString() + "actual: " + actual.toString() + "]",
				expected.toString(), actual.toString());
	}

	/**
	 * Listing of {@link ExecutionNode} in order of being executed.
	 */
	private final List<ExecutionNode<?>> executedNodes = new LinkedList<ExecutionNode<?>>();

	/**
	 * Adds an {@link ExecutionNode} that is being executed.
	 * 
	 * @param node
	 *            {@link ExecutionNode} that is being executed.
	 */
	void addExecutedNode(ExecutionNode<?> node) {
		this.executedNodes.add(node);
	}

	/**
	 * Indicates if the input {@link ExecutionNode} was executed.
	 * 
	 * @param node
	 *            {@link ExecutionNode}.
	 * @return <code>true</code> if input {@link ExecutionNode} was executed.
	 */
	boolean isExecuted(ExecutionNode<?> node) {
		// Check if executed
		for (ExecutionNode<?> executedNode : this.executedNodes) {
			if (node == executedNode) {
				return true;
			}
		}

		// Not executed
		return false;
	}

	/**
	 * Latest {@link ExecutionNode} to be created.
	 */
	private ExecutionNode<?> latestTaskNode;

	/**
	 * Specifies the latest {@link ExecutionNode} to be created.
	 * 
	 * @param node
	 *            Latest {@link ExecutionNode} to be created.
	 */
	void setLatestTaskNode(ExecutionNode<?> node) {
		this.latestTaskNode = node;
	}

	/**
	 * Obtains the latest {@link ExecutionNode} to be created.
	 * 
	 * @return Latest {@link ExecutionNode} to be created.
	 */
	ExecutionNode<?> getLatestTaskNode() {
		return this.latestTaskNode;
	}

	/**
	 * {@link Map} of {@link ExecutionNode} to {@link FlowFuture} instances.
	 */
	private final Map<ExecutionNode<?>, FlowFuture> flowFutures = new HashMap<ExecutionNode<?>, FlowFuture>();

	/**
	 * Registers the {@link FlowFuture} for the {@link ExecutionNode}.
	 * 
	 * @param node
	 *            {@link ExecutionNode}.
	 * @param future
	 *            {@link FlowFuture}.
	 */
	void registerFlowFuture(ExecutionNode<?> node, FlowFuture future) {
		this.flowFutures.put(node, future);
	}

	/**
	 * Obtains the {@link FlowFuture} for a {@link ExecutionNode}.
	 * 
	 * @param node
	 *            {@link ExecutionNode}.
	 * @return {@link FlowFuture}.
	 */
	FlowFuture getFlowFuture(ExecutionNode<?> node) {
		return this.flowFutures.get(node);
	}

	/**
	 * Id of the current {@link ExecutionNode}.
	 */
	private int currentExecutionNodeId = 0;

	/**
	 * Obtains the next Id for a new {@link ExecutionNode}.
	 * 
	 * @return Next Id for a new {@link ExecutionNode}.
	 */
	private int nextExecutionNodeId() {
		return ++this.currentExecutionNodeId;
	}

	/*
	 * ========================================================================
	 * Team
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#startWorking()
	 */
	public void startWorking() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.team.Team#assignTask(net.officefloor.frame.
	 * spi.team.TaskContainer)
	 */
	public void assignJob(Job task) {
		// Passively execute
		while (!task.doJob(new MockExecutionContext()))
			;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.Team#stopWorking()
	 */
	public void stopWorking() {
		// Do nothing
	}

	/**
	 * Mock {@link JobContext}.
	 */
	private class MockExecutionContext implements JobContext {

		/**
		 * Time.
		 */
		protected long time = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.team.ExecutionContext#getTime()
		 */
		public long getTime() {
			// Lazy obtain the time
			if (this.time == 0) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.spi.team.ExecutionContext#continueExecution()
		 */
		public boolean continueExecution() {
			// Continue
			return true;
		}

	}

}