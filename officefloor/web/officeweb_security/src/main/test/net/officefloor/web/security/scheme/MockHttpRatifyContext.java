/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
