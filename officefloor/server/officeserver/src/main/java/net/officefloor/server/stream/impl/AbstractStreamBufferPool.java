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

import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Abstract {@link StreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractStreamBufferPool<B> implements StreamBufferPool<B> {

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
	protected class UnpooledStreamBuffer extends StreamBuffer<B> {

		/**
		 * Instantiate.
		 * 
		 * @param buffer
		 *            Read-only {@link ByteBuffer}.
		 */
		private UnpooledStreamBuffer(ByteBuffer buffer) {
			super(null, buffer);
		}

		/*
		 * =================== StreamBuffer ======================
		 */

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

}