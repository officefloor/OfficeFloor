/*-
 * #%L
 * Spring Web MVC Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.spring.webmvc;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can provide {@link OfficeFloor} running of Spring App.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringTest extends OfficeFrameTestCase {

	/**
	 * Ensure can service GET simple.
	 */
	public void testGetSimple() throws Exception {
		this.doSpringTest(MockHttpServer.mockRequest("/simple"), 200, "Simple Spring");
	}

	/**
	 * Ensure can service GET inject.
	 */
	public void testGetInject() throws Exception {
		this.doSpringTest(MockHttpServer.mockRequest("/complex/inject"), 200, "Inject Dependency");
	}

	/**
	 * Ensure can service GET status.
	 */
	public void testGetStatus() throws Exception {
		this.doSpringTest(MockHttpServer.mockRequest("/complex/status"), 201, "Status");
	}

	/**
	 * Ensure can service GET path parameter.
	 */
	public void testGetPathParam() throws Exception {
		this.doSpringTest(MockHttpServer.mockRequest("/complex/path/value"), 200, "Parameter value");
	}

	/**
	 * Ensure can service GET query parameter.
	 */
	public void testGetQueryParam() throws Exception {
		this.doSpringTest(MockHttpServer.mockRequest("/complex/query?param=value"), 200, "Parameter value");
	}

	/**
	 * Ensure can service GET header.
	 */
	public void testGetHeader() throws Exception {
		this.doSpringTest(MockHttpServer.mockRequest("/complex/header").header("header", "value"), 200, "Header value");
	}

	/**
	 * Ensure can service POST.
	 */
	public void testPost() throws Exception {
		this.doSpringTest(MockHttpServer.mockRequest("/complex").method(HttpMethod.POST).entity("value"), 200,
				"Body value");
	}

	/**
	 * Undertakes Spring test.
	 * 
	 * @param request        {@link MockHttpRequestBuilder}.
	 * @param expectedStatus Expected status.
	 * @param expectedEntity Expected entity in response.
	 */
	private void doSpringTest(MockHttpRequestBuilder request, int expectedStatus, String expectedEntity)
			throws Exception {

		// Undertake test
		CompileWoof compile = new CompileWoof(true);
		try (MockWoofServer server = compile.open()) {
			MockHttpResponse response = server.send(request);
			response.assertResponse(expectedStatus, expectedEntity);
		}
	}

}
