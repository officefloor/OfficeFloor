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
package net.officefloor.plugin.web.http.template.integrate;

import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.web.http.template.HttpTemplateManagedFunctionSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.server.http.HttpTestUtil;

/**
 * Ensure integration of {@link HttpTemplateManagedFunctionSource} and
 * {@link HttpTemplateRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateAndRouteIntegrationTest extends TestCase {

	/**
	 * {@link CloseableHttpClient}.
	 */
	private final CloseableHttpClient client = HttpTestUtil.createHttpClient();

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
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.addSourceAliases();
		compiler.setCompilerIssues(new FailTestCompilerIssues());
		this.officeFloor = compiler.compile(officeFloorConfiguration);

		// Open the OfficeFloor
		this.officeFloor.openOfficeFloor();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop client
			this.client.close();
		} finally {
			// Stop server
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Ensure that {@link HttpTemplate} link is routed to the handling
	 * {@link ManagedFunction}.
	 */
	public void testRoute() throws Exception {

		// Request the initial page (PageOne)
		Properties initialPage = this.doRequest("/PageTwo-link", this.client);
		assertEquals("Incorrect initial page", "One", initialPage.getProperty("page"));

		// Follow link to get second page
		String secondPageLink = initialPage.getProperty("link");
		Properties secondPage = this.doRequest(secondPageLink, this.client);
		assertEquals("Incorrect second page", "Two", secondPage.getProperty("page"));

		// Follow link to get first page
		String firstPageLink = secondPage.getProperty("link");
		Properties firstPage = this.doRequest(firstPageLink, this.client);
		assertEquals("Incorrect first page", "One", firstPage.getProperty("page"));
	}

	/**
	 * Ensure that root {@link HttpTemplate} link is routed to the handling
	 * {@link ManagedFunction}.
	 */
	public void testRouteRoot() throws Exception {

		// Root page link
		Properties linkedPage = this.doRequest("/-link", this.client);
		assertEquals("Incorrect root page link", "One", linkedPage.getProperty("page"));
	}

	/**
	 * Ensure that {@link HttpTemplate} link is routed as canonical path.
	 */
	public void testRouteCanonicalPath() throws Exception {

		// Request the initial page with non-canonical path
		Properties initialPage = this.doRequest("/non-canonical-path/../PageTwo-link/", this.client);
		assertEquals("Incorrect page", "One", initialPage.getProperty("page"));
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
	private Properties doRequest(String uriPath, HttpClient client) throws Exception {

		// Do the request
		HttpGet method = new HttpGet("http://localhost:10101" + uriPath);
		HttpResponse response = client.execute(method);
		int status = response.getStatusLine().getStatusCode();
		assertEquals("Must be successful", 200, status);

		// Load properties from response
		Properties properties = new Properties();
		properties.load(response.getEntity().getContent());

		// Return the properties
		return properties;
	}

}