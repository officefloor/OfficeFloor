/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.integrate.managedobject;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.test.issues.StderrCompilerIssuesWrapper;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.managedobject.clazz.ProcessInterface;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Tests compiling a {@link OfficeFloor} {@link ManagedObject}.
 * 
 * @author Daniel
 */
public class CompileOfficeFloorManagedObjectTest extends
		AbstractCompileTestCase {

	// TODO remove once tests working
	@Override
	protected CompilerIssues enhanceIssues(CompilerIssues issues) {
		return new StderrCompilerIssuesWrapper(issues);
	}

	/**
	 * Tests compiling a simple {@link ManagedObject}.
	 */
	public void testSimpleManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} bound to {@link ProcessState}.
	 */
	public void testProcessBoundManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject(
				"MANAGED_OBJECT", "MANAGED_OBJECT"), null);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not linked.
	 */
	public void testManagedObjectWithDependencyNotLinked() {

		// Record building the office floor

		// Add managed objects to office floor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Register the office managed object with dependency not linked
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("DEPENDENT",
				"DEPENDENT");
		this.issues.addIssue(LocationType.OFFICE_FLOOR, "office-floor",
				AssetType.MANAGED_OBJECT, "DEPENDENT",
				"Dependency dependency is not linked to a ManagedObjectNode");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency not registered
	 * to the {@link Office}.
	 */
	public void testManagedObjectWithDependencyNotRegisteredToOffice() {

		// Record building the office floor

		// Add managed objects to office floor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE",
				ClassManagedObjectSource.class, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

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

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObject} with a dependency registered to
	 * the {@link Office}.
	 */
	public void testManagedObjectWithDependencyRegisteredToOffice() {

		// Record building the office floor

		// Add managed objects to office floor
		this.record_officeFloorBuilder_addManagedObject("DEPENDENT_SOURCE",
				ClassManagedObjectSource.class, "class.name",
				DependencyManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("SIMPLE_SOURCE",
				ClassManagedObjectSource.class, "class.name",
				SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Register the office linked managed objects with the office
		OfficeBuilder office = this
				.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("SIMPLE", "SIMPLE_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("SIMPLE", "SIMPLE");
		office.registerManagedObjectSource("DEPENDENT", "DEPENDENT_SOURCE");
		DependencyMappingBuilder mapper = this
				.record_officeBuilder_addProcessManagedObject("DEPENDENT",
						"DEPENDENT");
		mapper.mapDependency(0, "SIMPLE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure issue if {@link ManagedObjectFlow} of
	 * {@link OfficeFloorManagedObjectSource} is not linked.
	 */
	public void testManagedObjectSourceFlowNotLinked() {

		// Record building the office floor

		// Add the managed object source to office floor
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				"class.name", ProcessManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.issues.addIssue(LocationType.OFFICE_FLOOR, "office-floor",
				AssetType.MANAGED_OBJECT, "MANAGED_OBJECT_SOURCE",
				"Managed object flow doProcess is not linked to a TaskNode");
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests linking the {@link ManagedObjectSource} invoked
	 * {@link ProcessState} with a {@link Task}.
	 */
	public void testManagedObjectSourceWithFlow() {

		// Record building the office floor

		// Add the managed object source to office floor
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				"class.name", ProcessManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		managingOffice.linkProcess(0, "SECTION.WORK", "INPUT");
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("INPUT",
				"OFFICE_TEAM");
		task.linkParameter(0, Integer.class);

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