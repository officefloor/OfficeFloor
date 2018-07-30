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
package net.officefloor.report;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.reactiverse.pgclient.PgRowSet;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 *
 * @author Daniel Sagenschneider
 */
public class ReactivePgClientTest extends OfficeFrameTestCase {

	public void testReactive() throws Exception {

		// Pool options
		PgPoolOptions options = new PgPoolOptions().setPort(5432).setHost("localhost").setDatabase("postgres")
				.setUser("postgres").setPassword("test").setMaxSize(5);

		// Create the client pool
		PgPool client = PgClient.pool(options);

		// Allow indicating complete
		int[] completed = new int[] { 0 };

		// Run multiple queries
		int RUN_COUNT = 1;
		for (int i = 0; i < RUN_COUNT; i++) {

			// A simple query
			client.preparedQuery("SELECT ID, INVOICE_DETAILS FROM INVOICE", ar -> {
				if (ar.succeeded()) {
					PgRowSet result = ar.result();
					result.forEach((row) -> {
						System.out.println("  " + row.getInteger(0) + " -> " + row.getString(1));
					});
				} else {
					System.out.println("Failure: " + ar.cause().getMessage());
					ar.cause().printStackTrace();
				}

				// No complete
				synchronized (completed) {
					completed[0]++;
					if (completed[0] >= RUN_COUNT) {

						// Now close the pool
						client.close();

						// Notify complete
						completed.notify();
					}
				}
			});
		}

		// Wait until complete
		synchronized (completed) {
			while (completed[0] < RUN_COUNT) {
				completed.wait(1000);
			}
		}
	}

}