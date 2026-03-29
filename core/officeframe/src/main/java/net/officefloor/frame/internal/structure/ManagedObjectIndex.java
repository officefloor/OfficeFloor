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
 * Index of the {@link ManagedObject}, providing both the scope it lives within
 * and the index of it within that scope.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectIndex {

	/**
	 * Obtains the {@link ManagedObjectScope} that the {@link ManagedObject}
	 * resides within.
	 * 
	 * @return {@link ManagedObjectScope} that the {@link ManagedObject} resides
	 *         within.
	 */
	ManagedObjectScope getManagedObjectScope();

	/**
	 * Obtains the index of the {@link ManagedObject} within the
	 * {@link ManagedObjectScope}.
	 * 
	 * @return Index of the {@link ManagedObject} within the
	 *         {@link ManagedObjectScope}.
	 */
	int getIndexOfManagedObjectWithinScope();
}
