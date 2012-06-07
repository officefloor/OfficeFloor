/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.UsAsciiUtil;
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

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link HttpTemplateWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Path to template for missing items to check errors.
	 */
	private final String missingTemplateFilePath = this.getClass().getPackage()
			.getName().replace('.', '/')
			+ "/TemplateMissing.ofp";

	/**
	 * Template path.
	 */
	private final String templatePath = this.getClass().getPackage().getName()
			.replace('.', '/')
			+ "/Template.ofp";

	/**
	 * Root template path.
	 */
	private final String rootTemplatePath = this.getClass().getPackage()
			.getName().replace('.', '/')
			+ "/RootTemplate.ofp";

	/**
	 * Verifies the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpTemplateWorkSource.class,
				HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE, "template");
	}

	/**
	 * Validate type.
	 */
	public void testType() throws Exception {

		// Create the expected work
		HttpTemplateWork workFactory = new HttpTemplateWork();
		WorkTypeBuilder<HttpTemplateWork> work = WorkLoaderUtil
				.createWorkTypeBuilder(workFactory);

		// Create the task factory
		HttpTemplateTask httpTemplateTaskFactory = new HttpTemplateTask(null,
				false, null);
		RequestHandlerTask requestHandlerTaskFactory = new RequestHandlerTask();

		// 'template' task
		TaskTypeBuilder<Indexed, None> template = work.addTaskType("template",
				httpTemplateTaskFactory, Indexed.class, None.class);
		template.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		template.addObject(TemplateBean.class).setLabel("OBJECT");
		template.addEscalation(IOException.class);

		// 'BeanTree' task
		TaskTypeBuilder<Indexed, None> beanTree = work.addTaskType("BeanTree",
				httpTemplateTaskFactory, Indexed.class, None.class);
		beanTree.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		beanTree.addObject(BeanTreeBean.class).setLabel("OBJECT");
		beanTree.addEscalation(IOException.class);

		// 'NullBean' task
		TaskTypeBuilder<Indexed, None> nullBean = work.addTaskType("NullBean",
				httpTemplateTaskFactory, Indexed.class, None.class);
		nullBean.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		nullBean.addObject(TemplateBean.class).setLabel("OBJECT");
		nullBean.addEscalation(IOException.class);

		// 'List' task
		TaskTypeBuilder<Indexed, None> list = work.addTaskType("List",
				httpTemplateTaskFactory, Indexed.class, None.class);
		list.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		list.addObject(TableRowBean.class).setLabel("OBJECT");
		list.addEscalation(IOException.class);

		// 'Tail' task
		TaskTypeBuilder<Indexed, None> tail = work.addTaskType("Tail",
				httpTemplateTaskFactory, Indexed.class, None.class);
		tail.addObject(ServerHttpConnection.class).setLabel(
				"SERVER_HTTP_CONNECTION");
		tail.addEscalation(IOException.class);

		// Handle link 'beans' task
		TaskTypeBuilder<Indexed, None> beans = work.addTaskType("beans",
				requestHandlerTaskFactory, Indexed.class, None.class);
		beans.setDifferentiator(new HttpTemplateRequestHandlerDifferentiator());

		// Handle link 'submit' task
		TaskTypeBuilder<Indexed, None> submit = work.addTaskType("submit",
				requestHandlerTaskFactory, Indexed.class, None.class);
		submit.setDifferentiator(new HttpTemplateRequestHandlerDifferentiator());

		// Verify the work type
		WorkLoaderUtil.validateWorkType(work, HttpTemplateWorkSource.class,
				this.getProperties(this.templatePath));
	}

	/**
	 * Ensure indicates failure on missing bean for {@link HttpTemplateSection}.
	 */
	public void testMissingBean() {

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Record failure due to missing bean
		issues.addIssue(LocationType.SECTION, null, AssetType.WORK, null,
				"Missing property 'bean.template' for WorkSource "
						+ HttpTemplateWorkSource.class.getName());

		// Create and initiate the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Do not provide bean for template section
		PropertyList propertyList = OfficeFloorCompiler.newPropertyList();
		propertyList.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE)
				.setValue(this.missingTemplateFilePath);

		// Test loading ensuring indicates failure
		this.replayMockObjects();
		compiler.getWorkLoader().loadWorkType(HttpTemplateWorkSource.class,
				propertyList);
		this.verifyMockObjects();
	}

	/**
	 * Ensure indicates failure on missing property in the
	 * {@link HttpTemplateSection}.
	 */
	public void testMissingProperty() {
		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Record failure due to missing bean
		final String message = "Property 'MissingProperty' can not be sourced from bean type "
				+ Object.class.getName();
		issues.addIssue(LocationType.SECTION, null, AssetType.WORK, null,
				"Failed to source WorkType definition from WorkSource "
						+ HttpTemplateWorkSource.class.getName(),
				new Exception(message));
		this.control(issues).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect location type", expected[0], actual[0]);
				assertEquals("Incorrect location", expected[1], actual[1]);
				assertEquals("Incorrect asset type", expected[2], actual[2]);
				assertEquals("Incorrect asset", expected[3], actual[3]);
				assertEquals("Incorrect description", expected[4], actual[4]);
				Exception cause = (Exception) actual[5];
				assertEquals("Incorrect cause message", message,
						cause.getMessage());
				return true;
			}
		});

		// Create and initiate the compiler
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Provide bean that does not have the required property
		PropertyList propertyList = OfficeFloorCompiler.newPropertyList();
		propertyList.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE)
				.setValue(this.missingTemplateFilePath);
		propertyList.addProperty("bean.template").setValue(
				Object.class.getName());

		// Test loading ensuring indicates failure
		this.replayMockObjects();
		compiler.getWorkLoader().loadWorkType(HttpTemplateWorkSource.class,
				propertyList);
		this.verifyMockObjects();
	}

	/**
	 * Tests running the template to generate response.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testTemplate() throws Throwable {

		// Create the mock objects
		TaskContext taskContext = this.createMock(TaskContext.class);
		ServerHttpConnection httpConnection = this
				.createMock(ServerHttpConnection.class);

		// Create the HTTP response to record output
		MockHttpResponse httpResponse = new MockHttpResponse();

		// Load the work type
		WorkType<HttpTemplateWork> workType = WorkLoaderUtil.loadWorkType(
				HttpTemplateWorkSource.class,
				this.getProperties(this.templatePath));

		// Create the work and provide name
		HttpTemplateWork work = workType.getWorkFactory().createWork();
		work.setBoundWorkName("WORK");

		// Record actions for each task:
		// - 'template'
		// - 'NullBean'
		// - 'List' task with table row bean
		// - 'List' task with child row bean
		// - 'Tail'
		Object[] beans = new Object[5];
		beans[0] = new TemplateBean("Test");
		final int NULL_BEAN_INDEX = 1;
		beans[NULL_BEAN_INDEX] = null; // 'NullBean' template
		beans[2] = new TableRowBean("one", "Same", new PropertyBean("A"));
		beans[3] = new ChildTableRowBean("two", "Child", null); // no property
		beans[4] = null;
		for (int i = 0; i < beans.length; i++) {
			this.recordReturn(taskContext, taskContext.getObject(0),
					httpConnection);
			this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
					httpResponse);
			if ((beans[i] != null) || (i == NULL_BEAN_INDEX)) {
				this.recordReturn(taskContext, taskContext.getObject(1),
						beans[i]);
			}
			this.recordReturn(taskContext, taskContext.getWork(), work);
		}

		// Replay mocks
		this.replayMockObjects();

		// Execute the 'template' task
		this.doTask("template", work, workType, taskContext);

		// Execute the 'NullBean' task
		this.doTask("NullBean", work, workType, taskContext);

		// Execute the 'List' task (for table and its child)
		this.doTask("List", work, workType, taskContext); // table row bean
		this.doTask("List", work, workType, taskContext); // child row bean

		// Execute the 'Tail' task
		this.doTask("Tail", work, workType, taskContext);

		// Verify mocks
		this.verifyMockObjects();

		// Obtain the output template
		String actualOutput = UsAsciiUtil.convertToString(httpResponse
				.getBodyContent());

		// Expected output (removing last end of line appended)
		String expectedOutput = this.getFileContents(this.findFile(
				this.getClass(), "Template.expected"));

		// Validate output
		assertTextEquals("Incorrect output", expectedOutput, actualOutput);
	}

	/**
	 * Ensure able to use {@link ResourceSource} to load the
	 * {@link HttpTemplate}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testLoadWithResourceSource() throws Throwable {

		// Create the mock objects
		TaskContext taskContext = this.createMock(TaskContext.class);
		ServerHttpConnection httpConnection = this
				.createMock(ServerHttpConnection.class);
		MockHttpResponse httpResponse = new MockHttpResponse();

		// Template Content
		final String templateContent = "RAW TEMPLATE";
		final InputStream templateInput = new ByteArrayInputStream(
				templateContent.getBytes());

		// Configure the compiler the to load resources
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);

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
				assertEquals("Incorrect template path",
						HttpTemplateWorkSourceTest.this.templatePath, location);
				return templateInput;
			}
		});

		// Load the work type
		WorkType<HttpTemplateWork> workType = WorkLoaderUtil.loadWorkType(
				HttpTemplateWorkSource.class, compiler,
				this.getProperties(this.templatePath));

		// Create the work and provide name
		HttpTemplateWork work = workType.getWorkFactory().createWork();
		work.setBoundWorkName("WORK");

		// Record undertaking task to use raw content
		this.recordReturn(taskContext, taskContext.getObject(0), httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);
		this.recordReturn(taskContext, taskContext.getWork(), work);

		// Test
		this.replayMockObjects();
		this.doTask("template", work, workType, taskContext);
		this.verifyMockObjects();

		// Ensure raw HTTP template content
		String output = UsAsciiUtil.convertToString(httpResponse
				.getBodyContent());
		assertTextEquals("Incorrect output", templateContent, output);
	}

	/**
	 * Tests the root template.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testRootTemplate() throws Throwable {

		// Create the mock objects
		TaskContext taskContext = this.createMock(TaskContext.class);
		ServerHttpConnection httpConnection = this
				.createMock(ServerHttpConnection.class);

		// Create the HTTP response to record output
		MockHttpResponse httpResponse = new MockHttpResponse();

		// Load the work type
		WorkType<HttpTemplateWork> workType = WorkLoaderUtil.loadWorkType(
				HttpTemplateWorkSource.class,
				this.getProperties(this.rootTemplatePath));

		// Create as root template
		HttpTemplateWork work = workType.getWorkFactory().createWork();
		work.setBoundWorkName("/.links");

		// Record
		this.recordReturn(taskContext, taskContext.getObject(0), httpConnection);
		this.recordReturn(httpConnection, httpConnection.getHttpResponse(),
				httpResponse);
		this.recordReturn(taskContext, taskContext.getWork(), work);

		// Test
		this.replayMockObjects();

		// Execute the 'template' task
		this.doTask("template", work, workType, taskContext);

		// Verify mocks
		this.verifyMockObjects();

		// Obtain the output template
		String actualOutput = UsAsciiUtil.convertToString(httpResponse
				.getBodyContent());

		// Expected output (removing last end of line appended)
		String expectedOutput = this.getFileContents(this.findFile(
				this.getClass(), "RootTemplate.expected"));

		// Validate output
		assertTextEquals("Incorrect output", expectedOutput, actualOutput);
	}

	/**
	 * Ensure appropriately indicates if {@link HttpTemplateSection} requires a
	 * bean.
	 */
	public void testHttpTemplateSectionRequireBean() {

		// Ensure not require on no properties or beans
		HttpTemplateSection staticSection = new HttpTemplateSectionImpl(
				"STATIC", new HttpTemplateSectionContent[] {
						new StaticHttpTemplateSectionContentImpl(
								"STATIC CONTENT"),
						new LinkHttpTemplateSectionContentImpl("LINK") });
		assertFalse("Should not require bean",
				HttpTemplateWorkSource
						.isHttpTemplateSectionRequireBean(staticSection));

		// Ensure require bean on property
		HttpTemplateSection propertySection = new HttpTemplateSectionImpl(
				"PROPERTY",
				new HttpTemplateSectionContent[] { new PropertyHttpTemplateSectionContentImpl(
						"Property") });
		assertTrue("Require bean if have property in section",
				HttpTemplateWorkSource
						.isHttpTemplateSectionRequireBean(propertySection));

		// Ensure require bean on bean
		HttpTemplateSection beanSection = new HttpTemplateSectionImpl(
				"BEAN",
				new HttpTemplateSectionContent[] { new BeanHttpTemplateSectionContentImpl(
						"Bean", new HttpTemplateSectionContent[0]) });
		assertTrue("Require bean if have bean in section",
				HttpTemplateWorkSource
						.isHttpTemplateSectionRequireBean(beanSection));
	}

	/**
	 * Ensure appropriately obtains links from {@link HttpTemplateSection} and
	 * any {@link BeanHttpTemplateSectionContent}.
	 */
	public void testHttpTemplateLinkNames() {

		// Section with no beans but has link
		HttpTemplateSection noBeanSection = new HttpTemplateSectionImpl(
				"STATIC", new HttpTemplateSectionContent[] {
						new LinkHttpTemplateSectionContentImpl("STATIC_LINK"),
						new StaticHttpTemplateSectionContentImpl(
								"STATIC CONTENT"),
						new LinkHttpTemplateSectionContentImpl("STATIC_LINK") });

		// Section where link is within a bean
		HttpTemplateSection beanSection = new HttpTemplateSectionImpl(
				"BEAN",
				new HttpTemplateSectionContent[] {
						new BeanHttpTemplateSectionContentImpl("STATIC_REPEAT",
								noBeanSection.getContent()),
						new StaticHttpTemplateSectionContentImpl(
								"STATIC CONTENT"),
						new BeanHttpTemplateSectionContentImpl(
								"DYNAMIC_BEAN",
								new HttpTemplateSectionContent[] {
										new LinkHttpTemplateSectionContentImpl(
												"BEAN_LINK"),
										new StaticHttpTemplateSectionContentImpl(
												"STATIC CONTENT"),
										new BeanHttpTemplateSectionContentImpl(
												"SUB_BEAN",
												new HttpTemplateSectionContent[] { new LinkHttpTemplateSectionContentImpl(
														"SUB_BEAN_LINK") }) }) });

		// Obtain the links
		String[] links = HttpTemplateWorkSource
				.getHttpTemplateLinkNames(new HttpTemplateImpl(
						new HttpTemplateSection[] { noBeanSection, beanSection }));

		// Validate correct links
		assertEquals("Incorrect number of links", 3, links.length);
		assertEquals("Incorrect static section link", "STATIC_LINK", links[0]);
		assertEquals("Incorrect bean link", "BEAN_LINK", links[1]);
		assertEquals("Incorrect sub bean link", "SUB_BEAN_LINK", links[2]);
	}

	/**
	 * Does the {@link Task} on the {@link WorkType}.
	 * 
	 * @param taskName
	 *            Name of {@link Task} on {@link WorkType} to execute.
	 * @param work
	 *            {@link HttpTemplateWork}.
	 * @param workType
	 *            {@link WorkType}.
	 * @param taskContext
	 *            {@link TaskContext}.
	 * @throws Throwable
	 *             If fails.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doTask(String taskName, HttpTemplateWork work,
			WorkType<HttpTemplateWork> workType,
			TaskContext<HttpTemplateWork, ?, ?> taskContext) throws Throwable {

		// Obtain the index of the task
		int taskIndex = -1;
		TaskType<?, ?, ?>[] taskTypes = workType.getTaskTypes();
		for (int i = 0; i < taskTypes.length; i++) {
			if (taskName.equals(taskTypes[i].getTaskName())) {
				taskIndex = i;
			}
		}
		if (taskIndex == -1) {
			fail("Could not find task '" + taskName + "'");
		}

		// Create the task
		Task task = workType.getTaskTypes()[taskIndex].getTaskFactory()
				.createTask(work);

		// Execute the task
		task.doTask(taskContext);
	}

	/**
	 * Obtain the properties.
	 * 
	 * @return Properties.
	 */
	public String[] getProperties(String templatePath) {
		// Create the properties
		String[] properties = new String[10];
		properties[0] = HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE;
		properties[1] = templatePath;
		properties[2] = HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
				+ "template";
		properties[3] = TemplateBean.class.getName();
		properties[4] = HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
				+ "NullBean";
		properties[5] = TemplateBean.class.getName();
		properties[6] = HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX + "List";
		properties[7] = TableRowBean.class.getName();
		properties[8] = HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX
				+ "BeanTree";
		properties[9] = BeanTreeBean.class.getName();

		// Return the properties
		return properties;
	}
}