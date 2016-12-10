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
package net.officefloor.plugin.woof.servlet.integrate;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.server.Server;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.woof.WoofApplicationExtensionService;
import net.officefloor.plugin.woof.servlet.MockDependency;
import net.officefloor.plugin.woof.servlet.WoofServlet;

/**
 * <p>
 * Abstract tests for the {@link WoofServlet}.
 * <p>
 * This allows testing the {@link WoofServlet} within different JEE Servlet
 * Container implementations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWoofServletTestCase extends OfficeFrameTestCase {

	/**
	 * Port {@link Server} is listening on.
	 */
	private int port;

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

	/**
	 * Starts the server.
	 * 
	 * @param contextPath
	 *            Context path.
	 * @return Port the server is running on.
	 */
	protected abstract int startServer(String contextPath) throws Exception;

	/**
	 * Stops the server.
	 */
	protected abstract void stopServer() throws Exception;

	/**
	 * Ensure {@link WoofServlet} configures itself to service a request.
	 */
	public void testServiceRequest() throws Exception {
		this.doServiceRequestTest("");
	}

	/**
	 * Ensure {@link WoofServlet} respects the {@link ServletContext}.
	 */
	public void testServiceRequestWithinContext() throws Exception {
		this.doServiceRequestTest("/path");
	}

	/**
	 * Ensure can service a HTTP request.
	 * 
	 * @param contextPath
	 *            Context path.
	 */
	private void doServiceRequestTest(String contextPath) throws Exception {

		// Start Server (with context path)
		this.port = this.startServer(contextPath);

		// Validate appropriate response from HTTP template
		String responseText = this.doGetEntity(contextPath, "/test.woof");
		assertEquals("Incorrect template content",
				"TEMPLATE TEST OnePersonTeam_" + new AutoWire(MockDependency.class).getQualifiedType(), responseText);
	}

	/**
	 * Ensure {@link WoofApplicationExtensionService} instances are not loaded
	 * by the {@link WoofServlet}.
	 */
	public void testNoWoofApplicationExtension() throws Exception {

		// Start Server
		this.port = this.startServer("");

		// Ensure not load WoOF Application Extensions for Filter
		String responseText = this.doGetEntity("", "/chain.html");
		assertEquals("Should obtain resource and not be serviced by chain servicer", "NOT CHAINED", responseText);
	}

	/*
	 * =================== Setup/Teardown/Helper ==========================
	 */

	/**
	 * Executes a {@link HttpGet} against the URI.
	 * 
	 * @param contextPath
	 *            Context path.
	 * @param uri
	 *            URI for the {@link HttpGet} request.
	 * @return Entity of response as text.
	 */
	private String doGetEntity(String contextPath, String uri) throws Exception {

		// Ensure serviced by HTTP template from WoOF configuration
		HttpGet request = new HttpGet("http://localhost:" + this.port + contextPath + uri);
		HttpResponse response = this.client.execute(request);
		assertEquals("Must be successful", 200, response.getStatusLine().getStatusCode());

		// Obtain the response entity as text
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());

		// Return the response entity
		return responseText;
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.close();

		} finally {
			// Stop the server
			this.stopServer();
		}
	}

}