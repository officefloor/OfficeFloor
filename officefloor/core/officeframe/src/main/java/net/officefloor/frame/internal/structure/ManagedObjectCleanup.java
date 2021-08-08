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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;

/**
 * Manages the clean up of {@link ManagedObject} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectCleanup {

	/**
	 * Creates the clean up {@link FunctionState}.
	 * 
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} to recycle the {@link ManagedObject}.
	 * @param objectType
	 *            Type of the object from the {@link ManagedObject}.
	 * @param managedObject
	 *            {@link ManagedObject} to be cleaned up.
	 * @param managedObjectPool
	 *            Optional {@link ManagedObjectPool} to return the
	 *            {@link ManagedObject}. May be <code>null</code>.
	 * @return {@link FunctionState} to clean up the {@link ManagedObject}.
	 */
	FunctionState cleanup(FlowMetaData recycleFlowMetaData, Class<?> objectType, ManagedObject managedObject,
			ManagedObjectPool managedObjectPool);

}
