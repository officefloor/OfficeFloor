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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.security.HttpAuthenticateContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Mock {@link HttpAuthenticateContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpAuthenticateContext<S, C, D extends Enum<D>, F extends Enum<F>>
		implements HttpAuthenticateContext<S, C, D, F> {

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
		this.testCase.recordReturn(httpRequest, httpRequest.getHeaders(),
				headers);

		// Return the HTTP request
		return httpRequest;
	}

	/**
	 * Records the authenticate challenge.
	 * 
	 * @param authenticateHeaderValue
	 *            Authenticate {@link HttpHeader} value.
	 */
	public void recordAuthenticateChallenge(String authenticateHeaderValue) {

		HttpHeader header = this.testCase.createMock(HttpHeader.class);

		// Record obtaining the HTTP response
		HttpResponse httpResponse = this.recordGetHttpResponse();

		// Record the challenge
		httpResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
		this.testCase.recordReturn(httpResponse, httpResponse.addHeader(
				"WWW-Authenticate", authenticateHeaderValue), header);
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
	public void doFlow(F key) {
		// TODO implement HttpAuthenticateContext<S,C,F,F>.doFlow
		throw new UnsupportedOperationException(
				"TODO implement HttpAuthenticateContext<S,C,F,F>.doFlow");
	}

	@Override
	public void setHttpSecurity(S security) {
		this.httpSecurity = security;
	}

}