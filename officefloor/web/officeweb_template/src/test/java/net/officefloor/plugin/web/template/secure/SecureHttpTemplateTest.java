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
package net.officefloor.plugin.web.template.secure;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.test.WebCompileOfficeFloor;
import net.officefloor.plugin.web.template.build.WebTemplate;
import net.officefloor.plugin.web.template.build.WebTemplater;
import net.officefloor.plugin.web.template.build.WebTemplaterEmployer;
import net.officefloor.plugin.web.template.parse.ParsedTemplate;
import net.officefloor.plugin.web.template.parse.ParsedTemplateSection;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpParameters;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.state.HttpRequestObjectManagedObjectSource;

/**
 * Ensures secure functionality of {@link ParsedTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureHttpTemplateTest extends OfficeFrameTestCase {

	/**
	 * Non-secure URL prefix.
	 */
	private static final String NON_SECURE_URL_PREFIX = "http://TODO provide redirect host name:7878";

	/**
	 * Secure URL prefix.
	 */
	private static final String SECURE_URL_PREFIX = "https://TODO provide redirect host name:7979";

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();

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
		// Configure the HTTP server
		this.compiler.officeFloor((context) -> {
			MockHttpServer
					.configureMockHttpServer(context.getDeployedOffice().getDeployedOfficeInput("ROUTE", "route"));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		// Stop the server
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure template triggers a redirect if not secure.
	 */
	public void testSecureTemplateRedirect() throws Exception {
		this.doSecureTemplateTest(true, null, NON_SECURE_URL_PREFIX + "/template", SECURE_URL_PREFIX + "/template",
				false);
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureTemplateService() throws Exception {
		this.doSecureTemplateTest(true, null, SECURE_URL_PREFIX + "/template", null, false);
	}

	/**
	 * Ensure template triggers a redirect if secure.
	 */
	public void testInsecureTemplateServiceSecureAnyway() throws Exception {
		this.doSecureTemplateTest(false, null, SECURE_URL_PREFIX + "/template", null, false);
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureTemplateService() throws Exception {
		this.doSecureTemplateTest(false, null, NON_SECURE_URL_PREFIX + "/template", null, false);
	}

	/**
	 * Ensure link triggers a redirect if not secure.
	 */
	public void testSecureLinkRedirect() throws Exception {
		this.doSecureTemplateTest(false, true, NON_SECURE_URL_PREFIX + "/template-LINK",
				SECURE_URL_PREFIX + "/template-LINK", false);
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureLinkService() throws Exception {
		this.doSecureTemplateTest(false, true, SECURE_URL_PREFIX + "/template-LINK", null, false);
	}

	/**
	 * Ensure services non-secure link even though on secure connection.
	 */
	public void testInsecureLinkServiceSecureAnyway() throws Exception {
		this.doSecureTemplateTest(true, false, SECURE_URL_PREFIX + "/template-LINK", null, false);
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureLinkWithSecureRendering() throws Exception {
		this.doSecureTemplateTest(true, false, NON_SECURE_URL_PREFIX + "/template-LINK",
				SECURE_URL_PREFIX + "/template", false);
	}

	/**
	 * Ensure link triggers a redirect if not secure.
	 */
	public void testBeanSecureLinkRedirect() throws Exception {
		this.doSecureTemplateTest(false, true, NON_SECURE_URL_PREFIX + "/template-LINK",
				SECURE_URL_PREFIX + "/template-LINK", true);
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testBeanSecureLinkService() throws Exception {
		this.doSecureTemplateTest(false, true, SECURE_URL_PREFIX + "/template-LINK", null, true);
	}

	/**
	 * Ensure services non-secure link even though on secure connection.
	 */
	public void testBeanInsecureLinkServiceSecureAnyway() throws Exception {
		this.doSecureTemplateTest(true, false, SECURE_URL_PREFIX + "/template-LINK", null, true);
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testBeanInsecureLinkWithSecureRendering() throws Exception {
		this.doSecureTemplateTest(true, false, NON_SECURE_URL_PREFIX + "/template-LINK",
				SECURE_URL_PREFIX + "/template", true);
	}

	/**
	 * Undertakes test for secure settings of a {@link ParsedTemplateSection}.
	 */
	private void doSecureTemplateTest(boolean isTemplateSecure, Boolean isLinkSecure, String requestUrl,
			String redirectUrl, boolean isEncapsulateLinkWithinBean) throws Exception {

		// Configure the application
		this.compiler.web((context) -> {
			// Obtain the template location
			String templateLocation = this.getFileLocation(this.getClass(),
					(isEncapsulateLinkWithinBean ? "SecureBeanLink.ofp" : "secure.ofp"));

			// Configure the template
			WebTemplater templater = WebTemplaterEmployer.employWebTemplater(context.getWebArchitect(),
					context.getOfficeArchitect());
			WebTemplate template = templater.addTemplate("template", templateLocation);
			template.setLogicClass(isEncapsulateLinkWithinBean ? BeanTemplateLogic.class : TemplateLogic.class);
			template.setSecure(isTemplateSecure);
			if (isLinkSecure != null) {
				template.setLinkSecure("LINK", isLinkSecure.booleanValue());
			}
		});

		// Start the server
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();

		// Test
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest(requestUrl + "?name=Daniel&id=1"));

		// Determine the expected entity of serviced request
		String linkUri = "/template-LINK";
		if ((!isTemplateSecure) && (isLinkSecure == null) && (requestUrl.startsWith(SECURE_URL_PREFIX))) {
			// Prefix non-configured non-secure template links
			linkUri = NON_SECURE_URL_PREFIX + linkUri;

		} else if (isTemplateSecure && ((isLinkSecure != null) && (!isLinkSecure))) {
			// Prefix non-secure links for secure template
			linkUri = NON_SECURE_URL_PREFIX + linkUri;
		}
		String expectedEntity = (isLinkSecure != null ? "link-" : "") + "SECURE - Daniel(1) - " + linkUri;

		// Determine if redirecting
		if (redirectUrl != null) {
			// Ensure redirect to appropriately secure URL
			assertEquals("Should be redirect", 303, response.getStatus().getStatusCode());
			assertEquals("Incorrect redirect URL", redirectUrl, response.getHeader("Location").getValue());

			// Undertake redirect to ensure parameters and entity are maintained
			response = this.server.send(MockHttpServer.mockRequest(redirectUrl));
		}

		// Ensure service request as appropriately secure
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure correct content
		assertEquals("Incorrect template response", expectedEntity, response.getEntity(null));
	}

	/**
	 * Logic for servicing the template.
	 */
	public static class TemplateLogic {

		public Parameters getTemplate(Parameters parameters) {
			return parameters;
		}

		public void LINK(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("link-");
		}
	}

	/**
	 * Logic for servicing the template with bean secure link.
	 */
	public static class BeanTemplateLogic {

		private Parameters parameters;

		public BeanTemplateLogic getTemplate(Parameters parameters) {
			this.parameters = parameters;
			return this;
		}

		public Parameters getBean() {
			return this.parameters;
		}

		public void LINK(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("link-");
		}
	}

	/**
	 * Ensure URI triggers a redirect if not secure.
	 */
	public void testSecureUriRedirect() throws Exception {
		this.doSecureUriTest(true, NON_SECURE_URL_PREFIX + "/uri", SECURE_URL_PREFIX + "/uri");
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureUriService() throws Exception {
		this.doSecureUriTest(true, SECURE_URL_PREFIX + "/uri", null);
	}

	/**
	 * Ensure URI triggers a redirect if secure.
	 */
	public void testInsecureUriRedirect() throws Exception {
		this.doSecureUriTest(false, SECURE_URL_PREFIX + "/uri", NON_SECURE_URL_PREFIX + "/uri");
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureUriService() throws Exception {
		this.doSecureUriTest(false, NON_SECURE_URL_PREFIX + "/uri", null);
	}

	/**
	 * Undertakes test for secure settings of a {@link HttpUrlContinuation}.
	 */
	private void doSecureUriTest(boolean isUriSecure, String requestUrl, String redirectUrl) throws Exception {

		// Configure the application
		this.compiler.web((context) -> {
			WebArchitect web = context.getWebArchitect();

			// Configure the section for URI
			OfficeSection section = context.addSection("TEST", UriLogic.class);
			HttpUrlContinuation uriLink = web.link(isUriSecure, "uri", section.getOfficeSectionInput("service"));

			// Add HTTP parameters (as not loaded by template)
			OfficeManagedObjectSource parametersMos = context.getOfficeArchitect()
					.addOfficeManagedObjectSource("PARAMETERS", HttpRequestObjectManagedObjectSource.class.getName());
			parametersMos.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_CLASS_NAME,
					Parameters.class.getName());
			parametersMos.addProperty(HttpRequestObjectManagedObjectSource.PROPERTY_IS_LOAD_HTTP_PARAMETERS,
					String.valueOf(true));
			parametersMos.addOfficeManagedObject("PARAMETERS", ManagedObjectScope.PROCESS);
		});

		// Start the server
		this.officeFloor = this.compiler.compileAndOpenOfficeFloor();

		// Test (with parameters and entity)
		MockHttpRequestBuilder post = MockHttpServer.mockRequest(requestUrl + "?name=Daniel");
		post.getHttpEntity().write("id=1".getBytes(Charset.forName("ISO-8859-1")));
		MockHttpResponse response = this.server.send(post);

		// Determine if redirecting
		if (redirectUrl != null) {
			// Ensure redirect to appropriately secure connection
			assertEquals("Should be redirect", 303, response.getStatus().getStatusCode());
			assertEquals("Incorrect redirect URL", redirectUrl, response.getHeader("Location").getValue());

			// Undertake redirect to ensure parameters and entity are maintained
			response = this.server.send(MockHttpServer.mockRequest(redirectUrl));
		}

		// Ensure service request as appropriately secure
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure correct content
		assertEquals("Incorrect template response", "SECURE - Daniel(1)", response.getEntity(null));
	}

	/**
	 * Logic for servicing the URI.
	 */
	public static class UriLogic {
		public void service(ServerHttpConnection connection, Parameters parameters) throws IOException {
			connection.getResponse().getEntityWriter()
					.write("SECURE - " + parameters.getName() + "(" + parameters.getId() + ")");
		}
	}

	/**
	 * Parameters that should continue to be available after redirect.
	 */
	@HttpParameters
	public static class Parameters implements Serializable {

		private String id;

		public String getId() {
			return this.id;
		}

		public void setId(String id) {
			this.id = id;
		}

		private String name;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

}