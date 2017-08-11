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
	 * {@link StreamBuffer} instances with content.
	 */
	private final List<StreamBuffer<B>> buffers = new ArrayList<>();

	/**
	 * Current {@link StreamBuffer} being written.
	 */
	private StreamBuffer<B> currentBuffer = null;

	/**
	 * Instantiate.
	 * 
	 * @param bufferPool
	 *            {@link BufferPool}.
	 */
	public BufferPoolServerOutputStream(BufferPool<B> bufferPool) {
		this.bufferPool = bufferPool;
	}

	/**
	 * Obtains the {@link ServerWriter}.
	 * 
	 * @param charset
	 *            {@link Charset} for writing out {@link String} data.
	 * @return {@link ServerWriter}.
	 */
	public ServerWriter getServerWriter(Charset charset) {
		return new BufferPoolServerWriter(charset);
	}

	/**
	 * Obtains the content length of output data.
	 * 
	 * @return Content length of output data.
	 */
	public int getContentLength() {
		throw new UnsupportedOperationException("TODO implement");
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

	/*
	 * ===================== ServerOutputStream ======================
	 */

	@Override
	public void write(ByteBuffer buffer) throws IOException {

		// Easy access to attributes
		@SuppressWarnings("resource")
		BufferPoolServerOutputStream<B> stream = BufferPoolServerOutputStream.this;

		// Add the unpooled buffer
		StreamBuffer<B> streamBuffer = stream.bufferPool.getUnpooledStreamBuffer(buffer);
		stream.buffers.add(streamBuffer);

		// Clear current buffer, so new buffer to continue writing
		stream.currentBuffer = null;
	}

	@Override
	public void write(int b) throws IOException {

		// Easy access to attributes
		@SuppressWarnings("resource")
		BufferPoolServerOutputStream<B> stream = BufferPoolServerOutputStream.this;

		// Ensure have current buffer
		if (stream.currentBuffer == null) {
			stream.currentBuffer = stream.bufferPool.getPooledStreamBuffer();
			stream.buffers.add(stream.currentBuffer);
		}

		// Write the byte to the current buffer
		boolean isWritten = stream.currentBuffer.write((byte) b);

		// Determine if full and must write to another buffer
		if (!isWritten) {
			// Add another buffer and write the data
			stream.currentBuffer = stream.bufferPool.getPooledStreamBuffer();
			stream.buffers.add(stream.currentBuffer);
			isWritten = stream.currentBuffer.write((byte) b);
			if (!isWritten) {
				// Should always be able to write a byte to a new buffer
				throw new IOException("Failed to write byte " + String.valueOf(b) + " to new "
						+ stream.currentBuffer.getClass().getName());
			}
		}
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		// Easy access to attributes
		@SuppressWarnings("resource")
		BufferPoolServerOutputStream<B> stream = BufferPoolServerOutputStream.this;

		// All mutation of values
		int offset = off;
		int remaining = len;

		// Ensure have current buffer
		if (stream.currentBuffer == null) {
			stream.currentBuffer = stream.bufferPool.getPooledStreamBuffer();
			stream.buffers.add(stream.currentBuffer);
		}

		// Keep writing to buffers until complete
		do {

			// Write the bytes to buffer
			int bytesWritten = stream.currentBuffer.write(bytes, offset, remaining);

			// Determine number of bytes remaining
			remaining -= bytesWritten;

			// Adjust for potential another write
			if (remaining > 0) {
				offset += bytesWritten;
				stream.currentBuffer = stream.bufferPool.getPooledStreamBuffer();
				stream.buffers.add(stream.currentBuffer);
			}

		} while (remaining > 0);
	}

	@Override
	public void flush() throws IOException {
		// Nothing to flush
	}

	@Override
	public void close() throws IOException {
		// TODO trigger sending response
		throw new UnsupportedOperationException("TODO trigger sending response on OutputStream.close()");
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
			this.delegate.close();
		}
	}

}