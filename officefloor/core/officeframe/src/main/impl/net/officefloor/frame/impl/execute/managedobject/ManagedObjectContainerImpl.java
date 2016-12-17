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
import net.officefloor.frame.impl.execute.jobnode.WaitJobNode;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.CleanupSequence;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.ExtensionInterfaceExtractor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.JobNodeRunnable;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
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
import net.officefloor.frame.spi.managedobject.source.CriticalSection;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Container of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectContainerImpl implements ManagedObjectContainer, Asset {

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
	 * {@link CleanupSequence}.
	 */
	private final CleanupSequence cleanupSequence;

	/**
	 * {@link AssetLatch} for waiting to source the {@link ManagedObject}
	 * instance (the {@link Asset}).
	 */
	private final AssetLatch sourcingMonitor;

	/**
	 * {@link AssetLatch} for waiting on operations on the
	 * {@link ManagedObject} instance.
	 */
	private final AssetLatch operationsMonitor;

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
	 * Initiate the container.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param metaData
	 *            Meta-data of the {@link ManagedObject}.
	 * @param threadState
	 *            Initial {@link ThreadState} requiring the
	 *            {@link ManagedObject}.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(ManagedObjectMetaData<D> metaData, ThreadState threadState) {
		this.metaData = metaData;
		this.cleanupSequence = threadState.getProcessState().getCleanupSequence();

		// Create the monitor to source the managed object
		this.sourcingMonitor = this.metaData.getSourcingManager().createAssetMonitor(this);

		// Create the monitor for asynchronous operations (if needed)
		if (this.metaData.isManagedObjectAsynchronous()) {
			// Requires operations managing
			threadState.requireProcessCoordination();
			this.operationsMonitor = this.metaData.getOperationsManager().createAssetMonitor(this);
		} else {
			// No operations managing required
			this.operationsMonitor = null;
		}

		// Create the active governances
		this.activeGovernances = new ActiveGovernance[this.metaData.getGovernanceMetaData().length];
	}

	/**
	 * Initiate the container with a provided {@link ManagedObject}.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param managedObject
	 *            {@link ManagedObject} triggering the {@link ProcessState}.
	 * @param metaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject}.
	 * @param threadState
	 *            Initial {@link ThreadState} requiring the
	 *            {@link ManagedObject}.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(ManagedObject managedObject,
			ManagedObjectMetaData<D> metaData, ThreadState threadState) {
		this(metaData, threadState);

		// Flag managed object loaded
		this.managedObject = managedObject;
		this.containerState = ManagedObjectContainerState.LOADED;

		try {
			// Provide bound name if name aware
			if (this.metaData.isNameAwareManagedObject()) {
				((NameAwareManagedObject) this.managedObject)
						.setBoundManagedObjectName(this.metaData.getBoundManagedObjectName());
			}

			// Provide listener if asynchronous managed object
			if (this.metaData.isManagedObjectAsynchronous()) {
				((AsynchronousManagedObject) this.managedObject)
						.registerAsynchronousCompletionListener(new AsynchronousListenerImpl(this.operationsMonitor));
			}
		} catch (Throwable ex) {
			// Flag failure to handle later when Job attempts to use it
			this.setFailedState(new FailedToSourceManagedObjectEscalation(this.metaData.getObjectType(), ex));
		}

		// Create the recycle job node
		this.recycleJobNode = this.metaData.createRecycleJobNode(this.managedObject, this.cleanupSequence);
	}

	/**
	 * Unloads the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} to be unloaded.
	 * @param recycleJob
	 *            {@link JobNode} to recycle the {@link ManagedObject}.
	 */
	protected void unloadManagedObject(ManagedObject managedObject, JobNode recycleJob) {

		// Ensure have managed object to unload
		if (managedObject == null) {
			return;
		}

		// Job unload action
		if (recycleJob != null) {
			// Recycle the managed object
			this.cleanupSequence.registerCleanUpJob(recycleJob);

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
	 */

	@Override
	public boolean loadManagedObject(JobContext executionContext, JobNode jobNode) {

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
				// Not loaded therefore source the managed object
				ManagedObjectPool pool = this.metaData.getManagedObjectPool();
				if (pool != null) {
					// Source from pool
					pool.sourceManagedObject(new ManagedObjectUserImpl());
				} else {
					// Source directly
					ManagedObjectSource<?, ?> managedObjectSource = this.metaData.getManagedObjectSource();
					managedObjectSource.sourceManagedObject(new ManagedObjectUserImpl());
				}

			} catch (Throwable ex) {
				// Flag failed to source Managed Object
				this.setFailedState(new FailedToSourceManagedObjectEscalation(this.metaData.getObjectType(), ex));
			}

			// Propagate any failure in loading to the JobContainer
			if (this.failure != null) {
				throw this.failure;
			}

			// Determine if loaded managed object
			if (this.managedObject == null) {
				// Asynchronously obtaining managed object, so must coordinate
				jobNode.getThreadState().requireProcessCoordination();

				// Record time that attempted to source the managed object
				this.asynchronousStartTime = executionContext.getTime();

				// Register for sourcing monitoring
				this.sourcingMonitor.waitOnAsset(jobNode);

				// Wait to be loaded
				return false;
			}

		case LOADING:
			// Wait for loaded
			return false;

		case LOADED:
		case GOVERNING:
		case GOVERNED:
		case COORDINATING:
		case OBJECT_AVAILABLE:
			// Managed Object loaded
			return true;

		case UNLOAD_WAITING_GOVERNANCE:
		case UNLOADING:
			// Should never be called in this state
			// (only unloaded when no interest in this managed object)
			throw new IllegalStateException("Can not load an unloaded ManagedObject");

		default:
			throw new IllegalStateException("Unknown container state " + this.containerState);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public JobNode isManagedObjectReady(WorkContainer workContainer, JobContext jobContext, JobNode jobNode) {

		// Always propagate any failure to the Job container to handle.
		if (this.failure != null) {
			throw this.failure;
		}

		// Check if asynchronous operation or still to source
		if (this.asynchronousStartTime != NO_ASYNC_OPERATION) {

			// Determine if timed out
			long idleTime = jobContext.getTime() - this.asynchronousStartTime;
			if (idleTime > this.metaData.getTimeout()) {

				// Obtain the timeout failure
				Throwable timeoutFailure;
				if (this.managedObject == null) {
					// Source the managed object timed out
					timeoutFailure = new SourceManagedObjectTimedOutEscalation(this.metaData.getObjectType());
				} else {
					// Asynchronous operation timed out
					timeoutFailure = new ManagedObjectOperationTimedOutEscalation(this.metaData.getObjectType());
				}

				// Flag failed state and propagate to JobContainer
				this.setFailedState(timeoutFailure);
				throw this.failure;
			}

			// Wait for asynchronous operation to complete
			return new WaitJobNode(jobNode);
		}
	}

	@Override
	public JobNode setupManagedObject(WorkContainer<?> workContainer, JobContext jobContext, Flow flow,
			JobNode jobNode) {

		// Determine if managed object is ready
		JobNode readyJobNode = this.isManagedObjectReady(workContainer, jobContext, jobNode);
		if (readyJobNode != null) {
			return readyJobNode;
		}

		// Ensure the dependencies are ready
		if (!this.metaData.isDependenciesReady(workContainer, jobContext, jobNode)) {
			// Dependencies must be ready before setup
			return new WaitJobNode(jobNode);
		}

		// Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			// Should never be called before loadManagedObject
			throw new IllegalStateException("loadManagedObject must be called before governManagedObject");

		case LOADING:
			// Still loading managed object so wait until loaded
			return new WaitJobNode(jobNode);

		case LOADED:

			// Determine if require coordinating Managed Object
			if (this.metaData.isCoordinatingManagedObject()) {

				// Obtain the thread state
				ThreadState threadState = jobNode.getThreadState();

				// Create the object registry for the managed object
				ObjectRegistry<?> objectRegistry = this.metaData.createObjectRegistry(workContainer, threadState);

				try {
					// Coordinate the managed objects
					CoordinatingManagedObject cmo = (CoordinatingManagedObject) this.managedObject;
					cmo.loadObjects(objectRegistry);
				} catch (Throwable ex) {
					// Flag failure and propagate to the JobContainer
					this.setFailedState(new FailedToSourceManagedObjectEscalation(this.metaData.getObjectType(), ex));
					throw this.failure;
				}
			}

			// Flag now attempting to govern
			this.containerState = ManagedObjectContainerState.GOVERNING;

		case GOVERNING:
			// Identify the applicable governance for this managed object
			ManagedObjectGovernanceMetaData<?>[] governanceMetaDatas = this.metaData.getGovernanceMetaData();

			// Iterate over governance and govern by active governance
			ThreadState thread = jobNode.getThreadState();
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
				GovernanceContainer governance = thread.getGovernanceContainer(governanceIndex);

				// Obtain the extension interface
				ExtensionInterfaceExtractor<?> extractor = governanceMetaData.getExtensionInterfaceExtractor();
				Object extensionInterface = this.extractExtensionInterface(extractor);

				// Create and register the governance for the managed object
				activeGovernance = governance.createActiveGovernance(extensionInterface, this);
				this.activeGovernances[i] = activeGovernance;

				// Add the governance for activation
				GovernanceActivity<?, ?> activity = activeGovernance.createGovernActivity();
				return flow.createGovernanceNode(activity, jobNode);
			}

			// Now governed as always need to check governance
			this.containerState = ManagedObjectContainerState.GOVERNED;

		case GOVERNED:
			try {
				// Obtain the object
				this.object = this.managedObject.getObject();
			} catch (Throwable ex) {
				// Flag in failed state
				this.setFailedState(new FailedToSourceManagedObjectEscalation(this.metaData.getObjectType(), ex));
				throw this.failure;
			}

		case OBJECT_AVAILABLE:
			// Successfully now governed, or governance ready
			return null;

		case UNLOAD_WAITING_GOVERNANCE:
		case UNLOADING:
			// Should never be called in this state
			// (only unloaded when no interest in this managed object)
			throw new IllegalStateException("Can not coordinate an unloaded ManagedObject");

		default:
			throw new IllegalStateException("Unknown container state " + this.containerState);
		}

	}

	@Override
	public Object getObject(ThreadState threadState) {

		// Only return Object if in valid state
		if (this.containerState == ManagedObjectContainerState.OBJECT_AVAILABLE) {
			// Return the Object
			return this.object;
		}

		// Incorrect state if here
		throw new PropagateEscalationError(
				new FailedToSourceManagedObjectEscalation(this.metaData.getObjectType(), new IllegalStateException(
						"ManagedObject in incorrect state " + this.containerState + " to obtain Object")));
	}

	@Override
	public <I> I extractExtensionInterface(ExtensionInterfaceExtractor<I> extractor) {

		// Ensure have Managed Object
		if (this.managedObject == null) {
			throw new PropagateEscalationError(new FailedToSourceManagedObjectEscalation(this.metaData.getObjectType(),
					new NullPointerException("ManagedObject not loaded")));
		}

		// Return the extracted extension interface
		return extractor.extractExtensionInterface(this.managedObject, this.metaData);
	}

	@Override
	public JobNode unregisterManagedObjectFromGovernance(ActiveGovernance<?, ?> governance) {

		// Unregister the active governance
		int index = governance.getManagedObjectRegisteredIndex();
		this.activeGovernances[index] = null;

		// Determine if managed object waiting on governance to unload
		if (this.containerState == ManagedObjectContainerState.UNLOAD_WAITING_GOVERNANCE) {
			// Attempt to unload the managed object
			this.unloadManagedObject();
		}
	}

	@Override
	public JobNode unloadManagedObject(JobNode continueJobNode) {

		// TODO Handle based on state
		switch (this.containerState) {
		case NOT_LOADED:
			break;

		}

		// Flag that possibly waiting on governance to unload
		this.containerState = ManagedObjectContainerState.UNLOAD_WAITING_GOVERNANCE;

		// Ensure no active governance
		for (int i = 0; i < this.activeGovernances.length; i++) {
			if (this.activeGovernances[i] != null) {
				isActiveGovernance = true;
			}
		}

		// Flag that unloading
		this.containerState = ManagedObjectContainerState.UNLOADING;

		// Activate jobs permanently as Asset no longer being used
		this.sourcingMonitor.activateJobNodes(true);
		if (this.operationsMonitor != null) {
			this.operationsMonitor.activateJobNodes(true);
		}

		// Unload the managed object
		this.unloadManagedObject(this.managedObject, this.recycleJobNode);

		// Release reference to managed object to not unload again
		this.managedObject = null;
	}

	/**
	 * {@link AsynchronousListener} implementation.
	 */
	private static class AsynchronousListenerImpl implements AsynchronousListener {

		/**
		 * {@link AssetLatch} for operations.
		 */
		private final AssetLatch operationsMonitor;

		/**
		 * Instantiate.
		 * 
		 * @param operationsMonitor
		 *            {@link AssetLatch} for operations.
		 */
		public AsynchronousListenerImpl(AssetLatch operationsMonitor) {
			this.operationsMonitor = operationsMonitor;
		}

		/*
		 * =============== AsynchronousListener =======================
		 */

		@Override
		public void notifyStarted() {

			// Ignore starting operation if in failed state
			if (state.failure != null) {
				return;
			}

			// Ensure asynchronous operation not already started
			if (state.asynchronousStartTime != NO_ASYNC_OPERATION) {
				return;
			}

			// Flag start of asynchronous operation
			state.asynchronousStartTime = System.currentTimeMillis();
			return;
		}

		@Override
		public void notifyComplete() {

			// Ignore completing operation if in failed state
			if (state.failure != null) {
				return;
			}

			// Flag no asynchronous operation occurring
			state.asynchronousStartTime = NO_ASYNC_OPERATION;

			// Activate any jobs waiting on the asynchronous operation
			this.operationsMonitor.activateJobNodes(false);
		}
	}

	/**
	 * {@link ManagedObjectUser} implementation.
	 */
	private class ManagedObjectUserImpl implements ManagedObjectUser {

		/**
		 * {@link JobNode} requiring the {@link ManagedObject}.
		 */
		private final JobNode jobNode;

		/**
		 * Loads the {@link ManagedObject}.
		 * 
		 * @param managedObject
		 *            {@link ManagedObject}.
		 */
		private void loadManagedObject(ManagedObject managedObject) {

			// Easy reference to container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Determine if container in failed state
			if (container.failure != null) {
				// Discard the managed object and no further processing
				container.unloadManagedObject(managedObject,
						container.metaData.createRecycleJobNode(managedObject, container.cleanupSequence));
				return;
			}

			// Handle based on state
			switch (container.containerState) {
			case NOT_LOADED:
				// Discard the managed object
				container.unloadManagedObject(managedObject,
						container.metaData.createRecycleJobNode(managedObject, container.cleanupSequence));

				// Should never be called before loadManagedObject
				throw new IllegalStateException("loadManagedObject must be called before setManagedObject");

			case LOADING:
				// Determine if already loaded
				if (container.managedObject != null) {
					// Discard managed object as already loaded
					container.unloadManagedObject(managedObject,
							container.metaData.createRecycleJobNode(managedObject, container.cleanupSequence));
					return; // discarded and nothing further
				}

				// Ensure have the managed object
				if (managedObject == null) {
					// Flag no managed object provided
					container.setFailedState(new FailedToSourceManagedObjectEscalation(
							container.metaData.getObjectType(), new NullPointerException("No ManagedObject provided")));
					return;
				}

				// Flag loaded and no longer waiting to source
				container.managedObject = managedObject;
				container.containerState = ManagedObjectContainerState.LOADED;
				container.asynchronousStartTime = NO_ASYNC_OPERATION;

				try {
					// Provide bound name if name aware
					if (container.metaData.isNameAwareManagedObject()) {
						((NameAwareManagedObject) container.managedObject)
								.setBoundManagedObjectName(container.metaData.getBoundManagedObjectName());
					}

					// Provide listener if asynchronous managed object
					if (container.metaData.isManagedObjectAsynchronous()) {
						((AsynchronousManagedObject) container.managedObject).registerAsynchronousCompletionListener(
								new AsynchronousListenerImpl(ManagedObjectContainerImpl.this.operationsMonitor));
					}
				} catch (Throwable ex) {
					// Flag in failed state
					container.setFailedState(
							new FailedToSourceManagedObjectEscalation(container.metaData.getObjectType(), ex));
					return;
				}

				// Create the recycle job node for the managed object
				container.recycleJobNode = container.metaData.createRecycleJobNode(container.managedObject,
						container.cleanupSequence);
				break;

			case LOADED:
			case COORDINATING:
			case OBJECT_AVAILABLE:
				// Discard the managed object as already have a Managed Object
				container.unloadManagedObject(managedObject,
						container.metaData.createRecycleJobNode(managedObject, container.cleanupSequence));
				break;

			case UNLOAD_WAITING_GOVERNANCE:
			case UNLOADING:
				// Discard as managed object already flagged for unloading
				// (if unloading nothing should be waiting on it)
				container.unloadManagedObject(managedObject,
						container.metaData.createRecycleJobNode(managedObject, container.cleanupSequence));
				break;

			default:
				throw new IllegalStateException("Unknown container state " + container.containerState);
			}
		}

		/**
		 * Loads the failure.
		 * 
		 * @param cause
		 *            {@link Throwable} failure.
		 */
		private void loadFailure(Throwable cause) {

			// Easy reference to container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Load the failure
			container.setFailedState(
					new FailedToSourceManagedObjectEscalation(container.metaData.getObjectType(), cause));
		}

		/*
		 * ================ ManagedObjectUser ====================
		 */

		@Override
		public void setManagedObject(final ManagedObject managedObject) {

			// Determine if invoked on load Thread
			if (this.jobNode.getThreadState().isJobNodeLoopThread()) {
				// Same thread, so load the managed object
				this.loadManagedObject(managedObject);

			} else {
				// Different thread, so must load via job loop
				this.jobNode.getThreadState().run(new JobNodeRunnable() {
					@Override
					public JobNode run() {
						ManagedObjectUserImpl.this.loadManagedObject(managedObject);
						return ManagedObjectUserImpl.this.jobNode;
					}
				}, this.jobNode.getResponsibleTeam());
			}
		}

		@Override
		public void setFailure(final Throwable cause) {

			// Determine if invoked on load Thread
			if (this.jobNode.getThreadState().isJobNodeLoopThread()) {
				// Same thread, so load the failure
				this.loadFailure(cause);

			} else {
				// Different thread, so must load via job loop
				this.jobNode.getThreadState().run(new JobNodeRunnable() {
					@Override
					public JobNode run() {
						ManagedObjectUserImpl.this.loadFailure(cause);
						return ManagedObjectUserImpl.this.jobNode;
					}
				}, this.jobNode.getResponsibleTeam());
			}
		}
	}

	/*
	 * ================== Asset ==========================================
	 */

	/**
	 * State for the {@link CriticalSection} to check on the {@link Asset}.
	 */
	private static final class CheckOnAssetState {

		/**
		 * {@link ManagedObjectContainerImpl}.
		 */
		private final ManagedObjectContainerImpl container;

		/**
		 * {@link CheckAssetContext}.
		 */
		private final CheckAssetContext context;

		/**
		 * Initialise.
		 * 
		 * @param container
		 *            {@link ManagedObjectContainerImpl}.
		 * @param context
		 *            {@link CheckAssetContext}.
		 */
		public CheckOnAssetState(ManagedObjectContainerImpl container, CheckAssetContext context) {
			this.container = container;
			this.context = context;
		}
	}

	/**
	 * {@link CriticalSection} to check on the {@link Asset}.
	 */
	private static final CriticalSection<Object, CheckOnAssetState> checkOnAssetCriticalSection = new CriticalSection<Object, ManagedObjectContainerImpl.CheckOnAssetState>() {
		@Override
		public Object doCriticalSection(CheckOnAssetState state) throws Exception {

			// Determine if failure
			if (state.container.failure != null) {
				// Fail jobs waiting on this managed object permanently
				state.context.failJobNodes(state.container.failure.getCause(), true);

				// No further checking necessary
				return null;
			}

			// Not ready if undertaking an asynchronous operation
			if (state.container.asynchronousStartTime != NO_ASYNC_OPERATION) {

				// Determine if asynchronous operation has timed out
				long idleTime = state.context.getTime() - state.container.asynchronousStartTime;
				if (idleTime > state.container.metaData.getTimeout()) {

					// Obtain the time out failure
					Throwable timeoutFailure;
					if (state.container.managedObject == null) {
						// Source managed object timed out
						timeoutFailure = new SourceManagedObjectTimedOutEscalation(
								state.container.metaData.getObjectType());
					} else {
						// Asynchronous operation timed out
						timeoutFailure = new ManagedObjectOperationTimedOutEscalation(
								state.container.metaData.getObjectType());
					}

					// Fail job nodes waiting on this
					state.context.failJobNodes(timeoutFailure, true);

					// Flag in failed state (use OfficeManager to fail)
					state.container.setFailedState(timeoutFailure, null);
				}
			}

			return null;
		}
	};

	@Override
	public void checkOnAsset(CheckAssetContext context) {
		this.lockState.doProcessCriticalSection(new CheckOnAssetState(this, context), checkOnAssetCriticalSection);
	}

	/**
	 * Sets the {@link ManagedObjectContainer} into a failed state and makes the
	 * {@link AssetLatch} instances aware.
	 * 
	 * @param failure
	 *            Failure.
	 */
	private void setFailedState(Throwable failure) {

		// Flag failure (puts container into failed state)
		this.failure = new PropagateEscalationError(failure);

		// Permanently fail job nodes
		this.sourcingMonitor.failJobNodes(failure, true);
		if (this.operationsMonitor != null) {
			// Asynchronous so include permanently failing operations
			this.operationsMonitor.failJobNodes(failure, true);
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