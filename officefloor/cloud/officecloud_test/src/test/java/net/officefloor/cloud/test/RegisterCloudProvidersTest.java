package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Ensures all the {@link CloudTestService} instances are registered.
 */
@ExtendWith(OfficeFloorCloudProviders.class)
public class RegisterCloudProvidersTest {

	private static List<String> registeredServices = new LinkedList<>();

	@CloudTest
	public void registered(CloudTestService service) {
		registeredServices.add(service.getCloudServiceName());
	}

	@AfterAll
	public static void verifyAllRegistered() {
		assertEquals(2, registeredServices.size(), "Incorrect registered services " + registeredServices);
		assertEquals("MockOne", registeredServices.get(0), "Incorrect first cloud provider");
		assertEquals("MockTwo", registeredServices.get(1), "Incorrect second cloud provider");
	}
}
