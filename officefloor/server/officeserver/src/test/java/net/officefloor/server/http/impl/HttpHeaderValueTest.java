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
package net.officefloor.server.http.impl;

import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link HttpHeaderValue}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderValueTest extends OfficeFrameTestCase {

	/**
	 * Ensure can write value.
	 */
	public void testZero() {
		this.doValueTest(0);
	}

	/**
	 * Ensure can write value.
	 */
	public void testOne() {
		this.doValueTest(1);
	}

	/**
	 * Ensure can write value.
	 */
	public void testTen() {
		this.doValueTest(10);
	}

	/**
	 * Ensure can write value.
	 */
	public void testEachDigit() {
		this.doValueTest(123456789);
	}

	/**
	 * Ensure can write value.
	 */
	public void testLargeValue() {
		this.doValueTest(Long.MAX_VALUE);
	}

	/**
	 * Ensure can write value.
	 */
	public void testNegative() {
		this.doValueTest(-1);
	}

	/**
	 * Ensure can write value.
	 */
	public void testWithinBoundLargeNegative() {
		this.doValueTest(Long.MIN_VALUE + 1);
	}

	/**
	 * Ensure can write value.
	 */
	public void testLargeNegative() {
		this.doValueTest(Long.MIN_VALUE);
	}

	/**
	 * Undertakes test of value.
	 * 
	 * @param value
	 *            value.
	 */
	private void doValueTest(long value) {
		MockStreamBufferPool bufferPool = new MockStreamBufferPool();
		StreamBuffer<ByteBuffer> head = bufferPool.getPooledStreamBuffer();
		HttpHeaderValue.writeInteger(value, head, bufferPool);
		MockStreamBufferPool.releaseStreamBuffers(head);
		String actual = MockStreamBufferPool.getContent(head, ServerHttpConnection.HTTP_CHARSET);
		assertEquals("Incorrect value", String.valueOf(value), actual);
	}

}