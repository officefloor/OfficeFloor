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
 * Provides interface to wrap buffer pooling implementations.
 * 
 * @param <B>
 *            Type of buffer being pooled.
 * @author Daniel Sagenschneider
 */
public interface StreamBufferPool<B> {

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
	 * @param buffer
	 *            {@link ByteBuffer}.
	 * @return {@link StreamBuffer} for the unpooled {@link ByteBuffer}.
	 */
	StreamBuffer<B> getUnpooledStreamBuffer(ByteBuffer buffer);

}