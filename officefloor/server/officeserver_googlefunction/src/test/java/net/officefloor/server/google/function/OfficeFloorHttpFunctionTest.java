package net.officefloor.server.google.function;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.google.function.test.GoogleHttpFunctionExtension;
import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * Tests the {@link OfficeFloorHttpFunction}.
 */
public class OfficeFloorHttpFunctionTest {

	public static final @RegisterExtension GoogleHttpFunctionExtension httpFunction = new GoogleHttpFunctionExtension(
			OfficeFloorHttpFunction.class);

	/**
	 * Ensure loads and makes 
	 */
	@Disabled
	@Test
	public void request() {

		// Undertake request
		MockHttpResponse response = httpFunction
				.send(GoogleHttpFunctionExtension.mockJsonRequest(new MockDataTransferObject("MOCK REQUEST")));
		response.assertJson(200, new MockDataTransferObject("MOCK RESPONSE"));
	}

}
