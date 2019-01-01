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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

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
		handler.addServlet(MockHttpServlet.class, "/servlet");
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
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost("http://localhost:" + this.port + "/servlet");
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
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpPost post = new HttpPost("http://localhost:" + this.port + "/officefloor");
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
		public void input(ServerHttpConnection connection) throws IOException {

			// Only handle /officefloor
			HttpRequest request = connection.getRequest();
			HttpResponse response = connection.getResponse();
			if ("/officefloor".equals(request.getUri())) {

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
				return;
			}

			// As here, not handled
			response.setStatus(HttpStatus.NOT_FOUND);
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