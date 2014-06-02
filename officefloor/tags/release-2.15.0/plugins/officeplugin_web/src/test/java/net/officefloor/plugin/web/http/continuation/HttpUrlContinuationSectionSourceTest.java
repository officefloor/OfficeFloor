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
package net.officefloor.plugin.web.http.continuation;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.work.WorkSectionSource;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.source.HttpsServerSocketManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.application.HttpRequestStateManagedObjectSource;
import net.officefloor.plugin.web.http.application.HttpUriLink;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.route.HttpRouteTask;
import net.officefloor.plugin.web.http.route.HttpRouteWorkSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;
import net.officefloor.plugin.work.clazz.FlowInterface;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Tests the {@link HttpUrlContinuationSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil
				.validateSpecification(
						HttpUrlContinuationSectionSource.class,
						HttpUrlContinuationSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME,
						"Section Source");
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner type = SectionLoaderUtil
				.createSectionDesigner(HttpUrlContinuationSectionSource.class);

		// Load the initial type from class
		type.addSectionInput("inputOne", null);
		type.addSectionInput("inputTwo", null);
		type.addSectionOutput("next", null, false);
		type.addSubSection("TRANSFORMED", ClassSectionSource.class.getName(),
				MockSectionTypeClass.class.getName());

		// Provide the transformations for URL continuations
		SectionWork pathWork = type.addSectionWork("path",
				HttpUrlContinuationWorkSource.class.getName());
		pathWork.addSectionTask("path", HttpUrlContinuationWorkSource.TASK_NAME);
		SectionWork uriWork = type.addSectionWork("uri",
				HttpUrlContinuationWorkSource.class.getName());
		uriWork.addSectionTask("uri", HttpUrlContinuationWorkSource.TASK_NAME);
		SectionWork rootWork = type.addSectionWork("_root_",
				HttpUrlContinuationWorkSource.class.getName());
		rootWork.addSectionTask("_root_",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Validate the type
		SectionLoaderUtil
				.validateSection(
						type,
						HttpUrlContinuationSectionSource.class,
						(String) null,
						HttpUrlContinuationSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME,
						ClassSectionSource.class.getName(),
						HttpUrlContinuationSectionSource.PROPERTY_SECTION_LOCATION,
						MockSectionTypeClass.class.getName(),
						HttpUrlContinuationSectionSource.PROPERTY_URL_LINK_PREFIX
								+ "path",
						"inputOne",
						HttpUrlContinuationSectionSource.PROPERTY_URL_LINK_PREFIX
								+ "/uri",
						"inputTwo",
						HttpUrlContinuationSectionSource.PROPERTY_URL_LINK_PREFIX
								+ "/", "inputOne");
	}

	/**
	 * Ensure can non-secure service.
	 */
	public void testNonSecureTransformation() throws Exception {
		this.doServiceTest(false);
	}

	/**
	 * Ensure can secure service.
	 */
	public void testSecureTransformation() throws Exception {
		this.doServiceTest(true);
	}

	/**
	 * Undertakes the service URL continuation test.
	 */
	public void doServiceTest(boolean isSecure) throws Exception {

		// Create and configure application
		AutoWireApplication source = new AutoWireOfficeFloorSource();

		// Add the servicer
		AutoWireSection section = source.addSection("SECTION",
				ClassSectionSource.class.getName(),
				MockSectionRunClass.class.getName());

		// Provide remaining configuration
		HttpServerSocketManagedObjectSource.autoWire(source, 7878, "ROUTE",
				HttpRouteWorkSource.TASK_NAME);
		HttpsServerSocketManagedObjectSource.autoWire(source, 7979,
				HttpTestUtil.getSslEngineSourceClass(), "ROUTE",
				HttpRouteWorkSource.TASK_NAME);
		AutoWireSection route = source.addSection("ROUTE",
				WorkSectionSource.class.getName(),
				HttpRouteWorkSource.class.getName());
		source.link(route, "NOT_HANDLED", section, "notHandled");
		source.addManagedObject(
				HttpApplicationLocationManagedObjectSource.class.getName(),
				null, new AutoWire(HttpApplicationLocation.class));
		source.addManagedObject(
				HttpRequestStateManagedObjectSource.class.getName(), null,
				new AutoWire(HttpRequestState.class));
		source.addManagedObject(HttpSessionManagedObjectSource.class.getName(),
				null, new AutoWire(HttpSession.class)).setTimeout(1000);

		// Add the transformer
		HttpUrlContinuationSectionSource transformer = new HttpUrlContinuationSectionSource();
		source.addSectionTransformer(transformer);

		// Map in the URI
		HttpUriLink link = transformer.linkUri("uri", section, "service");
		if (isSecure) {
			// Flag to be secure (default is non-secure)
			link.setUriSecure(isSecure);
		}
		assertEquals("Incorrect URI", "uri", link.getApplicationUriPath());
		assertSame("Incorrect Section", section, link.getAutoWireSection());
		assertEquals("Incorrect Section Input", "service",
				link.getAutoWireSectionInputName());

		// Ensure return listing of all registered URI paths
		HttpUriLink[] registeredUriLinks = transformer
				.getRegisteredHttpUriLinks();
		assertEquals("Incorrect number of registered URI links", 1,
				registeredUriLinks.length);
		assertSame("Incorrect URI link", link, registeredUriLinks[0]);

		// Create the client (without redirect)
		HttpClientBuilder builder = HttpClientBuilder.create();
		HttpTestUtil.configureHttps(builder);
		HttpTestUtil.configureNoRedirects(builder);
		CloseableHttpClient client = builder.build();

		// Obtain the host name
		String hostName = HttpApplicationLocationManagedObjectSource
				.getDefaultHostName();

		// Obtain the URLs
		final String SECURE_URL = "https://" + hostName + ":7979/uri";
		final String NON_SECURE_URL = "http://" + hostName + ":7878/uri";
		String urlInitial;
		String urlRedirect;
		if (isSecure) {
			urlInitial = NON_SECURE_URL;
			urlRedirect = SECURE_URL + HttpRouteTask.REDIRECT_URI_SUFFIX;
		} else {
			urlInitial = SECURE_URL;
			urlRedirect = NON_SECURE_URL + HttpRouteTask.REDIRECT_URI_SUFFIX;
		}

		// Start application
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Ensure redirect if not appropriately secure
			HttpResponse response = client.execute(new HttpGet(urlInitial));
			assertEquals("Should be redirect", 303, response.getStatusLine()
					.getStatusCode());
			assertEquals("Incorrect redirect location", urlRedirect, response
					.getFirstHeader("Location").getValue());
			response.getEntity().getContent().close();

			// Ensure servicing request
			response = client.execute(new HttpGet(urlRedirect));
			assertEquals("Should be successful", 200, response.getStatusLine()
					.getStatusCode());
			assertEquals("Incorrect response", "SERVICED",
					HttpTestUtil.getEntityBody(response));

		} finally {
			try {
				// Ensure stop client
				client.close();
			} finally {
				// Ensure stop application
				officeFloor.closeOfficeFloor();
			}
		}
	}

	/**
	 * Mock {@link Class} for {@link ClassSectionSource} to aid testing type.
	 */
	public static class MockSectionTypeClass {

		@FlowInterface
		public static interface Flows {
			void next();
		}

		public void inputOne(Flows flows) {
		}

		public void inputTwo() {
		}
	}

	/**
	 * Mock {@link Class} for {@link ClassSectionSource} to aiding testing
	 * servicing.
	 */
	public static class MockSectionRunClass {

		public void service(ServerHttpConnection connection) throws Exception {
			connection.getHttpResponse().getEntityWriter().write("SERVICED");
		}

		public void notHandled(ServerHttpConnection connection)
				throws Exception {
			connection.getHttpResponse().getEntityWriter().write("Not handled");
		}
	}

}