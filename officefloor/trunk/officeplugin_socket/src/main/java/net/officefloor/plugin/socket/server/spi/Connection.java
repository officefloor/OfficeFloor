/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * <p>
 * Connection with the {@link Server}.
 * <p>
 * Provided by the Server Socket plug-in.
 * 
 * @author Daniel Sagenschneider
 */
public interface Connection {

	/**
	 * Obtains the lock that must be <code>synchronized</code> on before
	 * making any changes to the {@link Connection} or any of its
	 * {@link Message} instances.
	 * 
	 * @return Lock for this {@link Connection}.
	 */
	Object getLock();

	/**
	 * Obtains the first {@link ReadMessage} instance for this
	 * {@link Connection}.
	 * 
	 * @return Listing of first {@link ReadMessage}.
	 * @throws IOException
	 *             If issue with {@link Connection} (closed).
	 */
	ReadMessage getFirstReadMessage() throws IOException;

	/**
	 * Reads the data from this {@link Connection}.
	 * 
	 * @param buffer
	 *            Buffer to read data into.
	 * @param offset
	 *            Offset into buffer to start reading data.
	 * @param length
	 *            Number of bytes to read.
	 * @return Number of bytes read.
	 * @throws IOException
	 *             If the {@link Connection} is closed.
	 */
	int read(byte[] buffer, int offset, int length) throws IOException;

	/**
	 * Obtains the active {@link WriteMessage} instance for this
	 * {@link Connection}.
	 * 
	 * @return Active {@link WriteMessage} instance, or <code>null</code> if
	 *         no active {@link WriteMessage}.
	 * @throws IOException
	 *             If issue with {@link Connection} (closed).
	 */
	WriteMessage getActiveWriteMessage() throws IOException;

	/**
	 * Writes the data to this {@link Connection}.
	 * 
	 * @param data
	 *            Data to be written to this {@link Connection}.
	 * @param offset
	 *            Offset into data to start writing.
	 * @param length
	 *            Number of bytes to write.
	 * @throws IOException
	 *             If the {@link Connection} is closed.
	 */
	void write(byte[] data, int offset, int length) throws IOException;

	/**
	 * Writes the data within the {@link ByteBuffer} to this {@link Connection}.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer}.
	 * @throws IOException
	 *             If the {@link Connection} is closed.
	 */
	void write(ByteBuffer data) throws IOException;

	/**
	 * <p>
	 * Flags to flush the writes to the client.
	 * <p>
	 * Unlike {@link OutputStream#flush()} this method does not block, it only
	 * flags for the data written so far to be flushed to the client when
	 * possible.
	 */
	void flush();

	/**
	 * <p>
	 * Creates a new {@link WriteMessage} to send to the client.
	 * <p>
	 * This will call {@link WriteMessage#write()} on the active
	 * {@link WriteMessage} and subsequently replaces the newly created
	 * {@link WriteMessage} as the active {@link WriteMessage}.
	 * 
	 * @param listener
	 *            {@link WriteMessageListener} for the new {@link WriteMessage}.
	 *            May be <code>null</code> if no listening required.
	 * @return New {@link WriteMessage} to send to the client.
	 * @throws IOException
	 *             If issue with {@link Connection} (closed).
	 */
	WriteMessage createWriteMessage(WriteMessageListener listener)
			throws IOException;

}
