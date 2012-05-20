/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.woof.servlet;

import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServlet;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.woof.MockDependency;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Tests the {@link WoofServletFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServletFilterTest extends OfficeFrameTestCase {

	/**
	 * Port {@link Server} is listening on.
	 */
	private int port;

	/**
	 * {@link Server}.
	 */
	private Server server;

	@Override
	protected void setUp() throws Exception {

		// Obtain the port for the application
		this.port = MockHttpServer.getAvailablePort();

		// Obtain the path to the application.woof
		String applicationWoofPath = this.getPackageRelativePath(this
				.getClass()) + "/application.woof";

		// Start servlet container with filter
		this.server = new Server(this.port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.setSessionHandler(new SessionHandler());
		this.server.setHandler(context);

		// Add the WoOF Servlet Filter
		FilterHolder filter = new FilterHolder(new WoofServletFilter());
		filter.setInitParameter(
				WoofServletFilter.PROPERTY_WOOF_CONFIGURATION_LOCATION,
				applicationWoofPath);
		context.addFilter(filter, "/*", FilterMapping.REQUEST);

		// Add Servlet for being filtered
		context.addServlet(MockHttpServlet.class, "/");

		// Start the server
		this.server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop the server
		if (this.server != null) {
			this.server.stop();
		}
	}

	/**
	 * Ensure {@link WoofServletFilter} configures itself.
	 */
	public void testWoofInitiated() throws Exception {

		// Ensure serviced by HTTP template from WoOF configuration
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://localhost:" + this.port + "/test");
		HttpResponse response = client.execute(request);
		assertEquals("Must be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Validate appropriate response from HTTP template
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		response.getEntity().writeTo(buffer);
		String responseText = new String(buffer.toByteArray());
		assertEquals(
				"Incorrect template content",
				"TEMPLATE TEST OnePersonTeam_"
						+ new AutoWire(MockDependency.class).getQualifiedType(),
				responseText);
	}

	/**
	 * Mock template logic class.
	 */
	public static class MockTemplate {
		public MockContent getTemplate(MockDependency dependency) {
			Thread thread = Thread.currentThread();
			return new MockContent(dependency.getMessage() + " "
					+ thread.getName());
		}
	}

	/**
	 * Mock content for the template.
	 */
	public static class MockContent {

		private String text;

		public MockContent(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Mock {@link HttpServlet}.
	 */
	public static class MockHttpServlet extends HttpServlet {
	}

}