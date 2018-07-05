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
/*
 * Created on Jan 10, 2006
 */
package net.officefloor.frame.api.managedobject.pool;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;

/**
 * Pool of {@link ManagedObject} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPool {

	/**
	 * Sources the {@link ManagedObject} from this {@link ManagedObjectPool}.
	 * 
	 * @param user
	 *            {@link ManagedObjectUser} requiring the {@link ManagedObject}.
	 */
	void sourceManagedObject(ManagedObjectUser user);

	/**
	 * Returns an instance to the pool.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	void returnManagedObject(ManagedObject managedObject);

	/**
	 * Flags that the {@link ManagedObject} is lost.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to no longer be used.
	 * @param cause
	 *            Cause for the {@link ManagedObject} to be lost.
	 */
	void lostManagedObject(ManagedObject managedObject, Throwable cause);

	/**
	 * Should the {@link ManagedObjectPool} create a wrapper {@link ManagedObject}
	 * for pooling, this method is required to be implemented to extract the
	 * {@link ManagedObject} sourced from the {@link ManagedObjectSource}.
	 * 
	 * @param pooledManagedObject
	 *            {@link ManagedObject} source from this {@link ManagedObjectPool}.
	 * @return {@link ManagedObject} sourced from the underlying
	 *         {@link ManagedObjectSource}.
	 */
	default ManagedObject getSourcedManagedObject(ManagedObject pooledManagedObject) {
		return pooledManagedObject;
	}

}