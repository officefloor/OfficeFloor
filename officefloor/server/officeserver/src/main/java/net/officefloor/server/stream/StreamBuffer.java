/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.stream;

import java.nio.ByteBuffer;

/**
 * Buffer that is part of a stream.
 * 
 * @param <B>
 *            Type of buffer.
 * @author Daniel Sagenschneider
 */
public abstract class StreamBuffer<B> {

	/**
	 * Writes the bytes to the {@link StreamBuffer} stream.
	 * 
	 * @param bytes
	 *            Bytes to be written to the {@link StreamBuffer} stream.
	 * @param offset
	 *            Offset into the bytes to start writing.
	 * @param length
	 *            Length of bytes to write.
	 * @param headBuffer
	 *            Head {@link StreamBuffer} in the linked list of
	 *            {@link StreamBuffer} instances.
	 * @param bufferPool
	 *            {@link StreamBufferPool} should additional
	 *            {@link StreamBuffer} instances be required in writing the
	 *            bytes.
	 */
	public static <B> void write(byte[] bytes, int offset, int length, StreamBuffer<B> headBuffer,
			StreamBufferPool<B> bufferPool) {

		// Obtain the write stream buffer
		headBuffer = getWriteStreamBuffer(headBuffer, bufferPool);

		// Write the data to the buffer
		int bytesWritten = headBuffer.write(bytes, offset, length);
		length -= bytesWritten;
		while (length > 0) {
			offset += bytesWritten;

			// Append another buffer for remaining content
			headBuffer.next = bufferPool.getPooledStreamBuffer();
			headBuffer = headBuffer.next;

			// Attempt to complete writing bytes
			bytesWritten = headBuffer.write(bytes, offset, length);
			length -= bytesWritten;
		}
	}

	/**
	 * Writes the {@link CharSequence} to the {@link StreamBuffer} stream.
	 * 
	 * @param characters
	 *            Characters to be written to the {@link StreamBuffer} stream.
	 *            Head {@link StreamBuffer} in the linked list of
	 *            {@link StreamBuffer} instances.
	 * @param bufferPool
	 *            {@link StreamBufferPool} should additional
	 *            {@link StreamBuffer} instances be required in writing the
	 *            bytes.
	 */
	public static <B> void write(CharSequence characters, StreamBuffer<B> headBuffer, StreamBufferPool<B> bufferPool) {

		// Obtain the write stream buffer
		headBuffer = getWriteStreamBuffer(headBuffer, bufferPool);

		// Write the characters to the buffer
		int length = characters.length();
		for (int i = 0; i < length; i++) {
			byte character = (byte) characters.charAt(i);

			// Attempt to write the character
			if (!headBuffer.write(character)) {

				// Append another buffer for character
				headBuffer.next = bufferPool.getPooledStreamBuffer();
				headBuffer = headBuffer.next;

				// Write character to the new buffer
				headBuffer.write(character);
			}
		}
	}

	/**
	 * Obtains the {@link StreamBuffer} to use for writing.
	 * 
	 * @param headBuffer
	 *            Head {@link StreamBuffer} in the linked list of
	 *            {@link StreamBuffer} instances.
	 * @param bufferPool
	 *            {@link StreamBufferPool} should additional
	 *            {@link StreamBuffer} instances be required in writing the
	 *            bytes.
	 * @return {@link StreamBuffer} within the linked list to next write data.
	 */
	public static <B> StreamBuffer<B> getWriteStreamBuffer(StreamBuffer<B> headBuffer, StreamBufferPool<B> bufferPool) {

		// Only append to end of linked list
		while (headBuffer.next != null) {
			headBuffer = headBuffer.next;
		}

		// Ensure writing to pooled buffer
		if (!headBuffer.isPooled) {
			headBuffer.next = bufferPool.getPooledStreamBuffer();
			headBuffer = headBuffer.next;
		}

		// Return the write stream buffer
		return headBuffer;
	}

	/**
	 * Indicates if pooled.
	 * 
	 * @return <code>true</code> if pooled.
	 */
	public final boolean isPooled;

	/**
	 * Obtains the pooled buffer.
	 * 
	 * @return Buffer. Will be <code>null</code> if read-only.
	 */
	public final B pooledBuffer;

	/**
	 * Obtains the read-only {@link ByteBuffer}.
	 * 
	 * @return {@link ByteBuffer}. Will be <code>null</code> if read-only.
	 */
	public final ByteBuffer unpooledByteBuffer;

	/**
	 * <p>
	 * Next {@link StreamBuffer} in the stream.
	 * <p>
	 * This allows chaining {@link StreamBuffer} instances into a linked list
	 * (and avoids memory management overheads of creating/destroying lists).
	 */
	public StreamBuffer<B> next = null;

	/**
	 * Instantiate.
	 * 
	 * @param pooledBuffer
	 *            Pooled buffer. Must be <code>null</code> if unpooled
	 *            {@link ByteBuffer} provided.
	 * @param unpooledByteBuffer
	 *            Unpooled {@link ByteBuffer}. Must be <code>null</code> if
	 *            pooled buffer provided.
	 * @throws IllegalArgumentException
	 *             If not providing the one buffer.
	 */
	public StreamBuffer(B pooledBuffer, ByteBuffer unpooledByteBuffer) {
		if (pooledBuffer != null) {

			// Pooled buffer, so ensure only pooled buffer
			if (unpooledByteBuffer != null) {
				throw new IllegalArgumentException("Must provide either pooled or unpooled buffer (not both)");
			}

			// Setup for pooled buffer
			this.isPooled = true;
			this.pooledBuffer = pooledBuffer;
			this.unpooledByteBuffer = null;

		} else if (unpooledByteBuffer != null) {
			// Setup for unpooled buffer
			this.isPooled = false;
			this.pooledBuffer = null;
			this.unpooledByteBuffer = unpooledByteBuffer;

		} else {
			// No buffer provided
			throw new IllegalArgumentException("Must provide a pooled or unpooled buffer");
		}
	}

	/**
	 * Writes a byte to the pooled buffer.
	 * 
	 * @param datum
	 *            Byte value.
	 * @return <code>true</code> if written value to buffer. <code>false</code>
	 *         indicates the pooled buffer is full.
	 */
	public abstract boolean write(byte datum);

	/**
	 * Writes the data to the pooled buffer.
	 * 
	 * @param data
	 *            Data to write to the pooled buffer.
	 * @param offset
	 *            Offset within the data to write the data.
	 * @param length
	 *            Length of data to write the data.
	 * @return Number of bytes written.
	 */
	public abstract int write(byte[] data, int offset, int length);

	/**
	 * Writes all the data to the pooled buffer.
	 * 
	 * @param data
	 *            Data to write to the pooled buffer.
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