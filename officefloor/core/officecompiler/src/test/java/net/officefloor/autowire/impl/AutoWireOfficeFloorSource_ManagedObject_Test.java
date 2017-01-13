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

import java.io.File;
import java.sql.Connection;

import javax.sql.DataSource;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireProperties;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link ManagedObject} configuration of
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource_ManagedObject_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Ensure can wire {@link ManagedObject}.
	 */
	public void testManagedObject() throws Exception {

		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, autoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure issue if provided object type.
	 */
	public void testManagedObjectWithNoAutoWires() {
		ManagedObjectSourceWirer wirer = this
				.createMock(ManagedObjectSourceWirer.class);
		try {
			this.source.addManagedObject(
					ClassManagedObjectSource.class.getName(), wirer);
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause",
					"Must provide at least one AutoWire", ex.getMessage());
		}
	}

	/**
	 * Ensure not load unused {@link ManagedObject}.
	 */
	public void testUnusedManagedObject() throws Exception {

		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, autoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record (not load managed object)
		this.recordManagedObjectType(object);
		this.recordOffice(); // no objects used

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObject} with multiple {@link AutoWire}.
	 */
	public void testManagedObjectWithMultipleAutoWiring() throws Exception {

		final AutoWire typeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire objectAutoWire = new AutoWire(MockRawObject.class);

		// Add the managed object
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, typeAutoWire,
				objectAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(typeAutoWire); // only one used
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				typeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				typeAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, typeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire raw object for multiple {@link AutoWire}.
	 */
	public void testManagedObjectForMultipleAutoWiring() throws Exception {

		final AutoWire typeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire objectAutoWire = new AutoWire(MockRawObject.class);

		// Add the managed object
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, typeAutoWire,
				objectAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(typeAutoWire, objectAutoWire); // both used
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				typeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				typeAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, typeAutoWire);
		this.recordOfficeObject(mo, objectAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire qualified {@link ManagedObject}.
	 */
	public void testManagedObjectWithQualifiedAutoWire() throws Exception {

		final AutoWire autoWire = new AutoWire("QUALIFIED",
				MockRawType.class.getName());

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, autoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can specify the timeout to source the {@link ManagedObject}.
	 */
	public void testManagedObjectSourceTimeout() throws Exception {

		final long TIMEOUT = 100;
		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, autoWire);
		object.setTimeout(TIMEOUT);

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, TIMEOUT);
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can load properties from class path.
	 */
	public void testProperties() throws Exception {

		// Ensure no environment override so load from class path
		System.clearProperty(AutoWireProperties.ENVIRONMENT_PROPERTIES_DIRECTORY);

		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the managed object loading properties from class path
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, autoWire);
		object.loadProperties(this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/object.properties");

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, 0,
				"class.path.property", "available");
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can load environment properties from class path.
	 */
	public void testEnvironmentProperties() throws Exception {

		// Obtain location of the environment properties file
		File environmentPropertiesFile = this.findFile(this.getClass(),
				"object.properties");

		// Specify the environment override
		System.setProperty(AutoWireProperties.ENVIRONMENT_PROPERTIES_DIRECTORY,
				environmentPropertiesFile.getParentFile().getAbsolutePath());

		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the managed object loading properties from class path
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, autoWire);
		object.loadProperties("object.properties");

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, 0,
				"class.path.property", "available");
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObject} to dependency.
	 */
	public void testManagedObjectWithDependency() throws Exception {

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, connectionAutoWire);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source
				.addManagedObject(ClassManagedObjectSource.class.getName(),
						null, rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice(rawTypeAutoWire); // dependency should also load
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject managedObject = this.recordManagedObject(
				source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				new AutoWire(Connection.class));
		this.recordRawObject(connection, connectionAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "dependency",
				connectionAutoWire);
		this.recordOfficeObject(managedObject, rawTypeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource} based on its qualified
	 * {@link ManagedObjectType}.
	 */
	public void testManagedObjectWithQualifiedDependency() throws Exception {

		final AutoWire oneAutoWire = new AutoWire("ONE",
				Connection.class.getName());
		final AutoWire twoAutoWire = new AutoWire("TWO",
				Connection.class.getName());
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Provide two qualified dependency objects
		final Connection connectionOne = this.createMock(Connection.class);
		final Connection connectionTwo = this.createMock(Connection.class);
		this.source.addObject(connectionOne, oneAutoWire);
		this.source.addObject(connectionTwo, twoAutoWire);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source
				.addManagedObject(ClassManagedObjectSource.class.getName(),
						null, rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source (only used will be loaded)
		this.recordRawObjectType(connectionOne);
		this.recordRawObjectType(connectionTwo);
		this.recordManagedObjectType(object);
		this.recordOffice(rawTypeAutoWire); // appropriate dependency loads
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency", twoAutoWire);
		this.recordRawObject(connectionTwo, twoAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "dependency",
				twoAutoWire);
		this.recordOfficeObject(mo, rawTypeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource} qualified dependency to
	 * default {@link AutoWire}.
	 */
	public void testManagedObjectWithQualifiedDependencyToDefaultAutoWire()
			throws Exception {

		final AutoWire qualifiedAutoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());
		final AutoWire unqualifiedAutoWire = new AutoWire(Connection.class);
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, unqualifiedAutoWire);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source
				.addManagedObject(ClassManagedObjectSource.class.getName(),
						null, rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice(rawTypeAutoWire); // dependency should also load
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				qualifiedAutoWire);
		this.recordRawObject(connection, unqualifiedAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "dependency",
				unqualifiedAutoWire);
		this.recordOfficeObject(mo, rawTypeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure issue if {@link ManagedObjectDependencyType} without appropriate
	 * matching {@link ManagedObject}.
	 */
	public void testManagedObjectWithoutMatchingQualifiedDependency()
			throws Exception {

		final ManagedObjectDependency moDependency = this
				.createMock(ManagedObjectDependency.class);

		final AutoWire notMatchAutoWire = new AutoWire("NOT_MATCH",
				Connection.class.getName());
		final AutoWire dependencyAutoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Provide qualified dependency object that will not match
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, notMatchAutoWire);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source
				.addManagedObject(ClassManagedObjectSource.class.getName(),
						null, rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice(rawTypeAutoWire); // dependency should also load
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				dependencyAutoWire);
		this.recordReturn(mo, mo.getManagedObjectDependency("dependency"),
				moDependency);
		this.recordReturn(moDependency,
				moDependency.getManagedObjectDependencyName(), "dependency");
		this.deployer
				.addIssue("Managed Object "
						+ MockRawType.class.getName()
						+ " has no dependent managed object for auto-wiring dependency dependency (qualifier=QUALIFIED, type="
						+ Connection.class.getName() + ")");
		this.recordOfficeObject(mo, rawTypeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObject} to more specific dependency.
	 */
	public void testManagedObjectWithWiredDependency() throws Exception {

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Map dependency (providing more specific type than Object)
				context.mapDependency("dependency", new AutoWire(
						Connection.class));
			}
		};

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, connectionAutoWire);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer,
				rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source (with non-specific dependency types)
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice(rawTypeAutoWire); // dependency should also load
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject managedObject = this.recordManagedObject(
				source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				new AutoWire(Object.class));
		this.recordRawObject(connection, connectionAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "dependency",
				connectionAutoWire);
		this.recordOfficeObject(managedObject, rawTypeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObject} to used dependency.
	 */
	public void testManagedObjectWithUsedManagedObjectDependency()
			throws Exception {

		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire connectionAutoWire = new AutoWire(Connection.class);

		// Add the managed object
		AutoWireObject object = this.source
				.addManagedObject(ClassManagedObjectSource.class.getName(),
						null, rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Provide the dependencies
		// Note: provided after managed object to validate only loads to office
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, connectionAutoWire);

		// Record Managed Object Source (with non-specific dependency types)
		this.recordManagedObjectType(object);
		this.recordRawObjectType(connection);
		this.recordOffice(rawTypeAutoWire, connectionAutoWire); // both used
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject managedObject = this.recordManagedObject(
				source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "connection",
				new AutoWire(Connection.class));
		OfficeFloorManagedObject rawObject = this.recordRawObject(connection,
				connectionAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "connection",
				connectionAutoWire);
		this.recordOfficeObject(managedObject, rawTypeAutoWire);
		this.recordOfficeObject(rawObject, connectionAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure {@link ManagedObject} to multiple dependencies.
	 */
	public void testManagedObjectWithMultipleDependencies() throws Exception {

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire dataSourceAutoWire = new AutoWire(DataSource.class);
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Provide the dependencies
		final Connection connection = this.createMock(Connection.class);
		final DataSource dataSource = this.createMock(DataSource.class);
		this.source.addObject(connection, connectionAutoWire);
		this.source.addObject(dataSource, dataSourceAutoWire);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source
				.addManagedObject(ClassManagedObjectSource.class.getName(),
						null, rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source (with non-specific dependency types)
		this.recordRawObjectType(connection);
		this.recordRawObjectType(dataSource);
		this.recordManagedObjectType(object);
		this.recordOffice(rawTypeAutoWire); // dependencies should also load
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "connection",
				new AutoWire(Connection.class), "dataSource", new AutoWire(
						DataSource.class));
		this.recordRawObject(connection, connectionAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "connection",
				connectionAutoWire);
		this.recordRawObject(dataSource, dataSourceAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "dataSource",
				dataSourceAutoWire);
		this.recordOfficeObject(mo, rawTypeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObject} to multiple dependencies.
	 */
	public void testManagedObjectWithWiredMultipleDependencies()
			throws Exception {

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map dependencies (providing more specific type than Object)
				context.mapDependency("connection", new AutoWire(
						Connection.class));
				context.mapDependency("dataSource", new AutoWire(
						DataSource.class));
			}
		};

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire dataSourceAutoWire = new AutoWire(DataSource.class);
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Provide the dependencies
		final Connection connection = this.createMock(Connection.class);
		final DataSource dataSource = this.createMock(DataSource.class);
		this.source.addObject(connection, connectionAutoWire);
		this.source.addObject(dataSource, dataSourceAutoWire);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer,
				rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source (with non-specific dependency types)
		this.recordRawObjectType(connection);
		this.recordRawObjectType(dataSource);
		this.recordManagedObjectType(object);
		this.recordOffice(rawTypeAutoWire); // dependencies should also load
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "connection",
				new AutoWire(Object.class), "dataSource", new AutoWire(
						Object.class));
		this.recordRawObject(connection, connectionAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "connection",
				connectionAutoWire);
		this.recordRawObject(dataSource, dataSourceAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "dataSource",
				dataSourceAutoWire);
		this.recordOfficeObject(mo, rawTypeAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can configure cyclic dependency (allowing compiler to handle
	 * cyclic dependency issues).
	 */
	public void testCyclicManagedObjectDependency() throws Exception {

		final AutoWire cyclicAutoWire = new AutoWire(MockRawType.class);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, cyclicAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.recordManagedObjectType(object);
		this.recordOffice(cyclicAutoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				cyclicAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				cyclicAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				cyclicAutoWire);
		this.recordLinkManagedObjectDependency(cyclicAutoWire, "dependency",
				cyclicAutoWire);
		this.recordOfficeObject(mo, cyclicAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can configure cyclic graph dependency (allowing compiler to handle
	 * cyclic dependency issues).
	 */
	public void testCyclicGraphManagedObjectDependency() throws Exception {

		final AutoWire oneAutoWire = new AutoWire(MockRawType.class);
		final AutoWire twoAutoWire = new AutoWire(MockRawObject.class);

		// Add the first managed object
		AutoWireObject oneObject = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, oneAutoWire);
		oneObject.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Add the second managed object
		AutoWireObject twoObject = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, twoAutoWire);
		twoObject.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.recordManagedObjectType(oneObject);
		this.recordManagedObjectType(twoObject);
		this.recordOffice(oneAutoWire);
		OfficeFloorManagedObjectSource oneSource = this
				.recordManagedObjectSource(oneAutoWire,
						ClassManagedObjectSource.class, 0, 0,
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
						MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(oneSource,
				oneAutoWire);
		this.recordManagedObjectDependencies(oneObject, "dependency",
				twoAutoWire);
		OfficeFloorManagedObjectSource twoSource = this
				.recordManagedObjectSource(twoAutoWire,
						ClassManagedObjectSource.class, 0, 0,
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
						MockRawObject.class.getName());
		this.recordManagedObject(twoSource, twoAutoWire);
		this.recordManagedObjectDependencies(twoObject, "dependency",
				oneAutoWire);
		this.recordLinkManagedObjectDependency(twoAutoWire, "dependency",
				oneAutoWire);
		this.recordLinkManagedObjectDependency(oneAutoWire, "dependency",
				twoAutoWire);
		this.recordOfficeObject(mo, oneAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can change the {@link ManagedObjectScope}.
	 */
	public void testChangeManagedObjectScope() throws Exception {

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.setManagedObjectScope(ManagedObjectScope.WORK);
			}
		};

		final AutoWire autoWire = new AutoWire(MockRawType.class);
		final OfficeFloorManagedObject mo = this
				.createMock(OfficeFloorManagedObject.class);

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, autoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordManagedObjectType(object);
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordReturn(source, source.addOfficeFloorManagedObject(
				autoWire.getQualifiedType(), ManagedObjectScope.WORK), mo);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

}