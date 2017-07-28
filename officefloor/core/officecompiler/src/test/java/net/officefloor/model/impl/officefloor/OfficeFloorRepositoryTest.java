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
package net.officefloor.model.impl.officefloor;

import java.sql.Connection;

import org.easymock.AbstractMatcher;

import net.officefloor.configuration.WritableConfigurationItem;
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
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectPoolModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorRepository;
import net.officefloor.model.officefloor.OfficeFloorSupplierModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the {@link OfficeFloorRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this.createMock(ModelRepository.class);

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private final WritableConfigurationItem configurationItem = this.createMock(WritableConfigurationItem.class);

	/**
	 * {@link OfficeFloorRepository} to be tested.
	 */
	private final OfficeFloorRepository officeRepository = new OfficeFloorRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link OfficeFloorModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveOfficeFloor() throws Exception {

		// Create the raw OfficeFloor to be connected
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		OfficeFloorSupplierModel officeFloorSupplier = new OfficeFloorSupplierModel("SUPPLIER",
				"net.example.ExampleSupplierSource");
		officeFloor.addOfficeFloorSupplier(officeFloorSupplier);
		OfficeFloorManagedObjectSourceModel officeFloorManagedObjectSource = new OfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE", "net.example.ExampleManagedObjectSource", Connection.class.getName(), "0");
		officeFloor.addOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		OfficeFloorInputManagedObjectModel officeFloorInputManagedObject = new OfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT", Connection.class.getName());
		officeFloor.addOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		OfficeFloorManagedObjectModel officeFloorManagedObject = new OfficeFloorManagedObjectModel("MANAGED_OBJECT",
				"THREAD");
		officeFloor.addOfficeFloorManagedObject(officeFloorManagedObject);
		OfficeFloorManagedObjectPoolModel officeFloorManagedObjectPool = new OfficeFloorManagedObjectPoolModel("POOL",
				"net.example.ExampleManagedObjectPoolSource");
		officeFloor.addOfficeFloorManagedObjectPool(officeFloorManagedObjectPool);
		OfficeFloorTeamModel officeFloorTeam = new OfficeFloorTeamModel("OFFICE_FLOOR_TEAM",
				"net.example.ExampleTeamSource");
		officeFloor.addOfficeFloorTeam(officeFloorTeam);
		DeployedOfficeModel office = new DeployedOfficeModel("OFFICE", "net.example.ExampleOfficeSource",
				"OFFICE_LOCATION");
		officeFloor.addDeployedOffice(office);
		DeployedOfficeInputModel officeInput = new DeployedOfficeInputModel("SECTION", "INPUT",
				Integer.class.getName());
		office.addDeployedOfficeInput(officeInput);
		DeployedOfficeObjectModel officeObject = new DeployedOfficeObjectModel("OBJECT", Connection.class.getName());
		office.addDeployedOfficeObject(officeObject);
		DeployedOfficeTeamModel officeTeam = new DeployedOfficeTeamModel("OFFICE_TEAM");
		office.addDeployedOfficeTeam(officeTeam);

		// OfficeFloor managed object source -> OfficeFloor supplier
		OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel mosToSupplier = new OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel(
				"SUPPLIER", null, null);
		officeFloorManagedObjectSource.setOfficeFloorSupplier(mosToSupplier);

		// OfficeFloor managed object -> OfficeFloor managed object source
		OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel moToSource = new OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		officeFloorManagedObject.setOfficeFloorManagedObjectSource(moToSource);

		// OfficeFloor managed object source -> OfficeFloor managed object pool
		OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel mosToPool = new OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel(
				"POOL");
		officeFloorManagedObjectSource.setOfficeFloorManagedObjectPool(mosToPool);

		// input managed object -> bound managed object source
		OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel inputMoToBoundSource = new OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		officeFloorInputManagedObject.setBoundOfficeFloorManagedObjectSource(inputMoToBoundSource);

		// OfficeFloor managed object dependency -> OfficeFloor managed object
		OfficeFloorManagedObjectDependencyModel dependencyOne = new OfficeFloorManagedObjectDependencyModel(
				"DEPENDENCY_ONE", Connection.class.getName());
		officeFloorManagedObject.addOfficeFloorManagedObjectDependency(dependencyOne);
		OfficeFloorManagedObjectModel mo_dependency = new OfficeFloorManagedObjectModel("MO_DEPENDENCY", "THREAD");
		officeFloor.addOfficeFloorManagedObject(mo_dependency);
		OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel dependencyToMo = new OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel(
				"MO_DEPENDENCY");
		dependencyOne.setOfficeFloorManagedObject(dependencyToMo);

		// managed object dependency -> input managed object
		OfficeFloorManagedObjectDependencyModel dependencyTwo = new OfficeFloorManagedObjectDependencyModel(
				"DEPENDENCY_TWO", String.class.getName());
		officeFloorManagedObject.addOfficeFloorManagedObjectDependency(dependencyTwo);
		OfficeFloorInputManagedObjectModel mo_input_dependency = new OfficeFloorInputManagedObjectModel(
				"INPUT_DEPENDENCY", "THREAD");
		officeFloor.addOfficeFloorInputManagedObject(mo_input_dependency);
		OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel dependencyToInput = new OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel(
				"INPUT_DEPENDENCY");
		dependencyTwo.setOfficeFloorInputManagedObject(dependencyToInput);

		// OfficeFloor managed object source -> office
		OfficeFloorManagedObjectSourceToDeployedOfficeModel moSourceToOffice = new OfficeFloorManagedObjectSourceToDeployedOfficeModel(
				"OFFICE");
		officeFloorManagedObjectSource.setManagingOffice(moSourceToOffice);

		// OfficeFloor managed object source -> input managed object
		OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel moSourceToInputMo = new OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT");
		officeFloorManagedObjectSource.setOfficeFloorInputManagedObject(moSourceToInputMo);

		// managed object source input dependency -> managed object
		OfficeFloorManagedObjectSourceInputDependencyModel inputDependency = new OfficeFloorManagedObjectSourceInputDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		officeFloorManagedObjectSource.addOfficeFloorManagedObjectSourceInputDependency(inputDependency);
		OfficeFloorManagedObjectModel input_dependency = new OfficeFloorManagedObjectModel("INPUT_MO_DEPENDENCY",
				"PROCESS");
		officeFloor.addOfficeFloorManagedObject(input_dependency);
		OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel inputDependencyToMo = new OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel(
				"INPUT_MO_DEPENDENCY");
		inputDependency.setOfficeFloorManagedObject(inputDependencyToMo);

		// OfficeFloor managed object source flow -> office input
		OfficeFloorManagedObjectSourceFlowModel flow = new OfficeFloorManagedObjectSourceFlowModel("FLOW",
				Integer.class.getName());
		officeFloorManagedObjectSource.addOfficeFloorManagedObjectSourceFlow(flow);
		OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel(
				"OFFICE", "SECTION", "INPUT");
		flow.setDeployedOfficeInput(flowToInput);

		// OfficeFloor managed object source team -> OfficeFloor team
		OfficeFloorManagedObjectSourceTeamModel mosTeam = new OfficeFloorManagedObjectSourceTeamModel("MO_TEAM");
		officeFloorManagedObjectSource.addOfficeFloorManagedObjectSourceTeam(mosTeam);
		OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel(
				"OFFICE_FLOOR_TEAM");
		mosTeam.setOfficeFloorTeam(mosTeamToTeam);

		// office object -> OfficeFloor managed object
		DeployedOfficeObjectToOfficeFloorManagedObjectModel officeObjectToManagedObject = new DeployedOfficeObjectToOfficeFloorManagedObjectModel(
				"MANAGED_OBJECT");
		officeObject.setOfficeFloorManagedObject(officeObjectToManagedObject);

		// office object -> OfficeFloor input managed object
		DeployedOfficeObjectToOfficeFloorInputManagedObjectModel officeObjectToInputMo = new DeployedOfficeObjectToOfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT");
		officeObject.setOfficeFloorInputManagedObject(officeObjectToInputMo);

		// office team -> OfficeFloor team
		DeployedOfficeTeamToOfficeFloorTeamModel officeTeamToFloorTeam = new DeployedOfficeTeamToOfficeFloorTeamModel(
				"OFFICE_FLOOR_TEAM");
		officeTeam.setOfficeFloorTeam(officeTeamToFloorTeam);

		// Record retrieving the office
		this.modelRepository.retrieve(null, this.configurationItem);
		this.control(this.modelRepository).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertTrue("Must be office model", actual[0] instanceof OfficeFloorModel);
				assertEquals("Incorrect configuration item", OfficeFloorRepositoryTest.this.configurationItem,
						actual[1]);
				return true;
			}
		});

		// Retrieve the OfficeFloor
		this.replayMockObjects();
		this.officeRepository.retrieveOfficeFloor(officeFloor, this.configurationItem);
		this.verifyMockObjects();

		// Ensure OfficeFloor managed object source connected to its supplier
		assertEquals("OfficeFloor managed object source <- OfficeFloor supplier", officeFloorManagedObjectSource,
				mosToSupplier.getOfficeFloorManagedObjectSource());
		assertEquals("OfficeFloor managed object source -> OfficeFloor supplier", officeFloorSupplier,
				mosToSupplier.getOfficeFloorSupplier());

		// Ensure OfficeFloor managed object connected to its source
		assertEquals("OfficeFloor managed object <- OfficeFloor managed object source", officeFloorManagedObject,
				moToSource.getOfficeFloorManagedObject());
		assertEquals("OfficeFloor managed object -> OfficeFloor managed object source", officeFloorManagedObjectSource,
				moToSource.getOfficeFloorManagedObjectSource());

		// Ensure OfficeFloor managed object source connected to its pool
		assertEquals("OfficeFloor managed object source <- OfficeFloor managed object pool",
				officeFloorManagedObjectSource, mosToPool.getOfficeFloorManagedObjectSource());
		assertEquals("OfficeFloor managed object source -> OfficeFloor managed object pool",
				officeFloorManagedObjectPool, mosToPool.getOfficeFloorManagedObjectPool());

		// Ensure input managed object connected to its bound source
		assertEquals("input managed object <- bound managed object source", officeFloorInputManagedObject,
				inputMoToBoundSource.getBoundOfficeFloorInputManagedObject());
		assertEquals("input managed object -> bound managed object source", officeFloorManagedObjectSource,
				inputMoToBoundSource.getBoundOfficeFloorManagedObjectSource());

		// Ensure managed object dependency to managed object
		assertEquals("dependency <- managed object", dependencyOne,
				dependencyToMo.getOfficeFloorManagedObjectDependency());
		assertEquals("dependency -> managed object", mo_dependency, dependencyToMo.getOfficeFloorManagedObject());

		// Ensure managed object dependency to input managed object
		assertEquals("dependency <- input managed object", dependencyTwo,
				dependencyToInput.getOfficeFloorManagedObjectDependency());
		assertEquals("dependency -> input managed object", mo_input_dependency,
				dependencyToInput.getOfficeFloorInputManagedObject());

		// Ensure managed object source connected to managing office
		assertEquals("OfficeFloor managed object source <- office", officeFloorManagedObjectSource,
				moSourceToOffice.getOfficeFloorManagedObjectSource());
		assertEquals("OfficeFloor managed object source -> office", office, moSourceToOffice.getManagingOffice());

		// Ensure managed object source connected to input managed object
		assertEquals("OfficeFloor managed object source <- input managed object", officeFloorManagedObjectSource,
				moSourceToInputMo.getOfficeFloorManagedObjectSource());
		assertEquals("OfficeFloor managed object source -> input managed object", officeFloorInputManagedObject,
				moSourceToInputMo.getOfficeFloorInputManagedObject());

		// Ensure input dependency connected to managed object
		assertEquals("input dependency <- managed object", inputDependency,
				inputDependencyToMo.getOfficeFloorManagedObjectDependency());
		assertEquals("input dependency -> managed object", input_dependency,
				inputDependencyToMo.getOfficeFloorManagedObject());

		// Ensure managed object source flow connected to office input
		assertEquals("OfficeFloor managed object source flow <- office input", flow,
				flowToInput.getOfficeFloorManagedObjectSoruceFlow());
		assertEquals("OfficeFloor managed object source flow -> office input", officeInput,
				flowToInput.getDeployedOfficeInput());

		// Ensure managed object source team connected to OfficeFloor team
		assertEquals("OfficeFloor managed object source team <- OfficeFloor team", mosTeam,
				mosTeamToTeam.getOfficeFloorManagedObjectSourceTeam());
		assertEquals("OfficeFloor managed object source team -> OfficeFloor team", officeFloorTeam,
				mosTeamToTeam.getOfficeFloorTeam());

		// Ensure office object connected to OfficeFloor managed object
		assertEquals("office object <- OfficeFloor managed object", officeObject,
				officeObjectToManagedObject.getDeployedOfficeObject());
		assertEquals("office object -> OfficeFloor managed object", officeFloorManagedObject,
				officeObjectToManagedObject.getOfficeFloorManagedObject());

		// Ensure office object connected to OfficeFloor input managed object
		assertEquals("office object <- OfficeFloor input managed object", officeObject,
				officeObjectToInputMo.getDeployedOfficeObject());
		assertEquals("office object -> OfficeFloor input managed object", officeFloorInputManagedObject,
				officeObjectToInputMo.getOfficeFloorInputManagedObject());

		// Ensure office team connected to OfficeFloor team
		assertEquals("office team <- OfficeFloor team", officeTeam, officeTeamToFloorTeam.getDeployedOfficeTeam());
		assertEquals("office team -> OfficeFloor team", officeFloorTeam, officeTeamToFloorTeam.getOfficeFloorTeam());
	}

	/**
	 * Ensures on storing a {@link OfficeFloorModel} that all
	 * {@link ConnectionModel} instances are readied for storing.
	 */
	public void testStoreOfficeFloor() throws Exception {

		// Create the OfficeFloor (without connections)
		OfficeFloorModel officeFloor = new OfficeFloorModel();
		OfficeFloorSupplierModel officeFloorSupplier = new OfficeFloorSupplierModel("SUPPLIER",
				"net.example.ExampleSupplierSource");
		officeFloor.addOfficeFloorSupplier(officeFloorSupplier);
		OfficeFloorManagedObjectSourceModel officeFloorManagedObjectSource = new OfficeFloorManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE", "net.example.ExampleManagedObjectSource", Connection.class.getName(), "0");
		officeFloor.addOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		OfficeFloorInputManagedObjectModel officeFloorInputManagedObject = new OfficeFloorInputManagedObjectModel(
				"INPUT_MANAGED_OBJECT", Connection.class.getName());
		officeFloor.addOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		OfficeFloorManagedObjectModel officeFloorManagedObject = new OfficeFloorManagedObjectModel("MANAGED_OBJECT",
				"THREAD");
		officeFloor.addOfficeFloorManagedObject(officeFloorManagedObject);
		OfficeFloorManagedObjectPoolModel officeFloorManagedObjectPool = new OfficeFloorManagedObjectPoolModel("POOL",
				"net.example.ExampleManagedObjectPoolSource");
		officeFloor.addOfficeFloorManagedObjectPool(officeFloorManagedObjectPool);
		OfficeFloorTeamModel officeFloorTeam = new OfficeFloorTeamModel("OFFICE_FLOOR_TEAM",
				"net.example.ExampleTeamSource");
		officeFloor.addOfficeFloorTeam(officeFloorTeam);
		DeployedOfficeModel office = new DeployedOfficeModel("OFFICE", "net.example.ExampleOfficeSource",
				"OFFICE_LOCATION");
		officeFloor.addDeployedOffice(office);
		DeployedOfficeInputModel officeInput = new DeployedOfficeInputModel("SECTION", "INPUT",
				Integer.class.getName());
		office.addDeployedOfficeInput(officeInput);
		DeployedOfficeObjectModel officeObject = new DeployedOfficeObjectModel("OBJECT", Connection.class.getName());
		office.addDeployedOfficeObject(officeObject);
		DeployedOfficeTeamModel officeTeam = new DeployedOfficeTeamModel("OFFICE_TEAM");
		office.addDeployedOfficeTeam(officeTeam);

		// OfficeFloor managed object source -> OfficeFloor supplier
		OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel mosToSupplier = new OfficeFloorManagedObjectSourceToOfficeFloorSupplierModel();
		mosToSupplier.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		mosToSupplier.setOfficeFloorSupplier(officeFloorSupplier);
		mosToSupplier.connect();

		// OfficeFloor managed object -> OfficeFloor managed object source
		OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel moToSource = new OfficeFloorManagedObjectToOfficeFloorManagedObjectSourceModel();
		moToSource.setOfficeFloorManagedObject(officeFloorManagedObject);
		moToSource.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		moToSource.connect();

		// OfficeFloor managed object source -> OfficeFloor managed object pool
		OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel mosToPool = new OfficeFloorManagedObjectSourceToOfficeFloorManagedObjectPoolModel();
		mosToPool.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		mosToPool.setOfficeFloorManagedObjectPool(officeFloorManagedObjectPool);
		mosToPool.connect();

		// input managed object -> bound managed object source
		OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel inputMoToBoundSource = new OfficeFloorInputManagedObjectToBoundOfficeFloorManagedObjectSourceModel();
		inputMoToBoundSource.setBoundOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		inputMoToBoundSource.setBoundOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		inputMoToBoundSource.connect();

		// OfficeFloor managed object dependency -> OfficeFloor managed object
		OfficeFloorManagedObjectDependencyModel dependencyOne = new OfficeFloorManagedObjectDependencyModel(
				"DEPENDENCY_TWO", Connection.class.getName());
		officeFloorManagedObject.addOfficeFloorManagedObjectDependency(dependencyOne);
		OfficeFloorManagedObjectModel mo_dependency = new OfficeFloorManagedObjectModel("MO_DEPENDENCY", "THREAD");
		officeFloor.addOfficeFloorManagedObject(mo_dependency);
		OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel dependencyToMo = new OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel();
		dependencyToMo.setOfficeFloorManagedObjectDependency(dependencyOne);
		dependencyToMo.setOfficeFloorManagedObject(mo_dependency);
		dependencyToMo.connect();

		// managed object dependency -> input managed object
		OfficeFloorManagedObjectDependencyModel dependencyTwo = new OfficeFloorManagedObjectDependencyModel(
				"DEPENDENCY_TWO", String.class.getName());
		officeFloorManagedObject.addOfficeFloorManagedObjectDependency(dependencyTwo);
		OfficeFloorInputManagedObjectModel mo_input_dependency = new OfficeFloorInputManagedObjectModel(
				"INPUT_DEPENDENCY", "THREAD");
		officeFloor.addOfficeFloorInputManagedObject(mo_input_dependency);
		OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel dependencyToInput = new OfficeFloorManagedObjectDependencyToOfficeFloorInputManagedObjectModel();
		dependencyToInput.setOfficeFloorManagedObjectDependency(dependencyTwo);
		dependencyToInput.setOfficeFloorInputManagedObject(mo_input_dependency);
		dependencyToInput.connect();

		// OfficeFloor managed object source -> office
		OfficeFloorManagedObjectSourceToDeployedOfficeModel moSourceToOffice = new OfficeFloorManagedObjectSourceToDeployedOfficeModel();
		moSourceToOffice.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		moSourceToOffice.setManagingOffice(office);
		moSourceToOffice.connect();

		// OfficeFloor managed object source -> input managed object
		OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel moSourceToInputMo = new OfficeFloorManagedObjectSourceToOfficeFloorInputManagedObjectModel();
		moSourceToInputMo.setOfficeFloorManagedObjectSource(officeFloorManagedObjectSource);
		moSourceToInputMo.setOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		moSourceToInputMo.connect();

		// managed object source input dependency -> managed object
		OfficeFloorManagedObjectSourceInputDependencyModel inputDependency = new OfficeFloorManagedObjectSourceInputDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		officeFloorManagedObjectSource.addOfficeFloorManagedObjectSourceInputDependency(inputDependency);
		OfficeFloorManagedObjectModel input_dependency = new OfficeFloorManagedObjectModel("INPUT_MO_DEPENDENCY",
				"PROCESS");
		officeFloor.addOfficeFloorManagedObject(input_dependency);
		OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel inputDependencyToMo = new OfficeFloorManagedObjectSourceInputDependencyToOfficeFloorManagedObjectModel();
		inputDependencyToMo.setOfficeFloorManagedObjectDependency(inputDependency);
		inputDependencyToMo.setOfficeFloorManagedObject(input_dependency);
		inputDependencyToMo.connect();

		// OfficeFloor managed object source flow -> office input
		OfficeFloorManagedObjectSourceFlowModel flow = new OfficeFloorManagedObjectSourceFlowModel("FLOW",
				Integer.class.getName());
		officeFloorManagedObjectSource.addOfficeFloorManagedObjectSourceFlow(flow);
		OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel flowToInput = new OfficeFloorManagedObjectSourceFlowToDeployedOfficeInputModel();
		flowToInput.setOfficeFloorManagedObjectSoruceFlow(flow);
		flowToInput.setDeployedOfficeInput(officeInput);
		flowToInput.connect();

		// OfficeFloor managed object source team -> OfficeFloor team
		OfficeFloorManagedObjectSourceTeamModel mosTeam = new OfficeFloorManagedObjectSourceTeamModel("MO_TEAM");
		officeFloorManagedObjectSource.addOfficeFloorManagedObjectSourceTeam(mosTeam);
		OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel mosTeamToTeam = new OfficeFloorManagedObjectSourceTeamToOfficeFloorTeamModel();
		mosTeamToTeam.setOfficeFloorManagedObjectSourceTeam(mosTeam);
		mosTeamToTeam.setOfficeFloorTeam(officeFloorTeam);
		mosTeamToTeam.connect();

		// office object -> OfficeFloor managed object
		DeployedOfficeObjectToOfficeFloorManagedObjectModel officeObjectToManagedObject = new DeployedOfficeObjectToOfficeFloorManagedObjectModel();
		officeObjectToManagedObject.setDeployedOfficeObject(officeObject);
		officeObjectToManagedObject.setOfficeFloorManagedObject(officeFloorManagedObject);
		officeObjectToManagedObject.connect();

		// office object -> OfficeFloor input managed object
		DeployedOfficeObjectToOfficeFloorInputManagedObjectModel officeObjectToInputMo = new DeployedOfficeObjectToOfficeFloorInputManagedObjectModel();
		officeObjectToInputMo.setDeployedOfficeObject(officeObject);
		officeObjectToInputMo.setOfficeFloorInputManagedObject(officeFloorInputManagedObject);
		officeObjectToInputMo.connect();

		// office team -> OfficeFloor team
		DeployedOfficeTeamToOfficeFloorTeamModel officeTeamToFloorTeam = new DeployedOfficeTeamToOfficeFloorTeamModel();
		officeTeamToFloorTeam.setDeployedOfficeTeam(officeTeam);
		officeTeamToFloorTeam.setOfficeFloorTeam(officeFloorTeam);
		officeTeamToFloorTeam.connect();

		// Record storing the OfficeFloor
		this.modelRepository.store(officeFloor, this.configurationItem);

		// Store the OfficeFloor
		this.replayMockObjects();
		this.officeRepository.storeOfficeFloor(officeFloor, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("OfficeFloor managed object source - OfficeFloor supplier", "SUPPLIER",
				mosToSupplier.getOfficeFloorSupplierName());
		assertEquals("OfficeFloor managed object - OfficeFloor managed object source", "MANAGED_OBJECT_SOURCE",
				moToSource.getOfficeFloorManagedObjectSourceName());
		assertEquals("OfficeFloor managed object source - OfficeFloor managed object pool", "POOL",
				mosToPool.getOfficeFloorManagedObjectPoolName());
		assertEquals("input managed object - bound managed object source", "MANAGED_OBJECT_SOURCE",
				inputMoToBoundSource.getOfficeFloorManagedObjectSourceName());
		assertEquals("dependency - managed object", "MO_DEPENDENCY", dependencyToMo.getOfficeFloorManagedObjectName());
		assertEquals("dependency - input managed object", "INPUT_DEPENDENCY",
				dependencyToInput.getOfficeFloorInputManagedObjectName());
		assertEquals("OfficeFloor managed object source - office", "OFFICE", moSourceToOffice.getManagingOfficeName());
		assertEquals("OfficeFloor managed object source - input managed object", "INPUT_MANAGED_OBJECT",
				moSourceToInputMo.getOfficeFloorInputManagedObjectName());
		assertEquals("managed object source input dependency - managed object", "INPUT_MO_DEPENDENCY",
				inputDependencyToMo.getOfficeFloorManagedObjectName());
		assertEquals("OfficeFloor managed object source flow - office input (office)", "OFFICE",
				flowToInput.getDeployedOfficeName());
		assertEquals("OfficeFloor managed object source flow - office input (section)", "SECTION",
				flowToInput.getSectionName());
		assertEquals("OfficeFloor managed object source flow - office input (input)", "INPUT",
				flowToInput.getSectionInputName());
		assertEquals("OfficeFloor managed object team - OfficeFloor team", "OFFICE_FLOOR_TEAM",
				mosTeamToTeam.getOfficeFloorTeamName());
		assertEquals("office object - OfficeFloor managed object", "MANAGED_OBJECT",
				officeObjectToManagedObject.getOfficeFloorManagedObjectName());
		assertEquals("office object - OfficeFloor input managed object", "INPUT_MANAGED_OBJECT",
				officeObjectToInputMo.getOfficeFloorInputManagedObjectName());
		assertEquals("office team - OfficeFloor team", "OFFICE_FLOOR_TEAM",
				officeTeamToFloorTeam.getOfficeFloorTeamName());
	}

}