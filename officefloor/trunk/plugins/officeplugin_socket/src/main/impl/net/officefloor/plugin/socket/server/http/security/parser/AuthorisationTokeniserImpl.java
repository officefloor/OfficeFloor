/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.security.parser;

import org.apache.commons.codec.binary.Base64;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;

/**
 * {@link AuthorisationTokeniser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthorisationTokeniserImpl implements AuthorisationTokeniser {

	/**
	 * State of parsing parameters.
	 */
	private static enum ParameterState {
		INIT, NAME, NAME_VALUE_SEPARATION, VALUE, QUOTED_VALUE
	}

	/*
	 * =================== AuthorisationTokeniser =======================
	 */

	@Override
	public void tokeniseAuthorizationHeaderValue(String value,
			AuthorisationTokenHandler handler)
			throws AuthorisationTokeniseException {

		// Do nothing if no value
		if (value == null) {
			return;
		}

		// Initiate parsing
		boolean isAuthenticationSchemeStarted = false;
		int nameStart = -1;
		int nameEnd = -1;

		// Parse the tokens from the value
		NEXT_CHARACTER: for (int i = 0; i < value.length(); i++) {

			// Handle next character
			char character = value.charAt(i);
			switch (character) {

			case ' ':
				// Handle space
				if (!isAuthenticationSchemeStarted) {
					continue NEXT_CHARACTER; // ignore leading space
				}

				// Obtained the authentication scheme
				nameEnd = i;
				break NEXT_CHARACTER; // Obtained so stop processing further

			default:
				// Handle value
				if (!isAuthenticationSchemeStarted) {
					// Flag started authentication scheme name
					nameStart = i;
					isAuthenticationSchemeStarted = true;
				}
			}
		}

		// Obtain the authentication scheme
		String authenticationScheme;
		if (nameEnd < 0) {
			// Authentication scheme is entire value
			authenticationScheme = value.substring(nameStart);
		} else {
			// Extract the authentication scheme
			authenticationScheme = value.substring(nameStart, nameEnd);
		}

		// Handle the authentication scheme
		handler.handleAuthenticationScheme(authenticationScheme);

		// Obtain starting index to parse remaining content
		int startIndex = (nameEnd + 1); // character after authentication scheme
		// name

		// Handle based on type of authentication
		if ("Basic".equalsIgnoreCase(authenticationScheme)) {
			// Handle 'Basic' authentication
			this.tokeniseBasicCredentials(value, startIndex, handler);
		} else {
			// Handle parameters for authentication
			this.tokeniseParameters(value, startIndex, handler);
		}
	}

	/**
	 * Tokenises the <code>Basic</code> authentication user Id and password.
	 * 
	 * @param value
	 *            {@link HttpHeader} value.
	 * @param startIndex
	 *            Starting index to parse out user Id and password.
	 * @param handler
	 *            {@link AuthorisationTokenHandler}.
	 * @throws AuthorisationTokeniseException
	 *             If fails to tokenise credentials.
	 */
	private void tokeniseBasicCredentials(String value, int startIndex,
			AuthorisationTokenHandler handler)
			throws AuthorisationTokeniseException {

		// Obtain the credentials
		String credentials = value.substring(startIndex);
		credentials = credentials.trim();

		// Decode Base64 credentials
		byte[] decoded = Base64.decodeBase64(credentials);

		// Obtain the string value
		String userIdPassword = new String(decoded,
				HttpRequestParserImpl.US_ASCII);

		// Obtain location of user Id to password separator
		int separatorIndex = userIdPassword.indexOf(':');
		if (separatorIndex > 0) {
			// Have user Id and password, so parse out
			String userId = userIdPassword.substring(0, separatorIndex);
			String password = userIdPassword.substring(separatorIndex + 1);

			// Handle user Id and password
			handler.handleParameter(AuthorisationTokenHandler.BASIC_USER_ID,
					userId);
			handler.handleParameter(AuthorisationTokenHandler.BASIC_PASSWORD,
					password);
		}
	}

	/**
	 * Tokenises the parameters.
	 * 
	 * @param headerValue
	 *            {@link HttpHeader} value.
	 * @param startIndex
	 *            Starting index to parse out parameters.
	 * @param handler
	 *            {@link AuthorisationTokenHandler}.
	 * @throws AuthorisationTokeniseException
	 *             If fails to tokenise parameters.
	 */
	private void tokeniseParameters(String headerValue, int startIndex,
			AuthorisationTokenHandler handler)
			throws AuthorisationTokeniseException {

		// Initiate parsing
		ParameterState state = ParameterState.INIT;
		int start = -1;
		int end = -1;
		String name = null;

		// Parse the name and value parameters
		for (int i = startIndex; i < headerValue.length(); i++) {

			// Handle next character
			char character = headerValue.charAt(i);
			switch (character) {

			case ',':
				// Handle parameter separator
				switch (state) {
				case VALUE:
					// End of value, so obtain the value
					end = i;
					String value = headerValue.substring(start, end);
					value = value.trim(); // ignore spacing

					// Handle parameter
					handler.handleParameter(name, value);

					// Reset for next parameter
					state = ParameterState.INIT;
					break;
				}
				break;

			case '=':
				// Handle assignment
				switch (state) {
				case NAME:
					// Obtain the name
					end = i;
					name = headerValue.substring(start, end);
					name = name.trim(); // ignore spacing
					state = ParameterState.NAME_VALUE_SEPARATION;
					break;
				}
				break;

			case '"':
				// Handle quote
				switch (state) {
				case NAME_VALUE_SEPARATION:
					// Quoted value
					start = i + 1; // ignore quote
					state = ParameterState.QUOTED_VALUE;
					break;

				case QUOTED_VALUE:
					// End of quoted value
					end = i;
					String value = headerValue.substring(start, end);

					// Handle parameter
					handler.handleParameter(name, value);

					// Reset for next parameter
					state = ParameterState.INIT;
					break;
				}
				break;

			case ' ':
				// Handle space
				switch (state) {
				case INIT:
					break; // ignore leading space
				}
				break;

			default:
				// Handle value
				switch (state) {
				case INIT:
					// Start processing the name
					start = i;
					state = ParameterState.NAME;
					break;

				case NAME:
					break; // Letter of name

				case NAME_VALUE_SEPARATION:
					// Non quoted value
					start = i;
					state = ParameterState.VALUE;
					break;
				}
				break;
			}
		}

		// Provide final value
		switch (state) {
		case VALUE:
			// Remaining content is value
			String value = headerValue.substring(start);
			value = value.trim(); // ignore spacing

			// Handle parameter
			handler.handleParameter(name, value);
			break;
		}
	}

}