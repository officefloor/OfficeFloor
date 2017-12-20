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
package net.officefloor.web.security.integrate;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.HttpCredentials;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Abstract functionality for integration testing of the
 * {@link HttpSecuritySource} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSecurityIntegrateTestCase extends OfficeFrameTestCase {

	/**
	 * Port to use for testing.
	 */
	private final int PORT = 7878;

	/**
	 * {@link CloseableHttpClient} to use for testing.
	 */
	private CloseableHttpClient client = HttpClientTestUtil.createHttpClient(false);

	/**
	 * {@link HttpClientContext}.
	 */
	private HttpClientContext context = new HttpClientContext();

	/**
	 * FIXME: flag indicating if fix for Digest authentication in that the
	 * {@link HttpClient} does not send cookies on authentication request.
	 */
	private boolean isDigestHttpClientCookieBug = false;

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Configure the application
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.officeFloor((context) -> {
			this.server = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
		});
		compiler.web((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Employ security architect
			HttpSecurityArchitect securityArchitect = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web,
					context.getOfficeArchitect(), context.getOfficeSourceContext());

			// Configure the HTTP Security
			HttpSecurityBuilder security = this.configureHttpSecurity(context, securityArchitect);

			// Add servicing methods
			OfficeSection section = context.addSection("SERVICE", Servicer.class);
			web.link(false, "service", section.getOfficeSectionInput("service"));
			web.link(false, "logout", section.getOfficeSectionInput("logout"));

			// Determine if security configured
			if (security != null) {
				context.getOfficeArchitect().link(security.getOutput("Failure"),
						section.getOfficeSectionInput("handleFailure"));
			}

			// Inform web architect of security
			securityArchitect.informWebArchitect();
		});

		// Start the office
		this.officeFloor = compiler.compileAndOpenOfficeFloor();
	}

	/**
	 * Configures the {@link HttpSecurity}.
	 * 
	 * @param context
	 *            {@link CompileWebContext}.
	 * @param securityArchitect
	 *            {@link HttpSecurityArchitect}
	 * @return Configured {@link HttpSecurity}.
	 */
	protected abstract HttpSecurityBuilder configureHttpSecurity(CompileWebContext context,
			HttpSecurityArchitect securityArchitect);

	@Override
	protected void tearDown() throws Exception {
		// Stop client then server
		try {
			this.client.close();
		} finally {
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Use credentials.
	 * 
	 * @param realm
	 *            Security realm.
	 * @param scheme
	 *            Security scheme.
	 * @param username
	 *            User name.
	 * @param password
	 *            Password.
	 * @return {@link CredentialsProvider}.
	 * @throws IOException
	 *             If fails to use credentials.
	 */
	protected CredentialsProvider useCredentials(String realm, String scheme, String username, String password)
			throws IOException {

		// Close the existing client
		this.client.close();

		// Use client with credentials
		HttpClientBuilder builder = HttpClientBuilder.create();
		CredentialsProvider provider = HttpClientTestUtil.configureCredentials(builder, realm, scheme, username,
				password);
		this.client = builder.build();

		// Reset the client context
		this.context = new HttpClientContext();

		// FIXME: determine if HttpClient cookie authentication fix
		if ("Digest".equalsIgnoreCase(scheme)) {
			isDigestHttpClientCookieBug = true;
		}

		// Return the credentials provider
		return provider;
	}

	/**
	 * Asserts the response from the {@link HttpGet}.
	 * 
	 * @param requestUriPath
	 *            Request URI path.
	 * @param expectedStatus
	 *            Expected status.
	 * @param expectedBodyContent
	 *            Expected body content.
	 */
	protected void doRequest(String requestUriPath, int expectedStatus, String expectedBodyContent) {
		try {

			// Undertake the request
			HttpGet request = new HttpGet("http://localhost:" + PORT + "/" + requestUriPath);

			// Execute the method
			org.apache.http.HttpResponse response;
			if (this.isDigestHttpClientCookieBug) {
				// Use the context to keep cookies
				response = this.client.execute(request, this.context);
			} else {
				// Follow normal use
				response = this.client.execute(request);
			}

			/*
			 * FIXME: work-around for bug in HttpClient no cookies on
			 * authentication
			 */
			if (this.isDigestHttpClientCookieBug
					&& (response.getStatusLine().getStatusCode() == HttpStatus.UNAUTHORIZED.getStatusCode())) {
				// Try authentication again, with the cookie
				this.context.getTargetAuthState().reset();
				request.reset();
				response = this.client.execute(request, this.context);
			}

			// Obtain the details of the response
			int status = response.getStatusLine().getStatusCode();
			String body = HttpClientTestUtil.getEntityBody(response);

			// Verify response
			assertEquals(
					"Should be successful. Response: " + body + " [" + response.getStatusLine().getReasonPhrase() + "]",
					expectedStatus, status);
			assertEquals("Incorrect response body", expectedBodyContent, body);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Services the {@link HttpRequest}.
	 */
	public static class Servicer {

		/**
		 * Services the {@link HttpRequest}.
		 * 
		 * @param acessControl
		 *            {@link HttpAccessControl} dependency ensures
		 *            authentication before servicing.
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @throws IOException
		 *             If fails.
		 */
		public void service(HttpAccessControl acessControl, ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter()
					.write("Serviced for " + (acessControl == null ? "guest" : acessControl.getPrincipal().getName()));
		}

		/**
		 * Undertakes logging out.
		 * 
		 * @param authentication
		 *            {@link HttpAuthentication}.
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @throws IOException
		 *             If fails.
		 */
		public void logout(HttpAuthentication<HttpCredentials> authentication, ServerHttpConnection connection)
				throws IOException {

			// Log out
			authentication.logout(null);

			// Indicate logged out
			connection.getResponse().getEntityWriter().write("LOGOUT");
		}

		/**
		 * Handles failure.
		 * 
		 * @param failure
		 *            Failure.
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @throws IOException
		 *             If fails.
		 */
		public void handleFailure(@Parameter Throwable failure, ServerHttpConnection connection) throws IOException {
			PrintWriter writer = new PrintWriter(connection.getResponse().getEntityWriter());
			writer.write("ERROR: ");
			failure.printStackTrace(writer);
			writer.flush();
		}
	}

}