/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor;

import java.util.Arrays;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Provides <code>main</code> method to compile and run {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMain {

	/**
	 * Line output to <code>stdout</code> to indicate {@link OfficeFloor} is
	 * running.
	 */
	public static String STD_OUT_RUNNING_LINE = "OfficeFloor running";

	/**
	 * Line output to <code>stderr</code> to indicate {@link OfficeFloor} failed to
	 * start.
	 */
	public static String STD_ERR_FAIL_LINE = "OfficeFloor failed to open";

	/**
	 * Compiles and run {@link OfficeFloor}.
	 * 
	 * @param args Command line arguments.
	 * @throws Exception If fails to compile and open.
	 */
	public static void main(String... args) throws Exception {
		new OfficeFloorRunner().runAndBlockToClose(System.out, System.err, args);
	}

	/**
	 * <p>
	 * Compiles and runs {@link OfficeFloor} with default arguments.
	 * <p>
	 * This is used by specific main classes to start specific customisations of
	 * {@link OfficeFloor}.
	 * 
	 * @param defaultArgs Default arguments.
	 * @param args        Command line arguments.
	 * @throws Exception If fails to compile and open.
	 */
	protected static void mainWithDefaults(String[] defaultArgs, String... args) throws Exception {

		// Combine arguments
		String[] mainArgs;
		if ((defaultArgs == null) || (defaultArgs.length == 0)) {
			// No default args
			mainArgs = args;

		} else {
			// Combine arguments
			mainArgs = Arrays.copyOf(defaultArgs, defaultArgs.length + args.length);
			System.arraycopy(args, 0, mainArgs, defaultArgs.length, args.length);
		}

		// Run the main
		OfficeFloorMain.main(mainArgs);
	}

	/**
	 * {@link OfficeFloorListener} for main method.
	 */
	private static class MainOfficeFloorListener implements OfficeFloorListener {

		/**
		 * Flag indicating if closed.
		 */
		private boolean isClosed = false;

		/**
		 * Waits until the {@link OfficeFloor} is closed.
		 * 
		 * @throws InterruptedException If interrupted.
		 */
		private synchronized void waitForClose() throws InterruptedException {

			// Do not wait if closed
			if (this.isClosed) {
				return;
			}

			// Wait until closed
			this.wait();
		}

		/*
		 * ==================== OfficeFloorListener =======================
		 */

		@Override
		public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
			// Do nothing
		}

		@Override
		public synchronized void officeFloorClosed(OfficeFloorEvent event) throws Exception {

			// Flag closed
			this.isClosed = true;

			// Notify that closed
			this.notify();
		}
	}

	/**
	 * Active {@link OfficeFloor}.
	 */
	private static OfficeFloorThread activeOfficeFloor = null;

	/**
	 * <p>
	 * Convenience method to open a singleton {@link OfficeFloor} for embedded use.
	 * This is typically for unit testing.
	 * <p>
	 * Note previously open {@link OfficeFloor} instance by this method will be
	 * closed. Hence, avoids tests re-using the previous {@link OfficeFloor}
	 * instance.
	 *
	 * @param propertyNameValuePairs Name/value {@link Property} pairs.
	 * @return Opened {@link OfficeFloor}.
	 */
	public synchronized static OfficeFloor open(String... propertyNameValuePairs) {

		// Ensure closed
		close();

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String name = propertyNameValuePairs[i];
			String value = propertyNameValuePairs[i + 1];
			compiler.addProperty(name, value);
		}

		// Handle listening on close of OfficeFloor
		MainOfficeFloorListener closeListener = new MainOfficeFloorListener();
		compiler.addOfficeFloorListener(closeListener);

		// Propagate failure to compile OfficeFloor
		compiler.setCompilerIssues(new FailCompilerIssues());

		// Compile the OfficeFloor
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");

		// Open the OfficeFloor on a thread (in case of thread local aware team)
		activeOfficeFloor = new OfficeFloorThread(officeFloor, closeListener);
		activeOfficeFloor.start();

		// Wait until started
		synchronized (activeOfficeFloor) {

			// Determine if must wait for open
			if (activeOfficeFloor.openResult == null) {
				try {
					activeOfficeFloor.wait();
				} catch (InterruptedException ex) {
					// Flag failure in wait
					activeOfficeFloor.openResult = ex;
				}
			}

			// Determine if failed to open OfficeFloor
			if (activeOfficeFloor.openResult instanceof Throwable) {
				Throwable openFailure = (Throwable) activeOfficeFloor.openResult;

				// Clear the active OfficeFloor as failed to open
				activeOfficeFloor = null;

				// Propagate failure
				throw new Error("Failed to open OfficeFloor", openFailure);
			}
		}

		// Return the OfficeFloor
		return officeFloor;
	}

	/**
	 * Closes the singleton embedded {@link OfficeFloor}.
	 */
	public synchronized static void close() {

		// Do nothing if no active OfficeFloor
		if (activeOfficeFloor == null) {
			return;
		}

		try {
			// Close the OfficeFloor
			activeOfficeFloor.officeFloor.closeOfficeFloor();

			// Wait for OfficeFloor to close
			activeOfficeFloor.closeListener.waitForClose();

			// No further active OfficeFloor
			activeOfficeFloor = null;

		} catch (Exception ex) {
			// Propagate failure to close
			throw new Error("Failed to close OfficeFloor", ex);
		}
	}

	/**
	 * {@link Thread} to run the {@link OfficeFloor}.
	 */
	private static class OfficeFloorThread extends Thread {

		/**
		 * {@link OfficeFloor}.
		 */
		private final OfficeFloor officeFloor;

		/**
		 * {@link MainOfficeFloorListener} to wait on close of {@link OfficeFloor}.
		 */
		private final MainOfficeFloorListener closeListener;

		/**
		 * Result of opening the {@link OfficeFloor}.
		 */
		private Object openResult = null;

		/**
		 * Instantiate.
		 * 
		 * @param officeFloor   {@link OfficeFloor}.
		 * @param closeListener {@link MainOfficeFloorListener}.
		 */
		public OfficeFloorThread(OfficeFloor officeFloor, MainOfficeFloorListener closeListener) {
			this.officeFloor = officeFloor;
			this.closeListener = closeListener;
		}

		/*
		 * ================== Runnable ==========================
		 */

		@Override
		public synchronized void run() {
			try {
				// Open the OfficeFloor
				this.officeFloor.openOfficeFloor();

				// Open successful
				this.openResult = Boolean.TRUE;

			} catch (Throwable ex) {
				// Flag failure opening OfficeFloor
				this.openResult = ex;

			} finally {
				// Notify started
				this.notify();
			}
		}
	}

}
