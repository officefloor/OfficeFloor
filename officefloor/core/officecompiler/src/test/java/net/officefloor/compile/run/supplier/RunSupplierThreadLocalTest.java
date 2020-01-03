package net.officefloor.compile.run.supplier;

import net.officefloor.compile.run.AbstractRunTestCase;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.plugin.section.clazz.Next;

/**
 * Tests the {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunSupplierThreadLocalTest extends AbstractRunTestCase {

	/**
	 * Ensure able to access {@link ManagedObject} via the
	 * {@link SupplierThreadLocal} from {@link OfficeSupplier}.
	 */
	public void testOfficeSupplierThreadLocal() throws Exception {

		// Open the OfficeFloor
		MockSupplierSource.isInstantiated = false;
		OfficeFloor officeFloor = this.open();

		// Obtain the method
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.threadLocal");

		// Invoke function and ensues the thread local access to dependency
		CompileSection.threadLocalObject = null;
		CompileSection.dependencyObject = null;
		CompileSection.dependency = null;
		MockSupplierSource.isTerminated = false;
		function.invokeProcess(null, null);
		assertNull("Should not have thread local (as managed object not loaded)", CompileSection.threadLocalObject);
		assertNotNull("Should have dependency object", CompileSection.dependency);
		assertSame("Should obtain via thread local (as managed object loaded)", CompileSection.dependency,
				CompileSection.dependencyObject);

		// Close the OfficeFloor and ensure supplier closed
		assertFalse("Should keep supplier until OfficeFloor closed", MockSupplierSource.isTerminated);
		officeFloor.closeOfficeFloor();
		assertTrue("Supplier should be terminated with close of OfficeFloor", MockSupplierSource.isTerminated);
	}

	public static class CompileSection {

		private static MockObject threadLocalObject;

		private static MockObject dependencyObject;

		private static MockObject dependency;

		@Next("dependency")
		public void threadLocal(MockManagedObjectSource mos) {

			// Available as supplier source managed object dependency
			threadLocalObject = mos.threadLocal.get();
		}

		public void dependency(MockObject object, MockManagedObjectSource mos) {
			dependency = object;

			// Available as supplier source managed object dependency
			dependencyObject = mos.threadLocal.get();
		}
	}

	public static class MockObject {
	}

	@TestSource
	public static class MockSupplierSource extends AbstractSupplierSource {

		private static boolean isTerminated;

		private static boolean isInstantiated;

		public MockSupplierSource() {
			assertFalse("Should only instantiate the supplier once", isInstantiated);
			isInstantiated = true;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Obtain the thread local
			SupplierThreadLocal<MockObject> threadLocal = context.addSupplierThreadLocal(null, MockObject.class);

			// Add the managed object source
			context.addManagedObjectSource(null, MockManagedObjectSource.class,
					new MockManagedObjectSource(threadLocal));
		}

		@Override
		public void terminate() {
			isTerminated = true;
		}
	}

	@TestSource
	public static class MockManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private final SupplierThreadLocal<MockObject> threadLocal;

		public MockManagedObjectSource(SupplierThreadLocal<MockObject> threadLocal) {
			this.threadLocal = threadLocal;
		}

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(this.getClass());
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return () -> this;
		}
	}

}