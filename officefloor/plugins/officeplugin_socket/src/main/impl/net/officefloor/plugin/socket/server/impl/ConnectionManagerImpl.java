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
package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.plugin.socket.server.ConnectionManager;
import net.officefloor.plugin.socket.server.EstablishedConnection;
import net.officefloor.plugin.socket.server.protocol.Connection;

/**
 * Manages the {@link EstablishedConnection} instances for the
 * {@link ServerSocketAccepter} instances across the available
 * {@link SocketListener} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionManagerImpl implements ConnectionManager {

	/**
	 * Listing of {@link SocketListener} instances.
	 */
	private final SocketListener[] socketListeners;

	/**
	 * Index of the next {@link SocketListener} for a new {@link Connection}.
	 */
	private AtomicInteger nextSocketListener = new AtomicInteger(0);

	/**
	 * Initiate.
	 * 
	 * @param socketListeners
	 *            Available {@link SocketListener} instances.
	 */
	public ConnectionManagerImpl(SocketListener... socketListeners) {
		this.socketListeners = socketListeners;
	}

	/*
	 * ====================== ConnectionManager ===========================
	 */

	@Override
	public void openSocketSelectors() throws IOException {
		for (SocketListener socketListener : this.socketListeners) {
			socketListener.openSelector();
		}
	}

	@Override
	public void manageConnection(EstablishedConnection connection) {

		// Spread the connections across the socket listeners
		int next = this.nextSocketListener.getAndAccumulate(1,
				(prev, increment) -> (prev + increment) % this.socketListeners.length);

		// Register connection with socket listener
		this.socketListeners[next].registerEstablishedConnection(connection);
	}

	@Override
	public void closeSocketSelectors() throws IOException {

		// Stop the socket listeners
		for (SocketListener socketListener : this.socketListeners) {
			socketListener.closeSelector();
		}
	}

}