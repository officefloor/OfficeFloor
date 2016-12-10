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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.internal.CometInterest;
import net.officefloor.plugin.gwt.comet.internal.CometRequest;
import net.officefloor.plugin.gwt.comet.internal.CometResponse;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.comet.spi.PublishClock;
import net.officefloor.plugin.gwt.comet.spi.PublishedEvent;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnectionException;

import com.google.gwt.user.server.rpc.RPCRequest;

/**
 * {@link ManagedObjectSource} for the {@link CometService}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometServiceManagedObjectSource
		extends
		AbstractManagedObjectSource<None, CometServiceManagedObjectSource.Flows> {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(CometServiceManagedObjectSource.class.getName());

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		EXPIRE
	}

	/**
	 * Name of the {@link Team} responsible for expiring.
	 */
	public static final String EXPIRE_TEAM_NAME = "EXPIRE_TEAM";

	/**
	 * Interval in milliseconds for checking for expired {@link PublishedEvent}
	 * instances and expired {@link CometRequest} instances.
	 */
	public static final String PROPERTY_EXPIRE_CHECK_INTERVAL = "expire.check.interval";

	/**
	 * Default expire check interval for expired {@link PublishedEvent}
	 * instances and expired {@link CometRequest} instances.
	 */
	public static final long DEFAULT_EXPIRE_CHECK_INTERVAL = 5000; // 5 seconds

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
	public static final long DEFAULT_EVENT_TIMEOUT = 30 * 1000; // 30 seconds

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
	public static final long DEFAULT_REQUEST_TIMEOUT = 60 * 1000; // 60 seconds

	/**
	 * Provides the publish event timestamp.
	 */
	private final PublishClock clock;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Flows> executeContext;

	/**
	 * Interval in milliseconds to check for expiring.
	 */
	private long expireCheckInterval;

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
	 * @param sequenceNumber
	 *            {@link PublishedEvent} sequence number.
	 * @param listenerType
	 *            Listener type.
	 * @param event
	 *            Event.
	 * @param matchKey
	 *            Match key.
	 * @return Sequence number for the {@link PublishedEvent}.
	 */
	public synchronized long publishEvent(long sequenceNumber,
			String listenerType, Object event, Object matchKey) {

		// Obtain the published time stamp
		long publishTimestamp = this.clock.currentTimestamp();

		// Create the event
		long eventSequenceNumber = (sequenceNumber == CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER) ? this.nextEventId++
				: sequenceNumber;
		PublishedEventImpl newEvent = new PublishedEventImpl(
				eventSequenceNumber, listenerType, event, matchKey,
				publishTimestamp);

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
			INTERESTED: for (int i = 0; i < request.interests.length; i++) {
				CometInterest interest = request.interests[i];
				if (this.isMatch(interest, newEvent)) {

					// Ensure have list (reduces garbage collection)
					if (events == null) {
						events = new LinkedList<CometEvent>();
					}

					// Add the event for the interest
					events.add(new CometEvent(
							newEvent.getEventSequenceNumber(), interest
									.getListenerTypeName(), newEvent.getData(),
							newEvent.getMatchKey()));

					/*
					 * Only include the event once. It is responsibility of
					 * subscriber to multiplex events to interests.
					 */
					break INTERESTED;
				}
			}
			if (events != null) {
				// Remove from waiting list as has event
				iterator.remove();

				// Send response with events
				request.sendResponse(events.toArray(new CometEvent[0]));
			}
		}

		// Return the event sequence number
		return eventSequenceNumber;
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
	 * @param lastEventSequenceNumber
	 *            Sequence number of last {@link CometEvent} provided to the
	 *            client.
	 */
	public synchronized void receiveOrWaitOnEvents(CometInterest[] interests,
			ServerGwtRpcConnection<CometResponse> connection,
			AsynchronousListener asynchronousListener,
			long lastEventSequenceNumber) {

		PublishedEvent node = this.tail;

		// Ignore already published events to client
		if (lastEventSequenceNumber != CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER) {
			while ((node != null)
					&& (node.getEventSequenceNumber() <= lastEventSequenceNumber)) {
				// Ignore event as already published to client
				node = node.getNextEvent();
			}
		}

		// Determine if event available
		List<CometEvent> events = null;
		while (node != null) {

			// Determine if match
			NEXT_NODE: for (int i = 0; i < interests.length; i++) {
				CometInterest interest = interests[i];
				if (this.isMatch(interest, node)) {

					// Ensure have list
					if (events == null) {
						events = new LinkedList<CometEvent>();
					}

					// Add the event for the interest
					events.add(new CometEvent(node.getEventSequenceNumber(),
							interest.getListenerTypeName(), node.getData(),
							node.getMatchKey()));

					/*
					 * Only include the event once. It is responsibility of
					 * subscriber to multiplex events to interests.
					 */
					break NEXT_NODE;
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
		if (!(interest.getListenerTypeName()
				.equals(event.getListenerTypeName()))) {
			return false; // not same listener type
		}

		// Determine if filtered
		Object filterKey = interest.getFilterKey();
		if (filterKey != null) {
			Object matchKey = event.getMatchKey();
			if (!(filterKey.equals(matchKey))) {
				return false; // filtered from response
			}
		}

		// As here then a match
		return true;
	}

	/**
	 * Runs the loop for checking for expiring.
	 */
	private synchronized void expireCheckLoop() {

		// Undertake an expire
		this.expire();

		// Determine if continue checking
		if (this.executeContext == null) {
			return; // stopping, so no further checking
		}

		// Setup for next expire check
		this.executeContext.invokeProcess(Flows.EXPIRE, null,
				new CometServiceManagedObject(this), this.expireCheckInterval);
	}

	/*
	 * ================== ManagedObjectSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, Flows> context)
			throws Exception {
		ManagedObjectSourceContext<Flows> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the expire check interval
		this.expireCheckInterval = Long.parseLong(mosContext.getProperty(
				PROPERTY_EXPIRE_CHECK_INTERVAL,
				String.valueOf(DEFAULT_EXPIRE_CHECK_INTERVAL)));

		// Obtain the event timeout
		this.eventTimeout = Long.parseLong(mosContext.getProperty(
				PROPERTY_EVENT_TIMEOUT, String.valueOf(DEFAULT_EVENT_TIMEOUT)));

		// Obtain the request timeout
		this.requestTimeout = Long.parseLong(mosContext.getProperty(
				PROPERTY_REQUEST_TIMEOUT,
				String.valueOf(DEFAULT_REQUEST_TIMEOUT)));

		// Specify meta-data
		context.setObjectClass(CometService.class);
		context.setManagedObjectClass(CometServiceManagedObject.class);

		// Provide task to trigger expire
		ExpireTask factory = new ExpireTask();
		mosContext.addWork("EXPIRE", factory).addTask("TASK", factory)
				.setTeam(EXPIRE_TEAM_NAME);
		context.addFlow(Flows.EXPIRE, null);
		mosContext.linkProcess(Flows.EXPIRE, "EXPIRE", "TASK");
	}

	@Override
	public synchronized void start(ManagedObjectExecuteContext<Flows> context)
			throws Exception {
		this.executeContext = context;

		// Initiate the expire check loop
		this.expireCheckLoop();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new CometServiceManagedObject(this);
	}

	@Override
	public synchronized void stop() {
		// Stop expire check loop
		this.executeContext = null;
	}

	/**
	 * Expire {@link Task}.
	 */
	private class ExpireTask extends AbstractSingleTask<ExpireTask, None, None> {
		@Override
		public Object doTask(TaskContext<ExpireTask, None, None> context)
				throws Throwable {
			CometServiceManagedObjectSource.this.expireCheckLoop();
			return null;
		}
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
				// Send response
				this.connection.onSuccess(new CometResponse(events));

			} catch (ServerGwtRpcConnectionException ex) {
				// Likely that connection closed
				if (LOGGER.isLoggable(Level.FINE)) {
					StringBuilder msg = new StringBuilder();
					msg.append("Failed sending Events for ");
					msg.append(this.connection.getHttpRequest().getRequestURI());
					LOGGER.log(Level.FINE, msg.toString(), ex);
				}

			} finally {
				// Allow subscriber to complete
				this.asynchronousListener.notifyComplete();
			}
		}
	}

}