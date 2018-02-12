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
package net.officefloor.plugin.woof;

import java.sql.SQLException;
import java.util.logging.LogRecord;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.classloader.ClassLoaderConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.test.LoggerAssertion;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionException;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceService;
import net.officefloor.plugin.woof.template.impl.AbstractWoofTemplateExtensionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.type.HttpSecuritySourceSpecificationRunnableTest.MockHttpSecuritySource;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplater;

/**
 * Tests the {@link WoofLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

	/**
	 * {@link WoofLoader} to test.
	 */
	private final WoofLoader loader = new WoofLoaderImpl(new WoofRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * Mock {@link OfficeArchitect}.
	 */
	private final OfficeArchitect office = this.createMock(OfficeArchitect.class);

	/**
	 * Mock {@link WebArchitect}.
	 */
	private final WebArchitect web = this.createMock(WebArchitect.class);

	/**
	 * Mock {@link HttpSecurityArchitect}.
	 */
	private final HttpSecurityArchitect security = this.createMock(HttpSecurityArchitect.class);

	/**
	 * Mock {@link WebTemplater}.
	 */
	private final WebTemplater templater = this.createMock(WebTemplater.class);

	/**
	 * Mock {@link HttpResourceArchitect}.
	 */
	private final HttpResourceArchitect resources = this.createMock(HttpResourceArchitect.class);

	/**
	 * Mock {@link SourceContext}.
	 */
	private final OfficeExtensionContext extensionContext = this.createMock(OfficeExtensionContext.class);

	/**
	 * {@link LoggerAssertion}.
	 */
	private LoggerAssertion loggerAssertion;

	@Override
	protected void setUp() throws Exception {
		this.loggerAssertion = LoggerAssertion.setupLoggerAssertion(WoofLoaderImpl.class.getName());
	}

	@Override
	protected void tearDown() throws Exception {

		// Obtain the log records
		LogRecord[] records = this.loggerAssertion.disconnectFromLogger();

		// Validate warned failed to load unknown service
		assertEquals("Should warn of service failure", 1, records.length);
		LogRecord record = records[0];
		assertEquals("Incorrect cause message",
				WoofTemplateExtensionSourceService.class.getName()
						+ ": Provider woof.template.extension.not.available.Service not found",
				record.getThrown().getMessage());
	}

	/**
	 * Ensure can load configuration to {@link WebArchitect}.
	 */
	public void testLoading() throws Exception {

		final WebTemplate templateA = this.createMock(WebTemplate.class);
		final WebTemplate templateB = this.createMock(WebTemplate.class);
		final OfficeSection sectionA = this.createMock(OfficeSection.class);
		final OfficeSectionInput sectionAInput = this.createMock(OfficeSectionInput.class);
		final HttpUrlContinuation link = this.createMock(HttpUrlContinuation.class);
		final OfficeSection sectionB = this.createMock(OfficeSection.class);
		final HttpSecurityBuilder security = this.createMock(HttpSecurityBuilder.class);
		final OfficeGovernance governanceA = this.createMock(OfficeGovernance.class);
		final OfficeGovernance governanceB = this.createMock(OfficeGovernance.class);

		// Record initiating from source context
		this.recordInitateFromExtensionContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("example", "another");

		// Record loading templates
		this.recordReturn(this.templater, this.templater.addTemplate("example", "WOOF/TemplateA.ofp"), templateA);
		templateA.setLogicClass(Template.class);
		templateA.setContentType("text/html; charset=UTF-8");
		templateA.setSecure(true);
		templateA.setLinkSecure("LINK_1", true);
		templateA.setLinkSecure("LINK_2", false);
		templateA.addRenderMethod(HttpMethod.getHttpMethod("REDIRECT_POST"));
		templateA.addRenderMethod(HttpMethod.getHttpMethod("REDIRECT_PUT"));
		this.recordImplicitTemplateExtensions(templateA, "example");
		this.recordReturn(this.templater, this.templater.addTemplate("another", "WOOF/TemplateB.ofp"), templateB);
		templateB.setSecure(false);
		this.recordImplicitTemplateExtensions(templateB, "another");

		// Record loading sections
		this.recordReturn(this.office,
				this.office.addOfficeSection("SECTION_A", ClassSectionSource.class.getName(), Section.class.getName()),
				sectionA);
		sectionA.addProperty("name.one", "value.one");
		sectionA.addProperty("name.two", "value.two");
		this.recordReturn(sectionA, sectionA.getOfficeSectionInput("INPUT_B"), sectionAInput);
		this.recordReturn(this.web, this.web.link(false, "example", sectionAInput), link);
		this.recordReturn(this.office, this.office.addOfficeSection("SECTION_B", "CLASS", Section.class.getName()),
				sectionB);

		// Record loading access
		this.recordReturn(this.security, this.security.addHttpSecurity("SECURITY", MockHttpSecuritySource.class),
				security);
		security.setTimeout(2000);
		security.addProperty("name.first", "value.first");
		security.addProperty("name.second", "value.second");

		// Record linking template outputs
		this.office.link(this.recordGetSectionOutput(templateA, "OUTPUT_1"),
				this.recordGetSectionInput(sectionA, "INPUT_A"));
		templateB.link(this.recordGetSectionOutput(templateA, "OUTPUT_2"), null);
		this.office.link(this.recordGetSectionOutput(templateA, "OUTPUT_3"), this.recordGetSectionInput(security));
		this.resources.link(this.recordGetSectionOutput(templateA, "OUTPUT_4"), "Example.html");

		// Record linking section outputs
		this.office.link(this.recordGetSectionOutput(sectionA, "OUTPUT_A"),
				this.recordGetSectionInput(sectionB, "INPUT_1"));
		templateA.link(this.recordGetSectionOutput(sectionA, "OUTPUT_B"), null);
		this.office.link(this.recordGetSectionOutput(sectionA, "OUTPUT_C"), this.recordGetSectionInput(security));
		this.resources.link(this.recordGetSectionOutput(sectionA, "OUTPUT_D"), "Example.html");

		// Record link access outputs
		this.office.link(this.recordGetSectionOutput(security, "OUTPUT_ONE"),
				this.recordGetSectionInput(sectionB, "INPUT_1"));
		templateA.link(this.recordGetSectionOutput(security, "OUTPUT_TWO"), null);
		this.resources.link(this.recordGetSectionOutput(security, "OUTPUT_THREE"), "Example.html");

		// Record linking escalations
		OfficeEscalation escalationException = this.createMock(OfficeEscalation.class);
		this.recordReturn(this.office, this.office.addOfficeEscalation(Exception.class.getName()), escalationException);
		this.office.link(escalationException, this.recordGetSectionInput(sectionA, "INPUT_A"));
		OfficeEscalation escalationRuntime = this.createMock(OfficeEscalation.class);
		this.recordReturn(this.office, this.office.addOfficeEscalation(RuntimeException.class.getName()),
				escalationRuntime);
		templateA.link(escalationRuntime, null);
		OfficeEscalation escalationSql = this.createMock(OfficeEscalation.class);
		this.recordReturn(this.office, this.office.addOfficeEscalation(SQLException.class.getName()), escalationSql);
		this.resources.link(escalationSql, "Example.html");

		// Record linking starts
		OfficeStart startOne = this.createMock(OfficeStart.class);
		this.recordReturn(this.office, this.office.addOfficeStart("START_1"), startOne);
		this.office.link(startOne, this.recordGetSectionInput(sectionA, "INPUT_A"));
		OfficeStart startTwo = this.createMock(OfficeStart.class);
		this.recordReturn(this.office, this.office.addOfficeStart("START_2"), startTwo);
		this.office.link(startTwo, this.recordGetSectionInput(sectionB, "INPUT_1"));

		// Record loading governances
		this.recordReturn(this.office,
				this.office.addOfficeGovernance("GOVERNANCE_A", ClassGovernanceSource.class.getName()), governanceA);
		governanceA.addProperty("name.a", "value.a");
		governanceA.addProperty("name.b", "value.b");
		templateA.addGovernance(governanceA);
		sectionA.addGovernance(governanceA);
		this.recordReturn(this.office, this.office.addOfficeGovernance("GOVERNANCE_B", "CLASS"), governanceB);

		// Test
		this.replayMockObjects();
		this.loadConfiguration("application.woof");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load inheritance of {@link WoofTemplateModel} configuration.
	 */
	public void testInheritance() throws Exception {

		final WebTemplate parentTemplate = this.createMock(WebTemplate.class);
		final WebTemplate childTemplate = this.createMock(WebTemplate.class);
		final WebTemplate grandChildTemplate = this.createMock(WebTemplate.class);
		final WebTemplate templateOne = this.createMock(WebTemplate.class);
		final WebTemplate templateTwo = this.createMock(WebTemplate.class);
		final WebTemplate templateThree = this.createMock(WebTemplate.class);
		final OfficeSection section = this.createMock(OfficeSection.class);
		final HttpSecurityBuilder security = this.createMock(HttpSecurityBuilder.class);

		// Record initiating from source context
		this.recordInitateFromExtensionContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("parent", "child", "grandchild", "one", "two", "three");

		// Record loading parent template
		this.recordReturn(this.templater, this.templater.addTemplate("parent", "WOOF/Parent.ofp"), parentTemplate);
		parentTemplate.setSecure(false);
		parentTemplate.setLinkSecure("LINK_SECURE", true);
		parentTemplate.setLinkSecure("LINK_NON_SECURE", false);
		this.recordImplicitTemplateExtensions(parentTemplate, "parent");

		// Record loading child template (inheriting configuration)
		this.recordReturn(this.templater, this.templater.addTemplate("child", "WOOF/Child.ofp"), childTemplate);
		childTemplate.setSecure(false);
		childTemplate.setLinkSecure("LINK_OTHER", true);
		this.recordImplicitTemplateExtensions(childTemplate, "child");

		// Record loading grand child template (overriding configuration)
		this.recordReturn(this.templater, this.templater.addTemplate("grandchild", "WOOF/GrandChild.ofp"),
				grandChildTemplate);
		grandChildTemplate.setSecure(false);
		grandChildTemplate.setLinkSecure("LINK_SECURE", false);
		grandChildTemplate.setLinkSecure("LINK_NON_SECURE", true);
		this.recordImplicitTemplateExtensions(grandChildTemplate, "grandchild");

		// Record loading remaining templates
		this.recordReturn(this.templater, this.templater.addTemplate("one", "WOOF/TemplateOne.ofp"), templateOne);
		templateOne.setSecure(false);
		this.recordImplicitTemplateExtensions(templateOne, "one");
		this.recordReturn(this.templater, this.templater.addTemplate("two", "WOOF/TemplateTwo.ofp"), templateTwo);
		templateTwo.setSecure(false);
		this.recordImplicitTemplateExtensions(templateTwo, "two");
		this.recordReturn(this.templater, this.templater.addTemplate("three", "WOOF/TemplateThree.ofp"), templateThree);
		templateThree.setSecure(false);
		this.recordImplicitTemplateExtensions(templateThree, "three");

		// Record loading sections
		this.recordReturn(this.office,
				this.office.addOfficeSection("SECTION", "CLASS", "net.officefloor.ExampleSection"), section);

		// Record loading access
		this.recordReturn(this.security, this.security.addHttpSecurity("SECURITY", MockHttpSecuritySource.class),
				security);
		security.setTimeout(2000);

		// Record linking parent template outputs
		this.office.link(this.recordGetSectionOutput(parentTemplate, "OUTPUT_SECTION"),
				this.recordGetSectionInput(section, "INPUT_1"));
		templateOne.link(this.recordGetSectionOutput(parentTemplate, "OUTPUT_TEMPLATE"), null);
		this.office.link(this.recordGetSectionOutput(parentTemplate, "OUTPUT_ACCESS"),
				this.recordGetSectionInput(security));
		this.resources.link(this.recordGetSectionOutput(parentTemplate, "OUTPUT_RESOURCE"), "ResourceOne.html");

		// Child template inherits link configuration
		childTemplate.setSuperTemplate(parentTemplate);

		// Record linking grand child template outputs (overriding)
		grandChildTemplate.setSuperTemplate(childTemplate);
		this.office.link(this.recordGetSectionOutput(grandChildTemplate, "OUTPUT_SECTION"),
				this.recordGetSectionInput(section, "INPUT_2"));
		templateTwo.link(this.recordGetSectionOutput(grandChildTemplate, "OUTPUT_TEMPLATE"), null);
		this.office.link(this.recordGetSectionOutput(grandChildTemplate, "OUTPUT_ACCESS"),
				this.recordGetSectionInput(security));
		this.resources.link(this.recordGetSectionOutput(grandChildTemplate, "OUTPUT_RESOURCE"), "ResourceTwo.html");
		templateThree.link(this.recordGetSectionOutput(grandChildTemplate, "OUTPUT_ANOTHER"), null);

		// Test
		this.replayMockObjects();
		this.loadConfiguration("inheritance.woof");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load explicit {@link WoofTemplateExtensionSource}.
	 */
	public void testExplicitTemplateExtension() throws Exception {

		final WebTemplate template = this.createMock(WebTemplate.class);

		// Record initiating from source context
		this.recordInitateFromExtensionContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("example");

		// Record loading template
		this.recordReturn(this.templater, this.templater.addTemplate("example", "WOOF/Template.html"), template);
		template.setLogicClass(Template.class);
		template.setSecure(false);

		// Record extending with explicit template extension
		this.recordTemplateExtension(MockExplicitWoofTemplateExtensionSource.class);

		// Record implicit template extensions
		this.recordImplicitTemplateExtensions(template, "example");

		// Test
		this.replayMockObjects();
		this.loadConfiguration("explicit-template-extension.woof");
		this.verifyMockObjects();
	}

	/**
	 * Mock explicit {@link WoofTemplateExtensionSource}.
	 */
	public static class MockExplicitWoofTemplateExtensionSource extends AbstractWoofTemplateExtensionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {
			// Ensure correct template
			assertEquals("Obtain location to ensure extending", "URI", context.getApplicationPath());
		}
	}

	/**
	 * Ensure issue if unknown template extension.
	 */
	public void testUnknownTemplateExtension() throws Exception {

		final WebTemplate template = this.createMock(WebTemplate.class);

		// Record initiating from source context
		this.recordInitateFromExtensionContext();
		MockImplicitWoofTemplateExtensionSourceService.reset();

		// Record loading template
		this.recordReturn(this.templater, this.templater.addTemplate("example", "WOOF/Template.html"), template);
		template.setLogicClass(Template.class);
		template.setSecure(false);

		// Should not load further as unknown template extension
		this.recordReturn(this.extensionContext, this.extensionContext.isLoadingType(), true);
		final UnknownClassError unknownClassError = new UnknownClassError("UNKNOWN");
		this.extensionContext.loadClass("UNKNOWN");
		this.control(this.extensionContext).setThrowable(unknownClassError);

		// Test
		this.replayMockObjects();
		try {
			this.loadConfiguration("unknown-template-extension.woof");
			fail("Should not load successfully");
		} catch (WoofTemplateExtensionException ex) {
			assertEquals("Incorrect exception",
					"Failed loading Template Extension UNKNOWN. " + unknownClassError.getMessage(), ex.getMessage());
			assertTrue("Incorrect cause", ex.getCause() == unknownClassError);
		}
		this.verifyMockObjects();
	}

	/**
	 * Undertakes loading the configuration.
	 * 
	 * @param configurationFileLocation
	 *            Location of the {@link ConfigurationItem}.
	 */
	private void loadConfiguration(String configurationFileLocation) throws Exception {
		this.loader.loadWoofConfiguration(new WoofLoaderContext() {

			@Override
			public ConfigurationItem getConfiguration() {
				return WoofLoaderTest.this.getConfiguration(configurationFileLocation);
			}

			@Override
			public WebArchitect getWebArchitect() {
				return WoofLoaderTest.this.web;
			}

			@Override
			public OfficeArchitect getOfficeArchitect() {
				return WoofLoaderTest.this.office;
			}

			@Override
			public OfficeExtensionContext getOfficeExtensionContext() {
				return WoofLoaderTest.this.extensionContext;
			}
		});
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem getConfiguration(String fileName) {
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(this.compiler.getClassLoader(), null);
		ConfigurationItem configuration = context.getConfigurationItem(location, null);
		assertNotNull("Can not find configuration '" + fileName + "'", configuration);
		return configuration;
	}

	/**
	 * Mock template class.
	 */
	public static class Template {
		public void template() {
		}
	}

	/**
	 * Mock section class.
	 */
	public static class Section {
		public void taskOne() {
		}
	}

	/**
	 * Records obtain the {@link OfficeSectionOutput}.
	 * 
	 * @param section
	 *            {@link OfficeSection}.
	 * @param outputName
	 *            Name of {@link OfficeSectionOutput}.
	 * @return {@link OfficeSectionOutput}.
	 */
	private OfficeSectionOutput recordGetSectionOutput(OfficeSection section, String outputName) {
		OfficeSectionOutput sectionOutput = this.createMock(OfficeSectionOutput.class);
		this.recordReturn(section, section.getOfficeSectionOutput(outputName), sectionOutput);
		return sectionOutput;
	}

	/**
	 * Records obtain the {@link OfficeSectionInput}.
	 * 
	 * @param section
	 *            {@link OfficeSection}.
	 * @param inputName
	 *            Name of {@link OfficeSectionInput}.
	 * @return {@link OfficeSectionInput}.
	 */
	private OfficeSectionInput recordGetSectionInput(OfficeSection section, String inputName) {
		OfficeSectionInput sectionInput = this.createMock(OfficeSectionInput.class);
		this.recordReturn(section, section.getOfficeSectionInput(inputName), sectionInput);
		return sectionInput;
	}

	/**
	 * Records obtain {@link OfficeSectionOutput}.
	 * 
	 * @param template
	 *            {@link WebTemplate}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 * @return {@link OfficeSectionOutput}.
	 */
	private OfficeSectionOutput recordGetSectionOutput(WebTemplate template, String outputName) {
		OfficeSectionOutput output = this.createMock(OfficeSectionOutput.class);
		this.recordReturn(template, template.getOutput(outputName), output);
		return output;
	}

	/**
	 * Records obtain {@link OfficeSectionOutput}.
	 * 
	 * @param security
	 *            {@link HttpSecurityBuilder}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 * @return {@link OfficeSectionOutput}.
	 */
	private OfficeSectionOutput recordGetSectionOutput(HttpSecurityBuilder security, String outputName) {
		OfficeSectionOutput output = this.createMock(OfficeSectionOutput.class);
		this.recordReturn(security, security.getOutput(outputName), output);
		return output;
	}

	/**
	 * Records obtaining {@link OfficeSectionInput}.
	 * 
	 * @param security
	 *            {@link HttpSecurityBuilder}.
	 * @return {@link OfficeSectionInput}.
	 */
	private OfficeSectionInput recordGetSectionInput(HttpSecurityBuilder security) {
		OfficeSectionInput input = this.createMock(OfficeSectionInput.class);
		this.recordReturn(security, security.getAuthenticateInput(), input);
		return input;
	}

	/**
	 * Records initiating from the {@link OfficeExtensionContext}.
	 */
	private void recordInitateFromExtensionContext() {
		this.recordReturn(this.extensionContext, this.extensionContext.getClassLoader(),
				Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Record a template extension.
	 * 
	 * @param extensionSourceClass
	 *            {@link Class} of the {@link WoofTemplateExtensionSource}.
	 */
	private void recordTemplateExtension(Class<? extends WoofTemplateExtensionSource> extensionSourceClass) {

		// Load the source context
		this.recordReturn(this.extensionContext, this.extensionContext.isLoadingType(), true);

		// Record loading the template extension
		this.recordReturn(this.extensionContext, this.extensionContext.loadClass(extensionSourceClass.getName()),
				extensionSourceClass);
	}

	/**
	 * Records implicit {@link WoofTemplateExtensionSource} on the
	 * {@link WebTemplate}.
	 * 
	 * @param template
	 *            {@link WebTemplate}.
	 * @param templateUri
	 *            URI.
	 */
	private void recordImplicitTemplateExtensions(WebTemplate template, String templateUri) {

		// Record the template extension
		this.recordTemplateExtension(MockImplicitWoofTemplateExtensionSourceService.class);
	}

}