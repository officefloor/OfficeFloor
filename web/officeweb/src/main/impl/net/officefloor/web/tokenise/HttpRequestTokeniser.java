/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.tokenise;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpArgumentParser;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.escalation.BadRequestHttpException;
import net.officefloor.web.value.load.ValueLoader;

/**
 * Tokenises the {@link HttpRequest} for the path, parameters, fragment.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestTokeniser {

	/**
	 * {@link ThreadLocal} for the {@link ParseState}.
	 */
	private static ThreadLocal<ParseState> parseState = new ThreadLocal<ParseState>() {
		@Override
		protected ParseState initialValue() {
			return new ParseState();
		}
	};

	/**
	 * State of parsing.
	 */
	private static class ParseState implements CharSequence {

		/**
		 * Buffer with reasonable default size.
		 */
		private char[] buffer = new char[256];

		/**
		 * Length of {@link CharSequence}.
		 */
		private int length = 0;

		/*
		 * ============= CharSequence ================
		 */

		@Override
		public int length() {
			return this.length;
		}

		@Override
		public char charAt(int index) {
			return this.buffer[index];
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			throw new UnsupportedOperationException("Should not sub sequence " + this.getClass().getName());
		}

		@Override
		public String toString() {
			return new String(this.buffer, 0, this.length);
		}
	}

	/**
	 * Tokenises the {@link HttpRequest} for the arguments to the
	 * {@link ValueLoader}.
	 * 
	 * @param request
	 *            {@link HttpRequest} to be tokenised.
	 * @param valueLoader
	 *            {@link ValueLoader}.
	 * @param argumentParsers
	 *            {@link HttpArgumentParser} instances.
	 * @throws HttpException
	 *             If fails to tokenise the {@link HttpRequest}.
	 */
	public static void tokeniseHttpRequest(HttpRequest request, HttpArgumentParser[] argumentParsers,
			ValueLoader valueLoader) throws HttpException {

		// Obtain the parse state (ready for use)
		ParseState state = parseState.get();

		// Load the query string arguments
		String requestUri = request.getUri();

		// Values to aid in parsing
		boolean isPathProcessed = false;
		int begin = 0; // start of contents
		int end = -1;
		String name = null;
		String value = null;
		boolean isRequireDecode = false;

		// Iterate over the contents, loading the parameters
		PARSING: for (int i = 0; i < requestUri.length(); i++) {
			char character = requestUri.charAt(i);

			// Handle based on character
			switch (character) {

			case '?':
				// If not processing path then just include
				if (!isPathProcessed) {
					// No longer processing path
					isPathProcessed = true;
					begin = i + 1; // after '?'
				}
				break;

			case '=':
				// Flag to now obtain value
				end = i; // before '='
				name = decode(requestUri.substring(begin, end), isRequireDecode, state);
				begin = i + 1; // after '='
				end = -1;
				break;

			case '+': // space
			case '%': // escaping
				// Requires translating
				isRequireDecode = true;
				break;

			case '&':
			case ';':
				// Have parameter name/value, so load
				end = i; // before terminator
				value = decode(requestUri.substring(begin, end), isRequireDecode, state);
				valueLoader.loadValue(name, value, HttpValueLocation.QUERY);
				name = null;

				// Reset for next parameter name/value
				begin = i + 1; // after terminator
				end = -1;
				isRequireDecode = false;
				break;

			case '#':
				// Determine previous (path/parameter)
				if (isPathProcessed) {
					// At end of parameters as have fragment
					end = i; // before '#'
					break PARSING;
				}
			}
		}

		// Determine if load final value
		if ((name != null) && (begin > 0)) {

			// Obtain the value
			if (end == -1) {
				end = requestUri.length();
			}
			value = decode(requestUri.substring(begin, end), isRequireDecode, state);

			// Load the value
			valueLoader.loadValue(name, value, HttpValueLocation.QUERY);
		}

		// Load the header arguments (and determine content type)
		String contentType = null;
		for (HttpHeader header : request.getHeaders()) {
			name = header.getName();
			value = header.getValue();

			// Load the header value
			valueLoader.loadValue(name, value, HttpValueLocation.HEADER);

			// Handle specific header values
			if ("content-type".equalsIgnoreCase(name)) {
				// Capture content type, for later parsing content
				contentType = header.getValue();
			}
		}

		// Load content arguments
		if ((contentType != null) && (argumentParsers != null)) {
			for (int i = 0; i < argumentParsers.length; i++) {
				HttpArgumentParser parser = argumentParsers[i];
				if (contentType.equals(parser.getContentType())) {
					parser.parse(request, valueLoader);
					return; // use only first matching
				}
			}
		}
	}

	/**
	 * Tokenises the <code>application/x-www-form-urlencoded</code> entity.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @param valueLoader
	 *            {@link ValueLoader}.
	 * @throws HttpException
	 *             If fails to tokenise the form content.
	 */
	public static void tokeniseFormEntity(HttpRequest request, ValueLoader valueLoader) throws HttpException {

		// Obtain the parse state (ready for use)
		ParseState state = parseState.get();

		// Values to aid in parsing
		String name = null;
		String value = null;
		boolean isRequireDecode = false;

		// Parse the name / value pairs
		int index = 0;
		try {
			Reader reader = new InputStreamReader(request.getEntity().createBrowseInputStream(),
					ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
			for (int character = reader.read(); character != -1; character = reader.read()) {

				// Load the value (ensuring enough space for value)
				if (index >= state.buffer.length) {
					state.buffer = Arrays.copyOf(state.buffer, state.buffer.length * 2);
				}
				state.buffer[index] = (char) character;

				// Handle based on character
				switch (character) {

				case '=':
					// Flag to now obtain value
					state.length = index; // before '='
					name = decode(state, isRequireDecode, state);
					index = -1; // reset to start
					break;

				case '+': // space
				case '%': // escaping
					// Requires translating
					isRequireDecode = true;
					break;

				case '&':
				case ';':
					// Have parameter name/value, so load
					state.length = index; // before terminator
					value = decode(state, isRequireDecode, state);
					valueLoader.loadValue(name, value, HttpValueLocation.ENTITY);
					name = null;

					// Reset for next parameter name/value
					index = -1; // reset to start
					isRequireDecode = false;
					break;
				}

				// Increment for next index
				index++;
			}
		} catch (IOException ex) {
			throw new HttpException(ex);
		}

		// Determine if load final value
		if (name != null) {

			// Ensure have value
			state.length = index;
			value = decode(state, isRequireDecode, state);

			// Load the value
			valueLoader.loadValue(name, value, HttpValueLocation.ENTITY);
		}
	}

	/**
	 * Enum providing the escape state for translating.
	 */
	private static enum EscapeState {
		NONE, HIGH, LOW
	}

	/**
	 * Decodes the text.
	 * 
	 * @param text
	 *            Text to be decoded.
	 * @param state
	 *            {@link ParseState}.
	 * @return Decoded text.
	 * @throws HttpException
	 *             If fails to translate.
	 */
	private static String decode(CharSequence text, boolean isRequireDecode, ParseState state) throws HttpException {

		// Determine if require decode
		if (!isRequireDecode) {
			// No decode required
			return text.toString();
		}

		// Obtain the temporary buffer
		char[] buffer = state.buffer;

		// Ensure temporary buffer large enough
		if ((buffer == null) || (buffer.length < text.length())) {
			// Increase buffer size (translation should not be bigger)
			buffer = new char[text.length()];

			// Make available for further translations
			state.buffer = buffer;
		}

		// Iterate over parameter text translating
		int charIndex = 0;
		EscapeState escape = EscapeState.NONE;
		byte highBits = 0;
		for (int i = 0; i < text.length(); i++) {
			char character = text.charAt(i);

			// Handle on whether escaping
			switch (escape) {
			case NONE:
				// Not escaped so handle character
				switch (character) {
				case '+':
					// Translate to space
					buffer[charIndex++] = ' ';
					break;

				case '%':
					// Escaping
					escape = EscapeState.HIGH;
					break;

				default:
					// No translation needed of character
					buffer[charIndex++] = character;
					break;
				}
				break;

			case HIGH:
				// Obtain the high bits for escaping
				highBits = decodeEscapedCharToBits(character);
				escape = EscapeState.LOW;
				break;

			case LOW:
				// Have low bits, so obtain escaped character
				byte lowBits = decodeEscapedCharToBits(character);
				character = (char) ((highBits << 4) | lowBits);

				// Load the character and no longer escaped
				buffer[charIndex++] = character;
				escape = EscapeState.NONE;
				break;
			}
		}

		// Should always be in non-escape state after translating
		if (escape != EscapeState.NONE) {
			throw new BadRequestHttpException(null,
					"Invalid parameter text as escaping not complete: '" + text.toString() + "'");
		}

		// Return the translated text
		return new String(buffer, 0, charIndex);
	}

	/**
	 * Decode the character to the 4 bits as per escaping of HTTP.
	 * 
	 * @param character
	 *            Character to translate.
	 * @return Corresponding 4 bits for character.
	 * @throws HttpException
	 *             If invalid character for escaping.
	 */
	private static byte decodeEscapedCharToBits(char character) throws HttpException {

		// Obtain the bits for the character
		int bits;
		if (('0' <= character) && (character <= '9')) {
			bits = character - '0';
		} else if (('A' <= character) && (character <= 'F')) {
			bits = (character - 'A') + 0xA;
		} else if (('a' <= character) && (character <= 'f')) {
			bits = (character - 'a') + 0xA;
		} else {
			// Invalid character for escaping
			throw new BadRequestHttpException(null, "Invalid character for escaping: " + character);
		}

		// Return the bits
		return (byte) bits;
	}

	/**
	 * All access via static methods.
	 */
	private HttpRequestTokeniser() {
	}

}
