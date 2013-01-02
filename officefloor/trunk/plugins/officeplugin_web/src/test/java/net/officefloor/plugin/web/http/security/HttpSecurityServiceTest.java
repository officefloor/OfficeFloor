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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.HttpSecurityServiceImpl;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource.Dependencies;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link HttpSecurityService}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityServiceTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpSecuritySource}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpSecuritySource<Dependencies> source = this
			.createMock(HttpSecuritySource.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * Dependencies.
	 */
	@SuppressWarnings("unchecked")
	private final Map<Dependencies, Object> dependencies = this
			.createMock(Map.class);

	/**
	 * {@link HttpSecurityService} being tested.
	 */
	private final HttpSecurityService service = new HttpSecurityServiceImpl<Dependencies>(
			this.source, this.connection, this.session, dependencies);

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * Ensure that can provide {@link HttpSecurity} from the {@link HttpSession}
	 * for already authenticated {@link ServerHttpConnection}.
	 */
	public void testAlreadyAuthenticated() throws Exception {

		// Mocks
		final HttpSecurity security = this.createMock(HttpSecurity.class);

		// Record obtaining the cache HttpSecurity
		this.record_getCached(security);

		// Test
		this.replayMockObjects();
		HttpSecurity actualSecurity = this.service.authenticate();
		this.verifyMockObjects();

		// Ensure correct security
		assertEquals("Incorrect HTTP security", security, actualSecurity);
	}

	/**
	 * Ensure not authenticate if missing <code>Authenticate</code>
	 * {@link HttpHeader}.
	 */
	public void testMissingAuthenticateHttpHeader() throws Exception {

		// Record not providing Authenticate header
		this.record_getCached(null);
		this.record_getHeaders(null);

		// Test
		this.replayMockObjects();
		assertNull("Should not authenticate", this.service.authenticate());
		this.verifyMockObjects();
	}

	/**
	 * Ensure not authenticate if incorrect authentication scheme.
	 */
	public void testIncorrectAuthenticateScheme() throws Exception {

		// Record incorrect authentication scheme
		this.record_getCached(null);
		this.record_getHeaders("Digest incorrect");
		this.recordReturn(this.source, this.source.getAuthenticationScheme(),
				"Basic");

		// Test
		this.replayMockObjects();
		assertNull("Should not authenticate", this.service.authenticate());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testAuthenticate() throws Exception {

		// Mocks
		final HttpSecurity security = this.createMock(HttpSecurity.class);

		// Record
		this.record_getCached(null);
		this.record_getHeaders("Basic Base64UsernamePassword");
		this.recordReturn(this.source, this.source.getAuthenticationScheme(),
				"basic");
		this.recordReturn(this.source, this.source.authenticate(
				"Base64UsernamePassword", this.connection, this.session,
				this.dependencies), security);
		this.record_cache(security);

		// Test
		this.replayMockObjects();
		HttpSecurity actualSecurity = this.service.authenticate();
		this.verifyMockObjects();

		// Ensure correct security
		assertEquals("Incorrect security", security, actualSecurity);
	}

	/**
	 * Ensure can authenticate with extra spacing.
	 */
	public void testExtraSpacing() throws Exception {

		// Mocks
		final HttpSecurity security = this.createMock(HttpSecurity.class);

		// Record
		this.record_getCached(null);
		this.record_getHeaders(" Basic  Base64UsernamePassword");
		this.recordReturn(this.source, this.source.getAuthenticationScheme(),
				"Basic");
		this.recordReturn(this.source, this.source.authenticate(
				" Base64UsernamePassword", this.connection, this.session,
				this.dependencies), security);
		this.record_cache(security);

		// Test
		this.replayMockObjects();
		HttpSecurity actualSecurity = this.service.authenticate();
		this.verifyMockObjects();

		// Ensure correct security
		assertEquals("Incorrect security", security, actualSecurity);
	}

	/**
	 * Ensure can load unauthenticated.
	 */
	public void testLoadUnauthenticated() throws Exception {

		// Record
		this.source.loadUnauthorised(this.connection, this.session,
				this.dependencies);

		// Test
		this.replayMockObjects();
		this.service.loadUnauthorised();
		this.verifyMockObjects();
	}

	/**
	 * Records obtaining the cached {@link HttpSecurity} from the
	 * {@link HttpSession}.
	 * 
	 * @param security
	 *            {@link HttpSecurity} to return from the {@link HttpSession}.
	 *            May be <code>null</code>.
	 */
	private void record_getCached(HttpSecurity security) {
		this.recordReturn(this.session, this.session
				.getAttribute("#HttpSecurity#"), security);
	}

	/**
	 * Records caching the {@link HttpSecurity} in the {@link HttpSession}.
	 * 
	 * @param security
	 *            {@link HttpSecurity} to cache.
	 */
	private void record_cache(HttpSecurity security) {
		this.session.setAttribute("#HttpSecurity#", security);
	}

	/**
	 * Records retrieving the {@link HttpHeader} instances from the
	 * {@link HttpRequest}.
	 * 
	 * @param authenticateValue
	 *            Value for the <code>Authenticate</code> {@link HttpHeader}.
	 *            <code>null</code> means to not add the {@link HttpHeader}.
	 * @return Listing of the {@link HttpHeader} instances being returned.
	 */
	private List<HttpHeader> record_getHeaders(String authenticateValue) {

		// Create the listing of the HTTP headers
		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);

		// Add the HTTP header (if have value)
		if (authenticateValue != null) {
			headers.add(new HttpHeaderImpl("Authorization", authenticateValue));
		}

		// Record obtaining the HTTP headers from request
		this.recordReturn(this.connection, this.connection.getHttpRequest(),
				this.request);
		this.recordReturn(this.request, this.request.getHeaders(), headers);

		// Return the HTTP headers
		return headers;
	}

}