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

/**
 * Stream of buffers.
 *
 * @author Daniel Sagenschneider
 */
public interface BufferStream {

	/*
	 * ================= Write Functionality ===========================
	 */

	/**
	 * Obtains the {@link OutputBufferStream} for this {@link BufferStream}.
	 *
	 * @return {@link OutputBufferStream}.
	 */
	OutputBufferStream getOutputBufferStream();

	/**
	 * Writes the byte to the {@link BufferStream}.
	 *
	 * @param b
	 *            Byte.
	 * @throws IOException
	 *             If fails to write byte.
	 */
	void write(byte b) throws IOException;

	/*
	 * ================= Read Functionality ===========================
	 */

	/**
	 * Obtains the {@link InputBufferStream} for this {@link BufferStream}.
	 *
	 * @return {@link InputBufferStream}.
	 */
	InputBufferStream getInputBufferStream();

	/**
	 * Reads the content from the {@link BufferStream} into the input buffer
	 * returning the number of bytes loaded.
	 *
	 * @param readBuffer
	 *            Buffer to be loaded with the {@link BufferStream} content.
	 * @return Number of bytes loaded into the input buffer from the
	 *         {@link BufferStream}.
	 */
	int read(byte[] readBuffer);

}