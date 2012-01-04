/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests connecting the various objects together as dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource_Dependencies_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Type of sourcing for the object.
	 */
	private static enum SourceObjectType {
		RawObject, ManagedObject, InputManagedObject, SupplidManagedObject
	}

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
		case SupplidManagedObject:
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
		case SupplidManagedObject:
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
		case SupplidManagedObject:
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

	/**
	 * Adds the source object.
	 * 
	 * @param clazz
	 *            {@link Class} of the object.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 * @param typeQualifier
	 *            Type qualifier for the object.
	 * @param dependencyName
	 *            Name of dependency. May be <code>null</code> for no
	 *            dependency.
	 * @param dependencyType
	 *            Type of dependency.
	 * @param dependencyQualifier
	 *            Qualifier for the required dependency.
	 * @return Raw Object, {@link AutoWireObject} or {@link OfficeFloorSupplier}
	 *         identifier based on {@link SourceObjectType}.
	 */
	private Object addObject(Class<?> clazz, SourceObjectType sourceType,
			String typeQualifier, String dependencyName,
			Class<?> dependencyType, String dependencyQualifier) {

		final AutoWire autoWire = new AutoWire(typeQualifier, clazz.getName());

		// Load based on source type
		switch (sourceType) {
		case RawObject:
			// Add and return the raw object
			Object rawObject = this.createMock(clazz);
			this.source.addObject(rawObject, autoWire);
			return rawObject;

		case ManagedObject:
			// Add and return the managed object
			AutoWireObject managedObject = this.source.addManagedObject(
					ClassManagedObjectSource.class.getName(), null, autoWire);
			managedObject.addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					clazz.getName());
			return managedObject;

		case InputManagedObject:
			// Add and return the input managed object
			ManagedObjectSourceWirer inputManagedObjectWirer = new ManagedObjectSourceWirer() {
				@Override
				public void wire(ManagedObjectSourceWirerContext context) {
					context.mapFlow("flow", "SECTION", "INPUT");
				}
			};
			AutoWireObject inputManagedObject = this.source.addManagedObject(
					ClassManagedObjectSource.class.getName(),
					inputManagedObjectWirer, autoWire);
			inputManagedObject.addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					clazz.getName());
			return inputManagedObject;

		case SupplidManagedObject:
			// Add and return the supplied managed object
			final MockTypeManagedObjectSource supplied = new MockTypeManagedObjectSource(
					clazz);

			// Add dependency (if required)
			if (dependencyName != null) {
				supplied.addDependency(dependencyName, dependencyType,
						dependencyQualifier);
			}

			int identifier = this.addSupplierAndRecordType(new SupplierInit() {
				@Override
				public void supply(SupplierSourceContext context)
						throws Exception {
					context.addManagedObject(supplied, null, autoWire);
				}
			});
			return Integer.valueOf(identifier);

		default:
			fail("Unknown source type");
			return null;
		}
	}

	/**
	 * Records the type for the object.
	 * 
	 * @param object
	 *            Raw Object, {@link AutoWireObject} or <code>null</code>.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 */
	private void recordObjectType(Object object, SourceObjectType sourceType) {
		switch (sourceType) {
		case RawObject:
			// Record raw object type
			this.recordRawObjectType(object);
			break;

		case ManagedObject:
			// Record managed object type
			AutoWireObject managedObject = (AutoWireObject) object;
			this.recordManagedObjectType(managedObject);
			break;

		case InputManagedObject:
			// Record input managed object type
			AutoWireObject inputManagedObject = (AutoWireObject) object;
			this.registerManagedObjectFlowType(inputManagedObject, "flow");
			this.recordManagedObjectType(inputManagedObject);
			break;

		case SupplidManagedObject:
			// Type already recorded on adding supplier
			break;

		default:
			fail("Unknown source type");
		}
	}

	/**
	 * Records adding the object source.
	 * 
	 * @param object
	 *            Raw Object, {@link AutoWireObject} or <code>null</code>.
	 * @param objectType
	 *            Type of object.
	 * @param typeQualifier
	 *            Qualifier of object.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	private OfficeFloorManagedObjectSource recordObjectSource(Object object,
			Class<?> objectType, String typeQualifier,
			SourceObjectType sourceType) {

		final AutoWire autoWire = new AutoWire(typeQualifier,
				objectType.getName());

		// Record obtaining the managed object source
		OfficeFloorManagedObjectSource mos = null;
		switch (sourceType) {
		case RawObject:
			// Recorded in creating the Raw Object
			break;

		case ManagedObject:
		case InputManagedObject:
			// Record managed object source
			mos = this.recordManagedObjectSource(autoWire,
					ClassManagedObjectSource.class, 0, 0,
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					objectType.getName());
			break;

		case SupplidManagedObject:
			// Record supplied managed object source
			Integer identifier = (Integer) object;
			mos = this.recordSuppliedManagedObjectSource(identifier.intValue(),
					autoWire);
			break;

		default:
			fail("Unknown source type");
		}

		// Return the managed object source
		return mos;
	}

	/**
	 * Records add the object.
	 * 
	 * @param object
	 *            Raw Object, {@link AutoWireObject} or <code>null</code>.
	 * @param objectType
	 *            Type of object.
	 * @param typeQualifier
	 *            Qualifier of object.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 * @param mos
	 *            {@link OfficeFloorManagedObjectSource}.
	 * @param dependencyName
	 *            Name of dependency. May be <code>null</code> for no
	 *            dependency.
	 * @param dependencyType
	 *            Type of dependency.
	 * @param dependencyQualifier
	 *            Qualifier for the required dependency.
	 */
	private OfficeFloorManagedObject recordObject(Object object,
			Class<?> objectType, String typeQualifier,
			SourceObjectType sourceType, OfficeFloorManagedObjectSource mos,
			String dependencyName, Class<?> dependencyType,
			String dependencyQualifier) {

		// Create the listing for the dependency
		Object[] dependencyNameAutoWirePairing;
		if (dependencyName == null) {
			// No dependencies
			dependencyNameAutoWirePairing = new Object[0];
		} else {
			// Provide dependency
			dependencyNameAutoWirePairing = new Object[] { dependencyName,
					new AutoWire(dependencyQualifier, dependencyType.getName()) };
		}

		final AutoWire autoWire = new AutoWire(typeQualifier,
				objectType.getName());

		// Record source object
		OfficeFloorManagedObject mo = null;
		switch (sourceType) {
		case RawObject:
			// Record adding raw object
			mo = this.recordRawObject(object, autoWire);
			assertNull("Raw object can not have dependencies", dependencyName);
			break;

		case ManagedObject:
			// Record adding managed object
			mo = this.recordManagedObject(mos, autoWire);
			AutoWireObject autoWireManagedObject = (AutoWireObject) object;
			this.recordManagedObjectDependencies(autoWireManagedObject,
					dependencyNameAutoWirePairing);
			break;

		case InputManagedObject:
			// Record adding input managed object
			this.recordInputManagedObject(mos, autoWire);
			AutoWireObject autoWireInputManagedObject = (AutoWireObject) object;
			this.recordManagedObjectDependencies(autoWireInputManagedObject,
					dependencyNameAutoWirePairing);
			break;

		case SupplidManagedObject:
			// Record adding supplied managed object (dependencies from type)
			mo = this.recordManagedObject(mos, autoWire);
			break;

		default:
			fail("Unknown source type");
		}

		// Return the managed object
		return mo;
	}

	/**
	 * Records flows.
	 * 
	 * @param mos
	 *            {@link OfficeFloorManagedObjectSource}.
	 * @param sourceType
	 *            {@link SourceObjectType}.
	 */
	private void recordObjectFlow(OfficeFloorManagedObjectSource mos,
			SourceObjectType sourceType) {

		// Record source object
		switch (sourceType) {
		case RawObject:
		case ManagedObject:
			// No flows
			break;

		case InputManagedObject:
			// Record flow for input managed object
			this.recordManagedObjectFlow(mos, "flow", "SECTION", "INPUT");
			break;

		case SupplidManagedObject:
			// No flows
			break;

		default:
			fail("Unknown source type");
		}
	}

}