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
package net.officefloor.model.woof;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
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
				null, null, null, null, false, false);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel(
				"TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel(
				"SECTION_INPUT", null, null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel(
				"SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofAccessModel access = new WoofAccessModel();
		woof.setWoofAccess(access);
		WoofAccessInputModel accessInput = new WoofAccessInputModel(
				"ACCESS_INPUT", null);
		access.addInput(accessInput);
		WoofAccessOutputModel accessOutput = new WoofAccessOutputModel(
				"ACCESS_OUTPUT", null);
		access.addOutput(accessOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE", null);
		woof.addWoofResource(resource);
		WoofExceptionModel exception = new WoofExceptionModel("EXCEPTION");
		woof.addWoofException(exception);
		WoofStartModel start = new WoofStartModel();
		woof.addWoofStart(start);

		// Template Output -> Section Input
		WoofTemplateOutputToWoofSectionInputModel templateToSection = new WoofTemplateOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		templateOutput.setWoofSectionInput(templateToSection);

		// Template Output -> Template
		WoofTemplateOutputToWoofTemplateModel templateToTemplate = new WoofTemplateOutputToWoofTemplateModel(
				"TEMPLATE");
		templateOutput.setWoofTemplate(templateToTemplate);

		// Template Output -> Access Input
		WoofTemplateOutputToWoofAccessInputModel templateToAccess = new WoofTemplateOutputToWoofAccessInputModel(
				"ACCESS_INPUT");
		templateOutput.setWoofAccessInput(templateToAccess);

		// Template Output -> Resource
		WoofTemplateOutputToWoofResourceModel templateToResource = new WoofTemplateOutputToWoofResourceModel(
				"RESOURCE");
		templateOutput.setWoofResource(templateToResource);

		// Section Output -> Section Input
		WoofSectionOutputToWoofSectionInputModel sectionToSection = new WoofSectionOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		sectionOutput.setWoofSectionInput(sectionToSection);

		// Section Output -> Template
		WoofSectionOutputToWoofTemplateModel sectionToTemplate = new WoofSectionOutputToWoofTemplateModel(
				"TEMPLATE");
		sectionOutput.setWoofTemplate(sectionToTemplate);

		// Section Output -> Access Input
		WoofSectionOutputToWoofAccessInputModel sectionToAccess = new WoofSectionOutputToWoofAccessInputModel(
				"ACCESS_INPUT");
		sectionOutput.setWoofAccessInput(sectionToAccess);

		// Section Output -> Resource
		WoofSectionOutputToWoofResourceModel sectionToResource = new WoofSectionOutputToWoofResourceModel(
				"RESOURCE");
		sectionOutput.setWoofResource(sectionToResource);

		// Access Output -> Section Input
		WoofAccessOutputToWoofSectionInputModel accessToSection = new WoofAccessOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		accessOutput.setWoofSectionInput(accessToSection);

		// Access Output -> Template
		WoofAccessOutputToWoofTemplateModel accessToTemplate = new WoofAccessOutputToWoofTemplateModel(
				"TEMPLATE");
		accessOutput.setWoofTemplate(accessToTemplate);

		// Access Output -> Resource
		WoofAccessOutputToWoofResourceModel accessToResource = new WoofAccessOutputToWoofResourceModel(
				"RESOURCE");
		accessOutput.setWoofResource(accessToResource);

		// Exception -> Section Input
		WoofExceptionToWoofSectionInputModel exceptionToSection = new WoofExceptionToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		exception.setWoofSectionInput(exceptionToSection);

		// Exception -> Template
		WoofExceptionToWoofTemplateModel exceptionToTemplate = new WoofExceptionToWoofTemplateModel(
				"TEMPLATE");
		exception.setWoofTemplate(exceptionToTemplate);

		// Exception -> Resource
		WoofExceptionToWoofResourceModel exceptionToResource = new WoofExceptionToWoofResourceModel(
				"RESOURCE");
		exception.setWoofResource(exceptionToResource);

		// Start -> Section Input
		WoofStartToWoofSectionInputModel startToSection = new WoofStartToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		start.setWoofSectionInput(startToSection);

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

		// Ensure template output connected to template
		assertEquals("template output <- template", templateOutput,
				templateToTemplate.getWoofTemplateOutput());
		assertEquals("template output -> template", template,
				templateToTemplate.getWoofTemplate());

		// Ensure template output connected to access input
		assertEquals("template output <- access input", templateOutput,
				templateToAccess.getWoofTemplateOutput());
		assertEquals("template output -> access input", accessInput,
				templateToAccess.getWoofAccessInput());

		// Ensure template output connected to resource
		assertEquals("template output <- resource", templateOutput,
				templateToResource.getWoofTemplateOutput());
		assertEquals("template otuput -> resource", resource,
				templateToResource.getWoofResource());

		// Ensure section output connected to section input
		assertEquals("section output <- section input", sectionOutput,
				sectionToSection.getWoofSectionOutput());
		assertEquals("section output -> section input", sectionInput,
				sectionToSection.getWoofSectionInput());

		// Ensure section output connected to template
		assertEquals("section output <- template", sectionOutput,
				sectionToTemplate.getWoofSectionOutput());
		assertEquals("section output -> template", template,
				sectionToTemplate.getWoofTemplate());

		// Ensure section output connected to access input
		assertEquals("section output <- access input", sectionOutput,
				sectionToAccess.getWoofSectionOutput());
		assertEquals("section output -> access input", accessInput,
				sectionToAccess.getWoofAccessInput());

		// Ensure section output connected to resource
		assertEquals("section output <- resource", sectionOutput,
				sectionToResource.getWoofSectionOutput());
		assertEquals("section output -> resource", resource,
				sectionToResource.getWoofResource());

		// Ensure access output connected to section input
		assertEquals("access output <- section input", accessOutput,
				accessToSection.getWoofAccessOutput());
		assertEquals("access output -> section input", sectionInput,
				accessToSection.getWoofSectionInput());

		// Ensure access output connected to template
		assertEquals("access output <- template", accessOutput,
				accessToTemplate.getWoofAccessOutput());
		assertEquals("access output -> template", template,
				accessToTemplate.getWoofTemplate());

		// Ensure access output connected to resource
		assertEquals("access output <- resource", accessOutput,
				accessToResource.getWoofAccessOutput());
		assertEquals("access output -> resource", resource,
				accessToResource.getWoofResource());

		// Ensure exception connected to section input
		assertEquals("exception <- section input", exception,
				exceptionToSection.getWoofException());
		assertEquals("exception -> section input", sectionInput,
				exceptionToSection.getWoofSectionInput());

		// Ensure exception connected to template
		assertEquals("exception <- template", exception,
				exceptionToTemplate.getWoofException());
		assertEquals("exception -> template", template,
				exceptionToTemplate.getWoofTemplate());

		// Ensure exception connected to resource
		assertEquals("exception <- resource", exception,
				exceptionToResource.getWoofException());
		assertEquals("exception -> resource", resource,
				exceptionToResource.getWoofResource());

		// Ensure start connected to section input
		assertEquals("start <- section input", start,
				startToSection.getWoofStart());
		assertEquals("start -> section input", sectionInput,
				startToSection.getWoofSectionInput());
	}

	/**
	 * Ensures on storing a {@link WoofModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreWoOF() throws Exception {

		// Create the WoOF (without connections)
		WoofModel woof = new WoofModel();
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null,
				null, null, null, null, false, false);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel(
				"TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel(
				"SECTION_INPUT", null, null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel(
				"SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofAccessModel access = new WoofAccessModel();
		woof.setWoofAccess(access);
		WoofAccessInputModel accessInput = new WoofAccessInputModel(
				"ACCESS_INPUT", null);
		access.addInput(accessInput);
		WoofAccessOutputModel accessOutput = new WoofAccessOutputModel(
				"ACCESS_OUTPUT", null);
		access.addOutput(accessOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE", null);
		woof.addWoofResource(resource);
		WoofExceptionModel exception = new WoofExceptionModel("EXCEPTION");
		woof.addWoofException(exception);
		WoofStartModel start = new WoofStartModel();
		woof.addWoofStart(start);

		// Template Output -> Section Input
		WoofTemplateOutputToWoofSectionInputModel templateToSection = new WoofTemplateOutputToWoofSectionInputModel();
		templateToSection.setWoofTemplateOutput(templateOutput);
		templateToSection.setWoofSectionInput(sectionInput);
		templateToSection.connect();

		// Template Output -> Template
		WoofTemplateOutputToWoofTemplateModel templateToTemplate = new WoofTemplateOutputToWoofTemplateModel();
		templateToTemplate.setWoofTemplateOutput(templateOutput);
		templateToTemplate.setWoofTemplate(template);
		templateToTemplate.connect();

		// Template Output -> Access Input
		WoofTemplateOutputToWoofAccessInputModel templateToAccess = new WoofTemplateOutputToWoofAccessInputModel();
		templateToAccess.setWoofTemplateOutput(templateOutput);
		templateToAccess.setWoofAccessInput(accessInput);
		templateToAccess.connect();

		// Template Output -> Resource
		WoofTemplateOutputToWoofResourceModel templateToResource = new WoofTemplateOutputToWoofResourceModel();
		templateToResource.setWoofTemplateOutput(templateOutput);
		templateToResource.setWoofResource(resource);
		templateToResource.connect();

		// Section Output -> Section Input
		WoofSectionOutputToWoofSectionInputModel sectionToSection = new WoofSectionOutputToWoofSectionInputModel();
		sectionToSection.setWoofSectionOutput(sectionOutput);
		sectionToSection.setWoofSectionInput(sectionInput);
		sectionToSection.connect();

		// Section Output -> Template
		WoofSectionOutputToWoofTemplateModel sectionToTemplate = new WoofSectionOutputToWoofTemplateModel();
		sectionToTemplate.setWoofSectionOutput(sectionOutput);
		sectionToTemplate.setWoofTemplate(template);
		sectionToTemplate.connect();

		// Section Output -> Access Input
		WoofSectionOutputToWoofAccessInputModel sectionToAccess = new WoofSectionOutputToWoofAccessInputModel();
		sectionToAccess.setWoofSectionOutput(sectionOutput);
		sectionToAccess.setWoofAccessInput(accessInput);
		sectionToAccess.connect();

		// Section Output -> Resource
		WoofSectionOutputToWoofResourceModel sectionToResource = new WoofSectionOutputToWoofResourceModel();
		sectionToResource.setWoofSectionOutput(sectionOutput);
		sectionToResource.setWoofResource(resource);
		sectionToResource.connect();

		// Access Output -> Section Input
		WoofAccessOutputToWoofSectionInputModel accessToSection = new WoofAccessOutputToWoofSectionInputModel();
		accessToSection.setWoofAccessOutput(accessOutput);
		accessToSection.setWoofSectionInput(sectionInput);
		accessToSection.connect();

		// Access Output -> Template
		WoofAccessOutputToWoofTemplateModel accessToTemplate = new WoofAccessOutputToWoofTemplateModel();
		accessToTemplate.setWoofAccessOutput(accessOutput);
		accessToTemplate.setWoofTemplate(template);
		accessToTemplate.connect();

		// Access Output -> Resource
		WoofAccessOutputToWoofResourceModel accessToResource = new WoofAccessOutputToWoofResourceModel();
		accessToResource.setWoofAccessOutput(accessOutput);
		accessToResource.setWoofResource(resource);
		accessToResource.connect();

		// Exception -> Section Input
		WoofExceptionToWoofSectionInputModel exceptionToSection = new WoofExceptionToWoofSectionInputModel();
		exceptionToSection.setWoofException(exception);
		exceptionToSection.setWoofSectionInput(sectionInput);
		exceptionToSection.connect();

		// Exception -> Template
		WoofExceptionToWoofTemplateModel exceptionToTemplate = new WoofExceptionToWoofTemplateModel();
		exceptionToTemplate.setWoofException(exception);
		exceptionToTemplate.setWoofTemplate(template);
		exceptionToTemplate.connect();

		// Exception -> Resource
		WoofExceptionToWoofResourceModel exceptionToResource = new WoofExceptionToWoofResourceModel();
		exceptionToResource.setWoofException(exception);
		exceptionToResource.setWoofResource(resource);
		exceptionToResource.connect();

		// Start -> Section Input
		WoofStartToWoofSectionInputModel startToSection = new WoofStartToWoofSectionInputModel();
		startToSection.setWoofStart(start);
		startToSection.setWoofSectionInput(sectionInput);
		startToSection.connect();

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
		assertEquals("template output - template", "TEMPLATE",
				templateToTemplate.getTemplateName());
		assertEquals("template output - access input", "ACCESS_INPUT",
				templateToAccess.getInputName());
		assertEquals("template output - resource", "RESOURCE",
				templateToResource.getResourceName());
		assertEquals("section output - section input (section name)",
				"SECTION", sectionToSection.getSectionName());
		assertEquals("section output - section input (input name)",
				"SECTION_INPUT", sectionToSection.getInputName());
		assertEquals("section output - template", "TEMPLATE",
				sectionToTemplate.getTemplateName());
		assertEquals("section output - access input", "ACCESS_INPUT",
				sectionToAccess.getInputName());
		assertEquals("section output - resource", "RESOURCE",
				sectionToResource.getResourceName());
		assertEquals("access output - section input (section name)", "SECTION",
				accessToSection.getSectionName());
		assertEquals("access output - section input (input name",
				"SECTION_INPUT", accessToSection.getInputName());
		assertEquals("access output - template", "TEMPLATE",
				accessToTemplate.getTemplateName());
		assertEquals("access output - resource", "RESOURCE",
				accessToResource.getResourceName());
		assertEquals("exception - section input (section name)", "SECTION",
				exceptionToSection.getSectionName());
		assertEquals("exception - section input (input name)", "SECTION_INPUT",
				exceptionToSection.getInputName());
		assertEquals("exception - template", "TEMPLATE",
				exceptionToTemplate.getTemplateName());
		assertEquals("exception - resource", "RESOURCE",
				exceptionToResource.getResourceName());
		assertEquals("start - section input (section name)", "SECTION",
				startToSection.getSectionName());
		assertEquals("start - section input (input name)", "SECTION_INPUT",
				startToSection.getInputName());
	}

}