/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.managedobject.poll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.plugin.managedobject.poll.StatePoller.Builder;
import net.officefloor.plugin.managedobject.poll.StatePoller.Poller;

/**
 * Tests the {@link StatePoller}.
 * 
 * @author Daniel Sagenschneider
 */
public class StatePollerTest implements ManagedObjectExecuteContext<StatePollerTest.Flows>,
		ManagedObjectServiceContext<StatePollerTest.Flows> {

	/**
	 * Default poll milliseconds.
	 */
	private static long defaultMilliseconds = TimeUnit.HOURS.toMillis(1);

	/**
	 * Ensure {@link IllegalArgumentException} thrown if incorrect configuration of
	 * {@link StatePoller}.
	 */
	@Test
	public void protectPollerOfInvalidSetup() throws Exception {
		assertIllegalArgument("Must provide flow key",
				() -> StatePoller.builder(String.class, (Flows) null, null, null));
		assertIllegalArgument("Must provide valid flow index (provided -1)",
				() -> StatePoller.builder(String.class, -1, null, null));
		assertIllegalArgument("Must provide state type", () -> StatePoller.builder(null, Flows.DO_FLOW, null, null));
		assertIllegalArgument("Must provide ManagedObjectExecuteContext",
				() -> StatePoller.builder(String.class, Flows.DO_FLOW, null, null));
		assertIllegalArgument("Must provide ManagedObject factory",
				() -> StatePoller.builder(String.class, Flows.DO_FLOW, this, null));
		Builder<String, Flows> builder = StatePoller.builder(String.class, Flows.DO_FLOW, this,
				(context) -> new MockManagedObject(context));
		assertIllegalArgument(
				"Poll interval of -1 milliseconds will result in -1 milliseconds. Must be at least 1 millisecond.",
				() -> builder.defaultPollInterval(-1, null));
		assertIllegalArgument(
				"Poll interval of 0 milliseconds will result in 0 milliseconds. Must be at least 1 millisecond.",
				() -> builder.defaultPollInterval(0, null));
		assertIllegalArgument(
				"Poll interval of 900 microseconds will result in 0 milliseconds. Must be at least 1 millisecond.",
				() -> builder.defaultPollInterval(900, TimeUnit.MICROSECONDS));
		builder.defaultPollInterval(1, null);
		builder.defaultPollInterval(1, TimeUnit.DAYS);
		builder.successLogLevel(Level.FINE);
		builder.successLogLevel(null);
		builder.parameter((context) -> new MockParameter(context));
		builder.parameter(null);
		assertNotNull(builder.build(), "Should build");
	}

	private static void assertIllegalArgument(String message, Runnable runnable) {
		try {
			runnable.run();
			fail("Should trigger " + IllegalArgumentException.class.getSimpleName() + " with cause " + message);
		} catch (IllegalArgumentException ex) {
			assertEquals(message, ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure polling triggered at start up.
	 */
	@Test
	public void immediateStartupProcess() throws Exception {
		InvokedProcess process = this.startupProcess();
		assertNull(process.managedObject.pollContext.getCurrentState(), "Should not have current state");
		process.parameter.pollContext.setNextState("TEST", 1, TimeUnit.HOURS);
		assertEquals("TEST", this.poller.getState(10, TimeUnit.MILLISECONDS), "Incorrect startup state");
		this.assertLogs("Startup Process", "Next poll in ");
		assertTrue(process.isConcurrent, "Should be concurrent");
	}

	/**
	 * Ensure blocks waiting on initial value.
	 */
	@Test
	public void delayedStartupState() throws Exception {
		InvokedProcess process = this.startupProcess();
		Supplier<String> delayed = this.triggerGetState(10, TimeUnit.MILLISECONDS);
		process.parameter.pollContext.setNextState("TEST", 1, TimeUnit.HOURS);
		assertEquals("TEST", delayed.get(), "Incorrect delayed startup state");
		this.assertLogs("Delayed Startup Process", "Next poll in ");
	}

	/**
	 * Ensure blocks until successfully initialised.
	 */
	@Test
	public void failedStartup() throws Throwable {
		InvokedProcess process = this.startupProcess();
		assertNull(this.poller.getStateNow(), "Should be no state");
		Supplier<String> delayed = this.triggerGetState(1, TimeUnit.SECONDS);
		for (int i = 1; i < 10; i++) {

			// Indicate error from process
			process.parameter.pollContext.setFailure(new IOException("TEST FAILURE"), 100, TimeUnit.MILLISECONDS);
			this.assertLogs("Should have log message of failure for " + i, IOException.class.getName(), "TEST FAILURE",
					"Next poll in 100 milliseconds (approx ");
			assertNull(this.poller.getStateNow(), "Should be no startup state after failure " + i);

			// Complete the process (successfully)
			process.callback.run(null);

			// Obtain the process
			process = this.nextInvokedProcess(i, 100);
		}
		process.parameter.pollContext.setNextState("TEST", 1, TimeUnit.HOURS);
		assertEquals("TEST", delayed.get(), "Should now retrieve state");
	}

	/**
	 * Ensures continues to poll even if callbacks were not called.
	 */
	@Test
	public void noFeedback() throws Throwable {
		InvokedProcess process = this.startupProcess();
		assertNull(this.poller.getStateNow(), "Should be no state");
		Supplier<String> delayed = this.triggerGetState(1, TimeUnit.SECONDS);
		for (int i = 1; i < 10; i++) {

			// Process completes with error (failing to provide feedback)
			process.callback.run(new IOException("TEST FAILURE"));
			this.assertLogs("Should be logs for process error " + i, "Poll process failed", IOException.class.getName(),
					"TEST FAILURE", "Next poll in (default) " + defaultMilliseconds + " milliseconds (approx ");

			// Obtain the process
			process = this.nextInvokedProcess(i, defaultMilliseconds);
		}
		for (int i = 10; i < 20; i++) {

			// Indicate no error from process
			process.callback.run(null);
			this.assertLogs("Should be logs for no feedback " + i, "Poll process completed without providing state",
					"Next poll in (default) " + defaultMilliseconds + " milliseconds (approx ");

			// Obtain the process
			process = this.nextInvokedProcess(i, defaultMilliseconds);
		}
		process.parameter.pollContext.setNextState("TEST", 1, TimeUnit.HOURS);
		assertEquals("TEST", delayed.get(), "Should now retrieve state");
	}

	/**
	 * Ensure continues to provide old state until new state overwrites.
	 */
	@Test
	public void oldStateUntilRefreshed() throws Throwable {
		InvokedProcess process = this.startupProcess();
		final String STATE = "TEST";
		process.parameter.pollContext.setNextState(STATE, 1, TimeUnit.DAYS);
		assertSame(STATE, this.poller.getStateNow(), "Incorrect startup state");
		assertSame(STATE, this.poller.getState(1, TimeUnit.NANOSECONDS),
				"Incorrect startup state (immediately return)");
		process = this.nextInvokedProcess(1, TimeUnit.DAYS.toMillis(1));
		for (int i = 2; i < 10; i++) {

			// Failing to provide state
			process.parameter.pollContext.setFailure(new IOException("TEST FAILURE"), -1, null);
			this.assertLogs("Fail refresh " + i, IOException.class.getName(), "TEST FAILURE",
					"Next poll in (default) " + defaultMilliseconds + " milliseconds (approx ");

			// Ensure continue to be same value
			assertSame(STATE, this.poller.getStateNow(), "Incorrect startup state");
			assertSame(STATE, this.poller.getState(1, TimeUnit.NANOSECONDS),
					"Incorrect startup state (immediately return)");

			// Obtain the process
			process = this.nextInvokedProcess(i, defaultMilliseconds);
		}
		for (int i = 10; i < 20; i++) {

			// No feedback
			process.callback.run(null);

			// Ensure continue to be same value
			assertSame(STATE, this.poller.getStateNow(), "Incorrect startup state");
			assertSame(STATE, this.poller.getState(1, TimeUnit.NANOSECONDS),
					"Incorrect startup state (immediately return)");

			// Obtain the process
			process = this.nextInvokedProcess(i, defaultMilliseconds);
		}
		process.parameter.pollContext.setNextState("CHANGED", -1, null);
		assertEquals("CHANGED", this.poller.getStateNow(), "Should now update state");
		assertSame("CHANGED", this.poller.getState(1, TimeUnit.NANOSECONDS),
				"Should now update state (immediately return)");
	}

	/**
	 * Ensure refresh on successful polls.
	 */
	@Test
	public void updatingState() throws Throwable {
		InvokedProcess process = this.startupProcess();
		assertNull(process.managedObject.pollContext.getCurrentState(), "Should not have current state");
		for (int i = 1; i < 10; i++) {

			// Update state
			process.parameter.pollContext.setNextState("Process-" + i, 1, TimeUnit.MINUTES);
			long minuteMilliseconds = 60 * 1000;
			this.assertLogs("Update state " + i, "Next poll in " + minuteMilliseconds + " milliseconds (approx ");

			// Ensure updated state
			assertEquals("Process-" + i, this.poller.getStateNow(), "Incorrect state");
			assertEquals("Process-" + i, this.poller.getState(0, TimeUnit.MILLISECONDS),
					"Incorrect state (immediately return)");

			// Obtain the process
			process = this.nextInvokedProcess(i, minuteMilliseconds);
			assertEquals("Process-" + i, process.managedObject.pollContext.getCurrentState(),
					"Should have current state");
		}

		// Ensure can use default time
		process.parameter.pollContext.setNextState("Default", -1, null);
		long hourMilliseconds = 60 * 60 * 1000;
		this.assertLogs("Default poll", "Next poll in (default) " + hourMilliseconds + " milliseconds (approx ");
		assertEquals("Default", this.poller.getStateNow(), "Incorrect state");
		assertEquals("Default", this.poller.getState(0, TimeUnit.MILLISECONDS), "Incorrect state (immediately return)");
	}

	/**
	 * Ensure can use {@link Flow} index.
	 */
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void flowIndex() throws Throwable {

		// Transform to managed object execute context
		ManagedObjectExecuteContext<Indexed> indexedContext = (ManagedObjectExecuteContext) this;

		// Create the poller
		StatePoller<Long, Indexed> poller = StatePoller
				.builder(Long.class, 1, indexedContext, (context) -> new MockManagedObject((StatePollContext) context))
				.logger(this.logger).build();
		this.poller = (StatePoller) poller;

		// Validate the startup process
		InvokedProcess process = this.invokedProcesses.remove();
		assertEquals(Integer.valueOf(1), process.index, "Incorrect flow index");
		assertNull(poller.getStateNow(), "Should be no intial value");
		Supplier<String> delayed = this.triggerGetState(1, TimeUnit.SECONDS);

		// Ensure can invoke process with flow index
		StatePollContext pollContext = (StatePollContext) process.managedObject.pollContext;
		pollContext.setNextState(Long.valueOf(2), -1, null);
		process = this.invokedProcesses.remove();
		assertEquals(Long.valueOf(2), poller.getStateNow(), "Should have value");
		assertEquals(Long.valueOf(2), delayed.get(), "Should have delayed value");
	}

	/**
	 * Ensure issue if invalid state.
	 */
	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void invalidState() throws Throwable {
		InvokedProcess process = this.startupProcess();
		StatePollContext context = process.managedObject.pollContext;
		try {
			context.setNextState(Long.valueOf(1), -1, null);
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Invalid state type " + Long.class.getName() + " (required " + String.class.getName() + ")",
					ex.getMessage(), "Incorrect cause");
		}
	}

	/**
	 * Ensure can provide <code>null</code> parameter.
	 */
	@Test
	public void noParameter() {
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.logger(this.logger).build();
		InvokedProcess process = this.invokedProcesses.remove();
		assertNull(process.parameter, "Should be no startup parameter");
		process.managedObject.pollContext.setNextState("STATE", -1, null);
		process = this.invokedProcesses.remove();
		assertNull(process.parameter, "Should be no poll parameter");
	}

	/**
	 * Ensure can override the success log {@link Level}.
	 */
	@Test
	public void successLogLevel() {
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.successLogLevel(Level.FINEST).logger(this.logger).build();
		InvokedProcess process = this.invokedProcesses.remove();
		process.managedObject.pollContext.setNextState("STATE", -1, null);
		this.assertLogs("Poll", "FINEST");
	}

	/**
	 * Ensure can override the default poll interval.
	 */
	@Test
	public void defaultPollInterval() {
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.defaultPollInterval(30, TimeUnit.MINUTES).logger(this.logger).build();
		InvokedProcess process = this.invokedProcesses.remove();
		process.managedObject.pollContext.setNextState("DIFFERENT DEFAULT POLL INTERVAL", -1, null);
		long thirtyMinutes = 30 * 60 * 1000;
		this.assertLogs("Different efault poll interval", "Next poll in (default) " + thirtyMinutes + " milliseconds");
	}

	/**
	 * Ensure can decorate the {@link ManagedObjectStartupProcess}.
	 */
	@Test
	public void decorateStartupProcess() {
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.startup((startupProcess) -> startupProcess.setConcurrent(true)).build();
		InvokedProcess process = this.invokedProcesses.remove();
		assertTrue(process.isConcurrent, "Should be concurrently started");
	}

	/**
	 * Ensure can use custom {@link Poller}.
	 */
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void customPoller() {

		// Create with custom poller
		Builder builder = StatePoller.builder(String.class, (context, callback) -> {
			ManagedObjectStartupProcess startup = this.invokeStartupProcess(Flows.DO_FLOW, new MockParameter(context),
					new MockManagedObject(context), callback);
			startup.setConcurrent(true);
		}, (delay, context, callback) -> {
			this.invokeProcess(Flows.DO_FLOW, new MockParameter(context), new MockManagedObject(context), delay,
					callback);
		}).logger(this.logger);

		// Ensure can not set parameter factory (as custom poller)
		assertIllegalArgument("Custom Poller used so may not configure parameter factory",
				() -> builder.parameter((context) -> null));

		// Ensure can continue to poll
		this.poller = (StatePoller) builder.build();
		InvokedProcess process = this.invokedProcesses.remove();
		assertEquals(0, process.parameter.id, "Should be start up process");
		assertEquals(0, process.delay, "Should be immediate start");
		assertTrue(process.isConcurrent, "Should be concurrent start up");
		process.parameter.pollContext.setNextState("STATE", -1, null);
		process = this.nextInvokedProcess(1, defaultMilliseconds);
	}

	/**
	 * Ensure can customise poll logs.
	 */
	@Test
	public void customMessage() {

		// Create the poller
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.identifier("IDENTIFIER").logger(this.logger).build();
		InvokedProcess process = this.invokedProcesses.remove();
		process.managedObject.pollContext.setNextState("STATE", -1, null);
		this.assertLogs("Name", "Next poll for IDENTIFIER in (default) " + defaultMilliseconds + " milliseconds");
	}

	/**
	 * Ensure can manually trigger a poll.
	 */
	@Test
	public void manuallyPoll() {

		// Create the poller
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.identifier("IDENTIFIER").logger(this.logger).build();
		this.invokedProcesses.remove();
		assertEquals(0, this.invokedProcesses.size(), "Should be no further processes");

		// Manually trigger the poll
		this.poller.poll();

		// Ensure manually polled
		InvokedProcess process = this.invokedProcesses.remove();
		process.managedObject.pollContext.setNextState("STATE", -1, null);
		this.assertLogs("Name", "Manual poll for IDENTIFIER");
	}

	/**
	 * Ensure flag final state.
	 */
	@Test
	public void finalState() throws Exception {
		InvokedProcess process = this.startupProcess();
		assertNull(process.managedObject.pollContext.getCurrentState(), "Should not have current state");
		process.parameter.pollContext.setFinalState("TEST");
		assertEquals("TEST", this.poller.getState(10, TimeUnit.MILLISECONDS), "Incorrect startup state");
		assertEquals(0, this.invokedProcesses.size(), "Should be no further polling");
		this.assertLogs("Final", "Final state set. No further polling");
	}

	/**
	 * Ensure flag final state after polling.
	 */
	@Test
	public void finalStateAfterPolling() throws Exception {
		InvokedProcess process = this.startupProcess();
		assertNull(process.managedObject.pollContext.getCurrentState(), "Should not have current state");
		for (int i = 1; i < 10; i++) {
			process.parameter.pollContext.setNextState("Process-" + i, 1, TimeUnit.MINUTES);
			assertEquals("Process-" + i, this.poller.getStateNow(), "Incorrect state");
			process = this.nextInvokedProcess(i, 60 * 1000);
			assertEquals("Process-" + i, process.managedObject.pollContext.getCurrentState(),
					"Should have current state");
		}
		process.parameter.pollContext.setFinalState("FINAL");
		assertEquals("FINAL", this.poller.getState(10, TimeUnit.MILLISECONDS), "Incorrect startup state");
		assertEquals(0, this.invokedProcesses.size(), "Should be no further polling");
	}

	/**
	 * Ensure can clear state.
	 */
	@Test
	public void clearState() throws Exception {

		// Create the state
		InvokedProcess process = this.startupProcess();
		process.managedObject.pollContext.setNextState("STATE", -1, null);
		assertEquals("STATE", this.poller.getStateNow(), "Should have state");
		assertEquals("STATE", this.poller.getState(0, TimeUnit.SECONDS), "Should have waiting state");

		// Clear state
		this.poller.clear();
		assertNull(this.poller.getStateNow(), "Should not have state after clear");
		try {
			this.poller.getState(0, TimeUnit.SECONDS);
			fail("Should not be successful");
		} catch (TimeoutException ex) {
		}

		// Manual poll should trigger load again
		this.poller.poll();
		process = this.invokedProcesses.remove();
		process.managedObject.pollContext.setNextState("RELOAD", -1, null);
		assertEquals("RELOAD", this.poller.getStateNow(), "Should have state after poll");
	}

	/**
	 * <p>
	 * Ensure can initialise poller with value and not poll.
	 * <p>
	 * This is useful for possible test setup that avoids need to poll external
	 * system, but just configures state.
	 */
	@Test
	public void initialisedState() throws Exception {

		// Build for initialised state
		this.poller = StatePoller.state("TEST");

		// Ensure can obtain state
		assertEquals("TEST", this.poller.getStateNow(), "Incorrect immediate state");
		assertEquals("TEST", this.poller.getState(1, TimeUnit.SECONDS), "Incorrect state");

		// Attempt manual poll (should do nothing)
		this.poller.poll();

		// Ensure can clear
		this.poller.clear();
		assertNull(this.poller.getStateNow(), "Should clear state");
	}

	/**
	 * {@link Logger}.
	 */
	private Logger logger;

	/**
	 * {@link Handler} for {@link Logger}.
	 */
	private Handler logHandler;

	/**
	 * {@link Logger} logs.
	 */
	private ByteArrayOutputStream logs = new ByteArrayOutputStream();

	@BeforeEach
	protected void setUp() throws Exception {
		// Create logger (to not create output noise)
		this.logHandler = new StreamHandler(this.logs, new SimpleFormatter());
		this.logHandler.setLevel(Level.ALL);
		this.logger = Logger.getLogger(StatePollerTest.class.getName());
		this.logger.setLevel(Level.ALL);
		this.logger.setUseParentHandlers(false);
		this.logger.addHandler(this.logHandler);
	}

	/**
	 * Assert the logs contain appropriate content.
	 */
	private void assertLogs(String message, String... expectedLogContents) {
		this.logHandler.flush();
		String logs = this.logs.toString();
		assertTrue(logs.trim().length() > 0, message + ": no logs");
		for (String expectedLogContent : expectedLogContents) {
			assertTrue(logs.contains(expectedLogContent), message + ": missing content from logs\n\nEXPECTED:\n"
					+ expectedLogContent + "\n\nACTUAL LOG:\n" + logs + "\n\n");
		}
		this.logs.reset();
	}

	/**
	 * Triggers to obtain the state (but in background {@link Thread} to allow test
	 * to continue).
	 * 
	 * @param timeout Time to wait for the state.
	 * @param unit    {@link TimeUnit} for timeout.
	 * @return {@link Supplier} that blocks to obtain the state.
	 */
	@SuppressWarnings("unchecked")
	private <S> Supplier<S> triggerGetState(long timeout, TimeUnit unit) {
		ThreadSafeClosure<S> state = new ThreadSafeClosure<>();
		new Thread(() -> {
			try {
				state.set((S) this.poller.getState(timeout, unit));
			} catch (Throwable ex) {
				state.failure(ex);
			}
		}).start();
		return () -> state.waitAndGet();
	}

	/**
	 * Flows.
	 */
	public static enum Flows {
		DO_FLOW
	}

	/**
	 * Current {@link MockParameter} identifier.
	 */
	private int currentParameterId = -1;

	/**
	 * Mock parameter.
	 */
	private class MockParameter {

		private final int id = ++StatePollerTest.this.currentParameterId;

		private final StatePollContext<String> pollContext;

		private MockParameter(StatePollContext<String> pollContext) {
			this.pollContext = pollContext;
		}
	}

	/**
	 * Current {@link MockManagedObject} identifier.
	 */
	private int currentManagedObjectId = -1;

	/**
	 * Mock {@link ManagedObject}.
	 */
	private class MockManagedObject implements ManagedObject {

		private final int id = ++StatePollerTest.this.currentManagedObjectId;

		private final StatePollContext<String> pollContext;

		public MockManagedObject(StatePollContext<String> pollContext) {
			this.pollContext = pollContext;
		}

		@Override
		public Object getObject() throws Throwable {
			return this.id;
		}
	}

	/**
	 * Creates the {@link StatePoller} and obtains the startup process.
	 * 
	 * @return {@link InvokedProcess} for the startup process.
	 */
	protected InvokedProcess startupProcess() {

		// Create the poller
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.parameter((context) -> new MockParameter(context)).logger(this.logger).build();

		// Return the startup process
		return this.nextInvokedProcess(0, 0);
	}

	/**
	 * Obtains the next {@link InvokedProcess}.
	 * 
	 * @return Next {@link InvokedProcess}.
	 */
	protected InvokedProcess nextInvokedProcess(int identifier, long expectedDelay) {
		InvokedProcess process = this.invokedProcesses.poll();
		assertNotNull(process, "No process");
		assertEquals(Flows.DO_FLOW, process.key, "Incorrect flow key");
		assertEquals(identifier, process.parameter.id, "Incorrect parameter sequence");
		assertEquals(identifier, process.managedObject.id, "Incorrect managed object sequence");
		assertNotNull(process.parameter.pollContext, "Should have poll context");
		assertSame(process.parameter.pollContext, process.managedObject.pollContext, "Should have same poll context");
		assertEquals(expectedDelay, process.delay, "Incorrect delay");
		return process;
	}

	private StatePoller<String, Flows> poller = null;

	private Deque<InvokedProcess> invokedProcesses = new LinkedList<>();

	private static class InvokedProcess implements ProcessManager, ManagedObjectStartupProcess {

		private final Integer index;

		private final Flows key;

		private final MockParameter parameter;

		private final MockManagedObject managedObject;

		private final long delay;

		private final FlowCallback callback;

		private boolean isConcurrent = false;

		private InvokedProcess(Integer index, Flows key, Object parameter, ManagedObject managedObject, long delay,
				FlowCallback callback) {
			this.index = index;
			this.key = key;
			this.parameter = (MockParameter) parameter;
			this.managedObject = (MockManagedObject) managedObject;
			this.delay = delay;
			this.callback = callback;
		}

		/*
		 * ================ ProcessManager ====================
		 */

		@Override
		public void cancel() {
			fail("Should not cancel poll processes");
		}

		/*
		 * ========== ManagedObjectStartupProcess ==============
		 */

		@Override
		public void setConcurrent(boolean isConcurrent) {
			this.isConcurrent = isConcurrent;
		}
	}

	protected InvokedProcess addInvokedProcess(Integer index, Flows key, Object parameter, ManagedObject managedObject,
			long delay, FlowCallback callback) {
		InvokedProcess process = new InvokedProcess(index, key, parameter, managedObject, delay, callback);
		this.invokedProcesses.add(process);
		return process;
	}

	/*
	 * ============== ManagedObjectExecuteContext ===================
	 */

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public ThreadFactory[] getExecutionStrategy(int executionStrategyIndex) {
		fail("Should not require execution strategy");
		return null;
	}

	@Override
	public ManagedObjectStartupProcess invokeStartupProcess(Flows key, Object parameter, ManagedObject managedObject,
			FlowCallback callback) throws IllegalArgumentException {
		assertEquals(0, this.invokedProcesses.size(), "Should be no invoked process on start up");
		return this.addInvokedProcess(null, key, parameter, managedObject, 0, callback);
	}

	@Override
	public ManagedObjectStartupProcess invokeStartupProcess(int flowIndex, Object parameter,
			ManagedObject managedObject, FlowCallback callback) throws IllegalArgumentException {
		assertEquals(0, this.invokedProcesses.size(), "Should be no invoked process on start up");
		return this.addInvokedProcess(flowIndex, null, parameter, managedObject, 0, callback);
	}

	@Override
	public void addService(ManagedObjectService<Flows> service) {
		try {
			// Service immediately
			service.startServicing(this);
		} catch (Exception ex) {
			fail(ex);
		}
	}

	@Override
	public ProcessManager invokeProcess(Flows key, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		assertSame(Flows.DO_FLOW, key, "Incorrect invoked process flow key");
		return this.addInvokedProcess(null, key, parameter, managedObject, delay, callback);
	}

	@Override
	public ProcessManager invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		assertSame(1, flowIndex, "Incorrect invoked process flow");
		return this.addInvokedProcess(flowIndex, null, parameter, managedObject, delay, callback);
	}

}
