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
package net.officefloor.model.impl.officefloor;

import java.sql.Connection;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.officefloor.DeployedOfficeInputModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
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
 * @author Daniel Sagenschneider
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
		OfficeFloorInputManagedObjectModel officeFloorInputManagedObject = new OfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT", Connection.class.getName());
		officeFloor
				.addOfficeFloorInputManagedObject(officeFloorInputManagedObject);
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

		// input managed object -> bound managed object source
		OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel inputMoToBoundSource = new OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		officeFloorInputManagedObject
				.setBoundOfficeFloorManagedObjectSource(inputMoToBoundSource);

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
				"OFFICE");
		officeFloorManagedObjectSource.setManagingOffice(moSourceToOffice);

		// office floor managed object source -> input managed object
		OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel moSourceToInputMo = new OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT");
		officeFloorManagedObjectSource
				.setOfficeFloorInputManagedObject(moSourceToInputMo);

		// managed object source input dependency -> managed object
		OfficeFloorManagedObjectSourceInputDependencyModel inputDependency = new OfficeFloorManagedObjectSourceInputDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceInputDependency(inputDependency);
		OfficeFloorManagedObjectModel input_dependency = new OfficeFloorManagedObjectModel(
				"INPUT_MO_DEPENDENCY", "PROCESS");
		officeFloor.addOfficeFloorManagedObject(input_dependency);
		OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel inputDependencyToMo = new OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel(
				"INPUT_MO_DEPENDENCY");
		inputDependency.setOfficeFloorManagedObject(inputDependencyToMo);

		// office floor managed object source flow -> office input
		OfficeFloorManagedObjectSourceFlowModel flow = new OfficeFloorManagedObjectSourceFlowModel(
				"FLOW", Integer.class.getName());
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceFlow(flow);
		OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel(
				"OFFICE", "SECTION", "INPUT");
		flow.setDeployedOfficeInput(flowToInput);

		// office floor managed object source team -> office floor team
		OfficeFloorManagedObjectSourceTeamModel mosTeam = new OfficeFloorManagedObjectSourceTeamModel(
				"MO_TEAM");
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceTeam(mosTeam);
		OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel(
				"OFFICE_FLOOR_TEAM");
		mosTeam.setOfficeFloorTeam(mosTeamToTeam);

		// office object -> office floor managed object
		DeployedOfficeObjectToOfficeFloorManagedObjectModel officeObjectToManagedObject = new DeployedOfficeObjectToOfficeFloorManagedObjectModel(
				"MANAGED_OBJECT");
		officeObject.setOfficeFloorManagedObject(officeObjectToManagedObject);

		// office object -> office floor input managed object
		DeployedOfficeObjectToOfficeFloorInputManagedObjectModel officeObjectToInputMo = new DeployedOfficeObjectToOfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT");
		officeObject.setOfficeFloorInputManagedObject(officeObjectToInputMo);

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

		// Ensure input managed object connected to its bound source
		assertEquals("input managed object <- bound managed object source",
				officeFloorInputManagedObject, inputMoToBoundSource
						.getBoundOfficeFloorInputManagedObject());
		assertEquals("input managed object -> bound managed object source",
				officeFloorManagedObjectSource, inputMoToBoundSource
						.getBoundOfficeFloorManagedObjectSource());

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

		// Ensure managed object source connected to input managed object
		assertEquals(
				"office floor managed object source <- input managed object",
				officeFloorManagedObjectSource, moSourceToInputMo
						.getOfficeFloorManagedObjectSource());
		assertEquals(
				"office floor managed object source -> input managed object",
				officeFloorInputManagedObject, moSourceToInputMo
						.getOfficeFloorInputManagedObject());

		// Ensure input dependency connected to managed object
		assertEquals("input dependency <- managed object", inputDependency,
				inputDependencyToMo.getOfficeFloorManagedObjectDependency());
		assertEquals("input dependency -> managed object", input_dependency,
				inputDependencyToMo.getOfficeFloorManagedObject());

		// Ensure managed object source flow connected to office input
		assertEquals("office floor managed object source flow <- office input",
				flow, flowToInput.getOfficeFloorManagedObjectSoruceFlow());
		assertEquals("office floor managed object source flow -> office input",
				officeInput, flowToInput.getDeployedOfficeInput());

		// Ensure managed object source team connected to office floor team
		assertEquals(
				"office floor managed object source team <- office floor team",
				mosTeam, mosTeamToTeam.getOfficeFloorManagedObjectSourceTeam());
		assertEquals(
				"office floor managed object source team -> office floor team",
				officeFloorTeam, mosTeamToTeam.getOfficeFloorTeam());

		// Ensure office object connected to office floor managed object
		assertEquals("office object <- office floor managed object",
				officeObject, officeObjectToManagedObject
						.getDeployedOfficeObject());
		assertEquals("office object -> office floor managed object",
				officeFloorManagedObject, officeObjectToManagedObject
						.getOfficeFloorManagedObject());

		// Ensure office object connected to office floor input managed object
		assertEquals("office object <- office floor input managed object",
				officeObject, officeObjectToInputMo.getDeployedOfficeObject());
		assertEquals("office object -> office floor input managed object",
				officeFloorInputManagedObject, officeObjectToInputMo
						.getOfficeFloorInputManagedObject());

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
		OfficeFloorInputManagedObjectModel officeFloorInputManagedObject = new OfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT", Connection.class.getName());
		officeFloor
				.addOfficeFloorInputManagedObject(officeFloorInputManagedObject);
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

		// input managed object -> bound managed object source
		OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel inputMoToBoundSource = new OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel();
		inputMoToBoundSource
				.setBoundOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		inputMoToBoundSource
				.setBoundOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		inputMoToBoundSource.connect();

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

		// office floor managed object source -> input managed object
		OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel moSourceToInputMo = new OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel();
		moSourceToInputMo
				.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		moSourceToInputMo
				.setOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		moSourceToInputMo.connect();

		// managed object source input dependency -> managed object
		OfficeFloorManagedObjectSourceInputDependencyModel inputDependency = new OfficeFloorManagedObjectSourceInputDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceInputDependency(inputDependency);
		OfficeFloorManagedObjectModel input_dependency = new OfficeFloorManagedObjectModel(
				"INPUT_MO_DEPENDENCY", "PROCESS");
		officeFloor.addOfficeFloorManagedObject(input_dependency);
		OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel inputDependencyToMo = new OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel();
		inputDependencyToMo
				.setOfficeFloorManagedObjectDependency(inputDependency);
		inputDependencyToMo.setOfficeFloorManagedObject(input_dependency);
		inputDependencyToMo.connect();

		// office floor managed object source flow -> office input
		OfficeFloorManagedObjectSourceFlowModel flow = new OfficeFloorManagedObjectSourceFlowModel(
				"FLOW", Integer.class.getName());
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceFlow(flow);
		OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel();
		flowToInput.setOfficeFloorManagedObjectSoruceFlow(flow);
		flowToInput.setDeployedOfficeInput(officeInput);
		flowToInput.connect();

		// office floor managed object source team -> office floor team
		OfficeFloorManagedObjectSourceTeamModel mosTeam = new OfficeFloorManagedObjectSourceTeamModel(
				"MO_TEAM");
		officeFloorManagedObjectSource
				.addOfficeFloorManagedObjectSourceTeam(mosTeam);
		OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel();
		mosTeamToTeam.setOfficeFloorManagedObjectSourceTeam(mosTeam);
		mosTeamToTeam.setOfficeFloorTeam(officeFloorTeam);
		mosTeamToTeam.connect();

		// office object -> office floor managed object
		DeployedOfficeObjectToOfficeFloorManagedObjectModel officeObjectToManagedObject = new DeployedOfficeObjectToOfficeFloorManagedObjectModel();
		officeObjectToManagedObject.setDeployedOfficeObject(officeObject);
		officeObjectToManagedObject
				.setOfficeFloorManagedObject(officeFloorManagedObject);
		officeObjectToManagedObject.connect();

		// office object -> office floor input managed object
		DeployedOfficeObjectToOfficeFloorInputManagedObjectModel officeObjectToInputMo = new DeployedOfficeObjectToOfficeFloorInputManagedObjectModel();
		officeObjectToInputMo.setDeployedOfficeObject(officeObject);
		officeObjectToInputMo
				.setOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		officeObjectToInputMo.connect();

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
		assertEquals("input managed object - bound managed object source",
				"MANAGED_OBJECT_SOURCE", inputMoToBoundSource
						.getOfficeFloorManagedObjectSourceName());
		assertEquals("dependency - managed object", "MO_DEPENDENCY",
				dependencyToMo.getOfficeFloorManagedObjectName());
		assertEquals("office floor managed object source - office", "OFFICE",
				moSourceToOffice.getManagingOfficeName());
		assertEquals(
				"office floor managed object source - input managed object",
				"INPUT_MANAGED_OBJECT", moSourceToInputMo
						.getOfficeFloorInputManagedObjectName());
		assertEquals("managed object source input dependency - managed object",
				"INPUT_MO_DEPENDENCY", inputDependencyToMo
						.getOfficeFloorManagedObjectName());
		assertEquals(
				"office floor managed object source flow - office input (office)",
				"OFFICE", flowToInput.getDeployedOfficeName());
		assertEquals(
				"office floor managed object source flow - office input (section)",
				"SECTION", flowToInput.getSectionName());
		assertEquals(
				"office floor managed object source flow - office input (input)",
				"INPUT", flowToInput.getSectionInputName());
		assertEquals("office floor managed object team - office floor team",
				"OFFICE_FLOOR_TEAM", mosTeamToTeam.getOfficeFloorTeamName());
		assertEquals("office object - office floor managed object",
				"MANAGED_OBJECT", officeObjectToManagedObject
						.getOfficeFloorManagedObjectName());
		assertEquals("office object - office floor input managed object",
				"INPUT_MANAGED_OBJECT", officeObjectToInputMo
						.getOfficeFloorInputManagedObjectName());
		assertEquals("office team - office floor team", "OFFICE_FLOOR_TEAM",
				officeTeamToFloorTeam.getOfficeFloorTeamName());
	}

}