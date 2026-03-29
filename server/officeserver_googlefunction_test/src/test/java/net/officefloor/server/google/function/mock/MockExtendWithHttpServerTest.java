package net.officefloor.server.google.function.mock;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.server.google.function.AbstractExtendWithHttpServerTestCase;
import net.officefloor.server.google.function.wrap.TestHttpFunction;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;

/**
 * Ensure able to extend with {@link HttpServer}.
 */
public class MockExtendWithHttpServerTest extends AbstractExtendWithHttpServerTestCase {

	private static final @RegisterExtension @Order(0) MockGoogleHttpFunctionExtension httpFunction = extendWithHttpServer(
			new MockGoogleHttpFunctionExtension(TestHttpFunction.class));

	/**
	 * Ensure can continue to request the {@link HttpFunction}.
	 */
	@Test
	public void requestOnHttpFunction() throws Exception {
		MockHttpResponse response = httpFunction.send(MockHttpServer.mockRequest());
		response.assertResponse(200, "TEST");
	}

}
