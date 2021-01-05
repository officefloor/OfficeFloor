/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.test;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link TestSupport} for multiple {@link Thread} tests.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadedTestSupport implements TestSupport {

	/*
	 * ===================== TestSupport ====================================
	 */

	/**
	 * Default constructor.
	 */
	public ThreadedTestSupport() {
		// Nothing to initialise
	}

	@Override
	public void init(ExtensionContext context) throws Exception {
		// Nothing to initialise
	}

	/**
	 * Indicates if the GUI is available.
	 * 
	 * @return <code>true</code> if the GUI is available.
	 */
	public boolean isGuiAvailable() {
		return !GraphicsEnvironment.isHeadless();
	}

	/**
	 * Multi-threaded test logic interface.
	 * 
	 * @param <T> Possible {@link Throwable}.
	 */
	@FunctionalInterface
	public static interface MultithreadedTestLogic<T extends Throwable> {

		/**
		 * Undertakes test logic.
		 * 
		 * @throws T Possible {@link Throwable}.
		 */
		void run() throws T;
	}

	/**
	 * Undertakes multi-threaded testing of {@link MultithreadedTestLogic}.
	 * 
	 * @param threadCount    Number of {@link Thread} instances to run in parallel.
	 * @param iterationCount Number of iterations of {@link MultithreadedTestLogic}
	 *                       per {@link Thread}.
	 * @param test           {@link MultithreadedTestLogic}.
	 * @throws T Possible failure from failing {@link MultithreadedTestLogic}.
	 */
	public <T extends Throwable> void doMultiThreadedTest(int threadCount, int iterationCount,
			MultithreadedTestLogic<T> test) throws T {
		this.doMultiThreadedTest(threadCount, iterationCount, 3, test);
	}

	/**
	 * Undertakes multi-threaded testing of {@link MultithreadedTestLogic}.
	 * 
	 * @param threadCount    Number of {@link Thread} instances to run in parallel.
	 * @param iterationCount Number of iterations of {@link MultithreadedTestLogic}
	 *                       per {@link Thread}.
	 * @param timeout        Timeout.
	 * @param test           {@link MultithreadedTestLogic}.
	 * @throws T Possible failure from failing {@link MultithreadedTestLogic}.
	 */
	public <T extends Throwable> void doMultiThreadedTest(int threadCount, int iterationCount, int timeout,
			MultithreadedTestLogic<T> test) throws T {
		this.triggerMultiThreadedTest(threadCount, iterationCount, timeout, test).waitForCompletion();
	}

	/**
	 * Multi-threaded execution.
	 */
	public static interface MultiThreadedExecution<T extends Throwable> {

		/**
		 * Waits for completion of all threads.
		 * 
		 * @throws T If failure in a thread.
		 */
		void waitForCompletion() throws T;

		/**
		 * <p>
		 * Indicates if there is currently an error.
		 * <p>
		 * This method will not block.
		 * 
		 * @return <code>true</code> if an error.
		 */
		boolean isError();

		/**
		 * <p>
		 * Allows to use within predicate checks to throw failure if one.
		 * <p>
		 * This method will not block.
		 * 
		 * @return <code>false</code> always, as will throw failure.
		 * @throws T Failure if an error.
		 */
		boolean isErrorAndThrow() throws T;
	}

	/**
	 * Triggers single threaded testing of {@link MultithreadedTestLogic}.
	 * 
	 * @param test {@link MultithreadedTestLogic}.
	 * @return {@link MultiThreadedExecution}.
	 * @throws T Possible failure from failing {@link MultithreadedTestLogic}.
	 */
	public <T extends Throwable> MultiThreadedExecution<T> triggerThreadedTest(MultithreadedTestLogic<T> test)
			throws T {
		return this.triggerMultiThreadedTest(1, 1, 3, test);
	}

	/**
	 * Triggers multi-threaded testing of {@link MultithreadedTestLogic}.
	 * 
	 * @param threadCount    Number of {@link Thread} instances to run in parallel.
	 * @param iterationCount Number of iterations of {@link MultithreadedTestLogic}
	 *                       per {@link Thread}.
	 * @param test           {@link MultithreadedTestLogic}.
	 * @return {@link MultiThreadedExecution}.
	 * @throws T Possible failure from failing {@link MultithreadedTestLogic}.
	 */
	public <T extends Throwable> MultiThreadedExecution<T> triggerMultiThreadedTest(int threadCount, int iterationCount,
			MultithreadedTestLogic<T> test) throws T {
		return this.triggerMultiThreadedTest(threadCount, iterationCount, 3, test);
	}

	/**
	 * Triggers multi-threaded testing of {@link MultithreadedTestLogic}.
	 * 
	 * @param threadCount    Number of {@link Thread} instances to run in parallel.
	 * @param iterationCount Number of iterations of {@link MultithreadedTestLogic}
	 *                       per {@link Thread}.
	 * @param timeout        Timeout.
	 * @param test           {@link MultithreadedTestLogic}.
	 * @return {@link MultiThreadedExecution}.
	 * @throws T Possible failure from failing {@link MultithreadedTestLogic}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Throwable> MultiThreadedExecution<T> triggerMultiThreadedTest(int threadCount, int iterationCount,
			int timeout, MultithreadedTestLogic<T> test) throws T {

		// Create the threads with completion status
		boolean[] isComplete = new boolean[threadCount];
		Thread[] threads = new Thread[threadCount];
		Closure<Throwable> failure = new Closure<>();
		for (int t = 0; t < threads.length; t++) {
			isComplete[t] = false;
			final int threadIndex = t;
			threads[t] = new Thread(() -> {
				try {
					// Undertake all iterations of test
					for (int i = 0; i < iterationCount; i++) {
						test.run();
					}

				} catch (Throwable ex) {
					// Capture the first error (as likely cause)
					synchronized (isComplete) {
						if (failure.value == null) {
							failure.value = ex;
						}
					}

				} finally {
					// Flag complete
					synchronized (isComplete) {
						isComplete[threadIndex] = true;
						isComplete.notify(); // wake up immediately
					}
				}
			});
		}

		// Start all threads
		for (int t = 0; t < threads.length; t++) {
			threads[t].start();
		}

		// Return execution to block until completion
		return new MultiThreadedExecution<T>() {

			@Override
			public void waitForCompletion() throws T {

				// Wait until threads complete or time out
				long startTime = System.currentTimeMillis();
				synchronized (isComplete) {
					boolean isCompleted = false;
					while (!isCompleted) {

						// Determine if error
						if (failure.value != null) {
							throw (T) failure.value;
						}

						// Determine if complete
						isCompleted = true;
						for (boolean isThreadComplete : isComplete) {
							if (!isThreadComplete) {
								isCompleted = false;
							}
						}
						if (isCompleted) {
							return; // successfully completed
						}

						// Determine if timed out
						timeout(startTime, timeout);

						// Try again after some time
						try {
							isComplete.wait(50);
						} catch (InterruptedException ex) {
							Assertions.fail("Sleep interrupted: " + ex.getMessage());
						}
					}
				}
			}

			@Override
			public boolean isError() {
				synchronized (isComplete) {
					return failure.value != null;
				}
			}

			@Override
			public boolean isErrorAndThrow() throws T {
				synchronized (isComplete) {
					if (failure.value != null) {
						throw (T) failure.value;
					}
				}
				return false; // as here, no error
			}
		};
	}

	/**
	 * Facade helper function for invoking {@link Thread#sleep(long)}.
	 * 
	 * @param time Sleep time in seconds.
	 */
	public void sleep(int time) {
		try {
			Thread.sleep(time * 1000);
		} catch (InterruptedException ex) {
			Assertions.fail("Sleep interrupted: " + ex.getMessage());
		}
	}

	/**
	 * Facade method to timeout operations after 3 seconds.
	 * 
	 * @param startTime Start time from {@link System#currentTimeMillis()}.
	 */
	public void timeout(long startTime) {
		this.timeout(startTime, (String) null);
	}

	/**
	 * Facade method to timeout operations after 3 seconds.
	 * 
	 * @param startTime Start time from {@link System#currentTimeMillis()}.
	 * @param message   Message to include in possible failure.
	 */
	public void timeout(long startTime, String message) {
		this.timeout(startTime, 3, message);
	}

	/**
	 * Facade method to timeout operations after a second.
	 * 
	 * @param startTime    Start time from {@link System#currentTimeMillis()}.
	 * @param secondsToRun Seconds to run before timeout.
	 */
	public void timeout(long startTime, int secondsToRun) {
		this.timeout(startTime, secondsToRun, null);
	}

	/**
	 * Facade method to timeout operations after a second.
	 * 
	 * @param startTime    Start time from {@link System#currentTimeMillis()}.
	 * @param secondsToRun Seconds to run before timeout.
	 * @param message      Message to include in possible failure.
	 */
	public void timeout(long startTime, int secondsToRun, String message) {
		if ((System.currentTimeMillis() - startTime) > (secondsToRun * 1000)) {
			Assertions.fail((message != null ? message + " (" : "") + "TIME OUT after " + secondsToRun + " seconds"
					+ (message != null ? ")" : ""));
		}
	}

	/**
	 * Predicate to check for is true.
	 */
	@FunctionalInterface
	public static interface WaitForTruePredicate<T extends Throwable> {

		/**
		 * Predicate test.
		 * 
		 * @return <code>true</code> to indicate no further waiting.
		 * @throws T Possible exception.
		 */
		boolean test() throws T;
	}

	/**
	 * Waits for the check to be <code>true</code>.
	 * 
	 * @param <T>   Possible failure type.
	 * @param check Check.
	 * @throws T Possible failure.
	 */
	public <T extends Throwable> void waitForTrue(WaitForTruePredicate<T> check) throws T {
		this.waitForTrue(check, (String) null);
	}

	/**
	 * Waits for the check to be <code>true</code>.
	 * 
	 * @param <T>     Possible failure type.
	 * @param check   Check.
	 * @param message Message to include in possible failure.
	 * @throws T Possible failure.
	 */
	public <T extends Throwable> void waitForTrue(WaitForTruePredicate<T> check, String message) throws T {
		this.waitForTrue(check, 3, message);
	}

	/**
	 * Waits for the check to be <code>true</code>.
	 * 
	 * @param <T>          Possible failure type.
	 * @param check        Check.
	 * @param secondsToRun Seconds to wait before timing out.
	 * @throws T Possible failure.
	 */
	public <T extends Throwable> void waitForTrue(WaitForTruePredicate<T> check, int secondsToRun) throws T {
		this.waitForTrue(check, secondsToRun, null);
	}

	/**
	 * Waits for the check to be <code>true</code>.
	 * 
	 * @param <T>          Possible failure type.
	 * @param check        Check.
	 * @param secondsToRun Seconds to wait before timing out.
	 * @param message      Message to include in possible failure.
	 * @throws T Possible failure.
	 */
	public <T extends Throwable> void waitForTrue(WaitForTruePredicate<T> check, int secondsToRun, String message)
			throws T {
		long startTime = System.currentTimeMillis();
		while (!check.test()) {
			timeout(startTime, secondsToRun, message);
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				Assertions.fail("Sleep interrupted: " + ex.getMessage());
			}
		}
	}

}
