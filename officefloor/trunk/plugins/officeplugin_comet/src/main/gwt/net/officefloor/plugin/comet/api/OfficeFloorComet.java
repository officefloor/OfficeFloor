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
package net.officefloor.plugin.comet.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.plugin.comet.internal.CometAdapter;
import net.officefloor.plugin.comet.internal.CometAdapterMap;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometInterest;
import net.officefloor.plugin.comet.internal.CometPublicationService;
import net.officefloor.plugin.comet.internal.CometPublicationServiceAsync;
import net.officefloor.plugin.comet.internal.CometRequest;
import net.officefloor.plugin.comet.internal.CometResponse;
import net.officefloor.plugin.comet.internal.CometSubscriptionService;
import net.officefloor.plugin.comet.internal.CometSubscriptionServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Provides means to register an event listener.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorComet {

	/**
	 * Obtain the mapping of {@link CometSubscriber} interface type to its
	 * {@link CometAdapter}.
	 */
	private static final Map<Class<?>, CometAdapter> adapters = ((CometAdapterMap) GWT
			.create(CometAdapterMap.class)).getMap();

	/**
	 * {@link CometSubscriptionServiceAsync}.
	 */
	private static final CometSubscriptionServiceAsync subscriptionService = GWT
			.create(CometSubscriptionService.class);

	/**
	 * {@link CometPublicationServiceAsync}.
	 */
	private static final CometPublicationServiceAsync publicationService = GWT
			.create(CometPublicationService.class);

	/**
	 * Last {@link CometEvent} sequence number.
	 */
	private static long lastEventSequenceNumber = CometRequest.FIRST_REQUEST_SEQUENCE_NUMBER;

	/**
	 * {@link CometSubscription} instances.
	 */
	private static List<CometSubscription> subscriptions = new LinkedList<CometSubscription>();

	/**
	 * Indicates if subscribed.
	 */
	private static boolean isSubscribed = false;

	/**
	 * Flag indicating if multiple subscriptions.
	 */
	private static boolean isMultipleSubscriptions = false;

	/**
	 * <p>
	 * Allows specifying if initiating multiple subscriptions.
	 * <p>
	 * By default {@link OfficeFloorComet} will subscribe immediately.
	 * Specifying to have multiple subscriptions means that connecting to server
	 * for subscription of {@link CometEvent} instances is deferred and
	 * {@link #subscribe()} must be invoked to connect to server to start
	 * subscription.
	 * 
	 * @param isMultiple
	 *            Provide <code>true</code> for multiple subscriptions.
	 */
	public static void setMultipleSubscriptions(boolean isMultiple) {
		isMultipleSubscriptions = isMultiple;
	}

	/**
	 * Subscribes for asynchronous events.
	 * 
	 * @param listenerType
	 *            Listener interface type that should be marked by extending
	 *            {@link CometSubscriber}.
	 * @param handler
	 *            Handles the event.
	 * @param filterKey
	 *            Key to filter events. The {@link Object#equals(Object)} is
	 *            used to match the event match key to determine filtering. This
	 *            may be <code>null</code> to receive all events.
	 */
	public static <L extends CometSubscriber> void subscribe(
			Class<L> listenerType, final L handler, Object filterKey) {

		// Obtain the adapter
		final CometAdapter adapter = adapters.get(listenerType);

		// Add the subscription
		subscriptions.add(new CometSubscription(new CometInterest(listenerType
				.getName(), filterKey), handler, adapter));

		// Determine if multiple subscriptions
		if (!isMultipleSubscriptions) {
			// Not multiple subscriptions so subscribe immediately
			subscribe();
		}
	}

	/**
	 * Undertakes subscribing for {@link CometEvent} instances.
	 */
	public static void subscribe() {

		// Do not subscribe if already subscribed
		if (isSubscribed) {
			return; // already subscribed
		}
		isSubscribed = true; // subscribing

		// Create the list of interests
		List<CometInterest> interests = new ArrayList<CometInterest>(
				subscriptions.size());
		for (CometSubscription subscription : subscriptions) {
			interests.add(subscription.interest);
		}

		// Create request
		CometRequest request = new CometRequest(lastEventSequenceNumber,
				interests.toArray(new CometInterest[interests.size()]));

		// Subscribe for the next event
		subscriptionService.subscribe(request,
				new AsyncCallback<CometResponse>() {
					@Override
					public void onSuccess(CometResponse result) {

						// No long subscribed
						isSubscribed = false;

						// Obtain the events
						CometEvent[] events = result.getEvents();

						// Update the last event sequence number
						if (events.length > 0) {
							lastEventSequenceNumber = events[events.length - 1]
									.getSequenceNumber();
						}

						// Handle events
						for (CometEvent event : events) {
							for (CometSubscription subscription : subscriptions) {

								// Determine if match on listener type
								if (!(event.getListenerTypeName()
										.equals(subscription.interest
												.getListenerTypeName()))) {
									continue; // not event for this interest
								}

								// Determine if filtering
								Object interestFilterKey = subscription.interest
										.getFilterKey();
								if (interestFilterKey != null) {
									if (!(interestFilterKey.equals(event
											.getFilterKey()))) {
										continue; // mis-match on filter key
									}
								} else if (event.getFilterKey() != null) {
									continue; // only match if no filter key
								}

								// Handle event
								try {
									subscription.adapter.handleEvent(
											subscription.handler,
											event.getData());
								} catch (Throwable ex) {
									Window.alert("COMET HANDLE EVENT FAILURE: "
											+ ex.getMessage() + " ["
											+ ex.getClass().getName() + "]");
								}
							}
						}

						// Subscribe to the next event
						subscribe();
					}

					@Override
					public void onFailure(Throwable caught) {

						// No longer subscribed
						isSubscribed = false;

						// Alert error
						Window.alert("COMET SUBSCRIBE FAILURE: "
								+ caught.getMessage() + " ["
								+ caught.getClass().getName() + "]");
					}
				});
	}

	/**
	 * Publishes an event.
	 * 
	 * @param listenerType
	 *            Listener interface type that should be marked by extending
	 *            {@link CometSubscriber}.
	 * @param data
	 *            Data of event.
	 * @param matchKey
	 *            Match key to enable filtering. May be <code>null</code> to not
	 *            have the event filtered.
	 */
	public static <L extends CometSubscriber> void publish(
			Class<L> listenerType, Object data, Object matchKey) {
		// Publish event
		publicationService.publish(new CometEvent(listenerType.getName(), data,
				matchKey), new AsyncCallback<Long>() {
			@Override
			public void onSuccess(Long result) {
				// Do nothing as assume to be published
			}

			@Override
			public void onFailure(Throwable caught) {
				// Provide details of error
				StringBuilder message = new StringBuilder();
				message.append("COMET PUBLISH FAILURE: ");
				message.append(caught.getMessage());
				message.append(" [");
				message.append(caught.getClass().getName());
				message.append("]");

				// Determine if provide details of cause
				Throwable cause = caught.getCause();
				if (cause != null) {
					message.append(" CAUSE: ");
					message.append(cause.getMessage());
					message.append(" [");
					message.append(cause.getClass().getName());
					message.append("]");
				}

				// Alert regarding error
				Window.alert(message.toString());
			}
		});
	}

	/**
	 * Creates a publisher to allow using type safe publishing of events.
	 * 
	 * @param listenerType
	 *            Listener interface type that should be marked by extending
	 *            {@link CometSubscriber}.
	 * @param matchKey
	 *            Match key to enable filtering. May be <code>null</code> to not
	 *            have the event filtered.
	 * @return Implementation of {@link CometSubscriber} to enable invoking its
	 *         method to publish an event.
	 */
	@SuppressWarnings("unchecked")
	public static <L extends CometSubscriber> L createPublisher(
			Class<L> listenerType, Object matchKey) {

		// Obtain the adapter
		final CometAdapter adapter = adapters.get(listenerType);

		// Create the publisher
		L publisher = (L) adapter.createPublisher(matchKey);

		// Return the publisher
		return publisher;
	}

	/**
	 * All access via static methods.
	 */
	private OfficeFloorComet() {
	}

	/**
	 * Subscription for {@link CometEvent} instances.
	 */
	private static class CometSubscription {

		/**
		 * {@link CometInterest}.
		 */
		public final CometInterest interest;

		/**
		 * Handler.
		 */
		public final Object handler;

		/**
		 * {@link CometAdapter}.
		 */
		public final CometAdapter adapter;

		/**
		 * Initiate.
		 * 
		 * @param interest
		 *            {@link CometInterest}.
		 * @param handler
		 *            Handler.
		 * @param adapter
		 *            {@link CometAdapter}.
		 */
		public CometSubscription(CometInterest interest, Object handler,
				CometAdapter adapter) {
			this.interest = interest;
			this.handler = handler;
			this.adapter = adapter;
		}
	}

}