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
package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;

/**
 * {@link ServerOutputStream} that writes to {@link StreamBuffer} instances from
 * a {@link BufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferPoolServerOutputStream<B> extends ServerOutputStream {

	/**
	 * {@link BufferPool}.
	 */
	private final BufferPool<B> bufferPool;

	/**
	 * {@link CloseHandler}.
	 */
	private final CloseHandler closeHandler;

	/**
	 * {@link StreamBuffer} instances with content.
	 */
	private final List<StreamBuffer<B>> buffers = new ArrayList<>();

	/**
	 * Current {@link StreamBuffer} being written.
	 */
	private StreamBuffer<B> currentBuffer = null;

	/**
	 * <code>Content-Length</code>.
	 */
	private int contentLength = 0;

	/**
	 * Instantiate.
	 * 
	 * @param bufferPool
	 *            {@link BufferPool}.
	 * @param closeHandler
	 *            {@link CloseHandler}.
	 */
	public BufferPoolServerOutputStream(BufferPool<B> bufferPool, CloseHandler closeHandler) {
		this.bufferPool = bufferPool;
		this.closeHandler = closeHandler;
	}

	/**
	 * Instantiate.
	 * 
	 * @param bufferPool
	 *            {@link BufferPool}.
	 */
	public BufferPoolServerOutputStream(BufferPool<B> bufferPool) {
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
	 * @param charset
	 *            {@link Charset} for writing out {@link String} data.
	 * @return {@link ServerWriter}.
	 * @throws IOException
	 *             Should {@link ServerOutputStream} be closed.
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
	public int getContentLength() {
		return this.contentLength;
	}

	/**
	 * Obtains the {@link StreamBuffer} instances used by this
	 * {@link ServerOutputStream}.
	 * 
	 * @return {@link StreamBuffer} instances used by this
	 *         {@link ServerOutputStream}.
	 */
	public List<StreamBuffer<B>> getBuffers() {
		return this.buffers;
	}

	/**
	 * Ensures the {@link OutputStream} is open.
	 * 
	 * @throws IOException
	 *             If {@link OutputStream} is closed.
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

		// Add the unpooled buffer
		StreamBuffer<B> streamBuffer = this.bufferPool.getUnpooledStreamBuffer(buffer);
		this.buffers.add(streamBuffer);

		// Clear current buffer, so new buffer to continue writing
		this.currentBuffer = null;

		// Content included
		this.contentLength += buffer.remaining();
	}

	@Override
	public void write(int b) throws IOException {
		this.ensureOpen();

		// Ensure have current buffer
		if (this.currentBuffer == null) {
			this.currentBuffer = this.bufferPool.getPooledStreamBuffer();
			this.buffers.add(this.currentBuffer);
		}

		// Write the byte to the current buffer
		boolean isWritten = this.currentBuffer.write((byte) b);

		// Determine if full and must write to another buffer
		if (!isWritten) {
			// Add another buffer and write the data
			this.currentBuffer = this.bufferPool.getPooledStreamBuffer();
			this.buffers.add(this.currentBuffer);
			isWritten = this.currentBuffer.write((byte) b);
			if (!isWritten) {
				// Should always be able to write a byte to a new buffer
				throw new IOException("Failed to write byte " + String.valueOf(b) + " to new "
						+ this.currentBuffer.getClass().getName());
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

		// Ensure have current buffer
		if (this.currentBuffer == null) {
			this.currentBuffer = this.bufferPool.getPooledStreamBuffer();
			this.buffers.add(this.currentBuffer);
		}

		// Keep writing to buffers until complete
		do {

			// Write the bytes to buffer
			int bytesWritten = this.currentBuffer.write(bytes, offset, remaining);

			// Determine number of bytes remaining
			remaining -= bytesWritten;

			// Adjust for potential another write
			if (remaining > 0) {
				offset += bytesWritten;
				this.currentBuffer = this.bufferPool.getPooledStreamBuffer();
				this.buffers.add(this.currentBuffer);
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
	 * {@link BufferPool} {@link ServerWriter}.
	 */
	private class BufferPoolServerWriter extends ServerWriter {

		/**
		 * Delegate {@link OutputStreamWriter}.
		 */
		private final OutputStreamWriter delegate;

		/**
		 * Instantiate.
		 * 
		 * @param charset
		 *            {@link Charset}.
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