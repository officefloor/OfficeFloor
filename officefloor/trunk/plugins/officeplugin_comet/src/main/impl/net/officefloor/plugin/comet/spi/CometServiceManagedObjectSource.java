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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometInterest;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.comet.spi.CometServiceManagedObject.Dependencies;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * {@link ManagedObjectSource} for the {@link CometService}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometServiceManagedObjectSource extends
		AbstractManagedObjectSource<Dependencies, None> {

	/**
	 * <p>
	 * Property name to obtain the {@link CometRequest} timeout.
	 * <p>
	 * This is the time in milli-seconds that the {@link CometRequest} will
	 * &quot;long poll&quot; wait.
	 */
	public static final String PROPERTY_REQUEST_TIMEOUT = "request.timeout";

	/**
	 * Default {@link CometRequest} timeout.
	 */
	public static final long DEFAULT_REQUEST_TIMEOUT = 60 * 1000;

	/**
	 * <p>
	 * Property name to obtain the {@link PublishedEvent} timeout.
	 * <p>
	 * This is the time the {@link PublishedEvent} will be kept before being
	 * expired and no longer available to {@link CometRequest} instances.
	 */
	public static final String PROPERTY_EVENT_TIMEOUT = "event.timeout";

	/**
	 * Default {@link PublishedEvent} timeout.
	 */
	public static final long DEFAULT_EVENT_TIMEOUT = 30 * 1000;

	/**
	 * Provides the publish event timestamp.
	 */
	private final PublishClock clock;

	/**
	 * Time to timeout waiting {@link CometRequest}.
	 */
	private long requestTimeout;

	/**
	 * Time to timeout waiting {@link PublishedEvent}.
	 */
	private long eventTimeout;

	/**
	 * Head of {@link PublishedEvent} linked list.
	 */
	private PublishedEventImpl head = null;

	/**
	 * Tail of {@link PublishedEvent} linked list.
	 */
	private PublishedEventImpl tail = null;

	/**
	 * {@link Set} of {@link WaitingCometRequest} instances.
	 */
	private final Set<WaitingCometRequest> waitingRequests = new HashSet<WaitingCometRequest>();

	/**
	 * Next {@link PublishedEvent} Id.
	 */
	private long nextEventId = 1;

	/**
	 * Default constructor as per {@link ManagedObjectSource} requirements.
	 */
	public CometServiceManagedObjectSource() {
		this(new PublishClock() {
			@Override
			public long currentTimestamp() {
				return System.currentTimeMillis();
			}
		});
	}

	/**
	 * <p>
	 * Enable overriding the {@link PublishClock}.
	 * <p>
	 * This is mainly available for testing.
	 * 
	 * @param clock
	 *            {@link PublishClock}.
	 */
	public CometServiceManagedObjectSource(PublishClock clock) {
		this.clock = clock;
	}

	/**
	 * Publishes a {@link PublishedEvent}.
	 * 
	 * @param listenerType
	 *            Listener type.
	 * @param event
	 *            Event.
	 * @param matchKey
	 *            Match key.
	 */
	public synchronized void publishEvent(Class<?> listenerType, Object event,
			Object matchKey) {

		// Obtain the published time stamp
		long publishTimestamp = this.clock.currentTimestamp();

		// Create the event
		long eventId = this.nextEventId++;
		PublishedEventImpl newEvent = new PublishedEventImpl(eventId,
				listenerType, event, matchKey, publishTimestamp);

		// Add the event to head of list
		if (this.head == null) {
			// Only event so link as start and end of list
			this.head = newEvent;
			this.tail = newEvent;

		} else {
			// Other events, so add to head of list
			this.head.setNextEvent(newEvent);
			this.head = newEvent;
		}

		// Iterate over waiting requests to determine if interested in event
		Iterator<WaitingCometRequest> iterator = this.waitingRequests
				.iterator();
		while (iterator.hasNext()) {
			WaitingCometRequest request = iterator.next();

			// Determine if match event matches request
			List<CometEvent> events = null;
			for (int i = 0; i < request.interests.length; i++) {
				CometInterest interest = request.interests[i];
				if (this.isMatch(interest, newEvent)) {

					// Ensure have list (reduces garbage collection)
					if (events == null) {
						events = new LinkedList<CometEvent>();
					}

					// Add the event for the interest
					events.add(new CometEvent(interest.getListenerType(),
							newEvent.getEventParameter(), interest
									.getFilterKey()));
				}
			}
			if (events != null) {
				// Remove from waiting list as has event
				iterator.remove();

				// Send response with events
				request.sendResponse(events.toArray(new CometEvent[0]));
			}
		}
	}

	/**
	 * Receives or waits on {@link CometEvent} instances.
	 * 
	 * @param interests
	 *            Listing of {@link CometInterest} instances to determine
	 *            {@link CometEvent} instances of interest.
	 * @param connection
	 *            {@link ServerGwtRpcConnection}.
	 * @param asynchronousListener
	 *            {@link AsynchronousListener} for the
	 *            {@link CometServiceManagedObject}.
	 * @param lastEventId
	 *            Id of last {@link CometEvent} provided to the client.
	 */
	public synchronized void receiveOrWaitOnEvents(CometInterest[] interests,
			ServerGwtRpcConnection<CometResponse> connection,
			AsynchronousListener asynchronousListener, long lastEventId) {

		PublishedEvent node = this.tail;

		// Ignore already published events to client
		if (lastEventId != CometRequest.FIRST_REQUEST_EVENT_ID) {
			while ((node != null) && (node.getEventId() <= lastEventId)) {
				// Ignore event as already published to client
				node = node.getNextEvent();
			}
		}

		// Determine if event available
		List<CometEvent> events = null;
		while (node != null) {

			// Determine if match
			for (int i = 0; i < interests.length; i++) {
				CometInterest interest = interests[i];
				if (this.isMatch(interest, node)) {

					// Ensure have list
					if (events == null) {
						events = new LinkedList<CometEvent>();
					}

					// Add the event for the interest
					events.add(new CometEvent(interest.getListenerType(), node
							.getEventParameter(), interest.getFilterKey()));
				}
			}

			// Move to next event
			node = node.getNextEvent();
		}

		// Determine if events of interest
		if (events != null) {
			// Send events of interest
			CometResponse response = new CometResponse(
					events.toArray(new CometEvent[events.size()]));
			connection.onSuccess(response);

		} else {
			// No events, so wait on event available
			asynchronousListener.notifyStarted();

			// Determine time of registration
			long registrationTime = this.clock.currentTimestamp();

			// Wait on events of interest
			this.waitingRequests.add(new WaitingCometRequest(interests,
					connection, asynchronousListener, registrationTime));
		}
	}

	/**
	 * Expires waiting {@link CometRequest} instances and old
	 * {@link PublishedEvent} instances.
	 */
	public synchronized void expire() {

		// Obtain the current time
		long currentTime = this.clock.currentTimestamp();

		// Calculate the expire time for requests
		long requestExpireTime = currentTime - this.requestTimeout;

		// Iterate over waiting requests to determine if expire
		Iterator<WaitingCometRequest> iterator = this.waitingRequests
				.iterator();
		while (iterator.hasNext()) {
			WaitingCometRequest request = iterator.next();

			// Determine if require to expire event
			if (request.registeredTime < requestExpireTime) {

				// Unregister the request as being expired
				iterator.remove();

				// Expire the request
				request.sendResponse(new CometEvent[0]);
			}
		}

		// Calculate the expire time for events
		long eventExpireTime = currentTime - this.eventTimeout;

		// Iterate over events to determine if expire
		EXPIRED: while (this.tail != null) {

			// Obtain the next event
			PublishedEventImpl nextEvent = (PublishedEventImpl) this.tail
					.getNextEvent();

			// Determine if expire the event
			if (this.tail.getPublishTimestamp() < eventExpireTime) {
				// Expire the event (by removing reference to it)
				this.tail = nextEvent;
			} else {
				// No further events to expire
				break EXPIRED;
			}
		}
		if (this.tail == null) {
			this.head = null; // all events expired
		}
	}

	/**
	 * Determines if the {@link PublishedEvent} is a match for the
	 * {@link CometInterest}.
	 * 
	 * @param interest
	 *            {@link CometInterest}.
	 * @param event
	 *            {@link PublishedEvent}.
	 * @return <code>true</code> if a match.
	 */
	private boolean isMatch(CometInterest interest, PublishedEvent event) {

		// Determine if same listener type
		if (!(interest.getListenerType().equals(event.getListenerType()))) {
			return false; // not same listener type
		}

		// Determine if filtered
		Object matchKey = event.getMatchKey();
		Object filterKey = interest.getFilterKey();
		if ((matchKey != null) && (filterKey != null)) {
			if (!matchKey.equals(filterKey)) {
				return false; // filtered
			}
		}

		// As here then a match
		return true;
	}

	/*
	 * ================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the request timeout
		this.requestTimeout = Long.parseLong(mosContext.getProperty(
				PROPERTY_REQUEST_TIMEOUT,
				String.valueOf(DEFAULT_REQUEST_TIMEOUT)));

		// Obtain the event timeout
		this.eventTimeout = Long.parseLong(mosContext.getProperty(
				PROPERTY_EVENT_TIMEOUT, String.valueOf(DEFAULT_EVENT_TIMEOUT)));

		// Specify meta-data
		context.setObjectClass(CometService.class);
		context.setManagedObjectClass(CometServiceManagedObject.class);
		context.addDependency(Dependencies.SERVER_GWT_RPC_CONNECTION,
				ServerGwtRpcConnection.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new CometServiceManagedObject(this);
	}

	/**
	 * {@link CometRequest} waiting a {@link CometEvent} of
	 * {@link CometInterest}.
	 */
	private static class WaitingCometRequest {

		/**
		 * {@link CometInterest} instances for the
		 * {@link ServerGwtRpcConnection}.
		 */
		public final CometInterest[] interests;

		/**
		 * {@link ServerGwtRpcConnection}.
		 */
		public final ServerGwtRpcConnection<CometResponse> connection;

		/**
		 * {@link AsynchronousListener} for the {@link RPCRequest}.
		 */
		public final AsynchronousListener asynchronousListener;

		/**
		 * Time this {@link WaitingCometRequest} was registered to wait for a
		 * {@link CometEvent}.
		 */
		public final long registeredTime;

		/**
		 * Initiate.
		 * 
		 * @param interests
		 *            {@link CometInterest} instances for the
		 *            {@link ServerGwtRpcConnection}.
		 * @param connection
		 *            {@link ServerGwtRpcConnection}.
		 * @param asynchronousListener
		 *            {@link AsynchronousListener} for the {@link RPCRequest}.
		 * @param registeredTime
		 *            Time this {@link WaitingCometRequest} was registered to
		 *            wait for a {@link CometEvent}.
		 */
		public WaitingCometRequest(CometInterest[] interests,
				ServerGwtRpcConnection<CometResponse> connection,
				AsynchronousListener asynchronousListener, long registeredTime) {
			this.interests = interests;
			this.connection = connection;
			this.asynchronousListener = asynchronousListener;
			this.registeredTime = registeredTime;
		}

		/**
		 * Sends the {@link CometResponse}.
		 * 
		 * @param events
		 *            {@link CometEvent} instances.
		 */
		public void sendResponse(CometEvent[] events) {
			try {
				this.connection.onSuccess(new CometResponse(events));
			} finally {
				this.asynchronousListener.notifyComplete();
			}
		}
	}

}