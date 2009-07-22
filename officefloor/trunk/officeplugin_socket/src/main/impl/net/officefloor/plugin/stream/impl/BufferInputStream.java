/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.stream.impl;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.NoBufferStreamContentException;

/**
 * {@link InputBufferStream} {@link InputStream}.
 *
 * @author Daniel Sagenschneider
 */
public class BufferInputStream extends InputStream {

	/**
	 * {@link InputBufferStream}.
	 */
	private final InputBufferStream input;

	/**
	 * Read buffer to allow reading a byte at a time.
	 */
	private final byte[] readBuffer = new byte[1];

	/**
	 * Initiate.
	 *
	 * @param input
	 *            {@link InputBufferStream}.
	 */
	public BufferInputStream(InputBufferStream input) {
		this.input = input;
	}

	/*
	 * ============== InputStream ==============================
	 */

	@Override
	public int read() throws IOException {

		// Read from the buffer stream
		int size = this.input.read(this.readBuffer);
		switch (size) {
		case BufferStream.END_OF_STREAM:
			// End of stream
			return BufferStream.END_OF_STREAM;

		case 0:
			// Must have content as will not block waiting for content
			throw new NoBufferStreamContentException();

		default:
			// Return the byte read
			int b = this.readBuffer[0];
			return b;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {

		// Read from the buffer stream
		int size = this.input.read(b);
		switch (size) {
		case BufferStream.END_OF_STREAM:
			// End of stream
			return BufferStream.END_OF_STREAM;

		case 0:
			// Must have content as will not block waiting for content
			throw new NoBufferStreamContentException();

		default:
			// Return the size read
			return size;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		// Read from the buffer stream
		int size = this.input.read(b, off, len);
		switch (size) {
		case BufferStream.END_OF_STREAM:
			// End of stream
			return BufferStream.END_OF_STREAM;

		case 0:
			// Must have content as will not block waiting for content
			throw new NoBufferStreamContentException();

		default:
			// Return the size read
			return size;
		}
	}

	@Override
	public int available() throws IOException {
		long availableLength = this.input.available();
		return ((availableLength > Integer.MAX_VALUE) ? Integer.MAX_VALUE
				: (int) availableLength);
	}

	@Override
	public void close() throws IOException {
		this.input.close();
	}

}