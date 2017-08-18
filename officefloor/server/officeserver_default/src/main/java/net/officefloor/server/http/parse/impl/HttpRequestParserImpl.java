/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http.parse.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.impl.HttpEntityImpl;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;

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

	private static final byte Z = UsAscii('Z');

	private static final byte a = UsAscii('a');

	private static final byte z = UsAscii('z');

	private static final byte SP = UsAscii(' ');

	private static final byte HT = UsAscii('\t');

	private static final byte CR = UsAscii('\r');

	private static final byte LF = UsAscii('\n');

	private static final byte COLON = UsAscii(':');

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
		return ((character >= A) && (character <= Z)) || ((character >= a) && (character <= z));
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
		// Text is not control characters (except the tab control character)
		return (!isCtl(character)) || isWs(character);
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
	 * Maximum number of {@link HttpHeader} instances for a {@link HttpRequest}.
	 */
	private final int maxHeaderCount;

	/**
	 * Maximum length of the entity.
	 */
	private final long maxEntityLength;

	/**
	 * Current {@link StreamBuffer}.
	 */
	private StreamBuffer<ByteBuffer> currentStreamBuffer = null;

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
	 * {@link ServerInputStreamImpl}.
	 */
	private ServerInputStreamImpl entityStream;

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
	public HttpRequestParserImpl(int maxHeaderCount, int maxTextLength, long maxEntityLength) {
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
	 * @param errorHttpStatus
	 *            {@link HttpStatus} status on length being too long.
	 * @throws HttpRequestParseException
	 *             If TEXT is too long.
	 */
	private void appendCharacterToText(byte character, HttpStatus errorHttpStatus)
			throws IOException, HttpRequestParseException {

		try {
			// Append the character
			this.textBuffer[this.nextTextIndex++] = character;

		} catch (ArrayIndexOutOfBoundsException ex) {
			// TEXT is too long
			throw new HttpRequestParseException(errorHttpStatus);
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
		String text = new String(this.textBuffer, 0, this.nextTextIndex, US_ASCII);
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
	private void addHttpHeaderAndManageParseState(String name, String value, byte terminatingCharacter)
			throws HttpRequestParseException {

		// Determine if multiple line header value
		if (this.isMultipleLineHeaderValue) {

			// Determine if no value
			if (value.length() == 0) {
				// No multiple line value so consider blank separating line
				this.processHeader();
				this.parseState = (terminatingCharacter == CR ? ParseState.ENTITY_CR : ParseState.ENTITY);
				return;
			}

			// Remove last HTTP header and append value to re-add
			int numberOfHeaders = this.headers.size();
			if (numberOfHeaders == 0) {
				// Must have at least one HTTP header for multiple line value
				throw new HttpRequestParseException(WHITE_SPACING_BEFORE_FIRST_HTTP_HEADER);
			}
			HttpHeader header = this.headers.remove(numberOfHeaders - 1);
			name = header.getName();
			value = header.getValue() + " " + value; // multiple line value
		}

		// Must have header name
		if (name.length() == 0) {
			throw new HttpRequestParseException(MISSING_HEADER_NAME);
		}

		// Add the header
		this.headers.add(new SerialisableHttpHeader(name, value));

		// Reset for next header
		this.text_headerName = "";
		this.isMultipleLineHeaderValue = false;

		// Move to next state
		this.parseState = (terminatingCharacter == CR ? ParseState.HEADER_CR : ParseState.HEADER_CR_NAME_SEPARATION);
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
				throw new HttpRequestParseException(CONTENT_LENGTH_HEADER_VALUE_MUST_BE_AN_INTEGER);
			}
		}

		// Ensure the Content-Length within limits
		if (this.contentLength > 0) {
			if (this.contentLength > this.maxEntityLength) {
				throw new HttpRequestParseException(new HttpStatus(HttpStatus.REQUEST_ENTITY_TOO_LARGE.getStatusCode(),
						"Request entity must be less than maximum of " + this.maxEntityLength + " bytes"));
			}
		}

		// Ensure POST and PUT methods provided Content-Length
		if (("POST".equalsIgnoreCase(this.text_method)) || ("PUT".equalsIgnoreCase(this.text_method))) {
			if (this.contentLength < 0) {
				throw new HttpRequestParseException(new HttpStatus(HttpStatus.LENGTH_REQUIRED.getStatusCode(),
						"Must provide Content-Length header for " + this.text_method));
			}
		}
	}

	/*
	 * ====================== HttpRequestParser ===========================
	 */

	@Override
	public void reset() {
		this.parseState = ParseState.START;
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
		this.entityStream = new ServerInputStreamImpl(new Object());
	}

	@Override
	public boolean isFinishedReadingBuffer() {
		return this.nextByteToParseIndex >= this.currentStreamBuffer.getPooledBuffer().position();
	}

	private static final HttpStatus WHITE_SPACING_BEFORE_FIRST_HTTP_HEADER = new HttpStatus(
			HttpStatus.BAD_REQUEST.getStatusCode(), "White spacing before first HTTP header");
	private static final HttpStatus MISSING_HEADER_NAME = new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
			"Missing header name");
	private static final HttpStatus CONTENT_LENGTH_HEADER_VALUE_MUST_BE_AN_INTEGER = new HttpStatus(
			HttpStatus.LENGTH_REQUIRED.getStatusCode(), "Content-Length header value must be an integer");
	private static final HttpStatus METHOD_TOO_LONG = new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
			"Method too long");
	private static final HttpStatus VERSION_TOO_LONG = new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
			"Version too long");
	private static final HttpStatus SHOULD_EXPECT_LF_AFTER_CR_FOR_STATUS_LINE = new HttpStatus(
			HttpStatus.BAD_REQUEST.getStatusCode(), "Should expect LF after a CR for status line");
	private static final HttpStatus TOO_MANY_HEADERS = new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
			"Too Many Headers");
	private static final HttpStatus HEADER_NAME_TOO_LONG = new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
			"Header name too long");
	private static final HttpStatus HEADER_VALUE_TOO_LONG = new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
			"Header value too long");
	private static final HttpStatus SHOULD_EXPECT_LF_AFTER_CR_AFTER_HEADER = new HttpStatus(
			HttpStatus.BAD_REQUEST.getStatusCode(), "Should expect LR after a CR after header");

	@Override
	public boolean parse(StreamBuffer<ByteBuffer> streamBuffer) throws IOException, HttpRequestParseException {

		// Determine if still reading from same buffer (or new buffer)
		if (streamBuffer != this.currentStreamBuffer) {
			// Now current buffer
			this.currentStreamBuffer = streamBuffer;
			this.nextByteToParseIndex = 0;
		}

		// Obtain the data to read
		ByteBuffer data = streamBuffer.getPooledBuffer();

		// Determine if parsing head
		if (this.parseState != ParseState.ENTITY) {

			// Loop parsing available content up to entity
			NEXT_CHARACTER: while (this.nextByteToParseIndex < data.position()) {
				byte character = data.get(this.nextByteToParseIndex);

				// Parse the character of the HTTP request
				switch (this.parseState) {
				case START:
					// Ignore leading white space and blank lines
					if ((character == CR) || (character == LF) || isWs(character)) {
						// Skip over white space or end of line
						break;
					}
					this.parseState = ParseState.METHOD;

				case METHOD:
					if (isAlpha(character)) {
						// Append character of the method
						this.appendCharacterToText(character, METHOD_TOO_LONG);
					} else if (isWs(character)) {
						// Method name read in
						this.text_method = this.getTextAsString(true);

						// Skip white space and move to path separation
						this.parseState = ParseState.METHOD_PATH_SEPARATION;
					} else {
						// Unexpected character
						throw new HttpRequestParseException(new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
								"Unexpected character in method '" + character + "'"));
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
					if (isWs(character)) {
						// Path read in
						this.text_path = this.getTextAsString(false);

						// Skip white space and move to version separation
						this.parseState = ParseState.PATH_VERSION_SEPARATION;
					} else if (!isCtl(character)) {
						// Append character to path
						this.appendCharacterToText(character, HttpStatus.REQUEST_URI_TOO_LARGE);
					} else {
						// Unexpected character
						throw new HttpRequestParseException(new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
								"Unexpected character in path '" + character + "'"));
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
						this.appendCharacterToText(character, VERSION_TOO_LONG);
					}
					break;

				case HEADER_CR:
					if (character == LF) {
						// Skip over LF, continue on with header
						this.parseState = ParseState.HEADER_CR_NAME_SEPARATION;
						break;
					}
					throw new HttpRequestParseException(SHOULD_EXPECT_LF_AFTER_CR_FOR_STATUS_LINE);

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
							throw new HttpRequestParseException(TOO_MANY_HEADERS);
						}
						this.parseState = ParseState.HEADER_NAME;
					}

				case HEADER_NAME:
					if (character == COLON) {
						// Header name obtained
						this.text_headerName = this.getTextAsString(true);

						// Skip colon and move to name value separation
						this.parseState = ParseState.HEADER_NAME_VALUE_SEPARATION;
					} else if ((character == CR) || (character == LF)) {
						// Obtain the header name
						this.text_headerName = this.getTextAsString(true);

						// Skip CR/LF.
						// Provide blank header value and move to next header.
						this.addHttpHeaderAndManageParseState(this.text_headerName, "", character);
					} else if (isText(character)) {
						// Append the header name character
						this.appendCharacterToText(character, HEADER_NAME_TOO_LONG);
					} else {
						// Unknown header name character
						throw new HttpRequestParseException(new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
								"Unknown header name character '" + character + "'"));
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
						this.addHttpHeaderAndManageParseState(this.text_headerName, headerValue, character);
					} else if (isText(character)) {
						// Append the header value character
						this.appendCharacterToText(character, HEADER_VALUE_TOO_LONG);
					} else {
						// Unknown header value character
						throw new HttpRequestParseException(new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
								"Unknown header value character '" + character + "'"));
					}
					break;

				case ENTITY_CR:
					// Must have LF after CR for entity
					if (character != LF) {
						throw new HttpRequestParseException(SHOULD_EXPECT_LF_AFTER_CR_AFTER_HEADER);
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
					throw new IllegalStateException("Unknown parse state: " + this.parseState);
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
			int remainingBytes = data.position() - this.nextByteToParseIndex;

			// Determine the number of bytes required for entity
			long requiredBytes = this.contentLength - this.entityStream.available();

			// Determine if further data required
			boolean isFurtherDataRequired = (requiredBytes > remainingBytes);

			// Determine if consume partial of remaining bytes
			if (remainingBytes > requiredBytes) {
				// Consume partial of the bytes (-1 as index not length)
				int endIndex = this.nextByteToParseIndex + ((int) requiredBytes) - 1;
				this.entityStream.inputData(data, this.nextByteToParseIndex, endIndex, false);
				this.nextByteToParseIndex = endIndex + 1; // after bytes read

			} else {
				// Consume all of the bytes
				this.entityStream.inputData(data, this.nextByteToParseIndex, (data.position() - 1),
						isFurtherDataRequired);
				this.nextByteToParseIndex = -1; // all bytes consumed
			}

			// Indicate if all of request received
			return !isFurtherDataRequired;

		} else {
			// No content (indicate no further data)
			this.entityStream.inputData(null, 0, 0, false);

			// Determine if consumed all bytes
			if (this.nextByteToParseIndex == data.position()) {
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
	public String getVersion() {
		return this.text_version;
	}

	@Override
	public List<HttpHeader> getHeaders() {
		return this.headers;
	}

	@Override
	public HttpEntity getEntity() {
		return new HttpEntityImpl(this.entityStream);
	}

}