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
package net.officefloor.plugin.servlet.container.integrate;

import net.officefloor.plugin.servlet.container.source.JspWorkSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.server.HttpServicerFunction;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * Ensure can service {@link HttpRequest} with JSP.
 * 
 * @author Daniel Sagenschneider
 */
public class JspIntegrateTest extends MockHttpServletServer {

	@Override
	public HttpServicerFunction buildServlet(String servletContextName,
			String httpName, String requestAttributesName, String sessionName,
			String securityName) {
		// Construct and return JSP task
		return this.constructHttpServlet("JSP", servletContextName, httpName,
				requestAttributesName, sessionName, securityName,
				JspWorkSource.class);
	}

	/**
	 * Ensure service with simple JSP.
	 */
	public void testSimpleRequest() throws Exception {

		// Send request
		HttpClient client = this.createHttpClient();
		HttpGet request = new HttpGet(this.getServerUrl() + "/Simple.jsp");
		HttpResponse response = client.execute(request);

		// Validate the response
		assertHttpResponse(response, 200, "Hello World");
	}

	/**
	 * Ensure service with JSP containing a scriptlet.
	 */
	public void testScriplet() throws Exception {

		// Obtain the user name
		String userName = System.getProperty("user.name");
		assertNotNull("Must have username", userName);

		// Send request
		HttpClient client = this.createHttpClient();
		HttpGet request = new HttpGet(this.getServerUrl() + "/Scriptlet.jsp");
		HttpResponse response = client.execute(request);

		// Validate the response
		assertHttpResponse(response, 200, "Hello " + userName);
	}

	/**
	 * Ensure service by JSP include.
	 */
	public void testInclude() throws Exception {

		// Send request
		HttpClient client = this.createHttpClient();
		HttpGet request = new HttpGet(this.getServerUrl() + "/Include.jsp");
		HttpResponse response = client.execute(request);

		// Validate the response
		assertHttpResponse(response, 200, "Hello World");
	}

	/**
	 * Ensure service defining method in JSP.
	 */
	public void testDefineMethod() throws Exception {

		// Send request
		HttpClient client = this.createHttpClient();
		HttpGet request = new HttpGet(this.getServerUrl() + "/DefineMethod.jsp");
		HttpResponse response = client.execute(request);

		// Validate the response
		assertHttpResponse(response, 200, "Hello World");
	}

	/**
	 * Ensure service with JSP tag.
	 */
	public void testTag() throws Exception {

		// Send request
		HttpClient client = this.createHttpClient();
		HttpGet request = new HttpGet(this.getServerUrl() + "/Tag.jsp");
		HttpResponse response = client.execute(request);

		// Validate the response
		assertHttpResponse(response, 200, "Hello World");
	}

}