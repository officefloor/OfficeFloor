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
	 * Writes the bytes to the {@link BufferStream}.
	 * <p>
	 * The bytes are copied into the {@link BufferStream} so that the input
	 * array is no longer required.
	 *
	 * @param data
	 *            Bytes to be written to the {@link BufferStream}.
	 * @param offset
	 *            Offset into the data to start obtaining bytes to write.
	 * @param length
	 *            Number of bytes to write from the data.
	 * @throws IOException
	 *             If fails to write the bytes.
	 */
	void write(byte[] data, int offset, int length) throws IOException;

	/**
	 * Writes content to a {@link ByteBuffer} of the {@link BufferStream}.
	 *
	 * @param populator
	 *            {@link BufferPopulator} to write data to the
	 *            {@link ByteBuffer}.
	 * @throws IOException
	 *             If fails to write data to {@link ByteBuffer}.
	 */
	void write(BufferPopulator populator) throws IOException;

	/**
	 * <p>
	 * Appends the {@link ByteBuffer} to the {@link BufferStream}.
	 * <p>
	 * The {@link ByteBuffer} is used directly by the {@link BufferStream} and
	 * must not be changed until the {@link BufferStream} is no longer being
	 * used. This should only be used when the {@link ByteBuffer} never changes
	 * (contains static information).
	 * <p>
	 * Use {@link #append(BufferSquirt)} over this method to know when the
	 * {@link ByteBuffer} can be modified again.
	 *
	 * @param buffer
	 *            {@link ByteBuffer} ready to be read from.
	 * @throws IOException
	 *             If fails to append to the {@link BufferStream}.
	 */
	void append(ByteBuffer buffer) throws IOException;

	/**
	 * <p>
	 * Appends the {@link BufferSquirt} to the {@link BufferStream}.
	 * <p>
	 * {@link BufferSquirt#close()} is invoked when the {@link BufferStream} no
	 * longer requires the {@link BufferSquirt}.
	 *
	 * @param squirt
	 *            {@link BufferSquirt} to append to the {@link BufferStream}.
	 * @throws IOException
	 *             If fails to append to the {@link BufferStream}.
	 */
	void append(BufferSquirt squirt) throws IOException;

	/**
	 * Closes the stream releasing resources.
	 *
	 * @throws IOException
	 *             If fails to close the {@link BufferStream}.
	 */
	void close() throws IOException;

}