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
import net.officefloor.plugin.web.http.security.HttpChallengeContext;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;

/**
 * Mock {@link HttpChallengeContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpChallengeContext<D extends Enum<D>, F extends Enum<F>> implements HttpChallengeContext<D, F> {

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
	 * Flows.
	 */
	private final MockHttpChallengeContextFlows<F> flows;

	/**
	 * {@link HttpRequest}.
	 */
	private HttpRequest request;

	/**
	 * Initiate.
	 * 
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 */
	@SuppressWarnings("unchecked")
	public MockHttpChallengeContext(OfficeFrameTestCase testCase) {
		this.testCase = testCase;

		// Create the necessary mock objects
		this.connection = testCase.createMock(ServerHttpConnection.class);
		this.session = testCase.createMock(HttpSession.class);
		this.flows = testCase.createMock(MockHttpChallengeContextFlows.class);
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
	 * Records obtaining the {@link HttpRequest}.
	 * 
	 * @return {@link HttpRequest}.
	 */
	public HttpRequest recordGetHttpRequest() {
		this.request = this.testCase.createMock(HttpRequest.class);
		this.testCase.recordReturn(this.connection, this.connection.getHttpRequest(), this.request);
		return this.request;
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
		HttpResponse response = this.testCase.createMock(HttpResponse.class);
		this.testCase.recordReturn(this.connection, this.connection.getHttpResponse(), response);

		// Record the challenge
		response.setHttpStatus(HttpStatus.UNAUTHORIZED);
		HttpResponseHeaders headers = this.testCase.createMock(HttpResponseHeaders.class);
		this.testCase.recordReturn(response, response.getHttpHeaders(), headers);
		this.testCase.recordReturn(headers, headers.addHeader("WWW-Authenticate", authenticateHeaderValue), header);
	}

	/**
	 * Records undertaking the flow.
	 * 
	 * @param key
	 *            Key to the flow.
	 */
	public void recordDoFlow(F key) {
		this.doFlow(key);
	}

	/*
	 * =================== HttpChallengeContext =====================
	 */

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
		this.flows.doFlow(key);
	}

	/**
	 * Interface to create mock object for mocking flows.
	 */
	private static interface MockHttpChallengeContextFlows<F extends Enum<F>> {

		/**
		 * Undertakes the flow.
		 * 
		 * @param key
		 *            Key for the flow.
		 */
		void doFlow(F key);
	}

}