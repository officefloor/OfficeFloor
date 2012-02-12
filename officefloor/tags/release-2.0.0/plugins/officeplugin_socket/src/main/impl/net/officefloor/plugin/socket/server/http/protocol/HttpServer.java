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

package net.officefloor.plugin.socket.server.http.protocol;

import java.io.IOException;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.Server;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.conversation.HttpManagedObject;

/**
 * HTTP {@link Server}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServer implements Server<HttpConnectionHandler> {

	/**
	 * Flow index to handle processing {@link HttpRequest}.
	 */
	private final int requestHandlingFlowIndex;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Initiate.
	 * 
	 * @param requestHandlingFlowIndex
	 *            Flow index to handle processing {@link HttpRequest}.
	 */
	public HttpServer(int requestHandlingFlowIndex) {
		this.requestHandlingFlowIndex = requestHandlingFlowIndex;
	}

	/*
	 * ====================== Server ==================================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<Indexed> executeContext) {
		this.executeContext = executeContext;
	}

	@Override
	public void processRequest(HttpConnectionHandler connectionHandler,
			Object attachment) throws IOException {

		// Obtain the HTTP managed object
		HttpManagedObject managedObject = (HttpManagedObject) attachment;

		// Invoke processing of the HTTP managed object
		this.executeContext.invokeProcess(this.requestHandlingFlowIndex,
				managedObject.getServerHttpConnection(), managedObject, 0,
				managedObject.getEscalationHandler());
	}

}