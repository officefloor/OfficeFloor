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

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import junit.framework.TestCase;
import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpTestUtil;

/**
 * Tests the Secure Link.
 * 
 * @author Daniel Sagenschneider
 */
public class AuthenticationHttpServerTest extends TestCase {

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient(true);

	@Override
	protected void tearDown() throws Exception {
		// Ensure stop
		try {
			this.client.close();
		} finally {
			OfficeFloorMain.close();
		}
	}

	// START SNIPPET: tutorial
	public void testLogin() throws Exception {

		// Start the server
		OfficeFloorMain.open();

		// Ensure require login to get to page
		String loginPage = this.doHttpRequest("http://localhost:7878/hello.woof");
		assertTrue("Ensure login page", loginPage.contains("<title>Login</title>"));

		// Login
		String helloPage = this
				.doHttpRequest("https://localhost:7979/login-login.woof?username=Daniel&password=Daniel");
		assertTrue("Ensure hello page with login", helloPage.contains("<p>Hi Daniel</p>"));
	}

	public void testLogout() throws Exception {

		// Login (also starts server)
		this.testLogin();

		// Logout
		String logoutPage = this.doHttpRequest("http://localhost:7878/hello-logout.woof");
		assertTrue("Ensure logout page", logoutPage.contains("<title>Logout</title>"));

		// Attempt to go back to page (but require login)
		String loginPage = this.doHttpRequest("http://localhost:7878/hello.woof");
		assertTrue("Ensure login page", loginPage.contains("<title>Login</title>"));
	}

	private String doHttpRequest(String url) throws IOException {

		// Obtain the page
		HttpResponse response = this.client.execute(new HttpGet(url));
		String page = EntityUtils.toString(response.getEntity());

		// Return the page
		return page;
	}
	// END SNIPPET: tutorial

}