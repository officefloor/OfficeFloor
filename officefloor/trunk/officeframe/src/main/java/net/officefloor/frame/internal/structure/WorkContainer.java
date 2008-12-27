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
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Job;

/**
 * Container managing the {@link Work}.
 * 
 * @author Daniel
 */
public interface WorkContainer<W extends Work> {

	/**
	 * Obtains the type Id of this {@link Work}.
	 * 
	 * @return Type Id for this {@link Work}.
	 */
	int getWorkId();

	/**
	 * Obtains the {@link Work} being managed.
	 * 
	 * @param threadState
	 *            {@link ThreadState} requiring the {@link Work}.
	 * @return {@link Work} being managed.
	 */
	W getWork(ThreadState threadState);

	/**
	 * Flags for the particular {@link ManagedObject} instances to be loaded.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes identifying the {@link ManagedObject} instances to be
	 *            loaded.
	 * @param executionContext
	 *            Context for execution.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} instances
	 *            to be loaded.
	 * @param notifySet
	 *            {@link JobActivateSet} to add {@link Job} instances to notify.
	 * @return <code>true</code> if the {@link ManagedObject} instances were
	 *         loaded, otherwise <code>false</code> indicating that waiting on
	 *         the {@link ManagedObject} instances.
	 */
	boolean loadManagedObjects(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet);

	/**
	 * Co-ordinates the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes identifying the {@link ManagedObject} instances to be
	 *            co-ordinated.
	 * @param executionContext
	 *            Context for execution.
	 * @param jobNode
	 *            {@link JobNode} requesting the {@link ManagedObject} instances
	 *            to be co-ordinated.
	 * @param notifySet
	 *            {@link JobActivateSet} to add {@link Job} instances to notify.
	 */
	void coordinateManagedObjects(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet);

	/**
	 * Indicates if the particular {@link ManagedObject} is ready for use. In
	 * other words it has finished any asynchronous operations and is ready for
	 * further use.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes identifying the {@link ManagedObject} instances to
	 *            check if ready.
	 * @param executionContext
	 *            Context for execution.
	 * @param jobNode
	 *            {@link JobNode} requiring the {@link ManagedObject} to be
	 *            ready.
	 * @param notifySet
	 *            {@link JobActivateSet} to add {@link Job} instances to notify.
	 * @return <code>true</code> if the {@link ManagedObject} is ready for use,
	 *         otherwise <code>false</code> indicating that waiting on the
	 *         {@link ManagedObject}.
	 */
	boolean isManagedObjectsReady(int[] managedObjectIndexes,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet);

	/**
	 * Administers the {@link ManagedObject} instances as per the input
	 * {@link TaskDutyAssociation}.
	 * 
	 * @param duty
	 *            {@link TaskDutyAssociation} specifying the administration to
	 *            be undertaken.
	 * @param adminContext
	 *            {@link AdministratorContext}.
	 * @throws Exception
	 *             If fails to administer the {@link ManagedObject} instances.
	 */
	void administerManagedObjects(TaskDutyAssociation<?> duty,
			AdministratorContext adminContext) throws Exception;

	/**
	 * Obtains the Object of the particular {@link ManagedObject}.
	 * 
	 * @param moIndex
	 *            Index identifying the {@link ManagedObject}.
	 * @return Object of the particular {@link ManagedObject}.
	 */
	Object getObject(int moIndex, ThreadState threadState);

	/**
	 * Unloads the {@link Work}.
	 */
	void unloadWork();

}
