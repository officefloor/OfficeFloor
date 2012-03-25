/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.stream.inputstream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.GatheringBufferProcessor;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;
import net.officefloor.plugin.stream.synchronise.SynchronizedInputStream;

/**
 * {@link InputBufferStream} implementation that wraps an {@link InputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class InputStreamInputBufferStream implements InputBufferStream {

	/**
	 * {@link BrowsableInputStream}.
	 */
	private BrowsableInputStream browsableInput = null;

	/**
	 * {@link SynchronizedInputStream}.
	 */
	private SynchronizedInputStream synchronizedInput = null;

	/**
	 * Initiate.
	 * 
	 * @param input
	 *            {@link InputStream} being wrapped for
	 *            {@link InputBufferStream} functionality.
	 */
	public InputStreamInputBufferStream(InputStream input) {
		this.browsableInput = new BrowsableInputStream(input, 1024, this);
		this.synchronizedInput = new SynchronizedInputStream(
				this.browsableInput, this);
	}

	/*
	 * ============================ InputBufferStream ==========================
	 */

	@Override
	public InputStream getInputStream() {
		return this.synchronizedInput;
	}

	@Override
	public InputStream getBrowseStream() {
		return new SynchronizedInputStream(this.browsableInput.createBrowser(),
				this);
	}

	@Override
	public int read(byte[] readBuffer) throws IOException {
		return this.synchronizedInput.read(readBuffer);
	}

	@Override
	public int read(byte[] readBuffer, int offset, int length)
			throws IOException {
		return this.synchronizedInput.read(readBuffer, offset, length);
	}

	@Override
	public int read(BufferProcessor processor) throws IOException {
		int available = this.synchronizedInput.available();
		byte[] data = new byte[available];
		this.synchronizedInput.read(data);
		ByteBuffer buffer = ByteBuffer.wrap(data);
		processor.process(buffer);
		return data.length;
	}

	@Override
	public int read(int numberOfBytes, GatheringBufferProcessor processor)
			throws IOException {

		// Read the bytes
		byte[] data = new byte[numberOfBytes];
		int bytesRead = this.synchronizedInput.read(data);

		// Process the bytes
		ByteBuffer buffer = ByteBuffer.wrap(data, 0, bytesRead);
		processor.process(new ByteBuffer[] { buffer });

		// Return the bytes read
		return bytesRead;
	}

	@Override
	public int read(int numberOfBytes, OutputBufferStream outputBufferStream)
			throws IOException {

		// Read the bytes
		int bytesRead = 0;
		OutputStream output = outputBufferStream.getOutputStream();
		while (bytesRead < numberOfBytes) {

			// Read the value
			int value = this.synchronizedInput.read();
			if (value == -1) {
				return bytesRead; // no further bytes to read
			}

			// Read the byte
			output.write(value);
			bytesRead++;
		}

		// Return the number of bytes read
		return bytesRead;
	}

	@Override
	public long skip(long numberOfBytes) throws IOException {
		return this.synchronizedInput.skip(numberOfBytes);
	}

	@Override
	public long available() throws IOException {
		return this.synchronizedInput.available();
	}

	@Override
	public void close() throws IOException {
		this.synchronizedInput.close();
	}

}