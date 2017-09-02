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
package net.officefloor.server.http;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * <p>
 * Provides formatting of values for {@link HttpHeader} values.
 * <p>
 * Also provides means for common {@link HttpHeader} values in already encoded
 * HTTP bytes for faster writing.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderValue {

	/**
	 * {@link HttpHeader} value formatter of a {@link Date}.
	 */
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
			.withZone(ZoneOffset.UTC);

	/**
	 * HTTP - (minus) value.
	 */
	private static final byte MINUS = "-".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP 0 value.
	 */
	private static final byte ZERO = "0".getBytes(ServerHttpConnection.HTTP_CHARSET)[0];

	/**
	 * HTTP {@link Long#MIN_VALUE} value.
	 */
	private static final byte[] MIN_VALUE = String.valueOf(Long.MIN_VALUE).getBytes(ServerHttpConnection.HTTP_CHARSET);

	/**
	 * <p>
	 * Writes a long value to the {@link StreamBuffer}.
	 * <p>
	 * This is typically for efficient writing of the
	 * <code>Content-Length</code>.
	 * 
	 * @param value
	 *            Long value to write to the {@link StreamBuffer}.
	 * @param head
	 *            Head {@link StreamBuffer} of linked list of
	 *            {@link StreamBuffer} instances.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 */
	public static <B> void writeInteger(long value, StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {

		// Determine if min value (as can not make positive)
		if (value == Long.MIN_VALUE) {
			StreamBuffer.write(MIN_VALUE, 0, MIN_VALUE.length, head, bufferPool);
			return;
		}

		// Obtain the write buffer
		StreamBuffer<B> writeBuffer = StreamBuffer.getWriteStreamBuffer(head, bufferPool);

		// Write sign
		if (value < 0) {
			writeCharacter(MINUS, writeBuffer, bufferPool);

			// Make positive to write digits
			value = -value;
		}

		// Obtain the one's digit
		byte onesDigit = (byte) (value % 10);
		onesDigit += ZERO;

		// Write the value
		long lessMagnitude = value / 10;
		writeBuffer = recusiveWriteInteger(lessMagnitude, writeBuffer, bufferPool);

		// Always write the first digit
		writeCharacter(onesDigit, writeBuffer, bufferPool);
	}

	/**
	 * Uses recursion to write the long digits.
	 * 
	 * @param value
	 *            Value to write.
	 * @param writeBuffer
	 *            Write {@link StreamBuffer}.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 * @return Next write {@link StreamBuffer}.
	 */
	private static <B> StreamBuffer<B> recusiveWriteInteger(long value, StreamBuffer<B> writeBuffer,
			StreamBufferPool<B> bufferPool) {

		// Drop out when value at zero
		if (value == 0) {
			return writeBuffer;
		}

		// Not complete, so continue writing the next digit
		long lessMagnitude = value / 10;
		writeBuffer = recusiveWriteInteger(lessMagnitude, writeBuffer, bufferPool);

		// Now write the current digit
		byte currentDigit = (byte) (value % 10);
		currentDigit += ZERO;
		writeBuffer = writeCharacter(currentDigit, writeBuffer, bufferPool);

		// Return the write buffer
		return writeBuffer;
	}

	/**
	 * Writes a HTTP encoded character.
	 * 
	 * @param character
	 *            Character to write.
	 * @param writeBuffer
	 *            Write {@link StreamBuffer}.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 * @return Next write {@link StreamBuffer}.
	 */
	public static <B> StreamBuffer<B> writeCharacter(byte character, StreamBuffer<B> writeBuffer,
			StreamBufferPool<B> bufferPool) {
		// Attempt to write to buffer
		if (!writeBuffer.write(character)) {
			// Buffer full, so write to new buffer
			writeBuffer.next = bufferPool.getPooledStreamBuffer();
			writeBuffer = writeBuffer.next;
			if (!writeBuffer.write(character)) {
				throw new IllegalStateException("New pooled space buffer should always have space");
			}
		}
		return writeBuffer;
	}

	/**
	 * Obtains the HTTP value for an Integer.
	 * 
	 * @param value
	 *            Integer value.
	 * @return HTTP value for the Integer.
	 */
	public static String getIntegerValue(long value) {
		return String.valueOf(value);
	}

	/**
	 * Obtains the HTTP value for a {@link Date}.
	 * 
	 * @param value
	 *            {@link Date} value.
	 * @return HTTP value for the {@link Date}.
	 */
	public static String getDateValue(Date value) {
		return dateFormatter.format(value.toInstant());
	}

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Pre-encoded bytes of value ready for HTTP output.
	 */
	private final byte[] encodedValue;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            {@link HttpHeaderValue}.
	 */
	public HttpHeaderValue(String value) {
		this.value = value;
		this.encodedValue = this.value.getBytes(ServerHttpConnection.HTTP_CHARSET);
	}

	/**
	 * Instantiate with integer.
	 * 
	 * @param value
	 *            Integer.
	 */
	public HttpHeaderValue(int value) {
		this(getIntegerValue(value));
	}

	/**
	 * Instantiate with date.
	 * 
	 * @param value
	 *            Date.
	 */
	public HttpHeaderValue(Date value) {
		this(getDateValue(value));
	}

	/**
	 * Obtains the value.
	 * 
	 * @return value.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Writes this {@link HttpHeaderValue} to the {@link StreamBuffer}.
	 * 
	 * @param head
	 *            Head {@link StreamBuffer} of linked list of
	 *            {@link StreamBuffer} instances.
	 * @param bufferPool
	 *            {@link StreamBufferPool}.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {
		StreamBuffer.write(this.encodedValue, 0, this.encodedValue.length, head, bufferPool);
	}

}