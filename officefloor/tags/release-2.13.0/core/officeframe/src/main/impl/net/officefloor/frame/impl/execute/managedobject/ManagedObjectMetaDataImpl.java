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
package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessCompletionListener;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Meta-data of the {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectMetaDataImpl<D extends Enum<D>> implements
		ManagedObjectMetaData<D> {

	/**
	 * Name of the {@link ManagedObject} bound within the
	 * {@link ManagedObjectScope}.
	 */
	private final String boundManagedObjectName;

	/**
	 * Type of the {@link Object} returned from the {@link ManagedObject}.
	 */
	private final Class<?> objectType;

	/**
	 * Instance index.
	 */
	private final int instanceIndex;

	/**
	 * {@link ManagedObjectSource} of the {@link ManagedObject}.
	 */
	private final ManagedObjectSource<?, ?> source;

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link NameAwareManagedObject}.
	 */
	private final boolean isNameAwareManagedObject;

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link AsynchronousManagedObject}.
	 */
	private final boolean isManagedObjectAsynchronous;

	/**
	 * Timeout of an asynchronous operation.
	 */
	private final long timeout;

	/**
	 * Indicates if the {@link ManagedObject} implements
	 * {@link CoordinatingManagedObject}.
	 */
	private final boolean isCoordinatingManagedObject;

	/**
	 * {@link ManagedObjectIndex} for the dependencies in the index order
	 * required.
	 */
	private final ManagedObjectIndex[] dependencyMapping;

	/**
	 * {@link ManagedObjectPool} of the {@link ManagedObject}.
	 */
	private final ManagedObjectPool pool;

	/**
	 * {@link AssetManager} to manage the sourcing of the {@link ManagedObject}
	 * instances.
	 */
	private final AssetManager sourcingManager;

	/**
	 * <p>
	 * {@link AssetManager} to manage the asynchronous operations on the
	 * {@link ManagedObject} instances.
	 * <p>
	 * Should be <code>null</code> if {@link ManagedObject} is not
	 * {@link AsynchronousManagedObject}.
	 */
	private final AssetManager operationsManager;

	/**
	 * {@link ManagedObjectGovernanceMetaData} instances applicable to this
	 * {@link ManagedObject}.
	 */
	private final ManagedObjectGovernanceMetaData<?>[] governanceMetaData;

	/**
	 * {@link OfficeMetaData} containing this {@link ManagedObjectMetaData} to
	 * create the {@link Job} instances.
	 */
	private OfficeMetaData officeMetaData;

	/**
	 * {@link FlowMetaData} for the recycling of this {@link ManagedObject}.
	 */
	private FlowMetaData<?> recycleFlowMetaData;

	/**
	 * Initiate with meta-data of the {@link ManagedObject} to source specific
	 * to the {@link Work}.
	 * 
	 * @param boundManagedObjectName
	 *            Name of the {@link ManagedObject} bound within the
	 *            {@link ManagedObjectScope}.
	 * @param objectType
	 *            Type of the {@link Object} returned from the
	 *            {@link ManagedObject}.
	 * @param instanceIndex
	 *            Instance index.
	 * @param source
	 *            {@link ManagedObjectSource} of the {@link ManagedObject}.
	 * @param isNameAwareManagedObject
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link NameAwareManagedObject}.
	 * @param pool
	 *            {@link ManagedObjectPool} of the {@link ManagedObject}.
	 * @param sourcingManager
	 *            {@link AssetManager} to manage the sourcing of the
	 *            {@link ManagedObject} instances.
	 * @param isManagedObjectAsynchronous
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link AsynchronousManagedObject}.
	 * @param operationsManager
	 *            {@link AssetManager} to manage the asynchronous operations on
	 *            the {@link ManagedObject} instances.
	 * @param isCoordinatingManagedObject
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link CoordinatingManagedObject}.
	 * @param dependencyMapping
	 *            {@link ManagedObjectIndex} for the dependencies in the index
	 *            order required.
	 * @param timeout
	 *            Timeout of an asynchronous operation by the
	 *            {@link ManagedObject} being managed.
	 * @param governanceMetaData
	 *            {@link ManagedObjectGovernanceMetaData} instances applicable
	 *            to this {@link ManagedObject}.
	 */
	public ManagedObjectMetaDataImpl(String boundManagedObjectName,
			Class<?> objectType, int instanceIndex,
			ManagedObjectSource<?, ?> source, ManagedObjectPool pool,
			boolean isNameAwareManagedObject, AssetManager sourcingManager,
			boolean isManagedObjectAsynchronous,
			AssetManager operationsManager,
			boolean isCoordinatingManagedObject,
			ManagedObjectIndex[] dependencyMapping, long timeout,
			ManagedObjectGovernanceMetaData<?>[] governanceMetaData) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.objectType = objectType;
		this.instanceIndex = instanceIndex;
		this.source = source;
		this.timeout = timeout;
		this.isNameAwareManagedObject = isNameAwareManagedObject;
		this.isCoordinatingManagedObject = isCoordinatingManagedObject;
		this.dependencyMapping = dependencyMapping;
		this.pool = pool;
		this.isManagedObjectAsynchronous = isManagedObjectAsynchronous;
		this.sourcingManager = sourcingManager;
		this.operationsManager = operationsManager;
		this.governanceMetaData = governanceMetaData;
	}

	/**
	 * Loads the remaining state of this {@link ManagedObjectMetaData}.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData} of the {@link Office} containing this
	 *            {@link ManagedObjectMetaData}.
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} for the recycling of this
	 *            {@link ManagedObject}.
	 */
	public void loadRemainingState(OfficeMetaData officeMetaData,
			FlowMetaData<?> recycleFlowMetaData) {
		this.officeMetaData = officeMetaData;
		this.recycleFlowMetaData = recycleFlowMetaData;
	}

	/*
	 * ================= ManagedObjectMetaData ============================
	 */

	@Override
	public String getBoundManagedObjectName() {
		return this.boundManagedObjectName;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public int getInstanceIndex() {
		return this.instanceIndex;
	}

	@Override
	public ManagedObjectContainer createManagedObjectContainer(
			ProcessState processState) {
		return new ManagedObjectContainerImpl(this, processState);
	}

	@Override
	public AssetManager getSourcingManager() {
		return this.sourcingManager;
	}

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.source;
	}

	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.pool;
	}

	@Override
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public boolean isNameAwareManagedObject() {
		return this.isNameAwareManagedObject;
	}

	@Override
	public boolean isManagedObjectAsynchronous() {
		return this.isManagedObjectAsynchronous;
	}

	@Override
	public AssetManager getOperationsManager() {
		return this.operationsManager;
	}

	@Override
	public boolean isCoordinatingManagedObject() {
		return this.isCoordinatingManagedObject;
	}

	@Override
	public <W extends Work> boolean isDependenciesReady(
			WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context) {
		return workContainer.isManagedObjectsReady(this.dependencyMapping,
				jobContext, jobNode, activateSet, context);
	}

	@Override
	public <W extends Work> ObjectRegistry<D> createObjectRegistry(
			WorkContainer<W> workContainer, ThreadState threadState) {
		return new ObjectRegistryImpl<D>(workContainer, this.dependencyMapping,
				threadState);
	}

	@Override
	public ManagedObjectGovernanceMetaData<?>[] getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public JobNode createRecycleJobNode(ManagedObject managedObject) {
		if (this.recycleFlowMetaData == null) {
			// No recycling for managed objects
			return null;
		} else {
			// Create the recyle managed object parameter
			RecycleManagedObjectParameterImpl<ManagedObject> parameter = new RecycleManagedObjectParameterImpl<ManagedObject>(
					managedObject);

			// Create the recycle job node
			JobNode recycleJobNode = this.officeMetaData.createProcess(
					this.recycleFlowMetaData, parameter);

			// Listen to process completion (handle not being recycled)
			recycleJobNode.getJobSequence().getThreadState().getProcessState()
					.registerProcessCompletionListener(parameter);

			// Return the recycle job node
			return recycleJobNode;
		}
	}

	/**
	 * Implementation of {@link RecycleManagedObjectParameter}.
	 */
	private class RecycleManagedObjectParameterImpl<MO extends ManagedObject>
			implements RecycleManagedObjectParameter<MO>,
			ProcessCompletionListener {

		/**
		 * {@link ManagedObject} being recycled.
		 */
		private final MO managedObject;

		/**
		 * Flag indicating if has been recycled.
		 */
		private volatile boolean isRecycled = false;

		/**
		 * Initiate.
		 * 
		 * @param managedObject
		 *            {@link ManagedObject} to recycle.
		 */
		RecycleManagedObjectParameterImpl(MO managedObject) {
			this.managedObject = managedObject;
		}

		/*
		 * ============= RecycleManagedObjectParameter =======================
		 */

		@Override
		public MO getManagedObject() {
			return this.managedObject;
		}

		@Override
		public void reuseManagedObject(MO managedObject) {
			// Return to pool
			if (ManagedObjectMetaDataImpl.this.pool != null) {
				ManagedObjectMetaDataImpl.this.pool
						.returnManagedObject(managedObject);
			}

			// Flag recycled
			this.isRecycled = true;
		}

		/*
		 * ============= ProcessCompletionListener ============================
		 */

		@Override
		public void processComplete() {
			if ((!this.isRecycled)
					&& (ManagedObjectMetaDataImpl.this.pool != null)) {
				// Not recycled, therefore lost to pool
				ManagedObjectMetaDataImpl.this.pool
						.lostManagedObject(this.managedObject);
			}
		}

	}

}