package net.officefloor.maven.test;

import org.junit.jupiter.api.Test;

import net.officefloor.server.http.HttpMethod;

/**
 * Abstract testing for SAM application.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSamTestCase {

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
	 * Ensure simple GET request.
	 */
	@Test
	public void get() {
		this.doRequest(HttpMethod.GET, "/get", null, "Accept", "text/plain").assertResponse(200, "GET");
	}

	/**
	 * Ensure simple POST request.
	 */
	@Test
	public void post() {
		this.doRequest(HttpMethod.POST, "/post", "POST", "Accept", "text/plain").assertResponse(200, "POST");
	}

}