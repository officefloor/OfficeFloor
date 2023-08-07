package net.officefloor.maven.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.server.http.HttpMethod;

/**
 * Abstract testing for Google Function application.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractGoogleFunctionTestCase {

	/**
	 * Response object to assert results.
	 */
	protected abstract static class Response {

		/**
		 * Asserts the response
		 * 
		 * @param statusCode       Expected response status code.
		 * @param entity           Expected entity.
		 * @param headerNameValues Expected header name/value pairs.
		 */
		public abstract void assertResponse(int statusCode, String entity, String... headerNameValues);
	}

	/**
	 * Undertakes the request.
	 * 
	 * @param method           HTTP method.
	 * @param path             Path.
	 * @param entity           Entity.
	 * @param headerNameValues Header name/value pairs.
	 * @return {@link Response}.
	 */
	protected abstract Response doRequest(HttpMethod method, String path, String entity, String... headerNameValues);

	/**
	 * Obtains the {@link Firestore}.
	 * 
	 * @return {@link Firestore}.
	 */
	protected abstract Firestore getFirestore();

	/**
	 * Ensure simple GET request.
	 */
	@Test
	public void get() {
		this.doRequest(HttpMethod.GET, "/get", null, "Accept", "*/*").assertResponse(200, "GET");
	}

	/**
	 * Ensure simple POST request.
	 */
	@Test
	public void post() {
		this.doRequest(HttpMethod.POST, "/post", "POST", "Accept", "*/*").assertResponse(200, "POST");
	}

	/**
	 * Ensure handle headers.
	 */
	@Test
	public void headers() {
		this.doRequest(HttpMethod.GET, "/headers", null, "one", "1", "two", "2").assertResponse(204, null, "one", "1",
				"two", "2");
	}

	/**
	 * Send Cookie.
	 */
	@Test
	public void cookie() {
		this.doRequest(HttpMethod.GET, "/cookie", null).assertResponse(204, null, "set-cookie", "ONE=1");
	}

	/**
	 * Buffer.
	 */
	@Test
	public void buffer() {
		this.doRequest(HttpMethod.GET, "/buffer", null, "Accept", "*/*").assertResponse(200, "BUFFER");
	}

	/**
	 * File resource.
	 */
	@Test
	public void file() {
		this.doRequest(HttpMethod.GET, "/file", null, "Accept", "*/*").assertResponse(200, "FILE");
	}

	/**
	 * Ensure handle async servicing.
	 */
	@Test
	public void async() {
		this.doRequest(HttpMethod.GET, "/async", null, "Accept", "*/*").assertResponse(200, "ASYNC");
	}

	/**
	 * Path parameters.
	 */
	@Test
	public void pathParameters() {
		this.doRequest(HttpMethod.GET, "/one/two", null, "Accept", "*/*").assertResponse(200, "one-two");
	}

	/**
	 * Query parameters.
	 */
	@Test
	public void queryParameters() {
		this.doRequest(HttpMethod.GET, "/query?one=1&two=2", null, "Accept", "*/*").assertResponse(200, "1-2");
	}

	/**
	 * JSON.
	 */
	@Test
	public void json() {
		this.doRequest(HttpMethod.GET, "/json", null, "Accept", "application/json").assertResponse(200,
				"{\"message\":\"TEST\"}");
	}

	/**
	 * Ensure can configure the environment.
	 */
	@Test
	public void env() {
		this.doRequest(HttpMethod.GET, "/env", null, "Accept", "*/*").assertResponse(200, "TEST_ENV");
	}

	/**
	 * DynamoDB.
	 */
	@Test
	public void firestore() {
		this.doRequest(HttpMethod.POST, "/firestore/one", "{\"message\":\"TEST\"}", "Content-Type", "application/json")
				.assertResponse(201, null);
		this.doRequest(HttpMethod.GET, "/firestore/one", null, "Accept", "application/json").assertResponse(200,
				"{\"message\":\"TEST\"}");

		try {

			// Ensure can also access dynamo for test setup/verification
			DocumentSnapshot document = this.getFirestore().collection(MessageEntity.class.getSimpleName())
					.document("one").get().get();

			String message = document.getString("message");
			assertEquals("TEST", message, "Incorrect direct access to DynamoDB for test verification");

		} catch (Exception ex) {
			fail(ex);
		}
	}

}
