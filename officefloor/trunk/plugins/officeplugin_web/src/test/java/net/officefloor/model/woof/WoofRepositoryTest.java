/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.model.woof;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link WoofRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofRepositoryTest extends OfficeFrameTestCase {

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
	 * {@link WoofRepository}.
	 */
	private final WoofRepository woofRepository = new WoofRepositoryImpl(
			this.modelRepository);

	/**
	 * Ensures on retrieving a {@link WoofModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveWoOF() throws Exception {

		// Create the raw WoOF to be connected
		WoofModel woof = new WoofModel();
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null,
				null, null);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel(
				"TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel(
				"SECTION_INPUT", null, null);
		section.addInput(sectionInput);

		// Template Output -> Section Input
		WoofTemplateOutputToWoofSectionInputModel templateToSection = new WoofTemplateOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		templateOutput.setWoofSectionInput(templateToSection);

		// Record retrieving the WoOF
		this.recordReturn(this.modelRepository,
				this.modelRepository.retrieve(null, this.configurationItem),
				woof, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertTrue("Must be woof model",
								actual[0] instanceof WoofModel);
						assertEquals("Incorrect configuration item",
								WoofRepositoryTest.this.configurationItem,
								actual[1]);
						return true;
					}
				});

		// Retrieve the WoOF
		this.replayMockObjects();
		WoofModel retrievedWoof = this.woofRepository
				.retrieveWoOF(this.configurationItem);
		this.verifyMockObjects();
		assertEquals("Incorrect WoOF", woof, retrievedWoof);

		// Ensure template output connected to section input
		assertEquals("template output <- section input", templateOutput,
				templateToSection.getWoofTemplateOutput());
		assertEquals("template output -> section input", sectionInput,
				templateToSection.getWoofSectionInput());
	}

	/**
	 * Ensures on storing a {@link WoofModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreWoOF() throws Exception {

		// Create the WoOF (without connections)
		WoofModel woof = new WoofModel();
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null,
				null, null);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel(
				"TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel(
				"SECTION_INPUT", null, null);
		section.addInput(sectionInput);

		// Template Output -> Section Input
		WoofTemplateOutputToWoofSectionInputModel templateToSection = new WoofTemplateOutputToWoofSectionInputModel();
		templateToSection.setWoofTemplateOutput(templateOutput);
		templateToSection.setWoofSectionInput(sectionInput);
		templateToSection.connect();

		// Record storing the WoOf
		this.modelRepository.store(woof, this.configurationItem);

		// Store the WoOF
		this.replayMockObjects();
		this.woofRepository.storeWoOF(woof, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("template output - section input (section name)",
				"SECTION", templateToSection.getSectionName());
		assertEquals("template output - section input (input name)",
				"SECTION_INPUT", templateToSection.getInputName());
	}

}