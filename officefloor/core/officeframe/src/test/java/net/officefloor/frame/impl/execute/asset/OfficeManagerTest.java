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
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.job.JobNodeActivatableSetImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

/**
 * Tests the {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagerTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeManager}.
	 */
	private final OfficeManagerImpl officeManager = new OfficeManagerImpl(
			"TEST", 100000); // so large that should not trigger a manage

	/**
	 * Ensures the {@link AssetManager} is managed.
	 */
	public void testManagesTheAssetManagers() {

		final AssetManager assetManager = this.createMock(AssetManager.class);

		// Record registering the AssetManager
		this.recordReturn(assetManager, assetManager.getLinkedListSetOwner(),
				this.officeManager);
		this.recordReturn(assetManager, assetManager.getPrev(), null);

		// Record checking the assets
		assetManager.checkOnAssets(null);
		this.control(assetManager).setMatcher(
				new TypeMatcher(JobNodeActivateSet.class));
		this.recordReturn(assetManager, assetManager.getNext(), null);

		// Run managing the assets
		this.replayMockObjects();
		this.officeManager.registerAssetManager(assetManager);
		this.officeManager.startManaging();
		this.officeManager.checkOnAssets();
		this.officeManager.stopManaging();
		this.verifyMockObjects();
	}

	/**
	 * <p>
	 * Ensures continues managing the {@link AssetManager} instances if one
	 * {@link AssetManager} fails checking on {@link Asset} instances.
	 * <p>
	 * An {@link AssetManager} should never fail on checking {@link Asset}
	 * instances, however this test is here to ensure the {@link OfficeManager}
	 * {@link Thread} will continue looping an not exit due to the failure.
	 */
	public void testHandlesAssetManagerFailure() {

		final AssetManager assetManagerOne = this
				.createMock(AssetManager.class);
		final AssetManager assetManagerTwo = this
				.createMock(AssetManager.class);

		// Record registering the AssetManagers
		this.recordReturn(assetManagerOne,
				assetManagerOne.getLinkedListSetOwner(), this.officeManager);
		this.recordReturn(assetManagerOne, assetManagerOne.getPrev(), null);
		this.recordReturn(assetManagerTwo,
				assetManagerTwo.getLinkedListSetOwner(), this.officeManager);
		this.recordReturn(assetManagerTwo, assetManagerTwo.getPrev(), null);
		assetManagerOne.setNext(assetManagerTwo);
		assetManagerTwo.setPrev(assetManagerOne);

		// Record managing the assets with first failing
		assetManagerOne.checkOnAssets(null);
		this.control(assetManagerOne).setMatcher(
				new TypeMatcher(JobNodeActivateSet.class));
		this.control(assetManagerOne).expectAndThrow(null,
				new RuntimeException("Failure"));
		this.recordReturn(assetManagerOne, assetManagerOne.getNext(),
				assetManagerTwo);
		assetManagerTwo.checkOnAssets(null);
		this.control(assetManagerTwo).setMatcher(
				new TypeMatcher(JobNodeActivateSet.class));
		this.recordReturn(assetManagerTwo, assetManagerTwo.getNext(), null);

		// Run checking on the assets
		this.replayMockObjects();
		this.officeManager.registerAssetManager(assetManagerOne);
		this.officeManager.registerAssetManager(assetManagerTwo);
		this.officeManager.startManaging();
		this.officeManager.checkOnAssets();
		this.officeManager.stopManaging();
		this.verifyMockObjects();
	}

	/**
	 * Ensures the {@link OfficeManager} activates the {@link JobNode} instances
	 * in its own {@link Thread}.
	 */
	public void testActivateJobNodes() throws Exception {

		final JobSequence flow = this.createSynchronizedMock(JobSequence.class);

		// Create the Job Node to be activated
		MockJobNode job = new MockJobNode(flow);
		JobNodeActivatableSet activatableSet = new JobNodeActivatableSetImpl();
		activatableSet.addJobNode(job);

		// Start testing
		this.replayMockObjects();
		this.officeManager.startManaging();

		// Lock on job to ensure OfficeManager using its own thread
		synchronized (job) {
			// Give to Office Manager to activate
			this.officeManager.activateJobNodes(activatableSet);

			// Wait for Office Manager to activate job
			job.wait(1000);

			// Ensure the job was activated (and not wait finished)
			assertTrue("Job should be activated by OfficeManager",
					job.isActivated);
		}

		// Run again to ensure continues to activate jobs
		synchronized (job) {
			job.isActivated = false;
			this.officeManager.activateJobNodes(activatableSet);
			job.wait(1000);
			assertTrue("Job should again be activated", job.isActivated);
		}

		// Clean up and verify functionality
		this.officeManager.stopManaging();
		this.verifyMockObjects();
	}

	/**
	 * {@link JobNode} for testing.
	 */
	private class MockJobNode extends JobNodeAdapter {

		/**
		 * Flag indicating if activated.
		 */
		public boolean isActivated = false;

		/**
		 * Initiate.
		 * 
		 * @param flow
		 *            {@link JobSequence}.
		 */
		public MockJobNode(JobSequence flow) {
			super(flow);
		}

		/*
		 * =============== JobNode ============================
		 */

		@Override
		public synchronized void activateJob(TeamIdentifier currentTeam) {
			// Flag active and notify test that activated
			this.isActivated = true;
			this.notify();
		}
	}

}