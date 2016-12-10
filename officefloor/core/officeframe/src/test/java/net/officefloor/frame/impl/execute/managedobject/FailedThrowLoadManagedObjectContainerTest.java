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
import net.officefloor.frame.api.escalate.FailedToSourceManagedObjectEscalation;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests failure as {@link Throwable} thrown on sourcing {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class FailedThrowLoadManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 *
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(FailedThrowLoadManagedObjectContainerTest.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {

		final RuntimeException failure = new RuntimeException("Thrown failure");

		// Record indicating failure
		this.record_MoContainer_init(Connection.class);
		this.record_MoContainer_sourceManagedObject(false, failure);
		this.record_MoUser_setFailure(true, failure);

		// Record failure on checking if ready
		this.record_MoContainer_isManagedObjectReady(ReadyState.FAILURE);

		// Record activating the job nodes permanently
		this.record_MoContainer_unloadManagedObject(false);

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();

		try {
			// Loading managed object fails
			this.loadManagedObject(mo);
			fail("Should fail on loading object");
		} catch (PropagateEscalationError ex) {
			// Ensure correct error
			Throwable cause = this.assert_ManagedObjectEscalation(ex,
					FailedToSourceManagedObjectEscalation.class,
					Connection.class);
			assertEquals("Incorrect cause", failure, cause);
		}

		try {
			// Checking if ready fails
			this.isManagedObjectReady(mo, false);
			fail("Should fail on checking object");
		} catch (PropagateEscalationError ex) {
			// Ensure correct error
			Throwable cause = this.assert_ManagedObjectEscalation(ex,
					FailedToSourceManagedObjectEscalation.class,
					Connection.class);
			assertEquals("Incorrect cause", failure, cause);
		}

		// Unload the object
		this.unloadManagedObject(mo);

		// Verify mock objects
		this.verifyMockObjects();
	}

}