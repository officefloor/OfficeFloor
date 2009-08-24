/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Container of a {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
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
	 * {@link AssetMonitor} for waiting to source the {@link ManagedObject}
	 * instance (the {@link Asset}).
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
	 * {@link PropagateEscalationError} containing the cause of failure to
	 * obtain the {@link ManagedObject}.
	 */
	private PropagateEscalationError failure = null;

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
	 * {@link JobNodeActivateSet} that was passed to
	 * {@link #loadManagedObject(JobContext, Job, JobNodeActivateSet)} should
	 * the {@link ManagedObjectSource} provide the {@link ManagedObject}
	 * immediately.
	 */
	private JobNodeActivateSet assetActivateSet = null;

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
		this.metaData = metaData;
		this.lock = processState.getProcessLock();

		// Create the monitor to source the managed object
		this.sourcingMonitor = this.metaData.getSourcingManager()
				.createAssetMonitor(this);

		// Create the monitor for asynchronous operations (if needed)
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
	 * Initiate the container with a provided {@link ManagedObject}.
	 *
	 * @param managedObject
	 *            {@link ManagedObject} triggering the {@link ProcessState}.
	 * @param metaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 * @param processState
	 *            {@link ProcessState} that this {@link ManagedObjectContainer}
	 *            resides within.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(
			ManagedObject managedObject, ManagedObjectMetaData<D> metaData,
			ProcessState processState) {
		this(metaData, processState);

		// Flag managed object loaded
		this.managedObject = managedObject;
		this.containerState = ManagedObjectContainerState.LOADED;

		try {
			// Provide listener if asynchronous managed object
			if (this.metaData.isManagedObjectAsynchronous()) {
				((AsynchronousManagedObject) this.managedObject)
						.registerAsynchronousCompletionListener(this);
			}
		} catch (Throwable ex) {
			// Flag failure to handle later when Job attempts to use it
			this.setFailedState(new FailedToSourceManagedObjectEscalation(
					this.metaData.getObjectType(), ex), null);
		}

		// Create the recycle job node
		this.recycleJobNode = this.metaData
				.createRecycleJobNode(this.managedObject);
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
	 * Note: Synchronising of the methods is done by JobContainer holding a
	 * ProcessState lock.
	 */

	@Override
	public void loadManagedObject(JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet activateSet) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Always propagate any failure to the Job container to handle.
		// May have had failure immediately in loading to process state.
		if (this.failure != null) {
			throw this.failure;
		}

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Flag now loading the managed object
			this.containerState = ManagedObjectContainerState.LOADING;

			try {
				// Ensure activate set available if loaded immediately
				this.assetActivateSet = activateSet;

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
				// Flag failed to source Managed Object
				this.setFailedState(new FailedToSourceManagedObjectEscalation(
						this.metaData.getObjectType(), ex), activateSet);
			} finally {
				// Ensure clear activate set
				this.assetActivateSet = null;
			}

			// Propagate any failure in loading to the JobContainer
			if (this.failure != null) {
				throw this.failure;
			}

			// Determine if loaded managed object
			if (this.managedObject == null) {
				// Record time that attempted to source the managed object
				this.asynchronousStartTime = executionContext.getTime();
			}

		case LOADING:
			// Loading triggered so continue on to coordinate
			return;

		case LOADED:
		case COORDINATING:
		case OBJECT_AVAILABLE:
			// Managed Object loaded
			return;

		case UNLOADING:
			// Should never be called in this state
			// (only unloaded when no interest in this managed object)
			throw new IllegalStateException(
					"Can not load an unloaded ManagedObject");

		default:
			throw new IllegalStateException("Unknown container state "
					+ this.containerState);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean coordinateManagedObject(WorkContainer workContainer,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet activateSet) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Always propagate any failure to the Job container to handle
		if (this.failure != null) {
			throw this.failure;
		}

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Should never be called before loadManagedObject
			throw new IllegalStateException(
					"loadManagedObject must be called before coordinateManagedObject");

		case LOADING:
			// Still loading managed object so wait until loaded
			if (!this.sourcingMonitor.waitOnAsset(jobNode, activateSet)) {
				throw new IllegalStateException(
						"Must be able to wait until Managed Object loaded");
			}
			return false;

		case LOADED:
			// Determine if require coordinating Managed Object
			if (this.metaData.isCoordinatingManagedObject()) {

				// Ensure the dependencies are ready for coordination
				if (!this.metaData.isDependenciesReady(workContainer,
						executionContext, jobNode, activateSet)) {
					// Dependencies must be ready before coordinating
					return false;
				}

				// Ensure this managed object is ready
				if (!this.checkManagedObjectReady(executionContext, jobNode,
						activateSet, ManagedObjectContainerState.LOADED)) {
					// This object must be ready before coordinating it
					return false;
				}

				// Obtain the thread state
				ThreadState threadState = jobNode.getFlow().getThreadState();

				// Create the object registry for the managed object
				ObjectRegistry objectRegistry = this.metaData
						.createObjectRegistry(workContainer, threadState);

				try {
					// Coordinate the managed objects
					CoordinatingManagedObject cmo = (CoordinatingManagedObject) this.managedObject;
					cmo.loadObjects(objectRegistry);
				} catch (Throwable ex) {
					// Flag failure and propagate to the JobContainer
					this.setFailedState(
							new FailedToSourceManagedObjectEscalation(
									this.metaData.getObjectType(), ex),
							activateSet);
					throw this.failure;
				}
			}

			// Flag that now coordinating managed object
			this.containerState = ManagedObjectContainerState.COORDINATING;

		case COORDINATING:

			// Wait until managed object ready
			if (!this.checkManagedObjectReady(executionContext, jobNode,
					activateSet, ManagedObjectContainerState.COORDINATING)) {
				// Must be ready before obtaining the object
				return false;
			}

			try {
				// Obtain the object
				this.object = this.managedObject.getObject();
			} catch (Throwable ex) {
				// Flag in failed state
				this.setFailedState(new FailedToSourceManagedObjectEscalation(
						this.metaData.getObjectType(), ex), activateSet);
				throw this.failure;
			}

			// Flag that object available
			this.containerState = ManagedObjectContainerState.OBJECT_AVAILABLE;

		case OBJECT_AVAILABLE:
			// Object available and coordinated
			return true;

		case UNLOADING:
			// Should never be called in this state
			// (only unloaded when no interest in this managed object)
			throw new IllegalStateException(
					"Can not coordinate an unloaded ManagedObject");

		default:
			throw new IllegalStateException("Unknown container state "
					+ this.containerState);
		}
	}

	@Override
	public boolean isManagedObjectReady(JobContext executionContext,
			JobNode jobNode, JobNodeActivateSet activateSet) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Always propagate the failure to the Job container to handle
		if (this.failure != null) {
			throw this.failure;
		}

		// Return check on ready (object must be available)
		return this.checkManagedObjectReady(executionContext, jobNode,
				activateSet, ManagedObjectContainerState.OBJECT_AVAILABLE);
	}

	/**
	 * Checks that the {@link ManagedObject} is ready.
	 *
	 * @param executionContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param expectedContainerState
	 *            Should the {@link ManagedObject} be ready it is illegal for it
	 *            to be in any other state other than this one.
	 * @return <code>true</code> if the {@link ManagedObject} is ready.
	 */
	private boolean checkManagedObjectReady(JobContext executionContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ManagedObjectContainerState expectedContainerState) {

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Should never be called before loadManagedObject
			throw new IllegalStateException(
					"loadManagedObject must be called before isManagedObjectReady");

		case LOADING:
		case LOADED:
		case COORDINATING:
		case OBJECT_AVAILABLE:

			// Determine if not sourced or asynchronous
			if ((this.managedObject == null)
					|| this.metaData.isManagedObjectAsynchronous()) {

				// Check if asynchronous operation or still to source
				if (this.asynchronousStartTime != NO_ASYNC_OPERATION) {

					// Determine if timed out
					long idleTime = executionContext.getTime()
							- this.asynchronousStartTime;
					if (idleTime > this.metaData.getTimeout()) {

						// Obtain the timeout failure
						Throwable timeoutFailure;
						if (this.managedObject == null) {
							// Source the managed object timed out
							timeoutFailure = new SourceManagedObjectTimedOutEscalation(
									this.metaData.getObjectType());
						} else {
							// Asynchronous operation timed out
							timeoutFailure = new ManagedObjectOperationTimedOutEscalation(
									this.metaData.getObjectType());
						}

						// Flag failed state and propagate to JobContainer
						this.setFailedState(timeoutFailure, activateSet);
						throw this.failure;
					}

					// Determine if asynchronous operation
					if (this.managedObject != null) {
						// Not ready as waiting on asynchronous operation
						if (!this.operationsMonitor.waitOnAsset(jobNode,
								activateSet)) {
							throw new IllegalStateException(
									"Must be able to wait on asynchronous operation");
						}
						return false;
					}
				}
			}

			// Check if loaded
			if (this.managedObject == null) {
				// Wait on managed object to be loaded
				this.sourcingMonitor.waitOnAsset(jobNode, activateSet);

				// Waiting on managed object to be loaded
				return false;
			}

			// Ready so should be in expected container state
			if (this.containerState != expectedContainerState) {
				throw new IllegalStateException(
						"Ready but in wrong container state [expected "
								+ expectedContainerState + ", actual "
								+ this.containerState + "]");
			}

			// Ready for use
			return true;

		case UNLOADING:
			// Should never be called in this state
			// (only unloaded when no interest in this managed object)
			throw new IllegalStateException(
					"Can not check on an unloaded ManagedObject");

		default:
			throw new IllegalStateException("Unknown container state "
					+ this.containerState);
		}
	}

	@Override
	public Object getObject(ThreadState threadState) {

		// Access Point: JobContainer via WorkContainer
		// Locks: Unknown (object treated as final)

		// Ensure have Object
		if (this.object == null) {
			throw new PropagateEscalationError(
					new FailedToSourceManagedObjectEscalation(this.metaData
							.getObjectType(), new NullPointerException(
							"No object provided from ManagedObject")));
		}

		// Return the Object
		return this.object;
	}

	@Override
	public ManagedObject getManagedObject(ThreadState threadState) {

		// Access Point: JobContainer via WorkContainer
		// Locks: Unknown (managed object treated as final)

		// Ensure have Managed Object
		if (this.managedObject == null) {
			throw new PropagateEscalationError(
					new FailedToSourceManagedObjectEscalation(this.metaData
							.getObjectType(), new NullPointerException(
							"ManagedObject not loaded")));
		}

		// Return the Managed Object
		return this.managedObject;
	}

	@Override
	public void unloadManagedObject(JobNodeActivateSet activateSet) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Flag that unloading
		this.containerState = ManagedObjectContainerState.UNLOADING;

		// Activate jobs permanently as Asset no longer being used
		this.sourcingMonitor.activateJobNodes(activateSet, true);
		if (this.operationsMonitor != null) {
			this.operationsMonitor.activateJobNodes(activateSet, true);
		}

		// Unload the managed object
		this.unloadManagedObject(this.managedObject, this.recycleJobNode);

		// Release reference to managed object to not unload again
		this.managedObject = null;
	}

	/*
	 * ================ AsynchronousListener ==============================
	 */

	@Override
	public void notifyStarted() {

		// Access Point: Managed Object (potentially anywhere)
		// Locks: Unknown (as may be called by ManagedObject at any time)

		// Flag asynchronous operation started
		synchronized (this.lock) {

			// Ignore starting operation if in failed state
			if (this.failure != null) {
				return;
			}

			// Ensure asynchronous operation not already started
			if (this.asynchronousStartTime != NO_ASYNC_OPERATION) {
				return;
			}

			// Flag start of asynchronous operation
			this.asynchronousStartTime = System.currentTimeMillis();
		}
	}

	@Override
	public void notifyComplete() {

		// Access Point: Managed Object (potentially anywhere)
		// Locks: Unknown (as may be called by ManagedObject at any time)

		// Flag asynchronous operation completed
		synchronized (this.lock) {

			// Ignore completing operation if in failed state
			if (this.failure != null) {
				return;
			}

			// Flag no asynchronous operation occurring
			this.asynchronousStartTime = NO_ASYNC_OPERATION;

			// Activate any jobs waiting on asynchronous operation.
			// Can not activate jobs by this thread as not sure of locks.
			this.operationsMonitor.activateJobNodes(null, false);
		}
	}

	/*
	 * ================== ManagedObjectUser ===============================
	 */

	@Override
	public void setManagedObject(ManagedObject managedObject) {

		// Access Point: ManagedObjectSource/ManagedObjectPool
		// Locks: Unknown (as may be called by ManagedObjectSource at any time)

		// Lock to ensure synchronised.
		// (may be invoked by another thread from the Managed Object Source)
		synchronized (this.lock) {

			// Determine if container in failed state
			if (this.failure != null) {
				// Discard the managed object and no further processing
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleJobNode(managedObject));
				return;
			}

			// Obtain the activate set.
			// May be null if called by Managed Object Source at a later time.
			JobNodeActivateSet activateSet = this.assetActivateSet;

			// Handle based on state
			switch (this.containerState) {
			case NOT_LOADED:
				// Discard the managed object
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleJobNode(managedObject));

				// Should never be called before loadManagedObject
				throw new IllegalStateException(
						"loadManagedObject must be called before setManagedObject");

			case LOADING:
				// Determine if already loaded
				if (this.managedObject != null) {
					// Discard managed object as already loaded
					this.unloadManagedObject(managedObject, this.metaData
							.createRecycleJobNode(managedObject));
					return; // discarded, nothing further
				}

				// Ensure have the managed object
				if (managedObject == null) {
					// Flag no managed object provided
					this.setFailedState(
							new FailedToSourceManagedObjectEscalation(
									this.metaData.getObjectType(),
									new NullPointerException(
											"No ManagedObject provided")),
							activateSet);

					// Allow load/isReady methods to propagate failure
					return;
				}

				// Flag loaded and no longer waiting to source
				this.managedObject = managedObject;
				this.containerState = ManagedObjectContainerState.LOADED;
				this.asynchronousStartTime = NO_ASYNC_OPERATION;

				try {
					// Provide listener if asynchronous managed object
					if (this.metaData.isManagedObjectAsynchronous()) {
						((AsynchronousManagedObject) this.managedObject)
								.registerAsynchronousCompletionListener(this);
					}
				} catch (Throwable ex) {
					// Flag in failed state
					this.setFailedState(
							new FailedToSourceManagedObjectEscalation(
									this.metaData.getObjectType(), ex),
							activateSet);

					// Allow load/isReady methods to propagate failure
					return;
				}

				// Create the recycle job node for the managed object
				this.recycleJobNode = this.metaData
						.createRecycleJobNode(this.managedObject);

				// Activate jobs waiting to source permanently
				this.sourcingMonitor.activateJobNodes(activateSet, true);

				break;

			case LOADED:
			case COORDINATING:
			case OBJECT_AVAILABLE:
				// Discard the managed object as already have a Managed Object
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleJobNode(managedObject));
				break;

			case UNLOADING:
				// Discard as managed object already flagged for unloading
				// (if unloading nothing should be waiting on it)
				this.unloadManagedObject(managedObject, this.metaData
						.createRecycleJobNode(managedObject));
				break;

			default:
				throw new IllegalStateException("Unknown container state "
						+ this.containerState);
			}
		}
	}

	@Override
	public void setFailure(Throwable cause) {

		// Access Point: ManagedObjectSource/ManagedObjectPool
		// Locks: Unknown (as may be called by ManagedObjectSource at any time)

		// Lock to ensure synchronised and set failure before failing jobs
		// (may be invoked by another thread from the Managed Object Source)
		synchronized (this.lock) {

			// Obtain the activate set.
			// May be null if called by Managed Object Source at a later time.
			JobNodeActivateSet activateSet = this.assetActivateSet;

			// Flag in failed state
			this.setFailedState(new FailedToSourceManagedObjectEscalation(
					this.metaData.getObjectType(), cause), activateSet);
		}
	}

	/*
	 * ================== Asset ==========================================
	 */

	@Override
	public void checkOnAsset(CheckAssetContext context) {
		synchronized (this.lock) {

			// Determine if failure
			if (this.failure != null) {
				// Fail jobs waiting on this managed object permanently
				context.failJobNodes(this.failure.getCause(), true);

				// No further checking necessary
				return;
			}

			// Not ready if undertaking an asynchronous operation
			if (this.asynchronousStartTime != NO_ASYNC_OPERATION) {

				// Determine if asynchronous operation has timed out
				long idleTime = context.getTime() - this.asynchronousStartTime;
				if (idleTime > this.metaData.getTimeout()) {

					// Obtain the time out failure
					Throwable timeoutFailure;
					if (this.managedObject == null) {
						// Source managed object timed out
						timeoutFailure = new SourceManagedObjectTimedOutEscalation(
								this.metaData.getObjectType());
					} else {
						// Asynchronous operation timed out
						timeoutFailure = new ManagedObjectOperationTimedOutEscalation(
								this.metaData.getObjectType());
					}

					// Fail job nodes waiting on this
					context.failJobNodes(timeoutFailure, true);

					// Flag in failed state (use OfficeManager to fail)
					this.setFailedState(timeoutFailure, null);
				}
			}
		}
	}

	/**
	 * Sets the {@link ManagedObjectContainer} into a failed state and makes the
	 * {@link AssetMonitor} instances aware.
	 *
	 * @param failure
	 *            Failure.
	 * @param activateSet
	 *            {@link JobNodeActivateSet} for the {@link AssetMonitor}
	 *            instances. May be <code>null</code> to use the
	 *            {@link OfficeManager}.
	 */
	private void setFailedState(Throwable failure,
			JobNodeActivateSet activateSet) {

		// Flag failure (puts container into failed state)
		this.failure = new PropagateEscalationError(failure);

		// Permanently fail job nodes
		this.sourcingMonitor.failJobNodes(activateSet, failure, true);
		if (this.operationsMonitor != null) {
			// Asynchronous so include permanently failing operations
			this.operationsMonitor.failJobNodes(activateSet, failure, true);
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
		 * Indicates that loading the {@link ManagedObject} from the
		 * {@link ManagedObjectSource}.
		 */
		LOADING,

		/**
		 * Indicates that {@link ManagedObject} has been obtained from the
		 * {@link ManagedObjectSource}.
		 */
		LOADED,

		/**
		 * Indicates coordinating the {@link ManagedObject}.
		 */
		COORDINATING,

		/**
		 * Indicates that the {@link Object} from the {@link ManagedObject} is
		 * available.
		 */
		OBJECT_AVAILABLE,

		/**
		 * Indicates the {@link ManagedObject} is being unloaded.
		 */
		UNLOADING
	}

}