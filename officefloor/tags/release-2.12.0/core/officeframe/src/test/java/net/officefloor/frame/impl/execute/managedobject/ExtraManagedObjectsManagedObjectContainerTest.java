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
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests extra {@link ManagedObject} instances loaded.
 * 
 * @author Daniel Sagenschneider
 */
public class ExtraManagedObjectsManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(ExtraManagedObjectsManagedObjectContainerTest.class);
	}

	@Override
	protected void runTest() throws Throwable {

		final Object object = "object";

		// Record loading managed object
		this.record_MoContainer_init(object.getClass());
		this.record_MoContainer_sourceManagedObject(true, null);
		this.record_MoUser_setManagedObject(true);
		this.record_MoContainer_governManagedObject();
		this.record_MoContainer_coordinateManagedObject(true, true, null,
				object);
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);

		// Record another object that is immediately unloaded
		this.record_MoUser_unloadedImmediately();

		// Unload the managed object (that is being used)
		this.record_MoContainer_unloadManagedObject(true);

		// Record another object immediately unloaded as finished
		this.record_MoUser_unloadedImmediately();

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo);
		this.governManagedObject(mo, true);
		this.coordinateManagedObject(mo, true);
		this.isManagedObjectReady(mo, true);
		this.assert_getObject(mo, object);

		// Attempt to load another managed object
		this.managedObjectUser_setManagedObject(mo, "Another object");

		// Unload the managed object
		this.unloadManagedObject(mo);

		// Attempt to load managed object after complete
		this.managedObjectUser_setManagedObject(mo, "Object after completion");

		// Verify mock objects
		this.verifyMockObjects();
	}

}