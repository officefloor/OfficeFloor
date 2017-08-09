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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.ServerOutputStream;

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
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext context;

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
	 * @param context
	 *            {@link ProcessAwareContext}.
	 */
	public BufferPoolServerOutputStream(BufferPool<B> bufferPool, ProcessAwareContext context) {
		this.bufferPool = bufferPool;
		this.context = context;
	}

	/**
	 * Obtains the {@link StreamBuffer} instances used by this
	 * {@link ServerOutputStream}.
	 * 
	 * @return {@link StreamBuffer} instances used by this
	 *         {@link ServerOutputStream}.
	 */
	public List<StreamBuffer<B>> getBuffers() {
		return this.context.run(() -> this.buffers);
	}

	/*
	 * ==================== ServerOutputStream ===================
	 */

	@Override
	public void write(ByteBuffer buffer) throws IOException {

		// Add the unpooled buffer
		StreamBuffer<B> streamBuffer = this.bufferPool.getUnpooledStreamBuffer(buffer);
		this.buffers.add(streamBuffer);

		// Clear current buffer, so new buffer to continue writing
		this.currentBuffer = null;
	}

	@Override
	public void write(int b) throws IOException {
		this.context.run(() -> {

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

			// Void return
			return null;
		});
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		this.context.run(() -> {

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

			// Void return
			return null;
		});
	}

}