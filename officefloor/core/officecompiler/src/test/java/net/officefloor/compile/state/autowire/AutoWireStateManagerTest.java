package net.officefloor.compile.state.autowire;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Tests the {@link AutoWireStateManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireStateManagerTest {

	/**
	 * Ensure can get auto-wired object.
	 */
	@Test
	public void ensureCanGetAutoWireObject() throws Throwable {

		// Compile and open the OfficeFloor
		MockObject object = new MockObject();
		Closure<String> officeNameCapture = new Closure<>();
		Closure<AutoWireStateManagerFactory> stateManagerFactory = new Closure<>();
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.getOfficeFloorCompiler().addAutoWireStateManagerVisitor((officeName, factory) -> {
			assertNull(officeNameCapture.value, "Should only be one Office");
			officeNameCapture.value = officeName;
			stateManagerFactory.value = factory;
		});
		compiler.office((context) -> {
			Singleton.load(context.getOfficeArchitect(), object);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			assertEquals(ApplicationOfficeFloorSource.OFFICE_NAME, officeNameCapture.value,
					"Ensure have correct Office");

			// Ensure able to obtain the mock object
			try (AutoWireStateManager state = stateManagerFactory.value.createAutoWireStateManager()) {

				// Ensure able to obtain the object
				MockObject retrieved = state.getObject(null, MockObject.class, 0);
				assertSame(object, retrieved, "Should retrieve the object");
			}
		}
	}

	/**
	 * Mock object for testing.
	 */
	private static class MockObject {
	}
}