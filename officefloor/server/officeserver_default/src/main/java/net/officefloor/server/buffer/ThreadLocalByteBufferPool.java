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
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.AbstractStreamBufferPool;
import net.officefloor.server.stream.ByteBufferFactory;
import net.officefloor.server.stream.StreamBuffer;

/**
 * {@link StreamBufferPool} of {@link ByteBuffer} instances that utilises
 * {@link ThreadLocal} caches for performance.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalByteBufferPool extends AbstractStreamBufferPool<ByteBuffer>
		implements ThreadCompletionListenerFactory, ThreadCompletionListener {

	/**
	 * {@link ThreadLocal} pool.
	 */
	private final ThreadLocal<List<StreamBuffer<ByteBuffer>>> threadLocalPool = new ThreadLocal<List<StreamBuffer<ByteBuffer>>>() {
		@Override
		protected List<StreamBuffer<ByteBuffer>> initialValue() {
			/*
			 * Note: growing the array will occur when many buffers are being
			 * released. This typically occurs when low request load, hence
			 * there should be typically more CPU available to handle this.
			 * 
			 * Therefore, keep memory smaller (especially when thread completion
			 * listener tidies up a thread without requiring buffers).
			 */
			return new ArrayList<>();
		}
	};

	/**
	 * Core pool.
	 */
	private final List<StreamBuffer<ByteBuffer>> corePool = new ArrayList<>();

	/**
	 * {@link ByteBufferFactory}.
	 */
	private final ByteBufferFactory byteBufferFactory;

	/**
	 * {@link ThreadLocal} pool size.
	 */
	private final int threadLocalPoolSize;

	/**
	 * Core pool size.
	 */
	private final int corePoolSize;

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
	 * @param byteBufferFactory
	 *            {@link ByteBufferFactory}.
	 * @param threadLocalPoolSize
	 *            {@link ThreadLocal} pool size.
	 * @param corePoolSize
	 *            Core pool size.
	 */
	public ThreadLocalByteBufferPool(ByteBufferFactory byteBufferFactory, int threadLocalPoolSize, int corePoolSize) {
		this.byteBufferFactory = byteBufferFactory;
		this.threadLocalPoolSize = threadLocalPoolSize;
		this.corePoolSize = corePoolSize;
	}

	/**
	 * Releases the {@link StreamBuffer} to the core pool.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer}.
	 */
	private synchronized void releaseToCorePool(StreamBuffer<ByteBuffer> buffer) {

		// Determine if release
		if (this.corePool.size() < this.corePoolSize) {
			this.corePool.add(buffer);
			return; // released to core buffer
		}

		// Allow buffer to be garbage collected
	}

	/**
	 * Releases the {@link List} of {@link StreamBuffer} to the core pool.
	 * 
	 * @param buffers
	 *            {@link List} of {@link StreamBuffer} instances to release.
	 */
	private synchronized void releaseAllToCorePool(List<StreamBuffer<ByteBuffer>> buffers) {

		// Obtain number to release
		int numberToRelease = Math.min(this.corePoolSize - this.corePool.size(), buffers.size());

		// Release the buffers to the pool
		for (int i = 0; i < numberToRelease; i++) {
			this.corePool.add(buffers.get(i));
		}

		// Remaining buffers to be garbage collected
	}

	/**
	 * Obtains a {@link StreamBuffer} from the core pool.
	 * 
	 * @return {@link StreamBuffer} or <code>null</code> if core pool is empty.
	 */
	private synchronized StreamBuffer<ByteBuffer> getCorePoolBuffer() {

		// Determine if buffers in the core pool
		int poolSize = this.corePool.size();
		if (poolSize <= 0) {
			return null; // empty pool
		}

		// Return the last in core pool (avoids array copies)
		return this.corePool.remove(poolSize - 1);
	}

	/**
	 * =============== BufferPool ===========================
	 */

	@Override
	public StreamBuffer<ByteBuffer> getPooledStreamBuffer() {

		// Obtain the stream buffer
		StreamBuffer<ByteBuffer> pooledBuffer = null;

		// Attempt to obtain from thread local pool
		List<StreamBuffer<ByteBuffer>> threadLocalPool = this.threadLocalPool.get();
		if (threadLocalPool.size() > 0) {
			// Obtain last from pool (avoids array copies)
			// Note: buffer just used by thread so likely also cached
			pooledBuffer = threadLocalPool.remove(threadLocalPool.size() - 1);

		} else {
			// Attempt to obtain buffer form core pool
			pooledBuffer = this.getCorePoolBuffer();
		}

		// If not pooled, create new buffer
		if (pooledBuffer == null) {
			// Create new buffer
			ByteBuffer byteBuffer = this.byteBufferFactory.createByteBuffer();
			pooledBuffer = new PooledStreamBuffer(byteBuffer);

		} else {
			// Pooled buffer, so reset for use
			pooledBuffer.pooledBuffer.clear();
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
	public void threadComplete() {

		// Obtain the thread local pool
		List<StreamBuffer<ByteBuffer>> threadLocalPool = this.threadLocalPool.get();
		if (threadLocalPool.size() == 0) {
			return; // nothing to clean up
		}

		// Release the thread local pool buffers to core pool
		this.releaseAllToCorePool(threadLocalPool);

		// Release from thread memory
		threadLocalPool.clear();
	}

	/**
	 * Pooled {@link StreamBuffer}.
	 */
	private class PooledStreamBuffer extends StreamBuffer<ByteBuffer> {

		/**
		 * Instantiate.
		 * 
		 * @param byteBuffer
		 *            {@link ByteBuffer}.
		 */
		private PooledStreamBuffer(ByteBuffer byteBuffer) {
			super(byteBuffer, null);
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
			ThreadLocalByteBufferPool bufferPool = ThreadLocalByteBufferPool.this;

			// Attempt to release to thread local pool
			List<StreamBuffer<ByteBuffer>> threadLocalPool = bufferPool.threadLocalPool.get();
			if (threadLocalPool.size() < bufferPool.threadLocalPoolSize) {
				// Release to thread pool
				threadLocalPool.add(this);
				return; // released
			}

			// As here, release to core pool
			bufferPool.releaseToCorePool(this);
		}
	}

}