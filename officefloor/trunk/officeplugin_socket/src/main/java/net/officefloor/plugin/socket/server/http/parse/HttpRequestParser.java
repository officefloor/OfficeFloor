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
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for a HTTP request.
 * 
 * @author Daniel
 */
public class HttpRequestParser {

	/*
	 * ASCII values.
	 */
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	private static byte UsAscii(char character) {
		return String.valueOf(character).getBytes(US_ASCII)[0];
	}

	private static final byte A = UsAscii('A');

	private static final byte Z = UsAscii('Z');

	private static final byte a = UsAscii('a');

	private static final byte z = UsAscii('z');

	private static final byte SP = UsAscii(' ');

	private static final byte HT = UsAscii('\t');

	private static final byte CR = UsAscii('\r');

	private static final byte LF = UsAscii('\n');

	private static final byte COLON = UsAscii(':');

	private static final byte atoA = (byte) (A - a);

	/**
	 * Determines if character is a letter of the alphabet.
	 * 
	 * @param character
	 *            ASCII character.
	 * @return <code>true</code> if letter of alphabet.
	 */
	private static boolean isAlpha(byte character) {
		return ((character >= A) && (character <= Z))
				|| ((character >= a) && (character <= z));
	}

	/**
	 * Determines if character is a control.
	 * 
	 * @param character
	 *            ASCII character.
	 * @return <code>true</code> if letter is control.
	 */
	private static boolean isCtl(byte character) {
		return (character <= 31) || (character == 127);
	}

	/**
	 * Determines if character is white space.
	 * 
	 * @param character
	 *            ASCII character.
	 * @return <code>true</code> if white space.
	 */
	private static boolean isWs(byte character) {
		return isLws(character) || (character == CR) || (character == LF);
	}

	/**
	 * Determines if character is a liner white space.
	 * 
	 * @param character
	 *            ASCII character.
	 * @return <code>true</code> if {@link #SP} or {@link #HT}.
	 */
	private static boolean isLws(byte character) {
		return (character == SP) || (character == HT);
	}

	/**
	 * Converts ASCII character to upper case.
	 * 
	 * @param character
	 *            ASCII character.
	 * @return Upper case ASCII character.
	 */
	private static byte toUpper(byte character) {
		return ((character >= a) && (character <= z)) ? (byte) (character + atoA)
				: character;
	}

	/**
	 * Indicates the state of parsing.
	 */
	private static enum ParseState {
		START, METHOD, METHOD_PATH_SEPARATION, PATH, PATH_VERSION_SEPARATION, VERSION,

		HEADER_CR, HEADER_CR_NAME_SEPARATION, HEADER_NAME, HEADER_NAME_VALUE_SEPARATION, HEADER_VALUE,

		BODY_CR, BODY
	};

	/**
	 * {@link ParseState} which starts with the HTTP method.
	 */
	private ParseState parseState = ParseState.START;

	/**
	 * Method.
	 */
	private final UsAsciiStringBuilder method = new UsAsciiStringBuilder(7, 7);

	/**
	 * Path.
	 */
	private final UsAsciiStringBuilder path = new UsAsciiStringBuilder(30, 255);

	/**
	 * Version.
	 */
	private final UsAsciiStringBuilder version = new UsAsciiStringBuilder(8, 8);

	/**
	 * Header name.
	 */
	private final UsAsciiStringBuilder headerName = new UsAsciiStringBuilder(
			20, 50);

	/**
	 * Header value.
	 */
	private final UsAsciiStringBuilder headerValue = new UsAsciiStringBuilder(
			20, 255);

	/**
	 * Headers.
	 */
	private final Map<String, String> headers = new HashMap<String, String>(20);

	/**
	 * Body.
	 */
	private final UsAsciiStringBuilder body = new UsAsciiStringBuilder(10, 1024);

	/**
	 * Parses the additionally read content from the client.
	 * 
	 * @param buffer
	 *            Buffer containing the content.
	 * @param offset
	 *            Offset into the buffer where content starts.
	 * @param length
	 *            Number of bytes available in the buffer.
	 * @return <code>true</code> if HTTP request is fully received from
	 *         client.
	 * @throws ParseException
	 *             If fails to parse HTTP request.
	 */
	public boolean parseMoreContent(byte[] buffer, int offset, int length)
			throws ParseException {

		// Read the characters parsing the HTTP request
		for (int i = offset; i < (offset + length); i++) {
			byte character = buffer[i];

			// TODO remove
			System.out.print(new String(new byte[] { character }, 0, 1));

			switch (this.parseState) {
			case START:
				// Ignore leading white space
				if (isWs(character)) {
					break;
				}
				this.parseState = ParseState.METHOD;

			case METHOD:
				if (isAlpha(character)) {
					// Add the character of the method
					this.method.append(character);
				} else if (isLws(character)) {
					// Method name read in
					this.parseState = ParseState.METHOD_PATH_SEPARATION;
				} else {
					// Unexpected character
					throw new ParseException(
							"Unexpected character in method name '" + character
									+ "'");
				}
				break;

			case METHOD_PATH_SEPARATION:
				// Ignore separating linear white space
				if (isLws(character)) {
					break;
				}
				this.parseState = ParseState.PATH;

			case PATH:
				if (isLws(character)) {
					// Have retrieved the path
					this.parseState = ParseState.PATH_VERSION_SEPARATION;
				} else if (!isCtl(character)) {
					// Append character to path
					this.path.append(character);
				} else {
					// Unexpected character
					throw new ParseException("Unexpected character in path '"
							+ character + "'");
				}
				break;

			case PATH_VERSION_SEPARATION:
				// Ignore separating linear white space
				if (isLws(character)) {
					break;
				}
				this.parseState = ParseState.VERSION;

			case VERSION:
				if (character == CR) {
					// Have retrieved the version
					this.parseState = ParseState.HEADER_CR;
				} else {
					// Add the character of the version
					this.version.append(character);
				}
				break;

			case HEADER_CR:
				if (character == LF) {
					// Expecting LF, so continue on with header
					this.parseState = ParseState.HEADER_CR_NAME_SEPARATION;
					break;
				}
				throw new ParseException("Should expect LR after a CR");

			case HEADER_CR_NAME_SEPARATION:
				if (character == CR) {
					// End of headers
					this.parseState = ParseState.BODY_CR;
					break;
				} else if (isLws(character)) {
					// Continue previous header value
					this.parseState = ParseState.HEADER_NAME_VALUE_SEPARATION;
					break;
				}
				// New header

			case HEADER_NAME:
				if (character == COLON) {
					// Header value obtained
					this.parseState = ParseState.HEADER_NAME_VALUE_SEPARATION;
				} else if (!isCtl(character)) {
					// Append the header name character
					this.headerName.append(toUpper(character));
				} else {
					// Unknown header name character
					throw new ParseException("Unknown header name character '"
							+ character + "'");
				}
				break;

			case HEADER_NAME_VALUE_SEPARATION:
				// Ignore separating linear white space
				if (isLws(character)) {
					break;
				}
				this.parseState = ParseState.HEADER_VALUE;

			case HEADER_VALUE:
				if (character == CR) {
					// Load the header value
					String name = this.headerName.toString().trim();
					String value = this.headerValue.toString();
					this.headers.put(name, value);

					// Clear values
					this.headerName.clear();
					this.headerValue.clear();

					// Potentially end of header value
					this.parseState = ParseState.HEADER_CR;
				} else if (!isCtl(character)) {
					// Append the header value character
					this.headerValue.append(character);
				} else {
					// Unknown header value character
					throw new ParseException("Unknown header value character '"
							+ character + "'");
				}
				break;

			case BODY_CR:
				if (character == LF) {
					// Expecting LF, so continue on with body
					this.parseState = ParseState.BODY;
					break;
				}
				throw new ParseException("Should expect LR after a CR");

			case BODY:
				// Append the body data
				this.body.append(character);
				break;
			}
		}

		// Complete when body read
		// TODO tie into determining the body length
		return (this.parseState == ParseState.BODY);
	}

	/**
	 * Obtains the method.
	 * 
	 * @return Method.
	 */
	public String getMethod() {
		return this.method.toString();
	}

	/**
	 * Obtains the path.
	 * 
	 * @return Path.
	 */
	public String getPath() {
		return this.path.toString();
	}

	/**
	 * Obtains the version.
	 * 
	 * @return Version.
	 */
	public String getVersion() {
		return this.version.toString();
	}

	/**
	 * Obtain the headers.
	 * 
	 * @return Headers.
	 */
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	/**
	 * Obtains the header value.
	 * 
	 * @param name
	 *            Name of header.
	 * @return Value of header.
	 */
	public String getHeader(String name) {
		return this.headers.get(name.toUpperCase());
	}

	/**
	 * Obtains the body.
	 * 
	 * @return Body.
	 */
	public byte[] getBody() {
		// Return as the bytes input
		return this.body.toUsAscii();
	}

}
