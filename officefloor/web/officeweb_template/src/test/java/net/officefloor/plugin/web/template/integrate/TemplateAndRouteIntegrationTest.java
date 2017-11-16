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
package net.officefloor.plugin.web.template.integrate;

import java.util.Properties;

import junit.framework.TestCase;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.plugin.web.template.WebTemplateManagedFunctionSource;
import net.officefloor.plugin.web.template.parse.ParsedTemplate;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Ensure integration of {@link WebTemplateManagedFunctionSource} and
 * {@link HttpTemplateRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateAndRouteIntegrationTest extends TestCase {

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

		// Obtain location of configuration
		String officeFloorConfiguration = this.getClass().getPackage().getName().replace('.', '/')
				+ "/Configuration.officefloor";

		// Compile the OfficeFloor
		WebCompileOfficeFloor compile = new WebCompileOfficeFloor();
		compile.officeFloor((context) -> {
			this.server = MockHttpServer
					.configureMockHttpServer(context.getDeployedOffice().getDeployedOfficeInput("ROUTE", "route"));
		});
		compile.web((context) -> {
			
		});
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Open the OfficeFloor
		this.officeFloor.openOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop server
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure that {@link ParsedTemplate} link is routed to the handling
	 * {@link ManagedFunction}.
	 */
	public void testRoute() throws Exception {

		// Request the initial page (PageOne)
		Properties initialPage = this.doRequest("/PageTwo-link");
		assertEquals("Incorrect initial page", "One", initialPage.getProperty("page"));

		// Follow link to get second page
		String secondPageLink = initialPage.getProperty("link");
		Properties secondPage = this.doRequest(secondPageLink);
		assertEquals("Incorrect second page", "Two", secondPage.getProperty("page"));

		// Follow link to get first page
		String firstPageLink = secondPage.getProperty("link");
		Properties firstPage = this.doRequest(firstPageLink);
		assertEquals("Incorrect first page", "One", firstPage.getProperty("page"));
	}

	/**
	 * Ensure that root {@link ParsedTemplate} link is routed to the handling
	 * {@link ManagedFunction}.
	 */
	public void testRouteRoot() throws Exception {

		// Root page link
		Properties linkedPage = this.doRequest("/-link");
		assertEquals("Incorrect root page link", "One", linkedPage.getProperty("page"));
	}

	/**
	 * Ensure that {@link ParsedTemplate} link is routed as canonical path.
	 */
	public void testRouteCanonicalPath() throws Exception {

		// Request the initial page with non-canonical path
		Properties initialPage = this.doRequest("/non-canonical-path/../PageTwo-link/");
		assertEquals("Incorrect page", "One", initialPage.getProperty("page"));
	}

	/**
	 * Does the request.
	 * 
	 * @param uriPath
	 *            URI path.
	 * @return Properties of the returned page.
	 */
	private Properties doRequest(String uriPath) throws Exception {

		// Do the request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(uriPath);
		MockHttpResponse response = this.server.send(request);
		int status = response.getHttpStatus().getStatusCode();
		assertEquals("Must be successful", 200, status);

		// Load properties from response
		Properties properties = new Properties();
		properties.load(response.getHttpEntity());

		// Return the properties
		return properties;
	}

}