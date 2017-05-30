/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.integrate.pool;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.integrate.managedobject.CompileOfficeFloorManagedObjectTest.SimpleManagedObject;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Ensure can compile the {@link ManagedObjectPool}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileManagedObjectPoolTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedObjectPoolSource}.
	 */
	public void testSimpleManagedObjectPoolSource() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 10,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if pooled object is not super type of
	 * {@link ManagedObjectSource}.
	 */
	public void testIssueIfIncorrectPooledObjectType() {

		this.issues.recordIssue("Pooled object " + " must be super (or same) type for ManagedObjectSource");

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 10,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedObjectPoolSource} with a
	 * {@link ThreadCompletionListener}.
	 */
	public void testManagedObjectPoolSourceWithThreadCompletionListener() {

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 10,
				"class.name", SimpleManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");
		fail("TODO implement thread completion listener recording");

		// Compile the OfficeFloor
		this.compile(true);
	}

}