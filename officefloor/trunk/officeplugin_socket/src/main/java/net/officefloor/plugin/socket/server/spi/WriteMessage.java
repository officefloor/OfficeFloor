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
package net.officefloor.plugin.socket.server.spi;

import java.nio.ByteBuffer;

/**
 * {@link Message} to write to the client.
 * 
 * @author Daniel Sagenschneider
 */
public interface WriteMessage extends Message {

	/**
	 * Appends the whole input data to this {@link Message}.
	 * 
	 * @param data
	 *            Data.
	 */
	void append(byte[] data);

	/**
	 * Appends the specified input data to this {@link Message}.
	 * 
	 * @param data
	 *            Data.
	 * @param offset
	 *            Offset from start of data to include.
	 * @param length
	 *            Bytes from the offset to include.
	 */
	void append(byte[] data, int offset, int length);

	/**
	 * Appends a single {@link MessageSegment} to this {@link Message}.
	 * 
	 * @return Appended {@link MessageSegment}.
	 */
	MessageSegment appendSegment();

	/**
	 * <p>
	 * Appends a {@link MessageSegment} for the input {@link ByteBuffer}.
	 * <p>
	 * This allows for {@link ByteBuffer} optimises so that do not need to copy
	 * bytes between {@link ByteBuffer} instances.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} to append to this {@link Message}.
	 * @return {@link MessageSegment} for the input {@link ByteBuffer}.
	 */
	MessageSegment appendSegment(ByteBuffer byteBuffer);

	/**
	 * <p>
	 * Flags that this {@link Message} is complete and can be written in full to
	 * the client.
	 * <p>
	 * Once invoked, no further data can be added to this {@link WriteMessage}.
	 */
	void write();

	/**
	 * Flags whether this {@link Message} has been completely written to the
	 * client.
	 * 
	 * @return <code>true</code> if this {@link Message} has been completely
	 *         written to the client.
	 */
	boolean isWritten();
}
