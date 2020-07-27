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
