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

package net.officefloor.plugin.section.clazz.object;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.section.clazz.loader.ClassSectionManagedObject;

/**
 * Object context for {@link Class} section.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionObjectContext {

	/**
	 * Creates a {@link ClassSectionTypeQualifier}.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 * @return {@link ClassSectionTypeQualifier}.
	 */
	ClassSectionTypeQualifier createTypeQualifier(String qualifier, Class<?> type);

	/**
	 * Gets or creates the {@link SectionManagedObject}.
	 * 
	 * @param managedObjectSourceClassName {@link ManagedObjectSource} {@link Class}
	 *                                     name.
	 * @param properties                   {@link PropertyList} for the
	 *                                     {@link SectionManagedObject}.
	 * @param typeQualifiers               {@link ClassSectionTypeQualifier}
	 *                                     instances.
	 * @return {@link ClassSectionManagedObject}.
	 */
	ClassSectionManagedObject getOrCreateManagedObject(String managedObjectSourceClassName, PropertyList properties,
			ClassSectionTypeQualifier... typeQualifiers);

	/**
	 * Gets or creates the {@link SectionManagedObject}.
	 * 
	 * @param managedObjectSource {@link ManagedObjectSource}.
	 * @param properties          {@link PropertyList} for the
	 *                            {@link SectionManagedObject}.
	 * @param typeQualifiers      {@link ClassSectionTypeQualifier} instances.
	 * @return {@link ClassSectionManagedObject}.
	 */
	ClassSectionManagedObject getOrCreateManagedObject(ManagedObjectSource<?, ?> managedObjectSource,
			PropertyList properties, ClassSectionTypeQualifier... typeQualifiers);

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSourceContext();

}
