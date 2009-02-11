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

import net.officefloor.frame.impl.execute.job.JobActivatableSetImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AssetManager}.
 * 
 * @author Daniel
 */
public class AssetManagerTest extends OfficeFrameTestCase {

	/**
	 * {@link AssetManager} being monitored.
	 */
	private final AssetManager assetManager = new AssetManagerImpl();

	/**
	 * {@link Asset}.
	 */
	private final MockAsset asset = new MockAsset();

	/**
	 * {@link AssetMonitor}.
	 */
	private AssetMonitor assetMonitor;

	/**
	 * {@link JobNode}.
	 */
	private final JobNode jobNode = this.createMock(JobNode.class);

	/**
	 * {@link Flow}.
	 */
	private final Flow flow = this.createMock(Flow.class);

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * Setup.
	 */
	protected void setUp() throws Exception {
		// Create the necessary helper objects
		this.assetMonitor = this.assetManager.createAssetMonitor(this.asset);
	}

	/**
	 * Tests the managing of the Assets.
	 */
	public void testAssetManagement() {

		// Failure
		final Throwable failure = new Exception();

		// Record the failure
		// (Lock on the thread to set failure)
		this.recordReturn(this.jobNode, this.jobNode.getFlow(), this.flow);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getThreadLock(),
				"Thead Lock");
		// (Specify the failure on the ThreadState)
		this.threadState.setFailure(failure);
		// (Activates the job)
		this.jobNode.activateJob();

		// Replay
		this.replayMockObjects();

		// Wait on a Task
		this.assetMonitor.wait(this.jobNode, new JobActivatableSetImpl());

		// Manage
		this.assetManager.manageAssets();

		// Flag the Asset failed
		this.asset.setFailure(failure);

		// Manage again (this time should be failing)
		this.assetManager.manageAssets();

		// Verify
		this.verifyMockObjects();
	}
}
