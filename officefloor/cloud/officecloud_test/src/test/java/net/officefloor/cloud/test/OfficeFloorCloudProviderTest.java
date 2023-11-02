package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.cloud.test.app.MockDocument;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link OfficeFloorCloudProviders}.
 */
@ExtendWith(OfficeFloorCloudProviders.class)
public class OfficeFloorCloudProviderTest {

	private @Dependency MockWoofServer server;

	@CloudTest
	public void testRequest() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/hello"));
		MockDocument document = response.getJson(200, MockDocument.class);
		assertEquals("Hello from Cloud", document.getMessage(), "Incorrect message");
	}

}
