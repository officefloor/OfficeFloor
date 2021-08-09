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

/**
 * Meta-data for the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessMetaData {

	/**
	 * Obtains the {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 * instances bound to the {@link ProcessState}.
	 * 
	 * @return {@link ManagedObjectMetaData} of the {@link ManagedObject} instances
	 *         bound to the {@link ProcessState}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the {@link ThreadMetaData} of {@link ThreadState} instances spawned
	 * from the {@link ProcessState} of this {@link ProcessMetaData}.
	 * 
	 * @return {@link ThreadMetaData} of {@link ThreadState} instances spawned from
	 *         the {@link ProcessState} of this {@link ProcessMetaData}.
	 */
	ThreadMetaData getThreadMetaData();

}
