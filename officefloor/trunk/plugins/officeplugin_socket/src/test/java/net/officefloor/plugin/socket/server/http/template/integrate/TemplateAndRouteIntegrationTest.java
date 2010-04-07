/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.template.integrate;

import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.test.issues.FailCompilerIssues;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.socket.server.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplate;
import net.officefloor.plugin.socket.server.http.template.route.HttpTemplateRouteWorkSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Ensure integration of {@link HttpTemplateWorkSource} and
 * {@link HttpTemplateRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateAndRouteIntegrationTest extends TestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {

		// Obtain location of configuration
		String officeFloorConfiguration = this.getClass().getPackage()
				.getName().replace('.', '/')
				+ "/Configuration.officefloor";

		// Compile the OfficeFloor
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler();
		compiler.addSourceAliases();
		compiler.setCompilerIssues(new FailCompilerIssues());
		this.officeFloor = compiler.compile(officeFloorConfiguration);

		// Open the OfficeFloor
		this.officeFloor.openOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure that {@link HttpTemplate} link is routed to the handling
	 * {@link Task}.
	 */
	public void testRoute() throws Exception {

		// Create HTTP Client
		HttpClient client = new HttpClient();

		// Request the initial page (PageOne)
		Properties initialPage = this.doRequest(
				"/PageTwo.HttpTemplate-PageTwo.ofp/link.task", client);
		assertEquals("Incorrect initial page", "One", initialPage
				.getProperty("page"));

		// Follow link to get second page
		String secondPageLink = initialPage.getProperty("link");
		Properties secondPage = this.doRequest(secondPageLink, client);
		assertEquals("Incorrect second page", "Two", secondPage
				.getProperty("page"));

		// Follow link to get first page
		String firstPageLink = secondPage.getProperty("link");
		Properties firstPage = this.doRequest(firstPageLink, client);
		assertEquals("Incorrect first page", "One", firstPage
				.getProperty("page"));
	}

	/**
	 * Does the request.
	 * 
	 * @param uriPath
	 *            URI path.
	 * @param client
	 *            {@link HttpClient}.
	 * @return Properties of the returned page.
	 */
	private Properties doRequest(String uriPath, HttpClient client)
			throws Exception {

		// Do the request
		GetMethod method = new GetMethod("http://localhost:10101" + uriPath);
		try {
			int status = client.executeMethod(method);
			assertEquals("Must be successful", 200, status);

			// Load properties from response
			Properties properties = new Properties();
			properties.load(method.getResponseBodyAsStream());

			// Return the properties
			return properties;

		} finally {
			method.releaseConnection();
		}
	}

}