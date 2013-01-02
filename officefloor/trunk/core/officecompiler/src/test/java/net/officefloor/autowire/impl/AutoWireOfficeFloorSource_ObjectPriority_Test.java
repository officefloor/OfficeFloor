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

import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Provides priority in selecting the object to use for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource_ObjectPriority_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Creates all combinations for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations.
	 */
	public static Test suite() {

		// Create the test suite
		TestSuite suite = new TestSuite(
				AutoWireOfficeFloorSource_ObjectPriority_Test.class.getName());

		// Iterate over object types for creating the tests
		for (SourceObjectType prioritySourcing : SourceObjectType.values()) {

			// Do not include input managed object as takes precedence
			if (prioritySourcing.equals(SourceObjectType.InputManagedObject)) {
				continue;
			}

			// Iterate over including the Input Managed Object
			for (int i = 0; i < 2; i++) {

				// Obtain whether include Input Managed Object
				boolean isIncludeInputManagedObject = (i == 1);

				// Iterate over qualification
				for (int q = 0; q < 2; q++) {

					// Obtain the qualifier
					String qualifier = (q == 1 ? "Qualified" : null);

					// Obtain name of test
					String testName = "testObjectPriority_"
							+ (qualifier == null ? "" : qualifier)
							+ prioritySourcing.name()
							+ (isIncludeInputManagedObject ? "IncludingInputManagedObject"
									: "");

					// Create and add the test case
					AutoWireOfficeFloorSource_ObjectPriority_Test testCase = new AutoWireOfficeFloorSource_ObjectPriority_Test(
							prioritySourcing, qualifier,
							isIncludeInputManagedObject);
					testCase.setName(testName);
					suite.addTest(testCase);
				}
			}
		}

		// Return the test suite
		return suite;
	}

	@Override
	public void runBare() throws Throwable {
		this.doObjectPriorityTest(this.prioritySourceType, this.qualifier,
				this.isIncludeInputManagedObject);
	}

	/**
	 * Priority {@link SourceObjectType} to test.
	 */
	private final SourceObjectType prioritySourceType;

	/**
	 * Qualifier.
	 */
	private final String qualifier;

	/**
	 * Indicates if include {@link OfficeFloorInputManagedObject}.
	 */
	private final boolean isIncludeInputManagedObject;

	/**
	 * Initiate.
	 * 
	 * @param prioritySourceType
	 *            Priority {@link SourceObjectType} to test.
	 * @param qualifier
	 *            Qualifier. May be <code>null</code>.
	 * @param isIncludeInputManagedObject
	 *            Indicates if include {@link OfficeFloorInputManagedObject}.
	 */
	public AutoWireOfficeFloorSource_ObjectPriority_Test(
			SourceObjectType prioritySourceType, String qualifier,
			boolean isIncludeInputManagedObject) {
		this.prioritySourceType = prioritySourceType;
		this.qualifier = qualifier;
		this.isIncludeInputManagedObject = isIncludeInputManagedObject;
	}

	/**
	 * Undertakes the object priority testing.
	 * 
	 * @param prioritySourcing
	 *            Priority {@link SourceObjectType} to test.
	 * @param qualifier
	 *            Qualifier. May be <code>null</code>.
	 * @param isIncludeInputManagedObject
	 *            Indicates if to include the
	 *            {@link OfficeFloorInputManagedObject}.
	 */
	public void doObjectPriorityTest(SourceObjectType prioritySourcing,
			String qualifier, boolean isIncludeInputManagedObject)
			throws Exception {

		final AutoWire autoWire = new AutoWire(qualifier,
				MockRawType.class.getName());
		final Class<?> objectType = MockRawType.class;

		// Add the raw object priority
		Object object = this.addObject(objectType, prioritySourcing, qualifier,
				null, null, null);

		// Add the other types (not including input as takes precedence)
		List<SourceObjectType> otherSourceTypes = new LinkedList<SourceObjectType>();
		List<Object> otherObjects = new LinkedList<Object>();
		int index = 0;
		int inputManagedObjectIndex = -1;
		AutoWireObject inputAutoWireObject = null;
		for (SourceObjectType otherSourceType : SourceObjectType.values()) {

			// Determine if Input Managed Object
			if (SourceObjectType.InputManagedObject.equals(otherSourceType)) {
				inputManagedObjectIndex = index + 1; // +1 for object above

				// Determine if skip Input Managed Object
				if (!isIncludeInputManagedObject) {
					continue; // skip as not including Input Managed Object
				}
			}

			// Add the object
			Object otherObject = this.addObject(objectType, otherSourceType,
					qualifier, null, null, null);

			// Register for recording type
			otherSourceTypes.add(otherSourceType);
			otherObjects.add(otherObject);

			// Capture the input object
			if (SourceObjectType.InputManagedObject.equals(otherSourceType)) {
				inputAutoWireObject = (AutoWireObject) otherObject;
			}

			// Increment for next iteration
			index++;
		}

		// Record
		this.recordObjectType(object, prioritySourcing);
		for (int i = 0; i < otherSourceTypes.size(); i++) {
			this.recordObjectType(otherObjects.get(i), otherSourceTypes.get(i));
		}
		this.registerOfficeInput("SECTION", "INPUT");
		this.recordOffice(autoWire);

		// Input Managed Object overrides Managed Object
		if (isIncludeInputManagedObject) {
			// Record the Input Managed Object (as overrides)
			OfficeFloorManagedObjectSource mos = this
					.recordManagedObjectSource(autoWire,
							ClassManagedObjectSource.class,
							inputManagedObjectIndex, 0,
							ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
							MockRawType.class.getName());
			this.recordInputManagedObject(mos, autoWire);
			this.recordManagedObjectDependencies(inputAutoWireObject);
			this.recordManagedObjectFlow(mos, "flow", "SECTION", "INPUT");

		} else {
			// Record the used Managed Object
			OfficeFloorManagedObjectSource mos = this.recordObjectSource(
					object, objectType, qualifier, prioritySourcing);
			OfficeFloorManagedObject mo = this.recordObject(object, objectType,
					qualifier, prioritySourcing, mos, null, null, null);
			if (mo != null) {
				this.recordOfficeObject(mo, autoWire);
			}
		}

		// Test
		this.doSourceOfficeFloorTest();
	}

}