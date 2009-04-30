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
package net.officefloor.model.officefloor;

import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Changes that can be made to an {@link OfficeFloorModel}.
 * 
 * @author Daniel
 */
public interface OfficeFloorOperations {

	/**
	 * Value for {@link ManagedObjectScope#PROCESS} on
	 * {@link OfficeFloorManagedObjectModel} instances.
	 */
	String PROCESS_MANAGED_OBJECT_SCOPE = ManagedObjectScope.PROCESS.name();

	/**
	 * Value for {@link ManagedObjectScope#THREAD} on
	 * {@link OfficeFloorManagedObjectModel} instances.
	 */
	String THREAD_MANAGED_OBJECT_SCOPE = ManagedObjectScope.THREAD.name();

	/**
	 * Value for {@link ManagedObjectScope#WORK} on
	 * {@link OfficeFloorManagedObjectModel} instances.
	 */
	String WORK_MANAGED_OBJECT_SCOPE = ManagedObjectScope.WORK.name();

}