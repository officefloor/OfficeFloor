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

import junit.framework.TestCase;

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
	 * All access via static methods.
	 */
	private UsAsciiUtil() {
	}
}
