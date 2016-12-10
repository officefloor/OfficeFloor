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
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests the {@link Governance} already being established for a
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceEstablishedManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(GovernanceEstablishedManagedObjectContainerTest.class);
	}

	@Override
	protected void runTest() throws Throwable {

		final Object object = "object";

		// Record loading managed object
		this.record_MoContainer_init(object.getClass(), 3);
		this.record_MoContainer_sourceManagedObject(true, null);
		this.record_MoUser_setManagedObject(true);

		// Record establishing governance
		this.record_MoContainer_governManagedObject(true, false, true);

		// Record further governance (and clearing inactive)
		this.record_MoContainer_governManagedObject(true, true, false);

		// Record remaining managed object functionality
		this.record_MoContainer_coordinateManagedObject(true, true, null,
				object);
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);

		// Not unload managed object as governance active

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo);

		// Initially establish governance
		this.governManagedObject(mo, true);

		// Further governance
		this.governManagedObject(mo, true);

		// Remaining functionality
		this.coordinateManagedObject(mo, true);
		this.isManagedObjectReady(mo, true);
		this.assert_getObject(mo, object);
		this.unloadManagedObject(mo);

		// Verify mock objects
		this.verifyMockObjects();
	}

}