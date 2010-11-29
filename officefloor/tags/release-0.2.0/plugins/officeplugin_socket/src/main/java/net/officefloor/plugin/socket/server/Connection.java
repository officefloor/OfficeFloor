/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link SocketChannel} connection.
 * 
 * @author Daniel Sagenschneider
 */
public interface Connection {

	/**
	 * Obtains the lock to <code>synchronize</code> for using this
	 * {@link Connection}.
	 * 
	 * @return Lock for this {@link Connection}.
	 */
	Object getLock();

	/**
	 * Indicates if the connection is over a secure channel (e.g. utilising
	 * SSL).
	 * 
	 * @return <code>true</code> if connection is over a secure channel.
	 */
	boolean isSecure();

	/**
	 * Obtains the local address for the {@link Connection}.
	 * 
	 * @return {@link InetSocketAddress} describing the local {@link Socket} for
	 *         the {@link Connection}.
	 */
	InetSocketAddress getLocalAddress();

	/**
	 * Obtains the remote address for the {@link Connection}.
	 * 
	 * @return {@link InetSocketAddress} describing the remote {@link Socket}
	 *         for the {@link Connection}.
	 */
	InetSocketAddress getRemoteAddress();

	/**
	 * Obtains the {@link InputBufferStream} to obtain data from the client.
	 * 
	 * @return {@link InputBufferStream}.
	 */
	InputBufferStream getInputBufferStream();

	/**
	 * Obtains the {@link OutputBufferStream} to write data to the client.
	 * 
	 * @return {@link OutputBufferStream}.
	 */
	OutputBufferStream getOutputBufferStream();

}