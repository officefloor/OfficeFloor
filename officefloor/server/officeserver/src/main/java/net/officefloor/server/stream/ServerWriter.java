/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.stream;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Server {@link Writer}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ServerWriter extends Writer {

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
	 */
	public abstract void write(byte[] encodedBytes) throws IOException;

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
	 */
	public abstract void write(ByteBuffer encodedBytes) throws IOException;

	/**
	 * <p>
	 * Writes part of the {@link FileChannel} contents.
	 * <p>
	 * This is to enable efficient I/O (ie DMA) of writing {@link FileChannel}
	 * content.
	 * <p>
	 * To write the entire {@link FileChannel} contents, invoke
	 * <code>write(file, 0, -1)</code>.
	 * <p>
	 * Note that the underlying implementation will need to support
	 * {@link FileChannel} efficiencies.
	 * <p>
	 * Caution should also be taken to ensure that previous written content is
	 * not waiting for further surrogate characters.
	 * 
	 * @param file
	 *            {@link FileChannel}.
	 * @param position
	 *            Position within the {@link FileChannel} to start writing
	 *            content. Must be non-negative number.
	 * @param count
	 *            Count of bytes to write from the {@link FileChannel}. A
	 *            negative value (typically <code>-1</code>) indicates to write
	 *            the remaining {@link FileChannel} content from position.
	 * @param callback
	 *            Optional {@link FileCompleteCallback}. May be
	 *            <code>null</code>.
	 * @throws IOException
	 *             If fails to write the {@link FileChannel} content.
	 */
	public abstract void write(FileChannel file, long position, long count, FileCompleteCallback callback)
			throws IOException;

	/**
	 * <p>
	 * Writes the entire {@link FileChannel} contents.
	 * <p>
	 * This is a convenience method for <code>write(file, 0, -1)</code>.
	 * <p>
	 * Caution should also be taken to ensure that previous written content is
	 * not waiting for further surrogate characters.
	 * 
	 * @param file
	 *            {@link FileChannel}.
	 * @param callback
	 *            Optional {@link FileCompleteCallback}. May be
	 *            <code>null</code>.
	 * @throws IOException
	 *             If fails to write the {@link FileChannel} content.
	 */
	public abstract void write(FileChannel file, FileCompleteCallback callback) throws IOException;

}
