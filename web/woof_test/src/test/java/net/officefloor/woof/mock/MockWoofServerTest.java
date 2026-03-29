/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.model.Statement;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.system.SystemPropertiesRule;
import net.officefloor.web.build.HttpInput;
import net.officefloor.woof.MockSection;
import net.officefloor.woof.MockSection.MockJsonObject;
import net.officefloor.woof.WoOF;
import net.officefloor.woof.WoofLoaderSettings;
import net.officefloor.woof.mock.MockWoofServer.MockWoofInput;

/**
 * Tests the {@link WoofLoaderSettings}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerTest {

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server;

	@BeforeEach
	protected void setUp() throws Exception {

		// Start WoOF application for testing
		this.server = MockWoofServer.open();
	}

	@AfterEach
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.close();
		}
	}

	/**
	 * Ensure able to access template.
	 */
	@Test
	public void template() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/template"));
		response.assertResponse(200, "TEMPLATE");
	}

	/**
	 * Ensure able to utilise configured objects.
	 */
	@Test
	public void objects() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/objects"));
		response.assertResponse(200, "{\"message\":\"mock\"}");
	}

	/**
	 * Enable able to serve static resource.
	 */
	@Test
	public void resource() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/resource.html"));
		response.assertResponse(200, "RESOURCE");
	}

	/**
	 * Ensure runs with different {@link Team}.
	 */
	@Test
	public void team() throws Exception {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/teams"));
		response.assertResponse(200, "\"DIFFERENT THREAD\"");
	}

	/**
	 * Ensure can send and verify JSON.
	 */
	@Test
	public void json() throws Exception {
		MockJsonObject object = new MockJsonObject("MOCK JSON");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/json", object));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");
		response.assertJson(200, object);
	}

	/**
	 * Ensure can send and verify JSON.
	 */
	@Test
	public void getJson() throws Exception {
		MockJsonObject object = new MockJsonObject("MOCK JSON");
		MockWoofResponse response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/json", object));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");
		MockJsonObject jsonObject = response.getJson(200, MockJsonObject.class);
		assertEquals("MOCK JSON", jsonObject.getText(), "Incorrect JSON text");
	}

	/**
	 * Ensure can use convenient JSON {@link HttpException} assertion.
	 */
	@Test
	public void jsonHttpException() throws Exception {
		Throwable failure = new HttpException(HttpStatus.FORBIDDEN, "Mock Failure");
		MockSection.failure = failure;
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/failure"));
		assertEquals(403, response.getStatus().getStatusCode(), "Should be correct status");
		response.assertJsonError(failure);
	}

	/**
	 * Ensure can use convenient JSON error assertion.
	 */
	@Test
	public void jsonError() throws Exception {
		Throwable failure = new IOException("Mock Failure");
		MockSection.failure = failure;
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/failure"));
		assertEquals(500, response.getStatus().getStatusCode(), "Should be correct status");
		response.assertJsonError(failure);
	}

	/**
	 * Ensure can handle multiple requests.
	 */
	@Test
	public void multipleRequests() throws Exception {

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
	 * Ensure can wrap compile and open.
	 */
	@Test
	public void wrap() throws Exception {

		// Clear state
		WrapManagedObjectSource.WRAP.remove();
		WrapManagedObjectSource.META_DATA.remove();

		// Start with wrap
		this.server.close();
		this.server.wrap((context) -> {

			// Specify wrap
			WrapManagedObjectSource.WRAP.set("Wrap");

			// Undertake with thread locals
			context.compileAndOpen();
		});
		MockWoofServer.open(this.server, (context, compiler) -> {
			context.notLoadWoof();
			context.extend((woofContext) -> {
				woofContext.getOfficeArchitect()
						.addOfficeManagedObjectSource("WRAP", WrapManagedObjectSource.class.getName())
						.addOfficeManagedObject("WRAP", ManagedObjectScope.THREAD);
			});
		});

		// Ensure settings
		assertEquals("MetaData", WrapManagedObjectSource.META_DATA.get());
		assertEquals("Wrap", WrapManagedObjectSource.WRAP.get());
	}

	public static class WrapManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private static final ThreadLocal<String> WRAP = new ThreadLocal<>();

		private static final ThreadLocal<String> META_DATA = new ThreadLocal<>();

		/*
		 * ================= ManagedObjectSource ===============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			META_DATA.set("MetaData");
			context.setObjectClass(WrapManagedObjectSource.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return fail("Should not obtain");
		}
	}

	/**
	 * Ensure can configure {@link MockWoofServer}.
	 */
	@Test
	public void overrideConfiguration() throws Exception {

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
	@Test
	public void mockSocketServicing() throws Exception {

		// Start with additional functionality
		this.server.close();
		try (OfficeFloor officeFloor = MockWoofServer.open(7171, 7272, MockServicerSection.class)) {

			// Ensure can obtain HTTP response
			try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
				HttpResponse response = client.execute(new HttpGet("http://localhost:7171"));
				assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");
				assertEquals("MOCK", EntityUtils.toString(response.getEntity()), "Incorrect response");
			}

			// Ensure can obtain HTTPS response
			try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient(true)) {
				HttpPost post = new HttpPost("https://localhost:7272/path");
				post.setEntity(new StringEntity("1"));
				HttpResponse response = client.execute(post);
				assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");
				assertEquals("MOCK-1", EntityUtils.toString(response.getEntity()), "Incorrect response");
			}
		}
	}

	/**
	 * Ensure can setup HTTP only.
	 */
	@Test
	public void httpOnly() throws Exception {

		// Start with additional functionality
		this.server.close();
		try (OfficeFloor officeFloor = MockWoofServer.open(7171, MockServicerSection.class)) {

			// Ensure can obtain HTTP response
			try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
				HttpResponse response = client.execute(new HttpGet("http://localhost:7171"));
				assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");
				assertEquals("MOCK", EntityUtils.toString(response.getEntity()), "Incorrect response");
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

	/**
	 * Ensure no external configuration. As testing do not want false positives due
	 * to external changes.
	 */
	@Test
	public void noExternalConfiguration() throws Throwable {

		// Will create server in context
		this.server.close();

		// Run with overrides
		new SystemPropertiesRule().property(WoOF.DEFAULT_OFFICE_PROFILES, "external")
				.property("OFFICE.Property.function.property.override", "SYSTEM_OVERRIDE").run(() -> {

					// Expected values
					final String EXTERNAL_OVERRIDE_ENTITY = "SYSTEM_OVERRIDE, EXTERNAL_OVERRIDE, to be overridden by test profile";
					final String TEST_ONLY_ENTITY = "property to be overridden, to be overridden by profile, TEST_OVERRIDE";
					final String RULE_ENTITY = "RULE_PROPERTY, RULE_OVERRIDE, TEST_OVERRIDE";

					// Running non-mock so should override
					try (OfficeFloor officeFloor = WoOF.open(7171, -1)) {
						try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
							HttpResponse httpResponse = client.execute(new HttpGet("http://localhost:7171/property"));
							assertEquals(200, httpResponse.getStatusLine().getStatusCode(), "Should be successful");
							assertEquals(EXTERNAL_OVERRIDE_ENTITY, EntityUtils.toString(httpResponse.getEntity()),
									"Incorrect response");
						}
					}

					// Ensure no external override but with default test profile
					this.server = MockWoofServer.open();
					MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/property"));
					response.assertResponse(200, TEST_ONLY_ENTITY);

					// Again no external overrides but with test profile
					try (OfficeFloor officeFloor = MockWoofServer.open(7171, -1)) {
						try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
							HttpResponse httpResponse = client.execute(new HttpGet("http://localhost:7171/property"));
							assertEquals(200, httpResponse.getStatusLine().getStatusCode(), "Should be successful");
							assertEquals(TEST_ONLY_ENTITY, EntityUtils.toString(httpResponse.getEntity()),
									"Incorrect response");
						}
					}

					// Ensure rule not external and can add its own profiles and properties
					Closure<MockHttpResponse> ruleResponse = new Closure<>();
					try (MockWoofServerRule rule = new MockWoofServerRule()) {
						rule.profile("rule").property("Property.function.property.override", "RULE_PROPERTY")
								.apply(new Statement() {
									@Override
									public void evaluate() throws Throwable {
										ruleResponse.value = rule.send(MockHttpServer.mockRequest("/property"));
									}
								}, null).evaluate();
					}
					ruleResponse.value.assertResponse(200, RULE_ENTITY);
				});
	}

}
