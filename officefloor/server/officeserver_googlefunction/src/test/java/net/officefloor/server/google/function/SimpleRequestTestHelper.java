package net.officefloor.server.google.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Test helper to send simple requests.
 */
public class SimpleRequestTestHelper {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * All access vis static methods.
	 */
	private SimpleRequestTestHelper() {
	}

	/**
	 * Ensure can send request via socket.
	 */
	public static void assertRequest() {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Undertake request
			HttpPost request = new HttpPost("http://localhost:7878");
			String requestEntity = mapper.writeValueAsString(new MockDataTransferObject("MOCK REQUEST"));
			request.setEntity(new StringEntity(requestEntity));
			HttpResponse response = client.execute(request);

			// Ensure appropriate response
			String responseEntity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + responseEntity);
			MockDataTransferObject responseEntityObject = mapper.readValue(responseEntity,
					MockDataTransferObject.class);
			assertEquals("MOCK RESPONSE", responseEntityObject.getText(), "Incorrect response");

		} catch (Exception ex) {
			fail(ex);
			throw new IllegalStateException("fail should propagate failure");
		}
	}

	/**
	 * Asserts simple request to {@link MockHttpServer}.
	 * 
	 * @param mockHttpServer {@link MockHttpServer}.
	 */
	public static void assertMockRequest(MockHttpServer mockHttpServer) {
		MockHttpResponse response = mockHttpServer
				.send(MockGoogleHttpFunctionExtension.mockJsonRequest(new MockDataTransferObject("MOCK REQUEST")));
		response.assertJson(200, new MockDataTransferObject("MOCK RESPONSE"));
	}
}
