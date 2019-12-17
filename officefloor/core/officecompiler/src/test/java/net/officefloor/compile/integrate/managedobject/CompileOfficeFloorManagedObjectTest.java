/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.integrate.managedobject;

import java.sql.Connection;

import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * Tests compiling a {@link OfficeFloor} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloorManagedObjectTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 10,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} bound to {@link ProcessState}.
	 */
	public void testProcessBoundManagedObject() {

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT"), null);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a supplied {@link ManagedObject} bound to
	 * {@link ProcessState}.
	 */
	public void testSuppliedManagedObjectSource() {

		// Setup to provide managed object source instance
		final String managedObjectSourceName = "SUPPLIER.QUALIFIER-" + Connection.class.getName()
				+ ".MANAGED_OBJECT_SOURCE";
		MockSupplierSource.reset();
		final MockTypeManagedObjectSource mos = new MockTypeManagedObjectSource(Object.class, managedObjectSourceName);
		MockSupplierSource.managedObjectSource = mos;

		// Record building the OfficeFloor
		this.record_supplierSetup();
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", managedObjectSourceName);
		this.recordReturn(office, office.addProcessManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT"), null);

		// Record instance (as supplied)
		this.record_officeFloorBuilder_addManagedObject(managedObjectSourceName, mos, 0, "MO_NAME", "MO_VALUE");

		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Mock {@link SupplierSource}.
	 */
	public static class MockSupplierSource extends AbstractSupplierSource {

		/**
		 * Resets for the next test.
		 */
		public static void reset() {
			managedObjectSource = null;
		}

		/**
		 * {@link ManagedObjectSource}.
		 */
		public static ManagedObjectSource<?, ?> managedObjectSource = null;

		/*
		 * ===================== SupplierSource ========================+
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Specification should not be required");
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Ensure the property is available
			String value = context.getProperty("SUPPLY_NAME");
			assertEquals("Incorrect property value", "SUPPLY_VALUE", value);

			// Supply the managed object source
			SuppliedManagedObjectSource source = context.addManagedObjectSource("QUALIFIER", Connection.class,
					managedObjectSource);
			source.addProperty("MO_NAME", "MO_VALUE");
		}

		@Override
		public void terminate() {
			// nothing to clean up
		}
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not linked.
	 */
	public void testManagedObjectWithDependencyNotLinked() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office managed object with dependency not linked
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("DEPENDENT", "DEPENDENT");
		this.issues.recordIssue("dependency", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency dependency is not linked to a BoundManagedObjectNode");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not registered to
	 * the {@link Office}.
	 */
	public void testManagedObjectWithDependencyNotRegisteredToOffice() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");

		// Bind the managed object to the process of the office
		DependencyMappingBuilder mapper = this.record_officeBuilder_addProcessManagedObject("DEPENDENT", "DEPENDENT");

		// Map in the managed object dependency not registered to office
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		mapper.mapDependency(0, "SIMPLE");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency registered to the
	 * {@link Office}.
	 */
	public void testManagedObjectWithDependencyRegisteredToOffice() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this.record_officeBuilder_addProcessManagedObject("DEPENDENT", "DEPENDENT");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		mapper.mapDependency(0, "SIMPLE");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency as an
	 * {@link InputManagedObject}.
	 */
	public void testManagedObjectDependencyLinkedToInputManagedObject() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("INPUT_SOURCE", ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputManagingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT");
		inputManagingOffice.linkFlow(0, "SECTION.INPUT");
		this.record_officeFloorBuilder_addManagedObject("MO_SOURCE", ClassManagedObjectSource.class, 0, "class.name",
				InputDependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MO", "MO_SOURCE");
		DependencyMappingBuilder dependencies = this.record_officeBuilder_addProcessManagedObject("MO", "MO");
		office.setBoundInputManagedObject("INPUT", "INPUT_SOURCE");
		dependencies.mapDependency(0, "INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectFlow} of
	 * {@link OfficeFloorManagedObjectSource} is not linked.
	 */
	public void testManagedObjectSourceFlowNotLinked() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		this.issues.recordIssue("doProcess", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow doProcess is not linked to a ManagedFunctionNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link ManagedObjectFlow} but {@link ManagedObjectSource} is
	 * not {@link ProcessState} bound the {@link ManagingOffice}.
	 */
	public void testManagedObjectSourceFlowNotInputBoundToManagingOffice() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.issues.recordIssue("MANAGED_OBJECT_SOURCE", ManagedObjectSourceNodeImpl.class,
				"Must provide input managed object for managed object source MANAGED_OBJECT_SOURCE as managed object source has flows/teams");
		managingOffice.linkFlow(0, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked {@link ProcessState}
	 * with a {@link ManagedFunction}.
	 */
	public void testManagedObjectSourceFlowLinkedToFunction() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		managingOffice.linkFlow(0, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if linking {@link ManagedObjectFlow} to {@link ManagedFunction}
	 * that is not in the {@link ManagingOffice} for the
	 * {@link ManagedObjectSource}.
	 */
	public void testManagedObjectSourceFlowLinkedToFunctionNotInManagingOffice() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		// Add the team and offices along with the function
		this.record_officeFloorBuilder_addOffice("MANAGING_OFFICE");
		this.record_officeFloorBuilder_addOffice("OFFICE_WITH_FUNCTION");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);

		// Add the managed object source (flow linked to invalid office)
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("MANAGING_OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		this.issues.recordIssue("MANAGED_OBJECT_SOURCE", ManagedObjectSourceNodeImpl.class,
				"Linked function of flow doProcess from managed object source MANAGED_OBJECT_SOURCE must be within the managing office");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure no need to link {@link ManagedObjectTeam} of
	 * {@link ManagedObjectSource}.
	 */
	public void testManagedObjectSourceTeamNotLinked() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", TeamManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		// No issue, as will just be undertaken by any team

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to link in {@link Team} required by a
	 * {@link ManagedObjectSource}.
	 */
	public void testManagedObjectSourceTeamLinked() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", TeamManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_registerTeam("MANAGED_OBJECT_SOURCE.MANAGED_OBJECT_SOURCE_TEAM", "TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link OfficeFloorInputManagedObject} linked to
	 * {@link ManagedObjectSource} that does not input into the application.
	 */
	public void testInputManagedObjectLinkedToNonInputManagedObjectSource() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.issues.recordIssue("MANAGED_OBJECT_SOURCE", ManagedObjectSourceNodeImpl.class,
				"Attempting to configure managed object source MANAGED_OBJECT_SOURCE as input managed object, when does not input into the application");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link OfficeFloorInputManagedObject} type is not compatible
	 * with a linked {@link ManagedObjectSource}.
	 */
	public void testInputManagedObjectTypeNotCompatibleToManagedObjectSourceType() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.issues.recordIssue("MANAGED_OBJECT_SOURCE", ManagedObjectSourceNodeImpl.class,
				"Managed Object Source MANAGED_OBJECT_SOURCE object " + ProcessManagedObject.class.getName()
						+ " is not compatible with input managed object INPUT_MO (input object type "
						+ String.class.getName() + ")");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to link {@link OfficeFloorInputManagedObjectModel} to multiple
	 * {@link OfficeFloorManagedObjectSourceModel} instances along with specifying
	 * the bound {@link OfficeFloorManagedObjectSourceModel}.
	 */
	public void testInputManagedObjectLinkedToMultipleManagedObjectSources() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE_A", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosA = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mosA.linkFlow(0, "SECTION.INPUT");
		office.setBoundInputManagedObject("INPUT_MO", "MANAGED_OBJECT_SOURCE_A");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE_B", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosB = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mosB.linkFlow(0, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling an Input {@link ManagedObject} with dependencies linked via
	 * the {@link ManagedObjectSource}.
	 */
	public void testInputManagedObjectDependencyLinkedToManagedObject() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		// Register the office with the namespace for the input process flow
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "INPUT");
		function.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("INPUT_SOURCE", ClassManagedObjectSource.class, 0, "class.name",
				InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputMos = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputDependencies = this
				.record_managingOfficeBuilder_setInputManagedObjectName("INPUT");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		inputDependencies.mapDependency(0, "SIMPLE");
		inputMos.linkFlow(0, "SECTION.INPUT");
		office.setBoundInputManagedObject("INPUT", "INPUT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling an {@link ManagedObjectFunctionDependency} linked via the
	 * {@link ManagedObjectSource}.
	 */
	public void testManagedObjectFunctionDependencyLinkedToManagedObject() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the managed object function dependency
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE", FunctionDependencyManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Map the function dependency
		this.record_managingOfficeBuilder_mapFunctionDependency("DEPENDENCY", "SIMPLE");

		// Register remaining
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests to ensure can link {@link ManagedObject} with
	 * {@link ManagedObjectPool}.
	 */
	public void testManagedObjectPooling() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the managed object pool for managed object
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a {@link Dependency}.
	 */
	public static class DependencyManagedObject {

		@Dependency
		SimpleManagedObject dependency;
	}

	/**
	 * Class for {@link ClassManagedFunctionSource}.
	 */
	public static class ProcessClass {

		public void process(Integer parameter) {
		}
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a
	 * {@link FlowInterface}.
	 */
	public static class ProcessManagedObject {

		@FlowInterface
		public static interface Processes {
			void doProcess(Integer parameter);
		}

		Processes processes;
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing an input
	 * {@link Dependency}.
	 */
	public static class InputDependencyManagedObject {

		@Dependency
		ProcessManagedObject dependency;
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a {@link FlowInterface}
	 * and a {@link Dependency}.
	 */
	public static class InputManagedObject {

		@FlowInterface
		public static interface Processes {
			void doProcess(String parameter);
		}

		@Dependency
		SimpleManagedObject dependency;

		Processes processes;
	}

	/**
	 * {@link ManagedObjectSource} requiring a {@link Team}.
	 */
	@TestSource
	public static class TeamManagedObject extends AbstractManagedObjectSource<None, None>
			implements ManagedFunctionFactory<None, None> {

		/*
		 * ================= AbstractManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);

			// Require a team
			ManagedObjectSourceContext<?> mosContext = context.getManagedObjectSourceContext();
			mosContext.addManagedFunction("FUNCTION", this).setResponsibleTeam("MANAGED_OBJECT_SOURCE_TEAM");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require obtaining managed object in compiling");
			return null;
		}

		/*
		 * ==================== ManagedFunctionFactory ====================
		 */

		@Override
		public ManagedFunction<None, None> createManagedFunction() {
			fail("Should not require function in compiling");
			return null;
		}
	}

	/**
	 * {@link ManagedObjectSource} requiring a
	 * {@link ManagedObjectFunctionDependency}.
	 */
	@TestSource
	public static class FunctionDependencyManagedObject extends AbstractManagedObjectSource<None, None> {

		/*
		 * ================ ManagedObjectSource ====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setObjectClass(Object.class);
			context.getManagedObjectSourceContext().addFunctionDependency("DEPENDENCY", String.class);
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require obtaining managed object in compiling");
			return null;
		}
	}

}