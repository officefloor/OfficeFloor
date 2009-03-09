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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Raw meta-data of a {@link ManagedObject} for the {@link Work}.
 * 
 * @author Daniel
 */
public interface RawWorkManagedObjectMetaData {

	/**
	 * Obtains the {@link ManagedObjectIndex} of this {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectIndex} of this {@link ManagedObject}.
	 */
	ManagedObjectIndex getManagedObjectIndex();

	/**
	 * Obtains the index of this {@link ManagedObject} within the {@link Work}.
	 * 
	 * @return Index of this {@link ManagedObject} within the {@link Work}.
	 */
	int getWorkManagedObjectIndex();

	/**
	 * Obtains the dependent {@link RawWorkManagedObjectMetaData} of this
	 * {@link ManagedObject}.
	 * 
	 * @return Dependent {@link RawWorkManagedObjectMetaData} instances.
	 */
	RawWorkManagedObjectMetaData[] getDependencies();

}