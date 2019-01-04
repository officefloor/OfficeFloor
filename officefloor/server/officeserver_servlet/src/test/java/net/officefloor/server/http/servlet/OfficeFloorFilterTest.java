package net.officefloor.server.http.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.servlet.MockServerOfficeFloorExtensionService.Router;
import net.officefloor.server.http.servlet.MockServerOfficeFloorExtensionService.TeamMarker;

/**
 * Tests the {@link OfficeFloorFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorFilterTest extends OfficeFrameTestCase {

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
	 * @param path Path to request.
	 */
	private void assertPassThroughServicing(String path) throws Exception {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost("http://localhost:" + this.port + path);
			post.setEntity(new StringEntity("PassThrough"));
			org.apache.http.HttpResponse response = client.execute(post);
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals("Incorrect status: " + entity, 200, response.getStatusLine().getStatusCode());
			assertEquals("Incorrect content", "SERVLET-PassThrough", entity);
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

		@NextFunction("anotherTeam")
		public void teams() {
			teamsThread = Thread.currentThread();
		}

		@NextFunction("service")
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

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			StringWriter buffer = new StringWriter();
			Reader entity = req.getReader();
			for (int character = entity.read(); character != -1; character = entity.read()) {
				buffer.write(character);
			}
			resp.getWriter().write("SERVLET-" + buffer.toString());
		}
	}

}