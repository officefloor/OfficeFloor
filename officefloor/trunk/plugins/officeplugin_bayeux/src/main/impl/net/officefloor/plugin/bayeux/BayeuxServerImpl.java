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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.officefloor.plugin.bayeux.transport.ConnectResult;
import net.officefloor.plugin.bayeux.transport.DisconnectResult;
import net.officefloor.plugin.bayeux.transport.HandshakeResult;
import net.officefloor.plugin.bayeux.transport.PublishResult;
import net.officefloor.plugin.bayeux.transport.SubscribeResult;
import net.officefloor.plugin.bayeux.transport.TransportBayeuxServer;
import net.officefloor.plugin.bayeux.transport.TransportCallback;
import net.officefloor.plugin.bayeux.transport.TransportMessage.TransportMutable;
import net.officefloor.plugin.bayeux.transport.TransportServerSession;
import net.officefloor.plugin.bayeux.transport.UnsubscribeResult;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.Session;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.BayeuxServer.BayeuxServerListener;
import org.cometd.bayeux.server.BayeuxServer.ChannelListener;
import org.cometd.bayeux.server.BayeuxServer.Extension;
import org.cometd.bayeux.server.BayeuxServer.SessionListener;
import org.cometd.bayeux.server.BayeuxServer.SubscriptionListener;
import org.cometd.bayeux.server.ConfigurableServerChannel.Initializer;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;

/**
 * {@link BayeuxServer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class BayeuxServerImpl implements TransportBayeuxServer {

	/**
	 * Default {@link SecurityPolicy}.
	 */
	private static final SecurityPolicy defaultSecurityPolicy = new SecurityPolicy() {

		@Override
		public boolean canHandshake(BayeuxServer server, ServerSession session,
				ServerMessage message) {
			return true; // by default always allowed to handshake
		}

		@Override
		public boolean canCreate(BayeuxServer server, ServerSession session,
				String channelId, ServerMessage message) {
			return true; // by default always allowed to create a channel
		}

		@Override
		public boolean canSubscribe(BayeuxServer server, ServerSession session,
				ServerChannel channel, ServerMessage message) {
			return true; // by default always allowed to subscribe to a channel
		}

		@Override
		public boolean canPublish(BayeuxServer server, ServerSession session,
				ServerChannel channel, ServerMessage message) {
			return true; // by default always allowed publish to a channel
		}
	};

	/**
	 * Default {@link SessionIdentifierGenerator}.
	 */
	private static final SessionIdentifierGenerator defaultSessionIdentifierGenerator = new SessionIdentifierGenerator() {

		@Override
		public String newSessionId() {
			return UUID.randomUUID().toString();
		}
	};

	/**
	 * {@link SessionIdentifierGenerator}.
	 */
	private final SessionIdentifierGenerator sessionIdentifierGenerator;

	/**
	 * {@link ServerChannel} instances by their {@link ChannelId}.
	 */
	private final HashMap<String, ServerChannel> channels = new HashMap<String, ServerChannel>();

	/**
	 * {@link ServerSession} instances by their client identifier.
	 */
	private final HashMap<String, ServerSessionImpl> sessions = new HashMap<String, ServerSessionImpl>();

	/**
	 * {@link ChannelListener} instances.
	 */
	private final List<ChannelListener> channelListeners = new LinkedList<BayeuxServer.ChannelListener>();

	/**
	 * {@link SessionListener} instances.
	 */
	private final List<SessionListener> sessionListeners = new LinkedList<BayeuxServer.SessionListener>();

	/**
	 * {@link SubscriptionListener} instances.
	 */
	private final List<SubscriptionListener> subscriptionListeners = new LinkedList<BayeuxServer.SubscriptionListener>();

	/**
	 * {@link Extension} instances.
	 */
	private final List<Extension> extensions = new LinkedList<BayeuxServer.Extension>();

	/**
	 * {@link SecurityPolicy}.
	 */
	private SecurityPolicy securityPolicy = defaultSecurityPolicy;

	/**
	 * Default constructor.
	 */
	public BayeuxServerImpl() {
		this(null);
	}

	/**
	 * Configurable initiation.
	 * 
	 * @param sessionIdentifierGenerator
	 *            {@link SessionIdentifierGenerator}. May be <code>null</code>
	 *            to use default.
	 */
	public BayeuxServerImpl(
			SessionIdentifierGenerator sessionIdentifierGenerator) {
		this.sessionIdentifierGenerator = (sessionIdentifierGenerator != null) ? sessionIdentifierGenerator
				: defaultSessionIdentifierGenerator;
	}

	/**
	 * Creates a {@link BayeuxServer} instance.
	 * 
	 * @param session
	 *            {@link ServerSession} for the {@link BayeuxServer}.
	 * @return {@link BayeuxServer}.
	 */
	BayeuxServer createBayeuxServerInstance(ServerSession session) {
		return new BayeuxServerInstance(this, session);
	}

	/**
	 * Adds a {@link BayeuxServerListener}.
	 * 
	 * @param listener
	 *            {@link BayeuxServerListener}.
	 */
	void addListener(BayeuxServerListener listener) {
		if (listener instanceof ChannelListener) {
			this.channelListeners.add((ChannelListener) listener);
		} else if (listener instanceof SessionListener) {
			this.sessionListeners.add((SessionListener) listener);
		} else if (listener instanceof SubscriptionListener) {
			this.subscriptionListeners.add((SubscriptionListener) listener);
		} else {
			throw new IllegalStateException("Unknown "
					+ BayeuxServerListener.class.getSimpleName()
					+ " listener type " + listener.getClass().getName());
		}
	}

	/**
	 * Adds an {@link Extension}.
	 * 
	 * @param extension
	 *            {@link Extension}.
	 */
	void addExtension(Extension extension) {
		this.extensions.add(extension);
	}

	/**
	 * Specifies the {@link SecurityPolicy}.
	 * 
	 * @param securityPolicy
	 *            {@link SecurityPolicy}.
	 */
	void setSecurityPolicy(SecurityPolicy securityPolicy) {
		this.securityPolicy = (securityPolicy != null ? securityPolicy
				: defaultSecurityPolicy);
	}

	/**
	 * Creates the {@link ServerChannel}.
	 * 
	 * @param channelId
	 *            {@link ChannelId} value.
	 * @param initializers
	 *            {@link Initializer} instances for the {@link ServerChannel}.
	 * @return <code>true</code> if the {@link ServerChannel} is initialised.
	 */
	boolean createIfAbsent(String channelId, Initializer... initializers) {

		// Create the server channel
		ServerChannel channel = new ServerChannelImpl(channelId, this);

		// Create the bayeux server instance
		BayeuxServer server = this.createBayeuxServerInstance(null);

		// Check if may add the channel
		boolean isAllowedCreateChannel = this.securityPolicy.canCreate(server,
				null, channelId, null);
		if (!isAllowedCreateChannel) {
			return false; // not allowed to create channel
		}

		// Undertake general channel initialisation
		for (ChannelListener listener : this.channelListeners) {
			listener.configureChannel(channel);
		}

		// Undertake channel specific initialisation
		for (Initializer initializer : initializers) {
			initializer.configureChannel(channel);
		}

		// Register the channel
		this.channels.put(channelId, channel);

		// Undertake notifying channel added
		for (ChannelListener listener : this.channelListeners) {
			listener.channelAdded(channel);
		}

		// Flag initialised
		return true;
	}

	/**
	 * Obtains the {@link ServerChannel}.
	 * 
	 * @param channelId
	 *            {@link ChannelId} value.
	 * @return {@link ServerChannel}.
	 */
	ServerChannel getChannel(String channelId) {
		return this.channels.get(channelId);
	}

	/**
	 * Adds a new {@link ServerSession}.
	 * 
	 * @param clientId
	 *            Identifier for the {@link ServerSession}.
	 * @param handshakeMessage
	 *            Handshake {@link ServerMessage}.
	 * @return {@link TransportServerSession} for the {@link ServerSession} or
	 *         <code>null</code> if not allowed to create the
	 *         {@link ServerSession}.
	 */
	public TransportServerSession addSession(String clientId,
			ServerMessage handshakeMessage) {

		// Create the server session
		ServerSessionImpl session = new ServerSessionImpl(clientId, this);

		// Create the bayeux server instance
		BayeuxServer server = this.createBayeuxServerInstance(session);

		// Notify of adding the session
		for (SessionListener listener : this.sessionListeners) {
			listener.sessionAdded(session);
		}

		// Determine if allowed to create session
		boolean isAllowedToHandshake = this.securityPolicy.canHandshake(server,
				session, handshakeMessage);
		if (!isAllowedToHandshake) {
			return null; // not allowed to create session
		}

		// Register the server session
		this.sessions.put(clientId, session);

		// Return the server session
		return session;
	}

	/**
	 * Obtains the {@link ServerSession}.
	 * 
	 * @param clientId
	 *            Identifier for the {@link ServerSession}.
	 * @return {@link ServerSession}.
	 */
	ServerSession getSession(String clientId) {
		return this.sessions.get(clientId);
	}

	/**
	 * <p>
	 * Obtains the {@link ServerSession} for the {@link Session}.
	 * <p>
	 * It also validate that the {@link Session} is valid.
	 * 
	 * @param session
	 *            {@link Session}.
	 * @return Corresponding {@link ServerSession}.
	 * @throws IllegalArgumentException
	 *             If the {@link Session} is invalid.
	 */
	ServerSessionImpl getValidServerSession(Session session) {

		// Obtain the server session
		ServerSessionImpl serverSession;
		if (session instanceof ServerSessionImpl) {
			serverSession = (ServerSessionImpl) session;
		} else {
			// Not matched, so invalid session
			throw new IllegalArgumentException(
					"Invalid session. Unknown session type "
							+ session.getClass().getName());
		}

		// Return the server session
		return serverSession;
	}

	/**
	 * Triggers the
	 * {@link SubscriptionListener#subscribed(ServerSession, ServerChannel)}.
	 * 
	 * @param session
	 *            {@link ServerSession} subscribing.
	 * @param channel
	 *            {@link ServerChannel} being subscribed to.
	 */
	void _SubscriptionListener_subscribed(ServerSession session,
			ServerChannel channel) {

		// Undertake subscription listening
		for (SubscriptionListener listener : this.subscriptionListeners) {
			listener.subscribed(session, channel);
		}
	}

	/**
	 * Triggers the
	 * {@link SubscriptionListener#unsubscribed(ServerSession, ServerChannel)}.
	 * 
	 * @param session
	 *            {@link ServerSession} unsubscribing.
	 * @param channel
	 *            {@link ServerChannel} being unsubscribed from.
	 */
	void _SubscriptionListener_unsubscribed(ServerSession session,
			ServerChannel channel) {

		// Undertake unsubscription listening
		for (SubscriptionListener listener : this.subscriptionListeners) {
			listener.unsubscribed(session, channel);
		}
	}

	/**
	 * Triggers the {@link Extension#rcv(ServerSession, Mutable)}.
	 * 
	 * @param from
	 *            {@link ServerSession} publishing the {@link ServerMessage}.
	 * @param message
	 *            {@link Mutable} {@link ServerMessage} being published.
	 * @return <code>false</code> to stop publishing the {@link ServerMessage}.
	 */
	boolean _Extension_rcv(ServerSession from, Mutable message) {

		// Undertake receive message extensions
		for (Extension extension : this.extensions) {
			boolean isContinueToPublish = extension.rcv(from, message);
			if (!isContinueToPublish) {
				// Do not publish the message
				return false;
			}
		}

		// As here, continue to publish message
		return true;
	}

	/**
	 * Triggers the
	 * {@link Extension#send(ServerSession, ServerSession, Mutable)}.
	 * 
	 * @param from
	 *            {@link ServerSession} sending the {@link ServerMessage}.
	 * @param to
	 *            {@link ServerSession} receiving the {@link ServerMessage}.
	 * @param message
	 *            {@link Mutable} {@link ServerMessage}.
	 * @return <code>false</code> to stop publishing the {@link ServerMessage}.
	 */
	boolean _Extension_send(ServerSession from, ServerSession to,
			Mutable message) {

		// Undertake send message extensions
		for (Extension extension : this.extensions) {
			boolean isContinueToPublish = extension.send(from, to, message);
			if (!isContinueToPublish) {
				// Do not publish the message
				return false;
			}
		}

		// As here, continue to publish message
		return true;
	}

	/**
	 * Triggers the
	 * {@link SecurityPolicy#canSubscribe(BayeuxServer, ServerSession, ServerChannel, ServerMessage)}
	 * .
	 * 
	 * @param session
	 *            {@link ServerSession} attempting to subscribe.
	 * @param channel
	 *            {@link ServerChannel} being subscribed to.
	 * @param message
	 *            {@link ServerMessage} requesting the subscribe.
	 * @return <code>true</code> if may subscribe.
	 */
	boolean _SecurityPolicy_canSubscribe(ServerSession session,
			ServerChannel channel, ServerMessage message) {

		// Create the server
		BayeuxServer server = this.createBayeuxServerInstance(session);

		// Return whether may subscribe
		return this.securityPolicy.canSubscribe(server, session, channel,
				message);
	}

	/**
	 * Triggers the
	 * {@link SecurityPolicy#canPublish(BayeuxServer, ServerSession, ServerChannel, ServerMessage)}
	 * .
	 * 
	 * @param session
	 *            {@link ServerSession} attempting to publish.
	 * @param channel
	 *            {@link ServerChannel} being published on.
	 * @param message
	 *            {@link ServerMessage} being published.
	 * @return <code>true</code> if may publish.
	 */
	boolean _SecurityPolicy_canPublish(ServerSession session,
			ServerChannel channel, ServerMessage message) {

		// Create the server
		BayeuxServer server = this.createBayeuxServerInstance(session);

		// Return whether may publish
		return this.securityPolicy
				.canPublish(server, session, channel, message);
	}

	/*
	 * ======================== TransportBayeuxServer ==========================
	 */

	@Override
	public BayeuxServer getBayeuxServer() {
		return this.createBayeuxServerInstance(null);
	}

	@Override
	public TransportMutable createMessage() {
		// TODO implement TransportBayeuxServer.createMessage
		throw new UnsupportedOperationException(
				"TODO implement TransportBayeuxServer.createMessage");
	}

	@Override
	public void handshake(Message message,
			TransportCallback<? super HandshakeResult> callback) {
		// TODO implement TransportBayeuxServer.handshake
		throw new UnsupportedOperationException(
				"TODO implement TransportBayeuxServer.handshake");
	}

	@Override
	public void connect(Message message,
			TransportCallback<? super ConnectResult> callback) {
		// TODO implement TransportBayeuxServer.connect
		throw new UnsupportedOperationException(
				"TODO implement TransportBayeuxServer.connect");
	}

	@Override
	public void disconnect(Message message,
			TransportCallback<? super DisconnectResult> callback) {
		// TODO implement TransportBayeuxServer.disconnect
		throw new UnsupportedOperationException(
				"TODO implement TransportBayeuxServer.disconnect");
	}

	@Override
	public void subscribe(Message message,
			TransportCallback<? super SubscribeResult> callback) {
		// TODO implement TransportBayeuxServer.subscribe
		throw new UnsupportedOperationException(
				"TODO implement TransportBayeuxServer.subscribe");
	}

	@Override
	public void unsubscribe(Message message,
			TransportCallback<? super UnsubscribeResult> callback) {
		// TODO implement TransportBayeuxServer.unsubscribe
		throw new UnsupportedOperationException(
				"TODO implement TransportBayeuxServer.unsubscribe");
	}

	@Override
	public void publish(Message message,
			TransportCallback<? super PublishResult> callback) {
		// TODO implement TransportBayeuxServer.publish
		throw new UnsupportedOperationException(
				"TODO implement TransportBayeuxServer.publish");
	}

}