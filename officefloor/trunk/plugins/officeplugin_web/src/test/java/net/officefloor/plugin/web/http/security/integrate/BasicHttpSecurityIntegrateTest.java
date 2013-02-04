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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
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
import net.officefloor.plugin.web.http.security.scheme.BasicHttpSecuritySource;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.security.store.PasswordFileManagedObjectSource;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Integrate tests the {@link BasicHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class BasicHttpSecurityIntegrateTest extends OfficeFrameTestCase {

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

		// HTTP Security
		HttpSecurityAutoWireSection security = source
				.setHttpSecurity(BasicHttpSecuritySource.class);
		security.addProperty(BasicHttpSecuritySource.PROPERTY_REALM,
				"TestRealm");

		// Password File Credential Store
		String passwordFilePath = this.findFile(this.getClass(),
				"password-file.txt").getAbsolutePath();
		AutoWireObject passwordFile = source.addManagedObject(
				PasswordFileManagedObjectSource.class.getName(), null,
				new AutoWire(CredentialStore.class));
		passwordFile.addProperty(
				PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH,
				passwordFilePath);

		// Add servicing methods
		AutoWireSection section = source.addSection("SERVICE",
				ClassSectionSource.class.getName(), Servicer.class.getName());
		source.link(security, "Failure", section, "handleFailure");
		source.linkUri("service", section, "service");

		// Start the office
		this.officeFloor = source.openOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
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
			connection.getHttpResponse().getEntityWriter().write("ERROR");
		}
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Create the request
		HttpGet request = new HttpGet("http://localhost:" + PORT + "/service");

		// Should not authenticate (without credentials)
		this.doRequest(request, 401, null);

		// Should authenticate with credentials
		this.client.getCredentialsProvider().setCredentials(
				new AuthScope(null, -1, "TestRealm"),
				new UsernamePasswordCredentials("daniel", "password"));
		this.doRequest(request, 200, "Serviced for daniel");
	}

	/**
	 * Asserts the response from the {@link HttpGet}.
	 * 
	 * @param request
	 *            {@link HttpGet}.
	 * @param expectedStatus
	 *            Expected status.
	 * @param expectedBodyContent
	 *            Expected body content.
	 */
	private void doRequest(HttpGet request, int expectedStatus,
			String expectedBodyContent) {
		try {
			// Execute the method
			org.apache.http.HttpResponse response = this.client
					.execute(request);
			int status = response.getStatusLine().getStatusCode();
			String body = MockHttpServer.getEntityBody(response);

			// Verify response
			assertEquals("Incorrect response body", expectedBodyContent, body);
			assertEquals("Should be successful", expectedStatus, status);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}