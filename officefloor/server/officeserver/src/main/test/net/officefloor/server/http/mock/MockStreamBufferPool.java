/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.http.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.ByteBufferFactory;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Mock {@link StreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockStreamBufferPool implements StreamBufferPool<ByteBuffer> {

	/**
	 * Releases the {@link StreamBuffer} instances.
	 * 
	 * @param headBuffer Head {@link StreamBuffer} of linked list of
	 *                   {@link StreamBuffer} instances.
	 */
	public static void releaseStreamBuffers(StreamBuffer<ByteBuffer> headBuffer) {
		while (headBuffer != null) {
			headBuffer.release();
			headBuffer = headBuffer.next;
		}
	}

	/**
	 * Creates an {@link InputStream} to the content of the {@link StreamBuffer}
	 * instances.
	 * 
	 * @param headBuffer Head {@link StreamBuffer} of linked list of
	 *                   {@link StreamBuffer} instances.
	 * @return {@link InputStream} to read the data from the {@link StreamBuffer}
	 *         instances.
	 */
	public static InputStream createInputStream(StreamBuffer<ByteBuffer> headBuffer) {
		return new MockBufferPoolInputStream(headBuffer);
	}

	/**
	 * Convenience method to obtain the contents of the buffers as a string.
	 * 
	 * @param headBuffer Head {@link StreamBuffer} of linked list of
	 *                   {@link StreamBuffer} instances.
	 * @param charset    {@link Charset} of underlying data.
	 * @return Content of buffers as string.
	 */
	public static String getContent(StreamBuffer<ByteBuffer> headBuffer, Charset charset) {
		try {
			InputStream inputStream = createInputStream(headBuffer);
			InputStreamReader reader = new InputStreamReader(inputStream, charset);
			StringWriter content = new StringWriter();
			for (int character = reader.read(); character != -1; character = reader.read()) {
				content.write(character);
			}
			content.flush();
			return content.toString();
		} catch (IOException ex) {
			return JUnitAgnosticAssert.fail(ex);
		}
	}

	/**
	 * Identifier for the next {@link StreamBuffer}.
	 */
	private final AtomicInteger nextBufferId = new AtomicInteger(0);

	/**
	 * {@link ByteBufferFactory}.
	 */
	private final ByteBufferFactory byteBufferFactory;

	/**
	 * Registered {@link AbstractMockStreamBuffer} instances.
	 */
	private final Deque<AbstractMockStreamBuffer> createdBuffers = new ConcurrentLinkedDeque<>();

	/**
	 * Instantiate with default buffer size for testing.
	 */
	public MockStreamBufferPool() {
		// small buffer to ensure handling filling buffer
		this(() -> ByteBuffer.allocate(3));
	}

	/**
	 * Instantiate.
	 * 
	 * @param byteBufferFactory {@link ByteBufferFactory}.
	 */
	public MockStreamBufferPool(ByteBufferFactory byteBufferFactory) {
		this.byteBufferFactory = byteBufferFactory;
	}

	/**
	 * Indicates if there are active {@link StreamBuffer} instances.
	 * 
	 * @return <code>true</code> if there are active {@link StreamBuffer} instances
	 *         not released back to this {@link StreamBufferPool}.
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
			JUnitAgnosticAssert.assertTrue(buffer.isReleased, "Buffer " + buffer.id + " (of "
					+ this.createdBuffers.size() + ") should be released" + buffer.getStackTrace());
		}
	}

	/*
	 * =============== BufferPool =======================
	 */

	@Override
	public StreamBuffer<ByteBuffer> getPooledStreamBuffer() {
		MockPooledStreamBuffer buffer = new MockPooledStreamBuffer(this.byteBufferFactory.createByteBuffer());
		this.createdBuffers.add(buffer);
		return buffer;
	}

	@Override
	public StreamBuffer<ByteBuffer> getUnpooledStreamBuffer(ByteBuffer byteBuffer) {
		MockUnpooledStreamBuffer buffer = new MockUnpooledStreamBuffer(byteBuffer);
		this.createdBuffers.add(buffer);
		return buffer;
	}

	@Override
	public StreamBuffer<ByteBuffer> getFileStreamBuffer(FileChannel file, long position, long count,
			FileCompleteCallback callback) {
		MockFileStreamBuffer buffer = new MockFileStreamBuffer(new FileBuffer(file, position, count, callback));
		this.createdBuffers.add(buffer);
		return buffer;
	}

	@Override
	public void close() {
		// Nothing to close
	}

	/**
	 * Abstract mock {@link StreamBuffer}.
	 */
	private abstract class AbstractMockStreamBuffer extends StreamBuffer<ByteBuffer> {

		/**
		 * Id of this {@link StreamBuffer}.
		 */
		private int id;

		/**
		 * Stack trace of the creation of this {@link StreamBuffer}.
		 */
		private StackTraceElement[] stackTrace;

		/**
		 * Indicates if released.
		 */
		private volatile boolean isReleased = false;

		/**
		 * Instantiate.
		 * 
		 * @param pooledBuffer       Pooled {@link ByteBuffer}.
		 * @param unpooledByteBuffer Unpooled {@link ByteBuffer}.
		 * @param fileBuffer         {@link FileBuffer}.
		 */
		public AbstractMockStreamBuffer(ByteBuffer pooledBuffer, ByteBuffer unpooledByteBuffer, FileBuffer fileBuffer) {
			super(pooledBuffer, unpooledByteBuffer, fileBuffer);
			this.id = MockStreamBufferPool.this.nextBufferId.getAndIncrement();

			// Obtain the stack trace to locate creation
			this.stackTrace = new Exception().getStackTrace();
		}

		/**
		 * Obtains the stack trace.
		 * 
		 * @return Stack trace.
		 */
		protected String getStackTrace() {
			StringBuilder trace = new StringBuilder();
			trace.append("\n\nStreamBuffer created at:\n");
			for (int i = 0; i < this.stackTrace.length; i++) {
				trace.append('\t');
				trace.append(this.stackTrace[i].toString());
				trace.append('\n');
			}
			return trace.toString();
		}

		@Override
		public void release() {
			JUnitAgnosticAssert.assertFalse(this.isReleased,
					"StreamBuffer " + this.id + " should only be released once" + this.getStackTrace());
			this.isReleased = true;
		}
	}

	/**
	 * Mock pooled {@link StreamBuffer}.
	 */
	private class MockPooledStreamBuffer extends AbstractMockStreamBuffer {

		/**
		 * Instantiate.
		 * 
		 * @param buffer {@link ByteBuffer}.
		 */
		private MockPooledStreamBuffer(ByteBuffer buffer) {
			super(buffer, null, null);
		}

		/*
		 * ================== PooledBuffer ======================
		 */

		@Override
		public boolean write(byte datum) {

			// Determine if buffer full
			if (this.pooledBuffer.remaining() == 0) {
				return false;
			}

			// Add the byte to the buffer
			this.pooledBuffer.put(datum);
			return true;
		}

		@Override
		public int write(byte[] data, int offset, int length) {

			// Obtain the length of data to write
			int writeLength = Math.min(length, this.pooledBuffer.remaining());

			// Write the data
			this.pooledBuffer.put(data, offset, writeLength);

			// Return the bytes written
			return writeLength;
		}
	}

	/**
	 * Mock unpooled {@link StreamBuffer}.
	 */
	private class MockUnpooledStreamBuffer extends AbstractMockStreamBuffer {

		/**
		 * Instantiate.
		 * 
		 * @param buffer Unpooled {@link ByteBuffer}.
		 */
		private MockUnpooledStreamBuffer(ByteBuffer buffer) {
			super(null, buffer, null);
		}

		/*
		 * =================== PooledBuffer ======================
		 */

		@Override
		public boolean write(byte datum) {
			return JUnitAgnosticAssert.fail(this.getClass().getSimpleName() + " is unpooled" + this.getStackTrace());
		}

		@Override
		public int write(byte[] data, int offset, int length) {
			return JUnitAgnosticAssert.fail(this.getClass().getSimpleName() + " is unpooled" + this.getStackTrace());
		}
	}

	/**
	 * Mock file {@link StreamBuffer}.
	 */
	private class MockFileStreamBuffer extends AbstractMockStreamBuffer {

		/**
		 * Instantiate.
		 * 
		 * @param buffer {@link FileBuffer}.
		 */
		private MockFileStreamBuffer(FileBuffer buffer) {
			super(null, null, buffer);
		}

		/*
		 * =================== PooledBuffer ======================
		 */

		@Override
		public boolean write(byte datum) {
			return JUnitAgnosticAssert.fail(this.getClass().getSimpleName() + " is file" + this.getStackTrace());
		}

		@Override
		public int write(byte[] data, int offset, int length) {
			return JUnitAgnosticAssert.fail(this.getClass().getSimpleName() + " is file" + this.getStackTrace());
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
		 * @param value Byte value.
		 * @return Int value.
		 */
		private static int byteToInt(byte value) {
			return value & 0xff;
		}

		/**
		 * Current {@link StreamBuffer} to read contents.
		 */
		private StreamBuffer<ByteBuffer> currentBuffer = null;

		/**
		 * Position to read next value from current pooled {@link StreamBuffer}
		 */
		private int currentBufferPosition = 0;

		/**
		 * Used to read batch content from files for improved performance.
		 */
		private ByteBuffer fileContent = ByteBuffer.allocate(1024);

		/**
		 * Instantiate.
		 * 
		 * @param headBuffer Head {@link StreamBuffer} of linked list of
		 *                   {@link StreamBuffer} instances.
		 */
		private MockBufferPoolInputStream(StreamBuffer<ByteBuffer> headBuffer) {
			this.currentBuffer = headBuffer;

			// Ensure nothing to read
			BufferJvmFix.position(this.fileContent, BufferJvmFix.limit(this.fileContent));
		}

		/*
		 * =================== InputStream ==========================
		 */

		@Override
		public int read() throws IOException {

			// Loop until read byte (or end of stream)
			for (;;) {

				// Determine if completed stream
				if (this.currentBuffer == null) {
					return -1; // end of stream
				}

				// Attempt to obtain value from current buffer
				if (this.currentBuffer.pooledBuffer != null) {
					// Obtain the pooled data
					ByteBuffer bufferData = this.currentBuffer.pooledBuffer;

					// Determine if can read data from buffer
					if (this.currentBufferPosition < BufferJvmFix.position(bufferData)) {
						// Read the data from the buffer
						return byteToInt(bufferData.get(this.currentBufferPosition++));
					}

				} else if (this.currentBuffer.unpooledByteBuffer != null) {
					// Attempt to read from unpooled byte buffer
					ByteBuffer byteBuffer = this.currentBuffer.unpooledByteBuffer;

					// Determine if can read from byte buffer
					if (byteBuffer.remaining() > 0) {
						return byteToInt(byteBuffer.get());
					}
				} else {
					// Attempt to read from file buffer
					FileBuffer fileBuffer = this.currentBuffer.fileBuffer;

					// Determine if can read data from buffer
					long count = (fileBuffer.count < 0) ? fileBuffer.file.size() : fileBuffer.count;
					if (this.currentBufferPosition < count) {
						// Read data from the buffer
						long position = fileBuffer.position + this.currentBufferPosition;
						this.currentBufferPosition++;

						// Handle batch read from file for performance
						if (!this.fileContent.hasRemaining()) {
							this.fileContent.clear();
							fileBuffer.file.read(this.fileContent, position);
							this.fileContent.flip();
						}
						return byteToInt(this.fileContent.get());
					}

					// As here, all file content read (so complete)
					if (fileBuffer.callback != null) {
						fileBuffer.callback.complete(fileBuffer.file, true);
					}
				}

				// Obtain the next buffer to read
				this.currentBuffer = this.currentBuffer.next;
				this.currentBufferPosition = 0;
				if (this.currentBuffer == null) {
					this.currentBuffer = null;
					return -1; // end of stream
				}
			}
		}
	}

}
