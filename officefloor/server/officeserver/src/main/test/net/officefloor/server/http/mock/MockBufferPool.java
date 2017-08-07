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
package net.officefloor.server.http.mock;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.PooledBuffer;

/**
 * Mock {@link BufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockBufferPool implements BufferPool<byte[]> {

	/**
	 * Size of the buffers.
	 */
	private final int bufferSize;

	/**
	 * Registered {@link AbstractMockPooledBuffer} instances.
	 */
	private final Deque<AbstractMockPooledBuffer> createdBuffers = new ConcurrentLinkedDeque<>();

	/**
	 * Instantiate with default buffer size for testing.
	 */
	public MockBufferPool() {
		this(3); // small buffer to ensure handling filling buffer
	}

	/**
	 * Instantiate.
	 * 
	 * @param bufferSize
	 *            Size of the buffers.
	 */
	public MockBufferPool(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/*
	 * =============== BufferPool =======================
	 */

	@Override
	public PooledBuffer<byte[]> getPooledBuffer() {
		MockWritablePooledBuffer buffer = new MockWritablePooledBuffer(this.bufferSize);
		this.createdBuffers.add(buffer);
		return buffer;
	}

	@Override
	public PooledBuffer<byte[]> getReadOnlyBuffer(ByteBuffer byteBuffer) {
		MockReadOnlyPooledBuffer buffer = new MockReadOnlyPooledBuffer(byteBuffer);
		this.createdBuffers.add(buffer);
		return buffer;
	}

	/**
	 * Abstract mock {@link PooledBuffer}.
	 */
	private static abstract class AbstractMockPooledBuffer implements PooledBuffer<byte[]> {

		/**
		 * Indicates if released.
		 */
		private boolean isReleased = false;

		@Override
		public void release() {
			this.isReleased = true;
		}
	}

	/**
	 * Mock writable {@link PooledBuffer}.
	 */
	private static class MockWritablePooledBuffer extends AbstractMockPooledBuffer {

		/**
		 * Buffer.
		 */
		private final byte[] buffer;

		/**
		 * Position within buffer to write next data.
		 */
		private int position = 0;

		/**
		 * Instantiate.
		 * 
		 * @param bufferSize
		 *            Size of the buffer.
		 */
		private MockWritablePooledBuffer(int bufferSize) {
			this.buffer = new byte[bufferSize];
		}

		/*
		 * ================== PooledBuffer ======================
		 */

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public ByteBuffer getReadOnlyByteBuffer() {
			throw new IllegalStateException("Can not obtain ByteBuffer for " + this.getClass().getSimpleName());
		}

		@Override
		public byte[] getBuffer() {
			return this.buffer;
		}

		@Override
		public boolean write(byte datum) {

			// Determine if buffer full
			if (this.position >= this.buffer.length) {
				return false;
			}

			// Add the byte to the buffer
			this.buffer[this.position++] = datum;
			return true;
		}

		@Override
		public int write(byte[] data, int offset, int length) {

			// Obtain the length of data to write
			int writeLength = Math.min(length, this.buffer.length - this.position);

			// Write the data
			for (int i = offset; i < writeLength; i++) {
				this.buffer[this.position++] = data[i];
			}

			// Return the bytes written
			return writeLength;
		}

	}

	/**
	 * Mock read-only {@link PooledBuffer}.
	 */
	private static class MockReadOnlyPooledBuffer extends AbstractMockPooledBuffer {

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
		private MockReadOnlyPooledBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		/*
		 * =================== PooledBuffer ======================
		 */

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public byte[] getBuffer() {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is read-only");
		}

		@Override
		public ByteBuffer getReadOnlyByteBuffer() {
			return this.buffer;
		}

		@Override
		public boolean write(byte datum) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is read-only");
		}

		@Override
		public int write(byte[] data, int offset, int length) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is read-only");
		}
	}

}