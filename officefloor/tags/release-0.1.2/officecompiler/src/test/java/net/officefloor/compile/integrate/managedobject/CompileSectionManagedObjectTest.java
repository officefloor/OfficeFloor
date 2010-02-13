/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.managedobject.clazz.ProcessInterface;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Tests compiling a {@link SectionManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class CompileSectionManagedObjectTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {

		// Record building the office floor
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 10, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} bound to {@link ProcessState}.
	 */
	public void testProcessBoundManagedObject() {

		// Record building the office floor
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT"), null);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not linked.
	 */
	public void testManagedObjectWithDependencyNotLinked() {

		// Record building the office floor

		// Register the managed object with dependency not linked
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.DEPENDENT",
				"OFFICE.SECTION.DEPENDENT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject(
				"OFFICE.SECTION.DEPENDENT", "OFFICE.SECTION.DEPENDENT");
		this.issues
				.addIssue(LocationType.SECTION, "section",
						AssetType.MANAGED_OBJECT, "DEPENDENT",
						"Dependency dependency is not linked to a BoundManagedObjectNode");

		// Add managed objects to office floor
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency in
	 * {@link SectionModel}.
	 */
	public void testManagedObjectWithDependencyInSection() {

		// Record building the office

		// Register the office linked managed objects with the office
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.DEPENDENT",
				"OFFICE.SECTION.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this
				.record_officeBuilder_addProcessManagedObject(
						"OFFICE.SECTION.DEPENDENT", "OFFICE.SECTION.DEPENDENT");
		mapper.mapDependency(0, "OFFICE.SECTION.SIMPLE");
		office.registerManagedObjectSource("OFFICE.SECTION.SIMPLE",
				"OFFICE.SECTION.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject(
				"OFFICE.SECTION.SIMPLE", "OFFICE.SECTION.SIMPLE");

		// Add managed objects to office
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.SIMPLE_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency outside
	 * {@link SectionModel}.
	 */
	public void testManagedObjectWithDependencyOutSideSection() {

		// Record building the office

		// Register the section linked managed objects with the office
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SIMPLE",
				"OFFICE.SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("OFFICE.SIMPLE",
				"OFFICE.SIMPLE");
		office.registerManagedObjectSource("OFFICE.SECTION.DEPENDENT",
				"OFFICE.SECTION.DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this
				.record_officeBuilder_addProcessManagedObject(
						"OFFICE.SECTION.DEPENDENT", "OFFICE.SECTION.DEPENDENT");
		mapper.mapDependency(0, "OFFICE.SIMPLE");

		// Add managed objects to office
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SIMPLE_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectFlow} of
	 * {@link SectionManagedObjectSource} is not linked.
	 */
	public void testManagedObjectSourceFlowNotLinked() {

		// Record building the office floor
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.issues.addIssue(LocationType.SECTION, "section",
				AssetType.MANAGED_OBJECT, "MANAGED_OBJECT_SOURCE",
				"Managed object flow doProcess is not linked to a TaskNode");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked
	 * {@link ProcessState} with a {@link SubSectionInput}.
	 */
	public void testManagedObjectSourceFlowLinkedToSubSectionInput() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		managingOffice.linkProcess(0, "SECTION.DESK.WORK", "INPUT");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked
	 * {@link ProcessState} with an {@link ExternalFlowModel}.
	 */
	public void testManagedObjectSourceFlowLinkedToExternalFlow() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this
				.record_managingOfficeBuilder_setInputManagedObjectName("OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		managingOffice.linkProcess(0, "DESK.WORK", "INPUT");

		// Compile the office floor
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
	 * {@link ProcessInterface}.
	 */
	public static class ProcessManagedObject {

		public static interface Processes {
			void doProcess(Integer parameter);
		}

		@ProcessInterface
		Processes processes;
	}

}