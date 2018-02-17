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

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.easymock.AbstractMatcher;

import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.repository.ModelRepository;

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
		WoofApplicationPathModel applicationPath = new WoofApplicationPathModel("APPLICATION_PATH", false);
		woof.addWoofApplicationPath(applicationPath);
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null, null, null, null, null, null, false);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel("TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofSecurityModel security = new WoofSecurityModel("SECURITY", null, 1000);
		woof.addWoofSecurity(security);
		WoofSecurityOutputModel securityOutput = new WoofSecurityOutputModel("ACCESS_OUTPUT", null);
		security.addOutput(securityOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE");
		woof.addWoofResource(resource);
		WoofExceptionModel exception = new WoofExceptionModel("EXCEPTION");
		woof.addWoofException(exception);
		WoofStartModel start = new WoofStartModel();
		woof.addWoofStart(start);

		/*
		 * Application Path links
		 */

		// Application Path -> Section Input
		WoofApplicationPathToWoofSectionInputModel applicationPathToSectionInput = new WoofApplicationPathToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		applicationPath.setWoofSectionInput(applicationPathToSectionInput);

		// Application Path -> Template
		WoofApplicationPathToWoofTemplateModel applicationPathToTemplate = new WoofApplicationPathToWoofTemplateModel(
				"TEMPLATE");
		applicationPath.setWoofTemplate(applicationPathToTemplate);

		// Application Path -> Resource
		WoofApplicationPathToWoofResourceModel applicationPathToResource = new WoofApplicationPathToWoofResourceModel(
				"RESOURCE");
		applicationPath.setWoofResource(applicationPathToResource);

		// Application Path -> Security
		WoofApplicationPathToWoofSecurityModel applicationPathToSecurity = new WoofApplicationPathToWoofSecurityModel(
				"SECURITY");
		applicationPath.setWoofSecurity(applicationPathToSecurity);

		// Application Path -> Application Path
		WoofApplicationPathToWoofApplicationPathModel applicationPathToApplicationPath = new WoofApplicationPathToWoofApplicationPathModel(
				"APPLICATION_PATH");
		applicationPath.setWoofApplicationPath(applicationPathToApplicationPath);

		/**
		 * Template Output links
		 */

		// Template Output -> Section Input
		WoofTemplateOutputToWoofSectionInputModel templateOutputToSectionInput = new WoofTemplateOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		templateOutput.setWoofSectionInput(templateOutputToSectionInput);

		// Template Output -> Template
		WoofTemplateOutputToWoofTemplateModel templateOutputToTemplate = new WoofTemplateOutputToWoofTemplateModel(
				"TEMPLATE");
		templateOutput.setWoofTemplate(templateOutputToTemplate);

		// Template Output -> Resource
		WoofTemplateOutputToWoofResourceModel templateOutputToResource = new WoofTemplateOutputToWoofResourceModel(
				"RESOURCE");
		templateOutput.setWoofResource(templateOutputToResource);

		// Template Output -> Security
		WoofTemplateOutputToWoofSecurityModel templateOutputToSecurity = new WoofTemplateOutputToWoofSecurityModel(
				"SECURITY");
		templateOutput.setWoofSecurity(templateOutputToSecurity);

		// Template Output -> Application Path
		WoofTemplateOutputToWoofApplicationPathModel templateOutputToApplicationpath = new WoofTemplateOutputToWoofApplicationPathModel(
				"APPLICATION_PATH");
		templateOutput.setWoofApplicationPath(templateOutputToApplicationpath);

		/*
		 * Section Output links
		 */

		// Section Output -> Section Input
		WoofSectionOutputToWoofSectionInputModel sectionOutputToSectionInput = new WoofSectionOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		sectionOutput.setWoofSectionInput(sectionOutputToSectionInput);

		// Section Output -> Template
		WoofSectionOutputToWoofTemplateModel sectionOutputToTemplate = new WoofSectionOutputToWoofTemplateModel(
				"TEMPLATE");
		sectionOutput.setWoofTemplate(sectionOutputToTemplate);

		// Section Output -> Resource
		WoofSectionOutputToWoofResourceModel sectionOutputToResource = new WoofSectionOutputToWoofResourceModel(
				"RESOURCE");
		sectionOutput.setWoofResource(sectionOutputToResource);

		// Section Output -> Security
		WoofSectionOutputToWoofSecurityModel sectionOutputToSecurity = new WoofSectionOutputToWoofSecurityModel(
				"SECURITY");
		sectionOutput.setWoofSecurity(sectionOutputToSecurity);

		// Section Output -> Application Path
		WoofSectionOutputToWoofApplicationPathModel sectionOutputToApplicationPath = new WoofSectionOutputToWoofApplicationPathModel(
				"APPLICATION_PATH");
		sectionOutput.setWoofApplicationPath(sectionOutputToApplicationPath);

		/*
		 * Security Output links
		 */

		// Security Output -> Section Input
		WoofSecurityOutputToWoofSectionInputModel securityOutputToSectionInput = new WoofSecurityOutputToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		securityOutput.setWoofSectionInput(securityOutputToSectionInput);

		// Security Output -> Template
		WoofSecurityOutputToWoofTemplateModel securityOutputToTemplate = new WoofSecurityOutputToWoofTemplateModel(
				"TEMPLATE");
		securityOutput.setWoofTemplate(securityOutputToTemplate);

		// Security Output -> Resource
		WoofSecurityOutputToWoofResourceModel securityOutputToResource = new WoofSecurityOutputToWoofResourceModel(
				"RESOURCE");
		securityOutput.setWoofResource(securityOutputToResource);

		// Security Output -> Security
		WoofSecurityOutputToWoofSecurityModel securityOutputToSecurity = new WoofSecurityOutputToWoofSecurityModel(
				"SECURITY");
		securityOutput.setWoofSecurity(securityOutputToSecurity);

		// Security Output -> Application Path
		WoofSecurityOutputToWoofApplicationPathModel securityOutputToApplicationPath = new WoofSecurityOutputToWoofApplicationPathModel(
				"APPLICATION_PATH");
		securityOutput.setWoofApplicationPath(securityOutputToApplicationPath);

		/*
		 * Exception links
		 */

		// Exception -> Section Input
		WoofExceptionToWoofSectionInputModel exceptionToSectionInput = new WoofExceptionToWoofSectionInputModel(
				"SECTION", "SECTION_INPUT");
		exception.setWoofSectionInput(exceptionToSectionInput);

		// Exception -> Template
		WoofExceptionToWoofTemplateModel exceptionToTemplate = new WoofExceptionToWoofTemplateModel("TEMPLATE");
		exception.setWoofTemplate(exceptionToTemplate);

		// Exception -> Resource
		WoofExceptionToWoofResourceModel exceptionToResource = new WoofExceptionToWoofResourceModel("RESOURCE");
		exception.setWoofResource(exceptionToResource);

		// Exception -> Security
		WoofExceptionToWoofSecurityModel exceptionToSecurity = new WoofExceptionToWoofSecurityModel("SECURITY");
		exception.setWoofSecurity(exceptionToSecurity);

		// Exception -> Application Path
		WoofExceptionToWoofApplicationPathModel exceptionToApplicationPath = new WoofExceptionToWoofApplicationPathModel(
				"APPLICATION_PATH");
		exception.setWoofApplicationPath(exceptionToApplicationPath);

		/*
		 * Start links
		 */

		// Start -> Section Input
		WoofStartToWoofSectionInputModel startToSectionInput = new WoofStartToWoofSectionInputModel("SECTION",
				"SECTION_INPUT");
		start.setWoofSectionInput(startToSectionInput);

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

		// Application Path links
		AssertLinks<WoofApplicationPathModel> assertApplicationPath = new AssertLinks<>("application path",
				applicationPath);
		assertApplicationPath.assertLink(applicationPathToSectionInput, "section input", sectionInput);
		assertApplicationPath.assertLink(applicationPathToTemplate, "template", template);
		assertApplicationPath.assertLink(applicationPathToResource, "resource", resource);
		assertApplicationPath.assertLink(applicationPathToSecurity, "security", security);
		assertApplicationPath.assertLink(applicationPathToApplicationPath, "application path", applicationPath);

		// Template Output links
		AssertLinks<WoofTemplateOutputModel> assertTemplateOutput = new AssertLinks<>("template output",
				templateOutput);
		assertTemplateOutput.assertLink(templateOutputToSectionInput, "section input", sectionInput);
		assertTemplateOutput.assertLink(templateOutputToTemplate, "template", template);
		assertTemplateOutput.assertLink(templateOutputToResource, "resource", resource);
		assertTemplateOutput.assertLink(templateOutputToSecurity, "security", security);
		assertTemplateOutput.assertLink(templateOutputToApplicationpath, "application path", applicationPath);

		// Section Output links
		AssertLinks<WoofSectionOutputModel> assertSectionOutput = new AssertLinks<>("section output", sectionOutput);
		assertSectionOutput.assertLink(sectionOutputToSectionInput, "section input", sectionInput);
		assertSectionOutput.assertLink(sectionOutputToTemplate, "template", template);
		assertSectionOutput.assertLink(sectionOutputToResource, "resource", resource);
		assertSectionOutput.assertLink(sectionOutputToSecurity, "security", security);
		assertSectionOutput.assertLink(sectionOutputToApplicationPath, "application path", applicationPath);

		// Security Output links
		AssertLinks<WoofSecurityOutputModel> assertSecurityOutput = new AssertLinks<>("security output",
				securityOutput);
		assertSecurityOutput.assertLink(securityOutputToSectionInput, "section input", sectionInput);
		assertSecurityOutput.assertLink(securityOutputToTemplate, "template", template);
		assertSecurityOutput.assertLink(securityOutputToResource, "resource", resource);
		assertSecurityOutput.assertLink(securityOutputToSecurity, "security", security);
		assertSecurityOutput.assertLink(securityOutputToApplicationPath, "application path", applicationPath);

		// Exception links
		AssertLinks<WoofExceptionModel> assertException = new AssertLinks<>("exception", exception);
		assertException.assertLink(exceptionToSectionInput, "section input", sectionInput);
		assertException.assertLink(exceptionToTemplate, "template", template);
		assertException.assertLink(exceptionToResource, "resource", resource);
		assertException.assertLink(exceptionToSecurity, "security", security);
		assertException.assertLink(exceptionToApplicationPath, "application path", applicationPath);

		// Start links
		AssertLinks<WoofStartModel> assertStart = new AssertLinks<>("start", start);
		assertStart.assertLink(startToSectionInput, "section input", sectionInput);
	}

	/**
	 * Convenience class to simplify asserting links.
	 */
	private static class AssertLinks<S extends Model> {

		private String sourceName;
		private S source;

		private AssertLinks(String sourceName, S source) {
			this.sourceName = sourceName;
			this.source = source;
		}

		@SuppressWarnings("unchecked")
		private <L extends ConnectionModel, T extends Model> void assertLink(L link, String targetName, T target) {

			// Obtain the source
			S linkSource = (S) this.getModel(link, this.source.getClass());
			T linkTarget = (T) this.getModel(link, target.getClass());

			// Undertake the assertions
			assertEquals(this.sourceName + " <- " + targetName, this.source, linkSource);
			assertEquals(this.sourceName + " -> " + targetName, target, linkTarget);
		}

		private <L extends ConnectionModel> Object getModel(L link, Class<?> modelType) {
			for (Method method : link.getClass().getMethods()) {
				if (method.getReturnType() == modelType) {
					try {
						return method.invoke(link);
					} catch (Exception ex) {
						throw fail(ex);
					}
				}
			}
			fail("Can not obtain link end model " + modelType.getName() + " from link " + link.getClass().getName());
			return null;
		}
	}

	/**
	 * Ensures on storing a {@link WoofModel} that all {@link ConnectionModel}
	 * instances are readied for storing.
	 */
	public void testStoreWoOF() throws Exception {

		// Create the WoOF (without connections)
		WoofModel woof = new WoofModel();
		WoofApplicationPathModel applicationPath = new WoofApplicationPathModel("APPLICATION_PATH", false);
		woof.addWoofApplicationPath(applicationPath);
		WoofTemplateModel template = new WoofTemplateModel("TEMPLATE", null, null, null, null, null, null, false);
		woof.addWoofTemplate(template);
		WoofTemplateOutputModel templateOutput = new WoofTemplateOutputModel("TEMPLATE_OUTPUT", null);
		template.addOutput(templateOutput);
		WoofSectionModel section = new WoofSectionModel("SECTION", null, null);
		woof.addWoofSection(section);
		WoofSectionInputModel sectionInput = new WoofSectionInputModel("SECTION_INPUT", null);
		section.addInput(sectionInput);
		WoofSectionOutputModel sectionOutput = new WoofSectionOutputModel("SECTION_OUTPUT", null);
		section.addOutput(sectionOutput);
		WoofSecurityModel security = new WoofSecurityModel("SECURITY", null, 1000);
		woof.addWoofSecurity(security);
		WoofSecurityOutputModel securityOutput = new WoofSecurityOutputModel("ACCESS_OUTPUT", null);
		security.addOutput(securityOutput);
		WoofResourceModel resource = new WoofResourceModel("RESOURCE");
		woof.addWoofResource(resource);
		WoofExceptionModel exception = new WoofExceptionModel("EXCEPTION");
		woof.addWoofException(exception);
		WoofStartModel start = new WoofStartModel();
		woof.addWoofStart(start);

		// Application Path links
		WoofApplicationPathToWoofSectionInputModel applicationPathToSectionInput = link(
				new WoofApplicationPathToWoofSectionInputModel(), applicationPath, sectionInput);
		WoofApplicationPathToWoofTemplateModel applicationPathToTemplate = link(
				new WoofApplicationPathToWoofTemplateModel(), applicationPath, template);
		WoofApplicationPathToWoofResourceModel applicationPathToResource = link(
				new WoofApplicationPathToWoofResourceModel(), applicationPath, resource);
		WoofApplicationPathToWoofSecurityModel applicationPathToSecurity = link(
				new WoofApplicationPathToWoofSecurityModel(), applicationPath, security);
		WoofApplicationPathToWoofApplicationPathModel applicationPathToApplicationPath = new WoofApplicationPathToWoofApplicationPathModel();
		applicationPathToApplicationPath.setWoofApplicationPath(applicationPath);
		applicationPathToApplicationPath.setWoofRedirect(applicationPath);
		applicationPathToApplicationPath.connect();

		// Template Output links
		WoofTemplateOutputToWoofSectionInputModel templateOutputToSectionInput = link(
				new WoofTemplateOutputToWoofSectionInputModel(), templateOutput, sectionInput);
		WoofTemplateOutputToWoofTemplateModel templateOutputToTemplate = link(
				new WoofTemplateOutputToWoofTemplateModel(), templateOutput, template);
		WoofTemplateOutputToWoofResourceModel templateOutputToResource = link(
				new WoofTemplateOutputToWoofResourceModel(), templateOutput, resource);
		WoofTemplateOutputToWoofSecurityModel templateOutputToSecurity = link(
				new WoofTemplateOutputToWoofSecurityModel(), templateOutput, security);
		WoofTemplateOutputToWoofApplicationPathModel templateOutputToApplicationPath = link(
				new WoofTemplateOutputToWoofApplicationPathModel(), templateOutput, applicationPath);

		// Section Output links
		WoofSectionOutputToWoofSectionInputModel sectionOutputToSectionInput = link(
				new WoofSectionOutputToWoofSectionInputModel(), sectionOutput, sectionInput);
		WoofSectionOutputToWoofTemplateModel sectionOutputToTemplate = link(new WoofSectionOutputToWoofTemplateModel(),
				sectionOutput, template);
		WoofSectionOutputToWoofResourceModel sectionOutputToResource = link(new WoofSectionOutputToWoofResourceModel(),
				sectionOutput, resource);
		WoofSectionOutputToWoofSecurityModel sectionOutputToSecurity = link(new WoofSectionOutputToWoofSecurityModel(),
				sectionOutput, security);
		WoofSectionOutputToWoofApplicationPathModel sectionOutputToApplicationPath = link(
				new WoofSectionOutputToWoofApplicationPathModel(), sectionOutput, applicationPath);

		// Security Output links
		WoofSecurityOutputToWoofSectionInputModel securityOutputToSectionInput = link(
				new WoofSecurityOutputToWoofSectionInputModel(), securityOutput, sectionInput);
		WoofSecurityOutputToWoofTemplateModel securityOutputToTemplate = link(
				new WoofSecurityOutputToWoofTemplateModel(), securityOutput, template);
		WoofSecurityOutputToWoofResourceModel securityOutputToResource = link(
				new WoofSecurityOutputToWoofResourceModel(), securityOutput, resource);
		WoofSecurityOutputToWoofSecurityModel securityOutputToSecurity = link(
				new WoofSecurityOutputToWoofSecurityModel(), securityOutput, security);
		WoofSecurityOutputToWoofApplicationPathModel securityOutputToApplicationPath = link(
				new WoofSecurityOutputToWoofApplicationPathModel(), securityOutput, applicationPath);

		// Exception links
		WoofExceptionToWoofSectionInputModel exceptionToSectionInput = link(new WoofExceptionToWoofSectionInputModel(),
				exception, sectionInput);
		WoofExceptionToWoofTemplateModel exceptionToTemplate = link(new WoofExceptionToWoofTemplateModel(), exception,
				template);
		WoofExceptionToWoofResourceModel exceptionToResource = link(new WoofExceptionToWoofResourceModel(), exception,
				resource);
		WoofExceptionToWoofSecurityModel exceptionToSecurity = link(new WoofExceptionToWoofSecurityModel(), exception,
				security);
		WoofExceptionToWoofApplicationPathModel exceptionToApplicationPath = link(
				new WoofExceptionToWoofApplicationPathModel(), exception, applicationPath);

		// Start -> Section Input
		WoofStartToWoofSectionInputModel startToSectionInput = link(new WoofStartToWoofSectionInputModel(), start,
				sectionInput);

		// Record storing the WoOf
		this.modelRepository.store(woof, this.configurationItem);

		// Store the WoOF
		this.replayMockObjects();
		this.woofRepository.storeWoof(woof, this.configurationItem);
		this.verifyMockObjects();

		// Assert Application Path links
		assertEquals("application path - section input (section name)", "SECTION",
				applicationPathToSectionInput.getSectionName());
		assertEquals("application path - section input (input name)", "SECTION_INPUT",
				applicationPathToSectionInput.getInputName());
		assertEquals("application path - template", "TEMPLATE", applicationPathToTemplate.getApplicationPath());
		assertEquals("application path - resource", "RESOURCE", applicationPathToResource.getResourcePath());
		assertEquals("application path - security", "SECURITY", applicationPathToSecurity.getHttpSecurityName());
		assertEquals("application path - application path", "APPLICATION_PATH",
				applicationPathToApplicationPath.getApplicationPath());

		// Assert Template Output links
		assertEquals("template output - section input (section name)", "SECTION",
				templateOutputToSectionInput.getSectionName());
		assertEquals("template output - section input (input name)", "SECTION_INPUT",
				templateOutputToSectionInput.getInputName());
		assertEquals("template output - template", "TEMPLATE", templateOutputToTemplate.getApplicationPath());
		assertEquals("template output - resource", "RESOURCE", templateOutputToResource.getResourcePath());
		assertEquals("template output - security", "SECURITY", templateOutputToSecurity.getHttpSecurityName());
		assertEquals("template output - application path", "APPLICATION_PATH",
				templateOutputToApplicationPath.getApplicationPath());

		// Assert Section Output links
		assertEquals("section output - section input (section name)", "SECTION",
				sectionOutputToSectionInput.getSectionName());
		assertEquals("section output - section input (input name)", "SECTION_INPUT",
				sectionOutputToSectionInput.getInputName());
		assertEquals("section output - template", "TEMPLATE", sectionOutputToTemplate.getApplicationPath());
		assertEquals("section output - resource", "RESOURCE", sectionOutputToResource.getResourcePath());
		assertEquals("section output - security", "SECURITY", sectionOutputToSecurity.getHttpSecurityName());
		assertEquals("section output - application path", "APPLICATION_PATH",
				sectionOutputToApplicationPath.getApplicationPath());

		// Assert Security Output links
		assertEquals("security output - section input (section name)", "SECTION",
				securityOutputToSectionInput.getSectionName());
		assertEquals("access output - section input (input name", "SECTION_INPUT",
				securityOutputToSectionInput.getInputName());
		assertEquals("security output - template", "TEMPLATE", securityOutputToTemplate.getApplicationPath());
		assertEquals("security output - resource", "RESOURCE", securityOutputToResource.getResourcePath());
		assertEquals("security output - security", "SECURITY", securityOutputToSecurity.getHttpSecurityName());
		assertEquals("security output - application path", "APPLICATION_PATH",
				securityOutputToApplicationPath.getApplicationPath());

		// Assert Exception links
		assertEquals("exception - section input (section name)", "SECTION", exceptionToSectionInput.getSectionName());
		assertEquals("exception - section input (input name)", "SECTION_INPUT", exceptionToSectionInput.getInputName());
		assertEquals("exception - template", "TEMPLATE", exceptionToTemplate.getApplicationPath());
		assertEquals("exception - resource", "RESOURCE", exceptionToResource.getResourcePath());
		assertEquals("exception - security", "SECURITY", exceptionToSecurity.getHttpSecurityName());
		assertEquals("exception - application path", "APPLICATION_PATH",
				exceptionToApplicationPath.getApplicationPath());

		// Assert Start links
		assertEquals("start - section input (section name)", "SECTION", startToSectionInput.getSectionName());
		assertEquals("start - section input (input name)", "SECTION_INPUT", startToSectionInput.getInputName());
	}

	/**
	 * Convenience method to create a link.
	 */
	private static <L extends ConnectionModel> L link(L link, Model source, Model target) {
		final Consumer<Model> loadEndModel = (model) -> {
			for (Method method : link.getClass().getMethods()) {
				if (method.getParameterTypes().length == 1) {
					if (method.getParameterTypes()[0] == model.getClass()) {
						try {
							method.invoke(link, model);
						} catch (Exception ex) {
							throw fail(ex);
						}
						return; // loaded
					}
				}
			}
			fail("Unable to set model " + model.getClass().getName() + " on connection " + link.getClass().getName());
		};
		loadEndModel.accept(source);
		loadEndModel.accept(target);
		link.connect();
		return link;
	}

}