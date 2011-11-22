/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.impl.execute.flow.FlowMetaDataImpl;
import net.officefloor.frame.impl.execute.job.AbstractJobContainer;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.execute.process.ProcessMetaDataImpl;
import net.officefloor.frame.impl.execute.process.ProcessStateImpl;
import net.officefloor.frame.impl.execute.thread.ThreadMetaDataImpl;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Abstract functionality to test execution of {@link AbstractJobContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractTaskNodeTestCase<W extends Work> extends
		OfficeFrameTestCase implements Team {

	/**
	 * Initial {@link ExecutionNode}.
	 */
	private ExecutionNode<W> initialNode;

	/**
	 * {@link ManagedObjectSource} for the {@link ManagedObject} of the
	 * {@link ProcessState}.
	 */
	private ManagedObjectSource<?, ?> processMoSource;

	/**
	 * {@link ManagedObjectSource} for the {@link ManagedObject} of the
	 * {@link ThreadState}.
	 */
	private ManagedObjectSource<?, ?> threadMoSource;

	/**
	 * {@link ManagedObjectSource} for the {@link ManagedObject} of the
	 * {@link Work}.
	 */
	private ManagedObjectSource<?, ?> workMoSource;

	/**
	 * Initiate the Test.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setUp() throws Exception {

		// Create the mock objects
		this.processMoSource = this.createMock(ManagedObjectSource.class);
		this.threadMoSource = this.createMock(ManagedObjectSource.class);
		this.workMoSource = this.createMock(ManagedObjectSource.class);

		// Create the Work
		final Work work = new Work() {
		};

		// Create the Work Factory
		WorkFactory workFactory = new WorkFactory() {
			public Work createWork() {
				return work;
			}
		};

		// Create the Initial Flow
		FlowMetaData initialFlowMetaData = new FlowMetaDataImpl(
				FlowInstigationStrategyEnum.ASYNCHRONOUS,
				this.getInitialNode(), new AssetManagerImpl(null));

		// Create the Work Managed Object meta-data (single instance)
		ManagedObjectMetaDataImpl workMo = new ManagedObjectMetaDataImpl(
				"WORK_MO", Object.class, 0, this.workMoSource, null, false,
				new AssetManagerImpl(null), false, new AssetManagerImpl(null),
				false, null, 1000, new ManagedObjectGovernanceMetaData[0]);
		workMo.loadRemainingState(null, null);

		// Create the Work meta-data
		WorkMetaData workMetaData = new WorkMetaDataImpl("TEST_WORK",
				workFactory, new ManagedObjectMetaData[] { workMo },
				new AdministratorMetaData[0], initialFlowMetaData,
				new TaskMetaData[0]);

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
	 * Obtains the {@link ThreadState} bound {@link ManagedObjectSource}.
	 * 
	 * @return {@link ThreadState} bound {@link ManagedObjectSource}.
	 */
	protected ManagedObjectSource<?, ?> getThreadManagedObjectSource() {
		return this.threadMoSource;
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
		ExecutionNode<W> nextNode = new ExecutionNode<W>(
				this.nextExecutionNodeId(), this, currentNode.getWorkMetaData());

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
	 * Binds an {@link ExecutionNode} to instigated as a {@link JobSequence}.
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
		ExecutionNode<W> flowNode = new ExecutionNode<W>(
				this.nextExecutionNodeId(), this, currentNode.getWorkMetaData());

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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void execute() {

		// Executing therefore replay mock objects
		this.replayMockObjects();

		// Consider the thread meta-data (single instance)
		ManagedObjectMetaDataImpl<?> threadMoMetaData = new ManagedObjectMetaDataImpl(
				"THREAD_MO", Object.class, 0, this.threadMoSource, null, false,
				new AssetManagerImpl(null), false, new AssetManagerImpl(null),
				false, null, 1000, new ManagedObjectGovernanceMetaData[0]);
		ThreadMetaData threadMetaData = new ThreadMetaDataImpl(
				new ManagedObjectMetaData[] { threadMoMetaData },
				new GovernanceMetaData[0], new AdministratorMetaData[0],
				GovernanceDeactivationStrategy.DISREGARD);

		// Create the process meta-data (single instance)
		ManagedObjectMetaDataImpl<?> processMoMetaData = new ManagedObjectMetaDataImpl(
				"PROCESS_MO", Object.class, 0, this.processMoSource, null,
				false, new AssetManagerImpl(null), true, new AssetManagerImpl(
						null), false, null, 1000,
				new ManagedObjectGovernanceMetaData[0]);
		processMoMetaData.loadRemainingState(null, null);
		ProcessMetaData processMetaData = new ProcessMetaDataImpl(
				new ManagedObjectMetaData[] { processMoMetaData },
				new AdministratorMetaData[0], threadMetaData);

		// Create Flow for executing
		ProcessState processState = new ProcessStateImpl(processMetaData,
				new ProcessContextListener[0], null, null);
		WorkMetaData<W> workMetaData = this.getInitialNode().getWorkMetaData();
		FlowMetaData<?> flowMetaData = workMetaData.getInitialFlowMetaData();
		AssetManager flowAssetManager = flowMetaData.getFlowManager();
		JobSequence flow = processState.createThread(flowAssetManager);

		// Create the initial job node to execute
		JobNode initialJobNode = flow.createTaskNode(this.getInitialNode(),
				null, null, GovernanceDeactivationStrategy.ENFORCE);
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
		assertEquals(
				"Incorrect execution order [expected: " + expected.toString()
						+ "actual: " + actual.toString() + "]",
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
	 * ===================== Team =============================================
	 */

	@Override
	public void startWorking() {
		// Do nothing
	}

	@Override
	public void assignJob(Job task) {
		// Passively execute
		while (!task.doJob(new MockExecutionContext()))
			;
	}

	@Override
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
		private long time = 0;

		@Override
		public long getTime() {
			// Lazy obtain the time
			if (this.time == 0) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
		}

		@Override
		public boolean continueExecution() {
			// Continue
			return true;
		}
	}

}