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
package net.officefloor.model.impl.repository;

import java.sql.Connection;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationItem;
import net.officefloor.model.impl.repository.memory.MemoryConfigurationItem;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToOfficeTeamModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministratorModel;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectTeamModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityObjectModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskToPostDutyModel;
import net.officefloor.model.office.OfficeTaskToPreDutyModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * Tests the marshaling/unmarshaling of the {@link OfficeModel} via the
 * {@link ModelRepository}.
 * 
 * @author Daniel
 */
public class OfficeModelRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ConfigurationItem} containing the {@link DeskModel}.
	 */
	private ConfigurationItem configurationItem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Specify location of the configuration
		this.configurationItem = new FileSystemConfigurationItem(this.findFile(
				this.getClass(), "TestOffice.office.xml"), null);
	}

	/**
	 * Ensure retrieve the {@link OfficeModel}.
	 */
	public void testRetrieveOffice() throws Exception {

		// Load the Office
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeModel office = new OfficeModel();
		office = repository.retrieve(office, this.configurationItem);

		// ----------------------------------------
		// Validate the external managed objects
		// ----------------------------------------
		assertList(new String[] { "getExternalManagedObjectName",
				"getObjectType", "getX", "getY" }, office
				.getExternalManagedObjects(), new ExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT", Connection.class.getName(), null,
				null, 10, 11));
		ExternalManagedObjectModel extMo = office.getExternalManagedObjects()
				.get(0);
		assertList(new String[] { "getAdministratorName", "getOrder" }, extMo
				.getAdministrators(),
				new ExternalManagedObjectToAdministratorModel("ADMINISTRATOR",
						"1"));

		// ----------------------------------------
		// Validate the teams
		// ----------------------------------------
		assertList(new String[] { "getOfficeTeamName", "getX", "getY" }, office
				.getOfficeTeams(), new OfficeTeamModel("TEAM", null, null, 20,
				21));

		// ----------------------------------------
		// Validate the escalations
		// ----------------------------------------
		assertList(new String[] { "getEscalationType", "getX", "getY" }, office
				.getOfficeEscalations(), new OfficeEscalationModel(
				Exception.class.getName(), 30, 31));

		// ----------------------------------------
		// Validate the administrators
		// ----------------------------------------
		assertList(new String[] { "getAdministratorName",
				"getAdministratorSourceClassName", "getAdministratorScope",
				"getX", "getY" }, office.getOfficeAdministrators(),
				new AdministratorModel("ADMINISTRATOR",
						"net.example.ExampleAdministratorSource", "THREAD",
						null, null, null, null, 40, 41));
		AdministratorModel admin = office.getOfficeAdministrators().get(0);
		assertList(new String[] { "getName", "getValue" }, admin
				.getProperties(), new PropertyModel("ADMIN_ONE", "VALUE_ONE"),
				new PropertyModel("ADMIN_TWO", "VALUE_TWO"));
		assertProperties(new AdministratorToOfficeTeamModel("TEAM"), admin
				.getOfficeTeam(), "getOfficeTeamName");
		assertList(new String[] { "getDutyName" }, admin.getDuties(),
				new DutyModel("DUTY_ONE"), new DutyModel("DUTY_TWO"));

		// ----------------------------------------
		// Validate the sections
		// ----------------------------------------
		assertList(new String[] { "getOfficeSectionName",
				"getSectionSourceClassName", "getSectionLocation", "getX",
				"getY" }, office.getOfficeSections(),
				new OfficeSectionModel("SECTION",
						"net.example.ExampleSectionSource", "SECTION_LOCATION",
						null, null, null, null, null, null, 50, 51),
				new OfficeSectionModel("SECTION_TARGET",
						"net.example.ExampleSectionSource", "SECTION_LOCATION",
						null, null, null, null, null, null, 60, 61));
		OfficeSectionModel section = office.getOfficeSections().get(0);
		assertList(new String[] { "getName", "getValue" }, section
				.getProperties(), new PropertyModel("PROP_ONE", "VALUE_ONE"),
				new PropertyModel("PROP_TWO", "VALUE_TWO"));

		// Inputs of section
		assertList(new String[] { "getOfficeSectionInputName",
				"getParameterType" }, section.getOfficeSectionInputs(),
				new OfficeSectionInputModel("INPUT", Integer.class.getName()));

		// Outputs of section
		assertList(new String[] { "getOfficeSectionOutputName",
				"getArgumentType", "getEscalationOnly" }, section
				.getOfficeSectionOutputs(), new OfficeSectionOutputModel(
				"OUTPUT_ONE", Float.class.getName(), false),
				new OfficeSectionOutputModel("OUTPUT_TWO", Exception.class
						.getName(), true));
		OfficeSectionOutputModel output = section.getOfficeSectionOutputs()
				.get(0);
		assertProperties(new OfficeSectionOutputToOfficeSectionInputModel(
				"SECTION_TARGET", "INPUT"), output.getOfficeSectionInput(),
				"getOfficeSectionName", "getOfficeSectionInputName");

		// Objects of section
		assertList(
				new String[] { "getOfficeSectionObjectName", "getObjectType" },
				section.getOfficeSectionObjects(),
				new OfficeSectionObjectModel("OBJECT", Connection.class
						.getName()));
		OfficeSectionObjectModel object = section.getOfficeSectionObjects()
				.get(0);
		assertProperties(new OfficeSectionObjectToExternalManagedObjectModel(
				"EXTERNAL_MANAGED_OBJECT"), object.getExternalManagedObject(),
				"getExternalManagedObjectName");

		// Responsibilities of section
		assertList(new String[] { "getOfficeSectionResponsibilityName" },
				section.getOfficeSectionResponsibilities(),
				new OfficeSectionResponsibilityModel("RESPONSIBILITY"));
		OfficeSectionResponsibilityModel responsibility = section
				.getOfficeSectionResponsibilities().get(0);
		assertProperties(new OfficeSectionResponsibilityToOfficeTeamModel(
				"TEAM"), responsibility.getOfficeTeam(), "getOfficeTeamName");
		assertList(new String[] { "getOfficeSectionResponsibilityObjectName" },
				responsibility.getOfficeSectionResponsibilityObjects(),
				new OfficeSectionResponsibilityObjectModel(
						"RESPONSIBILITY_OBJECT"));

		// Sub sections
		assertNotNull("Must have sub section of office section", section
				.getOfficeSubSection());
		OfficeSubSectionModel officeSection = section.getOfficeSubSection();
		assertList(new String[] { "getOfficeSectionManagedObjectName" },
				officeSection.getOfficeSectionManagedObjects(),
				new OfficeSectionManagedObjectModel("SECTION_MANAGED_OBJECT"));
		OfficeSectionManagedObjectModel sectionMo = officeSection
				.getOfficeSectionManagedObjects().get(0);
		assertList(new String[] { "getOfficeSectionManagedObjectTeamName" },
				sectionMo.getOfficeSectionManagedObjectTeams(),
				new OfficeSectionManagedObjectTeamModel("MO_TEAM"));
		assertList(new String[] { "getOfficeTaskName" }, officeSection
				.getOfficeTasks(), new OfficeTaskModel("TASK"));
		OfficeTaskModel sectionTask = officeSection.getOfficeTasks().get(0);
		assertList(new String[] { "getAdministratorName", "getDutyName" },
				sectionTask.getPreDuties(), new OfficeTaskToPreDutyModel(
						"ADMINISTRATOR", "DUTY_ONE"));
		assertList(new String[] { "getOfficeSubSectionName" }, officeSection
				.getOfficeSubSections(), new OfficeSubSectionModel(
				"SUB_SECTION"));
		OfficeSubSectionModel subSection = officeSection.getOfficeSubSections()
				.get(0);
		assertList(new String[] { "getOfficeSectionManagedObjectName" },
				subSection.getOfficeSectionManagedObjects(),
				new OfficeSectionManagedObjectModel(
						"SUB_SECTION_MANAGED_OBJECT"));
		assertList(new String[] { "getOfficeTaskName" }, subSection
				.getOfficeTasks(), new OfficeTaskModel("SUB_SECTION_TASK"));
		OfficeTaskModel subSectionTask = subSection.getOfficeTasks().get(0);
		assertList(new String[] { "getAdministratorName", "getDutyName" },
				subSectionTask.getPostDuties(), new OfficeTaskToPostDutyModel(
						"ADMINISTRATOR", "DUTY_TWO"));
		assertList(new String[] { "getOfficeSubSectionName" }, subSection
				.getOfficeSubSections(), new OfficeSubSectionModel(
				"SUB_SUB_SECTION"));
	}

	/**
	 * Ensure able to round trip storing and retrieving the {@link OfficeModel}.
	 */
	public void testRoundTripStoreRetrieveOffice() throws Exception {

		// Load the Office
		ModelRepository repository = new ModelRepositoryImpl();
		OfficeModel office = new OfficeModel();
		office = repository.retrieve(office, this.configurationItem);

		// Store the Office
		MemoryConfigurationItem contents = new MemoryConfigurationItem();
		repository.store(office, contents);

		// Reload the Office
		OfficeModel reloadedOffice = new OfficeModel();
		reloadedOffice = repository.retrieve(reloadedOffice, contents);

		// Validate round trip
		assertGraph(office, reloadedOffice,
				RemoveConnectionsAction.REMOVE_CONNECTIONS_METHOD_NAME);
	}

}