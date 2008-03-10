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
package net.officefloor.frame.util;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * Implementation of a
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectUser} to
 * source an object from a
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class ManagedObjectUserStandAlone implements ManagedObjectUser {

	/**
	 * Sources the {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param source
	 *            {@link ManagedObjectSource}.
	 * @return Object from the {@link ManagedObjectSource}.
	 * @throws Exception
	 *             If fails to source object.
	 */
	public static ManagedObject sourceManagedObject(
			ManagedObjectSource<?, ?> source) throws Exception {

		// Create a new user
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();

		// Source the object
		source.sourceManagedObject(user);

		// Obtain the managed object
		return user.getManagedObject();
	}

	/**
	 * Sourced {@link ManagedObject}.
	 */
	private ManagedObject managedObject;

	/**
	 * Failure in sourcing the {@link ManagedObject}.
	 */
	private Throwable failure;

	/**
	 * Only via static methods.
	 */
	private ManagedObjectUserStandAlone() {
	}

	/**
	 * Obtain the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObject}.
	 * @throws Exception
	 *             If fails to source {@link ManagedObject}.
	 */
	private ManagedObject getManagedObject() throws Exception {
		// Propagate if failure
		if (this.failure != null) {
			if (this.failure instanceof Exception) {
				throw (Exception) this.failure;
			} else if (this.failure instanceof Error) {
				throw (Error) this.failure;
			} else {
				throw new Exception(this.failure);
			}
		}

		// Otherwise return managed object
		return this.managedObject;
	}

	/*
	 * ====================================================================
	 * ManagedObjectUser
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectUser#setManagedObject(net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
	public void setManagedObject(ManagedObject managedObject) {
		this.managedObject = managedObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectUser#setFailure(java.lang.Throwable)
	 */
	public void setFailure(Throwable cause) {
		this.failure = cause;
	}

}
