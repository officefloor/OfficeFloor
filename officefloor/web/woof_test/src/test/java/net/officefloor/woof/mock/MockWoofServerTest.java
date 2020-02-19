/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.mock;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.web.build.HttpInput;
import net.officefloor.woof.MockSection;
import net.officefloor.woof.MockSection.MockJsonObject;
import net.officefloor.woof.WoofLoaderSettings;
import net.officefloor.woof.mock.MockWoofServer.MockWoofInput;

/**
 * Tests the {@link WoofLoaderSettings}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerTest extends OfficeFrameTestCase {

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server;

	@Override
	protected void setUp() throws Exception {

		// Start WoOF application for testing
		this.server = MockWoofServer.open();
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.close();
		}
	}

	/**
	 * Ensure able to access template.
	 */
	public void testTemplate() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/template"));
		response.assertResponse(200, "TEMPLATE");
	}

	/**
	 * Ensure able to utilise configured objects.
	 */
	public void testObjects() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/objects"));
		response.assertResponse(200, "{\"message\":\"mock\"}");
	}

	/**
	 * Enable able to serve static resource.
	 */
	public void testResource() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/resource.html"));
		response.assertResponse(200, "RESOURCE");
	}

	/**
	 * Ensure runs with different {@link Team}.
	 */
	public void testTeam() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/teams"));
		response.assertResponse(200, "\"DIFFERENT THREAD\"");
	}

	/**
	 * Ensure can send and verify JSON.
	 */
	public void testJson() throws Exception {
		MockJsonObject object = new MockJsonObject("MOCK JSON");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/json", object));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		response.assertJson(200, object);
	}

	/**
	 * Ensure can send and verify JSON.
	 */
	public void testGetJson() throws Exception {
		MockJsonObject object = new MockJsonObject("MOCK JSON");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/json", object));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		MockJsonObject jsonObject = response.getJson(200, MockJsonObject.class);
		assertEquals("Incorrect JSON text", "MOCK JSON", jsonObject.getText());
	}

	/**
	 * Ensure can use convenient JSON {@link HttpException} assertion.
	 */
	public void testJsonHttpException() throws Exception {
		Throwable failure = new HttpException(HttpStatus.FORBIDDEN, "Mock Failure");
		MockSection.failure = failure;
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/failure"));
		assertEquals("Should be correct status", 403, response.getStatus().getStatusCode());
		response.assertJsonError(failure);
	}

	/**
	 * Ensure can use convenient JSON error assertion.
	 */
	public void testJsonError() throws Exception {
		Throwable failure = new IOException("Mock Failure");
		MockSection.failure = failure;
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/failure"));
		assertEquals("Should be correct status", 500, response.getStatus().getStatusCode());
		response.assertJsonError(failure);
	}

	/**
	 * Ensure can handle multiple requests.
	 */
	public void testMultipleRequests() throws Exception {

		// Run multiple requests ensuring appropriately handles
		MockHttpResponse response = null;
		for (int i = 0; i < 100; i++) {

			// Undertake request
			MockHttpRequestBuilder request = MockWoofServer.mockRequest("/path?param=" + i);
			if (response != null) {
				request.cookies(response);
			}
			response = this.server.send(request);
			response.assertResponse(200, "param=" + i + ", previous=" + (i - 1) + ", object=mock");
		}
	}

	/**
	 * Ensure can configure {@link MockWoofServer}.
	 */
	public void testOverrideConfiguration() throws Exception {

		// Start with additional functionality
		this.server.close();
		this.server = MockWoofServer.open((context, compiler) -> {
			context.notLoadWoof();
			context.extend((woofContext) -> {
				OfficeArchitect office = woofContext.getOfficeArchitect();

				// Add the section
				OfficeSection section = office.addOfficeSection("SECTION", ClassSectionSource.class.getName(),
						OverrideSection.class.getName());

				// Configure servicing the request
				HttpInput input = woofContext.getWebArchitect().getHttpInput(false, "GET", "/");
				office.link(input.getInput(), section.getOfficeSectionInput("service"));
			});
		});

		// Ensure can obtain overridden response
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest());
		response.assertResponse(200, "TEST");
	}

	public static class OverrideSection {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Ensure can setup servicing a particular port for testing.
	 */
	public void testMockSocketServicing() throws Exception {

		// Start with additional functionality
		this.server.close();
		try (OfficeFloor officeFloor = MockWoofServer.open(7171, 7272, MockServicerSection.class)) {

			// Ensure can obtain HTTP response
			try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
				HttpResponse response = client.execute(new HttpGet("http://localhost:7171"));
				assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
				assertEquals("Incorrect response", "MOCK", EntityUtils.toString(response.getEntity()));
			}

			// Ensure can obtain HTTPS response
			try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
				HttpPost post = new HttpPost("https://localhost:7272/path");
				post.setEntity(new StringEntity("1"));
				HttpResponse response = client.execute(post);
				assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
				assertEquals("Incorrect response", "MOCK-1", EntityUtils.toString(response.getEntity()));
			}
		}
	}

	/**
	 * Ensure can setup HTTP only.
	 */
	public void testHttpOnly() throws Exception {

		// Start with additional functionality
		this.server.close();
		try (OfficeFloor officeFloor = MockWoofServer.open(7171, MockServicerSection.class)) {

			// Ensure can obtain HTTP response
			try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
				HttpResponse response = client.execute(new HttpGet("http://localhost:7171"));
				assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
				assertEquals("Incorrect response", "MOCK", EntityUtils.toString(response.getEntity()));
			}
		}
	}

	public static class MockServicerSection {

		@MockWoofInput
		public void get(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("MOCK");
		}

		@MockWoofInput(secure = true, method = "POST", path = "/path")
		public void post(ServerHttpConnection connection) throws IOException {
			Reader reader = new InputStreamReader(connection.getRequest().getEntity(), Charset.forName("UTF-8"));
			StringWriter buffer = new StringWriter();
			for (int character = reader.read(); character != -1; character = reader.read()) {
				buffer.write(character);
			}
			connection.getResponse().getEntityWriter().write("MOCK-" + buffer.toString());
		}
	}

}
