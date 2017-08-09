/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockBufferPool;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link ServerWriter} provided by the
 * {@link BufferPoolServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferPoolServerWriterTest extends OfficeFrameTestCase {

	/**
	 * {@link MockBufferPool}.
	 */
	private final MockBufferPool bufferPool = new MockBufferPool();

	/**
	 * {@link BufferPoolServerOutputStream}.
	 */
	private final BufferPoolServerOutputStream<byte[]> outputStream = new BufferPoolServerOutputStream<>(
			this.bufferPool, new ProcessAwareContext() {
				@Override
				public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
					return operation.run();
				}
			});

	/**
	 * {@link ServerWriter} to test.
	 */
	private final ServerWriter writer = this.outputStream
			.getServerWriter(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

	/**
	 * Obtains the content written to buffers.
	 * 
	 * @return Content of buffers.
	 */
	private String getContent() {

		// Obtain the buffers
		List<StreamBuffer<byte[]>> buffers = this.outputStream.getBuffers();

		// Release the buffers (as consider request written)
		for (StreamBuffer<byte[]> buffer : buffers) {
			buffer.release();
		}

		// Obtain the content
		return MockBufferPool.getContent(buffers, ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
	}

	/**
	 * Ensure can write simple text output.
	 */
	public void testSimpleText() throws IOException {
		this.writer.write("Hello World");
		this.writer.flush();
		assertEquals("Incorrect written data", "Hello World", this.getContent());
	}

	/**
	 * Ensure can write bytes.
	 */
	public void testBytes() throws IOException {
		this.writer.write("TEST-");
		this.writer.write("bytes".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
		this.writer.write('.');
		this.writer.flush();
		assertEquals("Incorrect written data", "TEST-bytes.", this.getContent());
	}

	/**
	 * Ensure can write {@link ByteBuffer}.
	 */
	public void testByteBuffer() throws IOException {
		this.writer.write("TEST-");
		this.writer.write(
				ByteBuffer.wrap("buffer".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET)).duplicate());
		this.writer.write(new char[] { '.' });
		this.writer.flush();
		assertEquals("Incorrect written data", "TEST-buffer.", this.getContent());
	}

}