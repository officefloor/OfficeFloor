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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.job.AssetNotifySetImplAccess;
import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.impl.execute.linkedlist.AbstractLinkedList;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AssetMonitor}.
 * 
 * @author Daniel
 */
public class AssetMonitorTest extends OfficeFrameTestCase {

	/**
	 * {@link AssetMonitor} being tested.
	 */
	private AssetMonitor assetMonitor;

	/**
	 * {@link LinkedList} of the {@link AssetMonitor} instances.
	 */
	private final LinkedList<AssetMonitor, Object> monitors = new AbstractLinkedList<AssetMonitor, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// No action
		}
	};

	/**
	 * {@link Asset}.
	 */
	private final Asset asset = this.createMock(Asset.class);;

	/**
	 * {@link AssetManager}.
	 */
	private final AssetManager assetManager = this
			.createMock(AssetManager.class);

	/**
	 * {@link Flow}.
	 */
	private final Flow flow = this.createMock(Flow.class);

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		// Create the Task Monitor
		this.assetMonitor = new AssetMonitorImpl(this.asset, this.assetManager,
				this.monitors);
	}

	/**
	 * Ensure correct items are returned.
	 */
	public void testGetters() {
		assertEquals("Incorrect assset", this.asset, this.assetMonitor
				.getAsset());
	}

	/**
	 * Ensure activate no {@link Job} instances.
	 */
	public void testActivateNoJobs() {
		// Notify jobs (should do nothing)
		this.replayMockObjects();
		this.doActivateJobNodes(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure activates the {@link Job} instances.
	 */
	public void testActivateJobs() {
		// Create the Job Nodes
		MockJobNode taskOne = new MockJobNode(this.flow);
		MockJobNode taskTwo = new MockJobNode(this.flow);

		// Record mock objects
		this.recordAssetManagerRegistration();
		this.recordJobNodeActivation(taskOne);
		this.recordJobNodeActivation(taskTwo);

		// Replay
		this.replayMockObjects();

		// Tasks to wait on monitor
		this.doWait(taskOne, true);
		this.doWait(taskTwo, true);
		assertFalse("Task one should not be activated", taskOne.isActivated);
		assertFalse("Task two should not be activated", taskTwo.isActivated);

		// Notify the tasks
		this.doActivateJobNodes(false);
		assertTrue("Task one should be activated", taskOne.isActivated);
		assertTrue("Task two should be activated", taskTwo.isActivated);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to reuse the {@link AssetMonitor}.
	 */
	public void testActivateJobAgain() {
		// Create the Job Nodes
		MockJobNode taskOne = new MockJobNode(this.flow);
		MockJobNode taskTwo = new MockJobNode(this.flow);

		// Record waiting on tasks twice (for each wait/notify)
		this.recordAssetManagerRegistration();
		this.recordJobNodeActivation(taskOne);
		this.recordAssetManagerRegistration();
		this.recordJobNodeActivation(taskTwo);

		// Replay
		this.replayMockObjects();

		// Wait and notify on first task
		this.doWait(taskOne, true);
		this.doActivateJobNodes(false);
		assertTrue("Task one should be activated", taskOne.isActivated);

		// Wait and notify on second task
		this.doWait(taskTwo, true);
		this.doActivateJobNodes(false);
		assertTrue("Task two should be activated", taskOne.isActivated);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure activates the {@link Job} instances permanently.
	 */
	public void testActivatePermantly() {
		// Create the Job Nodes
		MockJobNode taskOne = new MockJobNode(this.flow);
		MockJobNode taskTwo = new MockJobNode(this.flow);

		// Record waiting on tasks only once
		this.recordAssetManagerRegistration();
		this.recordJobNodeActivation(taskOne);

		// Replay
		this.replayMockObjects();

		// Wait and permanently notify task one
		this.doWait(taskOne, true);
		assertFalse("Task one should not be activated", taskOne.isActivated);
		this.doActivateJobNodes(true);
		assertTrue("Task one should be activated", taskOne.isActivated);

		// Can no longer wait on monitor (and not register with Asset Manager)
		this.doWait(taskTwo, false);
		assertFalse("Task two should not be activated", taskTwo.isActivated);
		this.doActivateJobNodes(false);
		assertFalse("Task two should not be activated", taskTwo.isActivated);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure fails the {@link Job} instances.
	 */
	public void testFailJobs() {

		// Create the failure
		Throwable failure = new Exception();

		// Create the Job Nodes
		MockJobNode taskOne = new MockJobNode(this.flow);
		MockJobNode taskTwo = new MockJobNode(this.flow);

		// Record waiting on tasks only once
		this.recordAssetManagerRegistration();
		this.recordJobNodeActivation(taskOne, failure);
		this.recordJobNodeActivation(taskTwo, failure);

		// Replay
		this.replayMockObjects();

		// Wait on task containers
		this.doWait(taskOne, true);
		this.doWait(taskTwo, true);
		assertFalse("Task one should not be activated", taskOne.isActivated);
		assertFalse("Task two should not be activated", taskTwo.isActivated);

		// Notify the tasks of failure
		this.doFailJobNodes(failure, false);
		assertTrue("Task one should be activated", taskOne.isActivated);
		assertTrue("Task two should be activated", taskTwo.isActivated);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure fails the {@link Job} instances permanently.
	 */
	public void testFailPermanently() {

		// Create the failure
		Throwable failure = new Exception();

		// Create the Job Nodes
		MockJobNode taskOne = new MockJobNode(this.flow);
		MockJobNode taskTwo = new MockJobNode(this.flow);

		// Record waiting on tasks only once
		this.recordAssetManagerRegistration();
		this.recordJobNodeActivation(taskOne, failure);

		// Replay
		this.replayMockObjects();

		// Wait and permanently fail task one
		this.doWait(taskOne, true);
		assertFalse("Task one should not be activated", taskOne.isActivated);
		this.doFailJobNodes(failure, true);
		assertTrue("Task one should be activated", taskOne.isActivated);

		// Can no longer wait on monitor (and not register with Asset Manager)
		this.doWait(taskTwo, false);
		assertFalse("Task two should not be activated", taskTwo.isActivated);
		this.doActivateJobNodes(false);
		assertFalse("Task two should not be activated", taskTwo.isActivated);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Records {@link AssetManager} registration.
	 */
	private void recordAssetManagerRegistration() {
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
	}

	/**
	 * Records {@link JobNode} activation.
	 * 
	 * @param jobNode
	 *            {@link JobNode}.
	 */
	private void recordJobNodeActivation(JobNode jobNode) {
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getThreadLock(),
				"Thread lock");
	}

	/**
	 * Records {@link JobNode} activation.
	 * 
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param cause
	 *            {@link Throwable}.
	 */
	private void recordJobNodeActivation(JobNode jobNode, Throwable cause) {
		this.recordJobNodeActivation(jobNode);
		this.threadState.setFailure(cause);
	}

	/**
	 * Has the {@link JobNode} wait on the {@link AssetMonitor}.
	 * 
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param isExpectedWaitReturn
	 *            Expected return from {@link AssetMonitor#wait(Job)}.
	 */
	private void doWait(JobNode jobNode, boolean isExpectedWaitReturn) {
		JobNodeActivatableSetImpl notifySet = new JobNodeActivatableSetImpl();
		assertEquals("Incorrect waiting", isExpectedWaitReturn,
				this.assetMonitor.waitOnAsset(jobNode, notifySet));
		if (isExpectedWaitReturn) {
			// Waiting so should not be added
			assertNull("Task should not be added for notifying",
					AssetNotifySetImplAccess.tasks(notifySet).getHead());
		} else {
			// Ensure added as task to notify
			assertNotNull("Task should be added for notifying",
					AssetNotifySetImplAccess.tasks(notifySet).getHead());
		}
	}

	/**
	 * Activates the {@link JobNode} instances within the {@link AssetMonitor}.
	 * 
	 * @param isPermanent
	 *            <code>true</code> if permanently activate.
	 */
	private void doActivateJobNodes(boolean isPermanent) {
		JobNodeActivatableSetImpl activateSet = new JobNodeActivatableSetImpl();
		this.assetMonitor.activateJobNodes(activateSet, isPermanent);
		activateSet.activateJobNodes();
	}

	/**
	 * Fails the {@link JobNode} instances within the {@link AssetMonitor}.
	 * 
	 * @param failure
	 *            {@link Throwable} to fail {@link Job} instances with.
	 * @param isPermanent
	 *            <code>true</code> if permanently fail.
	 */
	private void doFailJobNodes(Throwable failure, boolean isPermanent) {
		JobNodeActivatableSetImpl activateSet = new JobNodeActivatableSetImpl();
		this.assetMonitor.failJobNodes(activateSet, failure, isPermanent);
		activateSet.activateJobNodes();
	}

	/**
	 * Mock {@link JobNode}.
	 */
	private static class MockJobNode extends JobNodeAdapter {

		/**
		 * Flag indicated if activated.
		 */
		public boolean isActivated = false;

		/**
		 * Initiate.
		 * 
		 * @param flow
		 *            {@link Flow}.
		 */
		public MockJobNode(Flow flow) {
			super(flow);
		}

		/*
		 * ================== JobNodeAdapter ===============================
		 */

		@Override
		public void activateJob() {
			this.isActivated = true;
		}
	}

}