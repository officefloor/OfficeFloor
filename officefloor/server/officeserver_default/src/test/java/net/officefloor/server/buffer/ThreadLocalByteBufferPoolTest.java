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
package net.officefloor.server.buffer;

import java.nio.ByteBuffer;

import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link ThreadLocalByteBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalByteBufferPoolTest extends OfficeFrameTestCase {

	/**
	 * Size of the pooled {@link ByteBuffer} instances.
	 */
	private static final int BUFFER_SIZE = 4;

	/**
	 * Pool size of {@link ThreadLocal} pool before returning
	 * {@link StreamBuffer} to core pool.
	 */
	private static final int THREAD_LOCAL_POOL_SIZE = 1;

	/**
	 * Pool size of the core pool before allowing pooled {@link StreamBuffer}
	 * instances to be garbage collected.
	 */
	private static final int CORE_POOL_SIZE = 2;

	/**
	 * {@link ThreadLocalByteBufferPool} to test.
	 */
	private final ThreadLocalByteBufferPool pool = new ThreadLocalByteBufferPool(() -> ByteBuffer.allocate(BUFFER_SIZE),
			THREAD_LOCAL_POOL_SIZE, CORE_POOL_SIZE);

	/**
	 * Obtains the unpooled {@link StreamBuffer}.
	 */
	public void testGetUnpooledBuffer() {

		// Obtain the unpooled buffer
		ByteBuffer content = ByteBuffer.wrap(new byte[] { 1 });
		StreamBuffer<ByteBuffer> buffer = this.pool.getUnpooledStreamBuffer(content);

		// Ensure have buffer
		assertNotNull("Should have buffer", buffer);
		assertFalse("Should be unpooled", buffer.isPooled());

		// Ensure correct byte buffer
		assertSame("Incorrect byte buffer", content, buffer.getUnpooledByteBuffer());
	}

	/**
	 * Obtains a pooled {@link StreamBuffer}.
	 */
	public void testGetPooledBuffer() {

		// Ensure can obtain a byte buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Ensure have buffer
		assertNotNull("Should have buffer", buffer);
		assertTrue("Should be pooled", buffer.isPooled());

		// Ensure have correct byte buffer by size
		ByteBuffer content = buffer.getPooledBuffer();
		assertEquals("Incorrect capacity", BUFFER_SIZE, content.capacity());
		assertEquals("Retrieved buffer should be ready to use", 0, content.position());
		assertEquals("Should have full use of buffer", BUFFER_SIZE, content.remaining());
	}

	/**
	 * Obtains a pooled {@link StreamBuffer}, releases it and ensures a clean
	 * {@link StreamBuffer} is retrieved.
	 */
	public void testReleaseGetPooledBuffer() {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> original = this.pool.getPooledStreamBuffer();

		// Write content to the buffer
		ByteBuffer originalContent = original.getPooledBuffer();
		original.getPooledBuffer().put((byte) 1);
		assertEquals("Should have written data", 1, originalContent.position());

		// Release the buffer back to pool
		original.release();

		// Obtain another buffer from pool
		StreamBuffer<ByteBuffer> another = this.pool.getPooledStreamBuffer();
		assertSame("Should be same buffer returned", original, another);

		// Ensure buffer is ready to use
		ByteBuffer anotherContent = another.getPooledBuffer();
		assertSame("Should same byte buffer", originalContent, anotherContent);
		assertEquals("Buffer should be ready to use", 0, anotherContent.position());
		assertEquals("Should have full use of buffer", BUFFER_SIZE, anotherContent.remaining());
	}

	/**
	 * Ensure can get and release and obtain a large number of
	 * {@link StreamBuffer} instances to ensure pooling.
	 */
	@SuppressWarnings("unchecked")
	public void testGetReleaseLargeNumberOfBuffers() {

		final int RETRIEVE_NUMBER = (THREAD_LOCAL_POOL_SIZE + CORE_POOL_SIZE) * 2;

		StreamBuffer<ByteBuffer>[] buffers = new StreamBuffer[RETRIEVE_NUMBER];

		// Retrieve the buffers (released to thread local pool)
		StreamBuffer<ByteBuffer>[] threadLocalBuffers = new StreamBuffer[THREAD_LOCAL_POOL_SIZE];
		for (int i = 0; i < threadLocalBuffers.length; i++) {
			threadLocalBuffers[i] = this.pool.getPooledStreamBuffer();
			buffers[i] = threadLocalBuffers[i];
		}

		// Retrieve the buffers (released to core pool)
		StreamBuffer<ByteBuffer>[] coreBuffers = new StreamBuffer[CORE_POOL_SIZE];
		for (int i = 0; i < coreBuffers.length; i++) {
			coreBuffers[i] = this.pool.getPooledStreamBuffer();
			buffers[THREAD_LOCAL_POOL_SIZE + i] = coreBuffers[i];
		}

		// Retrieve the remaining buffers
		for (int i = (THREAD_LOCAL_POOL_SIZE + CORE_POOL_SIZE); i < buffers.length; i++) {
			buffers[i] = this.pool.getPooledStreamBuffer();
		}

		// Release all the buffers
		for (int i = 0; i < buffers.length; i++) {
			buffers[i].release();
		}

		// Retrieve the buffers again
		StreamBuffer<ByteBuffer>[] reuse = new StreamBuffer[RETRIEVE_NUMBER];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = this.pool.getPooledStreamBuffer();
		}

		// First buffers should be thread pool buffers (popped off in reverse)
		for (int i = 0; i < threadLocalBuffers.length; i++) {
			assertSame("Incorrect thread local buffer " + i, reuse[i],
					threadLocalBuffers[THREAD_LOCAL_POOL_SIZE - 1 - i]);
		}

		// Second set of buffers should be core pool (popped off in reverse)
		for (int i = 0; i < coreBuffers.length; i++) {
			assertSame("Incorrect core buffer " + i, reuse[THREAD_LOCAL_POOL_SIZE + i],
					coreBuffers[CORE_POOL_SIZE - 1 - i]);
		}

		// Remaining buffers should be new buffers
	}

	/**
	 * Ensure return {@link ThreadLocal} {@link StreamBuffer} to core pool on
	 * {@link Thread} completion.
	 */
	@SuppressWarnings("unchecked")
	public void testThreadComplete() {

		final int RETRIEVE_NUMBER = THREAD_LOCAL_POOL_SIZE + 1;

		StreamBuffer<ByteBuffer>[] buffers = new StreamBuffer[RETRIEVE_NUMBER];

		// Retrieve the buffers (released to thread local pool)
		StreamBuffer<ByteBuffer>[] threadLocalBuffers = new StreamBuffer[THREAD_LOCAL_POOL_SIZE];
		for (int i = 0; i < threadLocalBuffers.length; i++) {
			threadLocalBuffers[i] = this.pool.getPooledStreamBuffer();
			buffers[i] = threadLocalBuffers[i];
		}

		// Obtain additional buffer (release to core pool)
		StreamBuffer<ByteBuffer> coreBuffer = this.pool.getPooledStreamBuffer();
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
			reuse[i] = this.pool.getPooledStreamBuffer();
		}

		// Pop off the thread local released to core first
		for (int i = 0; i < THREAD_LOCAL_POOL_SIZE; i++) {
			assertSame("Thread local popped off core first after completion",
					threadLocalBuffers[THREAD_LOCAL_POOL_SIZE - 1 - i], reuse[i]);
		}

		// Ensure last is core popped off buffer
		assertSame("Core is first after thread completion", coreBuffer, reuse[THREAD_LOCAL_POOL_SIZE]);
	}

	/**
	 * Ensure can write to {@link ByteBuffer} through {@link StreamBuffer} write
	 * facade.
	 */
	public void testWriteByte() {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();
		ByteBuffer content = buffer.getPooledBuffer();

		// Write content to the buffer
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertTrue("Should be able to write byte " + i, buffer.write((byte) (i + 1)));
			assertEquals("Should be moving position forward", i + 1, content.position());
		}
		assertEquals("Buffer should be full", 0, content.remaining());

		// Should no longer be able to write to buffer
		assertFalse("Buffer should be full", buffer.write((byte) BUFFER_SIZE));

		// Ensure data in buffer
		content.flip();
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals("Incorrect byte " + i, i + 1, content.get());
		}
	}

	/**
	 * Ensure can write byte array through {@link StreamBuffer} write facade.
	 */
	public void testWriteBytes() {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();
		ByteBuffer content = buffer.getPooledBuffer();

		// Write the content to the buffer
		byte[] data = new byte[BUFFER_SIZE];
		for (int i = 0; i < BUFFER_SIZE; i++) {
			data[i] = (byte) (i + 1);
		}
		assertEquals("Should be able to fill buffer", BUFFER_SIZE, buffer.write(data));
		assertEquals("Buffer should be full", 0, content.remaining());

		// Attempt to write again
		assertEquals("Buffer should be full", 0, buffer.write(data));

		// Ensure data in buffer
		content.flip();
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals("Incorrect byte " + i, i + 1, content.get());
		}
	}

	/**
	 * Ensure can write partial bytes.
	 */
	public void testWritePartialBytes() {

		// Obtain the buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();
		ByteBuffer content = buffer.getPooledBuffer();

		// Create content to write
		byte datum = 1;
		byte[] data = new byte[BUFFER_SIZE * 2];
		for (int i = 0; i < BUFFER_SIZE; i++) {
			data[i] = (byte) (i + 2);
		}

		// Write the content
		assertTrue("Should write byte", buffer.write(datum));
		assertEquals("Shoud fill remaining bytes", BUFFER_SIZE - 1, buffer.write(data));

		// Ensure content written
		content.flip();
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals("Incrrect byte " + i, i + 1, content.get());
		}
	}

}