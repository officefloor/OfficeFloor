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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.officefloor.plugin.socket.server.Server;

/**
 * Load on the {@link Server}.
 * 
 * @author Daniel Sagenschneider
 */
public class Load {

	/**
	 * Containing {@link Runner}.
	 */
	private final Runner runner;

	/**
	 * Listing of {@link Connection} instances for this {@link Load}.
	 */
	private final List<Connection> connections = new ArrayList<Connection>();

	/**
	 * Request URI.
	 */
	private final String requestUri;

	/**
	 * {@link ByteBuffer} containing the request for this {@link Load}.
	 */
	private final ByteBuffer request;

	/**
	 * Expected content of the response.
	 */
	private final String expectedResponseContent;

	/**
	 * Servicing times of the responses within an run interval.
	 */
	private final List<Long> serviceTimes = new ArrayList<Long>(10000);

	/**
	 * Initiate.
	 * 
	 * @param runner
	 *            Containing {@link Runner}.
	 * @param requestUri
	 *            Request URI.
	 * @param request
	 *            {@link ByteBuffer} containing the request for this
	 *            {@link Load}.
	 * @param expectedResponseContent
	 *            Expected content of the response.
	 */
	Load(Runner runner, String requestUri, ByteBuffer request,
			String expectedResponseContent) {
		this.requestUri = requestUri;
		this.runner = runner;
		this.request = request;
		this.expectedResponseContent = expectedResponseContent;
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
						+ this.requestUri + " (to avoid SYNC attack)";
				System.out.println(message);
				this.runner.runInterval(message, -1, false, null, true);
			}
		}

		// Connect remaining connections
		String message = "Establishing all connections for " + this.requestUri
				+ " (to avoid SYNC attack)";
		System.out.println(message);
		this.runner.runInterval(message, -1, false, null, true);
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

		// Trigger open of connection
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		Socket socket = channel.socket();
		socket.setSoTimeout(0); // wait forever
		socket.setTcpNoDelay(false);
		channel.connect(new InetSocketAddress(this.runner.getHostName(),
				this.runner.getPort()));

		// Create the connection
		Connection connection = new Connection(this, channel);

		// Register connection with selector to start requesting
		channel.register(this.runner.getSelector(), SelectionKey.OP_CONNECT,
				connection);

		// Add the connection
		this.connections.add(connection);
	}

	/**
	 * Reset for the next run interval.
	 */
	void reset() {
		this.serviceTimes.clear();
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
	 * Obtains the request URI for this load.
	 * 
	 * @return Request URI for this load.
	 */
	String getRequestUri() {
		return this.requestUri;
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
	 * Obtains the number of requests serviced.
	 * 
	 * @return Number of requests serviced.
	 */
	int getNumberOfRequestsServiced() {
		return this.serviceTimes.size();
	}

	/**
	 * Obtains the minimum servicing time that input percentage of requests were
	 * serviced within.
	 * 
	 * @param percentage
	 *            Percentage.
	 * @return Minimum servicing time that input percentage of requests were
	 *         serviced within.
	 */
	long getPercentileServiceTime(double percentage) {

		// Sort servicing times
		Collections.sort(this.serviceTimes);

		// Return the percentile service time
		if (this.serviceTimes.size() == 0) {
			return -1;
		} else {
			return this.serviceTimes
					.get((int) (this.serviceTimes.size() * percentage));
		}
	}

	/**
	 * Obtains the {@link ByteBuffer} containing the request.
	 * 
	 * @return {@link ByteBuffer} containing the request.
	 */
	ByteBuffer getRequest() {
		return this.request;
	}

	/**
	 * Obtains the expected response content.
	 * 
	 * @return Expected response content.
	 */
	String getExpectedResponseContent() {
		return this.expectedResponseContent;
	}

	/**
	 * Records the servicing of a request.
	 * 
	 * @param requestStart
	 *            Time in nanoseconds of request start.
	 * @param requestEnd
	 *            Time in nanoseconds of request end.
	 * @throws IOException
	 *             If invalid time.
	 */
	void requestServiced(long requestStart, long requestEnd) throws IOException {

		// Determine the service time
		long serviceTime;
		if (requestStart > requestEnd) {
			throw new IOException("Time recording invalid [" + requestStart
					+ ", " + requestEnd + "]");
		}
		if (requestStart < 0) {
			if (requestEnd < 0) {
				// Both start and end time negative
				serviceTime = Math.abs(requestStart) - Math.abs(requestEnd);
			} else {
				// Start negative but end time positive
				serviceTime = Math.abs(requestStart) + requestEnd;
			}
		} else {
			// Both start and end time positive
			serviceTime = requestEnd - requestStart;
		}

		// Add the service time
		this.serviceTimes.add(Long.valueOf(serviceTime));
	}

	/**
	 * Connection failed.
	 */
	void connectionFailed() {

	}

}