/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.run.supplier;

import static org.junit.Assert.assertNotEquals;

import net.officefloor.compile.run.AbstractRunTestCase;
import net.officefloor.compile.spi.supplier.source.SupplierCompileCompletion;
import net.officefloor.compile.spi.supplier.source.SupplierCompletionContext;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.supplier.SupplierLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.plugin.section.clazz.Next;

/**
 * Ensures the {@link SupplierSource} can register an
 * {@link ThreadSynchroniser}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunSupplierThreadSynchroniserTest extends AbstractRunTestCase {

	/**
	 * Instantiate to use same configuration file for all tests.
	 */
	public RunSupplierThreadSynchroniserTest() {
		this.setSameConfigurationForAllTests(true);
	}

	/**
	 * Tests {@link SupplierSource} registering {@link ThreadSynchroniser}.
	 */
	public void testSupplierThreadSynchroniserViaContext() throws Throwable {
		this.doSupplierThreadSynchroniserTest(AddThreadSynchroniserLocation.CONTEXT);
	}

	/**
	 * Tests {@link SupplierSource} registering {@link ThreadSynchroniser}.
	 */
	public void testSupplierThreadSynchroniserViaCompletion() throws Throwable {
		this.doSupplierThreadSynchroniserTest(AddThreadSynchroniserLocation.COMPLETION);
	}

	/**
	 * Undertakes the {@link ThreadSynchroniser} test.
	 */
	private void doSupplierThreadSynchroniserTest(AddThreadSynchroniserLocation location) throws Throwable {

		// Specify load location
		MockSupplierSource.addThreadSynchroniserLocation = location;

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.open();

		// Trigger function
		CompileSection.threadLocalValue = null;
		CompileSection.threadLocalThread = null;
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.threadLocal", null);

		// Ensure synchronised between threads
		assertEquals("Should synchronise thread locals", "MOCK", CompileSection.threadLocalValue);
	}

	private static enum AddThreadSynchroniserLocation {
		CONTEXT, COMPLETION
	}

	public static class TeamMarker {
	}

	public static class CompileSection {

		private static volatile String threadLocalValue;

		private static volatile Thread threadLocalThread;

		@Next("differentTeam")
		public void threadLocal(MockManagedObjectSource mos) {
			threadLocalThread = Thread.currentThread();

			// Load the thread local value
			mos.threadLocal.set("MOCK");
		}

		public void differentTeam(MockManagedObjectSource mos, TeamMarker marker) {
			assertNotNull("Should have thread local thread", threadLocalThread);
			assertNotEquals("Should be different thread", threadLocalThread, Thread.currentThread());

			// Capture the thread local value (synchronised to this thread)
			threadLocalValue = mos.threadLocal.get();
		}
	}

	@TestSource
	public static class MockSupplierSource extends AbstractSupplierSource {

		private static AddThreadSynchroniserLocation addThreadSynchroniserLocation;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Setup of thread synchroniser
			SupplierCompileCompletion setup = (completion) -> {

				// Add the managed object source
				MockManagedObjectSource mos = new MockManagedObjectSource();
				context.addManagedObjectSource(null, MockManagedObjectSource.class, mos);

				// Add the thread synchroniser
				context.addThreadSynchroniser(() -> new MockThreadSynchroniser(mos));
			};

			// Load thread synchroniser at appropriate location
			switch (addThreadSynchroniserLocation) {
			case CONTEXT:
				// Load immediately in this context
				SupplierCompletionContext completionContext = SupplierLoaderUtil.getSupplierCompletionContext(context);
				setup.complete(completionContext);
				break;

			case COMPLETION:
				// Setup at compile completion
				context.addCompileCompletion(setup);
				break;
			}
		}

		@Override
		public void terminate() {
			// nothing to terminate
		}
	}

	@TestSource
	public static class MockManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		private final ThreadLocal<String> threadLocal = new ThreadLocal<>();

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

	private static class MockThreadSynchroniser implements ThreadSynchroniser {

		private final MockManagedObjectSource mos;

		private String value = null;

		private MockThreadSynchroniser(MockManagedObjectSource mos) {
			this.mos = mos;
		}

		@Override
		public void suspendThread() {
			this.value = this.mos.threadLocal.get();
			this.mos.threadLocal.set(null);
		}

		@Override
		public void resumeThread() {
			this.mos.threadLocal.set(this.value);
		}
	}

}
