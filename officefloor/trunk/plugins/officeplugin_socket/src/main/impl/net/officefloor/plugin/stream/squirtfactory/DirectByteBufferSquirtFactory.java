/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.stream.squirtfactory;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * Direct {@link BufferSquirtFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DirectByteBufferSquirtFactory implements BufferSquirtFactory {

	/**
	 * Buffer size.
	 */
	private final int bufferSize;

	/**
	 * Pool of available {@link ByteBuffer} instances.
	 */
	private final Deque<ByteBuffer> pool = new LinkedList<ByteBuffer>();

	/**
	 * Initiate.
	 * 
	 * @param bufferSize
	 *            Buffer size.
	 */
	public DirectByteBufferSquirtFactory(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/*
	 * ==================== BufferSquirt ============================
	 */

	@Override
	public BufferSquirt createBufferSquirt() {

		// Attempt to obtain buffer from pool
		ByteBuffer buffer;
		synchronized (this.pool) {
			buffer = this.pool.pollFirst();
		}

		// Ensure have buffer
		if (buffer == null) {
			buffer = ByteBuffer.allocateDirect(this.bufferSize);
		}

		// Return the buffer squirt
		return new BufferSquirtImpl(buffer);
	}

	/**
	 * {@link BufferSquirt}.
	 */
	public class BufferSquirtImpl implements BufferSquirt {

		/**
		 * {@link ByteBuffer} to recycle.
		 */
		private final ByteBuffer recycle;

		/**
		 * {@link ByteBuffer} to use.
		 */
		private final ByteBuffer buffer;

		/**
		 * Initiate.
		 * 
		 * @param buffer
		 *            {@link ByteBuffer}.
		 */
		public BufferSquirtImpl(ByteBuffer buffer) {
			this.recycle = buffer;
			this.buffer = this.recycle.duplicate();
		}

		/*
		 * ================ BufferSquirt ======================
		 */

		@Override
		public ByteBuffer getBuffer() {
			return this.buffer;
		}

		@Override
		public void close() {
			// Recycle the buffer
			synchronized (DirectByteBufferSquirtFactory.this.pool) {
				DirectByteBufferSquirtFactory.this.pool.add(this.recycle);
			}
		}
	}

}