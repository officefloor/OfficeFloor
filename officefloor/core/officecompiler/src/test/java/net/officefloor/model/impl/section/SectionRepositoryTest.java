/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.section;

import java.sql.Connection;

import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionEscalationToSubSectionInputModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionFlowToSubSectionInputModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.FunctionToNextSubSectionInputModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectSourceToSectionManagedObjectPoolModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionRepository;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToFunctionModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * Tests the {@link SectionRepository}.
 *
 * @author Daniel Sagenschneider
 */
public class SectionRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this.createMock(ModelRepository.class);

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private final WritableConfigurationItem configurationItem = this.createMock(WritableConfigurationItem.class);

	/**
	 * {@link SectionRepository} to be tested.
	 */
	private final SectionRepository sectionRepository = new SectionRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link SectionModel} that all {@link ConnectionModel}
	 * instances are connected.
	 */
	public void testRetrieveSection() throws Exception {

		// Create the raw section to be connected
		SectionModel section = new SectionModel();
		SubSectionModel subSection = new SubSectionModel("SUB_SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION");
		section.addSubSection(subSection);
		SubSectionInputModel input = new SubSectionInputModel("INPUT", Integer.class.getName(), false, null);
		subSection.addSubSectionInput(input);
		SubSectionOutputModel output = new SubSectionOutputModel("OUTPUT", Integer.class.getName(), false);
		subSection.addSubSectionOutput(output);
		ExternalFlowModel extFlow = new ExternalFlowModel("EXTERNAL_FLOW", Integer.class.getName());
		section.addExternalFlow(extFlow);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT",
				Object.class.getName());
		section.addExternalManagedObject(extMo);
		SectionManagedObjectModel mo = new SectionManagedObjectModel("MANAGED_OBJECT", "THREAD");
		section.addSectionManagedObject(mo);
		SectionManagedObjectPoolModel pool = new SectionManagedObjectPoolModel("POOL",
				"net.example.ExampleManagedObjectPoolSource");
		section.addSectionManagedObjectPool(pool);
		SectionManagedObjectDependencyModel dependency = new SectionManagedObjectDependencyModel("DEPENDENCY",
				Object.class.getName());
		mo.addSectionManagedObjectDependency(dependency);
		SectionManagedObjectSourceModel mos = new SectionManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Connection.class.getName(), "0");
		section.addSectionManagedObjectSource(mos);
		SectionManagedObjectSourceFlowModel mosFlow = new SectionManagedObjectSourceFlowModel("FLOW",
				Object.class.getName());
		mos.addSectionManagedObjectSourceFlow(mosFlow);
		FunctionNamespaceModel namespace = new FunctionNamespaceModel("NAMESPACE",
				"net.example.ExampleManagedFunctionSource");
		section.addFunctionNamespace(namespace);
		ManagedFunctionModel managedFunction = new ManagedFunctionModel("MANAGED_FUNCTION");
		namespace.addManagedFunction(managedFunction);
		FunctionModel function = new FunctionModel("FUNCTION", false, "NAMESPACE", "MANAGED_FUNCTION",
				Object.class.getName());
		section.addFunction(function);
		ManagedFunctionObjectModel functionObject = new ManagedFunctionObjectModel();
		managedFunction.addManagedFunctionObject(functionObject);

		// managed object -> managed object source
		SectionManagedObjectToSectionManagedObjectSourceModel moToMos = new SectionManagedObjectToSectionManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		mo.setSectionManagedObjectSource(moToMos);

		// managed object source -> managed object pool
		SectionManagedObjectSourceToSectionManagedObjectPoolModel mosToPool = new SectionManagedObjectSourceToSectionManagedObjectPoolModel(
				"POOL");
		mos.setSectionManagedObjectPool(mosToPool);

		// dependency -> external managed object
		SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new SectionManagedObjectDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		dependency.setExternalManagedObject(dependencyToExtMo);

		// dependency -> managed object
		SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToMo = new SectionManagedObjectDependencyToSectionManagedObjectModel(
				"MANAGED_OBJECT");
		dependency.setSectionManagedObject(dependencyToMo);

		// managed object source flow -> external flow
		SectionManagedObjectSourceFlowToExternalFlowModel mosFlowToExtFlow = new SectionManagedObjectSourceFlowToExternalFlowModel(
				"EXTERNAL_FLOW");
		mosFlow.setExternalFlow(mosFlowToExtFlow);

		// managed object source flow -> sub section input
		SectionManagedObjectSourceFlowToSubSectionInputModel mosFlowToInput = new SectionManagedObjectSourceFlowToSubSectionInputModel(
				"SUB_SECTION", "INPUT");
		mosFlow.setSubSectionInput(mosFlowToInput);

		// managed object source flow -> function
		SectionManagedObjectSourceFlowToFunctionModel mosFlowToFunction = new SectionManagedObjectSourceFlowToFunctionModel(
				"FUNCTION");
		mosFlow.setFunction(mosFlowToFunction);

		// section object -> extMo
		SubSectionObjectModel sectionObject_extMo = new SubSectionObjectModel("OBJECT_EXT", Object.class.getName());
		subSection.addSubSectionObject(sectionObject_extMo);
		SubSectionObjectToExternalManagedObjectModel sectionObjectToExtMo = new SubSectionObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		sectionObject_extMo.setExternalManagedObject(sectionObjectToExtMo);

		// section object -> managed object
		SubSectionObjectModel sectionObject_mo = new SubSectionObjectModel("OBJECT_MO", Connection.class.getName());
		subSection.addSubSectionObject(sectionObject_mo);
		SubSectionObjectToSectionManagedObjectModel sectionObjectToMo = new SubSectionObjectToSectionManagedObjectModel(
				"MANAGED_OBJECT");
		sectionObject_mo.setSectionManagedObject(sectionObjectToMo);

		// output -> extFlow
		SubSectionOutputModel output_extFlow = new SubSectionOutputModel("OUTPUT_EXTERNAL_FLOW",
				Exception.class.getName(), true);
		subSection.addSubSectionOutput(output_extFlow);
		SubSectionOutputToExternalFlowModel outputToExtFlow = new SubSectionOutputToExternalFlowModel("EXTERNAL_FLOW");
		output_extFlow.setExternalFlow(outputToExtFlow);

		// output -> input
		SubSectionOutputToSubSectionInputModel outputToInput = new SubSectionOutputToSubSectionInputModel("SUB_SECTION",
				"INPUT");
		output.setSubSectionInput(outputToInput);

		// output -> function
		SubSectionOutputModel output_function = new SubSectionOutputModel("OUTPUT_FUNCTION", Integer.class.getName(),
				false);
		subSection.addSubSectionOutput(output_function);
		SubSectionOutputToFunctionModel outputToFunction = new SubSectionOutputToFunctionModel("FUNCTION");
		output_function.setFunction(outputToFunction);

		// functionObject -> extMo
		ManagedFunctionObjectToExternalManagedObjectModel functionObjectToExtMo = new ManagedFunctionObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		functionObject.setExternalManagedObject(functionObjectToExtMo);

		// functionObject -> managed object
		ManagedFunctionObjectToSectionManagedObjectModel functionObjectToMo = new ManagedFunctionObjectToSectionManagedObjectModel(
				"MANAGED_OBJECT");
		functionObject.setSectionManagedObject(functionObjectToMo);

		// functionFlow -> extFlow
		FunctionFlowModel functionFlow_extFlow = new FunctionFlowModel();
		function.addFunctionFlow(functionFlow_extFlow);
		ExternalFlowModel extFlow_functionFlow = new ExternalFlowModel("functionFlow - extFlow",
				String.class.getName());
		section.addExternalFlow(extFlow_functionFlow);
		FunctionFlowToExternalFlowModel functionFlowToExtFlow = new FunctionFlowToExternalFlowModel(
				"functionFlow - extFlow", false);
		functionFlow_extFlow.setExternalFlow(functionFlowToExtFlow);

		// functionFlow -> input
		FunctionFlowModel functionFlow_input = new FunctionFlowModel();
		function.addFunctionFlow(functionFlow_input);
		FunctionFlowToSubSectionInputModel functionFlowToInput = new FunctionFlowToSubSectionInputModel("SUB_SECTION",
				"INPUT", true);
		functionFlow_input.setSubSectionInput(functionFlowToInput);

		// functionFlow -> function
		FunctionFlowModel functionFlow_function = new FunctionFlowModel();
		function.addFunctionFlow(functionFlow_function);
		FunctionModel function_functionFlow = new FunctionModel("flow - function", false, "namespace",
				"managed_function", Object.class.getName());
		section.addFunction(function_functionFlow);
		FunctionFlowToFunctionModel functionFlowToFunction = new FunctionFlowToFunctionModel("flow - function", true);
		functionFlow_function.setFunction(functionFlowToFunction);

		// next -> extFlow
		ExternalFlowModel extFlow_next = new ExternalFlowModel("next - extFlow", Integer.class.getName());
		section.addExternalFlow(extFlow_next);
		FunctionToNextExternalFlowModel nextToExtFlow = new FunctionToNextExternalFlowModel("next - extFlow");
		function.setNextExternalFlow(nextToExtFlow);

		// next -> input
		FunctionToNextSubSectionInputModel nextToInput = new FunctionToNextSubSectionInputModel("SUB_SECTION", "INPUT");
		function.setNextSubSectionInput(nextToInput);

		// next -> function
		FunctionModel function_next = new FunctionModel("next - function", false, "namespace", "managed_function",
				Integer.class.getName());
		section.addFunction(function_next);
		FunctionToNextFunctionModel nextToFunction = new FunctionToNextFunctionModel("next - function");
		function.setNextFunction(nextToFunction);

		// functionEscalation -> extFlow
		FunctionEscalationModel functionEscalation_extFlow = new FunctionEscalationModel();
		function.addFunctionEscalation(functionEscalation_extFlow);
		ExternalFlowModel extFlow_functionEscalation = new ExternalFlowModel("escalation - extFlow",
				Throwable.class.getName());
		section.addExternalFlow(extFlow_functionEscalation);
		FunctionEscalationToExternalFlowModel escalationToExtFlow = new FunctionEscalationToExternalFlowModel(
				"escalation - extFlow");
		functionEscalation_extFlow.setExternalFlow(escalationToExtFlow);

		// functionEscalation -> input
		FunctionEscalationModel functionEscalation_input = new FunctionEscalationModel();
		function.addFunctionEscalation(functionEscalation_input);
		FunctionEscalationToSubSectionInputModel escalationToInput = new FunctionEscalationToSubSectionInputModel(
				"SUB_SECTION", "INPUT");
		functionEscalation_input.setSubSectionInput(escalationToInput);

		// functionEscalation -> function
		FunctionEscalationModel functionEscalation_function = new FunctionEscalationModel();
		function.addFunctionEscalation(functionEscalation_function);
		FunctionModel function_functionEscalation = new FunctionModel("escalation - function", false, "NAMESPACE",
				"MANAGED_FUNCTION", Object.class.getName());
		section.addFunction(function_functionEscalation);
		FunctionEscalationToFunctionModel escalationToFunction = new FunctionEscalationToFunctionModel(
				"escalation - function");
		functionEscalation_function.setFunction(escalationToFunction);

		// Record retrieving the section
		this.modelRepository.retrieve(this.paramType(SectionModel.class), this.param(this.configurationItem));

		// Retrieve the section
		this.replayMockObjects();
		this.sectionRepository.retrieveSection(section, this.configurationItem);
		this.verifyMockObjects();

		// Ensure managed object connected to its source
		assertEquals("mo -> mos", mos, moToMos.getSectionManagedObjectSource());
		assertEquals("mo <- mos", mo, moToMos.getSectionManagedObject());

		// Ensure managed object source connected to its pool
		assertEquals("mos -> pool", pool, mosToPool.getSectionManagedObjectPool());
		assertEquals("mos <- pool", mos, mosToPool.getSectionManagedObjectSource());

		// Ensure dependency connected to external managed object
		assertEquals("dependency <- external mo", dependency, dependencyToExtMo.getSectionManagedObjectDependency());
		assertEquals("dependency -> managed object", extMo, dependencyToExtMo.getExternalManagedObject());

		// Ensure dependency connected to managed object
		assertEquals("dependency <- managed object", dependency, dependencyToMo.getSectionManagedObjectDependency());
		assertEquals("dependency -> managed object", mo, dependencyToMo.getSectionManagedObject());

		// Ensure managed object source flow connected to external flow
		assertEquals("mos flow <- external flow", mosFlow, mosFlowToExtFlow.getSectionManagedObjectSourceFlow());
		assertEquals("mos flow -> external flow", extFlow, mosFlowToExtFlow.getExternalFlow());

		// Ensure managed object source flow connected to sub section input
		assertEquals("mos flow <- sub section input", mosFlow, mosFlowToInput.getSectionManagedObjectSourceFlow());
		assertEquals("mos flow -> sub section input", input, mosFlowToInput.getSubSectionInput());

		// Ensure managed object source flow connected to function
		assertEquals("mos flow <- function", mosFlow, mosFlowToFunction.getSectionManagedObjectSourceFlow());
		assertEquals("mos flow -> function", function, mosFlowToFunction.getFunction());

		// Ensure section object to external managed object connected
		assertEquals("section object -> extMo", extMo, sectionObjectToExtMo.getExternalManagedObject());
		assertEquals("section object <- extMo", sectionObject_extMo, sectionObjectToExtMo.getSubSectionObject());

		// Ensure section object to section managed object connected
		assertEquals("section object -> mo", mo, sectionObjectToMo.getSectionManagedObject());
		assertEquals("section object <- mo", sectionObject_mo, sectionObjectToMo.getSubSectionObject());

		// Ensure output to external flow connected
		assertEquals("section output -> extFlow", extFlow, outputToExtFlow.getExternalFlow());
		assertEquals("section output <- extFlow", output_extFlow, outputToExtFlow.getSubSectionOutput());

		// Ensure output to input connected
		assertEquals("section output -> input", input, outputToInput.getSubSectionInput());
		assertEquals("section output <- input", output, outputToInput.getSubSectionOutput());

		// Ensure output to function connected
		assertEquals("section output -> function", function, outputToFunction.getFunction());
		assertEquals("section output <- function", output_function, outputToFunction.getSubSectionOutput());

		// Ensure the functions are connected to their managed functions
		assertEquals("function <- managedFunction", managedFunction,
				function.getManagedFunction().getManagedFunction());
		assertEquals("function -> managedFunction", function, managedFunction.getFunctions().get(0).getFunction());

		// Ensure the external managed object connected
		assertEquals("function object <- extMo", functionObject, functionObjectToExtMo.getManagedFunctionObject());
		assertEquals("function object -> extMo", extMo, functionObjectToExtMo.getExternalManagedObject());

		// Ensure function object connected to managed object
		assertEquals("function object <- managed object", functionObject,
				functionObjectToMo.getManagedFunctionObject());
		assertEquals("function object -> managed object", mo, functionObjectToMo.getSectionManagedObject());

		// Ensure function flow to external flow connected
		assertEquals("function flow <- extFlow", functionFlow_extFlow, functionFlowToExtFlow.getFunctionFlow());
		assertEquals("function flow -> extFlow", extFlow_functionFlow, functionFlowToExtFlow.getExternalFlow());

		// Ensure function flow to input connected
		assertEquals("function flow <- input", functionFlow_input, functionFlowToInput.getFunctionFlow());
		assertEquals("function flow -> input", input, functionFlowToInput.getSubSectionInput());

		// Ensure function flow to function connected
		assertEquals("function flow <- function", functionFlow_function, functionFlowToFunction.getFunctionFlow());
		assertEquals("function flow -> function", function_functionFlow, functionFlowToFunction.getFunction());

		// Ensure next to external flow connected
		assertEquals("next -> extFlow", function, nextToExtFlow.getPreviousFunction());
		assertEquals("next <- extFlow", extFlow_next, nextToExtFlow.getNextExternalFlow());

		// Ensure next to section input connected
		assertEquals("next -> input", function, nextToInput.getPreviousFunction());
		assertEquals("next <- input", input, nextToInput.getNextSubSectionInput());

		// Ensure next to function connected
		assertEquals("next <- function", function, nextToFunction.getPreviousFunction());
		assertEquals("next -> function", function_next, nextToFunction.getNextFunction());

		// Ensure escalation to external flow connected
		assertEquals("function escalation <- extFlow", functionEscalation_extFlow,
				escalationToExtFlow.getFunctionEscalation());
		assertEquals("function escalation -> extFlow", extFlow_functionEscalation,
				escalationToExtFlow.getExternalFlow());

		// Ensure escalation to input connected
		assertEquals("function escalation <- input", functionEscalation_input,
				escalationToInput.getFunctionEscalation());
		assertEquals("function escalation -> input", input, escalationToInput.getSubSectionInput());

		// Ensure escalation to function connected
		assertEquals("function escalation <- function", functionEscalation_function,
				escalationToFunction.getEscalation());
		assertEquals("function escalation -> function", function_functionEscalation,
				escalationToFunction.getFunction());
	}

	/**
	 * Ensures on storing a {@link SectionModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreSection() throws Exception {

		// Create the section (without connections)
		SectionModel section = new SectionModel();
		SubSectionModel subSection = new SubSectionModel("SUB_SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION");
		section.addSubSection(subSection);
		SubSectionInputModel input = new SubSectionInputModel("INPUT", Integer.class.getName(), false, null);
		subSection.addSubSectionInput(input);
		SubSectionOutputModel output_extFlow = new SubSectionOutputModel("OUTPUT_EXTERNAL_FLOW",
				Exception.class.getName(), true);
		subSection.addSubSectionOutput(output_extFlow);
		SubSectionOutputModel output_input = new SubSectionOutputModel("OUTPUT_INPUT", Integer.class.getName(), false);
		subSection.addSubSectionOutput(output_input);
		SubSectionOutputModel output_function = new SubSectionOutputModel("OUTPUT_FUNCTION", String.class.getName(),
				false);
		subSection.addSubSectionOutput(output_function);
		ExternalFlowModel extFlow = new ExternalFlowModel("EXTERNAL_FLOW", Integer.class.getName());
		section.addExternalFlow(extFlow);
		SubSectionObjectModel object = new SubSectionObjectModel("OBJECT", Object.class.getName());
		subSection.addSubSectionObject(object);
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT",
				Object.class.getName());
		section.addExternalManagedObject(extMo);
		SectionManagedObjectModel mo = new SectionManagedObjectModel("MANAGED_OBJECT", "THREAD");
		section.addSectionManagedObject(mo);
		SectionManagedObjectPoolModel pool = new SectionManagedObjectPoolModel("POOL",
				"net.example.ExampleManagedObjectPoolSource");
		section.addSectionManagedObjectPool(pool);
		SectionManagedObjectDependencyModel dependency = new SectionManagedObjectDependencyModel("DEPENDENCY",
				Object.class.getName());
		mo.addSectionManagedObjectDependency(dependency);
		SectionManagedObjectSourceModel mos = new SectionManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", Connection.class.getName(), "0");
		section.addSectionManagedObjectSource(mos);
		SectionManagedObjectSourceFlowModel mosFlow = new SectionManagedObjectSourceFlowModel("MOS_FLOW",
				Object.class.getName());
		mos.addSectionManagedObjectSourceFlow(mosFlow);
		FunctionNamespaceModel namespace = new FunctionNamespaceModel("NAMESPACE",
				"net.example.ExampleManagedFunctionSource");
		section.addFunctionNamespace(namespace);
		ManagedFunctionModel managedFunction = new ManagedFunctionModel("MANAGED_FUNCTION");
		namespace.addManagedFunction(managedFunction);
		ManagedFunctionObjectModel functionObject = new ManagedFunctionObjectModel("OBJECT", null,
				Object.class.getName(), false);
		managedFunction.addManagedFunctionObject(functionObject);
		FunctionModel function = new FunctionModel("FUNCTION", false, "NAMESPACE", "MANAGED_FUNCTION",
				Object.class.getName());
		section.addFunction(function);
		FunctionFlowModel functionFlow = new FunctionFlowModel("FLOW", null, String.class.getName());
		function.addFunctionFlow(functionFlow);
		FunctionEscalationModel functionEscalation = new FunctionEscalationModel(Exception.class.getName());
		function.addFunctionEscalation(functionEscalation);
		section.addExternalManagedObject(extMo);
		section.addExternalFlow(extFlow);

		// mo -> mos
		SectionManagedObjectToSectionManagedObjectSourceModel moToMos = new SectionManagedObjectToSectionManagedObjectSourceModel();
		moToMos.setSectionManagedObject(mo);
		moToMos.setSectionManagedObjectSource(mos);
		moToMos.connect();

		// mos -> pool
		SectionManagedObjectSourceToSectionManagedObjectPoolModel mosToPool = new SectionManagedObjectSourceToSectionManagedObjectPoolModel();
		mosToPool.setSectionManagedObjectSource(mos);
		mosToPool.setSectionManagedObjectPool(pool);
		mosToPool.connect();

		// dependency -> extMo
		SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new SectionManagedObjectDependencyToExternalManagedObjectModel();
		dependencyToExtMo.setSectionManagedObjectDependency(dependency);
		dependencyToExtMo.setExternalManagedObject(extMo);
		dependencyToExtMo.connect();

		// dependency -> mo
		SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToMo = new SectionManagedObjectDependencyToSectionManagedObjectModel();
		dependencyToMo.setSectionManagedObjectDependency(dependency);
		dependencyToMo.setSectionManagedObject(mo);
		dependencyToMo.connect();

		// mos flow -> external flow
		SectionManagedObjectSourceFlowToExternalFlowModel mosFlowToExtFlow = new SectionManagedObjectSourceFlowToExternalFlowModel();
		mosFlowToExtFlow.setSectionManagedObjectSourceFlow(mosFlow);
		mosFlowToExtFlow.setExternalFlow(extFlow);
		mosFlowToExtFlow.connect();

		// mos flow -> sub section input
		SectionManagedObjectSourceFlowToSubSectionInputModel mosFlowToInput = new SectionManagedObjectSourceFlowToSubSectionInputModel();
		mosFlowToInput.setSectionManagedObjectSourceFlow(mosFlow);
		mosFlowToInput.setSubSectionInput(input);
		mosFlowToInput.connect();

		// managed object source flow -> function
		SectionManagedObjectSourceFlowToFunctionModel mosFlowToFunction = new SectionManagedObjectSourceFlowToFunctionModel();
		mosFlowToFunction.setSectionManagedObjectSourceFlow(mosFlow);
		mosFlowToFunction.setFunction(function);
		mosFlowToFunction.connect();

		// section object -> extMo
		SubSectionObjectToExternalManagedObjectModel sectionObjectToExtMo = new SubSectionObjectToExternalManagedObjectModel();
		sectionObjectToExtMo.setSubSectionObject(object);
		sectionObjectToExtMo.setExternalManagedObject(extMo);
		sectionObjectToExtMo.connect();

		// section object -> mo
		SubSectionObjectToSectionManagedObjectModel sectionObjectToMo = new SubSectionObjectToSectionManagedObjectModel();
		sectionObjectToMo.setSubSectionObject(object);
		sectionObjectToMo.setSectionManagedObject(mo);
		sectionObjectToMo.connect();

		// output -> extFlow
		SubSectionOutputToExternalFlowModel outputToExtFlow = new SubSectionOutputToExternalFlowModel();
		outputToExtFlow.setSubSectionOutput(output_extFlow);
		outputToExtFlow.setExternalFlow(extFlow);
		outputToExtFlow.connect();

		// output -> input
		SubSectionOutputToSubSectionInputModel outputToInput = new SubSectionOutputToSubSectionInputModel();
		outputToInput.setSubSectionOutput(output_input);
		outputToInput.setSubSectionInput(input);
		outputToInput.connect();

		// output -> function
		SubSectionOutputToFunctionModel outputToFunction = new SubSectionOutputToFunctionModel();
		outputToFunction.setSubSectionOutput(output_function);
		outputToFunction.setFunction(function);
		outputToFunction.connect();

		// functionObject -> extMo
		ManagedFunctionObjectToExternalManagedObjectModel functionObjectToExtMo = new ManagedFunctionObjectToExternalManagedObjectModel();
		functionObjectToExtMo.setManagedFunctionObject(functionObject);
		functionObjectToExtMo.setExternalManagedObject(extMo);
		functionObjectToExtMo.connect();

		// functionObject -> mo
		ManagedFunctionObjectToSectionManagedObjectModel functionObjectToMo = new ManagedFunctionObjectToSectionManagedObjectModel();
		functionObjectToMo.setManagedFunctionObject(functionObject);
		functionObjectToMo.setSectionManagedObject(mo);
		functionObjectToMo.connect();

		// functionFlow -> extFlow
		FunctionFlowToExternalFlowModel functionFlowToExtFlow = new FunctionFlowToExternalFlowModel();
		functionFlowToExtFlow.setFunctionFlow(functionFlow);
		functionFlowToExtFlow.setExternalFlow(extFlow);
		functionFlowToExtFlow.connect();

		// functionFlow -> input
		FunctionFlowToSubSectionInputModel functionFlowToInput = new FunctionFlowToSubSectionInputModel();
		functionFlowToInput.setFunctionFlow(functionFlow);
		functionFlowToInput.setSubSectionInput(input);
		functionFlowToInput.connect();

		// functionFlow -> function
		FunctionFlowToFunctionModel functionFlowToFunction = new FunctionFlowToFunctionModel();
		functionFlowToFunction.setFunctionFlow(functionFlow);
		functionFlowToFunction.setFunction(function);
		functionFlowToFunction.connect();

		// next -> extFlow
		FunctionToNextExternalFlowModel nextToExtFlow = new FunctionToNextExternalFlowModel();
		nextToExtFlow.setPreviousFunction(function);
		nextToExtFlow.setNextExternalFlow(extFlow);
		nextToExtFlow.connect();

		// next -> input
		FunctionToNextSubSectionInputModel nextToInput = new FunctionToNextSubSectionInputModel();
		nextToInput.setPreviousFunction(function);
		nextToInput.setNextSubSectionInput(input);
		nextToInput.connect();

		// next -> function
		FunctionToNextFunctionModel nextToFunction = new FunctionToNextFunctionModel();
		nextToFunction.setPreviousFunction(function);
		nextToFunction.setNextFunction(function);
		nextToFunction.connect();

		// functionEscalation -> extFlow
		FunctionEscalationToExternalFlowModel escalationToExtFlow = new FunctionEscalationToExternalFlowModel();
		escalationToExtFlow.setFunctionEscalation(functionEscalation);
		escalationToExtFlow.setExternalFlow(extFlow);
		escalationToExtFlow.connect();

		// functionEscalation -> input
		FunctionEscalationToSubSectionInputModel escalationToInput = new FunctionEscalationToSubSectionInputModel();
		escalationToInput.setFunctionEscalation(functionEscalation);
		escalationToInput.setSubSectionInput(input);
		escalationToInput.connect();

		// functionEscalation -> function
		FunctionEscalationToFunctionModel escalationToFunction = new FunctionEscalationToFunctionModel();
		escalationToFunction.setEscalation(functionEscalation);
		escalationToFunction.setFunction(function);
		escalationToFunction.connect();

		// Record storing the section
		this.modelRepository.store(section, this.configurationItem);

		// Store the section
		this.replayMockObjects();
		this.sectionRepository.storeSection(section, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("mo - mos", "MANAGED_OBJECT_SOURCE", moToMos.getSectionManagedObjectSourceName());
		assertEquals("mos - pool", "POOL", mosToPool.getSectionManagedObjectPoolName());
		assertEquals("dependency - extMo", "EXTERNAL_MANAGED_OBJECT", dependencyToExtMo.getExternalManagedObjectName());
		assertEquals("dependency - mo", "MANAGED_OBJECT", dependencyToMo.getSectionManagedObjectName());
		assertEquals("mos flow - extFlow", "EXTERNAL_FLOW", mosFlowToExtFlow.getExternalFlowName());
		assertEquals("mos flow - input (sub section)", "SUB_SECTION", mosFlowToInput.getSubSectionName());
		assertEquals("mos flow - input (input)", "INPUT", mosFlowToInput.getSubSectionInputName());
		assertEquals("mos flow - function", "FUNCTION", mosFlowToFunction.getFunctionName());
		assertEquals("section object - extMo", "EXTERNAL_MANAGED_OBJECT",
				sectionObjectToExtMo.getExternalManagedObjectName());
		assertEquals("section object - mo", "MANAGED_OBJECT", sectionObjectToMo.getSectionManagedObjectName());
		assertEquals("output - extFlow", "EXTERNAL_FLOW", outputToExtFlow.getExternalFlowName());
		assertEquals("output - input (sub section)", "SUB_SECTION", outputToInput.getSubSectionName());
		assertEquals("output - input (input)", "INPUT", outputToInput.getSubSectionInputName());
		assertEquals("output - function", "FUNCTION", outputToFunction.getFunctionName());
		assertEquals("function object - extMo", "EXTERNAL_MANAGED_OBJECT",
				functionObjectToExtMo.getExternalManagedObjectName());
		assertEquals("function object - mo", "MANAGED_OBJECT", functionObjectToMo.getSectionManagedObjectName());
		assertEquals("function flow - extFlow", "EXTERNAL_FLOW", functionFlowToExtFlow.getExternalFlowName());
		assertEquals("function flow - input (sub section)", "SUB_SECTION", functionFlowToInput.getSubSectionName());
		assertEquals("function flow - input (input)", "INPUT", functionFlowToInput.getSubSectionInputName());
		assertEquals("function flow - function", "FUNCTION", functionFlowToFunction.getFunctionName());
		assertEquals("next - extFlow", "EXTERNAL_FLOW", nextToExtFlow.getExternalFlowName());
		assertEquals("next - input (sub section)", "SUB_SECTION", nextToInput.getSubSectionName());
		assertEquals("next - input (input)", "INPUT", nextToInput.getSubSectionInputName());
		assertEquals("next - function", "FUNCTION", nextToFunction.getNextFunctionName());
		assertEquals("escalation - extFlow", "EXTERNAL_FLOW", escalationToExtFlow.getExternalFlowName());
		assertEquals("escalation - input (sub section)", "SUB_SECTION", escalationToInput.getSubSectionName());
		assertEquals("escalation - input (input)", "INPUT", escalationToInput.getSubSectionInputName());
		assertEquals("escalation - function", "FUNCTION", escalationToFunction.getFunctionName());
	}

}
