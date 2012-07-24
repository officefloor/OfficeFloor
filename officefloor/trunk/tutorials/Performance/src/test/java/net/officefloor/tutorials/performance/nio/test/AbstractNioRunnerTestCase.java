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
package net.officefloor.tutorials.performance.nio.test;

import java.text.DateFormat;
import java.util.Date;

import junit.framework.TestCase;
import net.officefloor.tutorials.performance.Servicer;
import net.officefloor.tutorials.performance.nio.Load;
import net.officefloor.tutorials.performance.nio.Request;
import net.officefloor.tutorials.performance.nio.Runner;

/**
 * Tests the performance.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractNioRunnerTestCase extends TestCase {

	/**
	 * System property to obtain the target host.
	 */
	public static final String PROPERTY_TARGET_HOST = "target.host";

	/**
	 * Creates the {@link Servicer}.
	 * 
	 * @return {@link Servicer}.
	 */
	protected abstract Servicer createServicer();

	/**
	 * Tests performance with the same load between CPU and Database.
	 */
	public void testPerformance_SameLoad() throws Throwable {
		this.doPerformanceTest(1, 1);
	}

	/**
	 * Tests performance with a magnitude more CPU load.
	 */
	public void testPerformance_MagnitudeMoreCpu() throws Throwable {
		this.doPerformanceTest(10, 1);
	}

	/**
	 * Tests performance with a magnitude more Database load.
	 */
	public void testPerformance_MagnitudeMoreDatabase() throws Throwable {
		this.doPerformanceTest(1, 10);
	}

	/**
	 * Tests the performance.
	 */
	public void doPerformanceTest(int cpuSeed, int dbSeed) throws Throwable {

		// Provide details
		int timeIntervalSeconds = 60;
		int runsPerIncrement = 10;
		int maximumNumberOfLoadConnections = 10000;

		// Indicate starting
		System.out.println();
		System.out.println();
		System.out.println();
		System.out
				.println("===============================================================");
		System.out.println("Starting " + this.getClass().getSimpleName() + "."
				+ this.getName() + " with interval of " + timeIntervalSeconds
				+ " secs, cpu seed=" + cpuSeed + ", db seed=" + dbSeed);
		System.out
				.println("===============================================================");
		System.out.println("START: "
				+ DateFormat.getDateTimeInstance().format(new Date()));

		// Create the servicer
		Servicer servicer = this.createServicer();

		// Obtain the host (and start servicer if local host)
		String host = System.getProperty(PROPERTY_TARGET_HOST, "localhost");
		boolean isServicerRequireStopping = false;
		if ("localhost".equals(host)) {
			// Start the servicer
			servicer.start();
			isServicerRequireStopping = true;
		}

		// Start the runner
		Runner runner = new Runner(host, servicer.getPort(), 0.1, 0.5, 0.9,
				0.95, 0.99);
		Load cpuLoad = runner.addLoad("cpu", true, new Request("/info.php?v=N",
				"n", 10));
		Load dbLoad = runner.addLoad("db", true, new Request("/info.php?v=Y",
				"y", 10));

		try {

			// Establish initial connections
			cpuLoad.addConnections(cpuSeed);
			dbLoad.addConnections(dbSeed);

			// Warm up for 5 times the interval
			runner.runInterval("STARTUP", (5 * timeIntervalSeconds));

			// Undertake the runs
			boolean isFinished = false;
			do {

				// Obtain current magnitude of connections
				int currentMagnitudeConnectionCount = Math.max(
						cpuLoad.getConnectionCount(),
						dbLoad.getConnectionCount());

				// Undertake warm up
				System.out.println();
				System.out.println();
				runner.runInterval("WARM NEW THREADS ("
						+ currentMagnitudeConnectionCount
						+ " connection magnitude)", timeIntervalSeconds);

				// Undertake multiple runs to average results
				for (int i = 0; i < runsPerIncrement; i++) {
					runner.runInterval("RUN " + (i + 1), timeIntervalSeconds);
				}

				// Obtain the next magnitude of connections
				int nextMagnitudeConnectionCount = currentMagnitudeConnectionCount * 10;

				// Determine if finished
				if ((nextMagnitudeConnectionCount > maximumNumberOfLoadConnections)
						|| (nextMagnitudeConnectionCount > servicer
								.getMaximumConnectionCount())) {
					// No further runs as reached maximum
					return;
				}

				// Increment the connections by another magnitude
				cpuLoad.addConnections((cpuLoad.getConnectionCount() * 10)
						- cpuLoad.getConnectionCount());
				dbLoad.addConnections((dbLoad.getConnectionCount() * 10)
						- dbLoad.getConnectionCount());

			} while (!isFinished);

		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;

		} finally {
			System.out.println("END: "
					+ DateFormat.getDateTimeInstance().format(new Date()));
			try {
				// Stop all connections
				runner.stop();
			} finally {
				// Stop servicer
				if (isServicerRequireStopping) {
					servicer.stop();
				}
			}
		}
	}
}