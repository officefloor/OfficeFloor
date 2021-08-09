/*-
 * #%L
 * JSON default for Web
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

package net.officefloor.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.compile.WebCompileOfficeFloor;

/**
 * Ensure can use Jackson to parse and response with JSON.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultJsonTest extends OfficeFrameTestCase {

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compile = new WebCompileOfficeFloor();

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
		this.compile.mockHttpServer((server) -> this.server = server);
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can parse and response with JSON via Jackson.
	 */
	public void testJacksonJson() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "POST", "/path", MockJacksonJson.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path").method(HttpMethod.POST)
				.header("Content-Type", "application/json").entity("{ \"input\": \"INPUT\" }"));
		response.assertResponse(200, "{\"output\":\"OUTPUT\"}");
	}

	public static class MockJacksonJson {
		public void service(InputObject input, ObjectResponse<OutputObject> response) {
			assertEquals("Incorrect JSON input", "INPUT", input.getInput());
			response.send(new OutputObject("OUTPUT"));
		}
	}

	/**
	 * Ensure can decorate the {@link ObjectMapper}.
	 */
	public void testJacksonDecorateRequest() throws Exception {

		// Enable decoration
		MockObjectMapperParserDecorator.isDecorate = true;
		try {
			// Configure the server
			this.compile.web((context) -> {
				context.link(false, "POST", "/path", MockJacksonJson.class);
			});
			this.officeFloor = this.compile.compileAndOpenOfficeFloor();

			// Send the request
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path").method(HttpMethod.POST)
					.header("Content-Type", "application/json").entity("{ \"input\": \"INPUT\", \"ignored\": true }"));
			response.assertResponse(200, "{\"output\":\"OUTPUT\"}");

		} finally {
			MockObjectMapperParserDecorator.isDecorate = false;
		}
	}

	/**
	 * Ensure can decorate the {@link ObjectMapper}.
	 */
	public void testJacksonDecorateResponse() throws Exception {

		// Enable decoration
		MockObjectMapperResponderDecorator.isDecorate = true;
		try {

			// Configure the server
			this.compile.web((context) -> {
				context.link(false, "POST", "/path", MockJacksonJson.class);
			});
			this.officeFloor = this.compile.compileAndOpenOfficeFloor();

			// Send the request
			MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path").method(HttpMethod.POST)
					.header("Content-Type", "application/json").entity("{ \"input\": \"INPUT\" }"));
			response.assertResponse(200, "{" + END_OF_LINE + "  \"output\" : \"OUTPUT\"" + END_OF_LINE + "}");

		} finally {
			MockObjectMapperResponderDecorator.isDecorate = false;
		}
	}

	@Data
	@HttpObject
	public static class InputObject {
		private String input;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OutputObject {
		private String output;
	}

	/**
	 * Ensure can handle exception in JSON.
	 */
	public void testJacksonException() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "/path", MockJacksonException.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request
		MockHttpResponse response = this.server
				.send(MockHttpServer.mockRequest("/path").header("accept", "application/json"));
		response.assertResponse(500, "{\"error\":\"ERROR with unsafe \\\" value\"}");
	}

	public static class MockJacksonException {
		public void service() throws Exception {
			throw new Exception("ERROR with unsafe \" value");
		}
	}

}
