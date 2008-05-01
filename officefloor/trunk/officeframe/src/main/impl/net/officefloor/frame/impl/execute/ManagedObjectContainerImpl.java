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

import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetReport;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.pool.ManagedObjectPool;
import net.officefloor.frame.spi.team.ExecutionContext;
import net.officefloor.frame.spi.team.TaskContainer;

/**
 * Container of a {@link ManagedObject}.
 * 
 * @author Daniel
 */
public class ManagedObjectContainerImpl implements ManagedObjectContainer,
		ManagedObjectUser, AsynchronousListener, Asset {

	/**
	 * Value indicating that there currently is no asynchronous operation
	 * occurring.
	 */
	private static final long NO_ASYNC_OPERATION = -1;

	/**
	 * Meta-data of the {@link ManagedObject}.
	 */
	private final ManagedObjectMetaData<?> metaData;

	/**
	 * Lock for managing of the {@link ManagedObjectContainer}.
	 */
	private final Object lock;

	/**
	 * {@link AssetMonitor} for waiting on sourcing the {@link ManagedObject}
	 * instance (ie the {@link Asset}).
	 */
	private final AssetMonitor sourcingMonitor;

	/**
	 * {@link AssetMonitor} for waiting on operations on the
	 * {@link ManagedObject} instance.
	 */
	private final AssetMonitor operationsMonitor;

	/**
	 * State of this {@link ManagedObjectContainer}.
	 */
	private ManagedObjectContainerState containerState = ManagedObjectContainerState.NOT_LOADED;

	/**
	 * {@link ManagedObject} being managed.
	 */
	private ManagedObject managedObject = null;

	/**
	 * Object from the {@link ManagedObject}.
	 */
	private Object object = null;

	/**
	 * Failure obtaining the {@link ManagedObject}.
	 */
	private Throwable failure = null;

	/**
	 * Time that an asynchronous operation was started by the
	 * {@link ManagedObject}.
	 */
	private volatile long asynchronousStartTime = NO_ASYNC_OPERATION;

	/**
	 * {@link TaskContainer} to recycle the {@link ManagedObject}. This is
	 * created up front to ensure available to recycle the {@link ManagedObject}.
	 */
	private TaskContainer recycleTask;

	/**
	 * Initiate the container.
	 * 
	 * @param metaData
	 *            Meta-data of the {@link ManagedObject}.
	 * @param lock
	 *            Lock for managing the {@link ManagedObjectContainer}.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(
			ManagedObjectMetaData<D> metaData, Object lock) {
		// Store state
		this.metaData = metaData;
		this.lock = lock;

		// Create the Sourcing Monitor
		this.sourcingMonitor = this.metaData.getSourcingManager()
				.createAssetMonitor(this, this.lock);

		// Create the Operations Monitor (if needed)
		if (this.metaData.isManagedObjectAsynchronous()) {
			// Requires operations managing
			this.operationsMonitor = this.metaData.getOperationsManager()
					.createAssetMonitor(this, this.lock);
		} else {
			// No operations managing required
			this.operationsMonitor = null;
		}
	}

	/**
	 * Allows loading the {@link ManagedObject} directly when creating a
	 * {@link ProcessState}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	public void loadManagedObject(ManagedObject managedObject) {
		synchronized (this.lock) {
			// Flag managed object loaded
			this.containerState = ManagedObjectContainerState.LOADING;
			this.managedObject = managedObject;

			// Provide listener if asynchronous managed object
			if (this.metaData.isManagedObjectAsynchronous()) {
				((AsynchronousManagedObject) this.managedObject)
						.registerAsynchronousCompletionListener(this);
			}

			// Create the recycle task
			this.recycleTask = this.metaData
					.createRecycleTask(this.managedObject);

			try {
				// Obtain the Object
				this.object = this.managedObject.getObject();
			} catch (Exception ex) {
				// Flag failure to handle later
				this.failure = ex;
			}
		}
	}

	/**
	 * Unloads the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be unloaded.
	 * @param recycleTask
	 *            {@link TaskContainer} to recycle the {@link ManagedObject}.
	 */
	protected void unloadManagedObject(ManagedObject managedObject,
			TaskContainer recycleTask) {

		// Ensure have managed object to unload
		if (managedObject == null) {
			return;
		}

		// Task unload action
		if (recycleTask != null) {
			// Recycle the managed object
			recycleTask.activateTask();

		} else {
			// Return directly to pool (if pooled)
			ManagedObjectPool pool = this.metaData.getManagedObjectPool();
			if (pool != null) {
				pool.returnManagedObject(managedObject);
			}
		}
	}

	/*
	 * ====================================================================
	 * ManagedObjectContainer
	 * 
	 * Note: Synchronizing of the methods is done in the WorkContainer scoped
	 * correctly to the work or process.
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#loadManagedObject(net.officefloor.frame.spi.team.TaskContainer)
	 */
	public boolean loadManagedObject(ExecutionContext executionContext,
			TaskContainer taskContainer) {

		// Access Point: ThreadState via TaskContainer
		// Locks: ThreadState -> Work/Process

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Flag now loading the managed object
			this.containerState = ManagedObjectContainerState.LOADING;

			// Record the time the managed object is sourced
			this.asynchronousStartTime = executionContext.getTime();

			// Not loaded therefore source the managed object
			ManagedObjectPool pool = this.metaData.getManagedObjectPool();
			if (pool != null) {
				// Source from pool
				pool.sourceManagedObject(this);
			} else {
				// Source directly
				this.metaData.getManagedObjectSource()
						.sourceManagedObject(this);
			}

		case LOADING:
			// Determine if failure in loading
			if (this.failure != null) {
				// Report failure
				taskContainer.getThreadState().setFailure(this.failure);

				// Managed Object failed
				return true;
			}

			// Determine if loaded managed object
			if (this.managedObject != null) {
				// Managed object loaded
				return true;

			} else {
				// Not loaded therefore wait for being loaded
				this.sourcingMonitor.wait(taskContainer);

				// Waiting on managed object to be loaded
				return false;
			}

		default:
			// Should never be called in this state
			// (only unloaded when no interest in this managed object)
			throw new IllegalStateException(
					"Can not load an unloaded ManagedObject");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#coordinateManagedObject(net.officefloor.frame.internal.structure.WorkContainer,
	 *      net.officefloor.frame.spi.team.ExecutionContext,
	 *      net.officefloor.frame.spi.team.TaskContainer)
	 */
	@SuppressWarnings("unchecked")
	public void coordinateManagedObject(WorkContainer workContainer,
			ExecutionContext executionContext, TaskContainer taskContainer) {

		// Determine if co-ordinating managed object
		if (this.metaData.isCoordinatingManagedObject()) {

			// Create the object registry for the managed object
			ObjectRegistry objectRegistry = this.metaData.createObjectRegistry(
					workContainer, taskContainer.getThreadState());

			try {
				// Co-ordinate the managed objects
				CoordinatingManagedObject cmo = (CoordinatingManagedObject) this.managedObject;
				cmo.loadObjects(objectRegistry);
			} catch (Exception ex) {
				// Flag failure
				taskContainer.getThreadState().setFailure(ex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#isManagedObjectReady(net.officefloor.frame.spi.team.ExecutionContext,
	 *      net.officefloor.frame.spi.team.TaskContainer)
	 */
	public boolean isManagedObjectReady(ExecutionContext executionContext,
			TaskContainer taskContainer) {

		// Access Point: ThreadState via TaskContainer
		// Locks: ThreadState -> Work/Process

		// Check for failure
		if (this.failure != null) {
			// Set the failure on the thread
			taskContainer.getThreadState().setFailure(this.failure);

			// Flag failure sourcing managed object
			throw new ExecutionError(
					ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE);
		}

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Should never be called before loadManagedObject
			throw new IllegalStateException(
					"loadManagedObject must be called before isManagedObjectReady");

		case LOADING:
			// Check if loaded
			if (this.managedObject == null) {
				// Wait on managed object to be loaded
				this.sourcingMonitor.wait(taskContainer);

				// Waiting on managed object to be loaded
				return false;
			}

			// Determine if asynchronous (saves volatile access)
			if (this.metaData.isManagedObjectAsynchronous()) {

				// Single access as volatile
				long startTime = this.asynchronousStartTime;

				// Not ready if undertaking an asynchronous operation
				if (startTime != NO_ASYNC_OPERATION) {

					// Determine if asynchronous operation has timed out
					long idleTime = executionContext.getTime() - startTime;
					if (idleTime > this.metaData.getTimeout()) {
						throw new ExecutionError(
								ExecutionErrorEnum.MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT);
					}

					// Not timed out but still undertaking asynchronous
					// operation
					this.operationsMonitor.wait(taskContainer);

					// Waiting on managed object to be ready
					return false;
				}
			}

			// If here then ready
			return true;

		default:
			// Should never be called in this state
			// (only unloaded when no interest in this managed object)
			throw new IllegalStateException(
					"Can not check on an unloaded ManagedObject");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#getObject(net.officefloor.frame.internal.structure.ThreadState)
	 */
	public Object getObject(ThreadState threadState) {

		// Access Point: ThreadState via TaskContainer
		// Locks: None (object treated as final)

		// Ensure is loaded and have object
		if (this.object == null) {
			throw new ExecutionError(
					ExecutionErrorEnum.MANAGED_OBJECT_NOT_LOADED);
		}

		// Return the Object
		return this.object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#getManagedObject()
	 */
	public ManagedObject getManagedObject(ThreadState threadState) {

		// Access Point: ThreadState via TaskContainer
		// Locks: None (managed object treated as final)

		return this.managedObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectContainer#unloadManagedObject()
	 */
	public void unloadManagedObject() {

		// Access Point: ThreadState via TaskContainer
		// Locks: ThreadState -> Work/Process

		// Flag that unloading
		this.containerState = ManagedObjectContainerState.UNLOADING;

		// Unload
		this.unloadManagedObject(this.managedObject, this.recycleTask);
	}

	/*
	 * ====================================================================
	 * AsynchronousListener
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.AsynchronousListener#notifyStarted()
	 */
	public void notifyStarted() {

		// Access Point: ThreadState via ManagedObject (from TaskContainer)
		// Locks: ThreadState -> Work/Process

		// Flag an asynchronous operation started
		this.asynchronousStartTime = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.AsynchronousCompletionListener#notifyComplete()
	 */
	public void notifyComplete() {

		// Access Point: ThreadState via ManagedObject (from TaskContainer)
		// Locks: ThreadState -> Work/Process

		// Flag asynchronous operation completed
		this.asynchronousStartTime = NO_ASYNC_OPERATION;
	}

	/*
	 * ====================================================================
	 * ManagedObjectUser
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectUser#setManagedObject(net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
	public void setManagedObject(ManagedObject managedObject) {

		// Access Point: ManagedObjectSource/ManagedObjectPool
		// Locks: None (though possibly ManagedObjectPool)

		// Flag whether to wake up the tasks
		boolean isWakeUp = false;

		// Lock to ensure synchronised
		// (may be invoked by another thread from the Managed Object Source)
		synchronized (this.lock) {

			// Handle base on state
			switch (this.containerState) {
			case NOT_LOADED:
				// Discard the managed object
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleTask(managedObject));

				// Should never be called before loadManagedObject
				throw new IllegalStateException(
						"loadManagedObject must be called before setManagedObject");

			case LOADING:
				// Determine if loaded
				if (this.managedObject == null) {
					// Load the managed object
					this.managedObject = managedObject;

					// Create the recycle task for the managed object
					this.recycleTask = this.metaData
							.createRecycleTask(this.managedObject);

					// Obtain the object
					try {
						this.object = this.managedObject.getObject();
					} catch (Exception ex) {
						// Flag failure
						this.failure = ex;
					}

					// Flag no longer waiting on sourcing managed object
					this.asynchronousStartTime = NO_ASYNC_OPERATION;

					// Provide listener if asynchronous managed object
					if (this.metaData.isManagedObjectAsynchronous()) {
						((AsynchronousManagedObject) this.managedObject)
								.registerAsynchronousCompletionListener(this);
					}

					// Wake up the tasks
					isWakeUp = true;

				} else {
					// Managed object already loaded, thus unload
					this.unloadManagedObject(managedObject, this.metaData
							.createRecycleTask(managedObject));
				}
				break;

			default:
				// Discard as managed object already flagged for unloading
				// (if unloading nothing should be waiting on it)
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleTask(managedObject));
			}
		}

		// Wake up the tasks if required
		if (isWakeUp) {
			this.sourcingMonitor.notifyTasks();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectUser#setFailure(java.lang.Throwable)
	 */
	public void setFailure(Throwable cause) {

		// Access Point: ManagedObjectSource/ManagedObjectPool
		// Locks: None (though possibly ManagedObjectPool)

		// Lock to ensure synchronised and set failure before failing tasks
		// (may be invoked by another thread from the Managed Object Source)
		synchronized (this.lock) {
			this.failure = cause;
		}

		// Fail the tasks
		this.sourcingMonitor.failTasks(cause);
		if (this.operationsMonitor != null) {
			// Asynchronous so fail those waiting on operations
			this.operationsMonitor.failTasks(cause);
		}
	}

	/*
	 * ====================================================================
	 * Asset
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Asset#reportOnAsset(net.officefloor.frame.internal.structure.AssetReport)
	 */
	public void reportOnAsset(AssetReport report) {

		// Determine if failure
		if (this.failure != null) {
			// Report on the failure
			report.setFailure(this.failure);

			// No further reporting necessary
			return;
		}

		// Single access as volatile
		long startTime = this.asynchronousStartTime;

		// Not ready if undertaking an asynchronous operation
		if (startTime != NO_ASYNC_OPERATION) {

			// Determine if asynchronous operation has timed out
			if ((report.getTime() - startTime) < this.metaData.getTimeout()) {

				// Flag timeout on the managed object
				this.failure = new ExecutionError(
						ExecutionErrorEnum.MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT);
				report.setFailure(this.failure);
			}
		}
	}

	/**
	 * States of the
	 * {@link net.officefloor.frame.internal.structure.ManagedObjectContainer}.
	 */
	private enum ManagedObjectContainerState {

		/**
		 * Initial state to indicate to load the
		 * {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
		 */
		NOT_LOADED,

		/**
		 * Indicates that loading (or has loaded) the {@link ManagedObject}.
		 */
		LOADING,

		/**
		 * Indicates the {@link ManagedObject} is being unloaded.
		 */
		UNLOADING,
	}

}
