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
package net.officefloor.server.http.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.BufferPoolServerOutputStream;

/**
 * Tests the {@link MockStreamBufferPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockStreamBufferPoolTest extends OfficeFrameTestCase {

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
	public void testReleasePooledStreamBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();
		assertTrue("Should not be pooled", buffer.isPooled);

		// Ensure issue if not returned to pool
		try {
			this.pool.assertAllBuffersReturned();
		} catch (AssertionError ex) {
			assertTrue("Incorrect failure", ex.getMessage().startsWith("Buffer 0 (of 1) should be released"));
		}

		// Release to pool
		buffer.release();
		this.pool.assertAllBuffersReturned();
	}

	/**
	 * Ensure can release unpooled {@link StreamBuffer} back to
	 * {@link MockStreamBufferPool}.
	 */
	public void testReleaseUnpooledStreamBuffer() {

		// Obtain the read-only buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getUnpooledStreamBuffer(ByteBuffer.allocate(4));
		assertFalse("Should be unpooled", buffer.isPooled);

		// Ensure issue if not returned to pool
		try {
			this.pool.assertAllBuffersReturned();
		} catch (AssertionError ex) {
			assertTrue("Incorrect failure", ex.getMessage().startsWith("Buffer 0 (of 1) should be released"));
		}

		// Release
		buffer.release();
		this.pool.assertAllBuffersReturned();
	}

	/**
	 * Ensure can write to the {@link StreamBuffer}.
	 */
	public void testWriteToBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Ensure buffer initialised to zero
		ByteBuffer data = buffer.pooledBuffer;
		assertEquals("Incorrect data size", BUFFER_SIZE, data.capacity());
		assertEquals("Should be no data", 0, data.position());

		// Write bytes to buffer
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertTrue("Should be able to write byte " + i, buffer.write((byte) i));
		}

		// Buffer full, so should not be able write another byte
		assertFalse("Should not be able to write byte to full buffer", buffer.write((byte) BUFFER_SIZE));

		// Ensure the buffer contains the data
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals("Incorrect byte value", i, data.get(i));
		}
	}

	/**
	 * Ensure can bulk write to buffer.
	 */
	public void testBulkWriteToBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Bulk write to data
		byte[] write = new byte[BUFFER_SIZE];
		for (int i = 0; i < write.length; i++) {
			write[i] = (byte) i;
		}

		// Bulk write the data
		assertEquals("Should write all data", write.length, buffer.write(write, 0, write.length));

		// Ensure not able to write further data
		assertEquals("Buffer should now be full", 0, buffer.write(write, 0, write.length));

		// Ensure the buffer contains the data
		ByteBuffer data = buffer.pooledBuffer;
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals("Incorrect byte value", i, data.get(i));
		}
	}

	/**
	 * Ensure can underwrite the {@link StreamBuffer}.
	 */
	public void testUnderwriteBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Write to the buffer
		buffer.write((byte) 1);
		byte[] furtherData = new byte[] { 2, 3 };
		buffer.write(furtherData);

		// Ensure the data is correct
		ByteBuffer data = buffer.pooledBuffer;
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect byte value", i + 1, data.get(i));
		}
		for (int i = 3; i < BUFFER_SIZE; i++) {
			assertEquals("Incorrect unset bytes", 0, data.get(i));
		}
	}

	/**
	 * Ensure can overwrite the {@link StreamBuffer}.
	 */
	public void testOverwriteBuffer() {

		// Obtain the writable buffer
		StreamBuffer<ByteBuffer> buffer = this.pool.getPooledStreamBuffer();

		// Write twice the data length
		byte[] write = new byte[BUFFER_SIZE * 2];
		for (int i = 0; i < write.length; i++) {
			write[i] = (byte) i;
		}

		// Bulk write the data
		assertEquals("Should write just the capacity", BUFFER_SIZE, buffer.write(write, 0, write.length));

		// Ensure not able to write further data
		assertEquals("Buffer should now be full", 0, buffer.write(write, BUFFER_SIZE, write.length - BUFFER_SIZE));

		// Ensure the buffer contains the data
		ByteBuffer data = buffer.pooledBuffer;
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals("Incorrect byte value", i, data.get(i));
		}
	}

	/**
	 * Ensure can input stream the buffer data.
	 */
	public void testOutputByte() throws IOException {

		// Write a single byte
		this.output.write((byte) 1);

		// Create the input stream to read in content
		InputStream input = MockStreamBufferPool.createInputStream(this.output.getBuffers());
		assertEquals("Should read the single byte", 1, input.read());
		assertEquals("Should now be end of stream", -1, input.read());
	}

	/**
	 * Ensure can output a large stream of bytes.
	 */
	public void testOutputLargeStreamOfBytes() throws IOException {

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
		assertEquals("Incorrect response", largeString.toString(), response.toString());
	}

	/**
	 * Ensure can interlace writing bytes and {@link ByteBuffer} instances.
	 */
	public void testInterlaceBytesAndByteBuffers() throws IOException {

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
		assertEquals("Incorrect response", expected.toString(), response.toString());
	}

}