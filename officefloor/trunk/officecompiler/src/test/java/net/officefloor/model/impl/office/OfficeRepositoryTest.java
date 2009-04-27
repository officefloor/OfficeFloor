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
package net.officefloor.model.impl.office;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRepository;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link OfficeRepository}.
 * 
 * @author Daniel
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
		OfficeTeamModel team = new OfficeTeamModel("TEAM");
		office.addOfficeTeam(team);
		OfficeSectionModel section = new OfficeSectionModel("SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION");
		office.addOfficeSection(section);
		OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel(
				"RESPONSIBILITY");
		section.addOfficeSectionResponsibility(responsibility);

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
	}

	/**
	 * Ensures on storing a {@link OfficeModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreOffice() throws Exception {

		// Create the office (without connections)
		OfficeModel office = new OfficeModel();
		OfficeTeamModel team = new OfficeTeamModel("TEAM");
		office.addOfficeTeam(team);
		OfficeSectionModel section = new OfficeSectionModel("SECTION",
				"net.example.ExampleSectionSource", "SECTION_LOCATION");
		office.addOfficeSection(section);
		OfficeSectionResponsibilityModel responsibility = new OfficeSectionResponsibilityModel(
				"RESPONSIBILITY");
		section.addOfficeSectionResponsibility(responsibility);

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
	}
}