/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs.chain;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.servlet.supply.ServletWoofExtensionService;
import net.officefloor.woof.compile.CompileWoof;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Ensure can run JAX-RS application.
 * 
 * @author Daniel Sagenschneider
 */
public class ChainJaxRsTest extends OfficeFrameTestCase {

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
		this.doJaxRsTest(HttpMethod.POST, "/jaxrs/form", (request) -> request.entity("param=form"),
				(response) -> response.assertResponse(200, "form"), "Content-Type",
				"application/x-www-form-urlencoded");
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
		this.doJaxRsTest(HttpMethod.POST, "/jaxrs/json", (request) -> request.entity("{\"input\":\"JSON\"}"),
				(response) -> response.assertResponse(200, "{\"message\":\"JSON\"}"), "Content-Type",
				"application/json");
	}

	/**
	 * Ensure inject.
	 */
	public void testInject() throws Exception {
		this.doJaxRsTest("/jaxrs/inject", "Inject Dependency");
	}

	/**
	 * Ensure context.
	 */
	public void testContext() throws Exception {
		this.doJaxRsTest("/jaxrs/context", "Context Dependency");
	}

	/**
	 * Ensure sub resource.
	 */
	public void testSubResource() throws Exception {
		this.doJaxRsTest("/jaxrs/sub/resource", "sub-resource");
	}

	/**
	 * Ensure async resumed synchronously.
	 */
	public void testAsyncSynchronous() throws Exception {
		this.doJaxRsTest("/jaxrs/async/synchronous", "Sync Dependency");
	}

	/**
	 * Ensure async resumed asynchronously.
	 */
	public void testAsyncAsynchronous() throws Exception {
		this.doJaxRsTest("/jaxrs/async/asynchronous", "Async Dependency");
	}

	/**
	 * Ensure handle checked exception.
	 */
	@Test
	public void testCheckedExeption() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/jaxrs/exception/checked", null, assertThrowable(IOException.class, "TEST"));
	}

	/**
	 * Ensure handle unchecked exception.
	 */
	@Test
	public void testUncheckedExeption() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/jaxrs/exception/unchecked", null,
				assertThrowable(RuntimeException.class, "TEST"));
	}

	/**
	 * Ensure handle async resumed exception.
	 */
	@Test
	public void testAsyncExeption() throws Exception {
		this.doJaxRsTest(HttpMethod.GET, "/jaxrs/exception/async", null, assertThrowable(null, "Request failed."));
	}

	/**
	 * Create assertion of {@link Throwable}.
	 * 
	 * @param exception {@link Throwable} {@link Class}.
	 * @param message   {@link Throwable} message.
	 * @return Assertion of the {@link Throwable}.
	 */
	private static Consumer<MockHttpResponse> assertThrowable(Class<? extends Throwable> exception, String message) {
		return (response) -> {
			response.assertStatus(500);
			String entity = response.getEntity(null);
			if (exception != null) {
				assertTrue("Should have exception in response: " + entity,
						entity.contains(exception.getName() + ": TEST"));
			} else {
				assertEquals("Incorrect entity", "{\"error\":\"" + message + "\"}", entity);
			}
		};
	}

	/**
	 * Ensure can inject {@link Dependency}.
	 */
	public void testDependency() throws Exception {
		this.doJaxRsTest("/dependency", "Dependency OfficeFloor");
	}

	/**
	 * Ensure can inject duplicate {@link Dependency}.
	 */
	public void testDuplicateDependency() throws Exception {
		this.doJaxRsTest("/dependency/duplicate", "Duplicate OfficeFloor");
	}

	/**
	 * Ensure can inject qualified {@link Dependency}.
	 */
	public void testQualifiedDependency() throws Exception {
		this.doJaxRsTest("/dependency/qualified", "Dependency Qualified OfficeFloor");
	}

	/**
	 * Ensure can {@link Inject} from {@link OfficeFloor}.
	 */
	public void testJustInTimeDependency() throws Exception {
		this.doJaxRsTest("/dependency/justintime", "Dependency just in time");
	}

	/**
	 * Ensure can {@link Inject} duplicate from {@link OfficeFloor}.
	 */
	public void testJustInTimeDuplicate() throws Exception {
		this.doJaxRsTest("/dependency/justintime/duplicate", "Duplicate just in time");
	}

	/**
	 * Ensure can qualified {@link Inject} from {@link OfficeFloor}.
	 */
	public void testQualifiedJustInTime() throws Exception {
		this.doJaxRsTest("/dependency/justintime/qualified", "Dependency Qualified just in time");
	}

	/**
	 * Ensure asynchronous use of {@link Dependency}.
	 */
	public void testAsyncDependency() throws Exception {
		this.doJaxRsTest("/dependency/async/dependency", "Async OfficeFloor");
	}

	/**
	 * Ensure asynchronous use of just in time {@link Inject}.
	 */
	public void testAsyncInject() throws Exception {
		this.doJaxRsTest("/dependency/async/inject", "Async just in time");
	}

	/**
	 * Undertakes JAX-RS test.
	 * 
	 * @param path                 Path to JAX-RS resource.
	 * @param expectedEntity       Expected entity in response.
	 * @param headerNameValuePairs {@link HttpHeader} name/value pairs.
	 */
	private void doJaxRsTest(String path, String expectedEntity, String... headerNameValuePairs) throws Exception {
		this.doJaxRsTest(HttpMethod.GET, path, null, (response) -> response.assertResponse(200, expectedEntity),
				headerNameValuePairs);
	}

	/**
	 * Undertakes JAX-RS test.
	 * 
	 * @param httpMethod           {@link HttpMethod}.
	 * @param path                 Path to JAX-RS resource.
	 * @param decorate             Decorates the request.
	 * @param validateResponse     Validates the response.
	 * @param headerNameValuePairs {@link HttpHeader} name/value pairs.
	 */
	private void doJaxRsTest(HttpMethod httpMethod, String path,
			Function<MockHttpRequestBuilder, MockHttpRequestBuilder> decorate,
			Consumer<MockHttpResponse> validateResponse, String... headerNameValuePairs) throws Exception {

		// Undertake test
		CompileWoof compile = new CompileWoof(true);
		compile.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			Singleton.load(office, "DEPENDENCY", new ResourceDependency("OfficeFloor"));
			Singleton.load(office, "QUALIFIED_DEPENDENCY", new ResourceDependency("Qualified OfficeFloor"))
					.addTypeQualification("QUALIFIED", ResourceDependency.class.getName());
			Singleton.load(office, "INJECT", new JustInTimeDependency("just in time"));
			Singleton.load(office, "QUALIFIED_INJECT", new JustInTimeDependency("Qualified just in time"))
					.addTypeQualification(QualifiedInject.class.getName(), JustInTimeDependency.class.getName());
		});
		try (MockWoofServer server = compile.open(ServletWoofExtensionService.getChainServletsPropertyName("OFFICE"),
				"true")) {
			MockHttpRequestBuilder request = MockHttpServer.mockRequest(path).method(httpMethod);
			for (int i = 0; i < headerNameValuePairs.length; i += 2) {
				request = request.header(headerNameValuePairs[i], headerNameValuePairs[i + 1]);
			}
			if (decorate != null) {
				request = decorate.apply(request);
			}
			MockHttpResponse response = server.send(request);
			validateResponse.accept(response);
		}
	}

}
