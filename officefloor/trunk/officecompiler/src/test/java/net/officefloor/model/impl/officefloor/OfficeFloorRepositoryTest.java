/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.officefloor;

import java.sql.Connection;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorRepository;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link OfficeFloorRepository}.
 * 
 * @author Daniel
 */
public class OfficeFloorRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this
			.createMock(ModelRepository.class);

	/**
	 * {@link ConfigurationItem}.
	 */
	private final ConfigurationItem configurationItem = this
			.createMock(ConfigurationItem.class);

	/**
	 * {@link OfficeFloorRepository} to be tested.
	 */
	private final OfficeFloorRepository officeRepository = new OfficeFloorRepositoryImpl(
			this.modelRepository);

	/**
	 * Ensures on retrieving a {@link OfficeFloorModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveOfficeFloor() throws Exception {

		// Create the raw office floor to be connected
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		OfficeFloorManagedObjectSourceModel officeFloorManagedObjectSource = new OfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Connection.class
						.getName());
		officeFloor
				.addOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		OfficeFloorManagedObjectModel officeFloorManagedObject = new OfficeFloorManagedObjectModel(
				"MANAGED_OBJECT", "THREAD");
		officeFloor.addOfficeFloorManagedObject(officeFloorManagedObject);
		OfficeFloorTeamModel officeFloorTeam = new OfficeFloorTeamModel(
				"OFFICE_FLOOR_TEAM", "net.example.ExampleTeamSource");
		officeFloor.addOfficeFloorTeam(officeFloorTeam);
		DeployedOfficeModel office = new DeployedOfficeModel("OFFICE",
				"net.example.ExampleOfficeSource", "OFFICE_LOCATION");
		officeFloor.addDeployedOffice(office);
		DeployedOfficeInputModel officeInput = new DeployedOfficeInputModel(
				"SECTION", "INPUT", Integer.class.getName());
		office.addDeployedOfficeInput(officeInput);
		DeployedOfficeObjectModel officeObject = new DeployedOfficeObjectModel(
				"OBJECT", Connection.class.getName());
		office.addDeployedOfficeObject(officeObject);
		DeployedOfficeTeamModel officeTeam = new DeployedOfficeTeamModel(
				"OFFICE_TEAM");
		office.addDeployedOfficeTeam(officeTeam);

		// office floor managed object -> office floor managed object source
		OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel moToSource = new OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		officeFloorManagedObject.setOfficeFloorManagedObjectSource(moToSource);

		// office floor managed object dependency -> office floor managed object
		OfficeFloorManagedObjectDependencyModel dependency = new OfficeFloorManagedObjectDependencyModel(
				"DEPENDENCY", Connection.class.getName());
		officeFloorManagedObject
				.addOfficeFloorManagedObjectDependency(dependency);
		OfficeFloorManagedObjectModel mo_dependency = new OfficeFloorManagedObjectModel(
				"MO_DEPENDENCY", "THREAD");
		officeFloor.addOfficeFloorManagedObject(mo_dependency);
		OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel dependencyToMo = new OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel(
				"MO_DEPENDENCY");
		dependency.setOfficeFloorManagedObject(dependencyToMo);

		// office floor managed object source -> office
		OfficeFloorManagedObjectSourceToDeployedOfficeModel moSourceToOffice = new OfficeFloorManagedObjectSourceToDeployedOfficeModel(
				"OFFICE", null);
		officeFloorManagedObjectSource.setManagingOffice(moSourceToOffice);

		// office floor managed object source flow -> office input
		OfficeFloorManagedObjectSourceFlowModel flow = new OfficeFloorManagedObjectSourceFlowModel(
				"FLOW", Integer.class.getName());
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceFlow(flow);
		OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel(
				"OFFICE", "SECTION", "INPUT");
		flow.setDeployedOfficeInput(flowToInput);

		// office object -> office floor managed object
		DeployedOfficeObjectToOfficeFloorManagedObjectModel officeObjectToManagedObject = new DeployedOfficeObjectToOfficeFloorManagedObjectModel(
				"MANAGED_OBJECT");
		officeObject.setOfficeFloorManagedObject(officeObjectToManagedObject);

		// office team -> office floor team
		DeployedOfficeTeamToOfficeFloorTeamModel officeTeamToFloorTeam = new DeployedOfficeTeamToOfficeFloorTeamModel(
				"OFFICE_FLOOR_TEAM");
		officeTeam.setOfficeFloorTeam(officeTeamToFloorTeam);

		// Record retrieving the office
		this.recordReturn(this.modelRepository, this.modelRepository.retrieve(
				null, this.configurationItem), officeFloor,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertTrue("Must be office model",
								actual[0] instanceof OfficeFloorModel);
						assertEquals(
								"Incorrect configuration item",
								OfficeFloorRepositoryTest.this.configurationItem,
								actual[1]);
						return true;
					}
				});

		// Retrieve the office floor
		this.replayMockObjects();
		OfficeFloorModel retrievedOfficeFloor = this.officeRepository
				.retrieveOfficeFloor(this.configurationItem);
		this.verifyMockObjects();
		assertEquals("Incorrect office", officeFloor, retrievedOfficeFloor);

		// Ensure office floor managed object connected to its source
		assertEquals(
				"office floor managed object <- office floor managed object source",
				officeFloorManagedObject, moToSource
						.getOfficeFloorManagedObject());
		assertEquals(
				"office floor managed object -> office floor managed object source",
				officeFloorManagedObjectSource, moToSource
						.getOfficeFloorManagedObjectSource());

		// Ensure managed object dependency to managed object
		assertEquals("dependency <- managed object", dependency, dependencyToMo
				.getOfficeFloorManagedObjectDependency());
		assertEquals("dependency -> managed object", mo_dependency,
				dependencyToMo.getOfficeFloorManagedObject());

		// Ensure managed object source connected to managing office
		assertEquals("office floor managed object source <- office",
				officeFloorManagedObjectSource, moSourceToOffice
						.getOfficeFloorManagedObjectSource());
		assertEquals("office floor managed object source -> office", office,
				moSourceToOffice.getManagingOffice());

		// Ensure managed object source flow connected to office input
		assertEquals("office floor managed object source flow <- office input",
				flow, flowToInput.getOfficeFloorManagedObjectSoruceFlow());
		assertEquals("office floor managed object source flow -> office input",
				officeInput, flowToInput.getDeployedOfficeInput());

		// Ensure office object connected to office floor managed object
		assertEquals("office object <- office floor managed object",
				officeObject, officeObjectToManagedObject
						.getDeployedOfficeObject());
		assertEquals("office object -> office floor managed object",
				officeFloorManagedObject, officeObjectToManagedObject
						.getOfficeFloorManagedObject());

		// Ensure office team connected to office floor team
		assertEquals("office team <- office floor team", officeTeam,
				officeTeamToFloorTeam.getDeployedOfficeTeam());
		assertEquals("office team -> office floor team", officeFloorTeam,
				officeTeamToFloorTeam.getOfficeFloorTeam());
	}

	/**
	 * Ensures on storing a {@link OfficeFloorModel} that all
	 * {@link ConnectionModel} instances are readied for storing.
	 */
	public void testStoreOfficeFloor() throws Exception {

		// Create the office floor (without connections)
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		OfficeFloorManagedObjectSourceModel officeFloorManagedObjectSource = new OfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Connection.class
						.getName());
		officeFloor
				.addOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		OfficeFloorManagedObjectModel officeFloorManagedObject = new OfficeFloorManagedObjectModel(
				"MANAGED_OBJECT", "THREAD");
		officeFloor.addOfficeFloorManagedObject(officeFloorManagedObject);
		OfficeFloorTeamModel officeFloorTeam = new OfficeFloorTeamModel(
				"OFFICE_FLOOR_TEAM", "net.example.ExampleTeamSource");
		officeFloor.addOfficeFloorTeam(officeFloorTeam);
		DeployedOfficeModel office = new DeployedOfficeModel("OFFICE",
				"net.example.ExampleOfficeSource", "OFFICE_LOCATION");
		officeFloor.addDeployedOffice(office);
		DeployedOfficeInputModel officeInput = new DeployedOfficeInputModel(
				"SECTION", "INPUT", Integer.class.getName());
		office.addDeployedOfficeInput(officeInput);
		DeployedOfficeObjectModel officeObject = new DeployedOfficeObjectModel(
				"OBJECT", Connection.class.getName());
		office.addDeployedOfficeObject(officeObject);
		DeployedOfficeTeamModel officeTeam = new DeployedOfficeTeamModel(
				"OFFICE_TEAM");
		office.addDeployedOfficeTeam(officeTeam);

		// office floor managed object -> office floor managed object source
		OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel moToSource = new OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel();
		moToSource.setOfficeFloorManagedObject(officeFloorManagedObject);
		moToSource
				.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		moToSource.connect();

		// office floor managed object dependency -> office floor managed object
		OfficeFloorManagedObjectDependencyModel dependency = new OfficeFloorManagedObjectDependencyModel(
				"DEPENDENCY", Connection.class.getName());
		officeFloorManagedObject
				.addOfficeFloorManagedObjectDependency(dependency);
		OfficeFloorManagedObjectModel mo_dependency = new OfficeFloorManagedObjectModel(
				"MO_DEPENDENCY", "THREAD");
		officeFloor.addOfficeFloorManagedObject(mo_dependency);
		OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel dependencyToMo = new OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel();
		dependencyToMo.setOfficeFloorManagedObjectDependency(dependency);
		dependencyToMo.setOfficeFloorManagedObject(mo_dependency);
		dependencyToMo.connect();

		// office floor managed object source -> office
		OfficeFloorManagedObjectSourceToDeployedOfficeModel moSourceToOffice = new OfficeFloorManagedObjectSourceToDeployedOfficeModel();
		moSourceToOffice
				.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		moSourceToOffice.setManagingOffice(office);
		moSourceToOffice.connect();

		// office floor managed object source flow -> office input
		OfficeFloorManagedObjectSourceFlowModel flow = new OfficeFloorManagedObjectSourceFlowModel(
				"FLOW", Integer.class.getName());
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceFlow(flow);
		OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel();
		flowToInput.setOfficeFloorManagedObjectSoruceFlow(flow);
		flowToInput.setDeployedOfficeInput(officeInput);
		flowToInput.connect();

		// office object -> office floor managed object
		DeployedOfficeObjectToOfficeFloorManagedObjectModel officeObjectToManagedObject = new DeployedOfficeObjectToOfficeFloorManagedObjectModel();
		officeObjectToManagedObject.setDeployedOfficeObject(officeObject);
		officeObjectToManagedObject
				.setOfficeFloorManagedObject(officeFloorManagedObject);
		officeObjectToManagedObject.connect();

		// office team -> office floor team
		DeployedOfficeTeamToOfficeFloorTeamModel officeTeamToFloorTeam = new DeployedOfficeTeamToOfficeFloorTeamModel();
		officeTeamToFloorTeam.setDeployedOfficeTeam(officeTeam);
		officeTeamToFloorTeam.setOfficeFloorTeam(officeFloorTeam);
		officeTeamToFloorTeam.connect();

		// Record storing the office floor
		this.modelRepository.store(officeFloor, this.configurationItem);

		// Store the office floor
		this.replayMockObjects();
		this.officeRepository.storeOfficeFloor(officeFloor,
				this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals(
				"office floor managed object - office floor managed object source",
				"MANAGED_OBJECT_SOURCE", moToSource
						.getOfficeFloorManagedObjectSourceName());
		assertEquals("dependency - managed object", "MO_DEPENDENCY",
				dependencyToMo.getOfficeFloorManagedObjectName());
		assertEquals("office floor managed object source - office", "OFFICE",
				moSourceToOffice.getManagingOfficeName());
		assertEquals(
				"office floor managed object source flow - office input (office)",
				"OFFICE", flowToInput.getDeployedOfficeName());
		assertEquals(
				"office floor managed object source flow - office input (section)",
				"SECTION", flowToInput.getSectionName());
		assertEquals(
				"office floor managed object source flow - office input (input)",
				"INPUT", flowToInput.getSectionInputName());
		assertEquals("office object - office floor managed object",
				"MANAGED_OBJECT", officeObjectToManagedObject
						.getOfficeFloorManagedObjectName());
		assertEquals("office team - office floor team", "OFFICE_FLOOR_TEAM",
				officeTeamToFloorTeam.getOfficeFloorTeamName());
	}

}