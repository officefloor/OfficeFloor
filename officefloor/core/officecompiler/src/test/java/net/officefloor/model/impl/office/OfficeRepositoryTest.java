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

package net.officefloor.model.impl.office;

import java.io.IOException;
import java.sql.Connection;

import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.AdministrationEscalationModel;
import net.officefloor.model.office.AdministrationEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.AdministrationFlowModel;
import net.officefloor.model.office.AdministrationFlowToOfficeSectionInputModel;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToExternalManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeSectionManagedObjectModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.GovernanceEscalationModel;
import net.officefloor.model.office.GovernanceEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.GovernanceFlowModel;
import net.officefloor.model.office.GovernanceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.GovernanceModel;
import net.officefloor.model.office.GovernanceToExternalManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeSectionManagedObjectModel;
import net.officefloor.model.office.GovernanceToOfficeTeamModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToGovernanceModel;
import net.officefloor.model.office.OfficeFunctionToOfficeTeamModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectPoolModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceToOfficeManagedObjectPoolModel;
import net.officefloor.model.office.OfficeManagedObjectSourceToOfficeSupplierModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToPreLoadAdministrationModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToGovernanceModel;
import net.officefloor.model.office.OfficeSupplierModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSupplierThreadLocalToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the {@link OfficeRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this.createMock(ModelRepository.class);

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private final WritableConfigurationItem configurationItem = this.createMock(WritableConfigurationItem.class);

	/**
	 * {@link OfficeRepository} to be tested.
	 */
	private final OfficeRepository officeRepository = new OfficeRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link OfficeModel} that all {@link ConnectionModel}
	 * instances are connected.
	 */
	public void testRetrieveOffice() throws Exception {

		// Create the raw office to be connected
		OfficeModel office = new OfficeModel();
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT",
				Connection.class.getName());
		OfficeManagedObjectModel mo = new OfficeManagedObjectModel("MANAGED_OBJECT", "THREAD");
		office.addOfficeManagedObject(mo);
		OfficeManagedObjectPoolModel pool = new OfficeManagedObjectPoolModel("POOL",
				"net.example.ExampleManagedObjectPoolSource");
		office.addOfficeManagedObjectPool(pool);
		OfficeSupplierModel supplier = new OfficeSupplierModel("SUPPLIER", "net.example.ExampleSupplierSource");
		office.addOfficeSupplier(supplier);
		OfficeSupplierThreadLocalModel supplierThreadLocal = new OfficeSupplierThreadLocalModel("QUALIFIER", "TYPE");
		supplier.addOfficeSupplierThreadLocal(supplierThreadLocal);
		OfficeManagedObjectDependencyModel dependency = new OfficeManagedObjectDependencyModel("DEPENDENCY",
				Connection.class.getName());
		mo.addOfficeManagedObjectDependency(dependency);
		OfficeManagedObjectSourceModel mos = new OfficeManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", "java.lang.Object", "0");
		office.addOfficeManagedObjectSource(mos);
		OfficeInputManagedObjectDependencyModel inputDependency = new OfficeInputManagedObjectDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		mos.addOfficeInputManagedObjectDependency(inputDependency);
		OfficeManagedObjectFunctionDependencyModel functionDependency = new OfficeManagedObjectFunctionDependencyModel(
				"FUNCTION_DEPENDENCY", Connection.class.getName());
		mos.addOfficeManagedObjectFunctionDependency(functionDependency);
		OfficeManagedObjectSourceFlowModel moFlow = new OfficeManagedObjectSourceFlowModel("FLOW",
				Integer.class.getName());
		mos.addOfficeManagedObjectSourceFlow(moFlow);
		OfficeManagedObjectSourceTeamModel moTeam = new OfficeManagedObjectSourceTeamModel("MO_TEAM");
		mos.addOfficeManagedObjectSourceTeam(moTeam);
		office.addExternalManagedObject(extMo);
		OfficeTeamModel team = new OfficeTeamModel("TEAM");
		office.addOfficeTeam(team);
		OfficeEscalationModel escalation = new OfficeEscalationModel("ESCALATION");
		office.addOfficeEscalation(escalation);
		OfficeSectionModel section = new OfficeSectionModel("SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION");
		office.addOfficeSection(section);
		OfficeSectionObjectModel object = new OfficeSectionObjectModel("OBJECT", Connection.class.getName());
		section.addOfficeSectionObject(object);
		AdministrationModel admin = new AdministrationModel("ADMINISTRATION", "net.example.ExampleAdministrationSource",
				false);
		office.addAdministration(admin);
		AdministrationFlowModel adminFlow = new AdministrationFlowModel("ADMIN_FLOW", null, Integer.class.getName());
		admin.addAdministrationFlow(adminFlow);
		AdministrationEscalationModel adminEscalation = new AdministrationEscalationModel(Exception.class.getName());
		admin.addAdministrationEscalation(adminEscalation);
		GovernanceModel governance = new GovernanceModel("GOVERNANCE", "net.example.ExampleGovernanceSource", false);
		office.addGovernance(governance);
		GovernanceFlowModel govFlow = new GovernanceFlowModel("GOVERNANCE_FLOW", null, Float.class.getName());
		governance.addGovernanceFlow(govFlow);
		GovernanceEscalationModel govEscalation = new GovernanceEscalationModel(IOException.class.getName());
		governance.addGovernanceEscalation(govEscalation);
		OfficeStartModel start = new OfficeStartModel("START");
		office.addOfficeStart(start);
		OfficeSectionModel targetSection = new OfficeSectionModel("SECTION_TARGET", "net.example.ExampleSectionSource",
				"SECTION_LOCATION");
		office.addOfficeSection(targetSection);
		OfficeSectionInputModel targetInput = new OfficeSectionInputModel("INPUT", String.class.getName());
		targetSection.addOfficeSectionInput(targetInput);

		// managed object -> managed object source
		OfficeManagedObjectToOfficeManagedObjectSourceModel moToMos = new OfficeManagedObjectToOfficeManagedObjectSourceModel(
				"MANAGED_OBJECT_SOURCE");
		mo.setOfficeManagedObjectSource(moToMos);

		// managed object source -> managed object pool
		OfficeManagedObjectSourceToOfficeManagedObjectPoolModel mosToPool = new OfficeManagedObjectSourceToOfficeManagedObjectPoolModel(
				"POOL");
		mos.setOfficeManagedObjectPool(mosToPool);

		// managed object source -> supplier
		OfficeManagedObjectSourceToOfficeSupplierModel mosToSupplier = new OfficeManagedObjectSourceToOfficeSupplierModel(
				"SUPPLIER", null, Object.class.getName());
		mos.setOfficeSupplier(mosToSupplier);

		// mo flow -> section input
		OfficeManagedObjectSourceFlowToOfficeSectionInputModel mosFlowToInput = new OfficeManagedObjectSourceFlowToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		moFlow.setOfficeSectionInput(mosFlowToInput);

		// managed object dependency -> external managed object
		OfficeManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new OfficeManagedObjectDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		dependency.setExternalManagedObject(dependencyToExtMo);

		// managed object dependency -> office managed object
		OfficeManagedObjectDependencyToOfficeManagedObjectModel dependencyToMo = new OfficeManagedObjectDependencyToOfficeManagedObjectModel(
				"MANAGED_OBJECT");
		dependency.setOfficeManagedObject(dependencyToMo);

		// supplier thread local -> external managed object
		OfficeSupplierThreadLocalToExternalManagedObjectModel supplierThreadLocalToExtMo = new OfficeSupplierThreadLocalToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		supplierThreadLocal.setExternalManagedObject(supplierThreadLocalToExtMo);

		// supplier thread local -> office managed object
		OfficeSupplierThreadLocalToOfficeManagedObjectModel supplierThreadLocalToMo = new OfficeSupplierThreadLocalToOfficeManagedObjectModel(
				"MANAGED_OBJECT");
		supplierThreadLocal.setOfficeManagedObject(supplierThreadLocalToMo);

		// input managed object dependency -> external managed object
		OfficeInputManagedObjectDependencyToExternalManagedObjectModel inputDependencyToExtMo = new OfficeInputManagedObjectDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		inputDependency.setExternalManagedObject(inputDependencyToExtMo);

		// input managed object dependency -> office managed object
		OfficeInputManagedObjectDependencyToOfficeManagedObjectModel inputDependencyToMo = new OfficeInputManagedObjectDependencyToOfficeManagedObjectModel(
				"MANAGED_OBJECT");
		inputDependency.setOfficeManagedObject(inputDependencyToMo);

		// managed object function dependency -> external managed object
		OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel functionDependencyToExtMo = new OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		functionDependency.setExternalManagedObject(functionDependencyToExtMo);

		// input managed object dependency -> office managed object
		OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel functionDependencyToMo = new OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel(
				"MANAGED_OBJECT");
		functionDependency.setOfficeManagedObject(functionDependencyToMo);

		// managed object -> pre-load administration
		OfficeManagedObjectToPreLoadAdministrationModel moToPreLoadAdmin = new OfficeManagedObjectToPreLoadAdministrationModel(
				"ADMINISTRATION");
		mo.addPreLoadAdministration(moToPreLoadAdmin);

		// mo team -> team
		OfficeManagedObjectSourceTeamToOfficeTeamModel mosTeamToTeam = new OfficeManagedObjectSourceTeamToOfficeTeamModel(
				"TEAM");
		moTeam.setOfficeTeam(mosTeamToTeam);

		// external managed object -> pre-load administration
		ExternalManagedObjectToPreLoadAdministrationModel extMoToPreLoadAdmin = new ExternalManagedObjectToPreLoadAdministrationModel(
				"ADMINISTRATION");
		extMo.addPreLoadAdministration(extMoToPreLoadAdmin);

		// start -> section input
		OfficeStartToOfficeSectionInputModel startToInput = new OfficeStartToOfficeSectionInputModel("SECTION_TARGET",
				"INPUT");
		start.setOfficeSectionInput(startToInput);

		// escalation -> section input
		OfficeEscalationToOfficeSectionInputModel escalationToInput = new OfficeEscalationToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		escalation.setOfficeSectionInput(escalationToInput);

		// section object -> external managed object
		OfficeSectionObjectToExternalManagedObjectModel objectToExtMo = new OfficeSectionObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		object.setExternalManagedObject(objectToExtMo);

		// section object -> office managed object
		OfficeSectionObjectToOfficeManagedObjectModel objectToMo = new OfficeSectionObjectToOfficeManagedObjectModel(
				"MANAGED_OBJECT");
		object.setOfficeManagedObject(objectToMo);

		// section output -> section input
		OfficeSectionOutputModel output = new OfficeSectionOutputModel("OUTPUT", String.class.getName(), false);
		section.addOfficeSectionOutput(output);
		OfficeSectionOutputToOfficeSectionInputModel outputToInput = new OfficeSectionOutputToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		output.setOfficeSectionInput(outputToInput);

		// office function
		OfficeSubSectionModel subSection = new OfficeSubSectionModel();
		section.setOfficeSubSection(subSection);
		OfficeSubSectionModel subSubSection = new OfficeSubSectionModel("SUB_SECTION");
		subSection.addOfficeSubSection(subSubSection);
		OfficeFunctionModel officeFunction = new OfficeFunctionModel("FUNCTION");
		subSubSection.addOfficeFunction(officeFunction);

		// section function -> team
		OfficeFunctionToOfficeTeamModel functionToTeam = new OfficeFunctionToOfficeTeamModel("TEAM");
		officeFunction.setOfficeTeam(functionToTeam);

		// office function -> pre administration
		OfficeFunctionToPreAdministrationModel functionToPreAdmin = new OfficeFunctionToPreAdministrationModel(
				"ADMINISTRATION");
		officeFunction.addPreAdministration(functionToPreAdmin);

		// office function -> post administration
		OfficeFunctionToPostAdministrationModel functionToPostAdmin = new OfficeFunctionToPostAdministrationModel(
				"ADMINISTRATION");
		officeFunction.addPostAdministration(functionToPostAdmin);

		// administration flow -> section input
		AdministrationFlowToOfficeSectionInputModel adminFlowToInput = new AdministrationFlowToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		adminFlow.setOfficeSectionInput(adminFlowToInput);

		// administration escalation -> section input
		AdministrationEscalationToOfficeSectionInputModel adminEscalationToInput = new AdministrationEscalationToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		adminEscalation.setOfficeSectionInput(adminEscalationToInput);

		// administration -> external managed object
		AdministrationToExternalManagedObjectModel extMoToAdmin = new AdministrationToExternalManagedObjectModel(
				"ADMINISTRATION", "1");
		extMo.addAdministration(extMoToAdmin);

		// administration -> managed object
		AdministrationToOfficeManagedObjectModel moToAdmin = new AdministrationToOfficeManagedObjectModel(
				"ADMINISTRATION", "1");
		mo.addAdministration(moToAdmin);

		// administration -> team
		AdministrationToOfficeTeamModel adminToTeam = new AdministrationToOfficeTeamModel("TEAM");
		admin.setOfficeTeam(adminToTeam);

		// section managed object (setup)
		OfficeSectionManagedObjectModel sectionMo = new OfficeSectionManagedObjectModel("SECTION_MO");
		subSection.addOfficeSectionManagedObject(sectionMo);

		// administration -> section managed object
		AdministrationToOfficeSectionManagedObjectModel sectionMoToAdmin = new AdministrationToOfficeSectionManagedObjectModel(
				"ADMINISTRATION", "1");
		sectionMo.addAdministration(sectionMoToAdmin);

		// section managed object -> pre-load administration
		OfficeSectionManagedObjectToPreLoadAdministrationModel sectionMoToPreLoadAdmin = new OfficeSectionManagedObjectToPreLoadAdministrationModel(
				"ADMINISTRATION");
		sectionMo.addPreLoadAdministration(sectionMoToPreLoadAdmin);

		// office function -> governance
		OfficeFunctionToGovernanceModel functionToGov = new OfficeFunctionToGovernanceModel("GOVERNANCE");
		officeFunction.addGovernance(functionToGov);

		// sub section -> governance
		OfficeSubSectionToGovernanceModel subSectionToGov = new OfficeSubSectionToGovernanceModel("GOVERNANCE");
		subSection.addGovernance(subSectionToGov);

		// governance flow -> section input
		GovernanceFlowToOfficeSectionInputModel govFlowToInput = new GovernanceFlowToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		govFlow.setOfficeSectionInput(govFlowToInput);

		// governance escalation -> section input
		GovernanceEscalationToOfficeSectionInputModel govEscalationToInput = new GovernanceEscalationToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		govEscalation.setOfficeSectionInput(govEscalationToInput);

		// governance -> external managed object
		GovernanceToExternalManagedObjectModel extMoToGov = new GovernanceToExternalManagedObjectModel("GOVERNANCE");
		extMo.addGovernance(extMoToGov);

		// governance -> managed object
		GovernanceToOfficeManagedObjectModel moToGov = new GovernanceToOfficeManagedObjectModel("GOVERNANCE");
		mo.addGovernance(moToGov);

		// governance -> section managed object
		GovernanceToOfficeSectionManagedObjectModel sectionMoToGov = new GovernanceToOfficeSectionManagedObjectModel(
				"GOVERNANCE");
		sectionMo.addGovernance(sectionMoToGov);

		// governance -> team
		GovernanceToOfficeTeamModel govToTeam = new GovernanceToOfficeTeamModel("TEAM");
		governance.setOfficeTeam(govToTeam);

		// Record retrieving the office
		this.modelRepository.retrieve(this.paramType(OfficeModel.class), this.param(this.configurationItem));

		// Retrieve the office
		this.replayMockObjects();
		this.officeRepository.retrieveOffice(office, this.configurationItem);
		this.verifyMockObjects();

		// Ensure managed object connected to its managed object source
		assertEquals("managed object <- managed object source", mo, moToMos.getOfficeManagedObject());
		assertEquals("managed object -> managed object source", mos, moToMos.getOfficeManagedObjectSource());

		// Ensure managed object source connected to its managed object pool
		assertEquals("managed object source <- managed object pool", mos, mosToPool.getOfficeManagedObjectSource());
		assertEquals("managed object source -> managed object pool", pool, mosToPool.getOfficeManagedObjectPool());

		// Ensure managed object source connected to its supplier
		assertEquals("managed object source <- supplier", mos, mosToSupplier.getOfficeManagedObjectSource());
		assertEquals("managed object source -> supplier", supplier, mosToSupplier.getOfficeSupplier());

		// Ensure managed object source flow connected to section input
		assertEquals("mos flow <- section input", moFlow, mosFlowToInput.getOfficeManagedObjectSourceFlow());
		assertEquals("mos flow -> section input", targetInput, mosFlowToInput.getOfficeSectionInput());

		// Ensure dependency connected to external managed object
		assertEquals("dependency <- external mo", dependency, dependencyToExtMo.getOfficeManagedObjectDependency());
		assertEquals("dependency -> external mo", extMo, dependencyToExtMo.getExternalManagedObject());

		// Ensure dependency connected to office managed object
		assertEquals("dependency <- managed object", dependency, dependencyToMo.getOfficeManagedObjectDependency());
		assertEquals("dependency -> managed object", mo, dependencyToMo.getOfficeManagedObject());

		// Ensure supplier thread local to external managed object
		assertSame("supplier thread local <- external mo", supplierThreadLocal,
				supplierThreadLocalToExtMo.getOfficeSupplierThreadLocal());
		assertSame("supplier thread local -> external mo", extMo,
				supplierThreadLocalToExtMo.getExternalManagedObject());

		// Ensure supplier thread local to managed object
		assertSame("supplier thread local <- managed object", supplierThreadLocal,
				supplierThreadLocalToMo.getOfficeSupplierThreadLocal());
		assertSame("supplier thread local -> managed object", extMo,
				supplierThreadLocalToExtMo.getExternalManagedObject());

		// Ensure input dependency connected to external managed object
		assertEquals("input dependency <- external mo", inputDependency,
				inputDependencyToExtMo.getOfficeInputManagedObjectDependency());
		assertEquals("input dependency -> external mo", extMo, inputDependencyToExtMo.getExternalManagedObject());

		// Ensure input dependency connected to office managed object
		assertEquals("input dependency <- managed object", inputDependency,
				inputDependencyToMo.getOfficeInputManagedObjectDependency());
		assertEquals("input dependency -> managed object", mo, inputDependencyToMo.getOfficeManagedObject());

		// Ensure function dependency connected to external managed object
		assertEquals("function dependency <- external mo", functionDependency,
				functionDependencyToExtMo.getOfficeManagedObjectFunctionDependency());
		assertEquals("function dependency -> external mo", extMo, functionDependencyToExtMo.getExternalManagedObject());

		// Ensure function dependency connected to office managed object
		assertEquals("function dependency <- managed object", functionDependency,
				functionDependencyToMo.getOfficeManagedObjectFunctionDependency());
		assertEquals("function dependency -> managed object", mo, functionDependencyToMo.getOfficeManagedObject());

		// Ensure managed object connected to its pre-load administration
		assertEquals("managed object <- pre-load admin", mo, moToPreLoadAdmin.getOfficeManagedObject());
		assertEquals("managed object -> pre-load admin", admin, moToPreLoadAdmin.getAdministration());

		// Ensure managed object source team connected to office team
		assertEquals("mos team <- office team", moTeam, mosTeamToTeam.getOfficeManagedObjectSourceTeam());
		assertEquals("mos team -> office team", team, mosTeamToTeam.getOfficeTeam());

		// Ensure external managed object connected to pre-load administration
		assertEquals("external managed object <- pre-load admin", extMo,
				extMoToPreLoadAdmin.getExternalManagedObject());
		assertEquals("external managed object -> pre-load admin", admin, extMoToPreLoadAdmin.getAdministration());

		// Ensure start flows connected
		assertEquals("start <- section input", start, startToInput.getOfficeStart());
		assertEquals("start -> section input", targetInput, startToInput.getOfficeSectionInput());

		// Ensure escalation connected to section input
		assertEquals("escalation <- section input", escalation, escalationToInput.getOfficeEscalation());
		assertEquals("escalation -> section input", targetInput, escalationToInput.getOfficeSectionInput());

		// Ensure the outputs connected to inputs
		assertEquals("output <- input", output, outputToInput.getOfficeSectionOutput());
		assertEquals("output -> input", targetInput, outputToInput.getOfficeSectionInput());

		// Ensure the objects connected to external managed object
		assertEquals("section object <- external managed object", object, objectToExtMo.getOfficeSectionObject());
		assertEquals("section object -> external managed object", extMo, objectToExtMo.getExternalManagedObject());

		// Ensure the objects connect to office managed object
		assertEquals("section object <- office managed object", object, objectToMo.getOfficeSectionObject());
		assertEquals("section object -> office managed object", mo, objectToMo.getOfficeManagedObject());

		// Ensure administration flow connected
		assertEquals("administration flow -> section input", adminFlow, adminFlowToInput.getAdministrationFlow());
		assertEquals("administration flow <- section input", targetInput, adminFlowToInput.getOfficeSectionInput());

		// Ensure administration escalation connected
		assertEquals("administration escalation -> section input", adminEscalation,
				adminEscalationToInput.getAdministrationEscalation());
		assertEquals("administration escalation <- section input", targetInput,
				adminEscalationToInput.getOfficeSectionInput());

		// Ensure the function connect to team
		assertEquals("section function <- team", officeFunction, functionToTeam.getOfficeFunction());
		assertEquals("section function -> team", team, functionToTeam.getOfficeTeam());

		// Ensure the office function pre administration connected
		assertEquals("function <- pre admin", officeFunction, functionToPreAdmin.getOfficeFunction());
		assertEquals("function -> pre admin", admin, functionToPreAdmin.getAdministration());

		// Ensure the office function post administration connected
		assertEquals("function <- post admin", officeFunction, functionToPostAdmin.getOfficeFunction());
		assertEquals("function -> post admin", admin, functionToPostAdmin.getAdministration());

		// Ensure external managed object administration connected
		assertEquals("external managed object <- administration", extMo, extMoToAdmin.getExternalManagedObject());
		assertEquals("external managed object -> administration", admin, extMoToAdmin.getAdministration());

		// Ensure managed object administration connected
		assertEquals("managed object <- administration", mo, moToAdmin.getOfficeManagedObject());
		assertEquals("managed object -> administration", admin, moToAdmin.getAdministration());

		// Ensure the administration teams connected
		assertEquals("administration <- team", admin, adminToTeam.getAdministration());
		assertEquals("administration -> team", team, adminToTeam.getOfficeTeam());

		// Ensure administer the section managed object
		assertEquals("section managed object <- administration", sectionMo,
				sectionMoToAdmin.getOfficeSectionManagedObject());
		assertEquals("section managed object -> administration", admin, sectionMoToAdmin.getAdministration());

		// Ensure section managed object connected to pre-load administration
		assertEquals("section managed object <- pre-load admin", sectionMo,
				sectionMoToPreLoadAdmin.getOfficeSectionManagedObject());
		assertEquals("section managed object -> pre-load admin", admin, sectionMoToPreLoadAdmin.getAdministration());

		// Ensure the office function governance connected
		assertEquals("function <- governance", officeFunction, functionToGov.getOfficeFunction());
		assertEquals("function -> governance", governance, functionToGov.getGovernance());

		// Ensure the sub section governance connected
		assertEquals("sub section <- governance", subSection, subSectionToGov.getOfficeSubSection());
		assertEquals("sub section -> governance", governance, subSectionToGov.getGovernance());

		// Ensure governance flow connected
		assertEquals("governance flow -> section input", govFlow, govFlowToInput.getGovernanceFlow());
		assertEquals("governance flow <- section input", targetInput, govFlowToInput.getOfficeSectionInput());

		// Ensure governance escalation connected
		assertEquals("governance escalation -> section input", govEscalation,
				govEscalationToInput.getGovernanceEscalation());
		assertEquals("governance escalation <- section input ", targetInput,
				govEscalationToInput.getOfficeSectionInput());

		// Ensure section managed object governed
		assertEquals("section managed object <- governance", sectionMo, sectionMoToGov.getOfficeSectionManagedObject());
		assertEquals("section managed object -> governance", governance, sectionMoToGov.getGovernance());

		// Ensure external managed object governance connected
		assertEquals("external managed object <- governance", extMo, extMoToGov.getExternalManagedObject());
		assertEquals("external managed object -> governance", governance, extMoToGov.getGovernance());

		// Ensure managed object governance connected
		assertEquals("managed object <- governance", mo, moToGov.getOfficeManagedObject());
		assertEquals("managed object -> governance", governance, moToGov.getGovernance());

		// Ensure the governance teams connected
		assertEquals("governance <- team", governance, govToTeam.getGovernance());
		assertEquals("governance -> team", team, govToTeam.getOfficeTeam());
	}

	/**
	 * Ensures on storing a {@link OfficeModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreOffice() throws Exception {

		// Create the office (without connections)
		OfficeModel office = new OfficeModel();
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT",
				Connection.class.getName());
		office.addExternalManagedObject(extMo);
		OfficeManagedObjectModel mo = new OfficeManagedObjectModel("MANAGED_OBJECT", "THREAD");
		office.addOfficeManagedObject(mo);
		OfficeManagedObjectPoolModel pool = new OfficeManagedObjectPoolModel("POOL",
				"net.example.ExampleManagedObjectPoolSource");
		office.addOfficeManagedObjectPool(pool);
		OfficeSupplierModel supplier = new OfficeSupplierModel("SUPPLIER", "net.example.ExampleSupplierSource");
		office.addOfficeSupplier(supplier);
		OfficeSupplierThreadLocalModel supplierThreadLocal = new OfficeSupplierThreadLocalModel("QUALIFIER", "TYPE");
		supplier.addOfficeSupplierThreadLocal(supplierThreadLocal);
		OfficeManagedObjectDependencyModel dependency = new OfficeManagedObjectDependencyModel("DEPENDENCY",
				Connection.class.getName());
		mo.addOfficeManagedObjectDependency(dependency);
		OfficeManagedObjectSourceModel mos = new OfficeManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", "java.lang.Object", "0");
		office.addOfficeManagedObjectSource(mos);
		OfficeInputManagedObjectDependencyModel inputDependency = new OfficeInputManagedObjectDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		mos.addOfficeInputManagedObjectDependency(inputDependency);
		OfficeManagedObjectFunctionDependencyModel functionDependency = new OfficeManagedObjectFunctionDependencyModel(
				"FUNCTION_DEPENDENCY", Connection.class.getName());
		mos.addOfficeManagedObjectFunctionDependency(functionDependency);
		OfficeManagedObjectSourceFlowModel moFlow = new OfficeManagedObjectSourceFlowModel("FLOW",
				Integer.class.getName());
		mos.addOfficeManagedObjectSourceFlow(moFlow);
		OfficeManagedObjectSourceTeamModel moTeam = new OfficeManagedObjectSourceTeamModel("MO_TEAM");
		mos.addOfficeManagedObjectSourceTeam(moTeam);
		OfficeEscalationModel escalation = new OfficeEscalationModel("ESCALATION");
		office.addOfficeEscalation(escalation);
		OfficeTeamModel team = new OfficeTeamModel("TEAM");
		office.addOfficeTeam(team);
		OfficeSectionModel section = new OfficeSectionModel("SECTION", "net.example.ExampleSectionSource",
				"SECTION_LOCATION");
		office.addOfficeSection(section);
		OfficeSectionObjectModel object = new OfficeSectionObjectModel("OBJECT", Connection.class.getName());
		section.addOfficeSectionObject(object);
		AdministrationModel admin = new AdministrationModel("ADMINISTRATION", "net.example.ExampleAdministrationSource",
				false);
		office.addAdministration(admin);
		AdministrationFlowModel adminFlow = new AdministrationFlowModel("ADMIN_FLOW", null, null);
		admin.addAdministrationFlow(adminFlow);
		AdministrationEscalationModel adminEscalation = new AdministrationEscalationModel("java.io.IOException");
		admin.addAdministrationEscalation(adminEscalation);
		GovernanceModel governance = new GovernanceModel("GOVERNANCE", "net.example.ExampleGovernanceSource", false);
		office.addGovernance(governance);
		GovernanceFlowModel govFlow = new GovernanceFlowModel("GOV_FLOW", null, null);
		governance.addGovernanceFlow(govFlow);
		GovernanceEscalationModel govEscalation = new GovernanceEscalationModel("java.sql.SQLException");
		governance.addGovernanceEscalation(govEscalation);
		OfficeStartModel start = new OfficeStartModel("START");
		office.addOfficeStart(start);

		// Create the target section input
		OfficeSectionModel targetSection = new OfficeSectionModel("SECTION_TARGET", "net.example.ExcampleSectionSource",
				"SECTION_LOCATION");
		office.addOfficeSection(targetSection);
		OfficeSectionInputModel targetInput = new OfficeSectionInputModel("INPUT", Object.class.getName());
		targetSection.addOfficeSectionInput(targetInput);

		// managed object -> managed object source
		OfficeManagedObjectToOfficeManagedObjectSourceModel moToMos = new OfficeManagedObjectToOfficeManagedObjectSourceModel();
		moToMos.setOfficeManagedObject(mo);
		moToMos.setOfficeManagedObjectSource(mos);
		moToMos.connect();

		// managed object source -> managed object pool
		OfficeManagedObjectSourceToOfficeManagedObjectPoolModel mosToPool = new OfficeManagedObjectSourceToOfficeManagedObjectPoolModel();
		mosToPool.setOfficeManagedObjectSource(mos);
		mosToPool.setOfficeManagedObjectPool(pool);
		mosToPool.connect();

		// managed object source -> supplier
		OfficeManagedObjectSourceToOfficeSupplierModel mosToSupplier = new OfficeManagedObjectSourceToOfficeSupplierModel();
		mosToSupplier.setOfficeManagedObjectSource(mos);
		mosToSupplier.setOfficeSupplier(supplier);
		mosToSupplier.connect();

		// managed object source flow -> section input
		OfficeManagedObjectSourceFlowToOfficeSectionInputModel flowToInput = new OfficeManagedObjectSourceFlowToOfficeSectionInputModel();
		flowToInput.setOfficeManagedObjectSourceFlow(moFlow);
		flowToInput.setOfficeSectionInput(targetInput);
		flowToInput.connect();

		// dependency -> external managed object
		OfficeManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = new OfficeManagedObjectDependencyToExternalManagedObjectModel();
		dependencyToExtMo.setOfficeManagedObjectDependency(dependency);
		dependencyToExtMo.setExternalManagedObject(extMo);
		dependencyToExtMo.connect();

		// dependency -> office managed object
		OfficeManagedObjectDependencyToOfficeManagedObjectModel dependencyToMo = new OfficeManagedObjectDependencyToOfficeManagedObjectModel();
		dependencyToMo.setOfficeManagedObjectDependency(dependency);
		dependencyToMo.setOfficeManagedObject(mo);
		dependencyToMo.connect();

		// supplier thread local -> external managed object
		OfficeSupplierThreadLocalToExternalManagedObjectModel supplierThreadLocalToExtMo = new OfficeSupplierThreadLocalToExternalManagedObjectModel();
		supplierThreadLocalToExtMo.setOfficeSupplierThreadLocal(supplierThreadLocal);
		supplierThreadLocalToExtMo.setExternalManagedObject(extMo);
		supplierThreadLocalToExtMo.connect();

		// supplier thread local -> managed object
		OfficeSupplierThreadLocalToOfficeManagedObjectModel supplierThreadLocalToMo = new OfficeSupplierThreadLocalToOfficeManagedObjectModel();
		supplierThreadLocalToMo.setOfficeSupplierThreadLocal(supplierThreadLocal);
		supplierThreadLocalToMo.setOfficeManagedObject(mo);
		supplierThreadLocalToMo.connect();

		// input dependency -> external managed object
		OfficeInputManagedObjectDependencyToExternalManagedObjectModel inputDependencyToExtMo = new OfficeInputManagedObjectDependencyToExternalManagedObjectModel();
		inputDependencyToExtMo.setOfficeInputManagedObjectDependency(inputDependency);
		inputDependencyToExtMo.setExternalManagedObject(extMo);
		inputDependencyToExtMo.connect();

		// input dependency -> office managed object
		OfficeInputManagedObjectDependencyToOfficeManagedObjectModel inputDependencyToMo = new OfficeInputManagedObjectDependencyToOfficeManagedObjectModel();
		inputDependencyToMo.setOfficeInputManagedObjectDependency(inputDependency);
		inputDependencyToMo.setOfficeManagedObject(mo);
		inputDependencyToMo.connect();

		// function dependency -> external managed object
		OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel functionDependencyToExtMo = new OfficeManagedObjectFunctionDependencyToExternalManagedObjectModel();
		functionDependencyToExtMo.setOfficeManagedObjectFunctionDependency(functionDependency);
		functionDependencyToExtMo.setExternalManagedObject(extMo);
		functionDependencyToExtMo.connect();

		// function dependency -> office managed object
		OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel functionDependencyToMo = new OfficeManagedObjectFunctionDependencyToOfficeManagedObjectModel();
		functionDependencyToMo.setOfficeManagedObjectFunctionDependency(functionDependency);
		functionDependencyToMo.setOfficeManagedObject(mo);
		functionDependencyToMo.connect();

		// managed object -> pre-load administration
		OfficeManagedObjectToPreLoadAdministrationModel moToPreLoadAdmin = new OfficeManagedObjectToPreLoadAdministrationModel();
		moToPreLoadAdmin.setAdministration(admin);
		moToPreLoadAdmin.setOfficeManagedObject(mo);
		moToPreLoadAdmin.connect();

		// managed object source team -> office team
		OfficeManagedObjectSourceTeamToOfficeTeamModel moTeamToTeam = new OfficeManagedObjectSourceTeamToOfficeTeamModel();
		moTeamToTeam.setOfficeManagedObjectSourceTeam(moTeam);
		moTeamToTeam.setOfficeTeam(team);
		moTeamToTeam.connect();

		// external managed object -> pre-load administration
		ExternalManagedObjectToPreLoadAdministrationModel extMoToPreLoadAdmin = new ExternalManagedObjectToPreLoadAdministrationModel();
		extMoToPreLoadAdmin.setAdministration(admin);
		extMoToPreLoadAdmin.setExternalManagedObject(extMo);
		extMoToPreLoadAdmin.connect();

		// start -> input
		OfficeStartToOfficeSectionInputModel startToInput = new OfficeStartToOfficeSectionInputModel();
		startToInput.setOfficeStart(start);
		startToInput.setOfficeSectionInput(targetInput);
		startToInput.connect();

		// escalation -> section input
		OfficeEscalationToOfficeSectionInputModel escalationToInput = new OfficeEscalationToOfficeSectionInputModel();
		escalationToInput.setOfficeEscalation(escalation);
		escalationToInput.setOfficeSectionInput(targetInput);
		escalationToInput.connect();

		// office function -> administration (setup)
		OfficeSubSectionModel subSection = new OfficeSubSectionModel();
		section.setOfficeSubSection(subSection);
		OfficeSubSectionModel subSubSection = new OfficeSubSectionModel("SUB_SECTION");
		subSection.addOfficeSubSection(subSubSection);
		OfficeFunctionModel officeFunction = new OfficeFunctionModel("FUNCTION");
		subSubSection.addOfficeFunction(officeFunction);

		// office function -> team
		OfficeFunctionToOfficeTeamModel functionToTeam = new OfficeFunctionToOfficeTeamModel();
		functionToTeam.setOfficeFunction(officeFunction);
		functionToTeam.setOfficeTeam(team);
		functionToTeam.connect();

		// office function -> pre administration
		OfficeFunctionToPreAdministrationModel functionToPreAdmin = new OfficeFunctionToPreAdministrationModel();
		functionToPreAdmin.setOfficeFunction(officeFunction);
		functionToPreAdmin.setAdministration(admin);
		functionToPreAdmin.connect();

		// office function -> post administration
		OfficeFunctionToPostAdministrationModel functionToPostAdmin = new OfficeFunctionToPostAdministrationModel();
		functionToPostAdmin.setOfficeFunction(officeFunction);
		functionToPostAdmin.setAdministration(admin);
		functionToPostAdmin.connect();

		// administration flow -> section input
		AdministrationFlowToOfficeSectionInputModel adminFlowToInput = new AdministrationFlowToOfficeSectionInputModel();
		adminFlowToInput.setAdministrationFlow(adminFlow);
		adminFlowToInput.setOfficeSectionInput(targetInput);
		adminFlowToInput.connect();

		// administration escalation -> section input
		AdministrationEscalationToOfficeSectionInputModel adminEscalationToInput = new AdministrationEscalationToOfficeSectionInputModel();
		adminEscalationToInput.setAdministrationEscalation(adminEscalation);
		adminEscalationToInput.setOfficeSectionInput(targetInput);
		adminEscalationToInput.connect();

		// administration -> external managed object
		AdministrationToExternalManagedObjectModel extMoToAdmin = new AdministrationToExternalManagedObjectModel();
		extMoToAdmin.setExternalManagedObject(extMo);
		extMoToAdmin.setAdministration(admin);
		extMoToAdmin.connect();

		// administration -> managed object
		AdministrationToOfficeManagedObjectModel moToAdmin = new AdministrationToOfficeManagedObjectModel();
		moToAdmin.setOfficeManagedObject(mo);
		moToAdmin.setAdministration(admin);
		moToAdmin.connect();

		// administration -> team
		AdministrationToOfficeTeamModel adminToTeam = new AdministrationToOfficeTeamModel();
		adminToTeam.setAdministration(admin);
		adminToTeam.setOfficeTeam(team);
		adminToTeam.connect();

		// section managed object (setup)
		OfficeSectionManagedObjectModel sectionMo = new OfficeSectionManagedObjectModel();
		subSection.addOfficeSectionManagedObject(sectionMo);

		// administration -> section managed object
		AdministrationToOfficeSectionManagedObjectModel sectionMoToAdmin = new AdministrationToOfficeSectionManagedObjectModel();
		sectionMoToAdmin.setAdministration(admin);
		sectionMoToAdmin.setOfficeSectionManagedObject(sectionMo);
		sectionMoToAdmin.connect();

		// section managed object -> pre-load administration
		OfficeSectionManagedObjectToPreLoadAdministrationModel sectionMoToPreLoadAdmin = new OfficeSectionManagedObjectToPreLoadAdministrationModel();
		sectionMoToPreLoadAdmin.setAdministration(admin);
		sectionMoToPreLoadAdmin.setOfficeSectionManagedObject(sectionMo);
		sectionMoToPreLoadAdmin.connect();

		// office function -> governance
		OfficeFunctionToGovernanceModel functionToGov = new OfficeFunctionToGovernanceModel();
		functionToGov.setOfficeFunction(officeFunction);
		functionToGov.setGovernance(governance);
		functionToGov.connect();

		// governance flow -> section input
		GovernanceFlowToOfficeSectionInputModel govFlowToInput = new GovernanceFlowToOfficeSectionInputModel();
		govFlowToInput.setGovernanceFlow(govFlow);
		govFlowToInput.setOfficeSectionInput(targetInput);
		govFlowToInput.connect();

		// governance escalation -> section input
		GovernanceEscalationToOfficeSectionInputModel govEscalationToInput = new GovernanceEscalationToOfficeSectionInputModel();
		govEscalationToInput.setGovernanceEscalation(govEscalation);
		govEscalationToInput.setOfficeSectionInput(targetInput);
		govEscalationToInput.connect();

		// sub section -> governance
		OfficeSubSectionToGovernanceModel subSectionToGov = new OfficeSubSectionToGovernanceModel();
		subSectionToGov.setOfficeSubSection(subSection);
		subSectionToGov.setGovernance(governance);
		subSectionToGov.connect();

		// governance -> section managed object
		GovernanceToOfficeSectionManagedObjectModel sectionMoToGov = new GovernanceToOfficeSectionManagedObjectModel();
		sectionMoToGov.setOfficeSectionManagedObject(sectionMo);
		sectionMoToGov.setGovernance(governance);
		sectionMoToGov.connect();

		// governance -> external managed object
		GovernanceToExternalManagedObjectModel extMoToGov = new GovernanceToExternalManagedObjectModel();
		extMoToGov.setExternalManagedObject(extMo);
		extMoToGov.setGovernance(governance);
		extMoToGov.connect();

		// governance -> managed object
		GovernanceToOfficeManagedObjectModel moToGov = new GovernanceToOfficeManagedObjectModel();
		moToGov.setOfficeManagedObject(mo);
		moToGov.setGovernance(governance);
		moToGov.connect();

		// governance -> team
		GovernanceToOfficeTeamModel govToTeam = new GovernanceToOfficeTeamModel();
		govToTeam.setGovernance(governance);
		govToTeam.setOfficeTeam(team);
		govToTeam.connect();

		// section output -> section input
		OfficeSectionOutputModel output = new OfficeSectionOutputModel("OUTPUT", String.class.getName(), false);
		section.addOfficeSectionOutput(output);
		OfficeSectionOutputToOfficeSectionInputModel outputToInput = new OfficeSectionOutputToOfficeSectionInputModel();
		outputToInput.setOfficeSectionOutput(output);
		outputToInput.setOfficeSectionInput(targetInput);
		outputToInput.connect();

		// section object -> external managed object
		OfficeSectionObjectToExternalManagedObjectModel objectToExtMo = new OfficeSectionObjectToExternalManagedObjectModel();
		objectToExtMo.setOfficeSectionObject(object);
		objectToExtMo.setExternalManagedObject(extMo);
		objectToExtMo.connect();

		// section object -> office managed object
		OfficeSectionObjectToOfficeManagedObjectModel objectToMo = new OfficeSectionObjectToOfficeManagedObjectModel();
		objectToMo.setOfficeSectionObject(object);
		objectToMo.setOfficeManagedObject(mo);
		objectToMo.connect();

		// Record storing the office
		this.modelRepository.store(office, this.configurationItem);

		// Store the office
		this.replayMockObjects();
		this.officeRepository.storeOffice(office, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieve
		assertEquals("managed object - managed object source", "MANAGED_OBJECT_SOURCE",
				moToMos.getOfficeManagedObjectSourceName());
		assertEquals("managed object source - managed object pool", "POOL", mosToPool.getOfficeManagedObjectPoolName());
		assertEquals("managed object source - supplier", "SUPPLIER", mosToSupplier.getOfficeSupplierName());
		assertEquals("managed object source flow - input (section name)", "SECTION_TARGET",
				flowToInput.getOfficeSectionName());
		assertEquals("managed object source flow - input (input name)", "INPUT",
				flowToInput.getOfficeSectionInputName());
		assertEquals("dependency - external managed object", "EXTERNAL_MANAGED_OBJECT",
				dependencyToExtMo.getExternalManagedObjectName());
		assertEquals("dependency - managed object", "MANAGED_OBJECT", dependencyToMo.getOfficeManagedObjectName());
		assertEquals("supplier thread local - external managed object", "EXTERNAL_MANAGED_OBJECT",
				supplierThreadLocalToExtMo.getExternalManagedObjectName());
		assertEquals("supplier thread local - managed object", "MANAGED_OBJECT",
				supplierThreadLocalToMo.getOfficeManagedObjectName());
		assertEquals("input dependency - external managed object", "EXTERNAL_MANAGED_OBJECT",
				inputDependencyToExtMo.getExternalManagedObjectName());
		assertEquals("input dependency - managed object", "MANAGED_OBJECT",
				inputDependencyToMo.getOfficeManagedObjectName());
		assertEquals("function dependency - external managed object", "EXTERNAL_MANAGED_OBJECT",
				functionDependencyToExtMo.getExternalManagedObjectName());
		assertEquals("function dependency - managed object", "MANAGED_OBJECT",
				functionDependencyToMo.getOfficeManagedObjectName());
		assertEquals("managed object - pre-load administration", "ADMINISTRATION",
				moToPreLoadAdmin.getAdministrationName());
		assertEquals("managed object source team - office team", "TEAM", moTeamToTeam.getOfficeTeamName());
		assertEquals("external managed object - pre-load administration", "ADMINISTRATION",
				extMoToPreLoadAdmin.getAdministrationName());
		assertEquals("start - input (section name)", "SECTION_TARGET", startToInput.getOfficeSectionName());
		assertEquals("start - input (input name)", "INPUT", startToInput.getOfficeSectionInputName());
		assertEquals("escalation - input (section name)", "SECTION_TARGET", escalationToInput.getOfficeSectionName());
		assertEquals("escalation - input (input name", "INPUT", escalationToInput.getOfficeSectionInputName());
		assertEquals("function - team", "TEAM", functionToTeam.getOfficeTeamName());
		assertEquals("function - pre admin (administration name)", "ADMINISTRATION",
				functionToPreAdmin.getAdministrationName());
		assertEquals("function - post duty (administration name)", "ADMINISTRATION",
				functionToPostAdmin.getAdministrationName());
		assertEquals("administration flow - input (section name)", "SECTION_TARGET",
				adminFlowToInput.getOfficeSectionName());
		assertEquals("administration flow - input (input name)", "INPUT", adminFlowToInput.getOfficeSectionInputName());
		assertEquals("administration escalation - input (section name)", "SECTION_TARGET",
				adminEscalationToInput.getOfficeSectionName());
		assertEquals("administration escalation - input (input name)", "INPUT",
				adminEscalationToInput.getOfficeSectionInputName());
		assertEquals("external managed object - administration", "ADMINISTRATION",
				extMoToAdmin.getAdministrationName());
		assertEquals("office managed object - administration", "ADMINISTRATION", moToAdmin.getAdministrationName());
		assertEquals("administration - team", "TEAM", adminToTeam.getOfficeTeamName());
		assertEquals("section managed object - administration", "ADMINISTRATION",
				sectionMoToAdmin.getAdministrationName());
		assertEquals("section managed object - pre-load administration", "ADMINISTRATION",
				sectionMoToPreLoadAdmin.getAdministrationName());
		assertEquals("function - governance", "GOVERNANCE", functionToGov.getGovernanceName());
		assertEquals("governance flow - input (section name)", "SECTION_TARGET", govFlowToInput.getOfficeSectionName());
		assertEquals("governance flow - input (input name)", "INPUT", govFlowToInput.getOfficeSectionInputName());
		assertEquals("governance escalation - input (section name)", "SECTION_TARGET",
				govEscalationToInput.getOfficeSectionName());
		assertEquals("governance escalation - input (input name)", "INPUT",
				govEscalationToInput.getOfficeSectionInputName());
		assertEquals("sub section - governance", "GOVERNANCE", subSectionToGov.getGovernanceName());
		assertEquals("section managed object - governance", "GOVERNANCE", sectionMoToGov.getGovernanceName());
		assertEquals("external managed object - governance", "GOVERNANCE", extMoToGov.getGovernanceName());
		assertEquals("office managed object - governance", "GOVERNANCE", moToGov.getGovernanceName());
		assertEquals("governance - team", "TEAM", govToTeam.getOfficeTeamName());
		assertEquals("section output - input (section name)", "SECTION_TARGET", outputToInput.getOfficeSectionName());
		assertEquals("section output - input (input name)", "INPUT", outputToInput.getOfficeSectionInputName());
		assertEquals("section object - external managed object", "EXTERNAL_MANAGED_OBJECT",
				objectToExtMo.getExternalManagedObjectName());
		assertEquals("section object - office managed object", "MANAGED_OBJECT",
				objectToMo.getOfficeManagedObjectName());
	}

}
