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
package net.officefloor.server.http.parse.impl;

import java.nio.ByteBuffer;

/**
 * Provides utility methods for scanning a {@link ByteBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class ByteBufferScanner {

	public static long createScanByteMask(byte value) {
		long mask = value;
		for (int i = 0; i < 7; i++) {
			mask <<= 8; // move bytes up
			mask |= value; // add in the value
		}
		return mask;
	}

	public static final int scanToByte(ByteBuffer buffer, int startPosition, byte value, long mask) {

		// Obtain the end position (exclusive)
		int endPosition = buffer.position();

		// Ensure avoid reading past buffer end
		int lastBytesStart = endPosition - 7;

		// Iterate over longs (8 bytes) for value
		while (startPosition < lastBytesStart) {

			// Obtain the 8 bytes
			long bytes = buffer.getLong(startPosition);
			
			// TODO REMOVE
			String bytesHex = Long.toHexString(bytes);

			// Determine if the bytes contain the value
			long xorWithMask = bytes ^ mask; // matching byte will be 0
			
			// TODO REMOVE
			String xorWithMaskHex = Long.toHexString(xorWithMask); 

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
			long overflow = xorWithMask + 0x7f7f7f7f7f7f7f7fl;
			
			// TODO REMOVE
			String overflowHex = Long.toHexString(overflow);
			
			long topBitCheck = overflow & 0x8080808080808080l;
			
			// TODO REMOVE
			String topBitCheckHex = Long.toHexString(topBitCheck);
			
			while (topBitCheck != 0x8080808080808080l) {

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
				long IIII_OOOO_Check = topBitCheck & 0x00000000ffffffffl;
				
				// TODO REMOVE
				String IIII_OOOO_CheckHex = Long.toHexString(IIII_OOOO_Check);
				
				if (IIII_OOOO_Check == 0x0000000080808080l) {
					// First byte of interest in first 4 bytes
					long IIOO_OOOO_Check = topBitCheck & 0x0000ffff00000000l;
					if (IIOO_OOOO_Check == 0x0000808000000000l) {
						// First byte of interest in first 2 bytes
						long IOOO_OOOO_Check = topBitCheck & 0x00ff000000000000l;
						if (IOOO_OOOO_Check == 0x0080000000000000l) {

							// Check first byte
							long check = bytes & 0xff00000000000000l;
							check >>= 56; // 7 x 8 bits
							if (check == value) {
								// Found at first byte
								return startPosition;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x8000000000000000l;
							}

						} else {

							// Check second byte
							long check = bytes & 0x00ff000000000000l;
							check >>= 48; // 6 x 8 bits
							if (check == value) {
								// Found at second byte
								return startPosition + 1;
							} else {
								// False positive, set top bit to ignore
								topBitCheck |= 0x0080000000000000l;
							}

						}
					} else {
						// First byte of interest in second 2 bytes
						long OOIO_OOOO_Check = topBitCheck & 0x0000ff00000000000000l;
						if (OOIO_OOOO_Check == 0x00008000000000000000l) {

						} else {

						}
					}
				} else {
					// First byte of interest in last 4 bytes

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

	/**
	 * All access via static methods.
	 */
	private ByteBufferScanner() {
	}

}