/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.impl.execute.job.FunctionLoopImpl;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.mocks.MockUtil;

/**
 * Tests the {@link AssetLatch}.
 *
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class AssetManagerTest {

	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * {@link MonitorClock}.
	 */
	private MonitorClock clock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop loop = new FunctionLoopImpl(null);

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = MockUtil.createProcessState();

	/**
	 * {@link AssetManager}.
	 */
	private AssetManager assetManager;

	/**
	 * {@link Asset}.
	 */
	private MockAsset asset;

	/**
	 * {@link AssetLatch}.
	 */
	private AssetLatch latch;

	@BeforeEach
	public void setup() {
		this.clock = this.mocks.createMock(MonitorClock.class);
		this.assetManager = new AssetManagerImpl(this.processState, this.clock, this.loop);
		this.asset = new MockAsset();
		this.latch = this.assetManager.createAssetLatch(this.asset);
	}

	/**
	 * Ensure can await on {@link AssetLatch}.
	 */
	@Test
	public void awaitOnLatch() {
		MockFunctionState function = new MockFunctionState();
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.mocks.verifyMockObjects();
		assertFalse(function.isExecuted, "Should have function awaiting");
	}

	/**
	 * Ensure can await and release.
	 */
	@Test
	public void releaseLatch() {
		MockFunctionState function = new MockFunctionState();
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertFalse(function.isExecuted, "Function should be awaiting");
		this.latch.releaseFunctions(false);
		this.mocks.verifyMockObjects();
		assertTrue(function.isExecuted, "Should execute function as released");
	}

	/**
	 * Ensure can await, release then await, release again.
	 */
	@Test
	public void releaseLatchTwice() {
		MockFunctionState first = new MockFunctionState();
		MockFunctionState second = new MockFunctionState();
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(first));
		assertFalse(first.isExecuted, "Function should be awaiting");
		this.latch.releaseFunctions(false);
		assertTrue(first.isExecuted, "Should execute function as released");
		this.doOperation(() -> this.latch.awaitOnAsset(second));
		assertFalse(second.isExecuted, "Second function should be awaiting");
		this.latch.releaseFunctions(false);
		assertTrue(second.isExecuted, "Should execute second function as released");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can providing additional {@link FunctionState} on completion.
	 */
	@Test
	public void releaseLatchWithFunctionState() {
		MockFunctionState waiting = new MockFunctionState();
		MockFunctionState additional = new MockFunctionState();
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(waiting));
		assertFalse(waiting.isExecuted, "Function should be waiting");
		this.latch.releaseFunctions(true, additional);
		this.mocks.verifyMockObjects();
		assertTrue(additional.isExecuted, "Should execute additional");
		assertTrue(waiting.isExecuted, "Should execute waiting");
	}

	/**
	 * Ensure permanently release.
	 */
	@Test
	public void permanentlyReleaseLatch() throws Throwable {
		MockFunctionState function = new MockFunctionState();
		this.mocks.replayMockObjects();
		this.latch.releaseFunctions(true);
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertTrue(function.isExecuted, "Should activate function immediately");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure can await and fail.
	 */
	@Test
	public void failLatch() {
		MockFunctionState function = new MockFunctionState();
		Exception failure = new Exception("TEST");
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertNull(function.exception, "Should be no failure");
		this.latch.failFunctions(failure, false);
		assertSame(failure, function.exception, "Should have failed the function");
		this.mocks.verifyMockObjects();
		assertFalse(function.isExecuted, "Should not have executed the function");
	}

	/**
	 * Ensure can await, fail and await, fail again.
	 */
	@Test
	public void failLatchTwice() {
		MockFunctionState first = new MockFunctionState();
		MockFunctionState second = new MockFunctionState();
		Exception failure = new Exception("TEST");
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(first));
		assertNull(first.exception, "Should be no failure for first function");
		this.latch.failFunctions(failure, false);
		assertSame(failure, first.exception, "Should have failed the first function");
		this.doOperation(() -> this.latch.awaitOnAsset(second));
		assertNull(second.exception, "Should be no failure for second function");
		this.latch.failFunctions(failure, false);
		assertSame(failure, second.exception, "Should have failed the second function");
		this.mocks.verifyMockObjects();
		assertFalse(first.isExecuted, "Should not have executed the first function");
		assertFalse(second.isExecuted, "Should not have executed the second function");
	}

	/**
	 * Ensure permanently fail.
	 */
	@Test
	public void permanentlyFailLatch() {
		MockFunctionState function = new MockFunctionState();
		Exception failure = new Exception("TEST");
		this.mocks.replayMockObjects();
		this.latch.failFunctions(failure, true);
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		assertSame(failure, function.exception, "Should fail function immediately");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Tests the {@link AssetManager} checking on no {@link Asset} instances.
	 */
	@Test
	public void checkOnNoAssets() {
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.assetManager);
		this.mocks.verifyMockObjects();
	}

	/**
	 * Tests the {@link AssetManager} checking on an {@link Asset}.
	 */
	@Test
	public void checkOnAsset() {
		MockFunctionState function = new MockFunctionState();
		this.asset.check = (context) -> {
		};
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.mocks.verifyMockObjects();
		assertFalse(function.isExecuted, "Function should not be executed");
		assertNull(function.exception, "Function should not be failed");
	}

	/**
	 * Ensures the time from {@link CheckAssetContext} is correct.
	 */
	@Test
	public void checkTime() {
		long currentTime = System.currentTimeMillis();
		this.mocks.recordReturn(this.clock, this.clock.currentTimeMillis(), currentTime);
		MockFunctionState function = new MockFunctionState();
		this.asset.check = (context) -> {
			assertEquals(currentTime, context.getTime(), "Incorrect time");
		};
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.mocks.verifyMockObjects();
		assertFalse(function.isExecuted, "Function should not be executed");
		assertNull(function.exception, "Function should not be failed");
	}

	/**
	 * Tests the {@link AssetManager} timing out the {@link Asset}.
	 */
	@Test
	public void timeoutAsset() {
		MockFunctionState function = new MockFunctionState();
		Exception failure = new Exception("TIMEOUT");
		this.asset.check = (context) -> {
			context.failFunctions(failure, false);
		};
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.mocks.verifyMockObjects();
		assertFalse(function.isExecuted, "Function should not be executed");
		assertSame(failure, function.exception, "Function should be failed");
	}

	/**
	 * Tests the {@link Asset} throwing an {@link Exception} on being checked.
	 */
	@Test
	public void handleCheckOnAssetThrowingException() {
		MockFunctionState function = new MockFunctionState();
		RuntimeException failure = new RuntimeException("TEST");
		this.asset.check = (context) -> {
			throw failure;
		};
		this.mocks.replayMockObjects();
		this.doOperation(() -> this.latch.awaitOnAsset(function));
		this.doOperation(() -> this.assetManager);
		this.mocks.verifyMockObjects();
		assertFalse(function.isExecuted, "Function should not be executed");
		assertSame(failure, function.exception, "Function should be failed");
	}

	/**
	 * Undertakes the operation.
	 * 
	 * @param operation Operation.
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
		private final ThreadState threadState = AssetManagerTest.this.processState.getMainThreadState();

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
		private final ThreadState threadState = AssetManagerTest.this.processState.getMainThreadState();

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
		public FunctionState execute(FunctionStateContext context) throws Throwable {
			this.isExecuted = true;
			return null;
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
			this.exception = escalation;
			return null;
		}
	}

}
