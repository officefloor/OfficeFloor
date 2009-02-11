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

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.frame.impl.execute.error.ExecutionError;
import net.officefloor.frame.impl.execute.error.ExecutionErrorEnum;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;

/**
 * Tests {@link AsynchronousManagedObject} doing asynchronous operations that
 * times out.
 * 
 * @author Daniel
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
					public boolean isFilter(boolean isAsynchronous,
							boolean isCoordinating, boolean isPooled,
							boolean isRecycled) {
						// Only asynchronous managed object
						return !isAsynchronous;
					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {

		final Object object = "object";

		// Record making the managed object available
		this.record_MoContainer_init();
		this.record_MoContainer_sourceManagedObject(true, null);
		this.record_MoUser_setManagedObject(true, object);
		this.record_MoContainer_coordinateManagedObject(null);
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);

		// Record under taking an asynchronous operation
		this.record_AsynchronousListener_notifyStart();

		// Record asynchronous operation timing out
		this
				.record_MoContainer_isManagedObjectReady(ReadyState.ASYNC_OPERATION_TIMED_OUT);

		// Record continue to report asynchronous operation timed out
		this
				.record_MoContainer_isManagedObjectReady(ReadyState.ASYNC_OPERATION_TIMED_OUT);

		// Record unloading managed object
		this.record_MoContainer_unloadManagedObject();

		// Replay mocks
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo, true);
		this.coordinateManagedObject(mo);
		this.isManagedObjectReady(mo, true);
		this.assert_getObject(mo, object);

		// Start an asynchronous operation
		this.asynchronousListener_notifyStarted(mo);

		try {
			// Checking results in propagating timeout failure
			this.isManagedObjectReady(mo, false);
			fail("Should not return on asynchronous timeout");
		} catch (ExecutionError ex) {
			// Ensure correct type
			assertEquals(
					"Incorrect execution error",
					ExecutionErrorEnum.MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT,
					ex.getErrorType());
		}

		try {
			// Checking against should also result in propagating timeout
			this.isManagedObjectReady(mo, false);
			fail("Should not return on asynchronous timeout");
		} catch (ExecutionError ex) {
			// Ensure correct type
			assertEquals(
					"Incorrect execution error",
					ExecutionErrorEnum.MANAGED_OBJECT_ASYNC_OPERATION_TIMED_OUT,
					ex.getErrorType());
		}

		// Ensure can unload the managed object
		mo.unloadManagedObject();

		// Verify mocks
		this.verifyMockObjects();
	}

}
