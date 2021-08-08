/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
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
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateArchitect;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;
import net.officefloor.woof.model.woof.WoofTemplateModel;
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
	 * Mock {@link WebTemplateArchitect}.
	 */
	private final WebTemplateArchitect templater = this.createMock(WebTemplateArchitect.class);

	/**
	 * Mock {@link HttpResourceArchitect}.
	 */
	private final HttpResourceArchitect resources = this.createMock(HttpResourceArchitect.class);

	/**
	 * Mock {@link ProcedureArchitect}.
	 */
	@SuppressWarnings("unchecked")
	private final ProcedureArchitect<OfficeSection> procedure = this.createMock(ProcedureArchitect.class);

	/**
	 * Mock {@link SourceContext}.
	 */
	private final OfficeExtensionContext extensionContext = this.createMock(OfficeExtensionContext.class);

	/**
	 * Ensure can load configuration to {@link WebArchitect}.
	 */
	public void testLoading() throws Exception {

		// Record loading templates
		this.recordNoImplicitTemplateExtensions();
		WebTemplateRecorder templateA = new WebTemplateRecorder(true, "/template/{param}", "WOOF/TemplateA.ofp");
		templateA.record((template) -> template.setLogicClass("net.example.Template"));
		templateA.record((template) -> template.setRedirectValuesFunction("redirect"));
		templateA.record((template) -> template.setContentType("text/html; charset=UTF-16"));
		templateA.record((template) -> template.setCharset("UTF-16"));
		templateA.record((template) -> template.setLinkSeparatorCharacter('_'));
		templateA.record((template) -> template.setLinkSecure("LINK_1", true));
		templateA.record((template) -> template.setLinkSecure("LINK_2", false));
		templateA.record((template) -> template.addRenderHttpMethod("POST"));
		templateA.record((template) -> template.addRenderHttpMethod("PUT"));
		WebTemplateRecorder templateB = new WebTemplateRecorder(false, "/template/another", "WOOF/TemplateB.ofp");

		// Record loading sections
		final OfficeSection sectionA = this.createMock(OfficeSection.class);
		this.recordReturn(this.office,
				this.office.addOfficeSection("SECTION_A", ClassSectionSource.class.getName(), "net.example.Section"),
				sectionA);
		sectionA.addProperty("name.one", "value.one");
		sectionA.addProperty("name.two", "value.two");
		final OfficeSection sectionB = this.createMock(OfficeSection.class);
		this.recordReturn(this.office, this.office.addOfficeSection("SECTION_B", "CLASS", "net.another.Section"),
				sectionB);

		// Record loading procedures
		final PropertyList procedurePropertiesA = this.createMock(PropertyList.class);
		this.recordReturn(this.extensionContext, this.extensionContext.createPropertyList(), procedurePropertiesA);
		final Property procedurePropertyOne = this.createMock(Property.class);
		this.recordReturn(procedurePropertiesA, procedurePropertiesA.addProperty("name.ONE"), procedurePropertyOne);
		procedurePropertyOne.setValue("value.ONE");
		final Property procedurePropertyTwo = this.createMock(Property.class);
		this.recordReturn(procedurePropertiesA, procedurePropertiesA.addProperty("name.TWO"), procedurePropertyTwo);
		procedurePropertyTwo.setValue("value.TWO");
		final OfficeSection procedureA = this.createMock(OfficeSection.class);
		this.recordReturn(this.procedure, this.procedure.addProcedure("PROCEDURE_A", "net.example.ExampleProcedure",
				"Class", "procedure", true, procedurePropertiesA), procedureA);

		// Remaining procedures
		OfficeSection procedureB = null;
		for (String procedureSuffix : new String[] { "B", "C" }) {
			final PropertyList procedureProperties = this.createMock(PropertyList.class);
			this.recordReturn(this.extensionContext, this.extensionContext.createPropertyList(), procedureProperties);
			final OfficeSection procedure = this.createMock(OfficeSection.class);
			if (procedureB == null) {
				procedureB = procedure; // first is procedure B
			}
			this.recordReturn(this.procedure, this.procedure.addProcedure("PROCEDURE_" + procedureSuffix,
					"net.example.Procedure" + procedureSuffix, "JavaScript", "function", false, procedureProperties),
					procedure);
		}

		// Record loading securities
		final HttpSecurityBuilder securityOne = this.createMock(HttpSecurityBuilder.class);
		this.recordReturn(this.security,
				this.security.addHttpSecurity("SECURITY_ONE", "net.example.HttpSecuritySource"), securityOne);
		securityOne.setTimeout(2000);
		securityOne.addProperty("name.first", "value.first");
		securityOne.addProperty("name.second", "value.second");
		securityOne.addContentType("application/json");
		securityOne.addContentType("application/xml");
		final HttpSecurityBuilder securityTwo = this.createMock(HttpSecurityBuilder.class);
		this.recordReturn(this.security,
				this.security.addHttpSecurity("SECURITY_TWO", "net.another.HttpSecuritySource"), securityTwo);

		// Record loading resources
		OfficeFlowSinkNode resourceHtml = this.recordResource("/resource.html");
		OfficeFlowSinkNode resourcePng = this.recordResource("/resource.png");

		// Record HTTP continuations
		HttpUrlContinuation pathA = this.recordHttpContinuation(true, "/pathA");
		pathA.setDocumentation("HTTP Continuation A");
		HttpUrlContinuation pathB = this.recordHttpContinuation(false, "/pathB");
		HttpUrlContinuation pathC = this.recordHttpContinuation(false, "/pathC");
		HttpUrlContinuation pathD = this.recordHttpContinuation(false, "/pathD");
		HttpUrlContinuation pathE = this.recordHttpContinuation(false, "/pathE");
		HttpUrlContinuation pathF = this.recordHttpContinuation(false, "/pathF");

		// Record linking HTTP continuations
		this.office.link(this.recordGetInput(pathA), this.recordGetInput(sectionA, "INPUT_A"));
		this.office.link(this.recordGetInput(pathB), templateA.recordGetRender(null));
		this.office.link(this.recordGetInput(pathC), this.recordGetInput(securityOne));
		this.office.link(this.recordGetInput(pathD), resourceHtml);
		this.office.link(this.recordGetInput(pathE), this.recordRedirect(pathA, null));
		this.office.link(this.recordGetInput(pathF), this.recordGetProcedure(procedureA));

		// Record HTTP inputs
		this.recordHttpInput(true, "POST", "/inputA", this.recordGetInput(sectionB, "INPUT_0"), "HTTP Input A");
		this.recordHttpInput(false, "PUT", "/inputB", templateB.recordGetRender(null), null);
		this.recordHttpInput(false, "DELETE", "/inputC", this.recordGetInput(securityTwo), null);
		this.recordHttpInput(false, "OPTIONS", "/inputD", resourcePng, null);
		this.recordHttpInput(false, "OTHER", "/inputE", this.recordRedirect(pathA, null), null);
		this.recordHttpInput(false, "GET", "/inputF", this.recordGetProcedure(procedureA), null);

		// Record linking template outputs
		this.office.link(templateA.recordGetOutput("OUTPUT_1"), this.recordGetInput(sectionA, "INPUT_A"));
		this.office.link(templateA.recordGetOutput("OUTPUT_2"), templateB.recordGetRender(Character.class));
		this.office.link(templateA.recordGetOutput("OUTPUT_3"), this.recordGetInput(securityOne));
		this.office.link(templateA.recordGetOutput("OUTPUT_4"), resourceHtml);
		this.office.link(templateA.recordGetOutput("OUTPUT_5"), this.recordRedirect(pathA, String.class));
		this.office.link(templateA.recordGetOutput("OUTPUT_6"), this.recordGetProcedure(procedureA));

		// Record linking section outputs
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_A"), this.recordGetInput(sectionB, "INPUT_0"));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_B"), templateA.recordGetRender(Short.class));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_C"), this.recordGetInput(securityOne));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_D"), resourcePng);
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_E"), this.recordRedirect(pathC, Long.class));
		this.office.link(this.recordGetOutput(sectionA, "OUTPUT_F"), this.recordGetProcedure(procedureA));

		// Record linking procedure next
		this.office.link(this.recordGetNext(procedureA), this.recordGetInput(sectionB, "INPUT_0"));
		this.office.link(this.recordGetNext(procedureA), templateB.recordGetRender(String.class));
		this.office.link(this.recordGetNext(procedureA), this.recordGetInput(securityTwo));
		this.office.link(this.recordGetNext(procedureA), resourceHtml);
		this.office.link(this.recordGetNext(procedureA), this.recordRedirect(pathD, String.class));
		this.office.link(this.recordGetNext(procedureA), this.recordGetProcedure(procedureB));

		// Record linking procedure outputs
		this.office.link(this.recordGetOutput(procedureA, "OUTPUT_a"), this.recordGetInput(sectionA, "INPUT_A"));
		this.office.link(this.recordGetOutput(procedureA, "OUTPUT_b"), templateA.recordGetRender(Object.class));
		this.office.link(this.recordGetOutput(procedureA, "OUTPUT_c"), this.recordGetInput(securityOne));
		this.office.link(this.recordGetOutput(procedureA, "OUTPUT_d"), resourcePng);
		this.office.link(this.recordGetOutput(procedureA, "OUTPUT_e"), this.recordRedirect(pathC, Map.class));
		this.office.link(this.recordGetOutput(procedureA, "OUTPUT_f"), this.recordGetProcedure(procedureB));

		// Record link security outputs
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_ONE"), this.recordGetInput(sectionB, "INPUT_0"));
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_TWO"), templateB.recordGetRender(Object.class));
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_THREE"), this.recordGetInput(securityTwo));
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_FOUR"), resourceHtml);
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_FIVE"), this.recordRedirect(pathD, Map.class));
		this.office.link(this.recordGetOutput(securityOne, "OUTPUT_SIX"), this.recordGetProcedure(procedureA));

		// Record linking escalations
		this.office.link(this.recordEscalation(Exception.class), this.recordGetInput(sectionA, "INPUT_A"));
		this.office.link(this.recordEscalation(RuntimeException.class),
				templateA.recordGetRender(RuntimeException.class));
		this.office.link(this.recordEscalation(UnsupportedOperationException.class), this.recordGetInput(securityTwo));
		this.office.link(this.recordEscalation(SQLException.class), resourcePng);
		this.office.link(this.recordEscalation(NullPointerException.class),
				this.recordRedirect(pathE, NullPointerException.class));
		this.office.link(this.recordEscalation(IllegalArgumentException.class), this.recordGetProcedure(procedureA));

		// Record linking starts
		OfficeStart startOne = this.createMock(OfficeStart.class);
		this.recordReturn(this.office, this.office.addOfficeStart("1"), startOne);
		this.office.link(startOne, this.recordGetInput(sectionA, "INPUT_A"));
		OfficeStart startTwo = this.createMock(OfficeStart.class);
		this.recordReturn(this.office, this.office.addOfficeStart("2"), startTwo);
		this.office.link(startTwo, this.recordGetProcedure(procedureA));

		// Record loading governances
		final OfficeGovernance governanceA = this.createMock(OfficeGovernance.class);
		this.recordReturn(this.office,
				this.office.addOfficeGovernance("GOVERNANCE_A", ClassGovernanceSource.class.getName()), governanceA);
		governanceA.addProperty("name.a", "value.a");
		governanceA.addProperty("name.b", "value.b");
		governanceA.enableAutoWireExtensions();
		templateA.record((template) -> template.addGovernance(governanceA));
		sectionA.addGovernance(governanceA);
		procedureA.addGovernance(governanceA);
		final OfficeGovernance governanceB = this.createMock(OfficeGovernance.class);
		this.recordReturn(this.office, this.office.addOfficeGovernance("GOVERNANCE_B", "CLASS"), governanceB);
		governanceB.enableAutoWireExtensions();

		// Test
		this.replayMockObjects();
		this.loadConfiguration("application.woof.xml");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load inheritance of {@link WoofTemplateModel} configuration.
	 */
	public void testInheritance() throws Exception {

		// Record no implicit template extensions
		this.recordNoImplicitTemplateExtensions();

		// Record loading parent template
		WebTemplateRecorder parentTemplate = new WebTemplateRecorder(false, "/parent", "WOOF/Parent.ofp");
		parentTemplate.record((template) -> template.setLinkSecure("LINK_SECURE", true));
		parentTemplate.record((template) -> template.setLinkSecure("LINK_NON_SECURE", false));

		// Record loading child template (inheriting configuration)
		WebTemplateRecorder childTemplate = new WebTemplateRecorder(false, "/child", "WOOF/Child.ofp");
		childTemplate.record((template) -> template.setLinkSecure("LINK_OTHER", true));

		// Record loading grand child template (overriding configuration)
		WebTemplateRecorder grandChildTemplate = new WebTemplateRecorder(false, "/grandchild", "WOOF/GrandChild.ofp");
		grandChildTemplate.record((template) -> template.setLinkSecure("LINK_SECURE", false));
		grandChildTemplate.record((template) -> template.setLinkSecure("LINK_NON_SECURE", true));

		// Record loading remaining templates
		WebTemplateRecorder templateOne = new WebTemplateRecorder(false, "/one", "WOOF/TemplateOne.ofp");
		WebTemplateRecorder templateTwo = new WebTemplateRecorder(false, "/two", "WOOF/TemplateTwo.ofp");
		WebTemplateRecorder templateThree = new WebTemplateRecorder(false, "/three", "WOOF/TemplateThree.ofp");

		// Record loading sections
		final OfficeSection section = this.createMock(OfficeSection.class);
		this.recordReturn(this.office,
				this.office.addOfficeSection("SECTION", "CLASS", "net.officefloor.ExampleSection"), section);

		// Record loading security
		final HttpSecurityBuilder security = this.createMock(HttpSecurityBuilder.class);
		this.recordReturn(this.security, this.security.addHttpSecurity("SECURITY", "net.example.HttpSecuritySource"),
				security);
		security.setTimeout(2000);

		// Record loading resources
		OfficeFlowSinkNode resourceOne = this.recordResource("/ResourceOne.html");
		OfficeFlowSinkNode resourceTwo = this.recordResource("/ResourceTwo.html");

		// Record linking parent template outputs
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_SECTION"), this.recordGetInput(section, "INPUT_1"));
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_TEMPLATE"), templateOne.recordGetRender(null));
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_SECURITY"), this.recordGetInput(security));
		this.office.link(parentTemplate.recordGetOutput("OUTPUT_RESOURCE"), resourceOne);

		// Child template inherits link configuration
		childTemplate.record((template) -> template.setSuperTemplate(parentTemplate.template));

		// Record linking grand child template outputs (overriding)
		grandChildTemplate.record((template) -> template.setSuperTemplate(childTemplate.template));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_SECTION"), this.recordGetInput(section, "INPUT_2"));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_TEMPLATE"), templateTwo.recordGetRender(null));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_SECURITY"), this.recordGetInput(security));
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_RESOURCE"), resourceTwo);
		this.office.link(grandChildTemplate.recordGetOutput("OUTPUT_ANOTHER"), templateThree.recordGetRender(null));

		// Test
		this.replayMockObjects();
		this.loadConfiguration("inheritance.woof.xml");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can load implicit {@link WoofTemplateExtensionSource}.
	 */
	public void testImplicitTemplateExtension() throws Exception {

		// Record implicit template extensions
		MockImplicitWoofTemplateExtensionSourceService.recordLoadImplicit(this.extensionContext, this, "example");

		// Record loading template
		new WebTemplateRecorder(false, "example", "WOOF/Template.html");

		// Record extending template
		this.recordTemplateExtension(null);

		// Test
		this.replayMockObjects();
		this.loadConfiguration("implicit-template-extension.woof.xml");
		this.verifyMockObjects();

		// Ensure implicit extension invoked
		MockImplicitWoofTemplateExtensionSourceService.assertTemplatesExtended();
	}

	/**
	 * Ensure can load explicit {@link WoofTemplateExtensionSource}.
	 */
	public void testExplicitTemplateExtension() throws Exception {

		// Record implicit template extensions
		this.recordNoImplicitTemplateExtensions();

		// Record loading template
		new WebTemplateRecorder(false, "example", "WOOF/Template.html");

		// Record extending with explicit template extension
		this.recordTemplateExtension(MockExplicitWoofTemplateExtensionSource.class);

		// Test
		MockExplicitWoofTemplateExtensionSource.isInvoked = false;
		this.replayMockObjects();
		this.loadConfiguration("explicit-template-extension.woof.xml");
		this.verifyMockObjects();
		assertTrue("Should invoke explicit templae extension", MockExplicitWoofTemplateExtensionSource.isInvoked);
	}

	/**
	 * Mock explicit {@link WoofTemplateExtensionSource}.
	 */
	public static class MockExplicitWoofTemplateExtensionSource extends AbstractWoofTemplateExtensionSource {

		private static boolean isInvoked = false;

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification");
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {
			// Ensure correct template
			assertEquals("Obtain location to ensure extending", "example", context.getApplicationPath());
			assertEquals("Should obtain property", "VALUE", context.getProperty("NAME"));
			isInvoked = true;
		}
	}

	/**
	 * Undertakes loading the configuration.
	 * 
	 * @param configurationFileLocation Location of the {@link ConfigurationItem}.
	 */
	private void loadConfiguration(String configurationFileLocation) throws Exception {
		this.loader.loadWoofConfiguration(new WoofContext() {

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

			@Override
			public HttpSecurityArchitect getHttpSecurityArchitect() {
				return WoofLoaderTest.this.security;
			}

			@Override
			public WebTemplateArchitect getWebTemplater() {
				return WoofLoaderTest.this.templater;
			}

			@Override
			public HttpResourceArchitect getHttpResourceArchitect() {
				return WoofLoaderTest.this.resources;
			}

			@Override
			public ProcedureArchitect<OfficeSection> getProcedureArchitect() {
				return WoofLoaderTest.this.procedure;
			}
		});
	}

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param fileName File name for {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem}.
	 */
	private ConfigurationItem getConfiguration(String fileName) {
		String location = this.getFileLocation(this.getClass(), fileName);
		ConfigurationContext context = new ClassLoaderConfigurationContext(this.compiler.getClassLoader(), null);
		ConfigurationItem configuration = context.getConfigurationItem(location, null);
		assertNotNull("Can not find configuration '" + fileName + "' at location " + location, configuration);
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

		private OfficeFlowSinkNode recordGetRender(Class<?> valuesType) {
			OfficeFlowSinkNode render = WoofLoaderTest.this.createMock(OfficeFlowSinkNode.class);
			WoofLoaderTest.this.recordReturn(this.template,
					this.template.getRender(valuesType == null ? null : valuesType.getName()), render);
			return render;
		}

		private OfficeFlowSourceNode recordGetOutput(String outputName) {
			OfficeFlowSourceNode output = WoofLoaderTest.this.createMock(OfficeFlowSourceNode.class);
			WoofLoaderTest.this.recordReturn(this.template, this.template.getOutput(outputName), output);
			return output;
		}
	}

	/**
	 * Records {@link HttpUrlContinuation}.
	 * 
	 * @param isSecure        Secure.
	 * @param applicationPath Application path.
	 * @return Mock {@link HttpUrlContinuation}.
	 */
	private HttpUrlContinuation recordHttpContinuation(boolean isSecure, String applicationPath) {
		HttpUrlContinuation continuation = this.createMock(HttpUrlContinuation.class);
		this.recordReturn(this.web, this.web.getHttpInput(isSecure, applicationPath), continuation);
		return continuation;
	}

	/**
	 * Records obtain the {@link OfficeFlowSourceNode}.
	 * 
	 * @param continuation {@link HttpUrlContinuation}.
	 * @return Mock {@link OfficeFlowSourceNode}.
	 */
	private OfficeFlowSourceNode recordGetInput(HttpUrlContinuation continuation) {
		OfficeFlowSourceNode input = this.createMock(OfficeFlowSourceNode.class);
		this.recordReturn(continuation, continuation.getInput(), input);
		return input;
	}

	/**
	 * Records {@link HttpInput}.
	 * 
	 * @param isSecure        Secure.
	 * @param httpMethod      {@link HttpMethod}.
	 * @param applicationPath Application path.
	 * @param flowSinkNode    {@link OfficeFlowSinkNode}.
	 * @param documentation   Documentation.
	 * @return Mock {@link HttpInput}.
	 */
	private HttpInput recordHttpInput(boolean isSecure, String httpMethod, String applicationPath,
			OfficeFlowSinkNode flowSinkNode, String documentation) {
		HttpInput httpInput = this.createMock(HttpInput.class);
		this.recordReturn(this.web, this.web.getHttpInput(isSecure, httpMethod, applicationPath), httpInput);
		if (documentation != null) {
			httpInput.setDocumentation(documentation);
		}
		OfficeFlowSourceNode input = this.createMock(OfficeFlowSourceNode.class);
		this.recordReturn(httpInput, httpInput.getInput(), input);
		this.office.link(input, flowSinkNode);
		return httpInput;
	}

	/**
	 * Records creating {@link OfficeEscalation}.
	 * 
	 * @param escalationType {@link Escalation} type.
	 * @return {@link OfficeEscalation}.
	 */
	private OfficeEscalation recordEscalation(Class<? extends Throwable> escalationType) {
		OfficeEscalation escalation = this.createMock(OfficeEscalation.class);
		this.recordReturn(this.office, this.office.addOfficeEscalation(escalationType.getName()), escalation);
		return escalation;
	}

	/**
	 * Records obtain the {@link HttpUrlContinuation} redirect.
	 * 
	 * @param continuation  {@link HttpUrlContinuation}.
	 * @param parameterType Parameter type.
	 * @return {@link OfficeFlowSinkNode}.
	 */
	private OfficeFlowSinkNode recordRedirect(HttpUrlContinuation continuation, Class<?> parameterType) {
		OfficeFlowSinkNode redirect = this.createMock(OfficeFlowSinkNode.class);
		this.recordReturn(continuation,
				continuation.getRedirect(parameterType == null ? null : parameterType.getName()), redirect);
		return redirect;
	}

	/**
	 * Records obtain the {@link OfficeSectionOutput}.
	 * 
	 * @param section    {@link OfficeSection}.
	 * @param outputName Name of {@link OfficeSectionOutput}.
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
	 * @param section   {@link OfficeSection}.
	 * @param inputName Name of {@link OfficeSectionInput}.
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
	 * @param security   {@link HttpSecurityBuilder}.
	 * @param outputName Name of the {@link OfficeSectionOutput}.
	 * @return {@link OfficeSectionOutput}.
	 */
	private OfficeSectionOutput recordGetOutput(HttpSecurityBuilder security, String outputName) {
		OfficeSectionOutput output = this.createMock(OfficeSectionOutput.class);
		this.recordReturn(security, security.getOutput(outputName), output);
		return output;
	}

	/**
	 * Records obtain the {@link OfficeSectionInput} for the {@link Procedure}.
	 * 
	 * @param section {@link OfficeSection}.
	 * @return {@link OfficeSectionInput} to the {@link Procedure}.
	 */
	private OfficeSectionInput recordGetProcedure(OfficeSection procedure) {
		OfficeSectionInput sectionInput = this.createMock(OfficeSectionInput.class);
		this.recordReturn(procedure, procedure.getOfficeSectionInput(ProcedureArchitect.INPUT_NAME), sectionInput);
		return sectionInput;
	}

	/**
	 * Records obtain the {@link OfficeSectionOutput} for {@link Procedure} next.
	 * 
	 * @param section    {@link OfficeSection}.
	 * @param outputName Name of {@link OfficeSectionOutput}.
	 * @return {@link OfficeSectionOutput}.
	 */
	private OfficeSectionOutput recordGetNext(OfficeSection section) {
		OfficeSectionOutput sectionOutput = this.createMock(OfficeSectionOutput.class);
		this.recordReturn(section, section.getOfficeSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), sectionOutput);
		return sectionOutput;
	}

	/**
	 * Records obtaining {@link OfficeSectionInput}.
	 * 
	 * @param security {@link HttpSecurityBuilder}.
	 * @return {@link OfficeSectionInput}.
	 */
	private OfficeSectionInput recordGetInput(HttpSecurityBuilder security) {
		OfficeSectionInput input = this.createMock(OfficeSectionInput.class);
		this.recordReturn(security, security.getAuthenticateInput(), input);
		return input;
	}

	/**
	 * Records obtaining resource {@link OfficeFlowSinkNode}.
	 * 
	 * @param resourcePath Resource path.
	 * @return {@link OfficeFlowSinkNode}.
	 */
	private OfficeFlowSinkNode recordResource(String resourcePath) {
		OfficeFlowSinkNode resource = this.createMock(OfficeFlowSinkNode.class);
		this.recordReturn(this.resources, this.resources.getResource(resourcePath), resource);
		return resource;
	}

	/**
	 * Records implicit {@link WoofTemplateExtensionSource} on the
	 * {@link WebTemplate}.
	 * 
	 * @param extensionSources {@link WoofTemplateExtensionSource} instances.
	 */
	private void recordNoImplicitTemplateExtensions() {
		this.recordReturn(this.extensionContext,
				this.extensionContext.loadOptionalServices(WoofTemplateExtensionSourceService.class),
				Collections.EMPTY_LIST);
	}

	/**
	 * Record a template extension.
	 * 
	 * @param extensionSourceClass {@link Class} of the
	 *                             {@link WoofTemplateExtensionSource}.
	 * @param nameValuePairs       Name/value {@link Property} pairs.
	 */
	private void recordTemplateExtension(Class<? extends WoofTemplateExtensionSource> extensionSourceClass,
			String... nameValuePairs) {

		// Record loading the template extension
		if (extensionSourceClass != null) {
			this.recordReturn(this.extensionContext, this.extensionContext.loadClass(extensionSourceClass.getName()),
					extensionSourceClass);
		}

		// Record obtaining properties
		PropertyList properties = new PropertyListImpl(nameValuePairs);
		this.recordReturn(this.extensionContext, this.extensionContext.createPropertyList(), properties);

		// Load the source context
		this.recordReturn(this.extensionContext, this.extensionContext.getName(), "template");
		this.recordReturn(this.extensionContext, this.extensionContext.isLoadingType(), true);
		this.recordReturn(this.extensionContext, this.extensionContext.getProfiles(), Collections.emptyList());
	}

}
