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
package net.officefloor.plugin.web.http.security.scheme;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Abstract test for a {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSecuritySourceTestCase<D extends Enum<D>>
		extends OfficeFrameTestCase {

	/**
	 * Assets the content of the {@link HttpSecurity}.
	 * 
	 * @param actual
	 *            {@link HttpSecurity} to check.
	 * @param expectedUserName
	 *            Expected user name.
	 * @param expectedRoles
	 *            Expected roles that user within.
	 */
	protected static void assertHttpSecurity(HttpSecurity actual,
			String expectedUserName, String... expectedRoles) {
		assertEquals("Incorrect remote user name", expectedUserName,
				actual.getRemoteUser());
		assertEquals("Incorrect principle user name", expectedUserName, actual
				.getUserPrincipal().getName());
		for (String expectedRole : expectedRoles) {
			assertTrue("Should be in role '" + expectedRole + "'",
					actual.isUserInRole(expectedRole));
		}
	}

	/**
	 * {@link HttpSecuritySource}.
	 */
	private final HttpSecuritySource<D> source;

	/**
	 * Expected authentication scheme.
	 */
	private final String expectedAuthenticationScheme;

	/**
	 * {@link HttpSecuritySourceContext}.
	 */
	@SuppressWarnings("unchecked")
	protected final HttpSecuritySourceContext<D> context = this
			.createMock(HttpSecuritySourceContext.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	protected final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpSession}.
	 */
	protected final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * Dependencies.
	 */
	private final Map<D, Object> dependencies = new HashMap<D, Object>();

	/**
	 * Initiate.
	 * 
	 * @param sourceClass
	 *            {@link Class} of the {@link HttpSecuritySource} being tested.
	 * @param expectedAuthenticationScheme
	 *            Expected authentication scheme.
	 */
	protected AbstractHttpSecuritySourceTestCase(
			Class<? extends HttpSecuritySource<D>> sourceClass,
			String expectedAuthenticationScheme) {
		try {
			this.source = sourceClass.newInstance();
			this.expectedAuthenticationScheme = expectedAuthenticationScheme;
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Loads the dependencies.
	 * 
	 * @param dependencies
	 *            {@link Map} to be loaded with dependencies.
	 */
	protected abstract void loadDependencies(Map<D, Object> dependencies);

	/**
	 * Does the test of authentication.
	 * 
	 * @param parameters
	 *            Parameters text from {@link HttpHeader}.
	 * @return {@link HttpSecurity}.
	 */
	protected HttpSecurity doAuthenticate(String parameters) {
		try {

			// Load the dependencies
			this.loadDependencies(this.dependencies);

			this.replayMockObjects();

			// Initialise the HTTP security source
			this.source.init(this.context);

			// Ensure correct authentication scheme
			assertEquals("Incorrect authentication scheme",
					this.expectedAuthenticationScheme,
					this.source.getAuthenticationScheme());

			// Authenticate
			HttpSecurity security = this.source.authenticate(parameters,
					this.connection, this.session, this.dependencies);

			// Ensure correct authentication scheme if authenticated
			if (security != null) {
				assertEquals("Security authentication scheme mismatch",
						this.expectedAuthenticationScheme,
						security.getAuthenticationScheme());
			}

			this.verifyMockObjects();

			// Return the security
			return security;

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Does the authentication test and validates the expected
	 * {@link HttpSecurity}.
	 * 
	 * @param parameters
	 *            Parameters text from {@link HttpHeader}.
	 * @param expectedUserName
	 *            Expected user name.
	 * @param expectedRoles
	 *            Expected roles that user within.
	 * @return {@link HttpSecurity}.
	 */
	protected HttpSecurity doAuthenticate(String parameters,
			String expectedUserName, String... expectedRoles) {
		HttpSecurity security = this.doAuthenticate(parameters);
		assertHttpSecurity(security, expectedUserName, expectedRoles);
		return security;
	}

	/**
	 * Does the challenge test.
	 * 
	 * @param parameters
	 *            Parameters text for the {@link HttpHeader}.
	 */
	protected void doChallenge(String parameters) {
		try {

			// Load the dependencies
			this.loadDependencies(this.dependencies);

			// Mock
			final HttpResponse response = this.createMock(HttpResponse.class);
			final HttpHeader header = this.createMock(HttpHeader.class);

			// Determine if send challenge
			if (parameters != null) {

				// Record loading the challenge
				this.recordReturn(this.connection,
						this.connection.getHttpResponse(), response);
				response.setStatus(HttpStatus.SC_UNAUTHORIZED);
				this.recordReturn(response, response.addHeader(
						"WWW-Authenticate", this.expectedAuthenticationScheme
								+ " " + parameters), header);
			}

			// Test
			this.replayMockObjects();

			// Initialise the HTTP security source
			this.source.init(this.context);

			// Load the unauthorised response details
			this.source.loadUnauthorised(this.connection, this.session,
					this.dependencies);

			this.verifyMockObjects();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}