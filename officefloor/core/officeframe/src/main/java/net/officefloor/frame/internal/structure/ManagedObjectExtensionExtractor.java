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
 * Extracts the extension interface from the {@link ManagedObject} within the
 * {@link ManagedObjectContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionExtractor<E> {

	/**
	 * Extracts the extension from the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to extract the extension interface from.
	 * @param managedObjectMetaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject} to
	 *            aid in extracting the extension interface.
	 * @return Extension Interface.
	 * @throws Throwable
	 *             If fails to extract the extension.
	 */
	E extractExtension(ManagedObject managedObject, ManagedObjectMetaData<?> managedObjectMetaData) throws Throwable;

}
