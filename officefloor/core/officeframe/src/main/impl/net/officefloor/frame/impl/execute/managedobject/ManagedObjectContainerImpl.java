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
import net.officefloor.frame.impl.execute.asset.OfficeManagerImpl;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.impl.execute.jobnode.FailThreadStateJobNode;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.ExtensionInterfaceExtractor;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeLoop;
import net.officefloor.frame.internal.structure.ManagedJobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
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
	 * {@link ThreadState} responsible for making changes to this
	 * {@link ManagedObjectContainer}.
	 */
	private final ThreadState responsibleThreadState;

	/**
	 * {@link AssetLatch} for waiting to source the {@link ManagedObject}
	 * instance (the {@link Asset}).
	 */
	private final AssetLatch sourcingLatch;

	/**
	 * {@link AssetLatch} for waiting on operations on the {@link ManagedObject}
	 * instance.
	 */
	private final AssetLatch operationsLatch;

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
	 * Failure of the {@link ManagedObject}.
	 */
	private Throwable failure = null;

	/**
	 * Time that an asynchronous operation was started by the
	 * {@link ManagedObject}.
	 */
	private long asynchronousStartTime = NO_ASYNC_OPERATION;

	/**
	 * Initiate the container.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param metaData
	 *            Meta-data of the {@link ManagedObject}.
	 * @param responsibleThreadState
	 *            {@link ThreadState} responsible for making changes to this
	 *            {@link ManagedObjectContainer}.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(ManagedObjectMetaData<D> metaData,
			ThreadState responsibleThreadState) {
		this.metaData = metaData;
		this.responsibleThreadState = responsibleThreadState;

		// Create the latch to source the managed object
		this.sourcingLatch = this.metaData.getSourcingManager().createAssetLatch(this);

		// Create the monitor for asynchronous operations (if needed)
		if (this.metaData.isManagedObjectAsynchronous()) {
			// Requires operations managing
			this.operationsLatch = this.metaData.getOperationsManager().createAssetLatch(this);
		} else {
			// No operations managing required
			this.operationsLatch = null;
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
	 * @param responsibleThreadState
	 *            {@link ThreadState} responsible for making changes to this
	 *            {@link ManagedObjectContainer}.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(ManagedObject managedObject,
			ManagedObjectMetaData<D> metaData, ThreadState responsibleThreadState) {
		this(metaData, responsibleThreadState);

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
						.registerAsynchronousCompletionListener(new AsynchronousListenerImpl());
			}
		} catch (Throwable ex) {
			// Flag failure to handle later when Job attempts to use it
			this.failure = new FailedToSourceManagedObjectEscalation(this.metaData.getObjectType(), ex);
		}
	}

	/**
	 * Undertakes the {@link ManagedObjectOperation}.
	 * 
	 * @param operation
	 *            {@link ManagedObjectOperation} to undertake.
	 */
	private void doOperation(ManagedObjectOperation operation) {
		JobNodeLoop loop = this.metaData.getJobNodeLoop();
		if (this.responsibleThreadState.isAttachedToThread()) {
			// Current Thread, so execute immediately
			loop.runJobNode(operation);
		} else {
			// Delegate to be undertaken
			loop.delegateJobNode(operation);
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
	protected void unloadManagedObject(ManagedObject managedObject, JobNode recycleJob) {

		// Ensure have managed object to unload
		if (managedObject == null) {
			return;
		}

		// Job unload action
		if (recycleJob != null) {
			// Recycle the managed object
			this.responsibleThreadState.getProcessState().getCleanupSequence().registerCleanUpJob(recycleJob);

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
	public TeamManagement getResponsibleTeam() {
		return this.metaData.getResponsibleTeam();
	}

	@Override
	public ThreadState getResponsibleThreadState() {
		return this.responsibleThreadState;
	}

	@Override
	public JobNode loadManagedObject(ManagedJobNode managedJobNode, WorkContainer<?> workContainer) {

		// Propagate failure to thread requiring managed object
		if (this.failure != null) {
			return new FailThreadStateJobNode(this.failure, managedJobNode.getThreadState()).then(managedJobNode);
		}

		// Load the managed object
		return new LoadManagedObjectJobNode(managedJobNode, workContainer);
	}

	/**
	 * Loads the {@link ManagedObject}.
	 */
	private class LoadManagedObjectJobNode extends ManagedObjectOperation {

		/**
		 * Requesting {@link ManagedJobNode}.
		 */
		private final ManagedJobNode managedJobNode;

		private final WorkContainer<?> workContainer;

		/**
		 * Instantiate.
		 * 
		 * @param requestingThreadState
		 *            {@link ThreadState} requesting the {@link ManagedObject}.
		 * @param workContainer
		 *            {@link WorkContainer}.
		 */
		public LoadManagedObjectJobNode(ManagedJobNode managedJobNode, WorkContainer<?> workContainer) {
			this.managedJobNode = managedJobNode;
			this.workContainer = workContainer;
		}

		@Override
		public TeamManagement getResponsibleTeam() {
			// Undertake by possible responsible team
			return ManagedObjectContainerImpl.this.metaData.getResponsibleTeam();
		}

		@Override
		public JobNode doJob() {

			// Easy access to container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Check if asynchronous operation or still to source
			if (container.asynchronousStartTime != NO_ASYNC_OPERATION) {

				// Determine if timed out
				long idleTime = OfficeManagerImpl.currentTimeMillis() - container.asynchronousStartTime;
				if (idleTime > container.metaData.getTimeout()) {

					// Obtain the timeout failure
					Throwable timeoutFailure;
					if (container.managedObject == null) {
						// Source the managed object timed out
						timeoutFailure = new SourceManagedObjectTimedOutEscalation(container.metaData.getObjectType());
					} else {
						// Asynchronous operation timed out
						timeoutFailure = new ManagedObjectOperationTimedOutEscalation(
								container.metaData.getObjectType());
					}

					// Flag failed and propagate to managed job node
					return new FailManagedObjectJobNode(timeoutFailure, this.managedJobNode);
				}

				// Wait for asynchronous operation to complete
				if (container.managedObject == null) {
					return container.sourcingLatch.awaitOnAsset(this);
				} else {
					return container.operationsLatch.awaitOnAsset(this);
				}
			}

			// Handle based on state
			switch (container.containerState) {
			case NOT_LOADED:

				// TODO check whether managed objects are ready
				boolean isDependenciesReady = container.metaData.isDependenciesReady(this.workContainer, this);

				// Flag now loading the managed object
				container.containerState = ManagedObjectContainerState.LOADING;

				try {
					// Not loaded therefore source the managed object
					ManagedObjectPool pool = container.metaData.getManagedObjectPool();
					if (pool != null) {
						// Source from pool
						pool.sourceManagedObject(new ManagedObjectUserImpl());
					} else {
						// Source directly
						ManagedObjectSource<?, ?> managedObjectSource = container.metaData.getManagedObjectSource();
						managedObjectSource.sourceManagedObject(new ManagedObjectUserImpl());
					}

				} catch (Throwable ex) {
					// Flag failed to source Managed Object
					return new FailManagedObjectJobNode(ex, this.managedJobNode);
				}

				// Determine if loaded managed object
				if (container.managedObject == null) {

					// Record time that attempted to source the managed object
					container.asynchronousStartTime = OfficeManagerImpl.currentTimeMillis();

					// Register on sourcing latch to proceed
					return container.sourcingLatch.awaitOnAsset(this);
				}

			case LOADING:
				if (container.managedObject == null) {
					// Wait for loaded
					return container.sourcingLatch.awaitOnAsset(this);
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
						((AsynchronousManagedObject) container.managedObject)
								.registerAsynchronousCompletionListener(new AsynchronousListenerImpl());
					}
				} catch (Throwable ex) {
					// Flag failed to source Managed Object
					return new FailManagedObjectJobNode(ex, this.managedJobNode);
				}

			case COORDINATING:
				// Determine if require coordinating Managed Object
				if (container.metaData.isCoordinatingManagedObject()) {

					// Create the object registry for the managed object
					ObjectRegistry<?> objectRegistry = container.metaData.createObjectRegistry(this.workContainer,
							container.responsibleThreadState);

					try {
						// Coordinate the managed objects
						CoordinatingManagedObject cmo = (CoordinatingManagedObject) container.managedObject;
						cmo.loadObjects(objectRegistry);
					} catch (Throwable ex) {
						// Flag failure and propagate to the JobContainer
						return new FailManagedObjectJobNode(ex, this.managedJobNode);
					}
				}

				// Flag now attempting to govern
				container.containerState = ManagedObjectContainerState.GOVERNING;

			case GOVERNING:
				// Identify the applicable governance for this managed object
				ManagedObjectGovernanceMetaData<?>[] governanceMetaDatas = container.metaData.getGovernanceMetaData();

				// Iterate over governance and govern by active governance
				ThreadState managedJobNodeThreadState = this.managedJobNode.getThreadState();
				NEXT_GOVERNANCE: for (int i = 0; i < governanceMetaDatas.length; i++) {
					ManagedObjectGovernanceMetaData<?> governanceMetaData = governanceMetaDatas[i];

					// Determine if already under active governance
					ActiveGovernance activeGovernance = container.activeGovernances[i];
					if (activeGovernance != null) {

						// Ensure still active
						if (activeGovernance.isActive()) {
							continue NEXT_GOVERNANCE; // already active
						}

						// No longer active
						container.activeGovernances[i] = null;
					}

					// Determine if the governance is active
					int governanceIndex = governanceMetaData.getGovernanceIndex();
					if (!managedJobNodeThreadState.isGovernanceActive(governanceIndex)) {
						continue NEXT_GOVERNANCE; // not active, so not govern
					}

					// TODO following to be in managed job's thread state?

					// Obtain the governance
					GovernanceContainer governance = managedJobNodeThreadState.getGovernanceContainer(governanceIndex);

					// Obtain the extension interface
					ExtensionInterfaceExtractor<?> extractor = governanceMetaData.getExtensionInterfaceExtractor();
					Object extensionInterface = container.extractExtensionInterface(extractor);

					// Create and register the governance for the managed object
					activeGovernance = governance.createActiveGovernance(extensionInterface, container);
					container.activeGovernances[i] = activeGovernance;

					// Add the governance for activation
					GovernanceActivity<?, ?> activity = activeGovernance.createGovernActivity();
					return this.managedJobNode.getFlow().createGovernanceNode(activity, this.managedJobNode);
				}

				// Now governed as always need to check governance
				container.containerState = ManagedObjectContainerState.GOVERNED;

			case GOVERNED:
				try {
					// Obtain the object
					container.object = container.managedObject.getObject();
				} catch (Throwable ex) {
					// Flag in failed state
					return new FailManagedObjectJobNode(ex, this.managedJobNode);
				}

			case OBJECT_AVAILABLE:
				// Managed Object loaded, so carry on with managed job
				return this.managedJobNode;

			case UNLOAD_WAITING_GOVERNANCE:
			case UNLOADING:
				// Should never be called in this state
				// (only unloaded when no interest in this managed object)
				throw new IllegalStateException("Can not load an unloaded ManagedObject");

			default:
				throw new IllegalStateException("Unknown container state " + container.containerState);
			}
		}
	}

	/**
	 * {@link ManagedObjectUser} implementation.
	 */
	private class ManagedObjectUserImpl implements ManagedObjectUser {

		@Override
		public void setManagedObject(final ManagedObject managedObject) {
			ManagedObjectContainerImpl.this.doOperation(new ManagedObjectOperation() {
				@Override
				public JobNode doJob() {

					// Easy access to the container
					ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

					// Load the managed object
					container.managedObject = managedObject;

					// Wake up any waiting jobs
					container.sourcingLatch.releaseJobNodes(true);

					// Nothing further to set the managed object
					return null;
				}
			});
		}

		@Override
		public void setFailure(final Throwable cause) {
			ManagedObjectContainerImpl.this.doOperation(new ManagedObjectOperation() {
				@Override
				public JobNode doJob() {
					// Easy access to the container
					ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

					// Flag failure of managed object
					container.failure = cause;

					// Wake up any waiting jobs
					container.sourcingLatch.failJobNodes(cause, true);

					// Nothing further to fail the managed object
					return null;
				}
			});
		}
	}

	@Override
	public Object getObject() {

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
			return this.unloadManagedObject();
		}

		// Nothing further to unregister
		return null;
	}

	@Override
	public JobNode unloadManagedObject() {
		return new UnloadManagedObjectOperation();
	}

	/**
	 * Unloads the {@link ManagedObject}.
	 */
	private class UnloadManagedObjectOperation extends ManagedObjectOperation {
		@Override
		public JobNode doJob() {

			// Easy access to the container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			switch (container.containerState) {
			case NOT_LOADED:
			case LOADING:
			case LOADED:
			case GOVERNING:
			case GOVERNED:
			case COORDINATING:
			case OBJECT_AVAILABLE:

				// Ensure no active governance
				boolean isActiveGovernance = false;
				for (int i = 0; i < container.activeGovernances.length; i++) {
					if (container.activeGovernances[i] != null) {
						isActiveGovernance = true;
					}
				}

				// Flag that unloading governance
				container.containerState = ManagedObjectContainerState.UNLOAD_WAITING_GOVERNANCE;

			case UNLOAD_WAITING_GOVERNANCE:
			case UNLOADING:

				// Ensure have managed object to unload
				if (container.managedObject == null) {
					return null;
				}

				// Create the recycle job node
				JobNode recycleJobNode = container.metaData.createRecycleJobNode(container.managedObject,
						container.responsibleThreadState.getProcessState().getCleanupSequence());

				// Job unload action
				if (recycleJobNode != null) {
					// Recycle the managed object
					container.responsibleThreadState.getProcessState().getCleanupSequence()
							.registerCleanUpJob(recycleJobNode);

				} else {
					// Return directly to pool (if pooled)
					ManagedObjectPool pool = container.metaData.getManagedObjectPool();
					if (pool != null) {
						pool.returnManagedObject(managedObject);
					}
				}

				// Release permanently as managed object no longer being used
				container.sourcingLatch.releaseJobNodes(true);
				if (container.operationsLatch != null) {
					container.operationsLatch.releaseJobNodes(true);
				}

				// Release reference to managed object to not unload again
				container.managedObject = null;
			}

			// Managed object unloaded
			return null;
		}
	}

	/**
	 * {@link AsynchronousListener} implementation.
	 */
	private class AsynchronousListenerImpl implements AsynchronousListener {

		@Override
		public void notifyStarted() {
			ManagedObjectContainerImpl.this.doOperation(new ManagedObjectOperation() {
				@Override
				public JobNode doJob() {

					// Easy access to the container
					ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

					// Ignore starting operation if in failed state
					if (container.failure != null) {
						return null;
					}

					// Ensure asynchronous operation not already started
					if (container.asynchronousStartTime != NO_ASYNC_OPERATION) {
						return null;
					}

					// Flag start of asynchronous operation
					container.asynchronousStartTime = OfficeManagerImpl.currentTimeMillis();
					return null;

				}
			});
		}

		@Override
		public void notifyComplete() {
			ManagedObjectContainerImpl.this.doOperation(new ManagedObjectOperation() {
				@Override
				public JobNode doJob() {

					// Easy access to the container
					ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

					// Ignore completing operation if in failed state
					if (container.failure != null) {
						return null;
					}

					// Flag no asynchronous operation occurring
					container.asynchronousStartTime = NO_ASYNC_OPERATION;

					// Release any jobs waiting on the asynchronous operation
					container.operationsLatch.releaseJobNodes(false);
					return null;
				}
			});
		}
	}

	/*
	 * ================== Asset ==========================================
	 */

	@Override
	public ThreadState getOwningThreadState() {
		return this.responsibleThreadState;
	}

	@Override
	public void checkOnAsset(CheckAssetContext context) {

		// Determine if failure
		if (this.failure != null) {
			// Fail jobs waiting on this managed object permanently
			context.failJobNodes(this.failure, true);

			// No further checking necessary
			return;
		}

		// Not ready if undertaking an asynchronous operation
		if (this.asynchronousStartTime != NO_ASYNC_OPERATION) {

			// Determine if asynchronous operation has timed out
			long idleTime = OfficeManagerImpl.currentTimeMillis() - this.asynchronousStartTime;
			if (idleTime > this.metaData.getTimeout()) {

				// Obtain the time out failure
				Throwable timeoutFailure;
				if (this.managedObject == null) {
					// Source managed object timed out
					timeoutFailure = new SourceManagedObjectTimedOutEscalation(this.metaData.getObjectType());
				} else {
					// Asynchronous operation timed out
					timeoutFailure = new ManagedObjectOperationTimedOutEscalation(this.metaData.getObjectType());
				}

				// Fail job nodes waiting on this
				this.sourcingLatch.failJobNodes(timeoutFailure, true);
				if (this.operationsLatch != null) {
					this.operationsLatch.failJobNodes(timeoutFailure, true);
				}
			}
		}
	}

	/**
	 * Operation to be undertaken to change the {@link ManagedObjectContainer}.
	 */
	private abstract class ManagedObjectOperation implements JobNode {

		@Override
		public ThreadState getThreadState() {
			return ManagedObjectContainerImpl.this.responsibleThreadState;
		}
	}

	/**
	 * {@link JobNode} to place the {@link ManagedObjectContainer} in a failed
	 * state.
	 */
	private class FailManagedObjectJobNode extends ManagedObjectOperation {

		/**
		 * Cause of the failure.
		 */
		private final Throwable failure;

		/**
		 * Optional {@link ManagedJobNode} requiring the {@link ManagedObject}.
		 */
		private final ManagedJobNode managedJobNode;

		/**
		 * Instantiate.
		 * 
		 * @param failure
		 *            Cause of the failure.
		 */
		public FailManagedObjectJobNode(Throwable failure) {
			this(failure, null);
		}

		/**
		 * Instantiate.
		 * 
		 * @param failure
		 *            Cause of the failure.
		 * @param managedJobNode
		 *            {@link ManagedJobNode} requring the {@link ManagedObject}.
		 */
		public FailManagedObjectJobNode(Throwable failure, ManagedJobNode managedJobNode) {
			this.failure = failure;
			this.managedJobNode = managedJobNode;
		}

		@Override
		public JobNode doJob() {

			// Flag failure (puts container into failed state)
			ManagedObjectContainerImpl.this.failure = this.failure;

			// Provide propagate failure to job nodes
			PropagateEscalationError error = new PropagateEscalationError(this.failure);

			// Permanently fail job nodes
			ManagedObjectContainerImpl.this.sourcingLatch.failJobNodes(error, true);
			if (ManagedObjectContainerImpl.this.operationsLatch != null) {
				// Asynchronous so include permanently failing operations
				ManagedObjectContainerImpl.this.operationsLatch.failJobNodes(error, true);
			}

			// Propagate failure to managed job node
			if (this.managedJobNode != null) {
				return new FailThreadStateJobNode(error, this.managedJobNode.getThreadState())
						.then(this.managedJobNode);
			}

			// Nothing further to fail this container
			return null;
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