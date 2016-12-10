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

/**
 * Tests that indicates failure on <code>getObject()</code> on invalid state for
 * the {@link ManagedObjectContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public class InvalidGetObjectManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(InvalidGetObjectManagedObjectContainerTest.class);
	}

	/*
	 * ========================= TestCase =================================
	 */

	@Override
	protected void runTest() throws Throwable {

		final Class<?> objectType = Integer.class;

		// Record loading managed object
		this.record_MoContainer_init(objectType);
		this.record_MoContainer_sourceManagedObject(true, null);
		this.record_MoUser_setManagedObject(true);
		this.record_MoMetaData_getObjectType();

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo);

		// Should be illegal state to getObject as not coordinated
		try {
			this.assert_getObject(mo, null);
			fail("Should be illegal state to getObject as not coordinated");
		} catch (PropagateEscalationError error) {
			FailedToSourceManagedObjectEscalation escalation = (FailedToSourceManagedObjectEscalation) error
					.getCause();
			assertEquals("Incorrect object type", objectType, escalation
					.getObjectType());
			IllegalStateException exception = (IllegalStateException) escalation
					.getCause();
			assertEquals("Incorrect reason",
					"ManagedObject in incorrect state LOADED to obtain Object",
					exception.getMessage());
		}

		// Verify mock objects
		this.verifyMockObjects();
	}

}