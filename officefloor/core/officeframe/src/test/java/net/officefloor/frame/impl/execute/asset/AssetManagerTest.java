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

import junit.framework.TestCase;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeManager}.
	 */
	private final OfficeManager officeManager = this
			.createMock(OfficeManager.class);

	/**
	 * {@link AssetManager} being tested.
	 */
	private final AssetManagerImpl assetManager = new AssetManagerImpl(
			this.officeManager);

	/**
	 * {@link Asset}.
	 */
	private final Asset asset = this.createMock(Asset.class);

	/**
	 * {@link AssetLatch}.
	 */
	private AssetLatch monitor = this.createMock(AssetLatch.class);

	/**
	 * {@link JobNodeActivateSet}.
	 */
	private final JobNodeActivateSet activateSet = this
			.createMock(JobNodeActivateSet.class);

	/**
	 * Tests the {@link AssetManager} checking on no {@link Asset} instances.
	 */
	public void testCheckOnNoAssets() {

		// Nothing to record

		// Register monitor and check on its asset that does nothing
		this.replayMockObjects();
		this.assetManager.checkOnAssets(this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Tests the {@link AssetManager} checking on an {@link Asset}.
	 */
	public void testCheckOnAsset() {

		// Record
		this.record_registerAssetMonitor_copyLinkedList();
		this.record_checkOnAsset();

		// Register monitor and check on its asset that does nothing
		this.replayMockObjects();
		this.assetManager.registerAssetMonitor(this.monitor);
		this.assetManager.checkOnAssets(this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Tests the {@link AssetLatch} being registered and unregistered before a
	 * check on the {@link Asset}.
	 */
	public void testRegisterAndUnregisterBeforeCheckOnAsset() {

		// Record
		this.recordReturn(this.monitor, this.monitor.getLinkedListSetOwner(),
				this.assetManager);
		this.recordReturn(this.monitor, this.monitor.getPrev(), null);
		this.recordReturn(this.monitor, this.monitor.getLinkedListSetOwner(),
				this.assetManager);
		this.recordReturn(this.monitor, this.monitor.getPrev(), null);
		this.recordReturn(this.monitor, this.monitor.getNext(), null);
		this.recordReturn(this.monitor, this.monitor.getNext(), null);
		this.recordReturn(this.monitor, this.monitor.getPrev(), null);
		this.monitor.setNext(null);
		this.monitor.setPrev(null);

		// Register and unregister monitor before the check on assets
		this.replayMockObjects();
		this.assetManager.registerAssetMonitor(this.monitor);
		this.assetManager.unregisterAssetMonitor(this.monitor);
		this.assetManager.checkOnAssets(this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Tests the {@link Asset} throwing an {@link Exception} on being checked.
	 */
	public void testHandleCheckOnAssetThrowingException() {

		final AssetLatch failingMonitor = this.createMock(AssetLatch.class);
		final Asset failingAsset = this.createMock(Asset.class);
		final RuntimeException failure = new RuntimeException("Fail check");
		final AssetLatch secondMonitor = this.createMock(AssetLatch.class);
		final Asset secondAsset = this.createMock(Asset.class);

		// Record registering both assets
		this.recordReturn(failingMonitor, failingMonitor
				.getLinkedListSetOwner(), this.assetManager);
		this.recordReturn(failingMonitor, failingMonitor.getPrev(), null);
		this.recordReturn(secondMonitor, secondMonitor.getLinkedListSetOwner(),
				this.assetManager);
		this.recordReturn(secondMonitor, secondMonitor.getPrev(), null);
		failingMonitor.setNext(secondMonitor);
		secondMonitor.setPrev(failingMonitor);

		// Record copying the list of asset monitors
		this.recordReturn(secondMonitor, secondMonitor.getPrev(),
				failingMonitor);
		this.recordReturn(failingMonitor, failingMonitor.getPrev(), null);

		// Record checking on the failing asset
		this.recordReturn(failingMonitor, failingMonitor.getAsset(),
				failingAsset);
		failingAsset.checkOnAsset(this.assetManager);
		this.control(failingAsset).expectAndThrow(null, failure);
		failingMonitor.failJobNodes(this.activateSet, failure, false);

		// Record checking on the second asset
		this.recordReturn(secondMonitor, secondMonitor.getAsset(), secondAsset);
		secondAsset.checkOnAsset(this.assetManager);

		// Register monitor and handle exception from checking on asset
		this.replayMockObjects();
		this.assetManager.registerAssetMonitor(failingMonitor);
		this.assetManager.registerAssetMonitor(secondMonitor);
		this.assetManager.checkOnAssets(this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensures the time from {@link CheckAssetContext} is correct.
	 */
	public void testCheckTime() {

		// Record
		this.record_registerAssetMonitor_copyLinkedList();
		this.record_checkOnAsset();
		this.control(this.asset).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CheckAssetContext context = (CheckAssetContext) actual[0];

				// Ensure time is accurate
				long checkTime = context.getTime();
				assertTrue("Check time inaccurate (given ms margin)", ((System
						.currentTimeMillis() - checkTime) < 1));

				// Sleep some time to move time on
				try {
					Thread.sleep(10);
				} catch (Throwable ex) {
					TestCase.fail("Failed in moving time on: "
							+ ex.getMessage());
				}

				// Ensure time is still the same (for optimising)
				assertEquals("Incorrect time", checkTime, context.getTime());

				return true;
			}
		});

		// Register monitor and check on its asset that uses the time
		this.replayMockObjects();
		this.assetManager.registerAssetMonitor(this.monitor);
		this.assetManager.checkOnAssets(this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensures can activate {@link FunctionState} instances from the
	 * {@link CheckAssetContext}.
	 */
	public void testActivateJobNodesFromCheckAssetContext() {

		final boolean isPermanent = false;

		// Record
		this.record_registerAssetMonitor_copyLinkedList();
		this.record_checkOnAsset();
		this.control(this.asset).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CheckAssetContext context = (CheckAssetContext) actual[0];
				context.activateJobNodes(isPermanent);
				return true;
			}
		});
		this.monitor.activateJobNodes(this.activateSet, isPermanent);

		// Register monitor and check on its asset that activates jobs
		this.replayMockObjects();
		this.assetManager.registerAssetMonitor(this.monitor);
		this.assetManager.checkOnAssets(this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Ensures can fail {@link FunctionState} instances from the
	 * {@link CheckAssetContext}.
	 */
	public void testFailJobNodesFromCheckAssetContext() {

		final boolean isPermanent = true;
		final Exception failure = new Exception("Asset failure");

		// Record
		this.record_registerAssetMonitor_copyLinkedList();
		this.record_checkOnAsset();
		this.control(this.asset).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CheckAssetContext context = (CheckAssetContext) actual[0];
				context.failFunctions(failure, isPermanent);
				return true;
			}
		});
		this.monitor.failJobNodes(this.activateSet, failure, isPermanent);

		// Register monitor and check on its asset that activates jobs
		this.replayMockObjects();
		this.assetManager.registerAssetMonitor(this.monitor);
		this.assetManager.checkOnAssets(this.activateSet);
		this.verifyMockObjects();
	}

	/**
	 * Records registering the {@link AssetLatch}.
	 */
	private void record_registerAssetMonitor_copyLinkedList() {
		this.recordReturn(this.monitor, this.monitor.getLinkedListSetOwner(),
				this.assetManager);
		this.recordReturn(this.monitor, this.monitor.getPrev(), null);
		this.recordReturn(this.monitor, this.monitor.getPrev(), null);
	}

	/**
	 * Records checking on the {@link Asset}.
	 */
	private void record_checkOnAsset() {
		this.recordReturn(this.monitor, this.monitor.getAsset(), this.asset);
		this.asset.checkOnAsset(this.assetManager);
	}
}