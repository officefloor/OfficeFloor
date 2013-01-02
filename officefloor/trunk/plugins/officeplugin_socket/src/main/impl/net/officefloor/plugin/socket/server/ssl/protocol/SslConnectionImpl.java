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
package net.officefloor.plugin.socket.server.ssl.protocol;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;

/**
 * {@link Connection} providing SSL wrapping of another {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslConnectionImpl implements Connection {

	/**
	 * {@link SslConnectionHandler}.
	 */
	private final SslConnectionHandler connectionHandler;

	/**
	 * Wrapped {@link Connection}.
	 */
	private final Connection wrappedConnection;

	/**
	 * Initiate.
	 * 
	 * @param connectionHandler
	 *            {@link SslConnectionHandler}.
	 * @param wrappedConnection
	 *            Wrapped {@link Connection}.
	 */
	public SslConnectionImpl(SslConnectionHandler connectionHandler,
			Connection wrappedConnection) {
		this.connectionHandler = connectionHandler;
		this.wrappedConnection = wrappedConnection;
	}

	/*
	 * =================== Connection =========================
	 */

	@Override
	public Object getLock() {
		return this.wrappedConnection.getLock();
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return this.wrappedConnection.getLocalAddress();
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return this.wrappedConnection.getRemoteAddress();
	}

	@Override
	public boolean isSecure() {
		// SSL connection is secure
		return true;
	}

	@Override
	public WriteBuffer createWriteBuffer(byte[] data, int length) {
		return this.wrappedConnection.createWriteBuffer(data, length);
	}

	@Override
	public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
		return this.wrappedConnection.createWriteBuffer(buffer);
	}

	@Override
	public void writeData(WriteBuffer[] data) {
		this.connectionHandler.writeData(data);
	}

	@Override
	public void close() {
		this.connectionHandler.triggerClose();
	}

	@Override
	public boolean isClosed() {
		return (this.connectionHandler.isClosing())
				|| (this.wrappedConnection.isClosed());
	}

}