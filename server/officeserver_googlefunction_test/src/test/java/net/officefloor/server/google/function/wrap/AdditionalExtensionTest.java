package net.officefloor.server.google.function.wrap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Ensure can add additional {@link OfficeFloorExtensionService} and
 * {@link OfficeExtensionService}.
 */
public class AdditionalExtensionTest {

	private boolean isOfficeFloorExtended = false;

	private boolean isOfficeExtended = false;

	public final @RegisterExtension @Order(0) MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension(
			TestHttpFunction.class).officeFloor((deployer, context) -> {
				AdditionalExtensionTest.this.isOfficeFloorExtended = true;
			}).office((architect, context) -> {
				AdditionalExtensionTest.this.isOfficeExtended = true;
			});

	public final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure can extend {@link OfficeFloor}.
	 */
	@Test
	public void officeFloor() {
		assertTrue(this.isOfficeFloorExtended, "Should extend OfficeFloor");
	}

	/**
	 * Ensure can extend {@link Office}.
	 */
	@Test
	public void office() {
		assertTrue(this.isOfficeExtended, "Should extend Office");
	}

}
