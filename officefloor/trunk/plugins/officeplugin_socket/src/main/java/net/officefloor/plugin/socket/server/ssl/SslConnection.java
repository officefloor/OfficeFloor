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

package net.officefloor.plugin.socket.server.ssl;

import java.io.IOException;

import net.officefloor.plugin.socket.server.Connection;

/**
 * Secure Socket Layer (SSL) {@link Connection}.
 *
 * @author Daniel Sagenschneider
 */
public interface SslConnection extends Connection {

	/**
	 * <p>
	 * Should data be available from the peer this triggers processing it to
	 * make it available for input.
	 * <p>
	 * This is necessary as the {@link SslConnection} needs to be made aware
	 * when further data is available from the peer, so that it can either:
	 * <ol>
	 * <li>further a handshake to completion, or</li>
	 * <li>transform the cipher text into plain text available for application
	 * input</li>
	 * </ol>
	 *
	 * @throws IOException
	 *             If fails to process the data from peer.
	 */
	void processDataFromPeer() throws IOException;

	/**
	 * Ensures the {@link SslConnection} is valid.
	 *
	 * @throws IOException
	 *             {@link IOException} of the cause of this
	 *             {@link SslConnection} being invalid.
	 */
	void validate() throws IOException;

}