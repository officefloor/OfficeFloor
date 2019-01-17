package net.officefloor.plugin.managedobject.poll;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.ThreadSafeClosure;
import net.officefloor.plugin.managedobject.poll.StatePoller.Builder;
import net.officefloor.plugin.managedobject.poll.StatePoller.Poller;

/**
 * Tests the {@link StatePoller}.
 * 
 * @author Daniel Sagenschneider
 */
public class StatePollerTest extends OfficeFrameTestCase implements ManagedObjectExecuteContext<StatePollerTest.Flows> {

	/**
	 * Default poll milliseconds.
	 */
	private static long defaultMilliseconds = TimeUnit.HOURS.toMillis(1);

	/**
	 * Ensure {@link IllegalArgumentException} thrown if incorrect configuration of
	 * {@link StatePoller}.
	 */
	public void testProtectPollerOfInvalidSetup() throws Exception {
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
		assertNotNull("Should build", builder.build());
	}

	private static void assertIllegalArgument(String message, Runnable runnable) {
		try {
			runnable.run();
			fail("Should trigger " + IllegalArgumentException.class.getSimpleName() + " with cause " + message);
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause", message, ex.getMessage());
		}
	}

	/**
	 * Ensure polling triggered at start up.
	 */
	public void testStartupProcess() throws Exception {
		InvokedProcess process = this.startupProcess();
		assertNull("Should not have current state", process.managedObject.pollContext.getCurrentState());
		process.parameter.pollContext.setNextState("TEST", 1, TimeUnit.HOURS);
		assertEquals("Incorrect startup state", "TEST", this.poller.getState(10, TimeUnit.MILLISECONDS));
		this.assertLogs("Startup Process", "Next poll in ");
	}

	/**
	 * Ensure blocks waiting on initial value.
	 */
	public void testDelayedStartupState() throws Exception {
		InvokedProcess process = this.startupProcess();
		Supplier<String> delayed = this.triggerGetState(10, TimeUnit.MILLISECONDS);
		process.parameter.pollContext.setNextState("TEST", 1, TimeUnit.HOURS);
		assertEquals("Incorrect delayed startup state", "TEST", delayed.get());
		this.assertLogs("Delayed Startup Process", "Next poll in ");
	}

	/**
	 * Ensure blocks until successfully initialised.
	 */
	public void testFailedStartup() throws Throwable {
		InvokedProcess process = this.startupProcess();
		assertNull("Should be no state", this.poller.getStateNow());
		Supplier<String> delayed = this.triggerGetState(1, TimeUnit.SECONDS);
		for (int i = 1; i < 10; i++) {

			// Indicate error from process
			process.parameter.pollContext.setFailure(new IOException("TEST FAILURE"), 100, TimeUnit.MILLISECONDS);
			this.assertLogs("Should have log message of failure for " + i, IOException.class.getName(), "TEST FAILURE",
					"Next poll in 100 milliseconds (approx ");
			assertNull("Should be no startup state after failure " + i, this.poller.getStateNow());

			// Complete the process (successfully)
			process.callback.run(null);

			// Obtain the process
			process = this.nextInvokedProcess(i, 100);
		}
		process.parameter.pollContext.setNextState("TEST", 1, TimeUnit.HOURS);
		assertEquals("Should now retrieve state", "TEST", delayed.get());
	}

	/**
	 * Ensures continues to poll even if callbacks were not called.
	 */
	public void testNoFeedback() throws Throwable {
		InvokedProcess process = this.startupProcess();
		assertNull("Should be no state", this.poller.getStateNow());
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
		assertEquals("Should now retrieve state", "TEST", delayed.get());
	}

	/**
	 * Ensure continues to provide old state until new state overwrites.
	 */
	public void testOldStateUntilRefreshed() throws Throwable {
		InvokedProcess process = this.startupProcess();
		final String STATE = "TEST";
		process.parameter.pollContext.setNextState(STATE, 1, TimeUnit.DAYS);
		assertSame("Incorrect startup state", STATE, this.poller.getStateNow());
		assertSame("Incorrect startup state (immediately return)", STATE,
				this.poller.getState(1, TimeUnit.NANOSECONDS));
		process = this.nextInvokedProcess(1, TimeUnit.DAYS.toMillis(1));
		for (int i = 2; i < 10; i++) {

			// Failing to provide state
			process.parameter.pollContext.setFailure(new IOException("TEST FAILURE"), -1, null);
			this.assertLogs("Fail refresh " + i, IOException.class.getName(), "TEST FAILURE",
					"Next poll in (default) " + defaultMilliseconds + " milliseconds (approx ");

			// Ensure continue to be same value
			assertSame("Incorrect startup state", STATE, this.poller.getStateNow());
			assertSame("Incorrect startup state (immediately return)", STATE,
					this.poller.getState(1, TimeUnit.NANOSECONDS));

			// Obtain the process
			process = this.nextInvokedProcess(i, defaultMilliseconds);
		}
		for (int i = 10; i < 20; i++) {

			// No feedback
			process.callback.run(null);

			// Ensure continue to be same value
			assertSame("Incorrect startup state", STATE, this.poller.getStateNow());
			assertSame("Incorrect startup state (immediately return)", STATE,
					this.poller.getState(1, TimeUnit.NANOSECONDS));

			// Obtain the process
			process = this.nextInvokedProcess(i, defaultMilliseconds);
		}
		process.parameter.pollContext.setNextState("CHANGED", -1, null);
		assertEquals("Should now update state", "CHANGED", this.poller.getStateNow());
		assertSame("Shoudl now update state (immediately return)", "CHANGED",
				this.poller.getState(1, TimeUnit.NANOSECONDS));
	}

	/**
	 * Ensure refresh on successful polls.
	 */
	public void testUpdatingState() throws Throwable {
		InvokedProcess process = this.startupProcess();
		assertNull("Should not have current state", process.managedObject.pollContext.getCurrentState());
		for (int i = 1; i < 10; i++) {

			// Update state
			process.parameter.pollContext.setNextState("Process-" + i, 1, TimeUnit.MINUTES);
			long minuteMilliseconds = 60 * 1000;
			this.assertLogs("Update state " + i, "Next poll in " + minuteMilliseconds + " milliseconds (approx ");

			// Ensure updated state
			assertEquals("Incorrect state", "Process-" + i, this.poller.getStateNow());
			assertEquals("Incorrect state (immediately return)", "Process-" + i,
					this.poller.getState(0, TimeUnit.MILLISECONDS));

			// Obtain the process
			process = this.nextInvokedProcess(i, minuteMilliseconds);
			assertEquals("Should have current state", "Process-" + i,
					process.managedObject.pollContext.getCurrentState());
		}

		// Ensure can use default time
		process.parameter.pollContext.setNextState("Default", -1, null);
		long hourMilliseconds = 60 * 60 * 1000;
		this.assertLogs("Default poll", "Next poll in (default) " + hourMilliseconds + " milliseconds (approx ");
		assertEquals("Incorrect state", "Default", this.poller.getStateNow());
		assertEquals("Incorrect state (immediately return)", "Default", this.poller.getState(0, TimeUnit.MILLISECONDS));
	}

	/**
	 * Ensure can use {@link Flow} index.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testFlowIndex() throws Throwable {

		// Transform to managed object execute context
		ManagedObjectExecuteContext<Indexed> indexedContext = (ManagedObjectExecuteContext) this;

		// Create the poller
		StatePoller<Long, Indexed> poller = StatePoller
				.builder(Long.class, 1, indexedContext, (context) -> new MockManagedObject((StatePollContext) context))
				.logger(this.logger).build();
		this.poller = (StatePoller) poller;

		// Validate the startup process
		InvokedProcess process = this.invokedProcesses.remove();
		assertEquals("Incorrect flow index", Integer.valueOf(1), process.index);
		assertNull("Should be no intial value", poller.getStateNow());
		Supplier<String> delayed = this.triggerGetState(1, TimeUnit.SECONDS);

		// Ensure can invoke process with flow index
		StatePollContext pollContext = (StatePollContext) process.managedObject.pollContext;
		pollContext.setNextState(Long.valueOf(2), -1, null);
		process = this.invokedProcesses.remove();
		assertEquals("Should have value", Long.valueOf(2), poller.getStateNow());
		assertEquals("Should have delayed value", Long.valueOf(2), delayed.get());
	}

	/**
	 * Ensure issue if invalid state.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testInvalidState() throws Throwable {
		InvokedProcess process = this.startupProcess();
		StatePollContext context = process.managedObject.pollContext;
		try {
			context.setNextState(Long.valueOf(1), -1, null);
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause",
					"Invalid state type " + Long.class.getName() + " (required " + String.class.getName() + ")",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can provide <code>null</code> parameter.
	 */
	public void testNoParameter() {
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.logger(this.logger).build();
		InvokedProcess process = this.invokedProcesses.remove();
		assertNull("Should be no startup parameter", process.parameter);
		process.managedObject.pollContext.setNextState("STATE", -1, null);
		process = this.invokedProcesses.remove();
		assertNull("Should be no poll parameter", process.parameter);
	}

	/**
	 * Ensure can override the success log {@link Level}.
	 */
	public void testSuccessLogLevel() {
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
	public void testDefaultPollInterval() {
		this.poller = StatePoller
				.builder(String.class, Flows.DO_FLOW, this, (context) -> new MockManagedObject(context))
				.defaultPollInterval(30, TimeUnit.MINUTES).logger(this.logger).build();
		InvokedProcess process = this.invokedProcesses.remove();
		process.managedObject.pollContext.setNextState("DIFFERENT DEFAULT POLL INTERVAL", -1, null);
		long thirtyMinutes = 30 * 60 * 1000;
		this.assertLogs("Different efault poll interval", "Next poll in (default) " + thirtyMinutes + " milliseconds");
	}

	/**
	 * Ensure can use custom {@link Poller}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testCustomPoller() {

		// Create with custom poller
		Builder builder = StatePoller.builder(String.class, (delay, context, callback) -> {
			if (delay == 0) {
				// Start up
				this.registerStartupProcess(Flows.DO_FLOW, new MockParameter(context), new MockManagedObject(context),
						callback);
			} else {
				// Delayed for next poll
				this.invokeProcess(Flows.DO_FLOW, new MockParameter(context), new MockManagedObject(context), delay,
						callback);
			}
		}).logger(this.logger);

		// Ensure can not set parameter factory (as custom poller)
		assertIllegalArgument("Custom Poller used so may not configure parameter factory",
				() -> builder.parameter((context) -> null));

		// Ensure can continue to poll
		this.poller = (StatePoller) builder.build();
		InvokedProcess process = this.invokedProcesses.remove();
		assertEquals("Should be start up process", 0, process.parameter.id);
		assertEquals("Should be immediate start", 0, process.delay);
		process.parameter.pollContext.setNextState("STATE", -1, null);
		process = this.nextInvokedProcess(1, defaultMilliseconds);
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

	@Override
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
		assertTrue(message + ": no logs", logs.trim().length() > 0);
		for (String expectedLogContent : expectedLogContents) {
			assertTrue(message + ": missing content from logs\n\nEXPECTED:\n" + expectedLogContent + "\n\nACTUAL LOG:\n"
					+ logs + "\n\n", logs.contains(expectedLogContent));
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
		assertNotNull("No process", process);
		assertEquals("Incorrect flow key", Flows.DO_FLOW, process.key);
		assertEquals("Incorrect parameter sequence", identifier, process.parameter.id);
		assertEquals("Incorrect managed object sequence", identifier, process.managedObject.id);
		assertNotNull("Should have poll context", process.parameter.pollContext);
		assertSame("Should have same poll context", process.parameter.pollContext, process.managedObject.pollContext);
		assertEquals("Incorrect delay", expectedDelay, process.delay);
		return process;
	}

	private StatePoller<String, Flows> poller = null;

	private Deque<InvokedProcess> invokedProcesses = new LinkedList<>();

	private static class InvokedProcess implements ProcessManager {

		private final Integer index;

		private final Flows key;

		private final MockParameter parameter;

		private final MockManagedObject managedObject;

		private final long delay;

		private final FlowCallback callback;

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
	public void registerStartupProcess(Flows key, Object parameter, ManagedObject managedObject, FlowCallback callback)
			throws IllegalArgumentException {
		assertEquals("Should be no invoked process on start up", 0, this.invokedProcesses.size());
		this.addInvokedProcess(null, key, parameter, managedObject, 0, callback);
	}

	@Override
	public void registerStartupProcess(int flowIndex, Object parameter, ManagedObject managedObject,
			FlowCallback callback) throws IllegalArgumentException {
		assertEquals("Should be no invoked process on start up", 0, this.invokedProcesses.size());
		this.addInvokedProcess(flowIndex, null, parameter, managedObject, 0, callback);
	}

	@Override
	public ProcessManager invokeProcess(Flows key, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		assertSame("Incorrect invoked process flow key", Flows.DO_FLOW, key);
		return this.addInvokedProcess(null, key, parameter, managedObject, delay, callback);
	}

	@Override
	public ProcessManager invokeProcess(int flowIndex, Object parameter, ManagedObject managedObject, long delay,
			FlowCallback callback) throws IllegalArgumentException {
		assertSame("Incorrect invoked process flow", 1, flowIndex);
		return this.addInvokedProcess(flowIndex, null, parameter, managedObject, delay, callback);
	}

	@Override
	public ThreadFactory[] getExecutionStrategy(int executionStrategyIndex) {
		fail("Should not require execution strategy");
		return null;
	}

}