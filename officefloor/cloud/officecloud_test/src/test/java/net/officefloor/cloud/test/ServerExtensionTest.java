package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Ensure can re-use the first registered {@link MockWoofServerExtension}.
 */
@ExtendWith(OfficeFloorCloudProviders.class)
public class ServerExtensionTest {

	/**
	 * <p>
	 * {@link MockWoofServer} to use.
	 * <p>
	 * Allows providing test specific configuration for {@link MockWoofServer}.
	 */
	private final @RegisterExtension MockWoofServerExtension server = new MockWoofServerExtension();

	/**
	 * Ensure uses the registered {@link MockWoofServerExtension}.
	 */
	@CloudTest
	public void ensureReuseServerExtension(MockWoofServer injectedServer) {
		assertSame(this.server, injectedServer, "Should pick up the server as registered extension");
	}
}