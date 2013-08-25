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

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests failure {@link ManagedObject} taken time to load.
 *
 * @author Daniel Sagenschneider
 */
public class FailedDelayedLoadManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 *
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(FailedDelayedLoadManagedObjectContainerTest.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {

		final Throwable failure = new Throwable("sourcing failure");

		// Record loading managed object (that is delayed)
		this.record_MoContainer_init(Object.class);
		this.record_MoContainer_sourceManagedObject(false, null);

		// Record later failure in sourcing managed object
		this.record_MoUser_setFailure(false, failure);

		// Record propagating failure in sourcing managed object
		this.record_MoContainer_isManagedObjectReady(ReadyState.FAILURE);

		// Record flagging permanently activate jobs
		this.record_MoContainer_unloadManagedObject(false);

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container and attempt to load
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo);

		// Specify failure in attempting to load
		this.managedObjectUser_setFailure(mo, failure);

		try {
			// Check ready should report failure to load
			this.isManagedObjectReady(mo, false);
			fail("Should propagate failure to source");
		} catch (PropagateEscalationError ex) {
			// Ensure exception details correct
			Throwable cause = this.assert_ManagedObjectEscalation(ex,
					FailedToSourceManagedObjectEscalation.class, Object.class);
			assertEquals("Incorrect sourcing cause", failure, cause);
		}

		// Unload the managed object (should only set state as not sourced)
		this.unloadManagedObject(mo);

		// Verify mock objects
		this.verifyMockObjects();
	}

}