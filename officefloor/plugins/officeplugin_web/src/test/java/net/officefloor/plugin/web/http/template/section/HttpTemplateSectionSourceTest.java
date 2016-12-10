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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.SectionClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.SectionClassWorkSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationWorkSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource.NoLogicClass;
import net.officefloor.plugin.web.http.template.section.TemplateLogic.RowBean;
import net.officefloor.plugin.work.clazz.ClassWorkSource;

/**
 * Tests the {@link HttpTemplateSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(HttpTemplateSectionSource.class,
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "URI Path");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();

		// Inputs (for HTTP Template rending)
		expected.addSectionInput("renderTemplate", null);

		// Inputs (for Template Logic methods - enables reuse of class)
		expected.addSectionInput("getTemplate", null);
		expected.addSectionInput("getTemplateName", null);
		expected.addSectionInput("getEscapedHtml", null);
		expected.addSectionInput("getUnescapedHtml", null);
		expected.addSectionInput("getNullBean", null);
		expected.addSectionInput("getBean", null);
		expected.addSectionInput("getBeanProperty", null);
		expected.addSectionInput("getBeanArray", null);
		expected.addSectionInput("getList", null);
		expected.addSectionInput("nextTask", null);
		expected.addSectionInput("submit", null);
		expected.addSectionInput("doInternalFlow", Integer.class.getName());
		expected.addSectionInput("notRenderTemplateAfter", null);

		// Outputs
		expected.addSectionOutput("doExternalFlow", String.class.getName(), false);
		expected.addSectionOutput("nonMethodLink", null, false);
		expected.addSectionOutput("output", null, false);
		expected.addSectionOutput(SQLException.class.getName(), SQLException.class.getName(), true);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		SectionObject connection = expected.addSectionObject(Connection.class.getName(), Connection.class.getName());
		SectionObject httpConnection = expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());
		SectionObject applicationLocation = expected.addSectionObject(HttpApplicationLocation.class.getName(),
				HttpApplicationLocation.class.getName());
		SectionObject requestState = expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		SectionObject httpSession = expected.addSectionObject(HttpSession.class.getName(), HttpSession.class.getName());

		// Managed Object Sources
		SectionManagedObjectSource sectionMos = expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName());
		sectionMos.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, TemplateLogic.class.getName());
		SectionManagedObject sectionMo = sectionMos.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);
		SectionManagedObjectSource injectMos = expected.addSectionManagedObjectSource("managedObject",
				ClassManagedObjectSource.class.getName());
		injectMos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, RowBean.class.getName());
		injectMos.addSectionManagedObject("managedObject", ManagedObjectScope.THREAD);

		// Initial, Template and Class work
		SectionWork initialWork = expected.addSectionWork("INITIAL", HttpTemplateInitialWorkSource.class.getName());
		initialWork.addProperty(HttpTemplateInitialWorkSource.PROPERTY_TEMPLATE_URI, "template");
		SectionWork templateWork = expected.addSectionWork("TEMPLATE", HttpTemplateWorkSource.class.getName());
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "Template.ofp"));
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI, "template");
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX + "template",
				TemplateLogic.class.getName());
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX + "List", RowBean.class.getName());
		SectionWork classWork = expected.addSectionWork("WORK", SectionClassWorkSource.class.getName());
		classWork.addProperty(ClassWorkSource.CLASS_NAME_PROPERTY_NAME, TemplateLogic.class.getName());
		SectionWork iteratorWork = expected.addSectionWork("ListArrayIterator",
				HttpTemplateArrayIteratorWorkSource.class.getName());
		iteratorWork.addProperty(HttpTemplateArrayIteratorWorkSource.PROPERTY_COMPONENT_TYPE_NAME,
				RowBean.class.getName());

		// Initial task
		SectionTask initial = initialWork.addSectionTask("_INITIAL_TASK_", HttpTemplateInitialWorkSource.TASK_NAME);
		expected.link(initial.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(initial.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);
		expected.link(initial.getTaskObject("REQUEST_STATE"), requestState);
		expected.link(initial.getTaskObject("HTTP_SESSION"), httpSession);
		initial.getTaskFlow("RENDER");

		// Template
		SectionTask getTemplate = classWork.addSectionTask("getTemplate", "getTemplate");
		expected.link(getTemplate.getTaskObject("OBJECT"), sectionMo);
		SectionTask template = templateWork.addSectionTask("template", "template");
		expected.link(template.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(template.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);
		expected.link(template.getTaskObject("OBJECT"), sectionMo);

		// Methods for beans/properties
		for (String beanMethodName : new String[] { "getTemplateName", "getEscapedHtml", "getUnescapedHtml",
				"getNullBean", "getBean", "getBeanProperty", "getBeanArray" }) {
			SectionTask beanMethodTask = classWork.addSectionTask(beanMethodName, beanMethodName);
			expected.link(beanMethodTask.getTaskObject("OBJECT"), sectionMo);
		}

		// List
		SectionTask getList = classWork.addSectionTask("getList", "getList");
		expected.link(getList.getTaskObject("OBJECT"), sectionMo);
		expected.link(getList.getTaskObject(HttpSession.class.getName()), httpSession);
		SectionTask list = templateWork.addSectionTask("List", "List");
		expected.link(list.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(list.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);
		expected.link(list.getTaskObject("OBJECT"), sectionMo);
		SectionTask listArrayIterator = iteratorWork.addSectionTask("ListArrayIterator", "iterate");
		listArrayIterator.getTaskObject("ARRAY").flagAsParameter();

		// Tail
		SectionTask tail = templateWork.addSectionTask("Tail", "Tail");
		expected.link(tail.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(tail.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);

		// nextTask link URL continuation
		SectionWork nextTaskContinuationWork = expected.addSectionWork("HTTP_URL_CONTINUATION_nextTask",
				HttpUrlContinuationWorkSource.class.getName());
		nextTaskContinuationWork.addProperty(HttpUrlContinuationWorkSource.PROPERTY_URI_PATH, "/next");
		nextTaskContinuationWork.addSectionTask("HTTP_URL_CONTINUATION_nextTask",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Handle nextTask task
		SectionTask nextTaskMethod = classWork.addSectionTask("nextTask", "nextTask");
		expected.link(nextTaskMethod.getTaskObject("OBJECT"), sectionMo);
		expected.link(nextTaskMethod.getTaskObject(ServerHttpConnection.class.getName()), httpConnection);

		// submit link URL continuation
		SectionWork submitContinuationWork = expected.addSectionWork("HTTP_URL_CONTINUATION_submit",
				HttpUrlContinuationWorkSource.class.getName());
		submitContinuationWork.addProperty(HttpUrlContinuationWorkSource.PROPERTY_URI_PATH, "/submit");
		submitContinuationWork.addSectionTask("HTTP_URL_CONTINUATION_submit", HttpUrlContinuationWorkSource.TASK_NAME);

		// Handle submit task
		SectionTask submitMethod = classWork.addSectionTask("submit", "submit");
		expected.link(submitMethod.getTaskObject("OBJECT"), sectionMo);
		expected.link(submitMethod.getTaskObject(ServerHttpConnection.class.getName()), httpConnection);

		// Route non-method link
		SectionWork nonMethodContinuationWork = expected.addSectionWork("HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.class.getName());
		nonMethodContinuationWork.addProperty(HttpUrlContinuationWorkSource.PROPERTY_URI_PATH, "/nonMethod");
		nonMethodContinuationWork.addSectionTask("HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Extra task
		SectionTask doInternalFlow = classWork.addSectionTask("doInternalFlow", "doInternalFlow");
		expected.link(doInternalFlow.getTaskObject("OBJECT"), sectionMo);
		doInternalFlow.getTaskObject(Integer.class.getName()).flagAsParameter();
		expected.link(doInternalFlow.getTaskObject(Connection.class.getName()), connection);
		expected.link(doInternalFlow.getTaskObject(ServerHttpConnection.class.getName()), httpConnection);

		// Not render template after link
		SectionWork notRenderTemplateAfterWork = expected.addSectionWork("HTTP_URL_CONTINUATION_notRenderTemplateAfter",
				HttpUrlContinuationWorkSource.class.getName());
		notRenderTemplateAfterWork.addProperty(HttpUrlContinuationWorkSource.PROPERTY_URI_PATH, "/notRender");
		notRenderTemplateAfterWork.addSectionTask("HTTP_URL_CONTINUATION_notRenderTemplateAfter",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Handle not render template after task
		SectionTask notRenderTemplateAfterMethod = classWork.addSectionTask("notRenderTemplateAfter",
				"notRenderTemplateAfter");
		expected.link(notRenderTemplateAfterMethod.getTaskObject("OBJECT"), sectionMo);
		expected.link(notRenderTemplateAfterMethod.getTaskObject(ServerHttpConnection.class.getName()), httpConnection);

		// Validate type
		SectionLoaderUtil.validateSection(expected, HttpTemplateSectionSource.class, this.getClass(), "Template.ofp",
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME, TemplateLogic.class.getName(),
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");
	}

	/**
	 * Ensure find methods with Data suffix.
	 */
	public void testTypeWithDataSuffix() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();

		// Inputs (for HTTP Template rending)
		expected.addSectionInput("renderTemplate", null);

		// Inputs (for Template Logic methods - enables reuse of class)
		expected.addSectionInput("getTemplateData", null);
		expected.addSectionInput("getMessage", null);
		expected.addSectionInput("getSectionData", null);
		expected.addSectionInput("getDescription", null);
		expected.addSectionInput("requiredForIntegration", null);

		// Outputs
		expected.addSectionOutput("output", null, false);
		expected.addSectionOutput("doExternalFlow", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		SectionObject httpConnection = expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());
		SectionObject applicationLocation = expected.addSectionObject(HttpApplicationLocation.class.getName(),
				HttpApplicationLocation.class.getName());
		SectionObject requestState = expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		SectionObject httpSession = expected.addSectionObject(HttpSession.class.getName(), HttpSession.class.getName());

		// Managed Object Sources
		SectionManagedObjectSource sectionMos = expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName());
		sectionMos.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				TemplateDataLogic.class.getName());
		SectionManagedObject sectionMo = sectionMos.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);

		// Initial, Template and Class work
		SectionWork initialWork = expected.addSectionWork("INITIAL", HttpTemplateInitialWorkSource.class.getName());
		initialWork.addProperty(HttpTemplateInitialWorkSource.PROPERTY_TEMPLATE_URI, "template");
		SectionWork templateWork = expected.addSectionWork("TEMPLATE", HttpTemplateWorkSource.class.getName());
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "TemplateData.ofp"));
		templateWork.addProperty(HttpTemplateInitialWorkSource.PROPERTY_TEMPLATE_URI, "template");
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX + "template",
				TemplateDataLogic.class.getName());
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_BEAN_PREFIX + "section",
				TemplateDataLogic.class.getName());
		SectionWork classWork = expected.addSectionWork("WORK", SectionClassWorkSource.class.getName());
		classWork.addProperty(SectionClassWorkSource.CLASS_NAME_PROPERTY_NAME, TemplateDataLogic.class.getName());

		// Initial task
		SectionTask initial = initialWork.addSectionTask("_INITIAL_TASK_", HttpTemplateInitialWorkSource.TASK_NAME);
		expected.link(initial.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(initial.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);
		expected.link(initial.getTaskObject("REQUEST_STATE"), requestState);
		expected.link(initial.getTaskObject("HTTP_SESSION"), httpSession);
		initial.getTaskFlow("RENDER");

		// Template
		SectionTask getTemplate = classWork.addSectionTask("getTemplateData", "getTemplateData");
		expected.link(getTemplate.getTaskObject("OBJECT"), sectionMo);
		SectionTask template = templateWork.addSectionTask("template", "template");
		expected.link(template.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(template.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);
		expected.link(template.getTaskObject("OBJECT"), sectionMo);

		// Message
		SectionTask message = classWork.addSectionTask("getMessage", "getMessage");
		expected.link(message.getTaskObject("OBJECT"), sectionMo);

		// Section
		SectionTask getSection = classWork.addSectionTask("getSectionData", "getSectionData");
		expected.link(getSection.getTaskObject("OBJECT"), sectionMo);
		SectionTask section = templateWork.addSectionTask("section", "section");
		expected.link(section.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(section.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);
		initial.getTaskObject("REQUEST_STATE");
		expected.link(section.getTaskObject("OBJECT"), sectionMo);

		// Description
		SectionTask description = classWork.addSectionTask("getDescription", "getDescription");
		expected.link(description.getTaskObject("OBJECT"), sectionMo);

		// External flow
		SectionTask doExternalFlow = classWork.addSectionTask("requiredForIntegration", "requiredForIntegration");
		expected.link(doExternalFlow.getTaskObject("OBJECT"), sectionMo);

		// Validate type
		SectionLoaderUtil.validateSection(expected, HttpTemplateSectionSource.class, this.getClass(),
				"TemplateData.ofp", HttpTemplateSectionSource.PROPERTY_CLASS_NAME, TemplateDataLogic.class.getName(),
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");
	}

	/**
	 * Ensure can use {@link HttpTemplateSectionSource} without a logic class.
	 */
	public void testTypeWithNoLogicClass() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();

		// Input (for HTTP Template rending)
		expected.addSectionInput("renderTemplate", null);

		// Outputs
		expected.addSectionOutput("output", null, false);
		expected.addSectionOutput("nonMethodLink", null, false);
		expected.addSectionOutput("doExternalFlow", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		SectionObject httpConnection = expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());
		SectionObject applicationLocation = expected.addSectionObject(HttpApplicationLocation.class.getName(),
				HttpApplicationLocation.class.getName());
		SectionObject requestState = expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		SectionObject httpSession = expected.addSectionObject(HttpSession.class.getName(), HttpSession.class.getName());

		// Managed Object Sources
		SectionManagedObjectSource sectionMos = expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName());
		sectionMos.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, NoLogicClass.class.getName());
		SectionManagedObject sectionMo = sectionMos.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);

		// Add the no logic class (with internal task)
		SectionWork classWork = expected.addSectionWork("WORK", SectionClassWorkSource.class.getName());
		classWork.addProperty(SectionClassWorkSource.CLASS_NAME_PROPERTY_NAME, NoLogicClass.class.getName());
		SectionTask getTemplate = classWork.addSectionTask("notIncludedInput", "notIncludedInput");
		expected.link(getTemplate.getTaskObject("OBJECT"), sectionMo);

		// Initial and Template work
		SectionWork initialWork = expected.addSectionWork("INITIAL", HttpTemplateInitialWorkSource.class.getName());
		initialWork.addProperty(HttpTemplateInitialWorkSource.PROPERTY_TEMPLATE_URI, "template");
		SectionWork templateWork = expected.addSectionWork("TEMPLATE", HttpTemplateWorkSource.class.getName());
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_FILE,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "NoLogicTemplate.ofp"));
		templateWork.addProperty(HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI, "template");

		// Initial task
		SectionTask initial = initialWork.addSectionTask("_INITIAL_TASK_", HttpTemplateInitialWorkSource.TASK_NAME);
		expected.link(initial.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(initial.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);
		expected.link(initial.getTaskObject("REQUEST_STATE"), requestState);
		expected.link(initial.getTaskObject("HTTP_SESSION"), httpSession);
		initial.getTaskFlow("RENDER");

		// Section
		SectionTask section = templateWork.addSectionTask("Section", "Section");
		expected.link(section.getTaskObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(section.getTaskObject("HTTP_APPLICATION_LOCATION"), applicationLocation);

		// nonMethodLink URL continuation
		SectionWork nonMethodContinuationWork = expected.addSectionWork("HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.class.getName());
		nonMethodContinuationWork.addProperty(HttpUrlContinuationWorkSource.PROPERTY_URI_PATH, "nonMethod");
		nonMethodContinuationWork.addSectionTask("HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// doExternalFlow URL continuation
		SectionWork doExternalFlowContinuationWork = expected.addSectionWork("HTTP_URL_CONTINUATION_doExternalFlow",
				HttpUrlContinuationWorkSource.class.getName());
		doExternalFlowContinuationWork.addProperty(HttpUrlContinuationWorkSource.PROPERTY_URI_PATH, "doExternalFlow");
		doExternalFlowContinuationWork.addSectionTask("HTTP_URL_CONTINUATION_doExternalFlow",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Validate type
		SectionLoaderUtil.validateSection(expected, HttpTemplateSectionSource.class, this.getClass(),
				"NoLogicTemplate.ofp", HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");
	}

	/**
	 * Ensure that all links do exist.
	 */
	public void testUnknownLinkSecured() {

		MockCompilerIssues issues = new MockCompilerIssues(this);

		// Obtain the template location
		String templatePath = this.getPackageRelativePath(this.getClass()) + "/NotExistLinkTemplate.ofp";

		// Record loading the work type
		issues.recordCaptureIssues(false);

		// Record errors
		issues.recordIssue("Type", SectionNodeImpl.class, "Link 'LINK' does not exist on template /uri");

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI).setValue("/uri");
		properties.addProperty(HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX + "LINK")
				.setValue(String.valueOf(true));

		// Test
		this.replayMockObjects();
		loader.loadSectionType(HttpTemplateSectionSource.class, templatePath, properties);
		this.verifyMockObjects();
	}

	/**
	 * Ensure that parent links (no longer in inherited template) are considered
	 * in all links. This is to allow the link secure information to be
	 * inherited.
	 */
	public void testLinkKnownInParent() {

		// Obtain the template locations
		String parentTemplatePath = this.getFileLocation(this.getClass(), "LinkInParent.ofp");
		String childTemplatePath = this.getFileLocation(this.getClass(), "LinkNotInChild.ofp");

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();

		// Input (for HTTP Template rending)
		expected.addSectionInput("renderTemplate", null);

		// Outputs
		expected.addSectionOutput("output", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		expected.addSectionObject(ServerHttpConnection.class.getName(), ServerHttpConnection.class.getName());
		expected.addSectionObject(HttpApplicationLocation.class.getName(), HttpApplicationLocation.class.getName());
		expected.addSectionObject(HttpRequestState.class.getName(), HttpRequestState.class.getName());
		expected.addSectionObject(HttpSession.class.getName(), HttpSession.class.getName());

		// Ensure correct type (and no link secure unknown issue)
		SectionLoaderUtil.validateSectionType(expected, HttpTemplateSectionSource.class, childTemplatePath,
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri",
				HttpTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES, parentTemplatePath,
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX + "link", String.valueOf(true));
	}

	/**
	 * Ensure have section method if section requires bean.
	 */
	public void testMissingSectionMethodOnTemplateLogic() {
		this.doMissingSectionMethodTest(MissingSectionTemplateLogic.class, "Missing method 'getsection' on class "
				+ MissingSectionTemplateLogic.class.getName() + " to provide bean for template uri");
	}

	/**
	 * Template logic with missing section data method.
	 */
	public static class MissingSectionTemplateLogic {
		public void noSectionDataMethod() {
		}
	}

	/**
	 * Ensure provide appropriate message if no template logic class but one is
	 * required.
	 */
	public void testMissingSectionMethodAsNoTemplateLogic() {
		this.doMissingSectionMethodTest(null, "Must provide template logic class for template uri");
	}

	/**
	 * Missing section method as section requires a bean.
	 * 
	 * @param templateLogicClass
	 *            Template logic {@link Class}.
	 * @param issueDescription
	 *            Expected issue description.
	 */
	private void doMissingSectionMethodTest(Class<?> templateLogicClass, String issueDescription) {

		MockCompilerIssues issues = new MockCompilerIssues(this);

		// Obtain the template location
		String templatePath = this.getPackageRelativePath(this.getClass()) + "/MissingSectionMethod.ofp";

		// Record loading the work type
		issues.recordCaptureIssues(false);

		// Record errors
		issues.recordIssue("Type", SectionNodeImpl.class, issueDescription);

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI).setValue("uri");
		if (templateLogicClass != null) {
			properties.addProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME)
					.setValue(templateLogicClass.getName());
		}

		// Test
		this.replayMockObjects();
		loader.loadSectionType(HttpTemplateSectionSource.class, templatePath, properties);
		this.verifyMockObjects();
	}

	/**
	 * Section method may not be annotated with {@link NextTask}.
	 */
	public void testIllegalNextTaskAnnotationForSectionMethod() {

		MockCompilerIssues issues = new MockCompilerIssues(this);

		// Obtain the template location
		String templatePath = this.getPackageRelativePath(this.getClass()) + "/NextTaskErrorTemplate.ofp";

		// Record loading the work type
		issues.recordCaptureIssues(false);

		// Record errors
		issues.recordIssue("Type", SectionNodeImpl.class,
				"Template bean method 'getSection' (task GETSECTION) must not be annotated with NextTask");

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME)
				.setValue(NextTaskErrorLogic.class.getName());
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI).setValue("uri");

		// Test
		this.replayMockObjects();
		loader.loadSectionType(HttpTemplateSectionSource.class, templatePath, properties);
		this.verifyMockObjects();
	}

}