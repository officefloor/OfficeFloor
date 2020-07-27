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
