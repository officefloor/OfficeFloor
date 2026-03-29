package net.officefloor.server.google.function.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Ensure provides 204 on empty response entity.
 */
public class EmptyResponseTest {

	public final @RegisterExtension @Order(0) GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			NoContentHttpFunction.class);

	private final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	private static final @RegisterExtension HttpClientExtension client = new HttpClientExtension();

	public static class NoContentHttpFunction implements HttpFunction {
		@Override
		public void service(HttpRequest request, HttpResponse response) throws Exception {
			// No content should have appropriate status code
		}
	}

	/**
	 * Ensure can continue to request the {@link HttpFunction}.
	 */
	@Test
	public void requestOnHttpFunction() throws Exception {
		org.apache.http.HttpResponse response = client.execute(new HttpGet("http://localhost:7878"));
		assertEquals(204, response.getStatusLine().getStatusCode(), "Should be no content success");
	}

}
