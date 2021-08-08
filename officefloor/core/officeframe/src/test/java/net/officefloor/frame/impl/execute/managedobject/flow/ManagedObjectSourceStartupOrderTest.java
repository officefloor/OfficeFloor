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

package net.officefloor.frame.impl.execute.managedobject.flow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.ConstructTestSupport;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;
import net.officefloor.frame.test.ThreadedTestSupport.MultiThreadedExecution;

/**
 * Ensure can order the start up of {@link ManagedObjectSource} instances.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ManagedObjectSourceStartupOrderTest {

	private final ConstructTestSupport construct = new ConstructTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	private final MockTestSupport mocks = new MockTestSupport();

	/**
	 * Ensure issue if unknown before {@link ManagedObjectSource}.
	 */
	@Test
	public void unknownBeforeManagedObjectSource() throws Throwable {

		// Record issue if unknown before managed object source
		OfficeFloorIssues issues = this.mocks.createMock(OfficeFloorIssues.class);
		issues.addIssue(AssetType.MANAGED_OBJECT, "of-ONE", "Unknown ManagedObjectSource 'TWO' to start up before");

		// Should fail compile
		this.mocks.replayMockObjects();
		this.construct
				.constructManagedObject("ONE", new MockStartupManagedObjectSource(true), this.construct.getOfficeName())
				.startupBefore("TWO");
		ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
		function.buildObject("ONE", ManagedObjectScope.THREAD);
		assertNull(this.construct.getOfficeFloorBuilder().buildOfficeFloor(issues), "Should not construct OfficeFloor");
		this.mocks.verifyMockObjects();
	}

	public static class MockOneMoFunction {
		public void function(MockStartupManagedObjectSource mo) {
			// Test method
		}
	}

	/**
	 * Ensure issue if unknown after {@link ManagedObjectSource}.
	 */
	@Test
	public void unknownAfterManagedObjectSource() throws Throwable {

		// Record issue if unknown after managed object source
		OfficeFloorIssues issues = this.mocks.createMock(OfficeFloorIssues.class);
		issues.addIssue(AssetType.MANAGED_OBJECT, "of-ONE", "Unknown ManagedObjectSource 'TWO' to start up after");

		// Should fail compile
		this.mocks.replayMockObjects();
		this.construct
				.constructManagedObject("ONE", new MockStartupManagedObjectSource(true), this.construct.getOfficeName())
				.startupAfter("TWO");
		ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
		function.buildObject("ONE", ManagedObjectScope.THREAD);
		assertNull(this.construct.getOfficeFloorBuilder().buildOfficeFloor(issues), "Should not construct OfficeFloor");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure able to start up in any order.
	 */
	@Test
	public void startupInAnyOrder() throws Throwable {

		// Complete start up immediately
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(true);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(true);

		// Should open immediately (without blocking)
		this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName());
		this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName());
		ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
		function.buildObject("ONE", ManagedObjectScope.THREAD);
		function.buildObject("TWO", ManagedObjectScope.THREAD);
		try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Should have started both
			assertTrue(one.isStarted, "Should have started first");
			assertTrue(two.isStarted, "Should have started second");
		}
	}

	public static class MockTwoMoFunction {
		public void function(MockStartupManagedObjectSource one, MockStartupManagedObjectSource two) {
			// Test method
		}
	}

	/**
	 * Wait on start up in any order.
	 */
	@Test
	public void waitOnStartupAnyOrder() throws Throwable {

		// Delay the start up
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName());
			this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName());
			ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
			function.buildObject("ONE", ManagedObjectScope.THREAD);
			function.buildObject("TWO", ManagedObjectScope.THREAD);
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// Both should start up immediately
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && (one.isStarted) && (two.isStarted));

		// Should also allow opening OfficeFloor
		one.startup.complete();
		two.startup.complete();
		execution.waitForCompletion();
	}

	/**
	 * Ensure before ordering of start up is respected.
	 */
	@Test
	public void startupBeforeOrdering() throws Throwable {

		// Delay the start up
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName());
			this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName()).startupBefore("of-ONE");
			ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
			function.buildObject("ONE", ManagedObjectScope.THREAD);
			function.buildObject("TWO", ManagedObjectScope.THREAD);
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// One should not be started
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && (!one.isStarted) && (two.isStarted));

		// Complete two and should start one
		two.startup.complete();
		this.threading.waitForTrue(() -> one.isStarted);

		// Complete one and should then open
		one.startup.complete();
		execution.waitForCompletion();
	}

	/**
	 * Ensure after ordering of start up is respected.
	 */
	@Test
	public void startupAfterOrdering() throws Throwable {

		// Delay the start up
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName()).startupAfter("of-TWO");
			this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName());
			ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
			function.buildObject("ONE", ManagedObjectScope.THREAD);
			function.buildObject("TWO", ManagedObjectScope.THREAD);
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// One should not be started
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && (!one.isStarted) && (two.isStarted));

		// Complete two and should start one
		two.startup.complete();
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && one.isStarted);

		// Complete one and should then open
		one.startup.complete();
		execution.waitForCompletion();
	}

	/**
	 * Ensure can have multiple orderings.
	 */
	@Test
	public void multipleOrderings() throws Throwable {

		// Delay the start up
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource three = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName()).startupAfter("of-TWO");
			this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName()).startupBefore("of-ONE");
			this.construct.constructManagedObject("THREE", three, this.construct.getOfficeName())
					.startupBefore("of-TWO");
			ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockThreeMoFunction(),
					"function");
			function.buildObject("ONE", ManagedObjectScope.THREAD);
			function.buildObject("TWO", ManagedObjectScope.THREAD);
			function.buildObject("THREE", ManagedObjectScope.THREAD);
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// Three should be started first
		this.threading.waitForTrue(
				() -> (!execution.isErrorAndThrow()) && (!one.isStarted) && (!two.isStarted) && (three.isStarted));

		// On three completing, two should be started next
		three.startup.complete();
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && (!one.isStarted) && (two.isStarted));

		// On two completing, one should be started next
		two.startup.complete();
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && one.isStarted);

		// Complete one and should then open
		one.startup.complete();
		execution.waitForCompletion();
	}

	public static class MockThreeMoFunction {
		public void function(MockStartupManagedObjectSource one, MockStartupManagedObjectSource two,
				MockStartupManagedObjectSource three) {
			// Test method
		}
	}

	/**
	 * Ensure group into parallel starts for faster loading.
	 */
	@Test
	public void parallelGrouping() throws Throwable {

		// Delay the start up
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource three = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource four = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName());
			this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName()).startupBefore("of-ONE");
			this.construct.constructManagedObject("THREE", three, this.construct.getOfficeName())
					.startupBefore("of-ONE");
			this.construct.constructManagedObject("FOUR", four, this.construct.getOfficeName()).startupAfter("of-TWO");
			ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockFourMoFunction(), "function");
			function.buildObject("ONE", ManagedObjectScope.THREAD);
			function.buildObject("TWO", ManagedObjectScope.THREAD);
			function.buildObject("THREE", ManagedObjectScope.THREAD);
			function.buildObject("FOUR", ManagedObjectScope.THREAD);
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// Two and Three grouped to start
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && (!one.isStarted) && (two.isStarted)
				&& (three.isStarted) && (!four.isStarted));

		// On two and three completing, remaining started in group
		two.startup.complete();
		three.startup.complete();
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && (one.isStarted) && (four.isStarted));

		// Complete remaining and should then open
		one.startup.complete();
		four.startup.complete();
		execution.waitForCompletion();
	}

	public static class MockFourMoFunction {
		public void function(MockStartupManagedObjectSource one, MockStartupManagedObjectSource two,
				MockStartupManagedObjectSource three, MockStartupManagedObjectSource four) {
			// Test method
		}
	}

	/**
	 * Ensure fail to compile if cyclic start up ordering.
	 */
	@Test
	public void cyclicOrder() throws Throwable {

		// Record issue if unknown before managed object source
		OfficeFloorIssues issues = this.mocks.createMock(OfficeFloorIssues.class);
		issues.addIssue(AssetType.OFFICE_FLOOR, this.construct.getOfficeFloorName(),
				"Cycle in ManagedObjectSource start up (of-ONE, of-THREE)");

		// Complete start up immediately
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(true);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(true);
		MockStartupManagedObjectSource three = new MockStartupManagedObjectSource(true);

		// Setup cyclic start up
		this.mocks.replayMockObjects();
		this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName()).startupBefore("of-TWO");
		this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName()).startupBefore("of-THREE");
		this.construct.constructManagedObject("THREE", three, this.construct.getOfficeName()).startupBefore("of-ONE");
		assertNull(this.construct.getOfficeFloorBuilder().buildOfficeFloor(issues), "Should not construct OfficeFloor");
		this.mocks.verifyMockObjects();
	}

	/**
	 * Ensure no further starting on start up failure.
	 */
	@Test
	public void noFurtherStartupsOnImmediateFailure() throws Throwable {

		// Fail first on start up immediately
		Exception failure = new Exception("TEST");
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(failure);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(true);

		// Should open immediately (without blocking)
		this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName()).startupBefore("of-TWO");
		this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName());
		ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
		function.buildObject("ONE", ManagedObjectScope.THREAD);
		function.buildObject("TWO", ManagedObjectScope.THREAD);
		try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {

			// Should fail to start up
			try {
				officeFloor.openOfficeFloor();
				fail("Should not successfully start up");
			} catch (Exception ex) {
				assertSame(failure, ex, "Incorrect start up failure");
			}

			// Only first should be started (as it failed)
			assertTrue(one.isStarted, "Should have started first, as it failed");
			assertFalse(two.isStarted, "Second should not have been started");

			// Both should however be stopped
			assertTrue(one.isStopped, "First should be stopped");
			assertTrue(two.isStopped, "Second should also be stopped");
		}
	}

	/**
	 * Ensure no further starting on start up failure.
	 */
	@Test
	public void noFurtherStartupsOnDelayedFailure() throws Throwable {

		// Delay the start up failure
		Exception failure = new Exception("TEST");
		MockStartupManagedObjectSource one = new MockStartupManagedObjectSource(false);
		MockStartupManagedObjectSource two = new MockStartupManagedObjectSource(false);

		// Construct OfficeFloor in another thread as blocks
		MultiThreadedExecution<?> execution = this.threading.triggerThreadedTest(() -> {
			this.construct.constructManagedObject("ONE", one, this.construct.getOfficeName()).startupBefore("of-TWO");
			this.construct.constructManagedObject("TWO", two, this.construct.getOfficeName());
			ReflectiveFunctionBuilder function = this.construct.constructFunction(new MockTwoMoFunction(), "function");
			function.buildObject("ONE", ManagedObjectScope.THREAD);
			function.buildObject("TWO", ManagedObjectScope.THREAD);
			try (OfficeFloor officeFloor = this.construct.constructOfficeFloor()) {
				officeFloor.openOfficeFloor();
			}
		});

		// Only first should be started
		this.threading.waitForTrue(() -> (!execution.isErrorAndThrow()) && (one.isStarted) && (!two.isStarted));

		// Fail first and should complete open
		one.startup.failOpen(failure);
		try {
			execution.waitForCompletion();
			fail("Should not successfully complete");
		} catch (Exception ex) {
			assertSame(failure, ex, "Incorrect cause of open failure");
		}

		// Only first should be started (as it failed)
		assertTrue(one.isStarted, "Should have started first, as it failed");
		assertFalse(two.isStarted, "Second should not have been started");

		// Both should however be stopped
		assertTrue(one.isStopped, "First should be stopped");
		assertTrue(two.isStopped, "Second should also be stopped");
	}

	/**
	 * Mock {@link ManagedObjectSource} to test start up logic.
	 */
	@TestSource
	private static class MockStartupManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject, ManagedFunction<None, None> {

		private final boolean isCompleteImmeidately;

		private final Exception immediateFailure;

		private volatile ManagedObjectStartupCompletion startup;

		private volatile boolean isStarted = false;

		private volatile boolean isStopped = false;

		private MockStartupManagedObjectSource(boolean isCompleteImmediately) {
			this.isCompleteImmeidately = isCompleteImmediately;
			this.immediateFailure = null;
		}

		private MockStartupManagedObjectSource(Exception immediateFailure) {
			this.isCompleteImmeidately = false;
			this.immediateFailure = immediateFailure;
		}

		/*
		 * ==================== ManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

			// Provide meta-data
			context.setObjectClass(this.getClass());

			// Create the startup completion
			this.startup = mosContext.createStartupCompletion();

			// Add the start up function
			mosContext.addManagedFunction("STARTUP", () -> this);
			mosContext.addStartupFunction("STARTUP", null);
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {

			// Determine if complete/fail immediately
			if (this.isCompleteImmeidately) {
				this.startup.complete();
			}
			if (this.immediateFailure != null) {
				this.startup.failOpen(this.immediateFailure);
			}
		}

		@Override
		public void stop() {
			this.isStopped = true;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ======================== ManagedObject ========================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * ======================= ManagedFunction =======================
		 */

		@Override
		public void execute(ManagedFunctionContext<None, None> context) throws Throwable {
			this.isStarted = true;
		}
	}

}
