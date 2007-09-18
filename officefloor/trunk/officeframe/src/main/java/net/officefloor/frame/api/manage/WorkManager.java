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

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Interface to manage a particular type of
 * {@link net.officefloor.frame.api.execute.Work}.
 * 
 * @author Daniel
 */
public interface WorkManager {

	/**
	 * Invokes a new instance of {@link net.officefloor.frame.api.execute.Work}
	 * which is done within the Office.
	 * 
	 * @param parameter
	 *            Parameter for the first
	 *            {@link net.officefloor.frame.api.execute.Task} of the
	 *            {@link net.officefloor.frame.api.execute.Work}.
	 */
	FlowFuture invokeWork(Object parameter);

	/**
	 * <p>
	 * Obtains a {@link ManagedObject} for the input Id.
	 * <p>
	 * As {@link ManagedObject} instances may be dependent on other
	 * {@link ManagedObject} instances, they require the
	 * {@link net.officefloor.frame.api.execute.Work} to specify these
	 * dependencies.
	 * 
	 * @param managedObjectId
	 *            Id of the {@link ManagedObject}.
	 * @return {@link ManagedObject} for the input Id.
	 * @throws Exception
	 *             If fails to obtain the {@link ManagedObject}.
	 */
	ManagedObject getManagedObject(String managedObjectId) throws Exception;

}
