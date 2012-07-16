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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;

/**
 * Connection to the {@link Servicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class Connection implements Runnable {

	/**
	 * {@link Client} that {@link Connection} is associated.
	 */
	private final Client client;

	/**
	 * {@link HttpClient}.
	 */
	private HttpClient httpClient = new DefaultHttpClient();

	/**
	 * Initiate.
	 * 
	 * @param client
	 *            {@link Client}.
	 */
	public Connection(Client client) {
		this.client = client;
	}

	/*
	 * ================== Runnable ==========================
	 */

	@Override
	public void run() {
		try {
			for (;;) {

				// Keep making requests until interrupted
				RequestInstance instance = this.client.nextRequest();
				Request request = instance.getRequest();

				// TODO re-establish connection on new request iteration

				// Obtain the start time
				long startTime = System.nanoTime();

				// Make the request (if required)
				HttpResponse response = null;
				if (request != null) {
					response = this.httpClient
							.execute(request.getHttpRequest());
				}

				// Obtain the end time
				long endTime = System.nanoTime();

				// Validate the response (if made)
				if (response != null) {
					Assert.assertEquals("Should be successful", 200,
							response.getStatusLine());

					// Ensure correct response
					HttpEntity entity = response.getEntity();
					int value = entity.getContent().read();
					Assert.assertEquals("Incorrect response",
							request.getExpectedResponse(), (char) value);
					entity.consumeContent();
				}

				// Notify completed request instance
				instance.complete(startTime, endTime);

			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			return;

		} catch (Exception ex) {
			// TODO provide better detail of error
			ex.printStackTrace();
		}
	}

}