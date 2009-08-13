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
package net.officefloor.plugin.socket.server;

import java.net.InetAddress;
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
	 * Obtains the remote address to which this {@link Connection} is connected.
	 *
	 * @return Remote address to which this {@link Connection} is connected.
	 */
	InetAddress getInetAddress();

	/**
	 * Obtains the remote port to which this {@link Connection} is connected.
	 *
	 * @return Remote port to which this {@link Connection} is connected.
	 */
	int getPort();

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