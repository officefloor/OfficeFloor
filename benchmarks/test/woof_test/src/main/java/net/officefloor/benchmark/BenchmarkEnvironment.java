/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.benchmark;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletableFuture;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;

/**
 * Provides benchmark environment.
 * 
 * @author Daniel Sagenschneider
 */
public class BenchmarkEnvironment {

	/**
	 * Creates {@link PostgreSqlRule} for benchmark.
	 * 
	 * @return {@link PostgreSqlRule}.
	 */
	public static PostgreSqlRule createPostgreSqlRule() {
		return new PostgreSqlRule("tfb-database", 5432, "hello_world", "benchmarkdbuser", "benchmarkdbpass");
	}

	/**
	 * <p>
	 * Undertakes a pipelined stress test.
	 * <p>
	 * This is similar requesting as per the Tech Empower benchmarks.
	 * 
	 * @param url               URL to send requests.
	 * @param iterations        Number of iterations.
	 * @param pipelineBatchSize Pipeline batch size (maximum number of requests
	 *                          pipelined together).
	 * @throws Exception If failure in stress test.
	 */
	public static void doStressTest(String url, int iterations, int pipelineBatchSize) throws Exception {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> {
			try (AsyncHttpClient client = Dsl.asyncHttpClient()) {

				// Indicate test
				System.out.println("STRESS: " + url);

				// Undertake the warm up
				doStressRequests(url, iterations / 10, pipelineBatchSize, 'w', client);

				// Capture the start time
				long startTime = System.currentTimeMillis();

				// Undertake the stress test
				doStressRequests(url, iterations, pipelineBatchSize, '.', client);

				// Capture the completion time
				long endTime = System.currentTimeMillis();

				// Indicate performance
				int totalRequests = iterations * pipelineBatchSize;
				long totalTime = endTime - startTime;
				int requestsPerSecond = (int) ((totalRequests) / (((float) totalTime) / 1000.0));
				System.out.println("\tRequests: " + totalRequests);
				System.out.println("\tTime: " + totalTime + " milliseconds");
				System.out.println("\tReq/Sec: " + requestsPerSecond);
			}
		});
	}

	/**
	 * Undertakes running the requests.
	 * 
	 * @param url               URL to send requests.
	 * @param iterations        Number of iterations.
	 * @param pipelineBatchSize Pipeline batch size (maximum number of requests
	 *                          pipelined together).
	 * @param progressCharacter Character to print out to indicate progress.
	 * @param client            {@link AsyncHttpClient}.
	 * @throws Exception If failure in stress test.
	 */
	@SuppressWarnings("unchecked")
	private static void doStressRequests(String url, int iterations, int pipelineBatchSize, char progressCharacter,
			AsyncHttpClient client) throws Exception {

		// Calculate the progress marker
		int progressMarker = iterations / 10;

		// Run the iterations
		CompletableFuture<Response>[] futures = new CompletableFuture[pipelineBatchSize];
		for (int i = 0; i < iterations; i++) {

			// Indicate progress
			if (i % (progressMarker) == 0) {
				System.out.print(progressCharacter);
				System.out.flush();
			}

			// Undertake pipelining via sending bursts of requests
			for (int p = 0; p < futures.length; p++) {
				futures[p] = client.prepareGet(url).execute().toCompletableFuture();
			}
			CompletableFuture.allOf(futures).get();

			// Ensure all responses are valid
			for (CompletableFuture<Response> future : futures) {
				assertEquals("Request should be successful", 200, future.get().getStatusCode());
			}
		}

		// End progress output
		System.out.println();
	}

}