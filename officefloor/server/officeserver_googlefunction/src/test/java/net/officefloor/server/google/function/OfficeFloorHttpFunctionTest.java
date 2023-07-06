package net.officefloor.server.google.function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;
import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * Tests the {@link OfficeFloorHttpFunction}.
 */
public class OfficeFloorHttpFunctionTest {

	public static final @RegisterExtension MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension(
			OfficeFloorHttpFunction.class);

	/**
	 * Ensure loads and makes 
	 */
	@Test
	public void request() {

		// Undertake request
		MockHttpResponse response = httpFunction
				.send(MockGoogleHttpFunctionExtension.mockJsonRequest(new MockDataTransferObject("MOCK REQUEST")));
		response.assertJson(200, new MockDataTransferObject("MOCK RESPONSE"));
	}

}
