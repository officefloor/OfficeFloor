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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.Assert;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Mock {@link BufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockBufferPool implements BufferPool<byte[]> {

	/**
	 * Releases the {@link StreamBuffer} instances.
	 * 
	 * @param buffers
	 *            {@link StreamBuffer} instances to release by to their
	 *            {@link BufferPool}.
	 */
	public static void releaseStreamBuffers(Iterable<StreamBuffer<byte[]>> buffers) {
		for (StreamBuffer<byte[]> buffer : buffers) {
			buffer.release();
		}
	}

	/**
	 * Creates an {@link InputStream} to the content of the {@link StreamBuffer}
	 * instances.
	 * 
	 * @param buffers
	 *            {@link StreamBuffer} instances that should be supplied by a
	 *            {@link MockBufferPool}.
	 * @return {@link InputStream} to read the data from the
	 *         {@link StreamBuffer} instances.
	 */
	public static InputStream createInputStream(Iterable<StreamBuffer<byte[]>> buffers) {
		return new MockBufferPoolInputStream(buffers.iterator());
	}

	/**
	 * Convenience method to obtain the contents of the buffers as a string.
	 * 
	 * @param buffers
	 *            {@link StreamBuffer} instances that should be supplied by a
	 *            {@link MockBufferPool}.
	 * @param charset
	 *            {@link Charset} of underlying data.
	 * @return Content of buffers as string.
	 */
	public static String getContent(Iterable<StreamBuffer<byte[]>> buffers, Charset charset) {
		try {
			InputStream inputStream = createInputStream(buffers);
			InputStreamReader reader = new InputStreamReader(inputStream, charset);
			StringWriter content = new StringWriter();
			for (int character = reader.read(); character != -1; character = reader.read()) {
				content.write(character);
			}
			content.flush();
			return content.toString();
		} catch (IOException ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Size of the buffers.
	 */
	private final int bufferSize;

	/**
	 * Registered {@link AbstractMockStreamBuffer} instances.
	 */
	private final Deque<AbstractMockStreamBuffer> createdBuffers = new ConcurrentLinkedDeque<>();

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

	/**
	 * Indicates if there are active {@link StreamBuffer} instances.
	 * 
	 * @return <code>true</code> if there are active {@link StreamBuffer}
	 *         instances not released back to this {@link BufferPool}.
	 */
	public boolean isActiveBuffers() {

		// Determine if non-released (active) buffer
		for (AbstractMockStreamBuffer buffer : this.createdBuffers) {
			if (!buffer.isReleased) {
				return true; // not released buffer, so still active
			}
		}

		// All buffers released, so none are active
		return false;
	}

	/**
	 * Asserts that all {@link StreamBuffer} instances have been released.
	 */
	public void assertAllBuffersReturned() {
		for (AbstractMockStreamBuffer buffer : this.createdBuffers) {
			Assert.assertTrue("Buffer should be released", buffer.isReleased);
		}
	}

	/*
	 * =============== BufferPool =======================
	 */

	@Override
	public StreamBuffer<byte[]> getPooledStreamBuffer() {
		MockPooledStreamBuffer buffer = new MockPooledStreamBuffer(this.bufferSize);
		this.createdBuffers.add(buffer);
		return buffer;
	}

	@Override
	public StreamBuffer<byte[]> getUnpooledStreamBuffer(ByteBuffer byteBuffer) {
		MockUnpooledStreamBuffer buffer = new MockUnpooledStreamBuffer(byteBuffer);
		this.createdBuffers.add(buffer);
		return buffer;
	}

	/**
	 * Abstract mock {@link StreamBuffer}.
	 */
	private static abstract class AbstractMockStreamBuffer implements StreamBuffer<byte[]> {

		/**
		 * Indicates if released.
		 */
		private volatile boolean isReleased = false;

		@Override
		public void release() {
			this.isReleased = true;
		}
	}

	/**
	 * Mock pooled {@link StreamBuffer}.
	 */
	private static class MockPooledStreamBuffer extends AbstractMockStreamBuffer {

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
		private MockPooledStreamBuffer(int bufferSize) {
			this.buffer = new byte[bufferSize];
		}

		/*
		 * ================== PooledBuffer ======================
		 */

		@Override
		public boolean isPooled() {
			return true;
		}

		@Override
		public ByteBuffer getUnpooledByteBuffer() {
			Assert.fail("Can not obtain ByteBuffer for " + this.getClass().getSimpleName());
			return null;
		}

		@Override
		public byte[] getPooledBuffer() {
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
			int endLength = offset + writeLength;
			for (int i = offset; i < endLength; i++) {
				this.buffer[this.position++] = data[i];
			}

			// Return the bytes written
			return writeLength;
		}
	}

	/**
	 * Mock unpooled {@link StreamBuffer}.
	 */
	private static class MockUnpooledStreamBuffer extends AbstractMockStreamBuffer {

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
		private MockUnpooledStreamBuffer(ByteBuffer buffer) {
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
		public byte[] getPooledBuffer() {
			Assert.fail(this.getClass().getSimpleName() + " is unpooled");
			return null;
		}

		@Override
		public ByteBuffer getUnpooledByteBuffer() {
			return this.buffer;
		}

		@Override
		public boolean write(byte datum) {
			Assert.fail(this.getClass().getSimpleName() + " is unpooled");
			return false;
		}

		@Override
		public int write(byte[] data, int offset, int length) {
			Assert.fail(this.getClass().getSimpleName() + " is unpooled");
			return 0;
		}
	}

	/**
	 * {@link InputStream} to read in the output content to {@link StreamBuffer}
	 * instances.
	 */
	private static class MockBufferPoolInputStream extends InputStream {

		/**
		 * Translate the byte to an int value.
		 * 
		 * @param value
		 *            Byte value.
		 * @return Int value.
		 */
		private static int byteToInt(byte value) {
			return value & 0xff;
		}

		/**
		 * {@link Iterator} over the {@link StreamBuffer} instances for the
		 * stream of data to return.
		 */
		private final Iterator<StreamBuffer<byte[]>> iterator;

		/**
		 * Current {@link StreamBuffer} to read contents.
		 */
		private AbstractMockStreamBuffer currentBuffer = null;

		/**
		 * Position to read next value from current pooled {@link StreamBuffer}
		 */
		private int currentBufferPosition = 0;

		/**
		 * Instantiate.
		 * 
		 * @param iterator
		 *            {@link Iterator} over the {@link StreamBuffer} instances
		 *            for the stream of data to return.
		 */
		private MockBufferPoolInputStream(Iterator<StreamBuffer<byte[]>> iterator) {
			this.iterator = iterator;
		}

		/*
		 * =================== InputStream ==========================
		 */

		@Override
		public int read() throws IOException {

			// Loop until read byte (or end of stream)
			for (;;) {

				// Determine if current buffer to read value
				if (this.currentBuffer != null) {

					// Attempt to obtain value from current buffer
					if (this.currentBuffer.isPooled()) {
						MockPooledStreamBuffer pooledBuffer = (MockPooledStreamBuffer) this.currentBuffer;

						// Obtain the pooled data
						byte[] bufferData = this.currentBuffer.getPooledBuffer();

						// Determine if can read data from buffer
						if (this.currentBufferPosition < pooledBuffer.position) {
							// Read the data from the buffer
							return byteToInt(bufferData[this.currentBufferPosition++]);
						}

						// As here, finished reading current buffer
						this.currentBuffer = null;

					} else {
						// Attempt to read from unpooled byte buffer
						ByteBuffer byteBuffer = this.currentBuffer.getUnpooledByteBuffer();

						// Determine if can read from byte buffer
						if (byteBuffer.remaining() > 0) {
							return byteToInt(byteBuffer.get());
						}

						// As here, finished reading current buffer
						this.currentBuffer = null;
					}
				}

				// Determine if further data
				if (!this.iterator.hasNext()) {
					return -1; // end of stream
				}

				// Obtain the next buffer to read
				StreamBuffer<byte[]> streamBuffer = this.iterator.next();
				this.currentBufferPosition = 0;

				// Ensure the buffer is released (as written HTTP response)
				Assert.assertTrue("Should only be mock buffer " + streamBuffer.getClass().getName(),
						streamBuffer instanceof AbstractMockStreamBuffer);
				this.currentBuffer = (AbstractMockStreamBuffer) streamBuffer;
				Assert.assertTrue("Mock buffer should be released", this.currentBuffer.isReleased);
			}
		}
	}

}