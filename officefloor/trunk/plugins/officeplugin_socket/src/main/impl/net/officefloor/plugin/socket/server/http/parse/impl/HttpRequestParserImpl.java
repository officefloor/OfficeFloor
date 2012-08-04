/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.stream.NioInputStream;
import net.officefloor.plugin.stream.impl.NioInputStreamImpl;

/**
 * Parser for a HTTP request.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestParserImpl implements HttpRequestParser {

	/*
	 * ASCII values.
	 */
	public static final Charset US_ASCII = Charset.forName("US-ASCII");

	private static byte UsAscii(char character) {
		return UsAscii(String.valueOf(character))[0];
	}

	private static byte[] UsAscii(String text) {
		return text.getBytes(US_ASCII);
	}

	private static final byte A = UsAscii('A');

	private static final byte F = UsAscii('F');

	private static final byte Z = UsAscii('Z');

	private static final byte a = UsAscii('a');

	private static final byte f = UsAscii('f');

	private static final byte z = UsAscii('z');

	private static final byte _0 = UsAscii('0');

	private static final byte _9 = UsAscii('9');

	private static final byte SP = UsAscii(' ');

	private static final byte HT = UsAscii('\t');

	private static final byte CR = UsAscii('\r');

	private static final byte LF = UsAscii('\n');

	private static final byte COLON = UsAscii(':');

	private static final byte PERCENTAGE = UsAscii('%');

	/**
	 * Header name for the Content-Length.
	 */
	private static final String HEADER_NAME_CONTENT_LENGTH = "CONTENT-LENGTH";

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
	 * Determines if character is a white space (space or tab).
	 * 
	 * @param character
	 *            ASCII character.
	 * @return <code>true</code> if {@link #SP} or {@link #HT}.
	 */
	private static boolean isWs(byte character) {
		return (character == SP) || (character == HT);
	}

	/**
	 * Determines if a character may be constituted in text.
	 * 
	 * @param character
	 *            ASCII character.
	 * @return <code>true</code> if valid character for text.
	 */
	private static boolean isText(byte character) {
		return (!isCtl(character)) || isWs(character);
	}

	/**
	 * Translates the escaped character to its non-escaped character value.
	 * 
	 * @param highBitsCharacter
	 *            High bits of the escaped character.
	 * @param lowBitsCharacter
	 *            Low bits of the escaped character.
	 * @return Non escaped character value.
	 * @throws HttpRequestParseException
	 *             If invalid hexidecimal character.
	 */
	private static byte translateEscapedCharacter(byte highBitsCharacter,
			byte lowBitsCharacter) throws HttpRequestParseException {

		// Translate to bits values
		byte highBits = translateEscapedHexCharacterToBits(highBitsCharacter);
		byte lowBits = translateEscapedHexCharacterToBits(lowBitsCharacter);

		// Return the byte character value
		byte character = (byte) ((highBits << 4) | lowBits);
		return character;
	}

	/**
	 * Translates the escaped character to its corresponding its corresponding
	 * hexidecimal bit value.
	 * 
	 * @param character
	 *            Hexidecimal character value to translate to bits.
	 * @return Bits for the hexidecimal character.
	 * @throws HttpRequestParseException
	 *             If invalid hexidecimal character.
	 */
	private static byte translateEscapedHexCharacterToBits(byte character)
			throws HttpRequestParseException {

		// Translate hexidecimal value
		int bits;
		if ((character >= _0) && (character <= _9)) {
			bits = character - _0;
		} else if ((character >= A) && (character <= F)) {
			bits = character - A + 0xA;
		} else if ((character >= a) && (character <= f)) {
			bits = character - a + 0xA;
		} else {
			// Unknown hexidecimal character
			throw new HttpRequestParseException(HttpStatus.SC_BAD_REQUEST,
					"Invalid escaped hexidecimal character '"
							+ ((char) character) + "'");
		}

		// Return the bits
		return (byte) bits;
	}

	/**
	 * Indicates the state of parsing.
	 */
	private static enum ParseState {
		START, METHOD, METHOD_PATH_SEPARATION, PATH, PATH_VERSION_SEPARATION, VERSION,

		HEADER_CR, HEADER_CR_NAME_SEPARATION, HEADER_NAME, HEADER_NAME_VALUE_SEPARATION, HEADER_VALUE,

		ENTITY_CR, ENTITY
	};

	/**
	 * Indicates state of escaped character from percentage.
	 */
	private static enum EscapedCharacterState {
		NOT_ESCAPED, HIGH_BITS, LOW_BITS
	}

	/**
	 * Maximum number of {@link HttpHeader} instances for a {@link HttpRequest}.
	 */
	private final int maxHeaderCount;

	/**
	 * Maximum length of the entity.
	 */
	private final long maxEntityLength;

	/**
	 * Next byte to parse index.
	 */
	private int nextByteToParseIndex;

	/**
	 * {@link ParseState} which starts with the HTTP method.
	 */
	private ParseState parseState;

	/**
	 * Content length value for request.
	 */
	private long contentLength;

	/**
	 * Maximum number of bytes per TEXT.
	 */
	private final byte[] textBuffer;

	/**
	 * Index to load next byte to TEXT buffer.
	 */
	private int nextTextIndex;

	/**
	 * State of escaping
	 */
	private EscapedCharacterState escapedCharacterState = EscapedCharacterState.NOT_ESCAPED;

	/**
	 * High bits of an escaped character.
	 */
	private byte escapedCharacterHighBits;

	/**
	 * Method.
	 */
	private String text_method;

	/**
	 * Request URI.
	 */
	private String text_path;

	/**
	 * HTTP version.
	 */
	private String text_version;

	/**
	 * Header name just parsed.
	 */
	private String text_headerName;

	/**
	 * Flag indicating if multiple line {@link HttpHeader} value.
	 */
	private boolean isMultipleLineHeaderValue;

	/**
	 * {@link HttpHeader} instances.
	 */
	private List<HttpHeader> headers;

	/**
	 * Entity.
	 */
	private NioInputStreamImpl entity;

	/**
	 * Initiate.
	 * 
	 * @param maxHeaderCount
	 *            Maximum number of {@link HttpHeader} instances for a
	 *            {@link HttpRequest}.
	 * @param maxTextLength
	 *            Maximum number of bytes per TEXT.
	 * @param maxEntityLength
	 *            Maximum length of the entity. Requests with entities greater
	 *            than this will fail parsing.
	 */
	public HttpRequestParserImpl(int maxHeaderCount, int maxTextLength,
			long maxEntityLength) {
		this.maxHeaderCount = maxHeaderCount;
		this.maxEntityLength = maxEntityLength;

		// Create the text buffer
		this.textBuffer = new byte[maxTextLength];

		// Initiate state
		this.reset();
	}

	/**
	 * Appends the character to the TEXT.
	 * 
	 * @param character
	 *            Character.
	 * @param httpErrorStatus
	 *            HTTP error status on length being too long.
	 * @param httpErrorMessage
	 *            HTTP error message on length being too long.
	 * @throws HttpRequestParseException
	 *             If TEXT is too long.
	 */
	private void appendCharacterToText(byte character, int httpErrorStatus,
			String httpErrorMessage) throws IOException,
			HttpRequestParseException {

		try {
			// Append the character
			this.textBuffer[this.nextTextIndex++] = character;

		} catch (ArrayIndexOutOfBoundsException ex) {
			// TEXT is too long
			throw new HttpRequestParseException(httpErrorStatus,
					httpErrorMessage);
		}
	}

	/**
	 * Obtains the TEXT as {@link String}.
	 * 
	 * @param isTrim
	 *            Indicates whether to trim the String.
	 * @return TEXT converted to {@link String}.
	 */
	private String getTextAsString(boolean isTrim) {

		// Obtain the string (removing surrounding white spacing)
		String text = new String(this.textBuffer, 0, this.nextTextIndex,
				US_ASCII);
		if (isTrim) {
			text = text.trim();
		}

		// Reset for next TEXT
		this.nextTextIndex = 0;

		// Return the TEXT as string
		return text;
	}

	/**
	 * Adds a {@link HttpHeader} and manages the {@link #parseState} for next
	 * character.
	 * 
	 * @param name
	 *            Name for {@link HttpHeader}.
	 * @param value
	 *            Value for {@link HttpHeader}.
	 * @param terminatingCharacter
	 *            Character terminating the {@link HttpHeader} value. Aids in
	 *            determining next {@link ParseState}.
	 * @throws HttpRequestParseException
	 *             If invalid {@link HttpRequest}.
	 */
	private void addHttpHeaderAndManageParseState(String name, String value,
			byte terminatingCharacter) throws HttpRequestParseException {

		// Determine if multiple line header value
		if (this.isMultipleLineHeaderValue) {

			// Determine if no value
			if (value.length() == 0) {
				// No multiple line value so consider blank separating line
				this.processHeader();
				this.parseState = (terminatingCharacter == CR ? ParseState.ENTITY_CR
						: ParseState.ENTITY);
				return;
			}

			// Remove last HTTP header and append value to re-add
			int numberOfHeaders = this.headers.size();
			if (numberOfHeaders == 0) {
				// Must have at least one HTTP header for multiple line value
				throw new HttpRequestParseException(HttpStatus.SC_BAD_REQUEST,
						"White spacing before first HTTP header");
			}
			HttpHeader header = this.headers.remove(numberOfHeaders - 1);
			name = header.getName();
			value = header.getValue() + " " + value; // multiple line value
		}

		// Must have header name
		if (name.length() == 0) {
			throw new HttpRequestParseException(HttpStatus.SC_BAD_REQUEST,
					"Missing header name");
		}

		// Add the header
		this.headers.add(new HttpHeaderImpl(name, value));

		// Reset for next header
		this.text_headerName = "";
		this.isMultipleLineHeaderValue = false;

		// Move to next state
		this.parseState = (terminatingCharacter == CR ? ParseState.HEADER_CR
				: ParseState.HEADER_CR_NAME_SEPARATION);
	}

	/**
	 * Processes the header to ensure correct and possibly obtain the
	 * <code>Content-Length</code> for the entity.
	 * 
	 * @throws HttpRequestParseException
	 *             If invalid header.
	 */
	private void processHeader() throws HttpRequestParseException {

		// Attempt to obtain the Content-Length
		String contentLengthValue = null;
		for (HttpHeader header : this.headers) {
			if (HEADER_NAME_CONTENT_LENGTH.equalsIgnoreCase(header.getName())) {
				contentLengthValue = header.getValue();
				break;
			}
		}

		// Ensure valid Content-Length
		if (contentLengthValue != null) {
			// Should always be able to convert to an integer
			try {
				this.contentLength = Long.parseLong(contentLengthValue);
			} catch (NumberFormatException ex) {
				throw new HttpRequestParseException(
						HttpStatus.SC_LENGTH_REQUIRED,
						"Content-Length header value must be an integer");
			}
		}

		// Ensure the Content-Length within limits
		if (this.contentLength > 0) {
			if (this.contentLength > this.maxEntityLength) {
				throw new HttpRequestParseException(
						HttpStatus.SC_REQUEST_ENTITY_TOO_LARGE,
						"Request entity must be less than maximum of "
								+ this.maxEntityLength + " bytes");
			}
		}

		// Ensure POST and PUT methods provided Content-Length
		if (("POST".equalsIgnoreCase(this.text_method))
				|| ("PUT".equalsIgnoreCase(this.text_method))) {
			if (this.contentLength < 0) {
				throw new HttpRequestParseException(
						HttpStatus.SC_LENGTH_REQUIRED,
						"Must provide Content-Length header for "
								+ this.text_method);
			}
		}
	}

	/*
	 * ====================== HttpRequestParser ===========================
	 */

	@Override
	public void reset() {
		this.parseState = ParseState.START;
		this.escapedCharacterState = EscapedCharacterState.NOT_ESCAPED;
		this.contentLength = -1;
		this.nextTextIndex = 0;
		this.text_method = "";
		this.text_path = "";
		this.text_version = "";
		this.text_headerName = "";
		this.isMultipleLineHeaderValue = false;
		// Allow reasonable number of headers
		this.headers = new ArrayList<HttpHeader>(16);

		// Once parsed, no further input so do not block writing by input
		this.entity = new NioInputStreamImpl(new Object());
	}

	@Override
	public int nextByteToParseIndex() {
		return this.nextByteToParseIndex;
	}

	@Override
	public boolean parse(byte[] data, int startIndex) throws IOException,
			HttpRequestParseException {

		// Flag next byte to parse
		this.nextByteToParseIndex = startIndex;

		// Determine if parsing head
		if (this.parseState != ParseState.ENTITY) {

			// Loop parsing available content up to entity
			NEXT_CHARACTER: for (; this.nextByteToParseIndex < data.length; this.nextByteToParseIndex++) {
				byte character = data[this.nextByteToParseIndex];

				// Determine if escaping
				boolean isEscaped = false;
				switch (this.escapedCharacterState) {
				case NOT_ESCAPED:
					// Determine if escape character
					if (character == PERCENTAGE) {
						// Escaping
						this.escapedCharacterState = EscapedCharacterState.HIGH_BITS;
						continue NEXT_CHARACTER; // loop to next character
					}
					break;
				case HIGH_BITS:
					// Record the high bits of escaped character
					this.escapedCharacterHighBits = character;
					this.escapedCharacterState = EscapedCharacterState.LOW_BITS;
					continue NEXT_CHARACTER; // loop for low bits
				case LOW_BITS:
					// Translate to non-escaped character (no longer escaped)
					character = translateEscapedCharacter(
							this.escapedCharacterHighBits, character);
					this.escapedCharacterState = EscapedCharacterState.NOT_ESCAPED;
					isEscaped = true; // character is escaped
					break;
				default:
					throw new IllegalStateException(
							"Unknown escaped character state: "
									+ this.escapedCharacterState);
				}

				// Parse the character of the HTTP request
				switch (this.parseState) {
				case START:
					// Ignore leading white space and blank lines
					if ((character == CR) || (character == LF)
							|| isWs(character)) {
						// Skip over white space or end of line
						break;
					}
					this.parseState = ParseState.METHOD;

				case METHOD:
					if (isAlpha(character)) {
						// Append character of the method
						this.appendCharacterToText(character,
								HttpStatus.SC_BAD_REQUEST, "Method too long");
					} else if (isWs(character)) {
						// Method name read in
						this.text_method = this.getTextAsString(true);

						// Skip white space and move to path separation
						this.parseState = ParseState.METHOD_PATH_SEPARATION;
					} else {
						// Unexpected character
						throw new HttpRequestParseException(
								HttpStatus.SC_BAD_REQUEST,
								"Unexpected character in method '" + character
										+ "'");
					}
					break;

				case METHOD_PATH_SEPARATION:
					// Ignore separating linear white space
					if (isWs(character)) {
						// Skip over white space
						break;
					}
					this.parseState = ParseState.PATH;

				case PATH:
					if (isWs(character) && (!isEscaped)) {
						// Path read in
						this.text_path = this.getTextAsString(false);

						// Skip white space and move to version separation
						this.parseState = ParseState.PATH_VERSION_SEPARATION;
					} else if (!isCtl(character)) {
						// Append character to path
						this.appendCharacterToText(character,
								HttpStatus.SC_REQUEST_URI_TOO_LARGE,
								"Request-URI Too Long");
					} else {
						// Unexpected character
						throw new HttpRequestParseException(
								HttpStatus.SC_BAD_REQUEST,
								"Unexpected character in path '" + character
										+ "'");
					}
					break;

				case PATH_VERSION_SEPARATION:
					// Ignore separating linear white space
					if (isWs(character)) {
						// Skip over white space
						break;
					}
					this.parseState = ParseState.VERSION;

				case VERSION:
					if ((character == CR) || (character == LF)) {
						// Version read in
						this.text_version = this.getTextAsString(true);

						// Skip CR / LF and move to header
						this.parseState = (character == CR ? ParseState.HEADER_CR
								: ParseState.HEADER_CR_NAME_SEPARATION);
					} else {
						// Append character to version
						this.appendCharacterToText(character,
								HttpStatus.SC_BAD_REQUEST, "Version too long");
					}
					break;

				case HEADER_CR:
					if (character == LF) {
						// Skip over LF, continue on with header
						this.parseState = ParseState.HEADER_CR_NAME_SEPARATION;
						break;
					}
					throw new HttpRequestParseException(
							HttpStatus.SC_BAD_REQUEST,
							"Should expect LF after a CR for status line");

				case HEADER_CR_NAME_SEPARATION:
					if (character == CR) {
						// Skip CR and move to entity
						this.parseState = ParseState.ENTITY_CR;
						break;
					} else if (character == LF) {
						// Tolerant request so process header
						this.processHeader();

						// Skip LF and move to entity
						this.parseState = ParseState.ENTITY;
						break;
					} else if (isWs(character)) {
						// Continue previous header value.
						// Flag that potentially multiple line value.
						this.isMultipleLineHeaderValue = true;

						// Continue parsing header value
						this.parseState = ParseState.HEADER_NAME_VALUE_SEPARATION;
						break;
					} else {
						// New header, determine if too many headers
						if (this.headers.size() >= this.maxHeaderCount) {
							throw new HttpRequestParseException(
									HttpStatus.SC_BAD_REQUEST,
									"Too Many Headers");
						}
						this.parseState = ParseState.HEADER_NAME;
					}

				case HEADER_NAME:
					if ((character == COLON) && (!isEscaped)) {
						// Header name obtained
						this.text_headerName = this.getTextAsString(true);

						// Skip colon and move to name value separation
						this.parseState = ParseState.HEADER_NAME_VALUE_SEPARATION;
					} else if ((character == CR) || (character == LF)) {
						// Obtain the header name
						this.text_headerName = this.getTextAsString(true);

						// Skip CR/LF.
						// Provide blank header value and move to next header.
						this.addHttpHeaderAndManageParseState(
								this.text_headerName, "", character);
					} else if (isText(character)) {
						// Append the header name character
						this.appendCharacterToText(character,
								HttpStatus.SC_BAD_REQUEST,
								"Header name too long");
					} else {
						// Unknown header name character
						throw new HttpRequestParseException(
								HttpStatus.SC_BAD_REQUEST,
								"Unknown header name character '" + character
										+ "'");
					}
					break;

				case HEADER_NAME_VALUE_SEPARATION:
					// Ignore separating linear white space
					if (isWs(character)) {
						// Skip the white space
						break;
					}
					this.parseState = ParseState.HEADER_VALUE;

				case HEADER_VALUE:
					if ((character == CR) || (character == LF)) {
						// Header name and value obtained
						String headerValue = this.getTextAsString(true);

						// Skip CR/LF, add header and move to next header
						this.addHttpHeaderAndManageParseState(
								this.text_headerName, headerValue, character);
					} else if (isText(character)) {
						// Append the header value character
						this.appendCharacterToText(character,
								HttpStatus.SC_BAD_REQUEST,
								"Header value too long");
					} else {
						// Unknown header value character
						throw new HttpRequestParseException(
								HttpStatus.SC_BAD_REQUEST,
								"Unknown header value character '" + character
										+ "'");
					}
					break;

				case ENTITY_CR:
					// Must have LF after CR for entity
					if (character != LF) {
						throw new HttpRequestParseException(
								HttpStatus.SC_BAD_REQUEST,
								"Should expect LR after a CR after header");
					}

					// Skip the LF and process the header
					this.processHeader();

					// Have LF and headers valid, so continue onto entity
					this.parseState = ParseState.ENTITY;
					break;

				case ENTITY:
					// Break loop to start entity
					break NEXT_CHARACTER;

				default:
					throw new IllegalStateException("Unknown parse state: "
							+ this.parseState);
				}
			}
		}

		// Ensure parsing entity
		if (this.parseState != ParseState.ENTITY) {
			// All bytes consumed and not yet complete HTTP request
			this.nextByteToParseIndex = -1;
			return false;
		}

		// Ensure all of the entity is received
		if (this.contentLength > 0) {

			// Determine remaining bytes
			int remainingBytes = data.length - this.nextByteToParseIndex;

			// Determine the number of bytes required for entity
			long requiredBytes = this.contentLength - this.entity.available();

			// Determine if further data required
			boolean isFurtherDataRequired = (requiredBytes > remainingBytes);

			// Determine if consume partial of remaining bytes
			if (remainingBytes > requiredBytes) {
				// Consume partial of the bytes (-1 as index not length)
				int endIndex = this.nextByteToParseIndex
						+ ((int) requiredBytes) - 1;
				this.entity.queueData(data, this.nextByteToParseIndex,
						endIndex, false);
				this.nextByteToParseIndex = endIndex + 1; // after bytes read

			} else {
				// Consume all of the bytes
				this.entity.queueData(data, this.nextByteToParseIndex,
						(data.length - 1), !isFurtherDataRequired);
				this.nextByteToParseIndex = -1; // all bytes consumed
			}

			// Indicate if all of request received
			return !isFurtherDataRequired;

		} else {
			// No content (indicate no further data)
			this.entity.queueData(null, 0, 0, false);

			// Determine if consumed all bytes
			if (this.nextByteToParseIndex == data.length) {
				this.nextByteToParseIndex = -1; // all bytes consumed
			}
		}

		// All of request received and parsed
		return true;
	}

	@Override
	public String getMethod() {
		return this.text_method;
	}

	@Override
	public String getRequestURI() {
		return this.text_path;
	}

	@Override
	public String getHttpVersion() {
		return this.text_version;
	}

	@Override
	public List<HttpHeader> getHeaders() {
		return this.headers;
	}

	@Override
	public NioInputStream getEntity() {
		return this.entity;
	}

}