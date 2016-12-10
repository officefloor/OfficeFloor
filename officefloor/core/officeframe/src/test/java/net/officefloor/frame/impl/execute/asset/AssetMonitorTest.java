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

import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link AssetMonitor}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetMonitorTest extends OfficeFrameTestCase {

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
	 * {@link OfficeManager}.
	 */
	private final OfficeManager officeManager = this
			.createMock(OfficeManager.class);

	/**
	 * {@link AssetMonitor} being tested.
	 */
	private final AssetMonitor assetMonitor = new AssetMonitorImpl(this.asset,
			this.assetManager);

	/**
	 * {@link JobNodeActivateSet}.
	 */
	private final JobNodeActivateSet activateSet = this
			.createMock(JobNodeActivateSet.class);

	/**
	 * Ensure correct {@link Asset} is returned.
	 */
	public void testGetAsset() {
		assertEquals("Incorrect assset", this.asset,
				this.assetMonitor.getAsset());
	}

	/**
	 * Ensure activate no {@link Job} instances.
	 */
	public void testActivateNoJobs() {
		this.replayMockObjects();
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure activates the {@link Job} instances.
	 */
	public void testActivateJobs() {

		// Only first job should cause registering with AssetManager
		final JobNode jobOne = this.createMock(JobNode.class);
		final JobNode jobTwo = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne);
		this.activateSet.addJobNode(jobTwo);

		// Add both jobs and activate them
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.waitOnAsset(jobTwo, this.activateSet);
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure {@link JobNode} only added once on waiting.
	 */
	public void testWaitOnJobTwice() {

		final JobNode job = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(job);

		// Add job, add job again, activate
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(job, this.activateSet);
		this.assetMonitor.waitOnAsset(job, this.activateSet);
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to reuse the {@link AssetMonitor}.
	 */
	public void testActivateJobAgain() {

		final JobNode jobOne = this.createMock(JobNode.class);
		final JobNode jobTwo = this.createMock(JobNode.class);
		final AssetMonitor markPosition = this.createMock(AssetMonitor.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne);
		this.assetManager.registerAssetMonitor(markPosition); // mark call
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobTwo);

		// Add job, activate, add another job, activate
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.assetManager.registerAssetMonitor(markPosition); // mark call
		this.assetMonitor.waitOnAsset(jobTwo, this.activateSet);
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure activate {@link JobNode} and then on activate again do nothing.
	 */
	public void testActivateJobThenActivateNothing() {

		final JobNode jobOne = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne);

		// Add job, activate, activate again
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure activates the {@link Job} instances permanently.
	 */
	public void testActivatePermanently() {

		final JobNode jobOne = this.createMock(JobNode.class);
		final JobNode jobTwo = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne);
		this.activateSet.addJobNode(jobTwo);

		// Add job, activate permanently, add another job
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.activateJobNodes(this.activateSet, true);
		this.assetMonitor.waitOnAsset(jobTwo, this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensure activates the {@link Job} instances permanently.
	 */
	public void testOncePermanentAlwaysPermanent() {

		final JobNode jobOne = this.createMock(JobNode.class);
		final JobNode jobTwo = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne);
		this.activateSet.addJobNode(jobTwo);

		// Add job, activate permanently, activate, add another job
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.activateJobNodes(this.activateSet, true);
		this.assetMonitor.activateJobNodes(this.activateSet, false);
		this.assetMonitor.waitOnAsset(jobTwo, this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensure fails the {@link Job} instances.
	 */
	public void testFailJobs() {

		Throwable failure = new Exception();
		final JobNode jobOne = this.createMock(JobNode.class);
		final JobNode jobTwo = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne, failure);
		this.activateSet.addJobNode(jobTwo, failure);

		// Add both jobs and fail them
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.waitOnAsset(jobTwo, this.activateSet);
		this.assetMonitor.failJobNodes(this.activateSet, failure, false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure fails the {@link Job} instances permanently.
	 */
	public void testFailPermanently() {

		Throwable failure = new Exception();
		final JobNode jobOne = this.createMock(JobNode.class);
		final JobNode jobTwo = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne, failure);
		this.activateSet.addJobNode(jobTwo, failure);

		// Add job, fail permanently, add another job
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.failJobNodes(this.activateSet, failure, true);
		this.assetMonitor.waitOnAsset(jobTwo, this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensure activates the {@link Job} instances permanently with a failure.
	 */
	public void testOncePermanentFailureAlwaysFailure() {

		Throwable failure = new Exception();
		final JobNode jobOne = this.createMock(JobNode.class);
		final JobNode jobTwo = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.activateSet.addJobNode(jobOne, failure);
		this.activateSet.addJobNode(jobTwo, failure);

		// Add job, fail permanently, activate permanently, add another job
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(jobOne, this.activateSet);
		this.assetMonitor.failJobNodes(this.activateSet, failure, true);
		this.assetMonitor.activateJobNodes(this.activateSet, true);
		this.assetMonitor.waitOnAsset(jobTwo, this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensure on <code>null</code> {@link JobNodeActivateSet} that uses
	 * {@link OfficeManager} to activate {@link JobNode} instances.
	 */
	public void testActivateByOfficeManager() {

		final JobNode job = this.createMock(JobNode.class);

		// Record
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
		this.recordReturn(this.assetManager,
				this.assetManager.getOfficeManager(), this.officeManager);
		this.officeManager.activateJobNodes(null);
		this.control(this.officeManager).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Invoke to verify within set
				JobNodeActivatableSet activatableSet = (JobNodeActivatableSet) actual[0];
				activatableSet
						.activateJobNodes(OfficeManagerImpl.MANAGE_OFFICE_TEAM);
				return true;
			}
		});
		// Triggered to indicate in set
		job.activateJob(OfficeManagerImpl.MANAGE_OFFICE_TEAM);

		// Add job and activate with no activate set
		this.replayMockObjects();
		this.assetMonitor.waitOnAsset(job, this.activateSet);
		this.assetMonitor.activateJobNodes(null, false);
		this.verifyMockObjects();
	}

	/**
	 * To keep the {@link OfficeManager} contention down only use
	 * {@link OfficeManager} if have {@link JobNode} instances to activate.
	 */
	public void testUseOfficeManagerOnlyIfJobToActivate() {

		// Nothing to record as should not do anything

		// No jobs and no activate set passed
		this.replayMockObjects();
		this.assetMonitor.activateJobNodes(null, true);
		this.verifyMockObjects();
	}

}