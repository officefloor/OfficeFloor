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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
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
public class ConnectionManagerImpl extends
		AbstractSingleTask<ConnectionManagerImpl, None, None> implements
		ConnectionManager {

	/**
	 * Heart beat interval in milliseconds.
	 */
	private final long heartbeatInterval;

	/**
	 * Listing of {@link SocketListener} instances.
	 */
	private final SocketListener[] socketListeners;

	/**
	 * Index of the next {@link SocketListener} for a new {@link Connection}.
	 */
	private int nextSocketListener = 0;

	/**
	 * Indicates if closed.
	 */
	private volatile boolean isClosed = false;

	/**
	 * Initiate.
	 * 
	 * @param heartbeatInterval
	 *            Heart beat interval in milliseconds.
	 * @param socketListeners
	 *            Available {@link SocketListener} instances.
	 */
	public ConnectionManagerImpl(long heartbeatInterval,
			SocketListener... socketListeners) {
		this.heartbeatInterval = heartbeatInterval;
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
		this.socketListeners[next].registerEstablishedConnection(connection);
	}

	@Override
	public void closeSocketSelectors() throws IOException {

		// Flag to close connection manager
		this.isClosed = true;

		// Stop the socket listeners
		for (SocketListener socketListener : this.socketListeners) {
			socketListener.closeSelector();
		}
	}

	/*
	 * ====================== Task ===========================
	 */

	@Override
	public Object doTask(TaskContext<ConnectionManagerImpl, None, None> context)
			throws Throwable {

		// Continue execution until closed
		context.setComplete(this.isClosed);

		// Wait period of time to provide heart beat
		Thread.sleep(this.heartbeatInterval);

		// Trigger a heart beat for the socket listeners
		for (SocketListener socketListener : this.socketListeners) {
			socketListener.doHeartBeat();
		}

		// Should be no further tasks
		return null;
	}

}