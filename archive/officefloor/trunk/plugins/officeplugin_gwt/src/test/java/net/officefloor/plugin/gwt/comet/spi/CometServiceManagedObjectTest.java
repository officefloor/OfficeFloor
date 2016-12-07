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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.comet.api.CometSubscriber;
import net.officefloor.plugin.gwt.comet.api.OfficeFloorComet;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.internal.CometInterest;
import net.officefloor.plugin.gwt.comet.internal.CometRequest;
import net.officefloor.plugin.gwt.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.comet.internal.CometSubscriptionService;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.comet.spi.CometServiceManagedObject;
import net.officefloor.plugin.gwt.comet.spi.CometServiceManagedObjectSource;
import net.officefloor.plugin.gwt.comet.spi.PublishClock;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;

import org.easymock.AbstractMatcher;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * Tests the {@link CometServiceManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometServiceManagedObjectTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link PublishClock}.
	 */
	private final PublishClock clock = this.createMock(PublishClock.class);

	/**
	 * {@link CometServiceManagedObjectSource}.
	 */
	private final CometServiceManagedObjectSource source = new CometServiceManagedObjectSource(
			this.clock);

	/**
	 * Mock {@link ServerGwtRpcConnection}.
	 */
	@SuppressWarnings("unchecked")
	private final ServerGwtRpcConnection<CometResponse> connection = this
			.createMock(ServerGwtRpcConnection.class);

	/**
	 * Mock {@link AsynchronousListener}.
	 */
	private final AsynchronousListener async = this
			.createMock(AsynchronousListener.class);

	/**
	 * Mock events to publish before servicing.
	 */
	private final List<MockEvent> events = new LinkedList<MockEvent>();

	/**
	 * Mock events to publish after servicing.
	 */
	private final List<MockEvent> published = new LinkedList<MockEvent>();

	/**
	 * Flag indicating whether to expire before servicing.
	 */
	private boolean isExpireBeforeServicing = false;

	/**
	 * Flag indicating whether to expire after servicing.
	 */
	private boolean isExpireAfterServicing = false;

	/**
	 * Ensure able to service finding an event already available.
	 */
	public void testNoEventAvailable() {
		this.recordInit();
		this.recordWait(10);
		this.doTest();
	}

	/**
	 * Ensure wait if no matching event by listener type.
	 */
	public void testNotMatchEventByListenerType() {
		this.recordEvent(1, MockOneListener.class, "EVENT", null);
		this.recordInit(new CometInterest(MockTwoListener.class.getName(), null));
		this.recordWait(10);
		this.doTest();
	}

	/**
	 * Ensure pick up published event.
	 */
	public void testMatchByListenerType() {
		this.recordEvent(1, MockOneListener.class, "EVENT", null);
		this.recordInit(new CometInterest(MockOneListener.class.getName(), null));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", null));
		this.doTest();
	}

	/**
	 * Ensure if no filter key to match all events for listener type.
	 */
	public void testMatchByNoFilterKey() {
		this.recordEvent(1, MockOneListener.class, "EVENT", "MATCH_KEY");
		this.recordInit(new CometInterest(MockOneListener.class.getName(), null));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", "MATCH_KEY"));
		this.doTest();
	}

	/**
	 * Ensure if no match key to match to all events for listener type.
	 */
	public void testMatchByNoMatchKey() {
		final Object filterKey = new MockKey() {
			@Override
			public boolean equals(Object obj) {
				assertNull("Should not have a match key", obj);
				return true;
			}
		};
		this.recordEvent(1, MockOneListener.class, "EVENT", null);
		this.recordInit(new CometInterest(MockOneListener.class.getName(),
				filterKey));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", null));
		this.doTest();
	}

	/**
	 * Ensure can match if match key equals the filter key.
	 */
	public void testMatchWithFiltering() {
		this.recordEvent(1, MockOneListener.class, "EVENT", "MATCH");
		this.recordInit(new CometInterest(MockOneListener.class.getName(),
				"MATCH"));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", "MATCH"));
		this.doTest();
	}

	/**
	 * Ensure filtered if match key does not match filter key.
	 */
	public void testFiltered() {
		this.recordEvent(1, MockOneListener.class, "EVENT", "NOT_MATCH");
		this.recordInit(new CometInterest(MockOneListener.class.getName(),
				"MATCH"));
		this.recordWait(10);
		this.doTest();
	}

	/**
	 * Ensure that it is the Filter key that checks the Match key. In other
	 * words, it is the {@link CometSubscriber} that provides the logic to match
	 * {@link CometEvent} instances of interest.
	 */
	public void testFilterChecksOnMatchKey() {
		final Object matchKey = new MockKey() {
			@Override
			public boolean equals(Object obj) {
				if (obj == this) {
					return true; // for assertion
				}
				fail("Match key should not be checking for filtering");
				return false;
			}
		};
		final Object filterKey = new MockKey() {
			@Override
			public boolean equals(Object obj) {
				assertEquals("Incorrect match key", matchKey, obj);
				return true; // match
			}
		};
		this.recordEvent(1, MockOneListener.class, "EVENT", matchKey);
		this.recordInit(new CometInterest(MockOneListener.class.getName(),
				filterKey));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", matchKey));
		this.doTest();
	}

	/**
	 * Ensure that if Filter key is provided that it checks all
	 * {@link CometEvent} instances, even if <code>null</code> match key.
	 */
	public void testFilterChecksOnNullMatchKey() {
		final boolean[] isInvokedForNull = new boolean[1];
		isInvokedForNull[0] = false;
		final Object filterKey = new MockKey() {
			@Override
			public boolean equals(Object obj) {
				assertNull("Should be no match key", obj);
				isInvokedForNull[0] = true;
				return true; // filter matches null
			}
		};
		this.recordEvent(1, MockOneListener.class, "EVENT", null);
		this.recordInit(new CometInterest(MockOneListener.class.getName(),
				filterKey));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", null));
		this.doTest();
		assertTrue("Ensure filter check on null match key", isInvokedForNull[0]);
	}

	/**
	 * <p>
	 * Ensure single transferred {@link CometEvent} instance on matching
	 * multiple {@link CometInterest} instances.
	 * <p>
	 * {@link OfficeFloorComet} will handle distributing to appropriate
	 * {@link CometSubscriber} instances.
	 */
	public void testMatchMultipleInterests() {
		this.recordEvent(1, MockOneListener.class, "EVENT", "MATCH");
		this.recordInit(new CometInterest(MockOneListener.class.getName(),
				"MATCH"), new CometInterest(MockOneListener.class.getName(),
				"MATCH"), new CometInterest(MockTwoListener.class.getName(),
				"NOT_MATCH"));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", "MATCH"));
		this.doTest();
	}

	/**
	 * Ensure able to match multiple {@link CometEvent} instances to a
	 * {@link CometInterest}.
	 */
	public void testMatchMultipleEvents() {
		this.recordEvent(1, MockOneListener.class, "EVENT_ONE", "MATCH");
		this.recordEvent(2, MockOneListener.class, "EVENT_TWO", "MATCH");
		this.recordEvent(3, MockTwoListener.class, "EVENT_THREE", "NOT_MATCH");
		this.recordInit(new CometInterest(MockOneListener.class.getName(),
				"MATCH"));
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT_ONE", "MATCH"),
				new CometEvent(2, MockOneListener.class.getName(), "EVENT_TWO",
						"MATCH"));
		this.doTest();
	}

	/**
	 * Ensure flag wait for {@link CometEvent} and trigger active when
	 * {@link CometEvent} of {@link CometInterest} is available.
	 */
	public void testPublishTriggerEvent() {
		this.recordInit(new CometInterest(MockOneListener.class.getName(), null));
		this.recordWait(10);
		this.recordPublishEvent(MockOneListener.class, "EVENT", null);
		this.async.notifyComplete();
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", null));
		this.doTest();
	}

	/**
	 * Ensure single transferred {@link CometEvent} instance on multiple waiting
	 * {@link CometInterest} instances for a published {@link CometEvent}.
	 */
	public void testPublishToMultipleInterests() {
		this.recordInit(
				new CometInterest(MockOneListener.class.getName(), null),
				new CometInterest(MockOneListener.class.getName(), null),
				new CometInterest(MockTwoListener.class.getName(), "NOT_MATCH"));
		this.recordWait(10);
		this.recordPublishEvent(MockOneListener.class, "EVENT", "MATCH");
		this.async.notifyComplete();
		this.recordResponse(new CometEvent(1, MockOneListener.class.getName(),
				"EVENT", "MATCH"));
		this.doTest();
	}

	/**
	 * Ensure timeout events.
	 */
	public void testTimeoutEvent() {

		// Setup an event (that will be expired)
		this.recordEvent(1, MockOneListener.class, "EVENT", null);

		this.recordInit(new CometInterest(MockOneListener.class.getName(), null));

		// Expire event
		this.recordExpireBeforeServicing(CometServiceManagedObjectSource.DEFAULT_EVENT_TIMEOUT * 2);

		// Should wait because no event (as expired)
		this.recordWait((CometServiceManagedObjectSource.DEFAULT_EVENT_TIMEOUT * 2) + 10);

		this.doTest();
	}

	/**
	 * Ensure timeout requests.
	 */
	public void testTimeoutRequests() {
		// Setup waiting on event
		this.recordInit(new CometInterest(MockOneListener.class.getName(), null));
		this.recordWait(1);

		// Expire as no event within request timeout
		this.recordExpireAfterServicing(CometServiceManagedObjectSource.DEFAULT_REQUEST_TIMEOUT * 2);
		this.async.notifyComplete();
		this.recordResponse();

		// Publishing event should not be sent to request
		this.recordPublishEvent(MockOneListener.class, "EVENT", null);

		this.doTest();
	}

	/**
	 * Ensure not resend same {@link CometEvent}.
	 */
	public void testNotResendEvent() {
		this.recordEvent(10, MockOneListener.class, "EVENT_ONE", null);
		this.recordEvent(20, MockOneListener.class, "EVENT_TWO", null);
		this.recordEvent(30, MockOneListener.class, "EVENT_THREE", null);
		this.recordInit(1, new CometInterest(MockOneListener.class.getName(),
				null));
		this.recordResponse(new CometEvent(2, MockOneListener.class.getName(),
				"EVENT_TWO", null),
				new CometEvent(3, MockOneListener.class.getName(),
						"EVENT_THREE", null));
		this.doTest();
	}

	/**
	 * Tests invoking {@link CometService} to service a {@link CometRequest}.
	 */
	private void doTest() {

		// Test
		this.replayMockObjects();

		// Create the service
		CometService service = this.createCometService();

		long expectedEventSequenceNumber = 1;

		// Publish before servicing events
		for (MockEvent event : this.events) {
			long eventSequenceNumber = service.publishEvent(
					event.listenerType.getName(), event.event, event.matchKey);
			assertEquals("Incorrect event sequence number",
					expectedEventSequenceNumber++, eventSequenceNumber);
		}

		// Undertake expiry before servicing
		if (this.isExpireBeforeServicing) {
			service.expire();
		}

		// Service
		service.service(this.connection);

		// Undertake expiry after servicing
		if (this.isExpireAfterServicing) {
			service.expire();
		}

		// Publish the after servicing events
		for (MockEvent event : this.published) {
			long eventSequenceNumber = service.publishEvent(
					event.listenerType.getName(), event.event, event.matchKey);
			assertEquals("Incorrect event sequence number",
					expectedEventSequenceNumber++, eventSequenceNumber);
		}

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Records an event to be published before servicing.
	 * 
	 * @param publishTimestamp
	 *            Timestamp of publishing the event.
	 * @param listenerType
	 *            Listener type.
	 * @param event
	 *            Event.
	 * @param matchKey
	 *            Match key.
	 */
	private void recordEvent(long publishTimestamp, Class<?> listenerType,
			Object event, Object matchKey) {

		// Record obtaining time stamp for publishing
		this.recordReturn(this.clock, this.clock.currentTimestamp(),
				publishTimestamp);

		// Register the event
		this.events.add(new MockEvent(listenerType, event, matchKey));
	}

	/**
	 * Records an event to be published after servicing.
	 * 
	 * @param listenerType
	 *            Listener type.
	 * @param event
	 *            Event.
	 * @param matchKey
	 *            Match key.
	 */
	private void recordPublishEvent(Class<?> listenerType, Object event,
			Object matchKey) {

		// Record obtaining time stamp for publishing.
		// Time not important as should trigger waiting service.
		this.recordReturn(this.clock, this.clock.currentTimestamp(), -1);

		// Register the event
		this.published.add(new MockEvent(listenerType, event, matchKey));
	}

	/**
	 * Records expiring before servicing.
	 * 
	 * @param currentTime
	 *            Time to determine expire.
	 */
	private void recordExpireBeforeServicing(long currentTime) {

		// Record obtaining time for expiring
		this.recordReturn(this.clock, this.clock.currentTimestamp(),
				currentTime);

		// Flag to expire
		this.isExpireBeforeServicing = true;
	}

	/**
	 * Records expiring after servicing.
	 * 
	 * @param currentTime
	 *            Time to determine expire.
	 */
	private void recordExpireAfterServicing(long currentTime) {

		// Record obtaining time for expiring
		this.recordReturn(this.clock, this.clock.currentTimestamp(),
				currentTime);

		// Flag to expire
		this.isExpireAfterServicing = true;
	}

	/**
	 * Records {@link CometRequest} waiting.
	 * 
	 * @param currentTime
	 *            Current time to determine waiting registration time.
	 */
	private void recordWait(long currentTime) {
		this.recordReturn(this.clock, this.clock.currentTimestamp(),
				currentTime);
		this.async.notifyStarted();
	}

	/**
	 * Records initialising the {@link CometRequest} and
	 * {@link CometServiceManagedObject}.
	 * 
	 * @param interests
	 *            Listing of {@link CometInterest}.
	 */
	private void recordInit(CometInterest... interests) {
		// First request (no previous event)
		this.recordInit(CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER, interests);
	}

	/**
	 * Records initialising the {@link CometRequest} and
	 * {@link CometServiceManagedObject}.
	 * 
	 * @param lastEventId
	 *            {@link CometEvent} Id of recently received {@link CometEvent}.
	 * @param interests
	 *            Listing of {@link CometInterest}.
	 */
	private void recordInit(long lastEventId, CometInterest... interests) {
		try {

			// Ensure have at least one interest
			if (interests.length == 0) {
				interests = new CometInterest[] { new CometInterest(
						MockOneListener.class.getName(), null) };
			}

			// Create the Comet Request
			CometRequest cometRequest = new CometRequest(lastEventId, interests);

			// Create the RPC Request
			Method method = CometSubscriptionService.class.getMethod(
					"subscribe", CometRequest.class);
			RPCRequest rpcRequest = new RPCRequest(method,
					new Object[] { cometRequest }, null, 0);

			// Record returning the RPC request
			this.recordReturn(this.connection, this.connection.getRpcRequest(),
					rpcRequest);

		} catch (Exception ex) {
			throw fail(ex); // should no occur in recording
		}
	}

	/**
	 * Record response.
	 * 
	 * @param events
	 *            Listing of {@link CometEvent}.
	 */
	private void recordResponse(CometEvent... events) {

		// Create the response
		CometResponse response = new CometResponse(events);

		// Record response
		this.connection.onSuccess(response);
		this.control(this.connection).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				CometResponse expectedResponse = (CometResponse) expected[0];
				CometResponse actualResponse = (CometResponse) actual[0];

				// Ensure similar number of events
				CometEvent[] expectedEvents = expectedResponse.getEvents();
				CometEvent[] actualEvents = actualResponse.getEvents();
				assertEquals("Incorrect number of events",
						expectedEvents.length, actualEvents.length);

				// Ensure events match
				for (int i = 0; i < expectedEvents.length; i++) {
					CometEvent expectedEvent = expectedEvents[i];
					CometEvent actualEvent = actualEvents[i];
					assertEquals("Incorrect sequence number for event " + i,
							expectedEvent.getSequenceNumber(),
							actualEvent.getSequenceNumber());
					assertEquals("Incorrect listener type for event " + i,
							expectedEvent.getListenerTypeName(),
							actualEvent.getListenerTypeName());
					assertEquals("Incorrect data for event " + i,
							expectedEvent.getData(), actualEvent.getData());
					assertEquals("Incorrect match key for event " + i,
							expectedEvent.getMatchKey(),
							actualEvent.getMatchKey());
				}

				// Match
				return true;
			}
		});
	}

	/**
	 * Creates the {@link CometService} to test.
	 * 
	 * @param request
	 *            {@link CometRequest}.
	 * @return {@link CometService} to test.
	 */
	private CometService createCometService() {
		try {

			// Create the Managed Object
			CometServiceManagedObject mo = new CometServiceManagedObject(
					this.source);

			// Register the Asynchronous Listener
			mo.registerAsynchronousCompletionListener(this.async);

			// Obtain the Comet Service
			CometService service = (CometService) mo.getObject();

			// Return the Comet Service
			return service;

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	/**
	 * Mock event to be published before servicing.
	 */
	private static class MockEvent {

		/**
		 * Listener type.
		 */
		public final Class<?> listenerType;

		/**
		 * Event.
		 */
		public final Object event;

		/**
		 * Match key.
		 */
		public final Object matchKey;

		/**
		 * Initiate.
		 * 
		 * @param listenerType
		 *            Listener type.
		 * @param event
		 *            Event.
		 * @param matchKey
		 *            Match key.
		 */
		public MockEvent(Class<?> listenerType, Object event, Object matchKey) {
			this.listenerType = listenerType;
			this.event = event;
			this.matchKey = matchKey;
		}
	}

	/**
	 * Mock {@link CometSubscriber} interface.
	 */
	public static interface MockOneListener extends CometSubscriber {

		void listenOne(String message);
	}

	/**
	 * Mock {@link CometSubscriber} interface.
	 */
	public static interface MockTwoListener extends CometSubscriber {

		void listenTwo(String message);
	}

	/**
	 * Mock filter/match key.
	 */
	public static class MockKey implements IsSerializable {
	}

}