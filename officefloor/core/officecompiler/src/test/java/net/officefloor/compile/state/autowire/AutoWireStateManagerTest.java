package net.officefloor.compile.state.autowire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.Parameter;

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
	public void autoWireObject() throws Throwable {

		MockObject object = new MockObject();
		this.doTest((context) -> {
			Singleton.load(context.getOfficeArchitect(), object);
		}, (officeFloor, state) -> {

			// Ensure able to obtain the object
			MockObject retrieved = state.getObject(null, MockObject.class, 0);
			assertSame(object, retrieved, "Should retrieve the object");

			// Ensure able to load object
			Closure<MockObject> loadedCapture = new Closure<>();
			state.load(null, MockObject.class, (loaded, failure) -> {
				loadedCapture.value = loaded;
			});
			assertSame(object, loadedCapture.value, "should load the object");
		});
	}

	/**
	 * Ensure issue if unavailable object type.
	 */
	@Test
	public void unavailableObjectType() throws Throwable {
		this.doTest((context) -> {
			// No objects available
		}, (officeFloor, state) -> {

			// Ensure correct exception on unavailable object type
			try {
				state.getObject(null, MockObject.class, 0);
				fail("Should not be successful");
			} catch (UnknownObjectException ex) {
				assertEquals(
						"Unknown ManagedObject by binding " + MockObject.class.getName() + " has 0 auto wire matches",
						ex.getMessage(), "Incorrect cause");
			}
		});
	}

	/**
	 * Ensure able to obtain supplied {@link ManagedObject}.
	 */
	@Test
	public void suppliedManagedObject() throws Throwable {

		MockSupplierSource supplier = new MockSupplierSource();
		this.doTest((context) -> {
			context.getOfficeArchitect().addSupplier("SUPPLIER", supplier);
			context.addSection("SECTION", MockSection.class);
		}, (officeFloor, state) -> {

			// Ensure supplied managed object available
			Closure<MockManagedObjectSource> mosCapture = new Closure<>();
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.function", mosCapture);
			assertSame(supplier.mos, mosCapture.value, "Should have available supplied managed object");

			// Ensure supplied object available
			MockManagedObjectSource mos = state.getObject("SUPPLIED", MockManagedObjectSource.class, 0);
			assertSame(supplier.mos, mos, "Should retrieve supplied managed object");
		});
	}

	/**
	 * Ensure unused supplied {@link ManagedObject} is not available.
	 */
	@Test
	public void unusedSuppliedManagedObject() throws Throwable {

		MockSupplierSource supplier = new MockSupplierSource();
		this.doTest((context) -> {
			context.getOfficeArchitect().addSupplier("SUPPLIER", supplier);
		}, (officeFloor, state) -> {

			// Unused supplied managed object should not be available
			try {
				state.getObject("SUPPLIED", MockManagedObjectSource.class, 0);
				fail("Should not be successful");
			} catch (UnknownObjectException ex) {
				assertEquals("Unknown ManagedObject by binding SUPPLIED:" + MockManagedObjectSource.class.getName()
						+ " is not used SuppliedManagedObjectSource", ex.getMessage(), "Incorrect cause");
			}

			// Unused loaded supplied managed object
			try {
				state.load("SUPPLIED", MockManagedObjectSource.class,
						(object, failure) -> fail("Should not be invoked"));
				fail("Should not be successful");
			} catch (UnknownObjectException ex) {
				assertEquals("Unknown ManagedObject by binding SUPPLIED:" + MockManagedObjectSource.class.getName()
						+ " is not used SuppliedManagedObjectSource", ex.getMessage(), "Incorrect cause");
			}

		});
	}

	/**
	 * {@link FunctionalInterface} to validate the test.
	 */
	@FunctionalInterface
	private static interface Validate {
		void validate(OfficeFloor officeFloor, AutoWireStateManager state) throws Throwable;
	}

	/**
	 * Undertakes test.
	 * 
	 * @param officeSetup Sets up the {@link Office}.
	 * @param validate    Validates the {@link AutoWireStateManager}.
	 */
	private void doTest(CompileOfficeExtension officeSetup, Validate validate) throws Throwable {

		// Capture the auto wire details
		Closure<String> officeNameCapture = new Closure<>();
		Closure<AutoWireStateManagerFactory> stateManagerFactory = new Closure<>();
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.getOfficeFloorCompiler().addAutoWireStateManagerVisitor((officeName, factory) -> {
			assertNull(officeNameCapture.value, "Should only be one Office");
			officeNameCapture.value = officeName;
			stateManagerFactory.value = factory;
		});
		compiler.office(officeSetup);
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			assertEquals(ApplicationOfficeFloorSource.OFFICE_NAME, officeNameCapture.value,
					"Ensure have correct Office");

			// Ensure able to obtain the mock object
			try (AutoWireStateManager state = stateManagerFactory.value.createAutoWireStateManager()) {
				validate.validate(officeFloor, state);
			}
		}
	}

	/**
	 * Mock object for testing.
	 */
	private static class MockObject {
	}

	/**
	 * Mock section for testing.
	 */
	public static class MockSection {
		public void function(@Parameter Closure<MockManagedObjectSource> capture,
				@Qualified("SUPPLIED") MockManagedObjectSource dependency) {
			capture.value = dependency;
		}
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	@TestSource
	public static class MockSupplierSource extends AbstractSupplierSource {

		private final MockManagedObjectSource mos = new MockManagedObjectSource();

		/*
		 * ================== SupplierSource ====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			context.addManagedObjectSource("SUPPLIED", MockManagedObjectSource.class, this.mos);
		}

		@Override
		public void terminate() {
			// Nothing to terminate
		}
	}

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class MockManagedObjectSource extends AbstractManagedObjectSource<None, None>
			implements ManagedObject {

		/*
		 * ================ ManagedObjectSource =================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return this;
		}

		/*
		 * ================= ManagedObject =======================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}