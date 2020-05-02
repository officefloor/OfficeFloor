package net.officefloor.jaxrs;

import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.tutorial.jaxrsapp.JaxRsDependency;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can run JAX-RS application.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsTest extends OfficeFrameTestCase {

	/**
	 * Ensure can GET.
	 */
	public void testGet() throws Exception {
		this.doJaxRsTest("/jaxrs", "GET");
	}

	/**
	 * Ensure path parameter.
	 */
	public void testPathParam() throws Exception {
		this.doJaxRsTest("/jaxrs/path/parameter", "parameter");
	}

	/**
	 * Ensure query parameter.
	 */
	public void testQueryParameter() throws Exception {
		this.doJaxRsTest("/jaxrs/query?param=parameter", "parameter");
	}

	/**
	 * Ensure default query parameter.
	 */
	public void testDefaultQueryParameter() throws Exception {
		this.doJaxRsTest("/jaxrs/query", "default");
	}

	/**
	 * Ensure header.
	 */
	public void testHeader() throws Exception {
		this.doJaxRsTest("/jaxrs/header", "header", "param", "header");
	}

	/**
	 * Ensure cookie.
	 */
	public void testCookie() throws Exception {
		this.doJaxRsTest("/jaxrs/cookie", "cookie", "cookie", "param=cookie");
	}

	/**
	 * Ensure form parameter.
	 */
	public void testFormParameter() throws Exception {
		this.doJaxRsTest(HttpMethod.POST, "/jaxrs/form", "form", (request) -> request.entity("param=form"));
	}

	/**
	 * Ensure {@link UriInfo}.
	 */
	public void testUriInfo() throws Exception {
		this.doJaxRsTest("/jaxrs/uriInfo/path?param=query",
				"PATH=jaxrs/uriInfo/path, PATH PARAM=path, QUERY PARAM=query");
	}

	/**
	 * Ensure {@link HttpHeaders}.
	 */
	public void testHttpHeaders() throws Exception {
		this.doJaxRsTest("/jaxrs/headers", "HEADER=header, COOKIE=cookie", "param", "header", "cookie", "param=cookie");
	}

	/**
	 * Ensure JSON.
	 */
	public void testJson() throws Exception {
		this.doJaxRsTest(HttpMethod.POST, "/jaxrs/json", "{\"message\":\"JSON\"}",
				(request) -> request.entity("{\"input\":\"JSON\"}"));
	}

	/**
	 * Ensure inject.
	 */
	public void testInject() throws Exception {
		this.doJaxRsTest("/jaxrs/inject", "Inject Override");
	}

	/**
	 * Ensure context.
	 */
	public void testContext() throws Exception {
		this.doJaxRsTest("/jaxrs/context", "Context Override");
	}

	/**
	 * Ensure sub resource.
	 */
	public void testSubResource() throws Exception {
		this.doJaxRsTest("/jaxrs/sub/resource", "sub-resource");
	}

	/**
	 * Undertakes JAX-RS test.
	 * 
	 * @param path                 Path to JAX-RS resource.
	 * @param expectedEntity       Expected entity in response.
	 * @param headerNameValuePairs {@link HttpHeader} name/value pairs.
	 */
	private void doJaxRsTest(String path, String expectedEntity, String... headerNameValuePairs) throws Exception {
		this.doJaxRsTest(HttpMethod.GET, path, expectedEntity, null, headerNameValuePairs);
	}

	/**
	 * Undertakes JAX-RS test.
	 * 
	 * @param httpMethod           {@link HttpMethod}.
	 * @param path                 Path to JAX-RS resource.
	 * @param expectedEntity       Expected entity in response.
	 * @param headerNameValuePairs {@link HttpHeader} name/value pairs.
	 */
	private void doJaxRsTest(HttpMethod httpMethod, String path, String expectedEntity,
			Function<MockHttpRequestBuilder, MockHttpRequestBuilder> decorate, String... headerNameValuePairs)
			throws Exception {

		// Undertake test
		CompileWoof compile = new CompileWoof(true);
		compile.office((context) -> {
			Singleton.load(context.getOfficeArchitect(), new OverrideJaxRsResource());
		});
		try (MockWoofServer server = compile.open()) {
			MockHttpRequestBuilder request = MockHttpServer.mockRequest(path).method(httpMethod);
			for (int i = 0; i < headerNameValuePairs.length; i += 2) {
				request = request.header(headerNameValuePairs[i], headerNameValuePairs[i + 1]);
			}
			if (decorate != null) {
				request = decorate.apply(request);
			}
			MockHttpResponse response = server.send(request);
			response.assertResponse(200, expectedEntity);
		}
	}

	/**
	 * Provide own {@link JaxRsDependency}.
	 */
	public static class OverrideJaxRsResource extends JaxRsDependency {

		@Override
		public String getMessage() {
			return "Override";
		}
	}

}