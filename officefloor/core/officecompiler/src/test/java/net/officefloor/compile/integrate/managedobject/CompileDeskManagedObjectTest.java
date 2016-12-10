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

import net.officefloor.compile.impl.structure.ManagedObjectDependencyNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectFlowNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Tests compiling a {@link DeskModel} {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileDeskManagedObjectTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Capture issues for managed object type
		this.issues.recordCaptureIssues(false);

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
		office.registerManagedObjectSource("OFFICE.DESK.MANAGED_OBJECT",
				"OFFICE.DESK.MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject(
				"OFFICE.DESK.MANAGED_OBJECT", "OFFICE.DESK.MANAGED_OBJECT"),
				null);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Capture issues for managed object type
		this.issues.recordCaptureIssues(false);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not linked.
	 */
	public void testManagedObjectWithDependencyNotLinked() {

		// Record issue creating section type
		CompilerIssue[] issues = this.issues.recordCaptureIssues(true);
		this.issues
				.recordIssue("dependency",
						ManagedObjectDependencyNodeImpl.class,
						"Managed Object Dependency dependency is not linked to a DependentObjectNode");
		this.issues.recordIssue("OFFICE", OfficeNodeImpl.class,
				"Failure loading OfficeSectionType from source DESK", issues);

		// Compile the OfficeFloor
		this.compile(false);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency in
	 * {@link DeskModel}.
	 */
	public void testManagedObjectWithDependencyInDesk() {

		// Capture issues for section type
		this.issues.recordCaptureIssues(false);

		// Record building the office
		this.record_init();

		// Register the office linked managed objects with the office
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");

		// Add managed objects to office
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK.DEPENDENT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DESK.DEPENDENT",
				"OFFICE.DESK.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this
				.record_officeBuilder_addProcessManagedObject(
						"OFFICE.DESK.DEPENDENT", "OFFICE.DESK.DEPENDENT");
		office.registerManagedObjectSource("OFFICE.DESK.SIMPLE",
				"OFFICE.DESK.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.DESK.SIMPLE",
				"OFFICE.DESK.SIMPLE");
		mapper.mapDependency(0, "OFFICE.DESK.SIMPLE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK.SIMPLE_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency outside
	 * {@link DeskModel}.
	 */
	public void testManagedObjectWithDependencyOutSideDesk() {

		// Capture issues for section type
		this.issues.recordCaptureIssues(false);

		// Record building the office
		this.record_init();

		// Register the section linked managed objects with the office
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");

		// Add first managed object
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SIMPLE",
				"OFFICE.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE",
				"OFFICE.SIMPLE");

		// Add second managed object
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK.DEPENDENT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.DESK.DEPENDENT",
				"OFFICE.DESK.DEPENDENT_SOURCE");

		// Map dependency between managed objects
		DependencyMappingBuilder mapper = this
				.record_officeBuilder_addProcessManagedObject(
						"OFFICE.DESK.DEPENDENT", "OFFICE.DESK.DEPENDENT");
		mapper.mapDependency(0, "OFFICE.SIMPLE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectFlow} of {@link ManagedObjectSource}
	 * is not linked.
	 */
	public void testManagedObjectSourceFlowNotLinked() {

		// Record building the office
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.DESK.MANAGED_OBJECT_SOURCE");

		// Record creating section type
		this.issues.recordCaptureIssues(false);

		// Record issue in flow not linked
		this.issues
				.recordIssue("doProcess", ManagedObjectFlowNodeImpl.class,
						"Managed Object Source Flow doProcess is not linked to a TaskNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked
	 * {@link ProcessState} with a {@link Task}.
	 */
	public void testManagedObjectSourceFlowLinkedToTask() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.DESK.MANAGED_OBJECT_SOURCE");
		managingOffice.linkProcess(0, "DESK.WORK", "INPUT");

		// Capture issues for managed object type
		this.issues.recordCaptureIssues(false);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked
	 * {@link ProcessState} with an {@link ExternalFlowModel}.
	 */
	public void testManagedObjectSourceFlowLinkedToExternalFlow() {

		// Capture issues for the two section types
		this.issues.recordCaptureIssues(false);
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.DESK_A.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.DESK_A.MANAGED_OBJECT_SOURCE");
		managingOffice.linkProcess(0, "DESK_B.WORK", "INPUT");
		this.record_officeBuilder_addWork("DESK_B.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);

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

}