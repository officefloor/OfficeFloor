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

import java.io.IOException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.HandlerContext;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.ReadMessage;
import net.officefloor.plugin.socket.server.spi.Server;

/**
 * HTTP {@link Server}.
 * 
 * @author Daniel
 */
public class HttpServer implements Server {

	/**
	 * {@link HandlerContext}.
	 */
	private final HandlerContext<Indexed> handlerContext;

	/**
	 * Initiate.
	 * 
	 * @param handlerContext
	 *            {@link HandlerContext}.
	 */
	public HttpServer(HandlerContext<Indexed> handlerContext) {
		this.handlerContext = handlerContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Server#processReadMessage(net.officefloor.plugin.socket.server.spi.ReadMessage,
	 *      net.officefloor.plugin.socket.server.spi.ConnectionHandler)
	 */
	@Override
	public void processReadMessage(ReadMessage message,
			ConnectionHandler connectionHandler) throws IOException {

		// Downcast to HTTP connection handler
		HttpConnectionHandler httpConnHandler = (HttpConnectionHandler) connectionHandler;

		// Create the HTTP managed object
		HttpManagedObject managedObject = new HttpManagedObject(httpConnHandler);
		
		// Reset the connection handler for next request
		httpConnHandler.resetForNextRequest();

		// Invoke with the managed object
		this.handlerContext.invokeProcess(0, managedObject, managedObject);
	}

}
