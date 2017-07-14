/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.spi.managedobject;

import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Dependency of a {@link SectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependency {

	/**
	 * Obtains the name of this {@link ManagedObjectDependency}.
	 * 
	 * @return Name of this {@link ManagedObjectDependency}.
	 */
	String getManagedObjectDependencyName();

	/**
	 * Overrides the qualifier specified by the {@link ManagedObjectSource}.
	 * 
	 * @param qualifier
	 *            Qualifier to use for the {@link ManagedObjectDependency}.
	 */
	void setOverrideQualifier(String qualifier);

	/**
	 * <p>
	 * Specifies a more specific type than the type specified by the
	 * {@link ManagedObjectSource}.
	 * <p>
	 * Note: the type needs to be child of the actual type, otherwise it would
	 * cause {@link ClassCastException} on attempting to the use the resulting
	 * dependency. Hence the type can not be overridden but rather is able to be
	 * made more specific (particularly for auto wiring the dependency).
	 * 
	 * @param type
	 *            Type to use for the {@link ManagedObjectDependency}.
	 */
	void setSpecificType(String type);

}