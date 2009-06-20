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
package net.officefloor.plugin.impl.socket.server.messagesegment;

import java.nio.ByteBuffer;

import net.officefloor.plugin.impl.socket.server.MessageSegmentPool;
import net.officefloor.plugin.impl.socket.server.PooledMessageSegment;
import net.officefloor.plugin.socket.server.spi.MessageSegment;

/**
 * {@link MessageSegmentPool} that does not pool the {@link MessageSegment}
 * instances. A new {@link MessageSegment} is created each time with a
 * {@link java.nio.HeapByteBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class NotPoolMessageSegmentPool implements MessageSegmentPool {

	/**
	 * Size of the {@link ByteBuffer} instances.
	 */
	private final int bufferSize;

	/**
	 * Initiate.
	 * 
	 * @param bufferSize
	 *            Size of the {@link ByteBuffer} instances being pooled.
	 */
	public NotPoolMessageSegmentPool(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/*
	 * ================ NotPoolMessageSegmentPool ==================
	 */

	@Override
	public int getMessageSegmentBufferSize() {
		return this.bufferSize;
	}

	@Override
	public PooledMessageSegment getMessageSegment() {
		return new InstanceMessageSegment(ByteBuffer.allocate(this.bufferSize));
	}

	@Override
	public PooledMessageSegment getMessageSegment(ByteBuffer buffer) {
		return new InstanceMessageSegment(buffer);
	}

	@Override
	public void returnMessageSegments(PooledMessageSegment segment) {
		// Do nothing as not pooling
	}

}