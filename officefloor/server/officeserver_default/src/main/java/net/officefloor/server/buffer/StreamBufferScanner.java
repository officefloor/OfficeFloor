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
	 * <p>
	 * List of previous {@link StreamBuffer} instances involved before current
	 * {@link StreamBuffer}.
	 * <p>
	 * Typically, content should not span more than one previous
	 * {@link StreamBuffer} (assuming {@link ByteBuffer} sizes are reasonably
	 * large).
	 */
	private final List<StreamBuffer<ByteBuffer>> previousBuffers = new ArrayList<>(1);

	/**
	 * Number of bytes from previous buffers.
	 */
	private int previousBufferBytes = 0;

	/**
	 * Current {@link StreamBuffer}.
	 */
	private StreamBuffer<ByteBuffer> currentBuffer = null;

	/**
	 * Start position to read data for {@link StreamBufferByteSequence}.
	 */
	private int start = -1;

	/**
	 * Position within the current {@link StreamBuffer}.
	 */
	private int position = 0;

	/**
	 * Past buffer long bytes.
	 */
	private long pastBufferLong = 0;

	/**
	 * Appends {@link StreamBuffer} for scanning.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer}.
	 */
	public void appendStreamBuffer(StreamBuffer<ByteBuffer> buffer) {

		// Do nothing if same buffer (just data appended)
		if (buffer == this.currentBuffer) {
			return;
		}

		// Keep track of previous buffer
		if (this.currentBuffer != null) {
			this.previousBuffers.add(this.currentBuffer);
		}

		// Now the current buffer
		this.currentBuffer = buffer;
		this.position = 0; // start at beginning of buffer
	}

	/**
	 * Builds a long (8 bytes) from the {@link StreamBuffer} at current
	 * position.
	 * 
	 * @param illegalValueExceptionFactory
	 *            {@link Supplier} to create {@link Throwable} for illegal long
	 *            value.
	 * @return Long value, otherwise <code>-1</code> if not enough bytes in
	 *         {@link StreamBuffer} to build a long.
	 * @throws T
	 *             If invalid value.
	 */
	public <T extends Throwable> long buildLong(Supplier<T> illegalValueExceptionFactory) throws T {

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.getPooledBuffer();
		int remaining = data.position() - this.position;

		// Determine if build immediately (typical case)
		if ((this.previousBufferBytes == 0) && (remaining >= 8)) {

			// Obtain the bytes directly from buffer
			long returnBytes = data.getLong(this.position);

			// Ensure legal value
			if (returnBytes == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the short bytes
			return returnBytes;
		}

		// Append to previous bytes
		remaining = Math.min(remaining, 8 - this.previousBufferBytes);
		this.previousBufferBytes += remaining; // will be appending bytes
		int position = this.position;
		while (remaining != 0) {
			switch (remaining) {
			case 7:
			case 6:
			case 5:
			case 4:
				// Take 4 bytes and append
				int intBytes = data.getInt(position);
				position += 4; // read the 4 bytes
				this.pastBufferLong <<= 32; // move up 4 bytes
				this.pastBufferLong += intBytes & 0xffffffff;
				remaining -= 4; // 4 bytes
				break;

			case 3:
			case 2:
				// Take 2 bytes and append
				int shortBytes = data.getShort(position);
				position += 2; // read the 2 bytes
				this.pastBufferLong <<= 16; // move up 2 bytes
				this.pastBufferLong += shortBytes & 0xffff;
				remaining -= 2; // 2 bytes
				break;

			case 1:
				// Append the single byte
				int singleByte = data.get(position);
				this.pastBufferLong <<= 8; // move up a byte
				this.pastBufferLong += singleByte & 0xff;
				remaining = 0; // should always complete on last single byte
				break;
			}
		}

		// Determine if have the long
		if (this.previousBufferBytes == 8) {
			// Have all bytes, so reset and return long
			long returnBytes = this.pastBufferLong;
			this.pastBufferLong = 0;
			this.previousBufferBytes = 0;

			// Ensure legal value
			if (returnBytes == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the long bytes
			return returnBytes;
		}

		// As here, not enough bytes to build long
		return -1;
	}

	/**
	 * Builds a short (2 bytes) from the {@link StreamBuffer} at current
	 * position.
	 *
	 * @param illegalValueExceptionFactory
	 *            {@link Supplier} to create {@link Throwable} for illegal short
	 *            value.
	 * @return Short value, otherwise <code>-1</code> if not enough bytes in
	 *         {@link StreamBuffer} to build a short.
	 * @throws T
	 *             If invalid value.
	 */
	public <T extends Throwable> short buildShort(Supplier<T> illegalValueExceptionFactory) throws T {

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.getPooledBuffer();
		int remaining = data.position() - this.position;

		// Determine if build immediately (typical case)
		if ((this.previousBufferBytes == 0) && (remaining >= 2)) {

			// Obtain the bytes directly from buffer
			short returnBytes = data.getShort(this.position);

			// Ensure legal value
			if (returnBytes == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the short bytes
			return returnBytes;
		}

		// Append to previous bytes (could be buffers of 1 byte each)
		remaining = Math.min(remaining, 2 - this.previousBufferBytes);
		this.previousBufferBytes += remaining; // will be appending bytes
		if (remaining == 1) {
			// Append the single byte
			// Will only ever be 1 byte appending, otherwise have short
			int singleByte = data.get(this.position);
			this.pastBufferLong <<= 8; // move up a byte
			this.pastBufferLong += singleByte & 0xff;
		}

		// Determine if have the short
		if (this.previousBufferBytes == 2) {

			// Have all bytes, so reset and return short
			short returnBytes = (short) (this.pastBufferLong & 0xffff);
			this.pastBufferLong = 0;
			this.previousBufferBytes = 0;

			// Ensure legal value
			if (returnBytes == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the short bytes
			return returnBytes;
		}

		// As here, not enough bytes to build short
		return -1;
	}

	/**
	 * Skips forward the particular number of bytes in the current
	 * {@link StreamBuffer}.
	 * 
	 * @param numberOfBytes
	 *            Number of bytes to skip.
	 */
	public void skipBytes(int numberOfBytes) {

		// Skip forward the bytes
		this.position += numberOfBytes;
	}

	/**
	 * Peeks in the current {@link StreamBuffer} to attempt to find the
	 * {@link ScanTarget}.
	 * 
	 * @param target
	 *            {@link ScanTarget}.
	 * @return Position of the {@link ScanTarget} within the current
	 *         {@link StreamBuffer} (starting at current position). Otherwise,
	 *         <code>-1</code> to indicate did not find {@link ScanTarget} in
	 *         current {@link StreamBuffer}.
	 */
	public <T extends Throwable> int peekToTarget(ScanTarget target) throws T {

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.getPooledBuffer();
		int lastPosition = data.position();

		// Obtain current position in the buffer to start scanning
		int position = this.position;

		// Keep reading until end of current buffer
		int lastFullReadPosition = lastPosition - 7;
		while (position < lastFullReadPosition) {

			// Read in the long (8 bytes)
			long bytes = data.getLong(position);

			// Determine if byte in long
			int index = indexOf(bytes, target);
			if (index != -1) {
				// Found the byte (offset of current position)
				return position + index - this.position;
			}

			// Not in long, so try next long (8 bytes)
			position += 8;
		}

		// Attempt to find in possible remaining bytes
		while (position < data.position()) {

			// At worse, 7 bytes so just check each
			byte value = data.get(position);
			if (value == target.value) {
				// Found the byte (offset of current position)
				return position - this.position;
			}

			// Attempt at next position
			position++;
		}

		// As here, did not find byte in the buffer
		return -1;
	}

	/**
	 * Scans a specific number of bytes.
	 * 
	 * @param numberOfBytes
	 *            Number of bytes to scan.
	 * @return {@link StreamBufferByteSequence} to the scanned bytes.
	 */
	public StreamBufferByteSequence scanBytes(long numberOfBytes) {

		// Ensure capture start
		if (this.start == -1) {
			this.start = this.position;
		}

		// Determine remaining bytes required
		long requiredBytes = numberOfBytes - this.previousBufferBytes;

		// Determine number of available bytes in current buffer
		ByteBuffer data = this.currentBuffer.getPooledBuffer();
		int availableBytes = data.position() - this.position;

		// Determine if have bytes to complete scan
		if (requiredBytes <= availableBytes) {

			// Have all bytes so create byte sequence
			// (Buffer size should always be less than max int)
			this.position = (int) (this.position + requiredBytes);
			return this.createByteSequence();
		}

		// Not enough bytes (consume buffer)
		this.previousBufferBytes += availableBytes;
		this.position = data.position();
		return null;
	}

	/**
	 * <p>
	 * Scans the {@link StreamBuffer} for the byte value from the current
	 * position.
	 * <p>
	 * The returned {@link StreamBufferByteSequence} is inclusive of the current
	 * position but exclusive of the target byte. This is because target bytes
	 * for HTTP are typically delimiters (eg. space, CR, etc) that are not
	 * included in the bytes of interest.
	 * 
	 * @param target
	 *            {@link ScanTarget}.
	 * @return {@link StreamBufferByteSequence} if found the byte. Otherwise,
	 *         <code>null</code> indicating further {@link StreamBuffer}
	 *         instances may contain the byte.
	 */
	public <T extends Throwable> StreamBufferByteSequence scanToTarget(ScanTarget target, int maxBytesLength,
			Supplier<T> tooLongExceptionFactory) throws T {

		// Ensure capture start
		if (this.start == -1) {
			this.start = this.position;
		}

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.getPooledBuffer();
		int lastPosition = data.position();

		// Keep reading until end of current buffer
		int lastFullReadPosition = lastPosition - 7;
		while (this.position < lastFullReadPosition) {

			// Read in the long (8 bytes)
			long bytes = data.getLong(this.position);

			// Determine if byte in long
			int index = indexOf(bytes, target);
			if (index != -1) {
				// Found the byte
				this.position += index;
				return this.createByteSequence();
			}

			// Not in long, so try next long (8 bytes)
			this.position += 8;
		}

		// Attempt to find in possible remaining bytes
		while (this.position < data.position()) {

			// At worse, 7 bytes so just check each
			byte value = data.get(position);
			if (value == target.value) {
				// Found the byte
				return this.createByteSequence();
			}

			// Attempt at next position
			this.position++;
		}

		// As here, did not find byte in the buffer
		return null;
	}

	/**
	 * <p>
	 * Creates the {@link StreamBufferByteSequence} up to current position.
	 * <p>
	 * This will also reset for obtaining the next
	 * {@link StreamBufferByteSequence}.
	 * 
	 * @return {@link StreamBufferByteSequence} up to current position.
	 */
	private StreamBufferByteSequence createByteSequence() {

		// Create the sequence
		StreamBufferByteSequence sequence;
		if (this.previousBuffers.size() == 0) {

			// Just current buffer
			int length = this.position - this.start;
			sequence = new StreamBufferByteSequence(this.currentBuffer, this.start, length);

		} else {

			// Previous buffers
			StreamBuffer<ByteBuffer> firstBuffer = this.previousBuffers.get(0);
			int length = firstBuffer.getPooledBuffer().position() - this.start;
			sequence = new StreamBufferByteSequence(firstBuffer, this.start, length);

			// Add in remaining previous buffers
			for (int i = 1; i < this.previousBuffers.size(); i++) {
				StreamBuffer<ByteBuffer> nextBuffer = this.previousBuffers.get(i);
				length = nextBuffer.getPooledBuffer().position();
				sequence.appendStreamBuffer(nextBuffer, 0, length);
			}

			// Add in the current buffer
			length = this.currentBuffer.getPooledBuffer().position();
			sequence.appendStreamBuffer(this.currentBuffer, 0, length);

			// Reset previous buffers, as now in byte sequence
			this.previousBuffers.clear();
		}

		// Reset
		this.start = -1;

		// Return the sequence
		return sequence;
	}

	/**
	 * <p>
	 * Scans the {@link ByteBuffer} to find the next byte of particular value.
	 * <p>
	 * Note: the algorithm used does not handle negative byte values. However,
	 * the values (ASCII characters) searched for in HTTP parsing are all
	 * positive (7 bits of the byte).
	 * 
	 * @param bytes
	 *            Long of 8 bytes to check for the value.
	 * @param target
	 *            {@link ScanTarget} for the byte to find the index.
	 * @return Index within the long of the first byte value. Otherwise,
	 *         <code>-1</code> if does not find the byte.
	 */
	public static final int indexOf(long bytes, ScanTarget target) {

		// Determine if the bytes contain the value
		long xorWithMask = bytes ^ target.mask; // matching byte will be 0

		/*
		 * Need to find if any byte is zero. As ASCII is 7 bits, can use the top
		 * bit for overflow to find if any byte is zero. In other words, add
		 * 0x7f (01111111) to each byte and if top bit is 1, then not the byte
		 * of interest (as byte is 0 from previous XOR).
		 * 
		 * Note: top bit value of 0 may only mean potential matching byte, as
		 * could have UTF-8 encoded characters. However, the separation
		 * characters for HTTP parsing are all 7 bit values. Therefore, will
		 * need to check for false positives (and null 0). However, most HTTP
		 * content is 7 bits so win efficiency in scanning past bytes not of
		 * interest.
		 */
		long overflowNonZero = xorWithMask + 0x7f7f7f7f7f7f7f7fL;
		long topBitCheck = overflowNonZero & 0x8080808080808080L;
		while (topBitCheck != 0x8080808080808080L) {

			/*
			 * Potential value within the 8 bytes, so search for it.
			 * 
			 * To reduce operations, use binary search to determine where byte
			 * of interest is.
			 * 
			 * Note: as may have two bytes of interest within the range, need to
			 * find the first actual matching byte. Example case is UTF-8
			 * creating false positives. However, also HTTP with end of HTTP
			 * headers having repeating CR LF bytes.
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
						if (check == target.value) {
							// Found at first byte
							return 0;
						} else {
							// False positive, set top bit to ignore
							topBitCheck |= 0x8000000000000000L;
						}

					} else {

						// Check second byte
						long check = bytes & 0x00ff000000000000L;
						check >>= 48; // 6 x 8 bits
						if (check == target.value) {
							// Found at second byte
							return 1;
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
						if (check == target.value) {
							// Found at third byte
							return 2;
						} else {
							// False positive, set top bit to ignore
							topBitCheck |= 0x0000800000000000L;
						}

					} else {

						// Check fourth byte
						long check = bytes & 0x000000ff00000000L;
						check >>= 32; // 4 x 8 bits
						if (check == target.value) {
							// Found at fourth byte
							return 3;
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
						if (check == target.value) {
							// Found at fifth byte
							return 4;
						} else {
							// False positive, set top bit to ignore
							topBitCheck |= 0x0000000080000000L;
						}

					} else {

						// Check sixth byte
						long check = bytes & 0x0000000000ff0000L;
						check >>= 16; // 2 x 8 bits
						if (check == target.value) {
							// Found at sixth byte
							return 5;
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
						if (check == target.value) {
							// Found at seventh byte
							return 6;
						} else {
							// False positive, set top bit to ignore
							topBitCheck |= 0x0000000000008000L;
						}

					} else {

						// Check eighth byte
						long check = bytes & 0x00000000000000ffL;
						// bytes already in correct place for check
						if (check == target.value) {
							// Found at eighth byte
							return 7;
						} else {
							// False positive, set top bit to ignore
							// Note: should exit loop
							topBitCheck |= 0x0000000000000080L;
						}

					}
				}
			}
		}

		// As here, did not find value in buffer
		return -1;
	}

}