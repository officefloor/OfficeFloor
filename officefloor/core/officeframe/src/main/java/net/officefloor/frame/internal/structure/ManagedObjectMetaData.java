/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Job;

/**
 * Meta-data of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectMetaData<D extends Enum<D>> {

	/**
	 * Obtains the name of the {@link ManagedObject} bound within the
	 * {@link ManagedObjectScope}.
	 * 
	 * @return Name of the {@link ManagedObject} bound within the
	 *         {@link ManagedObjectScope}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the type of the {@link Object} returned from the
	 * {@link ManagedObject}.
	 * 
	 * @return Type of the {@link Object} returned from the
	 *         {@link ManagedObject}.
	 */
	Class<?> getObjectType();

	/**
	 * <p>
	 * Obtains the instance index of the {@link ManagedObject} bound to the
	 * {@link ManagedObjectIndex}.
	 * <p>
	 * {@link ManagedObjectSource} instances that invoke {@link ProcessState}
	 * instances with the same type of Object may all be bound to the same
	 * {@link ManagedObjectIndex}. Allows similar {@link Job} processing of the
	 * {@link ManagedObject} instances.
	 * 
	 * @return Instance index of the {@link ManagedObject} bound to the
	 *         {@link ManagedObjectIndex}.
	 */
	int getInstanceIndex();

	/**
	 * Creates a new {@link ManagedObjectContainer}.
	 * 
	 * @param threadState
	 *            {@link ThreadState} responsible for managing this
	 *            {@link ManagedObjectContainer}.
	 * @return New {@link ManagedObjectContainer}.
	 */
	ManagedObjectContainer createManagedObjectContainer(ThreadState threadState);

	/**
	 * Obtains the {@link FunctionLoop} for the {@link ManagedObject}.
	 * 
	 * @return {@link FunctionLoop} for the {@link ManagedObject}.
	 */
	FunctionLoop getJobNodeLoop();

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
	 * Obtains the {@link OfficeClock}.
	 * 
	 * @return {@link OfficeClock}.
	 */
	OfficeClock getOfficeClock();

	/**
	 * Obtains the time out in milliseconds for the asynchronous operation to
	 * complete.
	 * 
	 * @return Time out in milliseconds.
	 */
	long getTimeout();

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link NameAwareManagedObject}.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} implements
	 *         {@link NameAwareManagedObject}.
	 */
	boolean isNameAwareManagedObject();

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
	 * Obtains the {@link ManagedObjectGovernanceMetaData} applicable to this
	 * {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectGovernanceMetaData} applicable to this
	 *         {@link ManagedObject}.
	 */
	ManagedObjectGovernanceMetaData<?>[] getGovernanceMetaData();

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link CoordinatingManagedObject}.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} implements
	 *         {@link CoordinatingManagedObject}.
	 */
	boolean isCoordinatingManagedObject();

	/**
	 * <p>
	 * Creates a {@link FunctionState} to check if the dependencies of this
	 * {@link ManagedObject} are ready.
	 * <p>
	 * Should a {@link ManagedObject} not be ready, then will latch the
	 * {@link ManagedFunctionContainer} to wait for the {@link ManagedObject} to be ready.
	 * 
	 * @param check
	 *            {@link ManagedObjectReadyCheck}.
	 * @param currentContainer
	 *            Optional able to include the current
	 *            {@link ManagedObjectContainer} for this
	 *            {@link ManagedObjectMetaData} in ready check. May be
	 *            <code>null</code> to not include.
	 * @return {@link FunctionState} instances to check if the dependencies of this
	 *         {@link ManagedObject} are ready.
	 */
	FunctionState createReadyCheckJobNode(ManagedObjectReadyCheck check, WorkContainer<?> workContainer,
			ManagedObjectContainer currentContainer);

	/**
	 * Creates the {@link ObjectRegistry} for the {@link ManagedObject}.
	 *
	 * @param <W>
	 *            {@link Work} type.
	 * @param workContainer
	 *            {@link WorkContainer} to obtain the coordinating
	 *            {@link ManagedObject} instances.
	 * @param threadState
	 *            {@link ThreadState} to provide access to the
	 *            {@link ProcessState} bound {@link ManagedObject} instances.
	 * @return {@link ObjectRegistry}.
	 */
	<W extends Work> ObjectRegistry<D> createObjectRegistry(WorkContainer<W> workContainer, ThreadState threadState);

	/**
	 * Creates the {@link FunctionState} for the recycling of the
	 * {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be recycled. Obtained by the
	 *            {@link RecycleManagedObjectParameter#getManagedObject()}.
	 * @param cleanupSequence
	 *            {@link ManagedObjectCleanup}.
	 * @return {@link FunctionState} for the recycling this {@link ManagedObject} or
	 *         <code>null</code> if no recycling is required for this
	 *         {@link ManagedObject}.
	 */
	FunctionState createRecycleJobNode(ManagedObject managedObject, ManagedObjectCleanup cleanupSequence);

}