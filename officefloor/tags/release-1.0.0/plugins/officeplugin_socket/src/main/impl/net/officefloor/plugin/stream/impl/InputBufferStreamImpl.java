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

package net.officefloor.plugin.stream.impl;

import java.io.IOException;
import java.io.InputStream;

import net.officefloor.plugin.stream.BufferProcessor;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.GatheringBufferProcessor;
import net.officefloor.plugin.stream.InputBufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link InputBufferStream} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class InputBufferStreamImpl implements InputBufferStream {

	/**
	 * {@link BufferStream}.
	 */
	private final BufferStream bufferStream;

	/**
	 * {@link InputStream}.
	 */
	private final BufferInputStream inputStream = new BufferInputStream(this);

	/**
	 * Initiate.
	 *
	 * @param bufferStream
	 *            {@link BufferStream}.
	 */
	public InputBufferStreamImpl(BufferStream bufferStream) {
		this.bufferStream = bufferStream;
	}

	/*
	 * ================ InputBufferStream ==============================
	 */

	@Override
	public InputStream getInputStream() {
		return this.inputStream;
	}

	@Override
	public InputStream getBrowseStream() {
		return this.bufferStream.getBrowseStream();
	}

	@Override
	public int read(byte[] readBuffer) throws IOException {
		return this.bufferStream.read(readBuffer);
	}

	@Override
	public int read(byte[] readBuffer, int offset, int length)
			throws IOException {
		return this.bufferStream.read(readBuffer, offset, length);
	}

	@Override
	public int read(BufferProcessor processor) throws IOException {
		return this.bufferStream.read(processor);
	}

	@Override
	public int read(int numberOfBytes, GatheringBufferProcessor processor)
			throws IOException {
		return this.bufferStream.read(numberOfBytes, processor);
	}

	@Override
	public int read(int numberOfBytes, OutputBufferStream outputBufferStream)
			throws IOException {
		return this.bufferStream.read(numberOfBytes, outputBufferStream);
	}

	@Override
	public long skip(long numberOfBytes) throws IOException {
		return this.bufferStream.skip(numberOfBytes);
	}

	@Override
	public long available() {
		return this.bufferStream.available();
	}

	@Override
	public void close() {
		this.bufferStream.closeInput();
	}

}