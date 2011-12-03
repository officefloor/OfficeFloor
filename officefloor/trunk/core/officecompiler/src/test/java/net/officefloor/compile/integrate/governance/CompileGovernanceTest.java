/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.compile.integrate.governance;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.integrate.managedobject.CompileOfficeFloorManagedObjectTest.InputManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.governance.clazz.Enforce;
import net.officefloor.plugin.governance.clazz.Govern;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.ManagedObject;
import net.officefloor.plugin.section.clazz.Property;
import net.officefloor.plugin.section.clazz.SectionClassManagedObjectSource;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Tests compiling a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileGovernanceTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling {@link Governance} for an
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testGovernOfficeFloorManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		DependencyMappingBuilder dependencies = this
				.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT",
						"MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		dependencies.mapGovernance("GOVERNANCE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure able to provide {@link Governance} to a
	 * {@link OfficeFloorInputManagedObjectModel} that has multiple
	 * {@link OfficeFloorManagedObjectSourceModel} instances (along with
	 * {@link BoundManagedObject}).
	 */
	public void testGovernInputManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<Work, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE_A", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosA = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder mapperA = this
				.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mosA.linkProcess(0, "SECTION.WORK", "INPUT");
		office.setBoundInputManagedObject("INPUT_MO", "MANAGED_OBJECT_SOURCE_A");
		mapperA.mapGovernance("GOVERNANCE");
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE_B", ClassManagedObjectSource.class, 0,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> mosB = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		DependencyMappingBuilder mapperB = this
				.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MO");
		mosB.linkProcess(0, "SECTION.WORK", "INPUT");
		mapperB.mapGovernance("GOVERNANCE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for an {@link OfficeManagedObject}.
	 */
	public void testGovernOfficeManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT",
				"OFFICE.MANAGED_OBJECT_SOURCE");
		DependencyMappingBuilder dependencies = this
				.record_officeBuilder_addProcessManagedObject(
						"OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		dependencies.mapGovernance("GOVERNANCE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for a
	 * {@link OfficeSectionManagedObject}.
	 */
	public void testGovernSectionManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");

		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);

		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask(
				"doSomething", "OFFICE_TEAM");
		task.linkManagedObject(0, "OFFICE.SECTION.OBJECT",
				SectionWithManagedObject.class);

		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.OBJECT", SectionClassManagedObjectSource.class,
				0, "class.name", SectionWithManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.OBJECT",
				"OFFICE.SECTION.OBJECT");
		DependencyMappingBuilder sectionDependencies = this
				.record_officeBuilder_addThreadManagedObject(
						"OFFICE.SECTION.OBJECT", "OFFICE.SECTION.OBJECT");
		sectionDependencies.mapDependency(0, "OFFICE.SECTION."
				+ SimpleManagedObject.class.getName());

		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION."
				+ SimpleManagedObject.class.getName(),
				ClassManagedObjectSource.class, 0, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION."
				+ SimpleManagedObject.class.getName(), "OFFICE.SECTION."
				+ SimpleManagedObject.class.getName());
		DependencyMappingBuilder managedObjectDependencies = this
				.record_officeBuilder_addProcessManagedObject("OFFICE.SECTION."
						+ SimpleManagedObject.class.getName(),
						"OFFICE.SECTION." + SimpleManagedObject.class.getName());
		managedObjectDependencies.mapGovernance("GOVERNANCE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} over a {@link Task} within a
	 * {@link OfficeSection}.
	 */
	public void testGovernSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> governedTask = this.record_workBuilder_addTask(
				"TASK", "OFFICE_TEAM");
		governedTask.addGovernance("GOVERNANCE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for a specific
	 * {@link OfficeSubSection}.
	 */
	public void testGovernSubSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);
		this.record_officeBuilder_addWork("SECTION.DESK.WORK");
		TaskBuilder<?, ?, ?> governedTask = this.record_workBuilder_addTask(
				"TASK", "OFFICE_TEAM");
		governedTask.addGovernance("GOVERNANCE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} for a specific {@link OfficeTask}.
	 */
	public void testGovernOfficeTask() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> governedTask = this.record_workBuilder_addTask(
				"TASK", "OFFICE_TEAM");
		governedTask.addGovernance("GOVERNANCE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Governance} over a {@link Task} within a
	 * {@link OfficeSubSection}.
	 */
	public void testSubSectionTaskInheritGovernance() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addGovernance("GOVERNANCE", "OFFICE_TEAM",
				ClassGovernanceSource.class, SimpleManagedObject.class);
		this.record_officeBuilder_addWork("GOVERNED_SECTION.GOVERNED_DESK.WORK");
		TaskBuilder<?, ?, ?> governedTask = this.record_workBuilder_addTask(
				"TASK", "OFFICE_TEAM");
		governedTask.addGovernance("GOVERNANCE");
		this.record_officeBuilder_addWork("NON_GOVERNED_DESK.WORK");
		this.record_workBuilder_addTask("TASK", "OFFICE_TEAM");

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
	 * Simple class for {@link ClassWorkSource}.
	 */
	public static class SimpleWork {

		public void simpleTask() {
			fail("Should not be invoked in compiling");
		}
	}

	/**
	 * {@link Work} for {@link InputManagedObject} for the
	 * {@link ClassWorkSource}.
	 */
	public static class InputManagedObjectWork {

		public void handleInputManagedObject(Integer parameter) {
		}
	}

	/**
	 * Simple class for {@link ClassGovernanceSource}.
	 */
	public static class SimpleGovernance {

		@Govern
		public void govern(SimpleManagedObject managedObject) {
		}

		@Enforce
		public void enforce() {
		}
	}

	/**
	 * Class for {@link ClassSectionSource} with a {@link ManagedObject}.
	 */
	public static class SectionWithManagedObject {

		@ManagedObject(source = ClassManagedObjectSource.class, properties = @Property(name = "class.name", valueClass = SimpleManagedObject.class))
		SimpleManagedObject managedObject;

		public void doSomething() {
		}
	}

}