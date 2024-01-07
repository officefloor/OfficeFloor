package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.cabinet.spi.CabinetManager;

/**
 * Ensures all the {@link CloudTestService} instances are registered.
 */
@ExtendWith(OfficeFloorCloudProviders.class)
public class RegisterCloudProvidersTest {

	private static final List<String> registeredServices = new LinkedList<>();

	private static final List<String> registeredOfficeStores = new LinkedList<>();

	@CloudTest
	public void registered(CloudTestService service, CabinetManager cabinetManager) {
		registeredServices.add(service.getCloudServiceName());

		AbstractMockCloudTestService cabinetService = (AbstractMockCloudTestService) cabinetManager;
		registeredOfficeStores.add(cabinetService.getCloudServiceName());
	}

	@AfterAll
	public static void verifyAllRegistered() {
		assertDependencies("registered services", registeredServices);
		assertDependencies("cabinet manager", registeredOfficeStores);
	}

	private static void assertDependencies(String prefix, List<String> dependencies) {
		assertEquals(2, dependencies.size(), "Incorrect " + prefix + " " + dependencies);
		assertEquals("MockOne", dependencies.get(0), "Incorrect " + prefix + " first cloud provider");
		assertEquals("MockTwo", dependencies.get(1), "Incorrect " + prefix + " second cloud provider");
	}
}
