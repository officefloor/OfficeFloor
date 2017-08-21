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
package net.officefloor.server.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Provides scanning of {@link StreamBuffer} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferScanner {

	/**
	 * Scan target.
	 */
	public static class ScanTarget {

		/**
		 * Byte value to scan.
		 */
		public final byte value;

		/**
		 * Mask for the value.
		 */
		public final long mask;

		/**
		 * Instantiate.
		 * 
		 * @param value
		 *            Target byte for scan.
		 */
		public ScanTarget(byte value) {
			this.value = value;

			// Create the mask from the value
			long mask = value;
			for (int i = 0; i < 7; i++) {
				mask <<= 8; // move bytes up
				mask |= value; // add in the value
			}
			this.mask = mask;
		}
	}

	/**
	 * List of {@link StreamBuffer} instances involved up to current position.
	 * Typically, content should not span more than two {@link StreamBuffer}
	 * instances.
	 */
	private final List<StreamBuffer<ByteBuffer>> buffers = new ArrayList<>(2);

	/**
	 * Appends {@link StreamBuffer} for scanning.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer}.
	 */
	public void appendStreamBuffer(StreamBuffer<ByteBuffer> buffer) {
	}

	/**
	 * Builds a long from the {@link StreamBuffer} at current position.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer} to build a long from.
	 * @param invalidHttpCharacterExceptionFactory
	 *            Factory to create an {@link Exception} should there be an
	 *            invalid HTTP character.
	 * @return Long value, otherwise <code>-1</code> if not enough bytes in
	 *         {@link StreamBuffer} to build a long.
	 * @throws T
	 *             If invalid HTTP character.
	 */
	public <T extends Throwable> long buildLong(Supplier<T> invalidHttpCharacterExceptionFactory) throws T {
		// TODO implement
		return -1;
	}

	public <T extends Throwable> short buildShort(Supplier<T> invalidHttpCharacterExceptionFactory) throws T {
		// TODO implement
		return -1;
	}

	public int skipBytes(int numberOfBytes) {
		return -1;
	}

	public <T extends Throwable> int peekToTarget(ScanTarget target, int maxBytesLength,
			Supplier<T> tooLongExceptionFactory) throws T {
		// TODO implement
		return -1;
	}

	public StreamBufferByteSequence scanBytes(int numberOfBytes) {
		// TODO implement
		return null;
	}

	/**
	 * Scans the {@link StreamBuffer} for the byte value from the current
	 * position.
	 * 
	 * @param target
	 *            {@link ScanTarget}.
	 * @return {@link StreamBufferByteSequence} if found the byte. Otherwise,
	 *         <code>null</code> indicating further {@link StreamBuffer}
	 *         instances may contain the byte.
	 */
	public <T extends Throwable> StreamBufferByteSequence scanToTarget(ScanTarget target, int maxBytesLength,
			Supplier<T> tooLongExceptionFactory) throws T {
		// TODO implement
		return null;
	}

	/**
	 * <p>
	 * Scans the {@link ByteBuffer} to find the next byte of particular value.
	 * <p>
	 * Note: the algorithm used does not handle negative byte values. However,
	 * the values (ASCII characters) searched for in HTTP parsing are all
	 * positive.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer} to scan. Position indicates last value
	 *            (exclusively).
	 * @param startPosition
	 *            Start position in the {@link ByteBuffer} to start scanning
	 *            from.
	 * @param value
	 *            Byte value to scan for.
	 * @param mask
	 *            Scan mask. See {@link #createScanByteMask(byte)}.
	 * @return Index within the {@link ByteBuffer} of the next byte value. Or
	 *         <code>-1</code> if does not find the byte.
	 */
	public static final int scanToByte(ByteBuffer buffer, int startPosition, byte value, long mask) {

		// Obtain the end position (exclusive)
		int endPosition = buffer.position();

		// Ensure avoid reading past buffer end
		int lastBytesStart = endPosition - 7;

		// Iterate over longs (8 bytes) for value
		while (startPosition < lastBytesStart) {

			// Obtain the 8 bytes
			long bytes = buffer.getLong(startPosition);

			// Determine if the bytes contain the value
			long xorWithMask = bytes ^ mask; // matching byte will be 0

			/*
			 * Need to find if any byte is zero. As ASCII is 7 bits, can use the
			 * top bit for overflow to find if any byte is zero. In other words,
			 * add 0x7f (01111111) to each byte and if top bit is 1, then not
			 * the byte of interest (as byte is 0 from previous XOR).
			 * 
			 * Note: top bit value of 0 may only mean potential matching byte,
			 * as could have UTF-8 encoded characters. However, the separation
			 * characters for HTTP parsing are all 7 bit values. Therefore, will
			 * need to check for false positives (and null 0). However, most
			 * HTTP content is 7 bits so win efficiency in scanning past bytes
			 * not of interest.
			 */
			long overflowNonZero = xorWithMask + 0x7f7f7f7f7f7f7f7fL;
			long topBitCheck = overflowNonZero & 0x8080808080808080L;
			while (topBitCheck != 0x8080808080808080L) {

				/*
				 * Potential value within the 8 bytes, so search for it.
				 * 
				 * To reduce operations, use binary search to determine where
				 * byte of interest is.
				 * 
				 * Note: as may have two bytes of interest within the range,
				 * need to find the first actual matching byte. Example case is
				 * UTF-8 creating false positives. However, also HTTP with end
				 * of HTTP headers having repeating CR LF bytes.
				 */
				long IIII_OOOO_Check = topBitCheck & 0xffffffff00000000L;
				if (IIII_OOOO_Check != 0x8080808000000000L) {
					// First byte of interest in first 4 bytes
					long IIOO_OOOO_Check = topBitCheck & 0xffff000000000000L;
					if (IIOO_OOOO_Check != 0x8080000000000000L) {
						// First byte of interest in first 2 bytes
						long IOOO_OOOO_Check = topBitCheck & 0xff00000000000000L;
						if (IOOO_OOOO_Check != 0x8000000000000000L) {

							// Check first byte
							long check = bytes & 0xff00000000000000L;
							check >>= 56; // 7 x 8 bits
							if (check == value) {
								// Found at first byte
								return startPosition;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x8000000000000000L;
							}

						} else {

							// Check second byte
							long check = bytes & 0x00ff000000000000L;
							check >>= 48; // 6 x 8 bits
							if (check == value) {
								// Found at second byte
								return startPosition + 1;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x0080000000000000L;
							}

						}
					} else {
						// First byte of interest in second 2 bytes
						long OOIO_OOOO_Check = topBitCheck & 0x0000ff0000000000L;
						if (OOIO_OOOO_Check != 0x0000800000000000L) {

							// Check third byte
							long check = bytes & 0x0000ff0000000000L;
							check >>= 40; // 5 x 8 bits
							if (check == value) {
								// Found at third byte
								return startPosition + 2;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x0000800000000000L;
							}

						} else {

							// Check fourth byte
							long check = bytes & 0x000000ff00000000L;
							check >>= 32; // 4 x 8 bits
							if (check == value) {
								// Found at fourth byte
								return startPosition + 3;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x0000008000000000L;
							}

						}
					}
				} else {
					// First byte of interest in last 4 bytes
					long OOOO_IIOO_Check = topBitCheck & 0x00000000ffff0000L;
					if (OOOO_IIOO_Check != 0x0000000080800000L) {
						// First byte of interest in third 2 bytes
						long OOOO_IOOO_Check = topBitCheck & 0x00000000ff000000L;
						if (OOOO_IOOO_Check != 0x0000000080000000L) {

							// Check fifth byte
							long check = bytes & 0x00000000ff000000L;
							check >>= 24; // 3 x 8 bits
							if (check == value) {
								// Found at fifth byte
								return startPosition + 4;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x0000000080000000L;
							}

						} else {

							// Check sixth byte
							long check = bytes & 0x0000000000ff0000L;
							check >>= 16; // 2 x 8 bits
							if (check == value) {
								// Found at sixth byte
								return startPosition + 5;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x0000000000800000L;
							}

						}
					} else {
						// First byte of interest in fourth 2 bytes
						long OOOO_OOIO_Check = topBitCheck & 0x000000000000ff00L;
						if (OOOO_OOIO_Check != 0x0000000000008000L) {

							// Check seventh byte
							long check = bytes & 0x000000000000ff00L;
							check >>= 8; // 1 x 8 bits
							if (check == value) {
								// Found at seventh byte
								return startPosition + 6;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x0000000000008000L;
							}

						} else {

							// Check eighth byte
							long check = bytes & 0x00000000000000ffL;
							// bytes already in correct place for check
							if (check == value) {
								// Found at eighth byte
								return startPosition + 7;
							} else {
								// False positive, set top bit to ignore
								// Note: should exit loop
								topBitCheck |= 0x0000000000000080L;
							}

						}
					}
				}
			}

			// Value not within the 8 bytes
			// Increment for next 8 bytes
			startPosition += 8;
		}

		// Need to check the remaining bytes of buffer
		while (startPosition < endPosition) {

			// Check the byte
			byte check = buffer.get(startPosition);
			if (check == value) {
				// Found the byte
				return startPosition;
			}

			// Check the next byte
			startPosition++;
		}

		// As here, did not find value in buffer
		return -1;
	}

}