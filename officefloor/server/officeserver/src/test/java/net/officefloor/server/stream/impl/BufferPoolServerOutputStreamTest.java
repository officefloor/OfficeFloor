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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link BufferPoolServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferPoolServerOutputStreamTest extends OfficeFrameTestCase {

	/**
	 * {@link MockBufferPool}.
	 */
	private final MockBufferPool bufferPool = new MockBufferPool(4);

	/**
	 * {@link BufferPoolServerOutputStream} to test.
	 */
	private final BufferPoolServerOutputStream<byte[]> outputStream = new BufferPoolServerOutputStream<>(
			this.bufferPool);

	/**
	 * Ensure can write a single byte.
	 */
	public void testSingleByte() throws IOException {
		this.outputStream.write(1);
		this.assertBuffers((buffer) -> assertData(buffer, 1));
	}

	/**
	 * Ensure can write no bytes.
	 */
	public void testNoBytes() throws IOException {
		this.outputStream.write(new byte[] {});
		this.assertBuffers((buffer) -> assertData(buffer));
	}

	/**
	 * Ensure can fill {@link StreamBuffer} without requiring a further
	 * {@link StreamBuffer}.
	 */
	public void testFillBuffer() throws IOException {
		this.outputStream.write(new byte[] { 1, 2, 3, 4 });
		this.assertBuffers((buffer) -> assertData(buffer, 1, 2, 3, 4));
	}

	/**
	 * Ensure can add further {@link StreamBuffer} instances as outputting.
	 */
	public void testMultipleBuffers() throws IOException {
		this.outputStream.write(new byte[] { 1, 2, 3, 4, 5, 6 });
		this.assertBuffers((buffer) -> assertData(buffer, 1, 2, 3, 4), (buffer) -> assertData(buffer, 5, 6));
	}

	/**
	 * Ensure can interlace {@link ByteBuffer} into the output stream.
	 */
	public void testInterlaceByteBuffer() throws IOException {
		this.outputStream.write(new byte[] { 1, 2, 3, 4 });
		this.outputStream.write(ByteBuffer.wrap(new byte[] { 5, 6, 7, 8, 9, 10 }).duplicate());
		this.outputStream.write(new byte[] { 11, 12, 13, 14 });
		this.assertBuffers((buffer) -> assertData(buffer, 1, 2, 3, 4),
				(buffer) -> assertByteBuffer(buffer, 5, 6, 7, 8, 9, 10),
				(buffer) -> assertData(buffer, 11, 12, 13, 14));
	}

	/**
	 * Ensure can interlace {@link ByteBuffer} into the output stream, when data
	 * does not line up to end of {@link StreamBuffer}.
	 */
	public void testInterlaceByteBufferNotOnBufferBoundary() throws IOException {
		this.outputStream.write(new byte[] { 1, 2 });
		this.outputStream.write(new byte[] { 3 });
		this.outputStream.write(ByteBuffer.wrap(new byte[] { 4 }).duplicate());
		this.outputStream.write(new byte[] { 5, 6, 7, 8, 9 });
		this.outputStream.write(ByteBuffer.wrap(new byte[] { 10 }).duplicate());
		this.outputStream.write(ByteBuffer.wrap(new byte[] { 11 }).duplicate());
		this.outputStream.write(new byte[] { 12 });
		this.assertBuffers((buffer) -> assertData(buffer, 1, 2, 3), (buffer) -> assertByteBuffer(buffer, 4),
				(buffer) -> assertData(buffer, 5, 6, 7, 8), (buffer) -> assertData(buffer, 9),
				(buffer) -> assertByteBuffer(buffer, 10), (buffer) -> assertByteBuffer(buffer, 11),
				(buffer) -> assertData(buffer, 12));
	}

	/**
	 * Ensure triggers close.
	 */
	public void testClose() throws IOException {

		final CloseHandler handler = this.createMock(CloseHandler.class);
		final BufferPoolServerOutputStream<byte[]> outputStream = new BufferPoolServerOutputStream<>(this.bufferPool,
				handler);

		// Record close only once
		this.recordReturn(handler, handler.isClosed(), false);
		handler.close();
		this.recordReturn(handler, handler.isClosed(), true);

		// Replay
		this.replayMockObjects();

		// Close
		outputStream.close();

		// Should only close once
		outputStream.close();

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
		this.outputStream.close();

		// Tests that exception on being closed
		Consumer<ClosedOperation> test = (operation) -> {
			try {
				operation.run();
				fail("Should not be successful");
			} catch (IOException ex) {
				assertEquals("Incorrect cause", "Closed", ex.getMessage());
			}
		};

		// Ensure closed
		test.accept(() -> this.outputStream.flush());
		test.accept(() -> this.outputStream.getServerWriter(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
		test.accept(() -> this.outputStream.write(new byte[] { 1 }));
		test.accept(() -> this.outputStream.write(ByteBuffer.wrap(new byte[] { 2 })));
		test.accept(() -> this.outputStream.write(3));
		test.accept(() -> this.outputStream.write(new byte[] { 4 }, 0, 1));
	}

	/**
	 * Validates the {@link StreamBuffer} instances from the
	 * {@link BufferPoolServerOutputStream}.
	 * 
	 * @param validators
	 *            Listing of validators for each {@link StreamBuffer}.
	 */
	@SafeVarargs
	private final void assertBuffers(Function<StreamBuffer<byte[]>, Integer>... validators) {
		List<StreamBuffer<byte[]>> buffers = this.outputStream.getBuffers();
		assertEquals("Incorrect number of buffers", validators.length, buffers.size());
		Iterator<StreamBuffer<byte[]>> bufferIterator = buffers.iterator();
		int expectedTotalBytes = 0;
		for (Function<StreamBuffer<byte[]>, Integer> validator : validators) {
			StreamBuffer<byte[]> buffer = bufferIterator.next();
			expectedTotalBytes += validator.apply(buffer);
		}
		assertEquals("Incorrect Content-Length", expectedTotalBytes, this.outputStream.getContentLength());
	}

	/**
	 * Asserts the {@link StreamBuffer} content.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer}.
	 * @param expectedBytes
	 *            Expected bytes.
	 * @return Number of bytes in buffer.
	 */
	private static int assertData(StreamBuffer<byte[]> buffer, int... expectedBytes) {
		assertTrue("Should be pooled buffer", buffer.isPooled());
		byte[] data = buffer.getPooledBuffer();
		assertTrue("Buffer should be larger than expected bytes", data.length >= expectedBytes.length);
		for (int i = 0; i < expectedBytes.length; i++) {
			assertEquals("Incorrect byte " + i, expectedBytes[i], data[i]);
		}
		for (int i = expectedBytes.length; i < data.length; i++) {
			assertEquals("Rest of buffer empty for byte " + i, 0, data[i]);
		}
		return expectedBytes.length;
	}

	/**
	 * Asserts the {@link StreamBuffer} content.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer}.
	 * @param expectedBytes
	 *            Expected bytes.
	 * @return Number of bytes in buffer.
	 */
	private static int assertByteBuffer(StreamBuffer<byte[]> buffer, int... expectedBytes) {
		assertFalse("Should be unpooled buffer", buffer.isPooled());
		ByteBuffer byteBuffer = buffer.getUnpooledByteBuffer();
		assertEquals("Incorrect number of bytes", expectedBytes.length, byteBuffer.remaining());
		for (int i = 0; i < expectedBytes.length; i++) {
			assertEquals("Incorrect byte " + i, expectedBytes[i], byteBuffer.get());
		}
		return expectedBytes.length;
	}

}