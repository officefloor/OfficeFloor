/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.managedobject;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data of a bound {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface RawBoundManagedObjectMetaData<D extends Enum<D>> {

	/**
	 * Obtains the name the {@link ManagedObject} is bound under.
	 * 
	 * @return Name the {@link ManagedObject} is bound under.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectIndex}.
	 * 
	 * @return {@link ManagedObjectIndex}.
	 */
	ManagedObjectIndex getManagedObjectIndex();

	/**
	 * Obtains the {@link RawManagedObjectMetaData}.
	 * 
	 * @return {@link RawManagedObjectMetaData}.
	 */
	RawManagedObjectMetaData<D, ?> getRawManagedObjectMetaData();

	/**
	 * Obtains the keys of the dependencies for this {@link ManagedObject}.
	 * 
	 * @return Keys of the dependencies for this {@link ManagedObject}.
	 */
	D[] getDependencyKeys();

	/**
	 * Obtains the {@link RawBoundManagedObjectMetaData} for the dependency.
	 * 
	 * @param dependencyKey
	 *            Dependency key.
	 * @return {@link RawBoundManagedObjectMetaData} for the dependency.
	 */
	RawBoundManagedObjectMetaData<?> getDependency(D dependencyKey);

	/**
	 * Obtains the {@link ManagedObjectMetaData} for this
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link ManagedObjectMetaData} for this
	 *         {@link RawBoundManagedObjectMetaData}.
	 */
	ManagedObjectMetaData<?> getManagedObjectMetaData(OfficeFloorIssues issues);

}