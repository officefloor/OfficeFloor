/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeTeam;
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
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.threadlocal.ThreadLocalDelegateManagedObjectSource;
import net.officefloor.plugin.threadlocal.ThreadLocalDelegateOfficeSource;

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

	@Override
	protected void setUp() throws Exception {
		// Reset for testing
		ThreadLocalDelegateOfficeSource.unbindDelegates();
		ThreadLocalDelegateManagedObjectSource.unbindDelegates();
	}

	/**
	 * Ensure can load simple case of just the {@link Office}.
	 */
	public void testSimple() throws Exception {

		// Record
		this.recordTeamAndOffice();

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can only add one raw object dependency per type.
	 */
	public void testOnlyOneRawObjectPerType() {

		// First is valid
		this.source.addObject(String.class, "valid");

		try {
			// Second of same type not valid
			this.source.addObject(String.class, "invalid for second by type");
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Object of type java.lang.String already added",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can load with a raw dependency.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testRawObjectDependency() throws Throwable {

		final MockRawObject dependency = new MockRawObject();

		// Add the raw object dependency
		this.source.addObject(MockRawType.class, dependency);

		// Record
		this.recordTeamAndOffice();
		this.recordRawObjectDependency(MockRawType.class);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();

		// Ensure able to obtain raw dependency
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				ThreadLocalDelegateManagedObjectSource.PROPERTY_INSTANCE_IDENTIFIER,
				"0");
		ThreadLocalDelegateManagedObjectSource source = (ThreadLocalDelegateManagedObjectSource) loader
				.loadManagedObjectSource((Class) ThreadLocalDelegateManagedObjectSource.class);
		ManagedObject mo = new ManagedObjectUserStandAlone()
				.sourceManagedObject(source);
		assertSame("Incorrect raw object", dependency, mo.getObject());
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource}.
	 */
	public void testManagedObjectSource() throws Exception {

		// Add the managed object source
		PropertyList properties = this.source.addObject(MockRawType.class,
				ClassManagedObjectSource.class, null);
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				MockRawObject.class.getName());

		// Record
		this.recordTeamAndOffice();
		this.recordManagedObjectSource(MockRawType.class,
				ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordManagedObject(MockRawType.class);

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link ManagedObjectSource}.
	 */
	public void testWireManagedObject() throws Exception {

		// Record Managed Object Source
		this.recordTeamAndOffice();
		this.recordRawObjectDependency(Connection.class);
		this.recordManagedObjectSource(MockRawType.class,
				ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordManagedObject(MockRawType.class);
		this.recordDependency(MockRawType.class, "dependency", Connection.class);
		this.recordFlow(MockRawType.class, "flow", "section", "sectionInput");
		this.recordTeam(MockRawType.class, "team", OnePersonTeamSource.class,
				"name", "value");

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Map dependency
				context.mapDependency("dependency", Connection.class);

				// Map flow
				context.mapFlow("flow", "section", "sectionInput");

				// Map team
				context.mapTeam("team", OnePersonTeamSource.class)
						.addProperty("name").setValue("value");
			}
		};

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(Connection.class, connection);

		// Add the wiring of the managed object source
		PropertyList properties = this.source.addObject(MockRawType.class,
				ClassManagedObjectSource.class, wirer);
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				MockRawObject.class.getName());

		// Test
		this.replayMockObjects();
		this.source.sourceOfficeFloor(this.deployer, this.context);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can wire {@link OfficeFloorInputManagedObject}.
	 */
	public void testWireInputManagedObject() throws Exception {

		// Record Managed Object Source
		this.recordTeamAndOffice();
		this.recordRawObjectDependency(Connection.class);
		this.recordManagedObjectSource(MockRawType.class,
				ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(MockRawType.class);
		this.recordInputDependency(MockRawType.class, "dependency",
				Connection.class);
		this.recordFlow(MockRawType.class, "flow", "section", "sectionInput");
		this.recordTeam(MockRawType.class, "team", OnePersonTeamSource.class,
				"name", "value");

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
				context.mapTeam("team", OnePersonTeamSource.class)
						.addProperty("name").setValue("value");
			}
		};

		// Provide the dependency object
		final Connection connection = this.createMock(Connection.class);
		this.source.addObject(Connection.class, connection);

		// Add the wiring of the managed object source
		PropertyList properties = this.source.addObject(MockRawType.class,
				ClassManagedObjectSource.class, wirer);
		properties.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(
				MockRawObject.class.getName());

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
	 * {@link OfficeFloorManagedObjectSource} instances by type.
	 */
	private final Map<Class<?>, OfficeFloorManagedObjectSource> managedObjectSources = new HashMap<Class<?>, OfficeFloorManagedObjectSource>();

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
	 * Instance index of the {@link ManagedObjectSource}.
	 */
	private int mosInstanceIndex = 0;

	/**
	 * Records the team and office.
	 */
	private void recordTeamAndOffice() {

		// Record the team
		this.recordReturn(
				this.deployer,
				this.deployer.addTeam("team",
						ProcessContextTeamSource.class.getName()), this.team);

		// Record the office
		this.recordReturn(this.deployer, this.deployer.addDeployedOffice(
				"OFFICE", ThreadLocalDelegateOfficeSource.class.getName(), ""),
				this.office);
		this.office
				.addProperty(
						ThreadLocalDelegateManagedObjectSource.PROPERTY_INSTANCE_IDENTIFIER,
						"0");

		// Record binding office team to team
		OfficeTeam officeTeam = this.createMock(OfficeTeam.class);
		this.recordReturn(this.office,
				this.office.getDeployedOfficeTeam("team"), officeTeam);
		this.deployer.link(officeTeam, this.team);
	}

	/**
	 * Records raw object dependency.
	 */
	private void recordRawObjectDependency(Class<?> type) {

		// Record the managed object source
		this.recordManagedObjectSource(
				type,
				ThreadLocalDelegateManagedObjectSource.class,
				ThreadLocalDelegateManagedObjectSource.PROPERTY_INSTANCE_IDENTIFIER,
				String.valueOf(this.mosInstanceIndex++));

		// Record the managed object
		this.recordManagedObject(type);
	}

	/**
	 * Records a {@link ManagedObjectSource}.
	 */
	private void recordManagedObjectSource(Class<?> type,
			Class<?> managedObjectSourceClass, String... propertyNameValues) {

		final OfficeFloorManagedObjectSource source = this
				.createMock(OfficeFloorManagedObjectSource.class);

		// Record the managed object source
		this.recordReturn(this.deployer, this.deployer.addManagedObjectSource(
				type.getName(), managedObjectSourceClass.getName()), source);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			source.addProperty(name, value);
		}

		// Register the managed object source
		this.managedObjectSources.put(type, source);

		// Have managed by office
		ManagingOffice managingOffice = this.createMock(ManagingOffice.class);
		this.recordReturn(source, source.getManagingOffice(), managingOffice);
		this.deployer.link(managingOffice, this.office);
	}

	/**
	 * Obtains the {@link OfficeFloorManagedObjectSource} for the type.
	 */
	private OfficeFloorManagedObjectSource getManagedObjectSource(Class<?> type) {

		// Obtain the managed object source
		OfficeFloorManagedObjectSource source = this.managedObjectSources
				.get(type);
		assertNotNull("No managed object source for type " + type.getName(),
				source);

		// Return the managed object source
		return source;
	}

	/**
	 * Records the {@link ManagedObject}.
	 */
	private void recordManagedObject(Class<?> type) {

		// Obtain the managed object source
		OfficeFloorManagedObjectSource source = this
				.getManagedObjectSource(type);

		// Record the managed object
		final OfficeFloorManagedObject mo = this
				.createMock(OfficeFloorManagedObject.class);
		this.recordReturn(source, source.addOfficeFloorManagedObject(
				type.getName(), ManagedObjectScope.PROCESS), mo);
		this.managedObjects.put(type, mo);

		// Record the managed object into the office
		final OfficeObject object = this.createMock(OfficeObject.class);
		this.recordReturn(this.office,
				this.office.getDeployedOfficeObject(type.getName()), object);
		this.deployer.link(object, mo);
		this.objects.put(type, object);
	}

	/**
	 * Records the {@link OfficeFloorInputManagedObject}.
	 */
	private void recordInputManagedObject(Class<?> type) {

		// Obtain the managed object source
		OfficeFloorManagedObjectSource source = this
				.getManagedObjectSource(type);

		// Input Managed Object
		final OfficeFloorInputManagedObject input = this
				.createMock(OfficeFloorInputManagedObject.class);
		this.recordReturn(this.deployer,
				this.deployer.addInputManagedObject(type.getName()), input);

		// Link the source to input
		this.deployer.link(source, input);
		input.setBoundOfficeFloorManagedObjectSource(source);
		this.inputManagedObjects.put(type, input);

		// Record the managed object into the office
		final OfficeObject object = this.createMock(OfficeObject.class);
		this.recordReturn(this.office,
				this.office.getDeployedOfficeObject(type.getName()), object);
		this.deployer.link(object, input);
		this.objects.put(type, object);
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
	private void recordInputDependency(Class<?> type,
			String managedObjectDependencyName, Class<?> dependencyType) {

		// Obtain the managed object source
		OfficeFloorManagedObjectSource source = this
				.getManagedObjectSource(type);

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
	private void recordFlow(Class<?> type, String managedObjectFlowName,
			String sectionName, String sectionInputName) {

		// Obtain the managed object source
		OfficeFloorManagedObjectSource source = this
				.getManagedObjectSource(type);

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
	private <S extends TeamSource> void recordTeam(Class<?> type,
			String managedObjectTeamName, Class<S> teamSourceClass,
			String... propertyNameValues) {

		// Obtain the managed object source
		OfficeFloorManagedObjectSource source = this
				.getManagedObjectSource(type);

		// Obtain the managed object team
		final ManagedObjectTeam moTeam = this
				.createMock(ManagedObjectTeam.class);
		this.recordReturn(source,
				source.getManagedObjectTeam(managedObjectTeamName), moTeam);

		// Register the team
		final OfficeFloorTeam team = this.createMock(OfficeFloorTeam.class);
		this.recordReturn(
				this.deployer,
				this.deployer.addTeam(type.getName() + "-"
						+ managedObjectTeamName, teamSourceClass.getName()),
				team);
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			team.addProperty(name, value);
		}

		// Link team
		this.deployer.link(moTeam, team);
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