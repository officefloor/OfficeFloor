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

import net.officefloor.autowire.AutoWireGovernance;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.test.LoggerAssertion;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.classloader.ClassLoaderConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.web.http.application.HttpSecurityAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.HttpUriLink;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.security.HttpSecuritySectionSource;
import net.officefloor.plugin.web.http.security.scheme.MockHttpSecuritySource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionException;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSource;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.plugin.woof.template.WoofTemplateExtensionSourceService;
import net.officefloor.plugin.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * Tests the {@link WoofLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler = OfficeFloorCompiler
			.newOfficeFloorCompiler(null);

	/**
	 * {@link WoofLoader} to test.
	 */
	private final WoofLoader loader = new WoofLoaderImpl(
			new WoofRepositoryImpl(new ModelRepositoryImpl()));

	/**
	 * Mock {@link WebAutoWireApplication}.
	 */
	private final WebAutoWireApplication app = this
			.createMock(WebAutoWireApplication.class);

	/**
	 * Mock {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this
			.createMock(SourceContext.class);

	/**
	 * {@link LoggerAssertion}.
	 */
	private LoggerAssertion loggerAssertion;

	@Override
	protected void setUp() throws Exception {
		this.loggerAssertion = LoggerAssertion
				.setupLoggerAssertion(WoofLoaderImpl.class.getName());
	}

	@Override
	protected void tearDown() throws Exception {

		// Obtain the log records
		LogRecord[] records = this.loggerAssertion.disconnectFromLogger();

		// Validate warned failed to load unknown service
		assertEquals("Should warn of service failure", 1, records.length);
		LogRecord record = records[0];
		assertEquals(
				"Incorrect cause message",
				WoofTemplateExtensionSourceService.class.getName()
						+ ": Provider woof.template.extension.not.available.Service not found",
				record.getThrown().getMessage());
	}

	/**
	 * Ensure can load configuration to {@link WebAutoWireApplication}.
	 */
	public void testLoading() throws Exception {

		final HttpTemplateAutoWireSection templateA = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSection templateB = this
				.createMock(HttpTemplateAutoWireSection.class);
		final AutoWireSection sectionA = this.createMock(AutoWireSection.class);
		final HttpUriLink link = this.createMock(HttpUriLink.class);
		final AutoWireSection sectionB = this.createMock(AutoWireSection.class);
		final HttpSecurityAutoWireSection security = this
				.createMock(HttpSecurityAutoWireSection.class);
		final AutoWireGovernance governanceA = this
				.createMock(AutoWireGovernance.class);
		final AutoWireGovernance governanceB = this
				.createMock(AutoWireGovernance.class);

		// Record initiating from source context
		this.recordInitateFromSourceContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("example",
				"another");

		// Record loading templates
		this.recordReturn(this.app, this.app.addHttpTemplate("example",
				"WOOF/TemplateA.ofp", Template.class), templateA);
		templateA.setTemplateContentType("text/html; charset=UTF-8");
		templateA.setTemplateSecure(true);
		templateA.setLinkSecure("LINK_1", true);
		templateA.setLinkSecure("LINK_2", false);
		templateA.addRenderRedirectHttpMethod("REDIRECT_POST");
		templateA.addRenderRedirectHttpMethod("REDIRECT_PUT");
		this.recordImplicitTemplateExtensions(templateA, "example");
		this.recordReturn(
				this.app,
				this.app.addHttpTemplate("another", "WOOF/TemplateB.ofp", null),
				templateB);
		templateB.setTemplateSecure(false);
		this.recordImplicitTemplateExtensions(templateB, "another");

		// Record loading sections
		this.recordReturn(
				this.app,
				this.app.addSection("SECTION_A",
						ClassSectionSource.class.getName(),
						Section.class.getName()), sectionA);
		sectionA.addProperty("name.one", "value.one");
		sectionA.addProperty("name.two", "value.two");
		this.recordReturn(this.app,
				this.app.linkUri("example", sectionA, "INPUT_B"), link);
		this.recordReturn(
				this.app,
				this.app.addSection("SECTION_B", "CLASS",
						Section.class.getName()), sectionB);

		// Record loading access
		this.recordReturn(this.app,
				this.app.setHttpSecurity(MockHttpSecuritySource.class),
				security);
		security.setSecurityTimeout(2000);
		security.addProperty("name.first", "value.first");
		security.addProperty("name.second", "value.second");

		// Record linking template outputs
		this.app.link(templateA, "OUTPUT_1", sectionA, "INPUT_A");
		this.app.linkToHttpTemplate(templateA, "OUTPUT_2", templateB);
		this.app.link(templateA, "OUTPUT_3", security,
				HttpSecuritySectionSource.INPUT_AUTHENTICATE);
		this.app.linkToResource(templateA, "OUTPUT_4", "Example.html");

		// Record linking section outputs
		this.app.link(sectionA, "OUTPUT_A", sectionB, "INPUT_1");
		this.app.linkToHttpTemplate(sectionA, "OUTPUT_B", templateA);
		this.app.link(sectionA, "OUTPUT_C", security,
				HttpSecuritySectionSource.INPUT_AUTHENTICATE);
		this.app.linkToResource(sectionA, "OUTPUT_D", "Example.html");

		// Record link access outputs
		this.app.link(security, "OUTPUT_ONE", sectionB, "INPUT_1");
		this.app.linkToHttpTemplate(security, "OUTPUT_TWO", templateA);
		this.app.linkToResource(security, "OUTPUT_THREE", "Example.html");

		// Record linking escalations
		this.app.linkEscalation(Exception.class, sectionA, "INPUT_A");
		this.app.linkEscalation(RuntimeException.class, templateA);
		this.app.linkEscalation(SQLException.class, "Example.html");

		// Record linking starts
		this.app.addStartupFlow(sectionA, "INPUT_A");
		this.app.addStartupFlow(sectionB, "INPUT_1");

		// Record loading governances
		this.recordReturn(this.app, this.app.addGovernance("GOVERNANCE_A",
				ClassGovernanceSource.class.getName()), governanceA);
		governanceA.addProperty("name.a", "value.a");
		governanceA.addProperty("name.b", "value.b");
		governanceA.governSection(templateA);
		governanceA.governSection(sectionA);
		this.recordReturn(this.app,
				this.app.addGovernance("GOVERNANCE_B", "CLASS"), governanceB);

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(
				this.getConfiguration("application.woof"), this.app,
				this.sourceContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load inheritance of {@link WoofTemplateModel} configuration.
	 */
	public void testInheritance() throws Exception {

		final HttpTemplateAutoWireSection parentTemplate = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSection childTemplate = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSection grandChildTemplate = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSection templateOne = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSection templateTwo = this
				.createMock(HttpTemplateAutoWireSection.class);
		final HttpTemplateAutoWireSection templateThree = this
				.createMock(HttpTemplateAutoWireSection.class);
		final AutoWireSection section = this.createMock(AutoWireSection.class);
		final HttpSecurityAutoWireSection security = this
				.createMock(HttpSecurityAutoWireSection.class);

		// Record initiating from source context
		this.recordInitateFromSourceContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("parent", "child",
				"grandchild", "one", "two", "three");

		// Record loading parent template
		this.recordReturn(this.app,
				this.app.addHttpTemplate("parent", "WOOF/Parent.ofp", null),
				parentTemplate);
		parentTemplate.setTemplateSecure(false);
		parentTemplate.setLinkSecure("LINK_SECURE", true);
		parentTemplate.setLinkSecure("LINK_NON_SECURE", false);
		this.recordImplicitTemplateExtensions(parentTemplate, "parent");

		// Record loading child template (inheriting configuration)
		this.recordReturn(this.app,
				this.app.addHttpTemplate("child", "WOOF/Child.ofp", null),
				childTemplate);
		childTemplate.setTemplateSecure(false);
		childTemplate.setLinkSecure("LINK_OTHER", true);
		this.recordImplicitTemplateExtensions(childTemplate, "child");

		// Record loading grand child template (overriding configuration)
		this.recordReturn(this.app, this.app.addHttpTemplate("grandchild",
				"WOOF/GrandChild.ofp", null), grandChildTemplate);
		grandChildTemplate.setTemplateSecure(false);
		grandChildTemplate.setLinkSecure("LINK_SECURE", false);
		grandChildTemplate.setLinkSecure("LINK_NON_SECURE", true);
		this.recordImplicitTemplateExtensions(grandChildTemplate, "grandchild");

		// Record loading remaining templates
		this.recordReturn(this.app,
				this.app.addHttpTemplate("one", "WOOF/TemplateOne.ofp", null),
				templateOne);
		templateOne.setTemplateSecure(false);
		this.recordImplicitTemplateExtensions(templateOne, "one");
		this.recordReturn(this.app,
				this.app.addHttpTemplate("two", "WOOF/TemplateTwo.ofp", null),
				templateTwo);
		templateTwo.setTemplateSecure(false);
		this.recordImplicitTemplateExtensions(templateTwo, "two");
		this.recordReturn(this.app, this.app.addHttpTemplate("three",
				"WOOF/TemplateThree.ofp", null), templateThree);
		templateThree.setTemplateSecure(false);
		this.recordImplicitTemplateExtensions(templateThree, "three");

		// Record loading sections
		this.recordReturn(this.app, this.app.addSection("SECTION", "CLASS",
				"net.officefloor.ExampleSection"), section);

		// Record loading access
		this.recordReturn(this.app,
				this.app.setHttpSecurity(MockHttpSecuritySource.class),
				security);
		security.setSecurityTimeout(2000);

		// Record linking parent template outputs
		this.app.link(parentTemplate, "OUTPUT_SECTION", section, "INPUT_1");
		this.app.linkToHttpTemplate(parentTemplate, "OUTPUT_TEMPLATE",
				templateOne);
		this.app.link(parentTemplate, "OUTPUT_ACCESS", security,
				"AUTHENTICATE_1");
		this.app.linkToResource(parentTemplate, "OUTPUT_RESOURCE",
				"ResourceOne.html");

		// Child template inherits link configuration
		childTemplate.setSuperSection(parentTemplate);

		// Record linking grand child template outputs (overriding)
		grandChildTemplate.setSuperSection(childTemplate);
		this.app.link(grandChildTemplate, "OUTPUT_SECTION", section, "INPUT_2");
		this.app.linkToHttpTemplate(grandChildTemplate, "OUTPUT_TEMPLATE",
				templateTwo);
		this.app.link(grandChildTemplate, "OUTPUT_ACCESS", security,
				"AUTHENTICATE_2");
		this.app.linkToResource(grandChildTemplate, "OUTPUT_RESOURCE",
				"ResourceTwo.html");
		this.app.linkToHttpTemplate(grandChildTemplate, "OUTPUT_ANOTHER",
				templateThree);

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(
				this.getConfiguration("inheritance.woof"), this.app,
				this.sourceContext);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load explicit {@link WoofTemplateExtensionSource}.
	 */
	public void testExplicitTemplateExtension() throws Exception {

		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);

		// Record initiating from source context
		this.recordInitateFromSourceContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("example");

		// Record loading template
		this.recordReturn(this.app, this.app.addHttpTemplate("example",
				"WOOF/Template.html", Template.class), template);
		template.setTemplateSecure(false);

		// Record extending with explicit template extension
		this.recordTemplateExtension(MockExplicitWoofTemplateExtensionSource.class);
		this.recordReturn(template, template.getTemplateUri(), "URI");

		// Record implicit template extensions
		this.recordImplicitTemplateExtensions(template, "example");

		// Test
		this.replayMockObjects();
		this.loader.loadWoofConfiguration(
				this.getConfiguration("explicit-template-extension.woof"),
				this.app, this.sourceContext);
		this.verifyMockObjects();
	}

	/**
	 * Mock explicit {@link WoofTemplateExtensionSource}.
	 */
	public static class MockExplicitWoofTemplateExtensionSource extends
			AbstractWoofTemplateExtensionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context)
				throws Exception {
			// Ensure correct template
			assertEquals("Obtain location to ensure extending", "URI", context
					.getTemplate().getTemplateUri());
		}
	}

	/**
	 * Ensure issue if unknown template extension.
	 */
	public void testUnknownTemplateExtension() throws Exception {

		final HttpTemplateAutoWireSection template = this
				.createMock(HttpTemplateAutoWireSection.class);

		// Record initiating from source context
		this.recordInitateFromSourceContext();
		MockImplicitWoofTemplateExtensionSourceService.reset();

		// Record loading template
		this.recordReturn(this.app, this.app.addHttpTemplate("example",
				"WOOF/Template.html", Template.class), template);
		template.setTemplateSecure(false);

		// Should not load further as unknown template extension
		this.recordReturn(this.sourceContext,
				this.sourceContext.isLoadingType(), true);
		final UnknownClassError unknownClassError = new UnknownClassError(
				"Unknown class", "UNKNOWN");
		this.sourceContext.loadClass("UNKNOWN");
		this.control(this.sourceContext).setThrowable(unknownClassError);

		// Test
		this.replayMockObjects();
		try {
			this.loader.loadWoofConfiguration(
					this.getConfiguration("unknown-template-extension.woof"),
					this.app, this.sourceContext);
			fail("Should not load successfully");
		} catch (WoofTemplateExtensionException ex) {
			assertEquals("Incorrect exception",
					"Failed loading Template Extension UNKNOWN. "
							+ unknownClassError.getMessage(), ex.getMessage());
			assertTrue("Incorrect cause", ex.getCause() == unknownClassError);
		}
		this.verifyMockObjects();
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName
	 *            File name for {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem getConfiguration(String fileName)
			throws Exception {
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(
				this.compiler.getClassLoader());
		ConfigurationItem configuration = context
				.getConfigurationItem(location);
		assertNotNull("Can not find configuration '" + fileName + "'",
				configuration);
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
	 * Records initiating from the {@link SourceContext}.
	 */
	private void recordInitateFromSourceContext() {
		this.recordReturn(this.sourceContext, this.sourceContext
				.getClassLoader(), Thread.currentThread()
				.getContextClassLoader());
	}

	/**
	 * Record a template extension.
	 * 
	 * @param extensionSourceClass
	 *            {@link Class} of the {@link WoofTemplateExtensionSource}.
	 */
	private void recordTemplateExtension(
			Class<? extends WoofTemplateExtensionSource> extensionSourceClass) {

		// Load the source context
		this.recordReturn(this.sourceContext,
				this.sourceContext.isLoadingType(), true);

		// Record loading the template extension
		this.recordReturn(this.sourceContext,
				this.sourceContext.loadClass(extensionSourceClass.getName()),
				extensionSourceClass);
	}

	/**
	 * Records implicit {@link WoofTemplateExtensionSource} on the
	 * {@link HttpTemplateAutoWireSection}.
	 * 
	 * @param template
	 *            {@link HttpTemplateAutoWireSection}.
	 * @param templateUri
	 *            URI.
	 */
	private void recordImplicitTemplateExtensions(
			HttpTemplateAutoWireSection template, String templateUri) {

		// Record the template extension
		this.recordTemplateExtension(MockImplicitWoofTemplateExtensionSourceService.class);

		// Record obtain the template URI (ensure implicit used)
		this.recordReturn(template, template.getTemplateUri(), templateUri);
	}

}