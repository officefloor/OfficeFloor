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
package net.officefloor.server.http.parse;

import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.parse.impl.ByteBufferScanner;

/**
 * Tests the {@link ByteBufferScanner}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteBufferScannerTest extends OfficeFrameTestCase {

	/**
	 * {@link ByteBuffer} for long comparison of finding byte.
	 */
	private static final ByteBuffer longNumberedBuffer = createBuffer(1, 2, 3, 4, 5, 6, 7, 8);

	/**
	 * Ensure creates correct scan mask.
	 */
	public void testScanMask() {
		assertEquals(0xffffffffffffffffL, mask(0xff));
		assertEquals(0, mask(0x00));
		assertEquals(0x0101010101010101L, mask(0x01));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFirstByte() {
		assertEquals(0, scan(longNumberedBuffer, 0, 1));
	}

	/**
	 * Convenience method to scan.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer}.
	 * @param startPosition
	 *            Start position within the {@link ByteBuffer}.
	 * @param value
	 *            Byte value to scan for.
	 * @return Index of the byte.
	 */
	private static int scan(ByteBuffer buffer, int startPosition, int value) {
		long mask = mask(value);
		return ByteBufferScanner.scanToByte(buffer, startPosition, (byte) value, mask);
	}

	/**
	 * Convenience method to create a mask.
	 * 
	 * @param value
	 *            Byte value.
	 * @return Mask.
	 */
	private static long mask(int value) {
		return ByteBufferScanner.createScanByteMask((byte) value);
	}

	/**
	 * Creates a test {@link ByteBuffer}.
	 * 
	 * @param values
	 *            Byte values for the {@link ByteBuffer}.
	 * @return {@link ByteBuffer}.
	 */
	private static ByteBuffer createBuffer(int... values) {
		byte[] bytes = new byte[values.length];
		for (int i = 0; i < values.length; i++) {
			bytes[i] = (byte) values[i];
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.position(buffer.capacity()); // mimic writing
		return buffer;
	}
}