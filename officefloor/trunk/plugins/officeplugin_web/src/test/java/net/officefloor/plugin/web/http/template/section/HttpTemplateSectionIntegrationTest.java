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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.sql.Connection;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.plugin.section.work.WorkSectionSource;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpRequestObjectManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateManagedObjectSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.route.HttpRouteTask;
import net.officefloor.plugin.web.http.route.HttpRouteWorkSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.NotRenderTemplateAfter;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.PostRedirectGetLogic.Parameters;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 * Tests the integration of the {@link HttpTemplateSectionSource}.
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
			+ "<tr><td>Name</td><td>Description</td></tr>"
			+ "<tr><td>row</td><td>test row</td></tr></table>"
			+ "<form action=\"${LINK_nextTask_QUALIFICATION}/uri-nextTask${LINK_SUFFIX}\"><input type=\"submit\"/></form>"
			+ "<form action=\"${LINK_submit_QUALIFICATION}/uri-submit${LINK_SUFFIX}\"><input type=\"submit\"/></form>"
			+ "<a href=\"${LINK_nonMethodLink_QUALIFICATION}/uri-nonMethodLink${LINK_SUFFIX}\">Non-method link</a>"
			+ "<a href=\"${LINK_notRenderTemplateAfter_QUALIFICATION}/uri-notRenderTemplateAfter${LINK_SUFFIX}\">Not render template after link</a>"
			+ "</body></html>";

	/**
	 * Host name.
	 */
	private static final String HOST_NAME = HttpApplicationLocationManagedObjectSource
			.getDefaultHostName();

	/**
	 * {@link AutoWireOfficeFloorSource}.
	 */
	private final AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();

	/**
	 * Mock {@link Connection}.
	 */
	private final Connection connection = this
			.createSynchronizedMock(Connection.class);

	/**
	 * HTTP port for running on.
	 */
	private int httpPort;

	/**
	 * HTTPS port for running on.
	 */
	private int httpsPort;

	/**
	 * Indicates if non-method link is provided.
	 */
	private boolean isNonMethodLink = false;

	/**
	 * Indicates if service-method link is provided.
	 */
	private boolean isServiceMethodLink = false;

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	/**
	 * {@link HttpClient}.
	 */
	private HttpClient client;

	@Override
	protected void setUp() throws Exception {

		// Obtain the ports
		this.httpPort = MockHttpServer.getAvailablePort();
		this.httpsPort = MockHttpServer.getAvailablePort();

		// Create the client that will not automatically redirect
		HttpParams params = new BasicHttpParams();
		params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
		this.client = new DefaultHttpClient(params);

		// Allow anonymous secure communication for testing
		MockHttpServer.configureHttps(this.client, this.httpsPort);
	}

	@Override
	protected void tearDown() throws Exception {

		// Disconnect client
		this.client.getConnectionManager().shutdown();

		// Close the OfficeFloor
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
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.NONE,
				LinkQualify.NONE, LinkQualify.NONE, null, rendering);
	}

	/**
	 * Ensure can render template with a URI suffix.
	 */
	public void testRenderTemplateWithUriSuffix() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI_SUFFIX, ".suffix");

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri.suffix", false);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.NONE,
				LinkQualify.NONE, LinkQualify.NONE, ".suffix", rendering);
	}

	/**
	 * Ensure attempting to render template through non-secure link results in
	 * redirect for secure connection.
	 */
	public void testRedirectForSecureTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
				String.valueOf(true),
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX + "submit",
				String.valueOf(false));

		// Ensure correct rendering of template
		HttpResponse response = this.doRawHttpRequest("/uri-submit", false);
		assertEquals("Should trigger redirect", 303, response.getStatusLine()
				.getStatusCode());
		assertEquals("Incorrect redirect URL", "https://" + HOST_NAME + ":"
				+ this.httpsPort + "/uri" + HttpRouteTask.REDIRECT_URI_SUFFIX,
				response.getFirstHeader("Location").getValue());
	}

	/**
	 * Ensure attempting to render template through secure link will always be
	 * rendered.
	 */
	public void testNotRedirectForNonSecureTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX + "submit",
				String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri-submit", true);
		this.assertRenderedResponse("<submit/>", LinkQualify.NON_SECURE,
				LinkQualify.NONE, LinkQualify.NON_SECURE,
				LinkQualify.NON_SECURE, null, rendering);
	}

	/**
	 * Ensure can render the template with secure links.
	 */
	public void testRenderSecureTemplate() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
				String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", true);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.NONE,
				LinkQualify.NONE, LinkQualify.NONE, null, rendering);
	}

	/**
	 * Ensure can render template with a particular secure link.
	 */
	public void testRenderTemplateWithSecureLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX + "submit",
				String.valueOf(true));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", false);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.SECURE,
				LinkQualify.NONE, LinkQualify.NONE, null, rendering);
	}

	/**
	 * Ensure can render a secure template with a non-secure link.
	 */
	public void testRenderSecureTemplateWithNonSecureLink() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
				String.valueOf(true),
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX
						+ "nonMethodLink", String.valueOf(false));

		// Ensure correct rendering of template
		String rendering = this.doHttpRequest("/uri", true);
		this.assertRenderedResponse("", LinkQualify.NONE, LinkQualify.NONE,
				LinkQualify.NON_SECURE, LinkQualify.NONE, null, rendering);
	}

	/**
	 * Ensure maintain HTTP request parameters across redirect.
	 */
	public void testPostRedirectGet_HttpRequestParameters() throws Exception {
		HttpPost post = this.createHttpPost("?text=TEST");
		this.doPostRedirectGetPatternTest(post, "TEST /uri-post");
	}

	/**
	 * Ensure continue to service GET method without redirect.
	 */
	public void testPostRedirectGet_NoRedirectAsGet() throws Exception {
		HttpGet request = new HttpGet("http://" + HOST_NAME + ":"
				+ this.httpPort + "/uri-post?text=TEST");
		this.doPostRedirectGetPatternTest(request, "TEST /uri-post");
	}

	/**
	 * Ensure maintain {@link HttpRequestState} across redirect.
	 */
	public void testPostRedirectGet_AlternateMethod() throws Exception {
		HttpUriRequest request = new HttpOther("http://" + HOST_NAME + ":"
				+ this.httpPort + "/uri-post?text=TEST");
		this.doPostRedirectGetPatternTest(
				request,
				"TEST /uri-post",
				HttpTemplateInitialWorkSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
				request.getMethod());
	}

	/**
	 * {@link HttpUriRequest} for HTTP method <code>OTHER</code>.
	 */
	private static class HttpOther extends HttpEntityEnclosingRequestBase {

		/**
		 * Initiate.
		 * 
		 * @param uri
		 *            URI.
		 */
		public HttpOther(String uri) {
			this.setURI(URI.create(uri));
		}

		@Override
		public String getMethod() {
			return "OTHER";
		}
	}

	/**
	 * Ensure maintain HTTP entity across redirect.
	 */
	public void testPostRedirectGet_HttpRequestEntity() throws Exception {
		HttpPost post = this.createHttpPost(null);
		post.setEntity(new StringEntity("text=TEST", "ISO-8859-1"));
		this.doPostRedirectGetPatternTest(post, "TEST /uri-post");
	}

	/**
	 * Ensure maintain HTTP response {@link HttpHeader} across redirect.
	 */
	public void testPostRedirectGet_HttpResponseHeader() throws Exception {
		HttpPost post = this.createHttpPost("?operation=HEADER");
		HttpResponse response = this.doPostRedirectGetPatternTest(post,
				" /uri-post");
		Header header = response.getFirstHeader("NAME");
		assertNotNull("Should have HTTP header", header);
		assertEquals("Incorrect HTTP header value", "VALUE", header.getValue());
	}

	/**
	 * Ensure maintain HTTP response entity across redirect.
	 */
	public void testPostRedirectGet_HttpResponseEntity() throws Exception {
		HttpPost post = this.createHttpPost("?operation=ENTITY");
		this.doPostRedirectGetPatternTest(post, "entity /uri-post");
	}

	/**
	 * Ensure maintain {@link HttpRequestState} across redirect.
	 */
	public void testPostRedirectGet_RequestState() throws Exception {
		HttpPost post = this.createHttpPost("?operation=REQUEST_STATE");
		this.doPostRedirectGetPatternTest(post, "RequestState /uri-post");
	}

	/**
	 * Creates the {@link HttpPost} for the POST/Redirect/GET tests.
	 * 
	 * @param requestUriSuffix
	 *            Optional suffix to request URI. May be <code>null</code>.
	 * @return {@link HttpPost}.
	 */
	private HttpPost createHttpPost(String requestUriSuffix) {
		return new HttpPost("http://" + HOST_NAME + ":" + this.httpPort
				+ "/uri-post"
				+ (requestUriSuffix == null ? "" : requestUriSuffix));
	}

	/**
	 * Undertakes the POST/redirect/GET pattern tests.
	 * 
	 * @param request
	 *            {@link HttpUriRequest}.
	 * @param expectedResponse
	 *            Expected rendered response after redirect.
	 * @param templateProperties
	 *            Template name/value property pairs.
	 * @return {@link HttpResponse}.
	 */
	private HttpResponse doPostRedirectGetPatternTest(HttpUriRequest request,
			String expectedResponse, String... templatePropertyPairs)
			throws Exception {

		// Start the server
		AutoWireObject parameters = this.source.addManagedObject(
				HttpRequestObjectManagedObjectSource.class.getName(), null,
				new AutoWire(Parameters.class));
		parameters.addProperty(
				HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME,
				Parameters.class.getName());
		parameters
				.addProperty(
						HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
						String.valueOf(true));
		this.startHttpServer("PostRedirectGet.ofp", PostRedirectGetLogic.class,
				templatePropertyPairs);

		// Execute the HTTP request
		HttpResponse response = this.client.execute(request);

		// No redirect if get
		if (!(request instanceof HttpGet)) {

			// Ensure is a redirect
			assertEquals("Should be redirect", 303, response.getStatusLine()
					.getStatusCode());
			String redirectUrl = response.getFirstHeader("Location").getValue();
			assertEquals("Incorrect redirect URL", "/uri"
					+ HttpRouteTask.REDIRECT_URI_SUFFIX, redirectUrl);
			response.getEntity().consumeContent();

			// Undertake the GET (as triggered by redirect)
			HttpGet get = new HttpGet("http://" + HOST_NAME + ":"
					+ this.httpPort + redirectUrl);
			response = this.client.execute(get);
			assertEquals("Should be successful", 200, response.getStatusLine()
					.getStatusCode());
		}

		// Ensure correct rendering of template
		String rendering = MockHttpServer.getEntityBody(response);
		assertEquals("Incorrect rendering", expectedResponse, rendering);

		// Return the response
		return response;
	}

	/**
	 * Ensure can handle submit to a link that has {@link NextTask} annotation
	 * for handling.
	 */
	public void testSubmitWithNextTask() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		final String RESPONSE = "nextTask - finished(NextTask)";

		// Ensure correctly renders template on submit
		this.assertHttpRequest("/uri-nextTask", false, RESPONSE);
	}

	/**
	 * Ensure default behaviour of #{link} method without a {@link NextTask}
	 * annotation is to render the template.
	 */
	public void testSubmitWithoutNextTask() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class);

		// Ensure correctly renders template on submit not invoking flow
		String response = this.doHttpRequest("/uri-submit", false);
		this.assertRenderedResponse("<submit />", LinkQualify.NONE,
				LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NONE, null,
				response);
	}

	/**
	 * Ensure default behaviour of #{link} method without a {@link NextTask}
	 * annotation is to render the template.
	 */
	public void testSubmitWithoutNextTaskHavingUriSuffix() throws Exception {

		// Start the server
		this.isNonMethodLink = true;
		this.startHttpServer("Template.ofp", TemplateLogic.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI_SUFFIX, ".suffix");

		// Ensure correctly renders template on submit not invoking flow
		String response = this.doHttpRequest("/uri-submit.suffix", false);
		this.assertRenderedResponse("<submit />", LinkQualify.NONE,
				LinkQualify.NONE, LinkQualify.NONE, LinkQualify.NONE,
				".suffix", response);
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
		this.assertHttpRequest("/uri-notRenderTemplateAfter", false,
				"NOT_RENDER_TEMPLATE_AFTER");
	}

	/**
	 * Ensure with {@link NextTask} annotation that invoking a Flow takes
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
		assertXmlEquals(
				"Incorrect rendering",
				"<html><body><p>hello world</p><p>section data</p></body></html>",
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
		this.assertHttpRequest("/uri", false,
				" /uri-nonMethodLink /uri-doExternalFlow");

		// Ensure links out from template
		this.assertHttpRequest("/uri-nonMethodLink", false, "LINKED");
	}

	/**
	 * Ensure add context path to link.
	 */
	public void testContextPathForLink() throws Exception {

		// Start the server (with context path)
		this.isNonMethodLink = true;
		this.startHttpServer(
				"NoLogicTemplate.ofp",
				null,
				HttpApplicationLocationManagedObjectSource.PROPERTY_CONTEXT_PATH,
				"context");

		// Ensure template has context path for links
		this.assertHttpRequest("/context/uri", false,
				" /context/uri-nonMethodLink /context/uri-doExternalFlow");
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
		this.assertHttpRequest("/uri?getEnd=getTemplate", false,
				"TemplateOne1TwoTemplateOne1TwoEnd");
	}

	/**
	 * Ensure template stateful across {@link HttpRequest}.
	 */
	public void testStatefulTemplate() throws Exception {

		// Start the server
		this.startHttpServer("StatefulTemplate.ofp",
				StatefulTemplateLogic.class);

		// Ensure retains state across HTTP requests (by incrementing counter)
		this.assertHttpRequest("/uri", false, "<a href='/uri-increment'>1</a>");
		this.assertHttpRequest("/uri-increment", false,
				"increment - finished(2)");
		this.assertHttpRequest("/uri", false, "<a href='/uri-increment'>2</a>");
		this.assertHttpRequest("/uri-increment", false,
				"increment - finished(3)");
		this.assertHttpRequest("/uri", false, "<a href='/uri-increment'>3</a>");
	}

	/**
	 * Ensure on submit link that has next {@link Task} instances that if last
	 * {@link Task} in Flow does not indicate {@link NextTask} that the template
	 * is rendered.
	 */
	public void testRenderByDefault() throws Exception {

		// Start the server
		this.startHttpServer("SubmitTemplate.ofp",
				RenderByDefaultTemplateLogic.class);

		// Ensure render template again by default on link submit
		this.assertHttpRequest("/uri-submit", false,
				"Submit-RenderByDefault-/uri-submit");
	}

	/**
	 * Template logic for testing.
	 */
	public static class RenderByDefaultTemplateLogic {

		@NextTask("renderByDefault")
		public void submit(ServerHttpConnection connection) throws IOException {
			writeMessage(connection, "Submit");
		}

		// Next is to render the template
		public void renderByDefault(ServerHttpConnection connection)
				throws IOException {
			writeMessage(connection, "-RenderByDefault-");
		}

		// Required for test configuration
		@NextTask("doExternalFlow")
		public void required() {
		}
	}

	/**
	 * Ensure not render {@link HttpTemplate} afterwards.
	 */
	public void testNotRenderTemplateAfter() throws Exception {

		// Start the server
		this.startHttpServer("SubmitTemplate.ofp",
				NotRenderTemplateAfterLogic.class);

		// Ensure not render template after on link submit
		this.assertHttpRequest("/uri-submit", false,
				"Submit-NotRenderTemplateAfter");
	}

	/**
	 * Template logic for testing {@link NotRenderTemplateAfterLogic}.
	 */
	public static class NotRenderTemplateAfterLogic {

		@NextTask("notRenderTemplateAfter")
		public void submit(ServerHttpConnection connection) throws IOException {
			writeMessage(connection, "Submit");
		}

		@NotRenderTemplateAfter
		public void notRenderTemplateAfter(ServerHttpConnection connection)
				throws IOException {
			writeMessage(connection, "-NotRenderTemplateAfter");
		}

		// Required for test configuration
		@NextTask("doExternalFlow")
		public void required() {
		}
	}

	/**
	 * Ensure can inherit child template.
	 */
	public void testInheritChildTemplate() throws Exception {

		// Start the server
		String parentTemplateLocation = this
				.getTemplateLocation("ParentTemplate.ofp");
		this.startHttpServer("ChildTemplate.ofp", InheritChildLogic.class,
				HttpTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES,
				parentTemplateLocation);

		// Ensure can inherit sections
		this.assertHttpRequest("/uri", false,
				"Parent VALUE Introduced Two Footer /uri-doExternalFlow");
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
		String parentTemplateLocation = this
				.getTemplateLocation("ParentTemplate.ofp");
		String childTemplateLocation = this
				.getTemplateLocation("ChildTemplate.ofp");
		this.startHttpServer("GrandChildTemplate.ofp",
				InheritGrandChildLogic.class,
				HttpTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES,
				parentTemplateLocation + ", " + childTemplateLocation);

		// Ensure can inherit sections
		this.assertHttpRequest("/uri", false,
				"Grand Child TEXT Override Different order Footer /uri-doExternalFlow");
	}

	/**
	 * Logic for grand child inheritance test.
	 */
	public static class InheritGrandChildLogic extends InheritChildLogic {

		public InheritGrandChildLogic getOne(
				HttpSession differentSignatureToEnsureOverrideByName) {
			return this;
		}

		public String getText() {
			return "TEXT";
		}
	}

	/**
	 * Ensure able to use a {@link HttpTemplateSectionExtension}.
	 */
	public void testTemplateSectionExtension() throws Exception {

		// Flag to provide service method link
		this.isServiceMethodLink = true;

		// Start the server (with extension)
		this.startHttpServer("ExtensionTemplate.ofp",
				MockExtensionTemplateLogic.class, "extension.1",
				MockHttpTemplateSectionExtension.class.getName(),
				"extension.1.name", "value",
				"extension.1.mock.extension.index", "1", "extension.2",
				MockHttpTemplateSectionExtension.class.getName(),
				"extension.2.name", "value",
				"extension.2.mock.extension.index", "2", "section.name",
				"section.value");

		// Ensure change with extension
		this.assertHttpRequest("/uri", false,
				"Overridden template with overridden class and /uri-serviceLink");

		// Ensure service method not render template
		this.assertHttpRequest("/uri-serviceLink", false, "SERVICE_METHOD");
	}

	/**
	 * Mock {@link HttpTemplateSectionExtension} for testing.
	 */
	public static class MockHttpTemplateSectionExtension implements
			HttpTemplateSectionExtension {

		/*
		 * ================== HttpTemplateSectionExtension ====================
		 */

		@Override
		public void extendTemplate(HttpTemplateSectionExtensionContext context)
				throws Exception {

			final String TEMPLATE_CONTENT = "Overridden template with ${property} and #{serviceLink}";

			// Obtain the particular extension index
			int extensionIndex = Integer.parseInt(context
					.getProperty("mock.extension.index"));

			// Validate overriding details
			switch (extensionIndex) {
			case 1:
				// Ensure original template content
				assertEquals("Incorrect original template content",
						"extension", context.getTemplateContent());
				break;
			case 2:
				// Ensure overridden template content
				assertEquals("Template content should be overridden",
						TEMPLATE_CONTENT, context.getTemplateContent());
				break;
			default:
				fail("Should only be two extensions");
			}

			// Validate extension configuration
			String[] names = context.getPropertyNames();
			assertEquals("Incorrect number of properties", 2, names.length);
			assertEquals("Incorrect property name", "name", names[0]);
			assertEquals("Incorrect property value", "value",
					context.getProperty("name"));
			assertEquals("Incorrect property mock.extension.index",
					"mock.extension.index", names[1]);
			assertEquals("Not defaulting property", "default",
					context.getProperty("unknown", "default"));
			try {
				// Ensure failure on unknown property
				context.getProperty("unknown");
				fail("Should not successfully obtain unknown property");
			} catch (UnknownPropertyError ex) {
				String unknownPropertyName = "extension." + extensionIndex
						+ ".unknown";
				assertEquals("Incorrect unknown property", unknownPropertyName,
						ex.getUnknownPropertyName());
				assertEquals("Incurrect unknown property message",
						"Unknown property '" + unknownPropertyName + "'",
						ex.getMessage());
			}

			// Validate section details
			assertEquals("Incorrect section context", "section.value", context
					.getSectionSourceContext().getProperty("section.name"));
			assertNotNull("Assuming correct designer",
					context.getSectionDesigner());

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
		@NextTask("doExternalFlow")
		public void submit() {
		}

		/**
		 * Service method that should not render template on completion.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 */
		public void serviceMethod(ServerHttpConnection connection)
				throws IOException {
			Writer entity = connection.getHttpResponse().getEntityWriter();
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
	 * @return {@link HttpResponse}.
	 */
	private HttpResponse doRawHttpRequest(String uri, boolean isSecure)
			throws Exception {

		// Execute the HTTP request
		HttpGet request;
		if (isSecure) {
			request = new HttpGet("https://" + HOST_NAME + ":" + this.httpsPort
					+ uri);
		} else {
			request = new HttpGet("http://" + HOST_NAME + ":" + this.httpPort
					+ uri);
		}
		HttpResponse response = this.client.execute(request);

		// Return the response
		return response;
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
		HttpResponse response = this.doRawHttpRequest(uri, isSecure);

		// Ensure successful
		assertEquals("Ensure successful", 200, response.getStatusLine()
				.getStatusCode());

		// Obtain and return the response content
		String content = MockHttpServer.getEntityBody(response);
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
	private void assertHttpRequest(String uri, boolean isSecure,
			String expectedResponse) throws Exception {

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
			qualification = "http://" + HOST_NAME + ":" + this.httpPort;
			break;
		case SECURE:
			qualification = "https://" + HOST_NAME + ":" + this.httpsPort;
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
	 * @param nextTaskQualify
	 *            {@link LinkQualify} for <code>nextTask</code> link.
	 * @param submitQualify
	 *            {@link LinkQualify} for <code>submit</code> link.
	 * @param nonMethodLinkQualify
	 *            {@link LinkQualify} for <code>nonMethodLink</code>.
	 * @param linkUriSuffix
	 *            Link URI suffix. May be <code>null</code> for no suffix.
	 * @param actualResponse
	 *            Actual rendered response
	 */
	private void assertRenderedResponse(String expectedResponsePrefix,
			LinkQualify nextTaskQualify, LinkQualify submitQualify,
			LinkQualify nonMethodLinkQualify,
			LinkQualify notRenderTemplateAfter, String linkUriSuffix,
			String actualResponse) {

		// Transform expected response for link qualifications
		String expectedResponse = expectedResponsePrefix
				+ RENDERED_TEMPLATE_XML;
		expectedResponse = expectedResponse.replace(
				"${LINK_nextTask_QUALIFICATION}",
				this.getLinkQualification(nextTaskQualify));
		expectedResponse = expectedResponse.replace(
				"${LINK_submit_QUALIFICATION}",
				this.getLinkQualification(submitQualify));
		expectedResponse = expectedResponse.replace(
				"${LINK_nonMethodLink_QUALIFICATION}",
				this.getLinkQualification(nonMethodLinkQualify));
		expectedResponse = expectedResponse.replace(
				"${LINK_notRenderTemplateAfter_QUALIFICATION}",
				this.getLinkQualification(notRenderTemplateAfter));
		expectedResponse = expectedResponse.replace("${LINK_SUFFIX}",
				(linkUriSuffix == null ? "" : linkUriSuffix));

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
	protected void startHttpServer(String templateName, Class<?> logicClass,
			String... templatePropertyPairs) throws Exception {

		// Add the HTTP server socket listener
		HttpServerSocketManagedObjectSource.autoWire(this.source,
				this.httpPort, "ROUTE", "route");

		// Add the HTTPS server socket listener
		HttpsServerSocketManagedObjectSource.autoWire(this.source,
				this.httpsPort, MockHttpServer.getSslEngineSourceClass(),
				"ROUTE", "route");

		// Add dependencies
		this.source.addObject(this.connection, new AutoWire(Connection.class));
		this.source.addManagedObject(
				HttpRequestStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpRequestState.class));
		this.source.addManagedObject(
				HttpSessionManagedObjectSource.class.getName(), null,
				new AutoWire(HttpSession.class)).setTimeout(10 * 1000);
		AutoWireObject location = this.source.addManagedObject(
				HttpApplicationLocationManagedObjectSource.class.getName(),
				null, new AutoWire(HttpApplicationLocation.class));
		location.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTP_PORT,
				String.valueOf(this.httpPort));
		location.addProperty(
				HttpApplicationLocationManagedObjectSource.PROPERTY_HTTPS_PORT,
				String.valueOf(this.httpsPort));

		// Provide HTTP router for testing
		AutoWireSection templateRouteSection = this.source.addSection("ROUTE",
				WorkSectionSource.class.getName(),
				HttpRouteWorkSource.class.getName());

		// Provide unknown URL continuation for not handled requests
		AutoWireSection unknownUrlContinuationSection = this.source.addSection(
				"UNKONWN_URL_CONTINUATION", ClassSectionSource.class.getName(),
				UnknownUrlContinuationServicer.class.getName());
		this.source.link(templateRouteSection, "NOT_HANDLED",
				unknownUrlContinuationSection, "service");

		// Load the template section
		final String templateLocation = this.getTemplateLocation(templateName);
		AutoWireSection templateSection = this.source.addSection("SECTION",
				HttpTemplateSectionSource.class.getName(), templateLocation);
		if (logicClass != null) {
			templateSection.addProperty(
					HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
					logicClass.getName());
		}
		templateSection.addProperty(
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");

		// Load the additional properties
		for (int i = 0; i < templatePropertyPairs.length; i += 2) {
			String name = templatePropertyPairs[i];
			String value = templatePropertyPairs[i + 1];
			templateSection.addProperty(name, value);
			location.addProperty(name, value);
		}

		// Load mock section for handling outputs
		AutoWireSection handleOutputSection = this.source
				.addSection("OUTPUT", ClassSectionSource.class.getName(),
						MockSection.class.getName());

		// Link flow outputs
		this.source.link(templateSection, "output", handleOutputSection,
				"finished");
		this.source.link(templateSection, "doExternalFlow",
				handleOutputSection, "finished");

		// Link non-method link
		if (this.isNonMethodLink) {
			AutoWireSection handleOutputLink = this.source.addSection("LINK",
					ClassSectionSource.class.getName(),
					MockLink.class.getName());
			this.source.link(templateSection, "nonMethodLink",
					handleOutputLink, "linked");
		}

		// Link service method link
		if (this.isServiceMethodLink) {
			this.source.link(templateSection, "serviceLink", templateSection,
					"serviceMethod");
		}

		// Open the OfficeFloor
		this.officeFloor = this.source.openOfficeFloor();
	}

	/**
	 * Obtains the template location.
	 * 
	 * @param templateName
	 *            Name of the template.
	 * @return Template location.
	 */
	public String getTemplateLocation(String templateName) {
		return this.getClass().getPackage().getName().replace('.', '/') + "/"
				+ templateName;
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
	private static void writeMessage(ServerHttpConnection connection,
			String message) throws IOException {
		Writer writer = connection.getHttpResponse().getEntityWriter();
		writer.write(message);
		writer.flush();
	}

	/**
	 * Mock section for output tasks of the template.
	 */
	public static class MockSection {
		public void finished(@Parameter String parameter,
				ServerHttpConnection connection) throws IOException {
			if ((parameter != null) && (parameter.length() > 0)) {
				Writer writer = connection.getHttpResponse().getEntityWriter();
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
			Writer writer = connection.getHttpResponse().getEntityWriter();
			writer.write("LINKED");
			writer.flush();
		}
	}

	/**
	 * Services the unknown URL continuations.
	 */
	public static class UnknownUrlContinuationServicer {
		public void service(ServerHttpConnection connection) throws IOException {
			Writer writer = connection.getHttpResponse().getEntityWriter();
			writer.write("UNKNOWN_URL_CONTINUATION");
			writer.flush();
		}
	}

}