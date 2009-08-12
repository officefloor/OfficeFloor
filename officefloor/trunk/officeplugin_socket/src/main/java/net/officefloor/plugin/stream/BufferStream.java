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
import java.nio.channels.GatheringByteChannel;

import javax.net.ssl.SSLEngine;

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
	 * Writes the bytes to the {@link BufferStream}.
	 *
	 * @param bytes
	 *            Bytes to be written to the {@link BufferStream}.
	 * @param offset
	 *            Offset into the bytes to start obtain bytes to write.
	 * @param length
	 *            Number of bytes to write.
	 * @throws IOException
	 *             If fails to write the bytes.
	 */
	void write(byte[] bytes, int offset, int length) throws IOException;

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
	 * Reads the content from the {@link BufferStream} into the input buffer
	 * returning the number of bytes loaded.
	 *
	 * @param readBuffer
	 *            Buffer to load {@link BufferStream} content.
	 * @param offset
	 *            Offset of the input read buffer to start loading data from the
	 *            {@link BufferStream}.
	 * @param length
	 *            Maximum number of bytes to be loaded.
	 * @return Number of bytes loaded into the buffer from the
	 *         {@link BufferStream}. Return of
	 *         {@link BufferStream#END_OF_STREAM} indicates end of stream with
	 *         no bytes loaded to buffer.
	 * @throws IOException
	 *             If fails to read input. Typically this will be because the
	 *             input is closed.
	 */
	int read(byte[] readBuffer, int offset, int length) throws IOException;

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
	 * <p>
	 * Processes a batch number of {@link ByteBuffer} instances so that the
	 * available data in the {@link ByteBuffer} instances is greater than or
	 * equal to the number of bytes specified.
	 * <p>
	 * Should the number of bytes be greater than the available, all
	 * {@link ByteBuffer} instances are provided to the
	 * {@link GatheringBufferProcessor}. In this case the provided data will be
	 * less than the number of bytes specified.
	 * <p>
	 * Typically this will be used by gather operations such as with the
	 * {@link GatheringByteChannel} and {@link SSLEngine}.
	 *
	 * @param numberOfBytes
	 *            Number of bytes to be processed from this
	 *            {@link InputBufferStream}.
	 * @param processor
	 *            {@link GatheringBufferProcessor}.
	 * @return Number of bytes read by the {@link GatheringBufferProcessor}.
	 * @throws IOException
	 *             If fails to read input. Typically this will be because the
	 *             input is closed.
	 */
	int read(int numberOfBytes, GatheringBufferProcessor processor)
			throws IOException;

	/**
	 * <p>
	 * Reads data from this {@link BufferStream} to the
	 * {@link OutputBufferStream}.
	 * <p>
	 * Only available bytes are read to the {@link OutputBufferStream} and
	 * therefore the request number of bytes may not be read. The return
	 * provides the number of bytes read.
	 *
	 * @param numberOfBytes
	 *            Number of bytes to read into the {@link OutputBufferStream}.
	 * @param outputBufferStream
	 *            {@link OutputBufferStream} to receive the data.
	 * @return Number of bytes transferred to the {@link OutputBufferStream}.
	 *         Return of {@link BufferStream#END_OF_STREAM} indicates end of
	 *         stream.
	 * @throws IOException
	 *             If fails to read input. Typically this will be because the
	 *             input is closed.
	 */
	int read(int numberOfBytes, OutputBufferStream outputBufferStream)
			throws IOException;

	/**
	 * Skips the input number of bytes in the {@link BufferStream}. As there may
	 * not be the available bytes to skip in the {@link BufferStream}, this
	 * method returns the actual number of bytes skipped from the available
	 * bytes.
	 *
	 * @param numberOfBytes
	 *            Maximum number of bytes to skip.
	 * @return Number of available bytes skipped in the {@link BufferStream}.
	 *         Return of {@link BufferStream#END_OF_STREAM} indicates end of
	 *         stream with no bytes skipped.
	 * @throws IOException
	 *             If fails to skip the bytes. Typically this will be because
	 *             the input is closed.
	 */
	long skip(long numberOfBytes) throws IOException;

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