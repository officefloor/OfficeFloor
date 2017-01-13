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
package net.officefloor.autowire.impl;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
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

}