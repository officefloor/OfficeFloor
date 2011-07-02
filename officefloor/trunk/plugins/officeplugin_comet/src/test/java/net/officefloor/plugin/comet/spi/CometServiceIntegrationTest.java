/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.comet.spi;

import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.autowire.AutoWireObject;
import net.officefloor.plugin.autowire.AutoWireOfficeFloor;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirer;
import net.officefloor.plugin.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.plugin.comet.api.CometListener;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometInterest;
import net.officefloor.plugin.comet.internal.CometListenerService;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnectionManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import com.gdevelop.gwt.syncrpc.SyncProxy;

/**
 * Tests integration of the {@link CometServiceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometServiceIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Port to run service on.
	 */
	private int port;

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor autoWireOfficefloor;

	@Override
	protected void setUp() throws Exception {

		final long eventTimeout = 500;

		// Start the server
		this.port = MockHttpServer.getAvailablePort();
		HttpServerAutoWireOfficeFloorSource server = new HttpServerAutoWireOfficeFloorSource(
				this.port);
		server.addManagedObject(
				ServerGwtRpcConnectionManagedObjectSource.class, null,
				ServerGwtRpcConnection.class);
		AutoWireObject comet = server.addManagedObject(
				CometServiceManagedObjectSource.class,
				new ManagedObjectSourceWirer() {
					@Override
					public void wire(ManagedObjectSourceWirerContext context) {
						context.setInput(true);
						context.mapTeam(
								CometServiceManagedObjectSource.EXPIRE_TEAM_NAME,
								OnePersonTeamSource.class);
					}
				}, CometService.class);
		comet.addProperty(
				CometServiceManagedObjectSource.PROPERTY_EVENT_TIMEOUT,
				String.valueOf(eventTimeout));
		comet.addProperty(
				CometServiceManagedObjectSource.PROPERTY_REQUEST_TIMEOUT,
				String.valueOf(eventTimeout * 2));
		comet.addProperty(
				CometServiceManagedObjectSource.PROPERTY_EXPIRE_CHECK_INTERVAL,
				String.valueOf((long) (eventTimeout / 10)));
		comet.setTimeout(eventTimeout * 4);
		AutoWireSection service = server.addSection("SERVICE",
				ClassSectionSource.class, Service.class.getName());
		server.linkUri("/comet/service", service, "service");
		server.linkUri("/comet/publishEvent", service, "publishEvent");
		this.autoWireOfficefloor = server.openOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		this.autoWireOfficefloor.closeOfficeFloor();
	}

	/**
	 * Ensure able to publish event. This ensure correct setup before testing
	 * functionality integration.
	 */
	public void testPublishEvent() {
		this.publishEvent();
	}

	/**
	 * Ensure can receive an awaiting {@link PublishedEvent}.
	 */
	public void testReceiveEventImmediately() {
		this.publishEvent();
		CometResponse response = this.longPoll().waitOnResponse();
		assertEvents(response, 1);
	}

	/**
	 * Ensure can receive a {@link CometEvent} on waiting for one.
	 */
	public void testReceiveEventOnWaiting() throws Exception {
		ServiceInvoker service = this.longPoll();
		Thread.sleep(100); // ensure some time to process response
		assertNull("Should be waiting on response", service.checkForResponse());
		this.publishEvent();
		CometResponse response = service.waitOnResponse();
		assertEvents(response, 1);
	}

	/**
	 * <p>
	 * Ensure that times out on waiting and sends response.
	 * <p>
	 * {@link StressTest} as execution takes time due to waiting and therefore
	 * only including on long test runs.
	 */
	@StressTest
	public void testTimedOutLongWait() {
		CometResponse response = this.longPoll().waitOnResponse();
		assertEvents(response); // no events
	}

	/**
	 * <p>
	 * Ensure times out old {@link PublishedEvent} instances.
	 * <p>
	 * {@link StressTest} as execution takes time due to waiting and therefore
	 * only including on long test runs.
	 */
	@StressTest
	public void testTimeOutOldEvent() throws Exception {
		this.publishEvent(); // event to be expired
		Thread.sleep(2000); // put time between events
		this.publishEvent();
		CometResponse response = this.longPoll().waitOnResponse();
		assertEvents(response, 2); // only second event provided
	}

	/**
	 * Ensure that not receive same events.
	 */
	public void testNotReceiveSameEvent() throws Exception {

		// Retrieve the first event
		this.publishEvent();
		CometResponse responseOne = this.longPoll().waitOnResponse();
		assertEvents(responseOne, 1);
		long lastEventId = responseOne.getEvents()[0].getEventId();

		// Now wait on second event
		ServiceInvoker service = this.longPoll(lastEventId);
		this.publishEvent();
		CometResponse responseTwo = service.waitOnResponse();
		assertEvents(responseTwo, 2);
	}

	/**
	 * Asserts the event.
	 * 
	 * @param eventIds
	 *            Event Ids of the expected {@link CometEvent} instances.
	 */
	private static void assertEvents(CometResponse response, long... eventIds) {

		// Ensure have correct number of events
		assertEquals("Incorrect number of events", eventIds.length,
				response.getEvents().length);

		// Ensure correct events
		for (int i = 0; i < eventIds.length; i++) {
			long expectedEventId = eventIds[i];
			CometEvent actualEvent = response.getEvents()[i];

			// Ensure event is correct
			assertEquals("Incorrect event Id for event " + i, expectedEventId,
					actualEvent.getEventId());
			assertEquals("Incorrect listener type for event " + i,
					MockListener.class.getName(),
					actualEvent.getListenerTypeName());
			assertEquals("Incorrect payload for event " + i, "EVENT",
					actualEvent.getEvent());
			assertNull("Should not have filter key for event " + i,
					actualEvent.getFilterKey());
		}
	}

	/**
	 * Triggers the long poll for the first {@link CometRequest}.
	 * 
	 * @return {@link ServiceInvoker}.
	 */
	public ServiceInvoker longPoll() {
		return this.longPoll(CometRequest.FIRST_REQUEST_EVENT_ID);
	}

	/**
	 * Triggers the long poll.
	 * 
	 * @param lastEventId
	 *            Last {@link CometEvent} Id.
	 * @return {@link ServiceInvoker}.
	 */
	public ServiceInvoker longPoll(long lastEventId) {
		return ServiceInvoker.invokeService(this.port, "service", lastEventId);
	}

	/**
	 * Publishes an event.
	 * 
	 * @return {@link ServiceInvoker}.
	 */
	public void publishEvent() {
		ServiceInvoker.invokeService(this.port, "publishEvent",
				CometRequest.FIRST_REQUEST_EVENT_ID).waitOnResponse();
	}

	/**
	 * Invokes the appropriate service.
	 */
	private static class ServiceInvoker extends Thread {

		/**
		 * Port service is running on.
		 */
		private final int port;

		/**
		 * Service name.
		 */
		private final String serviceName;

		/**
		 * Last {@link CometEvent} Id.
		 */
		private final long lastEventId;

		/**
		 * {@link CometResponse} response.
		 */
		private CometResponse response = null;

		/**
		 * Failure.
		 */
		private Throwable failure = null;

		/**
		 * Initiate.
		 * 
		 * @param port
		 *            Port service is running on.
		 * @param serviceName
		 *            Service name.
		 * @param lastEventId
		 *            Last {@link CometEvent} Id.
		 */
		private ServiceInvoker(int port, String serviceName, long lastEventId) {
			this.port = port;
			this.serviceName = serviceName;
			this.lastEventId = lastEventId;
		}

		/**
		 * Invokes the service.
		 * 
		 * @param port
		 *            Port the service is running.
		 * @param serviceName
		 *            Service name.
		 * @param lastEventId
		 *            Last {@link CometEvent} Id.
		 * @return {@link ServiceInvoker}.
		 */
		public static ServiceInvoker invokeService(int port,
				String serviceName, long lastEventId) {
			// Create the Serivce Invoker and trigger request
			ServiceInvoker invoker = new ServiceInvoker(port, serviceName,
					lastEventId);
			invoker.start();

			// Return Service Invoker
			return invoker;
		}

		/**
		 * Checks for a {@link CometResponse}.
		 * 
		 * @return {@link CometResponse}. <code>null</code> if not available.
		 */
		public synchronized CometResponse checkForResponse() {

			// Determine if failure
			if (this.failure != null) {
				throw fail(this.failure);
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
							throw fail(this.failure);
						}

						// Determine if time out
						if (System.currentTimeMillis() > (startTime + 500000)) {
							fail("Timed out waiting on response from service "
									+ this.serviceName);
						}

						// Wait on response
						this.wait(500);
					}
				}
			} catch (Throwable ex) {
				throw fail(ex);
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
				CometListenerService caller = (CometListenerService) SyncProxy
						.newProxyInstance(CometListenerService.class,
								"http://localhost:" + this.port + "/comet/",
								this.serviceName);
				response = caller.listen(new CometRequest(this.lastEventId,
						new CometInterest(MockListener.class.getName(), null)));
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

	/**
	 * Service class.
	 */
	public static class Service {

		@NextTask("finishedServicing")
		public void service(CometService service) {
			service.service();
		}

		public void finishedServicing(CometService service) {
			// Need task to trigger waiting on managed object completion
		}

		public void publishEvent(CometService service,
				ServerGwtRpcConnection<CometResponse> connection) {
			// Publish event
			service.publishEvent(MockListener.class, "EVENT", null);

			// Send response to allow trigger to complete
			connection.onSuccess(new CometResponse());
		}
	}

	/**
	 * Mock {@link CometListener} interface.
	 */
	public static interface MockListener extends CometListener {
	}

}