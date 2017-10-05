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
import java.nio.charset.Charset;
import java.sql.SQLException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.OfficeFloorNodeImpl;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;
import net.officefloor.plugin.web.http.route.HttpRouteFunction;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtension;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionExtensionContext;
import net.officefloor.plugin.web.http.test.CompileWebExtension;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpApplicationStateful;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.HttpRequestStateful;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.WebArchitect;
import net.officefloor.web.path.HttpApplicationLocationManagedObjectSource;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpApplicationState;
import net.officefloor.web.state.HttpRequestState;

/**
 * Tests the {@link WebApplicationArchitectEmployer}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebArchitectEmployerTest extends OfficeFrameTestCase {

	/**
	 * Host name for testing.
	 */
	private static final String HOST_NAME = HttpApplicationLocationManagedObjectSource.getDefaultHostName();

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Non-secure port.
	 */
	private final int port = 7878;

	/**
	 * Secure port.
	 */
	private final int securePort = 7979;

	@Override
	protected void setUp() throws Exception {

		// Configure the server
		this.compiler.officeFloor((context) -> {
			this.server = MockHttpServer
					.configureMockHttpServer(context.getDeployedOffice().getDeployedOfficeInput("SECTION", "INPUT"));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		// Ensure close
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Configures and compiles the {@link OfficeFloor}.
	 * 
	 * @param extension
	 *            {@link FacadeWebExtension}.
	 * @return Compiled and open {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link OfficeFloor}.
	 */
	private OfficeFloor open(FacadeWebExtension extension) throws Exception {
		this.compiler.web((context) -> extension.extend(context.getWebArchitect()));
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();
		return this.officeFloor;
	}

	/**
	 * Facade {@link CompileWebExtension}.
	 */
	private static interface FacadeWebExtension {

		/**
		 * Facade to provide only the {@link WebArchitect}.
		 * 
		 * @param architect
		 *            {@link WebArchitect}.
		 */
		void extend(WebArchitect architect);
	}

	/**
	 * Configures and compiles the {@link OfficeFloor}.
	 * 
	 * @param extension
	 *            {@link CompileWebExtension}.
	 * @return Compiled and open {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link OfficeFloor}.
	 */
	private OfficeFloor openWithContext(CompileWebExtension extension) throws Exception {
		this.compiler.web(extension);
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();
		return this.officeFloor;
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
	public void doTemplateWithNoLogicClassTest(boolean isSecure) throws Exception {

		final String templatePath = this.getClassPath("NoLogicTemplate.ofp");

		this.open((web) -> {
			// Add HTTP template with no logic class
			HttpTemplateSection template = web.addHttpTemplate("template", templatePath, null);
			template.setTemplateSecure(isSecure);
			web.linkToResource(template.getOfficeSection().getOfficeSectionOutput("link"), "resource.html");
		});

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

		this.open((web) -> {
			// Add HTTP template (with URL)
			HttpTemplateSection section = web.addHttpTemplate("uri", templatePath, MockTemplateLogic.class);
			section.setTemplateSecure(isSecure);
		});

		// Ensure template available
		this.assertHttpRequest("/uri", isSecure, 200, SUBMIT_URI);
	}

	/**
	 * Ensure able to provide the Content-Type with the {@link Charset}.
	 */
	public void testTemplateForContentTypeWithCharset() throws Exception {

		// Obtain non-default charset
		Charset charset = Charset.defaultCharset();
		if (ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET.name().equalsIgnoreCase(charset.name())) {
			charset = Charset.forName("UTF-16");
		}

		// Create the content type
		String contentType = "text/plain; one=1; charset=" + charset.name() + "; another";

		this.open((web) -> {
			// Add HTTP template with Content-Type
			HttpTemplateSection section = web.addHttpTemplate("uri", "PUBLIC/resource.html", null);
			section.setTemplateContentType(contentType);
		});

		// Ensure template correct (charset appended as handled specifically)
		MockHttpResponse response = this.assertHttpRequest("/uri", 200, "RESOURCE");
		assertEquals("Incorrect Content-Type on response", "text/plain; one=1; another; charset=" + charset.name(),
				response.getFirstHeader("Content-Type").getValue());
	}

	/**
	 * Ensure able to provide the Content-Type.
	 */
	public void testTemplateContentTypeWithDefaultCharset() throws Exception {

		this.open((web) -> {
			// Add HTTP template with Content-Type
			HttpTemplateSection section = web.addHttpTemplate("uri", "PUBLIC/resource.html", null);
			section.setTemplateContentType("text/plain");
		});

		// Ensure template correct
		MockHttpResponse response = this.assertHttpRequest("/uri", 200, "RESOURCE");
		assertEquals("Incorrect Content-Type on response", "text/plain",
				response.getFirstHeader("Content-Type").getValue());
	}

	/**
	 * Ensure <code>charset</code> parameter providing on unknown Content-Type.
	 */
	public void testTemplateNonTextContentType() throws Exception {

		this.open((web) -> {
			// Add HTTP template with Content-Type
			HttpTemplateSection section = web.addHttpTemplate("uri", "PUBLIC/resource.html", null);
			section.setTemplateContentType("x-test/non-text");
		});

		// Ensure template correct
		MockHttpResponse response = this.assertHttpRequest("/uri", 200, "RESOURCE");
		assertEquals("Incorrect Content-Type on response", "x-test/non-text",
				response.getFirstHeader("Content-Type").getValue());

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
	public void doRenderRedirectTest(String method, String... renderRedirectHttpMethods) throws Exception {

		this.open((web) -> {
			// Add the template
			final String templatePath = this.getClassPath("template.ofp");
			HttpTemplateSection template = web.addHttpTemplate("uri", templatePath, MockTemplateLogic.class);

			// Ensure able to provide appropriate render redirect HTTP methods
			for (String renderRedirectHttpMethod : renderRedirectHttpMethods) {
				template.addRenderRedirectHttpMethod(renderRedirectHttpMethod);
			}
		});

		// Ensure appropriately redirects
		MockHttpRequestBuilder request = MockHttpServer.mockRequest("/uri");
		String redirectUrl = "/uri" + HttpRouteFunction.REDIRECT_URI_SUFFIX;
		this.assertHttpRequest(request, redirectUrl, 200, "/uri-submit");
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

		this.open((web) -> {
			// Add HTTP template (with URL)
			HttpTemplateSection section = web.addHttpTemplate("/", templatePath, MockTemplateLogic.class);
			section.setTemplateSecure(isSecure);
		});

		// Ensure template available at default root
		this.assertHttpRequest("/", isSecure, 200, SUBMIT_URI);

		// Ensure root link works
		this.assertHttpRequest(SUBMIT_URI, isSecure, 200, "submitted" + SUBMIT_URI);
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
		@NextFunction("doNothing")
		public void submit(ServerHttpConnection connection) throws IOException {
			WebArchitectEmployerTest.writeResponse("submitted", connection);
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

		this.open((web) -> {
			// Add HTTP template
			web.addHttpTemplate(TEMPLATE_URI, this.getClassPath("template.ofp"), MockTemplateLogic.class);

			// Ensure indicates template already registered for URI
			try {
				web.addHttpTemplate(TEMPLATE_URI, this.getClassPath("template.ofp"), MockTemplateLogic.class);
				fail("Should not successfully add template for duplicate URI");
			} catch (IllegalStateException ex) {
				assertEquals("Incorrect cause", "HTTP Template already added for URI '/" + TEMPLATE_URI + "'",
						ex.getMessage());
			}

			// Ensure indicates template already registered for canonical URI
			try {
				web.addHttpTemplate("/" + TEMPLATE_URI, this.getClassPath("template.ofp"), MockTemplateLogic.class);
				fail("Should not successfully add template for duplicate URI");
			} catch (IllegalStateException ex) {
				assertEquals("Incorrect cause", "HTTP Template already added for URI '/" + TEMPLATE_URI + "'",
						ex.getMessage());
			}
		});
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

		this.open((web) -> {
			// Add HTTP template
			web.addHttpTemplate("uri", this.getClassPath("template.ofp"), MockTemplateLogic.class)
					.setTemplateSecure(isSecure);
		});

		// Ensure submit on task for template is correct
		this.assertHttpRequest(SUBMIT_URI, isSecure, 200, "submitted" + SUBMIT_URI);
	}

	/**
	 * Ensure can secure a link.
	 */
	public void testSecureLink() throws Exception {

		final String SUBMIT_URI = "/uri-submit";

		this.open((web) -> {
			// Add HTTP template
			web.addHttpTemplate("uri", this.getClassPath("template.ofp"), MockTemplateLogic.class)
					.setLinkSecure("submit", true);
		});

		// Ensure submit on task for template is correct
		this.assertHttpRequest(SUBMIT_URI, true, 200, "submitted" + SUBMIT_URI);
	}

	/**
	 * Ensure can set link as non-secure.
	 */
	public void testNonSecureLink() throws Exception {

		final String SUBMIT_URI = "/uri-submit";

		this.open((web) -> {
			// Add HTTP template
			HttpTemplateSection template = web.addHttpTemplate("uri", this.getClassPath("template.ofp"),
					MockTemplateLogic.class);
			template.setTemplateSecure(true);
			template.setLinkSecure("submit", false);
		});

		// Ensure submit on task for template is correct
		String requestUrl = "http://" + HOST_NAME + ":" + this.port + SUBMIT_URI;
		String redirectUrl = "https://" + HOST_NAME + ":" + this.securePort + "/uri"
				+ HttpRouteFunction.REDIRECT_URI_SUFFIX;
		String linkUrl = "http://" + HOST_NAME + ":" + this.port + SUBMIT_URI;
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(requestUrl);
		this.assertHttpRequest(request, redirectUrl, 200, "submitted" + linkUrl);
	}

	/**
	 * Ensure can inherit template.
	 */
	public void testInheritTemplate() throws Exception {

		this.open((web) -> {
			// Add link target template
			HttpTemplateSection target = web.addHttpTemplate("/target", this.getClassPath("/template.ofp"),
					MockTemplateLogic.class);

			// Add parent template
			HttpTemplateSection parent = web.addHttpTemplate("/parent", this.getClassPath("Parent.ofp"), null);
			web.linkToHttpTemplate(parent.getOfficeSection().getOfficeSectionOutput("submit"), target);

			// Add child template (inheriting content and links)
			HttpTemplateSection child = web.addHttpTemplate("/child", this.getClassPath("Child.ofp"), null);
			child.setSuperHttpTemplate(parent);
		});

		// Ensure child inherits content
		this.assertHttpRequest("/child", 200, "Parent CHILD introduced /child-submit");

		// Ensure child inherits link configuration
		this.assertHttpRequest("/child-submit", 200, "/target-submit");
	}

	/**
	 * Ensure template inheritance hierarchy is in correct order for ancestors.
	 */
	public void testInheritTemplateHierarchy() throws Exception {

		this.open((web) -> {
			// Add link target template
			HttpTemplateSection target = web.addHttpTemplate("/target", this.getClassPath("/template.ofp"),
					MockTemplateLogic.class);

			// Add parent template
			HttpTemplateSection parent = web.addHttpTemplate("/parent", this.getClassPath("Parent.ofp"), null);
			web.linkToHttpTemplate(parent.getOfficeSection().getOfficeSectionOutput("submit"), target);

			// Add child template (inheriting content and links)
			HttpTemplateSection child = web.addHttpTemplate("/child", this.getClassPath("Child.ofp"), null);
			child.setSuperHttpTemplate(parent);

			// Add grand child template (override the link)
			HttpTemplateSection grandChild = web.addHttpTemplate("/grandchild", this.getClassPath("GrandChild.ofp"),
					null);
			grandChild.setSuperHttpTemplate(child);
		});

		// Ensure grand child overrides section with link (no need to inherit)
		this.assertHttpRequest("/grandchild", 200, "Grandchild CHILD introduced Overridden");
	}

	/**
	 * Ensure template can inherit section link configuration.
	 */
	public void testTemplateInheritSectionLinkConfiguration() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add link target template
			HttpTemplateSection target = web.addHttpTemplate("/target", this.getClassPath("/template.ofp"),
					MockTemplateLogic.class);

			// Add grand parent section
			OfficeSection grandParent = context.addSection("GRAND_PARENT", GrandParentSection.class);
			web.linkToHttpTemplate(grandParent.getOfficeSectionOutput("submit"), target);

			// Add parent template (inheriting link configuration)
			HttpTemplateSection parent = web.addHttpTemplate("/parent", this.getClassPath("Parent.ofp"), null);
			parent.getOfficeSection().setSuperOfficeSection(grandParent);

			// Add child template (inheriting content and links)
			HttpTemplateSection child = web.addHttpTemplate("/child", this.getClassPath("Child.ofp"), null);
			child.setSuperHttpTemplate(parent);
		});

		// Ensure child inherits content
		this.assertHttpRequest("/child", 200, "Parent CHILD introduced /child-submit");

		// Ensure child inherits link configuration
		this.assertHttpRequest("/child-submit", 200, "/target-submit");
	}

	/**
	 * Parent section.
	 */
	public static class GrandParentSection {
		@NextFunction("submit")
		public void input() {
		}
	}

	/**
	 * Ensure can inherit link being secure.
	 */
	public void testInheritTemplateLinkSecure() throws Exception {

		this.open((web) -> {
			// Add link target template
			HttpTemplateSection target = web.addHttpTemplate("/target", this.getClassPath("/template.ofp"),
					MockTemplateLogic.class);

			// Add parent template
			HttpTemplateSection parent = web.addHttpTemplate("/parent", this.getClassPath("Parent.ofp"), null);
			parent.setLinkSecure("submit", false);
			web.linkToHttpTemplate(parent.getOfficeSection().getOfficeSectionOutput("submit"), target);

			// Add child template (inheriting content and links)
			HttpTemplateSection child = web.addHttpTemplate("/child", this.getClassPath("Child.ofp"), null);
			parent.setLinkSecure("submit", true); // overrides parent
			child.setSuperHttpTemplate(parent);

			// Add child template (inheriting content and links)
			HttpTemplateSection grandChild = web.addHttpTemplate("/grandchild", this.getClassPath("LinkChild.ofp"),
					null);
			grandChild.setSuperHttpTemplate(child);
		});

		// Ensure child inherits link secure
		this.assertHttpRequest("/grandchild", 200,
				"Parent LINK_CHILD override https://" + HOST_NAME + ":" + this.securePort + "/grandchild-submit");

		// Ensure child inherits link secure
		this.assertHttpRequest("/grandchild-submit", true, 200,
				"http://" + HOST_NAME + ":" + this.port + "/target-submit");
	}

	/**
	 * Ensure can not inherit template link secure if no longer exists (as
	 * containing section overridden and no longer contains the link).
	 */
	public void testNotInheritMissingTemplateLinkSecure() throws Exception {

		this.open((web) -> {
			// Add link target template
			HttpTemplateSection target = web.addHttpTemplate("/target", this.getClassPath("/template.ofp"),
					MockTemplateLogic.class);

			// Add parent template
			HttpTemplateSection parent = web.addHttpTemplate("/parent", this.getClassPath("Parent.ofp"), null);
			parent.setLinkSecure("submit", true);
			web.linkToHttpTemplate(parent.getOfficeSection().getOfficeSectionOutput("submit"), target);

			// Add child template (inheriting content and links)
			HttpTemplateSection child = web.addHttpTemplate("/child", this.getClassPath("Child.ofp"), null);
			child.setSuperHttpTemplate(parent);

			// Add grand child template (override the link)
			HttpTemplateSection grandChild = web.addHttpTemplate("/grandchild", this.getClassPath("GrandChild.ofp"),
					null);
			grandChild.setSuperHttpTemplate(child);
		});

		// Ensure grand child overrides section with link (no need to inherit)
		this.assertHttpRequest("/grandchild", 200, "Grandchild CHILD introduced Overridden");
	}

	/**
	 * Ensure issue if the template inheritance hierarchy is cyclic.
	 */
	public void testCyclicTemplateInheritanceHierarchy() throws Exception {

		// Record issue of cyclic inheritance hierarchy
		final MockCompilerIssues issues = new MockCompilerIssues(this);
		this.compiler.getOfficeFloorCompiler().setCompilerIssues(issues);
		issues.recordIssue("OfficeFloor", OfficeFloorNodeImpl.class,
				"Template /parent has a cyclic inheritance hierarchy ( child : parent : child : ... )");

		// Test
		this.replayMockObjects();

		this.compiler.web((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add link target template
			HttpTemplateSection target = web.addHttpTemplate("/target", this.getClassPath("/template.ofp"),
					MockTemplateLogic.class);

			// Add parent template
			HttpTemplateSection parent = web.addHttpTemplate("/parent", this.getClassPath("Parent.ofp"), null);
			web.linkToHttpTemplate(parent.getOfficeSection().getOfficeSectionOutput("submit"), target);

			// Add child template (inheriting content and links)
			HttpTemplateSection child = web.addHttpTemplate("/child", this.getClassPath("Child.ofp"), null);
			child.setSuperHttpTemplate(parent);

			// Cyclic inheritance hierarchy
			parent.setSuperHttpTemplate(child);
		});

		// Open OfficeFloor (manually to use mock compiler issues)
		OfficeFloor officeFloor = this.compiler.compileOfficeFloor();
		assertNull("Should not have loaded the OfficeFloor", officeFloor);

		// Ensure report cyclic inheritance hierarchy
		this.verifyMockObjects();
	}

	/**
	 * Ensure default template URI suffix is applied.
	 */
	public void testDefaultTemplateUriSuffix() throws Exception {

		final String SUFFIX = ".suffix";
		final String TEMPLATE_URI = "/uri" + SUFFIX;
		final String LINK_URI = "/uri-submit" + SUFFIX;

		this.open((web) -> {
			// Add HTTP template with default template URI suffix
			web.addHttpTemplate("uri", this.getClassPath("template.ofp"), MockTemplateLogic.class);
			web.setDefaultHttpTemplateUriSuffix(SUFFIX);
		});

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

		this.open((web) -> {
			// Add root HTTP template with default template URI suffix
			web.addHttpTemplate("/", this.getClassPath("template.ofp"), MockTemplateLogic.class);
			web.setDefaultHttpTemplateUriSuffix(SUFFIX);
		});

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

		this.open((web) -> {
			// Add HTTP template with default template URI suffix
			web.addHttpTemplate("uri", this.getClassPath("template.ofp"), MockTemplateLogic.class);
			web.setDefaultHttpTemplateUriSuffix(null);
		});

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

		this.open((web) -> {
			// Add HTTP template
			HttpTemplateSection template = web.addHttpTemplate("uri", this.getClassPath("template.ofp"),
					MockTemplateLogic.class);
			template.setTemplateUriSuffix(SUFFIX);
		});

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

		this.open((web) -> {
			// Provide default template URI suffix
			web.setDefaultHttpTemplateUriSuffix(".suffix");

			// Add HTTP template
			HttpTemplateSection template = web.addHttpTemplate("uri", this.getClassPath("template.ofp"),
					MockTemplateLogic.class);
			template.setTemplateUriSuffix(".override");
		});

		// Ensure service template URI with suffix
		this.assertHttpRequest(TEMPLATE_URI, 200, LINK_URI);

		// Ensure service template link URI with suffix
		this.assertHttpRequest(LINK_URI, 200, "submitted" + LINK_URI);
	}

	/**
	 * Ensure appropriate linked URIs.
	 */
	public void testLinkedUris() throws Exception {

		this.open((web) -> {
			// Add HTTP template (not root so should not be included)
			HttpTemplateSection template = web.addHttpTemplate("template", this.getClassPath("template.ofp"),
					MockTemplateLogic.class);

			// Provide URI link
			web.linkUri("uri", template);

			// Validate URIs
			assertUris(web.getURIs(), "/uri");

			// Validate with root HTTP template
			web.addHttpTemplate("/", this.getClassPath("template.ofp"), MockTemplateLogic.class);
			assertUris(web.getURIs(), "/", "/uri");
		});
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
		assertEquals("Incorrect number of URIs", expectedUris.length, actualUris.length);
		for (int i = 0; i < expectedUris.length; i++) {
			assertEquals("Incorrect URI " + i, expectedUris[i], actualUris[i]);
		}
	}

	/**
	 * Ensure able to provide {@link HttpTemplateSectionExtension}.
	 */
	public void testTemplateExtension() throws Exception {

		this.open((web) -> {
			// Add HTTP template
			HttpTemplateSection template = web.addHttpTemplate("template", this.getClassPath("Extension.ofp"),
					MockExtensionTemplateLogic.class);

			// Add template extension
			HttpTemplateAutoWireSectionExtension extension = template
					.addTemplateExtension(MockHttpTemplateSectionExtension.class);
			extension.addProperty("name", "value");
		});

		// Ensure extend the template
		this.assertHttpRequest("/template", 200, "extended");
	}

	/**
	 * Mock {@link HttpTemplateSectionExtension} for testing.
	 */
	public static class MockHttpTemplateSectionExtension implements HttpTemplateSectionExtension {
		@Override
		public void extendTemplate(HttpTemplateSectionExtensionContext context) throws Exception {
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

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add section for handling request
			OfficeSection section = context.addSection("SECTION", MockTemplateLogic.class);
			web.linkUri("test", section.getOfficeSectionInput("submit")).setUriSecure(isSecure);
		});

		// Ensure can send to URI
		this.assertHttpRequest("/test", isSecure, 200, "submitted");
	}

	/**
	 * Ensure able to link {@link OfficeSectionOutput} to {@link HttpTemplate}.
	 */
	public void testLinkToHttpTemplate() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add linking to HTTP template
			OfficeSection section = context.addSection("SECTION", MockLinkHttpTemplate.class);
			web.linkUri("test", section.getOfficeSectionInput("service"));
			HttpTemplateSection template = web.addHttpTemplate("template", this.getClassPath("template.ofp"),
					MockTemplateLogic.class);
			web.linkToHttpTemplate(section.getOfficeSectionOutput("http-template"), template);
		});

		// Ensure link to the HTTP template
		this.assertHttpRequest("/test", 200, "LINK to /template-submit");
	}

	/**
	 * Ensure can inherit link to {@link HttpTemplate}.
	 */
	public void testInheritLinkToHttpTemplate() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add the template
			HttpTemplateSection template = web.addHttpTemplate("template", this.getClassPath("template.ofp"),
					MockTemplateLogic.class);

			// Add parent linking to resource
			OfficeSection parent = context.addSection("PARENT", MockLinkHttpTemplate.class);
			web.linkToHttpTemplate(parent.getOfficeSectionOutput("http-template"), template);

			// Add child inheriting link configuration
			OfficeSection child = context.addSection("CHILD", MockLinkHttpTemplate.class);
			web.linkUri("test", child.getOfficeSectionInput("service"));
			child.setSuperOfficeSection(parent);
		});

		// Ensure link to the HTTP template
		this.assertHttpRequest("/test", 200, "LINK to /template-submit");
	}

	/**
	 * Provides mock functionality to link to a HTTP template.
	 */
	public static class MockLinkHttpTemplate {
		@NextFunction("http-template")
		public void service(ServerHttpConnection connection) throws IOException {
			WebArchitectEmployerTest.writeResponse("LINK to ", connection);
		}
	}

	/**
	 * Ensure able to link to resource.
	 */
	public void testLinkToResource() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add linking to resource
			OfficeSection section = context.addSection("SECTION", MockLinkResource.class);
			web.linkUri("test", section.getOfficeSectionInput("service"));
			web.linkToResource(section.getOfficeSectionOutput("resource"), "resource.html");
		});

		// Ensure provide the resource
		this.assertHttpRequest("/test", 200, "RESOURCE");
	}

	/**
	 * Ensure can inherit link to resource.
	 */
	public void testInheritLinkToResource() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add parent linking to resource
			OfficeSection parent = context.addSection("PARENT", MockLinkResource.class);
			web.linkToResource(parent.getOfficeSectionOutput("resource"), "resource.html");

			// Add child inheriting link configuration
			OfficeSection child = context.addSection("CHILD", MockLinkResource.class);
			web.linkUri("test", child.getOfficeSectionInput("service"));
			child.setSuperOfficeSection(parent);
		});

		// Ensure provide the resource
		this.assertHttpRequest("/test", 200, "RESOURCE");
	}

	/**
	 * Provides mock functionality to link to a resource.
	 */
	public static class MockLinkResource {
		@NextFunction("resource")
		public void service(ServerHttpConnection connection) throws IOException {
			WebArchitectEmployerTest.writeResponse("LINK to ", connection);
		}
	}

	/**
	 * Ensure able to link {@link Escalation} to {@link HttpTemplateSection}.
	 */
	public void testLinkEscalationToTemplate() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add escalation to template
			OfficeSection failingSection = context.addSection("FAILING", FailingSection.class);
			web.linkUri("test", failingSection.getOfficeSectionInput("task"));
			HttpTemplateSection template = web.addHttpTemplate("handler", this.getClassPath("template.ofp"),
					MockTemplateLogic.class);
			web.linkEscalation(SQLException.class, template);
		});

		// Ensure link escalation to template
		this.assertHttpRequest("/test", 200, "Escalated to /handler-submit");
	}

	/**
	 * Section class that fails and provides an {@link Escalation}.
	 */
	public static class FailingSection {
		public void task(ServerHttpConnection connection) throws Exception {
			WebArchitectEmployerTest.writeResponse("Escalated to ", connection);
			throw new SQLException("Test failure");
		}
	}

	/**
	 * Ensure able to link {@link Escalation} to resource.
	 */
	public void testLinkEscalationToResource() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add escalation to resource
			OfficeSection failingSection = context.addSection("FAILING", FailingSection.class);
			web.linkUri("test", failingSection.getOfficeSectionInput("task"));
			web.linkEscalation(SQLException.class, "resource.html");
		});

		// Ensure link escalation to resource
		this.assertHttpRequest("/test", 200, "RESOURCE");
	}

	/**
	 * Ensure able to utilise the Http Session object.
	 */
	public void testHttpSessionObject() throws Exception {

		this.open((web) -> {
			// Add two templates to ensure object available to both
			web.addHttpTemplate("one", this.getClassPath("StatefulObject.ofp"), MockHttpSessionObjectTemplate.class);
			web.addHttpTemplate("two", this.getClassPath("StatefulObject.ofp"), MockHttpSessionObjectTemplate.class);

			// Add the HTTP Session object
			web.addHttpSessionObject(MockHttpSessionObject.class);
		});

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

		this.open((web) -> {
			// Add two templates with annotations for HttpSessionStateful
			web.addHttpTemplate("one", this.getClassPath("StatefulObject.ofp"),
					MockAnnotatedHttpSessionStatefulTemplate.class);
			web.addHttpTemplate("two", this.getClassPath("StatefulObject.ofp"),
					MockAnnotatedHttpSessionStatefulTemplate.class);

			// HTTP Session Object should be detected and added
		});

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
		public MockAnnotatedHttpSessionStatefulObject getTemplate(MockAnnotatedHttpSessionStatefulObject object) {
			object.count++; // increment count to indicate maintaining state
			return object;
		}
	}

	/**
	 * Mock Http Session Object as annotated.
	 */
	@HttpSessionStateful
	public static class MockAnnotatedHttpSessionStatefulObject implements Serializable {
		public int count = 0;

		public int getCount() {
			return count;
		}
	}

	/**
	 * Ensure can override the binding name to the {@link HttpSession}.
	 */
	public void testHttpSessionStatefulAnnotationOverridingBoundName() throws Exception {

		this.open((web) -> {
			// Add the template
			web.addHttpTemplate("template", this.getClassPath("StatefulObject.ofp"),
					MockAnnotatedOverriddenBindNameHttpSessionStatefulTemplate.class);

			// HTTP Session object should be detected and added
		});

		// Ensure same object (test within template logic)
		this.assertHttpRequest("/template", 200, "1");
	}

	/**
	 * Provides mock template logic for validating the overriding binding name
	 * for the {@link HttpSession} object.
	 */
	public static class MockAnnotatedOverriddenBindNameHttpSessionStatefulTemplate implements Serializable {
		public MockAnnotatedOverriddenBindNameHttpSessionStatefulObject getTemplate(
				MockAnnotatedOverriddenBindNameHttpSessionStatefulObject object, HttpSession session) {

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
	public static class MockAnnotatedOverriddenBindNameHttpSessionStatefulObject implements Serializable {
		public int getCount() {
			return 1;
		}
	}

	/**
	 * Ensure able to utilise the {@link HttpRequestState} object.
	 */
	public void testHttpRequestObject() throws Exception {

		final String URI = "/template-submit";

		this.open((web) -> {
			// Add the template
			web.addHttpTemplate("template", this.getClassPath("HttpStateObject.ofp"),
					MockHttpRequestStateTemplate.class);

			// HTTP request object should be detected and added
		});

		// Ensure same object (test within template logic)
		this.assertHttpRequest(URI, 200, "maintained state-" + URI);
	}

	/**
	 * Provides mock template logic for validating the {@link HttpRequestState}.
	 */
	public static class MockHttpRequestStateTemplate {

		public void submit(MockHttpRequestStateObject object, HttpRequestState state) {

			// Ensure object bound under annotated name within the request state
			Object requestObject = state.getAttribute("BIND");
			assertEquals("Should be same object", object, requestObject);

			// Specify value as should maintain state through request
			object.text = "maintained state";
		}

		public MockHttpRequestStateObject getTemplate(MockHttpRequestStateObject object) {
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

		this.open((web) -> {
			// Add the template to use parameters object
			web.addHttpTemplate("template", this.getClassPath("ParametersObject.ofp"),
					MockHttpParametersObjectTemplate.class);

			// Add the HTTP Request Object to load parameters
			web.addHttpRequestObject(MockHttpParametersObject.class, true);
		});

		// Ensure provide HTTP parameters
		this.assertHttpRequest("/template?text=VALUE", 200, "VALUE");
	}

	/**
	 * Provides mock template logic for the HTTP Parameters Object.
	 */
	public static class MockHttpParametersObjectTemplate {
		public MockHttpParametersObject getTemplate(MockHttpParametersObject object) {
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

		this.open((web) -> {
			// Add the template to use parameters object
			web.addHttpTemplate("template", this.getClassPath("ParametersObject.ofp"),
					MockAnnotatedHttpParametersTemplate.class);
		});

		// Ensure provide HTTP parameters
		this.assertHttpRequest("/template?text=VALUE", 200, "VALUE");
	}

	/**
	 * Provides mock template logic for the HTTP Parameters Object.
	 */
	public static class MockAnnotatedHttpParametersTemplate {
		public MockAnnotatedHttpParameters getTemplate(MockAnnotatedHttpParameters object) {
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

		this.open((web) -> {
			// Add the template
			web.addHttpTemplate("template", this.getClassPath("HttpStateObject.ofp"),
					MockHttpApplicationStateTemplate.class);

			// HTTP application state object should be detected and added
		});

		// Ensure same object (test within template logic)
		this.assertHttpRequest(URI, 200, "maintained state-" + URI);
	}

	/**
	 * Provides mock template logic for validating the
	 * {@link HttpApplicationState}.
	 */
	public static class MockHttpApplicationStateTemplate {

		public void submit(MockHttpApplicationStateObject object, HttpApplicationState state) {

			// Ensure object bound under annotated name within application state
			Object applicationObject = state.getAttribute("BIND");
			assertEquals("Should be same object", object, applicationObject);

			// Specify value as should maintain state through application
			object.text = "maintained state";
		}

		public MockHttpApplicationStateObject getTemplate(MockHttpApplicationStateObject object) {
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
		File warDirectory = this.findFile(this.getClass(), "template.ofp").getParentFile();

		// Add configuration
		OfficeFloorCompiler compiler = this.compiler.getOfficeFloorCompiler();
		compiler.addProperty(SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES, warDirectory.getAbsolutePath());
		compiler.addProperty(SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES, "resource.html");

		// Start the HTTP Server
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();

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
		File warDirectory = new File(System.getProperty("java.io.tmpdir"),
				this.getClass().getSimpleName() + "-" + this.getName());
		if (warDirectory.exists()) {
			this.deleteDirectory(warDirectory);
		}
		assertTrue("Failed to create WAR directory", warDirectory.mkdir());

		// Create file within war directory
		FileWriter writer = new FileWriter(new File(warDirectory, "test.html"));
		writer.write(this.getName());
		writer.close();

		// Add configuration
		OfficeFloorCompiler compiler = this.compiler.getOfficeFloorCompiler();
		compiler.addProperty(SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES, warDirectory.getAbsolutePath());
		compiler.addProperty(SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES, "test.html");
		compiler.addProperty(SourceHttpResourceFactory.PROPERTY_DIRECT_STATIC_CONTENT, String.valueOf(false));

		// Start the HTTP Server
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();

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

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add section to override servicing
			OfficeSection section = context.addSection("SECTION", MockChainedServicer.class);
			web.chainServicer(section.getOfficeSectionInput("service"), section.getOfficeSectionOutput("notHandled"));
		});

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

		public void service(ServerHttpConnection connection, Flows flows) throws IOException {

			// Flag that serviced
			isServiced = true;

			// Service
			String uri = connection.getHttpRequest().getRequestURI();
			if ("/chain".equals(uri)) {
				// Service the request
				WebArchitectEmployerTest.writeResponse("CHAIN SERVICED - " + uri, connection);
			} else {
				// Write some content to indicate chained
				WebArchitectEmployerTest.writeResponse("CHAIN - ", connection);

				// Hand off to next in chain
				flows.notHandled();
			}
		}
	}

	/**
	 * Ensure able to specify as the last chain servicer.
	 */
	public void testChainLastServicer() throws Exception {

		this.openWithContext((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Add section to override servicing
			OfficeSection section = context.addSection("SECTION", MockLastChainServicer.class);
			web.chainServicer(section.getOfficeSectionInput("service"), null);

			// Add another in chain (which should be ignored with log warning)
			OfficeSection ignore = context.addSection("ANOTHER", MockChainedServicer.class);
			web.chainServicer(ignore.getOfficeSectionInput("service"), ignore.getOfficeSectionOutput("notHandled"));
		});

		// Ensure override non-routed servicing
		MockChainedServicer.isServiced = false;
		this.assertHttpRequest("/unhandled", 200, "CHAIN END - /unhandled");
		assertFalse("Should not be part of chain", MockChainedServicer.isServiced);
	}

	/**
	 * Provides mock functionality of last chain servicing.
	 */
	public static class MockLastChainServicer {
		public void service(ServerHttpConnection connection) throws IOException {
			String uri = connection.getHttpRequest().getRequestURI();
			WebArchitectEmployerTest.writeResponse("CHAIN END - " + uri, connection);
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
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse assertHttpRequest(String uri, int expectedResponseStatus, String expectedResponseEntity) {
		return this.assertHttpRequest(uri, false, expectedResponseStatus, expectedResponseEntity);
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
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse assertHttpRequest(String uri, boolean isRedirect, int expectedResponseStatus,
			String expectedResponseEntity) {

		// Create the request
		MockHttpRequestBuilder request = MockHttpServer.mockRequest();
		request.method(HttpMethod.GET);
		request.uri(uri);

		// Provide redirect URL if expecting to redirect
		String redirectUrl = null;
		if (isRedirect) {
			redirectUrl = uri + HttpRouteFunction.REDIRECT_URI_SUFFIX;
		}

		// Assert HTTP request
		return this.assertHttpRequest(request, redirectUrl, expectedResponseStatus, expectedResponseEntity);
	}

	/**
	 * Asserts the HTTP Request returns expected result after a redirect.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @param redirectUrl
	 *            Indicates if a redirect should occur and what is the expected
	 *            redirect URL.
	 * @param expectedResponseStatus
	 *            Expected response status.
	 * @param expectedResponseEntity
	 *            Expected response entity.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse assertHttpRequest(MockHttpRequestBuilder request, String redirectUrl,
			int expectedResponseStatus, String expectedResponseEntity) {
		try {

			// Send the request
			MockHttpResponse response = this.server.send(request);

			// Determine if redirect
			if (redirectUrl != null) {

				// Ensure appropriate redirect
				assertEquals("Should be redirect", 303, response.getHttpStatus().getStatusCode());
				assertEquals("Incorrect redirect URL", redirectUrl, response.getFirstHeader("Location").getValue());

				// Send the redirect for response
				MockHttpRequestBuilder redirect = MockHttpServer.mockRequest().uri(redirectUrl);
				response = this.server.send(redirect);
			}

			// Ensure obtained as expected
			String actualResponseBody = response.getHttpEntity(null);
			assertEquals("Incorrect response", expectedResponseEntity, actualResponseBody);

			// Ensure correct response status
			assertEquals("Should be successful", expectedResponseStatus, response.getHttpStatus().getStatusCode());

			// Return the response
			return response;

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
	private static void writeResponse(String response, ServerHttpConnection connection) throws IOException {
		Writer writer = connection.getHttpResponse().getEntityWriter();
		writer.append(response);
	}

}