package net.officefloor.maven.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the SAM application.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(DynamoDbExtension.class)
public class SamTest extends AbstractSamTestCase {

	// TODO REMOVE
	static {
		System.out.println("---------------- " + SamTest.class.getSimpleName() + " ----------------");
		System.out.println("PROCESS " + ProcessHandle.current().pid());
		final String AWS_SAM_LOCAL = "AWS_SAM_LOCAL";
		System.out.println(AWS_SAM_LOCAL + " = " + System.getenv(AWS_SAM_LOCAL));
		System.out.println("---------------------------------------------");
	}

	public @RegisterExtension final MockWoofServerExtension server = new MockWoofServerExtension();

	/*
	 * ================== AbstractSamTestCase =================
	 */

	@Override
	protected Response doRequest(HttpMethod method, String path, String entity, String... headerNameValues) {
		MockHttpRequestBuilder request = MockWoofServer.mockRequest(path).method(method).entity(entity);
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			request.header(name, value);
		}
		return new MockResponse(this.server.send(request));
	}

	/**
	 * {@link Response} to wrap {@link MockWoofResponse}.
	 */
	private static class MockResponse extends Response {

		/**
		 * {@link MockWoofResponse}.
		 */
		private final MockWoofResponse response;

		/**
		 * Instantiate.
		 * 
		 * @param response {@link MockWoofResponse}.
		 */
		private MockResponse(MockWoofResponse response) {
			this.response = response;
		}

		/*
		 * =================== Response =======================
		 */

		@Override
		public void assertResponse(int statusCode, String entity, String... headerNameValues) {

			// Determine if cookie
			String cookieName = null;
			String cookieValue = null;
			if ((headerNameValues.length > 0) && ("set-cookie".equals(headerNameValues[0]))) {

				// Obtain the cookie values
				String[] cookieParts = headerNameValues[1].split("=");
				cookieName = cookieParts[0];
				cookieValue = cookieParts[1];

				// Clear cookies from headers
				headerNameValues = new String[0];
			}

			// Assert the response
			this.response.assertResponse(statusCode, entity == null ? "" : entity, headerNameValues);

			// Assert the cookie
			if (cookieName != null) {
				this.response.assertCookie(new WritableHttpCookie(cookieName, cookieValue, null));
			}
		}
	}

}