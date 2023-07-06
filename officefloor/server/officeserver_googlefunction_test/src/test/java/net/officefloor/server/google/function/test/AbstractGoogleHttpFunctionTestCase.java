package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.http.HttpClientTestUtil;

/**
 * Abstract functionality for testing {@link HttpFunction}.
 */
public class AbstractGoogleHttpFunctionTestCase {

	/**
	 * Undertakes the test.
	 * 
	 * @param port Port that {@link HttpFunction} is running on.
	 */
	protected void doTest(int port) throws Exception {
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {
			HttpResponse response = client.execute(new HttpGet("http://localhost:" + port));
			String entity = EntityUtils.toString(response.getEntity());
			assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
			assertEquals("TEST", entity, "Incorrect response");
		}
	}

}
