/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.spi;

import java.nio.ByteBuffer;

/**
 * {@link net.officefloor.plugin.socket.server.spi.Message} to write to the
 * client.
 * 
 * @author Daniel
 */
public interface WriteMessage extends Message {

	/**
	 * Appends a single {@link MessageSegment} to this {@link Message}.
	 * 
	 * @return Appended {@link MessageSegment}.
	 */
	MessageSegment appendSegment();

	/**
	 * Appends <code>number</code> of {@link MessageSegment} instances to this
	 * {@link Message}.
	 * 
	 * @param number
	 *            Number of {@link MessageSegment} instances to append.
	 * @return First {@link MessageSegment} appended.
	 */
	MessageSegment appendSegments(int number);

	/**
	 * Appends a {@link MessageSegment} for the input {@link ByteBuffer}.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} to append to this {@link Message}.
	 * @return {@link MessageSegment} for the input {@link ByteBuffer}.
	 */
	MessageSegment appendSegment(ByteBuffer byteBuffer);

	/**
	 * Flags that this {@link Message} is complete and can be written in full to
	 * the client.
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
