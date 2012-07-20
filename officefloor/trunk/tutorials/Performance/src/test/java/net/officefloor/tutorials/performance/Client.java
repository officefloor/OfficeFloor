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
package net.officefloor.tutorials.performance;

/**
 * Client to the {@link Servicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class Client {

	/**
	 * {@link Connection} instances.
	 */
	private final Connection[] connections;

	/**
	 * {@link Thread} instances.
	 */
	private final Thread[] threads;

	/**
	 * Mask that must match {@link Request} instances to be run.
	 */
	private final int mask;

	/**
	 * Flag to keep connections alive.
	 */
	private volatile boolean isKeepAlive = true;

	/**
	 * {@link Request} iterations.
	 */
	private RequestIteration[] iterations;

	/**
	 * Current {@link RequestIteration}.
	 */
	private RequestIteration currentIteration;

	/**
	 * Results (iteration then {@link Request} index).
	 */
	private RequestInstance[][] results;

	/**
	 * {@link CompletionListener}.
	 */
	private CompletionListener completionListener;

	/**
	 * Initiate.
	 * 
	 * @param mask
	 *            Mask that must match {@link Request} instances to be run.
	 * @param connections
	 *            Number of {@link Connection} instances.
	 */
	public Client(int connections, int mask) {
		this.mask = mask;

		// Create the connections and threads for connections
		this.connections = new Connection[connections];
		this.threads = new Thread[connections];
		for (int i = 0; i < this.connections.length; i++) {

			// Create the connection
			this.connections[i] = new Connection(this);

			// Create and start the connection thread
			this.threads[i] = new Thread(this.connections[i]);
			this.threads[i].setDaemon(true);
			this.threads[i].start();
		}
	}

	/**
	 * Triggers the next run.
	 * 
	 * @param numberOfIterations
	 *            Number of iterations for run.
	 * @param requests
	 *            {@link Request} instances. Array elements may be
	 *            <code>null</code> to not send a {@link Request} (enables
	 *            validating test framework).
	 */
	public synchronized void triggerNextRun(int numberOfIterations,
			Request... requests) {

		// Create the listeners
		this.completionListener = new CompletionListener();

		// Create the new results
		this.iterations = new RequestIteration[numberOfIterations];
		this.results = new RequestInstance[numberOfIterations][requests.length];
		for (int i = 0; i < this.results.length; i++) {

			// Create the request iteration
			this.iterations[i] = new RequestIteration(i);

			// Find index of first and last request for client
			int firstIndex = 0; // first if no match
			int lastIndex = requests.length - 1; // last if no match
			boolean isFirst = true;
			for (int r = 0; r < this.results[i].length; r++) {
				if (this.isMatchingRequest(requests[r])) {

					// Determine if first
					if (isFirst) {
						firstIndex = r;
						isFirst = false; // no longer first
					}

					// Take highest last
					lastIndex = r;
				}
			}

			// Create requests for iteration
			for (int r = 0; r < this.results[i].length; r++) {

				// Create the appropriate listener
				Listener listener = null;
				if (r == firstIndex) {
					// First request of iteration for client
					listener = new FirstListener(this.iterations[i]);
					isFirst = false; // no longer first for iteration

				} else if ((i == (numberOfIterations - 1)) && (r == lastIndex)) {
					// Last request
					listener = this.completionListener;
				}

				// Create the request instance
				this.results[i][r] = new RequestInstance(requests[r],
						this.iterations[i], listener);
			}
		}

		// Start processing
		this.currentIteration = this.iterations[0];
		this.notify(); // wake up one connection for first request
	}

	/**
	 * Indicates if all {@link Connection} instances have connected.
	 * 
	 * @return <code>true</code> if all connected.
	 */
	public boolean hasAllConnectionsConnected() {
		boolean hasAllConnected = true;
		for (int i = 0; i < this.connections.length; i++) {
			if (!(this.connections[i].isConnected())) {
				hasAllConnected = false;
			}
		}
		return hasAllConnected;
	}

	/**
	 * Waits until complete.
	 */
	public RequestInstance[][] waitUntilComplete() throws InterruptedException {

		// Obtain the completion listener
		CompletionListener complete;
		RequestInstance[][] instances;
		synchronized (this) {
			complete = this.completionListener;
			instances = this.results;
		}

		// Wait to be notified that complete
		complete.waitOnCompletion();

		// Ensure iterations complete
		while (this.currentIteration != null) {
			Thread.sleep(100);
		}

		// Check that all request instances complete
		boolean isComplete;
		do {

			// Determine if all requests complete
			isComplete = true;
			for (int i = 0; i < instances.length; i++) {
				for (int r = 0; r < instances[i].length; r++) {
					if (!(instances[i][r].isComplete())) {
						isComplete = false;
					}
				}
			}

			// Wait some time to complete
			if (!isComplete) {
				Thread.sleep(100);
			}

		} while (!isComplete);

		// Return the results
		return instances;
	}

	/**
	 * Invoked by the {@link Connection} to obtain the next
	 * {@link RequestInstance}.
	 * 
	 * @return Next {@link RequestInstance}.
	 * @throws InterruptedException
	 *             Indicating no further {@link RequestInstance}.
	 */
	public synchronized RequestInstance nextRequest()
			throws InterruptedException {

		for (;;) {

			// Wait to start
			while (this.currentIteration == null) {
				this.wait(100);

				// Determine if further requests
				if (!this.isKeepAlive) {
					return null;
				}
			}

			// Obtain the current iteration
			RequestIteration iteration = this.currentIteration;

			// Obtain the iteration request instances
			RequestInstance[] requests = this.results[iteration.iterationIndex];

			// Determine if first request sent
			if (!iteration.isFirstSent) {
				// Send first request
				iteration.isFirstSent = true;
				RequestInstance instance = this
						.nextRequestInstanceForIteration(iteration);
				if (instance == null) {
					// No first request (so is complete)
					iteration.isFirstComplete = true;
				} else {
					// Return the first request
					return instance;
				}
			}

			// Wait until first is complete
			while (!iteration.isFirstComplete) {
				this.wait(100);
			}

			// Obtain the next request
			RequestInstance instance = this
					.nextRequestInstanceForIteration(iteration);
			if (instance != null) {
				// Provide the next request
				return instance;
			}

			// No further requests, wait until iteration complete
			boolean isComplete;
			do {
				isComplete = true;
				CHECK_COMPLETE: for (int r = 0; r < requests.length; r++) {
					if (!(requests[r].isComplete())) {
						isComplete = false;
						break CHECK_COMPLETE;
					}
				}

				/*
				 * Wait until all connections are complete.
				 * 
				 * Note that the last completing connection/thread will always
				 * pass this point and not require to wait.
				 */
				if (!isComplete) {
					this.wait(100);
				}
			} while (!isComplete);

			// Flag request iteration is complete
			if (!(iteration.isIterationComplete)) {
				// Iteration complete and move to next iteration
				iteration.isIterationComplete = true;

				// Only move to next iteration if the current iteration
				if ((this.currentIteration != null)
						&& (this.currentIteration.iterationIndex == iteration.iterationIndex)) {
					int nextIterationIndex = iteration.iterationIndex + 1;
					if (nextIterationIndex == this.iterations.length) {
						// No further iterations
						this.currentIteration = null;
					} else {
						// Move to next iteration
						this.currentIteration = this.iterations[nextIterationIndex];
					}
				}
			}
		}
	}

	/**
	 * Obtains the next {@link RequestInstance} for the {@link RequestIteration}
	 * .
	 * 
	 * @param iteration
	 *            {@link RequestIteration}.
	 * @return Next {@link RequestInstance} or <code>null</code> to indicate no
	 *         further {@link RequestInstance} instances for the
	 *         {@link RequestIteration}.
	 */
	private RequestInstance nextRequestInstanceForIteration(
			RequestIteration iteration) {

		// Obtain the requests
		RequestInstance[] instances = this.results[iteration.iterationIndex];

		// Find next request matching client mask
		for (int i = iteration.nextRequest; i < instances.length; i++) {
			RequestInstance instance = instances[i];

			// Move to next request
			iteration.nextRequest++;

			// Determine if found request
			if (this.isMatchingRequest(instance.getRequest())) {
				return instance; // found next for client
			}

			// Indicate request skipped
			instance.skip();
		}

		// As here, no further requests for iteration
		return null;
	}

	/**
	 * Determines if matching request to be run by this {@link Client}.
	 * 
	 * @param request
	 *            {@link Request}.
	 * @return <code>true</code> to run the {@link Request}.
	 */
	private boolean isMatchingRequest(Request request) {
		return (request.getMask() & this.mask) == this.mask;
	}

	/**
	 * Flags the {@link Client} to stop.
	 */
	public synchronized void flagStop() {

		// Do not keep connections alive
		this.isKeepAlive = false;

		// Notify all connections to stop
		this.notifyAll();
	}

	/**
	 * Blocks waiting on the {@link Connection} instances to stop.
	 */
	public void waitUntilStopped() {

		// Ensure flag stopping
		this.flagStop();

		boolean isComplete;
		do {

			// Determine if complete
			isComplete = true;
			for (int i = 0; i < this.connections.length; i++) {
				if (!(this.connections[i].isComplete())) {
					isComplete = false;
				}
			}

			// Allow some time for completion
			if (!isComplete) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					// Should not happen so finish
					return;
				}
			}

		} while (!isComplete);
	}

	/**
	 * {@link Request} iteration.
	 */
	public static class RequestIteration {

		/**
		 * Index of iteration.
		 */
		public final int iterationIndex;

		/**
		 * Indicates if first {@link Request} is sent.
		 */
		public boolean isFirstSent = false;

		/**
		 * Indicates if first {@link Request} is complete.
		 */
		public boolean isFirstComplete = false;

		/**
		 * Next {@link Request} to send.
		 */
		public int nextRequest = 0;

		/**
		 * Indicates if {@link RequestIteration} is complete.
		 */
		public boolean isIterationComplete = false;

		/**
		 * Initiate.
		 * 
		 * @param interationIndex
		 *            Index of iteration.
		 */
		public RequestIteration(int interationIndex) {
			this.iterationIndex = interationIndex;
		}
	}

	/**
	 * First {@link Request} {@link Listener}.
	 */
	public static class FirstListener implements Listener {

		/**
		 * {@link RequestIteration}.
		 */
		private final RequestIteration iteration;

		/**
		 * Initiate.
		 * 
		 * @param iteration
		 *            {@link RequestIteration}.
		 */
		public FirstListener(RequestIteration iteration) {
			this.iteration = iteration;
		}

		@Override
		public void trigger() {
			synchronized (this.iteration) {
				// Flag first complete and wake up remaining connections
				this.iteration.isFirstComplete = true;
				this.iteration.notifyAll();
			}
		}
	}

	/**
	 * Completion {@link Listener}.
	 */
	public static class CompletionListener implements Listener {

		/**
		 * Indicates if complete.
		 */
		private boolean isComplete = false;

		/**
		 * Waits on trigger of completion.
		 * 
		 * @throws InterruptedException
		 *             If interrupted.
		 */
		public synchronized void waitOnCompletion() throws InterruptedException {
			while (!this.isComplete) {
				this.wait(100);
			}
		}

		@Override
		public synchronized void trigger() {
			this.isComplete = true;
			this.notify();
		}
	}

}