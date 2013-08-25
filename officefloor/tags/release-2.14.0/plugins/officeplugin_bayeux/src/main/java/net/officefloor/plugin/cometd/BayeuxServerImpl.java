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
package net.officefloor.plugin.cometd;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.cometd.publish.TransportServerSession;

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.BayeuxServer.Extension;
import org.cometd.bayeux.server.BayeuxServer.SubscriptionListener;
import org.cometd.bayeux.server.SecurityPolicy;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.bayeux.server.BayeuxServer.BayeuxServerListener;
import org.cometd.bayeux.server.BayeuxServer.ChannelListener;
import org.cometd.bayeux.server.BayeuxServer.SessionListener;
import org.cometd.bayeux.server.ConfigurableServerChannel.Initializer;

/**
 * {@link BayeuxServer} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class BayeuxServerImpl {

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
	private SecurityPolicy securityPolicy = null;

	/**
	 * Creates a {@link BayeuxServer} instance.
	 * 
	 * @param session
	 *            {@link ServerSession} for the {@link BayeuxServer}.
	 * @return {@link BayeuxServer}.
	 */
	public BayeuxServer createBayeuxServerInstance(ServerSession session) {
		return new BayeuxServerInstance(this, session);
	}

	/**
	 * Adds a {@link BayeuxServerListener}.
	 * 
	 * @param listener
	 *            {@link BayeuxServerListener}.
	 */
	public void addListener(BayeuxServerListener listener) {
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
	public void addExtension(Extension extension) {
		this.extensions.add(extension);
	}

	/**
	 * Specifies the {@link SecurityPolicy}.
	 * 
	 * @param securityPolicy
	 *            {@link SecurityPolicy}.
	 */
	public void setSecurityPolicy(SecurityPolicy securityPolicy) {
		this.securityPolicy = securityPolicy;
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
	public boolean createIfAbsent(String channelId, Initializer... initializers) {

		// Create the server channel
		ServerChannel channel = new ServerChannelImpl();

		// Flag as initialised
		for (Initializer initializer : initializers) {
			initializer.configureChannel(channel);
		}

		// Register the channel
		this.channels.put(channelId, channel);

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
	public ServerChannel getChannel(String channelId) {
		return this.channels.get(channelId);
	}

	/**
	 * Adds a new {@link ServerSession}.
	 * 
	 * @param clientId
	 *            Identifier for the {@link ServerSession}.
	 * @param handshakeMessage
	 *            Handshake {@link ServerMessage}.
	 * @return {@link TransportServerSession} for the {@link ServerSession}.
	 */
	public TransportServerSession addSession(String clientId,
			ServerMessage handshakeMessage) {

		// Create the server session
		ServerSessionImpl session = new ServerSessionImpl();

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
	public ServerSession getSession(String clientId) {
		return this.sessions.get(clientId);
	}

}