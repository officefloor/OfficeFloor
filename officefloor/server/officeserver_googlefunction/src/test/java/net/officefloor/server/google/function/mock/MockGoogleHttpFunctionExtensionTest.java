package net.officefloor.server.google.function.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.server.google.function.GoogleFunctionHttpServerImplementation;
import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.SimpleRequestTestHelper;
import net.officefloor.server.http.HttpServer;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests default will load {@link OfficeFloorHttpFunction}.
 */
public class MockGoogleHttpFunctionExtensionTest {

	private static final @RegisterExtension @Order(0) MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension()
			.officeFloor((deployer, context) -> {

				// Configure the HTTP server
				DeployedOfficeInput input = deployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME)
						.getDeployedOfficeInput(null, null);
				HttpServer server = new HttpServer(input, deployer, context);

				// Ensure using appropriate implementation
				assertEquals(GoogleFunctionHttpServerImplementation.class,
						server.getHttpServerImplementation().getClass(),
						"Should load " + GoogleFunctionHttpServerImplementation.class.getSimpleName());
			});

	private static final @RegisterExtension @Order(1) OfficeFloorExtension officeFloor = new OfficeFloorExtension();

	/**
	 * Ensure servicing with {@link OfficeFloorHttpFunction}.
	 */
	@Test
	public void request() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction.getMockHttpServer());
	}
}
