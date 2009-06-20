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
package net.officefloor.plugin.work.http.html.form;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.work.http.html.form.CharacterBuffer;

/**
 * Tests the {@link CharacterBuffer}.
 * 
 * @author Daniel Sagenschneider
 */
public class CharacterBufferTest extends OfficeFrameTestCase {

	/**
	 * Append no characters.
	 */
	public void testNoAppending() {
		this.doTest(32, "");
	}

	/**
	 * Append within initial capacity.
	 */
	public void testWithinInitialCapacity() {
		this.doTest(32, "test");
	}

	/**
	 * Ensure able to reuse.
	 */
	public void testReuse() {
		this.doTest(32, "use", "again", "and", "again");
	}

	/**
	 * Append requiring increasing capacity.
	 */
	public void testIncreaseCapacity() {
		this.doTest(2, "test");
	}

	/**
	 * Ensure able to reuse increasing the capacity.
	 */
	public void testReuseIncreasingCapacity() {
		this.doTest(3, "use", "again", "and", "again", "many-times", "over");
	}

	/**
	 * Does the testing of the {@link CharacterBuffer}.
	 * 
	 * @param initialCapacity
	 *            Initial capacity of the {@link CharacterBuffer}.
	 * @param characterTests
	 *            Each listing of characters to append to the
	 *            {@link CharacterBuffer} with a clear between each one.
	 */
	private void doTest(int initialCapacity, String... characterTests) {

		// Create the character buffer
		CharacterBuffer buffer = new CharacterBuffer(initialCapacity);

		// Ensure no string content initially
		assertEquals("Empty string initially", "", buffer.toString());

		// Ensure validate each append and clear gets write string
		for (String characterTest : characterTests) {

			// Append the characters
			for (char character : characterTest.toCharArray()) {
				buffer.append(character);
			}

			// Validate correct string returned
			assertEquals("Incorrect string returned", characterTest, buffer
					.toString());
			assertEquals("Incorrect buffer length", characterTest.length(),
					buffer.length());

			// Clear the character string and validate empty string
			buffer.clear();
			assertEquals("Buffer expected to be empty", "", buffer.toString());
		}
	}
}
