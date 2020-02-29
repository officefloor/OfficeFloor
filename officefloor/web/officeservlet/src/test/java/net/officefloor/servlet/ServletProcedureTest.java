package net.officefloor.servlet;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.function.Consumer;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link ServletProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletProcedureTest extends OfficeFrameTestCase {

	/**
	 * Ensure can undertake GET.
	 */
	public void testGet() {
		this.doServletTest("GET", "/", GetHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "SUCCESS"));
	}

	public static class GetHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("SUCCESS");
		}
	}

	/**
	 * Ensure can change status.
	 */
	public void testStatus() {
		this.doServletTest("GET", "/", StatusHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(412, ""));
	}

	public static class StatusHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setStatus(412);
		}
	}

	/**
	 * Ensure can send and receive headers.
	 */
	public void testHeader() {
		this.doServletTest("PUT", "/", HeaderHttpServlet.class, (server) -> {
			server.send(MockHttpServer.mockRequest("/").method(HttpMethod.PUT).header("header", "value"))
					.assertResponse(204, "", "header", "value");
		});
	}

	public static class HeaderHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			String value = req.getHeader("header");
			assertEquals("Should have header value", "value", value);
			resp.setHeader("header", value);
		}
	}

	/**
	 * Ensure can undertake POST with entity.
	 */
	public void testPost() {
		this.doServletTest("POST", "/", PostHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/").method(HttpMethod.POST).entity("ENTITY"))
						.assertResponse(200, "ENTITY"));
	}

	public static class PostHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			StringWriter entity = new StringWriter();
			Reader reader = req.getReader();
			for (int character = reader.read(); character != -1; character = reader.read()) {
				entity.write(character);
			}
			resp.getWriter().write(entity.toString());
		}
	}

	/**
	 * Ensure can return error.
	 */
	public void testException() {
		this.doServletTest("GET", "/", ExceptionHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(500, ""));
	}

	public static class ExceptionHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			throw new ServletException("TEST FAILURE");
		}
	}

	/**
	 * Undertakes test.
	 * 
	 * @param httpMethodName HTTP method name.
	 * @param path           Path.
	 * @param servletClass   {@link Servlet} {@link Class}.
	 * @param validator      Validator.
	 */
	private void doServletTest(String httpMethodName, String path, Class<? extends Servlet> servletClass,
			Consumer<MockWoofServer> validator) {
		CompileWoof compiler = new CompileWoof();
		compiler.woof((context) -> {
			OfficeSection servlet = context.getProcedureArchitect().addProcedure("Servlet", servletClass.getName(),
					ServletProcedureSource.SOURCE_NAME, servletClass.getSimpleName(), false, null);
			context.getOfficeArchitect().link(
					context.getWebArchitect().getHttpInput(false, httpMethodName, path).getInput(),
					servlet.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		try (MockWoofServer server = compiler.open()) {
			validator.accept(server);
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}