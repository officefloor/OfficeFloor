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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.socket.server.Server;

/**
 * Load on the {@link Server}.
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
	 */
	Load(String description, Runner runner, boolean isDisconnectAfterSequence,
			Request[] requests) {
		this.description = description;
		this.runner = runner;
		this.isDisconnectAfterSequence = isDisconnectAfterSequence;
		this.requests = requests;
	}

	/**
	 * Adds multiple {@link Connection} instances.
	 * 
	 * @param numberOfConnections
	 *            Number of {@link Connection} instances to add.
	 * @throws IOException
	 *             If fails to add the connections.
	 */
	public void addConnections(int numberOfConnections) throws IOException {
		for (int i = 0; i < numberOfConnections; i++) {
			this.addConnection();

			// Determine if need to start establishing connections
			if (((i + 1) % 500) == 0) {
				String message = "Establshing up to " + i + " connections for "
						+ this.description + " (to avoid SYNC attack)";
				System.out.println(message);
				this.runner.runInterval(message, -1, null, false, null, true);
			}
		}

		// Connect remaining connections
		String message = "Establishing all " + this.getConnectionCount()
				+ " connections for " + this.description
				+ " (to avoid SYNC attack)";
		System.out.println(message);
		this.runner.runInterval(message, -1, null, false, null, true);
	}

	/**
	 * Obtains the number of {@link Connection} instances.
	 * 
	 * @return Number of {@link Connection} instances.
	 */
	public int getConnectionCount() {
		return this.connections.size();
	}

	/**
	 * Adds a new {@link Connection} for this {@link Load}.
	 * 
	 * @throws IOException
	 *             If fails to add {@link Connection}.
	 */
	void addConnection() throws IOException {
		// Add the connection
		this.connections.add(new Connection(this));
	}

	/**
	 * Reset for the next run interval.
	 */
	void reset() {

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
	 * Stops all {@link Connection} instances.
	 */
	void stop() {
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
	 * Indicates if all connected.
	 * 
	 * @return <code>true</code> if all {@link Connection} instances connected.
	 */
	boolean isAllConnected() {

		// Determine if all connected
		for (Connection connection : this.connections) {
			if (!(connection.isConnected())) {
				return false; // not all connected
			}
		}

		// As here, all connected
		return true;
	}

	/**
	 * Obtains the number of failed {@link Connection} instances.
	 * 
	 * @return Number of failed {@link Connection} instances.
	 */
	int getFailedConnectionCount() {
		int failed = 0;
		for (Connection connection : this.connections) {
			if (connection.isFailed()) {
				failed++;
			}
		}
		return failed;
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
	LoadSummary reportLastIntervalResults(String description,
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
		int averageRequestCount = (requestsCount / this.connections.size());

		// Provide throughput summary
		out.println("LOAD: " + this.description + " ["
				+ this.getConnectionCount() + " connections / "
				+ this.getFailedConnectionCount() + " failed / "
				+ reconnectCount + " reconnects / requests min="
				+ minRequestsCount + " avg=" + averageRequestCount + " max="
				+ maxRequestsCount + "]");

		// Create the load summary
		LoadSummary loadSummary = new LoadSummary(this.description,
				this.getConnectionCount(), this.getFailedConnectionCount());

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