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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpStatus;

/**
 * Parser for a HTTP request.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestParser {

	/*
	 * ASCII values.
	 */
	private static final Charset US_ASCII = Charset.forName("US-ASCII");

	private static byte UsAscii(char character) {
		return UsAscii(String.valueOf(character))[0];
	}

	private static byte[] UsAscii(String text) {
		return text.getBytes(US_ASCII);
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
	 * <p>
	 * Valid HTTP methods.
	 * <p>
	 * Note: order is done on most likely to occur to improve valid content
	 * performance.
	 */
	private static final byte[][] HTTP_METHODS = new byte[][] { UsAscii("GET"),
			UsAscii("POST"), UsAscii("HEAD"), UsAscii("PUT"),
			UsAscii("DELETE"), UsAscii("OPTIONS"), UsAscii("TRACE"),
			UsAscii("CONNECT") };

	/**
	 * HTTP methods requiring a Content-Length header.
	 */
	private static final byte[][] METHODS_REQUIRING_CONTENT_LENGTH = new byte[][] {
			UsAscii("POST"), UsAscii("PUT") };

	/**
	 * Header name for the Content-Length.
	 */
	private static final String HEADER_NAME_CONTENT_LENGTH = "CONTENT-LENGTH";

	/**
	 * HTTP version. HTTP/1.1.
	 */
	public static final String HTTP_1_1 = "HTTP/1.1";

	/**
	 * HTTP version. HTTP/1.0.
	 */
	public static final String HTTP_1_0 = "HTTP/1.0";

	/**
	 * Default HTTP version.
	 */
	public static final String DEFAULT_HTTP_VERSION = HTTP_1_0;

	/**
	 * <p>
	 * Valid HTTP versions.
	 * <p>
	 * Note: order is done on most likely to occur to improve valid content
	 * performance.
	 */
	private static final byte[][] HTTP_VERSIONS = new byte[][] {
			UsAscii(HTTP_1_1), UsAscii(HTTP_1_0) };

	/**
	 * Indicates if the partial content received so far is valid against the
	 * valid values.
	 * 
	 * @param partialContent
	 *            {@link UsAsciiStringBuilder} containing the partial content to
	 *            validate.
	 * @param validValues
	 *            Valid values for the content.
	 * @return <code>true</code> if partial content is valid.
	 */
	private static boolean isPartialContentValid(
			UsAsciiStringBuilder partialContent, byte[][] validValues) {

		// Characters to validate
		byte[] characters = partialContent.getBuffer();
		int count = partialContent.getCharacterCount();

		// Determine if partially matches a valid value
		VALID_VALUE: for (byte[] validValue : validValues) {

			// Not valid if partial content longer
			if (count > validValue.length) {
				continue VALID_VALUE;
			}

			// Ensure characters match
			for (int i = 0; i < count; i++) {
				if (characters[i] != validValue[i]) {
					continue VALID_VALUE;
				}
			}

			// Partially matches a valid value
			return true;
		}

		// Did not partially match a valid value
		return false;
	}

	/**
	 * Indicates if the content matches a valid value.
	 * 
	 * @param content
	 *            {@link UsAsciiStringBuilder} containing the content.
	 * @param validValues
	 *            Valid values for the content.
	 * @return <code>true</code> if content is valid value.
	 */
	private static boolean isContentValid(UsAsciiStringBuilder content,
			byte[][] validValues) {

		// Characters to validate
		byte[] characters = content.getBuffer();
		int count = content.getCharacterCount();

		// Determine if matches a valid value
		VALID_VALUE: for (byte[] validValue : validValues) {

			// Not valid if lengths different
			if (count != validValue.length) {
				continue VALID_VALUE;
			}

			// Ensure characters match
			for (int i = 0; i < validValue.length; i++) {
				if (characters[i] != validValue[i]) {
					continue VALID_VALUE;
				}
			}

			// Matches a valid value
			return true;
		}

		// Did not match a valid value
		return false;
	}

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
	 * {@link ParseState} indicating if method and version are correct.
	 */
	private ParseState validState = ParseState.START;

	/**
	 * Method {@link ParseExceptionFactory}/
	 */
	private static final ParseExceptionFactory methodParseFactory = new ParseExceptionFactory() {
		@Override
		public ParseException createParseException(UsAsciiStringBuilder content) {
			return new ParseException(HttpStatus._400, "Unknown method: "
					+ content.toString() + "...");
		}
	};

	/**
	 * Method.
	 */
	private final UsAsciiStringBuilder method = new UsAsciiStringBuilder(7, 7,
			methodParseFactory);

	/**
	 * Path {@link ParseExceptionFactory}.
	 */
	private static final ParseExceptionFactory pathParseFactory = new ParseExceptionFactory() {
		@Override
		public ParseException createParseException(UsAsciiStringBuilder content) {
			return new ParseException(HttpStatus._414,
					"Request URI must be less than "
							+ content.getCharacterCount() + " characters");
		}
	};

	/**
	 * Path.
	 */
	private final UsAsciiStringBuilder path = new UsAsciiStringBuilder(30, 255,
			pathParseFactory);

	/**
	 * Version {@link ParseExceptionFactory}.
	 */
	private static final ParseExceptionFactory versionParseFactory = new ParseExceptionFactory() {
		@Override
		public ParseException createParseException(UsAsciiStringBuilder content) {
			return new ParseException(HttpStatus._400, "Unknown version: "
					+ content.toString() + "...");
		}
	};

	/**
	 * Version.
	 */
	private final UsAsciiStringBuilder version = new UsAsciiStringBuilder(8, 8,
			versionParseFactory);

	/**
	 * Header name {@link ParseExceptionFactory}.
	 */
	private static final ParseExceptionFactory headerNameParseFactory = new ParseExceptionFactory() {
		@Override
		public ParseException createParseException(UsAsciiStringBuilder content) {
			return new ParseException(HttpStatus._400,
					"Header name too large: " + content.toString()
							+ "... (must be less than "
							+ content.getCharacterCount() + " characters)");
		}
	};

	/**
	 * Header name.
	 */
	private final UsAsciiStringBuilder headerName = new UsAsciiStringBuilder(
			20, 50, headerNameParseFactory);

	/**
	 * Header value {@link ParseExceptionFactory}.
	 */
	private static final ParseExceptionFactory headerValueParseFactory = new ParseExceptionFactory() {
		@Override
		public ParseException createParseException(UsAsciiStringBuilder content) {
			return new ParseException(HttpStatus._400,
					"Header value too large: " + content.toString()
							+ "... (must be less than "
							+ content.getCharacterCount() + " characters");
		}
	};

	/**
	 * Header value.
	 */
	private final UsAsciiStringBuilder headerValue = new UsAsciiStringBuilder(
			20, 255, headerValueParseFactory);

	/**
	 * Headers.
	 */
	private final Map<String, String> headers = new HashMap<String, String>(20);

	/**
	 * Body {@link ParseExceptionFactory}.
	 */
	private static final ParseExceptionFactory bodyParseFactory = new ParseExceptionFactory() {
		@Override
		public ParseException createParseException(UsAsciiStringBuilder content) {
			return new ParseException(HttpStatus._400,
					"Request entity exceeded Content-Length size of "
							+ content.getCharacterCount());
		}
	};

	/**
	 * Maximum initial length of the buffer to contain the body (entity).
	 */
	private final int initialBodyBufferLength;

	/**
	 * Maximum length of the body (entity).
	 */
	private final int maxBodyLength;

	/**
	 * Body. This is created based on the Content-Length taking into account
	 * {@link #initialBodyLength} and {@link #maxBodyLength}.
	 */
	private UsAsciiStringBuilder body = null;

	/**
	 * Content length value for request.
	 */
	private int contentLength = -1;

	/**
	 * Initiate.
	 * 
	 * @param initialBodyBufferLength
	 *            Initial length of the body buffer. This is so that all memory
	 *            need not be allocated on receiving request, only as request is
	 *            received.
	 * @param maxBodyLength
	 *            Maximum length of the body buffer. Requests with bodies
	 *            greater that this will fail parsing.
	 */
	public HttpRequestParser(int initialBodyBufferLength, int maxBodyLength) {
		this.initialBodyBufferLength = initialBodyBufferLength;
		this.maxBodyLength = maxBodyLength;
	}

	/**
	 * Parses the additionally read content from the client.
	 * 
	 * @param buffer
	 *            Buffer containing the content.
	 * @param offset
	 *            Offset into the buffer where content starts.
	 * @param length
	 *            Number of bytes available in the buffer.
	 * @return <code>true</code> if HTTP request is fully received from client.
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
					throw new ParseException(HttpStatus._400,
							"Unexpected character in method '" + character
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
					throw new ParseException(HttpStatus._400,
							"Unexpected character in path '" + character + "'");
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
				throw new ParseException(HttpStatus._400,
						"Should expect LF after a CR for status line");

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
					throw new ParseException(HttpStatus._400,
							"Unknown header name character '" + character + "'");
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
					String value = this.headerValue.toString().trim();
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
					throw new ParseException(HttpStatus._400,
							"Unknown header value character '" + character
									+ "'");
				}
				break;

			case BODY_CR:
				// Must have LF after CR for body
				if (character != LF) {
					throw new ParseException(HttpStatus._400,
							"Should expect LR after a CR after header");
				}

				// Attempt to obtain the Content-Length
				String contentLengthValue = this.headers
						.get(HEADER_NAME_CONTENT_LENGTH);
				if (contentLengthValue != null) {
					// Should always be able to convert to an integer
					try {
						this.contentLength = Integer
								.parseInt(contentLengthValue);
					} catch (NumberFormatException ex) {
						throw new ParseException(HttpStatus._411,
								"Content-Length header value "
										+ contentLengthValue
										+ " must be an integer");
					}
				} else {
					// Ensure method does not require Content-Length
					if (isContentValid(this.method,
							METHODS_REQUIRING_CONTENT_LENGTH)) {
						throw new ParseException(HttpStatus._411,
								"Must provide Content-Length header for "
										+ this.method.toString());
					}
				}

				// Provide body buffer if have Content-Length
				if (this.contentLength > 0) {
					// Ensure the Content-Length within limits
					if (this.contentLength > this.maxBodyLength) {
						throw new ParseException(HttpStatus._413,
								"Request entity must be less than maximum of "
										+ this.maxBodyLength + " bytes");
					}

					// Determine initial buffer length
					long initialLength = Math.min(this.contentLength,
							this.initialBodyBufferLength);
					this.body = new UsAsciiStringBuilder((int) initialLength,
							(int) this.contentLength, bodyParseFactory);
				}

				// Have LF and headers valid, so continue onto body
				this.parseState = ParseState.BODY;
				break;

			case BODY:
				// Ensure provided Content-Length and created buffer
				if (this.body == null) {
					throw new ParseException(HttpStatus._411,
							"Must provide Content-Length header if sending entity");
				}

				// Append the body data
				this.body.append(character);
				break;
			}
		}

		// Validate content
		switch (this.validState) {
		case START:
			// Ensure method is valid
			if (this.parseState == ParseState.METHOD) {
				// Ensure content of method is valid so far
				if (!isPartialContentValid(this.method, HTTP_METHODS)) {
					throw new ParseException(HttpStatus._400,
							"Unknown method: " + this.method.toString() + "...");
				}

			} else if (this.parseState.ordinal() > ParseState.METHOD.ordinal()) {
				// Ensure valid method
				if (!isContentValid(this.method, HTTP_METHODS)) {
					throw new ParseException(HttpStatus._400,
							"Unknown method: " + this.method.toString());
				}

				// Method is valid
				this.validState = ParseState.METHOD;
			}
			// Continue on to possibly validate version

		case METHOD:
			// Ensure version is valid
			if (this.parseState == ParseState.VERSION) {
				// Ensure content of version is valid so far
				if (!isPartialContentValid(this.version, HTTP_VERSIONS)) {
					throw new ParseException(HttpStatus._400,
							"Unknown version: " + this.version.toString()
									+ "...");
				}

			} else if (this.parseState.ordinal() > ParseState.VERSION.ordinal()) {
				// Ensure valid version
				if (!isContentValid(this.version, HTTP_VERSIONS)) {
					throw new ParseException(HttpStatus._400,
							"Unknown version: " + this.version.toString());
				}

				// Version is valid
				this.validState = ParseState.VERSION;
			}
		}

		// Only able to be complete if parsing the body
		if (this.parseState != ParseState.BODY) {
			return false;
		}

		// Ensure all of the body is received
		if (this.contentLength > 0) {
			if (this.body.getCharacterCount() < this.contentLength) {
				return false;
			}
		}

		// All of request received and parsed
		return true;
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
		// Return as the bytes input (body may not be provided)
		return (this.body == null ? new byte[0] : this.body.toUsAscii());
	}

}
