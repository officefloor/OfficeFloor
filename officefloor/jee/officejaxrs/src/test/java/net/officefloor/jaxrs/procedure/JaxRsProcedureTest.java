package net.officefloor.jaxrs.procedure;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.test.OfficeFrameTestCase;
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
		this.doJaxRsTest(JaxRsResource.class, "pathParam", "parameter");
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
		this.doJaxRsTest(HttpMethod.GET, "/info/{param}", JaxRsResource.class, "uriInfo", (server) -> {
			MockHttpResponse response = server.send(MockWoofServer.mockRequest("/info/path?param=query"));
			response.assertResponse(200, "PATH=info/path, PATH PARAM=path, QUERY PARAM=query");
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
		});
		try (MockWoofServer server = compiler.open()) {
			validator.accept(server);
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}