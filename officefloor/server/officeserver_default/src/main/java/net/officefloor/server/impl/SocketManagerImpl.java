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
package net.officefloor.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.server.AcceptedSocket;
import net.officefloor.server.SocketManager;
import net.officefloor.server.http.protocol.CommunicationProtocol;
import net.officefloor.server.http.protocol.Connection;

/**
 * Manages the {@link AcceptedSocket} instances for the
 * {@link ServerSocketAccepter} instances across the available
 * {@link SocketListener} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SocketManagerImpl implements SocketManager {

	/**
	 * Listing of {@link SocketListener} instances.
	 */
	private final SocketListener[] socketListeners;

	/**
	 * Index of the next {@link SocketListener} for handling the listening of a
	 * {@link ServerSocketChannel}.
	 */
	private int nextServerSocketListener = 0;

	/**
	 * Index of the next {@link SocketListener} for a new {@link Connection}.
	 */
	private final AtomicInteger nextSocketListener = new AtomicInteger(0);

	/**
	 * Indicates if the {@link SocketListener} instances are open.
	 */
	private boolean isSocketListenersOpen = false;

	/**
	 * Initiate.
	 * 
	 * @param socketListeners
	 *            Available {@link SocketListener} instances.
	 */
	public SocketManagerImpl(SocketListener... socketListeners) {
		this.socketListeners = socketListeners;
	}

	/*
	 * ====================== SocketManager ===========================
	 */

	@Override
	public synchronized void openSocketSelectors() throws IOException {

		// Determine if already open
		if (this.isSocketListenersOpen) {
			return;
		}

		// Open the socket listeners
		this.isSocketListenersOpen = true;
		for (SocketListener socketListener : this.socketListeners) {
			socketListener.openSelector();
		}
	}

	@Override
	public synchronized void bindServerSocket(int port, int serverSocketBackLogSize,
			CommunicationProtocol communicationProtocol, ManagedObjectExecuteContext<Indexed> executeContext)
			throws IOException {

		// Obtain the next socket listener
		int nextServerSocketListener = (this.nextServerSocketListener++) % this.socketListeners.length;
		SocketListener socketListener = this.socketListeners[nextServerSocketListener];

		// Bind the server socket
		socketListener.bindServerSocket(new InetSocketAddress(port), serverSocketBackLogSize, communicationProtocol,
				executeContext);
	}

	@Override
	public void manageSocket(AcceptedSocket connection) {

		// Spread the connections across the socket listeners
		int next = this.nextSocketListener.getAndAccumulate(1,
				(prev, increment) -> (prev + increment) % this.socketListeners.length);

		// Register connection with socket listener
		this.socketListeners[next].registerAcceptedConnection(connection);
	}

	@Override
	public synchronized void closeSocketSelectors() throws IOException {

		// No longer open
		this.isSocketListenersOpen = false;

		// Stop the socket listeners
		for (SocketListener socketListener : this.socketListeners) {
			socketListener.closeSelector();
		}
	}

	@Override
	public synchronized void waitForClose() throws IOException {
		for (SocketListener socketListener : this.socketListeners) {
			socketListener.waitForShutdown();
		}
	}

}