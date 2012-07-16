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

import junit.framework.TestCase;
import net.officefloor.tutorials.performance.RequestInstance;
import net.officefloor.tutorials.performance.Runner;
import net.officefloor.tutorials.performance.none.NoActionServicer;

/**
 * Validates the {@link Runner}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateRunnerTest extends TestCase {

	/**
	 * {@link Runner}.
	 */
	private Runner runner;

	@Override
	protected void setUp() throws Exception {
		// Create the runner
		int numberOfClients = 10000;
		int connectionsPerClient = 2;
		int numberOfIterations = 100;
		int numberOfStaticRequests = 3;
		this.runner = new Runner(numberOfClients, connectionsPerClient,
				numberOfIterations, numberOfStaticRequests);
	}

	/**
	 * Test no action.
	 */
	public void testNoAction() throws Exception {

		// Run the request
		int numberOfThreads = 1;
		RequestInstance[][][] results = this.runner.run(new NoActionServicer(),
				numberOfThreads);

		// Obtain the key details
		int numberOfRequests = 0;
		long minStartTime = Long.MAX_VALUE;
		long maxEndTime = -1;
		long minRequestTime = Long.MAX_VALUE;
		long maxRequestTime = -1;
		long totalRequestTime = 0;
		for (int c = 0; c < results.length; c++) {
			for (int i = 0; i < results[c].length; i++) {
				for (int r = 0; r < results[c][i].length; r++) {
					RequestInstance instance = results[c][i][r];

					// Keep track of count of requests
					numberOfRequests++;

					// Obtain request details
					long startTime = instance.getStartTime();
					long endTime = instance.getEndTime();
					long serviceTime = this.getServiceTime(startTime, endTime);

					// Determine the min start time
					if (startTime < minStartTime) {
						minStartTime = startTime;
					}

					// Determine the max end time
					if (endTime > maxEndTime) {
						maxEndTime = endTime;
					}

					// Determine min request time
					if (serviceTime < minRequestTime) {
						minRequestTime = serviceTime;
					}

					// Determine max request time
					if (serviceTime > maxRequestTime) {
						maxRequestTime = serviceTime;
					}

					// Sum total service time
					totalRequestTime += serviceTime;
				}
			}
		}

		// Determine the average response time
		long averageServiceTime = totalRequestTime / numberOfRequests;

		// Determine the 90th percentile and standard deviation
		long[] serviceTimes = new long[numberOfRequests];
		int index = 0;
		long deviationTotal = 0;
		for (int c = 0; c < results.length; c++) {
			for (int i = 0; i < results[c].length; i++) {
				for (int r = 0; r < results[c][i].length; r++) {
					RequestInstance instance = results[c][i][r];

					// Obtain request details
					long startTime = instance.getStartTime();
					long endTime = instance.getEndTime();
					long serviceTime = this.getServiceTime(startTime, endTime);

					// Load service time
					serviceTimes[index++] = serviceTime;

					// Increment deviation total
					deviationTotal += ((serviceTime - averageServiceTime) * (serviceTime - averageServiceTime));
				}
			}
		}
		Arrays.sort(serviceTimes);
		long ninetithPercentileServiceTime = serviceTimes[(int) (numberOfRequests * 0.90)];
		long standardDeviation = deviationTotal / (numberOfRequests - 1);

		// Provide results
		System.out.println("Serviced " + numberOfRequests + " requests in "
				+ this.getServiceTime(minStartTime, maxEndTime)
				+ " nanoseconds");
		System.out.println("90% of requests serviced in "
				+ ninetithPercentileServiceTime + " nanoseconds");
		System.out.println("Min request " + minRequestTime
				+ " nanoseconds while max request " + maxRequestTime
				+ " nanoseconds with average request " + averageServiceTime
				+ " nanoseconds with standard deviation of "
				+ standardDeviation + " (" + deviationTotal + ")");
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