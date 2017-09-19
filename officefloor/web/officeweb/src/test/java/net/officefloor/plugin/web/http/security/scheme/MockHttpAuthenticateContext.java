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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.AbstractMatcher;
import org.junit.Assert;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.parse.impl.HttpHeaderImpl;

/**
 * Mock {@link HttpAuthenticateContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpAuthenticateContext<S, C, D extends Enum<D>> implements
		HttpAuthenticateContext<S, C, D> {

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
	 * {@link HttpRequest}.
	 */
	private HttpRequest request;

	/**
	 * {@link HttpResponse}.
	 */
	private HttpResponse response;

	/**
	 * HTTP security.
	 */
	private S httpSecurity = null;

	/**
	 * {@link HttpSecurity} registered with the {@link HttpSession}.
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
	public MockHttpAuthenticateContext(C credentials,
			OfficeFrameTestCase testCase) {
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
	 * Records obtaining the {@link HttpRequest}.
	 * 
	 * @return {@link HttpRequest}.
	 */
	public HttpRequest recordGetHttpRequest() {
		this.request = this.testCase.createMock(HttpRequest.class);
		this.testCase.recordReturn(this.connection,
				this.connection.getHttpRequest(), this.request);
		return this.request;
	}

	/**
	 * Records obtaining the {@link HttpResponse}.
	 * 
	 * @return {@link HttpResponse}.
	 */
	public HttpResponse recordGetHttpResponse() {
		this.response = this.testCase.createMock(HttpResponse.class);
		this.testCase.recordReturn(this.connection,
				this.connection.getHttpResponse(), this.response);
		return this.response;
	}

	/**
	 * Records the Authorization {@link HttpHeader} value.
	 * 
	 * @param authorizationHeaderValue
	 *            Authorization {@link HttpHeader} value.
	 * @return {@link HttpRequest}.
	 */
	public HttpRequest recordAuthorizationHeader(String authorizationHeaderValue) {

		// Record obtaining the HTTP request
		HttpRequest httpRequest = this.recordGetHttpRequest();

		// Record providing the HTTP headers
		List<HttpHeader> headers = new ArrayList<HttpHeader>(1);
		if (authorizationHeaderValue != null) {
			headers.add(new HttpHeaderImpl("Authorization",
					authorizationHeaderValue));
		}
		this.testCase.recordReturn(httpRequest, httpRequest.getHttpHeaders(),
				headers);

		// Return the HTTP request
		return httpRequest;
	}

	/**
	 * Records registering {@link HttpSecurity} with the {@link HttpSession}.
	 * 
	 * @param attributeName
	 *            Name of attribute to register the {@link HttpSecurity} with
	 *            the {@link HttpSession}.
	 */
	public void recordRegisterHttpSecurityWithHttpSession(String attributeName) {
		this.session.setAttribute(attributeName, null);
		this.testCase.control(this.session).setMatcher(new AbstractMatcher() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object[] expected, Object[] actual) {
				Assert.assertEquals(
						"Incorrect HTTP Security session attribute name",
						expected[0], actual[0]);
				Assert.assertNotNull(
						"Must have HTTP Security if registering with HTTP Session",
						actual[0]);
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
	public void setHttpSecurity(S security) {
		this.httpSecurity = security;
	}

}