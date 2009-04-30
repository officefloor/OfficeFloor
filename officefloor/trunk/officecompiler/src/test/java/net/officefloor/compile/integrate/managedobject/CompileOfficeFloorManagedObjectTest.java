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
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * Tests compiling a {@link OfficeFloor} {@link ManagedObject}.
 * 
 * @author Daniel
 */
public class CompileOfficeFloorManagedObjectTest extends
		AbstractCompileTestCase {

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
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}

	/**
	 * Class for {@link ClassManagedObjectSource} containing a dependency.
	 */
	public static class DependencyManagedObject {

		@Dependency
		SimpleManagedObject dependency;
	}
}