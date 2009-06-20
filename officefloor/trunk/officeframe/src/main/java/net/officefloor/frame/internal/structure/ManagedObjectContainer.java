/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Container managing a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContainer {

	/**
	 * Loads the {@link ManagedObject}.
	 * 
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} to be
	 *            loaded.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @return <code>true</code> if the {@link ManagedObject} was loaded,
	 *         otherwise <code>false</code> indicating that the {@link JobNode}
	 *         is waiting on the {@link ManagedObject} to be loaded.
	 */
	boolean loadManagedObject(JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet);

	/**
	 * Allows this {@link ManagedObject} to coordinate with the other
	 * {@link ManagedObject} instances.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer} to source the other
	 *            {@link ManagedObject} instances.
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} to
	 *            coordinate.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 */
	<W extends Work> void coordinateManagedObject(
			WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet);

	/**
	 * Indicates if the {@link ManagedObject} is ready. This is to ensure the
	 * {@link ManagedObject}:
	 * <ol>
	 * <li>is loaded, and</li>
	 * <li>is not currently involved within an asynchronous operation (in other
	 * words the {@link AsynchronousManagedObject} completed execution and ready
	 * for another operation)</li>
	 * </ol>
	 * 
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode} requiring the {@link ManagedObject} to be
	 *            ready.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 * @return <code>true</code> if the {@link ManagedObject} is ready,
	 *         otherwise <code>false</code> indicating that waiting on a
	 *         {@link ManagedObject}.
	 */
	boolean isManagedObjectReady(JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet);

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
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet} to add {@link JobNode} instances to
	 *            activate.
	 */
	void unloadManagedObject(JobNodeActivateSet activateSet);

}