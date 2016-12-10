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
package net.officefloor.plugin.web.http.tokenise;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.stream.BrowseInputStream;
import net.officefloor.plugin.stream.ServerInputStream;

/**
 * {@link HttpRequestTokeniser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestTokeniserImpl implements HttpRequestTokeniser {

	/**
	 * <p>
	 * Convenience method for extracting the parameters from the
	 * {@link HttpRequest}.
	 * <p>
	 * Only the first value for the parameter will be returned.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @throws IOException
	 *             If fails to read data from {@link HttpRequest}.
	 * @return Parameter name values.
	 * @throws HttpRequestTokeniseException
	 *             If fails to extract the parameters.
	 */
	public static Map<String, String> extractParameters(HttpRequest request)
			throws IOException, HttpRequestTokeniseException {

		// Extract the parameters
		final Map<String, String> parameters = new HashMap<String, String>();
		new HttpRequestTokeniserImpl().tokeniseHttpRequest(request,
				new HttpRequestTokenHandler() {

					@Override
					public void handlePath(String path)
							throws HttpRequestTokeniseException {
						// Ignore
					}

					@Override
					public void handleHttpParameter(String name, String value)
							throws HttpRequestTokeniseException {
						// Only add the first value for parameter
						if (!parameters.containsKey(name)) {
							parameters.put(name, value);
						}
					}

					@Override
					public void handleQueryString(String queryString)
							throws HttpRequestTokeniseException {
						// Ignore
					}

					@Override
					public void handleFragment(String fragment)
							throws HttpRequestTokeniseException {
						// Ignore
					}
				});

		// Return the parameters
		return parameters;
	}

	/*
	 * ====================== HttpRequestTokeniser ============================
	 */

	@Override
	public void tokeniseHttpRequest(HttpRequest request,
			HttpRequestTokenHandler handler) throws IOException,
			HttpRequestTokeniseException {

		// Create the temporary buffer (aids reducing object creation)
		TempBuffer tempBuffer = new TempBuffer();

		// Always load the tokens from the request URI
		String requestUri = request.getRequestURI();
		this.loadTokens(requestUri, false, handler, tempBuffer);

		// Only load parameters of entity if a POST
		if ("POST".equalsIgnoreCase(request.getMethod())) {

			// Obtain the content encoding of the entity
			// TODO handle content encoding

			// Obtain the content type of the entity
			// TODO handle content type
			Charset charset = Charset.forName("UTF-8"); // default for now

			// Obtain the entity data
			ServerInputStream entity = request.getEntity();
			byte[] data;
			try {
				// Create buffer for data
				int bodySize = (int) entity.available();
				data = new byte[bodySize < 0 ? 0 : bodySize];

				// Obtain the data
				if (data.length > 0) {
					BrowseInputStream browseStream = entity
							.createBrowseInputStream();
					browseStream.read(data);
				}

			} catch (IOException ex) {
				// Propagate failure
				throw new HttpRequestTokeniseException(ex);
			}

			// Obtain the body data as string
			String bodyText = new String(data, charset);

			// Load the parameters from the body
			this.loadTokens(bodyText, true, handler, tempBuffer);
		}
	}

	@Override
	public void tokeniseRequestURI(String requestURI,
			HttpRequestTokenHandler handler)
			throws HttpRequestTokeniseException {

		// Create the temporary buffer (aids reducing object creation)
		TempBuffer tempBuffer = new TempBuffer();

		// Load the tokens
		this.loadTokens(requestURI, false, handler, tempBuffer);
	}

	/**
	 * Loads the tokens to the handler.
	 * 
	 * @param contents
	 *            Contents containing the parameter name/values to be parsed.
	 * @param isOnlyParameters
	 *            Flags to only load parameters.
	 * @param handler
	 *            {@link HttpRequestTokenHandler}.
	 * @param tempBuffer
	 *            {@link TempBuffer}.
	 * @throws HttpRequestTokeniseException
	 *             If fails to parse the parameters.
	 */
	private void loadTokens(String contents, boolean isOnlyParameters,
			HttpRequestTokenHandler handler, TempBuffer tempBuffer)
			throws HttpRequestTokeniseException {

		// The implementation of this method reduces character array creations
		// and copying by using sub strings. This should both improve parsing
		// performance and reduce memory.

		// Values to aid in parsing
		boolean isPathProcessed = isOnlyParameters;
		int nameBegin = 0; // start of contents
		int nameEnd = -1;
		int valueBegin = -1;
		int valueEnd = -1;
		int queryBegin = -1;
		int queryEnd = -1;
		boolean isRequireTranslate = false;

		// Iterate over the contents, loading the parameters
		for (int i = 0; i < contents.length(); i++) {
			char character = contents.charAt(i);

			// Handle based on character
			switch (character) {

			case '?':
				// If not processing path then just include
				if (!isPathProcessed) {
					// Load the path
					nameEnd = i; // before '?'
					this.loadPath(contents, nameBegin, nameEnd,
							isRequireTranslate, handler, tempBuffer);

					// No longer processing path
					isPathProcessed = true;
					nameBegin = i + 1; // after '?'
					queryBegin = nameBegin; // start of first parameter
				}
				break;

			case '=':
				// Flag to now obtain value
				nameEnd = i; // before '='
				valueBegin = i + 1; // after '='
				break;

			case '+': // space
			case '%': // escaping
				// Requires translating
				isRequireTranslate = true;
				break;

			case '&':
			case ';':
				// Have parameter name/value, so load
				valueEnd = i; // before terminator
				this.loadParameter(contents, nameBegin, nameEnd, valueBegin,
						valueEnd, isRequireTranslate, handler, tempBuffer);

				// Reset for next parameter name/value
				nameBegin = i + 1; // after terminator
				nameEnd = -1;
				valueBegin = -1;
				valueEnd = -1;
				isRequireTranslate = false;
				break;

			case '#':
				// Determine previous (path/parameter)
				if (!isPathProcessed) {
					// Load path
					nameEnd = i; // before '#'
					this.loadPath(contents, nameBegin, nameEnd,
							isRequireTranslate, handler, tempBuffer);

				} else {
					// At end of parameters as have fragment
					valueEnd = i; // before '#'
					if (valueBegin > 0) {
						// Have name/value before fragment so load
						this.loadParameter(contents, nameBegin, nameEnd,
								valueBegin, valueEnd, isRequireTranslate,
								handler, tempBuffer);

						// Load the query string
						queryEnd = valueEnd; // end of last parameter value
						this.loadQueryString(contents, queryBegin, queryEnd,
								isOnlyParameters, handler);
					}
				}

				// Load the fragment
				nameBegin = i + 1; // after '#'
				String fragment = contents.substring(nameBegin);
				fragment = this.translate(fragment, tempBuffer);
				handler.handleFragment(fragment);

				// Loaded fragment so stop parsing
				return;
			}
		}

		// Determine if load final token
		if (!isPathProcessed) {
			// Only path so load as path
			nameEnd = contents.length();
			this.loadPath(contents, nameBegin, nameEnd, isRequireTranslate,
					handler, tempBuffer);

		} else if (valueBegin > 0) {
			// Load the final parameter
			valueEnd = contents.length();
			this.loadParameter(contents, nameBegin, nameEnd, valueBegin,
					valueEnd, isRequireTranslate, handler, tempBuffer);

			// Load the query string
			queryEnd = valueEnd; // end of last parameter value
			this.loadQueryString(contents, queryBegin, queryEnd,
					isOnlyParameters, handler);
		}
	}

	/**
	 * Loads the path to the handler.
	 * 
	 * @param contents
	 *            Contents being parsed that contains the parameter name/values.
	 * @param pathBegin
	 *            Beginning index of path in contents.
	 * @param pathEnd
	 *            Ending index of path in contents.
	 * @param isRequireTranslate
	 *            Indicates if a translation is required.
	 * @param handler
	 *            {@link HttpRequestTokenHandler}.
	 * @param tempBuffer
	 *            {@link TempBuffer}.
	 * @throws HttpRequestTokeniseException
	 *             If fails to load the path.
	 */
	private void loadPath(String contents, int pathBegin, int pathEnd,
			boolean isRequireTranslate, HttpRequestTokenHandler handler,
			TempBuffer tempBuffer) throws HttpRequestTokeniseException {

		// Obtain the path
		String path = contents.substring(pathBegin, pathEnd);
		if (isRequireTranslate) {
			path = this.translate(path, tempBuffer);
		}

		// Handle path
		handler.handlePath(path);
	}

	/**
	 * Loads the query string to the handler.
	 * 
	 * @param contents
	 *            Contents being parsed that contains the parameter name/values.
	 * @param queryBegin
	 *            Beginning index of query string in contents.
	 * @param queryEnd
	 *            Ending index of query string in contents.
	 * @param isOnlyParameters
	 *            Flag indicate if only loading parameters.
	 * @param handler
	 *            {@link HttpRequestTokenHandler}.
	 * @throws HttpRequestTokeniseException
	 *             If fails to load the query string.
	 */
	private void loadQueryString(String contents, int queryBegin, int queryEnd,
			boolean isOnlyParameters, HttpRequestTokenHandler handler)
			throws HttpRequestTokeniseException {

		// Only load query string if not just parameters
		if (!isOnlyParameters) {
			// Load the query string
			String queryString = contents.substring(queryBegin, queryEnd);
			handler.handleQueryString(queryString);
		}
	}

	/**
	 * Loads the parameter to the handler.
	 * 
	 * @param contents
	 *            Contents being parsed that contains the parameter name/values.
	 * @param nameBegin
	 *            Beginning index of name in contents.
	 * @param nameEnd
	 *            Ending index of name in contents.
	 * @param valueBegin
	 *            Beginning index of value in contents.
	 * @param valueEnd
	 *            Ending index of value in contents.
	 * @param isRequireTranslate
	 *            Indicates if a translation is required.
	 * @param handler
	 *            {@link HttpRequestTokenHandler}.
	 * @param tempBuffer
	 *            {@link TempBuffer}.
	 * @throws HttpRequestTokeniseException
	 *             If fails to load the parameter.
	 */
	private void loadParameter(String contents, int nameBegin, int nameEnd,
			int valueBegin, int valueEnd, boolean isRequireTranslate,
			HttpRequestTokenHandler handler, TempBuffer tempBuffer)
			throws HttpRequestTokeniseException {

		// Ensure valid
		if ((nameEnd < 0) || (valueBegin < 0) || (valueEnd < 0)) {
			throw new HttpRequestTokeniseException(
					"Invalid HTTP contents (name " + nameBegin + "," + nameEnd
							+ "  value " + valueBegin + "," + valueEnd + "): "
							+ contents);
		}

		// Obtain the raw name and value
		String rawName = contents.substring(nameBegin, nameEnd);
		String rawValue = contents.substring(valueBegin, valueEnd);

		// Obtain the name and value
		String name = (isRequireTranslate ? this.translate(rawName, tempBuffer)
				: rawName);
		String value = (isRequireTranslate ? this.translate(rawValue,
				tempBuffer) : rawValue);

		// Handle the parameter
		handler.handleHttpParameter(name, value);
	}

	/**
	 * Enum providing the escape state for translating.
	 */
	private static enum EscapeState {
		NONE, HIGH, LOW
	}

	/**
	 * Translates the parameter text.
	 * 
	 * @param parameterText
	 *            Text to be translated.
	 * @param tempBuffer
	 *            {@link TempBuffer}.
	 * @return Translated text.
	 * @throws HttpRequestTokeniseException
	 *             If fails to translate.
	 */
	private String translate(String parameterText, TempBuffer tempBuffer)
			throws HttpRequestTokeniseException {

		// Obtain the temporary buffer
		char[] buffer = tempBuffer.buffer;

		// Ensure temporary buffer large enough
		if ((buffer == null) || (buffer.length < parameterText.length())) {
			// Increase buffer size (translation should not be bigger)
			buffer = new char[parameterText.length()];

			// Make available for further translations
			tempBuffer.buffer = buffer;
		}

		// Iterate over parameter text translating
		int charIndex = 0;
		EscapeState escape = EscapeState.NONE;
		byte highBits = 0;
		for (int i = 0; i < parameterText.length(); i++) {
			char character = parameterText.charAt(i);

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
				highBits = this.translateEscapedCharToBits(character);
				escape = EscapeState.LOW;
				break;

			case LOW:
				// Have low bits, so obtain escaped character
				byte lowBits = this.translateEscapedCharToBits(character);
				character = (char) ((highBits << 4) | lowBits);

				// Load the character and no longer escaped
				buffer[charIndex++] = character;
				escape = EscapeState.NONE;
				break;
			}
		}

		// Should always be in non-escape state after translating
		if (escape != EscapeState.NONE) {
			throw new HttpRequestTokeniseException(
					"Invalid parameter text as escaping not complete: '"
							+ parameterText + "'");
		}

		// Return the translated text
		return new String(buffer, 0, charIndex);
	}

	/**
	 * Translates the character to the 4 bits as per escaping of HTTP.
	 * 
	 * @param character
	 *            Character to translate.
	 * @return Corresponding 4 bits for character.
	 * @throws HttpRequestTokeniseException
	 *             If invalid character for escaping.
	 */
	private byte translateEscapedCharToBits(char character)
			throws HttpRequestTokeniseException {

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
			throw new HttpRequestTokeniseException(
					"Invalid character for escaping: " + character);
		}

		// Return the bits
		return (byte) bits;
	}

	/**
	 * Temporary buffer.
	 */
	private static class TempBuffer {

		/**
		 * Buffer.
		 */
		public char[] buffer = null;
	}

}