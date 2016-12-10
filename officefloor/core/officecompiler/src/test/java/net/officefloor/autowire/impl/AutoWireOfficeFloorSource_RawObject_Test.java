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
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;

/**
 * Tests the raw object configuration of the {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource_RawObject_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Ensure can load with a raw object.
	 */
	public void testRawObject() throws Throwable {

		final MockRawObject dependency = new MockRawObject();
		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the raw object dependency
		this.source.addObject(dependency, autoWire);

		// Record
		this.recordRawObjectType(dependency);
		this.recordOffice(autoWire);
		OfficeFloorManagedObject mo = this
				.recordRawObject(dependency, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure not load unused raw object.
	 */
	public void testUnusedRawObject() throws Throwable {

		final MockRawObject dependency = new MockRawObject();
		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the unused raw object
		this.source.addObject(dependency, autoWire);

		// Record (not load raw object)
		this.recordRawObjectType(dependency);
		this.recordOffice(); // no objects used

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can load with a raw dependency that defaults the type.
	 */
	public void testRawObjectDefaultAutoWireFromRawObject() throws Throwable {

		final String object = "default type to String";
		final AutoWire autoWire = new AutoWire(String.class);

		// Add the raw object dependency to default type
		this.source.addObject(object);

		// Record
		this.recordRawObjectType(object);
		this.recordOffice(new AutoWire(String.class));
		OfficeFloorManagedObject mo = this.recordRawObject(object, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can load with a raw object with qualification.
	 */
	public void testRawObjectQualifiedType() throws Throwable {

		final MockRawObject dependency = new MockRawObject();
		final AutoWire autoWire = new AutoWire("QUALIFICATION",
				MockRawType.class.getName());

		// Add the raw object dependency
		this.source.addObject(dependency, autoWire);

		// Record
		this.recordRawObjectType(dependency);
		this.recordOffice(autoWire);
		OfficeFloorManagedObject mo = this
				.recordRawObject(dependency, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire raw object with multiple types.
	 */
	public void testRawObjectWithMultipleTypes() throws Exception {

		final MockRawObject object = new MockRawObject();
		final AutoWire typeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire objectAutoWire = new AutoWire(MockRawObject.class);

		// Add raw object with multiple types
		this.source.addObject(object, typeAutoWire, objectAutoWire);

		// Record
		this.recordRawObjectType(object);
		this.recordOffice(typeAutoWire); // only use one
		OfficeFloorManagedObject mo = this
				.recordRawObject(object, typeAutoWire);
		this.recordOfficeObject(mo, typeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire raw object against multiple types.
	 */
	public void testRawObjectForMultipleTypes() throws Exception {

		final MockRawObject object = new MockRawObject();
		final AutoWire typeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire objectAutoWire = new AutoWire(MockRawObject.class);

		// Add raw object with multiple types
		this.source.addObject(object, typeAutoWire, objectAutoWire);

		// Record
		this.recordRawObjectType(object);
		this.recordOffice(typeAutoWire, objectAutoWire); // both types used
		OfficeFloorManagedObject managedObject = this.recordRawObject(object,
				typeAutoWire);
		this.recordOfficeObject(managedObject, typeAutoWire);
		this.recordOfficeObject(managedObject, objectAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

}