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

package net.officefloor.frame.impl.execute.managedobject;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.AsynchronousOperation;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.impl.execute.function.AbstractDelegateFunctionState;
import net.officefloor.frame.impl.execute.function.Promise;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetLatch;
import net.officefloor.frame.internal.structure.AssetManagerReference;
import net.officefloor.frame.internal.structure.CheckAssetContext;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.ManagedFunctionContainer;
import net.officefloor.frame.internal.structure.ManagedObjectAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectCleanup;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectExtensionExtractor;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectReadyCheck;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.RegisteredGovernance;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadStateContext;

/**
 * Container of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectContainerImpl implements ManagedObjectContainer, Asset {

	/**
	 * Convenience method to obtain the {@link ManagedObjectContainer}.
	 * 
	 * @param index           {@link ManagedObjectIndex} to identify the
	 *                        {@link ManagedObjectContainer}.
	 * @param managedFunction {@link ManagedFunctionContainer} to specify context
	 *                        for obtaining the {@link ManagedObjectContainer}.
	 * @return {@link ManagedObjectContainer}.
	 */
	public static ManagedObjectContainer getManagedObjectContainer(ManagedObjectIndex index,
			ManagedFunctionContainer managedFunction) {

		// Obtain the scope index
		int scopeIndex = index.getIndexOfManagedObjectWithinScope();

		// Obtain the managed object container
		switch (index.getManagedObjectScope()) {
		case FUNCTION:
			// Obtain the container from this managed function
			return managedFunction.getManagedObjectContainer(scopeIndex);

		case THREAD:
			// Obtain the container from the thread state
			return managedFunction.getThreadState().getManagedObjectContainer(scopeIndex);

		case PROCESS:
			// Obtain the container from the process state
			return managedFunction.getThreadState().getProcessState().getManagedObjectContainer(scopeIndex);
		}

		// As here, unknown scope
		throw new IllegalStateException("Unknown managed object scope " + index.getManagedObjectScope());
	}

	/**
	 * Value indicating that there currently is no asynchronous operation occurring.
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
	 * {@link AssetLatch} for waiting to source the {@link ManagedObject} instance
	 * (the {@link Asset}).
	 */
	private final AssetLatch sourcingLatch;

	/**
	 * {@link AssetLatch} for waiting on operations on the {@link ManagedObject}
	 * instance.
	 */
	private final AssetLatch operationsLatch;

	/**
	 * Listing of {@link RegisteredGovernance} for this {@link ManagedObject}.
	 */
	private final RegisteredGovernance[] registeredGovernances;

	/**
	 * State of this {@link ManagedObjectContainer}.
	 */
	private ManagedObjectContainerState containerState = ManagedObjectContainerState.NOT_LOADED;

	/**
	 * {@link ManagedObject} being managed. This is the {@link ManagedObject} from
	 * the {@link ManagedObjectSource}.
	 */
	private ManagedObject managedObject = null;

	/**
	 * {@link ManagedObjectReadyCheck} currently being undertaken.
	 */
	private ManagedObjectReadyCheckImpl check = null;

	/**
	 * Object from the {@link ManagedObject}.
	 */
	private Object object = null;

	/**
	 * Failure of the {@link ManagedObject}.
	 */
	private Throwable failure = null;

	/**
	 * Indicates the {@link ManagedObjectContainer} requires {@link ThreadState}
	 * safety.
	 */
	private boolean isRequireThreadStateSafety = false;

	/**
	 * Time that an asynchronous operation was started by the {@link ManagedObject}.
	 */
	private long asynchronousStartTime = NO_ASYNC_OPERATION;

	/**
	 * Initiate the container.
	 * 
	 * @param <D>                    Dependency key type.
	 * @param metaData               Meta-data of the {@link ManagedObject}.
	 * @param responsibleThreadState {@link ThreadState} responsible for making
	 *                               changes to this {@link ManagedObjectContainer}.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(ManagedObjectMetaData<D> metaData,
			ThreadState responsibleThreadState) {
		this.metaData = metaData;
		this.responsibleThreadState = responsibleThreadState;

		// Obtain the Office Manager for the process
		OfficeManager officeManager = responsibleThreadState.getProcessState().getOfficeManager();

		// Create the latch to source the managed object
		AssetManagerReference sourceManagerReference = this.metaData.getSourcingManagerReference();
		this.sourcingLatch = officeManager.getAssetManager(sourceManagerReference).createAssetLatch(this);

		// Create the monitor for asynchronous operations (if needed)
		if (this.metaData.isManagedObjectAsynchronous()) {
			// Requires operations managing
			AssetManagerReference operationsManagerReference = this.metaData.getOperationsManagerReference();
			this.operationsLatch = officeManager.getAssetManager(operationsManagerReference).createAssetLatch(this);
		} else {
			// No operations managing required
			this.operationsLatch = null;
		}

		// Create the listing for registration with governance
		this.registeredGovernances = new RegisteredGovernance[this.metaData.getGovernanceMetaData().length];
	}

	/**
	 * Initiate the container with a provided {@link ManagedObject}.
	 * 
	 * @param <D>                    Dependency key type.
	 * @param managedObject          {@link ManagedObject} triggering the
	 *                               {@link ProcessState}.
	 * @param metaData               {@link ManagedObjectMetaData} of the
	 *                               {@link ManagedObject}.
	 * @param responsibleThreadState {@link ThreadState} responsible for making
	 *                               changes to this {@link ManagedObjectContainer}.
	 */
	public <D extends Enum<D>> ManagedObjectContainerImpl(ManagedObject managedObject,
			ManagedObjectMetaData<D> metaData, ThreadState responsibleThreadState) {
		this(metaData, responsibleThreadState);

		// Flag managed object loaded
		this.managedObject = managedObject;
		this.containerState = ManagedObjectContainerState.LOADED;

		try {

			// Provide process awareness if process aware
			if (this.metaData.isContextAwareManagedObject()) {
				((ContextAwareManagedObject) this.managedObject)
						.setManagedObjectContext(new ManagedObjectContextImpl());
			}

			// Provide listener if asynchronous managed object
			if (this.metaData.isManagedObjectAsynchronous()) {
				((AsynchronousManagedObject) this.managedObject).setAsynchronousContext(new AsynchronousContextImpl());
			}
		} catch (Throwable ex) {
			// Flag failure to handle later when function attempts to use it
			this.failure = ex;
		}
	}

	/**
	 * Undertakes the {@link ManagedObjectOperation}.
	 * 
	 * @param operation {@link ManagedObjectOperation} to undertake.
	 */
	private void doOperation(ManagedObjectOperation operation) {
		FunctionLoop loop = this.metaData.getFunctionLoop();
		if (this.responsibleThreadState.isAttachedToThread()) {
			// Current Thread, so execute immediately
			loop.executeFunction(operation);
		} else {
			// Delegate to be undertaken
			loop.delegateFunction(operation);
		}
	}

	/*
	 * =============== ManagedObjectContainer =============================
	 */

	@Override
	public ThreadState getResponsibleThreadState() {
		return this.responsibleThreadState;
	}

	@Override
	public FunctionState loadManagedObject(ManagedFunctionContainer managedFunction) {

		// Propagate failure to thread requiring managed object
		if (this.failure != null) {
			return new FailManagedObjectOperation(this.failure, managedFunction);
		}

		// Load the managed object
		return new LoadManagedObjectOperation(managedFunction);
	}

	/**
	 * Loads the {@link ManagedObject}.
	 */
	private class LoadManagedObjectOperation extends ManagedObjectOperation {

		/**
		 * Requesting {@link ManagedFunctionContainer}.
		 */
		private final ManagedFunctionContainer managedFunction;

		/**
		 * Instantiate.
		 * 
		 * @param managedFunction {@link ManagedFunctionContainer} requesting the
		 *                        {@link ManagedObject} to be loaded.
		 */
		public LoadManagedObjectOperation(ManagedFunctionContainer managedFunction) {
			this.managedFunction = managedFunction;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public FunctionState execute(FunctionStateContext context) throws Throwable {

			// Easy access to container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Propagate failure if in failed state
			if (container.failure != null) {
				throw container.failure;
			}

			// Check if asynchronous operation or still to source
			if (container.asynchronousStartTime != NO_ASYNC_OPERATION) {

				// Determine if timed out
				long idleTime = container.metaData.getMonitorClock().currentTimeMillis()
						- container.asynchronousStartTime;
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
					return new FailManagedObjectOperation(timeoutFailure, this.managedFunction);
				}

				// Wait for asynchronous operation to complete
				container.check = null; // must re-check on release
				if (container.managedObject == null) {
					return container.sourcingLatch.awaitOnAsset(this);
				} else {
					return container.operationsLatch.awaitOnAsset(this);
				}
			}

			// Handle based on state
			switch (container.containerState) {
			case NOT_LOADED:

				// Undertake any pre-load administration
				ManagedFunctionContainer preLoadAdministration = null;
				ManagedObjectAdministrationMetaData<?, ?, ?>[] preLoadAdministrations = container.metaData
						.getPreLoadAdministration();
				for (int i = 0; i < preLoadAdministrations.length; i++) {
					preLoadAdministration = this.managedFunction.getFlow()
							.createAdministrationFunction(preLoadAdministrations[i], preLoadAdministration);
				}

				// Flag now pre-load administration
				container.containerState = ManagedObjectContainerState.PRE_LOAD_ADMINISTRATION;

				// Undertake the pre-load administration (before continuing)
				if (preLoadAdministration != null) {
					return Promise.then(preLoadAdministration, this);
				}

			case PRE_LOAD_ADMINISTRATION:
				// Check whether dependencies are ready
				if (container.check == null) {
					// Undertake check to ensure managed objects are ready
					container.check = new ManagedObjectReadyCheckImpl(this, this.managedFunction);
					FunctionState checkFunction = container.metaData.checkReady(this.managedFunction, container.check,
							null);
					if (checkFunction != null) {
						return Promise.then(checkFunction, this);
					}
				} else if (!container.check.isReady()) {
					// Not ready so wait on latch release and try again
					container.check = null;
					return null;
				}

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
					return new FailManagedObjectOperation(ex, this.managedFunction);
				}

				// Determine if loaded managed object
				if (container.managedObject == null) {

					// Require thread state safety to load managed object
					container.isRequireThreadStateSafety = true;

					// Record time that attempted to source the managed object
					container.asynchronousStartTime = container.metaData.getMonitorClock().currentTimeMillis();

					// Register on sourcing latch to proceed
					return container.sourcingLatch.awaitOnAsset(this);
				}

			case LOADING:

				// Ensure loaded (another function requiring managed object)
				if (container.managedObject == null) {
					return container.sourcingLatch.awaitOnAsset(this);
				}

				// Flag loaded and no longer waiting to source
				container.managedObject = managedObject;
				container.containerState = ManagedObjectContainerState.LOADED;

			case LOADED:

				try {
					// If not setup, then managed object should still be ready
					boolean isCheckReady = false;

					// Obtained the source managed object to configure
					ManagedObject sourceManagedObject = container.managedObject;
					ManagedObjectPool pool = container.metaData.getManagedObjectPool();
					if (pool != null) {
						sourceManagedObject = pool.getSourcedManagedObject(sourceManagedObject);
					}

					// Provide context awareness if process aware
					if (container.metaData.isContextAwareManagedObject()) {
						((ContextAwareManagedObject) sourceManagedObject)
								.setManagedObjectContext(new ManagedObjectContextImpl());
						isCheckReady = true;
					}

					// Provide listener if asynchronous managed object
					if (container.metaData.isManagedObjectAsynchronous()) {
						((AsynchronousManagedObject) sourceManagedObject)
								.setAsynchronousContext(new AsynchronousContextImpl());
						isCheckReady = true;
					}

					// Provide co-ordination if co-ordinating managed object
					if (container.metaData.isCoordinatingManagedObject()) {
						ObjectRegistry<?> objectRegistry = container.metaData
								.createObjectRegistry(this.managedFunction);
						CoordinatingManagedObject cmo = (CoordinatingManagedObject) sourceManagedObject;
						cmo.loadObjects(objectRegistry);
						isCheckReady = true;
					}

					// Only check ready if required to do so
					if (isCheckReady) {
						container.check = null;
					}

				} catch (Throwable ex) {
					// Flag failed to source Managed Object
					return new FailManagedObjectOperation(ex, this.managedFunction);
				}

				// Flag now attempting to govern
				container.containerState = ManagedObjectContainerState.GOVERNING;

			case GOVERNING:

				// Check whether managed object and its dependencies are ready
				if (container.check == null) {
					// Undertake check to ensure managed objects are ready
					container.check = new ManagedObjectReadyCheckImpl(this, this.managedFunction);
					FunctionState checkFunction = container.metaData.checkReady(this.managedFunction, container.check,
							container);
					if (checkFunction != null) {
						return Promise.then(checkFunction, this);
					}
				} else if (!container.check.isReady()) {
					// Not ready so watch on latch release and try again
					container.check = null;
					return null;
				}

				// Identify the applicable governance for this managed object
				ManagedObjectGovernanceMetaData<?>[] governanceMetaDatas = container.metaData.getGovernanceMetaData();

				// Iterate over governance and govern by active governance
				ThreadState managedFunctionThreadState = this.managedFunction.getThreadState();
				FunctionState governanceFunctions = null;
				NEXT_GOVERNANCE: for (int i = 0; i < governanceMetaDatas.length; i++) {
					ManagedObjectGovernanceMetaData<?> governanceMetaData = governanceMetaDatas[i];

					// Determine if already registered for governance
					RegisteredGovernance registeredGovernance = container.registeredGovernances[i];
					if (registeredGovernance != null) {
						continue NEXT_GOVERNANCE; // already registered
					}

					// Obtain the governance container
					int governanceIndex = governanceMetaData.getGovernanceIndex();
					GovernanceContainer governance = managedFunctionThreadState.getGovernanceContainer(governanceIndex);

					// Obtain the extension interface
					ManagedObjectExtensionExtractor extractor = governanceMetaData.getExtensionInterfaceExtractor();
					Object extensionInterface = extractor.extractExtension(container.managedObject, container.metaData);

					// Register the governance for the managed object
					registeredGovernance = governance.registerManagedObject(extensionInterface, container,
							container.metaData, this.managedFunction);
					container.registeredGovernances[i] = registeredGovernance;
					governanceFunctions = Promise.then(governanceFunctions, registeredGovernance);
				}

				// Flag governed after governance functions complete
				container.containerState = ManagedObjectContainerState.GOVERNED;

				// Undertake governance registrations
				if (governanceFunctions != null) {
					container.check = null; // must check
					return Promise.then(governanceFunctions, this);
				}

			case GOVERNED:

				// Check whether managed object and its dependencies are ready
				if (container.check == null) {
					// Undertake check to ensure managed objects are ready
					container.check = new ManagedObjectReadyCheckImpl(this, this.managedFunction);
					FunctionState checkFunction = container.metaData.checkReady(this.managedFunction, container.check,
							container);
					if (checkFunction != null) {
						return Promise.then(checkFunction, this);
					}
				} else if (!container.check.isReady()) {
					// Not ready so watch on latch release and try again
					container.check = null;
					return null;
				}

				try {
					// Obtain the object
					container.object = container.managedObject.getObject();
				} catch (Throwable ex) {
					// Flag in failed state
					return new FailManagedObjectOperation(ex, this.managedFunction);
				}

				// Have object
				container.containerState = ManagedObjectContainerState.OBJECT_AVAILABLE;

			case OBJECT_AVAILABLE:

				// Managed Object loaded, so carry on with managed function
				return this.managedFunction;

			case UNLOAD_WAITING_GOVERNANCE:
			case UNLOADING:
			case COMPLETE:
				// Should never be called in this state
				// (only unloaded when no interest in this managed object)
				throw new IllegalStateException("Can not load an unloaded ManagedObject");

			default:
				throw new IllegalStateException("Unknown container state " + container.containerState);
			}
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion escalationCompletion) {

			// Ensure not have escalation completion
			if (escalationCompletion != null) {
				throw new IllegalStateException("Should not have " + EscalationCompletion.class.getSimpleName()
						+ " for " + ManagedObjectContainer.class.getSimpleName());
			}

			// Fail the managed object and escalate to function
			return new FailManagedObjectOperation(escalation, this.managedFunction);
		}
	}

	/**
	 * {@link ManagedObjectUser} implementation.
	 */
	private class ManagedObjectUserImpl implements ManagedObjectUser {

		@Override
		public void setManagedObject(final ManagedObject managedObject) {
			ManagedObjectOperation setManagedObject = new ManagedObjectOperation() {
				@Override
				public FunctionState execute(FunctionStateContext context) {

					// Easy access to the container
					ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

					// Determine if still loading or already loaded/failed
					if ((container.containerState != ManagedObjectContainerState.LOADING)
							|| (container.managedObject != null) || (container.failure != null)) {
						// Clean up extra managed object
						return new CleanupManagedObjectOperation(managedObject);
					}

					// Ensure have a managed object
					if (managedObject == null) {
						// Fail the container, as must have managed object
						container.failure = new IllegalStateException("Null ManagedObject provided for "
								+ container.metaData.getBoundManagedObjectName() + " from source "
								+ container.metaData.getManagedObjectSource().getClass().getName());
						container.sourcingLatch.failFunctions(container.failure, true);
						return null;
					}

					// Load the managed object
					container.managedObject = managedObject;
					container.asynchronousStartTime = NO_ASYNC_OPERATION;

					// Wake up any waiting functions
					container.sourcingLatch.releaseFunctions(true);

					// Nothing further to set the managed object
					return null;
				}
			};
			ManagedObjectContainerImpl.this.doOperation(setManagedObject);
		}

		@Override
		public void setFailure(final Throwable cause) {
			ManagedObjectOperation failManagedObject = new ManagedObjectOperation() {
				@Override
				public FunctionState execute(FunctionStateContext context) {

					// Easy access to the container
					ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

					// Determine if still loading or already loaded/failed
					if ((container.containerState != ManagedObjectContainerState.LOADING)
							|| (container.managedObject != null) || (container.failure != null)) {
						// Already failure, so log additional failure
						OfficeFloorImpl.getFrameworkLogger().log(Level.WARNING,
								"Additional failure in sourcing ManagedObject "
										+ ManagedObjectContainerImpl.this.metaData.getBoundManagedObjectName()
										+ " for type "
										+ ManagedObjectContainerImpl.this.metaData.getObjectType().getName(),
								cause);
						return null;
					}

					// Flag failure of managed object
					container.failure = cause;

					// Wake up any waiting functions
					container.sourcingLatch.failFunctions(cause, true);

					// Nothing further to fail the managed object
					return null;
				}
			};
			ManagedObjectContainerImpl.this.doOperation(failManagedObject);
		}
	}

	/**
	 * Asynchronous {@link FunctionState}.
	 */
	private class AsynchronousFunction extends ManagedObjectOperation {

		/**
		 * {@link FunctionState} instances for asynchronous operations.
		 */
		private final Deque<FunctionState> functions = new ConcurrentLinkedDeque<>();

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {

			// Remove all the functions for execution
			FunctionState asynchronousFunctions = null;
			while (!this.functions.isEmpty()) {
				asynchronousFunctions = Promise.then(asynchronousFunctions, this.functions.remove());
			}

			// Undertake the asynchronous operations
			return asynchronousFunctions;
		}
	}

	/**
	 * {@link AsynchronousContext} implementation.
	 */
	private class AsynchronousContextImpl implements AsynchronousContext {

		/**
		 * {@link AsynchronousFunction}.
		 */
		private final AsynchronousFunction asynchronousFunction = new AsynchronousFunction();

		@Override
		public <T extends Throwable> void start(AsynchronousOperation<T> operation) {

			// Easy access to the container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Require thread state safety, as different threads
			container.isRequireThreadStateSafety = true;

			// Start asynchronous operation
			FunctionState function = new ManagedObjectOperation() {
				@Override
				public FunctionState execute(FunctionStateContext context) {

					// Ensure also thread state safety to complete
					container.isRequireThreadStateSafety = true;

					// Ignore starting operation if in failed state
					if (container.failure != null) {
						return null;
					}

					// Ensure asynchronous operation not already started
					if (container.asynchronousStartTime != NO_ASYNC_OPERATION) {
						return null;
					}

					// Flag start of asynchronous operation
					container.asynchronousStartTime = container.metaData.getMonitorClock().currentTimeMillis();
					return null;

				}
			};

			// Undertake the asynchronous operation
			function = Promise.then(function, this.createAsynchronousFunction(operation));
			this.asynchronousFunction.functions.add(function);
			container.doOperation(this.asynchronousFunction);
		}

		@Override
		public <T extends Throwable> void complete(AsynchronousOperation<T> operation) {

			// Easy access to the container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Complete asynchronous operation
			FunctionState function = new ManagedObjectOperation() {
				@Override
				public FunctionState execute(FunctionStateContext context) {

					// Flag no asynchronous operation occurring
					container.asynchronousStartTime = NO_ASYNC_OPERATION;

					// Ignore completing operation if in failed state
					if (container.failure != null) {
						return null;
					}

					// Release functions waiting on the asynchronous operation
					container.operationsLatch.releaseFunctions(false);
					return null;
				}
			};

			// Undertake the asynchronous operation
			function = Promise.then(function, this.createAsynchronousFunction(operation));
			this.asynchronousFunction.functions.add(function);
			container.doOperation(this.asynchronousFunction);
		}

		/**
		 * Create the {@link FunctionState} for the {@link AsynchronousOperation}.
		 * 
		 * @param operation {@link AsynchronousOperation}.
		 * @return {@link FunctionState} for the {@link AsynchronousOperation}.
		 */
		private <T extends Throwable> FunctionState createAsynchronousFunction(AsynchronousOperation<T> operation) {

			// Easy access to the container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Obtain the thread state for the function
			ThreadStateContext threadContext = ThreadStateImpl.currentThreadContext(container.responsibleThreadState);

			// As asynchronous operation, must have thread safety
			threadContext.flagRequiresThreadStateSafety();

			// Ensure have operation
			if (operation == null) {
				return null;
			}

			// Create function for the asynchronous operation
			return threadContext.createFunction((flow) -> {
				operation.run();
				return null;
			}, ManagedObjectContainerImpl.this.responsibleThreadState);
		}
	}

	/**
	 * {@link ManagedObjectContext} implementation.
	 */
	private class ManagedObjectContextImpl implements ManagedObjectContext {

		@Override
		public String getBoundName() {
			return ManagedObjectContainerImpl.this.metaData.getBoundManagedObjectName();
		}

		@Override
		public Logger getLogger() {
			return ManagedObjectContainerImpl.this.metaData.getLogger();
		}

		@Override
		public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {

			// Easy access to the container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Run the process safe operation
			return container.responsibleThreadState.runProcessSafeOperation(operation);
		}
	}

	@Override
	public FunctionState checkReady(final ManagedObjectReadyCheck check) {
		return new ManagedObjectOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) {

				// Easy access to the container
				ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

				// Determine if have the managed object
				if (container.managedObject == null) {
					// Not yet source, so wait on sourcing
					return Promise.then(container.sourcingLatch.awaitOnAsset(check.getLatchFunction()),
							check.setNotReady());
				}

				// Determine if within asynchronous operation
				if (container.asynchronousStartTime != NO_ASYNC_OPERATION) {
					// Must wait on the asynchronous operation
					return Promise.then(container.operationsLatch.awaitOnAsset(check.getLatchFunction()),
							check.setNotReady());
				}

				// Nothing further for checking
				return null;
			}
		};
	}

	@Override
	public Object getObject() {

		// Only return Object if in valid state
		if (this.containerState == ManagedObjectContainerState.OBJECT_AVAILABLE) {
			// Return the Object
			return this.object;
		}

		// Incorrect state if here
		throw new IllegalStateException(
				"ManagedObject in incorrect state " + this.containerState + " to obtain Object");
	}

	@Override
	public Object getOptionalObject() {

		// Return only if available
		switch (this.containerState) {
		case OBJECT_AVAILABLE:
			// Return the available object
			return this.object;

		default:
			return null; // not available
		}
	}

	@Override
	public <E> FunctionState extractExtension(final ManagedObjectExtensionExtractor<E> extractor,
			final E[] managedObjectExtensions, final int extensionIndex, TeamManagement responsibleTeam) {
		return new ManagedObjectOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) throws Throwable {

				// Easy access to the container
				ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

				// Ensure in appropriate state
				if (container.containerState != ManagedObjectContainerState.OBJECT_AVAILABLE) {
					throw new IllegalStateException(
							"Can not extract managed object extension in managed object container state "
									+ container.containerState);
				}

				// Obtain the source managed object
				ManagedObject sourceManagedObject = container.managedObject;
				ManagedObjectPool pool = container.metaData.getManagedObjectPool();
				if (pool != null) {
					sourceManagedObject = pool.getSourcedManagedObject(sourceManagedObject);
				}

				// Extract the extension
				E extension = extractor.extractExtension(sourceManagedObject, container.metaData);

				// Register the extension
				managedObjectExtensions[extensionIndex] = extension;

				// Nothing further to administer
				return null;
			}
		};
	}

	@Override
	public FunctionState unregisterGovernance(final int governanceIndex) {
		return new ManagedObjectOperation() {
			@Override
			public FunctionState execute(FunctionStateContext context) {

				// Easy access to the container
				ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

				// Unregister the governance
				container.registeredGovernances[governanceIndex] = null;

				// Determine if waiting on unloading governance
				if (container.containerState != ManagedObjectContainerState.UNLOAD_WAITING_GOVERNANCE) {
					return null; // not waiting to unload
				}

				// Determine if all governance unloaded
				for (int i = 0; i < container.registeredGovernances.length; i++) {
					if (container.registeredGovernances[i] != null) {
						// Governance still active
						return null;
					}
				}

				// No further governance, so unload the managed object
				container.containerState = ManagedObjectContainerState.UNLOADING;
				return new UnloadManagedObjectOperation();
			}
		};
	}

	@Override
	public FunctionState unloadManagedObject() {
		return new UnloadManagedObjectOperation();
	}

	/**
	 * Unloads the {@link ManagedObject}.
	 */
	private class UnloadManagedObjectOperation extends ManagedObjectOperation {

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to the container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			switch (container.containerState) {
			case NOT_LOADED:
			case PRE_LOAD_ADMINISTRATION:
			case LOADING:

				// Not loaded, so no need to unload.
				// Also stops infinite loop with recycle process creating
				// further managed objects.
				return null;

			case LOADED:
			case GOVERNING:
			case GOVERNED:
			case COORDINATING:
			case OBJECT_AVAILABLE:

				// Flag that unloading governance
				container.containerState = ManagedObjectContainerState.UNLOAD_WAITING_GOVERNANCE;

			case UNLOAD_WAITING_GOVERNANCE:

				// Ensure not registered with any governance
				for (int i = 0; i < container.registeredGovernances.length; i++) {
					RegisteredGovernance registeredGovernance = container.registeredGovernances[i];
					if (registeredGovernance != null) {

						// Wait on governance to unload
						return null;
					}
				}

			case UNLOADING:

				// Create the recycle function
				FunctionState recycle = new CleanupManagedObjectOperation(container.managedObject).execute(context);

				// Release permanently as managed object no longer being used
				container.sourcingLatch.releaseFunctions(true);
				if (container.operationsLatch != null) {
					container.operationsLatch.releaseFunctions(true);
				}

				// Release reference to managed object to not unload again
				container.managedObject = null;
				container.containerState = ManagedObjectContainerState.COMPLETE;

				// Recycle the managed object
				return recycle;

			case COMPLETE:
				// Do nothing
			}

			// Managed object unloaded
			return null;
		}
	}

	/**
	 * Cleans up the {@link ManagedObject}.
	 */
	private class CleanupManagedObjectOperation extends ManagedObjectOperation {

		/**
		 * {@link ManagedObject} to clean up.
		 */
		private final ManagedObject managedObject;

		/**
		 * Instantiate.
		 * 
		 * @param managedObject {@link ManagedObject} to clean up.
		 */
		public CleanupManagedObjectOperation(ManagedObject managedObject) {
			this.managedObject = managedObject;
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to the container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Create the recycle function
			ManagedObjectCleanup cleanup = container.responsibleThreadState.getProcessState().getManagedObjectCleanup();
			FunctionState recycle = container.metaData.recycle(this.managedObject, cleanup);
			if (recycle == null) {

				// No recycle, so return directly to pool (if pooled)
				ManagedObjectPool pool = container.metaData.getManagedObjectPool();
				if (pool != null) {
					pool.returnManagedObject(this.managedObject);
				}
			}

			// Return the recycle function
			return recycle;
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
			context.failFunctions(this.failure, true);

			// No further checking necessary
			return;
		}

		// Not ready if undertaking an asynchronous operation
		if (this.asynchronousStartTime != NO_ASYNC_OPERATION) {

			// Determine if asynchronous operation has timed out
			long idleTime = this.metaData.getMonitorClock().currentTimeMillis() - this.asynchronousStartTime;
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

				// Flag now in error
				this.failure = timeoutFailure;
				this.asynchronousStartTime = NO_ASYNC_OPERATION;

				// Fail job nodes waiting on this
				this.sourcingLatch.failFunctions(timeoutFailure, true);
				if (this.operationsLatch != null) {
					this.operationsLatch.failFunctions(timeoutFailure, true);
				}
			}
		}
	}

	/**
	 * {@link FunctionState} to place the {@link ManagedObjectContainer} in a failed
	 * state.
	 */
	private class FailManagedObjectOperation extends ManagedObjectOperation {

		/**
		 * Cause of the failure.
		 */
		private final Throwable failure;

		/**
		 * Optional {@link ManagedFunctionContainer} requiring the
		 * {@link ManagedObject}.
		 */
		private final ManagedFunctionContainer managedFunction;

		/**
		 * Instantiate.
		 * 
		 * @param failure         Cause of the failure.
		 * @param managedFunction {@link ManagedFunctionContainer} requiring the
		 *                        {@link ManagedObject}.
		 */
		public FailManagedObjectOperation(Throwable failure, ManagedFunctionContainer managedFunction) {
			this.failure = failure;
			this.managedFunction = managedFunction;
		}

		@Override
		public FunctionState execute(FunctionStateContext context) {

			// Easy access to container
			ManagedObjectContainerImpl container = ManagedObjectContainerImpl.this;

			// Flag failure (puts container into failed state)
			if (container.failure == null) {
				// Only capture first failure
				container.failure = this.failure;
			}

			// Permanently fail latched functions
			container.sourcingLatch.failFunctions(this.failure, true);
			if (container.operationsLatch != null) {
				// Asynchronous so include permanently failing operations
				container.operationsLatch.failFunctions(this.failure, true);
			}

			// Propagate failure to managed function
			if (this.managedFunction != null) {
				return new AbstractDelegateFunctionState(this.managedFunction) {
					@Override
					public FunctionState execute(FunctionStateContext context) throws Throwable {
						// Propagate failure to the managed function
						throw FailManagedObjectOperation.this.failure;
					}
				};
			}

			// Nothing further to fail this container
			return null;
		}
	}

	/**
	 * Operation to be undertaken to change the {@link ManagedObjectContainer}.
	 */
	private abstract class ManagedObjectOperation extends AbstractLinkedListSetEntry<FunctionState, Flow>
			implements FunctionState {

		@Override
		public boolean isRequireThreadStateSafety() {
			return ManagedObjectContainerImpl.this.isRequireThreadStateSafety;
		}

		@Override
		public ThreadState getThreadState() {
			return ManagedObjectContainerImpl.this.responsibleThreadState;
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
		 * Indicates pre-load {@link Administration} undertaken.
		 */
		PRE_LOAD_ADMINISTRATION,

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
		 * Indicates the {@link ManagedObject} has been loaded and is not in the process
		 * of being governed.
		 */
		GOVERNING,

		/**
		 * Indicates the {@link ManagedObject} now has appropriate {@link Governance}.
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
		 * Indicates that the {@link ManagedObject} is waiting on a {@link Governance}
		 * to be unloaded. At this point, it should no longer be used for {@link Task}
		 * functionality.
		 */
		UNLOAD_WAITING_GOVERNANCE,

		/**
		 * Indicates the {@link ManagedObject} is being unloaded.
		 */
		UNLOADING,

		/**
		 * {@link ManagedObject} has been unloaded and {@link ManagedObjectContainer} is
		 * complete in its use.
		 */
		COMPLETE
	}

}
