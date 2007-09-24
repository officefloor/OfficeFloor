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

import net.officefloor.frame.impl.execute.AbstractLinkedList;
import net.officefloor.frame.impl.execute.AssetMonitorImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.spi.team.TaskContainer;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link net.officefloor.frame.internal.structure.AssetMonitor}.
 * 
 * @author Daniel
 */
public class AssetMonitorTest extends OfficeFrameTestCase {

	/**
	 * {@link AssetMonitor} being tested.
	 */
	protected AssetMonitor assetMonitor;

	/**
	 * Lock for the {@link AssetMonitor}.
	 */
	protected final Object lock = new Object();

	/**
	 * {@link LinkedList} of the {@link AssetMonitor} instances.
	 */
	protected final LinkedList<AssetMonitor> monitors = new AbstractLinkedList<AssetMonitor>() {
		public void lastLinkedListEntryRemoved() {
			// No action
		}
	};

	/**
	 * Mock {@link Asset}.
	 */
	protected Asset asset;

	/**
	 * Mock {@link AssetManager}.
	 */
	protected AssetManager assetGroup;

	/**
	 * Setup.
	 */
	protected void setUp() throws Exception {
		// Create the Mock objects for testing
		this.asset = this.createMock(Asset.class);
		this.assetGroup = this.createMock(AssetManager.class);

		// Create the Task Monitor
		this.assetMonitor = new AssetMonitorImpl(this.asset, this.lock,
				this.assetGroup, this.monitors);
	}

	public void testTODO() {
		// TODO: redo the locking in this test
	}
	
	/**
	 * Ensure correct items are returend.
	 */
	public void testGetters() {
		assertEquals("Incorrect lock object", this.lock, this.assetMonitor
				.getAssetLock());
		assertEquals("Incorrect assset", this.asset, this.assetMonitor
				.getAsset());
	}

	/**
	 * Ensure notify no {@link TaskContainer} instances.
	 */
	public void testNotifyNoTasks() {
		// Replay
		this.replayMockObjects();

		// Notify tasks (should do nothing)
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.notifyTasks();
		}

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure notifies the {@link TaskContainer} instances.
	 */
	public void testNotifyTasks() {
		// Create the Task Containers
		MockTaskContainer taskOne = new MockTaskContainer(this);
		MockTaskContainer taskTwo = new MockTaskContainer(this);

		// Record waiting on tasks only once
		// (then no longer waiting after notify)
		this.assetGroup.registerAssetMonitor(this.assetMonitor);
		this.assetGroup.unregisterAssetMonitor(this.assetMonitor);

		// Replay
		this.replayMockObjects();

		// Wait on task containers
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.wait(taskOne);
		}
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.wait(taskTwo);
		}

		// Ensure tasks are not activated
		assertFalse("Task one should not be activated", taskOne.isActivated());
		assertFalse("Task two should not be activated", taskTwo.isActivated());

		// Notify the task containers
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.notifyTasks();
		}

		// Verify
		this.verifyMockObjects();

		// Ensure tasks were activated
		assertTrue("Task one should be activated", taskOne.isActivated());
		assertTrue("Task two should be activated", taskTwo.isActivated());
	}

	/**
	 * Ensure fails the {@link TaskContainer} instances.
	 */
	public void testFailTasks() {

		// Create the failure
		Throwable failure = new Exception();

		// Create the Task Containers
		MockTaskContainer taskOne = new MockTaskContainer(this);
		MockTaskContainer taskTwo = new MockTaskContainer(this);

		// Record waiting on tasks only once
		// (then no longer waiting after fail)
		this.assetGroup.registerAssetMonitor(this.assetMonitor);
		this.assetGroup.unregisterAssetMonitor(this.assetMonitor);

		// Record failing the first task
		taskOne.getThreadState().getThreadLock();
		this.control(taskOne.getThreadState()).setReturnValue(new Object());
		taskOne.getThreadState().setFailure(failure);

		// Record failing the second task
		taskTwo.getThreadState().getThreadLock();
		this.control(taskTwo.getThreadState()).setReturnValue(new Object());
		taskTwo.getThreadState().setFailure(failure);

		// Replay
		this.replayMockObjects();

		// Wait on task containers
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.wait(taskOne);
		}
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.wait(taskTwo);
		}

		// Ensure tasks are not activated
		assertFalse("Task one should not be activated", taskOne.isActivated());
		assertFalse("Task two should not be activated", taskTwo.isActivated());

		// Notify the task containers
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.failTasks(failure);
		}

		// Verify
		this.verifyMockObjects();

		// Ensure tasks were activated
		assertTrue("Task one should be activated", taskOne.isActivated());
		assertTrue("Task two should be activated", taskTwo.isActivated());
	}
}
