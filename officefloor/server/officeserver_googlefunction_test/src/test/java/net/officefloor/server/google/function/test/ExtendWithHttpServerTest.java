package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.google.function.AbstractExtendWithHttpServerTestCase;
import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.server.http.HttpServer;

/**
 * Ensure able to extend with {@link HttpServer}.
 */
public class ExtendWithHttpServerTest extends AbstractExtendWithHttpServerTestCase {

	private static final int HTTP_PORT = 17878;

	private static final int HTTPS_PORT = 17979;

	private static final @RegisterExtension @Order(0) GoogleHttpFunctionExtension httpFunction = extendWithHttpServer(
			new GoogleHttpFunctionExtension(TestHttpFunction.class)).httpPort(HTTP_PORT).httpsPort(HTTPS_PORT);

	private static final @RegisterExtension HttpClientExtension secureClient = new HttpClientExtension(true);

	/**
	 * Ensure can continue to request the {@link HttpFunction}.
	 */
	@Test
	public void requestOnHttpFunction() throws Exception {
		HttpResponse response = client.execute(new HttpGet("http://localhost:" + HTTP_PORT));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
		assertEquals("TEST", entity, "Incorrect response entity");
	}

	/**
	 * Ensure can continue to secure request the {@link HttpFunction}.
	 */
	@Test
	public void requestSecureOnHttpFunction() throws Exception {
		HttpResponse response = secureClient.execute(new HttpGet("https://localhost:" + HTTPS_PORT));
		String entity = EntityUtils.toString(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful: " + entity);
		assertEquals("TEST", entity, "Incorrect response entity");
	}

}
