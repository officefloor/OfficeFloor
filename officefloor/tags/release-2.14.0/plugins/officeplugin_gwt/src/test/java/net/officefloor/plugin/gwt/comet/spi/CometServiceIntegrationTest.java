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
package net.officefloor.plugin.gwt.comet.spi;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.comet.CometServiceInvoker;
import net.officefloor.plugin.gwt.comet.api.CometSubscriber;
import net.officefloor.plugin.gwt.comet.client.MockCometEventListener;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.internal.CometInterest;
import net.officefloor.plugin.gwt.comet.internal.CometRequest;
import net.officefloor.plugin.gwt.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicer;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicerManagedObjectSource;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.comet.spi.CometServiceManagedObjectSource;
import net.officefloor.plugin.gwt.comet.spi.PublishedEvent;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnectionManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

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
				ServerGwtRpcConnectionManagedObjectSource.class.getName(),
				null, new AutoWire(ServerGwtRpcConnection.class));
		server.addManagedObject(
				CometRequestServicerManagedObjectSource.class.getName(), null,
				new AutoWire(CometRequestServicer.class));
		AutoWireObject comet = server.addManagedObject(
				CometServiceManagedObjectSource.class.getName(),
				new ManagedObjectSourceWirer() {
					@Override
					public void wire(ManagedObjectSourceWirerContext context) {
						context.mapTeam(
								CometServiceManagedObjectSource.EXPIRE_TEAM_NAME,
								OnePersonTeamSource.class.getName());
					}
				}, new AutoWire(CometService.class));
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
				ClassSectionSource.class.getName(), Service.class.getName());
		server.linkUri("/service", service, "service");
		server.linkUri("/publishEvent", service, "publishEvent");
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
		CometServiceInvoker service = this.longPoll();
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
		long lastSequenceNumber = responseOne.getEvents()[0]
				.getSequenceNumber();

		// Now wait on second event
		CometServiceInvoker service = this.longPoll(lastSequenceNumber);
		this.publishEvent();
		CometResponse responseTwo = service.waitOnResponse();
		assertEvents(responseTwo, 2);
	}

	/**
	 * Asserts the event.
	 * 
	 * @param eventSequenceNumbers
	 *            Event sequence numbers of the expected {@link CometEvent}
	 *            instances.
	 */
	private static void assertEvents(CometResponse response,
			long... eventSequenceNumbers) {

		// Ensure have correct number of events
		assertEquals("Incorrect number of events", eventSequenceNumbers.length,
				response.getEvents().length);

		// Ensure correct events
		for (int i = 0; i < eventSequenceNumbers.length; i++) {
			long expectedEventSequenceNumber = eventSequenceNumbers[i];
			CometEvent actualEvent = response.getEvents()[i];

			// Ensure event is correct
			assertEquals("Incorrect event Id for event " + i,
					expectedEventSequenceNumber,
					actualEvent.getSequenceNumber());
			assertEquals("Incorrect listener type for event " + i,
					MockListener.class.getName(),
					actualEvent.getListenerTypeName());
			assertEquals("Incorrect data for event " + i, "EVENT",
					actualEvent.getData());
			assertNull("Should not have match key for event " + i,
					actualEvent.getMatchKey());
		}
	}

	/**
	 * Triggers the long poll for the first {@link CometRequest}.
	 * 
	 * @return {@link CometServiceInvoker}.
	 */
	public CometServiceInvoker longPoll() {
		return this.longPoll(CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER);
	}

	/**
	 * Triggers the long poll.
	 * 
	 * @param lastSequenceNumber
	 *            Last {@link CometEvent} sequence number.
	 * @return {@link CometServiceInvoker}.
	 */
	public CometServiceInvoker longPoll(long lastSequenceNumber) {
		return CometServiceInvoker.subscribe(this.port, "service",
				lastSequenceNumber,
				new CometInterest(MockListener.class.getName(), null));
	}

	/**
	 * Publishes an event.
	 * 
	 * @return {@link CometServiceInvoker}.
	 */
	public void publishEvent() {
		CometServiceInvoker.publish(this.port, "publishEvent", new CometEvent(
				MockCometEventListener.class.getName(), "EVENT", null));
	}

	/**
	 * Service class.
	 */
	public static class Service {

		@NextTask("finishedServicing")
		public void service(CometRequestServicer servicer) {
			servicer.service();
		}

		public void finishedServicing(CometRequestServicer service) {
			// Need task to trigger waiting on managed object completion
		}

		public void publishEvent(CometService service,
				ServerGwtRpcConnection<Long> connection) {
			// Publish event
			service.publishEvent(MockListener.class.getName(), "EVENT", null);

			// Send response to allow trigger to complete
			connection.onSuccess(Long.valueOf(1));
		}
	}

	/**
	 * Mock {@link CometSubscriber} interface.
	 */
	public static interface MockListener extends CometSubscriber {
	}

}