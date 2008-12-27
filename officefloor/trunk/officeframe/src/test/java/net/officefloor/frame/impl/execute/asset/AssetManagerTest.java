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

import net.officefloor.frame.impl.execute.AssetManagerImpl;
import net.officefloor.frame.impl.execute.JobActivatableSetImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link net.officefloor.frame.internal.structure.ProjectManager}.
 * 
 * @author Daniel
 */
public class AssetManagerTest extends OfficeFrameTestCase {

	/**
	 * {@link AssetManager} being monitored.
	 */
	protected AssetManager assetManager;

	/**
	 * Mock {@link Asset} for testing.
	 */
	protected MockAsset asset;

	/**
	 * {@link AssetMonitor} for testing.
	 */
	protected AssetMonitor assetMonitor;

	/**
	 * Mock {@link net.officefloor.frame.spi.team.Job}.
	 */
	protected MockJobNode taskContainer;

	/**
	 * Setup.
	 */
	protected void setUp() throws Exception {

		// Create the Asset Group to test
		this.assetManager = new AssetManagerImpl();

		// Create the mock objects
		this.asset = new MockAsset();
		this.taskContainer = new MockJobNode(this);

		// Create the necessary helper objects
		this.assetMonitor = this.assetManager.createAssetMonitor(this.asset,
				this.asset.getAssetLock());
	}

	/**
	 * Tests the managing of the Assets.
	 */
	public void testAssetManagement() {

		// Failure
		final Throwable failure = new Exception();

		// Record the failure
		// (Lock on the thread to set failure)
		this.taskContainer.getThreadState().getThreadLock();
		this.control(this.taskContainer.getThreadState()).setReturnValue(
				new Object());
		// (Specify the failure on the ThreadState)
		this.taskContainer.getThreadState().setFailure(failure);

		// Replay
		this.replayMockObjects();

		// Wait on a Task
		this.assetMonitor.wait(this.taskContainer, new JobActivatableSetImpl());

		// Manage
		this.assetManager.manageAssets();

		// Ensure the Task is not activated
		assertFalse("Task should not be activated", this.taskContainer
				.isActivated());

		// Flag the Asset failed
		this.asset.setFailure(failure);

		// Manage again (this time should be failing)
		this.assetManager.manageAssets();

		// Ensure the Task is activated
		assertTrue("Task should be activated (though failed)",
				this.taskContainer.isActivated());

		// Verify
		this.verifyMockObjects();
	}

}
