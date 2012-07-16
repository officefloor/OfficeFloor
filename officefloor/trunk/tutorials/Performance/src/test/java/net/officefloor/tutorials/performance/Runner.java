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
package net.officefloor.tutorials.performance;

import org.apache.http.client.methods.HttpGet;

/**
 * Runs the {@link RequestInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public class Runner {

	/**
	 * Clients.
	 */
	private final Client[] clients;

	/**
	 * Initiate.
	 * 
	 * @param clients
	 *            Number of {@link Client} instances.
	 * @param connectionsPerClient
	 *            Number of {@link Connection} instances per {@link Client}.
	 * @param iterations
	 *            Number of iterations through the {@link Request} sequence.
	 * @param staticRequests
	 *            Number of static (low overhead) {@link Request} instances.
	 */
	public Runner(int clients, int connectionsPerClient, int iterations,
			int staticRequests) {

		// Create the requests (first is dynamic then rest static)
		Request[] requests = new Request[staticRequests + 1];
		requests[0] = new Request(new HttpGet("http://localhost:8080/dynamic"),
				'd');
		for (int i = 1; i < requests.length; i++) {
			requests[i] = new Request(new HttpGet(
					"http://localhost:8080/static"), 's');
		}

		// Create the clients
		this.clients = new Client[clients];
		for (int i = 0; i < this.clients.length; i++) {
			this.clients[i] = new Client(requests, iterations,
					connectionsPerClient);
		}
	}

	/**
	 * Runs requests for the particular {@link Servicer}.
	 * 
	 * @param servicer
	 *            {@link Servicer}.
	 * @param threads
	 *            Number of servicing {@link Thread} instances to use by the
	 *            {@link Servicer}.
	 * @return Results.
	 * @throws InterruptedException
	 *             If interrupted.
	 */
	public RequestInstance[][][] run(Servicer servicer, int threads)
			throws InterruptedException {

		// Start the servicer
		servicer.start(threads);

		// Allow time to come up
		Thread.sleep(1000);

		// Warm up the servicer
		// for (int i = 0; i < this.clients.length; i++) {
		// this.clients[i].triggerWarmUp();
		// }
		// for (int i = 0; i < this.clients.length; i++) {
		// this.clients[i].waitUntilComplete();
		// }

		// Run the requests
		RequestInstance[][][] results = new RequestInstance[this.clients.length][][];
		for (int i = 0; i < this.clients.length; i++) {
			this.clients[i].triggerRun(servicer.isHandleRequests());
		}
		for (int i = 0; i < this.clients.length; i++) {
			results[i] = this.clients[i].waitUntilComplete();
		}

		// Stop the servicer
		servicer.stop();

		// Return the results
		return results;
	}

}