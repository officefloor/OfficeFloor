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
 * Retreives input from the {@link BufferStream}.
 *
 * @author Daniel Sagenschneider
 */
public interface InputBufferStream {

	/**
	 * <p>
	 * Obtains the {@link InputStream} that reads the contents of the
	 * {@link BufferStream}.
	 * <p>
	 * As the {@link BufferStream} is non-blocking any attempt to read data when
	 * none is available will result in an {@link IOException}.
	 * {@link InputStream#available()} is implemented to provide an accurate
	 * size of available bytes.
	 *
	 * @return {@link InputStream} to read from the {@link BufferStream}.
	 */
	InputStream getInputStream();

	/**
	 * <p>
	 * Obtains an {@link InputStream} that allows browsing the contents of the
	 * {@link BufferStream} without changing the {@link BufferStream} markers.
	 * <p>
	 * Once the available data has been browsed, further reads will return
	 * {@link BufferStream#END_OF_STREAM} indicating end of stream of available
	 * data. It is therefore optional for {@link InputStream#available()} to
	 * provide the available bytes (unlike {@link #getInputStream()}.
	 *
	 * @return {@link InputStream} to browse the {@link BufferStream}.
	 */
	InputStream getBrowseStream();

	/**
	 * Reads the content from the {@link BufferStream} into the input buffer
	 * returning the number of bytes loaded.
	 *
	 * @param readBuffer
	 *            Buffer to load {@link BufferStream} content.
	 * @return Number of bytes loaded into the buffer from the
	 *         {@link BufferStream}. Return of
	 *         {@link BufferStream#END_OF_STREAM} indicates end of stream with
	 *         no bytes loaded to buffer.
	 * @throws IOException
	 *             If fails to read input. Typically this will be because the
	 *             input is closed.
	 */
	int read(byte[] readBuffer) throws IOException;

	/**
	 * <p>
	 * Reads and processes the contents of a {@link ByteBuffer} from the
	 * {@link BufferStream}.
	 * <p>
	 * If there is no data in the {@link BufferStream} then the
	 * {@link BufferProcessor} will not be invoked.
	 *
	 * @param processor
	 *            {@link BufferProcessor} to process the data of the
	 *            {@link ByteBuffer}.
	 * @return Number of bytes in the {@link ByteBuffer} provided to the
	 *         {@link BufferProcessor} that were processed (read). Return of
	 *         {@link BufferStream#END_OF_STREAM} indicates end of stream.
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
	 * Closes the stream releasing resources.
	 */
	void close();

}