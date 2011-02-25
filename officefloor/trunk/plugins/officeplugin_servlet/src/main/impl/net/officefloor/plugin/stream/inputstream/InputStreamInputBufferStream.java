/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.GatheringBufferProcessor;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link InputBufferStream} implementation that wraps an {@link InputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class InputStreamInputBufferStream implements InputBufferStream {

	/**
	 * {@link InputStream} being wrapped for {@link InputBufferStream}
	 * functionality.
	 */
	private final InputStream input;

	/**
	 * Initiate.
	 * 
	 * @param input
	 *            {@link InputStream} being wrapped for
	 *            {@link InputBufferStream} functionality.
	 */
	public InputStreamInputBufferStream(InputStream input) {
		this.input = input;
	}

	/*
	 * ============================ InputBufferStream ==========================
	 */

	@Override
	public InputStream getInputStream() {
		return this.input;
	}

	@Override
	public InputStream getBrowseStream() {
		// TODO implement InputBufferStream.getBrowseStream
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.getBrowseStream");
	}

	@Override
	public int read(byte[] readBuffer) throws IOException {
		// TODO implement InputBufferStream.read
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.read");
	}

	@Override
	public int read(byte[] readBuffer, int offset, int length)
			throws IOException {
		// TODO implement InputBufferStream.read
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.read");
	}

	@Override
	public int read(BufferProcessor processor) throws IOException {
		// TODO implement InputBufferStream.read
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.read");
	}

	@Override
	public int read(int numberOfBytes, GatheringBufferProcessor processor)
			throws IOException {
		// TODO implement InputBufferStream.read
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.read");
	}

	@Override
	public int read(int numberOfBytes, OutputBufferStream outputBufferStream)
			throws IOException {
		// TODO implement InputBufferStream.read
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.read");
	}

	@Override
	public long skip(long numberOfBytes) throws IOException {
		// TODO implement InputBufferStream.skip
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.skip");
	}

	@Override
	public long available() {
		// TODO implement InputBufferStream.available
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.available");
	}

	@Override
	public void close() {
		// TODO implement InputBufferStream.close
		throw new UnsupportedOperationException(
				"TODO implement InputBufferStream.close");
	}

}