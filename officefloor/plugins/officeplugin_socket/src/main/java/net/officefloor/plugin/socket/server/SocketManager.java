/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;

/**
 * Manages the {@link SocketChannel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SocketManager {

	/**
	 * Opens the {@link Selector} instances for managing the
	 * {@link AcceptedSocket} instances.
	 * 
	 * @throws IOException
	 *             If fails to open all the {@link Selector} instances.
	 */
	void openSocketSelectors() throws IOException;

	/**
	 * Binds a {@link ServerSocketChannel}.
	 * 
	 * @param port
	 *            Port of the {@link ServerSocketChannel}.
	 * @param serverSocketBackLogSize
	 *            Server back log size.
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @throws IOException
	 *             If fails to bind the {@link ServerSocketChannel}.
	 */
	void bindServerSocket(int port, int serverSocketBackLogSize, CommunicationProtocol communicationProtocol,
			ManagedObjectExecuteContext<Indexed> executeContext) throws IOException;

	/**
	 * Manages the {@link AcceptedSocket}.
	 * 
	 * @param socket
	 *            {@link AcceptedSocket} to be managed.
	 */
	void manageSocket(AcceptedSocket socket);

	/**
	 * Closes the {@link Selector} instances for managing the
	 * {@link AcceptedSocket} instances.
	 * 
	 * @throws IOException
	 *             If fails to close {@link Selector} instances.
	 */
	void closeSocketSelectors() throws IOException;

	/**
	 * Waits for the {@link Selector} and {@link ServerSocketChannel} to close.
	 * 
	 * @throws IOException
	 *             If fails to close.
	 */
	void waitForClose() throws IOException;

}