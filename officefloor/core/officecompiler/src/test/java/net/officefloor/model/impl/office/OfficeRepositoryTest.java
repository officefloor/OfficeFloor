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
package net.officefloor.model.impl.office;

import java.io.IOException;
import java.sql.Connection;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.AdministrationEscalationModel;
import net.officefloor.model.office.AdministrationFlowModel;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministrationModel;
import net.officefloor.model.office.ExternalManagedObjectToGovernanceModel;
import net.officefloor.model.office.GovernanceEscalationModel;
import net.officefloor.model.office.GovernanceFlowModel;
import net.officefloor.model.office.GovernanceModel;
import net.officefloor.model.office.GovernanceToOfficeTeamModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToGovernanceModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectToAdministrationModel;
import net.officefloor.model.office.OfficeManagedObjectToGovernanceModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToGovernanceModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToGovernanceModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
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
	 * {@link ConfigurationItem}.
	 */
	private final ConfigurationItem configurationItem = this.createMock(ConfigurationItem.class);

	/**
	 * {@link OfficeRepository} to be tested.
	 */
	private final OfficeRepository officeRepository = new OfficeRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link OfficeModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveOffice() throws Exception {

		// Create the raw office to be connected
		OfficeModel office = new OfficeModel();
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel("EXTERNAL_MANAGED_OBJECT",
				Connection.class.getName());
		OfficeManagedObjectModel mo = new OfficeManagedObjectModel("MANAGED_OBJECT", "THREAD");
		office.addOfficeManagedObject(mo);
		OfficeManagedObjectDependencyModel dependency = new OfficeManagedObjectDependencyModel("DEPENDENCY",
				Connection.class.getName());
		mo.addOfficeManagedObjectDependency(dependency);
		OfficeManagedObjectSourceModel mos = new OfficeManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", "java.lang.Object", "0");
		office.addOfficeManagedObjectSource(mos);
		OfficeInputManagedObjectDependencyModel inputDependency = new OfficeInputManagedObjectDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		mos.addOfficeInputManagedObjectDependency(inputDependency);
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
		OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel("RESPONSIBILITY");
		section.addOfficeSectionResponsibility(responsibility);
		AdministrationModel admin = new AdministrationModel("ADMINISTRATION",
				"net.example.ExampleAdministrationSource");
		AdministrationFlowModel adminFlow = new AdministrationFlowModel("ADMIN_FLOW", null, Integer.class.getName());
		admin.addAdministrationFlow(adminFlow);
		AdministrationEscalationModel adminEscalation = new AdministrationEscalationModel(Exception.class.getName());
		admin.addAdministrationEscalation(adminEscalation);
		GovernanceModel governance = new GovernanceModel("GOVERNANCE", "net.example.ExampleGovernanceSource");
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

		// input managed object dependency -> external managed object
		OfficeInputManagedObjectDependencyToExternalManagedObjectModel inputDependencyToExtMo = new OfficeInputManagedObjectDependencyToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		inputDependency.setExternalManagedObject(inputDependencyToExtMo);

		// input managed object dependency -> office managed object
		OfficeInputManagedObjectDependencyToOfficeManagedObjectModel inputDependencyToMo = new OfficeInputManagedObjectDependencyToOfficeManagedObjectModel(
				"MANAGED_OBJECT");
		inputDependency.setOfficeManagedObject(inputDependencyToMo);

		// mo team -> team
		OfficeManagedObjectSourceTeamToOfficeTeamModel mosTeamToTeam = new OfficeManagedObjectSourceTeamToOfficeTeamModel(
				"TEAM");
		moTeam.setOfficeTeam(mosTeamToTeam);

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

		// section responsibility -> team
		OfficeSectionResponsibilityToOfficeTeamModel respToTeam = new OfficeSectionResponsibilityToOfficeTeamModel(
				"TEAM");
		responsibility.setOfficeTeam(respToTeam);

		// start -> section input
		OfficeStartToOfficeSectionInputModel startToInput = new OfficeStartToOfficeSectionInputModel("SECTION_TARGET",
				"INPUT");
		start.setOfficeSectionInput(startToInput);

		// office function
		OfficeSubSectionModel subSection = new OfficeSubSectionModel();
		section.setOfficeSubSection(subSection);
		OfficeSubSectionModel subSubSection = new OfficeSubSectionModel("SUB_SECTION");
		subSection.addOfficeSubSection(subSubSection);
		OfficeFunctionModel officeFunction = new OfficeFunctionModel("FUNCTION");
		subSubSection.addOfficeFunction(officeFunction);

		// office function -> pre administration
		OfficeFunctionToPreAdministrationModel functionToPreAdmin = new OfficeFunctionToPreAdministrationModel(
				"ADMINISTRATION");
		officeFunction.addPreAdministration(functionToPreAdmin);

		// office function -> post administration
		OfficeFunctionToPostAdministrationModel functionToPostAdmin = new OfficeFunctionToPostAdministrationModel(
				"ADMINISTRATION");
		officeFunction.addPostAdministration(functionToPostAdmin);

		fail("TODO administration flow -> section input");
		fail("TODO administration escalation -> section input");

		// external managed object -> administration
		ExternalManagedObjectToAdministrationModel extMoToAdmin = new ExternalManagedObjectToAdministrationModel(
				"ADMINISTRATION", "1");
		extMo.addAdministration(extMoToAdmin);

		// managed object -> administration
		OfficeManagedObjectToAdministrationModel moToAdmin = new OfficeManagedObjectToAdministrationModel(
				"ADMINISTRATION", "1");
		mo.addAdministration(moToAdmin);

		// administration -> team
		AdministrationToOfficeTeamModel adminToTeam = new AdministrationToOfficeTeamModel("TEAM");
		admin.setOfficeTeam(adminToTeam);

		// section managed object (setup)
		OfficeSectionManagedObjectModel sectionMo = new OfficeSectionManagedObjectModel("SECTION_MO");
		subSection.addOfficeSectionManagedObject(sectionMo);

		// office function -> governance
		OfficeFunctionToGovernanceModel functionToGov = new OfficeFunctionToGovernanceModel("GOVERNANCE");
		officeFunction.addGovernance(functionToGov);

		// sub section -> governance
		OfficeSubSectionToGovernanceModel subSectionToGov = new OfficeSubSectionToGovernanceModel("GOVERNANCE");
		subSection.addGovernance(subSectionToGov);

		fail("TODO governance flow -> section input");
		fail("TODO governance escalation -> section input");

		// external managed object -> governance
		ExternalManagedObjectToGovernanceModel extMoToGov = new ExternalManagedObjectToGovernanceModel("GOVERNANCE");
		extMo.addGovernance(extMoToGov);

		// managed object -> governance
		OfficeManagedObjectToGovernanceModel moToGov = new OfficeManagedObjectToGovernanceModel("GOVERNANCE");
		mo.addGovernance(moToGov);

		// section managed object -> governance
		OfficeSectionManagedObjectToGovernanceModel sectionMoToGov = new OfficeSectionManagedObjectToGovernanceModel(
				"GOVERNANCE");
		sectionMo.addGovernance(sectionMoToGov);

		// governance -> team
		GovernanceToOfficeTeamModel govToTeam = new GovernanceToOfficeTeamModel("TEAM");
		governance.setOfficeTeam(govToTeam);

		// Record retrieving the office
		this.recordReturn(this.modelRepository, this.modelRepository.retrieve(null, this.configurationItem), office,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertTrue("Must be office model", actual[0] instanceof OfficeModel);
						assertEquals("Incorrect configuration item", OfficeRepositoryTest.this.configurationItem,
								actual[1]);
						return true;
					}
				});

		// Retrieve the office
		this.replayMockObjects();
		OfficeModel retrievedOffice = this.officeRepository.retrieveOffice(this.configurationItem);
		this.verifyMockObjects();
		assertEquals("Incorrect office", office, retrievedOffice);

		// Ensure managed object connected to its managed object source
		assertEquals("managed object <- managed object source", mo, moToMos.getOfficeManagedObject());
		assertEquals("managed object -> managed object source", mos, moToMos.getOfficeManagedObjectSource());

		// Ensure managed object source flow connected to section input
		assertEquals("mos flow <- section input", moFlow, mosFlowToInput.getOfficeManagedObjectSourceFlow());
		assertEquals("mos flow -> section input", targetInput, mosFlowToInput.getOfficeSectionInput());

		// Ensure dependency connected to external managed object
		assertEquals("dependency <- external mo", dependency, dependencyToExtMo.getOfficeManagedObjectDependency());
		assertEquals("dependency -> external mo", extMo, dependencyToExtMo.getExternalManagedObject());

		// Ensure dependency connected to office managed object
		assertEquals("dependency <- managed object", dependency, dependencyToMo.getOfficeManagedObjectDependency());
		assertEquals("dependency -> managed object", mo, dependencyToMo.getOfficeManagedObject());

		// Ensure input dependency connected to external managed object
		assertEquals("input dependency <- external mo", inputDependency,
				inputDependencyToExtMo.getOfficeInputManagedObjectDependency());
		assertEquals("input dependency -> external mo", extMo, inputDependencyToExtMo.getExternalManagedObject());

		// Ensure input dependency connected to office managed object
		assertEquals("input dependency <- managed object", inputDependency,
				inputDependencyToMo.getOfficeInputManagedObjectDependency());
		assertEquals("input dependency -> managed object", mo, inputDependencyToMo.getOfficeManagedObject());

		// Ensure managed object source team connected to office team
		assertEquals("mos team <- office team", moTeam, mosTeamToTeam.getOfficeManagedObjectSourceTeam());
		assertEquals("mos team -> office team", team, mosTeamToTeam.getOfficeTeam());

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

		// Ensure the responsibility team connected
		assertEquals("section responsibility <- team", responsibility, respToTeam.getOfficeSectionResponsibility());
		assertEquals("section responsibility -> team", team, respToTeam.getOfficeTeam());

		// Ensure start flows connected
		assertEquals("start <- section input", start, startToInput.getOfficeStart());
		assertEquals("start -> section input", targetInput, startToInput.getOfficeSectionInput());
		
		fail("administration flow -> section input");
		fail("administration flow <- section input");

		fail("administration escalation -> section input");
		fail("administration escalation <- section input");

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

		// Ensure the office function governance connected
		assertEquals("function <- governance", officeFunction, functionToGov.getOfficeFunction());
		assertEquals("function -> governance", governance, functionToGov.getGovernance());

		// Ensure the sub section governance connected
		assertEquals("sub section <- governance", subSection, subSectionToGov.getOfficeSubSection());
		assertEquals("sub section -> governance", governance, subSectionToGov.getGovernance());

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
		OfficeManagedObjectDependencyModel dependency = new OfficeManagedObjectDependencyModel("DEPENDENCY",
				Connection.class.getName());
		mo.addOfficeManagedObjectDependency(dependency);
		OfficeManagedObjectSourceModel mos = new OfficeManagedObjectSourceModel("MANAGED_OBJECT_SOURCE",
				"net.example.ExampleManagedObjectSource", "java.lang.Object", "0");
		office.addOfficeManagedObjectSource(mos);
		OfficeInputManagedObjectDependencyModel inputDependency = new OfficeInputManagedObjectDependencyModel(
				"INPUT_DEPENDENCY", Connection.class.getName());
		mos.addOfficeInputManagedObjectDependency(inputDependency);
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
		OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel("RESPONSIBILITY");
		section.addOfficeSectionResponsibility(responsibility);
		AdministrationModel admin = new AdministrationModel("ADMINISTRATION",
				"net.example.ExampleAdministrationSource");

		fail("TODO test administration flows and escalations");

		GovernanceModel governance = new GovernanceModel("GOVERNANCE", "net.example.ExampleGovernanceSource");
		office.addGovernance(governance);

		fail("TODO test governance flows and escalations");

		OfficeStartModel start = new OfficeStartModel("START");
		office.addOfficeStart(start);

		// responsibility -> team
		OfficeSectionResponsibilityToOfficeTeamModel respToTeam = new OfficeSectionResponsibilityToOfficeTeamModel();
		respToTeam.setOfficeSectionResponsibility(responsibility);
		respToTeam.setOfficeTeam(team);
		respToTeam.connect();

		// section output -> section input
		OfficeSectionOutputModel output = new OfficeSectionOutputModel("OUTPUT", String.class.getName(), false);
		section.addOfficeSectionOutput(output);
		OfficeSectionModel targetSection = new OfficeSectionModel("SECTION_TARGET", "net.example.ExcampleSectionSource",
				"SECTION_LOCATION");
		office.addOfficeSection(targetSection);
		OfficeSectionInputModel targetInput = new OfficeSectionInputModel("INPUT", String.class.getName());
		targetSection.addOfficeSectionInput(targetInput);
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

		// managed object -> managed object source
		OfficeManagedObjectToOfficeManagedObjectSourceModel moToMos = new OfficeManagedObjectToOfficeManagedObjectSourceModel();
		moToMos.setOfficeManagedObject(mo);
		moToMos.setOfficeManagedObjectSource(mos);
		moToMos.connect();

		// managed object source flow -> section input
		OfficeManagedObjectSourceFlowToOfficeSectionInputModel flowToInput = new OfficeManagedObjectSourceFlowToOfficeSectionInputModel();
		flowToInput.setOfficeManagedObjectSourceFlow(moFlow);
		flowToInput.setOfficeSectionInput(targetInput);
		flowToInput.connect();

		// managed object source team -> office team
		OfficeManagedObjectSourceTeamToOfficeTeamModel moTeamToTeam = new OfficeManagedObjectSourceTeamToOfficeTeamModel();
		moTeamToTeam.setOfficeManagedObjectSourceTeam(moTeam);
		moTeamToTeam.setOfficeTeam(team);
		moTeamToTeam.connect();

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

		// administration -> team
		AdministrationToOfficeTeamModel adminToTeam = new AdministrationToOfficeTeamModel();
		adminToTeam.setAdministration(admin);
		adminToTeam.setOfficeTeam(team);
		adminToTeam.connect();

		// governance -> team
		GovernanceToOfficeTeamModel govToTeam = new GovernanceToOfficeTeamModel();
		govToTeam.setGovernance(governance);
		govToTeam.setOfficeTeam(team);
		govToTeam.connect();

		// start -> input
		OfficeStartToOfficeSectionInputModel startToInput = new OfficeStartToOfficeSectionInputModel();
		startToInput.setOfficeStart(start);
		startToInput.setOfficeSectionInput(targetInput);
		startToInput.connect();

		// office function -> duty (setup)
		OfficeSubSectionModel subSection = new OfficeSubSectionModel();
		section.setOfficeSubSection(subSection);
		OfficeSubSectionModel subSubSection = new OfficeSubSectionModel("SUB_SECTION");
		subSection.addOfficeSubSection(subSubSection);
		OfficeFunctionModel officeFunction = new OfficeFunctionModel("FUNCTION");
		subSubSection.addOfficeFunction(officeFunction);

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

		// office function -> governance
		OfficeFunctionToGovernanceModel functionToGov = new OfficeFunctionToGovernanceModel();
		functionToGov.setOfficeFunction(officeFunction);
		functionToGov.setGovernance(governance);
		functionToGov.connect();

		// sub section -> governance
		OfficeSubSectionToGovernanceModel subSectionToGov = new OfficeSubSectionToGovernanceModel();
		subSectionToGov.setOfficeSubSection(subSection);
		subSectionToGov.setGovernance(governance);
		subSectionToGov.connect();

		// section managed object (setup)
		OfficeSectionManagedObjectModel sectionMo = new OfficeSectionManagedObjectModel();
		subSection.addOfficeSectionManagedObject(sectionMo);

		// section managed object -> governance
		OfficeSectionManagedObjectToGovernanceModel sectionMoToGov = new OfficeSectionManagedObjectToGovernanceModel();
		sectionMoToGov.setOfficeSectionManagedObject(sectionMo);
		sectionMoToGov.setGovernance(governance);
		sectionMoToGov.connect();

		// external managed object -> administration
		ExternalManagedObjectToAdministrationModel extMoToAdmin = new ExternalManagedObjectToAdministrationModel();
		extMoToAdmin.setExternalManagedObject(extMo);
		extMoToAdmin.setAdministration(admin);
		extMoToAdmin.connect();

		// external managed object -> governance
		ExternalManagedObjectToGovernanceModel extMoToGov = new ExternalManagedObjectToGovernanceModel();
		extMoToGov.setExternalManagedObject(extMo);
		extMoToGov.setGovernance(governance);
		extMoToGov.connect();

		// managed object -> administration
		OfficeManagedObjectToAdministrationModel moToAdmin = new OfficeManagedObjectToAdministrationModel();
		moToAdmin.setOfficeManagedObject(mo);
		moToAdmin.setAdministration(admin);
		moToAdmin.connect();

		// managed object -> governance
		OfficeManagedObjectToGovernanceModel moToGov = new OfficeManagedObjectToGovernanceModel();
		moToGov.setOfficeManagedObject(mo);
		moToGov.setGovernance(governance);
		moToGov.connect();

		// escalation -> section input
		OfficeEscalationToOfficeSectionInputModel escalationToInput = new OfficeEscalationToOfficeSectionInputModel();
		escalationToInput.setOfficeEscalation(escalation);
		escalationToInput.setOfficeSectionInput(targetInput);
		escalationToInput.connect();

		// Record storing the office
		this.modelRepository.store(office, this.configurationItem);

		// Store the office
		this.replayMockObjects();
		this.officeRepository.storeOffice(office, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("responsibility - team", "TEAM", respToTeam.getOfficeTeamName());
		assertEquals("output - input (section name)", "SECTION_TARGET", outputToInput.getOfficeSectionName());
		assertEquals("output - input (input name)", "INPUT", outputToInput.getOfficeSectionInputName());
		assertEquals("object - external managed object", "EXTERNAL_MANAGED_OBJECT",
				objectToExtMo.getExternalManagedObjectName());
		assertEquals("object - office managed object", "MANAGED_OBJECT", objectToMo.getOfficeManagedObjectName());
		assertEquals("managed object - managed object source", "MANAGED_OBJECT_SOURCE",
				moToMos.getOfficeManagedObjectSourceName());
		assertEquals("managed object source flow - input (section name)", "SECTION_TARGET",
				flowToInput.getOfficeSectionName());
		assertEquals("managed object source flow - input (input name)", "INPUT",
				flowToInput.getOfficeSectionInputName());
		assertEquals("managed object source team - office team", "TEAM", moTeamToTeam.getOfficeTeamName());
		assertEquals("dependency - external managed object", "EXTERNAL_MANAGED_OBJECT",
				dependencyToExtMo.getExternalManagedObjectName());
		assertEquals("dependency - managed object", "MANAGED_OBJECT", dependencyToMo.getOfficeManagedObjectName());
		assertEquals("input dependency - external managed object", "EXTERNAL_MANAGED_OBJECT",
				inputDependencyToExtMo.getExternalManagedObjectName());
		assertEquals("input dependency - managed object", "MANAGED_OBJECT",
				inputDependencyToMo.getOfficeManagedObjectName());
		assertEquals("administration - team", "TEAM", adminToTeam.getOfficeTeamName());
		assertEquals("governance - team", "TEAM", govToTeam.getOfficeTeamName());
		assertEquals("start - input (section name)", "SECTION_TARGET", startToInput.getOfficeSectionName());
		assertEquals("start - input (input name)", "INPUT", startToInput.getOfficeSectionInputName());
		assertEquals("function - pre admin (administration name)", "ADMINISTRATION",
				functionToPreAdmin.getAdministrationName());

		fail("TODO test administration flows and escalations");

		assertEquals("function - post duty (administration name)", "ADMINISTRATION",
				functionToPostAdmin.getAdministrationName());

		fail("TODO test administration flows and escalations");

		assertEquals("function - governance", "GOVERNANCE", functionToGov.getGovernanceName());
		assertEquals("sub section - governance", "GOVERNANCE", subSectionToGov.getGovernanceName());
		assertEquals("section managed object - governance", "GOVERNANCE", sectionMoToGov.getGovernanceName());
		assertEquals("external managed object - administration", "ADMINISTRATION",
				extMoToAdmin.getAdministrationName());
		assertEquals("external managed object - governance", "GOVERNANCE", extMoToGov.getGovernanceName());
		assertEquals("office managed object - administration", "ADMINISTRATION", moToAdmin.getAdministrationName());
		assertEquals("office managed object - governance", "GOVERNANCE", moToGov.getGovernanceName());
		assertEquals("escalation - input (section name)", "SECTION_TARGET", escalationToInput.getOfficeSectionName());
		assertEquals("escalation - input (input name", "INPUT", escalationToInput.getOfficeSectionInputName());
	}

}