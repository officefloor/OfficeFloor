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
package net.officefloor.compile.integrate.pool;

import net.officefloor.compile.impl.structure.ManagedObjectPoolNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
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
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managedObjectBuilder_setManagedObjectPool("POOL");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure issue if pooled object is not super type of
	 * {@link ManagedObjectSource}.
	 */
	public void testIssueIfIncorrectPooledObjectType() {

		// Record issue of incorrect type
		this.issues.recordIssue("POOL", ManagedObjectPoolNodeImpl.class,
				"Pooled object " + OtherObject.class.getName()
						+ " must be super (or same) type for ManagedObjectSource object "
						+ CompileManagedObject.class.getName());

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addProcessManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	public static class CompileManagedObject {
	}

	public static class OtherObject {
	}

}