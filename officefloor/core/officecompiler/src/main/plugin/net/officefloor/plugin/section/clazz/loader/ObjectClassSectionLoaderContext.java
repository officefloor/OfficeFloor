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
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObject;

/**
 * Context for the {@link ClassSectionLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectClassSectionLoaderContext extends ClassSectionLoaderContext {

	/**
	 * Obtains the {@link SectionManagedObject}.
	 * 
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObject getSectionManagedObject();

	/**
	 * Obtains the {@link ManagedObjectType}.
	 * 
	 * @return {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> getManagedObjectType();

	/**
	 * Flags the {@link ManagedObjectDependency} linked.
	 * 
	 * @param dependencyIndex Index of the {@link ManagedObjectDependency}.
	 */
	void flagObjectDependencyLinked(int dependencyIndex);

}
