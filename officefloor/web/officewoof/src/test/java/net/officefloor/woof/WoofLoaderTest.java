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
package net.officefloor.woof;

import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.logging.LogRecord;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.classloader.ClassLoaderConfigurationContext;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.test.LoggerAssertion;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.HttpInputPath;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.type.HttpSecuritySourceSpecificationRunnableTest.MockHttpSecuritySource;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplater;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;
import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.template.WoofTemplateExtensionException;
import net.officefloor.woof.template.WoofTemplateExtensionSource;
import net.officefloor.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.woof.template.WoofTemplateExtensionSourceService;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

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

		final HttpUrlContinuation link = this.createMock(HttpUrlContinuation.class);
		final OfficeGovernance governanceA = this.createMock(OfficeGovernance.class);
		final OfficeGovernance governanceB = this.createMock(OfficeGovernance.class);

		// Record initiating from source context
		this.recordInitateFromExtensionContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("example", "another");

		// Record loading templates
		WebTemplateRecorder templateA = new WebTemplateRecorder(true, "/template/{param}", "WOOF/TemplateA.ofp");
		templateA.record((template) -> template.setLogicClass(Template.class));
		templateA.record((template) -> template.setRedirectValuesFunction("redirect"));
		templateA.record((template) -> template.setContentType("text/html; charset=UTF-8"));
		templateA.record((template) -> template.setCharset(Charset.forName("UTF-8")));
		templateA.record((template) -> template.setLinkSeparatorCharacter('_'));
		templateA.record((template) -> template.setLinkSecure("LINK_1", true));
		templateA.record((template) -> template.setLinkSecure("LINK_2", false));
		templateA.record((template) -> template.addRenderMethod(HttpMethod.getHttpMethod("POST")));
		templateA.record((template) -> template.addRenderMethod(HttpMethod.getHttpMethod("PUT")));
		this.recordImplicitTemplateExtensions();
		WebTemplateRecorder templateB = new WebTemplateRecorder(false, "/template/another", "WOOF/TemplateB.ofp");
		this.recordImplicitTemplateExtensions();

		// Record loading sections
		final OfficeSection sectionA = this.createMock(OfficeSection.class);
		this.recordReturn(this.office,
				this.office.addOfficeSection("SECTION_A", ClassSectionSource.class.getName(), Section.class.getName()),
				sectionA);
		sectionA.addProperty("name.one", "value.one");
		sectionA.addProperty("name.two", "value.two");
		final OfficeSectionInput sectionAInput = this.createMock(OfficeSectionInput.class);
		this.recordReturn(sectionA, sectionA.getOfficeSectionInput("INPUT_B"), sectionAInput);
		this.recordReturn(this.web, this.web.link(false, "example", sectionAInput), link);
		final OfficeSection sectionB = this.createMock(OfficeSection.class);
		this.recordReturn(this.office, this.office.addOfficeSection("SECTION_B", "CLASS", Section.class.getName()),
				sectionB);

		// Record loading securities
		final HttpSecurityBuilder securityOne = this.createMock(HttpSecurityBuilder.class);
		this.recordReturn(this.security,
				this.security.addHttpSecurity("SECURITY_ONE", MockHttpSecuritySource.class.getName()), securityOne);
		securityOne.setTimeout(2000);
		securityOne.addProperty("name.first", "value.first");
		securityOne.addProperty("name.second", "value.second");
		final HttpSecurityBuilder securityTwo = this.createMock(HttpSecurityBuilder.class);
		this.recordReturn(this.security,
				this.security.addHttpSecurity("SECURITY_TWO", MockHttpSecuritySource.class.getName()), securityTwo);

		// Record application paths
		HttpUrlContinuation pathAContinuation = this.createMock(HttpUrlContinuation.class);
		this.recordReturn(this.web, this.web.link(true, "/pathA", this.recordGetInput(sectionA, "INPUT_A")),
				pathAContinuation);
		HttpInputPath pathAInput = this.createMock(HttpInputPath.class);
		this.recordReturn(this.web,
				this.web.link(true, HttpMethod.POST, "/pathA", this.recordGetInput(sectionA, "INPUT_A")), pathAInput);
		// TODO link application path to template
		HttpUrlContinuation pathC = this.createMock(HttpUrlContinuation.class);
		this.recordReturn(this.web, this.web.link(false, "/pathC", securityOne.getAuthenticateInput()), pathC);
		// TODO link application path to resource
		HttpUrlContinuation pathE = this.createMock(HttpUrlContinuation.class);
		this.recordReturn(this.web, this.web.link(false, "/pathE", pathAContinuation.getRedirect(null)), pathE);

		// Record linking template outputs
		this.office.link(templateA.recordGetOutput("OUTPUT_1"), this.recordGetInput(sectionA, "INPUT_A"));
		this.office.link(templateA.recordGetOutput("OUTPUT_2"), templateB.recordGetRender(null));
		this.office.link(templateA.recordGetOutput("OUTPUT_3"), this.recordGetInput(securityOne));
		this.office.link(templateA.recordGetOutput("OUTPUT_4"), this.resources.getResource("/resource.html"));
		this.office.link(templateA.recordGetOutput("OUTPUT_5"), pathAContinuation.getRedirect(String.class));

		// Record linking section outputs
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_A"), this.recordGetInput(sectionB, "INPUT_1"));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_B"), templateA.recordGetRender(null));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_C"), this.recordGetInput(securityOne));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_D"), this.resources.getResource("/resource.png"));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_E"), pathC.getRedirect(Long.class));

		// Record link security outputs
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_ONE"), this.recordGetInput(sectionB, "INPUT_1"));
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_TWO"), templateB.recordGetRender(null));
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_THREE"), this.recordGetInput(securityTwo));
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_FOUR"),
				this.resources.getResource("/resource.html"));
		HttpUrlContinuation pathD = this.createMock(HttpUrlContinuation.class);
		// TODO configure HTTP URL continuation to resource
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_5"), pathD.getRedirect(null));

		// Record linking escalations
		this.office.link(this.recordEscalation(Exception.class), this.recordGetInput(sectionA, "INPUT_A"));
		this.office.link(this.recordEscalation(RuntimeException.class), templateA.recordGetRender(null));
		this.office.link(this.recordEscalation(UnsupportedOperationException.class), this.recordGetInput(securityTwo));
		this.office.link(this.recordEscalation(SQLException.class), this.resources.getResource("/resource.png"));
		// TODO configure HTTP URL continuation for exception
		this.office.link(this.recordEscalation(NullPointerException.class), pathE.getRedirect(null));

		// Record linking starts
		OfficeStart startOne = this.createMock(OfficeStart.class);
		this.recordReturn(this.office, this.office.addOfficeStart("START_1"), startOne);
		this.office.link(startOne, this.recordGetInput(sectionA, "INPUT_A"));
		OfficeStart startTwo = this.createMock(OfficeStart.class);
		this.recordReturn(this.office, this.office.addOfficeStart("START_2"), startTwo);
		this.office.link(startTwo, this.recordGetInput(sectionB, "INPUT_1"));

		// Record loading governances
		this.recordReturn(this.office,
				this.office.addOfficeGovernance("GOVERNANCE_A", ClassGovernanceSource.class.getName()), governanceA);
		governanceA.addProperty("name.a", "value.a");
		governanceA.addProperty("name.b", "value.b");
		templateA.record((template) -> template.addGovernance(governanceA));
		sectionA.addGovernance(governanceA);
		this.recordReturn(this.office, this.office.addOfficeGovernance("GOVERNANCE_B", "CLASS"), governanceB);

		// Test
		this.replayMockObjects();
		this.loadConfiguration("application.woof.xml");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load inheritance of {@link WoofTemplateModel} configuration.
	 */
	public void testInheritance() throws Exception {

		// Record initiating from source context
		this.recordInitateFromExtensionContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("parent", "child", "grandchild", "one", "two", "three");

		// Record loading parent template
		WebTemplateRecorder parentTemplate = new WebTemplateRecorder(false, "parent", "WOOF/Parent.ofp");
		parentTemplate.record((template) -> template.setLinkSecure("LINK_SECURE", true));
		parentTemplate.record((template) -> template.setLinkSecure("LINK_NON_SECURE", false));
		this.recordImplicitTemplateExtensions();

		// Record loading child template (inheriting configuration)
		WebTemplateRecorder childTemplate = new WebTemplateRecorder(false, "child", "WOOF/Child.ofp");
		childTemplate.record((template) -> template.setLinkSecure("LINK_OTHER", true));
		this.recordImplicitTemplateExtensions();

		// Record loading grand child template (overriding configuration)
		WebTemplateRecorder grandChildTemplate = new WebTemplateRecorder(false, "grandchild", "WOOF/GrandChild.ofp");
		grandChildTemplate.record((template) -> template.setLinkSecure("LINK_SECURE", false));
		grandChildTemplate.record((template) -> template.setLinkSecure("LINK_NON_SECURE", true));
		this.recordImplicitTemplateExtensions();

		// Record loading remaining templates
		WebTemplateRecorder templateOne = new WebTemplateRecorder(false, "one", "WOOF/TemplateOne.ofp");
		this.recordImplicitTemplateExtensions();
		WebTemplateRecorder templateTwo = new WebTemplateRecorder(false, "two", "WOOF/TemplateTwo.ofp");
		this.recordImplicitTemplateExtensions();
		WebTemplateRecorder templateThree = new WebTemplateRecorder(false, "three", "WOOF/TemplateThree.ofp");
		this.recordImplicitTemplateExtensions();

		// Record loading sections
		final OfficeSection section = this.createMock(OfficeSection.class);
		this.recordReturn(this.office,
				this.office.addOfficeSection("SECTION", "CLASS", "net.officefloor.ExampleSection"), section);

		// Record loading access
		final HttpSecurityBuilder security = this.createMock(HttpSecurityBuilder.class);
		this.recordReturn(this.security,
				this.security.addHttpSecurity("SECURITY", MockHttpSecuritySource.class.getName()), security);
		security.setTimeout(2000);

		// Record linking parent template outputs
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_SECTION"), this.recordGetInput(section, "INPUT_1"));
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_TEMPLATE"), templateOne.recordGetRender(null));
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_ACCESS"), this.recordGetInput(security));
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_RESOURCE"),
				this.resources.getResource("ResourceOne.html"));

		// Child template inherits link configuration
		childTemplate.template.setSuperTemplate(parentTemplate.template);

		// Record linking grand child template outputs (overriding)
		grandChildTemplate.template.setSuperTemplate(childTemplate.template);
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_SECTION"), this.recordGetInput(section, "INPUT_2"));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_TEMPLATE"), templateTwo.recordGetRender(null));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_ACCESS"), this.recordGetInput(security));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_RESOURCE"),
				this.resources.getResource("ResourceTwo.html"));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_ANOTHER"), templateThree.recordGetRender(null));

		// Test
		this.replayMockObjects();
		this.loadConfiguration("inheritance.woof.xml");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load explicit {@link WoofTemplateExtensionSource}.
	 */
	public void testExplicitTemplateExtension() throws Exception {

		// Record initiating from source context
		this.recordInitateFromExtensionContext();
		MockImplicitWoofTemplateExtensionSourceService.reset("example");

		// Record loading template
		WebTemplateRecorder example = new WebTemplateRecorder(false, "example", "WOOF/Template.html");
		example.record((template) -> template.setLogicClass(Template.class));

		// Record extending with explicit template extension
		this.recordTemplateExtension(MockExplicitWoofTemplateExtensionSource.class);

		// Record implicit template extensions
		this.recordImplicitTemplateExtensions();

		// Test
		this.replayMockObjects();
		this.loadConfiguration("explicit-template-extension.woof.xml");
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
		this.recordReturn(this.templater, this.templater.addTemplate(false, "example", "WOOF/Template.html"), template);
		template.setLogicClass(Template.class);

		// Should not load further as unknown template extension
		this.recordReturn(this.extensionContext, this.extensionContext.isLoadingType(), true);
		final UnknownClassError unknownClassError = new UnknownClassError("UNKNOWN");
		this.extensionContext.loadClass("UNKNOWN");
		this.control(this.extensionContext).setThrowable(unknownClassError);

		// Test
		this.replayMockObjects();
		try {
			this.loadConfiguration("unknown-template-extension.woof.xml");
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
	 * Convenience class to make {@link WebTemplate} recording easier.
	 */
	private class WebTemplateRecorder {

		private final WebTemplate template;

		private WebTemplateRecorder(boolean isSecure, String applicationPath, String location) {
			this.template = WoofLoaderTest.this.createMock(WebTemplate.class);
			WoofLoaderTest.this.recordReturn(WoofLoaderTest.this.templater,
					WoofLoaderTest.this.templater.addTemplate(isSecure, applicationPath, location), this.template);
		}

		private void record(Function<WebTemplate, WebTemplate> action) {
			WoofLoaderTest.this.recordReturn(this.template, action.apply(this.template), this.template);
		}

		private <R> void record(Function<WebTemplate, R> action, R returnValue) {
			WoofLoaderTest.this.recordReturn(this.template, action.apply(this.template), returnValue);
		}

		private OfficeFlowSinkNode recordGetRender(Class<?> valuesType) {
			OfficeFlowSinkNode render = WoofLoaderTest.this.createMock(OfficeFlowSinkNode.class);
			WoofLoaderTest.this.recordReturn(this.template, this.template.getRender(valuesType), render);
			return render;
		}

		private OfficeFlowSourceNode recordGetOutput(String outputName) {
			OfficeFlowSourceNode output = WoofLoaderTest.this.createMock(OfficeFlowSourceNode.class);
			WoofLoaderTest.this.recordReturn(this.template, this.template.getOutput(outputName), output);
			return output;
		}
	}

	/**
	 * Records creating {@link OfficeEscalation}.
	 * 
	 * @param escalationType
	 *            {@link Escalation} type.
	 * @return {@link OfficeEscalation}.
	 */
	private OfficeEscalation recordEscalation(Class<? extends Throwable> escalationType) {
		OfficeEscalation escalation = this.createMock(OfficeEscalation.class);
		this.recordReturn(this.office, this.office.addOfficeEscalation(escalationType.getName()), escalation);
		return escalation;
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
	private OfficeSectionOutput recordGetOutput(OfficeSection section, String outputName) {
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
	private OfficeSectionInput recordGetInput(OfficeSection section, String inputName) {
		OfficeSectionInput sectionInput = this.createMock(OfficeSectionInput.class);
		this.recordReturn(section, section.getOfficeSectionInput(inputName), sectionInput);
		return sectionInput;
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
	private OfficeSectionOutput recordGetOutput(HttpSecurityBuilder security, String outputName) {
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
	private OfficeSectionInput recordGetInput(HttpSecurityBuilder security) {
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
	 */
	private void recordImplicitTemplateExtensions() {

		// Record the template extension
		this.recordTemplateExtension(MockImplicitWoofTemplateExtensionSourceService.class);
	}

}