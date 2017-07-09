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
package net.officefloor.plugin.web.http.template.secure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpParameters;
import net.officefloor.plugin.web.http.application.HttpRequestObjectManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpTemplateSection;
import net.officefloor.plugin.web.http.application.HttpUriLink;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.route.HttpRouteTask;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Ensures secure functionality of {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class SecureHttpTemplateTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpServerAutoWireOfficeFloorSource}.
	 */
	private final HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource();

	/**
	 * Non-secure URL prefix.
	 */
	private final String NON_SECURE_URL_PREFIX = "http://"
			+ HttpApplicationLocationManagedObjectSource.getDefaultHostName()
			+ ":7878";

	/**
	 * Secure URL prefix.
	 */
	private final String SECURE_URL_PREFIX = "https://"
			+ HttpApplicationLocationManagedObjectSource.getDefaultHostName()
			+ ":7979";

	/**
	 * {@link AutoWireOfficeFloor}.
	 */
	private AutoWireOfficeFloor officeFloor;

	/**
	 * {@link CloseableHttpClient}.
	 */
	private CloseableHttpClient client;

	@Override
	protected void setUp() throws Exception {
		// Configure the client (to not redirect)
		HttpClientBuilder builder = HttpClientBuilder.create();
		HttpTestUtil.configureHttps(builder);
		HttpTestUtil.configureNoRedirects(builder);
		this.client = builder.build();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			if (this.client != null) {
				this.client.close();
			}

		} finally {
			// Stop the server
			if (this.officeFloor != null) {
				this.officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Ensure template triggers a redirect if not secure.
	 */
	public void testSecureTemplateRedirect() throws Exception {
		this.doSecureTemplateTest(true, null, NON_SECURE_URL_PREFIX
				+ "/template", SECURE_URL_PREFIX + "/template", false);
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureTemplateService() throws Exception {
		this.doSecureTemplateTest(true, null, SECURE_URL_PREFIX + "/template",
				null, false);
	}

	/**
	 * Ensure template triggers a redirect if secure.
	 */
	public void testInsecureTemplateServiceSecureAnyway() throws Exception {
		this.doSecureTemplateTest(false, null, SECURE_URL_PREFIX + "/template",
				null, false);
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureTemplateService() throws Exception {
		this.doSecureTemplateTest(false, null, NON_SECURE_URL_PREFIX
				+ "/template", null, false);
	}

	/**
	 * Ensure link triggers a redirect if not secure.
	 */
	public void testSecureLinkRedirect() throws Exception {
		this.doSecureTemplateTest(false, true, NON_SECURE_URL_PREFIX
				+ "/template-LINK", SECURE_URL_PREFIX + "/template-LINK", false);
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureLinkService() throws Exception {
		this.doSecureTemplateTest(false, true, SECURE_URL_PREFIX
				+ "/template-LINK", null, false);
	}

	/**
	 * Ensure services non-secure link even though on secure connection.
	 */
	public void testInsecureLinkServiceSecureAnyway() throws Exception {
		this.doSecureTemplateTest(true, false, SECURE_URL_PREFIX
				+ "/template-LINK", null, false);
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureLinkWithSecureRendering() throws Exception {
		this.doSecureTemplateTest(true, false, NON_SECURE_URL_PREFIX
				+ "/template-LINK", SECURE_URL_PREFIX + "/template", false);
	}

	/**
	 * Ensure link triggers a redirect if not secure.
	 */
	public void testBeanSecureLinkRedirect() throws Exception {
		this.doSecureTemplateTest(false, true, NON_SECURE_URL_PREFIX
				+ "/template-LINK", SECURE_URL_PREFIX + "/template-LINK", true);
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testBeanSecureLinkService() throws Exception {
		this.doSecureTemplateTest(false, true, SECURE_URL_PREFIX
				+ "/template-LINK", null, true);
	}

	/**
	 * Ensure services non-secure link even though on secure connection.
	 */
	public void testBeanInsecureLinkServiceSecureAnyway() throws Exception {
		this.doSecureTemplateTest(true, false, SECURE_URL_PREFIX
				+ "/template-LINK", null, true);
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testBeanInsecureLinkWithSecureRendering() throws Exception {
		this.doSecureTemplateTest(true, false, NON_SECURE_URL_PREFIX
				+ "/template-LINK", SECURE_URL_PREFIX + "/template", true);
	}

	/**
	 * Undertakes test for secure settings of a
	 * {@link HttpTemplateSection}.
	 */
	private void doSecureTemplateTest(boolean isTemplateSecure,
			Boolean isLinkSecure, String requestUrl, String redirectUrl,
			boolean isEncapsulateLinkWithinBean) throws Exception {

		// Obtain the template location
		String templateLocation = this.getFileLocation(this.getClass(),
				(isEncapsulateLinkWithinBean ? "SecureBeanLink.ofp"
						: "secure.ofp"));

		// Configure the template
		HttpTemplateSection template = this.source.addHttpTemplate(
				"template", templateLocation,
				(isEncapsulateLinkWithinBean ? BeanTemplateLogic.class
						: TemplateLogic.class));
		template.setTemplateSecure(isTemplateSecure);
		if (isLinkSecure != null) {
			template.setLinkSecure("LINK", isLinkSecure.booleanValue());
		}

		// Start the server
		this.officeFloor = this.source.openOfficeFloor();

		// Test
		HttpResponse response = this.client.execute(new HttpGet(requestUrl
				+ "?name=Daniel&id=1"));

		// Determine the expected entity of serviced request
		String linkUri = "/template-LINK";
		if ((!isTemplateSecure) && (isLinkSecure == null)
				&& (requestUrl.startsWith(SECURE_URL_PREFIX))) {
			// Prefix non-configured non-secure template links
			linkUri = NON_SECURE_URL_PREFIX + linkUri;

		} else if (isTemplateSecure
				&& ((isLinkSecure != null) && (!isLinkSecure))) {
			// Prefix non-secure links for secure template
			linkUri = NON_SECURE_URL_PREFIX + linkUri;
		}
		String expectedEntity = (isLinkSecure != null ? "link-" : "")
				+ "SECURE - Daniel(1) - " + linkUri;

		// Determine if redirecting
		if (redirectUrl != null) {
			// Ensure have prefix on redirect URL
			redirectUrl = redirectUrl + HttpRouteTask.REDIRECT_URI_SUFFIX;

			// Ensure redirect to appropriately secure URL
			assertEquals("Should be redirect", 303, response.getStatusLine()
					.getStatusCode());
			assertEquals("Incorrect redirect URL", redirectUrl, response
					.getFirstHeader("Location").getValue());

			// Complete request to do next request
			response.getEntity().getContent().close();

			// Undertake redirect to ensure parameters and entity are maintained
			response = this.client.execute(new HttpGet(redirectUrl));
		}

		// Ensure service request as appropriately secure
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Ensure correct content
		ByteArrayOutputStream actualEntity = new ByteArrayOutputStream();
		response.getEntity().writeTo(actualEntity);
		assertEquals("Incorrect template response", expectedEntity, new String(
				actualEntity.toByteArray()));
	}

	/**
	 * Logic for servicing the template.
	 */
	public static class TemplateLogic {

		public Parameters getTemplate(Parameters parameters) {
			return parameters;
		}

		public void LINK(ServerHttpConnection connection) throws IOException {
			connection.getHttpResponse().getEntityWriter().write("link-");
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
			connection.getHttpResponse().getEntityWriter().write("link-");
		}
	}

	/**
	 * Ensure URI triggers a redirect if not secure.
	 */
	public void testSecureUriRedirect() throws Exception {
		this.doSecureUriTest(true, NON_SECURE_URL_PREFIX + "/uri",
				SECURE_URL_PREFIX + "/uri" + HttpRouteTask.REDIRECT_URI_SUFFIX);
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
		this.doSecureUriTest(false, SECURE_URL_PREFIX + "/uri",
				NON_SECURE_URL_PREFIX + "/uri"
						+ HttpRouteTask.REDIRECT_URI_SUFFIX);
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureUriService() throws Exception {
		this.doSecureUriTest(false, NON_SECURE_URL_PREFIX + "/uri", null);
	}

	/**
	 * Undertakes test for secure settings of a {@link HttpUriLink}.
	 */
	private void doSecureUriTest(boolean isUriSecure, String requestUrl,
			String redirectUrl) throws Exception {

		// Configure the section for URI
		AutoWireSection section = this.source.addSection("TEST",
				ClassSectionSource.class.getName(), UriLogic.class.getName());
		HttpUriLink uriLink = this.source.linkUri("uri", section, "service");
		uriLink.setUriSecure(isUriSecure);

		// Add HTTP parameters (as not loaded by template)
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

		// Start the server
		this.officeFloor = this.source.openOfficeFloor();

		// Test (with parameters and entity)
		HttpPost post = new HttpPost(requestUrl + "?name=Daniel");
		post.setEntity(new StringEntity("id=1", "ISO-8859-1"));
		HttpResponse response = this.client.execute(post);

		// Determine if redirecting
		if (redirectUrl != null) {
			// Ensure redirect to appropriately secure connection
			assertEquals("Should be redirect", 303, response.getStatusLine()
					.getStatusCode());
			assertEquals("Incorrect redirect URL", redirectUrl, response
					.getFirstHeader("Location").getValue());
			response.getEntity().getContent().close();

			// Undertake redirect to ensure parameters and entity are maintained
			response = this.client.execute(new HttpGet(redirectUrl));
		}

		// Ensure service request as appropriately secure
		assertEquals("Should be successful", 200, response.getStatusLine()
				.getStatusCode());

		// Ensure correct content
		ByteArrayOutputStream actualEntity = new ByteArrayOutputStream();
		response.getEntity().writeTo(actualEntity);
		assertEquals("Incorrect template response", "SECURE - Daniel(1)",
				new String(actualEntity.toByteArray()));
	}

	/**
	 * Logic for servicing the URI.
	 */
	public static class UriLogic {
		public void service(ServerHttpConnection connection,
				Parameters parameters) throws IOException {
			connection
					.getHttpResponse()
					.getEntityWriter()
					.write("SECURE - " + parameters.getName() + "("
							+ parameters.getId() + ")");
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