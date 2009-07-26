/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.parse.impl;

import java.nio.charset.Charset;
import java.util.Arrays;

import net.officefloor.plugin.socket.server.http.parse.ParseException;

/**
 * Provides manipulation of US-ASCII raw data.
 *
 * @author Daniel Sagenschneider
 */
@Deprecated
public class UsAsciiStringBuilder {

	/**
	 * US-ASCII {@link Charset}.
	 */
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	/**
	 * Maximum number of characters allowed.
	 */
	private final int maxChars;

	/**
	 * {@link ParseExceptionFactory}.
	 */
	private final ParseExceptionFactory parseExceptionFactory;

	/**
	 * US-ASCII character buffer.
	 */
	private byte[] chars;

	/**
	 * Number of valid characters in {@link #chars}.
	 */
	private int count = 0;

	/**
	 * Initiate.
	 *
	 * @param initialCapacity
	 *            Initial capacity of the underlying buffer.
	 * @param maxChars
	 *            Maximum number of characters allowed.
	 * @param parseExceptionFactory
	 *            {@link ParseExceptionFactory}.
	 */
	public UsAsciiStringBuilder(int initialCapacity, int maxChars,
			ParseExceptionFactory parseExceptionFactory) {
		this.chars = new byte[initialCapacity];
		this.maxChars = maxChars;
		this.parseExceptionFactory = parseExceptionFactory;
	}

	/**
	 * Appends a character.
	 *
	 * @param character
	 *            US-ASCII character value.
	 * @throws ParseException
	 *             If string too long.
	 */
	public void append(byte character) throws ParseException {

		// Ensure enough space in the buffer
		if (this.count == this.chars.length) {

			// Ensure string is not too long
			if (this.count == this.maxChars) {
				throw this.parseExceptionFactory.createParseException(this);
			}

			// No space in buffer so double size of buffer
			this.chars = Arrays.copyOf(this.chars, Math.min(
					this.chars.length << 1, this.maxChars));
		}

		// Append the character
		this.chars[this.count++] = character;
	}

	/**
	 * <p>
	 * Obtains the underlying buffer.
	 * <p>
	 * This allows for efficient manipulation of the buffer.
	 *
	 * @return Underlying buffer.
	 */
	public byte[] getBuffer() {
		return this.chars;
	}

	/**
	 * Obtains the number of valid characters in the buffer.
	 *
	 * @return Number of valid characters in the buffer.
	 */
	public int getCharacterCount() {
		return this.count;
	}

	/**
	 * Obtains the US-ASCII data.
	 *
	 * @return US-ASCII data.
	 */
	public byte[] toUsAscii() {
		return Arrays.copyOf(this.chars, this.count);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String(this.chars, 0, this.count, US_ASCII);
	}

	/**
	 * Clears the characters (sets valid characters to 0).
	 */
	public void clear() {
		this.count = 0;
	}

}
