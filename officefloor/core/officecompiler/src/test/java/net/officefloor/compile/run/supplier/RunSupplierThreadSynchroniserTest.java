/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.run.supplier;

import static org.junit.Assert.assertNotEquals;

import net.officefloor.compile.run.AbstractRunTestCase;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.plugin.section.clazz.NextFunction;

/**
 * Ensures the {@link SupplierSource} can register an
 * {@link ThreadSynchroniser}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunSupplierThreadSynchroniserTest extends AbstractRunTestCase {

	/**
	 * Tests {@link SupplierSource} registering {@link ThreadSynchroniser}.
	 */
	public void testSupplierThreadSynchroniser() throws Throwable {

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.open();

		// Trigger function
		CompileSection.threadLocalValue = null;
		CompileSection.threadLocalThread = null;
		CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.threadLocal", null);

		// Ensure synchronised between threads
		assertEquals("Should synchronise thread locals", "MOCK", CompileSection.threadLocalValue);
	}

	public static class TeamMarker {
	}

	public static class CompileSection {

		private static volatile String threadLocalValue;

		private static volatile Thread threadLocalThread;

		@NextFunction("differentTeam")
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

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Add the managed object source
			MockManagedObjectSource mos = new MockManagedObjectSource();
			context.addManagedObjectSource(null, MockManagedObjectSource.class, mos);

			// Add the thread synchroniser
			context.addThreadSynchroniser(() -> new MockThreadSynchroniser(mos));
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