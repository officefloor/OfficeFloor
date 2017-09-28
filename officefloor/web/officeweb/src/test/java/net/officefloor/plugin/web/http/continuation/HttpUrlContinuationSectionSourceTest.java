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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.managedfunction.ManagedFunctionSectionSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource;
import net.officefloor.plugin.web.http.route.HttpRouteFunction;
import net.officefloor.plugin.web.http.route.HttpRouteManagedFunctionSource;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionManagedObjectSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.HttpUrlContinuation;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

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
		SectionLoaderUtil.validateSpecification(HttpUrlContinuationSectionSource.class,
				HttpUrlContinuationSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME, "Section Source");
	}

	/**
	 * Validate the type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner type = SectionLoaderUtil.createSectionDesigner();

		// Load the initial type from class
		type.addSectionInput("inputOne", null);
		type.addSectionInput("inputTwo", null);
		type.addSectionOutput("next", null, false);
		type.addSubSection("TRANSFORMED", ClassSectionSource.class.getName(), MockSectionTypeClass.class.getName());

		// Provide the transformations for URL continuations
		SectionFunctionNamespace pathNamespace = type.addSectionFunctionNamespace("path",
				HttpUrlContinuationManagedFunctionSource.class.getName());
		pathNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH, "/");
		pathNamespace.addSectionFunction("path", HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);
		SectionFunctionNamespace uriNamespace = type.addSectionFunctionNamespace("uri",
				HttpUrlContinuationManagedFunctionSource.class.getName());
		uriNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH, "/");
		uriNamespace.addSectionFunction("uri", HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);
		SectionFunctionNamespace rootNamespace = type.addSectionFunctionNamespace("_root_",
				HttpUrlContinuationManagedFunctionSource.class.getName());
		rootNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH, "/");
		rootNamespace.addSectionFunction("_root_", HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

		// Validate the type
		SectionLoaderUtil.validateSection(type, HttpUrlContinuationSectionSource.class, (String) null,
				HttpUrlContinuationSectionSource.PROPERTY_SECTION_SOURCE_CLASS_NAME, ClassSectionSource.class.getName(),
				HttpUrlContinuationSectionSource.PROPERTY_SECTION_LOCATION, MockSectionTypeClass.class.getName(),
				HttpUrlContinuationSectionSource.PROPERTY_URL_LINK_PREFIX + "path", "inputOne",
				HttpUrlContinuationSectionSource.PROPERTY_URL_LINK_PREFIX + "/uri", "inputTwo",
				HttpUrlContinuationSectionSource.PROPERTY_URL_LINK_PREFIX + "/", "inputOne");
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

		CompileOfficeFloor compiler = new CompileOfficeFloor();
		Closure<MockHttpServer> server = new Closure<>();
		compiler.officeFloor((context) -> {
			server.value = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput("ROUTE", HttpRouteManagedFunctionSource.FUNCTION_NAME));
		});
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Add the servicer
			OfficeSection section = context.addSection("SECTION", MockSectionRunClass.class);

			// Provide remaining configuration
			OfficeSection route = office.addOfficeSection("ROUTE", ManagedFunctionSectionSource.class.getName(),
					HttpRouteManagedFunctionSource.class.getName());
			office.link(route.getOfficeSectionOutput("NOT_HANDLED"), section.getOfficeSectionInput("notHandled"));
			office.addOfficeManagedObjectSource(HttpApplicationLocation.class.getSimpleName(),
					HttpApplicationLocationManagedObjectSource.class.getName())
					.addOfficeManagedObject(HttpApplicationLocation.class.getSimpleName(), ManagedObjectScope.PROCESS);
			office.addOfficeManagedObjectSource(HttpRequestState.class.getSimpleName(),
					HttpRequestStateManagedObjectSource.class.getName())
					.addOfficeManagedObject(HttpRequestState.class.getSimpleName(), ManagedObjectScope.PROCESS);
			OfficeManagedObjectSource sessionMos = office.addOfficeManagedObjectSource(
					HttpSession.class.getSimpleName(), HttpSessionManagedObjectSource.class.getName());
			sessionMos.setTimeout(1000);
			sessionMos.addOfficeManagedObject(HttpSession.class.getSimpleName(), ManagedObjectScope.PROCESS);

			// Add the transformer
			HttpUrlContinuationSectionSource transformer = new HttpUrlContinuationSectionSource();
			office.addOfficeSectionTransformer(transformer);

			// Map in the URI
			HttpUrlContinuation link = transformer.linkUri(HttpMethod.GET.getName(), "uri",
					section.getOfficeSectionInput("service"));
			if (isSecure) {
				// Flag to be secure (default is non-secure)
				link.setUriSecure(isSecure);
			}

			// Ensure return listing of all registered URI paths
			String[] registeredUris = transformer.getRegisteredHttpUris();
			assertEquals("Incorrect number of registered URI links", 1, registeredUris.length);
			assertSame("Incorrect URI link", "uri", registeredUris[0]);
		});

		// Start application
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();
		try {

			final String securePathRedirect = "/uri" + HttpRouteFunction.REDIRECT_URI_SUFFIX;
			final String secureUrlRedirect = "https://"
					+ HttpApplicationLocationManagedObjectSource.getDefaultHostName() + ":"
					+ HttpApplicationLocationManagedObjectSource.DEFAULT_HTTPS_PORT + securePathRedirect;

			// Ensure redirect if not appropriately secure
			MockHttpResponse response = server.value.send(MockHttpServer.mockRequest("/uri"));
			if (isSecure) {
				// Secure so will redirect to secure port
				assertEquals("Should be redirect", 303, response.getHttpStatus().getStatusCode());
				assertEquals("Incorrect redirect location", secureUrlRedirect,
						response.getFirstHeader("Location").getValue());

				// Ensure servicing request
				response = server.value.send(MockHttpServer.mockRequest(securePathRedirect).secure(true));
			}
			assertEquals("Should be successful", 200, response.getHttpStatus().getStatusCode());
			assertEquals("Incorrect response", "SERVICED", response.getHttpEntity(null));

		} finally {
			// Ensure stop application
			officeFloor.closeOfficeFloor();
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

		public void notHandled(ServerHttpConnection connection) throws Exception {
			connection.getHttpResponse().getEntityWriter().write("Not handled");
		}
	}

}