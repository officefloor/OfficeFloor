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
package net.officefloor.frame.integrate.managedobject.asynchronous;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource;

/**
 * Test {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class TestManagedObjectSource extends
		AbstractAsyncManagedObjectSource<None, None> {

	/**
	 * {@link ManagedObjectUser}.
	 */
	private static volatile ManagedObjectUser managedObjectUser;

	/**
	 * Loads the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	public static void loadManagedObject(ManagedObject managedObject) {
		managedObjectUser.setManagedObject(managedObject);
	}

	/**
	 * Initiate.
	 */
	public TestManagedObjectSource() {
		// Clears the user (resets for next test)
		managedObjectUser = null;
	}

	/*
	 * ===============================================================================
	 * AbstractAsyncManagedObjectSource
	 * ===============================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		// Asynchronous managed object
		context.setManagedObjectClass(AsynchronousManagedObject.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSource#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		managedObjectUser = user;
	}

}
