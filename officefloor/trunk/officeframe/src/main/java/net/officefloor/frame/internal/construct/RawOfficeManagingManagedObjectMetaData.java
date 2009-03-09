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

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data of a {@link ManagedObject} that is managed by the {@link Office}.
 * 
 * @author Daniel
 */
public interface RawOfficeManagingManagedObjectMetaData {

	/**
	 * Obtains the name for the {@link Office} managing the
	 * {@link ManagedObject}.
	 * 
	 * @return Name for the {@link Office} managing the {@link ManagedObject}.
	 */
	String getManagingOfficeName();

	/**
	 * <p>
	 * Obtains the {@link ProcessState} bound name for the {@link ManagedObject}
	 * within the {@link Office}.
	 * <p>
	 * As the {@link ManagedObject} is going to invoke a process, it is to be
	 * made available to the {@link ProcessState}. Whether the {@link Office}
	 * wants to make use of the {@link ManagedObject} is its choice but is
	 * available to do so.
	 * <p>
	 * Ultimately this means the {@link ManagedObject} has {@link Handler}
	 * instances that invoke {@link Task} instances within the {@link Office}.
	 * 
	 * @return {@link ProcessState} bound name for the {@link ManagedObject} or
	 *         <code>null</code> if not required to be {@link ProcessState}
	 *         bound.
	 */
	String getProcessBoundName();

	/**
	 * Obtains the {@link RawManagedObjectMetaData} for the
	 * {@link ManagedObject} to be managed by the {@link Office}.
	 * 
	 * @return {@link RawManagedObjectMetaData} for the {@link ManagedObject} to
	 *         be managed by the {@link Office}.
	 */
	RawManagedObjectMetaData<?, ?> getRawManagedObjectMetaData();
}