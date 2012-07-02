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
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.impl.ServerSocketAccepter.ServerSocketAccepterFlows;

/**
 * Manages the {@link Connection} instances with the {@link Server}.
 *
 * @author Daniel Sagenschneider
 */
public class ConnectionManager<CH extends ConnectionHandler>
		implements
		Work,
		WorkFactory<ConnectionManager<CH>>,
		TaskFactory<ConnectionManager<CH>, SocketListener.SocketListenerDependencies, Indexed> {

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * {@link Server}.
	 */
	private final Server<CH> server;

	/**
	 * Maximum {@link Connection} instances per {@link SocketListener}.
	 */
	private final int maxConnPerListener;

	/**
	 * Listing of active {@link SocketListener} instances.
	 */
	private final List<SocketListener<CH>> socketListeners = new LinkedList<SocketListener<CH>>();

	/**
	 * Initiate.
	 *
	 * @param moSource
	 *            {@link AbstractServerSocketManagedObjectSource}.
	 * @param maxConnPerListener
	 *            Maximum number of {@link Connection} instances per
	 *            {@link SocketListener}.
	 * @throws IOException
	 *             If fails creation.
	 */
	public ConnectionManager(SelectorFactory selectorFactory,
			Server<CH> server, int maxConnPerListener) {
		this.selectorFactory = selectorFactory;
		this.server = server;
		this.maxConnPerListener = maxConnPerListener;
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
	void registerConnection(
			ConnectionImpl<CH> connection,
			TaskContext<ServerSocketAccepter<CH>, None, ServerSocketAccepterFlows> taskContext)
			throws IOException {

		// Attempt to register with an existing socket listener
		synchronized (this.socketListeners) {
			for (SocketListener<CH> listener : this.socketListeners) {
				if (listener.registerConnection(connection)) {
					// Registered
					return;
				}
			}
		}

		// Not registered if at this point, therefore create a new listener
		taskContext.doFlow(ServerSocketAccepterFlows.LISTEN, connection);
	}

	/**
	 * Flags that the input {@link SocketListener} has completed and no further
	 * {@link Connection} instances may be registered with it.
	 *
	 * @param socketListener
	 *            Completed {@link SocketListener}.
	 */
	void socketListenerComplete(SocketListener<CH> socketListener) {
		synchronized (this.socketListeners) {
			this.socketListeners.remove(socketListener);
		}
	}

	/*
	 * ===================== WorkFactory =====================================
	 */

	@Override
	public ConnectionManager<CH> createWork() {
		return this;
	}

	/*
	 * ===================== TaskFactory =====================================
	 */

	@Override
	public Task<ConnectionManager<CH>, SocketListener.SocketListenerDependencies, Indexed> createTask(
			ConnectionManager<CH> work) {

		// Create the socket listener
		SocketListener<CH> socketListener = new SocketListener<CH>(
				this.selectorFactory, this.server, this.maxConnPerListener);

		// Register the socket listener
		synchronized (this.socketListeners) {
			this.socketListeners.add(socketListener);
		}

		// Return the socket listener
		return socketListener;
	}

}