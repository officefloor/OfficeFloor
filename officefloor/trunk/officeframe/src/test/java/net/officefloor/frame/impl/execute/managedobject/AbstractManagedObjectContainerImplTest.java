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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
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
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;
import org.easymock.internal.AlwaysMatcher;

/**
 * Contains functionality for testing the {@link ManagedObjectContainerImpl}.
 * 
 * @author Daniel
 */
public abstract class AbstractManagedObjectContainerImplTest extends
		OfficeFrameTestCase {

	/**
	 * Creates all meta-data combinations for the input {@link TestCase} class.
	 * 
	 * @param testCaseClass
	 *            {@link AbstractManagedObjectContainerImplTest} class.
	 * @param filters
	 *            {@link MetaDataScenarioFilter} instances to filter out
	 *            scenarios.
	 * @return {@link TestSuite} for each meta-data combination.
	 */
	protected static <T extends AbstractManagedObjectContainerImplTest> TestSuite createMetaDataCombinationTestSuite(
			Class<T> testCaseClass, MetaDataScenarioFilter... filters) {

		// Create the test suite of all meta-data combinations
		TestSuite suite = new TestSuite(testCaseClass.getName());
		for (int asynchronous = 0; asynchronous < 2; asynchronous++) {
			for (int coordinating = 0; coordinating < 2; coordinating++) {
				for (int pooled = 0; pooled < 2; pooled++) {
					NEXT_SCENARIO: for (int recycled = 0; recycled < 2; recycled++) {

						// Determine meta-data
						boolean isAsynchronous = (asynchronous == 1);
						boolean isCoordinating = (coordinating == 1);
						boolean isPooled = (pooled == 1);
						boolean isRecycled = (recycled == 1);

						// Determine if filter the scenario
						for (MetaDataScenarioFilter filter : filters) {
							if (filter.isFilter(isAsynchronous, isCoordinating,
									isPooled, isRecycled)) {
								// Do not include scenario
								continue NEXT_SCENARIO;
							}
						}

						// Create the test case
						AbstractManagedObjectContainerImplTest testCase;
						try {
							testCase = testCaseClass.newInstance();
						} catch (Throwable ex) {
							suite
									.addTest(new TestCase(testCaseClass
											.getName()) {
										@Override
										protected void runTest() {
											fail("Must provide public default constructor");
										}
									});
							return suite;
						}

						// Obtain test prefix from class (stripping off suffix)
						String testNamePrefix = testCaseClass.getSimpleName();
						String testNameSuffix = ManagedObjectContainer.class
								.getSimpleName()
								+ "Test";
						testNamePrefix = testNamePrefix.replace(testNameSuffix,
								"");

						// Specify state and indicate name
						StringBuilder testName = new StringBuilder();
						testName.append(testNamePrefix);
						if (isAsynchronous) {
							testCase.setAsynchronous();
							testName.append("-asynchronous");
						}
						if (isCoordinating) {
							testCase.setCoordinating();
							testName.append("-coordinating");
						}
						if (isPooled) {
							testCase.setPooled();
							testName.append("-pooled");
						}
						if (isRecycled) {
							testCase.setRecycled();
							testName.append("-recycled");
						}

						// Specify the name of the test case
						testCase.setName(testName.toString());

						// Add the test case
						suite.addTest(testCase);
					}
				}
			}
		}

		// Return the test suite
		return suite;
	}

	/**
	 * Provides ability to filter meta-data scenarios.
	 */
	protected static interface MetaDataScenarioFilter {

		/**
		 * Indicates if to filter out the scenario.
		 * 
		 * @param isAsynchronous
		 *            Is {@link AsynchronousManagedObject}.
		 * @param isCoordinating
		 *            Is {@link CoordinatingManagedObject}.
		 * @param isPooled
		 *            Is using a {@link ManagedObjectPool}.
		 * @param isRecycled
		 *            Is using a recycle {@link JobNode}.
		 * @return <code>true</code> to filter out scenario.
		 */
		boolean isFilter(boolean isAsynchronous, boolean isCoordinating,
				boolean isPooled, boolean isRecycled);
	}

	/**
	 * Flag indicating if {@link AsynchronousManagedObject}.
	 */
	private boolean isAsynchronous = false;

	/**
	 * Flag indicating if {@link CoordinatingManagedObject}.
	 */
	private boolean isCoordinating = false;

	/**
	 * Flag indicating if {@link ManagedObjectPool} being used.
	 */
	private boolean isPooled = false;

	/**
	 * Flag indicating if {@link ManagedObject} is recycled.
	 */
	private boolean isRecycled = false;

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final ManagedObjectMetaData<Indexed> managedObjectMetaData = this
			.createMock(ManagedObjectMetaData.class);

	/**
	 * Sourcing {@link AssetManager}.
	 */
	private final AssetManager sourcingAssetManager = this
			.createMock(AssetManager.class);

	/**
	 * Sourcing {@link AssetMonitor}.
	 */
	private final AssetMonitor sourcingAssetMonitor = this
			.createMock(AssetMonitor.class);

	/**
	 * Operations {@link AssetManager}.
	 */
	private final AssetManager operationsAssetManager = this
			.createMock(AssetManager.class);

	/**
	 * Operations {@link AssetMonitor}.
	 */
	private final AssetMonitor operationsAssetMonitor = this
			.createMock(AssetMonitor.class);

	/**
	 * {@link JobContext}.
	 */
	private final JobContext jobContext = this.createMock(JobContext.class);

	/**
	 * {@link JobNode}.
	 */
	private final JobNode jobNode = this.createMock(JobNode.class);

	/**
	 * {@link JobNodeActivateSet}.
	 */
	private final JobNodeActivateSet jobActivateSet = this
			.createMock(JobNodeActivatableSet.class);

	/**
	 * {@link ManagedObjectPool}.
	 */
	private final ManagedObjectPool managedObjectPool = this
			.createMock(ManagedObjectPool.class);

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource = this
			.createMock(ManagedObjectSource.class);

	/**
	 * {@link MockManagedObject}.
	 */
	private final MockManagedObject managedObject = this
			.createMock(MockManagedObject.class);

	/**
	 * Recycle {@link JobNode}.
	 */
	private final JobNode recycleJobNode = this.createMock(JobNode.class);

	/**
	 * {@link WorkContainer}.
	 */
	private final WorkContainer<?> workContainer = this
			.createMock(WorkContainer.class);

	/**
	 * {@link Flow}.
	 */
	private final Flow flow = this.createMock(Flow.class);

	/**
	 * {@link ThreadState}.
	 */
	private final ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * {@link ProcessState}.
	 */
	private final ProcessState processState = this
			.createMock(ProcessState.class);

	/**
	 * {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private final ObjectRegistry<Indexed> objectRegistry = this
			.createMock(ObjectRegistry.class);

	/**
	 * Flags that {@link ManagedObjectContainer} is initialised so can no longer
	 * change {@link ManagedObjectMetaData}.
	 */
	private boolean isInitialised = false;

	/**
	 * Checks that not initialised.
	 */
	private void checkNotInitialised() {
		if (this.isInitialised) {
			fail("May not change ManagedObject meta-data after initialising");
		}
	}

	/**
	 * Flags as a {@link AsynchronousManagedObject}.
	 */
	protected void setAsynchronous() {
		this.checkNotInitialised();
		this.isAsynchronous = true;
	}

	/**
	 * Flags as a {@link CoordinatingManagedObject}.
	 */
	protected void setCoordinating() {
		this.checkNotInitialised();
		this.isCoordinating = true;
	}

	/**
	 * Flags as having a {@link ManagedObjectPool}.
	 */
	protected void setPooled() {
		this.checkNotInitialised();
		this.isPooled = true;
	}

	/**
	 * Flags has a recycle {@link JobNode}.
	 */
	protected void setRecycled() {
		this.checkNotInitialised();
		this.isRecycled = true;
	}

	/**
	 * Records initialising the {@link ManagedObjectContainer}.
	 */
	protected void record_MoContainer_init() {

		// Flag now initialised
		this.isInitialised = true;

		// Obtains the process lock
		this.recordReturn(this.processState,
				this.processState.getProcessLock(), "Process lock");

		// Sourcing monitor
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getSourcingManager(),
				this.sourcingAssetManager);
		this.recordReturn(this.sourcingAssetManager, this.sourcingAssetManager
				.createAssetMonitor(null), this.sourcingAssetMonitor,
				new TypeMatcher(ManagedObjectContainerImpl.class));
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.isManagedObjectAsynchronous(),
				this.isAsynchronous);

		// Operations monitor (if asynchronous)
		if (this.isAsynchronous) {
			// Operations asset monitor only required if asynchronous
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.getOperationsManager(),
					this.operationsAssetManager);
			this.recordReturn(this.operationsAssetManager,
					this.operationsAssetManager.createAssetMonitor(null),
					this.operationsAssetMonitor, new TypeMatcher(
							ManagedObjectContainerImpl.class));
		}
	}

	/**
	 * Records sourcing the {@link ManagedObject}.
	 * 
	 * @param isSourced
	 *            Indicates if the {@link ManagedObject} is sourced.
	 * @param failure
	 *            Possible failure in sourcing the {@link ManagedObject}.
	 *            <code>null</code> indicates no failure.
	 *            {@link RuntimeException} is thrown rather than set on
	 *            {@link ManagedObjectUser}.
	 */
	protected void record_MoContainer_sourceManagedObject(
			final boolean isSourced, final Throwable failure) {

		// Determine the current time for testing
		long currentTime = System.currentTimeMillis();

		// Record set up to source the managed object
		this.recordReturn(this.jobContext, this.jobContext.getTime(),
				currentTime);
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getManagedObjectPool(),
				(this.isPooled ? this.managedObjectPool : null));

		// Create the matcher for attempting to source the managed object
		ArgumentsMatcher sourceMatcher = new AlwaysMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				ManagedObjectUser user = (ManagedObjectUser) actual[0];

				// Source managed object if specified
				if (isSourced) {
					user
							.setManagedObject(AbstractManagedObjectContainerImplTest.this.managedObject);
				}

				// Indicate if have failure and not runtime (as will be thrown)
				if (failure != null) {
					if (!(failure instanceof RuntimeException)) {
						user.setFailure(failure);
					}
				}
				return true;
			}
		};

		// Record sourcing the managed object
		MockControl control;
		if (this.isPooled) {
			// Attempt to source from pool
			this.managedObjectPool.sourceManagedObject(null);
			control = this.control(this.managedObjectPool);
		} else {
			// Attempt to source from managed object source directly
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.getManagedObjectSource(),
					this.managedObjectSource);
			this.managedObjectSource.sourceManagedObject(null);
			control = this.control(this.managedObjectSource);
		}
		control.setMatcher(sourceMatcher);

		// Throw the runtime exception (other will be set by sourcing)
		if (failure != null) {
			if (failure instanceof RuntimeException) {
				control.setThrowable(failure);
				return; // no further execution
			}

		} else {
			// Not sourced, so wait on managed object to source
			if (!isSourced) {
				this.recordReturn(this.sourcingAssetMonitor,
						this.sourcingAssetMonitor.waitOnAsset(this.jobNode,
								this.jobActivateSet), true);
			}
		}
	}

	/**
	 * Records setting the {@link ManagedObject} on the
	 * {@link ManagedObjectUser}.
	 * 
	 * @param isInLoadScope
	 *            Flag indicating if set within
	 *            {@link ManagedObjectContainer#loadManagedObject(JobContext, JobNode, JobNodeActivateSet)}
	 *            method.
	 * @param object
	 *            Object of the {@link ManagedObject}.
	 */
	protected void record_MoUser_setManagedObject(boolean isInLoadScope,
			Object object) {

		// Indicates if recycled
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData
						.createRecycleJobNode(this.managedObject),
				(this.isRecycled ? this.recycleJobNode : null));

		// Obtain the object
		try {
			this.recordReturn(this.managedObject, this.managedObject
					.getObject(), object);
		} catch (Throwable ex) {
			fail("Should not have exception: " + ex.getMessage());
		}

		// Indicate if asynchronous
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.isManagedObjectAsynchronous(),
				this.isAsynchronous);
		if (this.isAsynchronous) {
			this.managedObject.registerAsynchronousCompletionListener(null);
			this.control(this.managedObject).setMatcher(
					new TypeMatcher(ManagedObjectContainerImpl.class));
		}

		// Obtained managed object
		if (isInLoadScope) {
			// Using activate set from job container
			this.sourcingAssetMonitor.activateJobNodes(this.jobActivateSet,
					true);
		} else {
			// Managed object source loaded at later time, so no activate set
			this.sourcingAssetMonitor.activateJobNodes(null, true);
		}
	}

	/**
	 * Records setting a failure on the {@link ManagedObjectUser}.
	 */
	protected void record_MoUser_setFailure(boolean isInLoadScope,
			final Throwable failure) {

		// Obtained managed object
		if (isInLoadScope) {
			// Using activate set from job container
			this.sourcingAssetMonitor.failJobNodes(this.jobActivateSet,
					failure, true);
			if (this.isAsynchronous) {
				this.operationsAssetMonitor.failJobNodes(this.jobActivateSet,
						failure, true);
			}

		} else {
			// Managed object source failed at later time, so no activate set
			this.sourcingAssetMonitor.failJobNodes(null, failure, true);
			if (this.isAsynchronous) {
				this.operationsAssetMonitor.failJobNodes(null, failure, true);
			}
		}
	}

	/**
	 * Records unloading the {@link ManagedObject} immediately.
	 */
	protected void record_MoUser_unloadedImmediately() {

		// Create a recycle job for the managed object
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData
						.createRecycleJobNode(this.managedObject),
				(this.isRecycled ? this.recycleJobNode : null));

		// Record unloading the managed object
		this.record_unloadManagedObject();
	}

	/**
	 * Records coordinating the {@link CoordinatingManagedObject}.
	 * 
	 * @param isCoordinating
	 *            Flag indicating if coordinating.
	 * @param coordinateFailure
	 */
	protected void record_MoContainer_coordinateManagedObject(
			Throwable coordinateFailure) {
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.isCoordinatingManagedObject(),
				isCoordinating);
		if (isCoordinating) {
			// Coordinating so record coordination
			this.recordReturn(this.jobNode, this.jobNode.getFlow(), this.flow);
			this.recordReturn(this.flow, this.flow.getThreadState(),
					this.threadState);
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.createObjectRegistry(
							this.workContainer, this.threadState),
					this.objectRegistry);
			try {
				this.managedObject.loadObjects(this.objectRegistry);
			} catch (Throwable ex) {
				fail("Should not have exception: " + ex.getMessage());
			}
			if (coordinateFailure != null) {
				this.control(this.managedObject)
						.setThrowable(coordinateFailure);
			}
		}
	}

	/**
	 * States that the {@link ManagedObjectContainer} may be within when
	 * checking ready.
	 */
	protected static enum ReadyState {
		FAILURE, NOT_SOURCED, SOURCING_TIMEOUT, READY, IN_ASYNC_OPERATION, ASYNC_OPERATION_TIMED_OUT, UNLOADED
	};

	/**
	 * Records the checking the particular state of the
	 * {@link ManagedObjectContainer}.
	 * 
	 * @param readyState
	 *            {@link ReadyState}.
	 */
	protected void record_MoContainer_isManagedObjectReady(ReadyState readyState) {

		// Based on state, set the flags
		boolean isSourced = false;
		boolean isInAsyncOperation = false;
		boolean isTimedOut = false;
		switch (readyState) {
		case FAILURE:
			// Propagated immediately, so no checking done
			return;
		case NOT_SOURCED:
			break;
		case SOURCING_TIMEOUT:
			isTimedOut = true;
			break;
		case READY:
			isSourced = true;
			break;
		case IN_ASYNC_OPERATION:
			if (!this.isAsynchronous) {
				fail("Must be asynchronous managed object");
			}
			isSourced = true;
			isInAsyncOperation = true;
			break;
		case ASYNC_OPERATION_TIMED_OUT:
			if (!this.isAsynchronous) {
				fail("Must be asynchronous managed object");
			}
			isSourced = true;
			isInAsyncOperation = true;
			isTimedOut = true;
			break;
		case UNLOADED:
			// Should not check anything as illegal to call in this state
			return;
		default:
			fail("Unknown state " + readyState);
			break;
		}

		// Record is ready based on flags
		this.record_MoContainer_isManagedObjectReady(isSourced,
				isInAsyncOperation, isTimedOut);
	}

	/**
	 * Records whether {@link ManagedObject} is ready.
	 * 
	 * @param isSourced
	 *            Indicates if {@link ManagedObject} sourced.
	 * @param isInAsyncOperation
	 *            Indicates if within an asynchronous operation.
	 * @param isTimedOut
	 *            Indicates if sourcing or asynchronous operation timed out.
	 */
	private void record_MoContainer_isManagedObjectReady(boolean isSourced,
			boolean isInAsyncOperation, boolean isTimedOut) {

		// If sourced, then checks to see if asynchronous
		if (isSourced) {
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.isManagedObjectAsynchronous(),
					this.isAsynchronous);
		}

		// Check time if not sourced or in asynchronous operation
		if ((!isSourced) || (isInAsyncOperation)) {

			// Set values based on whether timed out
			long currentTime = (isTimedOut ? Long.MAX_VALUE : System
					.currentTimeMillis());
			long timeout = (isTimedOut ? 0 : Long.MAX_VALUE);

			// Record asynchronous operation check
			this.recordReturn(this.jobContext, this.jobContext.getTime(),
					currentTime);
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.getTimeout(), timeout);

			// Escalates if timed out
			if (isTimedOut) {
				return;
			}
		}

		// Record waiting if asynchronous operation
		if (isInAsyncOperation) {
			this.recordReturn(this.operationsAssetMonitor,
					this.operationsAssetMonitor.waitOnAsset(this.jobNode,
							this.jobActivateSet), true);
		}

		// Record waiting if not sourced
		if (!isSourced) {
			this.recordReturn(this.sourcingAssetMonitor,
					this.sourcingAssetMonitor.waitOnAsset(this.jobNode,
							this.jobActivateSet), true);
		}
	}

	/**
	 * Records notifying start of an asynchronous operation.
	 */
	protected void record_AsynchronousListener_notifyStart() {
		// Does nothing, but allows for documenting this call in recording
	}

	/**
	 * Records notifying completion of an asynchronous operation.
	 */
	protected void record_AsynchronousListener_notifyComplete() {
		// Record waking up job nodes on the operations monitor.
		// Never has an activate job set as relies on Office Manager.
		this.operationsAssetMonitor.activateJobNodes(null, false);
	}

	/**
	 * Records unloading the {@link ManagedObject}.
	 * 
	 * @param isUnload
	 *            <code>true</code> indicates that the {@link ManagedObject}
	 *            requires unloading.
	 */
	protected void record_MoContainer_unloadManagedObject(boolean isUnLoad) {

		// Record unloading managed object (if loaded)
		if (isUnLoad) {
			this.record_unloadManagedObject();
		}

		// Permanently notify managed object unloaded
		this.sourcingAssetMonitor.activateJobNodes(this.jobActivateSet, true);
		if (this.isAsynchronous) {
			this.operationsAssetMonitor.activateJobNodes(this.jobActivateSet,
					true);
		}
	}

	/**
	 * Records unloading the {@link ManagedObject}.
	 */
	private void record_unloadManagedObject() {
		if (this.isRecycled) {
			// Recycle managed object
			this.recycleJobNode.activateJob();

		} else {
			// Return whether pooled
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.getManagedObjectPool(),
					(this.isPooled ? this.managedObjectPool : null));
			if (this.isPooled) {
				// Return to pool
				this.managedObjectPool.returnManagedObject(this.managedObject);
			}
		}
	}

	/**
	 * Creates the {@link ManagedObjectContainer}.
	 * 
	 * @return {@link ManagedObjectContainer}.
	 */
	protected ManagedObjectContainer createManagedObjectContainer() {
		return new ManagedObjectContainerImpl(this.managedObjectMetaData,
				this.processState);
	}

	/**
	 * Loads the {@link ManagedObject}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @param isExpectedLoaded
	 *            If indicates should be loaded.
	 */
	protected void loadManagedObject(ManagedObjectContainer mo,
			boolean isExpectedLoaded) {
		boolean isLoaded = mo.loadManagedObject(this.jobContext, this.jobNode,
				this.jobActivateSet);
		assertEquals("Incorrect indicating if loaded", isExpectedLoaded,
				isLoaded);
	}

	/**
	 * Coordinates the {@link ManagedObject}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 */
	protected void coordinateManagedObject(ManagedObjectContainer mo) {
		mo.coordinateManagedObject(this.workContainer, this.jobContext,
				this.jobNode, this.jobActivateSet);
	}

	/**
	 * Checks if the {@link ManagedObject} is ready.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @param isExpectedReady
	 *            If indicates should be ready.
	 */
	protected void isManagedObjectReady(ManagedObjectContainer mo,
			boolean isExpectedReady) {
		boolean isReady = mo.isManagedObjectReady(this.jobContext,
				this.jobNode, this.jobActivateSet);
		assertEquals("Incorrect indicating if ready", isExpectedReady, isReady);
	}

	/**
	 * Unloads the {@link ManagedObject}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 */
	protected void unloadManagedObject(ManagedObjectContainer mo) {
		mo.unloadManagedObject(this.jobActivateSet);
	}

	/**
	 * Sets the {@link ManagedObject} on the {@link ManagedObjectUser}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @param object
	 *            Object of the {@link ManagedObject}.
	 */
	protected void managedObjectUser_setManagedObject(
			ManagedObjectContainer mo, Object object) {
		ManagedObjectContainerImpl impl = (ManagedObjectContainerImpl) mo;
		impl.setManagedObject(this.managedObject);
	}

	/**
	 * Sets the failure on the {@link ManagedObjectUser}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @param failure
	 *            Failure.
	 */
	protected void managedObjectUser_setFailure(ManagedObjectContainer mo,
			Throwable failure) {
		ManagedObjectContainerImpl impl = (ManagedObjectContainerImpl) mo;
		impl.setFailure(failure);
	}

	/**
	 * Obtains the Object of the {@link ManagedObject}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @return Object of the {@link ManagedObject}.
	 */
	protected Object getObject(ManagedObjectContainer mo) {
		Object object = mo.getObject(this.threadState);
		if (object != null) {
			// If have object, must also have managed object
			ManagedObject actualManagedObject = mo
					.getManagedObject(this.threadState);
			assertEquals("Incorrect managed object", this.managedObject,
					actualManagedObject);
		}
		return object;
	}

	/**
	 * Asserts the object of the {@link ManagedObject} is correct.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @param expectedObject
	 *            Expected object of the {@link ManagedObject}.
	 */
	protected void assert_getObject(ManagedObjectContainer mo,
			Object expectedObject) {
		Object object = this.getObject(mo);
		assertEquals("Incorrect object", expectedObject, object);
	}

	/**
	 * Notifies the {@link AsynchronousListener} that started asynchronous
	 * operation.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 */
	protected void asynchronousListener_notifyStarted(ManagedObjectContainer mo) {
		ManagedObjectContainerImpl impl = (ManagedObjectContainerImpl) mo;
		impl.notifyStarted();
	}

	/**
	 * Notifies the {@link AsynchronousListener} that completed asynchronous
	 * operation.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 */
	protected void asynchronousListener_notifyComplete(ManagedObjectContainer mo) {
		ManagedObjectContainerImpl impl = (ManagedObjectContainerImpl) mo;
		impl.notifyComplete();
	}

	/**
	 * Obtains the current time.
	 * 
	 * @param millisecondsInFuture
	 *            Time to be added to current time for return.
	 * @return Current time.
	 */
	protected long getFutureTime(long millisecondsInFuture) {
		return System.currentTimeMillis() + millisecondsInFuture;
	}

	/**
	 * {@link ManagedObject}.
	 */
	private static interface MockManagedObject extends
			AsynchronousManagedObject, CoordinatingManagedObject<Indexed> {
	}
}
