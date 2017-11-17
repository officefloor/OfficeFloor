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
package net.officefloor.plugin.web.template.section;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;

import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.plugin.web.template.NotRenderTemplateAfter;
import net.officefloor.plugin.web.template.WebTemplateManagedFunctionSource;
import net.officefloor.plugin.web.template.build.WebTemplate;
import net.officefloor.plugin.web.template.build.WebTemplater;
import net.officefloor.plugin.web.template.build.WebTemplaterEmployer;
import net.officefloor.plugin.web.template.extension.WebTemplateExtension;
import net.officefloor.plugin.web.template.extension.WebTemplateExtensionContext;
import net.officefloor.plugin.web.template.parse.ParsedTemplate;
import net.officefloor.plugin.web.template.section.PostRedirectGetLogic.Parameters;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;
import net.officefloor.web.state.HttpRequestState;

/**
 * Tests the integration of the {@link WebTemplateSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionIntegrationTest extends OfficeFrameTestCase {

	/**
	 * Rendered template XML.
	 */
	private static final String RENDERED_TEMPLATE_XML = "<html><body><p>Template Test</p>"
			+ "<p>&lt;img src=&quot;Test.png&quot; /&gt; <img src=\"Test.png\" /></p>"
			+ "<p>Bean with property bean-property  0 1 2 3 4 5 6 7 8 9</p><table>"
			+ "<tr><td>Name</td><td>Description</td></tr>" + "<tr><td>row</td><td>test row</td></tr></table>"
			+ "<form action=\"${LINK_nextTask_QUALIFICATION}/uri-nextTask${LINK_SUFFIX}\"><input type=\"submit\"/></form>"
			+ "<form action=\"${LINK_submit_QUALIFICATION}/uri-submit${LINK_SUFFIX}\"><input type=\"submit\"/></form>"
			+ "<a href=\"${LINK_nonMethodLink_QUALIFICATION}/uri-nonMethodLink${LINK_SUFFIX}\">Non-method link</a>"
			+ "<a href=\"${LINK_notRenderTemplateAfter_QUALIFICATION}/uri-notRenderTemplateAfter${LINK_SUFFIX}\">Not render template after link</a>"
			+ "</body></html>";

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * Mock {@link Connection}.
	 */
	private final Connection connection = this.createSynchronizedMock(Connection.class);

	/**
	 * HTTP port for running on.
	 */
	private int httpPort;

	/**
	 * HTTPS port for running on.
	 */
	private int httpsPort;

	/**
	 * Content-Type of response.
	 */
	private String contentType = null;

	/**
	 * {@link Charset} of response.
	 */
	private Charset charset = null;

	/**
	 * Indicates if non-method link is provided.
	 */
	private boolean isNonMethodLink = false;

	/**
	 * Indicates if service-method link is provided.
	 */
	private boolean isServiceMethodLink = false;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can render the template.
	 */
	public void testRenderTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", false);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NONE, null,
				rendering);
	}

	/**
	 * Ensure can render template with default Content-Type and {@link Charset}.
	 */
	public void testRenderTemplateWithDefaultContentTypeAndCharset() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.contentType = "text/html";
		this.charset = Charset.forName("UTF-8");
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Create the expected Content-Type header value
		String expectedContentType = this.contentType + "; charset=" + this.charset.name();

		// Ensure correct rendering (with headers)
		MockHttpResponse response = this.doRawHttpRequest("/uri", false);
		assertEquals("Incorrect Content-Type", expectedContentType, response.getHeader("Content-Type").getValue());
	}

	/**
	 * Ensure can render template specifying the Content-Type and
	 * {@link Charset}.
	 */
	public void testRenderTemplateWithConfiguredContentTypeAndCharset() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.contentType = "text/plain";
		this.charset = Charset.forName("UTF-16");
		this.startHttpServer("Template.ofp", TemplateLogic.class, WebTemplateSectionSource.PROPERTY_CONTENT_TYPE,
				"text/plain", WebTemplateSectionSource.PROPERTY_CHARSET, "UTF-16");

		// Create the expected Content-Type header value
		String expectedContentType = this.contentType + "; charset=" + this.charset.name();

		// Ensure correct rendering (with headers)
		MockHttpResponse response = this.doRawHttpRequest("/uri", false);
		assertEquals("Incorrect Content-Type", expectedContentType, response.getHeader("Content-Type").getValue());
	}

	/**
	 * Ensure attempting to render template through non-secure link results in
	 * redirect for secure connection.
	 */
	public void testRedirectForSecureTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				WebTemplateManagedFunctionSource.PROPERTY_TEMPLATE_SECURE, String.valueOf(true),
				WebTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "submit", String.valueOf(false));

		// Ensure correct rendering of template
		MockHttpResponse response = this.doRawHttpRequest("/uri-submit", false);
		assertEquals("Should trigger redirect", 303, response.getStatus().getStatusCode());
		assertEquals("Incorrect redirect URL", "TODO define redirect URL", response.getHeader("Location").getValue());
	}

	/**
	 * Ensure attempting to render template through secure link will always be
	 * rendered.
	 */
	public void testNotRedirectForNonSecureTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				WebTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "submit", String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri-submit", true);
		this.assertRenderedResponse("<submit/>", LinkQualify.NON_SECURE, LinkQualify.NONE, LinkQualify.NON_SECURE,
				LinkQualify.NON_SECURE, null, rendering);
	}

	/**
	 * Ensure can render the template with secure links.
	 */
	public void testRenderSecureTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				WebTemplateManagedFunctionSource.PROPERTY_TEMPLATE_SECURE, String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", true);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NONE, null,
				rendering);
	}

	/**
	 * Ensure can render template with a particular secure link.
	 */
	public void testRenderTemplateWithSecureLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				WebTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "submit", String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", false);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.SECURE, LinkQualify.NONE, LinkQualify.NONE, null,
				rendering);
	}

	/**
	 * Ensure can render a secure template with a non-secure link.
	 */
	public void testRenderSecureTemplateWithNonSecureLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				WebTemplateManagedFunctionSource.PROPERTY_TEMPLATE_SECURE, String.valueOf(true),
				WebTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "nonMethodLink", String.valueOf(false));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", true);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NON_SECURE, LinkQualify.NONE,
				null, rendering);
	}

	/**
	 * Ensure maintain HTTP request parameters across redirect.
	 */
	public void testPostRedirectGet_HttpRequestParameters() throws Exception {
		MockHttpRequestBuilder post = this.createHttpPost("?text=TEST");
		this.doPostRedirectGetPatternTest(post, "TEST /uri-post");
	}

	/**
	 * Ensure continue to service GET method without redirect.
	 */
	public void testPostRedirectGet_NoRedirectAsGet() throws Exception {
		MockHttpRequestBuilder request = MockHttpServer.mockRequest("/uri-post?text=TEST");
		this.doPostRedirectGetPatternTest(request, "TEST /uri-post");
	}

	/**
	 * Ensure maintain {@link HttpRequestState} across redirect.
	 */
	public void testPostRedirectGet_AlternateMethod() throws Exception {
		MockHttpRequestBuilder request = MockHttpServer.mockRequest("/uri-post?text=TEST")
				.method(new HttpMethod("OTHER"));
		this.doPostRedirectGetPatternTest(request, "TEST /uri-post",
				WebTemplateInitialManagedFunctionSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS, "OTHER");
	}

	/**
	 * Ensure maintain HTTP entity across redirect.
	 */
	public void testPostRedirectGet_HttpRequestEntity() throws Exception {
		MockHttpRequestBuilder post = this.createHttpPost(null);
		post.getHttpEntity().write("text=TEST".getBytes(Charset.forName("ISO-8859-1")));
		this.doPostRedirectGetPatternTest(post, "TEST /uri-post");
	}

	/**
	 * Ensure maintain HTTP response {@link HttpHeader} across redirect.
	 */
	public void testPostRedirectGet_HttpResponseHeader() throws Exception {
		MockHttpRequestBuilder post = this.createHttpPost("?operation=HEADER");
		MockHttpResponse response = this.doPostRedirectGetPatternTest(post, " /uri-post");
		HttpHeader header = response.getHeader("NAME");
		assertNotNull("Should have HTTP header", header);
		assertEquals("Incorrect HTTP header value", "VALUE", header.getValue());
	}

	/**
	 * Ensure maintain HTTP response entity across redirect.
	 */
	public void testPostRedirectGet_HttpResponseEntity() throws Exception {
		MockHttpRequestBuilder post = this.createHttpPost("?operation=ENTITY");
		this.doPostRedirectGetPatternTest(post, "entity /uri-post");
	}

	/**
	 * Ensure maintain {@link HttpRequestState} across redirect.
	 */
	public void testPostRedirectGet_RequestState() throws Exception {
		MockHttpRequestBuilder post = this.createHttpPost("?operation=REQUEST_STATE");
		this.doPostRedirectGetPatternTest(post, "RequestState /uri-post");
	}

	/**
	 * Creates the {@link MockHttpRequestBuilder} for the POST/Redirect/GET
	 * tests.
	 * 
	 * @param requestUriSuffix
	 *            Optional suffix to request URI. May be <code>null</code>.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	private MockHttpRequestBuilder createHttpPost(String requestUriSuffix) {
		return MockHttpServer.mockRequest("/uri-post" + (requestUriSuffix == null ? "" : requestUriSuffix));
	}

	/**
	 * Undertakes the POST/redirect/GET pattern tests.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @param expectedResponse
	 *            Expected rendered response after redirect.
	 * @param templateProperties
	 *            Template name/value property pairs.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse doPostRedirectGetPatternTest(MockHttpRequestBuilder request, String expectedResponse,
			String... templatePropertyPairs) throws Exception {

		// Start the server
		this.compiler.office((context) -> {
			OfficeManagedObjectSource parametersMos = context.getOfficeArchitect()
					.addOfficeManagedObjectSource("PARAMETERS", HttpRequestObjectManagedObjectSource.class.getName());
			parametersMos.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME,
					Parameters.class.getName());
			parametersMos.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
					String.valueOf(true));
			parametersMos.addOfficeManagedObject("PARAMETERS", ManagedObjectScope.PROCESS);
		});
		this.startHttpServer("PostRedirectGet.ofp", PostRedirectGetLogic.class, templatePropertyPairs);

		// Execute the HTTP request
		MockHttpResponse response = this.server.send(request);

		// No redirect if get
		if (!request.build().getMethod().equals(HttpMethod.GET)) {

			// Ensure is a redirect
			assertEquals("Should be redirect", 303, response.getStatus().getStatusCode());
			String redirectUrl = response.getHeader("Location").getValue();
			assertEquals("Should be no content", -1, response.getEntity().read());

			// Undertake the GET (as triggered by redirect)
			response = this.server.send(MockHttpServer.mockRequest(redirectUrl));
			assertEquals("Should be successful", 200, response.getStatus().getStatusCode());
		}

		// Ensure correct rendering of template
		String rendering = response.getEntity(null);
		assertEquals("Incorrect rendering", expectedResponse, rendering);

		// Return the response
		return response;
	}

	/**
	 * Ensure can handle submit to a link that has {@link NextFunction}
	 * annotation for handling.
	 */
	public void testSubmitWithNextFunction() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "nextFunction - finished(NextFunction)";

		// Ensure correctly renders template on submit
		this.assertHttpRequest("/uri-nextFunction", false, RESPONSE);
	}

	/**
	 * Ensure default behaviour of #{link} method without a {@link NextFunction}
	 * annotation is to render the template.
	 */
	public void testSubmitWithoutNextFunction() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Ensure correctly renders template on submit not invoking flow
		String response = this.doHttpRequest("/uri-submit", false);
		this.assertRenderedResponse("<submit />", LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NONE,
				LinkQualify.NONE, null, response);
	}

	/**
	 * Ensure may use {@link NotRenderTemplateAfter} annotation to avoid
	 * rendering the template afterwards by default.
	 */
	public void testSubmitAndNotRenderTemplateAfter() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Ensure not renders template afterwards
		this.assertHttpRequest("/uri-notRenderTemplateAfter", false, "NOT_RENDER_TEMPLATE_AFTER");
	}

	/**
	 * Ensure with {@link NextFunction} annotation that invoking a Flow takes
	 * precedence.
	 */
	public void testSubmitInvokingFlow() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "<submit /> - doInternalFlow[1] - finished(Parameter for External Flow)";

		// Ensure correctly renders template on submit when invoking flow
		this.assertHttpRequest("/uri-submit?doFlow=true", false, RESPONSE);
	}

	/**
	 * Ensure link straight to template output.
	 */
	public void testNonMethodLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "LINKED";

		// Ensure links out from template
		this.assertHttpRequest("/uri-nonMethodLink", false, RESPONSE);
	}

	/**
	 * Ensure can render page with section methods have Data suffix.
	 */
	public void testDataSuffix() throws Exception {

		// Start the server
		this.startHttpServer("TemplateData.ofp", TemplateDataLogic.class);

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", false);
		assertXmlEquals("Incorrect rendering", "<html><body><p>hello world</p><p>section data</p></body></html>",
				rendering);
	}

	/**
	 * Ensure can render template without logic class and have links.
	 */
	public void testNoLogicClass() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("NoLogicTemplate.ofp", null);

		// Ensure template is correct
		this.assertHttpRequest("/uri", false, " /uri-nonMethodLink /uri-doExternalFlow");

		// Ensure links out from template
		this.assertHttpRequest("/uri-nonMethodLink", false, "LINKED");
	}

	/**
	 * Ensure add context path to link.
	 */
	public void testContextPathForLink() throws Exception {

		// Start the server (with context path)
		this.isNonMethodLink = true;
		this.startHttpServer("NoLogicTemplate.ofp", null);

		// Ensure template has context path for links
		this.assertHttpRequest("/context/uri", false, " /context/uri-nonMethodLink /context/uri-doExternalFlow");
	}

	/**
	 * Ensure able to invoke flows by template logic to alter template
	 * rendering.
	 */
	public void testFlowControl() throws Exception {

		// Start the server
		this.startHttpServer("FlowTemplate.ofp", FlowTemplateLogic.class);

		// Ensure get full template
		this.assertHttpRequest("/uri", false, "TemplateOne1TwoEnd");

		// Ensure skip template one rendering
		this.assertHttpRequest("/uri?getOne=getTwo", false, "TemplateTwoEnd");

		// Ensure can skip to end
		this.assertHttpRequest("/uri?getTemplate=end", false, "End");

		// Ensure can loop back
		this.assertHttpRequest("/uri?getEnd=getTemplate", false, "TemplateOne1TwoTemplateOne1TwoEnd");
	}

	/**
	 * Ensure template stateful across {@link HttpRequest}.
	 */
	public void testStatefulTemplate() throws Exception {

		// Start the server
		this.startHttpServer("StatefulTemplate.ofp", StatefulTemplateLogic.class);

		// Ensure retains state across HTTP requests (by incrementing counter)
		this.assertHttpRequest("/uri", false, "<a href='/uri-increment'>1</a>");
		this.assertHttpRequest("/uri-increment", false, "increment - finished(2)");
		this.assertHttpRequest("/uri", false, "<a href='/uri-increment'>2</a>");
		this.assertHttpRequest("/uri-increment", false, "increment - finished(3)");
		this.assertHttpRequest("/uri", false, "<a href='/uri-increment'>3</a>");
	}

	/**
	 * Ensure on submit link that has next {@link ManagedFunction} instances
	 * that if last {@link ManagedFunction} in Flow does not indicate
	 * {@link NextFunction} that the template is rendered.
	 */
	public void testRenderByDefault() throws Exception {

		// Start the server
		this.startHttpServer("SubmitTemplate.ofp", RenderByDefaultTemplateLogic.class);

		// Ensure render template again by default on link submit
		this.assertHttpRequest("/uri-submit", false, "Submit-RenderByDefault-/uri-submit");
	}

	/**
	 * Template logic for testing.
	 */
	public static class RenderByDefaultTemplateLogic {

		@NextFunction("renderByDefault")
		public void submit(ServerHttpConnection connection) throws IOException {
			writeMessage(connection, "Submit");
		}

		// Next is to render the template
		public void renderByDefault(ServerHttpConnection connection) throws IOException {
			writeMessage(connection, "-RenderByDefault-");
		}

		// Required for test configuration
		@NextFunction("doExternalFlow")
		public void required() {
		}
	}

	/**
	 * Ensure not render {@link ParsedTemplate} afterwards.
	 */
	public void testNotRenderTemplateAfter() throws Exception {

		// Start the server
		this.startHttpServer("SubmitTemplate.ofp", NotRenderTemplateAfterLogic.class);

		// Ensure not render template after on link submit
		this.assertHttpRequest("/uri-submit", false, "Submit-NotRenderTemplateAfter");
	}

	/**
	 * Template logic for testing {@link NotRenderTemplateAfterLogic}.
	 */
	public static class NotRenderTemplateAfterLogic {

		@NextFunction("notRenderTemplateAfter")
		public void submit(ServerHttpConnection connection) throws IOException {
			writeMessage(connection, "Submit");
		}

		@NotRenderTemplateAfter
		public void notRenderTemplateAfter(ServerHttpConnection connection) throws IOException {
			writeMessage(connection, "-NotRenderTemplateAfter");
		}

		// Required for test configuration
		@NextFunction("doExternalFlow")
		public void required() {
		}
	}

	/**
	 * Ensure not render section if null bean.
	 */
	public void testNullSectionBean() throws Exception {

		// Start the server
		this.startHttpServer("NullBeanTemplate.ofp", NullBeanTemplateLogic.class);

		// Ensure not render section for null section bean
		this.assertHttpRequest("/uri", false, "START  END");
	}

	/**
	 * Null bean logic.
	 */
	public static class NullBeanTemplateLogic {

		public Object getSection() {
			return null;
		}

		@NextFunction("doExternalFlow")
		public void necessaryForTest() {
		}
	}

	/**
	 * Ensure render section if void return.
	 */
	public void testVoidSectionBean() throws Exception {

		// Start the server
		this.startHttpServer("NullBeanTemplate.ofp", VoidTemplateLogic.class);

		// Ensure render section for void section return
		this.assertHttpRequest("/uri", false, "START  Should not be rendered on null section bean.  END");
	}

	/**
	 * Void logic.
	 */
	public static class VoidTemplateLogic {

		public void getSection() {
		}

		@NextFunction("doExternalFlow")
		public void necessaryForTest() {
		}
	}

	/**
	 * Ensure not render section if null bean.
	 */
	public void testNullSectionDataBean() throws Exception {

		// Start the server
		this.startHttpServer("NullBeanTemplate.ofp", NullBeanDataTemplateLogic.class);

		// Ensure not render section for null section bean
		this.assertHttpRequest("/uri", false, "START  END");
	}

	/**
	 * Null bean logic with Data suffix.
	 */
	public static class NullBeanDataTemplateLogic {

		public Object getSectionData() {
			return null;
		}

		@NextFunction("doExternalFlow")
		public void necessaryForTest() {
		}
	}

	/**
	 * Ensure can inherit child template.
	 */
	public void testInheritChildTemplate() throws Exception {

		// Start the server
		String parentTemplateLocation = this.getTemplateLocation("ParentTemplate.ofp");
		this.startHttpServer("ChildTemplate.ofp", InheritChildLogic.class,
				WebTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES, parentTemplateLocation);

		// Ensure can inherit sections
		this.assertHttpRequest("/uri", false, "Parent VALUE Introduced Two Footer /uri-doExternalFlow");
	}

	/**
	 * Logic for child inheritance test.
	 */
	public static class InheritChildLogic {

		public InheritChildLogic getOne() {
			return this;
		}

		public String getValue() {
			return "VALUE";
		}
	}

	/**
	 * Ensure can inherit grand child template.
	 */
	public void testInheritGrandChildTemplate() throws Exception {

		// Start the server
		String parentTemplateLocation = this.getTemplateLocation("ParentTemplate.ofp");
		String childTemplateLocation = this.getTemplateLocation("ChildTemplate.ofp");
		this.startHttpServer("GrandChildTemplate.ofp", InheritGrandChildLogic.class,
				WebTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES,
				parentTemplateLocation + ", " + childTemplateLocation);

		// Ensure can inherit sections
		this.assertHttpRequest("/uri", false, "Grand Child TEXT Override Different order Footer /uri-doExternalFlow");
	}

	/**
	 * Logic for grand child inheritance test.
	 */
	public static class InheritGrandChildLogic extends InheritChildLogic {

		public InheritGrandChildLogic getOne(HttpSession differentSignatureToEnsureOverrideByName) {
			return this;
		}

		public String getText() {
			return "TEXT";
		}
	}

	/**
	 * Ensure able to use a {@link WebTemplateExtension}.
	 */
	public void testTemplateSectionExtension() throws Exception {

		// Flag to provide service method link
		this.isServiceMethodLink = true;

		// Start the server (with extension)
		this.startHttpServer("ExtensionTemplate.ofp", MockExtensionTemplateLogic.class, "extension.1",
				MockHttpTemplateSectionExtension.class.getName(), "extension.1.name", "value",
				"extension.1.mock.extension.index", "1", "extension.2",
				MockHttpTemplateSectionExtension.class.getName(), "extension.2.name", "value",
				"extension.2.mock.extension.index", "2", "section.name", "section.value");

		// Ensure change with extension
		this.assertHttpRequest("/uri", false, "Overridden template with overridden class and /uri-serviceLink");

		// Ensure service method not render template
		this.assertHttpRequest("/uri-serviceLink", false, "SERVICE_METHOD");
	}

	/**
	 * Mock {@link WebTemplateExtension} for testing.
	 */
	public static class MockHttpTemplateSectionExtension implements WebTemplateExtension {

		/*
		 * ================== HttpTemplateSectionExtension ====================
		 */

		@Override
		public void extendWebTemplate(WebTemplateExtensionContext context) throws Exception {

			final String TEMPLATE_CONTENT = "Overridden template with ${property} and #{serviceLink}";

			// Obtain the particular extension index
			int extensionIndex = Integer.parseInt(context.getProperty("mock.extension.index"));

			// Validate overriding details
			switch (extensionIndex) {
			case 1:
				// Ensure original template content
				assertEquals("Incorrect original template content", "extension", context.getTemplateContent());
				break;
			case 2:
				// Ensure overridden template content
				assertEquals("Template content should be overridden", TEMPLATE_CONTENT, context.getTemplateContent());
				break;
			default:
				fail("Should only be two extensions");
			}

			// Validate extension configuration
			String[] names = context.getPropertyNames();
			assertEquals("Incorrect number of properties", 2, names.length);
			assertEquals("Incorrect property name", "name", names[0]);
			assertEquals("Incorrect property value", "value", context.getProperty("name"));
			assertEquals("Incorrect property mock.extension.index", "mock.extension.index", names[1]);
			assertEquals("Not defaulting property", "default", context.getProperty("unknown", "default"));
			try {
				// Ensure failure on unknown property
				context.getProperty("unknown");
				fail("Should not successfully obtain unknown property");
			} catch (UnknownPropertyError ex) {
				String unknownPropertyName = "extension." + extensionIndex + ".unknown";
				assertEquals("Incorrect unknown property", unknownPropertyName, ex.getUnknownPropertyName());
				assertEquals("Incurrect unknown property message", "Unknown property '" + unknownPropertyName + "'",
						ex.getMessage());
			}

			// Validate section details
			assertEquals("Incorrect section context", "section.value",
					context.getSectionSourceContext().getProperty("section.name"));
			assertNotNull("Assuming correct designer", context.getSectionDesigner());

			// Extend the template (via overriding)
			context.setTemplateContent(TEMPLATE_CONTENT);

			// Flag a service method
			context.flagAsNonRenderTemplateMethod("serviceMethod");
		}
	}

	/**
	 * Template logic for the extension test.
	 */
	public static class MockExtensionTemplateLogic {

		/**
		 * Necessary as using this for overriding template logic class.
		 * 
		 * @return This.
		 */
		public MockExtensionTemplateLogic getTemplate() {
			return this;
		}

		/**
		 * Property to flag that overriding.
		 * 
		 * @return Value of property on template.
		 */
		public String getProperty() {
			return "overridden class";
		}

		/**
		 * Necessary as using this for overriding template logic class.
		 */
		@NextFunction("doExternalFlow")
		public void submit() {
		}

		/**
		 * Service method that should not render template on completion.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		public void serviceMethod(ServerHttpConnection connection) throws IOException {
			Writer entity = connection.getResponse().getEntityWriter();
			entity.write("SERVICE_METHOD");
			entity.flush();
		}
	}

	/**
	 * Executes the {@link HttpRequest}.
	 * 
	 * @param uri
	 *            URI.
	 * @param isSecure
	 *            Flags whether a secure connection is used.
	 * @return {@link MockHttpResponse}.
	 */
	private MockHttpResponse doRawHttpRequest(String uri, boolean isSecure) throws Exception {
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(uri);
		request.secure(isSecure);
		return this.server.send(request);
	}

	/**
	 * Sends the {@link HttpRequest}.
	 * 
	 * @param uri
	 *            URI.
	 * @param isSecure
	 *            Flags whether a secure connection is used.
	 * @return Content of the {@link HttpResponse}.
	 */
	private String doHttpRequest(String uri, boolean isSecure) throws Exception {

		// Send the request to obtain results of rending template
		MockHttpResponse response = this.doRawHttpRequest(uri, isSecure);

		// Ensure successful
		assertEquals("Ensure successful", 200, response.getStatus().getStatusCode());

		// Obtain and return the response content
		String content = response.getEntity(null);
		return content;
	}

	/**
	 * Asserts the {@link HttpRequest}.
	 * 
	 * @param uri
	 *            URI for the {@link HttpRequest}.
	 * @param isSecure
	 *            Flags whether a secure connection is used.
	 * @param expectedResponse
	 *            Expected content of the {@link HttpResponse}.
	 */
	private void assertHttpRequest(String uri, boolean isSecure, String expectedResponse) throws Exception {

		// Obtain the rendering
		String rendering = this.doHttpRequest(uri, isSecure);

		// Ensure correct rendering of template
		assertEquals("Incorrect rendering", expectedResponse, rendering);
	}

	/**
	 * Identifies the qualification for the link.
	 */
	private static enum LinkQualify {
		NONE, NON_SECURE, SECURE
	}

	/**
	 * Obtains the link qualification.
	 * 
	 * @param linkQualify
	 *            {@link LinkQualify}.
	 * @return Link qualification.
	 */
	private String getLinkQualification(LinkQualify linkQualify) {
		String qualification;
		switch (linkQualify) {
		case NONE:
			qualification = "";
			break;
		case NON_SECURE:
			qualification = "http://TODO define redirect host name:" + this.httpPort;
			break;
		case SECURE:
			qualification = "https://TODO define redirect host name:" + this.httpsPort;
			break;
		default:
			fail("Unknown link qualify " + linkQualify);
			qualification = "UNKNOWN";
		}
		return qualification;
	}

	/**
	 * Asserts the rendered response.
	 * 
	 * @param expectedResponsePrefix
	 *            Prefix on the expected response. Typically for testing
	 *            pre-processing before response.
	 * @param nextFunctionQualify
	 *            {@link LinkQualify} for <code>nextFunction</code> link.
	 * @param submitQualify
	 *            {@link LinkQualify} for <code>submit</code> link.
	 * @param nonMethodLinkQualify
	 *            {@link LinkQualify} for <code>nonMethodLink</code>.
	 * @param linkUriSuffix
	 *            Link URI suffix. May be <code>null</code> for no suffix.
	 * @param actualResponse
	 *            Actual rendered response
	 */
	private void assertRenderedResponse(String expectedResponsePrefix, LinkQualify nextFunctionQualify,
			LinkQualify submitQualify, LinkQualify nonMethodLinkQualify, LinkQualify notRenderTemplateAfter,
			String linkUriSuffix, String actualResponse) {

		// Transform expected response for link qualifications
		String expectedResponse = expectedResponsePrefix + RENDERED_TEMPLATE_XML;
		expectedResponse = expectedResponse.replace("${LINK_nextFunction_QUALIFICATION}",
				this.getLinkQualification(nextFunctionQualify));
		expectedResponse = expectedResponse.replace("${LINK_submit_QUALIFICATION}",
				this.getLinkQualification(submitQualify));
		expectedResponse = expectedResponse.replace("${LINK_nonMethodLink_QUALIFICATION}",
				this.getLinkQualification(nonMethodLinkQualify));
		expectedResponse = expectedResponse.replace("${LINK_notRenderTemplateAfter_QUALIFICATION}",
				this.getLinkQualification(notRenderTemplateAfter));
		expectedResponse = expectedResponse.replace("${LINK_SUFFIX}", (linkUriSuffix == null ? "" : linkUriSuffix));

		// Validate the rendered response
		assertXmlEquals("Incorrect rendering", expectedResponse, actualResponse);
	}

	/**
	 * Starts the HTTP server for testing.
	 * 
	 * @param templateName
	 *            Name of the template file.
	 * @param logicClass
	 *            Template logic class.
	 * @param templateProperties
	 *            Template name/value property pairs.
	 */
	protected void startHttpServer(String templateName, Class<?> logicClass, String... templatePropertyPairs)
			throws Exception {

		// Configure the server
		this.compiler.officeFloor((context) -> {
			this.server = MockHttpServer
					.configureMockHttpServer(context.getDeployedOffice().getDeployedOfficeInput("ROUTE", "route"));
		});

		// Configure the application
		this.compiler.web((context) -> {
			WebArchitect web = context.getWebArchitect();
			OfficeArchitect office = context.getOfficeArchitect();
			WebTemplater templater = WebTemplaterEmployer.employWebTemplater(web, office);

			// Add dependencies
			Singleton.load(office, this.connection, new AutoWire(Connection.class));

			// Provide unknown URL continuation for not handled requests
			OfficeSection unknownUrlContinuationSection = context.addSection("UNKONWN_URL_CONTINUATION",
					UnknownUrlContinuationServicer.class);
			web.chainServicer(unknownUrlContinuationSection.getOfficeSectionInput("service"), null);

			// Load the template section
			final String templateLocation = this.getTemplateLocation(templateName);
			WebTemplate template = templater.addTemplate("uri", templateLocation);
			template.setLogicClass(logicClass);

			// Load the additional properties
			for (int i = 0; i < templatePropertyPairs.length; i += 2) {
				String name = templatePropertyPairs[i];
				String value = templatePropertyPairs[i + 1];
				template.addProperty(name, value);
			}

			// Load mock section for handling outputs
			OfficeSection handleOutputSection = context.addSection("OUTPUT", MockSection.class);

			// Link flow outputs
			office.link(template.getOutput("output"), handleOutputSection.getOfficeSectionInput("finished"));
			office.link(template.getOutput("doExternalFlow"), handleOutputSection.getOfficeSectionInput("finished"));

			// Link non-method link
			if (this.isNonMethodLink) {
				OfficeSection handleOutputLink = context.addSection("LINK", MockLink.class);
				office.link(template.getOutput("nonMethodLink"), handleOutputLink.getOfficeSectionInput("linked"));
			}

			// Link service method link
			if (this.isServiceMethodLink) {
				office.link(template.getOutput("serviceLink"), template.getInput(null));
			}
		});

		// Open the OfficeFloor
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();

	}

	/**
	 * Obtains the template location.
	 * 
	 * @param templateName
	 *            Name of the template.
	 * @return Template location.
	 */
	public String getTemplateLocation(String templateName) {
		return this.getClass().getPackage().getName().replace('.', '/') + "/" + templateName;
	}

	/**
	 * Writes the message.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param message
	 *            Message.
	 * @throws IOException
	 *             If fails to write the message.
	 */
	private static void writeMessage(ServerHttpConnection connection, String message) throws IOException {
		Writer writer = connection.getResponse().getEntityWriter();
		writer.write(message);
		writer.flush();
	}

	/**
	 * Mock section for output tasks of the template.
	 */
	public static class MockSection {
		public void finished(@Parameter String parameter, ServerHttpConnection connection) throws IOException {
			if ((parameter != null) && (parameter.length() > 0)) {
				Writer writer = connection.getResponse().getEntityWriter();
				writer.write(" - finished(");
				writer.write(parameter);
				writer.write(")");
				writer.flush();
			}
		}
	}

	/**
	 * Mock section for non-method link from the template.
	 */
	public static class MockLink {
		public void linked(ServerHttpConnection connection) throws IOException {
			Writer writer = connection.getResponse().getEntityWriter();
			writer.write("LINKED");
			writer.flush();
		}
	}

	/**
	 * Services the unknown URL continuations.
	 */
	public static class UnknownUrlContinuationServicer {
		public void service(ServerHttpConnection connection) throws IOException {
			Writer writer = connection.getResponse().getEntityWriter();
			writer.write("UNKNOWN_URL_CONTINUATION");
			writer.flush();
		}
	}

}