/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.tcp.protocol;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext;
import net.officefloor.plugin.socket.server.CommunicationProtocol;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * TCP {@link CommunicationProtocol}.
 *
 * @author Daniel Sagenschneider
 */
public class TcpCommunicationProtocol implements
		CommunicationProtocol<TcpConnectionHandler>,
		ServerSocketHandler<TcpConnectionHandler> {

	/**
	 * Property to obtain the maximum idle time before the {@link Connection} is
	 * closed.
	 */
	public static final String PROPERTY_MAXIMUM_IDLE_TIME = "max.idle.time";

	/**
	 * Maximum idle time before the {@link Connection} is closed.
	 */
	private long maxIdleTime;

	/**
	 * {@link Flow} index to handle a new connection.
	 */
	private int newConnectionFlowIndex;

	/*
	 * =================== CommunicationProtocol ==============================
	 */

	@Override
	public void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_MAXIMUM_IDLE_TIME);
	}

	@Override
	public ServerSocketHandler<TcpConnectionHandler> createServerSocketHandler(
			MetaDataContext<None, Indexed> context,
			BufferSquirtFactory bufferSquirtFactory) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context
				.getManagedObjectSourceContext();

		// Obtain the maximum idle time
		this.maxIdleTime = Long.parseLong(mosContext
				.getProperty(PROPERTY_MAXIMUM_IDLE_TIME));

		// Specify types
		context.setManagedObjectClass(TcpConnectionHandler.class);
		context.setObjectClass(ServerTcpConnection.class);

		// Provide the flow to process a new connection
		this.newConnectionFlowIndex = context
				.addFlow(ServerTcpConnection.class).setLabel("NEW_CONNECTION")
				.getIndex();

		// Ensure connection is cleaned up when process finished
		new CleanupTask().registerAsRecycleTask(mosContext, "cleanup");

		// Return this as the server socket handler
		return this;
	}

	/*
	 * ======================= ServerSocketHandler ===========================
	 */

	@Override
	public Server<TcpConnectionHandler> createServer() {
		return new TcpServer(this.newConnectionFlowIndex);
	}

	@Override
	public TcpConnectionHandler createConnectionHandler(Connection connection) {
		return new TcpConnectionHandler(connection, this.maxIdleTime);
	}

}