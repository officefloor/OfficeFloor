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
package net.officefloor.server.stream.impl;

import java.nio.ByteBuffer;

import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Abstract {@link BufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractBufferPool<B> implements BufferPool<B> {

	/*
	 * ==================== BufferPool =======================
	 */

	@Override
	public StreamBuffer<B> getUnpooledStreamBuffer(ByteBuffer buffer) {
		return new UnpooledStreamBuffer(buffer);
	}

	/**
	 * Unpooled {@link StreamBuffer}.
	 */
	protected class UnpooledStreamBuffer implements StreamBuffer<B> {

		/**
		 * {@link ByteBuffer}.
		 */
		private final ByteBuffer buffer;

		/**
		 * Instantiate.
		 * 
		 * @param buffer
		 *            Read-only {@link ByteBuffer}.
		 */
		private UnpooledStreamBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		/*
		 * =================== PooledBuffer ======================
		 */

		@Override
		public boolean isPooled() {
			return false;
		}

		@Override
		public B getPooledBuffer() {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is unpooled");
		}

		@Override
		public ByteBuffer getUnpooledByteBuffer() {
			return this.buffer;
		}

		@Override
		public boolean write(byte datum) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is unpooled");
		}

		@Override
		public int write(byte[] data, int offset, int length) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is unpooled");
		}

		@Override
		public void release() {
			// Nothing to release
		}
	}

	/**
	 * Pooled {@link StreamBuffer}.
	 */
	protected abstract class AbstractPooledStreamBuffer implements StreamBuffer<B> {

		/*
		 * =================== PooledBuffer ======================
		 */

		@Override
		public boolean isPooled() {
			return true;
		}

		@Override
		public ByteBuffer getUnpooledByteBuffer() {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is pooled");
		}
	}

}