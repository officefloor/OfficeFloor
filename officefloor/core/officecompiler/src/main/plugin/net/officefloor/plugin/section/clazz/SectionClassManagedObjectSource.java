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

package net.officefloor.plugin.section.clazz;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * {@link ManagedObjectSource} implementation to make the
 * {@link ClassSectionSource} object available with necessary dependency
 * injection.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class SectionClassManagedObjectSource extends ClassManagedObjectSource {

	@Override
	protected List<Field> extractDependencyFields(Class<?> objectClass) {

		// Override to obtain both dependency and managed object fields
		List<Field> dependencyFields = new LinkedList<Field>();
		Class<?> interrogateClass = objectClass;
		while ((interrogateClass != null) && (!Object.class.equals(interrogateClass))) {
			for (Field field : interrogateClass.getDeclaredFields()) {
				if ((field.getAnnotation(Dependency.class) != null)
						|| (field.getAnnotation(ManagedObject.class) != null)) {
					// Annotated as a dependency field
					dependencyFields.add(field);
				}
			}
			interrogateClass = interrogateClass.getSuperclass();
		}

		// Return the dependency fields
		return dependencyFields;
	}

}
