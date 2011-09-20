/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.api.escalate.ManagedObjectEscalation;
import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.ExtensionInterfaceExtractor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivatableSet;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectGovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.NameAwareManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.test.match.TypeMatcher;

import org.easymock.AbstractMatcher;
import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;

/**
 * Contains functionality for testing the {@link ManagedObjectContainerImpl}.
 * 
 * @author Daniel Sagenschneider
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
		for (int nameAware = 0; nameAware < 2; nameAware++) {
			for (int asynchronous = 0; asynchronous < 2; asynchronous++) {
				for (int coordinating = 0; coordinating < 2; coordinating++) {
					for (int pooled = 0; pooled < 2; pooled++) {
						NEXT_SCENARIO: for (int recycled = 0; recycled < 2; recycled++) {

							// Determine meta-data
							boolean isNameAware = (nameAware == 1);
							boolean isAsynchronous = (asynchronous == 1);
							boolean isCoordinating = (coordinating == 1);
							boolean isPooled = (pooled == 1);
							boolean isRecycled = (recycled == 1);

							// Determine if filter the scenario
							for (MetaDataScenarioFilter filter : filters) {
								if (filter.isFilter(isNameAware,
										isAsynchronous, isCoordinating,
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
								suite.addTest(new TestCase(testCaseClass
										.getName()) {
									@Override
									protected void runTest() {
										fail("Must provide public default constructor");
									}
								});
								return suite;
							}

							// Obtain test prefix from class (stripping off
							// suffix)
							String testNamePrefix = testCaseClass
									.getSimpleName();
							String testNameSuffix = ManagedObjectContainer.class
									.getSimpleName() + "Test";
							testNamePrefix = testNamePrefix.replace(
									testNameSuffix, "");

							// Specify state and indicate name
							StringBuilder testName = new StringBuilder();
							testName.append(testNamePrefix);
							if (isNameAware) {
								testCase.setNameAware();
								testName.append("-nameAware");
							}
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
		 * @param isNameAware
		 *            Is {@link NameAwareManagedObject}.
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
		boolean isFilter(boolean isNameAware, boolean isAsynchronous,
				boolean isCoordinating, boolean isPooled, boolean isRecycled);
	}

	/**
	 * Flag indicating if {@link NameAwareManagedObject}.
	 */
	private boolean isNameAware = false;

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
	 * Type of {@link Object} returned from the {@link ManagedObject}.
	 */
	private Class<?> objectType;

	/**
	 * Checks that not initialised.
	 */
	private void checkNotInitialised() {
		if (this.isInitialised) {
			fail("May not change ManagedObject meta-data after initialising");
		}
	}

	/**
	 * Flag as a {@link NameAwareManagedObject}.
	 */
	protected void setNameAware() {
		this.checkNotInitialised();
		this.isNameAware = true;
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
	 * Records obtaining the object type, typically for escalating.
	 */
	protected void record_MoMetaData_getObjectType() {
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getObjectType(), this.objectType);
	}

	/**
	 * Records initialising the {@link ManagedObjectContainer}.
	 */
	protected void record_MoContainer_init(Class<?> objectType) {

		// Flag now initialised
		this.isInitialised = true;
		this.objectType = objectType;

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

		// Obtain the governance meta-data
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getGovernanceMetaData(),
				new ManagedObjectGovernanceMetaData[0]);
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

		// Record set up to source the managed object
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getManagedObjectPool(),
				(this.isPooled ? this.managedObjectPool : null));

		// Create the matcher for attempting to source the managed object
		ArgumentsMatcher sourceMatcher = new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				ManagedObjectUser user = (ManagedObjectUser) actual[0];

				// Source managed object if specified
				if (isSourced) {
					user.setManagedObject(AbstractManagedObjectContainerImplTest.this.managedObject);
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
				// Record obtaining time attempting to source managed object
				long currentTime = System.currentTimeMillis();
				this.recordReturn(this.jobContext, this.jobContext.getTime(),
						currentTime);
			}
		}
	}

	/**
	 * Records setting the {@link ManagedObject} on the
	 * {@link ManagedObjectUser}.
	 * 
	 * @param isInLoadScope
	 *            Flag indicating if {@link ManagedObject} set immediately (in
	 *            other words not at a later time).
	 */
	protected void record_MoUser_setManagedObject(boolean isInLoadScope) {

		// Indicate if name aware
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.isNameAwareManagedObject(),
				this.isNameAware);
		if (this.isNameAware) {
			final String BOUND_MO_NAME = "BOUND_MO_NAME";
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.getBoundManagedObjectName(),
					BOUND_MO_NAME);
			this.managedObject.setBoundManagedObjectName(BOUND_MO_NAME);
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

		// Indicates if recycled
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData
						.createRecycleJobNode(this.managedObject),
				(this.isRecycled ? this.recycleJobNode : null));

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

		// Record in failed state (no activate set if not in load scope)
		this.record_setFailedState(FailedToSourceManagedObjectEscalation.class,
				(isInLoadScope ? this.jobActivateSet : null));
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
	 * Records governing the {@link ManagedObject}.
	 */
	protected void record_MoContainer_governManagedObject() {

		// Ensure Managed Object ready
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.isManagedObjectAsynchronous(),
				this.isAsynchronous);

		// Obtain the governance
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getGovernanceMetaData(),
				new ManagedObjectGovernanceMetaData[0]);

		// Obtain the process state
		this.recordReturn(this.jobNode, this.jobNode.getFlow(), this.flow);
		this.recordReturn(this.flow, this.flow.getThreadState(),
				this.threadState);
		this.recordReturn(this.threadState, this.threadState.getProcessState(),
				this.processState);
	}

	/**
	 * Records governing {@link ManagedObject} that is still loading.
	 */
	protected void record_MoContainer_governManagedObject_stillLoading() {
		this.recordReturn(this.sourcingAssetMonitor, this.sourcingAssetMonitor
				.waitOnAsset(this.jobNode, this.jobActivateSet), true);
	}

	/**
	 * Records coordinating the {@link CoordinatingManagedObject}.
	 * 
	 * @param coordinateFailure
	 *            Failure in coordinating.
	 * @param object
	 *            Object from the {@link ManagedObject}.
	 */
	protected void record_MoContainer_coordinateManagedObject(
			boolean isDependenciesReady, boolean isManagedObjectReady,
			Throwable coordinateFailure, Object object) {

		// Determine if coordinating Managed Object
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.isCoordinatingManagedObject(),
				this.isCoordinating);
		if (this.isCoordinating) {
			// Coordinating so determine if dependencies ready
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.isDependenciesReady(
							this.workContainer, this.jobContext, this.jobNode,
							this.jobActivateSet), isDependenciesReady);
			if (!isDependenciesReady) {
				// Dependencies not ready so no further processing
				return;
			}

			// Dependencies ready so ensure Managed Object ready
			this.recordReturn(this.managedObjectMetaData,
					this.managedObjectMetaData.isManagedObjectAsynchronous(),
					this.isAsynchronous);
			if (!isManagedObjectReady) {
				assertTrue(
						"Managed Object can only not be ready if asynchronous",
						this.isAsynchronous);
				// Never time out but not ready
				this.recordReturn(this.jobContext, this.jobContext.getTime(),
						System.currentTimeMillis());
				this.recordReturn(this.managedObjectMetaData,
						this.managedObjectMetaData.getTimeout(), Long.MAX_VALUE);
				this.recordReturn(this.operationsAssetMonitor,
						this.operationsAssetMonitor.waitOnAsset(this.jobNode,
								this.jobActivateSet), true);
				return; // Not ready so no further processing
			}

			// Ready so coordinate Managed Object
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
				fail("Exception while recording: " + ex.getMessage());
			}
			if (coordinateFailure != null) {
				this.control(this.managedObject)
						.setThrowable(coordinateFailure);
			}
		}

		// For testing always ready after coordinating
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.isManagedObjectAsynchronous(),
				this.isAsynchronous);

		// Obtain the object
		try {
			this.recordReturn(this.managedObject,
					this.managedObject.getObject(), object);
		} catch (Throwable ex) {
			fail("Should not have exception: " + ex.getMessage());
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
				// Record setting into a failed state
				this.record_setFailedState(
						((!isSourced) ? SourceManagedObjectTimedOutEscalation.class
								: ManagedObjectOperationTimedOutEscalation.class),
						this.jobActivateSet);
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
	 * Records setting in a failed state.
	 */
	protected <E extends ManagedObjectEscalation> void record_setFailedState(
			final Class<E> escalationType, final JobNodeActivateSet activateSet) {
		// Record obtaining the object type for the escalation
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getObjectType(), this.objectType);

		// Record failing to source the managed object permanently
		this.sourcingAssetMonitor.failJobNodes(this.jobActivateSet, null, true);
		this.control(this.sourcingAssetMonitor).setMatcher(
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect activate set", activateSet,
								actual[0]);
						assertTrue("Incorrect escalation type",
								escalationType.isInstance(actual[1]));
						assertEquals("Incorrect permanent flag", true,
								actual[2]);
						return true;
					}
				});

		// Record failing operations on managed object permanently
		if (this.isAsynchronous) {
			this.operationsAssetMonitor.failJobNodes(this.jobActivateSet, null,
					true);
			this.control(this.operationsAssetMonitor).setMatcher(
					new AbstractMatcher() {
						@Override
						public boolean matches(Object[] expected,
								Object[] actual) {
							assertEquals("Incorrect activate set", activateSet,
									actual[0]);
							assertTrue("Incorrect escalation type",
									escalationType.isInstance(actual[1]));
							assertEquals("Incorrect permanent flag", true,
									actual[2]);
							return true;
						}
					});
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
	 * Records a check on the {@link ManagedObjectContainer} {@link Asset}.
	 * 
	 * @param timeout
	 *            Timeout for the {@link ManagedObject}.
	 */
	protected void record_Asset_checkOnAsset(long timeout) {
		this.recordReturn(this.managedObjectMetaData,
				this.managedObjectMetaData.getTimeout(), timeout);
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
	 */
	protected void loadManagedObject(ManagedObjectContainer mo) {
		mo.loadManagedObject(this.jobContext, this.jobNode, this.jobActivateSet);
	}

	/**
	 * Govern the {@link ManagedObject}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @param isExpectedGoverning
	 *            Indicates if should be governed.
	 */
	protected void governManagedObject(ManagedObjectContainer mo,
			boolean isExpectedGoverning) {
		boolean isGoverning = mo.governManagedObject(this.workContainer,
				this.jobContext, this.jobNode, this.jobActivateSet);
		assertEquals("Incorrect indicating if governed", isExpectedGoverning,
				isGoverning);
	}

	/**
	 * Coordinates the {@link ManagedObject}.
	 * 
	 * @param mo
	 *            {@link ManagedObjectContainer}.
	 * @param isExpectedCoordinate
	 *            Indicates if should be coordinated.
	 */
	protected void coordinateManagedObject(ManagedObjectContainer mo,
			boolean isExpectedCoordinate) {
		boolean isCoordinated = mo.coordinateManagedObject(this.workContainer,
				this.jobContext, this.jobNode, this.jobActivateSet);
		assertEquals("Incorrect indicating if coordinated",
				isExpectedCoordinate, isCoordinated);
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
		boolean isReady = mo.isManagedObjectReady(this.workContainer,
				this.jobContext, this.jobNode, this.jobActivateSet);
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
			// Extract the Managed Object
			ManagedObject actualManagedObject = mo
					.extractExtensionInterface(new ExtensionInterfaceExtractor<ManagedObject>() {
						@Override
						public ManagedObject extractExtensionInterface(
								ManagedObject managedObject,
								ManagedObjectMetaData<?> managedObjectMetaData) {
							// Return the actual Managed Object
							return managedObject;
						}
					});

			// If have object, must also have managed object
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
	 * Asserts the correct {@link Escalation} returning the {@link Escalation}.
	 * 
	 * @param propagate
	 *            {@link PropagateEscalationError}.
	 * @param escalationType
	 *            Type of {@link Escalation} expected.
	 * @return Specific {@link Escalation}.
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Escalation> E assert_Escalation(
			PropagateEscalationError propagate, Class<E> escalationType) {

		// Obtain the escalation and ensure correct type
		Throwable failure = propagate.getCause();
		assertNotNull("No escalation for propagate", failure);
		assertEquals("Incorrect escalation type", escalationType,
				failure.getClass());

		// Cast to escalation
		E escalation = (E) failure;

		// Return the escalation
		return escalation;
	}

	/**
	 * Asserts the correct {@link ManagedObjectEscalation} returning the cause
	 * of the {@link ManagedObjectEscalation}.
	 * 
	 * @param propagate
	 *            {@link PropagateEscalationError}.
	 * @param escalationType
	 *            Type of {@link ManagedObjectEscalation} expected.
	 * @param objectType
	 *            {@link Object} type expected on the
	 *            {@link ManagedObjectEscalation}.
	 * @return Cause of the {@link ManagedObjectEscalation}.
	 */
	protected <E extends ManagedObjectEscalation> Throwable assert_ManagedObjectEscalation(
			PropagateEscalationError propagate, Class<E> escalationType,
			Class<?> objectType) {

		// Assert escalation is correct
		E escalation = this.assert_Escalation(propagate, escalationType);

		// Asset the correct object type
		assertEquals("Incorrect object type", objectType,
				escalation.getObjectType());

		// Return the cause of the escalation
		return escalation.getCause();
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
	private static interface MockManagedObject extends NameAwareManagedObject,
			AsynchronousManagedObject, CoordinatingManagedObject<Indexed> {
	}

}