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
 * Container managing the {@link net.officefloor.frame.api.execute.Work}.
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
	 * Flags for the particular
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * to be loaded.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes identifying the {@link ManagedObject} instances to be
	 *            loaded.
	 * @param executionContext
	 *            Context for execution.
	 * @param taskContainer
	 *            {@link TaskContainer} requesting the {@link ManagedObject}
	 *            instances to be loaded.
	 * @return <code>true</code> if the {@link ManagedObject} instances were
	 *         loaded, otherwise <code>false</code> indicating that waiting on
	 *         the {@link ManagedObject} instances.
	 */
	boolean loadManagedObjects(int[] managedObjectIndexes,
			ExecutionContext executionContext, TaskContainer taskContainer);

	/**
	 * Co-ordinates the {@link ManagedObject} instances.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes identifying the {@link ManagedObject} instances to be
	 *            co-ordinated.
	 * @param executionContext
	 *            Context for execution.
	 * @param taskContainer
	 *            {@link TaskContainer} requesting the {@link ManagedObject}
	 *            instances to be co-ordinated.
	 */
	void coordinateManagedObjects(int[] managedObjectIndexes,
			ExecutionContext executionContext, TaskContainer taskContainer);

	/**
	 * Indicates if the particular {@link ManagedObject} is ready for use. In
	 * otherwords it has finished any asynchronous operations and is ready for
	 * further use.
	 * 
	 * @param managedObjectIndexes
	 *            Indexes identifying the {@link ManagedObject} instances to
	 *            check if ready.
	 * @param executionContext
	 *            Context for execution.
	 * @param taskContainer
	 *            {@link TaskContainer} requiring the {@link ManagedObject} to
	 *            be ready.
	 * @return <code>true</code> if the {@link ManagedObject} is ready for
	 *         use, otherwise <code>false</code> indicating that waiting on
	 *         the {@link ManagedObject}.
	 */
	boolean isManagedObjectsReady(int[] managedObjectIndexes,
			ExecutionContext executionContext, TaskContainer taskContainer);

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
	<A extends Enum<A>> void administerManagedObjects(
			TaskDutyAssociation<A> duty, AdministratorContext adminContext)
			throws Exception;

	/**
	 * Obtains the Object of the particular
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * 
	 * @param moIndex
	 *            Index identifying the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @return Object of the particular
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 */
	Object getObject(int moIndex, ThreadState threadState);

	/**
	 * Registers a {@link ThreadState} to use the {@link Work} of this
	 * {@link WorkContainer}.
	 * 
	 * @param thread
	 *            {@link ThreadState} registering to use the {@link Work} of
	 *            this {@link WorkContainer}.
	 */
	void registerThread(ThreadState thread);

	/**
	 * Unregisters a {@link ThreadState} once it has finished using the
	 * {@link Work} of this {@link WorkContainer}.
	 * 
	 * @param thread
	 *            {@link ThreadState} finished with this {@link WorkContainer}.
	 */
	void unregisterThread(ThreadState thread);

}
