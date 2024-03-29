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

package net.officefloor.server.stream.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;

/**
 * Tests the {@link ServerWriter} provided by the
 * {@link BufferPoolServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class BufferPoolServerWriterTest {

	/**
	 * {@link MockTestSupport}.
	 */
	private final MockTestSupport mocks = new MockTestSupport();

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

	@BeforeEach
	protected void setUp() throws Exception {

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
	@Test
	public void simpleText() throws IOException {
		this.writer.write("Hello World");
		this.writer.flush();
		assertEquals("Hello World", this.getContent(), "Incorrect written data");
	}

	/**
	 * Ensure can write/read UTF-16.
	 */
	@Test
	public void writeReadUTF16() throws IOException {
		Charset charset = Charset.forName("UTF-16");

		// Write out the data
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(buffer, charset);
		writer.write("Hello World");
		writer.flush();

		// Ensure the data is correct
		byte[] expectedData = "Hello World".getBytes(charset);
		byte[] actualData = buffer.toByteArray();
		assertEquals(expectedData.length, actualData.length, "Incorrect data length");
		for (int i = 0; i < expectedData.length; i++) {
			assertEquals(expectedData[i], actualData[i], "Incorrect byte " + i);
		}
		assertEquals("Hello World", new String(actualData, charset), "Incorrect content");

		// Ensure can read in data
		StringWriter result = new StringWriter();
		InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(actualData), charset);
		for (int character = reader.read(); character != -1; character = reader.read()) {
			result.write(character);
		}
		assertEquals("Hello World", result.toString(), "Incorrect write/read UTF-16 content");
	}

	/**
	 * Ensure can write in different {@link Charset}.
	 */
	@Test
	public void UTF16() throws IOException {

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
			assertEquals(expectedInput.read(), actualInput.read(),
					"Incorrect byte " + i + "(" + Character.valueOf((char) expectedData[i]) + ")");
		}
		assertEquals(-1, actualInput.read(), "Should be end of input data");

		// Ensure stream data
		StringWriter result = new StringWriter();
		InputStreamReader reader = new InputStreamReader(MockStreamBufferPool.createInputStream(headBuffer), charset);
		for (int character = reader.read(); character != -1; character = reader.read()) {
			result.write(character);
		}
		assertEquals("Hello World", result.toString(), "Incorrect write/read UTF-16 content");

		// Ensure can read in content
		String content = MockStreamBufferPool.getContent(headBuffer, charset);
		assertEquals("Hello World", content, "Incorrect written content");
	}

	/**
	 * Ensure can write bytes.
	 */
	@Test
	public void bytes() throws IOException {
		this.writer.write("TEST-");
		this.writer.write("bytes".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
		this.writer.write('.');
		this.writer.flush();
		assertEquals("TEST-bytes.", this.getContent(), "Incorrect written data");
	}

	/**
	 * Ensure can write {@link ByteBuffer}.
	 */
	@Test
	public void byteBuffer() throws IOException {
		this.writer.write("TEST-");
		this.writer.write(
				ByteBuffer.wrap("buffer".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET)).duplicate());
		this.writer.write(new char[] { '.' });
		this.writer.flush();
		assertEquals("TEST-buffer.", this.getContent(), "Incorrect written data");
	}

	/**
	 * Ensure can write {@link FileBuffer}.
	 */
	@Test
	public void fileBuffer() throws IOException {
		this.writer.write("TEST-");
		this.writer.write(TemporaryFiles.getDefault().createTempFile("testFileBuffer", "buffer"), null);
		this.writer.write(new char[] { '.' });
		this.writer.flush();
		assertEquals("TEST-buffer.", this.getContent(), "Incorrect written data");
	}

	/**
	 * Ensure triggers close.
	 */
	@Test
	public void close() throws IOException {

		final CloseHandler handler = this.mocks.createMock(CloseHandler.class);
		@SuppressWarnings("resource")
		BufferPoolServerOutputStream<ByteBuffer> outputStream = new BufferPoolServerOutputStream<>(this.bufferPool,
				handler);

		// Record close only once
		this.mocks.recordReturn(handler, handler.isClosed(), false); // ServerWriter
		this.mocks.recordReturn(handler, handler.isClosed(), false); // writer close
		this.mocks.recordReturn(handler, handler.isClosed(), false); // outputStream
		if (JavaFacet.isSupported((context) -> context.getFeature() >= 15)) {
			this.mocks.recordReturn(handler, handler.isClosed(), false); // additional check
		}
		handler.close();
		this.mocks.recordReturn(handler, handler.isClosed(), true);

		// Replay
		this.mocks.replayMockObjects();

		// Obtain the writer
		final ServerWriter writer = outputStream.getServerWriter(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

		// Close
		writer.close();

		// Should only close once
		writer.close();

		// Verify close only once
		this.mocks.verifyMockObjects();
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
	@Test
	public void ensureClosed() throws IOException {

		// Close the output
		this.writer.close();

		// Tests that exception on being closed
		Consumer<ClosedOperation> test = (operation) -> {
			try {
				operation.run();
				fail("Should not be successful");
			} catch (IOException ex) {
				assertEquals("Stream closed", ex.getMessage(), "Incorrect cause");
			}
		};

		// Ensure closed
		test.accept(() -> this.writer.flush());
		test.accept(() -> this.writer.append((char) 1));
		test.accept(() -> this.writer.append("2"));
		test.accept(() -> this.writer.append("3", 0, 1));
		test.accept(() -> this.writer.write(new byte[] { 4 }));
		test.accept(() -> this.writer.write(ByteBuffer.wrap(new byte[] { 5 })));
		test.accept(() -> this.writer
				.write(TemporaryFiles.getDefault().createTempFile("testEnsureClosed", "not written"), null));
		test.accept(() -> this.writer.write(6));
		test.accept(() -> this.writer.write(new char[] { 7 }));
		test.accept(() -> this.writer.write("9"));
		test.accept(() -> this.writer.write(new char[] { 9 }, 0, 1));
		test.accept(() -> this.writer.write("10", 0, 2));
	}

}
