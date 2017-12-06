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

import java.util.HashMap;
import java.util.Map;

import org.easymock.AbstractMatcher;
import org.junit.Assert;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpAuthenticateContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Mock {@link HttpAuthenticateContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpAuthenticateContext<S, C, D extends Enum<D>> implements HttpAuthenticateContext<S, C, D> {

	/**
	 * Credentials.
	 */
	private final C credentials;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * {@link OfficeFrameTestCase}.
	 */
	private final OfficeFrameTestCase testCase;

	/**
	 * Dependencies.
	 */
	private final Map<D, Object> dependencies = new HashMap<D, Object>();

	/**
	 * HTTP security.
	 */
	private S httpSecurity = null;

	/**
	 * {@link HttpAccessControl} registered with the {@link HttpSession}.
	 */
	private S sessionHttpSecurity = null;

	/**
	 * Initiate.
	 * 
	 * @param credentials
	 *            Credentials. May be <code>null</code>.
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 */
	public MockHttpAuthenticateContext(C credentials, OfficeFrameTestCase testCase) {
		this.credentials = credentials;
		this.testCase = testCase;

		// Create the necessary mock objects
		this.connection = testCase.createMock(ServerHttpConnection.class);
		this.session = testCase.createMock(HttpSession.class);
	}

	/**
	 * Registers and object.
	 * 
	 * @param key
	 *            Key for dependency.
	 * @param dependency
	 *            Dependency object.
	 */
	public void registerObject(D key, Object dependency) {
		this.dependencies.put(key, dependency);
	}

	/**
	 * Obtains the registered HTTP security.
	 * 
	 * @return HTTP security.
	 */
	public S getHttpSecurity() {
		return this.httpSecurity;
	}

	/**
	 * Obtains the registered HTTP security with the {@link HttpSession}.
	 * 
	 * @return Registered HTTP security with the {@link HttpSession}.
	 */
	public S getRegisteredHttpSecurityWithHttpSession() {
		return this.sessionHttpSecurity;
	}

	/**
	 * Records the Authorization {@link HttpHeader} value.
	 * 
	 * @param authorizationHeaderValue
	 *            Authorization {@link HttpHeader} value.
	 */
	public void recordHttpRequestWithAuthorizationHeader(String authorizationHeaderValue) {

		// Create the HTTP request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		if (authorizationHeaderValue != null) {
			request.header("Authorization", authorizationHeaderValue);
		}

		// Record return the HTTP request
		this.testCase.recordReturn(this.connection, this.connection.getRequest(), request.build());
	}

	/**
	 * Records registering {@link HttpAccessControl} with the {@link HttpSession}.
	 * 
	 * @param attributeName
	 *            Name of attribute to register the {@link HttpAccessControl} with
	 *            the {@link HttpSession}.
	 */
	public void recordRegisterHttpSecurityWithHttpSession(String attributeName) {
		this.session.setAttribute(attributeName, null);
		this.testCase.control(this.session).setMatcher(new AbstractMatcher() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object[] expected, Object[] actual) {
				Assert.assertEquals("Incorrect HTTP Security session attribute name", expected[0], actual[0]);
				Assert.assertNotNull("Must have HTTP Security if registering with HTTP Session", actual[0]);
				MockHttpAuthenticateContext.this.sessionHttpSecurity = (S) actual[1];
				return true;
			}
		});
	}

	/*
	 * ==================== HttpAuthenticateContext =========================
	 */

	@Override
	public C getCredentials() {
		return this.credentials;
	}

	@Override
	public ServerHttpConnection getConnection() {
		return this.connection;
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public Object getObject(D key) {
		return this.dependencies.get(key);
	}

	@Override
	public void setAccessControl(S security) {
		this.httpSecurity = security;
	}

}