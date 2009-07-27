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
package net.officefloor.plugin.socket.server.tcp.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;
import net.officefloor.plugin.socket.server.tcp.source.TcpServer.TcpServerFlows;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * {@link ManagedObjectSource} for a {@link ServerTcpConnection}.
 *
 * @author Daniel Sagenschneider
 */
public class TcpServerSocketManagedObjectSource
		extends
		AbstractServerSocketManagedObjectSource<TcpServerFlows, TcpConnectionHandler>
		implements ServerSocketHandler<TcpServerFlows, TcpConnectionHandler> {

	/**
	 * Property to obtain the maximum idle time before the {@link Connection} is
	 * closed.
	 */
	public static final String PROPERTY_MAXIMUM_IDLE_TIME = "max.idle.time";

	/**
	 * Maximum idle time before the {@link Connection} is closed.
	 */
	private long maxIdleTime;

	/*
	 * ============== AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		super.loadSpecification(context);
		context.addProperty(PROPERTY_MAXIMUM_IDLE_TIME);
	}

	@Override
	protected ServerSocketHandler<TcpServerFlows, TcpConnectionHandler> createServerSocketHandler(
			MetaDataContext<None, TcpServerFlows> context,
			BufferSquirtFactory bufferSquirtFactory) throws Exception {
		ManagedObjectSourceContext<TcpServerFlows> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the maximum idle time
		this.maxIdleTime = Long.parseLong(mosContext
				.getProperty(PROPERTY_MAXIMUM_IDLE_TIME));

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
	public Server<TcpServerFlows, TcpConnectionHandler> createServer() {
		return new TcpServer();
	}

	@Override
	public TcpConnectionHandler createConnectionHandler(Connection connection) {
		return new TcpConnectionHandler(connection, this.maxIdleTime);
	}

}