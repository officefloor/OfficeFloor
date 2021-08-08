/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.servlet.procedure.ServletProcedureSource;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link ServletProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletProcedureTest extends OfficeFrameTestCase {

	/**
	 * Additional setup for running.
	 */
	private CompileOfficeExtension officeExtraSetup = null;

	/**
	 * Ensure can list {@link Servlet} as {@link Procedure}.
	 */
	public void testList() {
		ProcedureLoaderUtil.validateProcedures(HttpServlet.class.getName(),
				ProcedureLoaderUtil.procedure(HttpServlet.class.getSimpleName(), ServletProcedureSource.class));
	}

	/**
	 * Ensure not list as {@link Servlet}.
	 */
	public void testNotProcedure() {
		ProcedureLoaderUtil.validateProcedures("NOT SERVLET");
	}

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
		this.doServletTest("GET", "/", ExceptionHttpServlet.class, (server) -> server
				.send(MockHttpServer.mockRequest("/")).assertResponse(500, "{\"error\":\"TEST FAILURE\"}"));
	}

	public static class ExceptionHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			throw new ServletException("TEST FAILURE");
		}
	}

	/**
	 * Ensure can undertake {@link AsyncContext}.
	 */
	public void testAsync() {
		this.doServletTest("GET", "/", AsyncHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "ASYNC"));
	}

	public static class AsyncHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			AsyncContext async = req.startAsync();
			new Thread(() -> {
				HttpServletResponse httpResponse = (HttpServletResponse) async.getResponse();
				try {
					httpResponse.getWriter().write("ASYNC");
				} catch (IOException ex) {
					httpResponse.setStatus(500);
				}
				async.complete();
			}).start();
		}
	}

	/**
	 * Ensure can inject dependency.
	 */
	public void testInject() {
		this.officeExtraSetup = (context) -> Singleton.load(context.getOfficeArchitect(), new InjectedObject());
		this.doServletTest("GET", "/", InjectHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "INJECT"));
	}

	public static class InjectedObject {
		public String getMessage() {
			return "INJECT";
		}
	}

	public static class InjectHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Dependency
		private InjectedObject dependency;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write(this.dependency.getMessage());
		}
	}

	/**
	 * Ensure can inject dependency via alternate {@link Annotation}.
	 */
	public void testInjectAlternateAnnotation() {
		this.officeExtraSetup = (context) -> Singleton.load(context.getOfficeArchitect(), new InjectedObject());
		this.doServletTest("GET", "/", InjectAlternateAnnotationHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "INJECT"));
	}

	public static class InjectAlternateAnnotationHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Inject
		private InjectedObject dependency;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write(this.dependency.getMessage());
		}
	}

	/**
	 * Ensure dependencies are still available for {@link AsyncContext}.
	 */
	public void testInjectAsync() {
		this.officeExtraSetup = (context) -> Singleton.load(context.getOfficeArchitect(), new InjectedObject());
		this.doServletTest("GET", "/", InjectAsyncHttpServlet.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "INJECT"));
	}

	public static class InjectAsyncHttpServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Dependency
		private InjectedObject dependency;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			AsyncContext async = req.startAsync();
			async.start(() -> {
				HttpServletResponse httpResponse = (HttpServletResponse) async.getResponse();
				try {
					httpResponse.getWriter().write(this.dependency.getMessage());
				} catch (IOException ex) {
					httpResponse.setStatus(500);
				}
				async.complete();
			});
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
		CompileWoof compiler = new CompileWoof(true);
		compiler.woof((context) -> {
			OfficeSection servlet = context.getProcedureArchitect().addProcedure("Servlet", servletClass.getName(),
					ServletProcedureSource.SOURCE_NAME, servletClass.getSimpleName(), false, null);
			context.getOfficeArchitect().link(
					context.getWebArchitect().getHttpInput(false, httpMethodName, path).getInput(),
					servlet.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		if (this.officeExtraSetup != null) {
			compiler.office(this.officeExtraSetup);
		}
		try (MockWoofServer server = compiler.open()) {
			validator.accept(server);
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}
