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