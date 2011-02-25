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
package net.officefloor.plugin.stream.outputstream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.officefloor.plugin.stream.BufferPopulator;
import net.officefloor.plugin.stream.BufferSquirt;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link OutputBufferStream} wrapping an {@link OutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class OutputStreamOutputBufferStream implements OutputBufferStream {

	/**
	 * {@link OutputStream}.
	 */
	private final OutputStream output;

	/**
	 * Initiate.
	 * 
	 * @param output
	 *            Wrapped {@link OutputStream}.
	 */
	public OutputStreamOutputBufferStream(OutputStream output) {
		this.output = output;
	}

	/*
	 * ======================== OutputBufferStream ======================
	 */

	@Override
	public OutputStream getOutputStream() {
		return this.output;
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		// TODO implement OutputBufferStream.write
		throw new UnsupportedOperationException(
				"TODO implement OutputBufferStream.write");
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		// TODO implement OutputBufferStream.write
		throw new UnsupportedOperationException(
				"TODO implement OutputBufferStream.write");
	}

	@Override
	public void write(BufferPopulator populator) throws IOException {
		// TODO implement OutputBufferStream.write
		throw new UnsupportedOperationException(
				"TODO implement OutputBufferStream.write");
	}

	@Override
	public void append(ByteBuffer buffer) throws IOException {
		// TODO implement OutputBufferStream.append
		throw new UnsupportedOperationException(
				"TODO implement OutputBufferStream.append");
	}

	@Override
	public void append(BufferSquirt squirt) throws IOException {
		// TODO implement OutputBufferStream.append
		throw new UnsupportedOperationException(
				"TODO implement OutputBufferStream.append");
	}

	@Override
	public void close() throws IOException {
		// TODO implement OutputBufferStream.close
		throw new UnsupportedOperationException(
				"TODO implement OutputBufferStream.close");
	}

}