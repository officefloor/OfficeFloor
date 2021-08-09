/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.run.supplier;

import net.officefloor.compile.run.AbstractRunTestCase;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompletionContext;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Next;

/**
 * Tests the {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunSupplierThreadLocalTest extends AbstractRunTestCase {

	/**
	 * Instantiate to use same configuration file for all tests.
	 */
	public RunSupplierThreadLocalTest() {
		this.setSameConfigurationForAllTests(true);
	}

	/**
	 * Ensure able to access {@link ManagedObject} via the
	 * {@link SupplierThreadLocal} from {@link OfficeSupplier}.
	 */
	public void testOfficeSupplierThreadLocalViaContext() throws Exception {
		this.doOfficeSupplierThreadLocalTest(AddThreadLocalLocation.CONTEXT);
	}

	/**
	 * Ensure other sources are able to register {@link SupplierThreadLocal}
	 * instances from the {@link OfficeSupplier}.
	 */
	public void testOfficeSupplierThreadLocalViaFunctionality() throws Exception {
		this.doOfficeSupplierThreadLocalTest(AddThreadLocalLocation.FUNCTIONALITY);
	}

	/**
	 * Ensure able to access {@link ManagedObject} via {@link SupplierThreadLocal}
	 * added in {@link SupplierCompileCompletion}.
	 */
	public void testOfficeSupplierThreadLocalViaCompletion() throws Exception {
		this.doOfficeSupplierThreadLocalTest(AddThreadLocalLocation.COMPLETION);
	}

	/**
	 * Undertakes {@link SupplierThreadLocal} test.
	 */
	private void doOfficeSupplierThreadLocalTest(AddThreadLocalLocation location) throws Exception {

		// Specify add location of thread local
		MockSupplierSource.addThreadLocalLocation = location;

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

	private static enum AddThreadLocalLocation {
		CONTEXT, FUNCTIONALITY, COMPLETION
	}

	public static class MockClassSectionSource extends ClassSectionSource {

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			super.sourceSection(designer, context);

			// Determine if register thread local
			if (!context.isLoadingType()) {
				switch (MockSupplierSource.addThreadLocalLocation) {
				case FUNCTIONALITY:
					MockSupplierSource.setup.run();
					break;

				default:
					// Do nothing, as setup in supplier
					break;
				}
			}
		}
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

		private static AddThreadLocalLocation addThreadLocalLocation;

		private static boolean isTerminated;

		private static boolean isInstantiated;

		private static Runnable setup;

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

			// Add supplier functionality
			Closure<SupplierThreadLocal<MockObject>> threadLocal = new Closure<>();
			SupplierCompileCompletion setupThreadLocal = (completion) -> {
				threadLocal.value = completion.addSupplierThreadLocal(null, MockObject.class);
			};
			SupplierCompileCompletion setupManagedObject = (completion) -> {
				SupplierThreadLocal<MockObject> supplierThreadLocal = threadLocal.value;
				assertNotNull("Should have supplier thread local", supplierThreadLocal);
				completion.addManagedObjectSource(null, MockManagedObjectSource.class,
						new MockManagedObjectSource(supplierThreadLocal));
			};
			
			// Obtain the completion context
			SupplierCompletionContext completionContext = SupplierLoaderUtil.getSupplierCompletionContext(context);

			// Allow functionality to setup thread local
			setup = () -> {
				try {
					setupThreadLocal.complete(completionContext);
				} catch (Exception ex) {
					throw fail(ex);
				}
			};

			// Undertake setup based on location
			switch (addThreadLocalLocation) {
			case CONTEXT:
				// Setup immediately
				setupThreadLocal.complete(completionContext);
				setupManagedObject.complete(completionContext);
				break;

			case FUNCTIONALITY:
				// Thread local done in section functionality
				context.addCompileCompletion(setupManagedObject);
				break;

			case COMPLETION:
				context.addCompileCompletion(setupThreadLocal);
				context.addCompileCompletion(setupManagedObject);
				break;
			}
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
