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

package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import net.officefloor.plugin.socket.server.CloseConnectionAction;
import net.officefloor.plugin.socket.server.ConnectionActionEnum;
import net.officefloor.plugin.socket.server.ManagedConnection;
import net.officefloor.plugin.socket.server.protocol.CommunicationProtocol;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.ConnectionHandler;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;

/**
 * Implementation of a {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionImpl implements Connection, ManagedConnection,
		CloseConnectionAction {

	/**
	 * {@link SelectionKey} for this {@link Connection}.
	 */
	private SelectionKey selectionKey;

	/**
	 * {@link SocketChannel} for this {@link Connection}.
	 */
	private SocketChannel socketChannel;

	/**
	 * {@link ConnectionHandler} for this {@link Connection}.
	 */
	private final ConnectionHandler connectionHandler;

	/**
	 * {@link SocketListener} handling this {@link Connection}.
	 */
	private SocketListener socketListener;

	/**
	 * Flags if this {@link Connection} has been closed.
	 */
	private volatile boolean isClosed = false;

	/**
	 * Initiate.
	 * 
	 * @param selectionKey
	 *            {@link SelectionKey} for this {@link Connection}.
	 * @param socketChannel
	 *            {@link SocketChannel} for this {@link Connection}.
	 * @param communicationProtocol
	 *            {@link CommunicationProtocol}.
	 * @param socketListener
	 *            {@link SocketListener}.
	 */
	public ConnectionImpl(SelectionKey selectionKey,
			SocketChannel socketChannel,
			CommunicationProtocol communicationProtocol,
			SocketListener socketListener) {
		this.selectionKey = selectionKey;
		this.socketChannel = socketChannel;
		this.socketListener = socketListener;

		// Create the connection handler for this connection
		this.connectionHandler = communicationProtocol
				.createConnectionHandler(this);
	}

	/*
	 * ================= Connection =======================================
	 */

	@Override
	public Object getLock() {
		return this;
	}

	@Override
	public boolean isSecure() {
		// Connection is not secure
		return false;
	}

	@Override
	public WriteBuffer createWriteBuffer(byte[] data, int length) {
		return new ArrayWriteBuffer(data, length);
	}

	@Override
	public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
		// TODO implement Connection.createWriteBuffer
		throw new UnsupportedOperationException(
				"TODO implement Connection.createWriteBuffer");
	}

	@Override
	public void writeData(WriteBuffer[] data) {
		this.socketListener.doConnectionAction(new WriteDataActionImpl(this,
				data));
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		Socket socket = this.socketChannel.socket();
		return new InetSocketAddress(socket.getLocalAddress(),
				socket.getLocalPort());
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		Socket socket = this.socketChannel.socket();
		return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
	}

	@Override
	public void close() {
		// Close and trigger for closing (after all data written)
		this.isClosed = true;
		this.socketListener.doConnectionAction(this);
	}

	@Override
	public boolean isClosed() {
		return this.isClosed;
	}

	/*
	 * ==================== ManagedConnection ============================
	 */

	@Override
	public SelectionKey getSelectionKey() {
		return this.selectionKey;
	}

	@Override
	public SocketChannel getSocketChannel() {
		return this.socketChannel;
	}

	@Override
	public ConnectionHandler getConnectionHandler() {
		return this.connectionHandler;
	}

	@Override
	public void terminate() throws IOException {

		// Ensure flagged as closed
		this.isClosed = true;

		// Terminate connection
		this.socketChannel.close();
		this.selectionKey.cancel();
	}

	/*
	 * ================= CloseConnectionAction ===========================
	 */

	@Override
	public ConnectionActionEnum getType() {
		return ConnectionActionEnum.CLOSE_CONNECTION;
	}

	@Override
	public ManagedConnection getConnection() {
		return this;
	}

}