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
	 * {@link Request} sequence.
	 */
	private final Request[] requests;

	/**
	 * Number of iterations through the {@link Request} sequence.
	 */
	private final int numberOfIterations;

	/**
	 * {@link Connection} instances.
	 */
	private final Connection[] connections;

	/**
	 * {@link Request} iterations.
	 */
	private RequestIteration[] iterations;

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
	 * @param requests
	 *            {@link Request} sequence.
	 * @param iterations
	 *            Number of iterations of the {@link Request} sequence.
	 * @param connections
	 *            Number of {@link Connection} instances.
	 */
	public Client(Request[] requests, int iterations, int connections) {
		this.requests = requests;
		this.numberOfIterations = iterations;

		// Create the connections
		this.connections = new Connection[connections];
		for (int i = 0; i < this.connections.length; i++) {
			this.connections[i] = new Connection(this);
			new Thread(this.connections[i]).start();
		}
	}

	/**
	 * Triggers the warm up.
	 * 
	 * @param isMakeRequest
	 *            Flags whether to make the {@link Request}.
	 */
	public void triggerWarmUp(boolean isMakeRequest) {
		// Provide two iterations for warm up
		this.triggerNextRun(2, isMakeRequest);
	}

	/**
	 * Triggers the run.
	 * 
	 * @param isMakeRequest
	 *            Flags whether to make the {@link Request}.
	 */
	public void triggerRun(boolean isMakeRequest) {
		this.triggerNextRun(this.numberOfIterations, isMakeRequest);
	}

	/**
	 * Triggers the next run.
	 * 
	 * @param numberOfIterations
	 *            Number of iterations for run.
	 * @param isMakeRequest
	 *            Flags whether to make the {@link Request}.
	 */
	private synchronized void triggerNextRun(int numberOfIterations,
			boolean isMakeRequest) {

		// Create the listeners
		this.completionListener = new CompletionListener();

		// Create the new results
		this.iterations = new RequestIteration[numberOfIterations];
		this.results = new RequestInstance[numberOfIterations][this.requests.length];
		for (int i = 0; i < this.results.length; i++) {

			// Create the request iteration
			this.iterations[i] = new RequestIteration(i);

			// Create requests for iteration
			for (int r = 0; r < this.results[i].length; r++) {

				// Create the appropriate listener
				Listener listener = null;
				if (r == 0) {
					// First request of iteration
					listener = new FirstListener(this.iterations[i]);

				} else if ((i == (numberOfIterations - 1))
						&& (r == (this.requests.length - 1))) {
					// Last request
					listener = this.completionListener;
				}

				// Create the request instance
				this.results[i][r] = new RequestInstance(
						(isMakeRequest ? this.requests[r] : null), listener);
			}
		}

		// Start processing
		this.currentIteration = this.iterations[0];
		this.notify(); // wake up one connection for first request
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

			// Wait very short time as should complete shortly
			if (!isComplete) {
				synchronized (instances) {
					instances.wait(0, 10);
				}
			}

		} while (!isComplete);

		// Return the results
		return instances;
	}

	/**
	 * Current {@link RequestIteration}.
	 */
	private RequestIteration currentIteration;

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
				this.wait(1000);
			}

			// Obtain the current iteration
			RequestIteration iteration = this.currentIteration;

			// Obtain the iteration request instances
			RequestInstance[] requests = this.results[iteration.iterationIndex];

			// Determine if first request sent
			if (!iteration.isFirstSent) {
				// Send first request
				iteration.isFirstSent = true;
				RequestInstance instance = requests[0];
				return instance;
			}

			// Wait until first is complete
			while (!iteration.isFirstComplete) {
				this.wait(100);
			}

			// Obtain the subsequent requests
			if (iteration.nextRequest == requests.length) {
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

			} else {
				// Obtain the next request
				RequestInstance instance = requests[iteration.nextRequest];
				iteration.nextRequest++;
				return instance;
			}

		}
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
		public int nextRequest = 1;

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