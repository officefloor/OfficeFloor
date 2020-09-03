package net.officefloor.woof.mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.MockObject;

/**
 * Tests the {@link MockWoofServerExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerExtensionTest {

	/**
	 * {@link MockWoofServerExtension} to test.
	 */
	@RegisterExtension
	public static final MockWoofServerExtension server = new MockWoofServerExtension();

	private final MockObject constructorDependency;

	public MockWoofServerExtensionTest(MockObject dependency) {
		this.constructorDependency = dependency;
	}

	private @Dependency MockObject fieldDependency;

	private MockObject setterDependency;

	public @Dependency void setDependency(MockObject dependency) {
		this.setterDependency = dependency;
	}

	/**
	 * Ensure the dependencies are available.
	 */
	@Test
	public void injectDependencies(MockObject parameter) {
		assertNotNull(parameter, "Should inject parameter");
		assertSame(parameter, this.constructorDependency, "Should inject constructor dependency");
		assertSame(parameter, this.fieldDependency, "Should inject field dependency");
		assertSame(parameter, this.setterDependency, "Incorrect setter dependency");
	}

	/**
	 * Ensure able to service a request.
	 */
	@Test
	public void serviceRequest() {
		MockHttpResponse response = server.send(MockWoofServer.mockRequest("/template"));
		response.assertResponse(200, "TEMPLATE");
	}

}