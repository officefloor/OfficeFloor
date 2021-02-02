package net.officefloor.server.aws.sam;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.servlet.test.MockServerSettings;

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
			MockServerSettings.runWithinContext((deployer, context) -> {

				// Configure the HTTP Server
				DeployedOfficeInput input = deployer.getDeployedOffice("OFFICE").getDeployedOfficeInput("SERVICE",
						"service");
				new HttpServer(input, deployer, context);

			}, (architect, context) -> {

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