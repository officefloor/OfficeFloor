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
package net.officefloor.compile.section;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> for a dependent {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface DependentManagedObjectType {

	/**
	 * Obtains the name of this dependent {@link ManagedObject}.
	 * 
	 * @return Name of this dependent {@link ManagedObject}.
	 */
	String getDependentManagedObjectName();

	/**
	 * <p>
	 * Obtains the {@link TypeQualification} instances for this
	 * {@link DependentManagedObject}.
	 * <p>
	 * Should no {@link TypeQualification} instances be manually assigned, the
	 * {@link TypeQualification} should be derived from the
	 * {@link ManagedObjectType} (i.e. type without qualifier).
	 * 
	 * @return {@link TypeQualification} instances for this
	 *         {@link DependentManagedObject}.
	 */
	TypeQualification[] getTypeQualifications();

	/**
	 * Obtains the {@link ObjectDependencyType} instances for this dependent
	 * {@link ManagedObject}.
	 * 
	 * @return {@link ObjectDependencyType} instances for this dependent
	 *         {@link ManagedObject}.
	 */
	ObjectDependencyType[] getObjectDependencies();

}