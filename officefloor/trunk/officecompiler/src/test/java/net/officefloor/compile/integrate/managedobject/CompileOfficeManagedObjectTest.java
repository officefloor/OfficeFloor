/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling an {@link Office} {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class CompileOfficeManagedObjectTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectSource}.
	 */
	public void testSimpleManagedObjectSource() {

		// Record building the office floor
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				"class.name", SimpleManagedObject.class.getName());
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
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT",
				"OFFICE.MANAGED_OBJECT_SOURCE");
		this.recordReturn(office, office.addProcessManagedObject(
				"OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT"), null);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Simple class for {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}

}