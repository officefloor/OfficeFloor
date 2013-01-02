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
package net.officefloor.plugin.stream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Server {@link Writer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerWriter extends OutputStreamWriter {

	/**
	 * {@link ServerOutputStream}.
	 */
	private final ServerOutputStream outputStream;

	/**
	 * Initiate.
	 * 
	 * @param outputStream
	 *            {@link ServerOutputStream}.
	 * @param charset
	 *            {@link Charset}.
	 * @param lock
	 *            Lock for <code>synchronize</code>.
	 */
	public ServerWriter(ServerOutputStream outputStream, Charset charset,
			Object lock) {
		super(outputStream, charset);
		this.lock = lock;
		this.outputStream = outputStream;
	}

	/**
	 * <p>
	 * Enables writing encoded bytes.
	 * <p>
	 * Caution should also be taken to ensure that previous written content is
	 * not waiting for further surrogate characters.
	 * 
	 * @param encodedBytes
	 *            Encoded bytes.
	 * @throws IOException
	 *             If fails to write the bytes.
	 * 
	 * @see #isDefaultServerCharset
	 */
	public final void write(byte[] encodedBytes) throws IOException {

		// Flush any content before directly writing bytes
		this.flush();

		// Write the encoded bytes
		this.outputStream.write(encodedBytes);
	}

	/**
	 * <p>
	 * Enables writing encoded bytes.
	 * <p>
	 * Caution should also be taken to ensure that previous written content is
	 * not waiting for further surrogate characters.
	 * 
	 * @param encodedBytes
	 *            {@link ByteBuffer} containing the encoded bytes.
	 * @throws IOException
	 *             If fails to write the bytes.
	 * 
	 * @see #isDefaultServerCharset
	 */
	public final void write(ByteBuffer encodedBytes) throws IOException {

		// Flush any content before directly writing bytes
		this.flush();

		// Write the encoded bytes
		this.outputStream.write(encodedBytes);
	}

}