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
package net.officefloor.frame.impl.execute.managedobject;

import java.util.Map;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
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
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.pool.ManagedObjectPool;
import net.officefloor.frame.spi.team.Job;

/**
 * Meta-data of the {@link ManagedObject}.
 * 
 * @author Daniel
 */
public class ManagedObjectMetaDataImpl<D extends Enum<D>> implements
		ManagedObjectMetaData<D> {

	/**
	 * Name of the {@link ManagedObject} bound within the
	 * {@link ManagedObjectScope}.
	 */
	private final String boundManagedObjectName;

	/**
	 * {@link ManagedObjectSource} of the {@link ManagedObject}.
	 */
	private final ManagedObjectSource<?, ?> source;

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
	 * Mappings for dependencies of this {@link ManagedObject}.
	 */
	private final Map<D, ManagedObjectIndex> dependencyMapping;

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
	 * @param source
	 *            {@link ManagedObjectSource} of the {@link ManagedObject}.
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
	 *            Mappings for dependencies of this {@link ManagedObject}.
	 * @param timeout
	 *            Timeout of an asynchronous operation by the
	 *            {@link ManagedObject} being managed.
	 */
	public ManagedObjectMetaDataImpl(String boundManagedObjectName,
			ManagedObjectSource<?, ?> source, ManagedObjectPool pool,
			AssetManager sourcingManager, boolean isManagedObjectAsynchronous,
			AssetManager operationsManager,
			boolean isCoordinatingManagedObject,
			Map<D, ManagedObjectIndex> dependencyMapping, long timeout) {
		this.boundManagedObjectName = boundManagedObjectName;
		this.source = source;
		this.timeout = timeout;
		this.isCoordinatingManagedObject = isCoordinatingManagedObject;
		this.dependencyMapping = dependencyMapping;
		this.pool = pool;
		this.isManagedObjectAsynchronous = isManagedObjectAsynchronous;
		this.sourcingManager = sourcingManager;
		this.operationsManager = operationsManager;
	}

	/**
	 * Loads the remaining state of this {@link ManagedObjectMetaData}.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData} of the {@link Office} containing this
	 *            {@link ManagedObjectMetaData}.
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} for the recycing of this
	 *            {@link ManagedObject}.
	 * @throws Exception
	 *             If fails to load remaining state.
	 */
	public void loadRemainingState(OfficeMetaData officeMetaData,
			FlowMetaData<?> recycleFlowMetaData) throws Exception {
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
	public <W extends Work> ObjectRegistry<D> createObjectRegistry(
			WorkContainer<W> workContainer, ThreadState threadState) {
		return new ObjectRegistryImpl<D>(workContainer, this.dependencyMapping,
				threadState);
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
					this.recycleFlowMetaData, parameter, null, -1, null);

			// Listen to process completion (handle not being recycled)
			recycleJobNode.getFlow().getThreadState().getProcessState()
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
				// Not recycle, therefore lost to pool
				ManagedObjectMetaDataImpl.this.pool
						.lostManagedObject(this.managedObject);
			}
		}

	}

}