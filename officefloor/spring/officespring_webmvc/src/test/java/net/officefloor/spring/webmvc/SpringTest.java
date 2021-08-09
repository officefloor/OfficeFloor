/*-
 * #%L
 * Spring Web MVC Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
