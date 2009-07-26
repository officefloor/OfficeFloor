/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
public class ConnectionManager<F extends Enum<F>, CH extends ConnectionHandler>
		implements
		Work,
		WorkFactory<ConnectionManager<F, CH>>,
		TaskFactory<ConnectionManager<F, CH>, SocketListener.SocketListenerDependencies, Indexed> {

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * {@link Server}.
	 */
	private final Server<F, CH> server;

	/**
	 * Maximum {@link Connection} instances per {@link SocketListener}.
	 */
	private final int maxConnPerListener;

	/**
	 * Listing of active {@link SocketListener} instances.
	 */
	private final List<SocketListener<F, CH>> socketListeners = new LinkedList<SocketListener<F, CH>>();

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
			Server<F, CH> server, int maxConnPerListener) {
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
			ConnectionImpl<F, CH> connection,
			TaskContext<ServerSocketAccepter<F, CH>, None, ServerSocketAccepterFlows> taskContext)
			throws IOException {

		// Attempt to register with an existing socket listener
		synchronized (this.socketListeners) {
			for (SocketListener<F, CH> listener : this.socketListeners) {
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
	void socketListenerComplete(SocketListener<F, CH> socketListener) {
		synchronized (this.socketListeners) {
			this.socketListeners.remove(socketListener);
		}
	}

	/*
	 * ===================== WorkFactory =====================================
	 */

	@Override
	public ConnectionManager<F, CH> createWork() {
		return this;
	}

	/*
	 * ===================== TaskFactory =====================================
	 */

	@Override
	public Task<ConnectionManager<F, CH>, SocketListener.SocketListenerDependencies, Indexed> createTask(
			ConnectionManager<F, CH> work) {

		// Create the socket listener
		SocketListener<F, CH> socketListener = new SocketListener<F, CH>(
				this.selectorFactory, this.server, this.maxConnPerListener);

		// Register the socket listener
		synchronized (this.socketListeners) {
			this.socketListeners.add(socketListener);
		}

		// Return the socket listener
		return socketListener;
	}

}