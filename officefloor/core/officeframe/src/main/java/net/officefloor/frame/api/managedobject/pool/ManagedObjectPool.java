/*-
 * #%L
 * OfficeFrame
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

/*
 * Created on Jan 10, 2006
 */
package net.officefloor.frame.api.managedobject.pool;

import net.officefloor.frame.api.manage.OfficeFloor;
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
	 * Should the {@link ManagedObjectPool} create a wrapper {@link ManagedObject}
	 * for pooling, this method is required to be implemented to extract the
	 * {@link ManagedObject} sourced from the {@link ManagedObjectSource}.
	 * 
	 * @param pooledManagedObject {@link ManagedObject} source from this
	 *                            {@link ManagedObjectPool}.
	 * @return {@link ManagedObject} sourced from the underlying
	 *         {@link ManagedObjectSource}.
	 */
	default ManagedObject getSourcedManagedObject(ManagedObject pooledManagedObject) {
		return pooledManagedObject;
	}

	/**
	 * Sources the {@link ManagedObject} from this {@link ManagedObjectPool}.
	 * 
	 * @param user {@link ManagedObjectUser} requiring the {@link ManagedObject}.
	 */
	void sourceManagedObject(ManagedObjectUser user);

	/**
	 * Returns an instance to the pool.
	 * 
	 * @param managedObject {@link ManagedObject}.
	 */
	void returnManagedObject(ManagedObject managedObject);

	/**
	 * Flags that the {@link ManagedObject} is lost.
	 * 
	 * @param managedObject {@link ManagedObject} to no longer be used.
	 * @param cause         Cause for the {@link ManagedObject} to be lost.
	 */
	void lostManagedObject(ManagedObject managedObject, Throwable cause);

	/**
	 * Invoked on close of the {@link OfficeFloor} to allow handling pooled
	 * {@link ManagedObject} instances.
	 */
	void empty();

}
