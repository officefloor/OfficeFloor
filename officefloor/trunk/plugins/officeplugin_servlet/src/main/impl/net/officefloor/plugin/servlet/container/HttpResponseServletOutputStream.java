/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.plugin.socket.server.http.HttpResponse;

/**
 * {@link ServletOutputStream} for the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseServletOutputStream extends ServletOutputStream {

	/**
	 * Delegate {@link OutputStream}.
	 */
	private final OutputStream delegate;

	/**
	 * Buffer size.
	 */
	private int bufferSize = 1024;

	/**
	 * Buffer to receive content.
	 */
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(
			this.bufferSize);

	/**
	 * Indicates number of nesting calls for buffering.
	 */
	private int bufferNestingLevel = 0;

	/**
	 * Flag indicating if committed.
	 */
	private boolean isCommitted = false;

	/**
	 * Initiate.
	 * 
	 * @param delegate
	 *            Delegate {@link OutputStream}.
	 */
	public HttpResponseServletOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}

	/**
	 * Ensures the {@link HttpServletRequest} is not committed.
	 * 
	 * @throws IllegalStateException
	 *             If committed.
	 */
	void ensureNotCommitted() throws IllegalStateException {
		if (this.isCommitted) {
			throw new IllegalStateException("Response has been committed");
		}
	}

	/**
	 * <p>
	 * Specifies the buffer size.
	 * <p>
	 * Setting this below the current buffered data will cause the data to start
	 * being sent and is therefore committed.
	 * 
	 * @param size
	 *            Buffer size.
	 * @throws IOException
	 *             If fails to commit full buffer.
	 */
	void setBufferSize(int size) throws IOException {

		// Can not change buffer size once committed
		this.ensureNotCommitted();

		// Specify the buffer size
		this.bufferSize = size;

		// Commit buffer if now becomes full
		this.commitFullBuffer();
	}

	/**
	 * Obtains the buffer size.
	 * 
	 * @return Buffer size.
	 */
	int getBufferSize() {
		return this.bufferSize;
	}

	/**
	 * Invoked before buffering content.
	 */
	void bufferingContent() {
		this.bufferNestingLevel++;
	}

	/**
	 * Invoked after content buffered.
	 */
	void contentBuffered() {
		this.bufferNestingLevel--;
	}

	/**
	 * Clears the buffer.
	 */
	void reset() {
		this.buffer.reset();
	}

	/**
	 * Determines if committed to the response.
	 * 
	 * @return <code>true</code> if committed to response.
	 */
	boolean isCommitted() {
		return this.isCommitted;
	}

	/**
	 * Commits the buffer if it is full.
	 * 
	 * @throws IOException
	 *             If fails to commit the response.
	 */
	private void commitFullBuffer() throws IOException {
		// Determine if buffer is full
		if (this.buffer.size() >= this.bufferSize) {
			// Full buffer so flush
			this.flush();
		}
	}

	/*
	 * ====================== ServletOutputStream =========================
	 */

	@Override
	public void write(int b) throws IOException {

		// Determine if buffer
		if (!this.isCommitted) {
			// Buffer the data
			this.buffer.write(b);

			// Commit if full buffer
			this.commitFullBuffer();
			return; // written
		}

		// Committed so always write the data
		this.delegate.write(b);
	}

	@Override
	public void flush() throws IOException {

		// Do nothing if buffering
		if (this.bufferNestingLevel > 0) {
			return;
		}

		// Flush any buffered content.
		// Only buffering if not yet committed.
		if (!this.isCommitted) {
			this.buffer.writeTo(this.delegate);
		}

		// Flag committed to response
		this.isCommitted = true;

		// Trigger flush on delegate
		this.delegate.flush();
	}

}