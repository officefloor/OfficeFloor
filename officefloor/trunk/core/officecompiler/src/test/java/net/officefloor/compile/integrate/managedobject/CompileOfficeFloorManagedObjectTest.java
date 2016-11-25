/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.integrate.managedobject;

import java.sql.Connection;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectTeamNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Tests compiling a {@link OfficeFloor} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloorManagedObjectTest extends
		AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 10,
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
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject(
				"MANAGED_OBJECT", "MANAGED_OBJECT"), null);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a supplied {@link ManagedObject} bound to
	 * {@link ProcessState}.
	 */
	public void testSuppliedManagedObject() {

		// Setup to provide managed object source instance
		MockSupplierSource.reset();
		final MockTypeManagedObjectSource mos = new MockTypeManagedObjectSource(
				Object.class);
		MockSupplierSource.managedObjectSource = mos;

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		this.issues.recordCaptureIssues(false); // load managed object type
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject(
				"MANAGED_OBJECT", "MANAGED_OBJECT"), null);

		// Record instance (as supplied)
		this.issues.recordCaptureIssues(false); // load managed object type
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", mos, 10, "MO_NAME", "MO_VALUE");

		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a supplied {@link ManagedObject} that provides its
	 * {@link Team}.
	 */
	public void testSuppliedManagedObjectWithProvidedTeam() {

		// Setup to provide managed object source instance (with team)
		MockSupplierSource.reset();
		final MockTypeManagedObjectSource mos = new MockTypeManagedObjectSource(
				Object.class);
		mos.addTeam("team");
		MockSupplierSource.managedObjectSource = mos;
		MockSupplierSource.wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				AutoWireTeam team = context.mapTeam("team",
						OnePersonTeamSource.class.getName());
				team.addProperty(
						OnePersonTeamSource.MAX_WAIT_TIME_PROPERTY_NAME, "200");
			}
		};

		// Record building the OfficeFloor
		this.issues.recordCaptureIssues(false); // load managed object type
		this.issues.recordCaptureIssues(false); // load managed object type
		this.record_init();
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");

		// Record binding supplied managed object for use
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject(
				"MANAGED_OBJECT", "MANAGED_OBJECT"), null);

		// Record supplied managed object
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", mos, 10, "MO_NAME", "MO_VALUE");

		// Record the supplied team
		this.record_officeFloorBuilder_addTeam("MANAGED_OBJECT_SOURCE-team",
				OnePersonTeamSource.class,
				OnePersonTeamSource.MAX_WAIT_TIME_PROPERTY_NAME, "200");
		this.record_officeBuilder_registerTeam("MANAGED_OBJECT_SOURCE-team",
				"MANAGED_OBJECT_SOURCE-team");

		// Record managing office
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("MANAGED_OBJECT_SOURCE");

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
			wirer = null;
		}

		/**
		 * {@link ManagedObjectSource}.
		 */
		public static ManagedObjectSource<?, ?> managedObjectSource = null;

		/**
		 * {@link ManagedObjectSourceWirer}.
		 */
		public static ManagedObjectSourceWirer wirer = null;

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
			AutoWireObject object = context.addManagedObject(
					managedObjectSource, wirer, new AutoWire("QUALIFIER",
							Connection.class.getName()));
			object.addProperty("MO_NAME", "MO_VALUE");
			object.setTimeout(10);
		}
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not linked.
	 */
	public void testManagedObjectWithDependencyNotLinked() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office managed object with dependency not linked
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("DEPENDENT",
				"DEPENDENT");
		this.issues
				.recordIssue(
						"dependency",
						ManagedObjectDependencyNodeImpl.class,
						"Managed Object Dependency dependency is not linked to a BoundManagedObjectNode");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not registered
	 * to the {@link Office}.
	 */
	public void testManagedObjectWithDependencyNotRegisteredToOffice() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");

		// Bind the managed object to the process of the office
		DependencyMappingBuilder mapper = this
				.record_officeBuilder_addProcessManagedObject("DEPENDENT",
						"DEPENDENT");

		// Map in the managed object dependency not registered to office
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		mapper.mapDependency(0, "SIMPLE");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency registered to
	 * the {@link Office}.
	 */
	public void testManagedObjectWithDependencyRegisteredToOffice() {

		// Record building the OfficeFloor
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this
				.record_officeBuilder_addProcessManagedObject("DEPENDENT",
						"DEPENDENT");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		mapper.mapDependency(0, "SIMPLE");

		// Add managed objects to OfficeFloor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
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

		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("INPUT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputManagingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT");
		inputManagingOffice.linkProcess(0, "SECTION.WORK", "INPUT");
		office.setBoundInputManagedObject("INPUT", "INPUT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("MO_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				InputDependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MO", "MO_SOURCE");
		DependencyMappingBuilder dependencies = this
				.record_officeBuilder_addProcessManagedObject("MO", "MO");
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
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		this.issues
				.recordIssue("doProcess", ManagedObjectFlowNodeImpl.class,
						"Managed Object Source Flow doProcess is not linked to a TaskNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link ManagedObjectFlow} but
	 * {@link ManagedObjectSource} is not {@link ProcessState} bound the
	 * {@link ManagingOffice}.
	 */
	public void testManagedObjectSourceFlowNotInputBoundToManagingOffice() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.issues
				.recordIssue(
						"MANAGED_OBJECT_SOURCE",
						ManagedObjectSourceNodeImpl.class,
						"Must provide input managed object for managed object source MANAGED_OBJECT_SOURCE as managed object source has flows/teams");
		managingOffice.linkProcess(0, "SECTION.WORK", "INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked
	 * {@link ProcessState} with a {@link Task}.
	 */
	public void testManagedObjectSourceFlowLinkedToTask() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		managingOffice.linkProcess(0, "SECTION.WORK", "INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if linking {@link ManagedObjectFlow} to {@link Task} that is
	 * not in the {@link ManagingOffice} for the {@link ManagedObjectSource}.
	 */
	public void testManagedObjectSourceFlowLinkedToTaskNotInManagingOffice() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		// Add the team and offices along with the task
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("MANAGING_OFFICE");
		this.record_officeFloorBuilder_addOffice("OFFICE_WITH_TASK",
				"OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);

		// Add the managed object source (flow linked to invalid office)
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("MANAGING_OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		this.issues
				.recordIssue(
						"MANAGED_OBJECT_SOURCE",
						ManagedObjectSourceNodeImpl.class,
						"Linked task of flow doProcess from managed object source MANAGED_OBJECT_SOURCE must be within the managing office");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectTeam} of {@link ManagedObjectSource}
	 * is not linked.
	 */
	public void testManagedObjectSourceTeamNotLinked() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", TeamManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("MANAGED_OBJECT_SOURCE");
		this.issues
				.recordIssue("MANAGED_OBJECT_SOURCE_TEAM",
						ManagedObjectTeamNodeImpl.class,
						"Managed Object Source Team MANAGED_OBJECT_SOURCE_TEAM is not linked to a TeamNode");

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
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", TeamManagedObject.class, 0);
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_registerTeam(
				"MANAGED_OBJECT_SOURCE.MANAGED_OBJECT_SOURCE_TEAM", "TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure able to link {@link OfficeFloorInputManagedObjectModel} to
	 * multiple {@link OfficeFloorManagedObjectSourceModel} instances along with
	 * specifying the bound {@link OfficeFloorManagedObjectSourceModel}.
	 */
	public void testInputManagedObjectLinkedToMultipleManagedObjectSources() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<Work, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE_A", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosA = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mosA.linkProcess(0, "SECTION.WORK", "INPUT");
		office.setBoundInputManagedObject("INPUT_MO", "MANAGED_OBJECT_SOURCE_A");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE_B", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosB = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mosB.linkProcess(0, "SECTION.WORK", "INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling an Input {@link ManagedObject} with dependencies linked
	 * via the {@link ManagedObjectSource}.
	 */
	public void testInputManagedObjectDependencyLinkedToManagedObject() {

		// Record the loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();

		// Register the office with the work for the input process flow
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<Work, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject("INPUT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> inputMos = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder inputDependencies = this
				.record_managingOfficeBuilder_setInputManagedObjectName("INPUT");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		inputDependencies.mapDependency(0, "SIMPLE");
		inputMos.linkProcess(0, "SECTION.WORK", "INPUT");
		office.setBoundInputManagedObject("INPUT", "INPUT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a
	 * {@link Dependency}.
	 */
	public static class DependencyManagedObject {

		@Dependency
		SimpleManagedObject dependency;
	}

	/**
	 * Class for {@link ClassWorkSource}.
	 */
	public static class ProcessWork {

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
	 * Class for {@link ClassManagedObjectSource} containing a
	 * {@link FlowInterface} and a {@link Dependency}.
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
	public static class TeamManagedObject extends
			AbstractManagedObjectSource<None, None> implements
			WorkFactory<Work>, TaskFactory<Work, None, None> {

		/*
		 * ================= AbstractManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {
			context.setObjectClass(Object.class);

			// Require a team
			ManagedObjectSourceContext<?> mosContext = context
					.getManagedObjectSourceContext();
			mosContext.addWork("WORK", this).addTask("TASK", this)
					.setTeam("MANAGED_OBJECT_SOURCE_TEAM");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			fail("Should not require obtaining managed object in compiling");
			return null;
		}

		/*
		 * =================== WorkFactory =================================
		 */

		@Override
		public Work createWork() {
			fail("Should not require work in compiling");
			return null;
		}

		/*
		 * ==================== TaskFactory ================================
		 */

		@Override
		public Task<Work, None, None> createTask(Work work) {
			fail("Should not require task in compiling");
			return null;
		}
	}

}