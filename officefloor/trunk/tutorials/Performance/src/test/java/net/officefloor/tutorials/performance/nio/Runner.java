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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.officefloor.tutorials.performance.Servicer;

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
	 * Indicates stopping.
	 */
	private boolean isStopping = false;

	/**
	 * Indicates when stopped.
	 */
	private boolean isStopped = false;

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
	 * Adds load by sending requests sequentially.
	 * 
	 * @param description
	 *            Description of the load.
	 * @param isDisconnectAfterSequence
	 *            Indicates if to disconnect after the sequence of
	 *            {@link Request} instances are serviced.
	 * @param requests
	 *            {@link Request} instances for the load.
	 * @return {@link Load} added.
	 * @throws IOException
	 *             If fails to add {@link Load}.
	 */
	public Load addLoad(String description, boolean isDisconnectAfterSequence,
			Request... requests) throws IOException {

		// Create and add the load
		Load load = new Load(description, this, isDisconnectAfterSequence,
				requests);
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
	synchronized void runInterval(String description, int timeIntervalSeconds,
			boolean isReport, PrintStream out,
			boolean isJustEstablishConnections) throws IOException {

		// Ensure do not run after stopped
		if (this.isStopped) {
			throw new IOException(this.getClass().getSimpleName() + " stopped");
		}

		// Reset for run
		for (Load load : this.loads) {
			load.reset();
		}

		// Indicate running
		if (isReport) {
			out.println("---------------------- " + description + " ["
					+ timeIntervalSeconds + " secs] "
					+ " ----------------------");
		}

		// Convert time interval to milliseconds
		long timeIntervalMilliseconds = (timeIntervalSeconds * 1000);

		long startTime = System.currentTimeMillis();
		long nextConnectionCheckTime = startTime + 1000;
		do {
		} while (this.runSelect(description, startTime,
				timeIntervalMilliseconds, nextConnectionCheckTime,
				isJustEstablishConnections, isReport, out));
	}

	/**
	 * Runs a single select and processes the {@link SelectionKey} instances.
	 * 
	 * @return <code>true</code> if should do another select.
	 * @throws IOException
	 *             If failed.
	 */
	private boolean runSelect(String description, long startTime,
			long timeIntervalMilliseconds, long nextConnectionCheckTime,
			boolean isJustEstablishConnections, boolean isReport,
			PrintStream out) throws IOException {

		// Select next keys
		this.selector.select(1000);

		// Indicate if stopped
		if ((this.isStopping) && (this.selector.keys().size() == 0)) {
			// No more keys (all connections closed), so stopped
			this.isStopped = true;
			return false;
		}

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
					return false; // all connected no more selects required
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
							(int) (timeIntervalMilliseconds / 1000), out);
				}

				// Time is up (do no process any further results)
				return false;
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

				// Attempt to re-establish connection
				connection.establishNewConnection();
			}
		}

		// Clear selected keys for next selection
		this.selector.selectedKeys().clear();

		// As here, need another select
		return true;
	}

	/**
	 * Stops the runner and closes all {@link Connection} instances.
	 * 
	 * @throws IOException
	 *             If fails to stop.
	 */
	public synchronized void stop() throws IOException {

		// Flag that stopping
		this.isStopping = true;

		// Flag all connections to stop
		for (Load load : this.loads) {
			load.stop();
		}

		// Keep looping until stopped
		System.out.print("STOPPING");
		System.out.flush();
		do {
			long startTime = System.currentTimeMillis();
			long timeIntervalMilliseconds = 1 * 1000; // 1 second
			do {
			} while (this.runSelect("STOPPING", startTime,
					timeIntervalMilliseconds, timeIntervalMilliseconds, false,
					false, null));
			System.out.print(".");
			System.out.flush();
		} while (!this.isStopped);
		System.out.println("STOPPED");
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
		for (Load load : this.loads) {
			load.reportLastIntervalResults(description, timeIntervalSeconds,
					out);
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

	/**
	 * Obtains the percentile service times to report.
	 * 
	 * @return Percentile service times to report.
	 */
	double[] getPecentileServiceTimes() {
		return this.percentileServiceTimes;
	}

}