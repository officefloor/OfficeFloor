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
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Tests {@link ManagedObject} taking time to be loaded.
 * 
 * @author Daniel
 */
public class DelayedLoadManagedObjectContainerTest extends
		AbstractManagedObjectContainerImplTest {

	/**
	 * Creates all combinations of meta-data for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations of
	 *         meta-data.
	 */
	public static Test suite() {
		return createMetaDataCombinationTestSuite(DelayedLoadManagedObjectContainerTest.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#runTest()
	 */
	@Override
	protected void runTest() throws Throwable {

		final Object object = "object";

		// Record loading managed object (that is delayed)
		this.record_MoContainer_init();
		this.record_MoContainer_sourceManagedObject(false, null);

		// Record attempting to check managed object available
		this.record_MoContainer_isManagedObjectReady(ReadyState.NOT_SOURCED);

		// Record loading the managed object
		this.record_MoUser_setManagedObject(false, object);

		// Record completion life cycle as managed object loaded
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);
		this.record_MoContainer_coordinateManagedObject(null);
		this.record_MoContainer_isManagedObjectReady(ReadyState.READY);
		this.record_MoContainer_unloadManagedObject(true);

		// Replay mock objects
		this.replayMockObjects();

		// Create the managed object container and attempt to load
		ManagedObjectContainer mo = this.createManagedObjectContainer();
		this.loadManagedObject(mo, false);

		// Check if loaded but not yet loaded (likely checking as another
		// managed object has woken up the job)
		this.isManagedObjectReady(mo, false);

		// Now load the managed object
		this.managedObjectUser_setManagedObject(mo, object);

		// Complete the life cycle as now loaded
		this.isManagedObjectReady(mo, true);
		this.coordinateManagedObject(mo);
		this.isManagedObjectReady(mo, true);
		this.assert_getObject(mo, object);
		this.unloadManagedObject(mo);

		// Verify mock objects
		this.verifyMockObjects();
	}

}