/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Provides interface to wrap buffer pooling implementations.
 * 
 * @param <B> Type of buffer being pooled.
 * @author Daniel Sagenschneider
 */
public interface StreamBufferPool<B> extends AutoCloseable {

	/**
	 * Obtains a {@link StreamBuffer}.
	 * 
	 * @return {@link StreamBuffer}.
	 */
	StreamBuffer<B> getPooledStreamBuffer();

	/**
	 * <p>
	 * Obtains an {@link StreamBuffer} that is not pooled. This is for
	 * {@link ByteBuffer} instances that are managed outside the BufferPool.
	 * <p>
	 * Typical use is to create {@link StreamBuffer} for some read-only cached
	 * content within a {@link ByteBuffer}.
	 * 
	 * @param buffer {@link ByteBuffer}.
	 * @return {@link StreamBuffer} for the unpooled {@link ByteBuffer}.
	 */
	StreamBuffer<B> getUnpooledStreamBuffer(ByteBuffer buffer);

	/**
	 * <p>
	 * Obtains a {@link StreamBuffer} for the {@link FileChannel} content.
	 * <p>
	 * This enables efficient writing (ie DMA) of {@link FileChannel} content.
	 * <p>
	 * To write the entire {@link FileChannel} contents, invoke
	 * <code>write(file, 0, -1)</code>.
	 * <p>
	 * Note that the underlying implementation will need to support
	 * {@link FileChannel} efficiencies.
	 *
	 * @param file     {@link FileChannel}.
	 * @param position Position within the {@link FileChannel} to start writing
	 *                 content. Must be non-negative number.
	 * @param count    Count of bytes to write from the {@link FileChannel}. A
	 *                 negative value (typically <code>-1</code>) indicates to write
	 *                 the remaining {@link FileChannel} content from the position.
	 * @param callback Optional {@link FileCompleteCallback}. May be
	 *                 <code>null</code>.
	 * @return {@link StreamBuffer} for the {@link FileChannel}.
	 * @throws IOException If fails to create the {@link StreamBuffer} for the
	 *                     {@link FileChannel}. Typically, this is because the
	 *                     underlying implementation does not support DMA and copies
	 *                     the data from the {@link FileChannel}.
	 */
	StreamBuffer<B> getFileStreamBuffer(FileChannel file, long position, long count, FileCompleteCallback callback)
			throws IOException;

	/**
	 * Closes pool releasing all {@link StreamBuffer} instances.
	 */
	void close();

}
