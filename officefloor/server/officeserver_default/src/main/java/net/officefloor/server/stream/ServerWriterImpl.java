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
package net.officefloor.server.stream;

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
public class ServerWriterImpl extends ServerWriter {

	/**
	 * {@link ServerOutputStream}.
	 */
	private final ServerOutputStream outputStream;

	/**
	 * {@link OutputStreamWriter}.
	 */
	private final OutputStreamWriter writer;

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
	public ServerWriterImpl(ServerOutputStream outputStream, Charset charset, Object lock) {
		this.outputStream = outputStream;
		this.writer = new OutputStreamWriter(outputStream, charset);
		this.lock = lock;
	}

	/*
	 * ====================== ServerWriter ========================
	 */

	@Override
	public final void write(byte[] encodedBytes) throws IOException {

		// Flush any content before directly writing bytes
		this.flush();

		// Write the encoded bytes
		this.outputStream.write(encodedBytes);
	}

	@Override
	public final void write(ByteBuffer encodedBytes) throws IOException {

		// Flush any content before directly writing bytes
		this.flush();

		// Write the encoded bytes
		this.outputStream.write(encodedBytes);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		this.writer.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException {
		this.writer.flush();
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}

}