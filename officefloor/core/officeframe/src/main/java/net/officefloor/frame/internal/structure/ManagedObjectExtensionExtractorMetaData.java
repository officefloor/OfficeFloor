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
 * Meta-data to extract the extension from the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionExtractorMetaData<E extends Object> {

	/**
	 * Obtains the {@link ManagedObjectIndex} to identify the
	 * {@link ManagedObject} to extract the extension interface from.
	 *
	 * @return {@link ManagedObjectIndex} to identify the {@link ManagedObject}
	 *         to extract the extension interface from.
	 */
	ManagedObjectIndex getManagedObjectIndex();

	/**
	 * Obtains the {@link ManagedObjectExtensionExtractor} to extract the
	 * Extension Interface from the {@link ManagedObject}.
	 *
	 * @return {@link ManagedObjectExtensionExtractor} to extract the Extension
	 *         Interface from the {@link ManagedObject}.
	 */
	ManagedObjectExtensionExtractor<E> getManagedObjectExtensionExtractor();

}
