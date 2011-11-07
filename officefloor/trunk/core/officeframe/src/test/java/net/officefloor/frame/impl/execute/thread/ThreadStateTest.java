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

package net.officefloor.frame.impl.execute.thread;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.escalate.FlowJoinTimedOutEscalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorContainer;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.TestInstanceFactory;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link ThreadState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadStateTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to create and complete the {@link ThreadState}.
	 */
	public void testState() {

		final ManagedObjectMetaData<?> moMetaData = this
				.createMock(ManagedObjectMetaData.class);
		final ManagedObjectContainer moContainer = this
				.createMock(ManagedObjectContainer.class);
		final AdministratorMetaData<?, ?> adminMetaData = this
				.createMock(AdministratorMetaData.class);
		final AdministratorContainer<?, ?> adminContainer = this
				.createMock(AdministratorContainer.class);

		// Record creating the ThreadState
		this.recordReturn(this.threadMetaData,
				this.threadMetaData.getManagedObjectMetaData(),
				new ManagedObjectMetaData[] { moMetaData });
		this.recordReturn(this.threadMetaData,
				this.threadMetaData.getAdministratorMetaData(),
				new AdministratorMetaData[] { adminMetaData });
		this.recordReturn(this.flowMetaData,
				this.flowMetaData.getFlowManager(), this.threadManager);

		// Record obtaining the Managed Object Container
		this.recordReturn(this.threadMetaData,
				this.threadMetaData.getManagedObjectMetaData(),
				new ManagedObjectMetaData[] { moMetaData });
		this.recordReturn(moMetaData,
				moMetaData.createManagedObjectContainer(this.processState),
				moContainer);

		// Record obtaining the Administrator Container
		this.recordReturn(this.threadMetaData,
				this.threadMetaData.getAdministratorMetaData(),
				new AdministratorMetaData[] { adminMetaData });
		this.recordReturn(adminMetaData,
				adminMetaData.createAdministratorContainer(), adminContainer);

		// Test
		this.replayMockObjects();
		ThreadState thread = this.createThreadState();

		// Lazy load managed object container
		assertEquals("Incorrect managed object container", moContainer,
				thread.getManagedObjectContainer(0));

		// Lazy load administrator container
		assertEquals("Incorrect administrator container", adminContainer,
				thread.getAdministratorContainer(0));

		// Verify
		this.verifyMockObjects();

		// Verify state of thread
		assertEquals("Incorrect lock", thread, thread.getThreadLock());
		assertEquals("Incorrect meta-data", this.threadMetaData,
				thread.getThreadMetaData());
		assertEquals("Incorrect process state", this.processState,
				thread.getProcessState());

		// Verify failure
		assertNull("No failure initially", thread.getFailure());
		Throwable failure = new Throwable("Thread Failure");
		thread.setFailure(failure);
		assertEquals("Incorrect failure", failure, thread.getFailure());
		thread.setFailure(null);
		assertNull("Should clear failure", thread.getFailure());
		assertFalse("Initially not complete", thread.isComplete());

		// Verify escalation level
		assertEquals("Incorrect initial escalation level",
				EscalationLevel.FLOW, thread.getEscalationLevel());
		thread.setEscalationLevel(EscalationLevel.OFFICE);
		assertEquals("Incorrect change in escalation level",
				EscalationLevel.OFFICE, thread.getEscalationLevel());

		// Ensure can get same managed object container
		assertEquals("Incorrect managed object container", moContainer,
				thread.getManagedObjectContainer(0));

		// Ensure can get same administrator container
		assertEquals("Incorrect administrator container", adminContainer,
				thread.getAdministratorContainer(0));
	}

	/**
	 * Ensure able to create and complete the {@link ThreadState}.
	 */
	public void testCreateAndCompleteThread() {

		final FlowMetaData<?> newFlow = this.createMock(FlowMetaData.class);

		// Record creating and completing the ThreadState
		this.record_ThreadState_init();
		this.record_ProcessState_threadComplete();

		// Test
		this.replayMockObjects();
		ThreadState thread = this.createThreadState();
		Flow flow = thread.createFlow(newFlow); // a flow always created
		thread.flowComplete(flow, this.activateSet);
		this.verifyMockObjects();

		// Thread should be complete
		assertTrue("Thread should be complete", thread.isComplete());
	}

	/**
	 * Ensure {@link ThreadState} does not complete while escalating.
	 */
	public void testEscalating() {

		final JobNode jobNode = this.createMock(JobNode.class);
		final FlowMetaData<?> flowMetaDataOne = this
				.createMock(FlowMetaData.class);
		final FlowMetaData<?> flowMetaDataTwo = this
				.createMock(FlowMetaData.class);

		// Record creating and completing the ThreadState
		this.record_ThreadState_init();
		this.record_ProcessState_threadComplete();

		// Test
		this.replayMockObjects();
		ThreadState thread = this.createThreadState();

		// Not completes while escalating
		Flow flowOne = thread.createFlow(flowMetaDataOne);
		thread.escalationStart(jobNode, this.activateSet);
		thread.flowComplete(flowOne, this.activateSet);
		assertFalse("Should not complete while escalating", thread.isComplete());

		// Completes after escalating
		thread.escalationComplete(jobNode, this.activateSet);
		Flow flowTwo = thread.createFlow(flowMetaDataTwo);
		thread.flowComplete(flowTwo, this.activateSet);
		assertTrue("Thread should be complete", thread.isComplete());

		this.verifyMockObjects();
	}

	/**
	 * Ensures a {@link JobNode} does not wait on the {@link ThreadState} when
	 * the {@link ThreadState} is complete.
	 */
	public void testNotWaitOnThreadWhenComplete() {

		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);
		final JobNode jobNode = this.createMock(JobNode.class);

		// Record not waiting on a completed thread
		this.record_ThreadState_init();
		this.record_ProcessState_threadComplete();
		this.activateSet.addJobNode(jobNode);

		// Test
		this.replayMockObjects();
		ThreadStateImpl thread = this.createThreadState();
		thread.flowComplete(thread.createFlow(flowMetaData), this.activateSet);
		thread.waitOnFlow(jobNode, 1000, "TOKEN", this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensures a {@link JobNode} does not wait on its {@link ThreadState}.
	 */
	public void testJobNodeNotWaitOnItsOwnThread() {

		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);
		final Work work = this.createMock(Work.class);
		final Task<?, ?, ?> task = this.createMock(Task.class);
		final JobNode[] jobNode = new JobNode[1];

		final WorkMetaData<Work> workMetaData = TestInstanceFactory
				.createWorkMetaData(work);
		final TaskMetaData<?, ?, ?> taskMetaData = TestInstanceFactory
				.createTaskMetaData(task, workMetaData);

		// Record initialising ThreadState and create a JobNode from it
		this.record_ThreadState_init();

		// Record not waiting on own thread state
		this.activateSet.addJobNode(null);
		this.control(this.activateSet).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect job node", jobNode[0], actual[0]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		ThreadStateImpl thread = this.createThreadState();
		Flow flow = thread.createFlow(flowMetaData);
		jobNode[0] = flow.createJobNode(taskMetaData, null, null);
		thread.waitOnFlow(jobNode[0], 1000, "TOKEN", this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensures that the joined {@link JobNode} instances are activated on
	 * {@link ThreadState} completion.
	 */
	public void testActivateJoinedJobNodesOnThreadCompletion() {

		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);
		final JobNode jobNode = this.createMock(JobNode.class);

		// Record not waiting on a completed thread
		this.record_ThreadState_init();
		AssetMonitor monitor = this.record_FlowAsset_waitOnFlow(jobNode);
		monitor.activateJobNodes(this.activateSet, true);
		this.record_ProcessState_threadComplete();

		// Test
		this.replayMockObjects();
		ThreadStateImpl thread = this.createThreadState();
		thread.waitOnFlow(jobNode, 1000, "TOKEN", this.activateSet);
		thread.flowComplete(thread.createFlow(flowMetaData), this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensures does nothing on a joined {@link JobNode} if the join is not timed
	 * out on a check.
	 */
	public void testCheckOnAssetWithNotTimedOutJoin() {

		final JobNode jobNode = this.createMock(JobNode.class);
		final CheckAssetContext context = this
				.createMock(CheckAssetContext.class);

		// Record only creation as check will do nothing
		this.record_ThreadState_init();
		this.record_FlowAsset_waitOnFlow(jobNode);
		this.recordReturn(context, context.getTime(),
				System.currentTimeMillis());

		// Test
		this.replayMockObjects();
		ThreadStateImpl thread = this.createThreadState();
		thread.waitOnFlow(jobNode, NOT_TIME_OUT, "TOKEN", this.activateSet);
		this.joinedAssets.get(0).checkOnAsset(context);
		this.verifyMockObjects();
	}

	/**
	 * Ensures can time out the {@link JobNode} join on a check.
	 */
	public void testTimeOutJoinForCheckOnAsset() {

		final JobNode jobNode = this.createMock(JobNode.class);
		final CheckAssetContext context = this
				.createMock(CheckAssetContext.class);
		final String token = "TOKEN";

		// Record check timing out the join
		this.record_ThreadState_init();
		this.record_FlowAsset_waitOnFlow(jobNode);
		this.recordReturn(context, context.getTime(), TIME_OUT_TIME);
		context.failJobNodes(null, true);
		this.control(context).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				FlowJoinTimedOutEscalation escalation = (FlowJoinTimedOutEscalation) actual[0];
				assertEquals("Incorrect escalation", token,
						escalation.getToken());
				assertEquals("Should be permanent failure", true, actual[1]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();
		ThreadStateImpl thread = this.createThreadState();
		thread.waitOnFlow(jobNode, 10, token, this.activateSet);
		this.joinedAssets.get(0).checkOnAsset(context);
		this.verifyMockObjects();
	}

	/**
	 * Ensures can time out the {@link JobNode} join and activate another
	 * {@link JobNode} join on {@link ThreadState} completion.
	 */
	public void testTimeOutOneJoinAndCompleteOtherJoin() {

		final JobNode timedOutJobNode = this.createMock(JobNode.class);
		final Object timedOutToken = "TOKEN";
		final JobNode activatedJobNode = this.createMock(JobNode.class);
		final Object activatedToken = new Integer(1);
		final CheckAssetContext context = this
				.createMock(CheckAssetContext.class);

		// Record check timing out the join
		this.record_ThreadState_init();
		this.record_FlowAsset_waitOnFlow(timedOutJobNode);
		AssetMonitor activatedJobNodeMonitor = this
				.record_FlowAsset_waitOnFlow(activatedJobNode);
		this.recordReturn(context, context.getTime(), TIME_OUT_TIME);
		context.failJobNodes(null, true);
		this.control(context).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				FlowJoinTimedOutEscalation escalation = (FlowJoinTimedOutEscalation) actual[0];
				assertEquals("Incorrect escalation", timedOutToken,
						escalation.getToken());
				assertEquals("Should be permanent failure", true, actual[1]);
				return true;
			}
		});
		activatedJobNodeMonitor.activateJobNodes(this.activateSet, true);
		this.record_ProcessState_threadComplete();

		// Test
		this.replayMockObjects();
		ThreadStateImpl thread = this.createThreadState();
		thread.waitOnFlow(timedOutJobNode, 10, timedOutToken, this.activateSet);
		thread.waitOnFlow(activatedJobNode, NOT_TIME_OUT, activatedToken,
				this.activateSet);
		this.joinedAssets.get(0).checkOnAsset(context);
		thread.flowComplete(thread.createFlow(flowMetaData), this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Timeout that should be large enough to not be reached.
	 */
	private final long NOT_TIME_OUT = 2000000;

	/**
	 * Time in the future that should be large enough to not be reached.
	 */
	private final long TIME_OUT_TIME = System.currentTimeMillis()
			+ (NOT_TIME_OUT / 2);

	/**
	 * {@link ThreadMetaData}.
	 */
	private final ThreadMetaData threadMetaData = this
			.createMock(ThreadMetaData.class);

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = this
			.createMock(ProcessState.class);

	/**
	 * {@link FlowMetaData}.
	 */
	private final FlowMetaData<?> flowMetaData = this
			.createMock(FlowMetaData.class);

	/**
	 * {@link AssetManager} for the {@link ThreadState}.
	 */
	private final AssetManager threadManager = this
			.createMock(AssetManager.class);

	/**
	 * {@link JobNodeActivateSet}.
	 */
	private final JobNodeActivateSet activateSet = this
			.createMock(JobNodeActivateSet.class);

	/**
	 * Records instantiating the {@link ThreadState}.
	 */
	private void record_ThreadState_init() {
		this.recordReturn(this.threadMetaData,
				this.threadMetaData.getManagedObjectMetaData(),
				new ManagedObjectMetaData[0]);
		this.recordReturn(this.threadMetaData,
				this.threadMetaData.getAdministratorMetaData(),
				new AdministratorMetaData[0]);
		this.recordReturn(this.flowMetaData,
				this.flowMetaData.getFlowManager(), this.threadManager);
	}

	/**
	 * Flag indicating if the createAssetMonitor matcher is set.
	 */
	private boolean isMatcherSet_createAssetMonitor = false;

	/**
	 * Listing of joined {@link Asset} instances.
	 */
	private List<Asset> joinedAssets = new LinkedList<Asset>();

	/**
	 * Records the {@link JobNode} joining on the {@link ThreadState}.
	 * 
	 * @param jobNode
	 *            {@link JobNode}.
	 * @return {@link AssetMonitor} monitoring the join.
	 */
	private AssetMonitor record_FlowAsset_waitOnFlow(JobNode jobNode) {

		final Flow flow = this.createMock(Flow.class);
		final ThreadState anotherThreadState = this
				.createMock(ThreadState.class);
		final AssetMonitor assetMonitor = this.createMock(AssetMonitor.class);

		// Record job node joining on ThreadState
		this.recordReturn(jobNode, jobNode.getFlow(), flow);
		this.recordReturn(flow, flow.getThreadState(), anotherThreadState);
		this.recordReturn(this.threadManager,
				this.threadManager.createAssetMonitor(null), assetMonitor);
		if (!this.isMatcherSet_createAssetMonitor) {
			this.control(this.threadManager).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					// Add joined asset to listing
					ThreadStateTest.this.joinedAssets.add((Asset) actual[0]);
					return true;
				}
			});
			this.isMatcherSet_createAssetMonitor = true;
		}
		this.recordReturn(assetMonitor,
				assetMonitor.waitOnAsset(jobNode, this.activateSet), true);

		// Return the asset monitor for the join
		return assetMonitor;
	}

	/**
	 * Records notifying the {@link ProcessState} that the {@link ThreadState}
	 * is complete.
	 */
	private void record_ProcessState_threadComplete() {
		this.processState.threadComplete(null, this.activateSet);
		this.control(this.processState).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				ThreadState threadState = (ThreadState) actual[0];
				assertEquals("Incorrect ThreadState",
						ThreadStateTest.this.processState,
						threadState.getLinkedListSetOwner());
				assertEquals("Incorrect activate set",
						ThreadStateTest.this.activateSet, actual[1]);
				return true;
			}
		});
	}

	/**
	 * Creates the {@link ThreadStateImpl}.
	 * 
	 * @return {@link ThreadStateImpl}.
	 */
	private ThreadStateImpl createThreadState() {
		return new ThreadStateImpl(this.threadMetaData, this.processState,
				this.flowMetaData);
	}
}