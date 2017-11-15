/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.plugin.web.template.build;

import java.io.IOException;
import java.io.StringReader;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.test.CompileWebContext;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.build.WebArchitect;

/**
 * Tests the {@link WebTemplater}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplaterTest extends OfficeFrameTestCase {

	/**
	 * Obtains the context path to use in testing.
	 * 
	 * @return Context path to use in testing. May be <code>null</code>.
	 */
	protected String getContextPath() {
		return null;
	}

	/**
	 * Context path to use for testing.
	 */
	private final String contextPath = this.getContextPath();

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compile = new WebCompileOfficeFloor(this.contextPath);

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
		this.compile.officeFloor((context) -> {
			this.server = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can add static template.
	 */
	public void testStaticTemplate() throws Exception {
		this.template("/path", (context, templater) -> templater.addTemplate("/path", new StringReader("TEST")),
				"TEST");
	}

	/**
	 * Ensure can add template with logic.
	 */
	public void testTemplateLogic() throws Exception {
		this.template("/path", (context, templater) -> templater.addTemplate("/path", new StringReader("Data=${value}"))
				.setLogicClass(TemplateLogic.class), "Data=value");
	}

	public static class TemplateLogic {
		public TemplateLogic getData() {
			return this;
		}

		public String getValue() {
			return "value";
		}
	}

	/**
	 * Ensure can have path parameters.
	 */
	public void testDynamicPath() throws Exception {
		this.template("/dynamic/value",
				(context, templater) -> templater.addTemplate("/dynamic/{param}", new StringReader("Data=${value}"))
						.setLogicClass(DynamicPathLogic.class),
				"Data=value");
	}

	public static class DynamicPathLogic {
		private String value;

		public DynamicPathLogic getData(@HttpPathParameter("param") String param) {
			this.value = param;
			return this;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * Ensure can invoke link from template.
	 */
	public void testLink() throws Exception {
		this.template("/path", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/link", new StringReader("Link=#{link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/path-link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/path-link"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section GET /path-link", response.getEntity(null));
	}

	public static class MockSection {
		public void service(ServerHttpConnection connection) throws IOException {
			HttpRequest request = connection.getRequest();
			connection.getResponse().getEntityWriter()
					.write("section  " + request.getMethod().getName() + " " + request.getUri());
		}
	}

	/**
	 * Ensure can invoke link for dynamic path.
	 */
	public void testDynamicLink() throws Exception {
		this.template("/dynamic", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/{param}", new StringReader("Link=#{link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/dynamic-link");

		// Ensure can GET link (use different path parameter)
		MockHttpResponse response = this.server.send(this.mockRequest("/another-link"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section GET /another-link");
	}

	/**
	 * Ensure both GET and POST supported by default for links. Makes easier for
	 * form HTML.
	 */
	public void testGetAndPostDefaults() throws Exception {
		this.template("/default", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/link", new StringReader("Link=#{link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/default-link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/default-link").method(HttpMethod.GET));
		assertEquals("GET link should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect GET response", "section GET /default-link", response.getEntity(null));

		// Ensure can POST link
		response = this.server.send(this.mockRequest("/default-link").method(HttpMethod.POST));
		assertEquals("POST link should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect POST resposne", "section POST /default-link", response.getEntity(null));
	}

	/**
	 * Ensure configure link as POST.
	 */
	public void testPostLinkOnly() throws Exception {
		this.template("/post", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/post", new StringReader("Link=#{POST:link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/post-link");

		// Ensure can POST link
		MockHttpResponse response = this.server.send(this.mockRequest("/post-link").method(HttpMethod.POST));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section POST /post-link");

		// Ensure can not GET link (as specifies only POST)
		response = this.server.send(this.mockRequest("/post-link").method(HttpMethod.GET));
		assertEquals("Should not support GET", 406, response.getStatus().getStatusCode());
	}

	/**
	 * Ensure configure link as PUT. This is typically for Javascript requests.
	 */
	public void testPutJavaScriptLink() throws Exception {
		this.template("/put", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/post", new StringReader("Link=#{PUT:link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/put-link");

		// Ensure can POST link
		MockHttpResponse response = this.server.send(this.mockRequest("/put-link").method(HttpMethod.PUT));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section PUT /put-link");
	}

	/**
	 * Adds the context path to the path.
	 * 
	 * @param server
	 *            Server details (e.g. http://officefloor.net:80 ).
	 * @param path
	 *            Path.
	 * @return URL with the context path.
	 */
	private String contextUrl(String server, String path) {
		if (this.contextPath != null) {
			path = this.contextPath + path;
		}
		return server + path;
	}

	/**
	 * Creates a {@link MockHttpRequestBuilder} for the path (including context
	 * path).
	 * 
	 * @param path
	 *            Path for the {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	private MockHttpRequestBuilder mockRequest(String path) {
		if (this.contextPath != null) {
			path = this.contextPath + path;
		}
		return MockHttpServer.mockRequest(path);
	}

	/**
	 * Initialises the {@link WebTemplate}.
	 */
	private static interface Initialiser {

		/**
		 * Undertakes initialising.
		 * 
		 * @param context
		 *            {@link CompileWebContext}.
		 * @param templater
		 *            {@link WebTemplater}.
		 */
		void initialise(CompileWebContext context, WebTemplater templater);
	}

	/**
	 * Runs a {@link WebTemplate}.
	 * 
	 * @param initialiser
	 *            {@link Initialiser} to initialise {@link WebTemplate}.
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse template(Initialiser initialiser, MockHttpRequestBuilder request) throws Exception {
		this.compile.web((context) -> {
			WebTemplater templater = WebTemplaterEmployer.employWebTemplater(context.getWebArchitect(),
					context.getOfficeArchitect());
			initialiser.initialise(context, templater);
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
		return this.server.send(request);
	}

	/**
	 * Runs s {@link WebTemplate} and validates the {@link HttpResponse}
	 * content.
	 * 
	 * @param path
	 *            Request path.
	 * @param initialiser
	 *            {@link Initialiser} to initialise {@link WebTemplate}.
	 * @param requestPath
	 *            Request path.
	 * @param expectedTemplate
	 *            Expected content of {@link WebTemplate}.
	 * @return {@link MockHttpResponse} for further validation.
	 */
	private MockHttpResponse template(String path, Initialiser initialiser, String expectedTemplate) throws Exception {
		MockHttpResponse response = this.template(initialiser, this.mockRequest(path));
		assertEquals("Incorrect template response", expectedTemplate, response.getEntity(null));
		return response;
	}

}