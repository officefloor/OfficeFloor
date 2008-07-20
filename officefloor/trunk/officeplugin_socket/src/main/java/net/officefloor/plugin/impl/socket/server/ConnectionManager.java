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
import net.officefloor.frame.api.execute.WorkContext;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.Server;

/**
 * Manages the {@link Connection} instances with the {@link Server}.
 * 
 * TODO handle cleaning up SocketListeners no longer listening to connections.
 * 
 * @author Daniel
 */
class ConnectionManager implements Work, WorkFactory<ConnectionManager>,
		TaskFactory<Object, ConnectionManager, None, Indexed> {

	/**
	 * {@link ServerSocketManagedObjectSource}.
	 */
	private final ServerSocketManagedObjectSource moSource;

	/**
	 * Maximum number of {@link Connection} instances per {@link SocketListener}.
	 */
	private final int maxConnPerListener;

	/**
	 * list of {@link SocketListener} instances.
	 */
	private final List<SocketListener> socketListeners = new ArrayList<SocketListener>();

	/**
	 * Initiate.
	 * 
	 * @param moSource
	 *            {@link ServerSocketManagedObjectSource}.
	 * @param maxConnPerListener
	 *            Maximum number of {@link Connection} instances per
	 *            {@link SocketListener}.
	 * @throws IOException
	 *             If fails creation.
	 */
	ConnectionManager(ServerSocketManagedObjectSource moSource,
			int maxConnPerListener) throws IOException {
		this.moSource = moSource;
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
	void registerConnection(ConnectionImpl<?> connection,
			TaskContext<?, ?, ?, ?> taskContext) throws IOException {

		// Attempt to register with existing socket listeners
		synchronized (this) {
			for (SocketListener listener : this.socketListeners) {
				if (listener.registerConnection(connection)) {
					// Registered
					return;
				}
			}
		}

		// Not registered if at this point, therefore create a new listener
		taskContext.doFlow(0, connection);
	}

	/*
	 * ====================================================================
	 * WorkFactory, Work, TaskFactory
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
	 */
	public ConnectionManager createWork() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
	 */
	public void setWorkContext(WorkContext context) throws Exception {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(W)
	 */
	public Task<Object, ConnectionManager, None, Indexed> createTask(
			ConnectionManager work) {
		// Return a new socket listener
		return new SocketListener(this.moSource, this.maxConnPerListener);
	}

}
