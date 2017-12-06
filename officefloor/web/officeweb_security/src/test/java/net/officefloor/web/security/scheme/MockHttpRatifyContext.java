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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpRatifyContext;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Mock {@link HttpRatifyContext} for testing {@link HttpSecuritySource}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpRatifyContext<AC> implements HttpRatifyContext<AC> {

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection;

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * {@link HttpAccessControl}.
	 */
	private AC accessControl = null;

	/**
	 * Initiate.
	 * 
	 * @param testCase
	 *            {@link OfficeFrameTestCase} to create necessary mock objects.
	 * @param authorizationHeaderValue
	 *            <code>authorization</code> {@link HttpHeader} value.
	 */
	public MockHttpRatifyContext(OfficeFrameTestCase testCase, String authorizationHeaderValue) {

		// Create the necessary mock objects
		this.connection = MockHttpAuthenticateContext.createRequestWithAuthorizationHeader(authorizationHeaderValue);
		this.session = testCase.createMock(HttpSession.class);
	}

	/**
	 * Obtains the access control.
	 * 
	 * @return Access control.
	 */
	public AC getAccessControl() {
		return this.accessControl;
	}

	/*
	 * ===================== HttpRatifyContext ===============================
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
	public void setAccessControl(AC accessControl) {
		this.accessControl = accessControl;
	}

}