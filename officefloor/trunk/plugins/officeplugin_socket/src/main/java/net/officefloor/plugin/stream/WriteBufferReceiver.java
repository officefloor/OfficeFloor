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
package net.officefloor.plugin.stream;

import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;

/**
 * Receives the output bytes.
 * 
 * @author Daniel Sagenschneider
 */
public interface WriteBufferReceiver {

	/**
	 * Obtains the lock to <code>synchronize</code> for using this
	 * {@link WriteBufferReceiver}.
	 * 
	 * @return Lock for this {@link WriteBufferReceiver}.
	 */
	Object getLock();

	/**
	 * Creates a {@link WriteBuffer} for the data.
	 * 
	 * @param data
	 *            Data.
	 * @param length
	 *            Length of data.
	 * @return {@link WriteBuffer}.
	 */
	WriteBuffer createWriteBuffer(byte[] data, int length);

	/**
	 * Creates the {@link WriteBuffer} for the {@link ByteBuffer}.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer}.
	 * @return {@link WriteBuffer}.
	 */
	WriteBuffer createWriteBuffer(ByteBuffer buffer);

	/**
	 * Writes data to client of this {@link Connection}.
	 * 
	 * @param data
	 *            Data to be written.
	 */
	void writeData(WriteBuffer[] data);

	/**
	 * <p>
	 * Flags to close the {@link WriteBufferReceiver}.
	 * <p>
	 * Close occurs after all data has been written.
	 */
	void close();

	/**
	 * Indicates if the {@link WriteBufferReceiver} is closed.
	 * 
	 * @return <code>true</code> if the {@link WriteBufferReceiver} is closed.
	 */
	boolean isClosed();

}