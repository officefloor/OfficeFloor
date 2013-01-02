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

import junit.framework.TestCase;
import net.officefloor.frame.api.manage.OfficeFloor;

import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

/**
 * Ensure that {@link OfficeFloor} can be integrated into a Servlet.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletIntegrationTest extends TestCase {

	/**
	 * {@link ServletTester} to aid in testing.
	 */
	private final ServletTester tester = new ServletTester();

	@Override
	protected void setUp() throws Exception {
		// Create the servlet tester
		this.tester.setContextPath("/");
		this.tester.addServlet(MockServlet.class, "/");
		this.tester.start();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop the servlet tester
		this.tester.stop();
	}

	/**
	 * Ensure that able to lookup and run {@link OfficeFloor} from within a
	 * Servlet.
	 */
	public void testOfficeFloorWithinServlet() throws Exception {

		// Send request
		HttpTester request = new HttpTester();
		request.setMethod("GET");
		request.addHeader("Host", "tester");
		request.setVersion("HTTP/1.0");
		request.setURI("/");

		// Obtain response
		HttpTester response = new HttpTester();
		response.parse(this.tester.getResponses(request.generate()));

		// Ensure successful response
		assertEquals("Unsuccessful request", 200, response.getStatus());

		// Ensure OfficeFloor task invoked
		String responseContent = response.getContent();		
		boolean isTaskInvoked = Boolean.parseBoolean(responseContent.trim());
		assertTrue("Task should be invoked", isTaskInvoked);
	}

}