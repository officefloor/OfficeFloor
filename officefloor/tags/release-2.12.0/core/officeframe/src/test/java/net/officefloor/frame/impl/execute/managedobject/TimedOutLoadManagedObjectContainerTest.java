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
import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests timing out load of the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class TimedOutLoadManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 *
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(TimedOutLoadManagedObjectContainerTest.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {

		// Coordinate uses same check as isReady so using this to test Managed
		// Object loaded.

		// Record loading managed object (that is delayed)
		this.record_MoContainer_init(Connection.class);
		this.record_MoContainer_sourceManagedObject(false, null);

		// Record attempting to check managed object available
		this.record_MoContainer_isManagedObjectReady(ReadyState.NOT_SOURCED);

		// Record check timing out on loading managed object
		this
				.record_MoContainer_isManagedObjectReady(ReadyState.SOURCING_TIMEOUT);

		// Record activating the job nodes permanently
		this.record_MoContainer_unloadManagedObject(false);

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container and attempt to load
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo);

		// Check if loaded but not yet loaded (likely checking as another
		// managed object has woken up the job)
		this.isManagedObjectReady(mo, false);

		try {
			// Check if loaded but is now timed out
			this.isManagedObjectReady(mo, false);
			fail("Check should not return as load timed out");
		} catch (PropagateEscalationError ex) {
			// Ensure correct error type
			this.assert_ManagedObjectEscalation(ex,
					SourceManagedObjectTimedOutEscalation.class,
					Connection.class);
		}

		// Unload the managed object (should only set state as not sourced)
		this.unloadManagedObject(mo);

		// Verify mock objects
		this.verifyMockObjects();
	}
}