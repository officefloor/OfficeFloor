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
package net.officefloor.plugin.servlet.webxml.integrate;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import net.officefloor.plugin.servlet.container.integrate.MockHttpServletServer;
import net.officefloor.plugin.servlet.webxml.AbstractWebXmlTestCase;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * Integration testing for configuring a {@link Servlet} application from a
 * <code>web.xml</code>.
 * 
 * @author Daniel Sagenschneider
 */
public class WebXmlIntegrateTest extends AbstractWebXmlTestCase {

	@Override
	protected void setUp() throws Exception {
		// Start servlet application for configuration based on test name
		String webXmlFileName = this.getName();
		webXmlFileName = webXmlFileName.substring("test".length()) + ".xml";
		this.startServletApplication(webXmlFileName);
	}

	/**
	 * Ensure can invoke a {@link Servlet} application with only a single
	 * {@link Servlet}.
	 */
	public void testSingleServlet() {
		this.doTest("/path", "SingleServlet");
	}

	/**
	 * Ensure {@link Filter} the {@link Servlet}.
	 */
	public void testFiltering() {
		this.doTest("/path", "Filter Dispatch Filter Handle");
	}

	/**
	 * Ensure can configure a MIME mapping.
	 */
	public void testMimeMappings() {
		this.doTest("/mime-mapping?extension=test", "plain/test");
	}

	/**
	 * Ensure can configure context params and init params.
	 */
	public void testParams() {
		this.doTest("/context-param?param=test", "available");
	}

	/**
	 * Undertakes test by sending request to server and validating the response.
	 * 
	 * @param path
	 *            Path for requesting on the server.
	 * @param expectedResponse
	 *            Expected body of response.
	 */
	private void doTest(String path, String expectedResponse) {
		try {

			// Send the request
			HttpGet request = new HttpGet(this.getServerUrl() + path);
			HttpResponse response = this.createHttpClient().execute(request);

			// Validate response
			MockHttpServletServer.assertHttpResponse(response, 200,
					expectedResponse);

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}