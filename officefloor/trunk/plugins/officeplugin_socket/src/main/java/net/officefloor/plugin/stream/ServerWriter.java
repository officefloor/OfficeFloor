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
package net.officefloor.plugin.stream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
	 * Indicates if using the default {@link Charset} for the Server.
	 */
	private final boolean isServerDefaultCharset;

	/**
	 * Initiate.
	 * 
	 * @param outputStream
	 *            {@link ServerOutputStream}.
	 * @param charset
	 *            {@link Charset}.
	 * @param isServerDefaultCharset
	 *            Indicates if using the default {@link Charset} for the Server.
	 * @param lock
	 *            Lock for <code>synchronize</code>.
	 */
	public ServerWriter(ServerOutputStream outputStream, Charset charset,
			boolean isServerDefaultCharset, Object lock) {
		super(outputStream, charset);
		this.lock = lock;
		this.outputStream = outputStream;
		this.isServerDefaultCharset = isServerDefaultCharset;
	}

	/**
	 * Indicates if using the default {@link Charset} for the Server.
	 * 
	 * @return <code>true</code> if encoding with default {@link Charset} for
	 *         the Server.
	 * 
	 * @see #write(byte[])
	 */
	public final boolean isServerDefaultCharset() {
		return this.isServerDefaultCharset;
	}

	/**
	 * <p>
	 * Enables writing encoded bytes.
	 * <p>
	 * This is only to be used if writing with the default server
	 * {@link Charset}.
	 * <p>
	 * Caution should also be taken to ensure that previous written content is
	 * not waiting for further surrogate characters.
	 * 
	 * @param encodedBytes
	 *            Encoded bytes.
	 * 
	 * @see #isDefaultServerCharset
	 */
	public final void write(byte[] encodedBytes) throws IOException {

		// Ensure server default charset
		if (!this.isServerDefaultCharset) {
			throw new IOException(
					"May only write encoded bytes if default charset");
		}

		// Write the encoded bytes
		this.outputStream.write(encodedBytes);
	}

}