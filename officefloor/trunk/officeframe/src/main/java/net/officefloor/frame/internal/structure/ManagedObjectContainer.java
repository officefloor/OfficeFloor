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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Container managing a
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectContainer {

	/**
	 * Loads the {@link ManagedObject}.
	 * 
	 * @param executionContext
	 *            Context for execution.
	 * @param taskContainer
	 *            {@link TaskContainer} requesting the {@link ManagedObject} to
	 *            be loaded.
	 * @return <code>true</code> if the {@link ManagedObject} was loaded,
	 *         otherwise <code>false</code> indicating that waiting on a
	 *         {@link ManagedObject}.
	 */
	boolean loadManagedObject(ExecutionContext executionContext,
			TaskContainer taskContainer);

	/**
	 * Allows this {@link ManagedObject} to co-ordinate with the other
	 * {@link ManagedObject} instances.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer} to source the other
	 *            {@link ManagedObject} instances.
	 * @param executionContext
	 *            Context for execution.
	 * @param taskContainer
	 *            {@link TaskContainer} requesting the {@link ManagedObject} to
	 *            co-ordinate.
	 */
	<W extends Work> void coordinateManagedObject(WorkContainer<W> workContainer,
			ExecutionContext executionContext, TaskContainer taskContainer);

	/**
	 * Indicates if the {@link ManagedObject} is ready. This is to ensure the
	 * {@link ManagedObject} is not currently involved within an asynchronous
	 * operation (ie {@link ManagedObject} completed execution and ready for
	 * another operation).
	 * 
	 * @param executionContext
	 *            Context for execution.
	 * @param taskContainer
	 *            {@link TaskContainer} requiring the {@link ManagedObject} to
	 *            be ready.
	 * @return <code>true</code> if the {@link ManagedObject} is ready,
	 *         otherwise <code>false</code> indicating that waiting on a
	 *         {@link ManagedObject}.
	 */
	boolean isManagedObjectReady(ExecutionContext executionContext,
			TaskContainer taskContainer);

	/**
	 * Obtains the object being managed by the {@link ManagedObject}.
	 * 
	 * @param threadState
	 *            {@link ThreadState} of thread requiring the object.
	 * @return Object being managed by the {@link ManagedObject}.
	 */
	Object getObject(ThreadState threadState);

	/**
	 * Obtains the {@link ManagedObject} or <code>null</code> if it is not
	 * loaded. This allows for access to the extension interfaces of the
	 * {@link ManagedObject}.
	 * 
	 * @param threadState
	 *            {@link ThreadState} requiring the {@link ManagedObject}.
	 * @return {@link ManagedObject} or <code>null</code> if it is not loaded.
	 */
	ManagedObject getManagedObject(ThreadState threadState);

	/**
	 * Unloads the {@link ManagedObject}.
	 */
	void unloadManagedObject();

}
