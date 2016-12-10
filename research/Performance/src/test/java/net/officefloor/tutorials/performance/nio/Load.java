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
import java.io.PrintStream;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;

/**
 * Load on the {@link CommunicationProtocol}.
 * 
 * @author Daniel Sagenschneider
 */
public class Load {

	/**
	 * {@link DecimalFormat} for the service times.
	 */
	public static final DecimalFormat serviceTimeFormat = new DecimalFormat(
			"0.00");

	/**
	 * Description of this load for display.
	 */
	private final String description;

	/**
	 * Containing {@link Runner}.
	 */
	private final Runner runner;

	/**
	 * {@link Selector}.
	 */
	private final Selector selector;

	/**
	 * Unique failures.
	 */
	private final Set<String> uniqueFailures = new HashSet<String>();

	/**
	 * Indicates if to disconnect after the {@link Request} sequence is sent.
	 */
	private final boolean isDisconnectAfterSequence;

	/**
	 * Sequence of {@link Request} instances.
	 */
	private final Request[] requests;

	/**
	 * Listing of {@link Connection} instances for this {@link Load}.
	 */
	private final List<Connection> connections = new ArrayList<Connection>();

	/**
	 * Initiate.
	 * 
	 * @param description
	 *            Description of the load.
	 * @param runner
	 *            Containing {@link Runner}.
	 * @param isDisconnectAfterSequence
	 *            Indicates if to disconnect after the sequence of
	 *            {@link Request} instances are serviced.
	 * @param requests
	 *            Sequence of {@link Request} instances.
	 * @throws IOException
	 *             If fails to create load.
	 */
	Load(String description, Runner runner, boolean isDisconnectAfterSequence,
			Request[] requests) throws IOException {
		this.description = description;
		this.runner = runner;
		this.isDisconnectAfterSequence = isDisconnectAfterSequence;
		this.requests = requests;

		// Create the selector
		this.selector = Selector.open();
	}

	/**
	 * Wakes up the {@link Selector}.
	 */
	void wakeupSelector() {
		this.selector.wakeup();
	}

	/**
	 * Reset for the next run interval.
	 */
	synchronized void reset() {

		// Reset the connection details
		for (Connection connection : this.connections) {
			connection.reset();
		}

		// Reset the request details
		for (Request request : this.requests) {
			request.reset();
		}
	}

	/**
	 * Runs a single select and processes the {@link SelectionKey} instances.
	 * 
	 * @param listener
	 *            {@link RunListener}.
	 * @param isJustEstablishConnections
	 *            Indicates whether to just establish connections.
	 * @return <code>true</code> if should do another select.
	 * @throws IOException
	 *             If failed.
	 */
	synchronized boolean runSelect(RunListener listener,
			boolean isJustEstablishConnections) throws IOException {

		// Select next keys
		this.selector.select(1000);

		// Obtain the load coordinator
		LoadCoordinator coordinator = this.runner.getLoadCoordinator();

		// Indicate if stopped
		if (coordinator.isStopping()) {
			if (this.selector.keys().size() == 0) {
				// No more keys (all connections closed), so stopped
				this.selector.close();
				return false;
			}

			// Close all connections
			for (SelectionKey key : this.selector.keys()) {
				((SocketChannel) key.channel()).close();
				key.cancel();
			}

			// Another selection necessary to clean up
			return true;
		}

		// Process selected keys
		NEXT_KEY: for (SelectionKey key : this.selector.selectedKeys()) {

			// For testing should always stay valid
			if (!key.isValid()) {
				throw new IOException("All clients to stay connected!");
			}

			// Obtain the connection
			Connection connection = (Connection) key.attachment();
			try {

				// Determine if connected
				if (key.isConnectable()) {
					// Connected, so send first request
					if (!(connection.finishConnect())) {
						throw new IOException("Failed to establish connection");
					}
					key.interestOps(SelectionKey.OP_WRITE);
					continue NEXT_KEY;
				}

				// Determine if sending requests (may just be connecting)
				if (!isJustEstablishConnections) {

					// Determine if writable
					if (key.isWritable()) {
						if (connection.writeRequest()) {
							key.interestOps(SelectionKey.OP_READ);

							// Indicate request sent
							if (listener != null) {
								listener.requestSent();
							}
						}

						// Determine if just closed connection
						if (!(key.isValid())) {
							continue NEXT_KEY;
						}
					}

					// Determine if received response
					if (key.isReadable()) {
						if (connection.readResponse()) {
							key.interestOps(SelectionKey.OP_WRITE);

							// Indicate response received
							if (listener != null) {
								listener.responseReceived();
							}
						}
					}
				}

			} catch (Exception ex) {

				// Provide each unique exception
				String exceptionIdentifier = ex.getClass().getSimpleName()
						+ " - " + ex.getMessage();
				if (!(this.uniqueFailures.contains(exceptionIdentifier))) {
					this.uniqueFailures.add(exceptionIdentifier);

					// Provide the exception
					ex.printStackTrace(System.out);
				}

				// Clean up connection
				connection.connectionFailed();
				key.channel().close();
				key.cancel();

				// Attempt to re-establish connection (if testing)
				if (!(coordinator.isStopping())) {
					connection.establishNewConnection();
				}
			}
		}

		// Clear selected keys for next selection
		this.selector.selectedKeys().clear();

		// As here, need another select
		return true;
	}

	/**
	 * Adds multiple {@link Connection} instances.
	 * 
	 * @param numberOfConnections
	 *            Number of {@link Connection} instances to add.
	 * @throws IOException
	 *             If fails to add the connections.
	 */
	public synchronized void addConnections(int numberOfConnections)
			throws IOException {
		for (int i = 0; i < numberOfConnections; i++) {

			// Add the connection
			this.connections.add(new Connection(this));

			// Determine if need to start establishing connections
			if (((i + 1) % 500) == 0) {
				String message = "Establshing up to " + i + " connections for "
						+ this.description + " (to avoid SYNC attack)";
				System.out.println(message);
				this.runSelect(null, true);
			}
		}

		// Connect remaining connections
		String message = "Establishing all " + this.getConnectionCount()
				+ " connections for " + this.description
				+ " (to avoid SYNC attack) ... ";
		System.out.print(message);
		System.out.flush();

		// Ensure all connected
		boolean isAllConnected = false;
		while (!isAllConnected) {

			// Determine if all is connected
			isAllConnected = true;
			for (Connection connection : this.connections) {
				if (!(connection.isConnected())) {
					isAllConnected = false;
				}
			}

			// Run select again to allow establishing all connections
			if (!isAllConnected) {
				this.runSelect(null, true);
			}
		}
		System.out.println(" all connected");
	}

	/**
	 * Obtains the number of {@link Connection} instances.
	 * 
	 * @return Number of {@link Connection} instances.
	 */
	public synchronized int getConnectionCount() {
		return this.connections.size();
	}

	/**
	 * Stops all {@link Connection} instances.
	 */
	synchronized void stop() {
		// Stop all connections
		for (Connection connection : this.connections) {
			connection.stop();
		}
	}

	/**
	 * Obtains the {@link Runner}.
	 * 
	 * @return {@link Runner}.
	 */
	Runner getRunner() {
		return this.runner;
	}

	/**
	 * Obtains the {@link Selector}.
	 * 
	 * @return {@link Selector}.
	 */
	Selector getSelector() {
		return this.selector;
	}

	/**
	 * Obtains the sequence of {@link Request} instances for this load.
	 * 
	 * @return Sequence of {@link Request} instances for this load.
	 */
	Request[] getRequests() {
		return this.requests;
	}

	/**
	 * Determines if to disconnect after the sequence of {@link Request}
	 * instances have been sent.
	 * 
	 * @return <code>true</code> to disconnect.
	 */
	boolean isDisconnectAfterSequence() {
		return this.isDisconnectAfterSequence;
	}

	/**
	 * Total run time for the last interval in milliseconds.
	 */
	private long lastTotalRunTime;

	/**
	 * Offset of this {@link Load} from the last run interval start time in
	 * milliseconds.
	 */
	private long lastStartOffset;

	/**
	 * Offset of this {@link Load} from the last run interval end time in
	 * milliseconds.
	 */
	private long lastEndOffset;

	/**
	 * Registers the times of the last interval.
	 * 
	 * @param totalRunTime
	 *            Total run time for the last interval in milliseconds.
	 * @param startOffset
	 *            Offset of this {@link Load} from the last run interval start
	 *            time in milliseconds.
	 * @param endOffset
	 *            Offset of this {@link Load} from the last run interval end
	 *            time in milliseconds.
	 */
	synchronized void registerLastRunIntervalTime(long totalRunTime,
			long startOffset, long endOffset) {
		this.lastTotalRunTime = totalRunTime;
		this.lastStartOffset = startOffset;
		this.lastEndOffset = endOffset;
	}

	/**
	 * Reports on the results of the last interval.
	 * 
	 * @param description
	 *            Description of the interval.
	 * @param timeIntervalSeconds
	 *            Time interval in seconds.
	 * @param out
	 *            {@link PrintStream} to write the results.
	 * @return {@link LoadSummary}.
	 */
	synchronized LoadSummary reportLastIntervalResults(String description,
			int timeIntervalSeconds, PrintStream out) {

		// Count the number of requests and reconnects
		int minRequestsCount = Integer.MAX_VALUE;
		int maxRequestsCount = -1;
		int requestsCount = 0;
		int reconnectCount = 0;
		for (Connection connection : this.connections) {
			int requestCount = connection.getRequestCount();
			if (minRequestsCount > requestCount) {
				minRequestsCount = requestCount;
			}
			if (maxRequestsCount < requestCount) {
				maxRequestsCount = requestCount;
			}
			requestsCount += requestCount;
			reconnectCount += connection.getReconnectCount();
		}
		int averageRequestCount = (requestsCount / Math.max(1,
				this.getConnectionCount()));

		// Determine the number of failed connections
		int failedConnections = 0;
		for (Connection connection : this.connections) {
			if (connection.isFailed()) {
				failedConnections++;
			}
		}

		// Provide throughput summary
		out.println("LOAD: " + this.description + " ["
				+ this.getConnectionCount() + " connections / "
				+ failedConnections + " failed / " + reconnectCount
				+ " reconnects / requests min=" + minRequestsCount + " avg="
				+ averageRequestCount + " max=" + maxRequestsCount + "] [time "
				+ this.lastTotalRunTime + " / start " + this.lastStartOffset
				+ " / end " + this.lastEndOffset + "]");

		// Create the load summary
		LoadSummary loadSummary = new LoadSummary(this.description,
				this.getConnectionCount(), failedConnections);

		// Provide summary of interval
		for (Request request : this.requests) {

			// Provide throughput summary
			String requestUri = request.getRequestUri();
			int requestsServiced = request.getNumberOfRequestsServiced();
			out.print("\t" + requestUri + ": " + requestsServiced
					+ " requests ");

			// Summarise the request for the load
			RequestSummary requestSummary = loadSummary.addRequest(requestUri,
					requestsServiced);

			// Provide latency summary
			for (double percentile : this.runner.getPecentileServiceTimes()) {
				long percentileServiceTime = request
						.getPercentileServiceTime(percentile);
				out.print(" "
						+ ((int) (percentile * 100))
						+ "%="
						+ serviceTimeFormat
								.format(percentileServiceTime / 1000000.0)
						+ "ms");

				// Summarise the request servicing time
				requestSummary.addPercentileServiceTime(percentileServiceTime);
			}
			out.println();
		}

		// Return the load summary
		return loadSummary;
	}

}