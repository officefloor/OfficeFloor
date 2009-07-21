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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Stream of buffers.
 *
 * @author Daniel Sagenschneider
 */
public interface BufferStream {

	/**
	 * Value returned from read operations to indicate end of stream.
	 */
	static int END_OF_STREAM = -1;

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
	 * Writes the bytes to the {@link BufferStream}.
	 *
	 * @param bytes
	 *            Bytes to be written to the {@link BufferStream}.
	 * @throws IOException
	 *             If fails to write the bytes.
	 */
	void write(byte[] bytes) throws IOException;

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
	 * Appends the {@link ByteBuffer} to the {@link BufferStream}.
	 *
	 * @param buffer
	 *            {@link ByteBuffer} to append.
	 * @throws IOException
	 *             If fails to append {@link ByteBuffer}.
	 */
	void append(ByteBuffer buffer) throws IOException;

	/**
	 * <p>
	 * Closes the output to the {@link BufferStream} and releases the resources.
	 * <p>
	 * Further output will result in an {@link IOException}.
	 */
	void closeOutput();

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
	 * <p>
	 * Obtains an {@link InputStream} that allows browsing the contents of the
	 * {@link BufferStream} without changing the {@link BufferStream} markers.
	 * <p>
	 * Once the available data has been browsed, further reads will return
	 * {@link BufferStream#END_OF_STREAM} indicating end of stream of available
	 * data.
	 *
	 * @return {@link InputStream} to browse the {@link BufferStream}.
	 */
	InputStream getBrowseStream();

	/**
	 * Reads the content from the {@link BufferStream} into the input buffer
	 * returning the number of bytes loaded.
	 *
	 * @param readBuffer
	 *            Buffer to be loaded with the {@link BufferStream} content.
	 * @return Number of bytes loaded into the input buffer from the
	 *         {@link BufferStream}. Return of {@link #END_OF_STREAM} indicates
	 *         end of stream with no bytes loaded to buffer.
	 * @throws IOException
	 *             If failure to read input. Typically this will be because the
	 *             input is closed.
	 */
	int read(byte[] readBuffer) throws IOException;

	/**
	 * Reads and processes the contents of a {@link ByteBuffer} from the
	 * {@link BufferStream}.
	 *
	 * @param processor
	 *            {@link BufferProcessor} to process the data of the
	 *            {@link ByteBuffer}.
	 * @return Number of bytes in the {@link ByteBuffer} provided to the
	 *         {@link BufferProcessor} to process. Return of
	 *         {@link #END_OF_STREAM} indicates end of stream with the
	 *         {@link BufferProcessor} not invoked.
	 * @throws IOException
	 *             If fails to read input. Typically this will be because the
	 *             input is closed.
	 */
	int read(BufferProcessor processor) throws IOException;

	/**
	 * Reads data from this {@link BufferStream} to the
	 * {@link OutputBufferStream}.
	 *
	 * @param outputBufferStream
	 *            {@link OutputBufferStream} to receive the data.
	 * @return Number of bytes transferred to the {@link OutputBufferStream}.
	 *         Return of {@link BufferStream#END_OF_STREAM} indicates end of
	 *         stream.
	 * @throws IOException
	 *             If fails to read input. Typically this will be because the
	 *             input is closed.
	 */
	int read(OutputBufferStream outputBufferStream) throws IOException;

	/**
	 * Provides an accurate number of bytes available in the
	 * {@link BufferStream}.
	 *
	 * @return Number of bytes available in the {@link BufferStream}. Return of
	 *         {@link BufferStream#END_OF_STREAM} indicates end of stream.
	 */
	long available();

	/**
	 * <p>
	 * Closes the input to the {@link BufferStream} and releases the resources.
	 * <p>
	 * Further output will be ignored.
	 */
	void closeInput();

}