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
public class MockHttpAuthenticateContext<AC, O extends Enum<O>> implements HttpAuthenticateContext<AC, O> {

	/**
	 * Creates the {@link ServerHttpConnection} with authorization
	 * {@link HttpHeader} value.
	 * 
	 * @param authorizationHeaderValue
	 *            Authorization {@link HttpHeader} value.
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
	private final Map<O, Object> dependencies = new HashMap<O, Object>();

	/**
	 * Access control.
	 */
	private AC accessControl = null;

	/**
	 * Access control bound to {@link HttpSession}.
	 */
	private AC sessionAccessControl = null;

	/**
	 * Initiate.
	 * 
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 * @param authorizationHeaderValue
	 *            <code>authorization</code> {@link HttpHeader} value.
	 */
	public MockHttpAuthenticateContext(OfficeFrameTestCase testCase, String authorizationHeaderValue) {
		this.testCase = testCase;

		// Create the necessary mock objects
		this.connection = createRequestWithAuthorizationHeader(authorizationHeaderValue);
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
	public void registerObject(O key, Object dependency) {
		this.dependencies.put(key, dependency);
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
	 * Obtains the registered access control with the {@link HttpSession}.
	 * 
	 * @return Registered access control with the {@link HttpSession}.
	 */
	public AC getRegisteredAccessControlWithHttpSession() {
		return this.sessionAccessControl;
	}

	/**
	 * Records registering {@link HttpAccessControl} with the
	 * {@link HttpSession}.
	 * 
	 * @param attributeName
	 *            Name of attribute to register the {@link HttpAccessControl}
	 *            with the {@link HttpSession}.
	 */
	public void recordRegisterAccessControlWithHttpSession(String attributeName) {
		this.session.setAttribute(attributeName, null);
		this.testCase.control(this.session).setMatcher(new AbstractMatcher() {
			@Override
			@SuppressWarnings("unchecked")
			public boolean matches(Object[] expected, Object[] actual) {
				Assert.assertEquals("Incorrect access control session attribute name", expected[0], actual[0]);
				Assert.assertNotNull("Must have access control if registering with HTTP Session", actual[0]);
				MockHttpAuthenticateContext.this.sessionAccessControl = (AC) actual[1];
				return true;
			}
		});
	}

	/*
	 * ==================== HttpAuthenticateContext =========================
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
	public Object getObject(O key) {
		return this.dependencies.get(key);
	}

	@Override
	public void setAccessControl(AC accessControl) {
		this.accessControl = accessControl;
	}

}