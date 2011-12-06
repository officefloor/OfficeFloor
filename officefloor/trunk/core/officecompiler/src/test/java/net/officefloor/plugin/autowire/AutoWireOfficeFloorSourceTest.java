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

package net.officefloor.plugin.autowire;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
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
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link AutoWireOfficeFloorSource} to test.
	 */
	private final AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();

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
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if provided object type.
	 */
	public void testEnsureProvideObjectType() {
		ManagedObjectSourceWirer wirer = this
				.createMock(ManagedObjectSourceWirer.class);
		try {
			this.source.addManagedObject(ClassManagedObjectSource.class, wirer);
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause",
					"Must provide at least one object type", ex.getMessage());
		}
	}

	/**
	 * Ensure can load with a raw object.
	 */
	public void testRawObject() throws Throwable {

		final MockRawObject dependency = new MockRawObject();

		// Record
		this.recordTeam();
		this.recordRawObjectType(dependency);
		this.recordOffice();
		this.recordRawObjectDependency(dependency, MockRawType.class);

		// Test
		this.replayMockObjects();

		// Add the raw object dependency
		this.source.addObject(dependency, MockRawType.class);
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load with a raw dependency that defaults the type..
	 */
	public void testRawObjectDefaultType() throws Throwable {

		final Object object = "default type to String";

		// Record
		this.recordTeam();
		this.recordRawObjectType(object);
		this.recordOffice();
		this.recordRawObjectDependency(object, String.class);

		// Test
		this.replayMockObjects();

		// Add the raw object dependency to default type
		this.source.addObject(object);
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObject}.
	 */
	public void testManagedObject() throws Exception {

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, null, MockRawType.class);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice();
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify the timeout to source the {@link ManagedObject}.
	 */
	public void testManagedObjectSourceTimeout() throws Exception {

		final long TIMEOUT = 100;

		// Add the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, null, MockRawType.class);
		object.setTimeout(TIMEOUT);

		// Record
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice();
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, TIMEOUT);
		this.recordManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load properties from class path.
	 */
	public void testProperties() throws Exception {

		// Ensure no environment override so load from class path
		System.clearProperty(AutoWireProperties.ENVIRONMENT_PROPERTIES_DIRECTORY);

		// Add the managed object loading properties from class path
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, null, MockRawType.class);
		object.loadProperties(this.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/object.properties");

		// Record
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice();
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				"class.path.property", "available");
		this.recordManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
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

		// Add the managed object loading properties from class path
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, null, MockRawType.class);
		object.loadProperties("object.properties");

		// Record
		this.recordTeam();
		this.recordManagedObjectType(object);
		this.recordOffice();
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				"class.path.property", "available");
		this.recordManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObject} against multiple types.
	 */
	public void testObjectForMultipleTypes() throws Exception {

		final MockRawObject object = new MockRawObject();

		// Record
		this.recordTeam();
		this.recordRawObjectType(object);
		this.recordOffice();
		this.recordRawObjectDependency(object, MockRawType.class,
				MockRawObject.class);

		// Test
		this.replayMockObjects();

		// Add raw object with multiple types
		this.source.addObject(object, MockRawType.class, MockRawObject.class);
		this.source.sourceOfficeFloor(this.deployer, this.context);
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

				// Map dependency
				context.mapDependency("dependency", Connection.class);

				// Map flow
				context.mapFlow("flow", "section", "sectionInput");

				// Map team
				context.mapTeam("team", OnePersonTeamSource.class).addProperty(
						"name", "value");
			}
		};

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, Connection.class);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, wirer, MockRawType.class);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source (with non-specific dependency types)
		this.recordTeam();
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice();
		this.recordRawObjectDependency(connection, Connection.class);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object, "dependency", Object.class);
		this.recordDependency(MockRawType.class, "dependency", Connection.class);
		this.recordFlow(source, "flow", "section", "sectionInput");
		this.recordTeam(source, MockRawType.class, "team",
				OnePersonTeamSource.class, "name", "value");

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire object to multiple dependencies.
	 */
	public void testObjectWiredToMultipleDependencies() throws Exception {

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map dependencies
				context.mapDependency("connection", Connection.class);
				context.mapDependency("dataSource", DataSource.class);
			}
		};

		// Provide the dependencies
		final Connection connection = this.createMock(Connection.class);
		final DataSource dataSource = this.createMock(DataSource.class);
		this.source.addObject(connection, Connection.class);
		this.source.addObject(dataSource, DataSource.class);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, wirer, MockRawType.class);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source (with non-specific dependency types)
		this.recordTeam();
		this.recordRawObjectType(connection);
		this.recordRawObjectType(dataSource);
		this.recordManagedObjectType(object);
		this.recordOffice();
		this.recordRawObjectDependency(connection, Connection.class);
		this.recordRawObjectDependency(dataSource, DataSource.class);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object, "connection",
				Object.class, "dataSource", Object.class);
		this.recordDependency(MockRawType.class, "connection", Connection.class);
		this.recordDependency(MockRawType.class, "dataSource", DataSource.class);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
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

				// Input
				context.setInput(true);

				// Map dependency
				context.mapDependency("dependency", Connection.class);

				// Map flow
				context.mapFlow("flow", "section", "sectionInput");

				// Map team
				context.mapTeam("team", OnePersonTeamSource.class).addProperty(
						"name", "value");
			}
		};

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, Connection.class);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, wirer, MockRawType.class);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.recordTeam();
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice();
		this.recordRawObjectDependency(connection, Connection.class);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object, "dependency",
				Connection.class);
		this.recordInputDependency(source, "dependency", Connection.class);
		this.recordFlow(source, "flow", "section", "sectionInput");
		this.recordTeam(source, MockRawType.class, "team",
				OnePersonTeamSource.class, "name", "value");

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
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
				context.setInput(true);
			}
		};

		// Add the wiring of first input managed object
		AutoWireObject firstObject = this.source.addManagedObject(
				ClassManagedObjectSource.class, wirer, MockRawType.class);
		firstObject.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Add the wiring of second input managed object
		AutoWireObject secondObject = this.source.addManagedObject(
				ClassManagedObjectSource.class, wirer, MockRawType.class);
		secondObject.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record team and office
		this.recordTeam();
		this.recordManagedObjectType(firstObject);
		this.recordManagedObjectType(secondObject);
		this.recordOffice();

		// Record the first input managed object
		OfficeFloorManagedObjectSource mosOne = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(mosOne, MockRawType.class);
		this.recordManagedObjectDependencies(firstObject);

		// Record the second input managed object
		OfficeFloorManagedObjectSource mosTwo = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 1, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(mosTwo, MockRawType.class);
		this.recordManagedObjectDependencies(secondObject);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource} based on its
	 * {@link ManagedObjectType}.
	 */
	public void testWireManagedObjectBasedOnDependencyType() throws Exception {

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(connection, Connection.class);

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class, null, MockRawType.class);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.recordTeam();
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice();
		this.recordRawObjectDependency(connection, Connection.class);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				MockRawType.class, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordManagedObject(source, MockRawType.class);
		this.recordManagedObjectDependencies(object, "dependency",
				Connection.class);
		this.recordDependency(MockRawType.class, "dependency", Connection.class);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure the added object is available.
	 */
	public void testObjectAvailable() throws Exception {

		// Add the Object
		this.source.addObject("TEST", String.class);

		// Test
		this.replayMockObjects();
		assertTrue("Added type should be available",
				this.source.isObjectAvailable(String.class));
		assertFalse("Type not added should not be available",
				this.source.isObjectAvailable(Integer.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure the added {@link ManagedObject} is available.
	 */
	public void testManagedObjectAvailable() throws Exception {

		// Add the Managed Object
		this.source.addManagedObject(ClassManagedObjectSource.class, null,
				String.class);

		// Test
		this.replayMockObjects();
		assertTrue("Added type should be available",
				this.source.isObjectAvailable(String.class));
		assertFalse("Type not added should not be available",
				this.source.isObjectAvailable(Integer.class));
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to assign a {@link Team}.
	 */
	public void testAssignTeam() throws Exception {

		// Assign the team
		AutoWireTeam team = this.source.assignTeam(OnePersonTeamSource.class,
				Connection.class);
		team.addProperty("name", "value");

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(new String[] { "name", "value" }, Connection.class);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to assign a {@link Team} multiple responsibilities.
	 */
	public void testAssignTeamMultipleResponsibilities() throws Exception {

		// Assign the team with multiple responsibilities
		this.source.assignTeam(OnePersonTeamSource.class, Connection.class,
				DataSource.class);

		// Record
		this.recordTeam();
		this.recordOffice();
		this.recordTeam(null, Connection.class, DataSource.class);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to override the default {@link Team}.
	 */
	public void testOverrideDefaultTeam() throws Exception {

		// Assign the default team
		AutoWireTeam defaultTeam = this.source
				.assignDefaultTeam(PassiveTeamSource.class);
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
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * {@link OfficeFloorTeam}.
	 */
	private final OfficeFloorTeam team = this.createMock(OfficeFloorTeam.class);

	/**
	 * {@link DeployedOffice}.
	 */
	private final DeployedOffice office = this.createMock(DeployedOffice.class);

	/**
	 * {@link OfficeFloorManagedObject} instances by type.
	 */
	private final Map<Class<?>, OfficeFloorManagedObject> managedObjects = new HashMap<Class<?>, OfficeFloorManagedObject>();

	/**
	 * {@link OfficeFloorInputManagedObject} instances by type.
	 */
	private final Map<Class<?>, OfficeFloorInputManagedObject> inputManagedObjects = new HashMap<Class<?>, OfficeFloorInputManagedObject>();

	/**
	 * {@link OfficeObject} instances by type.
	 */
	private final Map<Class<?>, OfficeObject> objects = new HashMap<Class<?>, OfficeObject>();

	/**
	 * {@link ManagedObjectType} instances by {@link AutoWireObject}.
	 */
	private final Map<AutoWireObject, ManagedObjectType<?>> managedObjectTypes = new HashMap<AutoWireObject, ManagedObjectType<?>>();

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
	 * Records the {@link Office}.
	 */
	private void recordOffice() {
		this.recordOffice(this.team);
	}

	/**
	 * Records the office.
	 * 
	 * @param defaultTeam
	 *            Default {@link Team}.
	 */
	private void recordOffice(OfficeFloorTeam defaultTeam) {

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

		// Record obtaining extension interfaces
		this.recordReturn(managedObjectType,
				managedObjectType.getExtensionInterfaces(), extensionInterfaces);
	}

	/**
	 * Indicates if the {@link AbstractMatcher} is specified for the raw object.
	 */
	private boolean isRawObjectMatcherSpecified = false;

	/**
	 * Records raw object dependency.
	 */
	private void recordRawObjectDependency(Object dependency, Class<?>... types) {

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record the managed object source
		this.recordReturn(this.deployer, this.deployer.addManagedObjectSource(
				types[0].getName(),
				new SingletonManagedObjectSource(dependency)), source);
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

		// Record the managed object
		this.recordManagedObject(source, types);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	private OfficeFloorManagedObjectSource recordManagedObjectSource(
			Class<?> type, Class<?> managedObjectSourceClass, int typeIndex,
			long timeout, String... propertyNameValues) {

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record the managed object source
		this.recordReturn(this.deployer, this.deployer.addManagedObjectSource(
				type.getName()
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
	 */
	private void recordManagedObject(OfficeFloorManagedObjectSource source,
			Class<?>... types) {

		// Obtain the first type for naming and identification
		Class<?> firstType = types[0];

		// Record the managed object
		final OfficeFloorManagedObject mo = this
				.createMock(OfficeFloorManagedObject.class);
		this.recordReturn(source, source.addOfficeFloorManagedObject(
				firstType.getName(), ManagedObjectScope.PROCESS), mo);
		this.managedObjects.put(firstType, mo);

		// Record the managed object into the office
		for (Class<?> type : types) {
			final OfficeObject object = this.createMock(OfficeObject.class);
			this.recordReturn(this.office,
					this.office.getDeployedOfficeObject(type.getName()), object);
			this.deployer.link(object, mo);
			this.objects.put(type, object);
		}
	}

	/**
	 * Records the {@link OfficeFloorInputManagedObject}.
	 */
	private OfficeFloorInputManagedObject recordInputManagedObject(
			OfficeFloorManagedObjectSource source, Class<?> type) {

		// Record the managed object into the office
		final OfficeObject object = this.createMock(OfficeObject.class);
		this.recordReturn(this.office,
				this.office.getDeployedOfficeObject(type.getName()), object);
		this.objects.put(type, object);

		// Obtain the Input Managed Object
		OfficeFloorInputManagedObject input = this.inputManagedObjects
				.get(type);
		if (input == null) {
			// Create and register the input managed object
			input = this.createMock(OfficeFloorInputManagedObject.class);
			this.inputManagedObjects.put(type, input);
			this.recordReturn(this.deployer,
					this.deployer.addInputManagedObject(type.getName()), input);

			// First managed object source is bound
			input.setBoundOfficeFloorManagedObjectSource(source);

			// Link input office object to input managed object
			this.deployer.link(object, input);
		}

		// Link the source to input
		this.deployer.link(source, input);

		// Return the input managed object
		return input;
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
		this.recordReturn(this.context, this.context.loadManagedObjectType(
				object.getManagedObjectSourceClass().getName(),
				object.getProperties()), managedObjectType);

		// Record obtaining extension interfaces
		this.recordReturn(managedObjectType,
				managedObjectType.getExtensionInterfaces(), extensionInterfaces);

		// Register the managed object type
		this.managedObjectTypes.put(object, managedObjectType);
	}

	/**
	 * Records obtaining the {@link ManagedObjectDependencyType} instances from
	 * the {@link ManagedObjectType}.
	 * 
	 * @param object
	 *            {@link AutoWireObject}.
	 * @param dependencyNameTypePairs
	 *            {@link ManagedObjectDependencyType} name/type pairs.
	 */
	private void recordManagedObjectDependencies(AutoWireObject object,
			Object... dependencyNameTypePairs) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = this.managedObjectTypes
				.get(object);
		assertNotNull("Unknown object", managedObjectType);

		// Record obtaining the dependency types
		ManagedObjectDependencyType<?>[] dependencyTypes = new ManagedObjectDependencyType<?>[dependencyNameTypePairs.length / 2];
		this.recordReturn(managedObjectType,
				managedObjectType.getDependencyTypes(), dependencyTypes);

		// Load dependencies and return names/types
		for (int i = 0; i < dependencyNameTypePairs.length; i += 2) {
			ManagedObjectDependencyType<?> dependencyType = this
					.createMock(ManagedObjectDependencyType.class);
			dependencyTypes[i / 2] = dependencyType;
			this.recordReturn(dependencyType,
					dependencyType.getDependencyName(),
					(String) dependencyNameTypePairs[i]);
			this.recordReturn(dependencyType,
					dependencyType.getDependencyType(),
					(Class<?>) dependencyNameTypePairs[i + 1]);
		}
	}

	/**
	 * Records a dependency.
	 */
	private void recordDependency(Class<?> type,
			String managedObjectDependencyName, Class<?> dependencyType) {

		// Obtain the managed object
		OfficeFloorManagedObject mo = this.managedObjects.get(type);
		assertNotNull("No managed object for type " + type.getName(), mo);

		// Record the dependency
		final ManagedObjectDependency dependency = this
				.createMock(ManagedObjectDependency.class);
		this.recordReturn(mo,
				mo.getManagedObjectDependency(managedObjectDependencyName),
				dependency);

		// Link dependency
		this.recordLinkDependency(dependency, dependencyType);
	}

	/**
	 * Records an input dependency.
	 */
	private void recordInputDependency(OfficeFloorManagedObjectSource source,
			String managedObjectDependencyName, Class<?> dependencyType) {

		// Record the dependency
		final ManagedObjectDependency dependency = this
				.createMock(ManagedObjectDependency.class);
		this.recordReturn(source, source
				.getInputManagedObjectDependency(managedObjectDependencyName),
				dependency);

		// Link dependency
		this.recordLinkDependency(dependency, dependencyType);
	}

	/**
	 * Link dependency.
	 */
	private void recordLinkDependency(ManagedObjectDependency dependency,
			Class<?> dependencyType) {

		// Obtain the potential dependencies
		OfficeFloorManagedObject dependencyMo = this.managedObjects
				.get(dependencyType);
		OfficeFloorInputManagedObject dependencyInput = this.inputManagedObjects
				.get(dependencyType);

		// Link to dependency
		if (dependencyMo != null) {
			// Link dependency to managed object
			this.deployer.link(dependency, dependencyMo);

		} else if (dependencyInput != null) {
			// Link dependency to input managed object
			this.deployer.link(dependency, dependencyInput);

		} else {
			fail("No managed object dependency for type "
					+ dependencyType.getName());
		}
	}

	/**
	 * Records the {@link ManagedObjectFlow}.
	 */
	private void recordFlow(OfficeFloorManagedObjectSource source,
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
	private <S extends TeamSource> void recordTeam(
			OfficeFloorManagedObjectSource source, Class<?> type,
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
	 * Records the {@link Team}.
	 * 
	 * @param propertyNameValuePairs
	 *            Name value pairs. May be <code>null</code> to indicate no
	 *            properties.
	 * @param objectTypes
	 *            Object types.
	 */
	private void recordTeam(String[] propertyNameValuePairs,
			Class<?>... objectTypes) {

		// Base name of first object type
		Class<?> nameObjectType = objectTypes[0];

		// Create the Office Floor team
		OfficeFloorTeam officeFloorTeam = this
				.createMock(OfficeFloorTeam.class);
		this.recordReturn(this.deployer,
				this.deployer.addTeam("team-" + nameObjectType.getName(),
						OnePersonTeamSource.class.getName()), officeFloorTeam);
		if (propertyNameValuePairs != null) {
			for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
				String name = propertyNameValuePairs[i];
				String value = propertyNameValuePairs[i + 1];
				officeFloorTeam.addProperty(name, value);
			}
		}

		// Create and link the responsibilities
		for (Class<?> objectType : objectTypes) {
			OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
			this.recordReturn(
					this.office,
					this.office.getDeployedOfficeTeam("team-"
							+ objectType.getName()), officeTeam);
			this.deployer.link(officeTeam, officeFloorTeam);
		}
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