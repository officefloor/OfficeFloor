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

import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.ExtensionInterfaceExtractor;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

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
	 * Listing of potential {@link ActiveGovernance}.
	 */
	private final ActiveGovernance<?, ?>[] activeGovernances;

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
	 * {@link TeamIdentifier} of the current {@link Team} loading the
	 * {@link ManagedObject} should the {@link ManagedObjectSource} provide the
	 * {@link ManagedObject} immediately.
	 */
	private TeamIdentifier currentTeam = null;

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

		// Create the active governances
		this.activeGovernances = new ActiveGovernance[this.metaData
				.getGovernanceMetaData().length];
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
			// Provide bound name if name aware
			if (this.metaData.isNameAwareManagedObject()) {
				((NameAwareManagedObject) this.managedObject)
						.setBoundManagedObjectName(this.metaData
								.getBoundManagedObjectName());
			}

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
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} unloading
	 *            the {@link ManagedObject}.
	 */
	protected void unloadManagedObject(ManagedObject managedObject,
			JobNode recycleJob, TeamIdentifier currentTeam) {

		// Ensure have managed object to unload
		if (managedObject == null) {
			return;
		}

		// Job unload action
		if (recycleJob != null) {
			// Recycle the managed object
			recycleJob.activateJob(currentTeam);

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
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext context) {

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
				// Ensure activate set and team available if loaded immediately
				this.assetActivateSet = activateSet;
				this.currentTeam = currentTeam;

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
				// Ensure clear activate set and team
				this.assetActivateSet = null;
				this.currentTeam = null;
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
		case GOVERNING:
		case GOVERNED:
		case COORDINATING:
		case OBJECT_AVAILABLE:
			// Managed Object loaded
			return;

		case UNLOAD_WAITING_GOVERNANCE:
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <W extends Work> boolean governManagedObject(
			WorkContainer<W> workContainer, JobContext jobContext,
			JobNode jobNode, JobNodeActivateSet activateSet,
			ContainerContext context) {

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
					"loadManagedObject must be called before governManagedObject");

		case LOADING:
			// Still loading managed object so wait until loaded
			if (!this.sourcingMonitor.waitOnAsset(jobNode, activateSet)) {
				throw new IllegalStateException(
						"Must be able to wait until Managed Object loaded");
			}
			context.flagJobToWait();
			return false;

		case LOADED:
			// Flag now attempting to govern
			this.containerState = ManagedObjectContainerState.GOVERNING;

		case GOVERNING:
			// Ensure this managed object is loaded for governing
			if (!this.checkManagedObjectReady(jobContext, workContainer,
					jobNode, activateSet, context,
					ManagedObjectContainerState.LOADED)) {
				// This object must be loaded before governing it
				context.flagJobToWait();
				return false;
			}

			// Now governed as always need to check governance
			this.containerState = ManagedObjectContainerState.GOVERNED;

		case GOVERNED:
		case COORDINATING:
		case OBJECT_AVAILABLE:
			// Must always determine governance (as can change between jobs)

			// Identify the applicable governance for this managed object
			ManagedObjectGovernanceMetaData<?>[] governanceMetaDatas = this.metaData
					.getGovernanceMetaData();

			// Iterate over governance and govern by active governance
			ThreadState thread = jobNode.getJobSequence().getThreadState();
			NEXT_GOVERNANCE: for (int i = 0; i < governanceMetaDatas.length; i++) {
				ManagedObjectGovernanceMetaData<?> governanceMetaData = governanceMetaDatas[i];

				// Determine if already under active governance
				ActiveGovernance activeGovernance = this.activeGovernances[i];
				if (activeGovernance != null) {

					// Ensure still active
					if (activeGovernance.isActive()) {
						continue NEXT_GOVERNANCE; // already active
					}

					// No longer active
					this.activeGovernances[i] = null;
				}

				// Determine if the governance is active
				int governanceIndex = governanceMetaData.getGovernanceIndex();
				if (!thread.isGovernanceActive(governanceIndex)) {
					continue NEXT_GOVERNANCE; // not active, so not govern
				}

				// Obtain the governance
				GovernanceContainer governance = thread
						.getGovernanceContainer(governanceIndex);

				// Obtain the extension interface
				ExtensionInterfaceExtractor<?> extractor = governanceMetaData
						.getExtensionInterfaceExtractor();
				Object extensionInterface = this
						.extractExtensionInterface(extractor);

				// Create and register the governance for the managed object
				activeGovernance = governance.createActiveGovernance(
						extensionInterface, this, i, workContainer);
				this.activeGovernances[i] = activeGovernance;

				// Add the governance for activation
				GovernanceActivity<?, ?> activity = activeGovernance
						.createGovernActivity();
				context.addGovernanceActivity(activity);
			}

			// Successfully now governed, or governance ready
			return true;

		case UNLOAD_WAITING_GOVERNANCE:
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean coordinateManagedObject(WorkContainer workContainer,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Always propagate any failure to the Job container to handle
		if (this.failure != null) {
			throw this.failure;
		}

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
		case LOADING:
		case LOADED:
		case GOVERNING:
			// Should never be called before governManagedObject
			throw new IllegalStateException(
					"governManagedObject must be called before coordinateManagedObject");

		case GOVERNED:
			// Determine if require coordinating Managed Object
			if (this.metaData.isCoordinatingManagedObject()) {

				// Ensure the dependencies are ready for coordination
				if (!this.metaData.isDependenciesReady(workContainer,
						executionContext, jobNode, activateSet, context)) {
					// Dependencies must be ready before coordinating
					context.flagJobToWait();
					return false;
				}

				// Ensure this managed object is ready
				if (!this.checkManagedObjectReady(executionContext,
						workContainer, jobNode, activateSet, context,
						ManagedObjectContainerState.GOVERNED)) {
					// This object must be ready before coordinating it
					context.flagJobToWait();
					return false;
				}

				// Obtain the thread state
				ThreadState threadState = jobNode.getJobSequence()
						.getThreadState();

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
			if (!this.checkManagedObjectReady(executionContext, workContainer,
					jobNode, activateSet, context,
					ManagedObjectContainerState.COORDINATING)) {
				// Must be ready before obtaining the object
				context.flagJobToWait();
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

		case UNLOAD_WAITING_GOVERNANCE:
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
	@SuppressWarnings("rawtypes")
	public boolean isManagedObjectReady(WorkContainer workContainer,
			JobContext executionContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context) {

		// Access Point: JobContainer via WorkContainer
		// Locks: ThreadState -> ProcessState

		// Always propagate the failure to the Job container to handle
		if (this.failure != null) {
			throw this.failure;
		}

		// Return check on ready (object must be available)
		return this.checkManagedObjectReady(executionContext, workContainer,
				jobNode, activateSet, context,
				ManagedObjectContainerState.OBJECT_AVAILABLE);
	}

	/**
	 * Checks that the {@link ManagedObject} is ready.
	 * 
	 * @param executionContext
	 *            {@link JobContext}.
	 * @param workContainer
	 *            {@link WorkContainer}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param context
	 *            {@link ContainerContext}.
	 * @param expectedContainerState
	 *            Should the {@link ManagedObject} be ready it is illegal for it
	 *            to be in any other state other than this one.
	 * @return <code>true</code> if the {@link ManagedObject} is ready.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean checkManagedObjectReady(JobContext executionContext,
			WorkContainer workContainer, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context,
			ManagedObjectContainerState expectedContainerState) {

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Should never be called before loadManagedObject
			throw new IllegalStateException(
					"loadManagedObject must be called before isManagedObjectReady");

		case LOADING:
		case LOADED:
		case GOVERNING:
		case GOVERNED:
		case COORDINATING:
		case OBJECT_AVAILABLE:
		case UNLOAD_WAITING_GOVERNANCE:

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

				// Chaining of coordination causes dependency to only be loaded
				switch (this.containerState) {
				case GOVERNING:
					// Only ready if expected loaded for governing
					return (expectedContainerState == ManagedObjectContainerState.LOADED);

				case LOADED:
					// Loaded, now need governing
					boolean isGoverned = this.governManagedObject(
							workContainer, executionContext, jobNode,
							activateSet, context);
					if (!isGoverned) {
						return false; // Waiting on governance
					}
					// Governance in place, so continue on to coordinate

				case GOVERNED:
					// As this a dependency loaded but not coordinated.
					// If coordination successful then ready.
					boolean isCoordinated = this.coordinateManagedObject(
							workContainer, executionContext, jobNode,
							activateSet, context);
					if (!isCoordinated) {
						return false; // Waiting on coordination
					}
					// Coordination in place, so continue on

				case OBJECT_AVAILABLE:
					// Object available and ready
					return true;

				case UNLOAD_WAITING_GOVERNANCE:
					// Ready for enforce/disregard governance
					return true;

				default:
					// Continue on to be not expected state
					break;
				}

				// Fail as not expected state
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
		// Locks: Unknown (though should have synchronise before call)

		// Only return Object if in valid state
		if (ManagedObjectContainerState.OBJECT_AVAILABLE
				.equals(this.containerState)) {
			// Return the Object
			return this.object;
		}

		// Incorrect state if here
		throw new PropagateEscalationError(
				new FailedToSourceManagedObjectEscalation(
						this.metaData.getObjectType(),
						new IllegalStateException(
								"ManagedObject in incorrect state "
										+ this.containerState
										+ " to obtain Object")));
	}

	@Override
	public <I> I extractExtensionInterface(
			ExtensionInterfaceExtractor<I> extractor) {

		// Access Point: WorkContainer (via DutyJob)
		// Locks: ThreadState -> ProcessState

		// Ensure have Managed Object
		if (this.managedObject == null) {
			throw new PropagateEscalationError(
					new FailedToSourceManagedObjectEscalation(this.metaData
							.getObjectType(), new NullPointerException(
							"ManagedObject not loaded")));
		}

		// Return the extracted extension interface
		return extractor.extractExtensionInterface(this.managedObject,
				this.metaData);
	}

	@Override
	public void unregisterManagedObjectFromGovernance(
			ActiveGovernance<?, ?> governance, JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam) {

		// Access Point: GovernanceContainer
		// Locks: ThreadState -> ProcessState

		// Unregister the active governance
		int index = governance.getManagedObjectRegisteredIndex();
		this.activeGovernances[index] = null;

		// Determine if managed object waiting on governance to unload
		if (this.containerState == ManagedObjectContainerState.UNLOAD_WAITING_GOVERNANCE) {
			// Attempt to unload the managed object
			this.unloadManagedObject(activateSet, currentTeam);
		}
	}

	@Override
	public void unloadManagedObject(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam) {

		// Access Point: JobContainer, GovernanceContainer (unregister)
		// Locks: ThreadState -> ProcessState

		// Flag that possibly waiting on governance to unload
		this.containerState = ManagedObjectContainerState.UNLOAD_WAITING_GOVERNANCE;

		// Ensure no active governance
		boolean isActiveGovernance = false;
		for (int i = 0; i < this.activeGovernances.length; i++) {
			if (this.activeGovernances[i] != null) {
				isActiveGovernance = true;
			}
		}
		if (isActiveGovernance) {
			return; // Do not unload if active governance
		}

		// Flag that unloading
		this.containerState = ManagedObjectContainerState.UNLOADING;

		// Activate jobs permanently as Asset no longer being used
		this.sourcingMonitor.activateJobNodes(activateSet, true);
		if (this.operationsMonitor != null) {
			this.operationsMonitor.activateJobNodes(activateSet, true);
		}

		// Unload the managed object
		this.unloadManagedObject(this.managedObject, this.recycleJobNode,
				currentTeam);

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

	/**
	 * {@link TeamIdentifier} for {@link ManagedObjectSource} to unload the
	 * {@link ManagedObject}.
	 */
	public static final TeamIdentifier MANAGED_OBJECT_LOAD_TEAM = new TeamIdentifier() {
	};

	@Override
	public void setManagedObject(ManagedObject managedObject) {

		// Access Point: ManagedObjectSource/ManagedObjectPool
		// Locks: Unknown (as may be called by ManagedObjectSource at any time)

		// Lock to ensure synchronised.
		// (may be invoked by another thread from the Managed Object Source)
		synchronized (this.lock) {

			// Obtain the current team
			TeamIdentifier team = this.currentTeam;
			if (team == null) {
				// Invoked by thread of managed object source
				team = MANAGED_OBJECT_LOAD_TEAM;
			}

			// Determine if container in failed state
			if (this.failure != null) {
				// Discard the managed object and no further processing
				this.unloadManagedObject(managedObject,
						this.metaData.createRecycleJobNode(managedObject), team);
				return;
			}

			// Obtain the activate set.
			// May be null if called by Managed Object Source at a later time.
			JobNodeActivateSet activateSet = this.assetActivateSet;

			// Handle based on state
			switch (this.containerState) {
			case NOT_LOADED:
				// Discard the managed object
				this.unloadManagedObject(managedObject,
						this.metaData.createRecycleJobNode(managedObject), team);

				// Should never be called before loadManagedObject
				throw new IllegalStateException(
						"loadManagedObject must be called before setManagedObject");

			case LOADING:
				// Determine if already loaded
				if (this.managedObject != null) {
					// Discard managed object as already loaded
					this.unloadManagedObject(managedObject,
							this.metaData.createRecycleJobNode(managedObject),
							team);
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
					// Provide bound name if name aware
					if (this.metaData.isNameAwareManagedObject()) {
						((NameAwareManagedObject) this.managedObject)
								.setBoundManagedObjectName(this.metaData
										.getBoundManagedObjectName());
					}

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
				this.unloadManagedObject(managedObject,
						this.metaData.createRecycleJobNode(managedObject), team);
				break;

			case UNLOAD_WAITING_GOVERNANCE:
			case UNLOADING:
				// Discard as managed object already flagged for unloading
				// (if unloading nothing should be waiting on it)
				this.unloadManagedObject(managedObject,
						this.metaData.createRecycleJobNode(managedObject), team);
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
		 * Indicates the {@link ManagedObject} has been loaded and is not in the
		 * process of being governed.
		 */
		GOVERNING,

		/**
		 * Indicates the {@link ManagedObject} now has appropriate
		 * {@link Governance}.
		 */
		GOVERNED,

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
		 * Indicates that the {@link ManagedObject} is waiting on a
		 * {@link Governance} to be unloaded. At this point, it should no longer
		 * be used for {@link Task} functionality.
		 */
		UNLOAD_WAITING_GOVERNANCE,

		/**
		 * Indicates the {@link ManagedObject} is being unloaded.
		 */
		UNLOADING
	}

}