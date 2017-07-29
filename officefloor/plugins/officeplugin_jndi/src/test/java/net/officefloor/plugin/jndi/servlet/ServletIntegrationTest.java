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
package net.officefloor.plugin.jndi.servlet;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import junit.framework.TestCase;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpTestUtil;

/**
 * Ensure that {@link OfficeFloor} can be integrated into a Servlet.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletIntegrationTest extends TestCase {

	/**
	 * Port of {@link Server}.
	 */
	private final int port = HttpTestUtil.getAvailablePort();

	/**
	 * {@link Server}.
	 */
	private Server server;

	@Override
	protected void setUp() throws Exception {

		// Start servlet container with servlet
		this.server = new Server(this.port);
		WebAppContext context = new WebAppContext();
		context.setBaseResource(
				Resource.newClassPathResource(this.getClass().getPackage().getName().replace('.', '/')));
		context.setContextPath("/");
		context.setSessionHandler(new SessionHandler());
		context.addServlet(MockServlet.class, "/");
		this.server.setHandler(context);
		this.server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop the servlet tester
		this.server.stop();
	}

	/**
	 * Ensure that able to lookup and run {@link OfficeFloor} from within a
	 * Servlet.
	 */
	public void testOfficeFloorWithinServlet() throws Exception {

		try (CloseableHttpClient client = HttpTestUtil.createHttpClient()) {

			// Send request
			HttpResponse response = client.execute(new HttpGet("http://localhost:" + this.port + "/"));

			// Ensure successful response
			assertEquals("Unsuccessful request", 200, response.getStatusLine().getStatusCode());

			// Ensure OfficeFloor task invoked
			String responseContent = HttpTestUtil.getEntityBody(response);
			boolean isTaskInvoked = Boolean.parseBoolean(responseContent.trim());
			assertTrue("Task should be invoked", isTaskInvoked);
		}
	}

}