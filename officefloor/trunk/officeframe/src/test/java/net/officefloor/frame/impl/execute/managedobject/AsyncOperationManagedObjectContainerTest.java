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
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;

/**
 * Tests {@link AsynchronousManagedObject} doing asynchronous operations.
 * 
 * @author Daniel
 */
public class AsyncOperationManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(
				AsyncOperationManagedObjectContainerTest.class,
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

		// Record managed object not ready as in asynchronous operation
		this
				.record_MoContainer_isManagedObjectReady(ReadyState.IN_ASYNC_OPERATION);

		// Record asynchronous operation completed
		this.record_AsynchronousListener_notifyComplete();

		// Record managed object ready
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);

		// Record unloading managed object after use
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

		// Should not be ready as asynchronous operation under way
		this.isManagedObjectReady(mo, false);

		// Complete the asynchronous operation
		this.asynchronousListener_notifyComplete(mo);

		// Should now be ready as asynchronous operation complete
		this.isManagedObjectReady(mo, true);

		// Unload the managed object after use
		mo.unloadManagedObject();

		// Verify mocks
		this.verifyMockObjects();
	}

}
