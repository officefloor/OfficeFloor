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
	 * Ensure can find byte.
	 */
	public void testSecondByte() {
		assertEquals(1, scan(longNumberedBuffer, 0, 2));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testThirdByte() {
		assertEquals(2, scan(longNumberedBuffer, 0, 3));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFourthByte() {
		assertEquals(3, scan(longNumberedBuffer, 0, 4));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFifthByte() {
		assertEquals(4, scan(longNumberedBuffer, 0, 5));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSixthByte() {
		assertEquals(5, scan(longNumberedBuffer, 0, 6));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSeventhByte() {
		assertEquals(6, scan(longNumberedBuffer, 0, 7));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testEighthByte() {
		assertEquals(7, scan(longNumberedBuffer, 0, 8));
	}

	/**
	 * Ensure ignore top bit false positives.
	 */
	public void testAllFalsePositives() {
		assertEquals(8, scan(createBuffer(0, 0, 0, 0, 0, 0, 0, 0, 1), 0, 1));
	}

	/**
	 * Ensure can handle less than long length.
	 */
	public void testLessThanLongLength() {
		assertEquals(6, scan(createBuffer(1, 2, 3, 4, 5, 6, 7), 0, 7));
	}

	/**
	 * Ensure can start with offset.
	 */
	public void testOffsetStart() {
		assertEquals(10, scan(createBuffer(1, 1, 1, 2, 3, 4, 5, 6, 7, 8, 1), 3, 7));
	}

	/**
	 * Run through each possible combination to ensure skips all false positives
	 * to find the byte.
	 */
	public void testExhaustiveFindByte() {

		// Exhaustively handle each byte value
		for (int value = Byte.MIN_VALUE; value <= Byte.MAX_VALUE; value++) {

			// Create the buffer with all other values before value
			int extraBytes = 16; // ensure uses long comparison
			byte[] data = new byte[Math.abs(Byte.MIN_VALUE) + Byte.MAX_VALUE + extraBytes];
			int writeIndex = 0;
			for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
				if (b != value) {
					data[writeIndex++] = (byte) b;
				}
			}

			// Write the value last
			int valueIndex = data.length - extraBytes - 1;
			data[valueIndex] = (byte) value;

			// Create the buffer
			ByteBuffer buffer = ByteBuffer.wrap(data);
			buffer.position(buffer.capacity());

			// Scan for the value
			int scanIndex = scan(buffer, 0, value);

			// Ensure correct index
			assertEquals("Incorrect index for byte " + Integer.toHexString(value), valueIndex, scanIndex);
		}
	}

	/**
	 * Run through each possible combination to ensure skips all false positives
	 * to not find the byte.
	 */
	public void testExhaustiveNotFindByte() {

		// Exhaustively handle each byte value
		for (int value = Byte.MIN_VALUE; value <= Byte.MAX_VALUE; value++) {

			// Create the buffer with all other values
			byte[] data = new byte[Math.abs(Byte.MIN_VALUE) + Byte.MAX_VALUE - 1];
			int writeIndex = 0;
			for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
				if (b != value) {
					data[writeIndex++] = (byte) b;
				}
			}

			// Create the buffer
			ByteBuffer buffer = ByteBuffer.wrap(data);
			buffer.position(buffer.capacity());

			// Scan for the value
			int scanIndex = scan(buffer, 0, value);

			// Ensure not find the value
			assertEquals("Should not find byte " + Integer.toHexString(value), -1, scanIndex);
		}
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