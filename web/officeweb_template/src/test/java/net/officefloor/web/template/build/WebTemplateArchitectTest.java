/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.build;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import net.officefloor.compile.impl.structure.FunctionNamespaceNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;
import net.officefloor.web.security.scheme.MockCredentials;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.template.NotEscaped;
import net.officefloor.web.template.NotRenderTemplateAfter;
import net.officefloor.web.template.extension.WebTemplateExtension;
import net.officefloor.web.template.extension.WebTemplateExtensionContext;
import net.officefloor.web.template.section.WebTemplateSectionSource;
import net.officefloor.web.template.section.WebTemplateSectionSource.WebTemplateManagedFunctionSource;

/**
 * Tests the {@link WebTemplateArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateArchitectTest extends OfficeFrameTestCase {

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
		this.compile.mockHttpServer((server) -> this.server = server);
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
				(context, templater) -> templater.addTemplate(false, "/path", new StringReader("TEST")), "TEST");

		// Ensure default values for template
		response.assertResponse(200, "");
		response.assertHeader("content-type", "text/html");
	}

	/**
	 * Ensure able to render {@link WebTemplate} with sections.
	 */
	public void testTemplateSection() throws Exception {
		this.template("/path", (context, templater) -> templater.addTemplate(false, "/path",
				new StringReader("Template<!-- {section} -->Section")), "TemplateSection");
	}

	/**
	 * Ensure can add template with logic.
	 */
	public void testTemplateLogic() throws Exception {
		this.template("/path",
				(context, templater) -> templater.addTemplate(false, "/path", new StringReader("Data=${value}"))
						.setLogicClass(TemplateLogic.class.getName()),
				"Data=&lt;value&gt;");
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
	 * Ensure able to render properties for a bean.
	 */
	public void testRenderBeanProperty() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("Bean=${bean ${property} $}"))
						.setLogicClass(BeanLogic.class.getName()),
				"Bean=value");
	}

	public static class BeanLogic {
		public BeanLogic getTemplate() {
			return this;
		}

		public BeanLogic getBean() {
			return this;
		}

		public String getProperty() {
			return "value";
		}
	}

	/**
	 * Ensure issue if missing bean property.
	 */
	public void testMissingBeanProperty() throws Exception {
		this.templateIssue((issues) -> {
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordIssue("OFFICE./path.TEMPLATE", FunctionNamespaceNodeImpl.class,
					"Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
							+ WebTemplateManagedFunctionSource.class.getName(),
					new Exception("Property 'missing' can not be sourced from bean type " + BeanLogic.class.getName()));
			issues.recordIssue("OFFICE./path", SectionNodeImpl.class,
					"Failure loading FunctionNamespaceType from source "
							+ WebTemplateManagedFunctionSource.class.getName());
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Failure loading OfficeSectionType from source " + WebTemplateSectionSource.class.getName());
		}, (context, templater) -> templater.addTemplate(false, "/path", new StringReader("Bean=${bean ${missing} $}"))
				.setLogicClass(BeanLogic.class.getName()));
	}

	/**
	 * Ensure can render an array of bean values.
	 */
	public void testRenderArrayOfBeans() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("Bean=${beans  ${property} $}"))
						.setLogicClass(ArrayBeanLogic.class.getName()),
				"Bean= 1 2");
	}

	public static class ArrayBeanLogic {
		private final String value;

		@Dependency
		public ArrayBeanLogic() {
			this(null);
		}

		public ArrayBeanLogic(String value) {
			this.value = value;
		}

		public ArrayBeanLogic getTemplate() {
			return this;
		}

		public ArrayBeanLogic[] getBeans() {
			return new ArrayBeanLogic[] { new ArrayBeanLogic("1"), new ArrayBeanLogic("2") };
		}

		public String getProperty() {
			return this.value;
		}
	}

	/**
	 * Ensure can render an array of sections.
	 */
	public void testRenderArrayOfSections() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path",
								new StringReader("Template <!-- {section}-->${value} <!-- {end}-->end"))
						.setLogicClass(ArraySectionLogic.class.getName()),
				"Template 1 2 3 4 5 end");
	}

	public static class ArraySectionLogic {
		private final String value;

		@Dependency
		public ArraySectionLogic() {
			this(0);
		}

		public ArraySectionLogic(int value) {
			this.value = String.valueOf(value);
		}

		public ArraySectionLogic[] getSection() {
			ArraySectionLogic[] sections = new ArraySectionLogic[5];
			for (int i = 0; i < sections.length; i++) {
				sections[i] = new ArraySectionLogic(i + 1);
			}
			return sections;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * Ensure can render an array of sections last.
	 */
	public void testRenderArrayOfSectionsLast() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("Template <!-- {section}-->${value} "))
						.setLogicClass(ArraySectionLogic.class.getName()),
				"Template 1 2 3 4 5 ");
	}

	/**
	 * Ensure can render section with bean property.
	 */
	public void testTemplateSectionProperty() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("Template <!-- {section} -->${property}"))
						.setLogicClass(TemplateSectionPropertyLogic.class.getName()),
				"Template value");
	}

	public static class TemplateSectionPropertyLogic {
		public TemplateSectionPropertyLogic getSection() {
			return this;
		}

		public String getProperty() {
			return "value";
		}
	}

	/**
	 * Ensure handle <code>null</code> bean.
	 */
	public void testNotRenderNullBean() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("${bean ignored ${property} $}${property}"))
						.setLogicClass(NullBeanLogic.class.getName()),
				"");
	}

	public static class NullBeanLogic {
		public NullBeanLogic getTemplate() {
			return this;
		}

		public NullBeanLogic getBean() {
			return null;
		}

		public String getProperty() {
			return null;
		}
	}

	/**
	 * Ensure section logic {@link Method} can have <code>void</code> return if no
	 * properties are required.
	 */
	public void testSectionWithVoidMethod() throws Exception {
		VoidBeanLogic.isInvoked = false;
		this.template("/path", (context, templater) -> templater
				.addTemplate(false, "/path", new StringReader("TEMPLATE")).setLogicClass(VoidBeanLogic.class.getName()),
				"TEMPLATE");
		assertTrue("Should invoke void template method", VoidBeanLogic.isInvoked);
	}

	public static class VoidBeanLogic {
		private static boolean isInvoked = false;

		public void getTemplate() {
			isInvoked = true;
		}
	}

	/**
	 * Ensure able to trigger section rendering by {@link Flow}.
	 */
	public void testSectionInvokedByFlow() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path",
								new StringReader("First <!-- {second} -->second <!-- {last} -->last"))
						.setLogicClass(FlowLogic.class.getName()),
				"First last");
	}

	@FlowInterface
	public static interface Flows {
		void last();
	}

	public static class FlowLogic {
		public void getSecond(Flows flows) {
			flows.last();
		}
	}

	/**
	 * Ensure issue if section {@link Method} is annotated with {@link Next}.
	 */
	public void testSectionMethodNotAllowedNextFunction() throws Exception {
		this.templateIssue((issues) -> {
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordIssue("OFFICE./path", SectionNodeImpl.class,
					"Template bean method 'getTemplate' must not be annotated with @Next (next function is always rendering template section)");
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Failure loading OfficeSectionType from source " + WebTemplateSectionSource.class.getName());
		}, (context, templater) -> {
			templater.addTemplate(false, "/path", new StringReader("TEMPLATE"))
					.setLogicClass(IllegalNextFunctionLogic.class.getName());
		});
	}

	public static class IllegalNextFunctionLogic {
		@Next("illegal")
		public IllegalNextFunctionLogic getTemplate() {
			return this;
		}
	}

	/**
	 * Ensure not render section for <code>null</code> data.
	 */
	public void testNotRenderSectionForNullData() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("Template <!-- {section} --> Section"))
						.setLogicClass(NullDataLogic.class.getName()),
				"");
	}

	public static class NullDataLogic {
		public NullDataLogic getTemplate() {
			return null;
		}

		public NullDataLogic getSection() {
			return null;
		}
	}

	/**
	 * Ensure issue if {@link WebTemplate} missing bean.
	 */
	public void testMissingTemplateLogicClass() throws Exception {
		this.templateIssue((issues) -> {
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordIssue("OFFICE./path", SectionNodeImpl.class,
					"Must provide template logic class for template /path");
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Failure loading OfficeSectionType from source " + WebTemplateSectionSource.class.getName());
		}, (context, templater) -> {
			templater.addTemplate(false, "/path", new StringReader("Data=${value}"));
		});
	}

	/**
	 * Ensure appropriately escapes values.
	 */
	public void testEscapedValues() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("<html>${content}</html>"))
						.setLogicClass(EscapedLogic.class.getName()),
				"<html>&lt; &quot; ' &mdash; &gt;</html>");
	}

	public static class EscapedLogic {
		public EscapedLogic getTemplate() {
			return this;
		}

		public String getContent() {
			return "< \" ' — >";
		}
	}

	/**
	 * Ensure render {@link NotEscaped}.
	 */
	public void testNotEscapedValue() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("<html>${content}</html>"))
						.setLogicClass(NotEscapedLogic.class.getName()),
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
				(context, templater) -> templater
						.addTemplate(false, "/dynamic/{param}", new StringReader("Data=${value}"))
						.setLogicClass(DynamicPathLogic.class.getName()).setRedirectValuesFunction("getPathValues"),
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
		}, (context, templater) -> {
			templater.addTemplate(false, "/{param}", new StringReader("TEMPLATE"));
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

		}, (context, templater) -> {
			templater.addTemplate(false, "/{param}", new StringReader("TEMPLATE"))
					.setLogicClass(DynamicPathLogic.class.getName());
		});
	}

	/**
	 * Ensure can invoke link from template.
	 */
	public void testLink() throws Exception {
		this.template("/path", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate(false, "/path", new StringReader("Link=#{link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/path+link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/path+link"));
		response.assertResponse(200, "section GET /path+link");
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
				(context, templater) -> templater.addTemplate(false, "/path", new StringReader("Template #{link}"))
						.setLogicClass(LinkRerenderTemplateLogic.class.getName()),
				"Template /path+link");

		// Ensure can GET link triggers redirect to template
		MockHttpResponse response = this.server.send(this.mockRequest("/path+link"));
		response.assertResponse(303, "LINK ", "location", this.contextUrl("", "/path"));

		// Ensure on GET redirect that able to load template
		response = this.server.send(this.mockRequest("/path"));
		response.assertResponse(200, "Template /path+link");
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
				(context, templater) -> templater.addTemplate(false, "/path", new StringReader("Template #{link}"))
						.setLogicClass(LinkNotRerenderTemplateLogic.class.getName()),
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
			WebTemplate template = templater.addTemplate(false, "/path", new StringReader("Link=#{link}"));
			template.setLinkSeparatorCharacter('|');
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/path|link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/path|link"));
		response.assertResponse(200, "section GET /path|link");
	}

	/**
	 * Ensure can invoke link for dynamic path.
	 */
	public void testDynamicLink() throws Exception {
		this.template("/dynamic", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate(false, "/{param}", new StringReader("Link=#{link}"))
					.setLogicClass(DynamicLinkLogic.class.getName()).setRedirectValuesFunction("getPathValues");
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/dynamic+link");

		// Ensure can GET link (use different path parameter)
		MockHttpResponse response = this.server.send(this.mockRequest("/another+link"));
		response.assertResponse(200, "section GET /another+link");
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
			WebTemplate template = templater.addTemplate(false, "/{param}", new StringReader("Link=#{link}"))
					.setLogicClass(DynamicLinkLogic.class.getName()).setRedirectValuesFunction("getPathValues");
			template.setLinkSeparatorCharacter('|');
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/dynamic|link");

		// Ensure can GET link (use different path parameter)
		MockHttpResponse response = this.server.send(this.mockRequest("/another|link"));
		response.assertResponse(200, "section GET /another|link");
	}

	/**
	 * Ensure both GET and POST supported by default for links. Makes easier for
	 * form HTML.
	 */
	public void testGetAndPostDefaults() throws Exception {
		this.template("/default", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate(false, "/default", new StringReader("Link=#{link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/default+link");

		// Ensure can GET link
		MockHttpResponse response = this.server.send(this.mockRequest("/default+link").method(HttpMethod.GET));
		response.assertResponse(200, "section GET /default+link");

		// Ensure can POST link
		response = this.server.send(this.mockRequest("/default+link").method(HttpMethod.POST));
		response.assertResponse(200, "section POST /default+link");
	}

	/**
	 * Ensure configure link as POST.
	 */
	public void testPostLinkOnly() throws Exception {
		this.template("/post", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate(false, "/post", new StringReader("Link=#{POST:link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/post+link");

		// Ensure can POST link
		MockHttpResponse response = this.server.send(this.mockRequest("/post+link").method(HttpMethod.POST));
		response.assertResponse(200, "section POST /post+link");

		// Ensure can not GET link (as specifies only POST)
		response = this.server.send(this.mockRequest("/post+link").method(HttpMethod.GET));
		response.assertResponse(405, "");
	}

	/**
	 * Ensure configure link as PUT. This is typically for Javascript requests.
	 */
	public void testPutJavaScriptLink() throws Exception {
		this.template("/put", (context, templater) -> {
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			WebTemplate template = templater.addTemplate(false, "/put", new StringReader("Link=#{PUT:link}"));
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=/put+link");

		// Ensure can POST link
		MockHttpResponse response = this.server.send(this.mockRequest("/put+link").method(HttpMethod.PUT));
		response.assertResponse(200, "section PUT /put+link");
	}

	/**
	 * Ensure allows template responses to {@link HttpMethod} values other than GET.
	 */
	public void testOtherMethod() throws Exception {
		MockHttpResponse response = this.template(
				(context, templater) -> templater.addTemplate(false, "/path", new StringReader("TEMPLATE"))
						.addRenderHttpMethod("TEST"),
				this.mockRequest("/path").method(HttpMethod.getHttpMethod("TEST")));
		response.assertResponse(200, "TEMPLATE");
	}

	/**
	 * Ensure can link to template.
	 */
	public void testLinkToTemplate() throws Exception {
		MockHttpResponse response = this.template("/redirect+link", (context, templater) -> {
			WebTemplate redirect = templater.addTemplate(false, "/redirect", new StringReader("#{link}"));
			WebTemplate template = templater.addTemplate(false, "/template", new StringReader("TEMPLATE"));
			context.getOfficeArchitect().link(redirect.getOutput("link"), template.getRender(null));
		}, "");

		// Ensure redirect
		response.assertResponse(303, "", "location", this.contextUrl("", "/template"));
		String location = response.getHeader("location").getValue();

		// Fire redirect to then get the template
		response = this.server.send(this.mockRequest(location).cookies(response));
		response.assertResponse(200, "TEMPLATE");
	}

	/**
	 * Ensure can link to template with path parameters.
	 */
	public void testLinkToTemplateWithDynamicPath() throws Exception {
		MockHttpResponse redirect = this.template("/redirect", (context, templater) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			WebArchitect web = context.getWebArchitect();
			OfficeSection section = context.addSection("SECTION", DynamicPathSection.class);
			office.link(web.getHttpInput(false, "/redirect").getInput(), section.getOfficeSectionInput("service"));
			WebTemplate template = templater.addTemplate(false, "/{param}", new StringReader("TEMPLATE"))
					.setLogicClass(LinkDynamicPathLogic.class.getName()).setRedirectValuesFunction("getPathValues");
			office.link(section.getOfficeSectionOutput("template"),
					template.getRender(DynamicPathSection.class.getName()));
		}, "");

		// Ensure redirect
		redirect.assertResponse(303, "", "location", this.contextUrl("", "/value"));
		String location = redirect.getHeader("location").getValue();

		// Fire redirect to then get the template
		MockHttpResponse response = this.server.send(this.mockRequest(location).cookies(redirect));
		response.assertResponse(200, "TEMPLATE");
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
		@Next("template")
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
				.addTemplate(false, "/path", new StringReader("{value: JSON}")).setContentType("application/json"),
				"{value: JSON}");
		response.assertHeader("content-type", "application/json");
	}

	/**
	 * Ensure can change the {@link Charset} for rendering the template.
	 */
	public void testCharset() throws Exception {
		String charsetName = "UTF-16";
		Charset charset = Charset.forName(charsetName);
		MockHttpResponse response = this.template((context, templater) -> templater
				.addTemplate(false, "/path", new StringReader("UTF-16 rendered")).setCharset(charsetName),
				this.mockRequest("/path"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		assertEquals("Incorrect content", "UTF-16 rendered", response.getEntity(charset));
	}

	/**
	 * Ensure only sends {@link WebTemplate} content over a secure connection.
	 */
	public void testSecureTemplate() throws Exception {
		MockHttpResponse response = this.template("/path",
				(context, templater) -> templater.addTemplate(true, "/path", new StringReader("SECURE")), "");

		// Non-secure request should have redirect to secure connection
		response.assertResponse(307, "", "location", this.contextUrl("https://mock.officefloor.net", "/path"));

		// Ensure able to obtain template over secure connection
		response = this.server.send(this.mockRequest("/path").secure(true));
		response.assertResponse(200, "SECURE");
	}

	/**
	 * Ensure can render insecure {@link WebTemplate} over a secure connection.
	 */
	public void testInsecureTemplateOnSecureLink() throws Exception {
		MockHttpResponse response = this.template(
				(context, templater) -> templater.addTemplate(false, "/path", new StringReader("INSECURE")),
				this.mockRequest("/path").secure(true));

		// Should obtain insecure template on secure connection
		response.assertResponse(200, "INSECURE");
	}

	/*
	 * Ensure only accepts link request over a secure connection.
	 */
	public void testSecureLink() throws Exception {
		MockHttpResponse response = this.template("/path+link", (context, templater) -> {
			WebTemplate template = templater.addTemplate(false, "/path", new StringReader("Link=#{link}"))
					.setLinkSecure("link", true);
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "");

		// Not-secure request should a have a redirect to secure connection
		response.assertResponse(307, "", "location", this.contextUrl("https://mock.officefloor.net", "/path+link"));

		// Ensure able to obtain link over secure connection
		response = this.server.send(this.mockRequest("/path+link").secure(true));
		response.assertResponse(200, "section GET /path+link");
	}

	/**
	 * Ensure can render a secure link for an insecure {@link WebTemplate}.
	 */
	public void testRenderSecureLink() throws Exception {
		this.template("/path", (context, templater) -> {
			WebTemplate template = templater.addTemplate(false, "/path", new StringReader("Link=#{link}"))
					.setLinkSecure("link", true);
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, "Link=https://mock.officefloor.net/path+link");
	}

	/**
	 * Ensure can render insecure link on a secure {@link WebTemplate}.
	 */
	public void testRenderInsecureLinkOnSecureTemplate() throws Exception {
		MockHttpResponse response = this.template((context, templater) -> {
			WebTemplate template = templater.addTemplate(true, "/path", new StringReader("Link=#{link}"))
					.setLinkSecure("link", false);
			OfficeSection section = context.addSection("SECTION", MockSection.class);
			context.getOfficeArchitect().link(template.getOutput("link"), section.getOfficeSectionInput("service"));
		}, this.mockRequest("/path").secure(true));

		// Keep link secure (to avoid browser warning issues)
		// (Also, avoids another connection needing to be established)
		response.assertResponse(200, "Link=/path+link");
	}

	/**
	 * Ensure {@link HttpSecurity} can be applied to the {@link WebTemplate}.
	 */
	@SuppressWarnings("unused")
	public void testAuthentication() throws Exception {

		// FIXME implement code to pass this test
		if (true) {
			System.err.println("TODO implement " + this.getClass().getName() + " testAuthentication");
			return;
		}

		MockHttpResponse response = this.template((context, templater) -> {
			WebArchitect web = context.getWebArchitect();
			OfficeArchitect office = context.getOfficeArchitect();
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, office,
					context.getOfficeSourceContext());
			security.addHttpSecurity("SECURITY", new MockChallengeHttpSecuritySource("REALM"));
			WebTemplate template = templater.addTemplate(false, "/path", new StringReader("SECURE"));
			template.getHttpSecurer();
			OfficeSection section = context.addSection("SECTION", NoAccessSection.class);
			context.getOfficeArchitect().link(template.getOutput("AcccessDisallowed"),
					section.getOfficeSectionInput("service"));
		}, this.mockRequest("/path"));

		// Ensure require being authenticated
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"));

		// Ensure can access template after authentication
		response = this.server.send(new MockCredentials("test", "test").loadHttpRequest(this.mockRequest("/path")));
		response.assertResponse(200, "SECURE");
	}

	public static class NoAccessSection {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("No Access");
		}
	}

	/**
	 * Ensure {@link HttpAccessControl} can be applied to the {@link WebTemplate}.
	 */
	@SuppressWarnings("unused")
	public void testAccessControl() throws Exception {

		// FIXME implement code to pass this test
		if (true) {
			System.err.println("TODO implement " + this.getClass().getName() + " testAccessControl");
			return;
		}

		MockHttpResponse response = this.template((context, templater) -> {
			WebArchitect web = context.getWebArchitect();
			OfficeArchitect office = context.getOfficeArchitect();
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, office,
					context.getOfficeSourceContext());
			security.addHttpSecurity("SECURITY", new MockChallengeHttpSecuritySource("REALM"));
			WebTemplate template = templater.addTemplate(false, "/path", new StringReader("SECURE"));
			template.getHttpSecurer().addRole("role");
			OfficeSection section = context.addSection("SECTION", NoAccessSection.class);
			context.getOfficeArchitect().link(template.getOutput("AcccessDisallowed"),
					section.getOfficeSectionInput("service"));
		}, this.mockRequest("/path"));

		// Ensure require being authenticated
		response.assertResponse(401, "", "www-authenticate",
				MockChallengeHttpSecuritySource.getHeaderChallengeValue("REALM"));

		// Ensure route to no access
		response = this.server.send(new MockCredentials("test", "test").loadHttpRequest(this.mockRequest("/path")));
		response.assertResponse(200, "No Access");

		// Ensure can access template with role
		response = this.server.send(new MockCredentials("role", "role").loadHttpRequest(this.mockRequest("/path")));
		response.assertResponse(200, "SECURE");
	}

	/**
	 * Ensure can load {@link WebTemplate} from a resource.
	 */
	public void testLoadTemplateFromResource() throws Exception {
		this.template("/path",
				(context, templater) -> templater.addTemplate(false, "/path", this.location("Resource.ofp")),
				"Resource loaded");
	}

	/**
	 * Ensure can specify super {@link WebTemplate}.
	 */
	public void testSuperTemplate() throws Exception {
		this.template("/child", (context, templater) -> {
			WebTemplate parent = templater.addTemplate(false, "/parent",
					new StringReader("TEST <!-- {section} --> PARENT"));
			templater.addTemplate(false, "/child", new StringReader("<!-- {:section} -->Child"))
					.setSuperTemplate(parent);
		}, "TEST Child");
	}

	/**
	 * Ensure can load {@link WebTemplate} and it's super {@link WebTemplate} from a
	 * resource.
	 */
	public void testLoadSuperFromResource() throws Exception {
		this.template("/child", (context, templater) -> {
			WebTemplate parent = templater.addTemplate(false, "/parent", this.location("Parent.ofp"));
			WebTemplate child = templater.addTemplate(false, "/child", new StringReader("<!-- {:override} -->Child"));
			child.setSuperTemplate(parent);
		}, "Parent Child");
	}

	/**
	 * Ensure can specify multiple {@link WebTemplate} instances for inheritance.
	 */
	public void testGrandSuperTemplate() throws Exception {
		this.template("/child", (context, templater) -> {
			WebTemplate grand = templater.addTemplate(false, "/grand",
					new StringReader("Grand <!-- {section} --> Parent"));
			WebTemplate parent = templater.addTemplate(false, "/parent",
					new StringReader("<!-- {:section} --> Override, but overridden"));
			parent.setSuperTemplate(grand);
			WebTemplate child = templater.addTemplate(false, "/child", new StringReader("<!-- {:section} -->Child"));
			child.setSuperTemplate(parent);
		}, "Grand Child");
	}

	/**
	 * Ensure issue if inheritance cycle.
	 */
	public void testInheritanceCycle() throws Exception {
		this.templateIssue((issues) -> {
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"WebTemplate inheritance cycle /child :: /parent :: /grand :: /child :: ...");
		}, (context, templater) -> {
			WebTemplate child = templater.addTemplate(false, "/child", new StringReader("<!-- {:section} -->"));
			WebTemplate parent = templater.addTemplate(false, "/parent", new StringReader("<!-- {:section} -->"));
			WebTemplate grand = templater.addTemplate(false, "/grand", new StringReader("Grand"));

			// Create cycle
			child.setSuperTemplate(parent);
			parent.setSuperTemplate(grand);
			grand.setSuperTemplate(child);
		});
	}

	/**
	 * Ensure can provide <code>Data</code> suffix to section {@link Method} name.
	 */
	public void testDataSuffix() throws Exception {
		this.template("/path",
				(context, templater) -> templater
						.addTemplate(false, "/path", new StringReader("${property}=<!-- {section} -->${property}"))
						.setLogicClass(DataSuffixLogic.class.getName()),
				"value=value");
	}

	public static class DataSuffixLogic {
		public DataSuffixLogic getTemplateData() {
			return this;
		}

		public DataSuffixLogic getSectionData() {
			return this;
		}

		public String getProperty() {
			return "value";
		}
	}

	/**
	 * Ensure can make the {@link WebTemplate} logic class
	 * {@link HttpSessionStateful}.
	 */
	public void testStatefulTemplate() throws Exception {
		MockHttpResponse response = this.template("/path", (context, templater) -> templater
				.addTemplate(false, "/path", new StringReader("${count}")).setLogicClass(StatefulLogic.class.getName()),
				"1");
		for (int i = 2; i < 10; i++) {
			assertEquals("Should stateful increment call count to " + i, String.valueOf(i),
					this.server.send(this.mockRequest("/path").cookies(response)).getEntity(null));
		}
	}

	@HttpSessionStateful
	public static class StatefulLogic implements Serializable {
		private static final long serialVersionUID = 1L;

		private int count = 0;

		public StatefulLogic getTemplate() {
			return this;
		}

		public int getCount() {
			this.count++;
			return this.count;
		}
	}

	/**
	 * Ensure can extend the {@link WebTemplate}.
	 */
	public void testExtendTemplate() throws Exception {
		this.template("/extend", (context, templater) -> {
			WebTemplate template = templater.addTemplate(false, "/extend", new StringReader("original"));
			template.addExtension(MockWebTemplateExtension.class.getName()).addProperty("test", "available");
		}, "extended");
	}

	public static class MockWebTemplateExtension implements WebTemplateExtension {
		@Override
		public void extendWebTemplate(WebTemplateExtensionContext context) throws Exception {
			assertEquals("Should obtain configured property", "available", context.getProperty("test"));
			context.setTemplateContent("extended");
		}
	}

	/**
	 * Adds the context path to the path.
	 * 
	 * @param server Server details (e.g. http://officefloor.net:80 ).
	 * @param path   Path.
	 * @return URL with the context path.
	 */
	private String contextUrl(String server, String path) {
		if (this.contextPath != null) {
			path = this.contextPath + path;
		}
		return server + path;
	}

	/**
	 * Obtains the location for the file.
	 * 
	 * @param fileName Name of file.
	 * @return Location of the file.
	 */
	private String location(String fileName) {
		return this.getFileLocation(this.getClass(), fileName);
	}

	/**
	 * Creates a {@link MockHttpRequestBuilder} for the path (including context
	 * path).
	 * 
	 * @param path Path for the {@link MockHttpRequestBuilder}.
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
		 * @param context   {@link CompileWebContext}.
		 * @param templater {@link WebTemplateArchitect}.
		 */
		void initialise(CompileWebContext context, WebTemplateArchitect templater);
	}

	/**
	 * Runs a {@link WebTemplate}.
	 * 
	 * @param initialiser {@link Initialiser} to initialise {@link WebTemplate}.
	 * @param request     {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse template(Initialiser initialiser, MockHttpRequestBuilder request) throws Exception {
		this.compile.web((context) -> {
			WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(context.getWebArchitect(),
					context.getOfficeArchitect(), context.getOfficeSourceContext());
			initialiser.initialise(context, templater);
			templater.informWebArchitect();
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
		return this.server.send(request);
	}

	/**
	 * Runs a {@link WebTemplate} and validates the {@link HttpResponse} content.
	 * 
	 * @param path             Request path.
	 * @param initialiser      {@link Initialiser} to initialise
	 *                         {@link WebTemplate}.
	 * @param requestPath      Request path.
	 * @param expectedTemplate Expected content of {@link WebTemplate}.
	 * @return {@link MockHttpResponse} for further validation.
	 */
	private MockHttpResponse template(String path, Initialiser initialiser, String expectedTemplate) throws Exception {
		MockHttpResponse response = this.template(initialiser, this.mockRequest(path));
		assertEquals("Incorrect template response", expectedTemplate, response.getEntity(null));
		return response;
	}

	/**
	 * Attempts to load {@link WebTemplateArchitect}, however should have
	 * {@link CompilerIssues}.
	 * 
	 * @param configureIssues {@link Consumer} to configure the
	 *                        {@link CompilerIssues}.
	 * @param initialiser     {@link Initialiser} to initialise the
	 *                        {@link WebTemplate}.
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
			WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(context.getWebArchitect(),
					context.getOfficeArchitect(), context.getOfficeSourceContext());
			initialiser.initialise(context, templater);
			templater.informWebArchitect();
		});
		this.compile.compileOfficeFloor();
		this.verifyMockObjects();
	}

}
