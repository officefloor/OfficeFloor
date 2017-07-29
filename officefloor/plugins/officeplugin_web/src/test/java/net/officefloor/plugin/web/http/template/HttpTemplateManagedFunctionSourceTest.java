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
package net.officefloor.plugin.web.http.template;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.parse.BeanHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.BeanHttpTemplateSectionContentImpl;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateImpl;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSectionImpl;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContentImpl;
import net.officefloor.plugin.web.http.template.parse.PropertyHttpTemplateSectionContentImpl;
import net.officefloor.plugin.web.http.template.parse.StaticHttpTemplateSectionContentImpl;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.parse.UsAsciiUtil;

/**
 * Tests the {@link HttpTemplateManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Path to template for missing items to check errors.
	 */
	private final String missingTemplateFilePath = this.getClass().getPackage().getName().replace('.', '/')
			+ "/TemplateMissing.ofp";

	/**
	 * Template path.
	 */
	private final String templatePath = this.getClass().getPackage().getName().replace('.', '/') + "/Template.ofp";

	/**
	 * Root template path.
	 */
	private final String rootTemplatePath = this.getClass().getPackage().getName().replace('.', '/')
			+ "/RootTemplate.ofp";

	/**
	 * Verifies the specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpTemplateManagedFunctionSource.class,
				HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_FILE, "Template",
				HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI, "URI Path");
	}

	/**
	 * Validate the type.
	 */
	public void testType() throws Exception {

		// Create the expected namespace
		FunctionNamespaceBuilder namespace = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Create the function factory
		HttpTemplateFunction httpTemplateFunctionFactory = new HttpTemplateFunction(null, false,
				Charset.defaultCharset());

		// 'template' function
		ManagedFunctionTypeBuilder<Indexed, None> template = namespace.addManagedFunctionType("template",
				httpTemplateFunctionFactory, Indexed.class, None.class);
		template.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		template.addObject(HttpApplicationLocation.class).setLabel("HTTP_APPLICATION_LOCATION");
		template.addObject(TemplateBean.class).setLabel("OBJECT");
		template.addEscalation(IOException.class);

		// 'BeanTree' function
		ManagedFunctionTypeBuilder<Indexed, None> beanTree = namespace.addManagedFunctionType("BeanTree",
				httpTemplateFunctionFactory, Indexed.class, None.class);
		beanTree.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		beanTree.addObject(HttpApplicationLocation.class).setLabel("HTTP_APPLICATION_LOCATION");
		beanTree.addObject(BeanTreeBean.class).setLabel("OBJECT");
		beanTree.addEscalation(IOException.class);

		// 'NullBean' function
		ManagedFunctionTypeBuilder<Indexed, None> nullBean = namespace.addManagedFunctionType("NullBean",
				httpTemplateFunctionFactory, Indexed.class, None.class);
		nullBean.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		nullBean.addObject(HttpApplicationLocation.class).setLabel("HTTP_APPLICATION_LOCATION");
		nullBean.addObject(Object.class).setLabel("OBJECT");
		nullBean.addEscalation(IOException.class);

		// 'NoBean' function
		ManagedFunctionTypeBuilder<Indexed, None> noBean = namespace.addManagedFunctionType("NoBean",
				httpTemplateFunctionFactory, Indexed.class, None.class);
		noBean.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		noBean.addObject(HttpApplicationLocation.class).setLabel("HTTP_APPLICATION_LOCATION");
		noBean.addEscalation(IOException.class);

		// 'List' function
		ManagedFunctionTypeBuilder<Indexed, None> list = namespace.addManagedFunctionType("List",
				httpTemplateFunctionFactory, Indexed.class, None.class);
		list.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		list.addObject(HttpApplicationLocation.class).setLabel("HTTP_APPLICATION_LOCATION");
		list.addObject(TableRowBean.class).setLabel("OBJECT");
		list.addEscalation(IOException.class);

		// 'Tail' function
		ManagedFunctionTypeBuilder<Indexed, None> tail = namespace.addManagedFunctionType("Tail",
				httpTemplateFunctionFactory, Indexed.class, None.class);
		tail.addObject(ServerHttpConnection.class).setLabel("SERVER_HTTP_CONNECTION");
		tail.addObject(HttpApplicationLocation.class).setLabel("HTTP_APPLICATION_LOCATION");
		tail.addEscalation(IOException.class);

		// Verify the managed function type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespace, HttpTemplateManagedFunctionSource.class,
				this.getProperties(this.templatePath, "uri"));
	}

	/**
	 * Ensure indicates failure on missing bean for {@link HttpTemplateSection}.
	 */
	public void testMissingBean() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record failure due to missing bean
		issues.recordIssue(
				"Missing property 'bean.template' for WorkSource " + HttpTemplateManagedFunctionSource.class.getName());

		// Create and initiate the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Do not provide bean for template section
		PropertyList propertyList = OfficeFloorCompiler.newPropertyList();
		propertyList.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_FILE)
				.setValue(this.missingTemplateFilePath);
		propertyList.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI).setValue("uri");

		// Test loading ensuring indicates failure
		this.replayMockObjects();
		compiler.getManagedFunctionLoader().loadManagedFunctionType(HttpTemplateManagedFunctionSource.class,
				propertyList);
		this.verifyMockObjects();
	}

	/**
	 * Ensure indicates failure on missing property in the
	 * {@link HttpTemplateSection}.
	 */
	public void testMissingProperty() {
		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record failure due to missing bean
		final String message = "Property 'MissingProperty' can not be sourced from bean type " + Object.class.getName();
		issues.recordIssue("Failed to source WorkType definition from WorkSource "
				+ HttpTemplateManagedFunctionSource.class.getName(), new Exception(message));

		// Create and initiate the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Provide bean that does not have the required property
		PropertyList propertyList = OfficeFloorCompiler.newPropertyList();
		propertyList.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_FILE)
				.setValue(this.missingTemplateFilePath);
		propertyList.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI).setValue("uri");
		propertyList.addProperty("bean.template").setValue(Object.class.getName());

		// Test loading ensuring indicates failure
		this.replayMockObjects();
		compiler.getManagedFunctionLoader().loadManagedFunctionType(HttpTemplateManagedFunctionSource.class,
				propertyList);
		this.verifyMockObjects();
	}

	/**
	 * Tests generating secure template response.
	 */
	public void testSecureTemplate() throws Throwable {
		this.doTemplateTest(true, null);
	}

	/**
	 * Tests generating secure template response with a template URI suffix.
	 */
	public void testSecureTemplateWithUriSuffix() throws Throwable {
		this.doTemplateTest(true, ".suffix");
	}

	/**
	 * Tests generating non-secure template response.
	 */
	public void testNonSecureTemplate() throws Throwable {
		this.doTemplateTest(false, null);
	}

	/**
	 * Tests generating non-secure template response with a template URI suffix.
	 */
	public void testNonSecureTemplateWithUriSuffix() throws Throwable {
		this.doTemplateTest(false, ".suffix");
	}

	/**
	 * Tests running the template to generate response.
	 */
	@SuppressWarnings("rawtypes")
	public void doTemplateTest(boolean isTemplateSecure, String templateUriSuffix) throws Throwable {

		// Create the mock objects
		ManagedFunctionContext functionContext = this.createMock(ManagedFunctionContext.class);
		ServerHttpConnection httpConnection = this.createMock(ServerHttpConnection.class);
		HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

		// Create the HTTP response to record output
		MockHttpResponse httpResponse = new MockHttpResponse();

		// Create the additional properties
		List<String> additionalProperties = new LinkedList<String>();
		if (isTemplateSecure) {
			// Secure template with non-secure link
			additionalProperties.addAll(
					Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_SECURE, String.valueOf(true)));
			additionalProperties.addAll(Arrays.asList(
					HttpTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "beans", String.valueOf(false)));
		} else {
			// Default non-secure template with secure link
			additionalProperties.addAll(Arrays.asList(
					HttpTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "submit", String.valueOf(true)));
		}

		// Specify the template URI suffix
		if (templateUriSuffix == null) {
			// Provide empty string to not have "null" appended
			templateUriSuffix = "";
		} else {
			// Provide property for template URI suffix
			additionalProperties.addAll(
					Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI_SUFFIX, templateUriSuffix));
		}

		// Load the namespace type
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil
				.loadManagedFunctionType(HttpTemplateManagedFunctionSource.class, this.getProperties(this.templatePath,
						"uri", additionalProperties.toArray(new String[additionalProperties.size()])));

		// Record actions for each task:
		// - 'template'
		// - 'BeanTree'
		// - 'NullBean'
		// (NoBean does not source bean)
		// - 'List' task with table row bean
		// - 'List' task with child row bean
		// - 'Tail'
		Object[] beans = new Object[7];
		beans[0] = new TemplateBean("Test");
		beans[1] = new BeanTreeBean();
		final int NULL_BEAN_INDEX = 2;
		beans[NULL_BEAN_INDEX] = null; // NullBean
		beans[3] = null; // NoBean
		beans[4] = new TableRowBean("one", "Same", new PropertyBean("A"));
		beans[5] = new ChildTableRowBean("two", "Child", null); // no property
		beans[6] = null;
		NEXT_BEAN: for (int i = 0; i < beans.length; i++) {

			// Record obtaining the bean for appropriate sections
			Object bean = beans[i];
			if ((bean != null) || (i == NULL_BEAN_INDEX)) {
				this.recordReturn(functionContext, functionContext.getObject(2), bean);
				if (i == NULL_BEAN_INDEX) {
					continue NEXT_BEAN; // Null bean, no content
				}
			}

			// Obtain the remaining dependencies
			this.recordReturn(functionContext, functionContext.getObject(0), httpConnection);
			this.recordReturn(functionContext, functionContext.getObject(1), location);

			// Obtain the HTTP response
			this.recordReturn(httpConnection, httpConnection.getHttpResponse(), httpResponse);

			// Provide link recording
			switch (i) {
			case 3:
				// #{beans} of NullBean section
				String beansUriPath = "/uri-beans" + templateUriSuffix;
				this.recordReturn(location, location.transformToClientPath(beansUriPath, false), beansUriPath);
				break;
			case 5:
				// #{submit} of Tail section
				String submitUriPath = "/uri-submit" + templateUriSuffix;
				this.recordReturn(location, location.transformToClientPath(submitUriPath, true), submitUriPath);
				break;
			}
		}

		// Replay mocks
		this.replayMockObjects();

		// Execute the 'template' function
		this.doFunction("template", namespace, functionContext);

		// Execute the 'BeanTree' function
		this.doFunction("BeanTree", namespace, functionContext);

		// Execute the 'NullBean' function
		this.doFunction("NullBean", namespace, functionContext);

		// Execute the 'NoBean' function
		this.doFunction("NoBean", namespace, functionContext);

		// Execute the 'List' function (for table and its child)
		this.doFunction("List", namespace, functionContext); // table row bean
		this.doFunction("List", namespace, functionContext); // child row bean

		// Execute the 'Tail' task
		this.doFunction("Tail", namespace, functionContext);

		// Verify mocks
		this.verifyMockObjects();

		// Obtain the output template
		String actualOutput = UsAsciiUtil.convertToString(httpResponse.getEntityContent());

		// Expected output
		String expectedOutput = this.getFileContents(this.findFile(this.getClass(), "Template.expected"));
		expectedOutput = expectedOutput.replace("${URI_SUFFIX}", templateUriSuffix);

		// Validate output
		assertTextEquals("Incorrect output", expectedOutput, actualOutput);
	}

	/**
	 * Ensure able to use {@link ResourceSource} to load the
	 * {@link HttpTemplate}.
	 */
	@SuppressWarnings("rawtypes")
	public void testLoadWithResourceSource() throws Throwable {

		// Create the mock objects
		ManagedFunctionContext functionContext = this.createMock(ManagedFunctionContext.class);
		ServerHttpConnection httpConnection = this.createMock(ServerHttpConnection.class);
		MockHttpResponse httpResponse = new MockHttpResponse();
		HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

		// Template Content
		final String templateContent = "RAW TEMPLATE";
		final InputStream templateInput = new ByteArrayInputStream(templateContent.getBytes());

		// Configure the compiler the to load resources
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);

		// Register Loader to not provide content
		compiler.addResources(new ResourceSource() {
			@Override
			public InputStream sourceResource(String location) {
				return null; // not able to provide content
			}
		});

		// Register the Raw HTTP Template Loader
		compiler.addResources(new ResourceSource() {
			@Override
			public InputStream sourceResource(String location) {
				// Ensure appropriate template path
				assertEquals("Incorrect template path", HttpTemplateManagedFunctionSourceTest.this.templatePath,
						location);
				return templateInput;
			}
		});

		// Load the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				HttpTemplateManagedFunctionSource.class, compiler, this.getProperties(this.templatePath, "uri"));

		// Record undertaking task to use raw content
		this.recordReturn(functionContext, functionContext.getObject(2), new TemplateBean("TEST"));
		this.recordReturn(functionContext, functionContext.getObject(0), httpConnection);
		this.recordReturn(functionContext, functionContext.getObject(1), location);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(), httpResponse);

		// Test
		this.replayMockObjects();
		this.doFunction("template", namespaceType, functionContext);
		this.verifyMockObjects();

		// Ensure raw HTTP template content
		String output = UsAsciiUtil.convertToString(httpResponse.getEntityContent());
		assertTextEquals("Incorrect output", templateContent, output);
	}

	/**
	 * Tests the root template.
	 */
	@SuppressWarnings("rawtypes")
	public void testRootTemplate() throws Throwable {

		// Create the mock objects
		ManagedFunctionContext functionContext = this.createMock(ManagedFunctionContext.class);
		ServerHttpConnection httpConnection = this.createMock(ServerHttpConnection.class);
		HttpApplicationLocation location = this.createMock(HttpApplicationLocation.class);

		// Create the HTTP response to record output
		MockHttpResponse httpResponse = new MockHttpResponse();

		// Load the namespace type
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				HttpTemplateManagedFunctionSource.class, this.getProperties(this.rootTemplatePath, "/"));

		// Record
		this.recordReturn(functionContext, functionContext.getObject(2), new TemplateBean("TEST"));
		this.recordReturn(functionContext, functionContext.getObject(0), httpConnection);
		this.recordReturn(functionContext, functionContext.getObject(1), location);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(), httpResponse);
		this.recordReturn(location, location.transformToClientPath("/-something", false), "/-something");

		// Test
		this.replayMockObjects();

		// Execute the 'template' function
		this.doFunction("template", namespaceType, functionContext);

		// Verify mocks
		this.verifyMockObjects();

		// Obtain the output template
		String actualOutput = UsAsciiUtil.convertToString(httpResponse.getEntityContent());

		// Expected output (removing last end of line appended)
		String expectedOutput = this.getFileContents(this.findFile(this.getClass(), "RootTemplate.expected"));

		// Validate output
		assertTextEquals("Incorrect output", expectedOutput, actualOutput);
	}

	/**
	 * Ensure appropriately indicates if {@link HttpTemplateSection} requires a
	 * bean.
	 */
	public void testHttpTemplateSectionRequireBean() {

		// Ensure not require on no properties or beans
		HttpTemplateSection staticSection = new HttpTemplateSectionImpl("STATIC", "STATIC CONTENT#{LINK}",
				new HttpTemplateSectionContent[] { new StaticHttpTemplateSectionContentImpl("STATIC CONTENT"),
						new LinkHttpTemplateSectionContentImpl("LINK") });
		assertFalse("Should not require bean",
				HttpTemplateManagedFunctionSource.isHttpTemplateSectionRequireBean(staticSection));

		// Ensure require bean on property
		HttpTemplateSection propertySection = new HttpTemplateSectionImpl("PROPERTY", "${Property}",
				new HttpTemplateSectionContent[] { new PropertyHttpTemplateSectionContentImpl("Property") });
		assertTrue("Require bean if have property in section",
				HttpTemplateManagedFunctionSource.isHttpTemplateSectionRequireBean(propertySection));

		// Ensure require bean on bean
		HttpTemplateSection beanSection = new HttpTemplateSectionImpl("BEAN", "${Bean $}",
				new HttpTemplateSectionContent[] {
						new BeanHttpTemplateSectionContentImpl("Bean", new HttpTemplateSectionContent[0]) });
		assertTrue("Require bean if have bean in section",
				HttpTemplateManagedFunctionSource.isHttpTemplateSectionRequireBean(beanSection));
	}

	/**
	 * Ensure appropriately provides {@link HttpTemplate} URL continuation path.
	 */
	public void testHttpTemplateUrlContinuationPath() throws Exception {

		// Obtain without suffix
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		properties.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI,
				"configured/../non/../canoncial/../path");
		assertEquals("Incorrect path without suffix", "/path",
				HttpTemplateManagedFunctionSource.getHttpTemplateUrlContinuationPath(properties));

		// Obtain with suffix
		properties.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI_SUFFIX, ".suffix");
		assertEquals("Incorrect path with suffix", "/path.suffix",
				HttpTemplateManagedFunctionSource.getHttpTemplateUrlContinuationPath(properties));
	}

	/**
	 * Ensure appropriately provides the root {@link HttpTemplate} URL
	 * continuation path.
	 */
	public void testRootHttpTemplateUrlContinuationPath() throws Exception {

		// Obtain without suffix
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		properties.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI, "/");
		assertEquals("Incorrect path without suffix", "/",
				HttpTemplateManagedFunctionSource.getHttpTemplateUrlContinuationPath(properties));

		// Obtain with suffix (should not include suffix)
		properties.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI_SUFFIX, ".suffix");
		assertEquals("Incorrect path with suffix", "/",
				HttpTemplateManagedFunctionSource.getHttpTemplateUrlContinuationPath(properties));
	}

	/**
	 * Ensure appropriately provides the {@link HttpTemplate} link URL
	 * continuation path.
	 */
	public void testHttpTemplateLinkUrlContinuationPath() throws Exception {

		// Obtain without suffix
		assertEquals("Incorrect link path without suffix", "/path-link", HttpTemplateManagedFunctionSource
				.getHttpTemplateLinkUrlContinuationPath("configured/../non/../canonical/../path", "link", null));

		// Obtain with suffix
		assertEquals("Incorrect link path with suffix", "/path-link.suffix",
				HttpTemplateManagedFunctionSource.getHttpTemplateLinkUrlContinuationPath("/path", "link", ".suffix"));
	}

	/**
	 * Ensure appropriately indicates if secure.
	 */
	public void testHttpTemplateSecure() {

		// Ensure by default not secure
		SourcePropertiesImpl properties = new SourcePropertiesImpl();
		assertFalse("Should default to not be secure",
				HttpTemplateManagedFunctionSource.isHttpTemplateSecure(properties));

		// Ensure secure when configured secure
		properties.addProperty(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_SECURE, String.valueOf(true));
		assertTrue("Should be secure", HttpTemplateManagedFunctionSource.isHttpTemplateSecure(properties));
	}

	/**
	 * Ensure appropriately obtains links from {@link HttpTemplateSection} and
	 * any {@link BeanHttpTemplateSectionContent}.
	 */
	public void testHttpTemplateLinkNames() {

		// Section with no beans but has link
		HttpTemplateSection noBeanSection = new HttpTemplateSectionImpl("STATIC",
				"#{STATIC_LINK}STATIC CONTENT#{STATIC_LINK}",
				new HttpTemplateSectionContent[] { new LinkHttpTemplateSectionContentImpl("STATIC_LINK"),
						new StaticHttpTemplateSectionContentImpl("STATIC CONTENT"),
						new LinkHttpTemplateSectionContentImpl("STATIC_LINK") });

		// Section where link is within a bean
		HttpTemplateSection beanSection = new HttpTemplateSectionImpl("BEAN",
				"${STATIC_REPEAT $}STATIC CONTENT ${DYNAMIC_BEAN #{BEAN_LINK}STATIC_CONTENT${SUB_BEAN #{SUB_BEAN_LINK}$}$}",
				new HttpTemplateSectionContent[] {
						new BeanHttpTemplateSectionContentImpl("STATIC_REPEAT", noBeanSection.getContent()),
						new StaticHttpTemplateSectionContentImpl("STATIC CONTENT"),
						new BeanHttpTemplateSectionContentImpl("DYNAMIC_BEAN", new HttpTemplateSectionContent[] {
								new LinkHttpTemplateSectionContentImpl("BEAN_LINK"),
								new StaticHttpTemplateSectionContentImpl("STATIC CONTENT"),
								new BeanHttpTemplateSectionContentImpl("SUB_BEAN", new HttpTemplateSectionContent[] {
										new LinkHttpTemplateSectionContentImpl("SUB_BEAN_LINK") }) }) });

		// Obtain the links
		String[] links = HttpTemplateManagedFunctionSource.getHttpTemplateLinkNames(
				new HttpTemplateImpl(new HttpTemplateSection[] { noBeanSection, beanSection }));

		// Validate correct links
		assertEquals("Incorrect number of links", 3, links.length);
		assertEquals("Incorrect static section link", "STATIC_LINK", links[0]);
		assertEquals("Incorrect bean link", "BEAN_LINK", links[1]);
		assertEquals("Incorrect sub bean link", "SUB_BEAN_LINK", links[2]);
	}

	/**
	 * Does the {@link ManagedFunction} on the {@link FunctionNamespaceType}.
	 * 
	 * @param functionName
	 *            Name of {@link ManagedFunction} on
	 *            {@link FunctionNamespaceType} to execute.
	 * @param namespaceType
	 *            {@link FunctionNamespaceType}.
	 * @param functionContext
	 *            {@link ManagedFunctionContext}.
	 * @throws Throwable
	 *             If fails.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doFunction(String functionName, FunctionNamespaceType namespaceType,
			ManagedFunctionContext<?, ?> functionContext) throws Throwable {

		// Obtain the index of the function
		int functionIndex = -1;
		ManagedFunctionType<?, ?>[] functionTypes = namespaceType.getManagedFunctionTypes();
		for (int i = 0; i < functionTypes.length; i++) {
			if (functionName.equals(functionTypes[i].getFunctionName())) {
				functionIndex = i;
			}
		}
		if (functionIndex == -1) {
			fail("Could not find task '" + functionName + "'");
		}

		// Create the function
		ManagedFunction function = namespaceType.getManagedFunctionTypes()[functionIndex].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function
		function.execute(functionContext);
	}

	/**
	 * Obtain the properties.
	 * 
	 * @param templatePath
	 *            Path to the {@link HttpTemplate} file.
	 * @param templateUri
	 *            {@link HttpTemplate} URI path.
	 * @param additionalPropertyNameValuePairs
	 *            Additional {@link Property} name value pairs.
	 * @return Properties.
	 */
	public String[] getProperties(String templatePath, String templateUri, String... additionalPropertyNameValuePairs) {

		// Create the properties
		List<String> properties = new LinkedList<String>();

		// Provide the template details
		properties.addAll(Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_FILE, templatePath));
		properties.addAll(Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_TEMPLATE_URI, templateUri));

		// Provide the beans
		properties.addAll(Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "template",
				TemplateBean.class.getName()));
		properties.addAll(Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "BeanTree",
				BeanTreeBean.class.getName()));
		properties.addAll(Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "NullBean",
				Object.class.getName()));
		properties.addAll(Arrays.asList(HttpTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "List",
				TableRowBean.class.getName()));

		// Provide the additional property values
		for (int i = 0; i < additionalPropertyNameValuePairs.length; i += 2) {
			String name = additionalPropertyNameValuePairs[i];
			String value = additionalPropertyNameValuePairs[i + 1];
			properties.addAll(Arrays.asList(name, value));
		}

		// Return the properties
		return properties.toArray(new String[properties.size()]);
	}

}