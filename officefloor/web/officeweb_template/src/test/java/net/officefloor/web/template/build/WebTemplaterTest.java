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
package net.officefloor.web.template.build;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import net.officefloor.compile.impl.structure.FunctionFlowNodeImpl;
import net.officefloor.compile.impl.structure.FunctionNamespaceNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.impl.structure.SectionOutputNodeImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.web.http.test.CompileWebContext;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.NotEscaped;
import net.officefloor.web.template.NotRenderTemplateAfter;
import net.officefloor.web.template.section.WebTemplateSectionSource;
import net.officefloor.web.template.section.WebTemplateSectionSource.WebTemplateManagedFunctionSource;

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
		MockHttpResponse response = this.template("/path",
				(context, templater) -> templater.addTemplate("/path", new StringReader("TEST")), "TEST");

		// Ensure default values for template
		assertEquals("Incorrect default content-type", "text/plain", response.getHeader("content-type").getValue());
	}

	/**
	 * Ensure can add template with logic.
	 */
	public void testTemplateLogic() throws Exception {
		this.template("/path", (context, templater) -> templater.addTemplate("/path", new StringReader("Data=${value}"))
				.setLogicClass(TemplateLogic.class), "Data=&lt;value&gt;");
	}

	public static class TemplateLogic {
		public TemplateLogic getTemplate() {
			return this;
		}

		public String getValue() {
			return "<value>";
		}
	}

	/**
	 * Ensure issue if {@link WebTemplate} missing bean.
	 */
	public void testMissingTemplateLogicClass() throws Exception {
		this.templateIssue((issues) -> {
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordIssue("/path", SectionNodeImpl.class, "Must provide template logic class for template /path");
			issues.recordIssue("TEMPLATE", FunctionNamespaceNodeImpl.class,
					"Missing property 'bean.template' for ManagedFunctionSource "
							+ WebTemplateManagedFunctionSource.class.getName());
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Failure loading OfficeSectionType from source " + WebTemplateSectionSource.class.getName());
		}, (context, templater) -> {
			templater.addTemplate("/path", new StringReader("Data=${value}"));
		});
	}

	/**
	 * Ensure render {@link NotEscaped}.
	 */
	public void testNotEscapedValue() throws Exception {
		this.template("/path", (context, templater) -> templater
				.addTemplate("/path", new StringReader("<html>${content}</html>")).setLogicClass(NotEscapedLogic.class),
				"<html><body>Hello World</body></html>");
	}

	public static class NotEscapedLogic {
		public NotEscapedLogic getTemplate() {
			return this;
		}

		@NotEscaped
		public String getContent() {
			return "<body>Hello World</body>";
		}
	}

	/**
	 * Ensure can have path parameters.
	 */
	public void testDynamicPath() throws Exception {
		this.template("/dynamic/value",
				(context, templater) -> templater.addTemplate("/dynamic/{param}", new StringReader("Data=${value}"))
						.setLogicClass(DynamicPathLogic.class).setRedirectValuesFunction("getPathValues"),
				"Data=value");
	}

	public static class DynamicPathLogic {
		private String value;

		public DynamicPathLogic getPathValues() {
			return this;
		}

		public String getParam() {
			return this.value;
		}

		public DynamicPathLogic getTemplate(@HttpPathParameter("param") String param) {
			this.value = param;
			return this;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * Ensure reports issue if no logic class when dynamic path to
	 * {@link WebTemplate}.
	 */
	public void testDynamicPathWithoutLogicClass() throws Exception {
		this.templateIssue((issues) -> {
			// Record more user friendly message
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Must provide template logic class for template /{param}, as has dynamic path");

			// Ensure the web template also indicates issue
			issues.recordCaptureIssues(false);
			issues.recordIssue("/{param}", SectionNodeImpl.class,
					"Must provide logic class, as template has path parameters");

			// Redirect for template, therefore not connected
			issues.recordIssue("REDIRECT", FunctionFlowNodeImpl.class,
					"Function Flow REDIRECT is not linked to a ManagedFunctionNode");
			issues.recordIssue("RENDER", FunctionFlowNodeImpl.class,
					"Function Flow RENDER is not linked to a ManagedFunctionNode");

		}, (context, templater) -> {
			templater.addTemplate("/{param}", new StringReader("TEMPLATE"));
		});
	}

	/**
	 * Ensure reports issue if no redirect values function when dynamic path to
	 * {@link WebTemplate}.
	 */
	public void testDynamicPathWithoutRedirectValuesFunction() throws Exception {
		this.templateIssue((issues) -> {
			// Record more user friendly message
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Must provide redirect values function for template /{param}, as has dynamic path");

			// Ensure the web template also indicates issue
			issues.recordCaptureIssues(false);
			issues.recordIssue("/{param}", SectionNodeImpl.class,
					"WebTemplate has path parameters but no redirect values function configured");

			// Redirect for template, therefore not connected
			issues.recordIssue("redirectToTemplate", SectionOutputNodeImpl.class,
					"Section Output redirectToTemplate is not linked to a ManagedFunctionNode");

		}, (context, templater) -> {
			templater.addTemplate("/{param}", new StringReader("TEMPLATE")).setLogicClass(DynamicPathLogic.class);
		});
	}

	/**
	 * Ensure can invoke link from template.
	 */
	public void testLink() throws Exception {
		this.template("/path", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/path", new StringReader("Link=#{link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/path+link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/path+link"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section GET /path+link", response.getEntity(null));
	}

	public static class MockSection {
		public void service(ServerHttpConnection connection) throws IOException {
			HttpRequest request = connection.getRequest();
			connection.getResponse().getEntityWriter()
					.write("section " + request.getMethod().getName() + " " + request.getUri());
		}
	}

	/**
	 * Ensure re-render template after handling link.
	 */
	public void testLinkRerenderTemplate() throws Exception {
		this.template("/path",
				(context, templater) -> templater.addTemplate("/path", new StringReader("Template #{link}"))
						.setLogicClass(LinkRerenderTemplateLogic.class),
				"Template /path+link");

		// Ensure can GET link triggers redirect to template
		MockHttpResponse response = this.server.send(this.mockRequest("/path+link"));
		assertEquals("Should be redirect to template", 303, response.getStatus().getStatusCode());
		response.assertHeader("location", this.contextUrl("", "/path"));

		// Ensure on GET redirect that able to load template
		response = this.server.send(this.mockRequest("/path"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect template", "Template /path+link", response.getEntity(null));
	}

	public static class LinkRerenderTemplateLogic {
		public void link(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("LINK ");
		}
	}

	/**
	 * Ensure not re-render template after handling link.
	 */
	public void testLinkNotRerenderTemplate() throws Exception {
		this.template("/path+link",
				(context, templater) -> templater.addTemplate("/path", new StringReader("Template #{link}"))
						.setLogicClass(LinkNotRerenderTemplateLogic.class),
				"LINK");
	}

	public static class LinkNotRerenderTemplateLogic {
		@NotRenderTemplateAfter
		public void link(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("LINK");
		}
	}

	/**
	 * Ensure can configure a different separator character for links.
	 */
	public void testLinkWithDifferentPathSeparator() throws Exception {
		this.template("/path", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/path", new StringReader("Link=#{link}"));
			template.setLinkSeparatorCharacter('|');
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/path|link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/path|link"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section GET /path|link", response.getEntity(null));
	}

	/**
	 * Ensure can invoke link for dynamic path.
	 */
	public void testDynamicLink() throws Exception {
		this.template("/dynamic", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/{param}", new StringReader("Link=#{link}"))
					.setLogicClass(DynamicLinkLogic.class).setRedirectValuesFunction("getPathValues");
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/dynamic+link");

		// Ensure can GET link (use different path parameter)
		MockHttpResponse response = this.server.send(this.mockRequest("/another+link"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section GET /another+link", response.getEntity(null));
	}

	public static class DynamicLinkLogic {
		private String value;

		public DynamicLinkLogic getPathValues(@HttpPathParameter("param") String value) {
			this.value = value;
			return this;
		}

		public String getParam() {
			return this.value;
		}
	}

	/**
	 * Ensure can configure a different separator character for links.
	 */
	public void testDynamicLinkWithDifferentPathSeparator() throws Exception {
		this.template("/dynamic", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/{param}", new StringReader("Link=#{link}"))
					.setLogicClass(DynamicLinkLogic.class).setRedirectValuesFunction("getPathValues");
			template.setLinkSeparatorCharacter('|');
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/dynamic|link");

		// Ensure can GET link (use different path parameter)
		MockHttpResponse response = this.server.send(this.mockRequest("/another|link"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section GET /another|link", response.getEntity(null));
	}

	/**
	 * Ensure both GET and POST supported by default for links. Makes easier for
	 * form HTML.
	 */
	public void testGetAndPostDefaults() throws Exception {
		this.template("/default", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/default", new StringReader("Link=#{link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/default+link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/default+link").method(HttpMethod.GET));
		assertEquals("GET link should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect GET response", "section GET /default+link", response.getEntity(null));

		// Ensure can POST link
		response = this.server.send(this.mockRequest("/default+link").method(HttpMethod.POST));
		assertEquals("POST link should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect POST resposne", "section POST /default+link", response.getEntity(null));
	}

	/**
	 * Ensure configure link as POST.
	 */
	public void testPostLinkOnly() throws Exception {
		this.template("/post", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/post", new StringReader("Link=#{POST:link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/post+link");

		// Ensure can POST link
		MockHttpResponse response = this.server.send(this.mockRequest("/post+link").method(HttpMethod.POST));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section POST /post+link", response.getEntity(null));

		// Ensure can not GET link (as specifies only POST)
		response = this.server.send(this.mockRequest("/post+link").method(HttpMethod.GET));
		assertEquals("Should not support GET", 405, response.getStatus().getStatusCode());
	}

	/**
	 * Ensure configure link as PUT. This is typically for Javascript requests.
	 */
	public void testPutJavaScriptLink() throws Exception {
		this.template("/put", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate("/put", new StringReader("Link=#{PUT:link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/put+link");

		// Ensure can POST link
		MockHttpResponse response = this.server.send(this.mockRequest("/put+link").method(HttpMethod.PUT));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect link response", "section PUT /put+link", response.getEntity(null));
	}

	/**
	 * Ensure allows template responses to {@link HttpMethod} values other than
	 * GET.
	 */
	public void testOtherMethod() throws Exception {
		final HttpMethod method = HttpMethod.getHttpMethod("TEST");
		MockHttpResponse response = this.template((context, templater) -> templater
				.addTemplate("/path", new StringReader("TEMPLATE")).addRenderMethod(method),
				this.mockRequest("/path").method(method));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect template", "TEMPLATE", response.getEntity(null));
	}

	/**
	 * Ensure can link to template.
	 */
	public void testLinkToTemplate() throws Exception {
		MockHttpResponse response = this.template("/redirect+link", (context, templater) -> {
			WebTemplate redirect = templater.addTemplate("/redirect", new StringReader("#{link}"));
			WebTemplate template = templater.addTemplate("/template", new StringReader("TEMPLATE"));
			template.link(redirect.getOutput("link"), null);
		}, "");

		// Ensure redirect
		assertEquals("Should be redirect", HttpStatus.SEE_OTHER, response.getStatus());
		String location = response.getHeader("location").getValue();
		assertEquals("Incorrect location", this.contextUrl("", "/template"), location);

		// Fire redirect to then get the template
		response = this.server.send(this.mockRequest(location).cookies(response));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Should have template", "TEMPLATE", response.getEntity(null));
	}

	/**
	 * Ensure can link to template with path parameters.
	 */
	public void testLinkToTemplateWithDynamicPath() throws Exception {
		MockHttpResponse redirect = this.template("/redirect", (context, templater) -> {
			WebArchitect web = context.getWebArchitect();
			OfficeSection section = context.addSection("SECTION", DynamicPathSection.class);
			web.link(false, "/redirect", section.getOfficeSectionInput("service"));
			WebTemplate template = templater.addTemplate("/{param}", new StringReader("TEMPLATE"))
					.setLogicClass(LinkDynamicPathLogic.class).setRedirectValuesFunction("getPathValues");
			template.link(section.getOfficeSectionOutput("template"), DynamicPathSection.class);
		}, "");

		// Ensure redirect
		assertEquals("Should be redirect", HttpStatus.SEE_OTHER, redirect.getStatus());
		String location = redirect.getHeader("location").getValue();
		assertEquals("Incorrect location", this.contextUrl("", "/value"), location);

		// Fire redirect to then get the template
		MockHttpResponse response = this.server.send(this.mockRequest(location).cookies(redirect));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Should have template", "TEMPLATE", response.getEntity(null));
	}

	public static class LinkDynamicPathLogic {
		public LinkDynamicPathLogic getPathValues() {
			return this;
		}

		public String getParam() {
			return "value";
		}
	}

	public static class DynamicPathSection {
		@NextFunction("template")
		public DynamicPathSection service() {
			return this;
		}

		public String getParam() {
			return "value";
		}
	}

	/**
	 * Ensure can change the <code>Content-Type</code> for the template.
	 */
	public void testContentType() throws Exception {
		MockHttpResponse response = this.template("/path", (context, templater) -> templater
				.addTemplate("/path", new StringReader("{value: JSON}")).setContentType("application/json"),
				"{value: JSON}");
		assertEquals("Incorrect default content-type", "application/json",
				response.getHeader("content-type").getValue());
	}

	/**
	 * ENsure can change the {@link Charset} for rendering the template.
	 */
	public void testCharset() throws Exception {
		Charset charset = Charset.forName("UTF-16");
		MockHttpResponse response = this.template((context, templater) -> templater
				.addTemplate("/path", new StringReader("UTF-16 rendered")).setCharset(charset),
				this.mockRequest("/path"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect content", "UTF-16 rendered", response.getEntity(charset));
	}

	/**
	 * Ensure only sends {@link WebTemplate} content over a secure connection.
	 */
	public void testSecureTemplate() throws Exception {
		MockHttpResponse response = this.template("/path",
				(context, templater) -> templater.addTemplate("/path", new StringReader("SECURE")).setSecure(true), "");

		// Non-secure request should have redirect to secure connection
		assertEquals("Should redirect for secure connection", 307, response.getStatus().getStatusCode());
		assertEquals("Should be secure redirect URL", this.contextUrl("https://mock.officefloor.net", "/path"),
				response.getHeader("location").getValue());

		// Ensure able to obtain template over secure connection
		response = this.server.send(this.mockRequest("/path").secure(true));
		assertEquals("Should obtain template", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect template", "SECURE", response.getEntity(null));
	}

	/**
	 * Ensure can render insecure {@link WebTemplate} over a secure connection.
	 */
	public void testInsecureTemplateOnSecureLink() throws Exception {
		MockHttpResponse response = this.template(
				(context, templater) -> templater.addTemplate("/path", new StringReader("INSECURE")),
				this.mockRequest("/path").secure(true));

		// Should obtain insecure template on secure connection
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Inorrect template", "INSECURE", response.getEntity(null));
	}

	/*
	 * Ensure only accepts link request over a secure connection.
	 */
	public void testSecureLink() throws Exception {
		MockHttpResponse response = this.template("/path+link", (context, templater) -> {
			WebTemplate template = templater.addTemplate("/path", new StringReader("Link=#{link}"));
			template.setLinkSecure("link", true);
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "");

		// Not-secure request should a have a redirect to secure connection
		assertEquals("Should redirect for secure connection", 307, response.getStatus().getStatusCode());
		assertEquals("Should be secure redirect URL", this.contextUrl("https://mock.officefloor.net", "/path+link"),
				response.getHeader("location").getValue());

		// Ensure able to obtain link over secure connection
		response = this.server.send(this.mockRequest("/path+link").secure(true));
	}

	/**
	 * Ensure can render a secure link for a insecure {@link WebTemplate}.
	 */
	public void testRenderSecureLink() throws Exception {
		fail("TODO implement test to render secure link");
	}

	/**
	 * Ensure can render insecure link on a secure {@link WebTemplate}.
	 */
	public void testRenderInsecureLinkOnSecureTemplate() throws Exception {
		fail("TODO implement test to render insecure link on secure template");
	}

	/**
	 * Ensure can specify super {@link WebTemplate}.
	 */
	public void testSuperTemplate() throws Exception {
		this.template("/child", (context, templater) -> {
			WebTemplate parent = templater.addTemplate("/parent", new StringReader("TEST <!-- {section} --> PARENT"));
			templater.addTemplate("/child", new StringReader("<!-- {:section} -->Child")).setSuperTemplate(parent);
		}, "TEST Child");
	}

	/**
	 * Ensure can specify multiple {@link WebTemplate} instances for
	 * inheritance.
	 */
	public void testGrandSuperTemplate() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure can load {@link WebTemplate} from a resource.
	 */
	public void testLoadTemplateFromResource() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure can load {@link WebTemplate} and it's super {@link WebTemplate}
	 * from a resource.
	 */
	public void testLoadSuperFromResource() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure can extend the {@link WebTemplate}.
	 */
	public void testExtendTemplate() throws Exception {
		this.template("/extend", (context, templater) -> {
			WebTemplate template = templater.addTemplate("/extend", new StringReader("original"));
			template.addExtension((extension) -> extension.setTemplateContent("extended"));
		}, "extended");
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
					context.getOfficeArchitect(), context.getOfficeSourceContext());
			initialiser.initialise(context, templater);
			templater.informWebArchitect();
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
		return this.server.send(request);
	}

	/**
	 * Runs a {@link WebTemplate} and validates the {@link HttpResponse}
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

	/**
	 * Attempts to load {@link WebTemplater}, however should have
	 * {@link CompilerIssues}.
	 * 
	 * @param configureIssues
	 *            {@link Consumer} to configure the {@link CompilerIssues}.
	 * @param initialiser
	 *            {@link Initialiser} to initialise the {@link WebTemplate}.
	 */
	private void templateIssue(Consumer<MockCompilerIssues> configureIssues, Initialiser initialiser) throws Exception {

		// Load mock issues
		MockCompilerIssues issues = new MockCompilerIssues(this);
		this.compile.getOfficeFloorCompiler().setCompilerIssues(issues);

		// Record the issue
		configureIssues.accept(issues);

		// Test
		this.replayMockObjects();
		this.compile.web((context) -> {
			WebTemplater templater = WebTemplaterEmployer.employWebTemplater(context.getWebArchitect(),
					context.getOfficeArchitect(), context.getOfficeSourceContext());
			initialiser.initialise(context, templater);
			templater.informWebArchitect();
		});
		this.compile.compileOfficeFloor();
		this.verifyMockObjects();
	}

}