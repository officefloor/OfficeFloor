/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.stream.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.junit.jupiter.api.Test;

import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.ServerMemoryOverloadHandler;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;

/**
 * Tests the {@link ThreadLocalStreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalStreamBufferPoolTest {

	/**
	 * {@link ServerMemoryOverloadHandler}.
	 */
	private static final ServerMemoryOverloadHandler OVERLOAD_HANDLER = () -> fail("Server should not be overloaded");

	/**
	 * Size of the pooled {@link ByteBuffer} instances.
	 */
	private static final int BUFFER_SIZE = 4;

	/**
	 * Pool size of {@link ThreadLocal} pool before returning {@link StreamBuffer}
	 * to core pool.
	 */
	private static final int THREAD_LOCAL_POOL_SIZE = 1;

	/**
	 * Pool size of the core pool before allowing pooled {@link StreamBuffer}
	 * instances to be garbage collected.
	 */
	private static final int CORE_POOL_SIZE = 2;

	/**
	 * {@link ThreadLocalStreamBufferPool} to test.
	 */
	private final ThreadLocalStreamBufferPool pool = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(BUFFER_SIZE), THREAD_LOCAL_POOL_SIZE, CORE_POOL_SIZE);

	/**
	 * Obtains the unpooled {@link StreamBuffer}.
	 */
	@Test
	public void getUnpooledBuffer() {

		// Obtain the unpooled buffer
		ByteBuffer content = ByteBuffer.wrap(new byte[] { 1 });
		StreamBuffer<ByteBuffer> buffer = this.pool.getUnpooledStreamBuffer(content);

		// Ensure have buffer
		assertNotNull(buffer, "Should have buffer");
		assertNotNull(buffer.unpooledByteBuffer, "Should be unpooled");

		// Ensure correct byte buffer
		assertSame(content, buffer.unpooledByteBuffer, "Incorrect byte buffer");
	}

	/**
	 * Obtains a pooled {@link StreamBuffer}.
	 */
	@Test
	public void getPooledBuffer() throws IOException {

		// Ensure can obtain a byte buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);

		// Ensure have buffer
		assertNotNull(buffer, "Should have buffer");
		assertNotNull(buffer.pooledBuffer, "Should be pooled");

		// Ensure have correct byte buffer by size
		ByteBuffer content = buffer.pooledBuffer;
		assertEquals(BUFFER_SIZE, content.capacity(), "Incorrect capacity");
		assertEquals(0, BufferJvmFix.position(content), "Retrieved buffer should be ready to use");
		assertEquals(BUFFER_SIZE, content.remaining(), "Should have full use of buffer");
	}

	/**
	 * Obtains a file {@link StreamBuffer}.
	 */
	@Test
	public void getFileBuffer() throws IOException {

		// Ensure can obtain a byte buffer
		FileChannel file = TemporaryFiles.getDefault().createTempFile("testGetFileBuffer", "test");
		FileCompleteCallback callback = (completedFile, isWritten) -> {
		};
		StreamBuffer<ByteBuffer> buffer = this.pool.getFileStreamBuffer(file, 0, -1, callback);

		// Ensure have buffer
		assertNotNull(buffer, "Should have buffer");
		assertNotNull(buffer.fileBuffer, "Should be file");

		// Ensure have correct file buffer by size
		FileBuffer content = buffer.fileBuffer;
		assertSame(file, content.file, "Incorrect file");
		assertEquals(0, content.position, "Incorrect position");
		assertEquals(-1, content.count, "Incorrect count");
		assertSame(callback, content.callback, "Incorrect callback");
	}

	/**
	 * Ensure same {@link StreamBuffer} returned after release.
	 */
	@Test
	public void threadLocalRecycle() throws IOException {
		final StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		buffer.release();
		assertSame(buffer, this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER), "Should obtain buffer just released");
	}

	/**
	 * Ensure same {@link StreamBuffer} returned after release (to core pool).
	 */
	@Test
	public void coreRecycle() throws IOException {
		final StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		buffer.release();
		this.pool.createThreadCompletionListener(null).threadComplete();
		assertSame(buffer, this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER), "Should obtain buffer just released");
	}

	/**
	 * Obtains a pooled {@link StreamBuffer}, releases it and ensures a clean
	 * {@link StreamBuffer} is retrieved.
	 */
	@Test
	public void releaseGetPooledBuffer() throws IOException {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> original = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);

		// Write content to the buffer
		ByteBuffer originalContent = original.pooledBuffer;
		original.pooledBuffer.put((byte) 1);
		assertEquals(1, BufferJvmFix.position(originalContent), "Should have written data");

		// Release the buffer back to pool
		original.release();

		// Obtain another buffer from pool
		StreamBuffer<ByteBuffer> another = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		assertSame(original, another, "Should be same buffer returned");

		// Ensure buffer is ready to use
		ByteBuffer anotherContent = another.pooledBuffer;
		assertSame(originalContent, anotherContent, "Should same byte buffer");
		assertEquals(0, BufferJvmFix.position(anotherContent), "Buffer should be ready to use");
		assertEquals(BUFFER_SIZE, anotherContent.remaining(), "Should have full use of buffer");
	}

	/**
	 * Ensure can get and release and obtain a large number of {@link StreamBuffer}
	 * instances to ensure pooling.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void getReleaseLargeNumberOfBuffers() throws IOException {

		final int RETRIEVE_NUMBER = (THREAD_LOCAL_POOL_SIZE + CORE_POOL_SIZE) * 2;

		StreamBuffer<ByteBuffer>[] buffers = new StreamBuffer[RETRIEVE_NUMBER];

		// Retrieve the buffers (released to thread local pool)
		StreamBuffer<ByteBuffer>[] threadLocalBuffers = new StreamBuffer[THREAD_LOCAL_POOL_SIZE];
		for (int i = 0; i < threadLocalBuffers.length; i++) {
			threadLocalBuffers[i] = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
			buffers[i] = threadLocalBuffers[i];
		}

		// Retrieve the buffers (released to core pool)
		StreamBuffer<ByteBuffer>[] coreBuffers = new StreamBuffer[CORE_POOL_SIZE];
		for (int i = 0; i < coreBuffers.length; i++) {
			coreBuffers[i] = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
			buffers[THREAD_LOCAL_POOL_SIZE + i] = coreBuffers[i];
		}

		// Retrieve the remaining buffers
		for (int i = (THREAD_LOCAL_POOL_SIZE + CORE_POOL_SIZE); i < buffers.length; i++) {
			buffers[i] = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		}

		// Release all the buffers
		for (int i = 0; i < buffers.length; i++) {
			buffers[i].release();
		}

		// Retrieve the buffers again
		StreamBuffer<ByteBuffer>[] reuse = new StreamBuffer[RETRIEVE_NUMBER];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		}

		// First buffers should be thread pool buffers (popped off in reverse)
		for (int i = 0; i < threadLocalBuffers.length; i++) {
			assertSame(reuse[i], threadLocalBuffers[THREAD_LOCAL_POOL_SIZE - 1 - i],
					"Incorrect thread local buffer " + i);
		}

		// Second set of buffers should be core pool (popped off in reverse)
		for (int i = 0; i < coreBuffers.length; i++) {
			assertSame(reuse[THREAD_LOCAL_POOL_SIZE + i], coreBuffers[CORE_POOL_SIZE - 1 - i],
					"Incorrect core buffer " + i);
		}

		// Remaining buffers should be new buffers
	}

	/**
	 * Ensure return {@link ThreadLocal} {@link StreamBuffer} to core pool on
	 * {@link Thread} completion.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void threadComplete() throws IOException {

		final int RETRIEVE_NUMBER = THREAD_LOCAL_POOL_SIZE + 1;

		StreamBuffer<ByteBuffer>[] buffers = new StreamBuffer[RETRIEVE_NUMBER];

		// Retrieve the buffers (released to thread local pool)
		StreamBuffer<ByteBuffer>[] threadLocalBuffers = new StreamBuffer[THREAD_LOCAL_POOL_SIZE];
		for (int i = 0; i < threadLocalBuffers.length; i++) {
			threadLocalBuffers[i] = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
			buffers[i] = threadLocalBuffers[i];
		}

		// Obtain additional buffer (release to core pool)
		StreamBuffer<ByteBuffer> coreBuffer = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		buffers[buffers.length - 1] = coreBuffer;

		// Release all the buffers
		for (int i = 0; i < buffers.length; i++) {
			buffers[i].release();
		}

		// Complete the thread (having thread local buffers added to core pool)
		ThreadCompletionListener completionListener = this.pool.createThreadCompletionListener(null);
		completionListener.threadComplete();

		// Ensure now on retrieving the thread local are after core
		StreamBuffer<ByteBuffer>[] reuse = new StreamBuffer[RETRIEVE_NUMBER];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		}

		// Pop off the thread local released to core first
		for (int i = 0; i < THREAD_LOCAL_POOL_SIZE; i++) {
			assertSame(threadLocalBuffers[THREAD_LOCAL_POOL_SIZE - 1 - i], reuse[i],
					"Thread local popped off core first after completion");
		}

		// Ensure last is core popped off buffer
		assertSame(coreBuffer, reuse[THREAD_LOCAL_POOL_SIZE], "Core is first after thread completion");
	}

	/**
	 * Ensure can write to {@link ByteBuffer} through {@link StreamBuffer} write
	 * facade.
	 */
	@Test
	public void writeByte() throws IOException {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		ByteBuffer content = buffer.pooledBuffer;

		// Write content to the buffer
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertTrue(buffer.write((byte) (i + 1)), "Should be able to write byte " + i);
			assertEquals(i + 1, BufferJvmFix.position(content), "Should be moving position forward");
		}
		assertEquals(0, content.remaining(), "Buffer should be full");

		// Should no longer be able to write to buffer
		assertFalse(buffer.write((byte) BUFFER_SIZE), "Buffer should be full");

		// Ensure data in buffer
		BufferJvmFix.flip(content);
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i + 1, content.get(), "Incorrect byte " + i);
		}
	}

	/**
	 * Ensure can write byte array through {@link StreamBuffer} write facade.
	 */
	@Test
	public void writeBytes() throws IOException {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		ByteBuffer content = buffer.pooledBuffer;

		// Write the content to the buffer
		byte[] data = new byte[BUFFER_SIZE];
		for (int i = 0; i < BUFFER_SIZE; i++) {
			data[i] = (byte) (i + 1);
		}
		assertEquals(BUFFER_SIZE, buffer.write(data), "Should be able to fill buffer");
		assertEquals(0, content.remaining(), "Buffer should be full");

		// Attempt to write again
		assertEquals(0, buffer.write(data), "Buffer should be full");

		// Ensure data in buffer
		BufferJvmFix.flip(content);
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i + 1, content.get(), "Incorrect byte " + i);
		}
	}

	/**
	 * Ensure can write partial bytes.
	 */
	@Test
	public void writePartialBytes() throws IOException {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		ByteBuffer content = buffer.pooledBuffer;

		// Create content to write
		byte datum = 1;
		byte[] data = new byte[BUFFER_SIZE * 2];
		for (int i = 0; i < BUFFER_SIZE; i++) {
			data[i] = (byte) (i + 2);
		}

		// Write the content
		assertTrue(buffer.write(datum), "Should write byte");
		assertEquals(BUFFER_SIZE - 1, buffer.write(data), "Shoud fill remaining bytes");

		// Ensure content written
		BufferJvmFix.flip(content);
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i + 1, content.get(), "Incrrect byte " + i);
		}
	}

}
