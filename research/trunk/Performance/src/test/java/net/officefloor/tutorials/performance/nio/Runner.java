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
import java.net.InetAddress;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

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
	 * Target {@link InetAddress} to send requests.
	 */
	private final InetAddress targetAddress;

	/**
	 * Port to send requests.
	 */
	private final int port;

	/**
	 * Percentile service times to report.
	 */
	private final double[] percentileServiceTimes;

	/**
	 * {@link LoadCoordinator}.
	 */
	private final LoadCoordinator coordinator = new LoadCoordinator();

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
		this.port = port;
		this.percentileServiceTimes = percentileServiceTimes;

		// Determine the Inet Address
		this.targetAddress = InetAddress.getByName(host);
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
	 * @throws Exception
	 *             If fails to add {@link Load}.
	 */
	public Load addLoad(String description, boolean isDisconnectAfterSequence,
			Request... requests) throws Exception {

		// Create and add the load
		Load load = new Load(description, this, isDisconnectAfterSequence,
				requests);
		this.loads.add(load);

		// Register with the load coordinator
		this.coordinator.registerLoad(load);

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
	 * @param listener
	 *            {@link RunListener}. May be <code>null</code>.
	 * @return {@link LoadSummary} instances for run interval.
	 * @throws Exception
	 *             If a request fails.
	 */
	public LoadSummary[] runInterval(String description,
			int timeIntervalSeconds, RunListener listener) throws Exception {

		// Ensure do not run after flagged to stop
		if (this.coordinator.isStopping()) {
			throw new IOException(this.getClass().getSimpleName() + " stopped");
		}

		// Reset for run
		for (Load load : this.loads) {
			load.reset();
		}

		// Indicate running
		System.out.println("---------------------- " + description + " ["
				+ timeIntervalSeconds + " secs] " + " ----------------------");

		// Run the interval
		this.coordinator.runInterval(timeIntervalSeconds, listener, false);

		// Report results of interval
		LoadSummary[] loadSummary = this.reportLastIntervalResults(description,
				timeIntervalSeconds, System.out);

		// Return the load summary
		return loadSummary;
	}

	/**
	 * Stops the runner and closes all {@link Connection} instances.
	 * 
	 * @throws Exception
	 *             If fails to stop.
	 */
	public synchronized void stop() throws Exception {
		this.coordinator.stop();
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
	 * @return {@link LoadSummary} instances.
	 */
	LoadSummary[] reportLastIntervalResults(String description,
			int timeIntervalSeconds, PrintStream out) {

		// Provide summary of interval
		LoadSummary[] summaries = new LoadSummary[this.loads.size()];
		int summaryIndex = 0;
		for (Load load : this.loads) {
			summaries[summaryIndex++] = load.reportLastIntervalResults(
					description, timeIntervalSeconds, out);
		}
		out.println("------------------------------------------------------------");

		// Return the summaries
		return summaries;
	}

	/**
	 * Obtains the target {@link InetAddress} to send requests.
	 * 
	 * @return Target {@link InetAddress} to send requests.
	 */
	InetAddress getTargetAddress() {
		return this.targetAddress;
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
	 * Obtains the {@link LoadCoordinator}.
	 * 
	 * @return {@link LoadCoordinator}.
	 */
	LoadCoordinator getLoadCoordinator() {
		return this.coordinator;
	}

	/**
	 * Obtains the percentile service times to report.
	 * 
	 * @return Percentile service times to report.
	 */
	public double[] getPecentileServiceTimes() {
		return this.percentileServiceTimes;
	}

}