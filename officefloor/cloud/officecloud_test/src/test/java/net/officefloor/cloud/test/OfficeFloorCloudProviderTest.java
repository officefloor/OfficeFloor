package net.officefloor.cloud.test;

import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link OfficeFloorCloudProviders}.
 */
@ExtendWith(OfficeFloorCloudProviders.class)
public class OfficeFloorCloudProviderTest {

	private @Dependency MockWoofServer server;
	
	@CloudTest
	public void testRunning() {
		
	}
	
}
