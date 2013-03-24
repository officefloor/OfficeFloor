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
import net.officefloor.frame.internal.structure.ManagedObjectContainer;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Tests duplicate calls to the {@link ManagedObjectContainer} when re-used
 * across {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public class DuplicateCallsManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(DuplicateCallsManagedObjectContainerTest.class);
	}

	@Override
	protected void runTest() throws Throwable {

		final Object object = "object";

		// Record loading managed object (each should only be run once)
		this.record_MoContainer_init(object.getClass());
		this.record_MoContainer_sourceManagedObject(true, null);
		this.record_MoUser_setManagedObject(true);

		// Governance is exception as may change between jobs
		this.record_MoContainer_governManagedObject();
		this.record_MoContainer_governManagedObject();

		// Record remaining loading (again should only be once)
		this.record_MoContainer_coordinateManagedObject(true, true, null,
				object);

		// Record further load attempt
		this.record_MoContainer_governManagedObject();

		// Recording remaining
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);
		this.record_MoContainer_unloadManagedObject(true);

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();

		// Attempt to load twice (with only first taking effect)
		this.loadManagedObject(mo);
		this.loadManagedObject(mo);

		// Must determine governance twice as may change between jobs
		this.governManagedObject(mo, true);
		this.governManagedObject(mo, true);

		// Attempt to coordinate twice (with only first taking effect)
		this.coordinateManagedObject(mo, true);
		this.coordinateManagedObject(mo, true);

		// Attempt another load, that should only check governance
		this.loadManagedObject(mo);
		this.governManagedObject(mo, true);
		this.coordinateManagedObject(mo, true);

		// Should be ready and working
		this.isManagedObjectReady(mo, true);
		this.assert_getObject(mo, object);
		this.unloadManagedObject(mo);

		// Verify mock objects
		this.verifyMockObjects();
	}

}