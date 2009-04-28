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
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

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
		ManagedObjectBuilder<?> moBuilder = this
				.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT",
						ClassManagedObjectSource.class, "class.name",
						SimpleManagedObject.class.getName());
		this.recordReturn(moBuilder, moBuilder.setManagingOffice("OFFICE"),
				null);
		this.record_officeFloorBuilder_addOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Simple class {@link ClassManagedObjectSource}.
	 */
	public static class SimpleManagedObject {
	}
}