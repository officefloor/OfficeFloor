/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.spi.managedobject.source.impl;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * <p>
 * Abstract {@link ManagedObjectSource} that allows to synchronously source the
 * {@link ManagedObject}.
 * <p>
 * For asynchronous sourcing of a {@link ManagedObject} use
 * {@link AbstractAsyncManagedObjectSource}.
 * 
 * @see AbstractAsyncManagedObjectSource
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedObjectSource<D extends Enum<D>, F extends Enum<F>>
		extends AbstractAsyncManagedObjectSource<D, F> {

	/*
	 * ============= ManagedObjectSource ===================================
	 */

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		try {
			// Obtain the managed object
			ManagedObject managedObject = this.getManagedObject();

			// Provide the managed object to the user
			user.setManagedObject(managedObject);

		} catch (Throwable ex) {
			// Flag error in retrieving
			user.setFailure(ex);
		}
	}

	/**
	 * Synchronously obtains the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObject}.
	 * @throws Throwable
	 *             If fails to obtain the {@link ManagedObject}.
	 */
	protected abstract ManagedObject getManagedObject() throws Throwable;

}