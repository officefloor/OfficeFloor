/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.model.impl.office;

import java.sql.Connection;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskToPostDutyModel;
import net.officefloor.model.office.OfficeTaskToPreDutyModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link OfficeRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeRepositoryTest extends OfficeFrameTestCase {

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
	 * {@link OfficeRepository} to be tested.
	 */
	private final OfficeRepository officeRepository = new OfficeRepositoryImpl(
			this.modelRepository);

	/**
	 * Ensures on retrieving a {@link OfficeModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveOffice() throws Exception {

		// Create the raw office to be connected
		OfficeModel office = new OfficeModel();
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT", Connection.class.getName());
		office.addExternalManagedObject(extMo);
		OfficeTeamModel team = new OfficeTeamModel("TEAM");
		office.addOfficeTeam(team);
		OfficeSectionModel section = new OfficeSectionModel("SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION");
		office.addOfficeSection(section);
		OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel(
				"RESPONSIBILITY");
		section.addOfficeSectionResponsibility(responsibility);
		AdministratorModel admin = new AdministratorModel("ADMINISTRATOR",
				"net.example.ExampleAdministratorSource", "THREAD");
		office.addOfficeAdministrator(admin);
		DutyModel duty = new DutyModel("DUTY");
		admin.addDuty(duty);

		// responsibility -> team
		OfficeSectionResponsibilityToOfficeTeamModel respToTeam = new OfficeSectionResponsibilityToOfficeTeamModel(
				"TEAM");
		responsibility.setOfficeTeam(respToTeam);

		// section output -> section input
		OfficeSectionOutputModel output = new OfficeSectionOutputModel(
				"OUTPUT", String.class.getName(), false);
		section.addOfficeSectionOutput(output);
		OfficeSectionModel targetSection = new OfficeSectionModel(
				"SECTION_TARGET", "net.example.ExcampleSectionSource",
				"SECTION_LOCATION");
		office.addOfficeSection(targetSection);
		OfficeSectionInputModel targetInput = new OfficeSectionInputModel(
				"INPUT", String.class.getName());
		targetSection.addOfficeSectionInput(targetInput);
		OfficeSectionOutputToOfficeSectionInputModel outputToInput = new OfficeSectionOutputToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT");
		output.setOfficeSectionInput(outputToInput);

		// section object -> external managed object
		OfficeSectionObjectModel object = new OfficeSectionObjectModel(
				"OBJECT", Connection.class.getName());
		section.addOfficeSectionObject(object);
		OfficeSectionObjectToExternalManagedObjectModel objectToExtMo = new OfficeSectionObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT");
		object.setExternalManagedObject(objectToExtMo);

		// administrator -> team
		AdministratorToOfficeTeamModel adminToTeam = new AdministratorToOfficeTeamModel(
				"TEAM");
		admin.setOfficeTeam(adminToTeam);

		// office task -> duty (setup)
		OfficeSubSectionModel subSection = new OfficeSubSectionModel();
		section.setOfficeSubSection(subSection);
		OfficeSubSectionModel subSubSection = new OfficeSubSectionModel(
				"SUB_SECTION");
		subSection.addOfficeSubSection(subSubSection);
		OfficeTaskModel officeTask = new OfficeTaskModel("TASK");
		subSubSection.addOfficeTask(officeTask);

		// office task -> pre duty
		OfficeTaskToPreDutyModel taskToPreDuty = new OfficeTaskToPreDutyModel(
				"ADMINISTRATOR", "DUTY");
		officeTask.addPreDuty(taskToPreDuty);

		// office task -> post duty
		OfficeTaskToPostDutyModel taskToPostDuty = new OfficeTaskToPostDutyModel(
				"ADMINISTRATOR", "DUTY");
		officeTask.addPostDuty(taskToPostDuty);

		// external managed object -> administrator
		ExternalManagedObjectToAdministratorModel extMoToAdmin = new ExternalManagedObjectToAdministratorModel(
				"ADMINISTRATOR", "1");
		extMo.addAdministrator(extMoToAdmin);

		// Record retrieving the office
		this.recordReturn(this.modelRepository, this.modelRepository.retrieve(
				null, this.configurationItem), office, new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertTrue("Must be office model",
						actual[0] instanceof OfficeModel);
				assertEquals("Incorrect configuration item",
						OfficeRepositoryTest.this.configurationItem, actual[1]);
				return true;
			}
		});

		// Retrieve the office
		this.replayMockObjects();
		OfficeModel retrievedOffice = this.officeRepository
				.retrieveOffice(this.configurationItem);
		this.verifyMockObjects();
		assertEquals("Incorrect office", office, retrievedOffice);

		// Ensure the responsibility team connected
		assertEquals("responsibility <- team", responsibility, respToTeam
				.getOfficeSectionResponsibility());
		assertEquals("responsibility -> team", team, respToTeam.getOfficeTeam());

		// Ensure the outputs connected to inputs
		assertEquals("output <- input", output, outputToInput
				.getOfficeSectionOutput());
		assertEquals("output -> input", targetInput, outputToInput
				.getOfficeSectionInput());

		// Ensure the objects connected to external managed object
		assertEquals("object <- external managed object", object, objectToExtMo
				.getOfficeSectionObject());
		assertEquals("object -> external managed object", extMo, objectToExtMo
				.getExternalManagedObject());

		// Ensure the administrator teams connected
		assertEquals("administrator <- team", admin, adminToTeam
				.getAdministrator());
		assertEquals("administrator -> team", team, adminToTeam.getOfficeTeam());

		// Ensure the office task pre duties connected
		assertEquals("task <- pre duty", officeTask, taskToPreDuty
				.getOfficeTask());
		assertEquals("task -> pre duty", duty, taskToPreDuty.getDuty());

		// Ensure the office task post duties connected
		assertEquals("task <- post duty", officeTask, taskToPostDuty
				.getOfficeTask());
		assertEquals("task -> post duty", duty, taskToPostDuty.getDuty());

		// Ensure external managed object administration connected
		assertEquals("external managed object <- administrator", extMo,
				extMoToAdmin.getExternalManagedObject());
		assertEquals("external managed object -> administrator", admin,
				extMoToAdmin.getAdministrator());
	}

	/**
	 * Ensures on storing a {@link OfficeModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreOffice() throws Exception {

		// Create the office (without connections)
		OfficeModel office = new OfficeModel();
		ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT", Connection.class.getName());
		office.addExternalManagedObject(extMo);
		OfficeTeamModel team = new OfficeTeamModel("TEAM");
		office.addOfficeTeam(team);
		OfficeSectionModel section = new OfficeSectionModel("SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION");
		office.addOfficeSection(section);
		OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel(
				"RESPONSIBILITY");
		section.addOfficeSectionResponsibility(responsibility);
		AdministratorModel admin = new AdministratorModel("ADMINISTRATOR",
				"net.example.ExampleAdministratorSource", "THREAD");
		office.addOfficeAdministrator(admin);
		DutyModel duty = new DutyModel("DUTY");
		admin.addDuty(duty);

		// responsibility -> team
		OfficeSectionResponsibilityToOfficeTeamModel respToTeam = new OfficeSectionResponsibilityToOfficeTeamModel();
		respToTeam.setOfficeSectionResponsibility(responsibility);
		respToTeam.setOfficeTeam(team);
		respToTeam.connect();

		// section output -> section input
		OfficeSectionOutputModel output = new OfficeSectionOutputModel(
				"OUTPUT", String.class.getName(), false);
		section.addOfficeSectionOutput(output);
		OfficeSectionModel targetSection = new OfficeSectionModel(
				"SECTION_TARGET", "net.example.ExcampleSectionSource",
				"SECTION_LOCATION");
		office.addOfficeSection(targetSection);
		OfficeSectionInputModel targetInput = new OfficeSectionInputModel(
				"INPUT", String.class.getName());
		targetSection.addOfficeSectionInput(targetInput);
		OfficeSectionOutputToOfficeSectionInputModel outputToInput = new OfficeSectionOutputToOfficeSectionInputModel();
		outputToInput.setOfficeSectionOutput(output);
		outputToInput.setOfficeSectionInput(targetInput);
		outputToInput.connect();

		// section object -> external managed object
		OfficeSectionObjectModel object = new OfficeSectionObjectModel(
				"OBJECT", Connection.class.getName());
		section.addOfficeSectionObject(object);
		OfficeSectionObjectToExternalManagedObjectModel objectToExtMo = new OfficeSectionObjectToExternalManagedObjectModel();
		objectToExtMo.setOfficeSectionObject(object);
		objectToExtMo.setExternalManagedObject(extMo);
		objectToExtMo.connect();

		// administrator -> team
		AdministratorToOfficeTeamModel adminToTeam = new AdministratorToOfficeTeamModel();
		adminToTeam.setAdministrator(admin);
		adminToTeam.setOfficeTeam(team);
		adminToTeam.connect();

		// office task -> duty (setup)
		OfficeSubSectionModel subSection = new OfficeSubSectionModel();
		section.setOfficeSubSection(subSection);
		OfficeSubSectionModel subSubSection = new OfficeSubSectionModel(
				"SUB_SECTION");
		subSection.addOfficeSubSection(subSubSection);
		OfficeTaskModel officeTask = new OfficeTaskModel("TASK");
		subSubSection.addOfficeTask(officeTask);

		// office task -> pre duty
		OfficeTaskToPreDutyModel taskToPreDuty = new OfficeTaskToPreDutyModel();
		taskToPreDuty.setOfficeTask(officeTask);
		taskToPreDuty.setDuty(duty);
		taskToPreDuty.connect();

		// office task -> post duty
		OfficeTaskToPostDutyModel taskToPostDuty = new OfficeTaskToPostDutyModel();
		taskToPostDuty.setOfficeTask(officeTask);
		taskToPostDuty.setDuty(duty);
		taskToPostDuty.connect();

		// external managed object -> administrator
		ExternalManagedObjectToAdministratorModel extMoToAdmin = new ExternalManagedObjectToAdministratorModel();
		extMoToAdmin.setExternalManagedObject(extMo);
		extMoToAdmin.setAdministrator(admin);
		extMoToAdmin.connect();

		// Record storing the office
		this.modelRepository.store(office, this.configurationItem);

		// Store the office
		this.replayMockObjects();
		this.officeRepository.storeOffice(office, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("responsibility - team", "TEAM", respToTeam
				.getOfficeTeamName());
		assertEquals("output - input (section name)", "SECTION_TARGET",
				outputToInput.getOfficeSectionName());
		assertEquals("output - input (input name)", "INPUT", outputToInput
				.getOfficeSectionInputName());
		assertEquals("object - external managed object",
				"EXTERNAL_MANAGED_OBJECT", objectToExtMo
						.getExternalManagedObjectName());
		assertEquals("administrator - team", "TEAM", adminToTeam
				.getOfficeTeamName());
		assertEquals("task - pre duty (administrator name)", "ADMINISTRATOR",
				taskToPreDuty.getAdministratorName());
		assertEquals("task - pre duty (duty name)", "DUTY", taskToPreDuty
				.getDutyName());
		assertEquals("task - post duty (administrator name)", "ADMINISTRATOR",
				taskToPostDuty.getAdministratorName());
		assertEquals("task - post duty (duty name)", "DUTY", taskToPostDuty
				.getDutyName());
		assertEquals("external managed object - administrator",
				"ADMINISTRATOR", extMoToAdmin.getAdministratorName());
	}
}