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
package net.officefloor.web.security.scheme;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;

/**
 * Wraps the {@link HttpRequest} to obtain the HTTP authentication scheme
 * information.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationScheme {

	/**
	 * Obtains the {@link HttpAuthenticationScheme} from the {@link HttpRequest}
	 * or <code>null</code> if no scheme is available.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @return {@link HttpAuthenticationScheme} or <code>null</code> if not
	 *         available on {@link HttpRequest}.
	 */
	public static HttpAuthenticationScheme getHttpAuthenticationScheme(HttpRequest request) {

		// Obtain the authenticate header value
		String authenticate = "";
		HttpHeader header = request.getHeaders().getHeader("authorization");
		if (header != null) {
			authenticate = header.getValue();
		}

		// Parse out the authentication scheme
		boolean isStarted = false;
		int startIndex = -1;
		int endIndex = -1;
		PARSING: for (int i = 0; i < authenticate.length(); i++) {
			char character = authenticate.charAt(i);
			switch (character) {
			case ' ':
				// Handle space
				if (isStarted) {
					// Completed authentication scheme
					endIndex = i;
					break PARSING;
				}
				// else ignore leading space
				break;

			default:
				// Handle non-space
				if (!isStarted) {
					// Flag that started
					startIndex = i;
					isStarted = true;
				}
			}
		}
		if (endIndex >= 0) {

			// Obtain the authentication scheme information
			String authenticationScheme = authenticate.substring(startIndex, endIndex);
			String parameters = authenticate.substring(endIndex + 1);

			// Return the authentication scheme
			return new HttpAuthenticationScheme(authenticationScheme, parameters);
		}

		// No authentication scheme
		return null;
	}

	/**
	 * HTTP authentication scheme.
	 */
	private final String authenticationScheme;

	/**
	 * Parameters for the scheme.
	 */
	private final String parameters;

	/**
	 * Initiate.
	 * 
	 * @param authenticationScheme
	 *            HTTP authentication scheme.
	 * @param parameters
	 *            Parameters for the scheme.
	 */
	private HttpAuthenticationScheme(String authenticationScheme, String parameters) {
		this.authenticationScheme = authenticationScheme;
		this.parameters = parameters;
	}

	/**
	 * Obtains the HTTP authentication scheme.
	 * 
	 * @return HTTP authentication scheme.
	 */
	public String getAuthentiationScheme() {
		return this.authenticationScheme;
	}

	/**
	 * Obtains the parameters.
	 * 
	 * @return Parameters or <code>null</code> if no parameters.
	 */
	public String getParameters() {
		return this.parameters;
	}

}