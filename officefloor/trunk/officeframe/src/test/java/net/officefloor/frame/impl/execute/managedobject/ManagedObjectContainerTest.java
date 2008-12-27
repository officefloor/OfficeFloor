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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.impl.execute.ManagedObjectContainerImpl;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.internal.structure.JobActivateSet;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.internal.AlwaysMatcher;

/**
 * Tests the {@link ManagedObjectContainer}.
 * 
 * @author Daniel
 */
public class ManagedObjectContainerTest extends OfficeFrameTestCase {

	/**
	 * {@link ManagedObjectContainer} being tested.
	 */
	private ManagedObjectContainerImpl moContainer;

	/**
	 * Mock {@link ManagedObjectMetaData}.
	 */
	private ManagedObjectMetaData<?> moMetaData = this
			.createMock(ManagedObjectMetaData.class);

	/**
	 * Lock.
	 */
	private Object lock = new Object();

	/**
	 * Mock {@link JobContext}.
	 */
	private JobContext executionContext = this.createMock(JobContext.class);

	/**
	 * Mock {@link JobNode}.
	 */
	private JobNode jobNode = this.createMock(JobNode.class);

	/**
	 * Mock {@link JobActivateSet}.
	 */
	private JobActivateSet assetNotifySet = this
			.createMock(JobActivateSet.class);

	/**
	 * Mock {@link WorkContainer}.
	 */
	private WorkContainer<?> workContainer = this
			.createMock(WorkContainer.class);

	/**
	 * Mock {@link ThreadState}.
	 */
	private ThreadState threadState = this.createMock(ThreadState.class);

	/**
	 * Mock {@link ManagedObject}.
	 */
	private MockManagedObject managedObject = this
			.createMock(MockManagedObject.class);

	/**
	 * {@link ManagedObject} object.
	 */
	private Object moObject = new Object();

	/**
	 * Mock sourcing {@link AssetManager}.
	 */
	private AssetManager sourcingManager = this.createMock(AssetManager.class);

	/**
	 * Mock sourcing {@link AssetMonitor}.
	 */
	private AssetMonitor sourcingMonitor = this.createMock(AssetMonitor.class);

	/**
	 * Mock operations {@link AssetManager}.
	 */
	private AssetManager operationsManager = this
			.createMock(AssetManager.class);

	/**
	 * Mock operations {@link AssetMonitor}.
	 */
	private AssetMonitor operationsMonitor = this
			.createMock(AssetMonitor.class);

	/**
	 * Mock {@link ManagedObjectSource}.
	 */
	private ManagedObjectSource<?, ?> moSource = this
			.createMock(ManagedObjectSource.class);

	/**
	 * Mock recycle {@link JobNode}.
	 */
	private JobNode recycleJobNode = this.createMock(JobNode.class);

	/**
	 * Mock {@link ObjectRegistry}.
	 */
	@SuppressWarnings("unchecked")
	private ObjectRegistry<Indexed> objectRegistry = this
			.createMock(ObjectRegistry.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
	}

	/**
	 * Ensures does the happy day scenario.
	 */
	public void testHappyDayLifeCycle() throws Exception {

		final boolean isAsynchronous = false;

		// Record creation
		this.recordManagedObjectContainerCreation(isAsynchronous);

		// Record loading
		this.recordReturn(this.executionContext, this.executionContext
				.getTime(), System.currentTimeMillis());
		this.recordReturn(this.moMetaData, this.moMetaData
				.getManagedObjectPool(), null);
		this.recordReturn(this.moMetaData, this.moMetaData
				.getManagedObjectSource(), this.moSource);
		this.moSource.sourceManagedObject(this.moContainer);
		this.control(this.moSource).setMatcher(new AlwaysMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Source the managed object
				ManagedObjectUser user = (ManagedObjectUser) actual[0];
				user
						.setManagedObject(ManagedObjectContainerTest.this.managedObject);
				return true; // Always match
			}
		});
		this.recordReturn(this.moMetaData, this.moMetaData
				.createRecycleJobNode(this.managedObject), this.recycleJobNode);
		this.recordReturn(this.managedObject, this.managedObject.getObject(),
				this.moObject);
		this.recordReturn(this.moMetaData, this.moMetaData
				.isManagedObjectAsynchronous(), isAsynchronous);
		this.sourcingMonitor.notifyPermanently(this.assetNotifySet);

		// Record ready
		this.recordReturn(this.moMetaData, this.moMetaData
				.isManagedObjectAsynchronous(), isAsynchronous);

		// Record co-ordinate
		this.recordReturn(this.moMetaData, this.moMetaData
				.isCoordinatingManagedObject(), true);
		this.recordReturn(this.jobNode, this.jobNode.getThreadState(),
				this.threadState);
		this.recordReturn(this.moMetaData, this.moMetaData
				.createObjectRegistry(this.workContainer, this.threadState),
				this.objectRegistry);
		this.managedObject.loadObjects(this.objectRegistry);

		// Record ready
		this.recordReturn(this.moMetaData, this.moMetaData
				.isManagedObjectAsynchronous(), isAsynchronous);

		// No recording for get managed object and get object

		// Record unload
		// TODO consider adding to notify set rather than activating directly
		this.recycleJobNode.activateJob();

		// Replay
		this.replayMockObjects();

		// Load, ready, co-ordinate, ready, get, unload
		this.createManagedObjectContainer();
		this.moContainer.loadManagedObject(this.executionContext,
				this.jobNode, this.assetNotifySet);
		this.moContainer.isManagedObjectReady(this.executionContext,
				this.jobNode, this.assetNotifySet);
		this.moContainer.coordinateManagedObject(this.workContainer,
				this.executionContext, this.jobNode, this.assetNotifySet);
		this.moContainer.isManagedObjectReady(this.executionContext,
				this.jobNode, this.assetNotifySet);
		assertEquals("Incorrect managed object", this.managedObject,
				this.moContainer.getManagedObject(this.threadState));
		assertEquals("Incorrect object", this.moObject, this.moContainer
				.getObject(this.threadState));
		this.moContainer.unloadManagedObject();

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Records the creation of the {@link ManagedObjectContainer}.
	 * 
	 * @param isAsynchronous
	 *            Flag indicating if the {@link ManagedObject} is asynchronous.
	 */
	private void recordManagedObjectContainerCreation(boolean isAsynchronous) {

		// Obtain the sourcing manager
		this.recordReturn(this.moMetaData,
				this.moMetaData.getSourcingManager(), this.sourcingManager);
		this.sourcingManager.createAssetMonitor(this.moContainer, this.lock);
		this.control(this.sourcingManager).setMatcher(new AlwaysMatcher());
		this.control(this.sourcingManager).setReturnValue(this.sourcingMonitor);

		// Handle if asynchronous
		this.recordReturn(this.moMetaData, this.moMetaData
				.isManagedObjectAsynchronous(), isAsynchronous);
		if (isAsynchronous) {
			// Obtain the operations manager
			this.recordReturn(this.moMetaData, this.moMetaData
					.getOperationsManager(), this.operationsManager);
			this.operationsManager.createAssetMonitor(this.moContainer,
					this.lock);
			this.control(this.operationsManager)
					.setMatcher(new AlwaysMatcher());
			this.control(this.operationsManager).setReturnValue(
					this.operationsMonitor);
		}
	}

	/**
	 * Creates the {@link ManagedObjectContainer}.
	 */
	private void createManagedObjectContainer() {
		// Creates the managed object container
		this.moContainer = new ManagedObjectContainerImpl(this.moMetaData,
				this.lock);
	}

	/**
	 * Interface for the necessary {@link ManagedObject} mock.
	 */
	private interface MockManagedObject extends ManagedObject,
			AsynchronousManagedObject, CoordinatingManagedObject<Indexed> {
	}
}
