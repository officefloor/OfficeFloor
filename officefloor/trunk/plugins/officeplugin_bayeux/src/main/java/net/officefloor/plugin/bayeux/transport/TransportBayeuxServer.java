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
package net.officefloor.plugin.bayeux.transport;

import net.officefloor.plugin.bayeux.transport.TransportMessage.TransportMutable;
import net.officefloor.plugin.bayeux.transport.disconnect.DisconnectCallback;
import net.officefloor.plugin.bayeux.transport.handshake.HandshakeCallback;

import org.cometd.bayeux.Message;
import org.cometd.bayeux.Transport;
import org.cometd.bayeux.server.BayeuxServer;

/**
 * Interface of the {@link BayeuxServer} for the {@link Transport}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TransportBayeuxServer {

	/**
	 * Obtains the {@link BayeuxServer}.
	 * 
	 * @return {@link BayeuxServer}.
	 */
	BayeuxServer getBayeuxServer();

	/**
	 * Creates the {@link TransportMutable}.
	 * 
	 * @return {@link TransportMutable}.
	 */
	TransportMutable createMessage();

	/**
	 * Undertakes handshake.
	 * 
	 * @param message
	 *            Handshake {@link Message}.
	 * @param callback
	 *            {@link HandshakeCallback} to notify of handshake result.
	 */
	void handshake(Message message, HandshakeCallback callback);

	/**
	 * Undertakes disconnect.
	 * 
	 * @param message
	 *            Disconnect {@link Message}.
	 * @param callback
	 *            {@link DisconnectCallback} to notify of disconnect result.
	 */
	void disconnect(Message message, DisconnectCallback callback);

}