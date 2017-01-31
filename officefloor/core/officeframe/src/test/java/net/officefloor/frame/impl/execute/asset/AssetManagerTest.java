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

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.officefloor.frame.impl.execute.job.FunctionLoopImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.office.OfficeManagerProcessState;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AssetLatch}.
 *
 * @author Daniel Sagenschneider
 */
public class AssetManagerTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeClock}.
	 */
	private final OfficeClock clock = this.createMock(OfficeClock.class);

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop loop = new FunctionLoopImpl(null);

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = new OfficeManagerProcessState(this.clock, this.loop);

	/**
	 * {@link AssetManager}.
	 */
	private final AssetManager assetManager = new AssetManagerImpl(this.processState, this.clock, this.loop);

	/**
	 * {@link Asset}.
	 */
	private final MockAsset asset = new MockAsset();

	/**
	 * {@link AssetLatch}.
	 */
	private final AssetLatch latch = this.assetManager.createAssetLatch(asset);

	/**
	 * Ensure can await on {@link AssetLatch}.
	 */
	public void testAwaitOnLatch() {
		MockFunctionState function = new MockFunctionState();
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.verifyMockObjects();
		assertFalse("Should have function awaiting", function.isExecuted);
	}

	/**
	 * Ensure can await and release.
	 */
	public void testReleaseLatch() {
		MockFunctionState function = new MockFunctionState();
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertFalse("Function should be awaiting", function.isExecuted);
		this.latch.releaseFunctions(false);
		this.verifyMockObjects();
		assertTrue("Should execute function as released", function.isExecuted);
	}

	/**
	 * Ensure can await, release then await, release again.
	 */
	public void testReleaseLatchTwice() {
		MockFunctionState first = new MockFunctionState();
		MockFunctionState second = new MockFunctionState();
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(first));
		assertFalse("Function should be awaiting", first.isExecuted);
		this.latch.releaseFunctions(false);
		assertTrue("Should execute function as released", first.isExecuted);
		this.doOperation(() -> this.latch.awaitOnAsset(second));
		assertFalse("Second function should be awaiting", second.isExecuted);
		this.latch.releaseFunctions(false);
		assertTrue("Should execute second function as released", second.isExecuted);
		this.verifyMockObjects();
	}

	/**
	 * Ensure permanently release.
	 */
	public void testPermanentlyReleaseLatch() throws Throwable {
		MockFunctionState function = new MockFunctionState();
		this.replayMockObjects();
		this.latch.releaseFunctions(true);
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertTrue("Should activate function immediately", function.isExecuted);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can await and fail.
	 */
	public void testFailLatch() {
		MockFunctionState function = new MockFunctionState();
		Exception failure = new Exception("TEST");
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertNull("Should be no failure", function.exception);
		this.latch.failFunctions(failure, false);
		assertSame("Should have failed the function", failure, function.exception);
		this.verifyMockObjects();
		assertFalse("Should not have executed the function", function.isExecuted);
	}

	/**
	 * Ensure can await, fail and await, fail again.
	 */
	public void testFailLatchTwice() {
		MockFunctionState first = new MockFunctionState();
		MockFunctionState second = new MockFunctionState();
		Exception failure = new Exception("TEST");
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(first));
		assertNull("Should be no failure for first function", first.exception);
		this.latch.failFunctions(failure, false);
		assertSame("Should have failed the first function", failure, first.exception);
		this.doOperation(() -> this.latch.awaitOnAsset(second));
		assertNull("Should be no failure for second function", second.exception);
		this.latch.failFunctions(failure, false);
		assertSame("Should have failed the second function", failure, second.exception);
		this.verifyMockObjects();
		assertFalse("Should not have executed the first function", first.isExecuted);
		assertFalse("Should not have executed the second function", second.isExecuted);
	}

	/**
	 * Ensure permanently fail.
	 */
	public void testPermanentlyFailLatch() {
		MockFunctionState function = new MockFunctionState();
		Exception failure = new Exception("TEST");
		this.replayMockObjects();
		this.latch.failFunctions(failure, true);
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertEquals("Should fail function immediately", failure, function.exception);
		this.verifyMockObjects();
	}

	/**
	 * Tests the {@link AssetManager} checking on no {@link Asset} instances.
	 */
	public void testCheckOnNoAssets() {
		this.replayMockObjects();
		this.doOperation(() -> this.assetManager);
		this.verifyMockObjects();
	}

	/**
	 * Tests the {@link AssetManager} checking on an {@link Asset}.
	 */
	public void testCheckOnAsset() {
		MockFunctionState function = new MockFunctionState();
		this.asset.check = (context) -> {
		};
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.verifyMockObjects();
		assertFalse("Function should not be executed", function.isExecuted);
		assertNull("Function should not be failed", function.exception);
	}

	/**
	 * Ensures the time from {@link CheckAssetContext} is correct.
	 */
	public void testCheckTime() {
		long currentTime = System.currentTimeMillis();
		this.recordReturn(this.clock, this.clock.currentTimeMillis(), currentTime);
		MockFunctionState function = new MockFunctionState();
		this.asset.check = (context) -> {
			assertEquals("Incorrect time", currentTime, context.getTime());
		};
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.verifyMockObjects();
		assertFalse("Function should not be executed", function.isExecuted);
		assertNull("Function should not be failed", function.exception);
	}

	/**
	 * Tests the {@link AssetManager} timing out the {@link Asset}.
	 */
	public void testTimeoutAsset() {
		MockFunctionState function = new MockFunctionState();
		Exception failure = new Exception("TIMEOUT");
		this.asset.check = (context) -> {
			context.failFunctions(failure, false);
		};
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.verifyMockObjects();
		assertFalse("Function should not be executed", function.isExecuted);
		assertSame("Function should be failed", failure, function.exception);
	}

	/**
	 * Tests the {@link Asset} throwing an {@link Exception} on being checked.
	 */
	public void testHandleCheckOnAssetThrowingException() {
		MockFunctionState function = new MockFunctionState();
		RuntimeException failure = new RuntimeException("TEST");
		this.asset.check = (context) -> {
			throw failure;
		};
		this.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.verifyMockObjects();
		assertFalse("Function should not be executed", function.isExecuted);
		assertSame("Function should be failed", failure, function.exception);
	}

	/**
	 * Undertakes the operation.
	 * 
	 * @param operation
	 *            Operation.
	 */
	private void doOperation(Supplier<FunctionState> operation) {
		FunctionState operationFunction = operation.get();
		this.loop.executeFunction(operationFunction);
	}

	/**
	 * Mock {@link Asset}.
	 */
	private class MockAsset implements Asset {

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState = AssetManagerTest.this.createMock(ThreadState.class);

		/**
		 * {@link Consumer} to check on the {@link Asset}.
		 */
		private Consumer<CheckAssetContext> check;

		/*
		 * ======================== Asset ====================================
		 */

		@Override
		public ThreadState getOwningThreadState() {
			return this.threadState;
		}

		@Override
		public void checkOnAsset(CheckAssetContext context) {
			this.check.accept(context);
		}
	}

	/**
	 * Mock {@link FunctionState}.
	 */
	private class MockFunctionState extends AbstractLinkedListSetEntry<FunctionState, Flow> implements FunctionState {

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState = AssetManagerTest.this.createMock(ThreadState.class);

		/**
		 * Indicates if executed.
		 */
		private boolean isExecuted = false;

		/**
		 * {@link Throwable}.
		 */
		private Throwable exception = null;

		/*
		 * ======================= FunctionState ==============================
		 */

		@Override
		public ThreadState getThreadState() {
			return this.threadState;
		}

		@Override
		public FunctionState execute() throws Throwable {
			this.isExecuted = true;
			return null;
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation) {
			this.exception = escalation;
			return null;
		}
	}

}