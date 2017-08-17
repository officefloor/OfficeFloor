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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.stream.StreamBuffer;

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
	 * Obtains the read {@link StreamBuffer}.
	 * 
	 * @return Read {@link StreamBuffer}.
	 */
	StreamBuffer<ByteBuffer> getReadStreamBuffer();

	/**
	 * Specifies the read {@link StreamBuffer}.
	 * 
	 * @param readStreamBuffer
	 *            Read {@link StreamBuffer}.
	 */
	void setReadStreamBuffer(StreamBuffer<ByteBuffer> readStreamBuffer);

	/**
	 * Obtains the {@link ConnectionHandler}.
	 * 
	 * @return {@link ConnectionHandler}.
	 */
	ConnectionHandler getConnectionHandler();

	/**
	 * Undertakes the queued writes and possible {@link Connection} close.
	 * 
	 * @return <code>true</code> if all data written. <code>false</code>
	 *         indicating not able to write all data, requiring waiting for
	 *         client to consume data (clear space in socket buffer).
	 * @throws IOException
	 *             If fails to process the queued writes.
	 */
	boolean processWriteQueue() throws IOException;

	/**
	 * Terminates this {@link Connection} immediately.
	 * 
	 * @throws IOException
	 *             If fails to terminate the {@link Connection}.
	 */
	void terminate() throws IOException;

}