/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.impl.socket.server.ServerSocketAccepter.ServerSocketAccepterFlows;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.Server;

/**
 * Manages the {@link Connection} instances with the {@link Server}.
 * 
 * @author Daniel
 */
class ConnectionManager
		implements
		Work,
		WorkFactory<ConnectionManager>,
		TaskFactory<ConnectionManager, SocketListener.SocketListenerDependencies, Indexed> {

	/**
	 * {@link SelectorFactory}.
	 */
	private final SelectorFactory selectorFactory;

	/**
	 * {@link Server}.
	 */
	private final Server<?> server;

	/**
	 * Maximum {@link Connection} instances per {@link SocketListener}.
	 */
	private final int maxConnPerListener;

	/**
	 * Listing of active {@link SocketListener} instances.
	 */
	private final List<SocketListener> socketListeners = new ArrayList<SocketListener>();

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
	ConnectionManager(SelectorFactory selectorFactory, Server<?> server,
			int maxConnPerListener) {
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
			ConnectionImpl<?> connection,
			TaskContext<ServerSocketAccepter, None, ServerSocketAccepterFlows> taskContext)
			throws IOException {

		// Attempt to register with an existing socket listener
		synchronized (this.socketListeners) {
			for (SocketListener listener : this.socketListeners) {
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
	void socketListenerComplete(SocketListener socketListener) {
		synchronized (this.socketListeners) {
			this.socketListeners.remove(socketListener);
		}
	}

	/*
	 * ===================== WorkFactory =====================================
	 */

	@Override
	public ConnectionManager createWork() {
		return this;
	}

	/*
	 * ===================== TaskFactory =====================================
	 */

	@Override
	public Task<ConnectionManager, SocketListener.SocketListenerDependencies, Indexed> createTask(
			ConnectionManager work) {

		// Create the socket listener
		SocketListener socketListener = new SocketListener(
				this.selectorFactory, this.server, this.maxConnPerListener);

		// Register the socket listener
		synchronized (this.socketListeners) {
			this.socketListeners.add(socketListener);
		}

		// Return the socket listener
		return socketListener;
	}

}