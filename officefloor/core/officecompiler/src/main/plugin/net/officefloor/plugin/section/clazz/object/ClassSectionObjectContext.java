/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
