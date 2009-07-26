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

import java.io.IOException;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.Connection;
import net.officefloor.plugin.socket.server.Server;

/**
 * TCP {@link Server}.
 *
 * @author Daniel Sagenschneider
 */
public class TcpServer implements
		Server<TcpServer.TcpServerFlows, TcpConnectionHandler> {

	/**
	 * {@link Flow} instance to handle a new TCP {@link Connection}.
	 */
	public static enum TcpServerFlows {
		NEW_CONNECTION
	}

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private ManagedObjectExecuteContext<TcpServerFlows> executeContext;

	/*
	 * ==================== Server ====================================
	 */

	@Override
	public void setManagedObjectExecuteContext(
			ManagedObjectExecuteContext<TcpServerFlows> executeContext) {
		this.executeContext = executeContext;
	}

	@Override
	public void processRequest(TcpConnectionHandler connectionHandler)
			throws IOException {

		// Invoke the process to handle message
		connectionHandler.invokeProcess(this.executeContext);
	}

}