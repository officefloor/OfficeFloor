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
package net.officefloor.plugin.bayeux;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.Session;
import org.cometd.bayeux.server.Authorizer;
import org.cometd.bayeux.server.Authorizer.Operation;
import org.cometd.bayeux.server.Authorizer.Result;
import org.cometd.bayeux.server.Authorizer.Result.Denied;
import org.cometd.bayeux.server.Authorizer.Result.Granted;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.bayeux.server.ServerSession.Extension;

/**
 * {@link ServerChannel} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerChannelImpl implements ServerChannel {

	/**
	 * Identifier of this {@link ServerChannel}.
	 */
	private final String id;

	/**
	 * {@link BayeuxServerImpl}.
	 */
	private final BayeuxServerImpl server;

	/**
	 * Indicates of this {@link ServerChannel} is persistent.
	 */
	private volatile boolean isPersistent = false;

	/**
	 * Subscribed {@link ServerSession} instances to this {@link ServerChannel}.
	 */
	private final List<ServerSession> subscriptions = new LinkedList<ServerSession>();

	/**
	 * {@link Authorizer} instances.
	 */
	private final List<Authorizer> authorizers = new LinkedList<Authorizer>();

	/**
	 * {@link ServerChannelListener} instances.
	 */
	private final List<ServerChannelListener> listeners = new LinkedList<ServerChannelListener>();

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Identifier of this {@link ServerChannel}.
	 * @param server
	 *            {@link BayeuxServerImpl}.
	 */
	public ServerChannelImpl(String id, BayeuxServerImpl server) {
		this.id = id;
		this.server = server;
	}

	/*
	 * ==================== ServerChannel ======================
	 */

	@Override
	public void addListener(ServerChannelListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(ServerChannelListener listener) {
		// TODO implement ConfigurableServerChannel.removeListener
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.removeListener");
	}

	@Override
	public List<ServerChannelListener> getListeners() {
		// TODO implement ConfigurableServerChannel.getListeners
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.getListeners");
	}

	@Override
	public boolean isLazy() {
		// TODO implement ConfigurableServerChannel.isLazy
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.isLazy");
	}

	@Override
	public void setLazy(boolean lazy) {
		// TODO implement ConfigurableServerChannel.setLazy
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.setLazy");
	}

	@Override
	public long getLazyTimeout() {
		// TODO implement ConfigurableServerChannel.getLazyTimeout
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.getLazyTimeout");
	}

	@Override
	public void setLazyTimeout(long lazyTimeout) {
		// TODO implement ConfigurableServerChannel.setLazyTimeout
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.setLazyTimeout");
	}

	@Override
	public boolean isPersistent() {
		return this.isPersistent;
	}

	@Override
	public void setPersistent(boolean persistent) {
		this.isPersistent = persistent;
	}

	@Override
	public void addAuthorizer(Authorizer authorizer) {
		this.authorizers.add(authorizer);
	}

	@Override
	public void removeAuthorizer(Authorizer authorizer) {
		// TODO implement ConfigurableServerChannel.removeAuthorizer
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.removeAuthorizer");
	}

	@Override
	public List<Authorizer> getAuthorizers() {
		// TODO implement ConfigurableServerChannel.getAuthorizers
		throw new UnsupportedOperationException(
				"TODO implement ConfigurableServerChannel.getAuthorizers");
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ChannelId getChannelId() {
		return new ChannelId(this.id);
	}

	@Override
	public boolean isMeta() {
		// TODO implement Channel.isMeta
		throw new UnsupportedOperationException("TODO implement Channel.isMeta");
	}

	@Override
	public boolean isService() {
		// TODO implement Channel.isService
		throw new UnsupportedOperationException(
				"TODO implement Channel.isService");
	}

	@Override
	public boolean isBroadcast() {
		// TODO implement Channel.isBroadcast
		throw new UnsupportedOperationException(
				"TODO implement Channel.isBroadcast");
	}

	@Override
	public boolean isWild() {
		// TODO implement Channel.isWild
		throw new UnsupportedOperationException("TODO implement Channel.isWild");
	}

	@Override
	public boolean isDeepWild() {
		// TODO implement Channel.isDeepWild
		throw new UnsupportedOperationException(
				"TODO implement Channel.isDeepWild");
	}

	@Override
	public void setAttribute(String name, Object value) {
		// TODO implement Channel.setAttribute
		throw new UnsupportedOperationException(
				"TODO implement Channel.setAttribute");
	}

	@Override
	public Object getAttribute(String name) {
		// TODO implement Channel.getAttribute
		throw new UnsupportedOperationException(
				"TODO implement Channel.getAttribute");
	}

	@Override
	public Set<String> getAttributeNames() {
		// TODO implement Channel.getAttributeNames
		throw new UnsupportedOperationException(
				"TODO implement Channel.getAttributeNames");
	}

	@Override
	public Object removeAttribute(String name) {
		// TODO implement Channel.removeAttribute
		throw new UnsupportedOperationException(
				"TODO implement Channel.removeAttribute");
	}

	@Override
	public Set<ServerSession> getSubscribers() {
		// TODO implement ServerChannel.getSubscribers
		throw new UnsupportedOperationException(
				"TODO implement ServerChannel.getSubscribers");
	}

	@Override
	public boolean subscribe(ServerSession session) {

		// Obtain the valid server session
		ServerSessionImpl validSession = this.server
				.getValidServerSession(session);

		// Validate allowed to subscribe
		boolean isAllowedToSubscribe = this.server
				._SecurityPolicy_canSubscribe(validSession, this, null);
		if (!isAllowedToSubscribe) {
			return false; // not allowed to subscribe
		}

		// Ensure able to subscribe
		Result lastResult = null;
		boolean isGranted = false;
		for (Authorizer authorisor : this.authorizers) {
			lastResult = authorisor.authorize(Operation.SUBSCRIBE,
					this.getChannelId(), validSession, null);
			if (lastResult instanceof Granted) {
				isGranted = true;
			} else if (lastResult instanceof Denied) {
				return false; // do not allow subscribe
			}
		}
		if ((lastResult != null) && (!isGranted)) {
			// Authorisor but none granted the subscribe
			return false; // do not allow subscribe
		}

		// Flag that subscribed with channel
		for (ServerChannelListener listener : this.listeners) {

			// Determine if subscription listener
			if (listener instanceof SubscriptionListener) {
				SubscriptionListener subscriptionListener = (SubscriptionListener) listener;

				// Undertake subscription listening
				subscriptionListener.subscribed(validSession, this);
			}
		}

		// Flag that subscribed with server
		this.server._SubscriptionListener_subscribed(validSession, this);

		// Flag that subscribed with session
		validSession.registerSubscribedChannel(this);

		// Subscribe
		this.subscriptions.add(validSession);

		// Successfully subscribed
		return true;
	}

	@Override
	public boolean unsubscribe(ServerSession session) {

		// Obtain the valid server session
		ServerSessionImpl validSession = this.server
				.getValidServerSession(session);

		// Flag that unsubscribed with channel
		for (ServerChannelListener listener : this.listeners) {

			// Determine if subscription listener
			if (listener instanceof SubscriptionListener) {
				SubscriptionListener subscriptionListener = (SubscriptionListener) listener;

				// Undertake unsubscribe listening
				subscriptionListener.unsubscribed(validSession, this);
			}
		}

		// Flag that unsubscribed with server
		this.server._SubscriptionListener_unsubscribed(validSession, this);

		// Notify session of unsubscribe
		validSession.unregisterSubscribedChannel(this);

		// Unsubscribe
		this.subscriptions.remove(validSession);

		// Successfully unsubscribed
		return true;
	}

	@Override
	public void publish(Session from, Mutable message) {

		// Obtain the valid server session
		ServerSession validSession = this.server.getValidServerSession(from);

		// Ensure able to publish
		boolean isAllowedToPublish = this.server._SecurityPolicy_canPublish(
				validSession, this, message);
		if (!isAllowedToPublish) {
			return; // do not publish
		}

		// Ensure able to publish
		Result lastResult = null;
		boolean isGranted = false;
		for (Authorizer authorisor : this.authorizers) {
			lastResult = authorisor.authorize(Operation.PUBLISH,
					this.getChannelId(), validSession, message);
			if (lastResult instanceof Granted) {
				isGranted = true;
			} else if (lastResult instanceof Denied) {
				return; // do not publish
			}
		}
		if ((lastResult != null) && (!isGranted)) {
			// Authorisor but none granted the publish
			return; // do not publish
		}

		// Enable server extensions
		isAllowedToPublish = this.server._Extension_rcv(validSession, message);
		if (!isAllowedToPublish) {
			return; // do not publish
		}

		// Enable session extensions
		if (validSession != null) {
			for (Extension extension : validSession.getExtensions()) {
				isAllowedToPublish = extension.rcv(validSession, message);
				if (!isAllowedToPublish) {
					return; // do not publish
				}
			}
		}

		// Enable listening on the channel
		for (ServerChannelListener listener : this.listeners) {

			// Determine if message listener
			if (listener instanceof MessageListener) {
				MessageListener messageListener = (MessageListener) listener;

				// Undertake message listening
				isAllowedToPublish = messageListener.onMessage(validSession,
						this, message);
				if (!isAllowedToPublish) {
					return; // do not publish
				}
			}
		}

		// Publish the message to the subscribers
		for (ServerSession session : this.subscriptions) {
			session.deliver(from, message);
		}
	}

	@Override
	public void publish(Session from, Object data, String id) {
		// TODO implement ServerChannel.publish
		throw new UnsupportedOperationException(
				"TODO implement ServerChannel.publish");
	}

	@Override
	public void remove() {
		// TODO implement ServerChannel.remove
		throw new UnsupportedOperationException(
				"TODO implement ServerChannel.remove");
	}

}