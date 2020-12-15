/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.buffer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import net.officefloor.server.buffer.StreamBufferScanner.ScanTarget;
import net.officefloor.server.http.mock.MockStreamBufferPool;
import net.officefloor.server.stream.ServerMemoryOverloadHandler;
import net.officefloor.server.stream.StreamBuffer;

/**
 * Tests the {@link StreamBufferScanner}.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferScannerTest {

	/**
	 * {@link ServerMemoryOverloadHandler}.
	 */
	private static final ServerMemoryOverloadHandler OVERLOAD_HANDLER = () -> fail("Server should not be overloaded");

	/**
	 * {@link Supplier} of an {@link Error} that should not occur.
	 */
	private static final Supplier<Error> shouldNotOccur = () -> new Error("Should not occur");

	/**
	 * Ensure creates correct scan mask.
	 */
	@Test
	public void scanMask() {
		assertEquals(0xffffffffffffffffL, new ScanTarget((byte) 0xff).mask);
		assertEquals(0, new ScanTarget((byte) 0x00).mask);
		assertEquals(0x0101010101010101L, new ScanTarget((byte) 0x01).mask);
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void firstByte() {
		assertEquals(0, indexOfFirst(1));
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void secondByte() {
		assertEquals(1, indexOfFirst(2));
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void thirdByte() {
		assertEquals(2, indexOfFirst(3));
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void fourthByte() {
		assertEquals(3, indexOfFirst(4));
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void fifthByte() {
		assertEquals(4, indexOfFirst(5));
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void sixthByte() {
		assertEquals(5, indexOfFirst(6));
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void seventhByte() {
		assertEquals(6, indexOfFirst(7));
	}

	/**
	 * Ensure can find byte.
	 */
	@Test
	public void eighthByte() {
		assertEquals(7, indexOfFirst(8));
	}

	/**
	 * Ensure ignore top bit false positives.
	 */
	@Test
	public void allFalsePositives() {
		assertEquals(8, scan(createBuffer(0, 0, 0, 0, 0, 0, 0, 0, 1), 1).length());
	}

	/**
	 * Ensure can handle less than long length.
	 */
	@Test
	public void lessThanLongLength() {
		assertEquals(6, scan(createBuffer(1, 2, 3, 4, 5, 6, 7), 7).length());
	}

	/**
	 * Run through each possible combination to ensure skips all false positives to
	 * find the byte.
	 */
	@Test
	public void exhaustiveFindByte() {

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
			assertEquals(valueIndex, scanIndex,
					"Incorrect index for byte " + Integer.toHexString(value) + " (" + value + ")");
		}
	}

	/**
	 * Run through each possible combination to ensure skips all false positives to
	 * not find the byte.
	 */
	@Test
	public void exhaustiveNotFindByte() {

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
			assertNull(sequence, "Should not find byte " + Integer.toHexString(value) + " (" + value + ")");
		}
	}

	/**
	 * Ensure can skip bytes.
	 */
	@Test
	public void skipBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2);

		// Ensure can skip bytes
		scanner.skipBytes(3);
		assertEquals(0x0202020202020202L, scanner.buildLong(shouldNotOccur), "Incorrect long");
		assertEquals(0x0202, scanner.buildShort(shouldNotOccur), "Incorrect short");

		// Ensure find target after skipping byte
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 1), 1000, shouldNotOccur);
		assertEquals(8, sequence.length(), "Incorrect target after skipping");
		for (int i = 0; i < 8; i++) {
			assertEquals(2, sequence.byteAt(i), "Incorrect sequnce byte " + i);
		}

		// Ensure scan is exclusive
		assertEquals(0x0102, scanner.buildShort(shouldNotOccur), "Should now obtain target and next byte");
	}

	/**
	 * Ensure can scan along the data.
	 */
	@Test
	public void multipleScans() {

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
		assertNull(scanner.scanToTarget(target, 1000, shouldNotOccur), "Should not find further bytes");
	}

	/**
	 * Ensure can scan to target and reset buffers.
	 */
	@Test
	public void resetBuffersOnScanTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3);

		// Scan in bytes
		StreamBufferByteSequence one = scanner.scanToTarget(new ScanTarget((byte) 3), 1000, shouldNotOccur);
		assertEquals(2, one.length(), "Incorrect number of bytes");
		scanner.skipBytes(1); // skip past the 3

		// Add further bytes and scan in
		scanner.appendStreamBuffer(createBuffer(4, 5, 6));
		StreamBufferByteSequence two = scanner.scanToTarget(new ScanTarget((byte) 6), 1000, shouldNotOccur);
		assertEquals(2, two.length(), "Incorrect number of additional bytes");
		scanner.skipBytes(1); // skip past the 6

		// Ensure correct bytes
		for (int i = 0; i < 2; i++) {
			assertEquals(i + 1, one.byteAt(i), "Incorrect first byte");
		}
		for (int i = 0; i < 2; i++) {
			assertEquals(i + 4, two.byteAt(i), "Incorrect second byte");
		}
	}

	/**
	 * Ensure scan to large.
	 */
	@Test
	public void scanToLarge() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3, 4, 5, 6, 7, 8);

		// Ensure exception if too large
		ScanTarget target = new ScanTarget((byte) 9);
		final Exception exception = new Exception("TEST");
		try {
			scanner.scanToTarget(target, 3, () -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame(exception, ex, "Incorrect exception");
		}
	}

	/**
	 * Ensure progressive scan to large.
	 */
	@Test
	public void progressiveScanToLarge() {

		// Create buffer to progressively write
		final int MAX_LENGTH = 10;
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(MAX_LENGTH));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer(OVERLOAD_HANDLER);

		// Create the scanner
		StreamBufferScanner scanner = new StreamBufferScanner();

		// Details for testing
		ScanTarget target = new ScanTarget((byte) MAX_LENGTH);

		// Progressively load data ensuring no issue of too long
		for (int i = 0; i < (MAX_LENGTH - 1); i++) {
			buffer.write((byte) i);
			scanner.appendStreamBuffer(buffer);
			assertNull(scanner.scanToTarget(target, MAX_LENGTH, shouldNotOccur), "Should not find target for " + i);
		}

		// Tip over max length (+1 so not find value)
		buffer.write((byte) (MAX_LENGTH + 1));
		final Exception exception = new Exception("TEST");
		try {
			scanner.scanToTarget(target, MAX_LENGTH, () -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame(exception, ex, "Incorrect exception");
		}
	}

	/**
	 * Ensure can build long.
	 */
	@Test
	public void buildLong() {

		// Create scanner with data just for first long
		StreamBufferScanner scanner = createScanner(1, 1, 1, 1, 1, 1, 1, 1);

		// Ensure can build first long
		assertEquals(0x0101010101010101L, scanner.buildLong(shouldNotOccur), "Incorrect immediate long");
		assertEquals(0x0101010101010101L, scanner.buildLong(shouldNotOccur), "Should be able build same long");

		// Skip the long bytes
		scanner.skipBytes(8);

		// Ensure require further data for second long
		assertEquals(-1, scanner.buildLong(shouldNotOccur), "Should require further bytes for another long");

		// Incrementally add further data (ensuring requires all data)
		for (int i = 1; i <= 7; i++) {
			scanner.appendStreamBuffer(createBuffer(2));
			assertEquals(-1, scanner.buildLong(shouldNotOccur), "Only " + i + " bytes for long");
		}

		// Add remaining byte to now build the long
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals(0x0202020202020202L, scanner.buildLong(shouldNotOccur), "Incorrect built long");
		scanner.skipBytes(8);

		// Ensure can scan in 7 bytes then 1 byte (test the bulk data reads)
		scanner.appendStreamBuffer(createBuffer(3));
		assertEquals(-1, scanner.buildLong(shouldNotOccur), "Only first byte");
		scanner.appendStreamBuffer(createBuffer(3, 3, 3, 3, 3, 3, 3));
		assertEquals(0x0303030303030303L, scanner.buildLong(shouldNotOccur), "Should bulk read in bytes");
	}

	/**
	 * Ensure handle negative values in building long.
	 */
	@Test
	public void buildLongWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xfe);
		assertEquals(0xfffffffffffffffeL, scanner.buildLong(shouldNotOccur), "Should provide long value");
	}

	/**
	 * Series of 0xff bytes should not occur when building a long.
	 */
	@Test
	public void buildIllegalLong() {

		// Create the scanner with 0xff long value in bytes
		StreamBufferScanner scanner = createScanner(0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff);
		assertEquals(-1, scanner.buildLong(shouldNotOccur), "Not enough bytes");

		// Add the final byte for -1 long value
		scanner.appendStreamBuffer(createBuffer(0xff));
		final Exception exception = new Exception("TEST");
		try {
			scanner.buildLong(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame(exception, ex, "Should be same exception");
		}
	}

	/**
	 * Ensure can build a long and then scan within the long buffer to find the
	 * bytes.
	 */
	@Test
	public void buildLongThenScanIncludesBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 8; i++) {
			assertEquals(-1, scanner.buildLong(shouldNotOccur), "Not enough bytes");
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the long
		long bytes = scanner.buildLong(shouldNotOccur);
		assertEquals(0x0102030405060708L, bytes, "Incorrect long");

		// Skip 4 bytes (e.g. "GET ")
		scanner.skipBytes(4);

		// Ensure can get bytes from buffer long
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 6), 1000, shouldNotOccur);
		assertEquals(1, sequence.length(), "Incorrect number of bytes");
		assertEquals(5, sequence.byteAt(0), "Incorrect byte");
	}

	/**
	 * Ensure can build a long and then skip some of the bytes and include bytes in
	 * scan.
	 */
	@Test
	public void buildLongThenSkipThenScanIncludesBufferLongBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 8; i++) {
			assertEquals(-1, scanner.buildLong(shouldNotOccur), "Not enough bytes");
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the long
		long bytes = scanner.buildLong(shouldNotOccur);
		assertEquals(0x0102030405060708L, bytes, "Incorrect long");

		// Skip 4 bytes (e.g. "GET ")
		scanner.skipBytes(4);

		// Should not find the 4 value (as skipped past)
		ScanTarget target = new ScanTarget((byte) 4);
		assertNull(scanner.scanToTarget(target, 1000, shouldNotOccur), "Should not find skipped value");

		// Add skipped value (should find with all remaining long values)
		scanner.appendStreamBuffer(createBuffer(4, 5));
		StreamBufferByteSequence sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals(4, sequence.length(), "Incorrect number of bytes");
		for (int i = 0; i < 4; i++) {
			assertEquals(i + 5, sequence.byteAt(i), "Incorrect byte");
		}

		// Ensure continue to find further scan targets
		sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals(0, sequence.length(), "Should find again");
		sequence = scanner.scanToTarget(new ScanTarget((byte) 5), 1000, shouldNotOccur);
		assertEquals(1, sequence.length(), "Should find target");
		assertEquals(4, sequence.byteAt(0), "Incorrect value");
	}

	/**
	 * Ensure can build short.
	 */
	@Test
	public void buildShort() {

		// Create scanner with data just for first long
		StreamBufferScanner scanner = createScanner(1, 1);

		// Ensure can build first short
		assertEquals(0x0101L, scanner.buildShort(shouldNotOccur), "Incorrect immediate short");
		assertEquals(0x0101L, scanner.buildShort(shouldNotOccur), "Should be able build same long");

		// Skip the short bytes
		scanner.skipBytes(2);

		// Ensure require further data for second short
		assertEquals(-1, scanner.buildShort(shouldNotOccur), "Should require further bytes for another short");

		// Incrementally add further data (ensuring requires all data)
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals(-1, scanner.buildShort(shouldNotOccur), "Only 1 byte for short");

		// Add remaining byte to now build the short
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals(0x0202L, scanner.buildShort(shouldNotOccur), "Incorrect built short");
	}

	/**
	 * Ensure handle negative values in building short.
	 */
	@Test
	public void buildShortWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xff, 0xfe);
		assertEquals(-2, scanner.buildShort(shouldNotOccur), "Should provide short value");
	}

	/**
	 * Series of -1 bytes should not occur when building a short.
	 */
	@Test
	public void buildIllegalShort() {

		// Create the scanner with -1 short value in bytes
		StreamBufferScanner scanner = createScanner(0xff);
		assertEquals(-1, scanner.buildShort(shouldNotOccur), "Not enough bytes");

		// Add the final byte for -1 short value
		scanner.appendStreamBuffer(createBuffer(0xff));
		final Exception exception = new Exception("TEST");
		try {
			scanner.buildShort(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame(exception, ex, "Should be same exception");
		}
	}

	/**
	 * Ensure can build a short and then scan within the long buffer to find the
	 * bytes.
	 */
	@Test
	public void buildShortThenScanIncludesBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 2; i++) {
			assertEquals(-1, scanner.buildShort(shouldNotOccur), "Not enough bytes");
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the short
		short bytes = scanner.buildShort(shouldNotOccur);
		assertEquals(0x0102, bytes, "Incorrect short");

		// Add byte after short to find
		scanner.appendStreamBuffer(createBuffer(3));

		// Ensure can get bytes from buffer long
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 3), 1000, shouldNotOccur);
		assertEquals(2, sequence.length(), "Should find in the buffer");
		for (int i = 0; i < 2; i++) {
			assertEquals(i + 1, sequence.byteAt(i), "Incorrect bytes");
		}

		// Ensure no long find bytes from long buffer
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertNull(sequence, "Should not find byte");
	}

	/**
	 * Ensure can build a long and then skip some of the bytes and include bytes in
	 * scan.
	 */
	@Test
	public void buildShortThenSkipThenScanIncludesBufferLongBytes() {

		// Progressively add bytes
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 2; i++) {
			assertEquals(-1, scanner.buildLong(shouldNotOccur), "Not enough bytes");
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Ensure able to build the short
		short bytes = scanner.buildShort(shouldNotOccur);
		assertEquals(0x0102, bytes, "Incorrect short");

		// Skip 1 bytes (e.g. " ")
		scanner.skipBytes(1);

		// Should not find the 4 value (as skipped past)
		ScanTarget target = new ScanTarget((byte) 1);
		assertNull(scanner.scanToTarget(target, 1000, shouldNotOccur), "Should not find skipped value");

		// Add skipped value (should find with all remaining long values)
		scanner.appendStreamBuffer(createBuffer(1, 2));
		StreamBufferByteSequence sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals(1, sequence.length(), "Incorrect number of bytes");
		assertEquals(2, sequence.byteAt(0), "Incorrect byte");

		// Ensure continue to find further scan targets
		sequence = scanner.scanToTarget(target, 1000, shouldNotOccur);
		assertEquals(0, sequence.length(), "Should find again");
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertEquals(1, sequence.length(), "Should find target");
		assertEquals(1, sequence.byteAt(0), "Incorrect value");
	}

	/**
	 * Ensure can build byte.
	 */
	@Test
	public void buildByte() {

		// Create scanner with data just for first byte
		StreamBufferScanner scanner = createScanner(1);

		// Ensure can build first short
		assertEquals(0x01, scanner.buildByte(shouldNotOccur), "Incorrect immediate byte");
		assertEquals(0x01, scanner.buildByte(shouldNotOccur), "Should be able build same byte");

		// Skip the byte
		scanner.skipBytes(1);

		// Ensure require further data for second byte
		assertEquals(-1, scanner.buildByte(shouldNotOccur), "Should require further bytes for another byte");

		// Add another byte and ensure can obtain
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals(0x02, scanner.buildByte(shouldNotOccur), "Incorrect built byte");
	}

	/**
	 * Ensure handle negative values in building byte.
	 */
	@Test
	public void buildByteWithNegativeValues() {
		StreamBufferScanner scanner = createScanner(0xfe);
		assertEquals(-2, scanner.buildByte(shouldNotOccur), "Should provide byte value");
	}

	/**
	 * Series of -1 bytes should not occur when building a byte.
	 */
	@Test
	public void buildIllegalByte() {

		// Create the scanner with -1 short value in bytes
		StreamBufferScanner scanner = createScanner(0xff);
		final Exception exception = new Exception("TEST");
		try {
			scanner.buildByte(() -> exception);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame(exception, ex, "Should be same exception");
		}
	}

	/**
	 * Ensure able to build byte then short then long from same position.
	 */
	@Test
	public void buildByteThenShortThenLong() {

		// Create the scanner with values
		StreamBufferScanner scanner = createScanner(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08);

		// Ensure can build byte
		assertEquals(0x01, scanner.buildByte(shouldNotOccur), "Incorrect built byte");

		// Ensure can build short
		assertEquals(0x0102, scanner.buildShort(shouldNotOccur), "Incorrect built short");

		// Ensure can build long
		assertEquals(0x0102030405060708L, scanner.buildLong(shouldNotOccur), "Incorrect built long");
	}

	/**
	 * Ensure able to build long then short then byte from same position.
	 */
	@Test
	public void buildLongThenShortThenByte() {

		// Create the scanner with values
		StreamBufferScanner scanner = createScanner(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08);

		// Ensure can build long
		assertEquals(0x0102030405060708L, scanner.buildLong(shouldNotOccur), "Incorrect built long");

		// Ensure can build short
		assertEquals(0x0102, scanner.buildShort(shouldNotOccur), "Incorrect built short");

		// Ensure can build byte
		assertEquals(0x01, scanner.buildByte(shouldNotOccur), "Incorrect built byte");
	}

	/**
	 * Ensure can build long and re-use bytes for scan.
	 */
	@Test
	public void progressivelyBuildLongThenScanToTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertEquals(-1, scanner.buildLong(shouldNotOccur), "Should not build");

		// Progressively build the long
		for (int i = 1; i < 7; i++) {
			scanner.appendStreamBuffer(createBuffer(i + 1));
			assertEquals(-1, scanner.buildLong(shouldNotOccur), "Should not build after " + i);
		}
		scanner.appendStreamBuffer(createBuffer(8));
		assertEquals(0x0102030405060708L, scanner.buildLong(shouldNotOccur), "Incorrect built long");

		// Ensure scan to target includes the long
		ScanTarget targetSeven = new ScanTarget((byte) 7);
		StreamBufferByteSequence sequence = scanner.scanToTarget(targetSeven, 1000, shouldNotOccur);
		assertEquals(6, sequence.length(), "Should have scanned bytes");
		for (int i = 0; i < 6; i++) {
			assertEquals(i + 1, sequence.byteAt(i), "Incorrect byte");
		}

		// Ensure able to scan again
		sequence = scanner.scanToTarget(targetSeven, 1000, shouldNotOccur);
		assertEquals(0, sequence.length(), "Should find again, but no data");

		// Ensure able to scan beyond the byte
		ScanTarget targetNine = new ScanTarget((byte) 9);
		sequence = scanner.scanToTarget(targetNine, 1000, shouldNotOccur);
		assertNull(sequence, "Should not find byte");

		// Ensure after adding target, includes the remaining long bytes
		scanner.appendStreamBuffer(createBuffer(9));
		sequence = scanner.scanToTarget(targetNine, 1000, shouldNotOccur);
		assertEquals(2, sequence.length(), "Should include remaining long content");
		for (int i = 0; i < 2; i++) {
			assertEquals(i + 7, sequence.byteAt(i), "Incorrect long byte");
		}
	}

	/**
	 * Ensure can build short and re-use bytes for scan.
	 */
	@Test
	public void progressivelyBuildShortThenScanToTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertEquals(-1, scanner.buildShort(shouldNotOccur), "Should not build");

		// Progressively build the short
		scanner.appendStreamBuffer(createBuffer(2));
		assertEquals(0x0102, scanner.buildShort(shouldNotOccur), "Incorrect built short");

		// Ensure scan to target includes the short
		ScanTarget targetTwo = new ScanTarget((byte) 2);
		StreamBufferByteSequence sequence = scanner.scanToTarget(targetTwo, 1000, shouldNotOccur);
		assertEquals(1, sequence.length(), "Should have scanned bytes");
		assertEquals(1, sequence.byteAt(0), "Incorrect byte");

		// Ensure able to scan again
		sequence = scanner.scanToTarget(targetTwo, 1000, shouldNotOccur);
		assertEquals(0, sequence.length(), "Should find again, but no data");

		// Ensure able to scan beyond the byte
		ScanTarget targetThree = new ScanTarget((byte) 3);
		sequence = scanner.scanToTarget(targetThree, 1000, shouldNotOccur);
		assertNull(sequence, "Should not find byte");

		// Ensure after adding target, includes the remaining short bytes
		scanner.appendStreamBuffer(createBuffer(3));
		sequence = scanner.scanToTarget(targetThree, 1000, shouldNotOccur);
		assertEquals(1, sequence.length(), "Should include remaining short content");
		assertEquals(2, sequence.byteAt(0), "Incorrect short remaining byte");
	}

	/**
	 * Ensure can build byte and re-use bytes for scan.
	 */
	@Test
	public void progressivelyBuildByteThenScanToTarget() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertEquals(0x0001, scanner.buildByte(shouldNotOccur), "Incorrect built byte");

		// Ensure scan to target includes the short
		StreamBufferByteSequence sequence = scanner.scanToTarget(new ScanTarget((byte) 1), 1000, shouldNotOccur);
		assertEquals(0, sequence.length(), "Should have scanned bytes");

		// Ensure able to scan again
		sequence = scanner.scanToTarget(new ScanTarget((byte) 1), 1000, shouldNotOccur);
		assertEquals(0, sequence.length(), "Should find again, but no data");

		// Ensure able to scan beyond the byte
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertNull(sequence, "Should not find byte");

		// Ensure after adding target, includes the remaining short bytes
		scanner.appendStreamBuffer(createBuffer(2));
		sequence = scanner.scanToTarget(new ScanTarget((byte) 2), 1000, shouldNotOccur);
		assertEquals(1, sequence.length(), "Should include remaining byte content");
		assertEquals(1, sequence.byteAt(0), "Incorrect remaining byte");
	}

	/**
	 * Ensure can peek bytes.
	 */
	@Test
	public void peekBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3);

		// Ensure not find byte
		assertEquals(-1, scanner.peekToTarget(new ScanTarget((byte) 4)), "Should not find byte");
		assertEquals(2, scanner.peekToTarget(new ScanTarget((byte) 3)), "Should find byte");

		// Should not have progressed through buffer data
		assertEquals(0x0102, scanner.buildShort(shouldNotOccur), "Should still be at start");
	}

	/**
	 * Ensure can peek bytes by larger buffer.
	 */
	@Test
	public void peekBytesOnLargerBuffer() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1);

		// Skip first 2 bytes
		scanner.skipBytes(2);

		// Ensure find byte
		assertEquals(12, scanner.peekToTarget(new ScanTarget((byte) 1)), "Incorrect offset position from skip");
	}

	/**
	 * Ensure can scan in fixed set of bytes.
	 */
	@Test
	public void scanBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3, 4, 5, 6);

		// Scan in bytes
		StreamBufferByteSequence sequence = scanner.scanBytes(3);
		assertEquals(3, sequence.length(), "Incorrect number of bytes");
		for (int i = 0; i < 3; i++) {
			assertEquals(i + 1, sequence.byteAt(i), "Incorrect byte");
		}

		// Scan in remaining bytes
		sequence = scanner.scanBytes(3);
		assertEquals(3, sequence.length(), "Should have remaining bytes");
		for (int i = 0; i < 3; i++) {
			assertEquals(i + 4, sequence.byteAt(i), "Incorrect byte");
		}
	}

	/**
	 * Ensure can scan buffer and reset buffers.
	 */
	@Test
	public void resetBufferOnScanBytes() {

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1, 2, 3);

		// Scan in bytes
		StreamBufferByteSequence one = scanner.scanBytes(3);
		assertEquals(3, one.length(), "Incorrect number of bytes");

		// Add further bytes and scan in
		scanner.appendStreamBuffer(createBuffer(4, 5, 6));
		StreamBufferByteSequence two = scanner.scanBytes(3);
		assertEquals(3, two.length(), "Incorrect number of additional bytes");

		// Ensure correct bytes
		for (int i = 0; i < 3; i++) {
			assertEquals(i + 1, one.byteAt(i), "Incorrect first byte");
		}
		for (int i = 0; i < 3; i++) {
			assertEquals(i + 4, two.byteAt(i), "Incorrect second byte");
		}
	}

	/**
	 * Ensure can scan in fixed set of bytes.
	 */
	@Test
	public void scanBytesAcrossMultipleBuffers() {

		final int BYTES_LENGTH = 10;

		// Create the scanner
		StreamBufferScanner scanner = createScanner(1);
		assertNull(scanner.scanBytes(BYTES_LENGTH), "Not enough bytes");

		// Scan in bytes (incrementally in worst case scenario)
		for (int i = 1; i < BYTES_LENGTH; i++) {
			assertNull(scanner.scanBytes(BYTES_LENGTH), "Should not obtain sequence, as not enough bytes - " + i);
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}

		// Should now obtain sequence
		StreamBufferByteSequence sequence = scanner.scanBytes(BYTES_LENGTH);
		assertNotNull(sequence, "Should have all bytes");

		// Ensure correct bytes
		assertEquals(BYTES_LENGTH, sequence.length(), "Incorrect number of bytes");
		for (int i = 0; i < BYTES_LENGTH; i++) {
			assertEquals(i + 1, sequence.byteAt(i), "Incorrect byte");
		}
	}

	/**
	 * Ensure can scan bytes from previous buffers (as previously built long).
	 */
	@Test
	public void scanBytesFromPreviousBuffers() {

		// Create the scanner (and build long)
		StreamBufferScanner scanner = createScanner(1);
		for (int i = 1; i < 8; i++) {
			assertEquals(-1, scanner.buildLong(shouldNotOccur), "Not enough bytes");
			scanner.appendStreamBuffer(createBuffer(i + 1));
		}
		assertEquals(0x0102030405060708L, scanner.buildLong(shouldNotOccur), "Should have long");

		// Ensure can scan in no data from previous buffer
		StreamBufferByteSequence sequence = scanner.scanBytes(0);
		assertEquals(0, sequence.length(), "Should be empty");

		// Ensure can scan in just previous buffer data
		sequence = scanner.scanBytes(4);
		assertEquals(4, sequence.length(), "Incorrect number of bytes");
		for (int i = 0; i < 4; i++) {
			assertEquals(i + 1, sequence.byteAt(i), "Incorrect byte");
		}

		// Ensure can scan past previous buffer
		scanner.appendStreamBuffer(createBuffer(9, 10, 11, 12));
		sequence = scanner.scanBytes(8);
		assertEquals(8, sequence.length(), "Incorrect number of bytes");
		for (int i = 0; i < 8; i++) {
			assertEquals(i + 5, sequence.byteAt(i), "Incorrect byte");
		}
	}

	/**
	 * Creates a number buffer with value being repeated for rest of long bytes.
	 * Then scans for that value to ensure returns first.
	 * 
	 * @param value Value.
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
	 * @param buffer        {@link StreamBuffer}.
	 * @param startPosition Start position within the {@link ByteBuffer}.
	 * @param value         Byte value to scan for.
	 * @return {@link StreamBufferByteSequence}. Or <code>null</code> if not found.
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
	 * @param values Values for the data.
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
	 * @param values Byte values for the {@link StreamBuffer}.
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
	 * @param data Data for the {@link StreamBuffer}.
	 * @return {@link StreamBuffer}.
	 */
	private static StreamBuffer<ByteBuffer> createBuffer(byte[] data) {
		MockStreamBufferPool pool = new MockStreamBufferPool(() -> ByteBuffer.allocate(data.length));
		StreamBuffer<ByteBuffer> buffer = pool.getPooledStreamBuffer(OVERLOAD_HANDLER);
		buffer.write(data);
		return buffer;
	}

}
