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

import java.sql.Connection;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.frame.api.escalate.ManagedObjectOperationTimedOutEscalation;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;

/**
 * Tests {@link AsynchronousManagedObject} doing asynchronous operations that
 * times out.
 * 
 * @author Daniel Sagenschneider
 */
public class AsyncOperationTimedOutManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(
				AsyncOperationTimedOutManagedObjectContainerTest.class,
				new MetaDataScenarioFilter() {
					@Override
					public boolean isFilter(boolean isNameAware,
							boolean isAsynchronous, boolean isCoordinating,
							boolean isPooled, boolean isRecycled) {
						// Only asynchronous managed object
						return !isAsynchronous;
					}
				});
	}

	@Override
	protected void runTest() throws Throwable {

		final Connection object = this.createMock(Connection.class);

		// Record making the managed object available
		this.record_MoContainer_init(Connection.class);
		this.record_MoContainer_sourceManagedObject(true, null);
		this.record_MoUser_setManagedObject(true);
		this.record_MoContainer_governManagedObject();
		this.record_MoContainer_coordinateManagedObject(true, true, null,
				object);
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);

		// Record under taking an asynchronous operation
		this.record_AsynchronousListener_notifyStart();

		// Record asynchronous operation timing out
		this.record_MoContainer_isManagedObjectReady(ReadyState.ASYNC_OPERATION_TIMED_OUT);

		// Record continue to report asynchronous operation timed out
		this.record_MoContainer_isManagedObjectReady(ReadyState.FAILURE);

		// Record unloading managed object
		this.record_MoContainer_unloadManagedObject(true);

		// Replay mocks
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo);
		this.governManagedObject(mo, true);
		this.coordinateManagedObject(mo, true);
		this.isManagedObjectReady(mo, true);
		this.assert_getObject(mo, object);

		// Start an asynchronous operation
		this.asynchronousListener_notifyStarted(mo);

		try {
			// Checking results in propagating timeout failure
			this.isManagedObjectReady(mo, false);
			fail("Should not return on asynchronous timeout");
		} catch (PropagateEscalationError ex) {
			// Ensure correct type
			this.assert_ManagedObjectEscalation(ex,
					ManagedObjectOperationTimedOutEscalation.class,
					Connection.class);
		}

		try {
			// Checking again should also result in propagating timeout
			this.isManagedObjectReady(mo, false);
			fail("Should not return on asynchronous timeout");
		} catch (PropagateEscalationError ex) {
			// Ensure correct type
			this.assert_ManagedObjectEscalation(ex,
					ManagedObjectOperationTimedOutEscalation.class,
					Connection.class);
		}

		// Ensure can unload the managed object
		this.unloadManagedObject(mo);

		// Verify mocks
		this.verifyMockObjects();
	}

}