/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.issues.FailCompilerIssues;
import net.officefloor.compile.spi.mbean.MBeanRegistrator;
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
	 * Compiles and run {@link OfficeFloor}.
	 * 
	 * @param args
	 *            Command line arguments.
	 * @throws Exception
	 *             If fails to compile and open.
	 */
	public static void main(String... args) throws Exception {

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Register the MBeans
		// (only means to gracefully close OfficeFloor, without killing process)
		compiler.setMBeanRegistrator(MBeanRegistrator.getPlatformMBeanRegistrator());

		// Handle listening on close of OfficeFloor
		MainOfficeFloorListener exitOnClose = new MainOfficeFloorListener();
		compiler.addOfficeFloorListener(exitOnClose);

		// Compile the OfficeFloor
		System.out.println("Compiling OfficeFloor");
		OfficeFloor officeFloor = compiler.compile("OfficeFloor");

		// Open the OfficeFloor
		System.out.println("Opening OfficeFloor");
		officeFloor.openOfficeFloor();
		System.out.println("OfficeFloor running");

		// Wait until closed
		exitOnClose.waitForClose();
		System.out.println("OfficeFloor closed");
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
		 * @throws InterruptedException
		 *             If interrupted.
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
	 * Convenience method to open a singleton {@link OfficeFloor} for embedded
	 * use. This is typically for unit testing.
	 * <p>
	 * Note previously open {@link OfficeFloor} instance by this method will be
	 * closed. Hence, avoids tests re-using the a previous {@link OfficeFloor}
	 * instance.
	 */
	public synchronized static void open() {

		// Ensure closed
		close();

		// Create the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

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

				// Clear the active OfficeFloor as failed to open
				activeOfficeFloor = null;

				// Propagate failure
				throw new Error("Failed to open OfficeFloor", (Throwable) activeOfficeFloor.openResult);
			}
		}
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
		 * {@link MainOfficeFloorListener} to wait on close of
		 * {@link OfficeFloor}.
		 */
		private final MainOfficeFloorListener closeListener;

		/**
		 * Result of opening the {@link OfficeFloor}.
		 */
		private Object openResult = null;

		/**
		 * Instantiate.
		 * 
		 * @param officeFloor
		 *            {@link OfficeFloor}.
		 * @param closeListener
		 *            {@link MainOfficeFloorListener}.
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