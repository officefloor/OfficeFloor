/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.tutorials.performance.nio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Enables coordinating the {@link Load} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadCoordinator {

	/**
	 * {@link Set} of {@link Load} instances being coordinated.
	 */
	private final List<LoadRunner> loads = new ArrayList<LoadRunner>();

	/**
	 * {@link Object} to allow notification to the {@link LoadCoordinator}.
	 */
	private final Object notifyCoordinator = new Object();

	/**
	 * {@link Object} to allow notification to the {@link LoadRunner}.
	 */
	private final Object notifyRunners = new Object();

	/**
	 * Indicates whether should be running.
	 */
	private volatile boolean isRun = false;

	/**
	 * Indicates stopping.
	 */
	private volatile boolean isStopping = false;

	/**
	 * {@link RunListener}.
	 */
	private volatile RunListener runListener;

	/**
	 * Indicates whether to just establish connections.
	 */
	private volatile boolean isJustEstablishConnections = false;

	/**
	 * Indicates if stopping.
	 * 
	 * @return <code>true</code> if stopping.
	 */
	public boolean isStopping() {
		return this.isStopping;
	}

	/**
	 * Registers the {@link Load}.
	 * 
	 * @param load
	 *            {@link Load}.
	 * @throws Exception
	 *             if within run.
	 */
	public synchronized void registerLoad(Load load) throws Exception {

		// Do not add load during a run
		if (this.isRun) {
			throw new IOException("Can not add load in the middle of a run");
		}

		// Create the load runner
		LoadRunner runner = new LoadRunner(load);

		// Register the load
		this.loads.add(runner);

		// Start running the load
		Thread thread = new Thread(runner);
		thread.setDaemon(true);
		thread.start();

		// Wait until load is blocked for notify
		synchronized (this.notifyCoordinator) {
			while (!(runner.isReadyToStart)) {
				this.notifyCoordinator.wait(100);
			}
		}
	}

	/**
	 * Runs the interval for the {@link Load} instances.
	 * 
	 * @throws InterruptedException
	 *             If fails to run interval.
	 */
	public synchronized void runInterval(long timeIntervalSeconds,
			RunListener listener, boolean isJustEstablishConnections)
			throws InterruptedException {

		// Provide state to runners
		this.runListener = listener;
		this.isJustEstablishConnections = isJustEstablishConnections;

		// Flag to run
		long startTime;
		synchronized (this.notifyRunners) {
			this.isRun = true;
			startTime = System.currentTimeMillis();
			this.notifyRunners.notifyAll();
		}

		// Wait the interval period of time
		Thread.sleep(timeIntervalSeconds * 1000);

		// Flag to stop
		long endTime = System.currentTimeMillis();
		this.isRun = false;

		// Wake up selectors for loads
		for (LoadRunner runner : this.loads) {
			runner.load.wakeupSelector();
		}

		// Wait until all runs are complete
		synchronized (this.notifyCoordinator) {
			boolean isRunComplete = false;
			while (!isRunComplete) {

				// Check if complete
				isRunComplete = true;
				for (LoadRunner runner : this.loads) {
					if (runner.isRunning) {
						isRunComplete = false;
					}
				}

				// Wait some time to check again if all not complete
				if (!isRunComplete) {

					// Wake up selectors again to try for completion
					for (LoadRunner runner : this.loads) {
						runner.load.wakeupSelector();
					}

					// Wait on completion
					this.notifyCoordinator.wait(1);
				}
			}
		}

		// Provide run interval times to ensure each load overlaps
		long totalRunTime = endTime - startTime;
		for (LoadRunner runner : this.loads) {

			// Provide load with times for run
			long loadStartTime = runner.lastIntervalStartTime;
			long loadEndTime = runner.lastIntervalEndTime;

			// Determine the offsets
			long startOffset = loadStartTime - startTime;
			long endOffset = loadEndTime - endTime;

			// Register details with load
			runner.load.registerLastRunIntervalTime(totalRunTime, startOffset,
					endOffset);
		}
	}

	/**
	 * Stops the {@link Load} instances.
	 * 
	 * @throws Exception
	 *             If fails to stop.
	 */
	public synchronized void stop() throws Exception {

		// Stop the connections
		for (LoadRunner runner : this.loads) {
			runner.load.stop();
		}

		// Flag that stopping
		this.isStopping = true;

		// Trigger running to allow to stop
		synchronized (this.notifyRunners) {
			this.isRun = true;
			this.notifyRunners.notifyAll();
		}

		// Wait until all stopped
		synchronized (this.notifyCoordinator) {
			boolean isStopped = false;
			while (!isStopped) {

				// Check if all stopped
				isStopped = true;
				for (LoadRunner runner : this.loads) {
					if (!(runner.isStopped)) {
						isStopped = false;
					}
				}

				// Wait some time to check again if all not stopped
				if (!isStopped) {
					this.notifyCoordinator.wait(1);
				}
			}
		}
	}

	/**
	 * {@link Runnable} running the {@link Load}.
	 */
	private class LoadRunner implements Runnable {

		/**
		 * {@link Load}.
		 */
		private final Load load;

		/**
		 * Indicates if ready to start.
		 */
		volatile boolean isReadyToStart = false;

		/**
		 * Indicates if running interval.
		 */
		volatile boolean isRunning = false;

		/**
		 * Start time in milliseconds of last interval.
		 */
		volatile long lastIntervalStartTime;

		/**
		 * End time in milliseconds of last interval.
		 */
		volatile long lastIntervalEndTime;

		/**
		 * Indicates if stopped.
		 */
		volatile boolean isStopped = false;

		/**
		 * Initiate.
		 * 
		 * @param load
		 *            {@link Load}.
		 */
		public LoadRunner(Load load) {
			this.load = load;
		}

		/*
		 * ====================== Runnable =========================
		 */

		@Override
		public void run() {

			try {

				// Ready to start
				synchronized (LoadCoordinator.this.notifyCoordinator) {
					this.isReadyToStart = true;
					LoadCoordinator.this.notifyCoordinator.notify();
				}

				// Run until flagged to stop
				for (;;) {

					// Block waiting to run
					synchronized (LoadCoordinator.this.notifyRunners) {
						while (!(LoadCoordinator.this.isRun)) {
							LoadCoordinator.this.notifyRunners.wait(100);
						}
					}

					// Determine if stopping
					if (LoadCoordinator.this.isStopping) {

						// Run until stopped
						while (this.load.runSelect(null, false))
							;

						// Notify coordinator that stopped
						synchronized (LoadCoordinator.this.notifyCoordinator) {
							this.isStopped = true;
							LoadCoordinator.this.notifyCoordinator.notify();
						}
						return; // stopped
					}

					// Indicate interval started
					this.isRunning = true;
					this.lastIntervalStartTime = System.currentTimeMillis();

					// Undertake running, until stop
					while (LoadCoordinator.this.isRun) {

						// Process the request
						this.load
								.runSelect(
										LoadCoordinator.this.runListener,
										LoadCoordinator.this.isJustEstablishConnections);
					}

					// Indicate interval stopped
					this.lastIntervalEndTime = System.currentTimeMillis();

					// Notify coordinator that run complete
					synchronized (LoadCoordinator.this.notifyCoordinator) {
						this.isRunning = false;
						LoadCoordinator.this.notifyCoordinator.notify();
					}
				}

			} catch (InterruptedException ex) {
				// Should not occur
				ex.printStackTrace();

			} catch (IOException ex) {
				// Should not occur
				ex.printStackTrace();

			} finally {
				// Indicate stopped
				this.isStopped = true;
			}
		}
	}

}