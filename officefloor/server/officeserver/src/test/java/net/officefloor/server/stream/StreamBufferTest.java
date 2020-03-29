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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.http.stream.TemporaryFiles;

/**
 * Tests the write functionality of the {@link StreamBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferTest extends OfficeFrameTestCase {

	/**
	 * Size of the buffers.
	 */
	private static int BUFFER_SIZE = 2;

	/**
	 * {@link StreamBufferPool}.
	 */
	private final MockStreamBufferPool bufferPool = new MockStreamBufferPool(() -> ByteBuffer.allocate(BUFFER_SIZE));

	/**
	 * Linked list head {@link StreamBuffer}.
	 */
	private final StreamBuffer<ByteBuffer> head = this.bufferPool.getPooledStreamBuffer();

	/**
	 * Ensure can write a single byte.
	 */
	public void testWriteByte() {
		this.write(1);
		this.assertBytes(1);
	}

	/**
	 * Ensure fill head buffer (without creating another buffer).
	 */
	public void testFillHeadBuffer() {
		this.write(1, 2);
		this.assertBytes(1, 2);
		assertEquals("Should fill buffer", 0, this.head.pooledBuffer.remaining());
		assertNull("Should not create another buffer", this.head.next);
	}

	/**
	 * Ensure can write beyond head buffer.
	 */
	public void testWriteBeyondHeader() {
		this.write(1, 2, 3);
		this.assertBytes(1, 2, 3);
		assertNotNull("Should create another buffer", this.head.next);
	}

	/**
	 * Ensure append stream buffers if headers full (even if remaining - as may be
	 * due to unpooled buffer).
	 */
	public void testWriteOnlyToLastBuffer() {
		this.head.next = this.bufferPool.getPooledStreamBuffer();
		this.head.next.next = this.bufferPool.getPooledStreamBuffer();
		this.write(1);
		assertEquals("Not write to first buffer", 0, BufferJvmFix.position(this.head.pooledBuffer));
		assertEquals("Not write to second buffer", 0, BufferJvmFix.position(this.head.next.pooledBuffer));
		assertEquals("Should write only to last buffer", 1, BufferJvmFix.position(this.head.next.next.pooledBuffer));
		assertNull("Should not append buffer", this.head.next.next.next);
		this.assertBytes(1);
	}

	/**
	 * Ensure if last buffer is unpooled, that a pooled buffer is added.
	 */
	public void testNotWriteUnpooledBuffer() {
		this.head.next = this.bufferPool.getUnpooledStreamBuffer(ByteBuffer.wrap(new byte[] { 1 }));
		this.write(2);
		assertEquals("Should not write to head", 0, BufferJvmFix.position(this.head.pooledBuffer));
		assertNotNull("Should append pooled buffer", this.head.next.next);
		assertNotNull("Should be pooled buffer appended", this.head.next.next.pooledBuffer);
		this.assertBytes(1, 2);
	}

	/**
	 * Ensure if last buffer is file, that a pooled buffer is added.
	 */
	public void testNotWriteFileBuffer() throws IOException {
		this.head.next = this.bufferPool
				.getFileStreamBuffer(TemporaryFiles.getDefault().createTempFile("test", new byte[] { 1 }), 0, -1, null);
		this.write(2);
		assertEquals("Should not write to head", 0, BufferJvmFix.position(this.head.pooledBuffer));
		assertNotNull("Should append pooled buffer", this.head.next.next);
		assertNotNull("Should be pooled buffer appended", this.head.next.next.pooledBuffer);
		this.assertBytes(1, 2);
	}

	/**
	 * Ensure can write offset of bytes.
	 */
	public void testWriteOffsetOfBytes() {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		StreamBuffer.write(bytes, 3, 4, this.head, this.bufferPool);
		this.assertBytes(4, 5, 6, 7);
	}

	/**
	 * Ensure can write {@link CharSequence}.
	 */
	public void testCharSequence() {
		final String text = "AbCd09";
		StreamBuffer.write(text, this.head, this.bufferPool);
		this.assertBytes(text.getBytes(Charset.forName("US-ASCII")));
	}

	/**
	 * Ensure can write sub sequence of {@link CharSequence}.
	 */
	public void testCharSubSequence() {
		final String text = "_ignore_text_ignore_";
		StreamBuffer.write(text, "_ignore_".length(), "text".length(), this.head, this.bufferPool);
		this.assertBytes("text".getBytes(Charset.forName("US-ASCII")));
	}

	/**
	 * Ensure can use {@link Appendable}.
	 */
	public void testAppendable() {
		Instant time = Instant.now();
		String expected = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT")).format(time);
		DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT")).formatTo(time,
				StreamBuffer.getAppendable(this.head, this.bufferPool));
		this.assertBytes(expected.getBytes(Charset.forName("US-ASCII")));
	}

	/**
	 * Ensure can write value.
	 */
	public void testZero() {
		this.doIntegerTest(0);
	}

	/**
	 * Ensure can write value.
	 */
	public void testOne() {
		this.doIntegerTest(1);
	}

	/**
	 * Ensure can write value.
	 */
	public void testTen() {
		this.doIntegerTest(10);
	}

	/**
	 * Ensure can write value.
	 */
	public void testEachDigit() {
		this.doIntegerTest(123456789);
	}

	/**
	 * Ensure can write value.
	 */
	public void testLargeValue() {
		this.doIntegerTest(Long.MAX_VALUE);
	}

	/**
	 * Ensure can write value.
	 */
	public void testNegative() {
		this.doIntegerTest(-1);
	}

	/**
	 * Ensure can write value.
	 */
	public void testWithinBoundLargeNegative() {
		this.doIntegerTest(Long.MIN_VALUE + 1);
	}

	/**
	 * Ensure can write value.
	 */
	public void testLargeNegative() {
		this.doIntegerTest(Long.MIN_VALUE);
	}

	/**
	 * Undertakes test of value.
	 * 
	 * @param value value.
	 */
	private void doIntegerTest(long value) {
		MockStreamBufferPool bufferPool = new MockStreamBufferPool();
		StreamBuffer<ByteBuffer> head = bufferPool.getPooledStreamBuffer();
		StreamBuffer.write(value, head, bufferPool);
		MockStreamBufferPool.releaseStreamBuffers(head);
		String actual = MockStreamBufferPool.getContent(head, ServerHttpConnection.HTTP_CHARSET);
		assertEquals("Incorrect integer", String.valueOf(value), actual);
	}

	/**
	 * Writes bytes to the linked list.
	 * 
	 * @param bytes Bytes to be written.
	 */
	private void write(int... bytes) {
		byte[] data = new byte[bytes.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) bytes[i];
		}
		StreamBuffer.write(data, this.head, this.bufferPool);
	}

	/**
	 * Verifies the bytes.
	 * 
	 * @param bytes Expected bytes.
	 */
	private void assertBytes(byte... bytes) {
		int[] transformed = new int[bytes.length];
		for (int i = 0; i < transformed.length; i++) {
			transformed[i] = bytes[i];
		}
		assertBytes(transformed);
	}

	/**
	 * Verifies the bytes.
	 * 
	 * @param bytes Expected bytes.
	 */
	private void assertBytes(int... bytes) {
		try {
			MockStreamBufferPool.releaseStreamBuffers(this.head);
			InputStream inputStream = MockStreamBufferPool.createInputStream(this.head);
			for (int i = 0; i < bytes.length; i++) {
				assertEquals("Incorrect byte " + i, bytes[i], inputStream.read());
			}
			assertEquals("Should be end of stream", -1, inputStream.read());
		} catch (IOException ex) {
			throw fail(ex);
		}
	}

}
