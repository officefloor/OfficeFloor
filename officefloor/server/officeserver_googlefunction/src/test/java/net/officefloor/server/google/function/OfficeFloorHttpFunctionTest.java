package net.officefloor.server.google.function;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.google.function.mock.MockGoogleHttpFunctionExtension;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.test.ExternalServerRunner;

/**
 * Tests the {@link OfficeFloorHttpFunction}.
 */
public class OfficeFloorHttpFunctionTest {

	public static final @RegisterExtension MockGoogleHttpFunctionExtension httpFunction = new MockGoogleHttpFunctionExtension(
			OfficeFloorHttpFunction.class);

	@BeforeAll
	private static void startGoogleFunction() {
		try {

			// Start servicing
			ExternalServerRunner.startExternalServer("SERVICE", "service", null, (architect, context) -> {

				// Provide servicing of input
				architect.enableAutoWireObjects();
				architect.addOfficeSection("SERVICE", ClassSectionSource.class.getName(), Servicer.class.getName());

			}, () -> {
				// Start the server
				OfficeFloorHttpFunction.open();
			});

		} catch (Exception ex) {
			fail(ex);
		}
	}

	public static class Servicer {
		public void service(ServerHttpConnection connection) throws Exception {
			connection.getResponse().getEntityWriter().write("TODO validate response");
		}
	}

	/**
	 * Ensure can request.
	 */
	@Test
	public void simpleRequest() {
		SimpleRequestTestHelper.assertMockRequest(httpFunction);
	}

}
