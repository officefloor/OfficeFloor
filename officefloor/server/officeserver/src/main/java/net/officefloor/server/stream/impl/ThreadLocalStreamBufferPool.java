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

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.ByteBufferFactory;
import net.officefloor.server.stream.ServerMemoryOverloadHandler;
import net.officefloor.server.stream.ServerMemoryOverloadedException;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * {@link StreamBufferPool} of {@link ByteBuffer} instances that utilises
 * {@link ThreadLocal} caches for performance.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalStreamBufferPool extends AbstractStreamBufferPool<ByteBuffer>
		implements ThreadCompletionListenerFactory, ThreadCompletionListener {

	/**
	 * {@link ThreadLocalPool}.
	 */
	private final ThreadLocal<ThreadLocalPool> threadLocalPool = new ThreadLocal<ThreadLocalPool>() {
		@Override
		protected ThreadLocalPool initialValue() {
			return new ThreadLocalPool();
		}
	};

	/**
	 * Number of {@link StreamBuffer} instances in circulation.
	 */
	private final AtomicInteger bufferCount = new AtomicInteger(0);

	/**
	 * {@link ByteBufferFactory}.
	 */
	private final ByteBufferFactory byteBufferFactory;

	/**
	 * Maximum {@link ThreadLocal} pool size.
	 */
	private final int maxThreadLocalPoolSize;

	/**
	 * Maximum core pool size.
	 */
	private final int maxCorePoolSize;

	/**
	 * Head {@link StreamBuffer} within the core pool.
	 */
	private StreamBuffer<ByteBuffer> coreHead = null;

	/**
	 * Core pool size.
	 */
	private int corePoolSize = 0;

	/**
	 * <p>
	 * Instantiate with details of pool sizes.
	 * <p>
	 * The total potential amount of memory used is:
	 * <p>
	 * <code>pooledByteBufferSize</code> * (<code>active buffers</code> +
	 * (<code>threadLocalPoolSize</code> * <code>active threads</code>) +
	 * <code>corePoolSize</code>).
	 * 
	 * @param byteBufferFactory      {@link ByteBufferFactory}.
	 * @param maxThreadLocalPoolSize Maximum {@link ThreadLocal} pool size.
	 * @param maxCorePoolSize        Maximum core pool size.
	 */
	public ThreadLocalStreamBufferPool(ByteBufferFactory byteBufferFactory, int maxThreadLocalPoolSize,
			int maxCorePoolSize) {
		this.byteBufferFactory = byteBufferFactory;
		this.maxThreadLocalPoolSize = maxThreadLocalPoolSize;
		this.maxCorePoolSize = maxCorePoolSize;
	}

	/**
	 * Obtains the number of {@link StreamBuffer} instances in circulation.
	 * 
	 * @return Number of {@link StreamBuffer} instances in circulation.
	 */
	public int getStreamBufferCount() {
		return this.bufferCount.get();
	}

	/**
	 * Releases the {@link StreamBuffer} to the core pool.
	 * 
	 * @param buffer {@link StreamBuffer}.
	 */
	private void unsafeReleaseToCorePool(StreamBuffer<ByteBuffer> buffer) {

		// Determine if release
		if (this.corePoolSize < this.maxCorePoolSize) {

			// Released to core pool
			buffer.next = this.coreHead;
			this.coreHead = buffer;
			this.corePoolSize++;
			return;
		}

		// Allow buffer to be garbage collected (too many buffers)
		this.bufferCount.decrementAndGet();
	}

	/**
	 * {@link Thread} safe release to core pool.
	 * 
	 * @param buffer {@link StreamBuffer}.
	 */
	private synchronized void safeReleaseToCorePool(StreamBuffer<ByteBuffer> buffer) {
		this.unsafeReleaseToCorePool(buffer);
	}

	/**
	 * Obtains a {@link StreamBuffer} from the core pool.
	 * 
	 * @return {@link StreamBuffer} or <code>null</code> if core pool is empty.
	 */
	private synchronized StreamBuffer<ByteBuffer> getCorePoolBuffer() {

		// Determine if buffers in the core pool
		if (this.corePoolSize <= 0) {
			return null; // empty pool
		}

		// Obtain the buffer from core pool
		StreamBuffer<ByteBuffer> buffer = this.coreHead;
		this.coreHead = this.coreHead.next;
		this.corePoolSize--;

		// Return the buffer
		return buffer;
	}

	/**
	 * Creates a pooled {@link StreamBuffer}.
	 * 
	 * @return New pooled {@link StreamBuffer}.
	 */
	private StreamBuffer<ByteBuffer> createPooledStreamBuffer() {

		// Capture created buffer
		this.bufferCount.incrementAndGet();

		// Create and return new buffer
		ByteBuffer byteBuffer = this.byteBufferFactory.createByteBuffer();
		return new PooledStreamBuffer(byteBuffer);
	}

	/**
	 * =============== StreamBufferPool ===========================
	 */

	@Override
	public StreamBuffer<ByteBuffer> getPooledStreamBuffer(ServerMemoryOverloadHandler serverMemoryOverloadedHandler)
			throws ServerMemoryOverloadedException {

		// Obtain the stream buffer
		StreamBuffer<ByteBuffer> pooledBuffer = null;

		// Attempt to obtain from thread local pool
		ThreadLocalPool pool = threadLocalPool.get();
		if (pool.threadHead != null) {
			// Obtain from thread pool
			pooledBuffer = pool.threadHead;
			pool.threadHead = pool.threadHead.next;
			pool.threadPoolSize--;

		} else {
			// No thread buffers, so attempt core pool
			pooledBuffer = this.getCorePoolBuffer();
		}

		// Ensure have a buffer
		if (pooledBuffer == null) {
			// Create new buffer
			pooledBuffer = this.createPooledStreamBuffer();

		} else {
			// Pooled buffer, so reset for use
			BufferJvmFix.clear(pooledBuffer.pooledBuffer);
			pooledBuffer.next = null;
		}

		// Return the pooled buffer
		return pooledBuffer;
	}

	/**
	 * ============= ThreadCompletionListenerFactory =========
	 */

	@Override
	public ThreadCompletionListener createThreadCompletionListener(ManagedObjectPool pool) {
		return this;
	}

	/**
	 * ================= ThreadCompletionListener ============
	 */

	@Override
	public synchronized void threadComplete() {

		// Obtain the thread pool
		ThreadLocalPool pool = threadLocalPool.get();

		// Release all to pool
		StreamBuffer<ByteBuffer> buffer = pool.threadHead;
		while (buffer != null) {

			// Obtain release buffer (must obtain next, as release sets next)
			StreamBuffer<ByteBuffer> release = buffer;
			buffer = buffer.next;

			// Release the buffer
			this.unsafeReleaseToCorePool(release);
		}
	}

	/**
	 * Pooled {@link StreamBuffer}.
	 */
	private class PooledStreamBuffer extends StreamBuffer<ByteBuffer> {

		/**
		 * Instantiate.
		 * 
		 * @param byteBuffer {@link ByteBuffer}.
		 */
		private PooledStreamBuffer(ByteBuffer byteBuffer) {
			super(byteBuffer, null, null);
		}

		/*
		 * ================= StreamBuffer ========================
		 */

		@Override
		public boolean write(byte datum) {

			// Ensure space write data
			if (this.pooledBuffer.remaining() <= 0) {
				return false; // buffer is full
			}

			// Write the data
			this.pooledBuffer.put(datum);
			return true;
		}

		@Override
		public int write(byte[] data, int offset, int length) {

			// Obtain the bytes to write
			int writeBytes = Math.min(length, this.pooledBuffer.remaining());

			// Write the bytes
			this.pooledBuffer.put(data, offset, writeBytes);

			// Return the number of written bytes
			return writeBytes;
		}

		@Override
		public void release() {

			// Easy access to pool
			ThreadLocalStreamBufferPool bufferPool = ThreadLocalStreamBufferPool.this;

			// Obtain the thread local pool
			ThreadLocalPool pool = threadLocalPool.get();

			// Attempt to release to thread local pool
			if (pool.threadPoolSize < bufferPool.maxThreadLocalPoolSize) {
				// Release to thread pool
				this.next = pool.threadHead;
				pool.threadHead = this;
				pool.threadPoolSize++;
				return; // released
			}

			// As here, release to core pool
			bufferPool.safeReleaseToCorePool(this);
		}
	}

	/**
	 * {@link Thread} local pool of {@link StreamBuffer} instances.
	 */
	private static class ThreadLocalPool {

		/**
		 * Head {@link StreamBuffer} to linked list of {@link StreamBuffer} instances.
		 */
		private StreamBuffer<ByteBuffer> threadHead = null;

		/**
		 * Number of {@link StreamBuffer} instances within this pool.
		 */
		private int threadPoolSize = 0;
	}

}
