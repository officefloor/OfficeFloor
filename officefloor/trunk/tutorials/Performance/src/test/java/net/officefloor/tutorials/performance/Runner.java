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

import java.text.DateFormat;
import java.util.Date;

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
	 * Number of iterations.
	 */
	private final int numberOfIterations;

	/**
	 * Initiate.
	 * 
	 * @param clients
	 *            Number of {@link Client} instances.
	 * @param connectionsPerClient
	 *            Number of {@link Connection} instances per {@link Client}.
	 * @param iterations
	 *            Number of iterations.
	 */
	public Runner(int clients, int connectionsPerClient, int iterations) {
		this.numberOfIterations = iterations;

		// Create the clients and their connections
		this.clients = new Client[clients];
		for (int i = 0; i < this.clients.length; i++) {
			this.clients[i] = new Client(connectionsPerClient);
		}
	}

	/**
	 * Obtains the number of {@link Client} instances.
	 * 
	 * @return Number of {@link Client} instances.
	 */
	public int getClientCount() {
		return this.clients.length;
	}

	/**
	 * Undertakes run returning results of run.
	 * 
	 * @param requests
	 *            {@link Request} instances for run.
	 * @return Results.
	 * @throws Exception
	 *             If failure in run.
	 */
	public RequestInstance[][][] run(Request... requests) throws Exception {

		// Obtain the date format
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.FULL);

		// Indicate running
		System.out.print("Run started at " + formatter.format(new Date())
				+ " ...");
		System.out.flush();

		// Run the requests
		RequestInstance[][][] results = new RequestInstance[this.clients.length][][];
		for (int i = 0; i < this.clients.length; i++) {
			this.clients[i].triggerNextRun(this.numberOfIterations, requests);
		}
		for (int i = 0; i < this.clients.length; i++) {
			results[i] = this.clients[i].waitUntilComplete();
		}

		// Indicating stopping
		System.out.println(" run complete at " + formatter.format(new Date()));

		// Return the results
		return results;
	}

	/**
	 * Stops the {@link Runner} and releases all {@link Thread} instances.
	 */
	public void stop() {

		// Flag all clients to stop
		for (int i = 0; i < this.clients.length; i++) {
			this.clients[i].flagStop();
		}

		// Wait on the clients to stop
		for (int i = 0; i < this.clients.length; i++) {
			this.clients[i].waitUntilStopped();
		}
	}

}