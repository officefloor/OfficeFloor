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
import java.io.OutputStream;

import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * {@link OutputBufferStream} {@link OutputStream}.
 *
 * @author Daniel Sagenschneider
 */
public class BufferOutputStream extends OutputStream {

	/**
	 * {@link OutputBufferStream}.
	 */
	private final OutputBufferStream output;

	/**
	 * Write buffer to allow writing a byte at a time.
	 */
	private final byte[] writeBuffer = new byte[1];

	/**
	 * Initiate.
	 *
	 * @param output
	 *            {@link OutputBufferStream}.
	 */
	public BufferOutputStream(OutputBufferStream output) {
		this.output = output;
	}

	/*
	 * ================ OutputStream ==============================
	 */

	@Override
	public void write(int b) throws IOException {
		this.writeBuffer[0] = (byte) b;
		this.output.write(this.writeBuffer);
	}

	@Override
	public void write(byte[] b) throws IOException {
		this.output.write(b);
	}

	@Override
	public void close() throws IOException {
		this.output.close();
	}

}