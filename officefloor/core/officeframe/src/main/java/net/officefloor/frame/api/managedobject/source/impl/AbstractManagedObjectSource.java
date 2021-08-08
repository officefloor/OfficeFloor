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

package net.officefloor.frame.api.managedobject.source.impl;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;

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
public abstract class AbstractManagedObjectSource<O extends Enum<O>, F extends Enum<F>>
		extends AbstractAsyncManagedObjectSource<O, F> {

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
