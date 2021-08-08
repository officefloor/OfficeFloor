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

package net.officefloor.server.stream.impl;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;

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

	@Override
	public StreamBuffer<B> getFileStreamBuffer(FileChannel file, long position, long count,
			FileCompleteCallback callback) {
		return new FileStreamBuffer(new FileBuffer(file, position, count, callback));
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
			super(null, buffer, null);
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

	/**
	 * {@link FileChannel} {@link StreamBuffer}.
	 */
	protected class FileStreamBuffer extends StreamBuffer<B> {

		/**
		 * Instantiate.
		 * 
		 * @param buffer
		 *            {@link FileBuffer}.
		 */
		private FileStreamBuffer(FileBuffer buffer) {
			super(null, null, buffer);
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
