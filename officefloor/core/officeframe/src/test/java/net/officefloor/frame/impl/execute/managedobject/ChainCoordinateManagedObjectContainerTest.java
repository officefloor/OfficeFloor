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
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;

/**
 * Tests having a {@link CoordinatingManagedObject} depending on another
 * {@link CoordinatingManagedObject}. This means the second is not ready by
 * <code>coordinateManagedObject</code> being called and must be triggered by
 * first {@link CoordinatingManagedObject} being coordinated.
 * 
 * @author Daniel Sagenschneider
 */
public class ChainCoordinateManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(ChainCoordinateManagedObjectContainerTest.class);
	}

	/*
	 * ========================= TestCase =================================
	 */

	@Override
	protected void runTest() throws Throwable {

		final Object object = "Object";

		// Record triggering coordinate from isReady (from dependency check)
		this.record_MoContainer_init(Object.class);
		this.record_MoContainer_sourceManagedObject(true, null);
		this.record_MoUser_setManagedObject(true);
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);
		this.record_MoContainer_governManagedObject();
		this.record_MoContainer_coordinateManagedObject(true, true, null,
				object); // Triggered by isReady
		this.record_MoContainer_unloadManagedObject(true);

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo);
		this.isManagedObjectReady(mo, true);
		this.assert_getObject(mo, object);
		this.unloadManagedObject(mo);

		// Verify mock objects
		this.verifyMockObjects();
	}

}