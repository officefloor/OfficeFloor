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

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireProperties;
import net.officefloor.autowire.AutoWireSupplier;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.autowire.impl.supplier.MockTypeManagedObjectSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceContext;
import net.officefloor.autowire.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.autowire.supplier.SuppliedManagedObjectDependencyType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectFlowType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.autowire.supplier.SuppliedManagedObjectType;
import net.officefloor.compile.integrate.managedobject.CompileOfficeFloorManagedObjectTest.InputManagedObject;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

import org.easymock.AbstractMatcher;
import org.junit.Ignore;

/**
 * Tests the {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO addSupplier")
public class AutoWireOfficeFloorSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure issue if {@link ManagedObject} depends on an unhandled
	 * {@link OfficeFloorInputManagedObject} (ie not loaded).
	 */
	public void testManagedObjectDependsOnUnhandledInputManagedObject() {
		fail("TODO implement test");
	}

	/**
	 * {@link AutoWireOfficeFloorSource} to test.
	 */
	private final AutoWireApplication source = new AutoWireOfficeFloorSource();

	/**
	 * {@link OfficeFloorDeployer}.
	 */
	private final OfficeFloorDeployer deployer = this
			.createMock(OfficeFloorDeployer.class);

	/**
	 * {@link OfficeFloorSourceContext}.
	 */
	private final OfficeFloorSourceContext context = this
			.createMock(OfficeFloorSourceContext.class);

	/**
	 * Ensure can load simple case of just the {@link Office}.
	 */
	public void testSimple() throws Exception {

		// Record
		this.recordTeam();
		this.recordOffice();

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if provided object type.
	 */
	public void testEnsureProvideObjectType() {
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
	 * Ensure can load with a raw object.
	 */
	public void testRawObject() throws Throwable {

		final MockRawObject dependency = new MockRawObject();
		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the raw object dependency
		this.source.addObject(dependency, autoWire);

		// Record
		this.recordTeam();
		this.recordRawObjectType(dependency);
		this.recordOffice(autoWire);
		OfficeFloorManagedObject mo = this
				.recordRawObject(dependency, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
		this.recordRawObjectType(dependency);
		this.recordOffice(); // no objects used

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
		this.recordRawObjectType(object);
		this.recordOffice(new AutoWire(String.class));
		OfficeFloorManagedObject mo = this.recordRawObject(object, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
		this.recordRawObjectType(dependency);
		this.recordOffice(autoWire);
		OfficeFloorManagedObject mo = this
				.recordRawObject(dependency, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
		this.recordRawObjectType(object);
		this.recordOffice(typeAutoWire); // only use one
		OfficeFloorManagedObject mo = this
				.recordRawObject(object, typeAutoWire);
		this.recordOfficeObject(mo, typeAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
		this.recordRawObjectType(object);
		this.recordOffice(typeAutoWire, objectAutoWire); // both types used
		OfficeFloorManagedObject managedObject = this.recordRawObject(object,
				typeAutoWire);
		this.recordOfficeObject(managedObject, typeAutoWire);
		this.recordOfficeObject(managedObject, objectAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice(); // no objects used

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObject} with multiple types.
	 */
	public void testManagedObjectWithMultipleTypes() throws Exception {

		final AutoWire typeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire objectAutoWire = new AutoWire(MockRawObject.class);

		// Add the managed object
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, typeAutoWire,
				objectAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire raw object for multiple types.
	 */
	public void testManagedObjectForMultipleTypes() throws Exception {

		final AutoWire typeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire objectAutoWire = new AutoWire(MockRawObject.class);

		// Add the managed object
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, typeAutoWire,
				objectAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire qualified {@link ManagedObject}.
	 */
	public void testManagedObjectWithQualifiedType() throws Exception {

		final AutoWire autoWire = new AutoWire("QUALIFIED",
				MockRawType.class.getName());

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), null, autoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, TIMEOUT);
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordManagedObjectDependencies(object);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource}.
	 */
	public void testWireManagedObject() throws Exception {

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Map dependency (providing more specific type than Object)
				context.mapDependency("dependency", new AutoWire(
						Connection.class));

				// Map flow
				context.mapFlow("flow", "section", "sectionInput");

				// Map team
				context.mapTeam("team", OnePersonTeamSource.class.getName())
						.addProperty("name", "value");
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
		this.recordTeam();
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
		this.recordManagedObjectFlow(source, "flow", "section", "sectionInput");
		this.recordManagedObjectTeam(source, "team", OnePersonTeamSource.class,
				"name", "value");
		this.recordOfficeObject(managedObject, rawTypeAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObject} to used dependency.
	 */
	public void testWiredManagedObjectToUsedDependeny() throws Exception {

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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObject} to multiple dependencies.
	 */
	public void testWiredManagedObjectToMultipleDependencies() throws Exception {

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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectTeam}.
	 */
	public void testAutoWireManagedObjectToQualifiedTeam() throws Exception {

		final AutoWire qualifiedTeamAutoWire = new AutoWire("QUALIFIED",
				MockRawType.class.getName());
		final AutoWire unqualifiedTeamAutoWire = new AutoWire(MockRawType.class);
		final AutoWire moAutoWire = new AutoWire(MockRawObject.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map team via auto-wire
				context.mapTeam("team", qualifiedTeamAutoWire);
			}
		};

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, moAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Add the team (one qualified, the other not to ensure pick qualified)
		this.source.assignTeam(OnePersonTeamSource.class.getName(),
				qualifiedTeamAutoWire);
		this.source.assignTeam(OnePersonTeamSource.class.getName(),
				unqualifiedTeamAutoWire);

		// Record Managed Object Source
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice(moAutoWire);
		this.recordTeam(new String[] {}, qualifiedTeamAutoWire);
		this.recordTeam(new String[] {}, unqualifiedTeamAutoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				moAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				moAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordManagedObjectTeam(source, "team", qualifiedTeamAutoWire);
		this.recordOfficeObject(mo, moAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectTeam} via default
	 * {@link AutoWire}.
	 */
	public void testAutoWireManagedObjectToDefaultTeam() throws Exception {

		final AutoWire qualifiedTeamAutoWire = new AutoWire("QUALIFIED",
				MockRawType.class.getName());
		final AutoWire unqualifiedTeamAutoWire = new AutoWire(MockRawType.class);
		final AutoWire moAutoWire = new AutoWire(MockRawObject.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map team via auto-wire
				context.mapTeam("team", qualifiedTeamAutoWire);
			}
		};

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, moAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Add the team for default auto-wire type
		this.source.assignTeam(OnePersonTeamSource.class.getName(),
				unqualifiedTeamAutoWire);

		// Record Managed Object Source
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice(moAutoWire);
		this.recordTeam(new String[] {}, unqualifiedTeamAutoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				moAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				moAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordManagedObjectTeam(source, "team", unqualifiedTeamAutoWire);
		this.recordOfficeObject(mo, moAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no appropriate {@link OfficeFloorTeam} for
	 * {@link ManagedObjectTeam} via {@link AutoWire}.
	 */
	public void testAutoWireManagedObjectWithoutMatchingTeam() throws Exception {

		final AutoWire teamAutoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());
		final AutoWire moAutoWire = new AutoWire(MockRawObject.class);
		final ManagedObjectTeam moTeam = this
				.createMock(ManagedObjectTeam.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map team via auto-wire
				context.mapTeam("TEAM", teamAutoWire);
			}
		};

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, moAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// No auto-wire team available

		// Record Managed Object Source
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice(moAutoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				moAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				moAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordReturn(source, source.getManagedObjectTeam("TEAM"), moTeam);
		this.deployer.addIssue(
				"No Team for auto-wiring ManagedObjectTeam TEAM (qualifier=QUALIFIED, type="
						+ Connection.class.getName() + ")",
				AssetType.MANAGED_OBJECT, moAutoWire.getQualifiedType());
		this.recordOfficeObject(mo, moAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource} from default type.
	 */
	public void testWireManagedObjectDependencyBasedOnType() throws Exception {

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

		// Record Managed Object Source
		this.recordTeam();
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
				connectionAutoWire);
		this.recordRawObject(connection, connectionAutoWire);
		this.recordLinkManagedObjectDependency(rawTypeAutoWire, "dependency",
				connectionAutoWire);
		this.recordOfficeObject(mo, rawTypeAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource} qualified dependency.
	 */
	public void testWireQualifiedManagedObjectDependencyBasedOnDefaultType()
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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource} based on its qualified
	 * {@link ManagedObjectType}.
	 */
	public void testWireQualifiedManagedObjectDependencyBasedOnQualifiedType()
			throws Exception {

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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if {@link ManagedObjectDependencyType} without appropriate
	 * matching {@link ManagedObject}.
	 */
	public void testQualifiedManagedObjectDependencyWithoutMatchingManagedObject()
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
		this.recordTeam();
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
				.addIssue(
						"No dependent managed object for auto-wiring dependency dependency (qualifier=QUALIFIED, type="
								+ Connection.class.getName() + ")",
						AssetType.MANAGED_OBJECT,
						rawTypeAutoWire.getQualifiedType());
		this.recordOfficeObject(mo, rawTypeAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
		this.recordTeam();
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link OfficeFloorInputManagedObject}.
	 */
	public void testWireInputManagedObject() throws Exception {

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Map flow
				context.mapFlow("flow", "section", "sectionInput");

				// Map team
				context.mapTeam("team", OnePersonTeamSource.class.getName())
						.addProperty("name", "value");
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

		// Record Managed Object Source
		this.recordTeam();
		this.recordRawObjectType(connection);
		this.registerManagedObjectFlowType(object, "flow");
		this.registerManagedObjectTeamType(object, "team");
		this.recordManagedObjectType(object);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice(); // handled input managed object always loaded
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				connectionAutoWire);
		this.recordRawObject(connection, connectionAutoWire);
		this.recordInputManagedObjectDependency(source, "dependency",
				connectionAutoWire);
		this.recordManagedObjectFlow(source, "flow", "section", "sectionInput");
		this.recordManagedObjectTeam(source, "team", OnePersonTeamSource.class,
				"name", "value");

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire qualified {@link OfficeFloorInputManagedObject}.
	 */
	public void testWireInputManagedObjectWithQualifiedType() throws Exception {

		// Providing wiring indicating input managed object
		ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "section", "sectionInput");
			}
		};

		final AutoWire autoWire = new AutoWire("QUALIFIED",
				MockRawType.class.getName());

		// Add the wiring of first input managed object
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, autoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record team and office
		this.recordTeam();
		this.registerManagedObjectFlowType(object, "flow");
		this.recordManagedObjectType(object);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice(); // input loaded as handled

		// Record the input managed object
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, autoWire);
		this.recordManagedObjectDependencies(object);
		this.recordManagedObjectFlow(source, "flow", "section", "sectionInput");

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure not include unhandled {@link OfficeFloorInputManagedObject}.
	 */
	public void testInputManagedObjectWithUnhandledFlow() throws Exception {

		// Configure as Input Managed Object
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "section", "sectionInput");
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

		// Record Managed Object Source
		this.recordTeam();
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice();
		// Not handled input, so not load managed objects

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure not include {@link OfficeFloorInputManagedObject} with only some
	 * handled flows.
	 */
	public void testInputManagedObjectWithSomeHandledFlows() throws Exception {

		// Configure as Input Managed Object
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flowOne", "handled", "input");
				context.mapFlow("flowTwo", "unhandled", "input");
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

		// Record Managed Object Source
		this.recordTeam();
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.registerOfficeInput("handled", "input");
		this.recordOffice();
		// Not all inputs handled, so not load managed objects

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if multiple {@link AutoWire} instances for
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testInputManagedObjectWithMultipleAutoWiring() throws Exception {

		// Configure as Input Managed Object
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "section", "sectionInput");
			}
		};

		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire rawObjectAutoWire = new AutoWire(MockRawObject.class);

		// Add the multiple wiring of the input managed object
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer,
				rawTypeAutoWire, rawObjectAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.recordTeam();
		this.registerManagedObjectFlowType(object, "flow");
		this.recordManagedObjectType(object);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice();
		this.deployer.addIssue("OfficeFloorInputManagedObject "
				+ rawTypeAutoWire.getQualifiedType()
				+ " must have only one AutoWire", AssetType.MANAGED_OBJECT,
				rawTypeAutoWire.getQualifiedType());
		// Not use the input managed object (even though it can be handled)

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire multiple {@link OfficeFloorInputManagedObject}.
	 */
	public void testWireMultipleInputManagedObject() throws Exception {

		// Providing wiring indicating input managed object
		ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "section", "sectionInput");
			}
		};

		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the wiring of first input managed object
		AutoWireObject firstObject = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, autoWire);
		firstObject.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Add the wiring of second input managed object
		AutoWireObject secondObject = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, autoWire);
		secondObject.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record team and office
		this.recordTeam();
		this.registerManagedObjectFlowType(firstObject, "flow");
		this.recordManagedObjectType(firstObject);
		this.registerManagedObjectFlowType(secondObject, "flow");
		this.recordManagedObjectType(secondObject);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice(); // both should be handled and therefore loaded

		// Record the first input managed object
		OfficeFloorManagedObjectSource mosOne = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(mosOne, autoWire);

		// Record the second input managed object
		OfficeFloorManagedObjectSource mosTwo = this.recordManagedObjectSource(
				autoWire, ClassManagedObjectSource.class, 1, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(mosTwo, autoWire);

		// Record linking input managed objects
		this.recordManagedObjectDependencies(firstObject);
		this.recordManagedObjectFlow(mosOne, "flow", "section", "sectionInput");
		this.recordManagedObjectDependencies(secondObject);
		this.recordManagedObjectFlow(mosTwo, "flow", "section", "sectionInput");

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
	 * Ensure able to assign a {@link Team}.
	 */
	public void testAssignTeam() throws Exception {

		final AutoWire autoWire = new AutoWire(Connection.class);

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(
				OnePersonTeamSource.class.getName(), autoWire);
		team.addProperty("name", "value");

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(new String[] { "name", "value" }, autoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to assign a {@link Team} with qualified type.
	 */
	public void testAssignTeamWithQualifiedType() throws Exception {

		final AutoWire autoWire = new AutoWire("QUALIFIED",
				Connection.class.getName());

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(
				OnePersonTeamSource.class.getName(), autoWire);
		team.addProperty("name", "value");

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(new String[] { "name", "value" }, autoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to assign a {@link Team} multiple responsibilities.
	 */
	public void testAssignTeamMultipleResponsibilities() throws Exception {

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire dataSourceAutoWire = new AutoWire(DataSource.class);

		// Assign the team with multiple responsibilities
		this.source.assignTeam(OnePersonTeamSource.class.getName(),
				connectionAutoWire, dataSourceAutoWire);

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(null, connectionAutoWire, dataSourceAutoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to override the default {@link Team}.
	 */
	public void testOverrideDefaultTeam() throws Exception {

		// Assign the default team
		AutoWireTeam defaultTeam = this.source
				.assignDefaultTeam(PassiveTeamSource.class.getName());
		defaultTeam.addProperty("name", "value");

		// Record
		OfficeFloorTeam officeFloorTeam = this
				.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer, this.deployer.addTeam("team",
				PassiveTeamSource.class.getName()), officeFloorTeam);
		officeFloorTeam.addProperty("name", "value");
		this.recordOffice(officeFloorTeam);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can use {@link SuppliedManagedObjectType}.
	 */
	public void testSuppliedManagedObject() throws Exception {

		final AutoWire autoWire = new AutoWire(Connection.class);
		final ClassManagedObjectSource mos = new ClassManagedObjectSource();

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				AutoWireObject object = context.addManagedObject(mos, null,
						autoWire);
				object.addProperty(
						ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
						MockRawObject.class.getName());
			}
		});

		// Record
		this.recordTeam();
		this.recordOffice(autoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				autoWire, mos, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		OfficeFloorManagedObject mo = this
				.recordManagedObject(source, autoWire);
		this.recordOfficeObject(mo, autoWire);

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can selectively use a {@link SuppliedManagedObjectType}.
	 */
	public void testSelectivelyUseSuppliedManagedObjects() throws Exception {

		final MockTypeManagedObjectSource one = new MockTypeManagedObjectSource(
				String.class);
		final MockTypeManagedObjectSource two = new MockTypeManagedObjectSource(
				Connection.class);
		final MockTypeManagedObjectSource three = new MockTypeManagedObjectSource(
				Integer.class);

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				one.addAsManagedObject(context, null);
				two.addAsManagedObject(context, null);
				three.addAsManagedObject(context, null);
			}
		});

		// Record (only two is loaded)
		this.recordTeam();
		this.recordOffice(two.getAutoWire()); // selectively only use two
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				two.getAutoWire(), two, 0, 0);
		OfficeFloorManagedObject mo = this.recordManagedObject(source,
				two.getAutoWire());
		this.recordOfficeObject(mo, two.getAutoWire());

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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
	 * Ensure can link {@link SuppliedManagedObjectFlowType}.
	 */
	public void testLinkSuppliedManagedObjectFlow() throws Exception {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				String.class);
		source.addFlow("flow", Integer.class);

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "SECTION", "INPUT");
			}
		};

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				source.addAsManagedObject(context, wirer);
			}
		});

		// Record
		this.recordTeam();
		DeployedOffice office = this.recordOffice(source.getAutoWire());
		OfficeFloorManagedObjectSource mos = this.recordManagedObjectSource(
				source.getAutoWire(), source, 0, 0);
		OfficeFloorManagedObject mo = this.recordManagedObject(mos,
				source.getAutoWire());

		// Record linking flow to input
		final ManagedObjectFlow flow = this.createMock(ManagedObjectFlow.class);
		final DeployedOfficeInput officeInput = this
				.createMock(DeployedOfficeInput.class);
		this.recordReturn(mos, mos.getManagedObjectFlow("flow"), flow);
		this.recordReturn(office,
				office.getDeployedOfficeInput("SECTION", "INPUT"), officeInput);
		this.deployer.link(flow, officeInput);

		// Record linking into office
		this.recordOfficeObject(mo, source.getAutoWire());

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link SuppliedManagedObjectTeamType} to
	 * {@link OfficeFloorTeam}.
	 */
	public void testLink_SuppliedManagedObjectTeam_To_OfficeFloorTeam()
			throws Exception {
		this.doLinkSuppliedManagedObjectTeamTest(null, null);
	}

	/**
	 * Ensure can link qualified {@link SuppliedManagedObjectTeamType} to
	 * qualified {@link OfficeFloorTeam}.
	 */
	public void testLink_QualifiedSuppliedManagedObjectTeam_To_QualifiedOfficeFloorTeam()
			throws Exception {
		this.doLinkSuppliedManagedObjectTeamTest("QUALIFIED", "QUALIFIED");
	}

	/**
	 * Ensure can link qualified {@link SuppliedManagedObjectTeamType} to
	 * {@link OfficeFloorTeam}.
	 */
	public void testLink_QualifiedSuppliedManagedObjectTeam_To_OfficeFloorTeam()
			throws Exception {
		this.doLinkSuppliedManagedObjectTeamTest("QUALIFIED", null);
	}

	/**
	 * Undertakes testing to link {@link ManagedObjectTeam} to
	 * {@link OfficeFloorTeam}.
	 * 
	 * @param managedObjectTeamQualifier
	 *            Qualifier for the {@link ManagedObjectTeam}.
	 * @param teamQualifier
	 *            Qualifier for the {@link OfficeFloorTeam}.
	 */
	private void doLinkSuppliedManagedObjectTeamTest(
			final String managedObjectTeamQualifier, String teamQualifier)
			throws Exception {

		final AutoWire managedObjectTeamAutoWire = new AutoWire(
				managedObjectTeamQualifier, Connection.class.getName());
		final AutoWire officeFloorTeamAutoWire = new AutoWire(teamQualifier,
				Connection.class.getName());

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				String.class);
		source.addTeam("team");

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapTeam("team", managedObjectTeamAutoWire);
			}
		};

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				source.addAsManagedObject(context, wirer);
			}
		});

		// Provide team
		this.source.assignTeam("TEAM", officeFloorTeamAutoWire);

		// Record
		this.recordTeam();
		OfficeFloorTeam team = this.recordTeam(new String[] {},
				officeFloorTeamAutoWire);
		this.recordOffice(source.getAutoWire());
		OfficeFloorManagedObjectSource mos = this.recordManagedObjectSource(
				source.getAutoWire(), source, 0, 0);
		OfficeFloorManagedObject mo = this.recordManagedObject(mos,
				source.getAutoWire());

		// Record linking managed object team to team
		final ManagedObjectTeam moTeam = this
				.createMock(ManagedObjectTeam.class);
		this.recordReturn(mos, mos.getManagedObjectTeam("team"), moTeam);
		this.deployer.link(moTeam, team);

		// Record linking into office
		this.recordOfficeObject(mo, source.getAutoWire());

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link OfficeFloorTeam} for
	 * {@link SuppliedManagedObjectTeamType}.
	 */
	public void testNoTeamForSuppliedManagedObjectTeam() throws Exception {

		final MockTypeManagedObjectSource source = new MockTypeManagedObjectSource(
				String.class);
		source.addTeam("team");

		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapTeam("team", new AutoWire(Connection.class));
			}
		};

		// Add the supplier
		this.addSupplier(new SupplierInit() {
			@Override
			public void supply(SupplierSourceContext context) throws Exception {
				source.addAsManagedObject(context, wirer);
			}
		});

		// No OfficeFloor Team

		// Record
		this.recordTeam();
		this.recordOffice(source.getAutoWire());
		OfficeFloorManagedObjectSource mos = this.recordManagedObjectSource(
				source.getAutoWire(), source, 0, 0);
		OfficeFloorManagedObject mo = this.recordManagedObject(mos,
				source.getAutoWire());

		// Record issue as no corresponding OfficeFloor Team
		this.deployer
				.addIssue(
						"No OfficeFloorTeam available for ManagedObjectTeam 'team' (qualifier=null, type="
								+ Connection.class.getName() + ")",
						AssetType.MANAGED_OBJECT, source.getAutoWire()
								.getQualifiedType());

		// Record linking into office
		this.recordOfficeObject(mo, source.getAutoWire());

		// Test
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
	}

	/**
	 * {@link OfficeFloorTeam}.
	 */
	private final OfficeFloorTeam team = this.createMock(OfficeFloorTeam.class);

	/**
	 * {@link OfficeFloorTeam} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeFloorTeam> teams = new HashMap<AutoWire, OfficeFloorTeam>();

	/**
	 * {@link DeployedOffice}.
	 */
	private final DeployedOffice office = this.createMock(DeployedOffice.class);

	/**
	 * Handled instances.
	 */
	private final List<AutoWire> handledInputs = new LinkedList<AutoWire>();

	/**
	 * {@link OfficeFloorManagedObject} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeFloorManagedObject> managedObjects = new HashMap<AutoWire, OfficeFloorManagedObject>();

	/**
	 * {@link OfficeFloorInputManagedObject} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<AutoWire, OfficeFloorInputManagedObject>();

	/**
	 * {@link OfficeObject} instances by {@link AutoWire}.
	 */
	private final Map<AutoWire, OfficeObject> objects = new HashMap<AutoWire, OfficeObject>();

	/**
	 * {@link ManagedObjectType} instances by {@link AutoWireObject}.
	 */
	private final Map<AutoWireObject, ManagedObjectType<?>> managedObjectTypes = new HashMap<AutoWireObject, ManagedObjectType<?>>();

	/**
	 * {@link ManagedObjectFlowType} names by {@link AutoWireObject}.
	 */
	private final Map<AutoWireObject, List<String>> managedObjectFlowTypes = new HashMap<AutoWireObject, List<String>>();

	/**
	 * {@link ManagedObjectTeamType} names by {@link AutoWireObject}.
	 */
	private final Map<AutoWireObject, List<String>> managedObjectTeamTypes = new HashMap<AutoWireObject, List<String>>();

	/**
	 * Adds {@link SupplierSource}.
	 * 
	 * @param init
	 *            {@link SupplierInit}.
	 */
	private void addSupplier(SupplierInit init) {
		DynamicSupplierSource.addSupplier(this.source, init);
	}

	/**
	 * Provides the supply for the {@link DynamicSupplierSource}.
	 */
	private static interface SupplierInit {

		/**
		 * Provides the supply.
		 * 
		 * @param context
		 *            {@link SupplierSourceContext}.
		 * @throws Exception
		 *             If fails to supply.
		 */
		void supply(SupplierSourceContext context) throws Exception;
	}

	/**
	 * Dynamic {@link SupplierSource}.
	 */
	@TestSource
	public static class DynamicSupplierSource extends AbstractSupplierSource {

		/**
		 * Identifier to obtain the appropriate {@link SupplierInit}.
		 */
		public static final String PROPERTY_SUPPLIER_IDENTIFIER = "SUPPLIER_ID";

		/**
		 * {@link SupplierInit} instances by their identifiers.
		 */
		private static final Map<Integer, SupplierInit> supplierInits = new HashMap<Integer, SupplierInit>();

		/**
		 * Setup for the next test.
		 */
		public static void reset() {
			supplierInits.clear();
		}

		/**
		 * Adds this to the {@link AutoWireApplication} with the
		 * {@link SupplierInit}.
		 * 
		 * @param application
		 *            {@link AutoWireApplication}.
		 * @param init
		 *            {@link SupplierInit}.
		 */
		public static void addSupplier(AutoWireApplication application,
				SupplierInit init) {

			// Obtain the next identifier (and register init)
			int identifier = supplierInits.size();
			supplierInits.put(Integer.valueOf(identifier), init);

			// Add the supplier
			AutoWireSupplier supplier = application
					.addSupplier(DynamicSupplierSource.class.getName());
			supplier.addProperty(PROPERTY_SUPPLIER_IDENTIFIER,
					String.valueOf(identifier));
		}

		/*
		 * ==================== SupplierSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_SUPPLIER_IDENTIFIER);
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {

			// Obtain the supplier init
			Integer identifier = Integer.valueOf(context
					.getProperty(PROPERTY_SUPPLIER_IDENTIFIER));
			SupplierInit init = supplierInits.get(identifier);
			assertNotNull("No init configured for identifier " + identifier,
					init);

			// Initialise the supplier
			init.supply(context);
		}
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
		this.replayMockObjects();
		this.doSourceOfficeFloor();
		this.verifyMockObjects();
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

	/**
	 * Sources the {@link OfficeFloor} for the {@link AutoWireApplication}.
	 */
	private void doSourceOfficeFloor() throws Exception {
		// Source the OfficeFloor
		((OfficeFloorSource) this.source).sourceOfficeFloor(this.deployer,
				this.context);
	}

	/**
	 * Records the {@link Team}.
	 */
	private void recordTeam() {
		this.recordReturn(
				this.deployer,
				this.deployer.addTeam("team",
						ProcessContextTeamSource.class.getName()), this.team);
	}

	/**
	 * Records the {@link DeployedOfficeInput}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	private void registerOfficeInput(String sectionName, String inputName) {
		this.handledInputs.add(new AutoWire(sectionName, inputName));
	}

	/**
	 * Records the {@link Office}.
	 * 
	 * @param officeObjectAutoWiring
	 *            {@link AutoWire} instances to identify the used
	 *            {@link OfficeObject} instances.
	 * @return {@link DeployedOffice}.
	 */
	private DeployedOffice recordOffice(AutoWire... officeObjectAutoWiring) {
		return this.recordOffice(this.team, officeObjectAutoWiring);
	}

	/**
	 * Records the office.
	 * 
	 * @param defaultTeam
	 *            Default {@link Team}.
	 * @param officeObjectAutoWiring
	 *            {@link AutoWire} instances to identify the used
	 *            {@link OfficeObject} instances.
	 * @return {@link DeployedOffice}.
	 */
	private DeployedOffice recordOffice(OfficeFloorTeam defaultTeam,
			AutoWire... officeObjectAutoWiring) {

		final PropertyList propertyList = this.createMock(PropertyList.class);
		final OfficeType officeType = this.createMock(OfficeType.class);

		// Create the office input types
		final OfficeInputType[] officeInputs = new OfficeInputType[this.handledInputs
				.size()];
		for (int i = 0; i < officeInputs.length; i++) {
			officeInputs[i] = this.createMock(OfficeInputType.class);
		}

		// Create the office managed object types
		final OfficeManagedObjectType[] officeObjects = new OfficeManagedObjectType[officeObjectAutoWiring.length];
		for (int i = 0; i < officeObjects.length; i++) {
			officeObjects[i] = this.createMock(OfficeManagedObjectType.class);
		}

		// Record the office type (with used office objects)
		this.recordReturn(this.context, this.context.createPropertyList(),
				propertyList);
		this.recordReturn(this.context, this.context.loadOfficeType(
				(OfficeSource) null, "auto-wire", propertyList), officeType,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertTrue("Incorrect Office",
								actual[0] instanceof OfficeSource);
						assertEquals("Incorrect location", expected[1],
								actual[1]);
						assertEquals("Incorrect properties", expected[2],
								actual[2]);
						return true;
					}
				});

		// Record the office input types
		this.recordReturn(officeType, officeType.getOfficeInputTypes(),
				officeInputs);
		for (int i = 0; i < officeInputs.length; i++) {
			OfficeInputType officeInput = officeInputs[i];
			AutoWire handledInput = this.handledInputs.get(i);
			this.recordReturn(officeInput, officeInput.getOfficeSectionName(),
					handledInput.getQualifier());
			this.recordReturn(officeInput,
					officeInput.getOfficeSectionInputName(),
					handledInput.getType());
		}

		// Record the office managed object types
		this.recordReturn(officeType, officeType.getOfficeManagedObjectTypes(),
				officeObjects);
		for (int i = 0; i < officeObjects.length; i++) {
			OfficeManagedObjectType officeObject = officeObjects[i];
			AutoWire officeObjectAutoWire = officeObjectAutoWiring[i];
			this.recordReturn(officeObject, officeObject.getObjectType(),
					officeObjectAutoWire.getType());
			this.recordReturn(officeObject, officeObject.getTypeQualifier(),
					officeObjectAutoWire.getQualifier());
		}

		// Record the office
		this.recordReturn(this.deployer, this.deployer.addDeployedOffice(
				"OFFICE", (OfficeSource) null, "auto-wire"), this.office,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect office name", expected[0],
								actual[0]);
						assertTrue("Incorrect Office",
								actual[1] instanceof OfficeSource);
						assertEquals("Incorrect location", expected[2],
								actual[2]);
						return true;
					}
				});

		// Record binding office team to team
		OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
		this.recordReturn(this.office,
				this.office.getDeployedOfficeTeam("team"), officeTeam);
		this.deployer.link(officeTeam, defaultTeam);

		// Return the office
		return this.office;
	}

	/**
	 * Indicates if the raw object {@link ManagedObjectType} matcher is
	 * specified.
	 */
	private boolean isRawObjectTypeMatcherSpecified = false;

	/**
	 * Records obtaining the {@link ManagedObjectType} for the raw object.
	 * 
	 * @param object
	 *            Raw Object.
	 * @param extensionInterfaces
	 *            Extension interfaces.
	 */
	private void recordRawObjectType(Object object,
			Class<?>... extensionInterfaces) {

		// Record the managed object type
		ManagedObjectType<?> managedObjectType = this
				.createMock(ManagedObjectType.class);
		this.recordReturn(this.context, this.context.loadManagedObjectType(
				new SingletonManagedObjectSource(object), null),
				managedObjectType);
		if (!this.isRawObjectTypeMatcherSpecified) {
			this.control(this.context).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertNotNull("Must have properties", actual[1]);

					// Match if raw objects match
					SingletonManagedObjectSource eMo = (SingletonManagedObjectSource) expected[0];
					SingletonManagedObjectSource aMo = (SingletonManagedObjectSource) actual[0];
					return eMo.getObject().equals(aMo.getObject());
				}
			});
			this.isRawObjectTypeMatcherSpecified = true;
		}

		// No flows and teams for raw object
		this.recordReturn(managedObjectType, managedObjectType.getFlowTypes(),
				new ManagedObjectFlowType<?>[0]);
		this.recordReturn(managedObjectType, managedObjectType.getTeamTypes(),
				new ManagedObjectTeamType[0]);

		// Record obtaining extension interfaces
		this.recordReturn(managedObjectType,
				managedObjectType.getExtensionInterfaces(), extensionInterfaces);
	}

	/**
	 * Register the {@link ManagedObjectFlowType} for the {@link ManagedObject}
	 * of the {@link AutoWireObject}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param flowNames
	 *            Names of {@link ManagedObjectFlowType}.
	 */
	private void registerManagedObjectFlowType(AutoWireObject object,
			String... flowNames) {
		List<String> flows = this.managedObjectFlowTypes.get(object);
		if (flows == null) {
			flows = new LinkedList<String>();
			this.managedObjectFlowTypes.put(object, flows);
		}
		flows.addAll(Arrays.asList(flowNames));
	}

	/**
	 * Register the {@link ManagedObjectTeamType} for the {@link ManagedObject}
	 * of the {@link AutoWireObject}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param teamNames
	 *            Names of {@link ManagedObjectTeamType}.
	 */
	private void registerManagedObjectTeamType(AutoWireObject object,
			String... teamNames) {
		List<String> teams = this.managedObjectTeamTypes.get(object);
		if (teams == null) {
			teams = new LinkedList<String>();
			this.managedObjectFlowTypes.put(object, teams);
		}
		teams.addAll(Arrays.asList(teamNames));
	}

	/**
	 * Records obtaining the {@link ManagedObjectType}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param extensionInterfaces
	 *            Extension interfaces.
	 */
	private void recordManagedObjectType(AutoWireObject object,
			Class<?>... extensionInterfaces) {

		// Record the managed object type
		ManagedObjectType<?> managedObjectType = this
				.createMock(ManagedObjectType.class);
		this.recordReturn(
				this.context,
				this.context.loadManagedObjectType(
						object.getManagedObjectSourceClassName(),
						object.getProperties()), managedObjectType);

		// Record flow types
		List<String> flowNames = this.managedObjectFlowTypes.get(object);
		int flowCount = (flowNames == null ? 0 : flowNames.size());
		this.recordReturn(managedObjectType, managedObjectType.getFlowTypes(),
				new ManagedObjectFlowType<?>[flowCount]);

		// Record team types
		List<String> teamNames = this.managedObjectTeamTypes.get(object);
		int teamCount = (teamNames == null ? 0 : teamNames.size());
		this.recordReturn(managedObjectType, managedObjectType.getTeamTypes(),
				new ManagedObjectTeamType[teamCount]);

		// Record obtaining extension interfaces
		this.recordReturn(managedObjectType,
				managedObjectType.getExtensionInterfaces(), extensionInterfaces);

		// Register the managed object type
		this.managedObjectTypes.put(object, managedObjectType);
	}

	/**
	 * Indicates if the {@link AbstractMatcher} is specified for the raw object.
	 */
	private boolean isRawObjectMatcherSpecified = false;

	/**
	 * Records raw object.
	 */
	private OfficeFloorManagedObject recordRawObject(Object dependency,
			AutoWire autoWire) {

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record the managed object source
		this.recordReturn(this.deployer, this.deployer.addManagedObjectSource(
				autoWire.getQualifiedType(), new SingletonManagedObjectSource(
						dependency)), source);
		if (!this.isRawObjectMatcherSpecified) {
			this.control(this.deployer).setMatcher(new AbstractMatcher() {
				@Override
				public boolean matches(Object[] expected, Object[] actual) {
					assertEquals("Incorrect name", expected[0], actual[0]);
					SingletonManagedObjectSource eMo = (SingletonManagedObjectSource) expected[1];
					SingletonManagedObjectSource aMo = (SingletonManagedObjectSource) actual[1];
					assertEquals("Incorrect singleton object", eMo.getObject(),
							aMo.getObject());
					return true;
				}
			});
			this.isRawObjectMatcherSpecified = true;
		}

		// Have managed by office
		ManagingOffice managingOffice = this.createMock(ManagingOffice.class);
		this.recordReturn(source, source.getManagingOffice(), managingOffice);
		this.deployer.link(managingOffice, this.office);

		// Record and return the managed object
		return this.recordManagedObject(source, autoWire);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	private OfficeFloorManagedObjectSource recordManagedObjectSource(
			AutoWire mosName, Class<?> managedObjectSourceClass, int typeIndex,
			long timeout, String... propertyNameValues) {
		return this.recordManagedObjectSource(mosName,
				managedObjectSourceClass, null, typeIndex, timeout,
				propertyNameValues);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	private OfficeFloorManagedObjectSource recordManagedObjectSource(
			AutoWire mosName, ManagedObjectSource<?, ?> managedObjectSource,
			int typeIndex, long timeout, String... propertyNameValues) {
		return this.recordManagedObjectSource(mosName, null,
				managedObjectSource, typeIndex, timeout, propertyNameValues);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	private OfficeFloorManagedObjectSource recordManagedObjectSource(
			AutoWire mosName, Class<?> managedObjectSourceClass,
			ManagedObjectSource<?, ?> managedObjectSource, int typeIndex,
			long timeout, String... propertyNameValues) {

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record the managed object source
		this.recordReturn(this.deployer, this.deployer.addManagedObjectSource(
				mosName.getQualifiedType()
						+ (typeIndex <= 0 ? "" : String.valueOf(typeIndex)),
				managedObjectSourceClass.getName()), source);

		// Record time out (-1 for no timeout on raw object)
		if (timeout != -1) {
			source.setTimeout(timeout);
		}

		// Record properties
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			source.addProperty(name, value);
		}

		// Have managed by office
		ManagingOffice managingOffice = this.createMock(ManagingOffice.class);
		this.recordReturn(source, source.getManagingOffice(), managingOffice);
		this.deployer.link(managingOffice, this.office);

		// Return the managed object source
		return source;
	}

	/**
	 * Records the {@link ManagedObject}.
	 * 
	 * @return {@link OfficeFloorManagedObject}.
	 */
	private OfficeFloorManagedObject recordManagedObject(
			OfficeFloorManagedObjectSource source, AutoWire autoWire) {

		// Record the managed object
		final OfficeFloorManagedObject mo = this
				.createMock(OfficeFloorManagedObject.class);
		this.recordReturn(source, source.addOfficeFloorManagedObject(
				autoWire.getQualifiedType(), ManagedObjectScope.PROCESS), mo);
		this.managedObjects.put(autoWire, mo);

		// Return the managed object
		return mo;
	}

	/**
	 * Records the {@link OfficeFloorInputManagedObject}.
	 */
	private OfficeFloorInputManagedObject recordInputManagedObject(
			OfficeFloorManagedObjectSource source, AutoWire autoWire) {

		// Obtain the Input Managed Object
		OfficeFloorInputManagedObject input = this.inputManagedObjects
				.get(autoWire);
		if (input == null) {
			// Create and register the input managed object
			input = this.createMock(OfficeFloorInputManagedObject.class);
			this.inputManagedObjects.put(autoWire, input);
			this.recordReturn(this.deployer, this.deployer
					.addInputManagedObject(autoWire.getQualifiedType()), input);

			// First managed object source is bound
			input.setBoundOfficeFloorManagedObjectSource(source);

			// Record the input managed object into the office
			final OfficeObject object = this.createMock(OfficeObject.class);
			this.recordReturn(this.office, this.office
					.getDeployedOfficeObject(autoWire.getQualifiedType()),
					object);
			this.deployer.link(object, input);
			this.objects.put(autoWire, object);
		}

		// Link the source to input
		this.deployer.link(source, input);

		// Return the input managed object
		return input;
	}

	/**
	 * Record the {@link OfficeObject} for the {@link OfficeFloorManagedObject}.
	 */
	private void recordOfficeObject(OfficeFloorManagedObject managedObject,
			AutoWire autoWire) {
		// Record the managed object into the office
		final OfficeObject object = this.createMock(OfficeObject.class);
		this.recordReturn(this.office, this.office
				.getDeployedOfficeObject(autoWire.getQualifiedType()), object);
		this.deployer.link(object, managedObject);
		this.objects.put(autoWire, object);
	}

	/**
	 * Records obtaining the {@link ManagedObjectDependencyType} instances from
	 * the {@link ManagedObjectType}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param dependencyNameAutoWirePairs
	 *            {@link ManagedObjectDependencyType} name/{@link AutoWire}
	 *            pairs.
	 */
	private void recordManagedObjectDependencies(AutoWireObject object,
			Object... dependencyNameAutoWirePairs) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = this.managedObjectTypes
				.get(object);
		assertNotNull("Unknown object", managedObjectType);

		// Record obtaining the dependency types
		ManagedObjectDependencyType<?>[] dependencyTypes = new ManagedObjectDependencyType<?>[dependencyNameAutoWirePairs.length / 2];
		this.recordReturn(managedObjectType,
				managedObjectType.getDependencyTypes(), dependencyTypes);

		try {
			// Load dependencies and return names/auto-wiring
			for (int i = 0; i < dependencyNameAutoWirePairs.length; i += 2) {
				ManagedObjectDependencyType<?> dependencyType = this
						.createMock(ManagedObjectDependencyType.class);
				dependencyTypes[i / 2] = dependencyType;

				// Obtain the dependency details
				String dependencyName = (String) dependencyNameAutoWirePairs[i];
				AutoWire dependencyAutoWire = (AutoWire) dependencyNameAutoWirePairs[i + 1];
				Class<?> dependencyClass = Thread.currentThread()
						.getContextClassLoader()
						.loadClass(dependencyAutoWire.getType());

				// Record the dependency
				this.recordReturn(dependencyType,
						dependencyType.getDependencyName(), dependencyName);
				this.recordReturn(dependencyType,
						dependencyType.getTypeQualifier(),
						dependencyAutoWire.getQualifier());
				this.recordReturn(dependencyType,
						dependencyType.getDependencyType(), dependencyClass);
			}
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Records linking a {@link ManagedObject} dependency.
	 */
	private void recordLinkManagedObjectDependency(AutoWire autoWire,
			String managedObjectDependencyName, AutoWire dependencyAutoWire) {

		// Obtain the managed object
		OfficeFloorManagedObject mo = this.managedObjects.get(autoWire);
		assertNotNull(
				"No managed object for type " + autoWire.getQualifiedType(), mo);

		// Record the dependency
		final ManagedObjectDependency dependency = this
				.createMock(ManagedObjectDependency.class);
		this.recordReturn(mo,
				mo.getManagedObjectDependency(managedObjectDependencyName),
				dependency);

		// Link dependency
		this.recordLinkDependency(dependency, dependencyAutoWire);
	}

	/**
	 * Records an {@link InputManagedObject} dependency.
	 */
	private void recordInputManagedObjectDependency(
			OfficeFloorManagedObjectSource source,
			String managedObjectDependencyName, AutoWire dependencyAutoWire) {

		// Record the dependency
		final ManagedObjectDependency dependency = this
				.createMock(ManagedObjectDependency.class);
		this.recordReturn(source, source
				.getInputManagedObjectDependency(managedObjectDependencyName),
				dependency);

		// Link dependency
		this.recordLinkDependency(dependency, dependencyAutoWire);
	}

	/**
	 * Link dependency.
	 */
	private void recordLinkDependency(ManagedObjectDependency dependency,
			AutoWire dependencyAutoWire) {

		// Obtain the potential dependencies
		OfficeFloorManagedObject dependencyMo = this.managedObjects
				.get(dependencyAutoWire);
		OfficeFloorInputManagedObject dependencyInput = this.inputManagedObjects
				.get(dependencyAutoWire);

		// Link to dependency
		if (dependencyMo != null) {
			// Link dependency to managed object
			this.deployer.link(dependency, dependencyMo);

		} else if (dependencyInput != null) {
			// Link dependency to input managed object
			this.deployer.link(dependency, dependencyInput);

		} else {
			fail("No managed object dependency for type "
					+ dependencyAutoWire.getQualifiedType());
		}
	}

	/**
	 * Records the {@link ManagedObjectFlow}.
	 */
	private void recordManagedObjectFlow(OfficeFloorManagedObjectSource source,
			String managedObjectFlowName, String sectionName,
			String sectionInputName) {

		// Obtain the managed object flow
		final ManagedObjectFlow flow = this.createMock(ManagedObjectFlow.class);
		this.recordReturn(source,
				source.getManagedObjectFlow(managedObjectFlowName), flow);

		// Obtain the section input
		final DeployedOfficeInput sectionInput = this
				.createMock(DeployedOfficeInput.class);
		this.recordReturn(this.office, this.office.getDeployedOfficeInput(
				sectionName, sectionInputName), sectionInput);

		// Link
		this.deployer.link(flow, sectionInput);
	}

	/**
	 * Records the {@link ManagedObjectTeam}.
	 */
	private <S extends TeamSource> void recordManagedObjectTeam(
			OfficeFloorManagedObjectSource source,
			String managedObjectTeamName, Class<S> teamSourceClass,
			String... propertyNameValues) {

		// Obtain the managed object team
		final ManagedObjectTeam moTeam = this
				.createMock(ManagedObjectTeam.class);
		this.recordReturn(source,
				source.getManagedObjectTeam(managedObjectTeamName), moTeam);

		// Register the team
		final OfficeFloorTeam team = this.createMock(OfficeFloorTeam.class);
		this.recordReturn(source,
				source.getOfficeFloorManagedObjectSourceName(), "TestName");
		this.recordReturn(this.deployer, this.deployer.addTeam("TestName-"
				+ managedObjectTeamName, teamSourceClass.getName()), team);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			team.addProperty(name, value);
		}

		// Link team
		this.deployer.link(moTeam, team);
	}

	/**
	 * Records the {@link ManagedObjectTeam}.
	 */
	private void recordManagedObjectTeam(OfficeFloorManagedObjectSource source,
			String managedObjectTeamName, AutoWire autoWire) {

		// Obtain the managed object team
		final ManagedObjectTeam moTeam = this
				.createMock(ManagedObjectTeam.class);
		this.recordReturn(source,
				source.getManagedObjectTeam(managedObjectTeamName), moTeam);

		// Obtain the team to link
		OfficeFloorTeam officeFloorTeam = this.teams.get(autoWire);

		// Record linking team
		this.deployer.link(moTeam, officeFloorTeam);
	}

	/**
	 * Records the {@link Team}.
	 * 
	 * @param propertyNameValuePairs
	 *            Name value pairs. May be <code>null</code> to indicate no
	 *            properties.
	 * @param autoWiring
	 *            {@link AutoWire} instances.
	 * @return {@link OfficeFloorTeam}.
	 */
	private OfficeFloorTeam recordTeam(String[] propertyNameValuePairs,
			AutoWire... autoWiring) {

		// Base name of first auto-wire
		AutoWire nameAutoWire = autoWiring[0];

		// Create the Office Floor team
		OfficeFloorTeam officeFloorTeam = this
				.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer, this.deployer.addTeam("team-"
				+ nameAutoWire.getQualifiedType(),
				OnePersonTeamSource.class.getName()), officeFloorTeam);
		if (propertyNameValuePairs != null) {
			for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
				String name = propertyNameValuePairs[i];
				String value = propertyNameValuePairs[i + 1];
				officeFloorTeam.addProperty(name, value);
			}
		}

		// Create and link the responsibilities, along with registering teams
		for (AutoWire autoWire : autoWiring) {
			OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
			this.recordReturn(
					this.office,
					this.office.getDeployedOfficeTeam("team-"
							+ autoWire.getQualifiedType()), officeTeam);
			this.deployer.link(officeTeam, officeFloorTeam);

			// Register the team
			this.teams.put(autoWire, officeFloorTeam);
		}

		// Return the team
		return officeFloorTeam;
	}

	/**
	 * Mock raw type.
	 */
	public static interface MockRawType {
	}

	/**
	 * Mock raw object.
	 */
	public static class MockRawObject implements MockRawType {
	}

}