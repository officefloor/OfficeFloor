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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockStreamBufferPool;
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
	 * {@link MockStreamBufferPool}.
	 */
	private final MockStreamBufferPool bufferPool = new MockStreamBufferPool();

	/**
	 * {@link BufferPoolServerOutputStream}.
	 */
	private final BufferPoolServerOutputStream<ByteBuffer> outputStream = new BufferPoolServerOutputStream<>(
			this.bufferPool);

	/**
	 * {@link ServerWriter} to test.
	 */
	private ServerWriter writer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Create the server writer to test
		this.writer = this.outputStream.getServerWriter(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
	}

	/**
	 * Obtains the content written to buffers.
	 * 
	 * @return Content of buffers.
	 */
	private String getContent() {

		// Obtain the buffers
		StreamBuffer<ByteBuffer> headBuffer = this.outputStream.getBuffers();

		// Release the buffers (as consider request written)
		MockStreamBufferPool.releaseStreamBuffers(headBuffer);

		// Obtain the content
		return MockStreamBufferPool.getContent(headBuffer, ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
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
	 * Ensure can write/read UTF-16.
	 */
	public void testWriteReadUTF16() throws IOException {
		Charset charset = Charset.forName("UTF-16");

		// Write out the data
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(buffer, charset);
		writer.write("Hello World");
		writer.flush();

		// Ensure the data is correct
		byte[] expectedData = "Hello World".getBytes(charset);
		byte[] actualData = buffer.toByteArray();
		assertEquals("Incorrect data length", expectedData.length, actualData.length);
		for (int i = 0; i < expectedData.length; i++) {
			assertEquals("Incorrect byte " + i, expectedData[i], actualData[i]);
		}
		assertEquals("Incorrect content", "Hello World", new String(actualData, charset));

		// Ensure can read in data
		StringWriter result = new StringWriter();
		InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(actualData), charset);
		for (int character = reader.read(); character != -1; character = reader.read()) {
			result.write(character);
		}
		assertEquals("Incorrect write/read UTF-16 content", "Hello World", result.toString());
	}

	/**
	 * Ensure can write in different {@link Charset}.
	 */
	public void testUTF16() throws IOException {

		Charset charset = Charset.forName("UTF-16");

		// Write out the data
		ServerWriter differentCharset = this.outputStream.getServerWriter(charset);
		differentCharset.write("Hello World");
		differentCharset.flush();

		// Obtain the buffers
		StreamBuffer<ByteBuffer> headBuffer = this.outputStream.getBuffers();
		MockStreamBufferPool.releaseStreamBuffers(headBuffer);

		// Ensure stream in correct data
		byte[] expectedData = "Hello World".getBytes(charset);
		InputStream expectedInput = new ByteArrayInputStream(expectedData);
		InputStream actualInput = MockStreamBufferPool.createInputStream(headBuffer);
		for (int i = 0; i < expectedData.length; i++) {
			assertEquals("Incorrect byte " + i + "(" + Character.valueOf((char) expectedData[i]) + ")",
					expectedInput.read(), actualInput.read());
		}
		assertEquals("Should be end of input data", -1, actualInput.read());

		// Ensure stream data
		StringWriter result = new StringWriter();
		InputStreamReader reader = new InputStreamReader(MockStreamBufferPool.createInputStream(headBuffer), charset);
		for (int character = reader.read(); character != -1; character = reader.read()) {
			result.write(character);
		}
		assertEquals("Incorrect write/read UTF-16 content", "Hello World", result.toString());

		// Ensure can read in content
		String content = MockStreamBufferPool.getContent(headBuffer, charset);
		assertEquals("Incorrect written content", "Hello World", content);
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

	/**
	 * Ensure triggers close.
	 */
	public void testClose() throws IOException {

		final CloseHandler handler = this.createMock(CloseHandler.class);
		@SuppressWarnings("resource")
		BufferPoolServerOutputStream<ByteBuffer> outputStream = new BufferPoolServerOutputStream<>(this.bufferPool,
				handler);

		// Record close only once
		this.recordReturn(handler, handler.isClosed(), false); // ServerWriter
		this.recordReturn(handler, handler.isClosed(), false); // writer close
		this.recordReturn(handler, handler.isClosed(), false); // outputStream
		handler.close();
		this.recordReturn(handler, handler.isClosed(), true);

		// Replay
		this.replayMockObjects();

		// Obtain the writer
		final ServerWriter writer = outputStream.getServerWriter(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

		// Close
		writer.close();

		// Should only close once
		writer.close();

		// Verify close only once
		this.verifyMockObjects();
	}

	/**
	 * Closed operation {@link Function} interface.
	 */
	private static interface ClosedOperation {
		void run() throws IOException;
	}

	/**
	 * Ensure {@link IOException} itestf closed.
	 */
	public void testEnsureClosed() throws IOException {

		// Close the output
		this.writer.close();

		// Tests that exception on being closed
		Consumer<ClosedOperation> test = (operation) -> {
			try {
				operation.run();
				fail("Should not be successful");
			} catch (IOException ex) {
				assertEquals("Incorrect cause", "Stream closed", ex.getMessage());
			}
		};

		// Ensure closed
		test.accept(() -> this.writer.flush());
		test.accept(() -> this.writer.append((char) 1));
		test.accept(() -> this.writer.append("2"));
		test.accept(() -> this.writer.append("3", 0, 1));
		test.accept(() -> this.writer.write(new byte[] { 4 }));
		test.accept(() -> this.writer.write(ByteBuffer.wrap(new byte[] { 5 })));
		test.accept(() -> this.writer.write(6));
		test.accept(() -> this.writer.write(new char[] { 7 }));
		test.accept(() -> this.writer.write("9"));
		test.accept(() -> this.writer.write(new char[] { 9 }, 0, 1));
		test.accept(() -> this.writer.write("10", 0, 2));
	}

}