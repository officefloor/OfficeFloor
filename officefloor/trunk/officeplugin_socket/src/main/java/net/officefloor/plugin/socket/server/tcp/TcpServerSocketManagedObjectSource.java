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
package net.officefloor.plugin.socket.server.tcp;

import net.officefloor.frame.api.build.HandlerBuilder;
import net.officefloor.frame.api.build.HandlerFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.plugin.impl.socket.server.ServerSocketHandlerEnum;
import net.officefloor.plugin.impl.socket.server.ServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;
import net.officefloor.plugin.socket.server.tcp.api.ServerTcpConnection;

/**
 * {@link ManagedObjectSource} for a {@link ServerTcpConnection}.
 * 
 * @author Daniel
 */
public class TcpServerSocketManagedObjectSource extends
		ServerSocketManagedObjectSource implements HandlerFactory<Indexed>,
		ServerSocketHandler<Indexed> {

	/*
	 * ================== ServerSocketManagedObjectSource ===============
	 */

	@Override
	protected void registerServerSocketHandler(
			MetaDataContext<None, ServerSocketHandlerEnum> context)
			throws Exception {
		ManagedObjectSourceContext<ServerSocketHandlerEnum> mosContext = context
				.getManagedObjectSourceContext();

		// Specify types
		context.setManagedObjectClass(TcpConnectionHandler.class);
		context.setObjectClass(ServerTcpConnection.class);

		// Provide the handler
		HandlerBuilder<Indexed> handlerBuilder = mosContext.getHandlerBuilder()
				.registerHandler(ServerSocketHandlerEnum.SERVER_SOCKET_HANDLER);
		handlerBuilder.setHandlerFactory(this);
		handlerBuilder.linkProcess(0, null, null); // handles the message

		// Ensure connection is cleaned up when process finished
		new CleanupTask().registerAsRecycleTask(mosContext,
				"tcp.connection.cleanup");
	}

	/*
	 * ======================= Handler ===================================
	 */

	@Override
	public Handler<Indexed> createHandler() {
		return this;
	}

	/**
	 * {@link HandlerContext}.
	 */
	private HandlerContext<Indexed> handlerContext;

	@Override
	public void setHandlerContext(HandlerContext<Indexed> context)
			throws Exception {
		this.handlerContext = context;
	}

	/*
	 * ==================== ServerSocketHandler ===========================
	 */

	@Override
	public Server createServer() {
		return new TcpServer();
	}

	@Override
	public ConnectionHandler createConnectionHandler(Connection connection) {
		return new TcpConnectionHandler(this.handlerContext, connection);
	}

}