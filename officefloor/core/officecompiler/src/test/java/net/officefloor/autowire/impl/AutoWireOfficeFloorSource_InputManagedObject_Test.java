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

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireObject;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.autowire.ManagedObjectSourceWirer;
import net.officefloor.autowire.ManagedObjectSourceWirerContext;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeamSource;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link OfficeFloorInputManagedObject} configuration of the
 * {@link AutoWireOfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorSource_InputManagedObject_Test extends
		AbstractAutoWireOfficeFloorSourceTestCase {

	/**
	 * Ensure can wire {@link OfficeFloorInputManagedObject}.
	 */
	public void testInputManagedObject() throws Exception {

		// Providing wiring indicating input managed object
		ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "section", "sectionInput");
			}
		};

		final AutoWire autoWire = new AutoWire(MockRawType.class);

		// Add the wiring of first input managed object
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, autoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record team and office
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
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire qualified {@link OfficeFloorInputManagedObject}.
	 */
	public void testQualifiedInputManagedObject() throws Exception {

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
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure issue if multiple {@link AutoWire} instances for
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testMultipleAutoWiredInputManagedObjectIssue() throws Exception {

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
		this.registerManagedObjectFlowType(object, "flow");
		this.recordManagedObjectType(object);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice();
		this.deployer.addIssue("OfficeFloorInputManagedObject "
				+ rawTypeAutoWire.getQualifiedType()
				+ " must have only one AutoWire");
		// Not use the input managed object (even though it can be handled)

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can build multiple {@link OfficeFloorInputManagedObject} for same
	 * {@link AutoWire}.
	 */
	public void testMultipleInputManagedObjectsForSameAutoWire()
			throws Exception {

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
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire a {@link ManagedObjectFlow} to
	 * {@link DeployedOfficeInput}.
	 */
	public void testInputManagedObjectFlowHandled() throws Exception {

		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "section", "sectionInput");
			}
		};

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer,
				rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source (with non-specific dependency types)
		this.registerManagedObjectFlowType(object, "flow");
		this.recordManagedObjectType(object);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice();
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordManagedObjectFlow(source, "flow", "section", "sectionInput");

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure not include unhandled {@link OfficeFloorInputManagedObject}.
	 */
	public void testInputManagedObjectFlowUnhandled() throws Exception {

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
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.recordOffice();
		// Not handled input, so not load managed objects

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure not include {@link OfficeFloorInputManagedObject} with only some
	 * handled flows.
	 */
	public void testInputManagedObjectFlowsPartiallyHandled() throws Exception {

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
		this.recordRawObjectType(connection);
		this.recordManagedObjectType(object);
		this.registerOfficeInput("handled", "input");
		this.recordOffice();
		// Not all inputs handled, so not load managed objects

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObjectTeam} to supplied
	 * {@link OfficeFloorTeam}.
	 */
	public void testInputManagedObjectWithSuppliedTeam() throws Exception {

		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map supplied team
				AutoWireTeam suppliedTeam = context.mapTeam("team",
						OnePersonTeamSource.class.getName());
				suppliedTeam.addProperty("name", "value");
			}
		};

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer,
				rawTypeAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record
		this.registerManagedObjectTeamType(object, "team");
		this.recordManagedObjectType(object);
		this.recordOffice(); // no flows but team so should be handled
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordManagedObjectTeam(source, "team", OnePersonTeamSource.class,
				"name", "value");

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectTeam} to the default
	 * {@link OfficeFloorTeam}.
	 */
	public void testManagedObjectTeamWiredToDefaultTeam() throws Exception {

		final AutoWire moAutoWire = new AutoWire(MockRawObject.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map team via auto-wire
				context.mapTeam("team", new AutoWire(MockRawType.class));
			}
		};

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, moAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Record Managed Object Source
		this.registerManagedObjectTeamType(object, "team");
		this.recordManagedObjectType(object);
		this.recordOffice(); // no flows so office can handle
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				moAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, moAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordDefaultTeam();
		this.recordManagedObjectTeam(source, "team", DEFAULT_TEAM);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can auto-wire {@link ManagedObjectTeam} to a
	 * {@link OfficeFloorTeam}.
	 */
	public void testManagedObjectTeamWiredToTeam() throws Exception {

		final AutoWire teamAutoWire = new AutoWire(MockRawType.class);
		final AutoWire moAutoWire = new AutoWire(MockRawObject.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				// Map team via auto-wire
				context.mapTeam("team", teamAutoWire);
			}
		};

		// Add the wiring of the managed object source
		AutoWireObject object = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), wirer, moAutoWire);
		object.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Add the team
		this.source.assignTeam(LeaderFollowerTeamSource.class.getName(),
				teamAutoWire);

		// Record Managed Object Source
		this.registerManagedObjectTeamType(object, "team");
		this.recordManagedObjectType(object);
		this.recordOffice(); // no flows so office can handle
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				moAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, moAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordTeam(LeaderFollowerTeamSource.class, teamAutoWire);
		this.recordManagedObjectTeam(source, "team", teamAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can auto-wire qualified {@link ManagedObjectTeam} to qualified
	 * {@link OfficeFloorTeam}.
	 */
	public void testQualifiedManagedObjectTeamWiredToQualifiedTeam()
			throws Exception {

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
		this.source.assignTeam(LeaderFollowerTeamSource.class.getName(),
				qualifiedTeamAutoWire);
		this.source.assignTeam(OnePersonTeamSource.class.getName(),
				unqualifiedTeamAutoWire);

		// Record Managed Object Source
		this.registerManagedObjectTeamType(object, "team");
		this.recordManagedObjectType(object);
		this.recordOffice(); // no flows so office can handle
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				moAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, moAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordTeam(LeaderFollowerTeamSource.class, qualifiedTeamAutoWire);
		this.recordManagedObjectTeam(source, "team", qualifiedTeamAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can auto-wire qualified {@link ManagedObjectTeam} to non-qualified
	 * {@link OfficeFloorTeam}.
	 */
	public void testQualifiedManagedObjectTeamWiredToTeam() throws Exception {

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
		this.registerManagedObjectTeamType(object, "team");
		this.recordManagedObjectType(object);
		this.recordOffice(moAutoWire);
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				moAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, moAutoWire);
		this.recordManagedObjectDependencies(object);
		this.recordTeam(OnePersonTeamSource.class, unqualifiedTeamAutoWire);
		this.recordManagedObjectTeam(source, "team", unqualifiedTeamAutoWire);

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObjectDependency}.
	 */
	public void testInputManagedObjectDependency() throws Exception {

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Map dependency (providing more specific type than Object)
				context.mapDependency("dependency", connectionAutoWire);

				// Map flow
				context.mapFlow("flow", "section", "sectionInput");
			}
		};

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
		this.registerManagedObjectFlowType(object, "flow");
		this.recordManagedObjectType(object);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice();
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				connectionAutoWire);
		this.recordRawObject(connection, connectionAutoWire);
		this.recordLinkInputManagedObjectDependency(source, "dependency",
				connectionAutoWire);
		this.recordManagedObjectFlow(source, "flow", "section", "sectionInput");

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure can wire {@link ManagedObjectDependency} with overridden
	 * dependency mapping.
	 */
	public void testInputManagedObjectDependencyWithOverriddenDependencyMapping()
			throws Exception {

		final AutoWire connectionAutoWire = new AutoWire(Connection.class);
		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);

		// Create wirer
		final ManagedObjectSourceWirer wirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {

				// Map dependency (providing more specific type than Object)
				context.mapDependency("dependency", connectionAutoWire);

				// Map flow
				context.mapFlow("flow", "section", "sectionInput");
			}
		};

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
		this.registerManagedObjectFlowType(object, "flow");
		this.recordManagedObjectType(object);
		this.registerOfficeInput("section", "sectionInput");
		this.recordOffice();
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(object, "dependency",
				new AutoWire(Object.class)); // auto-wire to be overridden
		this.recordRawObject(connection, connectionAutoWire);
		this.recordLinkInputManagedObjectDependency(source, "dependency",
				connectionAutoWire);
		this.recordManagedObjectFlow(source, "flow", "section", "sectionInput");

		// Test
		this.doSourceOfficeFloorTest();
	}

	/**
	 * Ensure issue if {@link ManagedObject} depends on an unhandled
	 * {@link OfficeFloorInputManagedObject} (ie not loaded).
	 */
	public void testManagedObjectDependsOnUnhandledInputManagedObject()
			throws Exception {

		final AutoWire rawTypeAutoWire = new AutoWire(MockRawType.class);
		final AutoWire connectionAutoWire = new AutoWire(Connection.class);

		// Add the wiring of the handled managed object
		final ManagedObjectSourceWirer handledWirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "handled", "input");
			}
		};
		AutoWireObject handled = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), handledWirer,
				rawTypeAutoWire);
		handled.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());

		// Add the wiring of the unhandled managed object
		final ManagedObjectSourceWirer unhandledWirer = new ManagedObjectSourceWirer() {
			@Override
			public void wire(ManagedObjectSourceWirerContext context) {
				context.mapFlow("flow", "unhandled", "input");
			}
		};
		AutoWireObject unhandled = this.source.addManagedObject(
				ClassManagedObjectSource.class.getName(), unhandledWirer,
				connectionAutoWire);
		unhandled.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				Connection.class.getName());

		// Record
		this.registerManagedObjectFlowType(handled, "flow");
		this.recordManagedObjectType(handled);
		this.registerManagedObjectFlowType(unhandled, "flow");
		this.recordManagedObjectType(unhandled);
		this.registerOfficeInput("handled", "input");
		this.recordOffice(); // handled input managed object always loaded
		OfficeFloorManagedObjectSource source = this.recordManagedObjectSource(
				rawTypeAutoWire, ClassManagedObjectSource.class, 0, 0,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockRawObject.class.getName());
		this.recordInputManagedObject(source, rawTypeAutoWire);
		this.recordManagedObjectDependencies(handled, "dependency",
				connectionAutoWire);

		// Record issue linking to unhandled InputManagedObject
		final ManagedObjectDependency moDependency = this
				.createMock(ManagedObjectDependency.class);
		this.recordReturn(source,
				source.getInputManagedObjectDependency("dependency"),
				moDependency);
		this.deployer
				.addIssue("May only depend on OfficeFloorInputManagedObject "
						+ connectionAutoWire.getQualifiedType()
						+ " if all of its flows are handled");

		// Record linking flow
		this.recordManagedObjectFlow(source, "flow", "handled", "input");

		// Test
		this.doSourceOfficeFloorTest();
	}

}