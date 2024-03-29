/*-
 * #%L
 * AWS SAM HTTP Server
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.server.aws.sam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.test.ExternalServerRunner;

/**
 * Tests the {@link OfficeFloorSam}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSamTest {

	@AfterEach
	public void shutdown() throws Exception {
		OfficeFloorSam.close();
	}

	/**
	 * Simple GET request.
	 */
	@Test
	public void get() {
		this.doRequest(ServiceGet.class, "GET", "/get", null).assertResponse(200, "TEST");
	}

	public static class ServiceGet {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntityWriter().write("TEST");
		}
	}

	/**
	 * Simple POST request.
	 */
	@Test
	public void post() {
		this.doRequest(ServicePost.class, "POST", "/post", "TEST").assertResponse(200, "TEST");
	}

	public static class ServicePost {
		public void service(ServerHttpConnection connection) throws IOException {
			InputStream input = connection.getRequest().getEntity();
			OutputStream output = connection.getResponse().getEntity();
			for (int character = input.read(); character != -1; character = input.read()) {
				output.write(character);
			}
		}
	}

	/**
	 * Send HTTP headers.
	 */
	@Test
	public void headers() {
		this.doRequest(ServiceHeaders.class, "GET", "/headers", null, "one", "1", "two", "2").assertResponse(204, null,
				false, "one", "1", "two", "2");
	}

	public static class ServiceHeaders {
		public void service(ServerHttpConnection connection) {
			for (HttpHeader header : connection.getRequest().getHeaders()) {
				connection.getResponse().getHeaders().addHeader(header.getName(), header.getValue());
			}
		}
	}

	/**
	 * Send Cookie.
	 */
	@Test
	public void cookies() {
		this.doRequest(ServiceCookies.class, "GET", "/cookies", null).assertResponse(204, null, false, "set-cookie",
				"ONE=1");
	}

	public static class ServiceCookies {
		public void service(ServerHttpConnection connection) {
			HttpResponseCookies cookies = connection.getResponse().getCookies();
			cookies.setCookie("ONE", "1");
		}
	}

	/**
	 * Handle query parameters.
	 */
	@Test
	public void query() {
		APIGatewayProxyRequestEvent request = this.request("GET", "/query", null);
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("one", "1");
		queryParams.put("two", "2");
		request.setQueryStringParameters(queryParams);
		Map<String, List<String>> expected = new HashMap<>();
		for (String key : queryParams.keySet()) {
			expected.put(key, Arrays.asList(queryParams.get(key)));
		}
		new Response(this.startSam(ServiceQuery.class).handleRequest(request, null)).assertResponse(200,
				assertQuery(expected), true);
	}

	private static BiConsumer<String, Boolean> assertQuery(Map<String, List<String>> values) {
		return (body, isBase64Encoded) -> {
			String requestUri = isBase64Encoded ? new String(Base64.getDecoder().decode(body), Charset.forName("UTF-8"))
					: body;
			String queryString = requestUri.split("\\?")[1];
			Map<String, List<String>> expected = new HashMap<>();
			for (String queryParam : queryString.split("&")) {
				String[] queryNameValue = queryParam.split("=");
				String queryName = queryNameValue[0];
				List<String> expectedValues = expected.get(queryName);
				if (expectedValues == null) {
					expectedValues = new LinkedList<>();
					expected.put(queryName, expectedValues);
				}
				expectedValues.add(queryNameValue[1]);
			}

			// Ensure same query param values
			assertEquals(expected.size(), values.size(),
					"Incorrect number of queries (" + expected + " != " + values + ")");
			for (String queryName : expected.keySet()) {
				List<String> expectedValues = expected.get(queryName);
				List<String> actualValues = values.get(queryName);
				assertNotNull(actualValues, "No values for query name " + queryName);
				assertEquals(expectedValues.size(), actualValues.size(), "Incorrect number of values for query name "
						+ queryName + " (" + expectedValues + " != " + actualValues + ")");
				for (int i = 0; i < expectedValues.size(); i++) {
					assertEquals(expectedValues.get(i), actualValues.get(i),
							"Incorrect query " + queryName + " value " + i);
				}
			}
		};
	}

	public static class ServiceQuery {
		public void service(ServerHttpConnection connection) throws IOException {
			String requestUri = connection.getRequest().getUri();
			connection.getResponse().getEntityWriter().write(requestUri);
		}
	}

	/**
	 * Handle query parameters.
	 */
	@Test
	public void queryMultiValues() {
		APIGatewayProxyRequestEvent request = this.request("GET", "/query", null);
		Map<String, List<String>> multiQueryParams = new HashMap<>();
		multiQueryParams.put("one", Arrays.asList("1", "2"));
		multiQueryParams.put("two", Arrays.asList("a", "b"));
		request.setMultiValueQueryStringParameters(multiQueryParams);

		// Should ignore query params if have multiple
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("one", "ignored");
		queryParams.put("ignored", "2");
		request.setQueryStringParameters(queryParams);

		new Response(this.startSam(ServiceQuery.class).handleRequest(request, null)).assertResponse(200,
				assertQuery(multiQueryParams), true);
	}

	/**
	 * Base64 request entity.
	 */
	@Test
	public void base64Request() {
		String entity = "TEST";
		String base64Entity = Base64.getEncoder().encodeToString(entity.getBytes(Charset.forName("UTF-8")));
		APIGatewayProxyRequestEvent request = this.request("GET", "/base64Request", base64Entity, true);
		new Response(this.startSam(ServiceBase64Request.class).handleRequest(request, null)).assertResponse(200,
				entity);
	}

	public static class ServiceBase64Request {
		public void service(ServerHttpConnection connection) throws IOException {
			Reader request = new InputStreamReader(connection.getRequest().getEntity());
			Writer response = connection.getResponse().getEntityWriter();
			StringBuilder entity = new StringBuilder();
			for (int character = request.read(); character != -1; character = request.read()) {
				response.write(character);
				entity.append((char) character);
			}
			assertEquals("TEST", entity.toString(), "Incorrect entity");
		}
	}

	/**
	 * Buffer.
	 */
	@Test
	public void buffer() {
		this.doRequest(ServiceBuffer.class, "GET", "/buffer", null).assertResponse(200, "BUFFER");
	}

	public static class ServiceBuffer {
		public void service(ServerHttpConnection connection) throws IOException {
			connection.getResponse().getEntity().write(ByteBuffer.wrap("BUFFER".getBytes(Charset.forName("UTF-8"))));
		}
	}

	/**
	 * File resource.
	 */
	@Test
	public void file() {
		this.doRequest(ServiceFile.class, "GET", "/file", null).assertResponse(200, "FILE");
	}

	public static class ServiceFile {
		public void service(ServerHttpConnection connection) throws IOException {
			FileChannel file = FileChannel.open(Paths.get("./src/test/resources/file.txt"));
			connection.getResponse().getEntity().write(file, null);
		}
	}

	/**
	 * Ensure handle async servicing.
	 */
	@Test
	public void asyncServicing() {
		this.doRequest(ServiceAsync.class, "GET", "/async", null).assertResponse(200, "ASYNC");
	}

	public static class ServiceAsync {
		public void service(AsynchronousFlow async, ServerHttpConnection connection) {
			new Thread(() -> {

				try {
					Thread.sleep(1); // ensure less chance of immediate return
				} catch (InterruptedException ex) {
					// carry on
				}

				async.complete(() -> connection.getResponse().getEntityWriter().write("ASYNC"));
			}).start();
		}
	}

	/**
	 * Starts {@link OfficeFloorSam}.
	 * 
	 * @param sectionClass {@link Class} providing section logic.
	 * @return Started {@link OfficeFloorSam}.
	 */
	private OfficeFloorSam startSam(Class<?> sectionClass) {
		try {

			// Start servicing
			ExternalServerRunner.startExternalServer("SERVICE", "service", null, (architect, context) -> {

				// Provide servicing of input
				architect.enableAutoWireObjects();
				architect.addOfficeSection("SERVICE", ClassSectionSource.class.getName(), sectionClass.getName());

			}, () -> {
				// Start the server
				OfficeFloorSam.open();
			});

			// Always new instance for servicing request
			return new OfficeFloorSam();

		} catch (Exception ex) {
			return fail(ex);
		}
	}

	/**
	 * Undertakes test.
	 * 
	 * @param sectionClass     {@link Class} providing section logic.
	 * @param method           HTTP method.
	 * @param path             Path.
	 * @param requestEntity    Request entity.
	 * @param statusCode       Response status code.
	 * @param responseEntity   Expected response entity.
	 * @param headerNameValues Expected header name values.
	 * @return {@link Response}.
	 */
	private Response doRequest(Class<?> sectionClass, String method, String path, String requestEntity,
			String... headerNameValues) {
		APIGatewayProxyRequestEvent request = this.request(method, path, requestEntity, headerNameValues);
		APIGatewayProxyResponseEvent response = this.startSam(sectionClass).handleRequest(request, null);
		return new Response(response);
	}

	/**
	 * Creates the {@link APIGatewayProxyRequestEvent}.
	 * 
	 * @return {@link APIGatewayProxyRequestEvent}.
	 */
	private APIGatewayProxyRequestEvent request(String method, String path, String body, String... headerNameValues) {
		return this.request(method, path, body, false, headerNameValues);
	}

	/**
	 * Creates the {@link APIGatewayProxyRequestEvent}.
	 * 
	 * @return {@link APIGatewayProxyRequestEvent}.
	 */
	private APIGatewayProxyRequestEvent request(String method, String path, String body, boolean isBase64,
			String... headerNameValues) {

		// Create and configure request
		APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
		request.setHttpMethod(method);
		request.setPath(path);
		Map<String, List<String>> headers = new HashMap<>();
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			List<String> values = headers.get(name);
			if (values == null) {
				values = new LinkedList<>();
				headers.put(name, values);
			}
			values.add(value);
		}
		request.setMultiValueHeaders(headers);
		request.setBody(body);
		request.setIsBase64Encoded(isBase64);

		// Return the request
		return request;
	}

	/**
	 * Response.
	 */
	private static class Response {

		private final APIGatewayProxyResponseEvent response;

		private Response(APIGatewayProxyResponseEvent response) {
			this.response = response;
		}

		/**
		 * Asserts the {@link APIGatewayProxyResponseEvent}.
		 */
		private void assertResponse(int statusCode, String body, String... headerNameValues) {
			this.assertResponse(statusCode, (actualBody, isBase64Encoded) -> {
				String expectedBody = isBase64Encoded
						? Base64.getEncoder().encodeToString(body.getBytes(Charset.forName("UTF-8")))
						: body;
				assertEquals(expectedBody, actualBody, "Incorrect response body (" + body + ")");
			}, true, headerNameValues);
		}

		/**
		 * Asserts the {@link APIGatewayProxyResponseEvent}.
		 */
		private void assertResponse(int statusCode, BiConsumer<String, Boolean> body, boolean isBase64,
				String... headerNameValues) {
			assertEquals(statusCode, this.response.getStatusCode(), "Incorrect status code");
			boolean isBase64Encoded = Optional.ofNullable(this.response.getIsBase64Encoded()).orElse(body != null);
			assertEquals(isBase64, isBase64Encoded, "Incorrect response base64 flag");
			if (body != null) {
				body.accept(this.response.getBody(), isBase64Encoded);
			} else {
				assertNull(this.response.getBody(), "Should not have body");
			}
			for (int i = 0; i < headerNameValues.length; i += 2) {
				String expectedName = headerNameValues[i];
				String expectedValue = headerNameValues[i + 1];
				Map<String, String> headers = this.response.getHeaders();
				assertTrue(headers.containsKey(expectedName), "No response header '" + expectedName + "'");
				assertEquals(expectedValue, headers.get(expectedName), "Incorrect response header " + expectedName);
			}
		}
	}

}
