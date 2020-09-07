package net.officefloor.woof.mock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.test.TestDependencyService;
import net.officefloor.woof.MockObject;

/**
 * Tests the {@link MockWoofServerRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServerRuleTest {

	/**
	 * {@link MockWoofServerRule} to test.
	 */
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this);

	private @Dependency MockObject fieldDependency;

	private MockObject setterDependency;

	public @Dependency void setDependency(MockObject dependency) {
		this.setterDependency = dependency;
	}

	/**
	 * Ensure the dependencies are available.
	 */
	@Test
	public void injectDependencies() {
		assertNotNull("Should inject field dependency", this.fieldDependency);
		assertSame("Incorrect setter dependency", this.fieldDependency, this.setterDependency);
	}

	/**
	 * Ensure able to service a request.
	 */
	@Test
	public void serviceRequest() {
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/template"));
		response.assertResponse(200, "TEMPLATE");
	}

	/**
	 * {@link MockTestDependency}.
	 */
	private static class MockTestDepedency {
	}

	/**
	 * Test dependency.
	 */
	private static MockTestDepedency DEPENDENCY = new MockTestDepedency();

	/**
	 * Ensure provide extra dependency.
	 */
	@ClassRule
	public static final MockTestDependencyService extraDependency = new MockTestDependencyService(DEPENDENCY);

	private @Dependency MockTestDepedency testDependency;

	/**
	 * Ensure can inject into dependency from {@link TestDependencyService}.
	 */
	@Test
	public void dependencyTestDependency() throws Throwable {
		assertSame(DEPENDENCY, this.testDependency, "Should inject extra test dependency");
	}

}