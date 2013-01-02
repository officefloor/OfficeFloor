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
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * {@link OutputStream} with additional methods to write cached
 * {@link ByteBuffer} instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ServerOutputStream extends OutputStream {

	/**
	 * <p>
	 * Writes a cached {@link ByteBuffer}.
	 * <p>
	 * This is to enable efficient I/O of writing cached content.
	 * 
	 * @param cachedBuffer
	 *            Cached {@link ByteBuffer} that should never change its
	 *            content.
	 * @throws IOException
	 *             If fails to write the {@link ByteBuffer}.
	 */
	public abstract void write(ByteBuffer cachedBuffer) throws IOException;

	/*
	 * ================= OutputStream =========================
	 */

	@Override
	public abstract void write(int b) throws IOException;

	// TODO provide more performance implementations
	// @Override
	// public abstract void write(byte[] b) throws IOException;
	//
	// @Override
	// public abstract void write(byte[] b, int off, int len) throws
	// IOException;

	@Override
	public abstract void flush() throws IOException;

	@Override
	public abstract void close() throws IOException;

}