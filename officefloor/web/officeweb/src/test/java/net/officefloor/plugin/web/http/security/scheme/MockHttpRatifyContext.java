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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.security.HttpRatifyContext;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Mock {@link HttpRatifyContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpRatifyContext<S, C> implements HttpRatifyContext<S, C> {

	/**
	 * Credentials.
	 */
	private final C credentials;

	/**
	 * {@link OfficeFrameTestCase}.
	 */
	private final OfficeFrameTestCase testCase;

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * {@link MockHttpRequestBuilder}.
	 */
	private MockHttpRequestBuilder request;

	/**
	 * {@link HttpResponse}.
	 */
	private HttpResponse response;

	/**
	 * {@link HttpSecurity}.
	 */
	private S security = null;

	/**
	 * Initiate.
	 * 
	 * @param credentials
	 *            Credentials.
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 */
	public MockHttpRatifyContext(C credentials, OfficeFrameTestCase testCase) {
		this.credentials = credentials;
		this.testCase = testCase;

		// Create the necessary mock objects
		this.connection = testCase.createMock(ServerHttpConnection.class);
		this.session = testCase.createMock(HttpSession.class);
	}

	/**
	 * Records obtaining the {@link HttpRequest}.
	 * 
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder recordGetHttpRequest() {
		this.request = MockHttpServer.mockRequest();
		this.testCase.recordReturn(this.connection, this.connection.getHttpRequest(), this.request);
		return this.request;
	}

	/**
	 * Records obtaining the {@link HttpResponse}.
	 * 
	 * @return {@link HttpResponse}.
	 */
	public HttpResponse recordGetHttpResponse() {
		this.response = this.testCase.createMock(HttpResponse.class);
		this.testCase.recordReturn(this.connection, this.connection.getHttpResponse(), this.response);
		return this.response;
	}

	/**
	 * Records the Authorization {@link HttpHeader} value.
	 * 
	 * @param authorizationHeaderValue
	 *            Authorization {@link HttpHeader} value.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder recordAuthorizationHeader(String authorizationHeaderValue) {

		// Record obtaining the HTTP request
		MockHttpRequestBuilder httpRequest = this.recordGetHttpRequest();

		// Record providing the HTTP headers
		httpRequest.header("Authorization", authorizationHeaderValue);

		// Return the HTTP request
		return httpRequest;
	}

	/**
	 * Obtains the HTTP security.
	 * 
	 * @return HTTP security.
	 */
	public S getHttpSecurity() {
		return this.security;
	}

	/*
	 * ===================== HttpRatifyContext ===============================
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
	public void setHttpSecurity(S security) {
		this.security = security;
	}

}