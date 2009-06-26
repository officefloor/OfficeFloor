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
package net.officefloor.plugin.socket.server.http.parse;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;
import net.officefloor.plugin.socket.server.http.HttpStatus;

/**
 * Tests the {@link UsAsciiStringBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class UsAsciiStringBuilderTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link ParseExceptionFactory}.
	 */
	private ParseExceptionFactory parseExceptionFactory = this
			.createMock(ParseExceptionFactory.class);

	/**
	 * Validate the conversion to US-ASCII.
	 */
	public void testUsAsciiUtil() throws Exception {

		// US-ASCII values
		final byte a = 97;
		final byte z = 122;
		final byte A = 65;
		final byte Z = 90;
		final byte _0 = 48;
		final byte _9 = 57;
		final byte colon = 58;
		final byte CR = 13;
		final byte LF = 10;

		// String
		final String text = "azAZ09:\r\n";

		byte[] ascii = UsAsciiUtil.convertToUsAscii(text);

		// Validate conversion to US-ASCII
		assertEquals(a, ascii[0]);
		assertEquals(z, ascii[1]);
		assertEquals(A, ascii[2]);
		assertEquals(Z, ascii[3]);
		assertEquals(_0, ascii[4]);
		assertEquals(_9, ascii[5]);
		assertEquals(colon, ascii[6]);
		assertEquals(CR, ascii[7]);
		assertEquals(LF, ascii[8]);

		// Validate conversion back to String
		assertEquals(text, UsAsciiUtil.convertToString(ascii));

		// Ensure the asserts are correct
		UsAsciiUtil.assertEquals(text, ascii);
		UsAsciiUtil.assertEquals("Text", text, ascii);

		// Validate character conversion
		assertEquals(a, UsAsciiUtil.convertToUsAscii('a'));
	}

	/**
	 * Ensure can handle a single character.
	 */
	public void testSingleCharacter() throws Exception {
		final char CHARACTER = 'a';
		this.replayMockObjects();

		UsAsciiStringBuilder text = new UsAsciiStringBuilder(10, 10,
				this.parseExceptionFactory);

		// Add data to text
		text.append(UsAsciiUtil.convertToUsAscii(CHARACTER));

		assertEquals("Incorrect number of characters", 1, text
				.getCharacterCount());
		assertEquals("Buffer size should not be increased", 10, text
				.getBuffer().length);
		assertEquals("Incorrect bufferred data", CHARACTER, UsAsciiUtil
				.convertToChar(text.getBuffer()[0]));
		assertEquals("Should not have additional data in buffer", 0, text
				.getBuffer()[1]);
		UsAsciiUtil.assertEquals("Incorrect ascii", String.valueOf(CHARACTER),
				text.toUsAscii());
		assertEquals("Incorrect string", String.valueOf(CHARACTER), text
				.toString());

		this.verifyMockObjects();
	}

	/**
	 * Ensure creates within initial capacity.
	 */
	public void testWithinInitialCapacity() throws Exception {
		final String STRING = "test56789!";
		this.replayMockObjects();

		UsAsciiStringBuilder text = new UsAsciiStringBuilder(10, 10,
				this.parseExceptionFactory);

		// Add data to text
		byte[] ascii = UsAsciiUtil.convertToUsAscii(STRING);
		for (byte character : ascii) {
			text.append(character);
		}

		assertEquals("Incorrect number of characters", 10, text
				.getCharacterCount());
		assertEquals("Buffer size should not be increased", 10, text
				.getBuffer().length);
		UsAsciiUtil.assertEquals("Incorrect bufferred data", STRING, text
				.getBuffer());
		UsAsciiUtil.assertEquals("Incorrect ascii", STRING, text.toUsAscii());
		assertEquals("Incorrect string", STRING, text.toString());

		this.verifyMockObjects();
	}

	/**
	 * Ensure able to increase capacity.
	 */
	public void testIncreaseCapacity() throws Exception {
		final String STRING = "test56789!";
		this.replayMockObjects();

		UsAsciiStringBuilder text = new UsAsciiStringBuilder(5, 15,
				this.parseExceptionFactory);

		// Add data to text
		for (byte character : UsAsciiUtil.convertToUsAscii(STRING)) {
			text.append(character);
		}

		assertEquals("Incorrect number of characters", 10, text
				.getCharacterCount());
		assertEquals("Buffer size should be increased", 10,
				text.getBuffer().length);
		UsAsciiUtil.assertEquals("Incorrect bufferred data", STRING, text
				.getBuffer());
		UsAsciiUtil.assertEquals("Incorrect ascii", STRING, text.toUsAscii());
		assertEquals("Incorrect string", STRING, text.toString());

		this.verifyMockObjects();
	}

	/**
	 * Ensure correctly recognises reached max size.
	 */
	public void testMaxSize() throws Exception {
		final String STRING = "test56789!";
		final ParseException parseException = new ParseException(
				HttpStatus._400, "test");

		// Record actions on mock
		this.recordReturn(this.parseExceptionFactory,
				this.parseExceptionFactory.createParseException(null),
				parseException, new TypeMatcher(UsAsciiStringBuilder.class));
		this.replayMockObjects();

		UsAsciiStringBuilder text = new UsAsciiStringBuilder(5, 10,
				this.parseExceptionFactory);

		// Add data to text
		for (byte character : UsAsciiUtil.convertToUsAscii(STRING)) {
			text.append(character);
		}
		assertEquals("Buffer should now be at maximum size", 10, text
				.getBuffer().length);

		// Ensure can not add another character
		try {
			text.append(UsAsciiUtil.convertToUsAscii('a'));
			fail("Should not be able add more than max characters");
		} catch (ParseException ex) {
			assertEquals("Incorrect parse exception", parseException, ex);
		}
		this.verifyMockObjects();
	}

	/**
	 * Ensure may clear.
	 */
	public void testClear() throws Exception {
		final String ONE = "ONE";
		final String TWO = "TWO";
		this.replayMockObjects();

		UsAsciiStringBuilder text = new UsAsciiStringBuilder(5, 15,
				this.parseExceptionFactory);

		// Add ONE
		for (byte character : UsAsciiUtil.convertToUsAscii(ONE)) {
			text.append(character);
		}
		assertEquals("Incorrect ONE", ONE, text.toString());

		// Clear
		text.clear();
		assertEquals("Incorrect after clear", "", text.toString());
		assertEquals("Incorrect character count", 0, text.getCharacterCount());

		// Add TWO
		for (byte character : UsAsciiUtil.convertToUsAscii(TWO)) {
			text.append(character);
		}
		assertEquals("Incorrect TWO", TWO, text.toString());

		this.verifyMockObjects();
	}

}
