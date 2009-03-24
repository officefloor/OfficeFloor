/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.form;

/**
 * Buffer of characters to efficiently manage parsing of HTTP path and POST
 * body.
 * 
 * @author Daniel
 */
public class CharacterBuffer {

	/**
	 * Buffer of characters.
	 */
	private char[] buffer;

	/**
	 * Length of the buffer containing the value.
	 */
	private int length;

	/**
	 * Initiate.
	 * 
	 * @param initialCapacity
	 *            Initial capacity of the buffer.
	 */
	public CharacterBuffer(int initialCapacity) {
		this.buffer = new char[initialCapacity];
		this.length = 0;
	}

	/**
	 * Clears the buffer so that contains no characters.
	 */
	public void clear() {
		this.length = 0;
	}

	/**
	 * Obtains the number of characters in the buffer.
	 * 
	 * @return Number of characters in the buffer.
	 */
	public int length() {
		return this.length;
	}

	/**
	 * Obtains the character buffer content as a {@link String}.
	 */
	@Override
	public String toString() {
		return new String(this.buffer, 0, this.length);
	}

	/**
	 * Appends a character to this buffer.
	 * 
	 * @param character
	 *            Character to append.
	 */
	public void append(char character) {

		// Ensure space to append the buffer
		if (this.buffer.length == this.length) {
			char[] newBuffer = new char[this.buffer.length * 2];
			System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
			this.buffer = newBuffer;
		}

		// Append the character
		this.buffer[this.length++] = character;
	}
}
