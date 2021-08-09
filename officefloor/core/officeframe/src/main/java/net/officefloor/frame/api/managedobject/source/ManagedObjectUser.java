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

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * User interested in using the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectUser {

	/**
	 * <p>
	 * Specifies the {@link ManagedObject} to be used.
	 * <p>
	 * This will be called by the
	 * {@link ManagedObjectSource#sourceManagedObject(ManagedObjectUser)} method
	 * to provide the {@link ManagedObject} to this {@link ManagedObjectUser}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be used.
	 */
	void setManagedObject(ManagedObject managedObject);

	/**
	 * Indicates failure to obtain the {@link ManagedObject}.
	 * 
	 * @param cause
	 *            Cause of the failure.
	 */
	void setFailure(Throwable cause);

}
