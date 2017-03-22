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
package net.officefloor.model.impl.desk;

import java.sql.Connection;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.impl.section.DeskRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.DeskManagedObjectDependencyModel;
import net.officefloor.model.section.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.DeskManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.section.DeskManagedObjectSourceModel;
import net.officefloor.model.section.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.section.DeskModel;
import net.officefloor.model.section.DeskRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToDeskManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;

/**
 * Tests the {@link DeskRepository}.
 *
 * @author Daniel Sagenschneider
 */
public class DeskRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this.createMock(ModelRepository.class);

	/**
	 * {@link ConfigurationItem}.
	 */
	private final ConfigurationItem configurationItem = this.createMock(ConfigurationItem.class);

	/**
	 * {@link DeskRepository} to be tested.
	 */
	private final DeskRepositoryImpl deskRepository = new DeskRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link DeskModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveDesk() throws Exception {

		// Create the raw desk to be connected
		DeskModel desk = new DeskModel();
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT",
				Connection.class.getName());
		desk.addExternalManagedObject(extMo);
		DeskManagedObjectSourceModel mos = new DeskManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Object.class.getName(), "0");
		desk.addDeskManagedObjectSource(mos);
		DeskManagedObjectSourceFlowModel mosFlow = new DeskManagedObjectSourceFlowModel("MOS_FLOW",
				Object.class.getName());
		mos.addDeskManagedObjectSourceFlow(mosFlow);
		DeskManagedObjectModel mo = new DeskManagedObjectModel("MANAGED_OBJECT", "THREAD");
		desk.addDeskManagedObject(mo);
		DeskManagedObjectDependencyModel dependency = new DeskManagedObjectDependencyModel("DEPENDENCY", "THREAD");
		mo.addDeskManagedObjectDependency(dependency);
		FunctionNamespaceModel namespace = new FunctionNamespaceModel("NAMESPACE",
				"net.example.ExampleManagedFunctionSource");
		desk.addFunctionNamespace(namespace);
		ManagedFunctionModel managedFunction = new ManagedFunctionModel("MANAGED_FUNCTION");
		namespace.addManagedFunction(managedFunction);
		FunctionModel function = new FunctionModel("FUNCTION", false, "NAMESPACE", "MANAGED_FUNCTION",
				Object.class.getName());
		desk.addFunction(function);
		ManagedFunctionObjectModel functionObject = new ManagedFunctionObjectModel();
		managedFunction.addManagedFunctionObject(functionObject);

		// managed object -> managed object source
		DeskManagedObjectToDeskManagedObjectSourceModel moToMos = new DeskManagedObjectToDeskManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		mo.setDeskManagedObjectSource(moToMos);

		// managed object source flow -> external flow
		ExternalFlowModel extFlow_mosFlow = new ExternalFlowModel("mosFlow - extFlow", String.class.getName());
		desk.addExternalFlow(extFlow_mosFlow);
		DeskManagedObjectSourceFlowToExternalFlowModel mosFlowToExtFlow = new DeskManagedObjectSourceFlowToExternalFlowModel(
				"mosFlow - extFlow");
		mosFlow.setExternalFlow(mosFlowToExtFlow);

		// managed object source flow -> function
		DeskManagedObjectSourceFlowToFunctionModel mosFlowToFunction = new DeskManagedObjectSourceFlowToFunctionModel(
				"FUNCTION");
		mosFlow.setFunction(mosFlowToFunction);

		// dependency -> external managed object
		DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new DeskManagedObjectDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		dependency.setExternalManagedObject(dependencyToExtMo);

		// dependency -> managed object
		DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToMo = new DeskManagedObjectDependencyToDeskManagedObjectModel(
				"MANAGED_OBJECT");
		dependency.setDeskManagedObject(dependencyToMo);

		// functionObject -> extMo
		ManagedFunctionObjectToExternalManagedObjectModel objectToExtMo = new ManagedFunctionObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		functionObject.setExternalManagedObject(objectToExtMo);

		// functionObject -> managed object
		ManagedFunctionObjectToDeskManagedObjectModel objectToMo = new ManagedFunctionObjectToDeskManagedObjectModel(
				"MANAGED_OBJECT");
		functionObject.setDeskManagedObject(objectToMo);

		// functionFlow -> extFlow
		FunctionFlowModel functionFlow_extFlow = new FunctionFlowModel();
		function.addFunctionFlow(functionFlow_extFlow);
		ExternalFlowModel extFlow_functionFlow = new ExternalFlowModel("functionFlow - extFlow",
				String.class.getName());
		desk.addExternalFlow(extFlow_functionFlow);
		FunctionFlowToExternalFlowModel flowToExtFlow = new FunctionFlowToExternalFlowModel("functionFlow - extFlow",
				false);
		functionFlow_extFlow.setExternalFlow(flowToExtFlow);

		// next -> extFlow
		ExternalFlowModel extFlow_next = new ExternalFlowModel("next - extFlow", Integer.class.getName());
		desk.addExternalFlow(extFlow_next);
		FunctionToNextExternalFlowModel nextToExtFlow = new FunctionToNextExternalFlowModel("next - extFlow");
		function.setNextExternalFlow(nextToExtFlow);

		// functionFlow -> function
		FunctionFlowModel functionFlow_function = new FunctionFlowModel();
		function.addFunctionFlow(functionFlow_function);
		FunctionModel function_functionFlow = new FunctionModel("flow - function", false, "namespace",
				"managed_function", Object.class.getName());
		desk.addFunction(function_functionFlow);
		FunctionFlowToFunctionModel flowToFunction = new FunctionFlowToFunctionModel("flow - function", true);
		functionFlow_function.setFunction(flowToFunction);

		// functionEscalation -> function
		FunctionEscalationModel functionEscalation_function = new FunctionEscalationModel();
		function.addFunctionEscalation(functionEscalation_function);
		FunctionModel function_functionEscalation = new FunctionModel("escalation - function", false, "NAMESPACE",
				"MANAGED_FUNCTION", Object.class.getName());
		desk.addFunction(function_functionEscalation);
		FunctionEscalationToFunctionModel escalationToFunction = new FunctionEscalationToFunctionModel(
				"escalation - function");
		functionEscalation_function.setFunction(escalationToFunction);

		// functionEscalation -> extFlow
		FunctionEscalationModel functionEscalation_extFlow = new FunctionEscalationModel();
		function.addFunctionEscalation(functionEscalation_extFlow);
		ExternalFlowModel extFlow_functionEscalation = new ExternalFlowModel("escalation - extFlow",
				Throwable.class.getName());
		desk.addExternalFlow(extFlow_functionEscalation);
		FunctionEscalationToExternalFlowModel escalationToExtFlow = new FunctionEscalationToExternalFlowModel(
				"escalation - extFlow");
		functionEscalation_extFlow.setExternalFlow(escalationToExtFlow);

		// next -> function
		FunctionModel function_next = new FunctionModel("next - function", false, "namespace", "managed_function",
				Integer.class.getName());
		desk.addFunction(function_next);
		FunctionToNextFunctionModel nextToFunction = new FunctionToNextFunctionModel("next - function");
		function.setNextFunction(nextToFunction);

		// Record retrieving the desk
		this.recordReturn(this.modelRepository, this.modelRepository.retrieve(null, this.configurationItem), desk,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertTrue("Must be desk model", actual[0] instanceof DeskModel);
						assertEquals("Incorrect configuration item", DeskRepositoryTest.this.configurationItem,
								actual[1]);
						return true;
					}
				});

		// Retrieve the desk
		this.replayMockObjects();
		DeskModel retrievedDesk = this.deskRepository.retrieveDesk(this.configurationItem);
		this.verifyMockObjects();
		assertEquals("Incorrect desk", desk, retrievedDesk);

		// Ensure the managed object connected to its source
		assertEquals("mo <- mos", mo, moToMos.getDeskManagedObject());
		assertEquals("mo -> mos", mos, moToMos.getDeskManagedObjectSource());

		// Ensure managed object source flow connected to external flow
		assertEquals("mos flow <- external flow", mosFlow, mosFlowToExtFlow.getDeskManagedObjectSourceFlow());
		assertEquals("mos flow -> external flow", extFlow_mosFlow, mosFlowToExtFlow.getExternalFlow());

		// Ensure managed object source flow connected to function
		assertEquals("mos flow <- function", mosFlow, mosFlowToFunction.getDeskManagedObjectSourceFlow());
		assertEquals("mos flow -> function", function, mosFlowToFunction.getFunction());

		// Ensure dependency connected to external managed object
		assertEquals("dependency <- external mo", dependency, dependencyToExtMo.getDeskManagedObjectDependency());
		assertEquals("dependency -> external mo", extMo, dependencyToExtMo.getExternalManagedObject());

		// Ensure dependency connected to managed object
		assertEquals("dependency <- mo", dependency, dependencyToMo.getDeskManagedObjectDependency());
		assertEquals("dependency -> mo", mo, dependencyToMo.getDeskManagedObject());

		// Ensure the external managed object connected
		assertEquals("functionObject <- extMo", functionObject, objectToExtMo.getManagedFunctionObject());
		assertEquals("functionObject -> extMo", extMo, objectToExtMo.getExternalManagedObject());

		// Ensure function object connected to managed object
		assertEquals("functionObject <- managed object", functionObject, objectToMo.getManagedFunctionObject());
		assertEquals("functionObject -> managed object", mo, objectToMo.getDeskManagedObject());

		// Ensure the external flow connected
		assertEquals("functionFlow <- extFlow", functionFlow_extFlow, flowToExtFlow.getFunctionFlow());
		assertEquals("functionFlow -> extFlow", extFlow_functionFlow, flowToExtFlow.getExternalFlow());

		// Ensure the next external flow connected
		assertEquals("next -> extFlow", function, nextToExtFlow.getPreviousFunction());
		assertEquals("next <- extFlow", extFlow_next, nextToExtFlow.getNextExternalFlow());

		// Ensure flow to function connected
		assertEquals("functionFlow <- function", functionFlow_function, flowToFunction.getFunctionFlow());
		assertEquals("functionFlow -> function", function_functionFlow, flowToFunction.getFunction());

		// Ensure escalation to function connected
		assertEquals("functionEscalation <- function", functionEscalation_function,
				escalationToFunction.getEscalation());
		assertEquals("functionEscalation -> function", function_functionEscalation, escalationToFunction.getFunction());

		// Ensure escalation to external flow connected
		assertEquals("functionEscalation <- extFlow", functionEscalation_extFlow,
				escalationToExtFlow.getFunctionEscalation());
		assertEquals("functionEscalation -> extFlow", extFlow_functionEscalation,
				escalationToExtFlow.getExternalFlow());

		// Ensure the next function connected
		assertEquals("next <- function", function, nextToFunction.getPreviousFunction());
		assertEquals("next -> function", function_next, nextToFunction.getNextFunction());

		// Ensure the functions are connected to their namespace functions
		assertEquals("function <- managedFunction", managedFunction,
				function.getManagedFunction().getManagedFunction());
		assertEquals("function -> managedFunction", function, managedFunction.getFunctions().get(0).getFunction());
	}

	/**
	 * Ensures on storing a {@link DeskModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreDesk() throws Exception {

		// Create the desk (without connections)
		DeskModel desk = new DeskModel();
		DeskManagedObjectSourceModel mos = new DeskManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Object.class.getName(), "0");
		desk.addDeskManagedObjectSource(mos);
		DeskManagedObjectSourceFlowModel mosFlow = new DeskManagedObjectSourceFlowModel("MOS_FLOW",
				Object.class.getName());
		mos.addDeskManagedObjectSourceFlow(mosFlow);
		DeskManagedObjectModel mo = new DeskManagedObjectModel("MANAGED_OBJECT", "THREAD");
		desk.addDeskManagedObject(mo);
		DeskManagedObjectDependencyModel dependency = new DeskManagedObjectDependencyModel("DEPENDENCY", "THREAD");
		mo.addDeskManagedObjectDependency(dependency);
		FunctionNamespaceModel namespace = new FunctionNamespaceModel("NAMESPACE",
				"net.example.ExampleManagedFunctionSource");
		desk.addFunctionNamespace(namespace);
		ManagedFunctionModel managedFunction = new ManagedFunctionModel("MANAGED_FUNCTION");
		namespace.addManagedFunction(managedFunction);
		ManagedFunctionObjectModel functionObject = new ManagedFunctionObjectModel("OBJECT", null,
				Object.class.getName(), false);
		managedFunction.addManagedFunctionObject(functionObject);
		FunctionModel function = new FunctionModel("FUNCTION", false, "NAMESPACE", "MANAGED_FUNCTION",
				Object.class.getName());
		desk.addFunction(function);
		FunctionFlowModel functionFlow = new FunctionFlowModel("FLOW", null, String.class.getName());
		function.addFunctionFlow(functionFlow);
		FunctionEscalationModel functionEscalation = new FunctionEscalationModel(Exception.class.getName());
		function.addFunctionEscalation(functionEscalation);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT",
				Object.class.getName());
		desk.addExternalManagedObject(extMo);
		ExternalFlowModel extFlow = new ExternalFlowModel("EXTERNAL_FLOW", Object.class.getName());
		desk.addExternalFlow(extFlow);

		// managed object -> managed object source
		DeskManagedObjectToDeskManagedObjectSourceModel moToMos = new DeskManagedObjectToDeskManagedObjectSourceModel();
		moToMos.setDeskManagedObject(mo);
		moToMos.setDeskManagedObjectSource(mos);
		moToMos.connect();

		// managed object source flow -> external flow
		DeskManagedObjectSourceFlowToExternalFlowModel mosFlowToExtFlow = new DeskManagedObjectSourceFlowToExternalFlowModel();
		mosFlowToExtFlow.setDeskManagedObjectSourceFlow(mosFlow);
		mosFlowToExtFlow.setExternalFlow(extFlow);
		mosFlowToExtFlow.connect();

		// managed object source flow -> function
		DeskManagedObjectSourceFlowToFunctionModel mosFlowToFunction = new DeskManagedObjectSourceFlowToFunctionModel();
		mosFlowToFunction.setDeskManagedObjectSourceFlow(mosFlow);
		mosFlowToFunction.setFunction(function);
		mosFlowToFunction.connect();

		// dependency -> extMo
		DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new DeskManagedObjectDependencyToExternalManagedObjectModel();
		dependencyToExtMo.setDeskManagedObjectDependency(dependency);
		dependencyToExtMo.setExternalManagedObject(extMo);
		dependencyToExtMo.connect();

		// dependency -> mo
		DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToMo = new DeskManagedObjectDependencyToDeskManagedObjectModel();
		dependencyToMo.setDeskManagedObjectDependency(dependency);
		dependencyToMo.setDeskManagedObject(mo);
		dependencyToMo.connect();

		// functionObject -> extMo
		ManagedFunctionObjectToExternalManagedObjectModel objectToExtMo = new ManagedFunctionObjectToExternalManagedObjectModel();
		objectToExtMo.setManagedFunctionObject(functionObject);
		objectToExtMo.setExternalManagedObject(extMo);
		objectToExtMo.connect();

		// functionObject -> mo
		ManagedFunctionObjectToDeskManagedObjectModel objectToMo = new ManagedFunctionObjectToDeskManagedObjectModel();
		objectToMo.setManagedFunctionObject(functionObject);
		objectToMo.setDeskManagedObject(mo);
		objectToMo.connect();

		// functionFlow -> extFlow
		FunctionFlowToExternalFlowModel flowToExtFlow = new FunctionFlowToExternalFlowModel();
		flowToExtFlow.setFunctionFlow(functionFlow);
		flowToExtFlow.setExternalFlow(extFlow);
		flowToExtFlow.connect();

		// next -> extFlow
		FunctionToNextExternalFlowModel nextToExtFlow = new FunctionToNextExternalFlowModel();
		nextToExtFlow.setPreviousFunction(function);
		nextToExtFlow.setNextExternalFlow(extFlow);
		nextToExtFlow.connect();

		// functionFlow -> function
		FunctionFlowToFunctionModel flowToFunction = new FunctionFlowToFunctionModel();
		flowToFunction.setFunctionFlow(functionFlow);
		flowToFunction.setFunction(function);
		flowToFunction.connect();

		// functionEscalation -> function
		FunctionEscalationToFunctionModel escalationToFunction = new FunctionEscalationToFunctionModel();
		escalationToFunction.setEscalation(functionEscalation);
		escalationToFunction.setFunction(function);
		escalationToFunction.connect();

		// functionEscalation -> extFlow
		FunctionEscalationToExternalFlowModel escalationToExtFlow = new FunctionEscalationToExternalFlowModel();
		escalationToExtFlow.setFunctionEscalation(functionEscalation);
		escalationToExtFlow.setExternalFlow(extFlow);
		escalationToExtFlow.connect();

		// next -> function
		FunctionToNextFunctionModel nextToFunction = new FunctionToNextFunctionModel();
		nextToFunction.setPreviousFunction(function);
		nextToFunction.setNextFunction(function);
		nextToFunction.connect();

		// Record storing the desk
		this.modelRepository.store(desk, this.configurationItem);

		// Store the desk
		this.replayMockObjects();
		this.deskRepository.storeDesk(desk, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("mo - mos", "MANAGED_OBJECT_SOURCE", moToMos.getDeskManagedObjectSourceName());
		assertEquals("mosFlow - extFlow", "EXTERNAL_FLOW", mosFlowToExtFlow.getExternalFlowName());
		assertEquals("mosFlow - function", "FUNCTION", mosFlowToFunction.getFunctionName());
		assertEquals("dependency - extMo", "EXTERNAL_MANAGED_OBJECT", dependencyToExtMo.getExternalManagedObjectName());
		assertEquals("dependency - mo", "MANAGED_OBJECT", dependencyToMo.getDeskManagedObjectName());
		assertEquals("functionObject - extMo", "EXTERNAL_MANAGED_OBJECT", objectToExtMo.getExternalManagedObjectName());
		assertEquals("functionObject - mo", "MANAGED_OBJECT", objectToMo.getDeskManagedObjectName());
		assertEquals("functionFlow - extFlow", "EXTERNAL_FLOW", flowToExtFlow.getExternalFlowName());
		assertEquals("next - extFlow", "EXTERNAL_FLOW", nextToExtFlow.getExternalFlowName());
		assertEquals("flow - function", "FUNCTION", flowToFunction.getFunctionName());
		assertEquals("escalation - function", "FUNCTION", escalationToFunction.getFunctionName());
		assertEquals("escalation - extFlow", "EXTERNAL_FLOW", escalationToExtFlow.getExternalFlowName());
		assertEquals("next - function", "FUNCTION", nextToFunction.getNextFunctionName());
	}

}