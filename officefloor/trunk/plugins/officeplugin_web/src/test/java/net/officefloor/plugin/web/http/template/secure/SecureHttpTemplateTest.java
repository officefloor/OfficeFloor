/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.web.http.application.HttpParameters;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpUriLink;
import net.officefloor.plugin.web.http.server.HttpServerAutoWireOfficeFloorSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Ignore;

/**
 * Ensures secure functionality of {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO fix code to have working")
public class SecureHttpTemplateTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpServerAutoWireOfficeFloorSource}.
	 */
	private final HttpServerAutoWireOfficeFloorSource source = new HttpServerAutoWireOfficeFloorSource();

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
		// Configure the client (to not redirect)
		HttpParams params = new BasicHttpParams();
		params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
		this.client = new DefaultHttpClient(params);

		// Configure the client for anonymous HTTPS
		MockHttpServer.configureAnonymousHttps(this.client, 7979);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			// Stop the client
			if (this.client != null) {
				this.client.getConnectionManager().shutdown();
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
		this.doSecureTemplateTest(true, null, "http://localhost:7878/template",
				"https://localhost:7979/template");
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureTemplateService() throws Exception {
		this.doSecureTemplateTest(true, null,
				"https://localhost:7979/template", null);
	}

	/**
	 * Ensure template triggers a redirect if secure.
	 */
	public void testInsecureTemplateRedirect() throws Exception {
		this.doSecureTemplateTest(false, null,
				"https://localhost:7979/template",
				"http://localhost:7878/template");
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureTemplateService() throws Exception {
		this.doSecureTemplateTest(false, null,
				"http://localhost:7878/template", null);
	}

	/**
	 * Ensure link triggers a redirect if not secure.
	 */
	public void testSecureLinkRedirect() throws Exception {
		this.doSecureTemplateTest(false, true,
				"http://localhost:7878/template.links-LINK.task",
				"https://localhost:7979/template.links-LINK.task");
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureLinkService() throws Exception {
		this.doSecureTemplateTest(false, true,
				"https://localhost:7979/template.links-LINK.task", null);
	}

	/**
	 * Ensure link triggers a redirect if secure.
	 */
	public void testInsecureLinkRedirect() throws Exception {
		this.doSecureTemplateTest(true, false,
				"https://localhost:7979/template.links-LINK.task",
				"http://localhost:7878/template.links-LINK.task");
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureLinkService() throws Exception {
		this.doSecureTemplateTest(true, false,
				"http://localhost:7878/template.links-LINK.task", null);
	}

	/**
	 * Undertakes test for secure settings of a
	 * {@link HttpTemplateAutoWireSection}.
	 */
	private void doSecureTemplateTest(boolean isTemplateSecure,
			Boolean isLinkSecure, String requestUrl, String redirectUrl)
			throws Exception {

		// Obtain the template location
		String templateLocation = this.getFileLocation(this.getClass(),
				"secure.ofp");

		// Configure the template
		HttpTemplateAutoWireSection template = this.source.addHttpTemplate(
				templateLocation, TemplateLogic.class, "template");
		template.setTemplateSecure(isTemplateSecure);
		if (isLinkSecure != null) {
			template.setLinkSecure("LINK", isLinkSecure.booleanValue());
		}

		// Start the server
		this.officeFloor = this.source.openOfficeFloor();

		// Test (with parameters and entity)
		HttpPost post = new HttpPost(requestUrl + "?name=Daniel");
		post.setEntity(new StringEntity("id=1", "ISO-8859-1"));
		HttpResponse response = this.client.execute(post);

		// Determine the expected entity of serviced request
		String linkUri = "/template.links-LINK.task";
		if ((isLinkSecure != null)
				&& (isLinkSecure.booleanValue() != isTemplateSecure)) {
			// Fully qualified URL as differently secure
			linkUri = redirectUrl;
		}
		String expectedEntity = (isLinkSecure != null ? "link-" : "")
				+ "SECURE - Daniel(1) - " + linkUri;

		// Determine if redirecting
		if (redirectUrl != null) {
			// Ensure redirect to appropriately secure URL
			assertEquals("Should be redirect", 303, response.getStatusLine()
					.getStatusCode());
			assertEquals("Incorrect redirect URL", redirectUrl, response
					.getFirstHeader("Location").getValue());

			// Complete request to do next request
			response.getEntity().consumeContent();

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
	 * Ensure URI triggers a redirect if not secure.
	 */
	public void testSecureUriRedirect() throws Exception {
		this.doSecureUriTest(true, "http://localhost:7878/uri",
				"https://localhost:7979/uri");
	}

	/**
	 * Ensure service request if appropriately secure.
	 */
	public void testSecureUriService() throws Exception {
		this.doSecureUriTest(true, "https://localhost:7979/uri", null);
	}

	/**
	 * Ensure URI triggers a redirect if secure.
	 */
	public void testInsecureUriRedirect() throws Exception {
		this.doSecureUriTest(false, "https://localhost:7979/uri",
				"http://localhost:7878/uri");
	}

	/**
	 * Ensure service request if appropriately insecure.
	 */
	public void testInsecureUriService() throws Exception {
		this.doSecureUriTest(false, "http://localhost:7878/uri", null);
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

		// Start the server
		this.officeFloor = this.source.openOfficeFloor();

		// Test (with parameters and entity)
		HttpPost post = new HttpPost(requestUrl + "?name=Daniel");
		post.setEntity(new StringEntity("id=1", "ISO-8859-1"));
		HttpResponse response = this.client.execute(post);

		// Determine if redirecting
		if (redirectUrl != null) {
			// Ensure redirect to appropriately secure connection
			assertEquals("Should be redirect", 301, response.getStatusLine()
					.getStatusCode());
			assertEquals("Incorrect redirect URL", redirectUrl,
					response.getHeaders("Location"));

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
	public static class Parameters {

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