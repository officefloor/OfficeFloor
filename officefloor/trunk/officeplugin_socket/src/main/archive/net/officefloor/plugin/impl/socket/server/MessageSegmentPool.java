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
package net.officefloor.plugin.impl.socket.server;

import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.spi.Message;
import net.officefloor.plugin.socket.server.spi.MessageSegment;

/**
 * Pool of {@link PooledMessageSegment} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface MessageSegmentPool {

	/**
	 * Obtains the {@link ByteBuffer} size of each {@link MessageSegment}.
	 *
	 * @return {@link ByteBuffer} size of each {@link MessageSegment}.
	 */
	int getMessageSegmentBufferSize();

	/**
	 * <p>
	 * Returns a {@link MessageSegment} to this {@link MessageSegmentPool}.
	 * <p>
	 * Should the {@link MessageSegment} have next {@link MessageSegment}
	 * instances, they are also returned to this {@link MessageSegmentPool}.
	 * This aids returning {@link Message} instances in bulk.
	 *
	 * @param segment
	 *            Start {@link MessageSegment} listing to be returned to this
	 *            {@link MessageSegmentPool}.
	 */
	void returnMessageSegments(PooledMessageSegment segment);

	/**
	 * Obtains a {@link PooledMessageSegment}.
	 *
	 * @return {@link PooledMessageSegment}.
	 */
	PooledMessageSegment getMessageSegment();

	/**
	 * Obtains a {@link PooledMessageSegment} for the input {@link ByteBuffer}.
	 *
	 * @param buffer
	 *            {@link ByteBuffer}.
	 * @return {@link PooledMessageSegment}.
	 */
	PooledMessageSegment getMessageSegment(ByteBuffer buffer);
}
