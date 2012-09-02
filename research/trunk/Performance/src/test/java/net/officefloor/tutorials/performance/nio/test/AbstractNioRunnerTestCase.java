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
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import net.officefloor.tutorials.performance.Servicer;
import net.officefloor.tutorials.performance.nio.Load;
import net.officefloor.tutorials.performance.nio.LoadSummary;
import net.officefloor.tutorials.performance.nio.Request;
import net.officefloor.tutorials.performance.nio.RequestSummary;
import net.officefloor.tutorials.performance.nio.Runner;

/**
 * Tests the performance.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractNioRunnerTestCase extends TestCase {

	/**
	 * {@link Set} of warmed {@link Servicer} instances to only warm up once.
	 */
	private static final Set<Class<?>> warmedServicers = new HashSet<Class<?>>();

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
		this.doPerformanceTest(1000, 100);
	}

	/**
	 * Tests performance with a magnitude more Database load.
	 */
	public void testPerformance_MagnitudeMoreDatabase() throws Throwable {
		this.doPerformanceTest(100, 1000);
	}

	/**
	 * Tests performance with only CPU load.
	 */
	public void testPerformance_OnlyCpu() throws Throwable {
		this.doPerformanceTest(1000, 0);
	}

	/**
	 * Tests performance with only Database load.
	 */
	public void testPerformance_OnlyDatabase() throws Throwable {
		this.doPerformanceTest(0, 1000);
	}

	/**
	 * Tests the performance.
	 */
	public void doPerformanceTest(int cpuSeed, int dbSeed) throws Throwable {

		// Provide details
		int timeIntervalSeconds = 60;
		int runsPerIncrement = 3;
		int maximumNumberOfLoadConnections = 10000;

		// Disconnect after so many requests to avoid throttling
		int requestsRepeatedInSequence = 99;
		boolean isDisconnectAfterSequence = true;

		// Provide maximum priority to runner
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

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
			System.out.print("Starting servicer ...");
			System.out.println();
			servicer.start();
			System.out.println(" started");
			isServicerRequireStopping = true;
		}

		// Start the runner
		Runner runner = new Runner(host, servicer.getPort(), 0.1, 0.5, 0.9,
				0.95, 0.99, 1.0);
		Load cpuLoad = runner.addLoad("cpu", isDisconnectAfterSequence,
				new Request("/test.php?v=N", "n", requestsRepeatedInSequence));
		Load dbLoad = runner.addLoad("db", isDisconnectAfterSequence,
				new Request("/test.php?v=D", "d", requestsRepeatedInSequence));

		try {

			// Establish initial connections
			cpuLoad.addConnections(cpuSeed);
			dbLoad.addConnections(dbSeed);

			// Determine if need warm up
			Class<?> servicerType = servicer.getClass();
			if (!(warmedServicers.contains(servicerType))) {
				// Warm up for 5 times the interval
				runner.runInterval("STARTUP", (5 * timeIntervalSeconds), null);

				// Now warm
				warmedServicers.add(servicerType);
			}

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
						+ " connection magnitude)", timeIntervalSeconds, null);

				// Undertake multiple runs to average results
				LoadSummary[][] runSummaries = new LoadSummary[runsPerIncrement][];
				for (int i = 0; i < runsPerIncrement; i++) {
					runSummaries[i] = runner.runInterval("RUN " + (i + 1),
							timeIntervalSeconds, null);
				}
				System.out.println("RUN SUMMARY: cpu "
						+ cpuLoad.getConnectionCount() + " connections, db "
						+ dbLoad.getConnectionCount() + " connections");
				this.reportRunIntervalLoadSummary("cpu", 0, runSummaries,
						runner);
				this.reportRunIntervalLoadSummary("db", 1, runSummaries, runner);
				System.out
						.println("------------------------------------------------------------");

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

	/**
	 * Reports on a particular {@link Load}.
	 * 
	 * @param loadDescription
	 *            Description of the {@link Load}.
	 * @param loadIndex
	 *            Index of the {@link Load}.
	 * @param runSummaries
	 *            {@link LoadSummary} instances from the runs.
	 * @param runner
	 *            {@link Runner}.
	 */
	private void reportRunIntervalLoadSummary(String loadDescription,
			int loadIndex, LoadSummary[][] runSummaries, Runner runner) {

		// Provide averages for the runs
		int totalServicedRequests = 0;
		double[] percentileServiceTimes = runner.getPecentileServiceTimes();
		long[] totalPercentileServicingTimes = new long[percentileServiceTimes.length];
		for (LoadSummary[] summaries : runSummaries) {

			// Obtain the request summary (as only one request for load)
			LoadSummary cpuSummary = summaries[loadIndex];
			RequestSummary requestSummary = cpuSummary.getRequestSummaries()
					.get(0);

			// Total request services to be averaged
			totalServicedRequests += requestSummary.getServicedRequestCount();

			// Total servicing times to be averaged
			for (int i = 0; i < percentileServiceTimes.length; i++) {
				totalPercentileServicingTimes[i] += requestSummary
						.getPercentileServiceTimes().get(i);
			}
		}
		System.out.print("   " + loadDescription + " summary: "
				+ (totalServicedRequests / runSummaries.length) + " requests ");
		for (int i = 0; i < totalPercentileServicingTimes.length; i++) {
			System.out
					.print(" "
							+ ((int) (percentileServiceTimes[i] * 100))
							+ "%="
							+ Load.serviceTimeFormat
									.format((totalPercentileServicingTimes[i] / runSummaries.length) / 1000000.0)
							+ "ms");
		}
		System.out.println();
	}
}
