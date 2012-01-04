/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.autowire.impl;

import org.junit.Ignore;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO object priority ordering as loaded")
public class AutoWireOfficeFloorSource_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Ensure can load simple case of just the {@link Office}.
	 */
	public void testSimple() throws Exception {

		// Record
		this.recordOffice();

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure the added object is available.
	 */
	public void testObjectAvailable() throws Exception {

		// Add the Object
		this.source.addObject("TEST", new AutoWire(String.class));

		// Test
		this.replayMockObjects();
		assertTrue("Added type should be available",
				this.source.isObjectAvailable(new AutoWire(String.class)));
		assertFalse("Type not added should not be available",
				this.source.isObjectAvailable(new AutoWire(Integer.class)));
		this.verifyMockObjects();
	}

	/**
	 * Ensure the added {@link ManagedObject} is available.
	 */
	public void testManagedObjectAvailable() throws Exception {

		// Add the Managed Object
		this.source.addManagedObject(ClassManagedObjectSource.class.getName(),
				null, new AutoWire(String.class));

		// Test
		this.replayMockObjects();
		assertTrue("Added type should be available",
				this.source.isObjectAvailable(new AutoWire(String.class)));
		assertFalse("Type not added should not be available",
				this.source.isObjectAvailable(new AutoWire(Integer.class)));
		this.verifyMockObjects();
	}

	/**
	 * Ensure {@link AutoWireObject} instances are used in the order they are
	 * added. In other words, the first added take priority over the latter
	 * added.
	 */
	public void testObjectPriorityOrdering() {
		fail("TODO test priority of first object over latter objects - combinations of Raw, ManagedObject, Input, Supplied, etc");
	}

	/**
	 * Ensure can use {@link SuppliedManagedObject}.
	 */
	public void testIntegrationOfSuppliedManagedObject() {
		fail("TODO integration test of supplied managed object - preferably one with a flow to trigger functionality with another depending the input");
	}

}