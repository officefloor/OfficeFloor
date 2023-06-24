package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.invoker.runner.Invoker;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * Tests the {@link HttpFunctionSectionSource} to be consistent with
 * {@link Invoker}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConsistentHttpFunctionTest {

	/**
	 * {@link Invoker} to compare expected functionality.
	 */
	private static final Invoker invoker = new Invoker(8181, ConsistentHttpFunction.class.getName(), null,
			ConsistentHttpFunction.class.getClassLoader());

	/**
	 * Testing to ensure this consistently invokes the {@link HttpFunction}.
	 */
	private static final @RegisterExtension GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			ConsistentHttpFunction.class);

	private static final @RegisterExtension HttpClientExtension client = new HttpClientExtension();

	private static String SERVER_URL = "http://localhost:8181";

	@BeforeAll
	public static void startServer() throws Exception {
		invoker.startTestServer();
	}

	@AfterAll
	public static void stopServer() throws Exception {
		invoker.stopServer();
	}

	/**
	 * Ensure {@link HttpRequest} details are correct.
	 */
	@Test
	public void request() throws Exception {

		// Create requests
		String url = url("request");
		HttpPost invokerRequest = new HttpPost(url);
		MockHttpRequestBuilder mockRequest = GoogleHttpFunctionExtension.mockRequest(url).method(HttpMethod.POST);

		// Configure headers
		String[] headers = new String[] { "duplicate", "one", "duplicate", "two", "another", "test", "Content-Type",
				"text/html; charset=UTF-8" };
		for (int i = 0; i < headers.length; i += 2) {
			String name = headers[i];
			String value = headers[i + 1];
			invokerRequest.addHeader(name, value);
			mockRequest.header(name, value);
		}

		// Configure entity
		String entity = "ENTITY";
		invokerRequest.setEntity(new StringEntity(entity));
		mockRequest.entity(entity);

		// Assert the request
		assertRequest(invokerRequest, mockRequest, HttpStatus.OK);
	}

	/**
	 * Ensure {@link HttpRequest} {@link InputStream} is appropriate.
	 */
	@Test
	public void requestInputStream() throws Exception {

		// Create requests
		String url = url("request_inputstream");
		HttpPost invokerRequest = new HttpPost(url);
		MockHttpRequestBuilder mockRequest = GoogleHttpFunctionExtension.mockRequest(url).method(HttpMethod.POST);

		// Provide byte entities
		byte[] entity = new byte[] { 1, 2, 10, 127 };
		invokerRequest.setEntity(new ByteArrayEntity(entity));
		mockRequest.getHttpEntity().write(entity);

		// Invoke request for each
		HttpResponse invokerResponse = client.execute(invokerRequest);
		MockHttpResponse mockResponse = httpFunction.send(mockRequest);

		// Ensure both successful
		assertEquals(200, invokerResponse.getStatusLine().getStatusCode(), "Invoker request should be successful");
		assertEquals(200, mockResponse.getStatus().getStatusCode(), "Mock request should be successful");

		// Ensure response matches
		byte[] invokerEntity = EntityUtils.toByteArray(invokerResponse.getEntity());
		InputStream mockEntity = mockResponse.getEntity();
		for (int i = 0; i < invokerEntity.length; i++) {
			int expected = invokerEntity[i];
			int actual = mockEntity.read();
			assertEquals(expected, actual, "Incorrect byte " + i);
		}
		assertEquals(-1, mockEntity.read(), "Should be end of mock entity");
	}

	/**
	 * Ensure {@link HttpRequest} {@link Optional} values can be blank.
	 */
	@Test
	public void requestOptionals() {
		HttpGet invokerRequest = new HttpGet(SERVER_URL);
		MockHttpRequestBuilder mockRequest = GoogleHttpFunctionExtension.mockRequest(SERVER_URL);
		assertRequest(invokerRequest, mockRequest, HttpStatus.OK);
	}

	/**
	 * Ensure provide response status.
	 */
	@Test
	public void responseStatus() {
		assertRequest("response_status", HttpStatus.EXPECTATION_FAILED);
	}

	/**
	 * Ensure provide response status message.
	 */
	@Test
	public void responseStatusMessage() {
		assertRequest("response_statusMessage", HttpStatus.ACCEPTED);
	}

	/**
	 * Ensure provide response content-type.
	 */
	@Test
	public void responseContentType() {
		assertRequest("response_contentType", HttpStatus.OK, "Content-Type");
	}

	/**
	 * Ensure provide response header.
	 */
	@Test
	public void responseHeader() {
		assertRequest("response_header", HttpStatus.OK, "Test-Header");
	}

	/**
	 * Ensure provide multiple response headers.
	 */
	@Test
	public void responseMultipleHeaders() {
		assertRequest("response_multipleHeaders", HttpStatus.OK, "Header-One", "Header-Two", "Header-Three");
	}

	/**
	 * Ensure provide response entity.
	 */
	@Test
	public void responseEntity() {
		assertRequest("response_entity", HttpStatus.OK);
	}

	/**
	 * Creates URL for test type.
	 * 
	 * @param test Test type.
	 * @return URL for test type.
	 */
	private static String url(String test) {
		return SERVER_URL + "/?test=" + test;
	}

	/**
	 * Assert {@link HttpGet}.
	 * 
	 * @param test               Test identifier.
	 * @param expectedHttpStatus Expected {@link HttpStatus}.
	 * @param verifyHeaderNames  Names of HTTP headers to verify.
	 * @return {@link HttpResponse}.
	 * @throws Exception If fails to test.
	 */
	private static void assertRequest(String test, HttpStatus expectedHttpStatus, String... verifyHeaderNames) {
		String url = url(test);
		assertRequest(new HttpGet(url), GoogleHttpFunctionExtension.mockRequest(url), expectedHttpStatus,
				verifyHeaderNames);
	}

	/**
	 * Assert {@link HttpUriRequest}.
	 * 
	 * @param invokerRequest     {@link HttpUriRequest}.
	 * @param mockRequest        {@link MockHttpRequestBuilder}.
	 * @param expectedHttpStatus Expected {@link HttpStatus}.
	 * @param verifyHeaderNames  Names of HTTP headers to verify.
	 * @throws Exception If fails to test.
	 */
	private static void assertRequest(HttpUriRequest invokerRequest, MockHttpRequestBuilder mockRequest,
			HttpStatus expectedHttpStatus, String... verifyHeaderNames) {
		try {

			// Invoke request for each
			HttpResponse invokerResponse = client.execute(invokerRequest);
			MockHttpResponse mockResponse = httpFunction.send(mockRequest);

			// Obtain the potential invoker error
			String invokerEntity = EntityUtils.toString(invokerResponse.getEntity());

			// Obtain potential mock error
			String mockEntity = mockResponse.getEntity(null);
			String possibleMockError = "\n\n" + mockEntity;

			// Ensure expected status
			assertEquals(expectedHttpStatus.getStatusCode(), invokerResponse.getStatusLine().getStatusCode(),
					"Incorrect invoker status\n\n" + invokerEntity);
			assertEquals(expectedHttpStatus, mockResponse.getStatus(), "Incorrect mock status" + possibleMockError);

			// Confirm status line
			assertEquals(invokerResponse.getStatusLine().getProtocolVersion().toString(),
					mockResponse.getVersion().getName(), "Incorrect HTTP protocol");
			assertEquals(invokerResponse.getStatusLine().getReasonPhrase(), mockResponse.getStatus().getStatusMessage(),
					"Incorrrect status message");

			// Confirm headers
			for (String verifyHeaderName : verifyHeaderNames) {
				Header[] invokerHeaders = invokerResponse.getHeaders(verifyHeaderName);
				assertTrue(invokerHeaders.length > 0, "No invoker headers for " + verifyHeaderName);
				List<WritableHttpHeader> mockHeaders = mockResponse.getHeaders().stream()
						.filter((header) -> verifyHeaderName.equalsIgnoreCase(header.getName()))
						.collect(Collectors.toList());
				assertTrue(mockHeaders.size() > 0, "No mock headers for " + verifyHeaderName);
				assertEquals(invokerHeaders.length, mockHeaders.size(),
						"Incorrect number of headers for " + verifyHeaderName);
				for (int i = 0; i < invokerHeaders.length; i++) {
					Header invokerHeader = invokerHeaders[i];
					WritableHttpHeader mockHeader = mockHeaders.get(i);
					assertEquals(invokerHeader.getValue(), mockHeader.getValue(),
							"Incorrect header " + i + " for " + verifyHeaderName);
				}
			}

			// Confirm entity
			assertEquals(invokerEntity, mockEntity, "Incorrect response entity");

		} catch (Exception ex) {
			fail(ex);
		}
	}

}