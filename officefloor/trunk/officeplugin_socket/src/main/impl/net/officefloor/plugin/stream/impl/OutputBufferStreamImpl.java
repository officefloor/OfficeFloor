/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.BufferStream;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link OutputBufferStream} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OutputBufferStreamImpl implements OutputBufferStream {

	/**
	 * {@link BufferStream}.
	 */
	private final BufferStream bufferStream;

	/**
	 * {@link OutputStream}.
	 */
	private final BufferOutputStream outputStream = new BufferOutputStream(this);

	/**
	 * Initiate.
	 *
	 * @param bufferStream
	 *            {@link BufferStream}.
	 */
	public OutputBufferStreamImpl(BufferStream bufferStream) {
		this.bufferStream = bufferStream;
	}

	/*
	 * =============== OutputBufferStream ============================
	 */

	@Override
	public OutputStream getOutputStream() {
		return this.outputStream;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		this.bufferStream.write(bytes);
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		this.bufferStream.write(data, offset, length);
	}

	@Override
	public void write(BufferPopulator populator) throws IOException {
		this.bufferStream.write(populator);
	}

	@Override
	public void append(ByteBuffer buffer) throws IOException {
		this.bufferStream.append(buffer);
	}

	@Override
	public void append(BufferSquirt squirt) throws IOException {
		this.bufferStream.append(squirt);
	}

	@Override
	public void close() {
		this.bufferStream.closeOutput();
	}

}