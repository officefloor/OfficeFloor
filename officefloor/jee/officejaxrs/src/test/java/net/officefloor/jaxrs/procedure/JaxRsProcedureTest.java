package net.officefloor.jaxrs.procedure;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.tutorial.jaxrsapp.JaxRsDependency;
import net.officefloor.tutorial.jaxrsapp.JaxRsResource;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link JaxRsProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsProcedureTest extends OfficeFrameTestCase {

	/**
	 * Ensure can GET.
	 */
	public void testGet() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "get", "GET");
	}

	/**
	 * Ensure path parameter.
	 */
	public void testPathParam() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/officefloor/prefix-{param}-suffix", JaxRsResource.class, "pathParam",
				(server) -> {
					MockHttpResponse response = server
							.send(MockWoofServer.mockRequest("/officefloor/prefix-parameter-suffix"));
					response.assertResponse(200, "parameter");
				});
	}

	/**
	 * Ensure query parameter.
	 */
	public void testQueryParameter() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/", JaxRsResource.class, "queryParam", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/?param=parameter"));
			response.assertResponse(200, "parameter");
		});
	}

	/**
	 * Ensure default query parameter.
	 */
	public void testDefaultQueryParameter() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "queryParam", "default");
	}

	/**
	 * Ensure header.
	 */
	public void testHeader() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "headerParam", "header", "param", "header");
	}

	/**
	 * Ensure cookie.
	 */
	public void testCookie() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "cookieParam", "cookie", "cookie", "param=cookie");
	}

	/**
	 * Ensure form parameter.
	 */
	public void testFormParameter() throws Exception {
		this.doJaxRsTest(HttpMethod.POST, "/", JaxRsResource.class, "formParam", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/").method(HttpMethod.POST)
					.header("Content-Type", "application/x-www-form-urlencoded").entity("param=form"));
			response.assertResponse(200, "form", "Content-Type", "text/plain");
		});
	}

	/**
	 * Ensure {@link UriInfo}.
	 */
	public void testUriInfo() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/officefloor/{param}", JaxRsResource.class, "uriInfo", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/officefloor/path?param=query"));
			response.assertResponse(200, "PATH=uriInfo/path, PATH PARAM=path, QUERY PARAM=query");
		});
	}

	/**
	 * Ensure {@link HttpHeaders}.
	 */
	public void testHttpHeaders() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "httpHeaders", "HEADER=header, COOKIE=cookie", "param", "header",
				"cookie", "param=cookie");
	}

	/**
	 * Ensure JSON.
	 */
	public void testJson() throws Exception {
		this.doJaxRsTest(HttpMethod.POST, "/", JaxRsResource.class, "json", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/").method(HttpMethod.POST)
					.header("Content-Type", "application/json").entity("{\"input\":\"JSON\"}"));
			response.assertResponse(200, "{\"message\":\"JSON\"}", "Content-Type", "application/json");
		});
	}

	/**
	 * Ensure inject.
	 */
	public void testInject() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "inject", "Inject Dependency");
	}

	/**
	 * Ensure context.
	 */
	public void testContext() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "context", "Context Dependency");
	}

	/**
	 * Ensure async resumed synchronously.
	 */
	public void testAsyncSynchronous() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "asyncSynchronous", "Sync Dependency");
	}

	/**
	 * Ensure async resumed asynchronously.
	 */
	public void testAsyncAsynchronous() throws Exception {
		this.doJaxRsTest(JaxRsResource.class, "asyncAsynchronous", "Async Dependency");
	}

	/**
	 * Ensure able invoke JAX-RS {@link Method} configured to one HTTP method but
	 * configured as {@link JaxRsProcedureSource} via another HTTP method.
	 */
	public void testDifferentHttpMethod() throws Exception {
		this.doJaxRsTest(HttpMethod.PUT, "/", DifferentHttpMethodResource.class, "post", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/").method(HttpMethod.PUT));
			response.assertResponse(200, "Method PUT");
		});
	}

	@Path("/")
	public static class DifferentHttpMethodResource {

		@POST
		public String post(@Context HttpServletRequest request) {
			return "Method " + request.getMethod();
		}
	}

	/**
	 * Ensure able invoke JAX-RS {@link Method} configured to one path but
	 * configured as {@link JaxRsProcedureSource} via path.
	 */
	public void testDifferentPath() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/different/path", DifferentPathResource.class, "path", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/different/path"));
			response.assertResponse(200, "Path configured/path");
		});
	}

	@Path("/configured/path")
	public static class DifferentPathResource {

		@GET
		public String path(@Context UriInfo info) {
			return "Path " + info.getPath();
		}
	}

	/**
	 * Ensure async dependency.
	 */
	public void testAsyncDependency() throws Exception {
		this.doJaxRsTest(AsyncDependencyResource.class, "async", "Async Dependency");
	}

	@Path("/")
	public static class AsyncDependencyResource {

		private @Inject ManagedExecutorService executor;

		private @Dependency AsyncDependency dependency;

		@GET
		public void async(@Suspended AsyncResponse async) {
			assertTrue("Should be suspended", async.isSuspended());
			this.executor.execute(() -> async.resume("Async " + this.dependency.getMessage()));
		}
	}

	public static class AsyncDependency {

		public String getMessage() {
			return "Dependency";
		}
	}

	/**
	 * Ensure handle exception in asynchronous execution.
	 */
	public void testAsyncException() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/", AsyncExceptionResource.class, "async", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/"));
			String entity = response.getEntity(null);
			response.assertStatus(500);
			assertTrue("Should find failure in response: " + entity,
					entity.contains(IOException.class.getName() + ": TEST"));
		});
	}

	@Path("/")
	public static class AsyncExceptionResource {

		private @Inject ManagedExecutorService executor;

		@GET
		public void async(@Suspended AsyncResponse async) {
			assertTrue("Should be suspended", async.isSuspended());
			this.executor.execute(() -> {
				async.resume(new IOException("TEST"));
			});
		}
	}

	/**
	 * Ensure handle checked {@link Exception}.
	 */
	public void testCheckedExeption() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/", JaxRsResource.class, "checkedException",
				assertThrowable(IOException.class, "TEST"));
	}

	/**
	 * Ensure handle unchecked {@link Exception}.
	 */
	public void testUncheckedExeption() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/", JaxRsResource.class, "uncheckedException",
				assertThrowable(RuntimeException.class, "TEST"));
	}

	/**
	 * Ensure handle resume with {@link Exception}.
	 */
	public void testAsyncExeption() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/", JaxRsResource.class, "asyncException",
				assertThrowable(Exception.class, "TEST"));
	}

	/**
	 * Ensure handle exception thrown in asynchronous execution.
	 */
	public void testAsyncThrow() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/", AsyncThrowResource.class, "async",
				assertThrowable(RuntimeException.class, "TEST"));
	}

	@Path("/")
	public static class AsyncThrowResource {

		private @Inject ManagedExecutorService executor;

		@GET
		public void async(@Suspended AsyncResponse async) {
			assertTrue("Should be suspended", async.isSuspended());
			this.executor.execute(() -> {
				throw new RuntimeException("TEST");
			});
		}
	}

	/**
	 * Validates the {@link Exception} thrown/reported.
	 * 
	 * @param exceptionType Exception type.
	 * @param message       Message.
	 * @return Validator to confirm {@link Exception} response.
	 */
	private static Consumer<MockWoofServer> assertThrowable(Class<? extends Throwable> exceptionType, String message) {
		return (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/"));
			String entity = response.getEntity(null);
			response.assertStatus(500);
			assertTrue("Should find failure in response: " + entity,
					entity.contains(RuntimeException.class.getName() + ": " + message));
		};
	}

	/**
	 * Undertakes test.
	 * 
	 * @param jaxrsResourceClass      JAX-RS resource {@link Class}.
	 * @param jaxrsResourceMethodName Name of JAX-RS resource {@link Method}.
	 * @param expectedEntity          Expected entity.
	 * @param headerNameValuePairs    Header name/value pairs.
	 */
	private void doJaxRsTest(Class<?> jaxrsResourceClass, String jaxrsResourceMethodName, String expectedEntity,
			String... headerNameValuePairs) {
		this.doJaxRsTest(HttpMethod.GET, "/", jaxrsResourceClass, jaxrsResourceMethodName, (server) -> {
			MockHttpRequestBuilder request = MockWoofServer.mockRequest("/");
			for (int i = 0; i < headerNameValuePairs.length; i += 2) {
				request.header(headerNameValuePairs[i], headerNameValuePairs[i + 1]);
			}
			MockHttpResponse response = server.send(request);
			response.assertResponse(200, expectedEntity);
		});
	}

	/**
	 * Undertakes test.
	 * 
	 * @param httpMethod              {@link HttpMethod}.
	 * @param path                    Path.
	 * @param jaxrsResourceClass      JAX-RS resource {@link Class}.
	 * @param jaxrsResourceMethodName Name of JAX-RS resource {@link Method}.
	 * @param validator               Validator.
	 */
	private void doJaxRsTest(HttpMethod httpMethod, String path, Class<?> jaxrsResourceClass,
			String jaxrsResourceMethodName, Consumer<MockWoofServer> validator) {
		CompileWoof compiler = new CompileWoof(true);
		compiler.woof((context) -> {
			OfficeSection controller = context.getProcedureArchitect().addProcedure("JAXRS",
					jaxrsResourceClass.getName(), JaxRsProcedureSource.SOURCE_NAME, jaxrsResourceMethodName, false,
					null);
			context.getOfficeArchitect().link(
					context.getWebArchitect().getHttpInput(false, httpMethod.getName(), path).getInput(),
					controller.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME));
		});
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			Singleton.load(office, new JaxRsDependency());
			Singleton.load(office, new AsyncDependency());
		});
		try (MockWoofServer server = compiler.open()) {
			validator.accept(server);
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}