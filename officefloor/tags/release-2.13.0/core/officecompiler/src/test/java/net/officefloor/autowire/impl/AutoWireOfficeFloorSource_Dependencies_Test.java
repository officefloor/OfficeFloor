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

import java.sql.Connection;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.autowire.AutoWire;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;

/**
 * Tests connecting the various objects together as dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource_Dependencies_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Creates all combinations of for testing.
	 * 
	 * @return {@link TestSuite} containing tests for all combinations.
	 */
	public static Test suite() {

		// Create the test suite
		TestSuite suite = new TestSuite(
				AutoWireOfficeFloorSource_Dependencies_Test.class.getName());

		// Iterate over object types for creating the tests
		for (SourceObjectType objectSourcing : SourceObjectType.values()) {

			// Raw objects can not have dependencies
			if (SourceObjectType.RawObject.equals(objectSourcing)) {
				continue;
			}

			// Iterate over dependency types for creating the tests
			for (SourceObjectType dependencySourcing : SourceObjectType
					.values()) {

				// Iterate over dependency qualification
				for (int d = 0; d < 2; d++) {

					// Obtain the dependency qualifier
					String dependencyQualifier = (d == 1 ? "Qualified" : null);

					// Iterate over type qualification
					for (int t = 0; t < 2; t++) {

						// Obtain the type qualifier
						String typeQualifier = (t == 1 ? "Qualified" : null);

						// Default to Qualified will not match
						if ((dependencyQualifier == null)
								&& (typeQualifier != null)) {
							continue;
						}

						// Obtain name of test
						String testName = "testDependency_"
								+ (dependencyQualifier == null ? ""
										: dependencyQualifier)
								+ objectSourcing.name() + "Dependency_to_"
								+ (typeQualifier == null ? "" : typeQualifier)
								+ dependencySourcing.name();

						// Create and add the test case
						AutoWireOfficeFloorSource_Dependencies_Test testCase = new AutoWireOfficeFloorSource_Dependencies_Test(
								objectSourcing, dependencyQualifier,
								dependencySourcing, typeQualifier);
						testCase.setName(testName);
						suite.addTest(testCase);
					}
				}
			}
		}

		// Return the test suite
		return suite;
	}

	/**
	 * {@link SourceObjectType} for object with dependency.
	 */
	private final SourceObjectType objectSourcing;

	/**
	 * Qualifier for the required dependency.
	 */
	private final String dependencyQualifier;

	/**
	 * {@link SourceObjectType} for the dependency.
	 */
	private final SourceObjectType dependencySourcing;

	/**
	 * Qualifier for the dependency.
	 */
	private final String typeQualifier;

	/**
	 * Initiate.
	 * 
	 * @param objectSourcing
	 *            {@link SourceObjectType} for object with dependency.
	 * @param dependencyQualifier
	 *            Qualifier for the required dependency.
	 * @param dependencySourcing
	 *            {@link SourceObjectType} for the dependency.
	 * @param typeQualifier
	 *            Qualifier for the dependency.
	 */
	public AutoWireOfficeFloorSource_Dependencies_Test(
			SourceObjectType objectSourcing, String dependencyQualifier,
			SourceObjectType dependencySourcing, String typeQualifier) {
		this.objectSourcing = objectSourcing;
		this.dependencyQualifier = dependencyQualifier;
		this.dependencySourcing = dependencySourcing;
		this.typeQualifier = typeQualifier;
	}

	@Override
	public void runBare() throws Throwable {
		this.doWireDependencyTest(this.objectSourcing,
				this.dependencyQualifier, this.dependencySourcing,
				this.typeQualifier);
	}

	/**
	 * Undertakes the wiring dependency testing.
	 */
	private void doWireDependencyTest(SourceObjectType usedType,
			String dependencyQualifier, SourceObjectType dependencyType,
			String typeQualifier) throws Exception {

		final Class<?> usedClass = MockRawType.class;
		final Class<?> dependencyClass = Connection.class;

		// Add the objects
		Object usedObject = this.addObject(usedClass, usedType, null,
				"dependency", dependencyClass, dependencyQualifier);
		Object dependencyObject = this.addObject(dependencyClass,
				dependencyType, typeQualifier, null, null, null);

		// ------------------------------
		// Build Managed Object Types
		// ------------------------------

		// Record
		this.recordObjectType(usedObject, usedType);
		this.recordObjectType(dependencyObject, dependencyType);
		this.registerOfficeInput("SECTION", "INPUT");
		this.recordOffice(new AutoWire(usedClass)); // dependency should load

		// ------------------------------
		// Build Input Managed Objects
		// ------------------------------

		OfficeFloorManagedObjectSource usedMos;
		OfficeFloorManagedObject usedMo;
		OfficeFloorManagedObjectSource dependencyMos;

		// Determine if non-input Object depending on input Object
		boolean isNonInputObjectDependingOnInputObject = (!(usedType
				.equals(SourceObjectType.InputManagedObject)) && (dependencyType
				.equals(SourceObjectType.InputManagedObject)));

		// Input Managed Objects always loaded first
		if (isNonInputObjectDependingOnInputObject) {
			// Load the dependency input first (as inputs loaded before objects)
			dependencyMos = this.recordObjectSource(dependencyObject,
					dependencyClass, typeQualifier, dependencyType);
			this.recordObject(dependencyObject, dependencyClass, typeQualifier,
					dependencyType, dependencyMos, null, null, null);

			// Link input dependency before used loading managed object
			this.recordObjectFlow(dependencyMos, dependencyType);

			// Linking, triggers loading used (non-input) Object
			usedMos = this.recordObjectSource(usedObject, usedClass, null,
					usedType);
			usedMo = this
					.recordObject(usedObject, usedClass, null, usedType,
							usedMos, "dependency", dependencyClass,
							dependencyQualifier);

		} else {
			// Load in used then dependency
			usedMos = this.recordObjectSource(usedObject, usedClass, null,
					usedType);
			usedMo = this
					.recordObject(usedObject, usedClass, null, usedType,
							usedMos, "dependency", dependencyClass,
							dependencyQualifier);
			dependencyMos = this.recordObjectSource(dependencyObject,
					dependencyClass, typeQualifier, dependencyType);
			this.recordObject(dependencyObject, dependencyClass, typeQualifier,
					dependencyType, dependencyMos, null, null, null);
		}

		// ------------------------------
		// Link Input Managed Objects
		// ------------------------------

		// Handle input linking for used
		switch (usedType) {
		case InputManagedObject:
			// Link the input dependency
			this.recordLinkInputManagedObjectDependency(usedMos, "dependency",
					new AutoWire(typeQualifier, dependencyClass.getName()));
			this.recordObjectFlow(usedMos, usedType);
			break;

		case RawObject:
		case ManagedObject:
		case SuppliedManagedObject:
			// No input linking
			break;

		default:
			fail("Unknown source type");
		}

		// Handle input linking for dependency
		switch (dependencyType) {
		case InputManagedObject:
			// Will already be linked (as dependency loaded before used)
			if (isNonInputObjectDependingOnInputObject) {
				break;
			}

			// Link the flow
			this.recordObjectFlow(dependencyMos, dependencyType);
			break;

		case RawObject:
		case ManagedObject:
		case SuppliedManagedObject:
			// No flow/team linking required
			break;

		default:
			fail("Unknown source type");
		}

		// ------------------------------
		// Build and Link Managed Objects
		// ------------------------------

		// Handle linking used to dependency
		switch (usedType) {
		case InputManagedObject:
			// Already linked above
			break;

		case RawObject:
		case ManagedObject:
		case SuppliedManagedObject:
			// Link the dependency
			this.recordLinkManagedObjectDependency(new AutoWire(usedClass),
					"dependency",
					new AutoWire(typeQualifier, dependencyClass.getName()));
			this.recordOfficeObject(usedMo, new AutoWire(usedClass));
			break;

		default:
			fail("Unknown source type");
		}

		// Test
		this.doSourceOfficeFloorTest();
	}

}