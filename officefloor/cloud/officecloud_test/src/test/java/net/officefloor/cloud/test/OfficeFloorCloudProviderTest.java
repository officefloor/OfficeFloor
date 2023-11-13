package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cloud.test.app.MockDocument;
import net.officefloor.cloud.test.app.MockRepository;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link OfficeFloorCloudProviders}.
 */
@ExtendWith(OfficeFloorCloudProviders.class)
public class OfficeFloorCloudProviderTest {

	private @Dependency MockWoofServer server;

	private @Dependency MockRepository respository;

	private @Dependency CabinetManager cabinetManager;

	@CloudTest
	public void request() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/hello"));
		MockDocument document = response.getJson(200, MockDocument.class);
		assertEquals("Hello from Cloud", document.getMessage(), "Incorrect message");
	}

	@CloudTest
	public void cabinet() {
		MockDocument document = new MockDocument("Test");
		this.respository.store(document);
		MockDocument retrieved = this.respository.getMockDocumentByKey(document.getKey());
		assertSame(document, retrieved, "Should be same document");
	}

	@CloudTest
	public void retrieve() throws Exception {
		MockDocument document = new MockDocument("Test");
		this.respository.store(document);
		this.cabinetManager.flush();

		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/retrieve/" + document.getKey()));
		MockDocument retrieved = response.getJson(200, MockDocument.class);
		assertEquals(document.getMessage(), retrieved.getMessage(), "Incorrect retrieved document");
	}

	@CloudTest
	public void store() throws Exception {
		MockDocument document = new MockDocument("Test");
		MockWoofResponse response = this.server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/store", document));
		String key = response.getEntity(null);

		MockDocument stored = this.respository.getMockDocumentByKey(key);
		assertEquals(document.getMessage(), stored.getMessage(), "Incorrect stored document");
	}

}
