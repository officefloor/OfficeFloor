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

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.spi.security.AuthenticateContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Mock {@link AuthenticateContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpAuthenticateContext<AC extends Serializable, O extends Enum<O>, F extends Enum<F>>
		extends AbstractMockHttpSecurityActionContext<O, F> implements AuthenticateContext<AC, O, F> {

	/**
	 * Creates the {@link ServerHttpConnection} with authorization
	 * {@link HttpHeader} value.
	 * 
	 * @param authorizationHeaderValue Authorization {@link HttpHeader} value.
	 * @return {@link ServerHttpConnection}.
	 */
	public static ServerHttpConnection createRequestWithAuthorizationHeader(String authorizationHeaderValue) {

		// Create the HTTP request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		if (authorizationHeaderValue != null) {
			request.header("Authorization", authorizationHeaderValue);
		}

		// Return the connection with request
		return MockHttpServer.mockConnection(request);
	}

	/**
	 * Access control.
	 */
	private AC accessControl = null;

	/**
	 * Escalation.
	 */
	private Throwable escalation = null;

	/**
	 * Initiate with no <code>authorization</code> {@link HttpHeader}.
	 */
	public MockHttpAuthenticateContext() {
		this((String) null);
	}

	/**
	 * Initiate.
	 * 
	 * @param authorizationHeaderValue <code>authorization</code> {@link HttpHeader}
	 *                                 value.
	 */
	public MockHttpAuthenticateContext(String authorizationHeaderValue) {
		this(createRequestWithAuthorizationHeader(authorizationHeaderValue));
	}

	/**
	 * Initiate.
	 * 
	 * @param connection {@link ServerHttpConnection}.
	 */
	public MockHttpAuthenticateContext(ServerHttpConnection connection) {
		super(connection);
	}

	/**
	 * Obtains the registered access control.
	 * 
	 * @return Access control.
	 */
	public AC getAccessControl() {
		return this.accessControl;
	}

	/**
	 * Obtains the registered escalation.
	 * 
	 * @return Escalation.
	 */
	public Throwable getEscalation() {
		return this.escalation;
	}

	/*
	 * ==================== HttpAuthenticateContext =========================
	 */

	@Override
	public void accessControlChange(AC accessControl, Throwable escalation) {
		this.accessControl = accessControl;
		this.escalation = escalation;
	}

}
