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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * {@link ServerOutputStream} that writes to {@link StreamBuffer} instances from
 * a {@link StreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferPoolServerOutputStream<B> extends ServerOutputStream {

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<B> bufferPool;

	/**
	 * {@link CloseHandler}.
	 */
	private final CloseHandler closeHandler;

	/**
	 * Head {@link StreamBuffer}.
	 */
	private StreamBuffer<B> head = null;

	/**
	 * Tail {@link StreamBuffer}.
	 */
	private StreamBuffer<B> tail = null;

	/**
	 * <code>Content-Length</code>.
	 */
	private long contentLength = 0;

	/**
	 * Instantiate.
	 * 
	 * @param bufferPool   {@link StreamBufferPool}.
	 * @param closeHandler {@link CloseHandler}.
	 */
	public BufferPoolServerOutputStream(StreamBufferPool<B> bufferPool, CloseHandler closeHandler) {
		this.bufferPool = bufferPool;
		this.closeHandler = closeHandler;
	}

	/**
	 * Instantiate.
	 * 
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public BufferPoolServerOutputStream(StreamBufferPool<B> bufferPool) {
		this(bufferPool, new CloseHandler() {

			private boolean isClosed = false;

			@Override
			public boolean isClosed() {
				return this.isClosed;
			}

			@Override
			public void close() {
				this.isClosed = true;
			}
		});
	}

	/**
	 * Obtains the {@link ServerWriter}.
	 * 
	 * @param charset {@link Charset} for writing out {@link String} data.
	 * @return {@link ServerWriter}.
	 * @throws IOException Should {@link ServerOutputStream} be closed.
	 */
	public ServerWriter getServerWriter(Charset charset) throws IOException {
		this.ensureOpen();
		return new BufferPoolServerWriter(charset);
	}

	/**
	 * Obtains the content length of output data.
	 * 
	 * @return Content length of output data.
	 */
	public long getContentLength() {
		return this.contentLength;
	}

	/**
	 * Obtains the head {@link StreamBuffer} instances used by this
	 * {@link ServerOutputStream}.
	 * 
	 * @return {@link StreamBuffer} instances used by this
	 *         {@link ServerOutputStream}.
	 */
	public StreamBuffer<B> getBuffers() {
		return this.head;
	}

	/**
	 * Clears this {@link OutputStream} and releases the {@link StreamBuffer}
	 * instances.
	 * 
	 * @throws IOException If failure in clearing {@link OutputStream}.
	 */
	public void clear() throws IOException {

		// Release all the buffers (and clear list)
		while (this.head != null) {
			StreamBuffer<B> release = this.head;
			this.head = this.head.next;

			// Determine if file with callback
			if ((release.fileBuffer != null) && (release.fileBuffer.callback != null)) {
				// File will not be written, so complete
				release.fileBuffer.callback.complete(release.fileBuffer.file, false);
			}

			// Release after (so next not cleared)
			release.release();
		}
		this.tail = null;

		// No content
		this.contentLength = 0;
	}

	/**
	 * Ensures the {@link OutputStream} is open.
	 * 
	 * @throws IOException If {@link OutputStream} is closed.
	 */
	private final void ensureOpen() throws IOException {
		if (this.closeHandler.isClosed()) {
			throw new IOException("Closed");
		}
	}

	/*
	 * ===================== ServerOutputStream ======================
	 */

	@Override
	public void write(ByteBuffer buffer) throws IOException {
		this.ensureOpen();

		// Capture bytes being added
		// (implementation may copy out bytes to stream buffer)
		int bytesToInclude = buffer.remaining();

		// Add the unpooled buffer
		StreamBuffer<B> streamBuffer = this.bufferPool.getUnpooledStreamBuffer(buffer);
		if (this.head == null) {
			// First buffer
			this.head = streamBuffer;
			this.tail = streamBuffer;
		} else {
			// Append buffer
			this.tail.next = streamBuffer;
			this.tail = streamBuffer;
		}

		// Content included
		this.contentLength += bytesToInclude;
	}

	@Override
	public void write(FileChannel file, long position, long count, FileCompleteCallback callback) throws IOException {
		this.ensureOpen();

		// Add the file buffer
		StreamBuffer<B> fileBuffer = this.bufferPool.getFileStreamBuffer(file, position, count, callback);
		if (this.head == null) {
			this.head = fileBuffer;
			this.tail = fileBuffer;
		} else {
			// Add buffer
			this.tail.next = fileBuffer;
			this.tail = fileBuffer;
		}

		// Content included
		count = (count < 0) ? file.size() - position : count;
		this.contentLength += count;
	}

	@Override
	public void write(FileChannel file, FileCompleteCallback callback) throws IOException {
		this.write(file, 0, -1, callback);
	}

	@Override
	public void write(int b) throws IOException {
		this.ensureOpen();

		// Ensure have current pooled buffer
		if (this.tail == null) {
			// Add first buffer
			StreamBuffer<B> streamBuffer = this.bufferPool.getPooledStreamBuffer();
			this.head = streamBuffer;
			this.tail = streamBuffer;
		} else if (this.tail.pooledBuffer == null) {
			// Last is not pooled, so add pooled
			StreamBuffer<B> streamBuffer = this.bufferPool.getPooledStreamBuffer();
			this.tail.next = streamBuffer;
			this.tail = streamBuffer;
		}

		// Write the byte to the current buffer
		boolean isWritten = this.tail.write((byte) b);

		// Determine if full and must write to another buffer
		if (!isWritten) {
			// Add another buffer and write the data
			StreamBuffer<B> streamBuffer = this.bufferPool.getPooledStreamBuffer();
			this.tail.next = streamBuffer;
			this.tail = streamBuffer;
			isWritten = this.tail.write((byte) b);
			if (!isWritten) {
				// Should always be able to write a byte to a new buffer
				throw new IOException(
						"Failed to write byte " + String.valueOf(b) + " to new " + this.tail.getClass().getName());
			}
		}

		// Additional byte written
		this.contentLength++;
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		this.ensureOpen();

		// All mutation of values
		int offset = off;
		int remaining = len;

		// Ensure have current pooled buffer
		if (this.tail == null) {
			// Add first buffer
			StreamBuffer<B> streamBuffer = this.bufferPool.getPooledStreamBuffer();
			this.head = streamBuffer;
			this.tail = streamBuffer;
		} else if (this.tail.pooledBuffer == null) {
			// Last is not pooled, so add pooled
			StreamBuffer<B> streamBuffer = this.bufferPool.getPooledStreamBuffer();
			this.tail.next = streamBuffer;
			this.tail = streamBuffer;
		}

		// Keep writing to buffers until complete
		do {

			// Write the bytes to buffer
			int bytesWritten = this.tail.write(bytes, offset, remaining);

			// Determine number of bytes remaining
			remaining -= bytesWritten;

			// Adjust for potential another write
			if (remaining > 0) {
				offset += bytesWritten;
				StreamBuffer<B> streamBuffer = this.bufferPool.getPooledStreamBuffer();
				this.tail.next = streamBuffer;
				this.tail = streamBuffer;
			}

		} while (remaining > 0);

		// Content written
		this.contentLength += len;
	}

	@Override
	public void flush() throws IOException {
		this.ensureOpen();

		// Nothing to flush, as always writes straight to buffers
	}

	@Override
	public void close() throws IOException {

		// Ensure close only once
		if (this.closeHandler.isClosed()) {
			return; // already closed
		}

		// Handle close
		this.closeHandler.close();
	}

	/**
	 * {@link StreamBufferPool} {@link ServerWriter}.
	 */
	private class BufferPoolServerWriter extends ServerWriter {

		/**
		 * Delegate {@link OutputStreamWriter}.
		 */
		private final OutputStreamWriter delegate;

		/**
		 * Instantiate.
		 * 
		 * @param charset {@link Charset}.
		 */
		private BufferPoolServerWriter(Charset charset) {
			this.delegate = new OutputStreamWriter(BufferPoolServerOutputStream.this, charset);
		}

		/*
		 * ============= ServerWriter =====================
		 */

		@Override
		public void write(byte[] encodedBytes) throws IOException {

			// Flush to ensure written out
			this.delegate.flush();

			// Write the encoded bytes
			BufferPoolServerOutputStream.this.write(encodedBytes);
		}

		@Override
		public void write(ByteBuffer encodedBytes) throws IOException {

			// Flush to ensure written out
			this.delegate.flush();

			// Write the buffer
			BufferPoolServerOutputStream.this.write(encodedBytes);
		}

		@Override
		public void write(FileChannel file, long position, long count, FileCompleteCallback callback)
				throws IOException {

			// Flush to ensure written out
			this.delegate.flush();

			// Write the file content
			BufferPoolServerOutputStream.this.write(file, position, count, callback);
		}

		@Override
		public void write(FileChannel file, FileCompleteCallback callback) throws IOException {

			// Flush to ensure written out
			this.delegate.flush();

			// Write the file content
			BufferPoolServerOutputStream.this.write(file, callback);
		}

		/*
		 * ================ Writer ========================
		 */

		@Override
		public void write(int c) throws IOException {
			this.delegate.write(c);
		}

		@Override
		public void write(char[] cbuf) throws IOException {
			this.delegate.write(cbuf);
		}

		@Override
		public void write(String str) throws IOException {
			this.delegate.write(str);
		}

		@Override
		public void write(String str, int off, int len) throws IOException {
			this.delegate.write(str, off, len);
		}

		@Override
		public Writer append(CharSequence csq) throws IOException {
			this.delegate.append(csq);
			return this;
		}

		@Override
		public Writer append(CharSequence csq, int start, int end) throws IOException {
			this.delegate.append(csq, start, end);
			return this;
		}

		@Override
		public Writer append(char c) throws IOException {
			this.delegate.append(c);
			return this;
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			this.delegate.write(cbuf, off, len);
		}

		@Override
		public void flush() throws IOException {
			this.delegate.flush();
		}

		@Override
		public void close() throws IOException {

			// Determine if already closed
			if (BufferPoolServerOutputStream.this.closeHandler.isClosed()) {
				return; // already closed
			}

			// Close
			this.delegate.close();
		}
	}

}
