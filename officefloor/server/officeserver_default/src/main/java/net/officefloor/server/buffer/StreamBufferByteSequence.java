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
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.function.Function;
import java.util.function.Supplier;

import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link ByteSequence} reading from {@link StreamBuffer} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class StreamBufferByteSequence implements ByteSequence, CharSequence {

	/**
	 * Maximum length of bytes (and by this the maximum number of characters).
	 */
	private static int maxByteLength = 4096;

	/**
	 * Specifies the maximum byte length of any {@link ByteSequence}.
	 * 
	 * @param length
	 *            Maximum byte length of any {@link ByteSequence}.
	 */
	public static void setMaxByteLength(int length) {
		maxByteLength = length;
	}

	/**
	 * <p>
	 * {@link ThreadLocal} {@link CharBuffer} to re-use to reduce memory
	 * creation (and garbage collection).
	 * <p>
	 * Note: {@link #setMaxByteLength(int)} needs to be set before using
	 * {@link StreamBufferByteSequence} instances to ensure the
	 * {@link CharBuffer} is appropriately sized for purpose.
	 */
	private static final ThreadLocal<ThreadLocalState> threadLocalState = new ThreadLocal<ThreadLocalState>() {
		@Override
		protected ThreadLocalState initialValue() {
			return new ThreadLocalState(maxByteLength);
		}
	};

	/**
	 * {@link ThreadLocal} state.
	 */
	private static class ThreadLocalState {

		/**
		 * Re-useable {@link CharBuffer} for reduced memory.
		 */
		private final CharBuffer charBuffer;

		/**
		 * 
		 */
		private final CharsetDecoder uriDecoder = ServerHttpConnection.URI_CHARSET.newDecoder();

		/**
		 * Instantiate.
		 * 
		 * @param charBufferLength
		 *            Length of the {@link CharBuffer}.
		 */
		private ThreadLocalState(int charBufferLength) {
			charBuffer = CharBuffer.allocate(charBufferLength);
		}
	}

	/**
	 * Obtains a {@link CharBuffer} ensuring it fits the max character length.
	 * 
	 * @param maxCharLength
	 *            Maximum number of characters to fit.
	 * @return {@link CharBuffer}.
	 */
	private static CharBuffer getCharBuffer(int maxCharLength) {

		// Obtain the char buffer
		ThreadLocalState state = threadLocalState.get();
		if (state.charBuffer.capacity() < maxCharLength) {
			// Need to use larger char buffer
			state = new ThreadLocalState(maxCharLength);
			threadLocalState.set(state);

		} else {
			// Use the char buffer, but clear ready for use
			state.charBuffer.clear();
		}

		// Return the char buffer
		return state.charBuffer;
	}

	/**
	 * Obtains the URI {@link CharsetDecoder}.
	 * 
	 * @return URI {@link CharsetDecoder}.
	 */
	private static CharsetDecoder getUriDecoder() {
		CharsetDecoder decoder = threadLocalState.get().uriDecoder;
		decoder.reset();
		return decoder;
	}

	/**
	 * HTTP space (' ') character.
	 */
	private static final byte HTTP_SPACE = " ".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP tab ('\t') character.
	 */
	private static final byte HTTP_TAB = "\t".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP quote ('"') character.
	 */
	private static final byte HTTP_QUOTE = "\"".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP % character.
	 */
	private static final byte HTTP_PERCENTAGE = "%".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP + character.
	 */
	private static final byte HTTP_PLUS = "+".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * Head {@link StreamSegment}.
	 */
	private StreamSegment head = null;

	/**
	 * Tail {@link StreamSegment}.
	 */
	private StreamSegment tail = null;

	/**
	 * Length of all {@link StreamSegment} instances.
	 */
	private int sequenceLength = 0;

	/**
	 * <p>
	 * Current position within this {@link ByteSequence}.
	 * <p>
	 * Allows for more efficient forward scanning through the
	 * {@link ByteSequence}.
	 */
	private int currentPosition = 0;

	/**
	 * Position within the current {@link StreamSegment}.
	 */
	private int currentSegmentPosition = 0;

	/**
	 * <p>
	 * Current {@link StreamSegment}.
	 * <p>
	 * Works with the {@link #currentPosition} for more efficient forward
	 * scanning through the {@link ByteSequence}.
	 */
	private StreamSegment currentSegment;

	/**
	 * Instantiate with the first {@link StreamBuffer}.
	 * 
	 * @param buffer
	 *            First {@link StreamBuffer} of this {@link ByteSequence}.
	 * @param offset
	 *            Offset into the {@link StreamBuffer} to read data.
	 * @param length
	 *            Length of data from the {@link StreamBuffer}.
	 */
	public StreamBufferByteSequence(StreamBuffer<ByteBuffer> buffer, int offset, int length) {
		// Load the state (allow first buffer to be empty)
		this.head = new StreamSegment(buffer, offset, length);
		this.tail = this.head;
		this.sequenceLength = length;
		this.currentSegment = this.head;
	}

	/**
	 * Appends a {@link StreamBuffer} to this {@link ByteSequence}.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer} to append to this {@link ByteSequence}.
	 * @param offset
	 *            Offset into the {@link StreamBuffer} to read data.
	 * @param length
	 *            Length of data from the {@link StreamBuffer}.
	 */
	public void appendStreamBuffer(StreamBuffer<ByteBuffer> buffer, int offset, int length) {

		// Ignore empty buffers
		if (length == 0) {
			return;
		}

		// Append segment
		this.tail.next = new StreamSegment(buffer, offset, length);
		this.tail = this.tail.next;
		this.sequenceLength += length;
	}

	/**
	 * Trims this {@link ByteSequence} of outer HTTP spaces and tabs.
	 * 
	 * @return <code>this</code>.
	 */
	public StreamBufferByteSequence trim() {

		// Trim the left side
		StreamSegment segment = this.head;
		int segmentTrimOffset = 0;
		byte character = segment.buffer.getPooledBuffer().get(segment.offset + segmentTrimOffset);
		while ((character == HTTP_SPACE) || (character == HTTP_TAB)) {

			// Space so trim off the content
			this.sequenceLength--;
			segmentTrimOffset++;

			// Determine if trimmed out buffer
			if (segmentTrimOffset >= segment.length) {
				// Trim off the buffer
				this.head = this.head.next;
				segment = this.head;
				segmentTrimOffset = 0;
				if (segment == null) {
					return this; // all content trimmed
				}
			}

			// Obtain the next charcter
			character = segment.buffer.getPooledBuffer().get(segment.offset + segmentTrimOffset);
		}
		segment.offset += segmentTrimOffset;
		segment.length -= segmentTrimOffset;

		// Reset current position to start
		this.currentSegment = this.head;
		this.currentSegmentPosition = 0;
		this.currentPosition = 0;

		// Trim the right side
		segment = this.tail;
		int segmentTrimLength = segment.length;
		character = segment.buffer.getPooledBuffer().get(segment.offset + segmentTrimLength - 1);
		while ((character == HTTP_SPACE) || (character == HTTP_TAB)) {

			// Space so trim off the content
			this.sequenceLength--;
			segmentTrimLength--;

			// Determine if trimmed out buffer
			if (segmentTrimLength <= 0) {
				// Trim off the buffer

				// Obtain the previous segment
				// (Note: this should happen rarely enough that scan through is
				// better than additional memory pointer)
				StreamSegment prev = this.head;
				while (prev.next != this.tail) {
					prev = prev.next;
				}

				// Discard the last segment
				this.tail = prev;
				prev.next = null;
				segment = prev;
				segmentTrimLength = segment.length;
			}

			// Obtain the next character
			character = segment.buffer.getPooledBuffer().get(segment.offset + segmentTrimLength - 1);
		}
		segment.length = segmentTrimLength;

		// Return this
		return this;
	}

	/**
	 * Removes surrounding HTTP quotes.
	 * 
	 * @return <code>this</code>.
	 */
	public <T extends Throwable> StreamBufferByteSequence removeQuotes(Supplier<T> invalidValueExceptionFactory)
			throws T {

		// Determine if starts with quote
		byte character = this.head.buffer.getPooledBuffer().get(this.head.offset);
		if (character == HTTP_QUOTE) {

			// Determine if last character is quote
			character = this.tail.buffer.getPooledBuffer().get(this.tail.offset + this.tail.length - 1);
			if (character != HTTP_QUOTE) {
				throw invalidValueExceptionFactory.get();
			}

			// Quoted value, so remove quotes
			this.head.length--;
			if (this.head.length <= 0) {
				// Discard the head
				this.head = this.head.next;
			} else {
				// Same segment, so discard character
				this.head.offset++;
			}

			// Decrement the length of last buffer for quote character
			this.tail.length--;

			// Decrement the length for removed quotes
			this.sequenceLength -= 2;

			// Reset to current position to start
			this.currentSegment = this.head;
			this.currentSegmentPosition = 0;
			this.currentPosition = 0;
		}

		// Return this
		return this;
	}

	/**
	 * <p>
	 * Obtains this {@link ByteSequence} as a {@link CharSequence} for HTTP
	 * {@link Character} values.
	 * <p>
	 * This is for use with {@link NonMaterialisedHttpHeader} comparing on the
	 * name.
	 * 
	 * @return {@link CharSequence} for HTTP {@link Character} values of this
	 *         {@link ByteSequence}.
	 */
	public CharSequence getHttpCharSequence() {
		return this;
	}

	/**
	 * Obtains this {@link ByteSequence} decoded to a HTTP {@link String}.
	 * 
	 * @return {@link ByteSequence} decoded to a HTTP {@link String}.
	 */
	public String toHttpString() {

		// Obtain the HTTP sequence
		CharSequence httpSequence = this.getHttpCharSequence();
		int charLength = httpSequence.length();

		// Obtain the char buffer (ready for use)
		CharBuffer temp = getCharBuffer(charLength);

		// Add content to temp buffer
		for (int i = 0; i < charLength; i++) {
			temp.put(httpSequence.charAt(i));
		}

		// Return the string value
		temp.flip();
		return temp.toString();
	}

	/**
	 * Decode URI states
	 */
	private static enum DecodeState {
		NO_ESCAPE, AWAITING_HI_BITS, AWAITING_LOW_BITS
	}

	/**
	 * Decodes the URI.
	 * 
	 * @param invalidDecodeExceptionFactory
	 *            {@link Function} to create an invalid encoding
	 *            {@link Throwable}.
	 * @return <code>this</code>.
	 * @throws T
	 *             If invalid encoding.
	 */
	public <T extends Throwable> StreamBufferByteSequence decodeUri(Function<String, T> invalidDecodeExceptionFactory)
			throws T {

		// Decode the % encoding
		StreamSegment readSegment = this.head;
		StreamSegment writeSegment = this.head;
		int writeSegmentPosition = 0;

		// Decode details
		DecodeState state = DecodeState.NO_ESCAPE;
		byte hiBits = 0;
		byte lowBits = 0;
		int decodeCount = 0;

		// Loop decoding the content
		while (readSegment != null) {

			// Read the values for the segment
			for (int i = 0; i < readSegment.length; i++) {
				byte readByte = readSegment.buffer.getPooledBuffer().get(readSegment.offset + i);

				// Handle writing content (based on state)
				boolean isWriteByte = true;
				switch (state) {
				case NO_ESCAPE:
					// Determine if escaping
					if (readByte == HTTP_PERCENTAGE) {
						// Encoded byte
						decodeCount++;
						state = DecodeState.AWAITING_HI_BITS;
						isWriteByte = false;

					} else if (readByte == HTTP_PLUS) {
						// Space character ('+' = ' ')
						readByte = HTTP_SPACE;
					}
					break;

				case AWAITING_HI_BITS:
					hiBits = readByte;
					state = DecodeState.AWAITING_LOW_BITS;
					isWriteByte = false;
					break;

				case AWAITING_LOW_BITS:
					// Decode the encoded byte
					lowBits = decodeUriByte(readByte, invalidDecodeExceptionFactory);
					readByte = decodeUriByte(hiBits, invalidDecodeExceptionFactory);
					readByte <<= 4; // move low bits to hi bits
					readByte |= lowBits; // add low bits
					state = DecodeState.NO_ESCAPE;
				}

				// Determine if write the byte
				if (isWriteByte) {
					// Write out the unencoded byte
					if (writeSegmentPosition >= writeSegment.length) {
						// Move to next buffer (as filled current buffer)
						writeSegment = writeSegment.next;
						writeSegmentPosition = 0;
					}
					writeSegment.buffer.getPooledBuffer().put(writeSegment.offset + writeSegmentPosition, readByte);
					writeSegmentPosition++;
				}
			}

			// Obtain the next segment
			readSegment = readSegment.next;
		}

		// Determine if incomplete encoding
		if (state != DecodeState.NO_ESCAPE) {
			throw invalidDecodeExceptionFactory.apply("Incomplete encoding");
		}

		// Remove the remaining content
		this.sequenceLength -= (decodeCount * 2); // %XX (3) => Y (1)
		writeSegment.length = writeSegmentPosition;
		writeSegment.next = null;
		this.tail = writeSegment;

		// Reset current position
		this.currentSegment = this.head;
		this.currentSegmentPosition = 0;
		this.currentPosition = 0;

		// Return this
		return this;
	}

	/**
	 * Decodes the byte to its 4 bit value.
	 * 
	 * @param value
	 *            Value to be decoded.
	 * @param invalidDecodeExceptionFactory
	 *            {@link Function} to generate the {@link Throwable} should be
	 *            invalid encoding.
	 * @return Encoded 4 bits in the low bits of the return byte.
	 * @throws T
	 *             If invalid encoding.
	 */
	private static <T extends Throwable> byte decodeUriByte(byte value,
			Function<String, T> invalidDecodeExceptionFactory) throws T {
		byte hi = (byte) (value & 0xf0);
		byte low = (byte) (value & 0x0f);
		switch (hi) {
		case 0x30: // '0'
			// Digits (low value correct)
			if (low > 10) {
				throw invalidDecodeExceptionFactory
						.apply("Invalid encoded character " + Character.toString((char) value));
			}
			return low;

		case 0x40: // 'A'
			// Capital letter (A + 10)
			if ((low == 0) || (low > 6)) { // '@' or 'F'
				throw invalidDecodeExceptionFactory
						.apply("Invalid encoded character " + Character.toString((char) value));
			}
			return (byte) (low + 9); // (A==1) + 9 = 10

		case 0x60: // 'a'
			if ((low == 0) || (low > 6)) { // '@' or 'f'
				throw invalidDecodeExceptionFactory
						.apply("Invalid encoded character " + Character.toString((char) value));
			}
			return (byte) (low + 9); // (a==1) + 9 = 10

		default:
			throw invalidDecodeExceptionFactory.apply("Invalid encoded character " + Character.toString((char) value));
		}
	}

	/**
	 * Obtains this {@link ByteSequence} decoded to a URI {@link String}.
	 * 
	 * @param invalidValueExceptionFactory
	 *            Factory to create the {@link Exception} should the
	 *            {@link ByteSequence} not be valid for the URI.
	 * @return {@link ByteSequence} decoded to a URI {@link String}.
	 * @throws T
	 *             If invalid {@link ByteSequence} for the URI.
	 */
	public <T extends Throwable> String toUriString(Function<CoderResult, T> invalidValueExceptionFactory) throws T {

		// Obtain the URI decoder
		CharsetDecoder uriDecorder = getUriDecoder();

		// Return the string
		return this.toString(uriDecorder, invalidValueExceptionFactory);
	}

	/**
	 * Obtains this {@link ByteSequence} as a {@link String} for the particular
	 * {@link Charset}.
	 * 
	 * @param charset
	 *            {@link Charset} for the decoding the {@link StreamBuffer}
	 *            data.
	 * @param invalidValueExceptionFactory
	 *            Factory to create the {@link Exception} should the
	 *            {@link ByteSequence} not be valid for the {@link Charset}.
	 * @return {@link String} value for the {@link StreamBuffer} values.
	 * @throws T
	 *             If invalid {@link ByteSequence} for {@link Charset}.
	 */
	public <T extends Throwable> String toString(Charset charset, Function<CoderResult, T> invalidValueExceptionFactory)
			throws T {

		// Create the decoder for the charset
		CharsetDecoder decoder = charset.newDecoder();

		// Return the string
		return this.toString(decoder, invalidValueExceptionFactory);
	}

	/**
	 * Obtains this {@link ByteSequence} as a {@link String} via the
	 * {@link CharsetDecoder}.
	 * 
	 * @param decoder
	 *            {@link CharsetDecoder}.
	 * @param invalidValueExceptionFactory
	 *            Factory to create the {@link Exception} should the
	 *            {@link ByteSequence} not be valid for the {@link Charset}.
	 * @return {@link String} value for the {@link StreamBuffer} values.
	 * @throws T
	 *             If invalid {@link ByteSequence} for {@link Charset}.
	 */
	private <T extends Throwable> String toString(CharsetDecoder decoder,
			Function<CoderResult, T> invalidValueExceptionFactory) throws T {

		// Determine worst case char buffer size
		int maxCharLength = (int) Math.ceil(this.sequenceLength * decoder.maxCharsPerByte());

		// Obtain the char buffer (ready for use)
		CharBuffer temp = getCharBuffer(maxCharLength);

		// Decode the content into the char buffer
		StreamSegment segment = this.head;
		while (segment != null) {

			// Slice up buffer to content
			ByteBuffer input = segment.buffer.getPooledBuffer().duplicate();
			input.position(segment.offset);
			input.limit(segment.offset + segment.length);

			// Move to next segment (to determine if last input)
			segment = segment.next;

			// Decode the content into the char buffer
			CoderResult result = decoder.decode(input, temp, (segment == null));
			if (result.isError()) {
				throw invalidValueExceptionFactory.apply(result);
			}
		}

		// Flip to get content just decoded
		temp.flip();

		// Return the string value
		return temp.toString();
	}

	/**
	 * Obtains this {@link ByteSequence} as a <code>long</code> value.
	 * 
	 * @param invalidDigitExceptionFactory
	 *            {@link Function} to create an {@link Throwable} should there
	 *            be an invalid HTTP digit.
	 * @return <code>long</code> value.
	 * @throws T
	 *             If invalid value to convert to long.
	 */
	public <T extends Throwable> long toLong(Function<Character, T> invalidDigitExceptionFactory) throws T {

		// Obtain the HTTP sequence
		CharSequence httpSequence = this.getHttpCharSequence();
		int charLength = httpSequence.length();

		// Add bytes to create long
		long value = 0;
		for (int i = 0; i < charLength; i++) {

			// Obtain the character
			char character = httpSequence.charAt(i);

			// Translate character to digit value
			char digit = (char) (character - '0');
			if ((digit < 0) || (digit > 9)) {
				throw invalidDigitExceptionFactory.apply(digit);
			}

			// Add the digit to the value
			value *= 10;
			value += digit;
		}

		// Return the long value
		return value;
	}

	/*
	 * =================== ByteSequence ======================
	 */

	@Override
	public byte byteAt(int index) {

		// Ensure within range
		if ((index < 0) || (index > this.sequenceLength)) {
			throw new ArrayIndexOutOfBoundsException("Asking for byte " + index + " of "
					+ ByteSequence.class.getSimpleName() + " with length " + this.sequenceLength + " bytes");
		}

		// Determine if asking for byte previous to current position
		if (this.currentPosition > index) {
			// Reset to start of sequence (for skipping to position)
			this.currentSegment = this.head;
			this.currentSegmentPosition = 0;
			this.currentPosition = 0;
		}

		// Determine if asking for byte after current position
		if (this.currentPosition < index) {

			// Determine the number of bytes to skip
			int skipLength = index - this.currentPosition;

			// Increment current position
			this.currentPosition += skipLength;

			// Skip forward the required number of bytes
			while (skipLength > 0) {

				// Determine remaining from current segment
				int currentSegementRemaining = this.currentSegment.length - this.currentSegmentPosition;
				if (currentSegementRemaining > skipLength) {
					// Current segment contains byte
					this.currentSegmentPosition += skipLength;
					skipLength = 0;

				} else {
					// Byte after this current segment
					skipLength -= currentSegementRemaining;
					this.currentSegment = this.currentSegment.next;
					this.currentSegmentPosition = 0;
				}

			}
		}

		// Return the byte at the current position
		return this.currentSegment.buffer.getPooledBuffer()
				.get(this.currentSegment.offset + this.currentSegmentPosition);
	}

	@Override
	public int length() {
		return this.sequenceLength;
	}

	/*
	 * ===================== CharSequence ======================
	 */

	@Override
	public char charAt(int index) {
		return (char) (this.byteAt(index) & 0xff);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		throw new UnsupportedOperationException("Can not sub sequence a " + this.getClass().getName());
	}

	/**
	 * Segment of the {@link StreamBuffer} for the {@link ByteSequence}.
	 */
	private class StreamSegment {

		/**
		 * {@link StreamBuffer} for this segment.
		 */
		private final StreamBuffer<ByteBuffer> buffer;

		/**
		 * Offset into the {@link StreamBuffer} for this segment.
		 */
		private int offset;

		/**
		 * Length of data from the {@link StreamBuffer} for this segment.
		 */
		private int length;

		/**
		 * Next {@link StreamSegment}.
		 */
		private StreamSegment next = null;

		/**
		 * Instantiate.
		 * 
		 * @param buffer
		 *            {@link StreamBuffer} for this segment.
		 * @param offset
		 *            Offset into the {@link StreamBuffer} for this segment.
		 * @param length
		 *            Length of data from the {@link StreamBuffer} for this
		 *            segment.
		 */
		private StreamSegment(StreamBuffer<ByteBuffer> buffer, int offset, int length) {
			this.buffer = buffer;
			this.offset = offset;
			this.length = length;
		}
	}

}