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
package net.officefloor.plugin.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Appends data to the {@link BufferStream}.
 *
 * @author Daniel Sagenschneider
 */
public interface OutputBufferStream {

	/**
	 * <p>
	 * Obtains an {@link OutputStream} that writes content to the
	 * {@link BufferStream}.
	 * <p>
	 * This allows use of existing stream implementations.
	 *
	 * @return {@link OutputStream}.
	 */
	OutputStream getOutputStream();

	/**
	 * <p>
	 * Writes the bytes to the {@link BufferStream}.
	 * <p>
	 * The bytes are copied into the {@link BufferStream} so that the input
	 * array is no longer required.
	 *
	 * @param bytes
	 *            Bytes to be written to the {@link BufferStream}.
	 * @throws IOException
	 *             If fails to write the bytes.
	 */
	void write(byte[] bytes) throws IOException;

	/**
	 * <p>
	 * Appends the {@link ByteBuffer} to the {@link BufferStream}.
	 * <p>
	 * The input {@link ByteBuffer} is used directly by the {@link BufferStream}
	 * and must not be changed until the {@link BufferStream} is no longer being
	 * used.
	 *
	 * @param buffer
	 *            {@link ByteBuffer} ready to be read from.
	 */
	void append(ByteBuffer buffer) throws IOException;

	/**
	 * Closes the stream releasing resources.
	 */
	void close();

}