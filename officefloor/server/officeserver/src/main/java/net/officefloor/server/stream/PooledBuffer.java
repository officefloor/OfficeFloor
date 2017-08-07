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
 * Pooled buffer.
 * 
 * @param <B>
 *            Type of buffer.
 * @author Daniel Sagenschneider
 */
public interface PooledBuffer<B> {

	/**
	 * Indicates if pooled.
	 * 
	 * @return <code>true</code> if pooled.
	 */
	boolean isReadOnly();

	/**
	 * Obtains the buffer.
	 * 
	 * @return Buffer. Will be <code>null</code> if read-only.
	 */
	B getBuffer();

	/**
	 * Obtains the read-only {@link ByteBuffer}.
	 * 
	 * @return {@link ByteBuffer}. Will be <code>null</code> if read-only.
	 */
	ByteBuffer getReadOnlyByteBuffer();

	/**
	 * Writes a byte to the buffer.
	 * 
	 * @param datum
	 *            Byte value.
	 * @return <code>true</code> if written value to buffer. <code>false</code>
	 *         indicates the buffer is full.
	 */
	boolean write(byte datum);

	/**
	 * Writes the data to the buffer.
	 * 
	 * @param data
	 *            Data to write to the buffer.
	 * @param offset
	 *            Offset within the data to write the data.
	 * @param length
	 *            Length of data to write the data.
	 * @return Number of bytes written.
	 */
	int write(byte[] data, int offset, int length);

	/**
	 * Writes all the data to the buffer.
	 * 
	 * @param data
	 *            Data to write to the buffer.
	 * @return Number of bytes written.
	 */
	default int write(byte[] data) {
		return this.write(data, 0, data.length);
	}

	/**
	 * Releases this {@link PooledBuffer} for re-use.
	 */
	void release();

}