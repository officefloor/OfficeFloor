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
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.ByteBufferFactory;
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
	private final ThreadLocal<ThreadLocalPool> threadLocalPool = new ThreadLocal<>();

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
	private volatile int maxCorePoolSize;

	/**
	 * Core pool of {@link StreamBuffer} instances.
	 */
	private Deque<StreamBuffer<ByteBuffer>> corePool = new ConcurrentLinkedDeque<>();

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
	 * Activates {@link ThreadLocal} pooling of {@link StreamBuffer} on current
	 * {@link Thread}.
	 */
	public void activeThreadLocalPooling() {

		// Ensure only singleton on the thread
		if (this.threadLocalPool.get() == null) {
			this.threadLocalPool.set(new ThreadLocalPool());
		}
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
	private void releaseToCorePool(StreamBuffer<ByteBuffer> buffer) {

		// Determine if release (keep approximate core pool size)
		if (this.corePool.size() < this.maxCorePoolSize) {

			// Released to core pool
			this.corePool.push(buffer);
			return;
		}

		// Allow buffer to be garbage collected (too many buffers)
		this.bufferCount.decrementAndGet();
	}

	/**
	 * Creates a pooled {@link StreamBuffer}.
	 * 
	 * @return New pooled {@link StreamBuffer}.
	 */
	private StreamBuffer<ByteBuffer> createPooledStreamBuffer() {

		// Create and return new buffer
		ByteBuffer byteBuffer = this.byteBufferFactory.createByteBuffer();
		StreamBuffer<ByteBuffer> streamBuffer = new PooledStreamBuffer(byteBuffer);

		// Capture created buffer
		this.bufferCount.incrementAndGet();

		// Return the created buffer
		return streamBuffer;
	}

	/**
	 * =============== StreamBufferPool ===========================
	 */

	@Override
	public StreamBuffer<ByteBuffer> getPooledStreamBuffer() {

		// Attempt to obtain from thread local pool
		ThreadLocalPool pool = threadLocalPool.get();
		if (pool != null) {
			if (pool.threadHead != null) {
				// Obtain from thread pool
				StreamBuffer<ByteBuffer> pooledBuffer = pool.threadHead;
				pool.threadHead = pool.threadHead.next;
				pool.threadPoolSize--;

				// Clear buffer, so reset for use
				BufferJvmFix.clear(pooledBuffer.pooledBuffer);
				pooledBuffer.next = null;

				// Use the thread local buffer
				return pooledBuffer;
			}
		}

		// No thread buffers, so attempt core pool
		StreamBuffer<ByteBuffer> pooledBuffer = this.corePool.poll();
		if (pooledBuffer != null) {

			// Clear buffer, so reset for use
			BufferJvmFix.clear(pooledBuffer.pooledBuffer);
			pooledBuffer.next = null;

			// Use the core pool buffer
			return pooledBuffer;
		}

		// Create new buffer
		return this.createPooledStreamBuffer();
	}

	@Override
	public void close() {

		// Thread local pools clean on thread exit, so avoid going to core
		this.maxCorePoolSize = 0;

		// Release the core pool
		while (this.corePool.size() > 0) {
			for (Iterator<StreamBuffer<ByteBuffer>> iterator = this.corePool.iterator(); iterator.hasNext();) {
				iterator.next().release();
				iterator.remove();
			}
		}
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
	public void threadComplete() {

		// Obtain the thread pool
		ThreadLocalPool pool = this.threadLocalPool.get();
		if (pool == null) {
			return; // no thread local pool to clean up
		}

		// Release all to pool
		StreamBuffer<ByteBuffer> buffer = pool.threadHead;
		while (buffer != null) {

			// Obtain release buffer (must obtain next, as release sets next)
			StreamBuffer<ByteBuffer> release = buffer;
			buffer = buffer.next;

			// Release the buffer
			this.releaseToCorePool(release);
		}

		// Remove from thread local pooling
		this.threadLocalPool.remove();
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
			@SuppressWarnings("resource")
			ThreadLocalStreamBufferPool bufferPool = ThreadLocalStreamBufferPool.this;

			// Attempt to release to thread local pool
			ThreadLocalPool pool = bufferPool.threadLocalPool.get();
			if (pool != null) {
				if (pool.threadPoolSize < bufferPool.maxThreadLocalPoolSize) {
					// Release to thread pool
					this.next = pool.threadHead;
					pool.threadHead = this;
					pool.threadPoolSize++;
					return; // released
				}
			}

			// As here, release to core pool
			bufferPool.releaseToCorePool(this);
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
