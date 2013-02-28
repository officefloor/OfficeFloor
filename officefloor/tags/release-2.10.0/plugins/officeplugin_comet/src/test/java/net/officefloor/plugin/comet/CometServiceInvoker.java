/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.comet;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometInterest;
import net.officefloor.plugin.comet.internal.CometPublicationService;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.comet.internal.CometSubscriptionService;
import net.officefloor.plugin.comet.spi.CometService;

import com.gdevelop.gwt.syncrpc.SyncProxy;

/**
 * <p>
 * Invokes the appropriate {@link CometService}.
 * <p>
 * It invokes with another {@link Thread} to allow subscribe long polling to
 * occur while publishing {@link CometEvent} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CometServiceInvoker extends Thread {

	/**
	 * Port service is running on.
	 */
	private final int port;

	/**
	 * Service URI.
	 */
	private final String serviceUri;

	/**
	 * Last {@link CometEvent} sequence number.
	 */
	private final long lastSequenceNumber;

	/**
	 * {@link CometInterest} instances.
	 */
	private final CometInterest[] interests;

	/**
	 * {@link CometResponse} response.
	 */
	private CometResponse response = null;

	/**
	 * Failure.
	 */
	private Throwable failure = null;

	/**
	 * Subscribes for a {@link CometEvent}.
	 * 
	 * @param port
	 *            Port the service is running.
	 * @param serviceUri
	 *            Service URI.
	 * @param lastSequenceNumber
	 *            Last {@link CometEvent} sequence number.
	 * @param interests
	 *            {@link CometInterest} instances.
	 * @return {@link CometServiceInvoker}.
	 */
	public static CometServiceInvoker subscribe(int port, String serviceUri,
			long lastSequenceNumber, CometInterest... interests) {
		// Create the Service Invoker and trigger request
		CometServiceInvoker invoker = new CometServiceInvoker(port, serviceUri,
				lastSequenceNumber, interests);
		invoker.start();

		// Return Service Invoker
		return invoker;
	}

	/**
	 * Publishes the {@link CometEvent}.
	 * 
	 * @param port
	 *            Port service is running on.
	 * @param serviceUri
	 *            Service URI.
	 * @param event
	 *            {@link CometEvent}.
	 * @return Sequence number of the published {@link CometEvent}.
	 */
	public static long publish(int port, String serviceUri, CometEvent event) {
		try {
			// Ensure URI
			serviceUri = (serviceUri.startsWith("/") ? serviceUri : "/"
					+ serviceUri);

			// Publish event
			CometPublicationService caller = (CometPublicationService) SyncProxy
					.newProxyInstance(CometPublicationService.class,
							"http://localhost:" + port, serviceUri);
			Long response = caller.publish(event);

			// Return sequence number
			return response.longValue();
		} catch (Throwable ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param port
	 *            Port service is running on.
	 * @param serviceUri
	 *            Service URI.
	 * @param lastSequenceNumber
	 *            Last {@link CometEvent} sequence number.
	 * @param interests
	 *            {@link CometInterest} instances.
	 */
	private CometServiceInvoker(int port, String serviceUri,
			long lastSequenceNumber, CometInterest[] interests) {
		this.port = port;
		this.serviceUri = (serviceUri.startsWith("/") ? serviceUri : "/"
				+ serviceUri);
		this.lastSequenceNumber = lastSequenceNumber;
		this.interests = interests;
	}

	/**
	 * Checks for a {@link CometResponse}.
	 * 
	 * @return {@link CometResponse}. <code>null</code> if not available.
	 */
	public synchronized CometResponse checkForResponse() {

		// Determine if failure
		if (this.failure != null) {
			throw OfficeFrameTestCase.fail(this.failure);
		}

		// Return current value for response
		return this.response;
	}

	/**
	 * Waits on the {@link CometResponse}.
	 * 
	 * @return {@link CometResponse}.
	 */
	public synchronized CometResponse waitOnResponse() {
		try {
			long startTime = System.currentTimeMillis();
			synchronized (this) {
				for (;;) {

					// Determine if complete
					if (this.response != null) {
						return this.response;
					}

					// Determine if failure
					if (this.failure != null) {
						throw OfficeFrameTestCase.fail(this.failure);
					}

					// Determine if time out
					if (System.currentTimeMillis() > (startTime + 500000)) {
						OfficeFrameTestCase
								.fail("Timed out waiting on response from service "
										+ this.serviceUri);
					}

					// Wait on response
					this.wait(500);
				}
			}
		} catch (Throwable ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/*
	 * ====================== Thread =======================
	 */

	@Override
	public void run() {

		// Call the service
		CometResponse response = null;
		Throwable failure = null;
		try {
			CometSubscriptionService caller = (CometSubscriptionService) SyncProxy
					.newProxyInstance(CometSubscriptionService.class,
							"http://localhost:" + this.port, this.serviceUri);
			response = caller.subscribe(new CometRequest(
					this.lastSequenceNumber, this.interests));
		} catch (Throwable ex) {
			failure = ex;
		}

		// Provide response and notify complete
		synchronized (this) {
			this.response = response;
			this.failure = failure;
			this.notify();
		}
	}

}