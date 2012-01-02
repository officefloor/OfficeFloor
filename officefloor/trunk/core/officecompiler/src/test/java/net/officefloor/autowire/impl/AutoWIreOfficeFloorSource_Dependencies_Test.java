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

import org.junit.Ignore;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests connecting the various objects together as dependencies.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO rather than manually specify each dependency type - dynamically create tests")
public class AutoWIreOfficeFloorSource_Dependencies_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	// TODO dynamic create the tests for the various dependency scenarios
	public void test_TODO_dynamicallyCreateTestCombinations() {
		fail("TODO dynamic create the tests for the various dependency scenarios");
	}

	/**
	 * Ensure can wire {@link ManagedObjectDependency} to
	 * {@link SuppliedManagedObjectType}.
	 */
	public void testWire_ManagedObjectDependency_To_SuppliedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, null,
				SourceObjectType.SUPPLIED, null);
	}

	/**
	 * Ensure can wire qualified {@link ManagedObjectDependency} to qualified
	 * {@link SuppliedManagedObjectType}.
	 */
	public void testWire_QualifiedManagedObjectDependency_To_QualifiedSuppliedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, "QUALIFIED",
				SourceObjectType.SUPPLIED, "QUALIFIED");
	}

	/**
	 * Ensure can wire qualified {@link ManagedObjectDependency} to default
	 * {@link SuppliedManagedObjectType}.
	 */
	public void testWire_QualifiedManagedObjectDependency_To_SuppliedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, "QUALIFIED",
				SourceObjectType.SUPPLIED, null);
	}

	/**
	 * Ensure can wire {@link ManagedObjectDependency} to {@link ManagedObject}.
	 */
	public void testWire_ManagedObjectDependency_To_ManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, null,
				SourceObjectType.MANAGED_OBJECT, null);
	}

	/**
	 * Ensure can wire qualified {@link ManagedObjectDependency} to qualified
	 * {@link ManagedObject}.
	 */
	public void testWire_QualifiedManagedObjectDependency_To_QualifiedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, "QUALIFIED",
				SourceObjectType.MANAGED_OBJECT, "QUALIFIED");
	}

	/**
	 * Ensure can wire qualified {@link ManagedObjectDependency} to default
	 * {@link ManagedObject}.
	 */
	public void testWire_QualifiedManagedObjectDependency_To_ManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, "QUALIFIED",
				SourceObjectType.MANAGED_OBJECT, null);
	}

	/**
	 * Ensure can wire {@link ManagedObjectDependency} to raw object.
	 */
	public void testWire_ManagedObjectDependency_To_RawObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, null,
				SourceObjectType.RAW, null);
	}

	/**
	 * Ensure can wire qualified {@link ManagedObjectDependency} to qualified
	 * raw object.
	 */
	public void testWire_QualifiedManagedObjectDependency_To_QualifiedRawObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, "QUALIFIED",
				SourceObjectType.RAW, "QUALIFIED");
	}

	/**
	 * Ensure can wire qualified {@link ManagedObjectDependency} to default raw
	 * object.
	 */
	public void testWire_QualifiedManagedObjectDependency_To_RawObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.MANAGED_OBJECT, "QUALIFIED",
				SourceObjectType.RAW, null);
	}

	/**
	 * Ensure can wire {@link SuppliedManagedObjectDependencyType} to
	 * {@link SuppliedManagedObjectType}.
	 */
	public void testWire_SuppliedManagedObjectDependency_To_SuppliedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, null,
				SourceObjectType.SUPPLIED, null);
	}

	/**
	 * Ensure can wire qualified {@link SuppliedManagedObjectDependencyType} to
	 * qualified {@link SuppliedManagedObjectType}.
	 */
	public void testWire_QualifiedSuppliedManagedObjectDependency_To_QualifiedSuppliedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, "QUALIFIED",
				SourceObjectType.SUPPLIED, "QUALIFIED");
	}

	/**
	 * Ensure can wire qualified {@link SuppliedManagedObjectDependencyType} to
	 * {@link SuppliedManagedObjectType}.
	 */
	public void testWire_QualifiedSuppliedManagedObjectDependency_To_SuppliedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, "QUALIFIED",
				SourceObjectType.SUPPLIED, null);
	}

	/**
	 * Ensure can wire {@link SuppliedManagedObjectDependencyType} to
	 * {@link ManagedObject}.
	 */
	public void testWire_SuppliedManagedObjectDependency_To_ManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, null,
				SourceObjectType.MANAGED_OBJECT, null);
	}

	/**
	 * Ensure can wire qualified {@link SuppliedManagedObjectDependencyType} to
	 * qualified {@link ManagedObject}.
	 */
	public void testWire_QualifiedSuppliedManagedObjectDependency_To_QualifiedManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, "QUALIFIED",
				SourceObjectType.MANAGED_OBJECT, "QUALIFIED");
	}

	/**
	 * Ensure can wire qualified {@link SuppliedManagedObjectDependencyType} to
	 * {@link ManagedObject}.
	 */
	public void testWire_QualifiedSuppliedManagedObjectDependency_To_ManagedObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, "QUALIFIED",
				SourceObjectType.MANAGED_OBJECT, null);
	}

	/**
	 * Ensure can wire {@link SuppliedManagedObjectDependencyType} to raw
	 * object.
	 */
	public void testWire_SuppliedManagedObjectDependency_To_RawObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, null,
				SourceObjectType.RAW, null);
	}

	/**
	 * Ensure can wire qualified {@link SuppliedManagedObjectDependencyType} to
	 * qualified raw object.
	 */
	public void testWire_QualifiedSuppliedManagedObjectDependency_To_QualifiedRawObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, "QUALIFIED",
				SourceObjectType.RAW, "QUALIFIED");
	}

	/**
	 * Ensure can wire qualified {@link SuppliedManagedObjectDependencyType} to
	 * raw object.
	 */
	public void testWire_QualifiedSuppliedManagedObjectDependency_To_RawObject()
			throws Exception {
		this.doWireDependencyTest(SourceObjectType.SUPPLIED, "QUALIFIED",
				SourceObjectType.RAW, null);
	}

	/**
	 * Type of sourcing for the object.
	 */
	private static enum SourceObjectType {
		RAW, MANAGED_OBJECT, SUPPLIED
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
		Object usedObject = this.addSourceObject(usedClass, usedType, null);
		Object dependencyObject = this.addSourceObject(dependencyClass,
				dependencyType, typeQualifier);

		// Record
		this.recordTeam();
		this.recordSourceObjectType(usedObject, usedType);
		this.recordSourceObjectType(dependencyObject, dependencyType);
		this.recordOffice(new AutoWire(usedClass)); // dependency should load
		OfficeFloorManagedObject mo = this.recordSourceObject(usedObject,
				usedClass, null, usedType, "dependency", dependencyClass,
				dependencyQualifier);
		this.recordSourceObject(dependencyObject, dependencyClass,
				typeQualifier, dependencyType, null, null, null);
		this.recordLinkManagedObjectDependency(new AutoWire(usedClass),
				"dependency",
				new AutoWire(typeQualifier, dependencyClass.getName()));
		this.recordOfficeObject(mo, new AutoWire(usedClass));

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
	 * @param qualifier
	 *            Type qualifier for the object.
	 * @return Raw Object, {@link AutoWireObject} or {@link ManagedObjectSource}
	 *         based on {@link SourceObjectType}.
	 */
	private Object addSourceObject(Class<?> clazz, SourceObjectType sourceType,
			String qualifier) {

		final AutoWire autoWire = new AutoWire(qualifier, clazz.getName());

		// Load based on source type
		switch (sourceType) {
		case RAW:
			// Add and return the raw object
			Object rawObject = this.createMock(clazz);
			this.source.addObject(rawObject, autoWire);
			return rawObject;

		case MANAGED_OBJECT:
			// Add and return the managed object
			AutoWireObject object = this.source.addManagedObject(
					ClassManagedObjectSource.class.getName(), null, autoWire);
			object.addProperty(
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					clazz.getName());
			return object;

		case SUPPLIED:
			// Add and return the supplied managed object
			final MockTypeManagedObjectSource supplied = new MockTypeManagedObjectSource(
					clazz);
			this.addSupplier(new SupplierInit() {
				@Override
				public void supply(SupplierSourceContext context)
						throws Exception {
					context.addManagedObject(supplied, null, autoWire);
				}
			});
			return supplied;

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
	private void recordSourceObjectType(Object object,
			SourceObjectType sourceType) {
		switch (sourceType) {
		case RAW:
			// Record raw object type
			this.recordRawObjectType(object);
			break;

		case MANAGED_OBJECT:
			// Record managed object type
			AutoWireObject autoWireObject = (AutoWireObject) object;
			this.recordManagedObjectType(autoWireObject);
			break;

		case SUPPLIED:
			// Type loaded without mocks
			break;

		default:
			fail("Unknown source type");
		}
	}

	/**
	 * Records the source object.
	 */
	private OfficeFloorManagedObject recordSourceObject(Object object,
			Class<?> objectType, String typeQualifier,
			SourceObjectType sourceType, String dependencyName,
			Class<?> dependencyType, String dependencyQualifier) {

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
		OfficeFloorManagedObjectSource mos;

		// Record source object
		OfficeFloorManagedObject mo = null;
		switch (sourceType) {
		case RAW:
			// Record adding raw object
			mo = this.recordRawObject(object, autoWire);
			assertNull("Raw object can not have dependencies", dependencyName);
			break;

		case MANAGED_OBJECT:
			// Record adding managed object
			mos = this.recordManagedObjectSource(autoWire,
					ClassManagedObjectSource.class, 0, 0,
					ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
					objectType.getName());
			mo = this.recordManagedObject(mos, autoWire);
			AutoWireObject autoWireObject = (AutoWireObject) object;
			this.recordManagedObjectDependencies(autoWireObject,
					dependencyNameAutoWirePairing);
			break;

		case SUPPLIED:
			// Record adding supplied managed object
			MockTypeManagedObjectSource mosSource = (MockTypeManagedObjectSource) object;
			mos = this.recordManagedObjectSource(autoWire, mosSource, 0, 0);
			mo = this.recordManagedObject(mos, autoWire);

			// Add dependency (if required)
			if (dependencyName != null) {
				// Type should only be loaded in sourcing OfficeFloor.
				// Therefore type should not be loaded, and ok to define here.
				mosSource.addDependency(dependencyName, dependencyType, null);
			}
			break;

		default:
			fail("Unknown source type");
		}

		// Return the managed object
		return mo;
	}

}