package net.officefloor.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Connector;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.servlet.tomcat.EmbeddedServletContainer;
import net.officefloor.web.compile.WebCompileOfficeFloor;

/**
 * Ensure can provide {@link OfficeFloor} {@link Connector}.
 * 
 * @author Daniel Sagenschneider
 */
public class TomcatServletTest extends OfficeFrameTestCase {

	/**
	 * Ensure can override the {@link Connector}.
	 */
	public void testTomcatViaOfficeFloor() throws Exception {

		Closure<MockHttpServer> server = new Closure<>();
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
		compiler.web((context) -> context.link(false, "/test", Servicer.class));
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Undertake request
			MockHttpResponse response = server.value.send(MockHttpServer.mockRequest("/test"));
			response.assertResponse(200, "Hello World");
		}
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws Exception {

			// Create the container
			EmbeddedServletContainer container = new EmbeddedServletContainer("/");
			container.addServlet("/test", "TEST", new MockHttpServlet());
			container.start();

			// Service
			container.service(connection);
		}
	}

	private static class MockHttpServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("Hello World");
		}
	}

}