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
import net.officefloor.frame.impl.execute.ExecutionError;
import net.officefloor.frame.impl.execute.ExecutionErrorEnum;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests failure to load {@link ManagedObject}.
 * 
 * @author Daniel
 */
public class FailedLoadManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(FailedLoadManagedObjectContainerTest.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {

		final Exception failure = new Exception("Load failure");

		// Record indicating failure
		this.record_MoContainer_init();
		this.record_MoContainer_sourceManagedObject(false, failure);
		this.record_MoUser_setFailure(true, failure);

		// Record failure on checking if ready
		this.record_MoContainer_isManagedObjectReady(ReadyState.FAILURE);

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();

		try {
			// Loading managed object fails
			this.loadManagedObject(mo, true);
			fail("Should fail on loading object");
		} catch (ExecutionError ex) {
			// Indicate correct error
			assertEquals("Incorrect error type",
					ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE, ex
							.getErrorType());
			assertEquals("Incorrect cause", failure, ex.getCause());
		}

		try {
			// Checking if ready fails
			this.isManagedObjectReady(mo, false);
			fail("Should fail on checking object");
		} catch (ExecutionError ex) {
			// Indicate correct error
			assertEquals("Incorrect error type",
					ExecutionErrorEnum.MANAGED_OBJECT_SOURCING_FAILURE, ex
							.getErrorType());
			assertEquals("Incorrect cause", failure, ex.getCause());
		}

		// Unload the object
		mo.unloadManagedObject();

		// Verify mock objects
		this.verifyMockObjects();
	}

}
