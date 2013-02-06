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
package net.officefloor.plugin.web.http.security.integrate;

import java.io.IOException;
import java.io.PrintWriter;

import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpSecurityAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Abstract functionality for integration testing of the
 * {@link HttpSecuritySource} implementations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpSecurityIntegrateTestCase extends
		OfficeFrameTestCase {

	/**
	 * Port to use for testing.
	 */
	private final int PORT = MockHttpServer.getAvailablePort();

	/**
	 * {@link HttpClient} to use for testing.
	 */
	private final DefaultHttpClient client = new DefaultHttpClient();

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Configure the application
		WebAutoWireApplication source = new HttpServerAutoWireOfficeFloorSource(
				PORT);

		// Configure the HTTP Security
		HttpSecurityAutoWireSection security = this
				.configureHttpSecurity(source);

		// Add servicing methods
		AutoWireSection section = source.addSection("SERVICE",
				ClassSectionSource.class.getName(), Servicer.class.getName());
		source.link(security, "Failure", section, "handleFailure");
		source.linkUri("service", section, "service");

		// Start the office
		this.officeFloor = source.openOfficeFloor();
	}

	/**
	 * Configures the {@link HttpSecurityAutoWireSection}.
	 * 
	 * @param application
	 *            {@link WebAutoWireApplication}.
	 * @return Confgured {@link HttpSecurityAutoWireSection}.
	 */
	protected abstract HttpSecurityAutoWireSection configureHttpSecurity(
			WebAutoWireApplication application) throws Exception;

	@Override
	protected void tearDown() throws Exception {
		// Stop client then server
		try {
			this.client.getConnectionManager().shutdown();
		} finally {
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Obtains the {@link HttpClient}.
	 * 
	 * @return {@link HttpClient}.
	 */
	protected DefaultHttpClient getHttpClient() {
		return this.client;
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
	protected void doRequest(String requestUriPath, int expectedStatus,
			String expectedBodyContent) {
		try {

			// Undertake the request
			HttpGet request = new HttpGet("http://localhost:" + PORT + "/"
					+ requestUriPath);

			// Execute the method
			org.apache.http.HttpResponse response = this.client
					.execute(request);
			int status = response.getStatusLine().getStatusCode();
			String body = MockHttpServer.getEntityBody(response);

			// Verify response
			assertEquals("Should be successful. Response: " + body,
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
		 * @param security
		 *            {@link HttpSecurity} dependency ensures authentication
		 *            before servicing.
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @throws IOException
		 *             If fails.
		 */
		public void service(HttpSecurity security,
				ServerHttpConnection connection) throws IOException {
			connection.getHttpResponse().getEntityWriter()
					.write("Serviced for " + security.getRemoteUser());
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
		public void handleFailure(@Parameter Throwable failure,
				ServerHttpConnection connection) throws IOException {
			PrintWriter writer = new PrintWriter(connection.getHttpResponse()
					.getEntityWriter());
			writer.write("ERROR: ");
			failure.printStackTrace(writer);
			writer.flush();
		}
	}

}