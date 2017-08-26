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
package net.officefloor.server.ssl.protocol;

import java.net.InetSocketAddress;

import net.officefloor.server.http.protocol.Connection;

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

}