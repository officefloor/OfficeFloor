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

import jakarta.inject.Inject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.servlet.procedure.FilterProcedureSource;
import net.officefloor.servlet.procedure.FilterProcedureSource.FlowKeys;
import net.officefloor.servlet.procedure.ServletProcedureSource;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link ServletProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterProcedureTest extends OfficeFrameTestCase {

	/**
	 * Additional setup for running.
	 */
	private CompileOfficeExtension officeExtraSetup = null;

	/**
	 * Ensure can list {@link Filter} as {@link Procedure}.
	 */
	public void testList() {
		ProcedureLoaderUtil.validateProcedures(HttpFilter.class.getName(),
				ProcedureLoaderUtil.procedure(HttpFilter.class.getSimpleName(), FilterProcedureSource.class));
	}

	/**
	 * Ensure not list as {@link Filter}.
	 */
	public void testNotProcedure() {
		ProcedureLoaderUtil.validateProcedures("NOT FILTER");
	}

	/**
	 * Ensure can response without chaining.
	 */
	public void testNotChain() {
		this.doFilterTest("GET", "/", NotChainHttpFilter.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(412, "FILTER"));
	}

	public static class NotChainHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			response.setStatus(412);
			response.getWriter().write("FILTER");
			// not chain
		}
	}

	/**
	 * Ensure can chain..
	 */
	public void testChain() {
		this.doFilterTest("GET", "/", ChainHttpFilter.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "FILTER CHAINED"));
	}

	public static class ChainHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			response.getWriter().write("FILTER ");
			chain.doFilter(request, response);
		}
	}

	/**
	 * Ensure can send and receive headers.
	 */
	public void testHeader() {
		this.doFilterTest("PUT", "/", HeaderHttpFilter.class, (server) -> {
			server.send(MockHttpServer.mockRequest("/").method(HttpMethod.PUT).header("header", "value"))
					.assertResponse(204, "", "header", "value");
		});
	}

	public static class HeaderHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			String value = request.getHeader("header");
			assertEquals("Should have header value", "value", value);
			response.setHeader("header", value);
		}
	}

	/**
	 * Ensure can undertake POST with entity.
	 */
	public void testPost() {
		this.doFilterTest("POST", "/", PostHttpFilter.class,
				(server) -> server.send(MockHttpServer.mockRequest("/").method(HttpMethod.POST).entity("ENTITY"))
						.assertResponse(200, "ENTITY"));
	}

	public static class PostHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			StringWriter entity = new StringWriter();
			Reader reader = request.getReader();
			for (int character = reader.read(); character != -1; character = reader.read()) {
				entity.write(character);
			}
			response.getWriter().write(entity.toString());
		}
	}

	/**
	 * Ensure can return error.
	 */
	public void testException() {
		this.doFilterTest("GET", "/", ExceptionHttpFilter.class, (server) -> server
				.send(MockHttpServer.mockRequest("/")).assertResponse(500, "{\"error\":\"TEST FAILURE\"}"));
	}

	public static class ExceptionHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			throw new ServletException("TEST FAILURE");
		}
	}

	/**
	 * Ensure can undertake {@link AsyncContext}.
	 */
	public void testAsync() {
		this.doFilterTest("GET", "/", AsyncHttpFilter.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "ASYNC"));
	}

	public static class AsyncHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			AsyncContext async = request.startAsync();
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
		this.doFilterTest("GET", "/", InjectHttpFilter.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "INJECT"));
	}

	public static class InjectedObject {
		public String getMessage() {
			return "INJECT";
		}
	}

	public static class InjectHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Dependency
		private InjectedObject dependency;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			response.getWriter().write(this.dependency.getMessage());
		}
	}

	/**
	 * Ensure can inject dependency via alternate {@link Annotation}.
	 */
	public void testInjectAlternateAnnotation() {
		this.officeExtraSetup = (context) -> Singleton.load(context.getOfficeArchitect(), new InjectedObject());
		this.doFilterTest("GET", "/", InjectAlternateAnnotationHttpFilter.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "INJECT"));
	}

	public static class InjectAlternateAnnotationHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Inject
		private InjectedObject dependency;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			response.getWriter().write(this.dependency.getMessage());
		}
	}

	/**
	 * Ensure dependencies are still available for {@link AsyncContext}.
	 */
	public void testInjectAsync() {
		this.officeExtraSetup = (context) -> Singleton.load(context.getOfficeArchitect(), new InjectedObject());
		this.doFilterTest("GET", "/", InjectAsyncHttpFilter.class,
				(server) -> server.send(MockHttpServer.mockRequest("/")).assertResponse(200, "INJECT"));
	}

	public static class InjectAsyncHttpFilter extends HttpFilter {
		private static final long serialVersionUID = 1L;

		@Dependency
		private InjectedObject dependency;

		@Override
		protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			AsyncContext async = request.startAsync();
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
	 * @param filterClass    {@link Filter} {@link Class}.
	 * @param validator      Validator.
	 */
	private void doFilterTest(String httpMethodName, String path, Class<? extends Filter> filterClass,
			Consumer<MockWoofServer> validator) {
		CompileWoof compiler = new CompileWoof(true);
		compiler.woof((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();

			// Configure the filter
			OfficeSection filter = context.getProcedureArchitect().addProcedure("Filter", filterClass.getName(),
					FilterProcedureSource.SOURCE_NAME, filterClass.getSimpleName(), false, null);
			office.link(context.getWebArchitect().getHttpInput(false, httpMethodName, path).getInput(),
					filter.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));

			// Provide chaining of filter
			OfficeSection chain = office.addOfficeSection("Chain", ClassSectionSource.class.getName(),
					ChainSection.class.getName());
			office.link(filter.getOfficeSectionOutput(FlowKeys.NEXT.name()), chain.getOfficeSectionInput("chain"));
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

	public static class ChainSection {
		public void chain(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("CHAINED");
		}
	}

}
