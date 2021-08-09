/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.internal.structure;

import java.util.logging.Logger;

import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Meta-data of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectMetaData<O extends Enum<O>> {

	/**
	 * Obtains the name of the {@link ManagedObject} bound within the
	 * {@link ManagedObjectScope}.
	 * 
	 * @return Name of the {@link ManagedObject} bound within the
	 *         {@link ManagedObjectScope}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the {@link Logger} for the {@link ManagedObject}.
	 * 
	 * @return {@link Logger} for the {@link ManagedObject}.
	 */
	Logger getLogger();

	/**
	 * Obtains the type of the {@link Object} returned from the
	 * {@link ManagedObject}.
	 * 
	 * @return Type of the {@link Object} returned from the {@link ManagedObject}.
	 */
	Class<?> getObjectType();

	/**
	 * <p>
	 * Obtains the instance index of the {@link ManagedObject} bound to the
	 * {@link ManagedObjectIndex}.
	 * <p>
	 * {@link ManagedObjectSource} instances that invoke {@link ProcessState}
	 * instances with the same type of Object may all be bound to the same
	 * {@link ManagedObjectIndex}.
	 * 
	 * @return Instance index of the {@link ManagedObject} bound to the
	 *         {@link ManagedObjectIndex}.
	 */
	int getInstanceIndex();

	/**
	 * Obtains the {@link FunctionLoop} for the {@link ManagedObject}.
	 * 
	 * @return {@link FunctionLoop} for the {@link ManagedObject}.
	 */
	FunctionLoop getFunctionLoop();

	/**
	 * Obtains the {@link AssetManagerReference} that manages the sourcing of the
	 * {@link ManagedObject}.
	 * 
	 * @return {@link AssetManagerReference} that manages the sourcing of the
	 *         {@link ManagedObject}.
	 */
	AssetManagerReference getSourcingManagerReference();

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
	 * Obtains the {@link ManagedObjectStartupFunction} instances for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectStartupFunction} instances for the
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectStartupFunction[] getStartupFunctions();

	/**
	 * Obtains the {@link MonitorClock}.
	 * 
	 * @return {@link MonitorClock}.
	 */
	MonitorClock getMonitorClock();

	/**
	 * Obtains the time out in milliseconds for the asynchronous operation to
	 * complete.
	 * 
	 * @return Time out in milliseconds.
	 */
	long getTimeout();

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link ContextAwareManagedObject}.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} implements
	 *         {@link ContextAwareManagedObject}.
	 */
	boolean isContextAwareManagedObject();

	/**
	 * <p>
	 * Indicates if the {@link ManagedObject} implements
	 * {@link AsynchronousManagedObject}.
	 * <p>
	 * Should the {@link ManagedObject} implement {@link AsynchronousManagedObject}
	 * then it will require checking if ready.
	 * 
	 * @return <code>true</code> if the {@link ManagedObject} implements
	 *         {@link AsynchronousManagedObject}.
	 */
	boolean isManagedObjectAsynchronous();

	/**
	 * Obtains the {@link AssetManagerReference} that manages asynchronous
	 * operations on the {@link ManagedObject}.
	 * 
	 * @return {@link AssetManagerReference} that manages asynchronous operations on
	 *         the {@link ManagedObject}.
	 */
	AssetManagerReference getOperationsManagerReference();

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
	 * {@link ManagedFunctionContainer} to wait for the {@link ManagedObject} to be
	 * ready.
	 * 
	 * @param managedFunction  {@link ManagedFunctionContainer} requesting the check
	 *                         of the {@link ManagedObject} to be ready.
	 * @param check            {@link ManagedObjectReadyCheck}.
	 * @param currentContainer Optional able to include the current
	 *                         {@link ManagedObjectContainer} for this
	 *                         {@link ManagedObjectMetaData} in ready check. May be
	 *                         <code>null</code> to not include.
	 * @return {@link FunctionState} instances to check if the dependencies of this
	 *         {@link ManagedObject} are ready.
	 */
	FunctionState checkReady(ManagedFunctionContainer managedFunction, ManagedObjectReadyCheck check,
			ManagedObjectContainer currentContainer);

	/**
	 * Creates the {@link ObjectRegistry} for the {@link ManagedObject}.
	 *
	 * @param currentContainer {@link ManagedFunctionContainer}.
	 * @return {@link ObjectRegistry}.
	 */
	ObjectRegistry<O> createObjectRegistry(ManagedFunctionContainer currentContainer);

	/**
	 * Obtains the pre-load {@link ManagedObjectAdministrationMetaData}.
	 * 
	 * @return Pre-load {@link ManagedObjectAdministrationMetaData}.
	 */
	ManagedObjectAdministrationMetaData<?, ?, ?>[] getPreLoadAdministration();

	/**
	 * Creates the {@link FunctionState} for the recycling of the
	 * {@link ManagedObject}.
	 * 
	 * @param managedObject   {@link ManagedObject} to be recycled. Obtained by the
	 *                        {@link RecycleManagedObjectParameter#getManagedObject()}.
	 * @param cleanupSequence {@link ManagedObjectCleanup}.
	 * @return {@link FunctionState} for the recycling this {@link ManagedObject} or
	 *         <code>null</code> if no recycling is required for this
	 *         {@link ManagedObject}.
	 */
	FunctionState recycle(ManagedObject managedObject, ManagedObjectCleanup cleanupSequence);

}
