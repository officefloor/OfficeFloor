/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.nio.channels.Selector;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;

/**
 * Manages the {@link Connection} instances for {@link ServerSocketAccepter}
 * across the available {@link SocketListener} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionManager<CH extends ConnectionHandler> {

	/**
	 * Listing of {@link SocketListener} instances.
	 */
	private final SocketListener<CH>[] socketListeners;

	/**
	 * Index of the next {@link SocketListener} for a new {@link Connection}.
	 */
	private int nextSocketListener = 0;

	/**
	 * Initiate.
	 * 
	 * @param socketListeners
	 *            Available {@link SocketListener} instances.
	 */
	public ConnectionManager(SocketListener<CH>[] socketListeners) {
		this.socketListeners = socketListeners;
	}

	/**
	 * Opens the {@link Selector} instances for the {@link SocketListener}
	 * instances.
	 * 
	 * @throws IOException
	 *             If fails to open all the {@link Selector} instances.
	 */
	void openSocketListenerSelectors() throws IOException {
		for (SocketListener<CH> socketListener : this.socketListeners) {
			socketListener.openSelector();
		}
	}

	/**
	 * Closes the {@link Selector} instances for the {@link SocketListener}
	 * instances.
	 */
	void closeSocketListenerSelectors() {
		for (SocketListener<CH> socketListener : this.socketListeners) {
			socketListener.closeSelector();
		}
	}

	/**
	 * Registers the {@link Connection} for management.
	 * 
	 * @param connection
	 *            {@link Connection} to be managed.
	 * @param taskContext
	 *            To invoke new {@link SocketListener} instances.
	 * @throws IOException
	 *             If fails registering the {@link Connection}.
	 */
	void registerConnection(ConnectionImpl<CH> connection,
			TaskContext<ServerSocketAccepter<CH>, None, None> taskContext)
			throws IOException {

		// Spread the connections across the socket listeners.
		// (synchronized as may be called by differing accepters/threads)
		int next;
		synchronized (this.socketListeners) {
			// TODO determine if better algorithm than round robin
			next = this.nextSocketListener;
			this.nextSocketListener = (this.nextSocketListener + 1)
					% this.socketListeners.length;
		}

		// Register connection with socket listener
		this.socketListeners[next].registerConnection(connection);
	}

}