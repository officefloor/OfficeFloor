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
package net.officefloor.tutorial.authenticationhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.session.HttpSessionManagedObjectSource;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the Secure Link.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationHttpServerTest {

	/**
	 * Main to run for manual testing.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: tutorial
	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	private WritableHttpCookie session;

	@Test
	public void login() throws Exception {

		// Ensure require login to get to page
		MockHttpResponse loginRedirect = this.server.send(MockHttpServer.mockRequest("/hello"));
		assertEquals("Ensure redirect", 303, loginRedirect.getStatus().getStatusCode());
		loginRedirect.assertHeader("location", "https://mock.officefloor.net/login");

		// Obtain the session cookie
		this.session = loginRedirect.getCookie(HttpSessionManagedObjectSource.DEFAULT_SESSION_ID_COOKIE_NAME);

		// Login
		MockHttpRequestBuilder loginRequest = MockHttpServer.mockRequest("/login+login?username=Daniel&password=Daniel")
				.secure(true).cookie(this.session.getName(), this.session.getValue());
		MockHttpResponse loggedInRedirect = this.server.send(loginRequest);
		assertEquals("Ensure successful login: " + loggedInRedirect.getEntity(null), 200,
				loggedInRedirect.getStatus().getStatusCode());

		// Ensure now able to access hello page
		MockHttpResponse helloPage = this.server
				.send(MockHttpServer.mockRequest("/hello").cookie(this.session.getName(), this.session.getValue()));
		String helloPageContent = helloPage.getEntity(null);
		assertEquals("Should obtain hello page: " + helloPageContent, 200, helloPage.getStatus().getStatusCode());
		assertTrue("Ensure hello page with login: " + helloPageContent, helloPageContent.contains("<p>Hi Daniel</p>"));
	}

	@Test
	public void logout() throws Exception {

		// Login
		this.login();

		// Logout
		MockHttpResponse logoutRedirect = this.server.send(
				MockHttpServer.mockRequest("/hello+logout").cookie(this.session.getName(), this.session.getValue()));
		assertEquals("Ensure logout: " + logoutRedirect.getEntity(null), 303,
				logoutRedirect.getStatus().getStatusCode());
		logoutRedirect.assertHeader("location", "/logout");

		// Attempt to go back to page (but require login)
		MockHttpResponse loginPage = this.server
				.send(MockHttpServer.mockRequest("/hello").cookie(this.session.getName(), this.session.getValue()));
		assertEquals("Ensure redirect", 303, loginPage.getStatus().getStatusCode());
		loginPage.assertHeader("location", "https://mock.officefloor.net/login");
	}
	// END SNIPPET: tutorial

}