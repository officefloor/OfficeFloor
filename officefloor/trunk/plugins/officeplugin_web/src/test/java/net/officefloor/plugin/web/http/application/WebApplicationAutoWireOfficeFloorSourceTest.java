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
package net.officefloor.plugin.web.http.application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.sql.SQLException;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireManagement;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.web.http.route.HttpRouteTask;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;
import net.officefloor.plugin.work.clazz.FlowInterface;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Ignore;

/**
 * Tests the {@link WebApplicationAutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO provide tests for inheriting secure link configuration")
public class WebApplicationAutoWireOfficeFloorSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Host name for testing.
	 */
	private static final String HOST_NAME = HttpApplicationLocationManagedObjectSource
			.getDefaultHostName();

	/**
	 * {@link WebApplicationAutoWireOfficeFloorSource} to be tested.
	 */
	private final WebApplicationAutoWireOfficeFloorSource source = new WebApplicationAutoWireOfficeFloorSource();

	/**
	 * {@link HttpClient}.
	 */
	private HttpClient client;

	/**
	 * Port on which the web application is to run.
	 */
	private int port;

	/**
	 * Secure port on which the web application is to be run.
	 */
	private int securePort;

	@Override
	protected void setUp() throws Exception {

		// Configure the port
		this.port = MockHttpServer.getAvailablePort();
		this.source.getOfficeFloorCompiler().addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
				String.valueOf(this.port));
		HttpServerSocketManagedObjectSource.autoWire(this.source, this.port,
				WebApplicationAutoWireOfficeFloorSource.HANDLER_SECTION_NAME,
				WebApplicationAutoWireOfficeFloorSource.HANDLER_INPUT_NAME);

		// Configure the secure port
		this.securePort = MockHttpServer.getAvailablePort();
		this.source.getOfficeFloorCompiler().addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTPS_PORT,
				String.valueOf(this.securePort));
		HttpsServerSocketManagedObjectSource.autoWire(this.source,
				this.securePort, MockHttpServer.getSslEngineSourceClass(),
				WebApplicationAutoWireOfficeFloorSource.HANDLER_SECTION_NAME,
				WebApplicationAutoWireOfficeFloorSource.HANDLER_INPUT_NAME);

		// Configure the HTTP Request State and HTTP Session
		this.source.addManagedObject(
				HttpRequestStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpRequestState.class));
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(60 * 1000);

		// Configure the client (to not redirect)
		HttpParams params = new BasicHttpParams();
		params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
		this.client = new DefaultHttpClient(params);

		// Configure HTTPS for client
		MockHttpServer.configureHttps(this.client, this.securePort);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			this.client.getConnectionManager().shutdown();
		} finally {
			// Ensure close
			AutoWireManagement.closeAllOfficeFloors();
		}
	}

	/**
	 * Ensure able to auto-wire template with no logic class.
	 */
	public void testTemplateWithNoLogicClass() throws Exception {
		this.doTemplateWithNoLogicClassTest(false);
	}

	/**
	 * Ensure able to auto-wire secure template with no logic class.
	 */
	public void testSecureTemplateWithNoLogicClass() throws Exception {
		this.doTemplateWithNoLogicClassTest(true);
	}

	/**
	 * Undertakes test to enable to auto-wire template with no logic class.
	 */
	public void doTemplateWithNoLogicClassTest(boolean isSecure)
			throws Exception {

		final String templatePath = this.getClassPath("NoLogicTemplate.ofp");

		// Add HTTP template with no logic class
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"template", templatePath, null);
		template.setTemplateSecure(isSecure);
		this.source.linkToResource(template, "link", "resource.html");
		this.source.openOfficeFloor();

		// Ensure template available
		this.assertHttpRequest("/template", isSecure, 200, "/template-link");

		// Ensure link connected to resource
		this.assertHttpRequest("/template-link", isSecure, 200, "RESOURCE");
	}

	/**
	 * Ensure able to add HTTP template.
	 */
	public void testTemplate() throws Exception {
		this.doTemplateTest(false);
	}

	/**
	 * Ensure able to add secure HTTP template.
	 */
	public void testSecureTemplate() throws Exception {
		this.doTemplateTest(true);
	}

	/**
	 * Undertakes test to ensure able to add HTTP template.
	 */
	private void doTemplateTest(boolean isSecure) throws Exception {

		final String SUBMIT_URI = "/uri-submit";

		final String templatePath = this.getClassPath("template.ofp");

		// Add HTTP template (with URL)
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate(
				"uri", templatePath, MockTemplateLogic.class);
		section.setTemplateSecure(isSecure);
		this.source.openOfficeFloor();

		// Ensure correct section details
		assertEquals("Incorrect section name", "uri", section.getSectionName());
		assertEquals("Incorrect section source",
				HttpTemplateSectionSource.class.getName(),
				section.getSectionSourceClassName());
		assertEquals("Incorrect section location",
				this.getClassPath("template.ofp"), templatePath);
		assertEquals("Incorrect template path", templatePath,
				section.getTemplatePath());
		assertEquals("Incorrect template URI", "/uri", section.getTemplateUri());

		// Ensure template available
		this.assertHttpRequest("/uri", isSecure, 200, SUBMIT_URI);
	}

	/**
	 * Tests redirect on POST for default configuration.
	 */
	public void testPostDefaultRenderRedirect() throws Exception {
		this.doRenderRedirectTest("POST");
	}

	/**
	 * Tests redirect on configured HTTP method.
	 */
	public void testConfiguredRenderRedirect() throws Exception {
		this.doRenderRedirectTest("OTHER", "POST", "PUT", "OTHER");
	}

	/**
	 * Ensure redirect before rendering for particular HTTP methods.
	 * 
	 * @param method
	 *            HTTP method to use.
	 * @param renderRedirectHttpMethods
	 *            Render redirect HTTP methods.
	 */
	public void doRenderRedirectTest(String method,
			String... renderRedirectHttpMethods) throws Exception {

		// Add the template
		final String templatePath = this.getClassPath("template.ofp");
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"uri", templatePath, MockTemplateLogic.class);

		// Ensure able to provide appropriate render redirect HTTP methods
		for (String renderRedirectHttpMethod : renderRedirectHttpMethods) {
			template.addRenderRedirectHttpMethod(renderRedirectHttpMethod);
		}

		// Ensure correctly provided
		String[] configuredMethods = template.getRenderRedirectHttpMethods();
		assertEquals("Incorrect number of render redirect HTTP methods",
				renderRedirectHttpMethods.length, configuredMethods.length);
		for (int i = 0; i < renderRedirectHttpMethods.length; i++) {
			assertEquals("Incorrect render redirect HTTP method " + i,
					renderRedirectHttpMethods[i], configuredMethods[i]);
		}

		// Open
		this.source.openOfficeFloor();

		// Ensure appropriately redirects
		String url = "http://" + HOST_NAME + ":" + this.port + "/uri";
		HttpUriRequest request = new HttpConfiguredRequest(method, url);
		String redirectUrl = "/uri" + HttpRouteTask.REDIRECT_URI_SUFFIX;
		this.assertHttpRequest(request, redirectUrl, 200, "/uri-submit");
	}

	/**
	 * {@link HttpUriRequest} for HTTP method <code>OTHER</code>.
	 */
	private static class HttpConfiguredRequest extends
			HttpEntityEnclosingRequestBase {

		/**
		 * HTTP method.
		 */
		private final String method;

		/**
		 * Initiate.
		 * 
		 * @param method
		 *            HTTP method.
		 * @param uri
		 *            URI.
		 */
		public HttpConfiguredRequest(String method, String uri) {
			this.setURI(URI.create(uri));
			this.method = method;
		}

		@Override
		public String getMethod() {
			return this.method;
		}
	}

	/**
	 * Ensure able to add HTTP template that is available for default root.
	 */
	public void testRootTemplate() throws Exception {
		this.doRootTemplateTest(false);
	}

	/**
	 * Ensure able to add secure HTTP template that is available for default
	 * root.
	 */
	public void testSecureRootTemplate() throws Exception {
		this.doRootTemplateTest(true);
	}

	/**
	 * Undertakes test to ensure able to add HTTP template that is available for
	 * default root.
	 */
	public void doRootTemplateTest(boolean isSecure) throws Exception {

		final String SUBMIT_URI = "/-submit";

		final String templatePath = this.getClassPath("template.ofp");

		// Add HTTP template (with URL)
		HttpTemplateAutoWireSection section = this.source.addHttpTemplate("/",
				templatePath, MockTemplateLogic.class);
		section.setTemplateSecure(isSecure);
		this.source.openOfficeFloor();

		// Ensure correct section details
		assertEquals("Incorrect section name", "_root_",
				section.getSectionName());
		assertEquals("Incorrect section source",
				HttpTemplateSectionSource.class.getName(),
				section.getSectionSourceClassName());
		assertEquals("Incorrect section location",
				this.getClassPath("template.ofp"), templatePath);
		assertEquals("Incorrect template path", templatePath,
				section.getTemplatePath());
		assertEquals("Incorrect template URI", "/", section.getTemplateUri());

		// Ensure template available at default root
		this.assertHttpRequest("/", isSecure, 200, SUBMIT_URI);

		// Ensure root link works
		this.assertHttpRequest(SUBMIT_URI, isSecure, 200, "submitted"
				+ SUBMIT_URI);
	}

	/**
	 * Mock logic for the template.
	 */
	public static class MockTemplateLogic {

		/**
		 * Submit handler.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		@NextTask("doNothing")
		public void submit(ServerHttpConnection connection) throws IOException {
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"submitted", connection);
		}

		/**
		 * Do nothing after submit.
		 */
		public void doNothing() {
		}
	}

	/**
	 * Ensure issue if attempt to add more than one HTTP template for a URI.
	 */
	public void testMultipleTemplatesWithSameUri() throws Exception {

		final String TEMPLATE_URI = "template";

		// Add HTTP template
		this.source.addHttpTemplate(TEMPLATE_URI,
				this.getClassPath("template.ofp"), MockTemplateLogic.class);

		// Ensure indicates template already registered for URI
		try {
			this.source.addHttpTemplate(TEMPLATE_URI,
					this.getClassPath("template.ofp"), MockTemplateLogic.class);
			fail("Should not successfully add template for duplicate URI");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"HTTP Template already added for URI '/" + TEMPLATE_URI
							+ "'", ex.getMessage());
		}

		// Ensure indicates template already registered for canonical URI
		try {
			this.source.addHttpTemplate("/" + TEMPLATE_URI,
					this.getClassPath("template.ofp"), MockTemplateLogic.class);
			fail("Should not successfully add template for duplicate URI");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"HTTP Template already added for URI '/" + TEMPLATE_URI
							+ "'", ex.getMessage());
		}
	}

	/**
	 * Ensure able to request the template link.
	 */
	public void testTemplateLink() throws Exception {
		this.doTemplateLinkTest(false);
	}

	/**
	 * Ensure able to request the secure template link.
	 */
	public void testSecureTemplateLink() throws Exception {
		this.doTemplateLinkTest(true);
	}

	/**
	 * Undertakes test to ensure able to request the template link.
	 */
	private void doTemplateLinkTest(boolean isSecure) throws Exception {

		final String SUBMIT_URI = "/uri-submit";

		// Add HTTP template
		this.source.addHttpTemplate("uri", this.getClassPath("template.ofp"),
				MockTemplateLogic.class).setTemplateSecure(isSecure);
		this.source.openOfficeFloor();

		// Ensure submit on task for template is correct
		this.assertHttpRequest(SUBMIT_URI, isSecure, 200, "submitted"
				+ SUBMIT_URI);
	}

	/**
	 * Ensure can secure a link.
	 */
	public void testSecureLink() throws Exception {

		final String SUBMIT_URI = "/uri-submit";

		// Add HTTP template
		this.source.addHttpTemplate("uri", this.getClassPath("template.ofp"),
				MockTemplateLogic.class).setLinkSecure("submit", true);
		this.source.openOfficeFloor();

		// Ensure submit on task for template is correct
		this.assertHttpRequest(SUBMIT_URI, true, 200, "submitted" + SUBMIT_URI);
	}

	/**
	 * Ensure can set link as non-secure.
	 */
	public void testNonSecureLink() throws Exception {

		final String SUBMIT_URI = "/uri-submit";

		// Add HTTP template
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"uri", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		template.setTemplateSecure(true);
		template.setLinkSecure("submit", false);
		this.source.openOfficeFloor();

		// Ensure submit on task for template is correct
		String requestUrl = "http://" + HOST_NAME + ":" + this.port
				+ SUBMIT_URI;
		String redirectUrl = "https://" + HOST_NAME + ":" + this.securePort
				+ "/uri" + HttpRouteTask.REDIRECT_URI_SUFFIX;
		String linkUrl = "http://" + HOST_NAME + ":" + this.port + SUBMIT_URI;
		this.assertHttpRequest(new HttpGet(requestUrl), redirectUrl, 200,
				"submitted" + linkUrl);
	}

	/**
	 * Ensure can inherit link being secure.
	 */
	public void testInheritTemplateLinkSecure() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure can not inherit template link secure if no longer exists (as
	 * containing section overridden and no longer contains the link).
	 */
	public void testNotInheritMissingTemplateLinkSecure() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure issue if the template inheritance hierarchy is cyclic.
	 */
	public void testCyclicTemplateInheritanceHierarchy() throws Exception {
		fail("TODO implement");
	}

	/**
	 * Ensure default template URI suffix is applied.
	 */
	public void testDefaultTemplateUriSuffix() throws Exception {

		final String SUFFIX = ".suffix";
		final String TEMPLATE_URI = "/uri" + SUFFIX;
		final String LINK_URI = "/uri-submit" + SUFFIX;

		// Add HTTP template with default template URI suffix
		this.source.addHttpTemplate("uri", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		this.source.setDefaultHttpTemplateUriSuffix(SUFFIX);
		this.source.openOfficeFloor();

		// Ensure service template URI with suffix
		this.assertHttpRequest(TEMPLATE_URI, 200, LINK_URI);

		// Ensure service template link URI with suffix
		this.assertHttpRequest(LINK_URI, 200, "submitted" + LINK_URI);
	}

	/**
	 * Ensure root template does not apply suffix for template, only links.
	 */
	public void testRootTemplateUriSuffix() throws Exception {

		final String SUFFIX = ".suffix";
		final String TEMPLATE_URI = "/";
		final String LINK_URI = "/-submit" + SUFFIX;

		// Add root HTTP template with default template URI suffix
		this.source.addHttpTemplate("/", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		this.source.setDefaultHttpTemplateUriSuffix(SUFFIX);
		this.source.openOfficeFloor();

		// Ensure service template URI with suffix
		this.assertHttpRequest(TEMPLATE_URI, 200, LINK_URI);

		// Ensure service template link URI with suffix
		this.assertHttpRequest(LINK_URI, 200, "submitted" + LINK_URI);
	}

	/**
	 * Ensure can specify no default template URI suffix is applied.
	 */
	public void testNoDefaultTemplateUriSuffix() throws Exception {

		final String TEMPLATE_URI = "/uri";
		final String LINK_URI = "/uri-submit";

		// Add HTTP template with default template URI suffix
		this.source.addHttpTemplate("uri", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		this.source.setDefaultHttpTemplateUriSuffix(null);
		this.source.openOfficeFloor();

		// Ensure service template URI with suffix
		this.assertHttpRequest(TEMPLATE_URI, 200, LINK_URI);

		// Ensure service template link URI with suffix
		this.assertHttpRequest(LINK_URI, 200, "submitted" + LINK_URI);
	}

	/**
	 * Ensure template URI suffix appended to template URI and link URIs.
	 */
	public void testTemplateUriSuffix() throws Exception {

		final String SUFFIX = ".suffix";
		final String TEMPLATE_URI = "/uri" + SUFFIX;
		final String LINK_URI = "/uri-submit" + SUFFIX;

		// Add HTTP template
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"uri", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		template.setTemplateUriSuffix(SUFFIX);
		this.source.openOfficeFloor();

		// Ensure service template URI with suffix
		this.assertHttpRequest(TEMPLATE_URI, 200, LINK_URI);

		// Ensure service template link URI with suffix
		this.assertHttpRequest(LINK_URI, 200, "submitted" + LINK_URI);
	}

	/**
	 * Ensure able to override default template URI suffix.
	 */
	public void testOverrideTemplateUriSuffix() throws Exception {

		final String TEMPLATE_URI = "/uri.override";
		final String LINK_URI = "/uri-submit.override";

		// Provide default template URI suffix
		this.source.setDefaultHttpTemplateUriSuffix(".suffix");

		// Add HTTP template
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"uri", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		template.setTemplateUriSuffix(".override");
		this.source.openOfficeFloor();

		// Ensure service template URI with suffix
		this.assertHttpRequest(TEMPLATE_URI, 200, LINK_URI);

		// Ensure service template link URI with suffix
		this.assertHttpRequest(LINK_URI, 200, "submitted" + LINK_URI);
	}

	/**
	 * Ensure appropriate linked URIs.
	 */
	public void testLinkedUris() {

		// Add HTTP template (not root so should not be included)
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"template", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);

		// Provide URI link
		this.source.linkUri("uri", template,
				HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);

		// Validate URIs
		assertUris(this.source.getURIs(), "/uri");

		// Validate with root HTTP template
		this.source.addHttpTemplate("/", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		assertUris(this.source.getURIs(), "/", "/uri");
	}

	/**
	 * Asserts the URIs are correct.
	 * 
	 * @param actualUris
	 *            Actual URIs.
	 * @param expectedUris
	 *            Expected URIs.
	 */
	private static void assertUris(String[] actualUris, String... expectedUris) {
		assertEquals("Incorrect number of URIs", expectedUris.length,
				actualUris.length);
		for (int i = 0; i < expectedUris.length; i++) {
			assertEquals("Incorrect URI " + i, expectedUris[i], actualUris[i]);
		}
	}

	/**
	 * Ensure able to provide {@link HttpTemplateSectionExtension}.
	 */
	public void testTemplateExtension() throws Exception {

		// Add HTTP template
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"template", this.getClassPath("Extension.ofp"),
				MockExtensionTemplateLogic.class);

		// Add template extension
		HttpTemplateAutoWireSectionExtension extension = template
				.addTemplateExtension(MockHttpTemplateSectionExtension.class);
		extension.addProperty("name", "value");

		// Open
		this.source.openOfficeFloor();

		// Ensure extend the template
		this.assertHttpRequest("/template", 200, "extended");
	}

	/**
	 * Mock {@link HttpTemplateSectionExtension} for testing.
	 */
	public static class MockHttpTemplateSectionExtension implements
			HttpTemplateSectionExtension {
		@Override
		public void extendTemplate(HttpTemplateSectionExtensionContext context)
				throws Exception {
			context.setTemplateContent("${extend}");
		}
	}

	/**
	 * Template logic for the extension test.
	 */
	public static class MockExtensionTemplateLogic {

		public MockExtensionTemplateLogic getTemplate() {
			return this;
		}

		public String getExtend() {
			return "extended";
		}
	}

	/**
	 * Ensure able to link URI to {@link OfficeSectionInput} for processing.
	 */
	public void testLinkUriToSectionInput() throws Exception {
		this.doLinkUriToSectionInputTest(false);
	}

	/**
	 * Ensure able to link secure URI to {@link OfficeSectionInput} for
	 * processing.
	 */
	public void testLinkSecureUriToSectionInput() throws Exception {
		this.doLinkUriToSectionInputTest(true);
	}

	/**
	 * Ensure able to link URI to {@link OfficeSectionInput} for processing.
	 */
	public void doLinkUriToSectionInputTest(boolean isSecure) throws Exception {

		// Add section for handling request
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockTemplateLogic.class.getName());
		this.source.linkUri("test", section, "submit").setUriSecure(isSecure);
		this.source.openOfficeFloor();

		// Ensure can send to URI
		this.assertHttpRequest("/test", isSecure, 200, "submitted");
	}

	/**
	 * Ensure able to link {@link OfficeSectionOutput} to {@link HttpTemplate}.
	 */
	public void testLinkToHttpTemplate() throws Exception {

		// Add linking to HTTP template
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockLinkHttpTemplate.class.getName());
		this.source.linkUri("test", section, "service");
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"template", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		this.source.linkToHttpTemplate(section, "http-template", template);
		this.source.openOfficeFloor();

		// Ensure link to the HTTP template
		this.assertHttpRequest("/test", 200, "LINK to /template-submit");
	}

	/**
	 * Ensure can inherit link to {@link HttpTemplate}.
	 */
	public void testInheritLinkToHttpTemplate() throws Exception {

		// Add the template
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"template", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);

		// Add parent linking to resource
		AutoWireSection parent = this.source.addSection("PARENT",
				ClassSectionSource.class.getName(),
				MockLinkHttpTemplate.class.getName());
		this.source.linkToHttpTemplate(parent, "http-template", template);

		// Add child inheriting link configuration
		AutoWireSection child = this.source.addSection("CHILD",
				ClassSectionSource.class.getName(),
				MockLinkHttpTemplate.class.getName());
		this.source.linkUri("test", child, "service");
		child.setSuperSection(parent);

		// Open OfficeFloor
		this.source.openOfficeFloor();

		// Ensure link to the HTTP template
		this.assertHttpRequest("/test", 200, "LINK to /template-submit");
	}

	/**
	 * Provides mock functionality to link to a HTTP template.
	 */
	public static class MockLinkHttpTemplate {
		@NextTask("http-template")
		public void service(ServerHttpConnection connection) throws IOException {
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"LINK to ", connection);
		}
	}

	/**
	 * Ensure able to link to resource.
	 */
	public void testLinkToResource() throws Exception {

		// Add linking to resource
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockLinkResource.class.getName());
		this.source.linkUri("test", section, "service");
		this.source.linkToResource(section, "resource", "resource.html");
		this.source.openOfficeFloor();

		// Ensure provide the resource
		this.assertHttpRequest("/test", 200, "RESOURCE");
	}

	/**
	 * Ensure can inherit link to resource.
	 */
	public void testInheritLinkToResource() throws Exception {

		// Add parent linking to resource
		AutoWireSection parent = this.source.addSection("PARENT",
				ClassSectionSource.class.getName(),
				MockLinkResource.class.getName());
		this.source.linkToResource(parent, "resource", "resource.html");

		// Add child inheriting link configuration
		AutoWireSection child = this.source.addSection("CHILD",
				ClassSectionSource.class.getName(),
				MockLinkResource.class.getName());
		this.source.linkUri("test", child, "service");
		child.setSuperSection(parent);

		// Open OfficeFloor
		this.source.openOfficeFloor();

		// Ensure provide the resource
		this.assertHttpRequest("/test", 200, "RESOURCE");
	}

	/**
	 * Provides mock functionality to link to a resource.
	 */
	public static class MockLinkResource {
		@NextTask("resource")
		public void service(ServerHttpConnection connection) throws IOException {
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"LINK to ", connection);
		}
	}

	/**
	 * Ensure able to link {@link Escalation} to
	 * {@link HttpTemplateAutoWireSection}.
	 */
	public void testLinkEscalationToTemplate() throws Exception {

		// Add escalation to template
		AutoWireSection failingSection = this.source.addSection("FAILING",
				ClassSectionSource.class.getName(),
				FailingSection.class.getName());
		this.source.linkUri("test", failingSection, "task");
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				"handler", this.getClassPath("template.ofp"),
				MockTemplateLogic.class);
		this.source.linkEscalation(SQLException.class, template);
		this.source.openOfficeFloor();

		// Ensure link escalation to template
		this.assertHttpRequest("/test", 200, "Escalated to /handler-submit");
	}

	/**
	 * Section class that fails and provides an {@link Escalation}.
	 */
	public static class FailingSection {
		public void task(ServerHttpConnection connection) throws Exception {
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"Escalated to ", connection);
			throw new SQLException("Test failure");
		}
	}

	/**
	 * Ensure able to link {@link Escalation} to resource.
	 */
	public void testLinkEscalationToResource() throws Exception {

		// Add escalation to resource
		AutoWireSection failingSection = this.source.addSection("FAILING",
				ClassSectionSource.class.getName(),
				FailingSection.class.getName());
		this.source.linkUri("test", failingSection, "task");
		this.source.linkEscalation(SQLException.class, "resource.html");
		this.source.openOfficeFloor();

		// Ensure link escalation to resource
		this.assertHttpRequest("/test", 200, "RESOURCE");
	}

	/**
	 * Ensure able to utilise the Http Session object.
	 */
	public void testHttpSessionObject() throws Exception {

		// Provide HTTP Session
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(10 * 1000);

		// Add two templates to ensure object available to both
		this.source.addHttpTemplate("one",
				this.getClassPath("StatefulObject.ofp"),
				MockHttpSessionObjectTemplate.class);
		this.source.addHttpTemplate("two",
				this.getClassPath("StatefulObject.ofp"),
				MockHttpSessionObjectTemplate.class);

		// Add the HTTP Session object
		this.source.addHttpSessionObject(MockHttpSessionObject.class);

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure state maintained across requests
		this.assertHttpRequest("/one", 200, "1");
		this.assertHttpRequest("/one", 200, "2");

		// Ensure state maintained on another template
		this.assertHttpRequest("/two", 200, "3");

		// Ensure reflected on original template
		this.assertHttpRequest("/one", 200, "4");
	}

	/**
	 * Provides mock template logic for Http Session Object.
	 */
	public static class MockHttpSessionObjectTemplate {
		public MockHttpSessionObject getTemplate(MockHttpSessionObject object) {
			object.count++; // increment count to indicate maintaining state
			return object;
		}
	}

	/**
	 * Mock Http Session Object.
	 */
	public static class MockHttpSessionObject implements Serializable {
		public int count = 0;

		public int getCount() {
			return count;
		}
	}

	/**
	 * Ensure {@link HttpSessionStateful} annotation is honoured for templates.
	 */
	public void testHttpSessionStatefulAnnotation() throws Exception {

		// Provide HTTP Session
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(10 * 1000);

		// Add two templates with annotations for HttpSessionStateful
		this.source.addHttpTemplate("one",
				this.getClassPath("StatefulObject.ofp"),
				MockAnnotatedHttpSessionStatefulTemplate.class);
		this.source.addHttpTemplate("two",
				this.getClassPath("StatefulObject.ofp"),
				MockAnnotatedHttpSessionStatefulTemplate.class);

		// HTTP Session Object should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure state maintained across requests
		this.assertHttpRequest("/one", 200, "1");
		this.assertHttpRequest("/one", 200, "2");

		// Ensure state maintained on another template
		this.assertHttpRequest("/two", 200, "3");

		// Ensure reflected on original template
		this.assertHttpRequest("/one", 200, "4");
	}

	/**
	 * Provides mock template logic for using the {@link HttpSessionStateful}
	 * annotation.
	 */
	public static class MockAnnotatedHttpSessionStatefulTemplate {
		public MockAnnotatedHttpSessionStatefulObject getTemplate(
				MockAnnotatedHttpSessionStatefulObject object) {
			object.count++; // increment count to indicate maintaining state
			return object;
		}
	}

	/**
	 * Mock Http Session Object as annotated.
	 */
	@HttpSessionStateful
	public static class MockAnnotatedHttpSessionStatefulObject implements
			Serializable {
		public int count = 0;

		public int getCount() {
			return count;
		}
	}

	/**
	 * Ensure can override the binding name to the {@link HttpSession}.
	 */
	public void testHttpSessionStatefulAnnotationOverridingBoundName()
			throws Exception {

		// Provide HTTP Session
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(10 * 1000);

		// Add the template
		this.source
				.addHttpTemplate(
						"template",
						this.getClassPath("StatefulObject.ofp"),
						MockAnnotatedOverriddenBindNameHttpSessionStatefulTemplate.class);

		// HTTP Session object should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure same object (test within template logic)
		this.assertHttpRequest("/template", 200, "1");
	}

	/**
	 * Provides mock template logic for validating the overriding binding name
	 * for the {@link HttpSession} object.
	 */
	public static class MockAnnotatedOverriddenBindNameHttpSessionStatefulTemplate
			implements Serializable {
		public MockAnnotatedOverriddenBindNameHttpSessionStatefulObject getTemplate(
				MockAnnotatedOverriddenBindNameHttpSessionStatefulObject object,
				HttpSession session) {

			// Ensure object bound under annotated name within the session
			Object sessionObject = session.getAttribute("BIND");
			assertEquals("Should be same object", object, sessionObject);

			// Return for rendering
			return object;
		}
	}

	/**
	 * Mock Http Session Object with overridden binding name.
	 */
	@HttpSessionStateful("BIND")
	public static class MockAnnotatedOverriddenBindNameHttpSessionStatefulObject
			implements Serializable {
		public int getCount() {
			return 1;
		}
	}

	/**
	 * Ensure able to utilise the {@link HttpRequestState} object.
	 */
	public void testHttpRequestObject() throws Exception {

		final String URI = "/template-submit";

		// Provide HTTP Request State
		this.source.addManagedObject(
				HttpRequestStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpRequestState.class));

		// Add the template
		this.source.addHttpTemplate("template",
				this.getClassPath("HttpStateObject.ofp"),
				MockHttpRequestStateTemplate.class);

		// HTTP request object should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure same object (test within template logic)
		this.assertHttpRequest(URI, 200, "maintained state-" + URI);
	}

	/**
	 * Provides mock template logic for validating the {@link HttpRequestState}.
	 */
	public static class MockHttpRequestStateTemplate {

		public void submit(MockHttpRequestStateObject object,
				HttpRequestState state) {

			// Ensure object bound under annotated name within the request state
			Object requestObject = state.getAttribute("BIND");
			assertEquals("Should be same object", object, requestObject);

			// Specify value as should maintain state through request
			object.text = "maintained state";
		}

		public MockHttpRequestStateObject getTemplate(
				MockHttpRequestStateObject object) {
			// Value should be specified in submit
			return object;
		}
	}

	/**
	 * Mock Http Request State Object with overridden binding name.
	 */
	@HttpRequestStateful("BIND")
	public static class MockHttpRequestStateObject implements Serializable {

		public String text = "not specified";

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure able to utilise the HTTP Request Object to load parameters.
	 */
	public void testRequestObjectLoadingParameters() throws Exception {

		// Add the template to use parameters object
		this.source.addHttpTemplate("template",
				this.getClassPath("ParametersObject.ofp"),
				MockHttpParametersObjectTemplate.class);

		// Add the HTTP Request Object to load parameters
		this.source.addHttpRequestObject(MockHttpParametersObject.class, true);

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure provide HTTP parameters
		this.assertHttpRequest("/template?text=VALUE", 200, "VALUE");
	}

	/**
	 * Provides mock template logic for the HTTP Parameters Object.
	 */
	public static class MockHttpParametersObjectTemplate {
		public MockHttpParametersObject getTemplate(
				MockHttpParametersObject object) {
			return object;
		}
	}

	/**
	 * Mock HTTP Parameters Object.
	 */
	public static class MockHttpParametersObject implements Serializable {

		private String text;

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure {@link HttpParameters} is honoured for templates.
	 */
	public void testAnnotatedHttpParameters() throws Exception {

		// Add the template to use parameters object
		this.source.addHttpTemplate("template",
				this.getClassPath("ParametersObject.ofp"),
				MockAnnotatedHttpParametersTemplate.class);

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure provide HTTP parameters
		this.assertHttpRequest("/template?text=VALUE", 200, "VALUE");
	}

	/**
	 * Provides mock template logic for the HTTP Parameters Object.
	 */
	public static class MockAnnotatedHttpParametersTemplate {
		public MockAnnotatedHttpParameters getTemplate(
				MockAnnotatedHttpParameters object) {
			return object;
		}
	}

	/**
	 * Mock HTTP Parameters Object.
	 */
	@HttpParameters("BIND")
	public static class MockAnnotatedHttpParameters implements Serializable {

		private String text;

		public void setText(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure able to utilise the HTTP Application object.
	 */
	public void testHttpApplicationObject() throws Exception {

		final String URI = "/template-submit";

		// Provide HTTP Application State
		this.source.addManagedObject(
				HttpApplicationStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpApplicationState.class));

		// Add the template
		this.source.addHttpTemplate("template",
				this.getClassPath("HttpStateObject.ofp"),
				MockHttpApplicationStateTemplate.class);

		// HTTP application state object should be detected and added

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure same object (test within template logic)
		this.assertHttpRequest(URI, 200, "maintained state-" + URI);
	}

	/**
	 * Provides mock template logic for validating the
	 * {@link HttpApplicationState}.
	 */
	public static class MockHttpApplicationStateTemplate {

		public void submit(MockHttpApplicationStateObject object,
				HttpApplicationState state) {

			// Ensure object bound under annotated name within application state
			Object applicationObject = state.getAttribute("BIND");
			assertEquals("Should be same object", object, applicationObject);

			// Specify value as should maintain state through application
			object.text = "maintained state";
		}

		public MockHttpApplicationStateObject getTemplate(
				MockHttpApplicationStateObject object) {
			// Value should be specified in submit
			return object;
		}
	}

	/**
	 * Mock Http Application State Object with overridden binding name.
	 */
	@HttpApplicationStateful("BIND")
	public static class MockHttpApplicationStateObject {

		public String text = "not specified";

		public String getText() {
			return this.text;
		}
	}

	/**
	 * Ensure able to obtain resources.
	 */
	public void testObtainResources() throws Exception {

		// Obtain war directory
		File warDirectory = this.findFile(this.getClass(), "template.ofp")
				.getParentFile();

		// Add configuration
		OfficeFloorCompiler compiler = this.source.getOfficeFloorCompiler();
		compiler.addProperty(
				SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES,
				warDirectory.getAbsolutePath());
		compiler.addProperty(
				SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
				"resource.html");

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure class path resources are available
		this.assertHttpRequest("/resource.html", 200, "RESOURCE");
		this.assertHttpRequest("/", 200, "RESOURCE");

		// Ensure WAR directory resource are available
		this.assertHttpRequest("/template.ofp", 200, "#{submit}");
	}

	/**
	 * Ensure able to obtain changing resources.
	 */
	public void testObtainChangingResources() throws Exception {

		// Create WAR directory in temp for testing
		File warDirectory = new File(System.getProperty("java.io.tmpdir"), this
				.getClass().getSimpleName() + "-" + this.getName());
		if (warDirectory.exists()) {
			this.deleteDirectory(warDirectory);
		}
		assertTrue("Failed to create WAR directory", warDirectory.mkdir());

		// Create file within war directory
		FileWriter writer = new FileWriter(new File(warDirectory, "test.html"));
		writer.write(this.getName());
		writer.close();

		// Add configuration
		OfficeFloorCompiler compiler = this.source.getOfficeFloorCompiler();
		compiler.addProperty(
				SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES,
				warDirectory.getAbsolutePath());
		compiler.addProperty(
				SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
				"test.html");
		compiler.addProperty(
				SourceHttpResourceFactory.PROPERTY_DIRECT_STATIC_CONTENT,
				String.valueOf(false));

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure obtain resource
		this.assertHttpRequest("/test.html", 200, this.getName());
		this.assertHttpRequest("/", 200, this.getName());

		// Change the resource
		writer = new FileWriter(new File(warDirectory, "test.html"));
		writer.write("CHANGED");
		writer.close();

		// Ensure as not keeping in direct memory that pick up changes
		this.assertHttpRequest("/test.html", 200, "CHANGED");
		this.assertHttpRequest("/", 200, "CHANGED");
	}

	/**
	 * Ensure able to chain a servicer.
	 */
	public void testChainServicer() throws Exception {

		// Add section to override servicing
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockChainedServicer.class.getName());
		this.source.chainServicer(section, "service", "notHandled");

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure chained servicer services request
		MockChainedServicer.isServiced = false;
		this.assertHttpRequest("/chain", 200, "CHAIN SERVICED - /chain");
		assertTrue("Should be chained", MockChainedServicer.isServiced);

		// Ensure default end of chain servicing
		MockChainedServicer.isServiced = true;
		this.assertHttpRequest("/resource.html", 200, "RESOURCE");
		assertTrue("Should be chained", MockChainedServicer.isServiced);
	}

	/**
	 * Provides mock functionality of chained servicer.
	 */
	public static class MockChainedServicer {

		public volatile static boolean isServiced = false;

		@FlowInterface
		public static interface Flows {
			void notHandled();
		}

		public void service(ServerHttpConnection connection, Flows flows)
				throws IOException {

			// Flag that serviced
			isServiced = true;

			// Service
			String uri = connection.getHttpRequest().getRequestURI();
			if ("/chain".equals(uri)) {
				// Service the request
				WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
						"CHAIN SERVICED - " + uri, connection);
			} else {
				// Write some content to indicate chained
				WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
						"CHAIN - ", connection);

				// Hand off to next in chain
				flows.notHandled();
			}
		}
	}

	/**
	 * Ensure able to specify as the last chain servicer.
	 */
	public void testChainLastServicer() throws Exception {

		// Add section to override servicing
		AutoWireSection section = this.source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockLastChainServicer.class.getName());
		this.source.chainServicer(section, "service", null);

		// Add another in chain (which should be ignored with log warning)
		AutoWireSection ignore = this.source.addSection("ANOTHER",
				ClassSectionSource.class.getName(),
				MockChainedServicer.class.getName());
		this.source.chainServicer(ignore, "service", "notHandled");

		// Start the HTTP Server
		this.source.openOfficeFloor();

		// Ensure override non-routed servicing
		MockChainedServicer.isServiced = false;
		this.assertHttpRequest("/unhandled", 200, "CHAIN END - /unhandled");
		assertFalse("Should not be part of chain",
				MockChainedServicer.isServiced);
	}

	/**
	 * Provides mock functionality of last chain servicing.
	 */
	public static class MockLastChainServicer {
		public void service(ServerHttpConnection connection) throws IOException {
			String uri = connection.getHttpRequest().getRequestURI();
			WebApplicationAutoWireOfficeFloorSourceTest.writeResponse(
					"CHAIN END - " + uri, connection);
		}
	}

	/**
	 * Asserts the HTTP request returns expected result.
	 * 
	 * @param uri
	 *            URI to send the HTTP request.
	 * @param expectedResponseStatus
	 *            Expected response status.
	 * @param expectedResponseEntity
	 *            Expected response entity.
	 */
	private void assertHttpRequest(String uri, int expectedResponseStatus,
			String expectedResponseEntity) {
		assertHttpRequest(uri, false, expectedResponseStatus,
				expectedResponseEntity);
	}

	/**
	 * Asserts the HTTP Request returns expected result after a redirect.
	 * 
	 * @param uri
	 *            URI to send the HTTP request.
	 * @param isRedirect
	 *            Indicates if a redirect for secure
	 *            {@link ServerHttpConnection} should occur.
	 * @param expectedResponseStatus
	 *            Expected response status.
	 * @param expectedResponseEntity
	 *            Expected response entity.
	 */
	private void assertHttpRequest(String uri, boolean isRedirect,
			int expectedResponseStatus, String expectedResponseEntity) {

		// Create the request
		String url = "http://" + HOST_NAME + ":" + this.port + uri;
		HttpGet request = new HttpGet(url);

		// Provide redirect URL if expecting to redirect
		String redirectUrl = null;
		if (isRedirect) {
			redirectUrl = "https://" + HOST_NAME + ":" + this.securePort + uri
					+ HttpRouteTask.REDIRECT_URI_SUFFIX;
		}

		// Assert HTTP request
		this.assertHttpRequest(request, redirectUrl, expectedResponseStatus,
				expectedResponseEntity);
	}

	/**
	 * Asserts the HTTP Request returns expected result after a redirect.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @param redirectUrl
	 *            Indicates if a redirect should occur and what is the expected
	 *            redirect URL.
	 * @param expectedResponseStatus
	 *            Expected response status.
	 * @param expectedResponseEntity
	 *            Expected response entity.
	 */
	private void assertHttpRequest(HttpUriRequest request, String redirectUrl,
			int expectedResponseStatus, String expectedResponseEntity) {
		try {

			// Send the request
			HttpResponse response = this.client.execute(request);

			// Determine if redirect
			if (redirectUrl != null) {

				// Ensure appropriate redirect
				assertEquals("Should be redirect", 303, response
						.getStatusLine().getStatusCode());
				assertEquals("Incorrect redirect URL", redirectUrl, response
						.getFirstHeader("Location").getValue());

				// Consume response to allow sending next request
				response.getEntity().consumeContent();

				// Handle server relative redirect
				if (redirectUrl.startsWith("/")) {
					redirectUrl = "http://" + HOST_NAME + ":" + this.port
							+ redirectUrl;
				}

				// Send the redirect for response
				response = this.client.execute(new HttpGet(redirectUrl));
			}

			// Ensure obtained as expected
			String actualResponseBody = MockHttpServer.getEntityBody(response);
			assertEquals("Incorrect response", expectedResponseEntity,
					actualResponseBody);

			// Ensure correct response status
			assertEquals("Should be successful", expectedResponseStatus,
					response.getStatusLine().getStatusCode());

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Obtains the class path to the file.
	 * 
	 * @param fileName
	 *            Name of the file.
	 * @return Class path to the file.
	 */
	private String getClassPath(String fileName) {
		return this.getFileLocation(this.getClass(), fileName);
	}

	/**
	 * Writes the response.
	 * 
	 * @param response
	 *            Response.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	private static void writeResponse(String response,
			ServerHttpConnection connection) throws IOException {
		Writer writer = connection.getHttpResponse().getEntityWriter();
		writer.append(response);
	}

}