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
package net.officefloor.tutorials.performance.test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.officefloor.tutorials.performance.Client;
import net.officefloor.tutorials.performance.Connection;
import net.officefloor.tutorials.performance.Request;
import net.officefloor.tutorials.performance.RequestInstance;
import net.officefloor.tutorials.performance.Runner;
import net.officefloor.tutorials.performance.Servicer;

/**
 * Validates the {@link Runner}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRunnerTestCase extends TestCase {

	/**
	 * System property to obtain the target host.
	 */
	public static final String PROPERTY_TARGET_HOST = "target.host";

	/**
	 * Obtains the port on the server.
	 * 
	 * @return Port on the server.
	 */
	protected abstract Integer getServerPort();

	/**
	 * Obtains the {@link Servicer}.
	 * 
	 * @return {@link Servicer}.
	 */
	protected abstract Servicer getServicer();

	/**
	 * {@link Connection} instances per {@link Client}.
	 */
	private final int connectionsPerClient = 1;

	/**
	 * Number of iterations.
	 */
	private final int numberOfIterations = 1000;

	/**
	 * Address of the server.
	 */
	private String serverAddress;

	/**
	 * {@link Servicer}.
	 */
	private Servicer servicer = null;

	/**
	 * Current {@link Runner}.
	 */
	private Runner runner;

	@Override
	protected void setUp() throws Exception {

		// Obtain the server location
		Integer port = this.getServerPort();
		if (port == null) {
			// No location
			this.serverAddress = null;

		} else {
			// Provide location
			String host = System.getProperty(PROPERTY_TARGET_HOST, "localhost");
			this.serverAddress = host + ":" + this.getServerPort().intValue();

			// Start servicer if localhost
			if ("localhost".equals(host)) {
				// Start the servicer
				this.servicer = this.getServicer();
				this.servicer.start();
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the possible current runner
			if (this.runner != null) {
				this.runner.stop();
			}

		} finally {
			// Stop the possible servicer
			if (this.servicer != null) {
				this.servicer.stop();
			}
		}
	}

	/**
	 * Under takes the test for each {@link Servicer}.
	 */
	public void testRuns() throws Exception {

		// Provide mask distribution
		int[] maskDistribution = new int[] { 1 };

		// Obtain the requests
		Request[] requests = new Request[] {
				new Request(this.serverAddress, "info.php?v=N", 'n', 1),
				new Request(this.serverAddress, "info.php?v=Y", 'y', 1) };

		// Do warm up
		this.runner = new Runner(10, this.connectionsPerClient,
				this.numberOfIterations, maskDistribution);
		this.doTestRun("WARM UP", requests);

		for (int i = 0; i < 10; i++) {
			this.doTestRun("AGAIN", requests);
		}
		this.runner.stop();
	}

	/**
	 * Undertakes a test run.
	 * 
	 * @param runDescription
	 *            Description of the run.
	 * @param servicer
	 *            {@link Servicer}.
	 */
	public void doTestRun(String runDescription, Request[] requests)
			throws Exception {

		// Indicate running
		System.out.println("================ "
				+ this.getClass().getSimpleName() + " with "
				+ this.runner.getClientCount() + "["
				+ this.connectionsPerClient + "] clients by "
				+ this.numberOfIterations + " iterations (" + runDescription
				+ ") against " + this.serverAddress
				+ " =========================");

		// Run the request
		RequestInstance[][][] results = this.runner.run(requests);

		// Obtain the key details
		final long[] minStartTime = new long[] { Long.MAX_VALUE };
		final long[] maxEndTime = new long[] { -1 };
		Map<String, SummarisedResult> summarisedResultsMap = new HashMap<String, SummarisedResult>();
		this.summariseResults(results, summarisedResultsMap, new Summarise() {
			@Override
			public void summarise(long startTime, long endTime,
					long serviceTime, Throwable failure, SummarisedResult result) {

				// Determine if failure
				if (failure != null) {
					// Just record failure
					result.numberOfFailures++;
					return;
				}

				// Keep track of count of requests
				result.numberOfRequests++;

				// Determine the min start time
				if (startTime < minStartTime[0]) {
					minStartTime[0] = startTime;
				}

				// Determine the max end time
				if (endTime > maxEndTime[0]) {
					maxEndTime[0] = endTime;
				}

				// Determine min request time
				if (serviceTime < result.minRequestTime) {
					result.minRequestTime = serviceTime;
				}

				// Determine max request time
				if (serviceTime > result.maxRequestTime) {
					result.maxRequestTime = serviceTime;
				}
			}
		});

		// Determine the percentile service times
		this.summariseResults(results, summarisedResultsMap, new Summarise() {
			@Override
			public void summarise(long startTime, long endTime,
					long serviceTime, Throwable failure, SummarisedResult result) {

				// Ensure have request times array
				if (result.requestTimes == null) {
					result.requestTimes = new long[result.numberOfRequests];
				}

				// Determine if failure
				if (failure != null) {
					return; // ignore
				}

				// Load request time
				result.requestTimes[result.nextRequestTimeIndex++] = serviceTime;
			}
		});

		// Obtain the ordered summarised results
		SummarisedResult[] summarisedResults = summarisedResultsMap.values()
				.toArray(new SummarisedResult[summarisedResultsMap.size()]);
		Arrays.sort(summarisedResults, new Comparator<SummarisedResult>() {
			@Override
			public int compare(SummarisedResult a, SummarisedResult b) {
				return a.uri.compareToIgnoreCase(b.uri);
			}
		});

		// Provide results
		int totalNumberOfRequests = 0;
		for (int i = 0; i < summarisedResults.length; i++) {
			totalNumberOfRequests += summarisedResults[i].numberOfRequests;

			// Also sort request times for ceiling percentile values
			Arrays.sort(summarisedResults[i].requestTimes);
		}
		long serviceDuration = (totalNumberOfRequests == 0 ? -1 : this
				.getServiceTime(minStartTime[0], maxEndTime[0]));

		// Provide throughput summary
		System.out.print("Serviced " + totalNumberOfRequests + " requests in "
				+ (serviceDuration / 1000000000L) + " seconds ("
				+ (serviceDuration / 1000000L) + " milliseconds, "
				+ serviceDuration + " nanoseconds");
		for (int i = 0; i < summarisedResults.length; i++) {
			SummarisedResult result = summarisedResults[i];
			System.out.print(i == 0 ? " [" : ",");
			System.out.print(result.uri + " " + result.numberOfRequests + " <"
					+ result.numberOfFailures + ">");
		}
		System.out.println("]");

		// Provide latency summary
		for (int i = 0; i < summarisedResults.length; i++) {
			SummarisedResult result = summarisedResults[i];
			System.out.println("[90%, 95%, 99%] of " + result.uri
					+ " serviced in " + (result.get90RequestTime() / 1000000L)
					+ ", " + (result.get95RequestTime() / 1000000L) + ", "
					+ (result.get99RequestTime() / 1000000L)
					+ " milliseconds (" + result.get90RequestTime() + ", "
					+ result.get95RequestTime() + ", "
					+ result.get99RequestTime() + " nanoseconds)");
		}
	}

	/**
	 * Summarises the results.
	 * 
	 * @param results
	 *            Results to be summarised.
	 * @param summarisedResults
	 *            Loaded with the {@link SummarisedResult} per URI.
	 * @param summarise
	 *            Means to summarise.
	 */
	private void summariseResults(RequestInstance[][][] results,
			Map<String, SummarisedResult> summarisedResults, Summarise summarise) {
		for (int c = 0; c < results.length; c++) {
			for (int i = 0; i < results[c].length; i++) {
				for (int r = 0; r < results[c][i].length; r++) {
					RequestInstance instance = results[c][i][r];

					// Obtain request details
					long startTime = instance.getStartTime();
					long endTime = instance.getEndTime();
					long serviceTime = this.getServiceTime(startTime, endTime);
					Throwable failure = instance.getFailure();

					// Obtain the summarised result for URI
					String uri = instance.getRequest().getUri();
					SummarisedResult summarisedResult = summarisedResults
							.get(uri);
					if (summarisedResult == null) {
						summarisedResult = new SummarisedResult(uri);
						summarisedResults.put(uri, summarisedResult);
					}

					// Summarise results
					summarise.summarise(startTime, endTime, serviceTime,
							failure, summarisedResult);
				}
			}
		}
	}

	/**
	 * Summarised result.
	 */
	private static class SummarisedResult {

		public int numberOfRequests = 0;
		public int numberOfFailures = 0;
		public long minRequestTime = Long.MAX_VALUE;
		public long maxRequestTime = -1;
		public long[] requestTimes = null;
		public int nextRequestTimeIndex = 0;

		/**
		 * URI being summarised.
		 */
		public final String uri;

		/**
		 * Initiate.
		 * 
		 * @param uri
		 *            URI being summarised.
		 */
		public SummarisedResult(String uri) {
			this.uri = uri;
		}

		/**
		 * Obtains time in which 90% of requests were service in.
		 * 
		 * @return Time in which 90% of requests were service in.
		 */
		public long get90RequestTime() {
			if (this.requestTimes.length == 0) {
				return -1;
			} else {
				return this.requestTimes[(int) (this.requestTimes.length * 0.90)];
			}
		}

		/**
		 * Obtains time in which 95% of requests were service in.
		 * 
		 * @return Time in which 95% of requests were service in.
		 */
		public long get95RequestTime() {
			if (this.requestTimes.length == 0) {
				return -1;
			} else {
				return this.requestTimes[(int) (this.requestTimes.length * 0.95)];
			}
		}

		/**
		 * Obtains time in which 99% of requests were service in.
		 * 
		 * @return Time in which 99% of requests were service in.
		 */
		public long get99RequestTime() {
			if (this.requestTimes.length == 0) {
				return -1;
			} else {
				return this.requestTimes[(int) (this.requestTimes.length * 0.99)];
			}
		}
	}

	/**
	 * Summarises results.
	 */
	private static interface Summarise {

		/**
		 * Summarises results for dynamic {@link Request}.
		 * 
		 * @param startTime
		 *            Start time of {@link Request}.
		 * @param endTime
		 *            End time of {@link Request}.
		 * @param serviceTime
		 *            Service time of {@link Request}.
		 * @param failure
		 *            Possible failure. Most likely <code>null</code>.
		 * @param summarisedResult
		 *            Collects the summarised result.
		 */
		void summarise(long startTime, long endTime, long serviceTime,
				Throwable failure, SummarisedResult summarisedResult);
	}

	/**
	 * Obtains the service time.
	 * 
	 * @param startTime
	 *            Start time.
	 * @param endTime
	 *            End time.
	 * @return Service time.
	 */
	private long getServiceTime(long startTime, long endTime) {
		assertTrue("Time recording invalid", (startTime < endTime));
		long serviceTime;
		if (startTime < 0) {
			if (endTime < 0) {
				// Both start and end time negative
				serviceTime = Math.abs(startTime) - Math.abs(endTime);
			} else {
				// Start negative but end time positive
				serviceTime = Math.abs(startTime) + endTime;
			}
		} else {
			// Both start and end time positive
			serviceTime = endTime - startTime;
		}
		return serviceTime;
	}

}