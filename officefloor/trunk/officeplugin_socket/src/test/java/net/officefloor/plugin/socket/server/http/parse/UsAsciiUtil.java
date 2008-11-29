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
package net.officefloor.plugin.socket.server.http.parse;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.AbstractMatcher;
import org.easymock.ArgumentsMatcher;

/**
 * Utility methods to help in US-ASCII testing.
 * 
 * @author Daniel
 */
public class UsAsciiUtil {

	/**
	 * US-ASCII {@link Charset}.
	 */
	public static final Charset US_ASCII = Charset.forName("US-ASCII");

	private static final byte CR = convertToUsAscii('\r');

	private static final byte LF = convertToUsAscii('\n');

	/**
	 * Asserts the US-ASCII content matches the expected String.
	 * 
	 * @param message
	 *            Message.
	 * @param expected
	 *            Expected text.
	 * @param actual
	 *            Actual US-ASCII text.
	 */
	public static void assertEquals(String message, String expected,
			byte[] actual) {
		TestCase.assertEquals(message, expected, convertToString(actual));
	}

	/**
	 * Asserts the US-ASCII content matches the expected String.
	 * 
	 * @param expected
	 *            Expected text.
	 * @param actual
	 *            Actual US-ASCII text.
	 */
	public static void assertEquals(String expected, byte[] actual) {
		TestCase.assertEquals(expected, convertToString(actual));
	}

	/**
	 * Converts the input text into US-ASCII format.
	 * 
	 * @param text
	 *            Text.
	 * @return Text in US-ASCII format.
	 */
	public static byte[] convertToUsAscii(String text) {
		// Return the ascii bytes
		return text.getBytes(US_ASCII);
	}

	/**
	 * Convenience method to convert to US-ASCII and HTTP form.
	 * 
	 * @param text
	 *            Text.
	 * @return HTTP.
	 */
	public static byte[] convertToHttp(String text) {
		return convertToHttp(convertToUsAscii(text));
	}

	/**
	 * Ensures that CR characters are followed by a LF.
	 * 
	 * @param ascii
	 *            Ascii content.
	 * @return HTTP.
	 */
	public static byte[] convertToHttp(byte[] ascii) {

		// Transform end of lines if necessary
		List<Byte> bytes = new ArrayList<Byte>(ascii.length + 10);
		boolean isLastCr = false;
		for (byte character : ascii) {

			// Add CR before LF is necessary
			if (character == LF) {
				if (!isLastCr) {
					bytes.add(new Byte(CR));
				}
			}

			// Add the character
			bytes.add(character);

			// Flag if last is CR
			isLastCr = false;
			if (character == CR) {
				isLastCr = true;
			}
		}

		// Create the HTTP bytes
		byte[] buffer = new byte[bytes.size()];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = bytes.get(i).byteValue();
		}

		// Return the HTTP bytes
		return buffer;
	}

	/**
	 * Converts the input US-ASCII to String.
	 * 
	 * @param ascii
	 *            US-ASCII.
	 * @return US-ASCII characters as a String.
	 */
	public static String convertToString(byte[] ascii) {
		// Return the ascii as String
		return new String(ascii, US_ASCII);
	}

	/**
	 * Converts the input US-ASCII character to a char.
	 * 
	 * @param asciiChar
	 *            US-ASCII character.
	 * @return char.
	 */
	public static char convertToChar(byte asciiChar) {
		return convertToString(new byte[] { asciiChar }).charAt(0);
	}

	/**
	 * Converts the input character into US-ASCII character.
	 * 
	 * @param character
	 *            Character.
	 * @return US-ASCII character.
	 */
	public static byte convertToUsAscii(char character) {
		return convertToUsAscii(String.valueOf(character))[0];
	}

	/**
	 * Creates a {@link ArgumentsMatcher} for method with only one parameter
	 * being US-ASCII characters.
	 * 
	 * @return {@link ArgumentsMatcher}.
	 */
	public static ArgumentsMatcher createUsAsciiMatcher() {
		return new AbstractMatcher() {
			@Override
			protected boolean argumentMatches(Object expected, Object actual) {
				byte[] expectedMessage = (byte[]) expected;
				byte[] actualMessage = (byte[]) actual;
				return Arrays.equals(expectedMessage, actualMessage);
			}

			@Override
			protected String argumentToString(Object argument) {
				return (argument == null ? "" : UsAsciiUtil
						.convertToString((byte[]) argument));
			}
		};
	}

	/**
	 * All access via static methods.
	 */
	private UsAsciiUtil() {
	}
}
