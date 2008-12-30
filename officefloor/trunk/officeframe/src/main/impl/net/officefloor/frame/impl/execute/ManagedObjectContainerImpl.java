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
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.AssetReport;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.pool.ManagedObjectPool;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;

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
	private long asynchronousStartTime = NO_ASYNC_OPERATION;

	/**
	 * {@link JobNode} to recycle the {@link ManagedObject}. This is created up
	 * front to ensure available to recycle the {@link ManagedObject}.
	 */
	private JobNode recycleJobNode;

	/**
	 * {@link JobActivateSet} that was passed to
	 * {@link #loadManagedObject(JobContext, Job, JobActivateSet)} should the
	 * {@link ManagedObjectSource} provide the {@link ManagedObject}
	 * immediately.
	 */
	private JobActivateSet assetNotifySet = null;

	/**
	 * Initiate the container.
	 * 
	 * @param metaData
	 *            Meta-data of the {@link ManagedObject}.
	 * @param processState
	 *            {@link ProcessState} that this {@link ManagedObjectContainer}
	 *            resides within.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(
			ManagedObjectMetaData<D> metaData, ProcessState processState) {
		// Store state
		this.metaData = metaData;
		this.lock = processState.getProcessLock();

		// Create the Sourcing Monitor
		this.sourcingMonitor = this.metaData.getSourcingManager()
				.createAssetMonitor(this);

		// Create the Operations Monitor (if needed)
		if (this.metaData.isManagedObjectAsynchronous()) {
			// Requires operations managing
			this.operationsMonitor = this.metaData.getOperationsManager()
					.createAssetMonitor(this);
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

			// Create the recycle job node
			this.recycleJobNode = this.metaData
					.createRecycleJobNode(this.managedObject);

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
	 * @param recycleJob
	 *            {@link JobNode} to recycle the {@link ManagedObject}.
	 */
	protected void unloadManagedObject(ManagedObject managedObject,
			JobNode recycleJob) {

		// Ensure have managed object to unload
		if (managedObject == null) {
			return;
		}

		// Job unload action
		if (recycleJob != null) {
			// Recycle the managed object
			recycleJob.activateJob();

		} else {
			// Return directly to pool (if pooled)
			ManagedObjectPool pool = this.metaData.getManagedObjectPool();
			if (pool != null) {
				pool.returnManagedObject(managedObject);
			}
		}
	}

	/*
	 * =============== ManagedObjectContainer =============================
	 * 
	 * Note: Synchronizing of the methods is done by JobContainer holding a
	 * ProcessState lock.
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * loadManagedObject(net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean loadManagedObject(JobContext executionContext,
			JobNode jobNode, JobActivateSet notifySet) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Flag now loading the managed object
			this.containerState = ManagedObjectContainerState.LOADING;

			// Record the time the managed object is sourced
			this.asynchronousStartTime = executionContext.getTime();

			try {
				// Ensure notify set available if loaded immediately
				this.assetNotifySet = notifySet;

				// Not loaded therefore source the managed object
				ManagedObjectPool pool = this.metaData.getManagedObjectPool();
				if (pool != null) {
					// Source from pool
					pool.sourceManagedObject(this);
				} else {
					// Source directly
					ManagedObjectSource<?, ?> managedObjectSource = this.metaData
							.getManagedObjectSource();
					managedObjectSource.sourceManagedObject(this);
				}
			} catch (Throwable ex) {
				// Flag failure
				this.failure = ex;

				// Permanently notify of failure
				this.sourcingMonitor.failPermanently(notifySet, ex);
				if (this.operationsMonitor != null) {
					// Asynchronous so fail those waiting on operations
					this.operationsMonitor.failPermanently(notifySet, ex);
				}

				// Propagate failure
				throw new ExecutionError(
						ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE, ex);
			} finally {
				// Ensure unset the notify set
				this.assetNotifySet = null;
			}

		case LOADING:
			// Determine if failure in loading
			if (this.failure != null) {
				// Propagate the failure to the Job container to handle
				throw new ExecutionError(
						ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE,
						this.failure);
			}

			// Determine if loaded managed object
			if (this.managedObject != null) {
				// Managed object loaded
				return true;

			} else {
				// Not loaded therefore wait for being loaded
				this.sourcingMonitor.wait(jobNode, notifySet);

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
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * coordinateManagedObject
	 * (net.officefloor.frame.internal.structure.WorkContainer,
	 * net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void coordinateManagedObject(WorkContainer workContainer,
			JobContext executionContext, JobNode jobNode,
			JobActivateSet notifySet) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Determine if co-ordinating managed object
		if (this.metaData.isCoordinatingManagedObject()) {

			// Obtain the thread state
			ThreadState threadState = jobNode.getFlow().getThreadState();

			// Create the object registry for the managed object
			ObjectRegistry objectRegistry = this.metaData.createObjectRegistry(
					workContainer, threadState);

			try {
				// Co-ordinate the managed objects
				CoordinatingManagedObject cmo = (CoordinatingManagedObject) this.managedObject;
				cmo.loadObjects(objectRegistry);
			} catch (Throwable ex) {
				// Propagate the failure to the Job container to handle
				throw new ExecutionError(
						ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE, ex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * isManagedObjectReady(net.officefloor.frame.spi.team.JobContext,
	 * net.officefloor.frame.internal.structure.JobNode,
	 * net.officefloor.frame.internal.structure.JobActivateSet)
	 */
	@Override
	public boolean isManagedObjectReady(JobContext executionContext,
			JobNode jobNode, JobActivateSet notifySet) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Check for failure
		if (this.failure != null) {
			// Propagate the failure to the Job container to handle
			throw new ExecutionError(
					ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE,
					this.failure);
		}

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Should never be called before loadManagedObject
			throw new IllegalStateException(
					"loadManagedObject must be called before isManagedObjectReady");

		case LOADING:

			// Determine if not sourced or asynchronous
			if ((this.managedObject == null)
					|| this.metaData.isManagedObjectAsynchronous()) {

				// Single access for volatile access
				long startTime = this.asynchronousStartTime;

				// Check if asynchronous operation or still sourcing
				if (startTime != NO_ASYNC_OPERATION) {

					// Determine if timed out
					long idleTime = executionContext.getTime() - startTime;
					if (idleTime > this.metaData.getTimeout()) {
						if (this.managedObject == null) {
							// Timed out sourcing managed object
							throw new ExecutionError(
									ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE);
						} else {
							// Asynchronous operation timed out
							throw new ExecutionError(
									ExecutionErrorEnum.MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT);
						}
					}

					// Determine if asynchronous operation
					if (this.managedObject != null) {
						// Not ready as waiting on asynchronous operation
						this.operationsMonitor.wait(jobNode, notifySet);
						return false;
					}
				}
			}

			// Check if loaded
			if (this.managedObject == null) {
				// Wait on managed object to be loaded
				this.sourcingMonitor.wait(jobNode, notifySet);

				// Waiting on managed object to be loaded
				return false;
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
	 * @see
	 * net.officefloor.frame.internal.structure.ManagedObjectContainer#getObject
	 * (net.officefloor.frame.internal.structure.ThreadState)
	 */
	@Override
	public Object getObject(ThreadState threadState) {

		// Access Point: JobContainer via WorkContainer
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
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * getManagedObject()
	 */
	public ManagedObject getManagedObject(ThreadState threadState) {

		// Access Point: JobContainer via WorkContainer
		// Locks: None (managed object treated as final)

		return this.managedObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.internal.structure.ManagedObjectContainer#
	 * unloadManagedObject()
	 */
	@Override
	public void unloadManagedObject() {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Access Point: ThreadState via TaskContainer
		// Locks: ThreadState -> Work/Process

		// Flag that unloading
		this.containerState = ManagedObjectContainerState.UNLOADING;

		// Unload
		this.unloadManagedObject(this.managedObject, this.recycleJobNode);
	}

	/*
	 * ================ AsynchronousListener ==============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.managedobject.AsynchronousListener#notifyStarted
	 * ()
	 */
	@Override
	public void notifyStarted() {

		// Access Point: Managed Object (potentially anywhere)
		// Locks: None (or potentially ThreadState)

		// Flag asynchronous operation started
		synchronized (this.lock) {

			// Ensure asynchronous operation not already started
			if (this.asynchronousStartTime != NO_ASYNC_OPERATION) {
				return;
			}

			// Flag start of asynchronous operation
			this.asynchronousStartTime = System.currentTimeMillis();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.managedobject.AsynchronousCompletionListener
	 * #notifyComplete()
	 */
	@Override
	public void notifyComplete() {

		// Access Point: Managed Object (potentially anywhere)
		// Locks: None (or potentially ThreadState)

		// Flag asynchronous operation completed
		JobActivatableSetImpl notifySet = new JobActivatableSetImpl();
		synchronized (this.lock) {

			// Flag no asynchronous operation occurring
			this.asynchronousStartTime = NO_ASYNC_OPERATION;

			// Notify any tasks waiting on asynchronous operation
			this.operationsMonitor.notifyTasks(notifySet);
		}
		notifySet.activateJobs();
	}

	/*
	 * ================== ManagedObjectUser ===============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.frame.spi.managedobject.source.ManagedObjectUser#
	 * setManagedObject(net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
	@Override
	public void setManagedObject(ManagedObject managedObject) {

		// Access Point: ManagedObjectSource/ManagedObjectPool
		// Locks: None (though possibly ManagedObjectPool)

		// Outside lock, as may need wake up
		JobActivatableSetImpl notifySetImpl = null;

		// Lock to ensure synchronised
		// (may be invoked by another thread from the Managed Object Source)
		synchronized (this.lock) {

			// Determine if require waking up within this method
			JobActivateSet notifySet;
			if (this.assetNotifySet == null) {
				// Invoked outside loadManagedObject call
				notifySetImpl = new JobActivatableSetImpl();
				notifySet = notifySetImpl;
			} else {
				// Invoked within loadManagedObject call
				notifySet = this.assetNotifySet;
			}

			// Handle base on state
			switch (this.containerState) {
			case NOT_LOADED:
				// Discard the managed object
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleJobNode(managedObject));

				// Should never be called before loadManagedObject
				throw new IllegalStateException(
						"loadManagedObject must be called before setManagedObject");

			case LOADING:
				// Determine if loaded
				if (this.managedObject == null) {
					// Load the managed object
					this.managedObject = managedObject;

					// Create the recycle job node for the managed object
					this.recycleJobNode = this.metaData
							.createRecycleJobNode(this.managedObject);

					// Obtain the object
					try {
						this.object = this.managedObject.getObject();
					} catch (Throwable ex) {
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

					// Wake up the tasks permanently as loaded
					this.sourcingMonitor.notifyPermanently(notifySet);

				} else {
					// Managed object already loaded, thus unload
					this.unloadManagedObject(managedObject, this.metaData
							.createRecycleJobNode(managedObject));
				}
				break;

			default:
				// Discard as managed object already flagged for unloading
				// (if unloading nothing should be waiting on it)
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleJobNode(managedObject));
			}
		}

		// Determine if require to wake up tasks
		if (notifySetImpl != null) {
			notifySetImpl.activateJobs();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.managedobject.source.ManagedObjectUser#setFailure
	 * (java.lang.Throwable)
	 */
	@Override
	public void setFailure(Throwable cause) {

		// Access Point: ManagedObjectSource/ManagedObjectPool
		// Locks: None (though possibly ManagedObjectPool)

		// Outside lock, as may need wake up
		JobActivatableSetImpl notifySetImpl = null;

		// Lock to ensure synchronised and set failure before failing tasks
		// (may be invoked by another thread from the Managed Object Source)
		synchronized (this.lock) {

			// Record failure
			this.failure = cause;

			// Determine if require waking up within this method
			JobActivateSet notifySet;
			if (this.assetNotifySet == null) {
				// Invoked outside loadManagedObject call
				notifySetImpl = new JobActivatableSetImpl();
				notifySet = notifySetImpl;
			} else {
				// Invoked within loadManagedObject call
				notifySet = this.assetNotifySet;
			}

			// Permanently notify of failure
			this.sourcingMonitor.failPermanently(notifySet, cause);
			if (this.operationsMonitor != null) {
				// Asynchronous so fail those waiting on operations
				this.operationsMonitor.failPermanently(notifySet, cause);
			}
		}

		// Determine if require to wake up jobs
		if (notifySetImpl != null) {
			notifySetImpl.activateJobs();
		}
	}

	/*
	 * ================== Asset ==========================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.Asset#getAssetLock()
	 */
	@Override
	public Object getAssetLock() {
		return this.lock;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.internal.structure.Asset#reportOnAsset(net.officefloor
	 * .frame.internal.structure.AssetReport)
	 */
	@Override
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
				if (this.managedObject == null) {
					// Sourcing failure
					this.failure = new ExecutionError(
							ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE);
				} else {
					// Asynchronous operation timeout
					this.failure = new ExecutionError(
							ExecutionErrorEnum.MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT);
				}

				// Report the error
				report.setFailure(this.failure);
			}
		}
	}

	/**
	 * States of the {@link ManagedObjectContainer}.
	 */
	private enum ManagedObjectContainerState {

		/**
		 * Initial state to indicate to load the {@link ManagedObject}.
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
