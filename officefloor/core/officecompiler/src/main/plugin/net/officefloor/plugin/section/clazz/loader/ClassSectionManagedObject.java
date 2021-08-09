/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.section.clazz.loader;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.section.SectionManagedObject;

/**
 * {@link SectionManagedObject} with meta-data for {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionManagedObject {

	/**
	 * {@link SectionManagedObject}.
	 */
	private final SectionManagedObject managedObject;

	/**
	 * {@link ManagedObjectType}.
	 */
	private final ManagedObjectType<?> managedObjectType;

	/**
	 * Instantiate.
	 * 
	 * @param managedObject     {@link SectionManagedObject}.
	 * @param managedObjectType {@link ManagedObjectType}.
	 */
	public ClassSectionManagedObject(SectionManagedObject managedObject, ManagedObjectType<?> managedObjectType) {
		this.managedObject = managedObject;
		this.managedObjectType = managedObjectType;
	}

	/**
	 * Obtains the {@link SectionManagedObject}.
	 * 
	 * @return {@link SectionManagedObject}.
	 */
	public SectionManagedObject getManagedObject() {
		return managedObject;
	}

	/**
	 * Obtains the {@link ManagedObjectType}.
	 * 
	 * @return {@link ManagedObjectType}.
	 */
	public ManagedObjectType<?> getManagedObjectType() {
		return managedObjectType;
	}

}
