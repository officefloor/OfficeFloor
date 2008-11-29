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
package net.officefloor.plugin.socket.server.http;

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
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;

/**
 * {@link ManagedObjectSource} for a {@link ServerHttpConnection}.
 * 
 * @author Daniel
 */
public class HttpServerSocketManagedObjectSource extends
		ServerSocketManagedObjectSource implements HandlerFactory<Indexed>,
		ServerSocketHandler<Indexed> {

	/**
	 * Initial size in bytes of the buffer to contain the body's request.
	 */
	private int initialRequestBodyBufferLength = 1024;

	/**
	 * Maximum length of the request's body.
	 */
	private int maxRequestBodyLength = (1024 * 1024);

	/*
	 * ================= ServerSocketManagedObjectSource ==================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.impl.socket.server.ServerSocketManagedObjectSource
	 * #
	 * registerServerSocketHandler(net.officefloor.frame.spi.managedobject.source
	 * .impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void registerServerSocketHandler(
			MetaDataContext<None, ServerSocketHandlerEnum> context)
			throws Exception {
		ManagedObjectSourceContext mosContext = context
				.getManagedObjectSourceContext();

		// Specify type of object
		context.setObjectClass(ServerHttpConnection.class);

		// Provide the handler
		HandlerBuilder<Indexed> handlerBuilder = mosContext.getHandlerBuilder(
				ServerSocketHandlerEnum.class).registerHandler(
				ServerSocketHandlerEnum.SERVER_SOCKET_HANDLER);
		handlerBuilder.setHandlerFactory(this);
		handlerBuilder.linkProcess(0, null, null); // handles the message
	}

	/*
	 * ================== HandlerFactory =====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.HandlerFactory#createHandler()
	 */
	@Override
	public Handler<Indexed> createHandler() {
		return this;
	}

	/*
	 * =================== ServerSocketHandler ===========================
	 */

	/**
	 * {@link HandlerContext}.
	 */
	private HandlerContext<Indexed> handlerContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.api.execute.Handler#setHandlerContext(net.officefloor
	 * .frame.api.execute.HandlerContext)
	 */
	@Override
	public void setHandlerContext(HandlerContext<Indexed> context)
			throws Exception {
		this.handlerContext = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.socket.server.spi.ServerSocketHandler#createServer
	 * ()
	 */
	@Override
	public Server createServer() {
		return new HttpServer(this.handlerContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.socket.server.spi.ServerSocketHandler#
	 * createConnectionHandler
	 * (net.officefloor.plugin.socket.server.spi.Connection)
	 */
	@Override
	public ConnectionHandler createConnectionHandler(Connection connection) {
		return new HttpConnectionHandler(connection,
				this.initialRequestBodyBufferLength, this.maxRequestBodyLength);
	}

}
