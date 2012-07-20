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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
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
	private final HttpClient httpClient = new DefaultHttpClient();

	/**
	 * Indicates if connected.
	 */
	private volatile boolean isConnected = false;

	/**
	 * Indicates if complete.
	 */
	private volatile boolean isComplete = false;

	/**
	 * Initiate.
	 * 
	 * @param client
	 *            {@link Client}.
	 */
	public Connection(Client client) {
		this.client = client;

		// Configure the HTTP client
		this.httpClient.getParams().setIntParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 0);
		this.httpClient.getParams().setIntParameter(
				CoreConnectionPNames.SO_TIMEOUT, 0);
		this.httpClient.getParams().setBooleanParameter(
				CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
	}

	/**
	 * Indicates if connected.
	 * 
	 * @return <code>true</code> if connected.
	 */
	public boolean isConnected() {
		return this.isConnected;
	}

	/**
	 * Indicates if complete.
	 * 
	 * @return <code>true</code> if complete.
	 */
	public boolean isComplete() {
		return this.isComplete;
	}

	/*
	 * ================== Runnable ==========================
	 */

	@Override
	public void run() {
		try {
			for (;;) {
				try {

					// Keep making requests until interrupted
					RequestInstance instance = this.client.nextRequest();
					if (instance == null) {
						// No further requests
						this.httpClient.getConnectionManager().shutdown();
						return;
					}
					Request request = instance.getRequest();
					HttpUriRequest httpRequest = request.getHttpRequest();

					HttpResponse response = null;
					try {

						// Obtain the start time
						long startTime = System.nanoTime();

						// Make the request
						if (httpRequest != null) {
							response = this.httpClient.execute(httpRequest);
						}

						// Validate response (to ensure full response received)
						if (response != null) {
							Assert.assertEquals("Should be successful", 200,
									response.getStatusLine().getStatusCode());

							// Ensure correct response
							HttpEntity entity = response.getEntity();
							int value = entity.getContent().read();
							Assert.assertEquals("Incorrect response",
									request.getExpectedResponse(), value);
							entity.consumeContent();
						}

						// Obtain the end time
						long endTime = System.nanoTime();

						// Flag now that connected
						this.isConnected = true;

						// Notify completed request instance
						instance.complete(startTime, endTime);

					} catch (Throwable ex) {
						// Flag failure on instance
						instance.failed(ex);

						// Ensure consume content to re-use connection
						try {
							if (response != null) {
								response.getEntity().consumeContent();
							}
						} catch (Exception failure) {
							failure.printStackTrace();
						}
					}

				} catch (InterruptedException ex) {
					// Just keep asking for next request until null
				}
			}

		} finally {
			// Flag completed
			this.isComplete = true;
		}
	}

}