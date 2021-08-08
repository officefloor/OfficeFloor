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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.http.stream.TemporaryFiles;

/**
 * Tests the write functionality of the {@link StreamBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferTest {

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
	@Test
	public void writeByte() throws IOException {
		this.write(1);
		this.assertBytes(1);
	}

	/**
	 * Ensure fill head buffer (without creating another buffer).
	 */
	@Test
	public void fillHeadBuffer() throws IOException {
		this.write(1, 2);
		this.assertBytes(1, 2);
		assertEquals(0, this.head.pooledBuffer.remaining(), "Should fill buffer");
		assertNull(this.head.next, "Should not create another buffer");
	}

	/**
	 * Ensure can write beyond head buffer.
	 */
	@Test
	public void writeBeyondHeader() throws IOException {
		this.write(1, 2, 3);
		this.assertBytes(1, 2, 3);
		assertNotNull(this.head.next, "Should create another buffer");
	}

	/**
	 * Ensure append stream buffers if headers full (even if remaining - as may be
	 * due to unpooled buffer).
	 */
	@Test
	public void writeOnlyToLastBuffer() throws IOException {
		this.head.next = this.bufferPool.getPooledStreamBuffer();
		this.head.next.next = this.bufferPool.getPooledStreamBuffer();
		this.write(1);
		assertEquals(0, BufferJvmFix.position(this.head.pooledBuffer), "Not write to first buffer");
		assertEquals(0, BufferJvmFix.position(this.head.next.pooledBuffer), "Not write to second buffer");
		assertEquals(1, BufferJvmFix.position(this.head.next.next.pooledBuffer), "Should write only to last buffer");
		assertNull(this.head.next.next.next, "Should not append buffer");
		this.assertBytes(1);
	}

	/**
	 * Ensure if last buffer is unpooled, that a pooled buffer is added.
	 */
	@Test
	public void notWriteUnpooledBuffer() throws IOException {
		this.head.next = this.bufferPool.getUnpooledStreamBuffer(ByteBuffer.wrap(new byte[] { 1 }));
		this.write(2);
		assertEquals(0, BufferJvmFix.position(this.head.pooledBuffer), "Should not write to head");
		assertNotNull(this.head.next.next, "Should append pooled buffer");
		assertNotNull(this.head.next.next.pooledBuffer, "Should be pooled buffer appended");
		this.assertBytes(1, 2);
	}

	/**
	 * Ensure if last buffer is file, that a pooled buffer is added.
	 */
	@Test
	public void notWriteFileBuffer() throws IOException {
		this.head.next = this.bufferPool
				.getFileStreamBuffer(TemporaryFiles.getDefault().createTempFile("test", new byte[] { 1 }), 0, -1, null);
		this.write(2);
		assertEquals(0, BufferJvmFix.position(this.head.pooledBuffer), "Should not write to head");
		assertNotNull(this.head.next.next, "Should append pooled buffer");
		assertNotNull(this.head.next.next.pooledBuffer, "Should be pooled buffer appended");
		this.assertBytes(1, 2);
	}

	/**
	 * Ensure can write offset of bytes.
	 */
	@Test
	public void writeOffsetOfBytes() throws IOException {
		byte[] bytes = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		StreamBuffer.write(bytes, 3, 4, this.head, this.bufferPool);
		this.assertBytes(4, 5, 6, 7);
	}

	/**
	 * Ensure can write {@link CharSequence}.
	 */
	@Test
	public void charSequence() throws IOException {
		final String text = "AbCd09";
		StreamBuffer.write(text, this.head, this.bufferPool);
		this.assertBytes(text.getBytes(Charset.forName("US-ASCII")));
	}

	/**
	 * Ensure can write sub sequence of {@link CharSequence}.
	 */
	@Test
	public void charSubSequence() throws IOException {
		final String text = "_ignore_text_ignore_";
		StreamBuffer.write(text, "_ignore_".length(), "text".length(), this.head, this.bufferPool);
		this.assertBytes("text".getBytes(Charset.forName("US-ASCII")));
	}

	/**
	 * Ensure can use {@link Appendable}.
	 */
	@Test
	public void appendable() throws IOException {
		Instant time = Instant.now();
		String expected = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT")).format(time);
		DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT")).formatTo(time,
				StreamBuffer.getAppendable(this.head, this.bufferPool));
		this.assertBytes(expected.getBytes(Charset.forName("US-ASCII")));
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void zero() throws IOException {
		this.doIntegerTest(0);
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void one() throws IOException {
		this.doIntegerTest(1);
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void ten() throws IOException {
		this.doIntegerTest(10);
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void eachDigit() throws IOException {
		this.doIntegerTest(123456789);
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void largeValue() throws IOException {
		this.doIntegerTest(Long.MAX_VALUE);
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void negative() throws IOException {
		this.doIntegerTest(-1);
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void withinBoundLargeNegative() throws IOException {
		this.doIntegerTest(Long.MIN_VALUE + 1);
	}

	/**
	 * Ensure can write value.
	 */
	@Test
	public void largeNegative() throws IOException {
		this.doIntegerTest(Long.MIN_VALUE);
	}

	/**
	 * Undertakes test of value.
	 * 
	 * @param value value.
	 */
	private void doIntegerTest(long value) throws IOException {
		MockStreamBufferPool bufferPool = new MockStreamBufferPool();
		StreamBuffer<ByteBuffer> head = bufferPool.getPooledStreamBuffer();
		StreamBuffer.write(value, head, bufferPool);
		MockStreamBufferPool.releaseStreamBuffers(head);
		String actual = MockStreamBufferPool.getContent(head, ServerHttpConnection.HTTP_CHARSET);
		assertEquals(String.valueOf(value), actual, "Incorrect integer");
	}

	/**
	 * Writes bytes to the linked list.
	 * 
	 * @param bytes Bytes to be written.
	 */
	private void write(int... bytes) throws IOException {
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
				assertEquals(bytes[i], inputStream.read(), "Incorrect byte " + i);
			}
			assertEquals(-1, inputStream.read(), "Should be end of stream");
		} catch (IOException ex) {
			fail(ex);
		}
	}

}
