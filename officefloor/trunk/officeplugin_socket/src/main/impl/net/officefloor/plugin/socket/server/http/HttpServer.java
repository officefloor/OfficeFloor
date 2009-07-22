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
package net.officefloor.plugin.socket.server.http;

import java.io.IOException;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.Request;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;

/**
 * HTTP {@link Server}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpServer implements Server<HttpServer.HttpServerFlows> {

	/**
	 * {@link Flow} to handle the {@link HttpRequest}.
	 */
	public static enum HttpServerFlows {
		HANDLE_HTTP_REQUEST
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<HttpServerFlows> executeContext;

	/*
	 * ====================== Server ==================================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<HttpServerFlows> executeContext) {
		this.executeContext = executeContext;
	}

	@Override
	public void processRequest(Request request,
			ConnectionHandler connectionHandler) throws IOException {

		// Downcast to HTTP connection handler
		HttpConnectionHandler httpConnHandler = (HttpConnectionHandler) connectionHandler;

		// Create the HTTP managed object
		HttpManagedObject managedObject = new HttpManagedObject(httpConnHandler);

		// Reset the connection handler for next request
		httpConnHandler.resetForNextRequest();

		// Invoke with the managed object
		this.executeContext.invokeProcess(HttpServerFlows.HANDLE_HTTP_REQUEST,
				managedObject, managedObject, managedObject);
	}

}