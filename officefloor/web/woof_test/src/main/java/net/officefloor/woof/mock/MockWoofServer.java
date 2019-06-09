/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.mock;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.woof.WoOF;
import net.officefloor.woof.WoofLoaderExtensionService;
import net.officefloor.woof.WoofLoaderExtensionService.WoofLoaderRunnableContext;

/**
 * <p>
 * {@link MockHttpServer} loading the WoOF application.
 * <p>
 * This provides convenient means to test WoOF applications.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServer extends MockHttpServer implements AutoCloseable {

	/**
	 * {@link ObjectMapper}.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Annotates service methods for the {@link ClassSectionSource}.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface MockWoofInput {

		/**
		 * Indicates if service from secure port.
		 */
		boolean secure() default false;

		/**
		 * {@link HttpMethod} for servicing.
		 */
		String method() default "GET";

		/**
		 * URL path to service.
		 */
		String path() default "/";
	}

	/**
	 * Enables configuring the {@link MockWoofServer}.
	 */
	@FunctionalInterface
	public static interface MockWoofServerConfigurer {

		/**
		 * Allows overriding the configuration of the {@link MockWoofServer}.
		 * 
		 * @param context  {@link WoofLoaderRunnableContext}.
		 * @param compiler {@link CompileOfficeFloor}.
		 * @throws Exception If fails to configure.
		 */
		void configure(WoofLoaderRunnableContext context, CompileOfficeFloor compiler) throws Exception;
	}

	/**
	 * Opens the {@link MockWoofServer}.
	 * 
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 * @return {@link MockWoofServer}.
	 * @throws Exception If fails to start the {@link MockWoofServer}.
	 */
	public static MockWoofServer open(MockWoofServerConfigurer... configurers) throws Exception {

		// Create the server
		MockWoofServer server = new MockWoofServer();

		// Return the open server
		return open(server, configurers);
	}

	/**
	 * <p>
	 * Opens the {@link WoOF} on a particular port with the
	 * {@link ClassSectionSource} to service {@link ServerHttpConnection}.
	 * <p>
	 * This is a convenience method to typically setup a mock server to respond to
	 * application requests.
	 * 
	 * @param httpPort     HTTP port.
	 * @param sectionClass {@link Class} for {@link ClassSectionSource}.
	 * @return {@link MockWoofServer}.
	 * @throws Exception If fails to start the {@link MockWoofServer}.
	 */
	public static OfficeFloor open(int httpPort, Class<?> sectionClass) throws Exception {
		return open(httpPort, -1, sectionClass);
	}

	/**
	 * <p>
	 * Opens the {@link WoOF} on a particular port with the
	 * {@link ClassSectionSource} to service {@link ServerHttpConnection}.
	 * <p>
	 * This is a convenience method to typically setup a mock server to respond to
	 * application requests.
	 * 
	 * @param httpPort     HTTP port.
	 * @param httpsPort    HTTPS port.
	 * @param sectionClass {@link Class} for {@link ClassSectionSource}.
	 * @return {@link MockWoofServer}.
	 * @throws Exception If fails to start the {@link MockWoofServer}.
	 */
	public static OfficeFloor open(int httpPort, int httpsPort, Class<?> sectionClass) throws Exception {
		return open(httpPort, httpsPort, (loadContext, compiler) -> loadContext.notLoadWoof(),
				createSectionServicer(sectionClass));
	}

	/**
	 * Creates a {@link MockWoofServerConfigurer} for servicing via
	 * {@link ClassSectionSource} {@link Class}.
	 * 
	 * @param sectionClass {@link ClassSectionSource} {@link Class}.
	 * @return {@link MockWoofServerConfigurer}.
	 */
	public static MockWoofServerConfigurer createSectionServicer(Class<?> sectionClass) {
		return (loadContext, compiler) -> {
			loadContext.extend((context) -> {
				OfficeArchitect office = context.getOfficeArchitect();
				WebArchitect web = context.getWebArchitect();

				// Load the section
				OfficeSection section = office.addOfficeSection("SECTION", new ClassSectionSource(),
						sectionClass.getName());

				// Link in the servicing
				for (Method method : sectionClass.getMethods()) {
					MockWoofInput config = method.getAnnotation(MockWoofInput.class);
					if (config != null) {
						HttpInput input = web.getHttpInput(config.secure(), config.method(), config.path());
						office.link(input.getInput(), section.getOfficeSectionInput(method.getName()));
					}
				}
			});
		};
	}

	/**
	 * Opens the {@link WoOF} on particular ports.
	 * 
	 * @param httpPort    HTTP port.
	 * @param httpsPort   HTTPS port.
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 * @return {@link MockWoofServer}.
	 * @throws Exception If fails to start the {@link MockWoofServer}.
	 */
	public static OfficeFloor open(int httpPort, int httpsPort, MockWoofServerConfigurer... configurers)
			throws Exception {

		// Undertake compiling
		return WoofLoaderExtensionService.contextualLoad((loadContext) -> {

			// Compile the OfficeFloor to run the server
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			OfficeFloorCompiler officeFloorCompiler = compiler.getOfficeFloorCompiler();
			officeFloorCompiler.addProperty(HttpServerLocation.PROPERTY_HTTP_PORT, String.valueOf(httpPort));
			officeFloorCompiler.addProperty(HttpServerLocation.PROPERTY_HTTPS_PORT, String.valueOf(httpsPort));
			compiler.office((context) -> {
				// Configured by WoOF extension
			});
			for (MockWoofServerConfigurer configurer : configurers) {
				configurer.configure(loadContext, compiler);
			}
			return compiler.compileAndOpenOfficeFloor();
		});
	}

	/**
	 * Opens the {@link MockWoofServer}.
	 * 
	 * @param server      {@link MockWoofServer}.
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 * @return Input {@link MockWoofServer}.
	 * @throws Exception If fails to open the {@link MockWoofServer}.
	 */
	protected static MockWoofServer open(MockWoofServer server, MockWoofServerConfigurer... configurers)
			throws Exception {

		// Undertake compiling (without HTTP Server loading)
		return WoofLoaderExtensionService.contextualLoad((loadContext) -> {

			// Mock the HTTP Server, so do not load
			loadContext.notLoadHttpServer();

			// Compile the OfficeFloor to run the server
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			compiler.officeFloor((context) -> {

				// Configure server to service requests
				DeployedOfficeInput input = context.getDeployedOffice()
						.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME);
				MockHttpServer.configureMockHttpServer(server, input);
			});
			compiler.office((context) -> {
				// Configured by WoOF extension
			});
			for (MockWoofServerConfigurer configurer : configurers) {
				configurer.configure(loadContext, compiler);
			}
			server.officeFloor = compiler.compileAndOpenOfficeFloor();

			// Return the server
			return server;
		});
	}

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Obtains the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	public OfficeFloor getOfficeFloor() {
		return this.officeFloor;
	}

	/*
	 * ================== MockHttpServer =======================
	 */

	@Override
	protected MockHttpResponse createMockHttpResponse(MockHttpRequest request, HttpVersion version, HttpStatus status,
			List<WritableHttpHeader> headers, List<WritableHttpCookie> cookies, InputStream entityInputStream) {
		return new MockWoofResponseImpl(request, version, status, headers, cookies, entityInputStream);
	}

	@Override
	protected MockHttpResponse createMockHttpResponse(MockHttpRequest request, Throwable failure) {
		return new MockWoofResponseImpl(request, failure);
	}

	@Override
	public MockWoofResponse send(MockHttpRequestBuilder request) {
		return (MockWoofResponse) super.send(request);
	}

	/*
	 * =================== AutoCloseable =======================
	 */

	@Override
	public void close() throws Exception {
		this.officeFloor.closeOfficeFloor();
	}

	/**
	 * {@link MockWoofResponse} implementation.
	 */
	protected class MockWoofResponseImpl extends MockHttpResponseImpl implements MockWoofResponse {

		protected MockWoofResponseImpl(MockHttpRequest request, HttpVersion version, HttpStatus status,
				List<WritableHttpHeader> headers, List<WritableHttpCookie> cookies, InputStream entityInputStream) {
			super(request, version, status, headers, cookies, entityInputStream);
		}

		public MockWoofResponseImpl(MockHttpRequest request, Throwable failure) {
			super(request, failure);
		}

		/*
		 * ======================== MockWoofResponse =======================
		 */

		@Override
		public void assertJson(int statusCode, Object entity, String... headerNameValuePairs) {
			this.assertJson(statusCode, entity, mapper, headerNameValuePairs);
		}

		@Override
		public void assertJson(int statusCode, Object entity, ObjectMapper mapper, String... headerNameValuePairs) {

			// Create the expected entity
			String expectedEntity;
			try {
				expectedEntity = mapper.writeValueAsString(entity);
			} catch (IOException ex) {
				throw new AssertionError(ex.getMessage(), ex);
			}

			// Assert the JSON response
			this.assertResponse(statusCode, expectedEntity, headerNameValuePairs);
		}

		@Override
		public void assertJsonError(Throwable failure, String... headerNameValuePairs) {

			// Obtain the failure status code
			int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode();
			if (failure instanceof HttpException) {
				HttpException httpException = (HttpException) failure;
				statusCode = httpException.getHttpStatus().getStatusCode();
			}

			// Assert the JSON error
			this.assertJsonError(statusCode, failure, headerNameValuePairs);
		}

		@Override
		public void assertJsonError(int httpStatus, Throwable failure, String... headerNameValuePairs) {

			// Create the expected entity
			String expectedEntity;
			try {
				expectedEntity = JacksonHttpObjectResponderFactory.getEntity(failure, mapper);
			} catch (IOException ex) {
				throw new AssertionError(ex.getMessage(), ex);
			}

			// Assert the response
			this.assertResponse(httpStatus, expectedEntity, headerNameValuePairs);
		}
	}

}