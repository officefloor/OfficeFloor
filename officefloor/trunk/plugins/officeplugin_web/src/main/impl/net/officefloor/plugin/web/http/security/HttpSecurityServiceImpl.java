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
package net.officefloor.plugin.web.http.security;

import java.io.IOException;
import java.util.Map;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link HttpSecurityService} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityServiceImpl<D extends Enum<D>> implements
		HttpSecurityService {

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<D> source;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * Dependencies.
	 */
	private final Map<D, Object> dependencies;

	/**
	 * Initiate.
	 * 
	 * @param source
	 *            {@link HttpSecuritySource}.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param session
	 *            {@link HttpSession}.
	 * @param dependencies
	 *            Dependencies.
	 */
	public HttpSecurityServiceImpl(HttpSecuritySource<D> source,
			ServerHttpConnection connection, HttpSession session,
			Map<D, Object> dependencies) {
		this.source = source;
		this.connection = connection;
		this.session = session;
		this.dependencies = dependencies;
	}

	/*
	 * ================= HttpSecurityService ======================
	 */

	@Override
	public HttpSecurity authenticate() throws IOException,
			AuthenticationException {

		final String ATTRIBUTE_HTTP_SECURITY = "#HttpSecurity#";

		// Determine if session already authenticated
		HttpSecurity security = (HttpSecurity) this.session
				.getAttribute(ATTRIBUTE_HTTP_SECURITY);
		if (security != null) {
			return security; // already authenticated
		}

		// Obtain the authenticate header value
		String authenticate = "";
		HttpRequest request = this.connection.getHttpRequest();
		for (HttpHeader header : request.getHeaders()) {
			if ("Authorization".equalsIgnoreCase(header.getName())) {
				authenticate = header.getValue();
			}
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
		if (endIndex < 0) {
			// no authentication scheme, not authenticated
			return null;
		}
		String authenticationScheme = authenticate.substring(startIndex,
				endIndex);

		// Ensure the correct authentication scheme
		if (!authenticationScheme.equalsIgnoreCase(this.source
				.getAuthenticationScheme())) {
			return null; // Incorrect scheme, so not authenticated
		}

		// Obtain the parameters (+1 follows space after authentication scheme)
		String parameters = authenticate.substring(endIndex + 1);

		// Authenticate
		security = this.source.authenticate(parameters, this.connection,
				this.session, this.dependencies);

		// Cache security
		this.session.setAttribute(ATTRIBUTE_HTTP_SECURITY, security);

		// Return the authentication
		return security;
	}

	@Override
	public void loadUnauthorised() throws AuthenticationException {
		// Load unauthorised
		this.source.loadUnauthorised(this.connection, this.session,
				this.dependencies);
	}

}