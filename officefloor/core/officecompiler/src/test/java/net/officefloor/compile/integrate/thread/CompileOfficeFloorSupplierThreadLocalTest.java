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
package net.officefloor.compile.integrate.thread;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.compile.impl.structure.SupplierThreadLocalNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.thread.OptionalThreadLocal;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling an {@link OfficeFloor} {@link SupplierThreadLocal}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloorSupplierThreadLocalTest extends AbstractCompileTestCase {

	/**
	 * Ensure issue if no {@link SupplierThreadLocal} for configured qualifier and
	 * object type.
	 */
	public void testUnknownSupplierThreadLocal() {

		// Record no supplier thread local for configuration
		this.issues.recordIssueRegex("UNKNOWN-" + ThreadLocalManagedObject.class.getName(),
				SupplierThreadLocalNodeImpl.class, "Supplier Thread Local not implemented.+");

		// Should compile
		this.compile(false);
	}

	/**
	 * Ensure issue if no {@link ManagedObject} linked to
	 * {@link SupplierThreadLocal}.
	 */
	public void testNoManagedObjectForSupplierThreadLocal() {

		// Add supplier thread local
		MockSupplierSource.addSupplierThreadLocal(ThreadLocalManagedObject.class);

		// Record no managed object for supplier
		this.record_init();
		this.issues.recordIssue(ThreadLocalManagedObject.class.getName(), SupplierThreadLocalNodeImpl.class,
				"Supplier Thread Local " + ThreadLocalManagedObject.class.getName() + " is not linked to a "
						+ BoundManagedObjectNode.class.getSimpleName());

		// Should compile
		this.compile(true);
	}

	/**
	 * Ensure can link {@link OfficeFloorSupplierThreadLocal} to
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testSupplierThreadLocalLinkToManagedObject() {

		// Add supplier thread local
		MockSupplierSource.addSupplierThreadLocal(ThreadLocalManagedObject.class);

		// Record no managed object for supplier
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, SuppliedManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		ThreadDependencyMappingBuilder mo = this.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT",
				"MANAGED_OBJECT");

		// Record obtaining thread local for supplier thread local
		this.recordReturn(mo, mo.getOptionalThreadLocal(), this.createMock(OptionalThreadLocal.class));

		// Should not compile
		this.compile(true);
	}

	/**
	 * Ensure can link {@link OfficeFloorSupplierThreadLocal} to
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testSupplierThreadLocalLinkToInputManagedObject() {

		// Add supplier thread local
		MockSupplierSource.addSupplierThreadLocal(ThreadLocalManagedObject.class);

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "INPUT");
		this.record_officeFloorBuilder_addManagedObject("INPUT_SOURCE", ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputManagingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		ThreadDependencyMappingBuilder input = this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT");
		inputManagingOffice.linkFlow(0, "SECTION.INPUT");
		office.setBoundInputManagedObject("INPUT", "INPUT_SOURCE");

		// Record obtaining thread local for supplier thread local
		this.recordReturn(input, input.getOptionalThreadLocal(), this.createMock(OptionalThreadLocal.class));

		// Compile the OfficeFloor
		this.compile(true);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MockSupplierSource.threadLocals.clear();
	}

	public static class ThreadLocalManagedObject {
	}

	public static class SuppliedManagedObject {
	}

	public static class ProcessManagedObject {

		@FlowInterface
		public static interface ProcessFlows {
			void doProcess();
		}

		ProcessFlows flows;
	}

	public static class ProcessSection {
		public void process() {
		}
	}

	@TestSource
	public static class MockSupplierSource extends AbstractSupplierSource {

		private static List<SupplierThreadLocalInstance> threadLocals = new ArrayList<>(1);

		private static void addSupplierThreadLocal(Class<?> objectType) {
			addSupplierThreadLocal(null, objectType);
		}

		private static void addSupplierThreadLocal(String qualifier, Class<?> objectType) {
			threadLocals.add(new SupplierThreadLocalInstance(qualifier, objectType));
		}

		/*
		 * ================= SupplierSource ============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Add the thread local
			for (SupplierThreadLocalInstance instance : threadLocals) {
				context.addSupplierThreadLocal(instance.qualifier, instance.objectType);
			}

			// Add the managed object source
			context.addManagedObjectSource(null, SuppliedManagedObject.class, new ClassManagedObjectSource())
					.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
							SuppliedManagedObject.class.getName());
		}
	}

	public static class SupplierThreadLocalInstance {

		private final String qualifier;

		private final Class<?> objectType;

		public SupplierThreadLocalInstance(String qualifier, Class<?> objectType) {
			this.qualifier = qualifier;
			this.objectType = objectType;
		}
	}

}