/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.server.stream.BufferJvmFix;
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
		 * @param value Target byte for scan.
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
	 * Start position to read data from for the first previous {@link StreamBuffer}.
	 * All previous {@link StreamBuffer} instances after first are assumed to start
	 * at 0.
	 */
	private int firstPreviousBufferStart = 0;

	/**
	 * Number of bytes in the previous {@link StreamBuffer} instances. This is a
	 * long to enable potentially very large HTTP entities.
	 */
	private long previousBuffersByteCount = 0;

	/**
	 * Current {@link StreamBuffer}.
	 */
	private StreamBuffer<ByteBuffer> currentBuffer = null;

	/**
	 * Starting position within the current {@link StreamBuffer}.
	 */
	private int currentBufferStartPosition = 0;

	/**
	 * Scan starting position with the current {@link StreamBuffer}. This allows the
	 * {@link #currentBufferStartPosition} to only move forward when data obtained.
	 */
	private int currentBufferScanStart = 0;

	/**
	 * Ending position with the current {@link StreamBuffer}.
	 */
	private int currentBufferEndPosition = 0;

	/**
	 * Buffer long (8) bytes.
	 */
	private long bufferLong = 0;

	/**
	 * Number of bytes from previous buffers. Value is always between 0 - 8 (as only
	 * 8 bytes in long).
	 */
	private byte bufferLongByteCount = 0;

	/**
	 * Appends {@link StreamBuffer} for scanning.
	 * 
	 * @param buffer {@link StreamBuffer}.
	 */
	public void appendStreamBuffer(StreamBuffer<ByteBuffer> buffer) {

		// Do nothing if same buffer (just data appended)
		if (buffer == this.currentBuffer) {
			return;
		}

		// Keep track of previous buffer
		if (this.currentBuffer != null) {

			// Determine if first previous buffer (start at position)
			if (this.previousBuffers.size() == 0) {
				// Add the first previous buffer (keep track of start position)
				this.firstPreviousBufferStart = this.currentBufferStartPosition;
				this.previousBuffersByteCount += (BufferJvmFix.position(this.currentBuffer.pooledBuffer)
						- this.firstPreviousBufferStart);
			} else {
				// Add further previous buffer (entire buffer)
				this.previousBuffersByteCount += BufferJvmFix.position(this.currentBuffer.pooledBuffer);
			}

			// Add the current buffer as a previous buffer
			this.previousBuffers.add(this.currentBuffer);
		}

		// Now the current buffer
		this.currentBuffer = buffer;

		// Start at beginning of buffer
		this.currentBufferStartPosition = 0;
		this.currentBufferScanStart = 0;
		this.currentBufferEndPosition = 0;
	}

	/**
	 * Builds a long (8 bytes) from the {@link StreamBuffer} at current position.
	 *
	 * @param <T>                          Illegal value {@link Exception}
	 * @param illegalValueExceptionFactory {@link Supplier} to create
	 *                                     {@link Throwable} for illegal long value.
	 * @return Long value, otherwise <code>-1</code> if not enough bytes in
	 *         {@link StreamBuffer} to build a long.
	 * @throws T If invalid value.
	 */
	public <T extends Throwable> long buildLong(Supplier<T> illegalValueExceptionFactory) throws T {

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.pooledBuffer;
		int remaining = BufferJvmFix.position(data) - this.currentBufferScanStart;

		// Determine if build immediately (typical case)
		if (remaining >= 8) {

			/*
			 * Buffer contains enough data to read in the full long. Therefore determine
			 * whether have data to read in the full long in one go (avoid extra build steps
			 * below, if just built a short or byte). Two scenarios are:
			 * 
			 * 1) No previous build of shorter content, so read in entire long from scan
			 * start
			 * 
			 * 2) Or, have already read in some bytes to buffer, so need to ensure those
			 * bytes are in current buffer for full read
			 */
			if ((this.bufferLongByteCount == 0) || (this.bufferLongByteCount < this.currentBufferScanStart)) {

				// Obtain the bytes directly from buffer
				long returnBytes = data.getLong(this.currentBufferScanStart);

				// Ensure legal value
				if (returnBytes == -1) {
					// Illegal value
					throw illegalValueExceptionFactory.get();
				}

				// Return the short bytes
				return returnBytes;
			}
		}

		// Append to previous bytes
		remaining = Math.min(remaining, 8 - this.bufferLongByteCount);
		this.bufferLongByteCount += remaining; // will be appending bytes
		while (remaining > 0) {
			switch (remaining) {
			case 7:
			case 6:
			case 5:
			case 4:
				// Take 4 bytes and append
				int intBytes = data.getInt(this.currentBufferScanStart);
				this.currentBufferScanStart += 4; // read the 4 bytes
				this.bufferLong <<= 32; // move up 4 bytes
				this.bufferLong += intBytes & 0xffffffff;
				remaining -= 4; // 4 bytes
				break;

			case 3:
			case 2:
				// Take 2 bytes and append
				int shortBytes = data.getShort(this.currentBufferScanStart);
				this.currentBufferScanStart += 2; // read the 2 bytes
				this.bufferLong <<= 16; // move up 2 bytes
				this.bufferLong += shortBytes & 0xffff;
				remaining -= 2; // 2 bytes
				break;

			case 1:
				// Append the single byte
				int singleByte = data.get(this.currentBufferScanStart);
				this.currentBufferScanStart++; // read the 1 byte
				this.bufferLong <<= 8; // move up a byte
				this.bufferLong += singleByte & 0xff;
				remaining = 0; // should always complete on last single byte
				break;
			}
		}

		// Determine if have the long
		if (this.bufferLongByteCount >= 8) {
			// Have all bytes, so ensure legal value
			if (this.bufferLong == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the long bytes
			return this.bufferLong;
		}

		// Not able to build long
		return -1;
	}

	/**
	 * Builds a short (2 bytes) from the {@link StreamBuffer} at current position.
	 *
	 * @param <T>                          Illegal value {@link Exception}
	 * @param illegalValueExceptionFactory {@link Supplier} to create
	 *                                     {@link Throwable} for illegal short
	 *                                     value.
	 * @return Short value, otherwise <code>-1</code> if not enough bytes in
	 *         {@link StreamBuffer} to build a short.
	 * @throws T If invalid value.
	 */
	public <T extends Throwable> short buildShort(Supplier<T> illegalValueExceptionFactory) throws T {

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.pooledBuffer;
		int remaining = BufferJvmFix.position(data) - this.currentBufferScanStart;

		// Determine if build immediately (typical case)
		if ((this.bufferLongByteCount == 0) && (remaining >= 2)) {

			// Obtain the bytes directly from buffer
			short returnBytes = data.getShort(this.currentBufferScanStart);

			// Ensure legal value
			if (returnBytes == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the short bytes
			return returnBytes;
		}

		// Append to previous bytes (could be buffers of 1 byte each)
		remaining = Math.min(remaining, 2 - this.bufferLongByteCount);
		this.bufferLongByteCount += remaining; // will be appending bytes
		if (remaining == 1) {
			// Append the single byte
			// Will only ever be 1 byte appending, otherwise have short
			int singleByte = data.get(this.currentBufferScanStart);
			this.currentBufferScanStart++; // read in byte
			this.bufferLong <<= 8; // move up a byte
			this.bufferLong += singleByte & 0xff;
		}

		// Determine if have the short
		if (this.bufferLongByteCount >= 2) {

			// Obtain the just the first two bytes
			long bytes = this.bufferLong;
			bytes >>= (this.bufferLongByteCount - 2) * 8;

			// Have all bytes, so obtain short
			short returnBytes = (short) (bytes & 0xffff);

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
	 * Builds a byte from the {@link StreamBuffer} at current position.
	 *
	 * @param <T>                          Illegal value {@link Exception}
	 * @param illegalValueExceptionFactory {@link Supplier} to create
	 *                                     {@link Throwable} for illegal short
	 *                                     value.
	 * @return Byte value, otherwise <code>-1</code> if not enough bytes in
	 *         {@link StreamBuffer} to build a byte.
	 * @throws T If invalid value.
	 */
	public <T extends Throwable> byte buildByte(Supplier<T> illegalValueExceptionFactory) throws T {

		// Determine if remaining content for byte in current buffer
		ByteBuffer data = this.currentBuffer.pooledBuffer;
		int remaining = BufferJvmFix.position(data) - this.currentBufferScanStart;

		// Determine if build immediately (typical case)
		if ((this.bufferLongByteCount == 0) && (remaining >= 1)) {

			// Obtain the bytes directly from buffer
			byte returnBytes = data.get(this.currentBufferScanStart);

			// Ensure legal value
			if (returnBytes == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the short bytes
			return returnBytes;
		}

		// Determine if have byte
		if (this.bufferLongByteCount >= 1) {

			// Obtain the just the first byte
			long bytes = this.bufferLong;
			bytes >>= (this.bufferLongByteCount - 1) * 8;

			// Have all bytes, so obtain byte
			byte returnByte = (byte) (bytes & 0xff);

			// Ensure legal value
			if (returnByte == -1) {
				// Illegal value
				throw illegalValueExceptionFactory.get();
			}

			// Return the byte
			return returnByte;
		}

		// As here, not enough bytes to build byte
		return -1;
	}

	/**
	 * Peeks in the current {@link StreamBuffer} to attempt to find the
	 * {@link ScanTarget}.
	 * 
	 * @param target {@link ScanTarget}.
	 * @return Position of the {@link ScanTarget} within the current
	 *         {@link StreamBuffer} (starting at current position). Otherwise,
	 *         <code>-1</code> to indicate did not find {@link ScanTarget} in
	 *         current {@link StreamBuffer}.
	 */
	public int peekToTarget(ScanTarget target) {

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.pooledBuffer;
		int lastPosition = BufferJvmFix.position(data);

		// Obtain current position in the buffer to start scanning
		int position = this.currentBufferStartPosition;

		// Keep reading until end of current buffer
		int lastFullReadPosition = lastPosition - 7;
		while (position < lastFullReadPosition) {

			// Read in the long (8 bytes)
			long bytes = data.getLong(position);

			// Determine if byte in long
			int index = indexOf(bytes, target);
			if (index != -1) {
				// Found the byte (offset of current position)
				return position + index - this.currentBufferStartPosition;
			}

			// Not in long, so try next long (8 bytes)
			position += 8;
		}

		// Attempt to find in possible remaining bytes
		while (position < BufferJvmFix.position(data)) {

			// At worse, 7 bytes so just check each
			byte value = data.get(position);
			if (value == target.value) {
				// Found the byte (offset of current position)
				return position - this.currentBufferStartPosition;
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
	 * @param numberOfBytes Number of bytes to scan.
	 * @return {@link StreamBufferByteSequence} to the scanned bytes.
	 */
	public StreamBufferByteSequence scanBytes(long numberOfBytes) {

		// Determine remaining bytes required
		long requiredBytes = numberOfBytes - this.previousBuffersByteCount;
		if (requiredBytes < 0) {
			// Data from previous buffers
			return this.createByteSequence((int) numberOfBytes);
		}

		// Determine number of available bytes in current buffer
		ByteBuffer data = this.currentBuffer.pooledBuffer;
		int availableBytes = BufferJvmFix.position(data) - this.currentBufferStartPosition;

		// Determine if have bytes to complete scan
		if (requiredBytes <= availableBytes) {

			// Have all bytes so create byte sequence
			// (Buffer size should always be less than max int)
			this.currentBufferEndPosition = (int) (this.currentBufferStartPosition + requiredBytes);
			return this.createByteSequence();
		}

		// Not enough bytes
		return null;
	}

	/**
	 * <p>
	 * Scans the {@link StreamBuffer} for the byte value from the current position.
	 * <p>
	 * The returned {@link StreamBufferByteSequence} is inclusive of the current
	 * position but exclusive of the target byte. This is because target bytes for
	 * HTTP are typically delimiters (eg. space, CR, etc) that are not included in
	 * the bytes of interest.
	 * 
	 * @param <T>                     Too long {@link Exception} type.
	 * @param target                  {@link ScanTarget}.
	 * @param maxBytesLength          Max bytes.
	 * @param tooLongExceptionFactory {@link Supplier} to provide too long
	 *                                {@link Exception}.
	 * @return {@link StreamBufferByteSequence} if found the byte. Otherwise,
	 *         <code>null</code> indicating further {@link StreamBuffer} instances
	 *         may contain the byte.
	 * @throws T If too long {@link Exception}.
	 */
	public <T extends Throwable> StreamBufferByteSequence scanToTarget(ScanTarget target, int maxBytesLength,
			Supplier<T> tooLongExceptionFactory) throws T {

		// Determine if previous bytes
		if (this.bufferLongByteCount != 0) {

			// Previous bytes from short/long, so use first
			byte previousBytesIndex = indexOf(this.bufferLong, target);
			if (previousBytesIndex != -1) {
				// Obtain number of bytes (may not be full, as possibly short)
				int numberOfPreviousBytes = previousBytesIndex - (8 - this.bufferLongByteCount);

				// Return content to index
				return this.createByteSequence(numberOfPreviousBytes);
			}

			// As here, did not find in buffer (so clear)
			this.bufferLong = 0;
			this.bufferLongByteCount = 0;
		}

		// Determine if remaining content for long in current buffer
		ByteBuffer data = this.currentBuffer.pooledBuffer;

		// Keep reading until end of current buffer
		int lastFullReadPosition = BufferJvmFix.position(data) - 7;
		while (this.currentBufferScanStart < lastFullReadPosition) {

			// Read in the long (8 bytes)
			long bytes = data.getLong(this.currentBufferScanStart);

			// Determine if byte in long
			int index = indexOf(bytes, target);
			if (index != -1) {
				// Found the byte
				this.currentBufferEndPosition = (this.currentBufferScanStart + index);
				return this.createByteSequence();
			}

			// Not in long, so try next long (8 bytes)
			this.currentBufferScanStart += 8;
		}

		// Attempt to find in possible remaining bytes
		while (this.currentBufferScanStart < BufferJvmFix.position(data)) {

			// At worse, 7 bytes so just check each
			byte value = data.get(this.currentBufferScanStart);
			if (value == target.value) {
				// Found the byte
				this.currentBufferEndPosition = this.currentBufferScanStart;
				return this.createByteSequence();
			}

			// Attempt at next position
			this.currentBufferScanStart++;
		}

		// Add to previous bytes (and error if too long)
		long scannedBytes = (this.currentBufferScanStart - this.currentBufferStartPosition)
				+ this.previousBuffersByteCount;
		if (scannedBytes >= maxBytesLength) {
			throw tooLongExceptionFactory.get();
		}

		// As here, did not find byte in the buffer
		return null;
	}

	/**
	 * Skips forward the particular number of bytes in the current
	 * {@link StreamBuffer}.
	 * 
	 * @param numberOfBytes Number of bytes to skip.
	 */
	public void skipBytes(int numberOfBytes) {

		// Keep track of original bytes skipped
		int numberOfBytesSkipped = numberOfBytes;

		// Remove possible previous buffer bytes
		this.removeBufferLongBytes(numberOfBytes);

		// Consume number of bytes
		if (this.previousBuffers.size() == 0) {

			// Only current buffer
			this.currentBufferStartPosition += numberOfBytes;
			this.currentBufferScanStart = this.currentBufferStartPosition;

		} else {
			// Have past buffers, so include data from them
			StreamBuffer<ByteBuffer> firstBuffer = this.previousBuffers.get(0);
			int length = BufferJvmFix.position(firstBuffer.pooledBuffer) - this.firstPreviousBufferStart;

			// Determine if all data on first previous buffer
			if (numberOfBytes <= length) {
				// All content from first buffer (adjust for remaining)
				this.firstPreviousBufferStart += numberOfBytes;
				return;
			}

			// Consumes all of first buffer and requires further data
			numberOfBytes -= length;

			// Create iterator and remove the first buffer (as already included)
			Iterator<StreamBuffer<ByteBuffer>> iterator = this.previousBuffers.iterator();
			iterator.next(); // move to first buffer
			iterator.remove(); // remove first buffer

			// Load in remaining data
			while (iterator.hasNext()) {

				// Obtain next buffer
				StreamBuffer<ByteBuffer> buffer = iterator.next();

				// Obtain the number of bytes in next buffer
				length = BufferJvmFix.position(buffer.pooledBuffer);

				// Determine if buffer completes the required data
				if (numberOfBytes <= length) {
					// Set start to appropriate position
					this.firstPreviousBufferStart = numberOfBytes;
					this.previousBuffersByteCount -= numberOfBytesSkipped;
					return;
				}

				// Skip the entire buffer
				iterator.remove();

				// Obtain remaining bytes
				numberOfBytes -= length;
			}

			// All previous buffers removed
			this.firstPreviousBufferStart = 0;
			this.previousBuffersByteCount = 0;

			// Set position for next scan
			this.currentBufferStartPosition = numberOfBytes;
			this.currentBufferScanStart = this.currentBufferStartPosition;
		}
	}

	/**
	 * Removes past bytes from the long buffer.
	 * 
	 * @param numberOfBytes Number of bytes to remove. Value is absolute and does
	 *                      not take into account number of bytes in the buffer.
	 */
	private void removeBufferLongBytes(int numberOfBytes) {

		// Remove possible previous buffer bytes
		if (this.bufferLongByteCount != 0) {
			int pastBufferBytes;
			if (numberOfBytes > this.bufferLongByteCount) {
				// Remove all bytes in long buffer
				pastBufferBytes = 8;
			} else {
				// Obtain index of remaining byte
				pastBufferBytes = 8 - (this.bufferLongByteCount - numberOfBytes);
			}

			// Remove previous bytes (as now consumed)
			switch (pastBufferBytes) {
			case 8:
				// Remove all bytes
				this.bufferLong = 0;
				this.bufferLongByteCount = 0;
				break;
			case 7:
				// Leave only last byte
				this.bufferLong &= 0xffL;
				this.bufferLongByteCount = 1;
				break;
			case 6:
				// Leave two bytes
				this.bufferLong &= 0xffffL;
				this.bufferLongByteCount = 2;
				break;
			case 5:
				// Leave 3 bytes
				this.bufferLong &= 0xffffffL;
				this.bufferLongByteCount = 3;
				break;
			case 4:
				// Leave 4 bytes
				this.bufferLong &= 0xffffffffL;
				this.bufferLongByteCount = 4;
				break;
			case 3:
				// Leave 5 bytes
				this.bufferLong &= 0xffffffffffL;
				this.bufferLongByteCount = 5;
				break;
			case 2:
				// Leave 6 bytes
				this.bufferLong &= 0xffffffffffffL;
				this.bufferLongByteCount = 6;
				break;
			case 1:
				// Leave 7 bytes
				this.bufferLong &= 0xffffffffffffffL;
				this.bufferLongByteCount = 7;
				break;
			// 0 leave all bytes
			}
		}
	}

	/**
	 * <p>
	 * Creates the {@link StreamBufferByteSequence} up to current position.
	 * <p>
	 * This will also reset for obtaining the next {@link StreamBufferByteSequence}.
	 * 
	 * @return {@link StreamBufferByteSequence} up to current position.
	 */
	private StreamBufferByteSequence createByteSequence() {

		// Create the sequence
		StreamBufferByteSequence sequence;
		if (this.previousBuffers.size() == 0) {

			// Just current buffer
			int length = this.currentBufferEndPosition - this.currentBufferStartPosition;
			sequence = new StreamBufferByteSequence(this.currentBuffer, this.currentBufferStartPosition, length);

		} else {

			// Previous buffers
			StreamBuffer<ByteBuffer> firstBuffer = this.previousBuffers.get(0);
			int length = BufferJvmFix.position(firstBuffer.pooledBuffer) - this.firstPreviousBufferStart;
			sequence = new StreamBufferByteSequence(firstBuffer, this.firstPreviousBufferStart, length);

			// Add in remaining previous buffers
			for (int i = 1; i < this.previousBuffers.size(); i++) {
				StreamBuffer<ByteBuffer> nextBuffer = this.previousBuffers.get(i);
				length = BufferJvmFix.position(nextBuffer.pooledBuffer);
				sequence.appendStreamBuffer(nextBuffer, 0, length);
			}

			// Add in the current buffer
			length = this.currentBufferEndPosition;
			sequence.appendStreamBuffer(this.currentBuffer, 0, length);

			// Reset previous buffers, as now in byte sequence
			this.firstPreviousBufferStart = 0;
			this.previousBuffersByteCount = 0;
			this.previousBuffers.clear();
		}

		// Remove long buffer data
		this.removeBufferLongBytes(sequence.length());

		// Set to current position to continue
		this.currentBufferStartPosition = this.currentBufferEndPosition;
		this.currentBufferScanStart = this.currentBufferStartPosition;

		// Return the sequence
		return sequence;
	}

	/**
	 * <p>
	 * Creates a {@link StreamBufferByteSequence} from current position to the
	 * specified number of bytes.
	 * <p>
	 * This will also reset to after the number of bytes.
	 * 
	 * @param numberOfBytes Number of bytes for the
	 *                      {@link StreamBufferByteSequence}.
	 * @return {@link StreamBufferByteSequence} with the specified number of bytes.
	 */
	private StreamBufferByteSequence createByteSequence(int numberOfBytes) {

		// Keep track of original bytes skipped
		int numberOfBytesSkipped = numberOfBytes;

		// Remove possible previous buffer bytes
		this.removeBufferLongBytes(numberOfBytes);

		// Create the byte sequence
		StreamBufferByteSequence sequence;
		if (this.previousBuffers.size() == 0) {

			// Only current buffer
			sequence = new StreamBufferByteSequence(this.currentBuffer, this.currentBufferStartPosition, numberOfBytes);
			this.currentBufferStartPosition += numberOfBytes;
			this.currentBufferScanStart = this.currentBufferStartPosition;

		} else {
			// Have past buffers, so include data from them
			StreamBuffer<ByteBuffer> firstBuffer = this.previousBuffers.get(0);
			int length = BufferJvmFix.position(firstBuffer.pooledBuffer) - this.firstPreviousBufferStart;

			// Determine if all data on first previous buffer
			if (numberOfBytes <= length) {
				// All content from first buffer (adjust for remaining)
				sequence = new StreamBufferByteSequence(firstBuffer, this.firstPreviousBufferStart, numberOfBytes);
				this.firstPreviousBufferStart += numberOfBytes;
				this.previousBuffersByteCount -= numberOfBytesSkipped;
				return sequence;
			}

			// Consumes all of first buffer and requires further data
			sequence = new StreamBufferByteSequence(firstBuffer, this.firstPreviousBufferStart, length);
			numberOfBytes -= length;

			// Create iterator and remove the first buffer (as already included)
			Iterator<StreamBuffer<ByteBuffer>> iterator = this.previousBuffers.iterator();
			iterator.next(); // move to first buffer
			iterator.remove(); // remove first buffer

			// Load in remaining data
			while (iterator.hasNext()) {

				// Obtain next buffer
				StreamBuffer<ByteBuffer> buffer = iterator.next();

				// Obtain the number of bytes in next buffer
				length = BufferJvmFix.position(buffer.pooledBuffer);

				// Determine if buffer completes the required data
				if (numberOfBytes <= length) {
					// Add the sequence (with appropriate bytes)
					sequence.appendStreamBuffer(buffer, 0, numberOfBytes);

					// Set start to appropriate position
					this.firstPreviousBufferStart = numberOfBytes;
					this.previousBuffersByteCount -= numberOfBytesSkipped;

					// Return the completed sequence
					return sequence;
				}

				// Include the entire buffer (and remove as included)
				sequence.appendStreamBuffer(buffer, 0, length);
				iterator.remove();

				// Obtain remaining bytes
				numberOfBytes -= length;
			}

			// All previous buffers removed
			this.firstPreviousBufferStart = 0;
			this.previousBuffersByteCount = 0;

			// As here, require data from current buffer
			sequence.appendStreamBuffer(this.currentBuffer, 0, numberOfBytes);

			// Set position for next scan
			this.currentBufferStartPosition = numberOfBytes;
			this.currentBufferScanStart = this.currentBufferStartPosition;
		}

		// Return the sequence
		return sequence;
	}

	/**
	 * <p>
	 * Scans the {@link ByteBuffer} to find the next byte of particular value.
	 * <p>
	 * Note: the algorithm used does not handle negative byte values. However, the
	 * values (ASCII characters) searched for in HTTP parsing are all positive
	 * (lower 7 bits of the byte).
	 * 
	 * @param bytes  Long of 8 bytes to check for the value.
	 * @param target {@link ScanTarget} for the byte to find the index.
	 * @return Index within the long of the first byte value. Otherwise,
	 *         <code>-1</code> if does not find the byte.
	 */
	public static final byte indexOf(long bytes, ScanTarget target) {

		// Determine if the bytes contain the value
		long xorWithMask = bytes ^ target.mask; // matching byte will be 0

		/*
		 * Need to find if any byte is zero. As ASCII is 7 bits, can use the top bit for
		 * overflow to find if any byte is zero. In other words, add 0x7f (01111111) to
		 * each byte and if top bit is 1, then not the byte of interest (as byte of
		 * interest is 0 from previous XOR).
		 * 
		 * Note: top bit value of 0 may only mean potential matching byte, as could have
		 * UTF-8 encoded characters. However, the separation characters for HTTP parsing
		 * are all 7 bit values. Therefore, will need to check for false positives (e.g.
		 * null 0). However, most HTTP content is 7 bits so win efficiency in scanning
		 * past bytes not of interest (i.e. can potentially skip past 8 bytes in 4
		 * operations - where looping over the 8 bytes is 8 operations minimum).
		 */
		long overflowNonZero = xorWithMask + 0x7f7f7f7f7f7f7f7fL;
		long topBitCheck = overflowNonZero & 0x8080808080808080L;
		while (topBitCheck != 0x8080808080808080L) {

			/*
			 * Potential value within the 8 bytes, so search for it.
			 * 
			 * To reduce operations, use binary search to determine where byte of interest
			 * is.
			 * 
			 * Note: as may have two bytes of interest within the range, need to find the
			 * first actual matching byte. Example case is UTF-8 creating false positives.
			 * However, also HTTP with end of HTTP headers having repeating CR LF bytes.
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
