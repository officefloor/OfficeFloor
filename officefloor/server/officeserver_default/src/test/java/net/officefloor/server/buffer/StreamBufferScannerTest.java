/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.buffer;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.buffer.StreamBufferScanner.ScanTarget;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link StreamBufferScanner}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferScannerTest extends OfficeFrameTestCase {

	/**
	 * {@link Supplier} of an {@link Error} that should not occur.
	 */
	private static final Supplier<Error> shouldNotOccur = () -> new Error("Should not occur");

	/**
	 * Ensure creates correct scan mask.
	 */
	public void testScanMask() {
		assertEquals(0xffffffffffffffffL, new ScanTarget((byte) 0xff).mask);
		assertEquals(0, new ScanTarget((byte) 0x00).mask);
		assertEquals(0x0101010101010101L, new ScanTarget((byte) 0x01).mask);
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFirstByte() {
		assertEquals(0, indexOfFirst(1));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSecondByte() {
		assertEquals(1, indexOfFirst(2));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testThirdByte() {
		assertEquals(2, indexOfFirst(3));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFourthByte() {
		assertEquals(3, indexOfFirst(4));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testFifthByte() {
		assertEquals(4, indexOfFirst(5));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSixthByte() {
		assertEquals(5, indexOfFirst(6));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testSeventhByte() {
		assertEquals(6, indexOfFirst(7));
	}

	/**
	 * Ensure can find byte.
	 */
	public void testEighthByte() {
		assertEquals(7, indexOfFirst(8));
	}

	/**
	 * Ensure ignore top bit false positives.
	 */
	public void testAllFalsePositives() {
		assertEquals(8, scan(createBuffer(0, 0, 0, 0, 0, 0, 0, 0, 1), 1).length());
	}

	/**
	 * Ensure can handle less than long length.
	 */
	public void testLessThanLongLength() {
		assertEquals(6, scan(createBuffer(1, 2, 3, 4, 5, 6, 7), 7).length());
	}

	/**
	 * Run through each possible combination to ensure skips all false positives
	 * to find the byte.
	 */
	public void testExhaustiveFindByte() {

		// Exhaustively handle each byte value
		// (Don't need to check negatives, as don't search for negative HTTP
		// characters)
		for (int value = 0; value <= Byte.MAX_VALUE; value++) {

			// Create buffer with all other values before value (+1 for zero)
			int extraBytes = 16; // ensure uses long comparison
			byte[] data = new byte[Math.abs(Byte.MIN_VALUE) + Byte.MAX_VALUE + extraBytes + 1];
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
			StreamBuffer<ByteBuffer> buffer = createBuffer(data);

			// Scan for the value
			int scanIndex = scan(buffer, value).length();

			// Ensure correct index
			assertEquals("Incorrect index for byte " + Integer.toHexString(value) + " (" + value + ")", valueIndex,
					scanIndex);
		}
	}

	/**
	 * Run through each possible combination to ensure skips all false positives
	 * to not find the byte.
	 */
	public void testExhaustiveNotFindByte() {

		// Exhaustively handle each byte value
		// (again don't need to check for negative values)
		for (int value = 0; value <= Byte.MAX_VALUE; value++) {

			// Create the buffer with all other values (+1 for zero)
			byte[] data = new byte[Math.abs(Byte.MIN_VALUE) + Byte.MAX_VALUE + 1];
			int writeIndex = 0;
			for (int b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
				if (b != value) {
					data[writeIndex++] = (byte) b;
				}
			}

			// Ensure last byte not zero (to match 0)
			data[writeIndex] = -1;

			// Create the buffer
			StreamBuffer<ByteBuffer> buffer = createBuffer(data);

			// Scan for the value
			StreamBufferByteSequence sequence = scan(buffer, value);

			// Ensure not find the value
			assertNull("Should not find byte " + Integer.toHexString(value) + " (" + value + ")", sequence);
		}
	}

	/**
	 * Ensure can skip bytes.
	 */
	public void testSkipBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2);

		// Ensure can skip bytes
		scanner.skipBytes(3);
		assertEquals("Incorrect long", 0x0202020202020202L, scanner.buildLong(shouldNotOccur));
		assertEquals("Incorrect short", 0x0202, scanner.buildShort(shouldNotOccur));

		// Ensure find target after skipping byte
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 1), 1000, shouldNotOccur);
		assertEquals("Incorrect target after skipping", 8, sequence.length());
		for (int i = 0; i < 8; i++) {
			assertEquals("Incorrect sequnce byte " + i, 2, sequence.byteAt(i));
		}

		// Ensure scan is exclusive
		assertEquals("Should now obtain target and next byte", 0x0102, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Ensure can scan along the data.
	 */
	public void testMultipleScans() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(0, 1, 1, 2, 3, 4, 5, 6, 7, 8, 1, 0);

		// Create the target
		ScanTarget target = new ScanTarget((byte) 1);

		// Ensure find each one in data (note scans are exclusive to target)
		assertEquals(1, scanner.scanToTarget(target, 1000, shouldNotOccur).length());
		scanner.skipBytes(1); // skip past for next match
		assertEquals(0, scanner.scanToTarget(target, 1000, shouldNotOccur).length());
		scanner.skipBytes(1); // skip past for next match
		assertEquals(7, scanner.scanToTarget(target, 1000, shouldNotOccur).length());
		scanner.skipBytes(1); // skip past for next match
		assertNull("Should not find further bytes", scanner.scanToTarget(target, 1000, shouldNotOccur));
	}

	/**
	 * Ensure can scan to target and reset buffers.
	 */
	public void testResetBuffersOnScanTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3);

		// Scan in bytes
		StreamBufferByteSequence one = scanner.scanToTarget(new ScanTarget((byte) 3), 1000, shouldNotOccur);
		assertEquals("Incorrect number of bytes", 2, one.length());
		scanner.skipBytes(1); // skip past the 3

		// Add further bytes and scan in
		scanner.appendStreamBuffer(createBuffer(4, 5, 6));
		StreamBufferByteSequence two = scanner.scanToTarget(new ScanTarget((byte) 6), 1000, shouldNotOccur);
		assertEquals("Incorrect number of additional bytes", 2, two.length());
		scanner.skipBytes(1); // skip past the 6

		// Ensure correct bytes
		for (int i = 0; i < 2; i++) {
			assertEquals("Incorrect first byte", i + 1, one.byteAt(i));
		}
		for (int i = 0; i < 2; i++) {
			assertEquals("Incorrect second byte", i + 4, two.byteAt(i));
		}
	}

	/**
	 * Ensure scan to large.
	 */
	public void testScanToLarge() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3, 4, 5, 6, 7, 8);

		// Ensure exception if too large
		ScanTarget target = new ScanTarget((byte) 9);
		final Exception exception = new Exception("TEST");
		try {
			scanner.scanToTarget(target, 3, () -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Incorrect exception", exception, ex);
		}
	}

	/**
	 * Ensure progressive scan to large.
	 */
	public void testProgressiveScanToLarge() {

		// Create buffer to progressively write
		final int MAX_LENGTH = 10;
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(MAX_LENGTH));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();

		// Create the scanner
		StreamBufferScanner scanner = new StreamBufferScanner();

		// Details for testing
		ScanTarget target = new ScanTarget((byte) MAX_LENGTH);

		// Progressively load data ensuring no issue of too long
		for (int i = 0; i < (MAX_LENGTH - 1); i++) {
			buffer.write((byte) i);
			scanner.appendStreamBuffer(buffer);
			assertNull("Should not find target for " + i, scanner.scanToTarget(target, MAX_LENGTH, shouldNotOccur));
		}

		// Tip over max length (+1 so not find value)
		buffer.write((byte) (MAX_LENGTH + 1));
		final Exception exception = new Exception("TEST");
		try {
			scanner.scanToTarget(target, MAX_LENGTH, () -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Incorrect exception", exception, ex);
		}
	}

	/**
	 * Ensure can build long.
	 */
	public void testBuildLong() {

		// Create scanner with data just for first long
		StreamBufferScanner scanner = createScanner(1, 1, 1, 1, 1, 1, 1, 1);

		// Ensure can build first long
		assertEquals("Incorrect immediate long", 0x0101010101010101L, scanner.buildLong(shouldNotOccur));
		assertEquals("Should be able build same long", 0x0101010101010101L, scanner.buildLong(shouldNotOccur));

		// Skip the long bytes
		scanner.skipBytes(8);

		// Ensure require further data for second long
		assertEquals("Should require further bytes for another long", -1, scanner.buildLong(shouldNotOccur));

		// Incrementally add further data (ensuring requires all data)
		for (int i = 1; i <= 7; i++) {
			scanner.appendStreamBuffer(createBuffer(2));
			assertEquals("Only " + i + " bytes for long", -1, scanner.buildLong(shouldNotOccur));
		}

		// Add remaining byte to now build the long
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Incorrect built long", 0x0202020202020202L, scanner.buildLong(shouldNotOccur));
		scanner.skipBytes(8);

		// Ensure can scan in 7 bytes then 1 byte (test the bulk data reads)
		scanner.appendStreamBuffer(createBuffer(3));
		assertEquals("Only first byte", -1, scanner.buildLong(shouldNotOccur));
		scanner.appendStreamBuffer(createBuffer(3, 3, 3, 3, 3, 3, 3));
		assertEquals("Should bulk read in bytes", 0x0303030303030303L, scanner.buildLong(shouldNotOccur));
	}

	/**
	 * Ensure handle negative values in building long.
	 */
	public void testBuildLongWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xfe);
		assertEquals("Should provide long value", 0xfffffffffffffffeL, scanner.buildLong(shouldNotOccur));
	}

	/**
	 * Series of 0xff bytes should not occur when building a long.
	 */
	public void testBuildIllegalLong() {

		// Create the scanner with 0xff long value in bytes
		StreamBufferScanner scanner = createScanner(0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff);
		assertEquals("Not enough bytes", -1, scanner.buildLong(shouldNotOccur));

		// Add the final byte for -1 long value
		scanner.appendStreamBuffer(createBuffer(0xff));
		final Exception exception = new Exception("TEST");
		try {
			scanner.buildLong(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Should be same exception", exception, ex);
		}
	}

	/**
	 * Ensure can build a long and then scan within the long buffer to find the
	 * bytes.
	 */
	public void testBuildLongThenScanIncludesBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 8; i++) {
			assertEquals("Not enough bytes", -1, scanner.buildLong(shouldNotOccur));
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the long
		long bytes = scanner.buildLong(shouldNotOccur);
		assertEquals("Incorrect long", 0x0102030405060708L, bytes);

		// Skip 4 bytes (e.g. "GET ")
		scanner.skipBytes(4);

		// Ensure can get bytes from buffer long
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 6), 1000, shouldNotOccur);
		assertEquals("Incorrect number of bytes", 1, sequence.length());
		assertEquals("Incorrect byte", 5, sequence.byteAt(0));
	}

	/**
	 * Ensure can build a long and then skip some of the bytes and include bytes
	 * in scan.
	 */
	public void testBuildLongThenSkipThenScanIncludesBufferLongBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 8; i++) {
			assertEquals("Not enough bytes", -1, scanner.buildLong(shouldNotOccur));
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the long
		long bytes = scanner.buildLong(shouldNotOccur);
		assertEquals("Incorrect long", 0x0102030405060708L, bytes);

		// Skip 4 bytes (e.g. "GET ")
		scanner.skipBytes(4);

		// Should not find the 4 value (as skipped past)
		ScanTarget target = new ScanTarget((byte) 4);
		assertNull("Should not find skipped value", scanner.scanToTarget(target, 1000, shouldNotOccur));

		// Add skipped value (should find with all remaining long values)
		scanner.appendStreamBuffer(createBuffer(4, 5));
		StreamBufferByteSequence sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals("Incorrect number of bytes", 4, sequence.length());
		for (int i = 0; i < 4; i++) {
			assertEquals("Incorrect byte", i + 5, sequence.byteAt(i));
		}

		// Ensure continue to find further scan targets
		sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals("Should find again", 0, sequence.length());
		sequence = scanner.scanToTarget(new ScanTarget((byte) 5), 1000, shouldNotOccur);
		assertEquals("Should find target", 1, sequence.length());
		assertEquals("Incorrect value", 4, sequence.byteAt(0));
	}

	/**
	 * Ensure can build short.
	 */
	public void testBuildShort() {

		// Create scanner with data just for first long
		StreamBufferScanner scanner = createScanner(1, 1);

		// Ensure can build first short
		assertEquals("Incorrect immediate short", 0x0101L, scanner.buildShort(shouldNotOccur));
		assertEquals("Should be able build same long", 0x0101L, scanner.buildShort(shouldNotOccur));

		// Skip the short bytes
		scanner.skipBytes(2);

		// Ensure require further data for second short
		assertEquals("Should require further bytes for another short", -1, scanner.buildShort(shouldNotOccur));

		// Incrementally add further data (ensuring requires all data)
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Only 1 byte for short", -1, scanner.buildShort(shouldNotOccur));

		// Add remaining byte to now build the short
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Incorrect built short", 0x0202L, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Ensure handle negative values in building short.
	 */
	public void testBuildShortWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xff, 0xfe);
		assertEquals("Should provide short value", -2, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Series of -1 bytes should not occur when building a short.
	 */
	public void testBuildIllegalShort() {

		// Create the scanner with -1 short value in bytes
		StreamBufferScanner scanner = createScanner(0xff);
		assertEquals("Not enough bytes", -1, scanner.buildShort(shouldNotOccur));

		// Add the final byte for -1 short value
		scanner.appendStreamBuffer(createBuffer(0xff));
		final Exception exception = new Exception("TEST");
		try {
			scanner.buildShort(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Should be same exception", exception, ex);
		}
	}

	/**
	 * Ensure can build a short and then scan within the long buffer to find the
	 * bytes.
	 */
	public void testBuildShortThenScanIncludesBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 2; i++) {
			assertEquals("Not enough bytes", -1, scanner.buildShort(shouldNotOccur));
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the short
		short bytes = scanner.buildShort(shouldNotOccur);
		assertEquals("Incorrect short", 0x0102, bytes);

		// Add byte after short to find
		scanner.appendStreamBuffer(createBuffer(3));

		// Ensure can get bytes from buffer long
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 3), 1000, shouldNotOccur);
		assertEquals("Should find in the buffer", 2, sequence.length());
		for (int i = 0; i < 2; i++) {
			assertEquals("Incorrect bytes", i + 1, sequence.byteAt(i));
		}

		// Ensure no long find bytes from long buffer
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertNull("Should not find byte", sequence);
	}

	/**
	 * Ensure can build a long and then skip some of the bytes and include bytes
	 * in scan.
	 */
	public void testBuildShortThenSkipThenScanIncludesBufferLongBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 2; i++) {
			assertEquals("Not enough bytes", -1, scanner.buildLong(shouldNotOccur));
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the short
		short bytes = scanner.buildShort(shouldNotOccur);
		assertEquals("Incorrect short", 0x0102, bytes);

		// Skip 1 bytes (e.g. " ")
		scanner.skipBytes(1);

		// Should not find the 4 value (as skipped past)
		ScanTarget target = new ScanTarget((byte) 1);
		assertNull("Should not find skipped value", scanner.scanToTarget(target, 1000, shouldNotOccur));

		// Add skipped value (should find with all remaining long values)
		scanner.appendStreamBuffer(createBuffer(1, 2));
		StreamBufferByteSequence sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals("Incorrect number of bytes", 1, sequence.length());
		assertEquals("Incorrect byte", 2, sequence.byteAt(0));

		// Ensure continue to find further scan targets
		sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals("Should find again", 0, sequence.length());
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertEquals("Should find target", 1, sequence.length());
		assertEquals("Incorrect value", 1, sequence.byteAt(0));
	}

	/**
	 * Ensure can build byte.
	 */
	public void testBuildByte() {

		// Create scanner with data just for first byte
		StreamBufferScanner scanner = createScanner(1);

		// Ensure can build first short
		assertEquals("Incorrect immediate byte", 0x01, scanner.buildByte(shouldNotOccur));
		assertEquals("Should be able build same byte", 0x01, scanner.buildByte(shouldNotOccur));

		// Skip the byte
		scanner.skipBytes(1);

		// Ensure require further data for second byte
		assertEquals("Should require further bytes for another byte", -1, scanner.buildByte(shouldNotOccur));

		// Add another byte and ensure can obtain
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Incorrect built byte", 0x02, scanner.buildByte(shouldNotOccur));
	}

	/**
	 * Ensure handle negative values in building byte.
	 */
	public void testBuildByteWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xfe);
		assertEquals("Should provide byte value", -2, scanner.buildByte(shouldNotOccur));
	}

	/**
	 * Series of -1 bytes should not occur when building a byte.
	 */
	public void testBuildIllegalByte() {

		// Create the scanner with -1 short value in bytes
		StreamBufferScanner scanner = createScanner(0xff);
		final Exception exception = new Exception("TEST");
		try {
			scanner.buildByte(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Should be same exception", exception, ex);
		}
	}

	/**
	 * Ensure able to build byte then short then long from same position.
	 */
	public void testBuildByteThenShortThenLong() {

		// Create the scanner with values
		StreamBufferScanner scanner = createScanner(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08);

		// Ensure can build byte
		assertEquals("Incorrect built byte", 0x01, scanner.buildByte(shouldNotOccur));

		// Ensure can build short
		assertEquals("Incorrect built short", 0x0102, scanner.buildShort(shouldNotOccur));

		// Ensure can build long
		assertEquals("Incorrect built long", 0x0102030405060708L, scanner.buildLong(shouldNotOccur));
	}

	/**
	 * Ensure able to build long then short then byte from same position.
	 */
	public void testBuildLongThenShortThenByte() {

		// Create the scanner with values
		StreamBufferScanner scanner = createScanner(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08);

		// Ensure can build long
		assertEquals("Incorrect built long", 0x0102030405060708L, scanner.buildLong(shouldNotOccur));

		// Ensure can build short
		assertEquals("Incorrect built short", 0x0102, scanner.buildShort(shouldNotOccur));

		// Ensure can build byte
		assertEquals("Incorrect built byte", 0x01, scanner.buildByte(shouldNotOccur));
	}

	/**
	 * Ensure can build long and re-use bytes for scan.
	 */
	public void testProgressivelyBuildLongThenScanToTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertEquals("Should not build", -1, scanner.buildLong(shouldNotOccur));

		// Progressively build the long
		for (int i = 1; i < 7; i++) {
			scanner.appendStreamBuffer(createBuffer(i + 1));
			assertEquals("Should not build after " + i, -1, scanner.buildLong(shouldNotOccur));
		}
		scanner.appendStreamBuffer(createBuffer(8));
		assertEquals("Incorrect built long", 0x0102030405060708L, scanner.buildLong(shouldNotOccur));

		// Ensure scan to target includes the long
		ScanTarget targetSeven = new ScanTarget((byte) 7);
		StreamBufferByteSequence sequence = scanner.scanToTarget(targetSeven, 1000, shouldNotOccur);
		assertEquals("Should have scanned bytes", 6, sequence.length());
		for (int i = 0; i < 6; i++) {
			assertEquals("Incorrect byte", i + 1, sequence.byteAt(i));
		}

		// Ensure able to scan again
		sequence = scanner.scanToTarget(targetSeven, 1000, shouldNotOccur);
		assertEquals("Should find again, but no data", 0, sequence.length());

		// Ensure able to scan beyond the byte
		ScanTarget targetNine = new ScanTarget((byte) 9);
		sequence = scanner.scanToTarget(targetNine, 1000, shouldNotOccur);
		assertNull("Should not find byte", sequence);

		// Ensure after adding target, includes the remaining long bytes
		scanner.appendStreamBuffer(createBuffer(9));
		sequence = scanner.scanToTarget(targetNine, 1000, shouldNotOccur);
		assertEquals("Should include remaining long content", 2, sequence.length());
		for (int i = 0; i < 2; i++) {
			assertEquals("Incorrect long byte", i + 7, sequence.byteAt(i));
		}
	}

	/**
	 * Ensure can build short and re-use bytes for scan.
	 */
	public void testProgressivelyBuildShortThenScanToTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertEquals("Should not build", -1, scanner.buildShort(shouldNotOccur));

		// Progressively build the short
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals("Incorrect built short", 0x0102, scanner.buildShort(shouldNotOccur));

		// Ensure scan to target includes the short
		ScanTarget targetTwo = new ScanTarget((byte) 2);
		StreamBufferByteSequence sequence = scanner.scanToTarget(targetTwo, 1000, shouldNotOccur);
		assertEquals("Should have scanned bytes", 1, sequence.length());
		assertEquals("Incorrect byte", 1, sequence.byteAt(0));

		// Ensure able to scan again
		sequence = scanner.scanToTarget(targetTwo, 1000, shouldNotOccur);
		assertEquals("Should find again, but no data", 0, sequence.length());

		// Ensure able to scan beyond the byte
		ScanTarget targetThree = new ScanTarget((byte) 3);
		sequence = scanner.scanToTarget(targetThree, 1000, shouldNotOccur);
		assertNull("Should not find byte", sequence);

		// Ensure after adding target, includes the remaining short bytes
		scanner.appendStreamBuffer(createBuffer(3));
		sequence = scanner.scanToTarget(targetThree, 1000, shouldNotOccur);
		assertEquals("Should include remaining short content", 1, sequence.length());
		assertEquals("Incorrect short remaining byte", 2, sequence.byteAt(0));
	}

	/**
	 * Ensure can build byte and re-use bytes for scan.
	 */
	public void testProgressivelyBuildByteThenScanToTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertEquals("Incorrect built byte", 0x0001, scanner.buildByte(shouldNotOccur));

		// Ensure scan to target includes the short
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 1), 1000, shouldNotOccur);
		assertEquals("Should have scanned bytes", 0, sequence.length());

		// Ensure able to scan again
		sequence = scanner.scanToTarget(new ScanTarget((byte) 1), 1000, shouldNotOccur);
		assertEquals("Should find again, but no data", 0, sequence.length());

		// Ensure able to scan beyond the byte
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertNull("Should not find byte", sequence);

		// Ensure after adding target, includes the remaining short bytes
		scanner.appendStreamBuffer(createBuffer(2));
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertEquals("Should include remaining byte content", 1, sequence.length());
		assertEquals("Incorrect remaining byte", 1, sequence.byteAt(0));
	}

	/**
	 * Ensure can peek bytes.
	 */
	public void testPeekBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3);

		// Ensure not find byte
		assertEquals("Should not find byte", -1, scanner.peekToTarget(new ScanTarget((byte) 4)));
		assertEquals("Should find byte", 2, scanner.peekToTarget(new ScanTarget((byte) 3)));

		// Should not have progressed through buffer data
		assertEquals("Should still be at start", 0x0102, scanner.buildShort(shouldNotOccur));
	}

	/**
	 * Ensure can peek bytes by larger buffer.
	 */
	public void testPeekBytesOnLargerBuffer() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1);

		// Skip first 2 bytes
		scanner.skipBytes(2);

		// Ensure find byte
		assertEquals("Incorrect offset position from skip", 12, scanner.peekToTarget(new ScanTarget((byte) 1)));
	}

	/**
	 * Ensure can scan in fixed set of bytes.
	 */
	public void testScanBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3, 4, 5, 6);

		// Scan in bytes
		StreamBufferByteSequence sequence = scanner.scanBytes(3);
		assertEquals("Incorrect number of bytes", 3, sequence.length());
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect byte", i + 1, sequence.byteAt(i));
		}

		// Scan in remaining bytes
		sequence = scanner.scanBytes(3);
		assertEquals("Should have remaining bytes", 3, sequence.length());
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect byte", i + 4, sequence.byteAt(i));
		}
	}

	/**
	 * Ensure can scan buffer and reset buffers.
	 */
	public void testResetBufferOnScanBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3);

		// Scan in bytes
		StreamBufferByteSequence one = scanner.scanBytes(3);
		assertEquals("Incorrect number of bytes", 3, one.length());

		// Add further bytes and scan in
		scanner.appendStreamBuffer(createBuffer(4, 5, 6));
		StreamBufferByteSequence two = scanner.scanBytes(3);
		assertEquals("Incorrect number of additional bytes", 3, two.length());

		// Ensure correct bytes
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect first byte", i + 1, one.byteAt(i));
		}
		for (int i = 0; i < 3; i++) {
			assertEquals("Incorrect second byte", i + 4, two.byteAt(i));
		}
	}

	/**
	 * Ensure can scan in fixed set of bytes.
	 */
	public void testScanBytesAcrossMultipleBuffers() {

		final int BYTES_LENGTH = 10;

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertNull("Not enough bytes", scanner.scanBytes(BYTES_LENGTH));

		// Scan in bytes (incrementally in worst case scenario)
		for (int i = 1; i < BYTES_LENGTH; i++) {
			assertNull("Should not obtain sequence, as not enough bytes - " + i, scanner.scanBytes(BYTES_LENGTH));
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Should now obtain sequence
		StreamBufferByteSequence sequence = scanner.scanBytes(BYTES_LENGTH);
		assertNotNull("Should have all bytes", sequence);

		// Ensure correct bytes
		assertEquals("Incorrect number of bytes", BYTES_LENGTH, sequence.length());
		for (int i = 0; i < BYTES_LENGTH; i++) {
			assertEquals("Incorrect byte", i + 1, sequence.byteAt(i));
		}
	}

	/**
	 * Ensure can scan bytes from previous buffers (as previously built long).
	 */
	public void testScanBytesFromPreviousBuffers() {

		// Create the scanner (and build long)
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 8; i++) {
			assertEquals("Not enough bytes", -1, scanner.buildLong(shouldNotOccur));
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}
		assertEquals("Should have long", 0x0102030405060708L, scanner.buildLong(shouldNotOccur));

		// Ensure can scan in no data from previous buffer
		StreamBufferByteSequence sequence = scanner.scanBytes(0);
		assertEquals("Should be empty", 0, sequence.length());

		// Ensure can scan in just previous buffer data
		sequence = scanner.scanBytes(4);
		assertEquals("Incorrect number of bytes", 4, sequence.length());
		for (int i = 0; i < 4; i++) {
			assertEquals("Incorrect byte", i + 1, sequence.byteAt(i));
		}

		// Ensure can scan past previous buffer
		scanner.appendStreamBuffer(createBuffer(9, 10, 11, 12));
		sequence = scanner.scanBytes(8);
		assertEquals("Incorrect number of bytes", 8, sequence.length());
		for (int i = 0; i < 8; i++) {
			assertEquals("Incorrect byte", i + 5, sequence.byteAt(i));
		}
	}

	/**
	 * Creates a number buffer with value being repeated for rest of long bytes.
	 * Then scans for that value to ensure returns first.
	 * 
	 * @param value
	 *            Value.
	 * @return {@link ByteBuffer} for long read.
	 */
	private static int indexOfFirst(int value) {

		// Create the buffer
		long data = 0;
		for (int i = 0; i < value; i++) {
			data <<= 8; // move up a byte
			data += (byte) (i + 1);
		}
		for (int i = value; i < 8; i++) {
			data <<= 8; // move up a byte
			data += (byte) value;
		}

		// Scan the buffer for the value
		int scanIndex = StreamBufferScanner.indexOf(data, new ScanTarget((byte) value));

		// Return the scan index
		return scanIndex;
	}

	/**
	 * Convenience method to scan.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer}.
	 * @param startPosition
	 *            Start position within the {@link ByteBuffer}.
	 * @param value
	 *            Byte value to scan for.
	 * @return {@link StreamBufferByteSequence}. Or <code>null</code> if not
	 *         found.
	 */
	private static StreamBufferByteSequence scan(StreamBuffer<ByteBuffer> buffer, int value) {
		StreamBufferScanner scanner = new StreamBufferScanner();
		scanner.appendStreamBuffer(buffer);
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) value), 1000, shouldNotOccur);
		return sequence;
	}

	/**
	 * Creates a {@link StreamBufferScanner} with the data.
	 * 
	 * @param values
	 *            Values for the data.
	 * @return {@link StreamBufferScanner}.
	 */
	private static StreamBufferScanner createScanner(int... values) {
		StreamBuffer<ByteBuffer> buffer = createBuffer(values);
		StreamBufferScanner scanner = new StreamBufferScanner();
		scanner.appendStreamBuffer(buffer);
		return scanner;
	}

	/**
	 * Creates a test {@link StreamBuffer}.
	 * 
	 * @param values
	 *            Byte values for the {@link StreamBuffer}.
	 * @return {@link StreamBuffer}.
	 */
	private static StreamBuffer<ByteBuffer> createBuffer(int... values) {
		byte[] data = new byte[values.length];
		for (int i = 0; i < values.length; i++) {
			data[i] = (byte) values[i];
		}
		return createBuffer(data);
	}

	/**
	 * Creates a test {@link StreamBuffer}.
	 * 
	 * @param data
	 *            Data for the {@link StreamBuffer}.
	 * @return {@link StreamBuffer}.
	 */
	private static StreamBuffer<ByteBuffer> createBuffer(byte[] data) {
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(data.length));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer();
		buffer.write(data);
		return buffer;
	}

}