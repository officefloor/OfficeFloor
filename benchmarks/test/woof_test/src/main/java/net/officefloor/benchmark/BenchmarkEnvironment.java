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
	 * @param clients           Number of clients.
	 * @param iterations        Number of iterations.
	 * @param pipelineBatchSize Pipeline batch size (maximum number of requests
	 *                          pipelined together).
	 * @throws Exception If failure in stress test.
	 */
	public static void doStressTest(String url, int clients, int iterations, int pipelineBatchSize) throws Exception {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> {

			// Undertake warm up
			try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
				doStressRequests(url, iterations / 10, pipelineBatchSize, 'w', new AsyncHttpClient[] { client });
			}

			// Run load
			AsyncHttpClient[] asyncClients = new AsyncHttpClient[clients];
			for (int i = 0; i < asyncClients.length; i++) {
				asyncClients[i] = Dsl.asyncHttpClient();
			}
			try {

				// Indicate test
				System.out.println();
				System.out.println("STRESS: " + url + " (with " + clients + " clients)");

				// Undertake the warm up
				doStressRequests(url, iterations / 10, pipelineBatchSize, 'w', asyncClients);

				// Capture the start time
				long startTime = System.currentTimeMillis();

				// Undertake the stress test
				doStressRequests(url, iterations, pipelineBatchSize, '.', asyncClients);

				// Capture the completion time
				long endTime = System.currentTimeMillis();

				// Indicate performance
				int totalRequests = iterations * pipelineBatchSize;
				long totalTime = endTime - startTime;
				int requestsPerSecond = (int) ((totalRequests) / (((float) totalTime) / 1000.0));
				System.out.println("\tRequests: " + totalRequests);
				System.out.println("\tTime: " + totalTime + " milliseconds");
				System.out.println("\tReq/Sec: " + requestsPerSecond);
				System.out.println();

			} finally {
				// Close the clients
				for (AsyncHttpClient asyncClient : asyncClients) {
					asyncClient.close();
				}
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
	 * @param clients           {@link AsyncHttpClient} instances.
	 * @throws Exception If failure in stress test.
	 */
	@SuppressWarnings("unchecked")
	private static void doStressRequests(String url, int iterations, int pipelineBatchSize, char progressCharacter,
			AsyncHttpClient[] clients) throws Exception {

		// Calculate the progress marker
		int progressMarker = iterations / 10;

		// Run the iterations
		CompletableFuture<Response>[] futures = new CompletableFuture[clients.length * pipelineBatchSize];
		for (int i = 0; i < iterations; i++) {

			// Indicate progress
			if (i % (progressMarker) == 0) {
				System.out.print(progressCharacter);
				System.out.flush();
			}

			// Run the iteration
			for (int p = 0; p < pipelineBatchSize; p++) {
				for (int c = 0; c < clients.length; c++) {

					// Determine the index
					int index = (c * pipelineBatchSize) + p;

					// Undertake the request
					futures[index] = clients[c].prepareGet(url).execute().toCompletableFuture();
				}
			}

			// Ensure all responses are valid
			CompletableFuture.allOf(futures).get();
			for (CompletableFuture<Response> future : futures) {
				assertEquals("Request should be successful", 200, future.get().getStatusCode());
			}
		}

		// End progress output
		System.out.println();
	}

}