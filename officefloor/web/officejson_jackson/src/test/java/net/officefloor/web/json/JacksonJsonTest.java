/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.json;

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
public class JacksonJsonTest extends OfficeFrameTestCase {

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

	@Data
	@HttpObject
	@NoArgsConstructor
	@AllArgsConstructor
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

	/**
	 * Ensure {@link JacksonHttpObjectParserServiceFactory} registered with other.
	 */
	public void testOtherContentType() throws Exception {

		// Configure the server
		this.compile.web((context) -> {
			context.link(false, "POST", "/path", MockJacksonJson.class);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();

		// Send the request (ensure handle JSON)
//		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path").method(HttpMethod.POST)
//				.header("Content-Type", "application/json").entity("{ \"input\": \"INPUT\" }"));
//		response.assertResponse(200, "{\"output\":\"OUTPUT\"}");

		// Send the request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path").method(HttpMethod.POST)
				.header("Content-Type", "test/mock").entity("{ \"input\": \"OVERWRITE\" }"));
		response.assertResponse(200, "MOCK");
	}

}