package net.officefloor.maven.test;

import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the SAM application.
 * 
 * @author Daniel Sagenschneider
 */
public class SamTest extends AbstractSamTestCase {

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
			this.response.assertResponse(statusCode, entity, headerNameValues);
		}
	}

}