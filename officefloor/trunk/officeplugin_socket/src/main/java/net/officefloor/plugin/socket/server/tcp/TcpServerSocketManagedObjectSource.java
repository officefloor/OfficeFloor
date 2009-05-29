/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.tcp;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.impl.socket.server.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;
import net.officefloor.plugin.socket.server.tcp.TcpServer.TcpServerFlows;
import net.officefloor.plugin.socket.server.tcp.api.ServerTcpConnection;

/**
 * {@link ManagedObjectSource} for a {@link ServerTcpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class TcpServerSocketManagedObjectSource extends
		AbstractServerSocketManagedObjectSource<TcpServerFlows> implements
		ServerSocketHandler<TcpServerFlows> {

	/*
	 * ============== AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected ServerSocketHandler<TcpServerFlows> createServerSocketHandler(
			MetaDataContext<None, TcpServerFlows> context) throws Exception {

		// Specify types
		context.setManagedObjectClass(TcpConnectionHandler.class);
		context.setObjectClass(ServerTcpConnection.class);

		// Provide the flow to process a new connection
		context.addFlow(TcpServerFlows.NEW_CONNECTION,
				ServerTcpConnection.class);

		// Ensure connection is cleaned up when process finished
		new CleanupTask().registerAsRecycleTask(context
				.getManagedObjectSourceContext(), "cleanup");

		// Return this as the server socket handler
		return this;
	}

	/*
	 * ==================== ServerSocketHandler ===========================
	 */

	@Override
	public Server<TcpServerFlows> createServer() {
		return new TcpServer();
	}

	@Override
	public ConnectionHandler createConnectionHandler(Connection connection) {
		return new TcpConnectionHandler(connection);
	}

}