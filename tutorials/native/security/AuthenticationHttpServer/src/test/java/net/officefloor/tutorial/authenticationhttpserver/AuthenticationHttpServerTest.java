package net.officefloor.tutorial.authenticationhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.session.HttpSessionManagedObjectSource;
import net.officefloor.woof.mock.MockWoofServerExtension;

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
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	private WritableHttpCookie session;

	@Test
	public void login() throws Exception {

		// Ensure require login to get to page
		MockHttpResponse loginRedirect = this.server.send(MockHttpServer.mockRequest("/hello"));
		assertEquals(303, loginRedirect.getStatus().getStatusCode(), "Ensure redirect");
		loginRedirect.assertHeader("location", "https://mock.officefloor.net/login");

		// Obtain the session cookie
		this.session = loginRedirect.getCookie(HttpSessionManagedObjectSource.DEFAULT_SESSION_ID_COOKIE_NAME);

		// Login
		MockHttpRequestBuilder loginRequest = MockHttpServer.mockRequest("/login+login?username=Daniel&password=Daniel")
				.secure(true).cookie(this.session.getName(), this.session.getValue());
		MockHttpResponse loggedInRedirect = this.server.send(loginRequest);
		assertEquals(200, loggedInRedirect.getStatus().getStatusCode(),
				"Ensure successful login: " + loggedInRedirect.getEntity(null));

		// Ensure now able to access hello page
		MockHttpResponse helloPage = this.server
				.send(MockHttpServer.mockRequest("/hello").cookie(this.session.getName(), this.session.getValue()));
		String helloPageContent = helloPage.getEntity(null);
		assertEquals(200, helloPage.getStatus().getStatusCode(), "Should obtain hello page: " + helloPageContent);
		assertTrue(helloPageContent.contains("<p>Hi Daniel</p>"), "Ensure hello page with login: " + helloPageContent);
	}

	@Test
	public void logout() throws Exception {

		// Login
		this.login();

		// Logout
		MockHttpResponse logoutRedirect = this.server.send(
				MockHttpServer.mockRequest("/hello+logout").cookie(this.session.getName(), this.session.getValue()));
		assertEquals(303, logoutRedirect.getStatus().getStatusCode(),
				"Ensure logout: " + logoutRedirect.getEntity(null));
		logoutRedirect.assertHeader("location", "/logout");

		// Attempt to go back to page (but require login)
		MockHttpResponse loginPage = this.server
				.send(MockHttpServer.mockRequest("/hello").cookie(this.session.getName(), this.session.getValue()));
		assertEquals(303, loginPage.getStatus().getStatusCode(), "Ensure redirect");
		loginPage.assertHeader("location", "https://mock.officefloor.net/login");
	}
	// END SNIPPET: tutorial

}
