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

package net.officefloor.server.stream.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.http.stream.TemporaryFiles;
import net.officefloor.server.stream.BufferJvmFix;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBuffer.FileBuffer;

/**
 * Tests the {@link BufferPoolServerOutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class BufferPoolServerOutputStreamTest extends OfficeFrameTestCase {

	/**
	 * {@link MockStreamBufferPool}.
	 */
	private final MockStreamBufferPool bufferPool = new MockStreamBufferPool(() -> ByteBuffer.allocate(4));

	/**
	 * {@link BufferPoolServerOutputStream} to test.
	 */
	private final BufferPoolServerOutputStream<ByteBuffer> outputStream = new BufferPoolServerOutputStream<>(
			this.bufferPool);

	/**
	 * Ensure can write a single byte.
	 */
	public void testSingleByte() throws IOException {
		this.outputStream.write(1);
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer, 1));
	}

	/**
	 * Ensure can write no bytes.
	 */
	public void testNoBytes() throws IOException {
		this.outputStream.write(new byte[] {});
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer));
	}

	/**
	 * Ensure can fill {@link StreamBuffer} without requiring a further
	 * {@link StreamBuffer}.
	 */
	public void testFillBuffer() throws IOException {
		this.outputStream.write(new byte[] { 1, 2, 3, 4 });
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer, 1, 2, 3, 4));
	}

	/**
	 * Ensure can add further {@link StreamBuffer} instances as outputting.
	 */
	public void testMultipleBuffers() throws IOException {
		this.outputStream.write(new byte[] { 1, 2, 3, 4, 5, 6 });
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer, 1, 2, 3, 4),
				(buffer) -> assertPooledBuffer(buffer, 5, 6));
	}

	/**
	 * Ensure can interlace {@link ByteBuffer} into the output stream.
	 */
	public void testInterlaceByteBuffer() throws IOException {
		this.outputStream.write(new byte[] { 1, 2, 3, 4 });
		this.outputStream.write(ByteBuffer.wrap(new byte[] { 5, 6, 7, 8, 9, 10 }).duplicate());
		this.outputStream.write(new byte[] { 11, 12, 13, 14 });
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer, 1, 2, 3, 4),
				(buffer) -> assertUnpooledBuffer(buffer, 5, 6, 7, 8, 9, 10),
				(buffer) -> assertPooledBuffer(buffer, 11, 12, 13, 14));
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
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer, 1, 2, 3), (buffer) -> assertUnpooledBuffer(buffer, 4),
				(buffer) -> assertPooledBuffer(buffer, 5, 6, 7, 8), (buffer) -> assertPooledBuffer(buffer, 9),
				(buffer) -> assertUnpooledBuffer(buffer, 10), (buffer) -> assertUnpooledBuffer(buffer, 11),
				(buffer) -> assertPooledBuffer(buffer, 12));
	}

	/**
	 * Ensure can interlace {@link FileBuffer} into the output stream.
	 */
	public void testInterlaceFileBuffer() throws IOException {
		this.outputStream.write(new byte[] { 1, 2, 3, 4 });
		this.outputStream.write(
				TemporaryFiles.getDefault().createTempFile("testInterlaceFileBuffer", new byte[] { 5, 6, 7, 8, 9, 10 }),
				null);
		this.outputStream.write(new byte[] { 11, 12, 13, 14 });
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer, 1, 2, 3, 4),
				(buffer) -> assertFileBuffer(buffer, 5, 6, 7, 8, 9, 10),
				(buffer) -> assertPooledBuffer(buffer, 11, 12, 13, 14));
	}

	/**
	 * Ensure can interlace {@link FileBuffer} into the output stream, when data
	 * does not line up to end of {@link StreamBuffer}.
	 */
	public void testInterlaceFileBufferNotOnBufferBoundary() throws IOException {
		this.outputStream.write(new byte[] { 1, 2 });
		this.outputStream.write(new byte[] { 3 });
		this.outputStream.write(TemporaryFiles.getDefault().createTempFile("testInterlaceFileBufferNotOnBufferBoundary",
				new byte[] { 4 }), null);
		this.outputStream.write(new byte[] { 5, 6, 7, 8, 9 });
		this.outputStream.write(TemporaryFiles.getDefault().createTempFile("testInterlaceFileBufferNotOnBufferBoundary",
				new byte[] { 10 }), null);
		this.outputStream.write(TemporaryFiles.getDefault().createTempFile("testInterlaceFileBufferNotOnBufferBoundary",
				new byte[] { 11 }), null);
		this.outputStream.write(new byte[] { 12 });
		this.assertBuffers((buffer) -> assertPooledBuffer(buffer, 1, 2, 3), (buffer) -> assertFileBuffer(buffer, 4),
				(buffer) -> assertPooledBuffer(buffer, 5, 6, 7, 8), (buffer) -> assertPooledBuffer(buffer, 9),
				(buffer) -> assertFileBuffer(buffer, 10), (buffer) -> assertFileBuffer(buffer, 11),
				(buffer) -> assertPooledBuffer(buffer, 12));
	}

	/**
	 * Ensure can clear the {@link BufferPoolServerOutputStream}.
	 */
	public void testClear() throws IOException {

		FileChannel stayOpenFile = TemporaryFiles.getDefault().createTempFile("testClear_leaveOpen", new byte[] { 9 });
		FileChannel closeFile = TemporaryFiles.getDefault().createTempFile("testClear_close", new byte[] { 10 });

		// Write content
		this.outputStream.write(new byte[] { 1, 2, 3, 4, 5 });
		this.outputStream.write(ByteBuffer.wrap(new byte[] { 6, 7, 8 }));
		this.outputStream.write(stayOpenFile, null);
		this.outputStream.write(closeFile, (file, isWritten) -> file.close());
		this.outputStream.write(11);
		assertEquals("Incorrect content length", 11, this.outputStream.getContentLength());

		// Clear the output
		this.outputStream.clear();

		// Ensure the files are open/closed appropriately
		assertTrue("File should still be open", stayOpenFile.isOpen());
		assertFalse("File should be closed on clear", closeFile.isOpen());

		// Ensure all buffers released
		this.bufferPool.assertAllBuffersReturned();
		assertEquals("Should be no content", 0, this.outputStream.getContentLength());

		// Ensure can write some further content
		this.outputStream.write(12);
		assertEquals("Should start length incrementing again", 1, this.outputStream.getContentLength());

		// Ensure only data after the clear
		MockStreamBufferPool.releaseStreamBuffers(this.outputStream.getBuffers());
		InputStream input = MockStreamBufferPool.createInputStream(this.outputStream.getBuffers());
		assertEquals("Incorrect value after clear", 12, input.read());
		assertEquals("Should be only the byte", -1, input.read());
	}

	/**
	 * Ensure triggers close.
	 */
	public void testClose() throws IOException {

		final CloseHandler handler = this.createMock(CloseHandler.class);
		final BufferPoolServerOutputStream<ByteBuffer> outputStream = new BufferPoolServerOutputStream<>(
				this.bufferPool, handler);

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
		test.accept(() -> this.outputStream
				.write(TemporaryFiles.getDefault().createTempFile("testEnsureClosed", "not written"), null));
		test.accept(() -> this.outputStream.write(3));
		test.accept(() -> this.outputStream.write(new byte[] { 4 }, 0, 1));
	}

	/**
	 * Validates the {@link StreamBuffer} instances from the
	 * {@link BufferPoolServerOutputStream}.
	 * 
	 * @param validators Listing of validators for each {@link StreamBuffer}.
	 */
	@SafeVarargs
	private final void assertBuffers(Function<StreamBuffer<ByteBuffer>, Integer>... validators) {
		StreamBuffer<ByteBuffer> buffer = this.outputStream.getBuffers();
		int expectedTotalBytes = 0;
		for (Function<StreamBuffer<ByteBuffer>, Integer> validator : validators) {
			expectedTotalBytes += validator.apply(buffer);
			buffer = buffer.next;
		}
		assertNull("Incorrect number of buffers", buffer); // move past end
		assertEquals("Incorrect Content-Length", expectedTotalBytes, this.outputStream.getContentLength());
	}

	/**
	 * Asserts the {@link StreamBuffer} content.
	 * 
	 * @param buffer        {@link StreamBuffer}.
	 * @param expectedBytes Expected bytes.
	 * @return Number of bytes in buffer.
	 */
	private static int assertPooledBuffer(StreamBuffer<ByteBuffer> buffer, int... expectedBytes) {
		assertNotNull("Should be pooled buffer", buffer.pooledBuffer);
		ByteBuffer data = buffer.pooledBuffer;
		assertTrue("Buffer should be larger than expected bytes", BufferJvmFix.position(data) >= expectedBytes.length);
		for (int i = 0; i < expectedBytes.length; i++) {
			assertEquals("Incorrect byte " + i, expectedBytes[i], data.get(i));
		}
		for (int i = expectedBytes.length; i < BufferJvmFix.position(data); i++) {
			assertEquals("Rest of buffer empty for byte " + i, 0, data.get(i));
		}
		return expectedBytes.length;
	}

	/**
	 * Asserts the {@link StreamBuffer} content.
	 * 
	 * @param buffer        {@link StreamBuffer}.
	 * @param expectedBytes Expected bytes.
	 * @return Number of bytes in buffer.
	 */
	private static int assertUnpooledBuffer(StreamBuffer<ByteBuffer> buffer, int... expectedBytes) {
		assertNotNull("Should be unpooled buffer", buffer.unpooledByteBuffer);
		ByteBuffer byteBuffer = buffer.unpooledByteBuffer;
		assertEquals("Incorrect number of bytes", expectedBytes.length, byteBuffer.remaining());
		for (int i = 0; i < expectedBytes.length; i++) {
			assertEquals("Incorrect byte " + i, expectedBytes[i], byteBuffer.get());
		}
		return expectedBytes.length;
	}

	/**
	 * Asserts the {@link StreamBuffer} content.
	 * 
	 * @param buffer        {@link StreamBuffer}.
	 * @param expectedBytes Expected bytes.
	 * @return Number of bytes in buffer.
	 */
	private static int assertFileBuffer(StreamBuffer<ByteBuffer> buffer, int... expectedBytes) {
		try {
			assertNotNull("Should be file buffer", buffer.fileBuffer);
			FileBuffer fileBuffer = buffer.fileBuffer;
			long count = (fileBuffer.count < 0) ? fileBuffer.file.size() : fileBuffer.count;
			assertEquals("Incorrect number of bytes", expectedBytes.length, count);
			ByteBuffer input = ByteBuffer.allocate(1);
			for (int i = 0; i < expectedBytes.length; i++) {
				BufferJvmFix.clear(input);
				assertEquals("Failed to read byte " + i, 1, fileBuffer.file.read(input));
				assertEquals("Incorrect byte " + i, expectedBytes[i], input.get(0));
			}
			return expectedBytes.length;
		} catch (IOException ex) {
			throw fail(ex);
		}
	}

}
