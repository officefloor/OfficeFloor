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

package net.officefloor.server.http.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.BufferPoolServerOutputStream;

/**
 * Tests the {@link MockStreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockStreamBufferPoolTest {

	/**
	 * Size of the {@link StreamBuffer}.
	 */
	private static final int BUFFER_SIZE = 4;

	/**
	 * {@link MockStreamBufferPool}.
	 */
	private final MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(BUFFER_SIZE));

	/**
	 * {@link ServerOutputStream} to write data to buffers.
	 */
	private final BufferPoolServerOutputStream<ByteBuffer> output = new BufferPoolServerOutputStream<>(this.pool);

	/**
	 * Ensure can release pooled {@link StreamBuffer} back to
	 * {@link MockStreamBufferPool}.
	 */
	@Test
	public void releasePooledStreamBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();
		assertNotNull(buffer.pooledBuffer, "Should be pooled");

		// Ensure issue if not returned to pool
		try {
			this.pool.assertAllBuffersReturned();
		} catch (AssertionError ex) {
			assertTrue(ex.getMessage().startsWith("Buffer 0 (of 1) should be released"), "Incorrect failure");
		}

		// Release to pool
		buffer.release();
		this.pool.assertAllBuffersReturned();
	}

	/**
	 * Ensure can release unpooled {@link StreamBuffer} back to
	 * {@link MockStreamBufferPool}.
	 */
	@Test
	public void releaseUnpooledStreamBuffer() {

		// Obtain the read-only buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getUnpooledStreamBuffer(ByteBuffer.allocate(4));
		assertNotNull(buffer.unpooledByteBuffer, "Should be unpooled");

		// Ensure issue if not returned to pool
		try {
			this.pool.assertAllBuffersReturned();
		} catch (AssertionError ex) {
			assertTrue(ex.getMessage().startsWith("Buffer 0 (of 1) should be released"), "Incorrect failure");
		}

		// Release
		buffer.release();
		this.pool.assertAllBuffersReturned();
	}

	/**
	 * Ensure can release file {@link StreamBuffer} back to
	 * {@link MockStreamBufferPool}.
	 */
	@Test
	public void releaseFileStreamBuffer() throws IOException {

		// Obtain the read-only buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getFileStreamBuffer(
				TemporaryFiles.getDefault().createTempFile("ReleaseFileBuffer", "contest"), 0, -1, null);
		assertNotNull(buffer.fileBuffer, "Should be file");

		// Ensure issue if not returned to pool
		try {
			this.pool.assertAllBuffersReturned();
		} catch (AssertionError ex) {
			assertTrue(ex.getMessage().startsWith("Buffer 0 (of 1) should be released"), "Incorrect failure");
		}

		// Release
		buffer.release();
		this.pool.assertAllBuffersReturned();
	}

	/**
	 * Ensure can write to the {@link StreamBuffer}.
	 */
	@Test
	public void writeToBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Ensure buffer initialised to zero
		ByteBuffer data = buffer.pooledBuffer;
		assertEquals(BUFFER_SIZE, data.capacity(), "Incorrect data size");
		assertEquals(0, BufferJvmFix.position(data), "Should be no data");

		// Write bytes to buffer
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertTrue(buffer.write((byte) i), "Should be able to write byte " + i);
		}

		// Buffer full, so should not be able write another byte
		assertFalse(buffer.write((byte) BUFFER_SIZE), "Should not be able to write byte to full buffer");

		// Ensure the buffer contains the data
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i, data.get(i), "Incorrect byte value");
		}
	}

	/**
	 * Ensure can bulk write to buffer.
	 */
	@Test
	public void bulkWriteToBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Bulk write to data
		byte[] write = new byte[BUFFER_SIZE];
		for (int i = 0; i < write.length; i++) {
			write[i] = (byte) i;
		}

		// Bulk write the data
		assertEquals(write.length, buffer.write(write, 0, write.length), "Should write all data");

		// Ensure not able to write further data
		assertEquals(0, buffer.write(write, 0, write.length), "Buffer should now be full");

		// Ensure the buffer contains the data
		ByteBuffer data = buffer.pooledBuffer;
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i, data.get(i), "Incorrect byte value");
		}
	}

	/**
	 * Ensure can underwrite the {@link StreamBuffer}.
	 */
	@Test
	public void underwriteBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Write to the buffer
		buffer.write((byte) 1);
		byte[] furtherData = new byte[] { 2, 3 };
		buffer.write(furtherData);

		// Ensure the data is correct
		ByteBuffer data = buffer.pooledBuffer;
		for (int i = 0; i < 3; i++) {
			assertEquals(i + 1, data.get(i), "Incorrect byte value");
		}
		for (int i = 3; i < BUFFER_SIZE; i++) {
			assertEquals(0, data.get(i), "Incorrect unset bytes");
		}
	}

	/**
	 * Ensure can overwrite the {@link StreamBuffer}.
	 */
	@Test
	public void overwriteBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Write twice the data length
		byte[] write = new byte[BUFFER_SIZE * 2];
		for (int i = 0; i < write.length; i++) {
			write[i] = (byte) i;
		}

		// Bulk write the data
		assertEquals(BUFFER_SIZE, buffer.write(write, 0, write.length), "Should write just the capacity");

		// Ensure not able to write further data
		assertEquals(0, buffer.write(write, BUFFER_SIZE, write.length - BUFFER_SIZE), "Buffer should now be full");

		// Ensure the buffer contains the data
		ByteBuffer data = buffer.pooledBuffer;
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i, data.get(i), "Incorrect byte value");
		}
	}

	/**
	 * Ensure can input stream the buffer data.
	 */
	@Test
	public void outputByte() throws IOException {

		// Write a single byte
		this.output.write((byte) 1);

		// Create the input stream to read in content
		InputStream input = MockStreamBufferPool.createInputStream(this.output.getBuffers());
		assertEquals(1, input.read(), "Should read the single byte");
		assertEquals(-1, input.read(), "Should now be end of stream");
	}

	/**
	 * Ensure can output a large stream of bytes.
	 */
	@Test
	public void outputLargeStreamOfBytes() throws IOException {

		final int REPEATS = 100;

		// Create the large output stream
		StringBuilder largeString = new StringBuilder();
		for (int i = 0; i < REPEATS; i++) {
			largeString.append("TEST_" + i + ",");
		}

		// Write large output stream
		OutputStreamWriter writer = new OutputStreamWriter(this.output,
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		writer.write(largeString.toString());
		writer.flush();

		// Ensure can read in the large string
		InputStream input = MockStreamBufferPool.createInputStream(this.output.getBuffers());
		InputStreamReader reader = new InputStreamReader(input, ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);

		// Read in the text
		StringWriter response = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader.read()) {
			response.write(character);
		}

		// Ensure able to retrieve the content
		assertEquals(largeString.toString(), response.toString(), "Incorrect response");
	}

	/**
	 * Ensure can interlace writing bytes and {@link ByteBuffer} instances.
	 */
	@Test
	public void interlaceBytesAndByteBuffers() throws IOException {

		final int REPEATS = 100;

		// Create the repeated inputs
		byte[] TEST = "TEST_".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		byte comma = ",".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET)[0];

		// Write the output (captured the expected string)
		StringWriter expected = new StringWriter();
		for (int i = 0; i < REPEATS; i++) {

			// Write the expected content
			expected.write("TEST_");
			expected.write(String.valueOf(i));
			expected.write(",");

			// Write the actual content
			this.output.write(TEST);
			this.output.write(
					ByteBuffer.wrap(String.valueOf(i).getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET)));
			this.output.write(comma);
		}

		// Read in the text
		InputStream input = MockStreamBufferPool.createInputStream(this.output.getBuffers());
		InputStreamReader reader = new InputStreamReader(input, ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		StringWriter response = new StringWriter();
		for (int character = reader.read(); character != -1; character = reader.read()) {
			response.write(character);
		}

		// Ensure correct response
		assertEquals(expected.toString(), response.toString(), "Incorrect response");
	}

}
