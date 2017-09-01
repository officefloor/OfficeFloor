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
package net.officefloor.server.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockStreamBufferPool;

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
	 * Ensure append stream buffers if headers full (even if remaining - as may
	 * be due to unpooled buffer).
	 */
	public void testWriteOnlyToLastBuffer() {
		this.head.next = this.bufferPool.getPooledStreamBuffer();
		this.head.next.next = this.bufferPool.getPooledStreamBuffer();
		write(1);
		assertEquals("Not write to first buffer", 0, this.head.pooledBuffer.position());
		assertEquals("Not write to second buffer", 0, this.head.next.pooledBuffer.position());
		assertEquals("Should write only to last buffer", 1, this.head.next.next.pooledBuffer.position());
		assertNull("Should not append buffer", this.head.next.next.next);
		this.assertBytes(1);
	}

	/**
	 * Ensure if last buffer is unpooled, that a pooled buffer is added.
	 */
	public void testNotWriteUnpooledBuffer() {
		this.head.next = this.bufferPool.getUnpooledStreamBuffer(ByteBuffer.wrap(new byte[] { 1 }));
		this.write(2);
		assertEquals("Should not write to head", 0, this.head.pooledBuffer.position());
		assertNotNull("Should append pooled buffer", this.head.next.next);
		assertTrue("Should be pooled buffer appended", this.head.next.next.isPooled);
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
	 * Writes bytes to the linked list.
	 * 
	 * @param bytes
	 *            Bytes to be written.
	 */
	private void write(int... bytes) {
		byte[] data = new byte[bytes.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) bytes[i];
		}
		StreamBuffer.write(data, 0, data.length, this.head, this.bufferPool);
	}

	/**
	 * Verifies the bytes.
	 * 
	 * @param bytes
	 *            Expected bytes.
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