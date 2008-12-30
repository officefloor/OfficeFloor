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
package net.officefloor.frame.impl.execute;

import java.util.Map;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.OfficeImpl;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
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
import net.officefloor.frame.spi.pool.ManagedObjectPoolContext;
import net.officefloor.frame.spi.team.Job;

/**
 * Meta-data of the {@link ManagedObject}.
 * 
 * @author Daniel
 */
public class ManagedObjectMetaDataImpl<D extends Enum<D>> implements
		ManagedObjectMetaData<D>, ManagedObjectPoolContext {

	/**
	 * Index of the {@link ManagedObject} for this
	 * {@link ManagedObjectContainer} within the {@link ProcessState} or
	 * {@link ManagedObjectMetaData#NON_PROCESS_INDEX}.
	 */
	private final int processStateManagedObjectIndex;

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
	private final Map<D, Integer> dependencyMapping;

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
	 * {@link OfficeImpl} to create the {@link Job} instances.
	 */
	private OfficeImpl office;

	/**
	 * {@link FlowMetaData} for the recycling of this {@link ManagedObject}.
	 */
	private FlowMetaData<?> recycleFlowMetaData;

	/**
	 * Initiate with meta-data of the {@link ManagedObject} to source from the
	 * {@link ProcessState}.
	 * 
	 * @param processStateManagedObjectIndex
	 *            Index of the {@link ManagedObject} within the
	 *            {@link ProcessState}.
	 */
	public ManagedObjectMetaDataImpl(int processStateManagedObjectIndex) {
		this.processStateManagedObjectIndex = processStateManagedObjectIndex;
		this.source = null;
		this.pool = null;
		this.isManagedObjectAsynchronous = false;
		this.timeout = 0;
		this.isCoordinatingManagedObject = false;
		this.dependencyMapping = null;
		this.sourcingManager = null;
		this.operationsManager = null;
		this.recycleFlowMetaData = null;
	}

	/**
	 * Initiate with meta-data of the {@link ManagedObject} to source specific
	 * to the {@link Work}.
	 * 
	 * @param source
	 *            {@link ManagedObjectSource} of the {@link ManagedObject}.
	 * @param timeout
	 *            Timeout of an asynchronous operation by the
	 *            {@link ManagedObject} being managed.
	 * @param dependencyMapping
	 *            Mappings for dependencies of this {@link ManagedObject}.
	 * @param pool
	 *            {@link ManagedObjectPool} of the {@link ManagedObject}.
	 * @param isManagedObjectAsynchronous
	 *            <code>true</code> if the {@link ManagedObject} is
	 *            {@link AsynchronousManagedObject}.
	 * @param sourcingManager
	 *            {@link AssetManager} to manage the sourcing of the
	 *            {@link ManagedObject} instances.
	 * @param operationsManager
	 *            {@link AssetManager} to manage the asynchronous operations on
	 *            the {@link ManagedObject} instances.
	 */
	public ManagedObjectMetaDataImpl(ManagedObjectSource<?, ?> source,
			ManagedObjectPool pool, AssetManager sourcingManager,
			boolean isManagedObjectAsynchronous,
			AssetManager operationsManager,
			boolean isCoordinatingManagedObject,
			Map<D, Integer> dependencyMapping, long timeout) {
		this.processStateManagedObjectIndex = ManagedObjectMetaData.NON_PROCESS_INDEX;
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
	 * @param office
	 *            {@link OfficeImpl}.
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} for the recycing of this
	 *            {@link ManagedObject}.
	 * @throws Exception
	 *             If fails to load remaining state.
	 */
	public void loadRemainingState(OfficeImpl office,
			FlowMetaData<?> recycleFlowMetaData) throws Exception {
		// Load the remaining state
		this.office = office;
		this.recycleFlowMetaData = recycleFlowMetaData;

		// Initiate the pool
		if (this.pool != null) {
			this.pool.init(this);
		}
	}

	/*
	 * ================= ManagedObjectMetaData ============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * createManagedObjectContainer
	 * (net.officefloor.frame.internal.structure.ProcessState)
	 */
	@Override
	public ManagedObjectContainer createManagedObjectContainer(
			ProcessState processState) {

		// Create the container for the Managed Object
		ManagedObjectContainer managedObjectContainer;
		if (this.processStateManagedObjectIndex == ManagedObjectMetaData.NON_PROCESS_INDEX) {
			// Source specific to this work (locking on input lock)
			managedObjectContainer = new ManagedObjectContainerImpl(this, processState);
		} else {
			// Source from process state
			managedObjectContainer = new ManagedObjectContainerProxy(
					processStateManagedObjectIndex);
		}

		// Return the managed object container
		return managedObjectContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * getProcessStateManagedObjectIndex()
	 */
	@Override
	public int getProcessStateManagedObjectIndex() {
		return this.processStateManagedObjectIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * getSourcingManager()
	 */
	@Override
	public AssetManager getSourcingManager() {
		return this.sourcingManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * getManagedObjectSource()
	 */
	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * getManagedObjectPool()
	 */
	@Override
	public ManagedObjectPool getManagedObjectPool() {
		return this.pool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.ManagedObjectMetaData#getTimeout
	 * ()
	 */
	@Override
	public long getTimeout() {
		return this.timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * isManagedObjectAsynchronous()
	 */
	@Override
	public boolean isManagedObjectAsynchronous() {
		return this.isManagedObjectAsynchronous;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * getOperationsManager()
	 */
	@Override
	public AssetManager getOperationsManager() {
		return this.operationsManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * isCoordinatingManagedObject()
	 */
	@Override
	public boolean isCoordinatingManagedObject() {
		return this.isCoordinatingManagedObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * createObjectRegistry
	 * (net.officefloor.frame.internal.structure.WorkContainer,
	 * net.officefloor.frame.internal.structure.ThreadState)
	 */
	@Override
	public <W extends Work> ObjectRegistry<D> createObjectRegistry(
			WorkContainer<W> workContainer, ThreadState threadState) {
		return new ObjectRegistryImpl<D>(workContainer, this.dependencyMapping,
				threadState);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectMetaData#
	 * createRecycleJobNode
	 * (net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
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
			JobNode recycleJobNode = this.office.createProcess(
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
		 * ====================================================================
		 * RecycleManagedObjectParameter
		 * ====================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.officefloor.frame.spi.managedobject.recycle.
		 * RecycleManagedObjectParameter#getManagedObject()
		 */
		@Override
		public MO getManagedObject() {
			return this.managedObject;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.officefloor.frame.spi.managedobject.recycle.
		 * RecycleManagedObjectParameter#reuseManagedObject(MO)
		 */
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
		 * ====================================================================
		 * ProcessCompletionListener
		 * ====================================================================
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * net.officefloor.frame.internal.structure.ProcessCompletionListener
		 * #processComplete()
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
