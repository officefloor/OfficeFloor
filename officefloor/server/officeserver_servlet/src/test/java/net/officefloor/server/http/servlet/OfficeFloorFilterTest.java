/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
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

package net.officefloor.server.http.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.EnumSet;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.servlet.test.MockServerSettings;
import net.officefloor.woof.WoofLoaderSettings;

/**
 * Tests the {@link OfficeFloorFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorFilterTest extends OfficeFrameTestCase {

	/**
	 * Name of HTTP handling {@link OfficeSection}.
	 */
	public static final String HANDLER_SECTION_NAME = "section";

	/**
	 * Name of HTTP handling {@link OfficeSectionInput}.
	 */
	public static final String HANDLER_INPUT_NAME = "input";

	/**
	 * {@link Server}.
	 */
	private Server server;

	/**
	 * Port for the {@link Server}.
	 */
	private int port;

	@Override
	protected void setUp() throws Exception {
		WoofLoaderSettings.contextualLoad((loadContext) -> {
			loadContext.notLoad();

			// Start the server (using application extension)
			MockServerSettings.runWithinContext((officeFloorDeployer, context) -> {

				// Obtain the input to service the HTTP requests
				DeployedOffice office = officeFloorDeployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
				DeployedOfficeInput officeInput = office.getDeployedOfficeInput(HANDLER_SECTION_NAME,
						HANDLER_INPUT_NAME);

				// Load the HTTP server
				HttpServer server = new HttpServer(officeInput, officeFloorDeployer, context);

				// Ensure correct implementation
				assertEquals("Incorrect HTTP server implementation", HttpServletHttpServerImplementation.class,
						server.getHttpServerImplementation().getClass());

				// Load the team marker and team
				Singleton.load(officeFloorDeployer, new TeamMarker(), office);
				officeFloorDeployer.addTeam("TEAM", OnePersonTeamSource.class.getName()).addTypeQualification(null,
						TeamMarker.class.getName());

			}, (officeArchitect, context) -> {

				// Enable auto-wiring
				officeArchitect.enableAutoWireObjects();
				officeArchitect.enableAutoWireTeams();

				// Add section to service requests
				OfficeSection router = officeArchitect.addOfficeSection(HANDLER_SECTION_NAME,
						ClassSectionSource.class.getName(), Router.class.getName());

				// Create the service handlers
				OfficeSection officeFloorHandler = officeArchitect.addOfficeSection("officefloor",
						ClassSectionSource.class.getName(), OfficeFloorFilterTest.Servicer.class.getName());
				OfficeSection officeFloorTeamHandler = officeArchitect.addOfficeSection("officefloorTeam",
						ClassSectionSource.class.getName(), OfficeFloorFilterTest.TeamServicer.class.getName());
				OfficeSection delayedFallbackHandler = officeArchitect.addOfficeSection("delayedFallback",
						ClassSectionSource.class.getName(), DelayedFallbackServicer.class.getName());

				// Wire servicing
				officeArchitect.link(router.getOfficeSectionOutput("service"),
						officeFloorHandler.getOfficeSectionInput("service"));
				officeArchitect.link(router.getOfficeSectionOutput("serviceTeams"),
						officeFloorTeamHandler.getOfficeSectionInput("teams"));
				officeArchitect.link(router.getOfficeSectionOutput("delayedFallback"),
						delayedFallbackHandler.getOfficeSectionInput("service"));

			}, () -> {

				// Start the server
				this.server = new Server(0);
				ServletContextHandler handler = new ServletContextHandler();
				handler.setContextPath("/");
				handler.addFilter(OfficeFloorFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
				handler.addServlet(MockHttpServlet.class, "/*");
				this.server.setHandler(handler);
				this.server.start();

				// Obtain the port
				this.port = ((ServerConnector) (this.server.getConnectors()[0])).getLocalPort();
			});

			// No return
			return null;
		});
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.server != null) {
			this.server.stop();
		}
	}

	/**
	 * Ensure can pass request through to {@link HttpServlet}.
	 */
	public void testPassThroughToServlet() throws Exception {
		this.assertPassThroughServicing("/servlet");
	}

	/**
	 * Ensure if another {@link Team} flags not found, that
	 * {@link OfficeFloorFilter} waits for servicing to complete.
	 */
	public void testDelayedPassThroughToServlet() throws Exception {
		this.assertPassThroughServicing(Router.DELAYED_NOT_FOUND_PATH);
	}

	/**
	 * Asserts servicing by OfficeFloor passing through to servlet container.
	 * 
	 * @param path         Path to request.
	 * @param entitySuffix Suffix for entity.
	 */
	private void assertPassThroughServicing(String path) throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost("http://localhost:" + this.port + path);
			post.setEntity(new StringEntity("PassThrough"));
			org.apache.http.HttpResponse response = client.execute(post);
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect content", "SERVLET-PassThrough-END", entity);
		}
	}

	/**
	 * Ensure loads the {@link OfficeFloorFilter}.
	 */
	public void testServiceWithOfficeFloorFilter() throws Exception {
		this.assertOfficeFloorServicing(Router.SINGLE_TEAM_PATH);
	}

	/**
	 * Ensures co-ordinates the threading to use synchronously within servlet
	 * container (even if {@link Team} instances used).
	 */
	public void testServiceWithOfficeFloorTeams() throws Exception {
		TeamServicer.teamsThread = null; // reset for test
		this.assertOfficeFloorServicing(Router.MULTIPLE_TEAM_PATH);
		assertNotNull("Should be invoked for original team", TeamServicer.teamsThread);
	}

	/**
	 * Asserts servicing of OfficeFloor request appropriately.
	 * 
	 * @param path Path to request.
	 */
	private void assertOfficeFloorServicing(String path) throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost("http://localhost:" + this.port + path);
			post.addHeader("test", "header");
			post.setEntity(new StringEntity("Handled"));
			org.apache.http.HttpResponse response = client.execute(post);
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Incorrect status: " + entity, 201, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect content type", "plain/text", response.getFirstHeader("Content-Type").getValue());
			assertEquals("Incorrect content", "OfficeFloor-header-Handled", entity);
		}
	}

	public static class TeamMarker {
	}

	@FlowInterface
	public static interface Routes {
		void service();

		void serviceTeams();

		void delayedFallback();
	}

	/**
	 * {@link ClassSectionSource} to route requests.
	 */
	public static class Router {

		public static final String SINGLE_TEAM_PATH = "/single";

		public static final String MULTIPLE_TEAM_PATH = "/multiple";

		public static final String DELAYED_NOT_FOUND_PATH = "/delayed-fallback";

		public void input(ServerHttpConnection connection, Routes routes) {

			// Attempt to route request
			switch (connection.getRequest().getUri()) {
			case SINGLE_TEAM_PATH:
				routes.service();
				return;

			case MULTIPLE_TEAM_PATH:
				routes.serviceTeams();
				return;

			case DELAYED_NOT_FOUND_PATH:
				routes.delayedFallback();
				return;
			}

			// As here, not handled
			connection.getResponse().setStatus(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Delayed fallback to {@link HttpServlet}.
	 */
	public static class DelayedFallbackServicer {
		public void service(ServerHttpConnection connection, TeamMarker marker) {
			connection.getResponse().setStatus(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Servicer {@link ClassSectionSource}.
	 */
	public static class Servicer {
		public void service(ServerHttpConnection connection) throws IOException {

			// Service request
			HttpRequest request = connection.getRequest();
			HttpResponse response = connection.getResponse();

			// Obtain the header
			HttpHeader header = request.getHeaders().getHeader("test");

			// Read in the entity
			StringWriter buffer = new StringWriter();
			Reader entity = new InputStreamReader(request.getEntity());
			for (int character = entity.read(); character != -1; character = entity.read()) {
				buffer.write(character);
			}

			// Handle request
			response.setStatus(HttpStatus.CREATED);
			response.setContentType("plain/text", null);
			response.getEntityWriter().write("OfficeFloor-" + header.getValue() + "-" + buffer.toString());
		}
	}

	/**
	 * Service {@link ClassSectionSource} that incorporates {@link Team} to service
	 * the request.
	 */
	public static class TeamServicer extends Servicer {

		private static volatile Thread teamsThread = null;

		@Next("anotherTeam")
		public void teams() {
			teamsThread = Thread.currentThread();
		}

		@Next("service")
		public void anotherTeam(TeamMarker teamMarker) {

			// Ensure serviced in different thread
			assertNotNull("Should have original teams thread", teamsThread);
			assertNotSame("Should be invoked with different thread", teamsThread, Thread.currentThread());
		}
	}

	/**
	 * Mock {@link HttpServlet}.
	 */
	public static class MockHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			StringWriter buffer = new StringWriter();
			Reader entity = new InputStreamReader(req.getInputStream());
			for (int character = entity.read(); character != -1; character = entity.read()) {
				buffer.write(character);
			}
			resp.getWriter().write("SERVLET-" + buffer.toString() + "-END");
		}
	}

}
