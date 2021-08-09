/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * {@link OutputStream} with additional methods to write cached
 * {@link ByteBuffer} instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ServerOutputStream extends OutputStream {

	/**
	 * <p>
	 * Writes a {@link ByteBuffer}.
	 * <p>
	 * This is to enable efficient I/O of writing content (typically cached).
	 * 
	 * @param buffer
	 *            {@link ByteBuffer} that should never change its content.
	 * @throws IOException
	 *             If fails to write the {@link ByteBuffer}.
	 */
	public abstract void write(ByteBuffer buffer) throws IOException;

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
