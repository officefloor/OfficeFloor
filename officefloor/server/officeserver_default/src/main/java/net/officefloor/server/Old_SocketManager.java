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
package net.officefloor.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.server.http.protocol.CommunicationProtocol;
import net.officefloor.server.http.protocol.Connection;

/**
 * Manages the {@link AcceptedSocket} instances across the available
 * {@link Old_SocketListener} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class Old_SocketManager {

	/**
	 * Listing of {@link Old_SocketListener} instances.
	 */
	private final Old_SocketListener[] socketListeners;

	/**
	 * Index of the next {@link Old_SocketListener} for handling the listening of a
	 * {@link ServerSocketChannel}.
	 */
	private int nextServerSocketListener = 0;

	/**
	 * Index of the next {@link Old_SocketListener} for a new {@link Connection}.
	 */
	private final AtomicInteger nextSocketListener = new AtomicInteger(0);

	/**
	 * Indicates if the {@link Old_SocketListener} instances are open.
	 */
	private boolean isSocketListenersOpen = false;

	/**
	 * Initiate.
	 * 
	 * @param socketListeners
	 *            Available {@link Old_SocketListener} instances.
	 */
	public Old_SocketManager(Old_SocketListener... socketListeners) {
		this.socketListeners = socketListeners;
	}

	public synchronized void openSocketSelectors() throws IOException {

		// Determine if already open
		if (this.isSocketListenersOpen) {
			return;
		}

		// Open the socket listeners
		this.isSocketListenersOpen = true;
		for (Old_SocketListener socketListener : this.socketListeners) {
			socketListener.openSelector();
		}
	}

	public synchronized void bindServerSocket(int port, int serverSocketBackLogSize,
			CommunicationProtocol communicationProtocol, ManagedObjectExecuteContext<Indexed> executeContext)
			throws IOException {

		// Obtain the next socket listener
		int nextServerSocketListener = (this.nextServerSocketListener++) % this.socketListeners.length;
		Old_SocketListener socketListener = this.socketListeners[nextServerSocketListener];

		// Bind the server socket
		socketListener.bindServerSocket(new InetSocketAddress(port), serverSocketBackLogSize, communicationProtocol,
				executeContext);
	}

	public void manageSocket(AcceptedSocket connection) {

		// Spread the connections across the socket listeners
		int next = this.nextSocketListener.getAndAccumulate(1,
				(prev, increment) -> (prev + increment) % this.socketListeners.length);

		// Register connection with socket listener
		this.socketListeners[next].registerAcceptedConnection(connection);
	}

	public synchronized void closeSocketSelectors() throws IOException {

		// No longer open
		this.isSocketListenersOpen = false;

		// Stop the socket listeners
		for (Old_SocketListener socketListener : this.socketListeners) {
			socketListener.closeSelector();
		}
	}

	public synchronized void waitForClose() throws IOException {
		for (Old_SocketListener socketListener : this.socketListeners) {
			socketListener.waitForShutdown();
		}
	}

}