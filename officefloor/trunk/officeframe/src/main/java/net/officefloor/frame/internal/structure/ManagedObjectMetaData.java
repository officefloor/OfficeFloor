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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;
import net.officefloor.frame.spi.team.Job;

/**
 * Meta-data of a {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectMetaData<D extends Enum<D>> {

	/**
	 * Index indicating the {@link ManagedObject} will not be sourced from the
	 * {@link ProcessState}.
	 */
	static final int NON_PROCESS_INDEX = -1;

	/**
	 * Creates a new {@link ManagedObjectContainer}.
	 * 
	 * @param lock
	 *            Lock for the {@link ManagedObjectContainer} to synchronize on.
	 * @return New {@link ManagedObjectContainer}.
	 */
	ManagedObjectContainer createManagedObjectContainer(Object lock);

	/**
	 * <p>
	 * Obtains the index of the {@link ManagedObject} within the
	 * {@link ProcessState}.
	 * <p>
	 * Note that if this does not provide a value of {@link #NON_PROCESS_INDEX}
	 * then the {@link ManagedObject} will be sourced only for the {@link Work}.
	 * 
	 * @return Index of the {@link ManagedObject} within the
	 *         {@link ProcessState} or {@link #NON_PROCESS_INDEX}.
	 */
	int getProcessStateManagedObjectIndex();

	/**
	 * Obtains the {@link AssetManager} that manages the sourcing of the
	 * {@link ManagedObject}.
	 * 
	 * @return {@link AssetManager} that manages the sourcing of the
	 *         {@link ManagedObject}.
	 */
	AssetManager getSourcingManager();

	/**
	 * Obtains the {@link ManagedObjectSource} for the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectSource} for the {@link ManagedObject}.
	 */
	ManagedObjectSource<?, ?> getManagedObjectSource();

	/**
	 * Obtains the {@link ManagedObjectPool} for the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectPool} for the {@link ManagedObject}.
	 */
	ManagedObjectPool getManagedObjectPool();

	/**
	 * Obtains the time out in milliseconds for the asynchronous operation to
	 * complete.
	 * 
	 * @return Time out in milliseconds.
	 */
	long getTimeout();

	/**
	 * <p>
	 * Indicates if the {@link ManagedObject} implements
	 * {@link AsynchronousManagedObject}.
	 * <p>
	 * Should the {@link ManagedObject} implement
	 * {@link AsynchronousManagedObject} then it will require checking if ready.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} implements
	 *         {@link AsynchronousManagedObject}.
	 */
	boolean isManagedObjectAsynchronous();

	/**
	 * Obtains the {@link AssetManager} that manages asynchronous operations on
	 * the {@link ManagedObject}.
	 * 
	 * @return {@link AssetManager} that manages asynchronous operations on the
	 *         {@link ManagedObject}.
	 */
	AssetManager getOperationsManager();

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link CoordinatingManagedObject}.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} implements
	 *         {@link CoordinatingManagedObject}.
	 */
	boolean isCoordinatingManagedObject();

	/**
	 * Creates the {@link ObjectRegistry} for the {@link ManagedObject}.
	 * 
	 * @param workContainer
	 *            {@link WorkContainer} to obtain the co-ordinating
	 *            {@link ManagedObject} instances.
	 * @param threadState
	 *            {@link ThreadState} to provide access to the
	 *            {@link ProcessState} bound {@link ManagedObject} instances.
	 * @return {@link ObjectRegistry}.
	 */
	<W extends Work> ObjectRegistry<D> createObjectRegistry(
			WorkContainer<W> workContainer, ThreadState threadState);

	/**
	 * Creates the {@link Task} for the recycling of the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be recycled. Obtained by the
	 *            {@link TaskContext#getParameter()} within the {@link Task}.
	 * @return {@link Task} for the recycling this {@link ManagedObject} or
	 *         <code>null</code> if no recycling is required for this
	 *         {@link ManagedObject}.
	 */
	Job createRecycleTask(ManagedObject managedObject);

}
