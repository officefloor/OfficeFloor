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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.officefloor.plugin.web.http.security.integrate.HttpSecurityIntegrateTest.Servicer;

/**
 * Runner to make requests.
 * 
 * @author Daniel Sagenschneider
 */
public class Runner extends TestCase {

	/**
	 * Loads on the {@link Servicer}.
	 */
	private final List<Load> loads = new ArrayList<Load>();

	/**
	 * Host to send requests.
	 */
	private final String host;

	/**
	 * Port to send requests.
	 */
	private final int port;

	/**
	 * Percentile service times to report.
	 */
	private final double[] percentileServiceTimes;

	/**
	 * {@link Selector}.
	 */
	private final Selector selector;

	/**
	 * Unique failures.
	 */
	private final Set<String> uniqueFailures = new HashSet<String>();

	/**
	 * Initiate.
	 * 
	 * @param host
	 *            Name of host to run requests against.
	 * @param port
	 *            Port on host to run requests against.
	 * @param percentileServiceTimes
	 *            Percentile service times to report.
	 * @throws IOException
	 *             If fails to open {@link Selector}.
	 */
	public Runner(String host, int port, double... percentileServiceTimes)
			throws IOException {
		this.host = host;
		this.port = port;
		this.percentileServiceTimes = percentileServiceTimes;
		this.selector = Selector.open();
	}

	/**
	 * Adds load by sending request.
	 * 
	 * @param uri
	 *            URI of request.
	 * @param expectedResponseContent
	 *            Expected response content.
	 * @return {@link Load} added.
	 * @throws IOException
	 *             If fails to add {@link Load}.
	 */
	public Load addLoad(String uri, String expectedResponseContent)
			throws IOException {

		// Ensure uri begins with slash
		uri = (uri.startsWith("/") ? uri : "/" + uri);

		// Create the request
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(content, Connection.CHARSET);
		writer.write("GET " + uri + " HTTP/1.1\r\nHost: test\r\n\r\n");
		writer.flush();
		ByteBuffer request = ByteBuffer.allocateDirect(1024);
		request.put(content.toByteArray());
		request.flip();

		// Create and add the load
		Load load = new Load(this, uri, request, expectedResponseContent);
		this.loads.add(load);

		// Return the load
		return load;
	}

	/**
	 * Runs interval reporting results to System out.
	 * 
	 * @param description
	 *            Description of the interval.
	 * @param timeIntervalSeconds
	 *            Time interval in seconds.
	 * @throws IOException
	 *             If a request fails.
	 */
	public void runInterval(String description, int timeIntervalSeconds)
			throws IOException {
		this.runInterval(description, timeIntervalSeconds, true, System.out,
				false);
	}

	/**
	 * Runs for the specified period of time.
	 * 
	 * @param description
	 *            Description of the interval.
	 * @param timeIntervalSeconds
	 *            Time interval in seconds.
	 * @param isReport
	 *            Indicates whether to report.
	 * @param out
	 *            {@link PrintStream}.
	 * @param isJustEstablishConnections
	 *            Indicates whether to just establish connections.
	 * @throws IOException
	 *             If a request fails.
	 */
	void runInterval(String description, int timeIntervalSeconds,
			boolean isReport, PrintStream out,
			boolean isJustEstablishConnections) throws IOException {

		// Reset for run
		for (Load load : this.loads) {
			load.reset();
		}

		// Convert time interval to milliseconds
		long timeIntervalMilliseconds = (timeIntervalSeconds * 1000);

		long startTime = System.currentTimeMillis();
		long nextConnectionCheckTime = startTime + 1000;
		for (;;) {

			// Select next keys
			this.selector.select(1000);

			// Determine if just connecting or sending request
			if (isJustEstablishConnections) {
				// Wait until all connections are established
				if (System.currentTimeMillis() > nextConnectionCheckTime) {

					// Determine if all connections established
					boolean isAllConnected = true;
					for (Load load : this.loads) {
						if (!(load.isAllConnected())) {
							isAllConnected = false;
						}
					}
					if (isAllConnected) {
						return; // all connected
					}

					// Setup for next check
					nextConnectionCheckTime = System.currentTimeMillis() + 1000;
				}

			} else {
				// Determine if time is up
				if ((System.currentTimeMillis() - startTime) > timeIntervalMilliseconds) {

					// Report results of interval
					if (isReport) {
						this.reportLastIntervalResults(description,
								timeIntervalSeconds, out);
					}

					// Time is up (do no process any further results)
					return;
				}
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
							throw new IOException(
									"Failed to establish connection");
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
							}
						}

						// Determine if received response
						if (key.isReadable()) {
							if (connection.readResponse()) {
								key.interestOps(SelectionKey.OP_WRITE);
							}
						}
					}

				} catch (IOException ex) {

					// Provide each unique exception
					String exceptionIdentifier = ex.getClass().getSimpleName()
							+ " - " + ex.getMessage();
					if (!(this.uniqueFailures.contains(exceptionIdentifier))) {
						this.uniqueFailures.add(exceptionIdentifier);

						// Provide the exception
						ex.printStackTrace(out == null ? System.out : out);
					}

					// Clean up connection
					connection.connectionFailed();
					key.cancel();
					key.channel().close();
				}
			}

			// Clear selected keys for next selection
			this.selector.selectedKeys().clear();
		}
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
	 */
	void reportLastIntervalResults(String description, int timeIntervalSeconds,
			PrintStream out) {

		// Provide summary of interval
		out.println("---------------------- " + description + " ["
				+ timeIntervalSeconds + " secs] " + " ----------------------");
		for (Load load : this.loads) {

			// Provide throughput summary
			out.print(load.getRequestUri() + " [" + load.getConnectionCount()
					+ " connections / " + load.getFailedConnectionCount()
					+ " failed] : " + load.getNumberOfRequestsServiced()
					+ " requests");

			// Provide latency summary
			for (double percentile : this.percentileServiceTimes) {
				long percentileServiceTime = load
						.getPercentileServiceTime(percentile);
				out.print(" " + ((int) (percentile * 100)) + "%= "
						+ (percentileServiceTime / 1000000L) + " ms ["
						+ percentileServiceTime + " ns]");
			}
			out.println();
		}
		out.println("------------------------------------------------------------");
	}

	/**
	 * Obtains the name of the host to send requests.
	 * 
	 * @return Name of host to send requests.
	 */
	String getHostName() {
		return this.host;
	}

	/**
	 * Obtains the port on the host to send requests.
	 * 
	 * @return Port on the host to send requests.
	 */
	int getPort() {
		return this.port;
	}

	/**
	 * Obtains the {@link Selector}.
	 * 
	 * @return {@link Selector}.
	 */
	Selector getSelector() {
		return this.selector;
	}

}