/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.security.scheme;

import java.io.Serializable;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.security.impl.AuthenticationContextManagedObjectSource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.RatifyContext;
import net.officefloor.web.state.HttpRequestState;

/**
 * Mock {@link RatifyContext} for testing {@link HttpSecuritySource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpRatifyContext<AC extends Serializable> implements RatifyContext<AC> {

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState requestState;

	/**
	 * Access control.
	 */
	private AC accessControl = null;

	/**
	 * {@link Escalation}.
	 */
	private Throwable escalation = null;

	/**
	 * Initiate with no <code>authorization</code> {@link HttpHeader}.
	 */
	public MockHttpRatifyContext() {
		this((String) null);
	}

	/**
	 * Initiate.
	 * 
	 * @param authorizationHeaderValue <code>authorization</code> {@link HttpHeader}
	 *                                 value.
	 */
	public MockHttpRatifyContext(String authorizationHeaderValue) {
		this(MockHttpAuthenticateContext.createRequestWithAuthorizationHeader(authorizationHeaderValue));
	}

	/**
	 * Initiate.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 */
	public MockHttpRatifyContext(ServerHttpConnection connection) {
		this.connection = connection;
		this.session = MockWebApp.mockSession(this.connection);
		this.requestState = MockWebApp.mockRequestState(this.connection);
	}

	/**
	 * Obtains the access control.
	 * 
	 * @return Access control.
	 */
	public AC getAccessControl() {
		return this.accessControl;
	}

	/**
	 * Obtains the registered escalation.
	 * 
	 * @return {@link Escalation}.
	 */
	public Throwable getEscalation() {
		return this.escalation;
	}

	/*
	 * ===================== HttpRatifyContext ===============================
	 */

	@Override
	public ServerHttpConnection getConnection() {
		return this.connection;
	}

	@Override
	public String getQualifiedAttributeName(String attributeName) {
		return AuthenticationContextManagedObjectSource.getQualifiedAttributeName("mock", attributeName);
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public HttpRequestState getRequestState() {
		return this.requestState;
	}

	@Override
	public void accessControlChange(AC accessControl, Throwable escalation) {
		this.accessControl = accessControl;
		this.escalation = escalation;
	}

}
