package net.officefloor.server.aws.sam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;

/**
 * Tests the {@link OfficeFloorSam}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSamTest {

	/**
	 * {@link OfficeFloorSam} under test.
	 */
	private OfficeFloorSam sam;

	@BeforeEach
	public void start() throws Throwable {
		this.sam = new OfficeFloorSam();
	}

	@AfterEach
	public void shutdown() throws Exception {
		this.sam.close();
	}

	/**
	 * Simple GET request.
	 */
	@Test
	public void get() {
		this.doRequest("GET", "/get", null).assertResponse(200, "TEST");
	}

	public void serviceGet(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write("TEST");
	}

	/**
	 * Simple POST request.
	 */
	@Test
	public void post() {
		this.doRequest("POST", "/post", "TEST").assertResponse(200, "TEST");
	}

	public void servicePost(ServerHttpConnection connection) throws IOException {
		InputStream input = connection.getRequest().getEntity();
		OutputStream output = connection.getResponse().getEntity();
		for (int character = input.read(); character != -1; character = input.read()) {
			output.write(character);
		}
	}

	/**
	 * Send HTTP headers.
	 */
	@Test
	public void headers() {
		this.doRequest("GET", "/headers", null, "one", "1", "two", "2").assertResponse(204, null, false, "one", "1",
				"two", "2");
	}

	public void serviceHeaders(ServerHttpConnection connection) {
		for (HttpHeader header : connection.getRequest().getHeaders()) {
			connection.getResponse().getHeaders().addHeader(header.getName(), header.getValue());
		}
	}

	/**
	 * Send Cookie.
	 */
	@Test
	public void cookies() {
		this.doRequest("GET", "/cookies", null).assertResponse(204, null, false, "set-cookie", "ONE=1");
	}

	public void serviceCookies(ServerHttpConnection connection) {
		HttpResponseCookies cookies = connection.getResponse().getCookies();
		cookies.setCookie("ONE", "1");
	}

	/**
	 * Base64 request entity.
	 */
	@Test
	public void base64Request() {
		String entity = "TEST";
		String base64Entity = Base64.getEncoder().encodeToString(entity.getBytes(Charset.forName("UTF-8")));
		APIGatewayProxyRequestEvent request = this.request("GET", "/base64Request", base64Entity, true);
		new Response(this.sam.handleRequest(request, null)).assertResponse(200, entity);
	}

	public void serviceBase64Request(ServerHttpConnection connection) throws IOException {
		Reader request = new InputStreamReader(connection.getRequest().getEntity());
		Writer response = connection.getResponse().getEntityWriter();
		StringBuilder entity = new StringBuilder();
		for (int character = request.read(); character != -1; character = request.read()) {
			response.write(character);
			entity.append((char) character);
		}
		assertEquals("TEST", entity.toString(), "Incorrect entity");
	}

	/**
	 * Buffer.
	 */
	@Test
	public void buffer() {
		this.doRequest("GET", "/buffer", null).assertResponse(200, "BUFFER");
	}

	public void serviceBuffer(ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntity().write(ByteBuffer.wrap("BUFFER".getBytes(Charset.forName("UTF-8"))));
	}

	/**
	 * File resource.
	 */
	@Test
	public void file() {
		this.doRequest("GET", "/file", null).assertResponse(200, "FILE");
	}

	/**
	 * Path parameter.
	 */
	@Test
	public void pathParameter() {
		this.doRequest("GET", "/path/one/two", null).assertResponse(200, "one,two");
	}

	public void servicePathParameter(@HttpPathParameter("paramOne") String paramOne,
			@HttpPathParameter("paramTwo") String paramTwo, ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write(paramOne + "," + paramTwo);
	}

	/**
	 * Query parameter.
	 */
	@Test
	public void queryParameter() {
		this.doRequest("GET", "/query?paramOne=one&paramTwo=two", null).assertResponse(200, "one,two");
	}

	public void serviceQueryParameter(@HttpQueryParameter("paramOne") String paramOne,
			@HttpQueryParameter("paramTwo") String paramTwo, ServerHttpConnection connection) throws IOException {
		connection.getResponse().getEntityWriter().write(paramOne + "," + paramTwo);
	}

	/**
	 * Ensure handle async servicing.
	 */
	@Test
	public void asyncServicing() {
		this.doRequest("GET", "/async", null).assertResponse(200, "ASYNC");
	}

	public void serviceAsync(AsynchronousFlow async, ServerHttpConnection connection) {
		new Thread(() -> {

			try {
				Thread.sleep(1); // ensure less chance of immediate return
			} catch (InterruptedException ex) {
				// carry on
			}

			async.complete(() -> connection.getResponse().getEntityWriter().write("ASYNC"));
		}).start();
	}

	/**
	 * Undertakes test.
	 * 
	 * @param method           HTTP method.
	 * @param path             Path.
	 * @param requestEntity    Request entity.
	 * @param statusCode       Response status code.
	 * @param responseEntity   Expected response entity.
	 * @param headerNameValues Expected header name values.
	 * @return {@link Response}.
	 */
	private Response doRequest(String method, String path, String requestEntity, String... headerNameValues) {
		APIGatewayProxyRequestEvent request = this.request(method, path, requestEntity, headerNameValues);
		APIGatewayProxyResponseEvent response = sam.handleRequest(request, null);
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
			this.assertResponse(statusCode, body, true, headerNameValues);
		}

		/**
		 * Asserts the {@link APIGatewayProxyResponseEvent}.
		 */
		private void assertResponse(int statusCode, String body, boolean isBase64, String... headerNameValues) {
			assertEquals(statusCode, this.response.getStatusCode(), "Incorrect status code");
			boolean isBase64Encoded = Optional.ofNullable(this.response.getIsBase64Encoded()).orElse(body != null);
			assertEquals(isBase64, isBase64Encoded, "Incorrect response base64 flag");
			String expectedBody = isBase64Encoded
					? Base64.getEncoder().encodeToString(body.getBytes(Charset.forName("UTF-8")))
					: body;
			assertEquals(expectedBody, this.response.getBody(), "Incorrect response body (" + body + ")");
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