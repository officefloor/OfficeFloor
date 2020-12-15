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
import java.time.format.DateTimeFormatter;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Buffer that is part of a stream.
 * 
 * @param <B> Type of buffer.
 * @author Daniel Sagenschneider
 */
public abstract class StreamBuffer<B> {

	/**
	 * {@link FileChannel} content buffer.
	 */
	public static class FileBuffer {

		/**
		 * {@link FileChannel}.
		 */
		public final FileChannel file;

		/**
		 * Position within the {@link FileChannel} to write content.
		 */
		public final long position;

		/**
		 * Count of bytes after the position to write from the {@link FileChannel}.
		 * Negative number (eg <code>-1</code>) will write remaining content of
		 * {@link FileChannel}.
		 */
		public final long count;

		/**
		 * Optional {@link FileCompleteCallback}. May be <code>null</code>.
		 */
		public final FileCompleteCallback callback;

		/**
		 * Bytes written. This is used by server implementations to track the number of
		 * bytes written from the {@link FileChannel}.
		 */
		public long bytesWritten = 0;

		/**
		 * Instantiate.
		 * 
		 * @param file     {@link FileChannel}.
		 * @param position Position.
		 * @param count    Count.
		 * @param callback Optional {@link FileCompleteCallback}. May be
		 *                 <code>null</code>.
		 */
		public FileBuffer(FileChannel file, long position, long count, FileCompleteCallback callback) {
			this.file = file;
			this.position = position;
			this.count = count;
			this.callback = callback;
		}

		/**
		 * Instantiate to write the entire {@link FileChannel} content.
		 * 
		 * @param file     {@link FileChannel}.
		 * @param callback Optional {@link FileCompleteCallback}. May be
		 *                 <code>null</code>.
		 */
		public FileBuffer(FileChannel file, FileCompleteCallback callback) {
			this(file, 0, -1, callback);
		}
	}

	/**
	 * Writes all the bytes to the {@link StreamBuffer} stream.
	 * 
	 * @param <B>                           Buffer type.
	 * @param bytes                         Bytes to be written to the
	 *                                      {@link StreamBuffer} stream.
	 * @param headBuffer                    Head {@link StreamBuffer} in the linked
	 *                                      list of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool} should
	 *                                      additional {@link StreamBuffer}
	 *                                      instances be required in writing the
	 *                                      bytes.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> void write(byte[] bytes, StreamBuffer<B> headBuffer, StreamBufferPool<B> bufferPool,
			ServerMemoryOverloadHandler serverMemoryOverloadedHandler) throws ServerMemoryOverloadedException {
		write(bytes, 0, bytes.length, headBuffer, bufferPool, serverMemoryOverloadedHandler);
	}

	/**
	 * Writes the bytes to the {@link StreamBuffer} stream.
	 * 
	 * @param <B>                           Buffer type.
	 * @param bytes                         Bytes to be written to the
	 *                                      {@link StreamBuffer} stream.
	 * @param offset                        Offset into the bytes to start writing.
	 * @param length                        Length of bytes to write.
	 * @param headBuffer                    Head {@link StreamBuffer} in the linked
	 *                                      list of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool} should
	 *                                      additional {@link StreamBuffer}
	 *                                      instances be required in writing the
	 *                                      bytes.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> void write(byte[] bytes, int offset, int length, StreamBuffer<B> headBuffer,
			StreamBufferPool<B> bufferPool, ServerMemoryOverloadHandler serverMemoryOverloadedHandler)
			throws ServerMemoryOverloadedException {

		// Obtain the write stream buffer
		headBuffer = getWriteStreamBuffer(headBuffer, bufferPool, serverMemoryOverloadedHandler);

		// Write the data to the buffer
		int bytesWritten = headBuffer.write(bytes, offset, length);
		length -= bytesWritten;
		while (length > 0) {
			offset += bytesWritten;

			// Append another buffer for remaining content
			headBuffer.next = bufferPool.getPooledStreamBuffer(serverMemoryOverloadedHandler);
			headBuffer = headBuffer.next;

			// Attempt to complete writing bytes
			bytesWritten = headBuffer.write(bytes, offset, length);
			length -= bytesWritten;
		}
	}

	/**
	 * Writes all the {@link CharSequence} to the {@link StreamBuffer} stream.
	 * 
	 * @param <B>                           Buffer type.
	 * @param characters                    Characters to be written to the
	 *                                      {@link StreamBuffer} stream.
	 * @param headBuffer                    Head {@link StreamBuffer} in the linked
	 *                                      list of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool} should
	 *                                      additional {@link StreamBuffer}
	 *                                      instances be required in writing the
	 *                                      bytes.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> void write(CharSequence characters, StreamBuffer<B> headBuffer, StreamBufferPool<B> bufferPool,
			ServerMemoryOverloadHandler serverMemoryOverloadedHandler) throws ServerMemoryOverloadedException {
		write(characters, 0, characters.length(), headBuffer, bufferPool, serverMemoryOverloadedHandler);
	}

	/**
	 * Writes the {@link CharSequence} to the {@link StreamBuffer} stream.
	 *
	 * @param <B>                           Buffer type.
	 * @param characters                    Characters to be written to the
	 *                                      {@link StreamBuffer} stream.
	 * @param offset                        Offset into the {@link CharSequence} to
	 *                                      start writing.
	 * @param length                        Length of characters to write.
	 * @param headBuffer                    Head {@link StreamBuffer} in the linked
	 *                                      list of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool} should
	 *                                      additional {@link StreamBuffer}
	 *                                      instances be required in writing the
	 *                                      bytes.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> void write(CharSequence characters, int offset, int length, StreamBuffer<B> headBuffer,
			StreamBufferPool<B> bufferPool, ServerMemoryOverloadHandler serverMemoryOverloadedHandler)
			throws ServerMemoryOverloadedException {

		// Obtain the write stream buffer
		headBuffer = getWriteStreamBuffer(headBuffer, bufferPool, serverMemoryOverloadedHandler);

		// Write the characters to the buffer
		for (int i = 0; i < length; i++) {
			byte character = (byte) characters.charAt(offset + i);
			headBuffer = writeByte(character, headBuffer, bufferPool, serverMemoryOverloadedHandler);
		}
	}

	/**
	 * HTTP - (minus) value.
	 */
	private static final byte MINUS = "-".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP 0 value.
	 */
	private static final byte ZERO = "0".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP {@link Long#MIN_VALUE} value.
	 */
	private static final byte[] MIN_VALUE = String.valueOf(Long.MIN_VALUE).getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * Writes a long value to the {@link StreamBuffer}.
	 *
	 * @param <B>                           Buffer type.
	 * @param value                         Long value to write to the
	 *                                      {@link StreamBuffer}.
	 * @param head                          Head {@link StreamBuffer} of linked list
	 *                                      of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool}.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> void write(long value, StreamBuffer<B> head, StreamBufferPool<B> bufferPool,
			ServerMemoryOverloadHandler serverMemoryOverloadedHandler) throws ServerMemoryOverloadedException {

		// Determine if min value (as can not make positive)
		if (value == Long.MIN_VALUE) {
			StreamBuffer.write(MIN_VALUE, head, bufferPool, serverMemoryOverloadedHandler);
			return;
		}

		// Obtain the write buffer
		StreamBuffer<B> writeBuffer = StreamBuffer.getWriteStreamBuffer(head, bufferPool,
				serverMemoryOverloadedHandler);

		// Write sign
		if (value < 0) {
			writeByte(MINUS, writeBuffer, bufferPool, serverMemoryOverloadedHandler);

			// Make positive to write digits
			value = -value;
		}

		// Obtain the one's digit
		byte onesDigit = (byte) (value % 10);
		onesDigit += ZERO;

		// Write the value
		long lessMagnitude = value / 10;
		writeBuffer = recusiveWriteInteger(lessMagnitude, writeBuffer, bufferPool, serverMemoryOverloadedHandler);

		// Always write the first digit
		writeByte(onesDigit, writeBuffer, bufferPool, serverMemoryOverloadedHandler);
	}

	/**
	 * Uses recursion to write the long digits.
	 * 
	 * @param value                         Value to write.
	 * @param writeBuffer                   Write {@link StreamBuffer}.
	 * @param bufferPool                    {@link StreamBufferPool}.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @return Next write {@link StreamBuffer}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	private static <B> StreamBuffer<B> recusiveWriteInteger(long value, StreamBuffer<B> writeBuffer,
			StreamBufferPool<B> bufferPool, ServerMemoryOverloadHandler serverMemoryOverloadedHandler)
			throws ServerMemoryOverloadedException {

		// Drop out when value at zero
		if (value == 0) {
			return writeBuffer;
		}

		// Not complete, so continue writing the next digit
		long lessMagnitude = value / 10;
		writeBuffer = recusiveWriteInteger(lessMagnitude, writeBuffer, bufferPool, serverMemoryOverloadedHandler);

		// Now write the current digit
		byte currentDigit = (byte) (value % 10);
		currentDigit += ZERO;
		writeBuffer = writeByte(currentDigit, writeBuffer, bufferPool, serverMemoryOverloadedHandler);

		// Return the write buffer
		return writeBuffer;
	}

	/**
	 * <p>
	 * Obtains an {@link Appendable} to write to the {@link StreamBuffer} stream.
	 * <p>
	 * Typical use of this is for the {@link DateTimeFormatter}.
	 * 
	 * @param <B>                           Buffer type.
	 * @param headBuffer                    Head {@link StreamBuffer} in the linked
	 *                                      list of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool} should
	 *                                      additional {@link StreamBuffer}
	 *                                      instances be required in writing the
	 *                                      bytes.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @return {@link Appendable} to write to the {@link StreamBuffer} stream.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> Appendable getAppendable(StreamBuffer<B> headBuffer, StreamBufferPool<B> bufferPool,
			ServerMemoryOverloadHandler serverMemoryOverloadedHandler) throws ServerMemoryOverloadedException {
		StreamBuffer<B> initialWriteBuffer = StreamBuffer.getWriteStreamBuffer(headBuffer, bufferPool,
				serverMemoryOverloadedHandler);
		return new Appendable() {

			private StreamBuffer<B> writeBuffer = initialWriteBuffer;

			@Override
			public Appendable append(CharSequence csq, int start, int end) throws IOException {
				for (int i = start; i < end; i++) {
					char character = csq.charAt(i);
					this.append(character);
				}
				return this;
			}

			@Override
			public Appendable append(char c) throws IOException {
				this.writeBuffer = writeByte((byte) c, this.writeBuffer, bufferPool, serverMemoryOverloadedHandler);
				return this;
			}

			@Override
			public Appendable append(CharSequence csq) throws IOException {
				return this.append(csq, 0, csq.length());
			}
		};
	}

	/**
	 * Obtains the {@link StreamBuffer} to use for writing.
	 * 
	 * @param <B>                           Buffer type.
	 * @param headBuffer                    Head {@link StreamBuffer} in the linked
	 *                                      list of {@link StreamBuffer} instances.
	 * @param bufferPool                    {@link StreamBufferPool} should
	 *                                      additional {@link StreamBuffer}
	 *                                      instances be required in writing the
	 *                                      bytes.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @return {@link StreamBuffer} within the linked list to next write data.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> StreamBuffer<B> getWriteStreamBuffer(StreamBuffer<B> headBuffer, StreamBufferPool<B> bufferPool,
			ServerMemoryOverloadHandler serverMemoryOverloadedHandler) throws ServerMemoryOverloadedException {

		// Only append to end of linked list
		while (headBuffer.next != null) {
			headBuffer = headBuffer.next;
		}

		// Ensure writing to pooled buffer
		if (headBuffer.pooledBuffer == null) {
			// Not pooled, so append pooled
			headBuffer.next = bufferPool.getPooledStreamBuffer(serverMemoryOverloadedHandler);
			headBuffer = headBuffer.next;
		}

		// Return the write stream buffer
		return headBuffer;
	}

	/**
	 * Writes a HTTP encoded character.
	 *
	 * @param <B>                           Buffer type.
	 * @param character                     Character to write.
	 * @param writeBuffer                   Write {@link StreamBuffer}.
	 * @param bufferPool                    {@link StreamBufferPool}.
	 * @param serverMemoryOverloadedHandler {@link ServerMemoryOverloadHandler}.
	 * @return Next write {@link StreamBuffer}.
	 * @throws ServerMemoryOverloadedException If a {@link StreamBuffer} is required
	 *                                         and server memory overloaded.
	 */
	public static <B> StreamBuffer<B> writeByte(byte character, StreamBuffer<B> writeBuffer,
			StreamBufferPool<B> bufferPool, ServerMemoryOverloadHandler serverMemoryOverloadedHandler)
			throws ServerMemoryOverloadedException {
		// Attempt to write to buffer
		if (!writeBuffer.write(character)) {
			// Buffer full, so write to new buffer
			writeBuffer.next = bufferPool.getPooledStreamBuffer(serverMemoryOverloadedHandler);
			writeBuffer = writeBuffer.next;
			if (!writeBuffer.write(character)) {
				throw new IllegalStateException("New pooled space buffer should always have space");
			}
		}
		return writeBuffer;
	}

	/**
	 * Obtains the pooled buffer. Will be <code>null</code> if not pooled.
	 */
	public final B pooledBuffer;

	/**
	 * Obtains the non-pooled {@link ByteBuffer}. Will be <code>null</code> if
	 * non-pooled.
	 */
	public final ByteBuffer unpooledByteBuffer;

	/**
	 * Obtains the {@link FileBuffer}. Will be <code>null</code> if not file.
	 */
	public final FileBuffer fileBuffer;

	/**
	 * <p>
	 * Next {@link StreamBuffer} in the stream.
	 * <p>
	 * This allows chaining {@link StreamBuffer} instances into a linked list (and
	 * avoids memory management overheads of creating/destroying lists).
	 */
	public StreamBuffer<B> next = null;

	/**
	 * Instantiate.
	 * 
	 * @param pooledBuffer       Pooled buffer. Must be <code>null</code> if another
	 *                           buffer provided.
	 * @param unpooledByteBuffer Unpooled {@link ByteBuffer}. Must be
	 *                           <code>null</code> if another buffer provided.
	 * @param fileBuffer         {@link FileBuffer}. Must be <code>null</code> if
	 *                           another buffer provided.
	 * @throws IllegalArgumentException If not providing the one buffer.
	 */
	public StreamBuffer(B pooledBuffer, ByteBuffer unpooledByteBuffer, FileBuffer fileBuffer) {
		if (pooledBuffer != null) {

			// Pooled buffer, so ensure only pooled buffer
			if ((unpooledByteBuffer != null) || (fileBuffer != null)) {
				throw new IllegalArgumentException("Must provide either a pooled, unpooled or file buffer");
			}

			// Setup for pooled buffer
			this.pooledBuffer = pooledBuffer;
			this.unpooledByteBuffer = null;
			this.fileBuffer = null;

		} else if (unpooledByteBuffer != null) {

			// Unpooled buffer, so ensure only unpooled buffer
			// (pooled checked above)
			if (fileBuffer != null) {
				throw new IllegalArgumentException("Must provide either a pooled, unpooled or file buffer");
			}

			// Setup for unpooled buffer
			this.pooledBuffer = null;
			this.unpooledByteBuffer = unpooledByteBuffer;
			this.fileBuffer = null;

		} else if (fileBuffer != null) {

			// Above checks others are null

			// Setup for file buffer
			this.pooledBuffer = null;
			this.unpooledByteBuffer = null;
			this.fileBuffer = fileBuffer;

		} else {
			// No buffer provided
			throw new IllegalArgumentException("Must provide a pooled, unpooled or file buffer");
		}
	}

	/**
	 * Writes a byte to the pooled buffer.
	 * 
	 * @param datum Byte value.
	 * @return <code>true</code> if written value to buffer. <code>false</code>
	 *         indicates the pooled buffer is full.
	 */
	public abstract boolean write(byte datum);

	/**
	 * Writes the data to the pooled buffer.
	 * 
	 * @param data   Data to write to the pooled buffer.
	 * @param offset Offset within the data to write the data.
	 * @param length Length of data to write the data.
	 * @return Number of bytes written.
	 */
	public abstract int write(byte[] data, int offset, int length);

	/**
	 * Writes all the data to the pooled buffer.
	 * 
	 * @param data Data to write to the pooled buffer.
	 * @return Number of bytes written.
	 */
	public int write(byte[] data) {
		return this.write(data, 0, data.length);
	}

	/**
	 * Releases this {@link StreamBuffer} for re-use.
	 */
	public abstract void release();

}
