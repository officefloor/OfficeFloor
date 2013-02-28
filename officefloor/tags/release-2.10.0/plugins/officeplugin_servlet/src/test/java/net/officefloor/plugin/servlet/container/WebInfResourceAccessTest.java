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
package net.officefloor.plugin.servlet.container;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Confirm whether the {@link Servlet} container allows access to the WEB-INF
 * directory.
 * 
 * @author Daniel Sagenschneider
 */
public class WebInfResourceAccessTest extends OfficeFrameTestCase {

	/**
	 * Asserts the resource.
	 * 
	 * @param request
	 *            {@link HttpServletRequest}.
	 * @param resourcePath
	 *            Path to the resource.
	 * @param expectedResourceContent
	 *            Expected content of the resource.
	 */
	private static void assertResource(HttpServletRequest request,
			String resourcePath, String expectedResourceContent)
			throws IOException {

		// Validate obtain the resource
		InputStream resource = request.getServletContext().getResourceAsStream(
				resourcePath);
		assertNotNull("Should have " + resourcePath + " resource", resource);

		// Validate content of resource
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int value = resource.read(); value != -1; value = resource.read()) {
			buffer.write(value);
		}
		String resourceContent = new String(buffer.toByteArray());
		assertEquals("Incorrect content for resource " + resourcePath,
				expectedResourceContent, resourceContent);
	}

	/**
	 * Ensure able to access the WEB-INF content.
	 */
	public void testAccessWebInfContent() throws Exception {

		int port = MockHttpServer.getAvailablePort();

		// Start the HTTP container for the HTTP Servlet
		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.setResourceBase(new File(".", "src/test/webapp")
				.getAbsolutePath());
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override
			protected void doPost(HttpServletRequest request,
					HttpServletResponse response) throws ServletException,
					IOException {

				// Validate non WEB-INF resource
				assertResource(request, "/resource.html", "RESOURCE");

				// Validate WEB-INF resource
				assertResource(request, "/WEB-INF/web.xml", "<web-app />");
			}
		}), "/*");
		server.setHandler(context);
		try {
			server.start();

			// Send request to the server
			HttpClient client = new DefaultHttpClient();
			try {
				HttpPost request = new HttpPost("http://localhost:" + port
						+ "/servlet");
				HttpResponse response = client.execute(request);
				TestCase.assertEquals("Expecting response to be successful",
						200, response.getStatusLine().getStatusCode());
			} finally {
				client.getConnectionManager().shutdown();
			}

		} finally {
			// Ensure stop the server
			server.stop();
			server.destroy();
		}
	}

}