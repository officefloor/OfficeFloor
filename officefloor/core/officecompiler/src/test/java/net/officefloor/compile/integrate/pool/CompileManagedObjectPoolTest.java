/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
