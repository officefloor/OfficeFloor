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
package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Office within the {@link OfficeFloor}.
 * 
 * @author Daniel
 */
public interface Office {

	/**
	 * Obtains the {@link WorkManager} for the named {@link Work}.
	 * 
	 * @param name
	 *            Name of the {@link Work}.
	 * @return {@link WorkManager} for the named {@link Work}.
	 * @throws UnknownWorkException
	 *             If unknown {@link Work} name.
	 */
	WorkManager getWorkManager(String workName) throws UnknownWorkException;

	/**
	 * Obtains a {@link ManagedObject} for the input Id.
	 * 
	 * @param managedObjectId
	 *            Id of the {@link ManagedObject}.
	 * @return {@link ManagedObject} for the input Id.
	 * @throws Exception
	 *             If fails to obtain the {@link ManagedObject}.
	 */
	// TODO consider moving this to OfficeFloor
	@Deprecated
	ManagedObject getManagedObject(String managedObjectId) throws Exception;

}