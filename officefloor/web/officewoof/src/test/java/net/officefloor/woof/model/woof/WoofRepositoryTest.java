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
package net.officefloor.woof.model.woof;

import org.easymock.AbstractMatcher;

import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.woof.model.woof.WoofExceptionModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofExceptionToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofRepository;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;
import net.officefloor.woof.model.woof.WoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionModel;
import net.officefloor.woof.model.woof.WoofSectionOutputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSectionOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofSecurityModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofSecurityOutputToWoofTemplateModel;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofResourceModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSectionInputModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofSecurityModel;
import net.officefloor.woof.model.woof.WoofTemplateOutputToWoofTemplateModel;

/**
 * Tests the {@link WoofRepository}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofRepositoryTest extends OfficeFrameTestCase {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository = this.createMock(ModelRepository.class);

	/**
	 * {@link WritableConfigurationItem}.
	 */
	private final WritableConfigurationItem configurationItem = this.createMock(WritableConfigurationItem.class);

	/**
	 * {@link WoofRepository}.
	 */
	private final WoofRepository woofRepository = new WoofRepositoryImpl(this.modelRepository);

	/**
	 * Ensures on retrieving a {@link WoofModel} that all
	 * {@link ConnectionModel} instances are connected.
	 */
	public void testRetrieveWoOF() throws Exception {

		// Create the raw WoOF to be connected
		WoofModel woof = new WoofModel();
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null, null, null, null, null, null, false, null);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel("TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofSecurityModel security = new WoofSecurityModel("SECURITY", null, 1000, null);
		woof.addWoofSecurity(security);
		WoofSecurityOutputModel securityOutput = new WoofSecurityOutputModel("ACCESS_OUTPUT", null);
		security.addOutput(securityOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE");
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

		// Template Output -> Security
		WoofTemplateOutputToWoofSecurityModel templateToSecurity = new WoofTemplateOutputToWoofSecurityModel(
				"SECURITY");
		templateOutput.setWoofSecurity(templateToSecurity);

		// Template Output -> Resource
		WoofTemplateOutputToWoofResourceModel templateToResource = new WoofTemplateOutputToWoofResourceModel(
				"RESOURCE");
		templateOutput.setWoofResource(templateToResource);

		// Section Output -> Section Input
		WoofSectionOutputToWoofSectionInputModel sectionToSection = new WoofSectionOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		sectionOutput.setWoofSectionInput(sectionToSection);

		// Section Output -> Template
		WoofSectionOutputToWoofTemplateModel sectionToTemplate = new WoofSectionOutputToWoofTemplateModel("TEMPLATE");
		sectionOutput.setWoofTemplate(sectionToTemplate);

		// Section Output -> Security
		WoofSectionOutputToWoofSecurityModel sectionToSecurity = new WoofSectionOutputToWoofSecurityModel("SECURITY");
		sectionOutput.setWoofSecurity(sectionToSecurity);

		// Section Output -> Resource
		WoofSectionOutputToWoofResourceModel sectionToResource = new WoofSectionOutputToWoofResourceModel("RESOURCE");
		sectionOutput.setWoofResource(sectionToResource);

		// Security Output -> Section Input
		WoofSecurityOutputToWoofSectionInputModel securityToSection = new WoofSecurityOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		securityOutput.setWoofSectionInput(securityToSection);

		// Security Output -> Template
		WoofSecurityOutputToWoofTemplateModel securityToTemplate = new WoofSecurityOutputToWoofTemplateModel(
				"TEMPLATE");
		securityOutput.setWoofTemplate(securityToTemplate);

		// Security Output -> Resource
		WoofSecurityOutputToWoofResourceModel securityToResource = new WoofSecurityOutputToWoofResourceModel(
				"RESOURCE");
		securityOutput.setWoofResource(securityToResource);

		// Exception -> Section Input
		WoofExceptionToWoofSectionInputModel exceptionToSection = new WoofExceptionToWoofSectionInputModel("SECTION",
				"SECTION_INPUT");
		exception.setWoofSectionInput(exceptionToSection);

		// Exception -> Template
		WoofExceptionToWoofTemplateModel exceptionToTemplate = new WoofExceptionToWoofTemplateModel("TEMPLATE");
		exception.setWoofTemplate(exceptionToTemplate);

		// Exception -> Resource
		WoofExceptionToWoofResourceModel exceptionToResource = new WoofExceptionToWoofResourceModel("RESOURCE");
		exception.setWoofResource(exceptionToResource);

		// Start -> Section Input
		WoofStartToWoofSectionInputModel startToSection = new WoofStartToWoofSectionInputModel("SECTION",
				"SECTION_INPUT");
		start.setWoofSectionInput(startToSection);

		// Record retrieving the WoOF
		this.modelRepository.retrieve(null, this.configurationItem);
		this.control(this.modelRepository).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertTrue("Must be woof model", actual[0] instanceof WoofModel);
				assertEquals("Incorrect configuration item", WoofRepositoryTest.this.configurationItem, actual[1]);
				return true;
			}
		});

		// Retrieve the WoOF
		this.replayMockObjects();
		this.woofRepository.retrieveWoof(woof, this.configurationItem);
		this.verifyMockObjects();

		// Ensure template output connected to section input
		assertEquals("template output <- section input", templateOutput, templateToSection.getWoofTemplateOutput());
		assertEquals("template output -> section input", sectionInput, templateToSection.getWoofSectionInput());

		// Ensure template output connected to template
		assertEquals("template output <- template", templateOutput, templateToTemplate.getWoofTemplateOutput());
		assertEquals("template output -> template", template, templateToTemplate.getWoofTemplate());

		// Ensure template output connected to security
		assertEquals("template output <- security", templateOutput, templateToSecurity.getWoofTemplateOutput());
		assertEquals("template output -> security", security, templateToSecurity.getWoofSecurity());

		// Ensure template output connected to resource
		assertEquals("template output <- resource", templateOutput, templateToResource.getWoofTemplateOutput());
		assertEquals("template otuput -> resource", resource, templateToResource.getWoofResource());

		// Ensure section output connected to section input
		assertEquals("section output <- section input", sectionOutput, sectionToSection.getWoofSectionOutput());
		assertEquals("section output -> section input", sectionInput, sectionToSection.getWoofSectionInput());

		// Ensure section output connected to template
		assertEquals("section output <- template", sectionOutput, sectionToTemplate.getWoofSectionOutput());
		assertEquals("section output -> template", template, sectionToTemplate.getWoofTemplate());

		// Ensure section output connected to access input
		assertEquals("section output <- security", sectionOutput, sectionToSecurity.getWoofSectionOutput());
		assertEquals("section output -> security", security, sectionToSecurity.getWoofSecurity());

		// Ensure section output connected to resource
		assertEquals("section output <- resource", sectionOutput, sectionToResource.getWoofSectionOutput());
		assertEquals("section output -> resource", resource, sectionToResource.getWoofResource());

		// Ensure security output connected to section input
		assertEquals("security output <- section input", securityOutput, securityToSection.getWoofSecurityOutput());
		assertEquals("security output -> section input", sectionInput, securityToSection.getWoofSectionInput());

		// Ensure security output connected to template
		assertEquals("security output <- template", securityOutput, securityToTemplate.getWoofSecurityOutput());
		assertEquals("security output -> template", template, securityToTemplate.getWoofTemplate());

		// Ensure security output connected to resource
		assertEquals("security output <- resource", securityOutput, securityToResource.getWoofSecurityOutput());
		assertEquals("security output -> resource", resource, securityToResource.getWoofResource());

		// Ensure exception connected to section input
		assertEquals("exception <- section input", exception, exceptionToSection.getWoofException());
		assertEquals("exception -> section input", sectionInput, exceptionToSection.getWoofSectionInput());

		// Ensure exception connected to template
		assertEquals("exception <- template", exception, exceptionToTemplate.getWoofException());
		assertEquals("exception -> template", template, exceptionToTemplate.getWoofTemplate());

		// Ensure exception connected to resource
		assertEquals("exception <- resource", exception, exceptionToResource.getWoofException());
		assertEquals("exception -> resource", resource, exceptionToResource.getWoofResource());

		// Ensure start connected to section input
		assertEquals("start <- section input", start, startToSection.getWoofStart());
		assertEquals("start -> section input", sectionInput, startToSection.getWoofSectionInput());
	}

	/**
	 * Ensures on storing a {@link WoofModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreWoOF() throws Exception {

		// Create the WoOF (without connections)
		WoofModel woof = new WoofModel();
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null, null, null, null, null, null, false, null);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel("TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofSecurityModel security = new WoofSecurityModel("ACCESS", null, 1000, null);
		woof.addWoofSecurity(security);
		WoofSecurityOutputModel securityOutput = new WoofSecurityOutputModel("ACCESS_OUTPUT", null);
		security.addOutput(securityOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE");
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

		// Template Output -> Security
		WoofTemplateOutputToWoofSecurityModel templateToSecurity = new WoofTemplateOutputToWoofSecurityModel();
		templateToSecurity.setWoofTemplateOutput(templateOutput);
		templateToSecurity.setWoofSecurity(security);
		templateToSecurity.connect();

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

		// Section Output -> Security
		WoofSectionOutputToWoofSecurityModel sectionToSecurity = new WoofSectionOutputToWoofSecurityModel();
		sectionToSecurity.setWoofSectionOutput(sectionOutput);
		sectionToSecurity.setWoofSecurity(security);
		sectionToSecurity.connect();

		// Section Output -> Resource
		WoofSectionOutputToWoofResourceModel sectionToResource = new WoofSectionOutputToWoofResourceModel();
		sectionToResource.setWoofSectionOutput(sectionOutput);
		sectionToResource.setWoofResource(resource);
		sectionToResource.connect();

		// security Output -> Section Input
		WoofSecurityOutputToWoofSectionInputModel securityToSection = new WoofSecurityOutputToWoofSectionInputModel();
		securityToSection.setWoofSecurityOutput(securityOutput);
		securityToSection.setWoofSectionInput(sectionInput);
		securityToSection.connect();

		// Security Output -> Template
		WoofSecurityOutputToWoofTemplateModel securityToTemplate = new WoofSecurityOutputToWoofTemplateModel();
		securityToTemplate.setWoofSecurityOutput(securityOutput);
		securityToTemplate.setWoofTemplate(template);
		securityToTemplate.connect();

		// Security Output -> Resource
		WoofSecurityOutputToWoofResourceModel securityToResource = new WoofSecurityOutputToWoofResourceModel();
		securityToResource.setWoofSecurityOutput(securityOutput);
		securityToResource.setWoofResource(resource);
		securityToResource.connect();

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
		this.woofRepository.storeWoof(woof, this.configurationItem);
		this.verifyMockObjects();

		// Ensure the connections have links to enable retrieving
		assertEquals("template output - section input (section name)", "SECTION", templateToSection.getSectionName());
		assertEquals("template output - section input (input name)", "SECTION_INPUT", templateToSection.getInputName());
		assertEquals("template output - template", "TEMPLATE", templateToTemplate.getApplicationPath());
		assertEquals("template output - access input", "ACCESS_INPUT", templateToSecurity.getHttpSecurityName());
		assertEquals("template output - resource", "RESOURCE", templateToResource.getResourcePath());
		assertEquals("section output - section input (section name)", "SECTION", sectionToSection.getSectionName());
		assertEquals("section output - section input (input name)", "SECTION_INPUT", sectionToSection.getInputName());
		assertEquals("section output - template", "TEMPLATE", sectionToTemplate.getApplicationPath());
		assertEquals("section output - access input", "ACCESS_INPUT", sectionToSecurity.getHttpSecurityName());
		assertEquals("section output - resource", "RESOURCE", sectionToResource.getResourcePath());
		assertEquals("access output - section input (section name)", "SECTION", securityToSection.getSectionName());
		assertEquals("access output - section input (input name", "SECTION_INPUT", securityToSection.getInputName());
		assertEquals("access output - template", "TEMPLATE", securityToTemplate.getApplicationPath());
		assertEquals("access output - resource", "RESOURCE", securityToResource.getResourcePath());
		assertEquals("exception - section input (section name)", "SECTION", exceptionToSection.getSectionName());
		assertEquals("exception - section input (input name)", "SECTION_INPUT", exceptionToSection.getInputName());
		assertEquals("exception - template", "TEMPLATE", exceptionToTemplate.getApplicationPath());
		assertEquals("exception - resource", "RESOURCE", exceptionToResource.getResourcePath());
		assertEquals("start - section input (section name)", "SECTION", startToSection.getSectionName());
		assertEquals("start - section input (input name)", "SECTION_INPUT", startToSection.getInputName());
	}

}