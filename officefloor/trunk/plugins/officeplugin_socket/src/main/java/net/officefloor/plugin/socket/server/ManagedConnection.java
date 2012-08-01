/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;

/**
 * Managed {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedConnection {

	/**
	 * Obtains the {@link SelectionKey} for the {@link Connection}.
	 * 
	 * @return {@link SelectionKey} for the {@link Connection}.
	 */
	SelectionKey getSelectionKey();

	/**
	 * Obtains the {@link SocketChannel} for the {@link Connection}.
	 * 
	 * @return {@link SocketChannel} for the {@link Connection}.
	 */
	SocketChannel getSocketChannel();

	/**
	 * Obtains the {@link ConnectionHandler}.
	 * 
	 * @return {@link ConnectionHandler}.
	 */
	ConnectionHandler getConnectionHandler();

	/**
	 * Terminates this {@link Connection}.
	 * 
	 * @throws IOException
	 *             If fails to terminate the {@link Connection}.
	 */
	void terminate() throws IOException;

}