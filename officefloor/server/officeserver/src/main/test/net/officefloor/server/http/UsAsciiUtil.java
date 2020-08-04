/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Utility methods to help in US-ASCII testing.
 * 
 * @author Daniel Sagenschneider
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
	 * @param message  Message.
	 * @param expected Expected text.
	 * @param actual   Actual US-ASCII text.
	 */
	public static void assertEquals(String message, String expected, byte[] actual) {
		TestCase.assertEquals(message, expected, convertToString(actual));
	}

	/**
	 * Asserts the US-ASCII content matches the expected String.
	 * 
	 * @param expected Expected text.
	 * @param actual   Actual US-ASCII text.
	 */
	public static void assertEquals(String expected, byte[] actual) {
		TestCase.assertEquals(expected, convertToString(actual));
	}

	/**
	 * Converts the input text into US-ASCII format.
	 * 
	 * @param text Text.
	 * @return Text in US-ASCII format.
	 */
	public static byte[] convertToUsAscii(String text) {
		// Return the ascii bytes
		return text.getBytes(US_ASCII);
	}

	/**
	 * Convenience method to convert to US-ASCII and HTTP form.
	 * 
	 * @param text Text.
	 * @return HTTP.
	 */
	public static byte[] convertToHttp(String text) {
		return convertToHttp(convertToUsAscii(text));
	}

	/**
	 * Ensures that CR characters are followed by a LF.
	 * 
	 * @param ascii Ascii content.
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
					bytes.add(Byte.valueOf(CR));
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
	 * @param ascii US-ASCII.
	 * @return US-ASCII characters as a String.
	 */
	public static String convertToString(byte[] ascii) {
		// Return the ascii as String
		return new String(ascii, US_ASCII);
	}

	/**
	 * Converts the input US-ASCII character to a char.
	 * 
	 * @param asciiChar US-ASCII character.
	 * @return char.
	 */
	public static char convertToChar(byte asciiChar) {
		return convertToString(new byte[] { asciiChar }).charAt(0);
	}

	/**
	 * Converts the input character into US-ASCII character.
	 * 
	 * @param character Character.
	 * @return US-ASCII character.
	 */
	public static byte convertToUsAscii(char character) {
		return convertToUsAscii(String.valueOf(character))[0];
	}

//	/**
//	 * Creates a {@link ArgumentsMatcher} for method with only one parameter being
//	 * US-ASCII characters.
//	 * 
//	 * @return {@link ArgumentsMatcher}.
//	 */
//	public static ArgumentsMatcher createUsAsciiMatcher() {
//		return new UsAsciiArgumentsMatcher();
//	}
//
//	/**
//	 * {@link ArgumentsMatcher} for US-ASCII comparison.
//	 */
//	private static class UsAsciiArgumentsMatcher implements ArgumentsMatcher {
//
//		/**
//		 * ================= ArgumentsMatcher ======================
//		 */
//
//		@Override
//		public boolean matches(Object[] expected, Object[] actual) {
//
//			// First argument is always content
//			String expectedContent = UsAsciiUtil.convertToString(this.getAsciiContent(expected[0]));
//
//			// Obtain actual data
//			byte[] actualData = this.getAsciiContent(actual[0]);
//
//			// Determine if subset of data
//			if (actual.length == 3) {
//				// Obtain subset from data
//				int offset = ((Integer) actual[1]).intValue();
//				int length = ((Integer) actual[2]).intValue();
//				byte[] data = new byte[length];
//				System.arraycopy(actualData, offset, data, 0, length);
//				actualData = data;
//			}
//
//			// Obtain the actual content
//			String actualContent = UsAsciiUtil.convertToString(actualData);
//
//			// Return whether matches
//			return expectedContent.endsWith(actualContent);
//		}
//
//		@Override
//		public String toString(Object[] arguments) {
//			return UsAsciiUtil.convertToString(this.getAsciiContent(arguments[0]));
//		}
//
//		/**
//		 * Obtains the US-ASCII content from the argument.
//		 * 
//		 * @param argument Argument containing US-ASCII content.
//		 * @return US-ASCII content.
//		 */
//		private byte[] getAsciiContent(Object argument) {
//			if (argument == null) {
//				return new byte[0];
//			} else if (argument instanceof byte[]) {
//				return (byte[]) argument;
//			} else if (argument instanceof ByteBuffer) {
//				ByteBuffer buffer = (ByteBuffer) argument;
//				if (BufferJvmFix.position(buffer) > 0) {
//					buffer = buffer.duplicate();
//					BufferJvmFix.flip(buffer);
//				}
//				byte[] data = new byte[BufferJvmFix.limit(buffer)];
//				buffer.get(data, 0, data.length);
//				return data;
//			} else {
//				TestCase.fail("Unknown argument type: " + argument.getClass().getName());
//				return null;
//			}
//		}
//	}

	/**
	 * All access via static methods.
	 */
	private UsAsciiUtil() {
	}
}
