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
package net.officefloor.plugin.socket.server.http.source;

import java.io.IOException;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * HTTP {@link Server}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpServer implements
		Server<HttpServer.HttpServerFlows, HttpConnectionHandler> {

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
	public void processRequest(HttpConnectionHandler connectionHandler)
			throws IOException {

		// TODO take advantage of request
		// TODO list requests and send response only for first request

		// Create the HTTP managed object
		HttpManagedObject managedObject = new HttpManagedObject(
				connectionHandler);

		// Reset the connection handler for next request
		connectionHandler.resetForNextRequest();

		// Invoke with the managed object
		this.executeContext.invokeProcess(HttpServerFlows.HANDLE_HTTP_REQUEST,
				managedObject, managedObject, managedObject);
	}

}