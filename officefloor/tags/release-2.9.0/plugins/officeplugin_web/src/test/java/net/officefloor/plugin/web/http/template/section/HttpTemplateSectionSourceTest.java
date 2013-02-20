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
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextTask;
import net.officefloor.plugin.section.clazz.SectionClassManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationWorkSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource.NoLogicClass;
import net.officefloor.plugin.web.http.template.section.TemplateLogic.RowBean;

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
		SectionLoaderUtil.validateSpecification(
				HttpTemplateSectionSource.class,
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "URI Path");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil
				.createSectionDesigner(HttpTemplateSectionSource.class);

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

		// Outputs
		expected.addSectionOutput("doExternalFlow", String.class.getName(),
				false);
		expected.addSectionOutput("nonMethodLink", null, false);
		expected.addSectionOutput("output", null, false);
		expected.addSectionOutput(SQLException.class.getName(),
				SQLException.class.getName(), true);
		expected.addSectionOutput(IOException.class.getName(),
				IOException.class.getName(), true);

		// Objects
		expected.addSectionObject(Connection.class.getName(),
				Connection.class.getName());
		expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());
		expected.addSectionObject(HttpApplicationLocation.class.getName(),
				HttpApplicationLocation.class.getName());
		expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		expected.addSectionObject(HttpSession.class.getName(),
				HttpSession.class.getName());

		// Initial, Template and Class work
		SectionWork initialWork = expected.addSectionWork("INITIAL",
				HttpTemplateInitialWorkSource.class.getName());
		SectionWork templateWork = expected.addSectionWork("TEMPLATE",
				HttpTemplateWorkSource.class.getName());
		SectionWork classWork = expected.addSectionWork("WORK",
				ClassSectionSource.class.getName());

		// Initial task
		SectionTask initial = initialWork.addSectionTask("_INITIAL_TASK_",
				HttpTemplateInitialWorkSource.TASK_NAME);
		initial.getTaskObject("SERVER_HTTP_CONNECTION");
		initial.getTaskObject("HTTP_APPLICATION_LOCATION");
		initial.getTaskObject("REQUEST_STATE");
		initial.getTaskObject("HTTP_SESSION");
		initial.getTaskFlow("RENDER");

		// Template
		SectionTask getTemplate = classWork.addSectionTask("getTemplate",
				"getTemplate");
		getTemplate.getTaskObject("OBJECT");
		SectionTask template = templateWork.addSectionTask("template",
				"template");
		template.getTaskObject("SERVER_HTTP_CONNECTION");
		template.getTaskObject("HTTP_APPLICATION_LOCATION");
		template.getTaskObject("OBJECT");

		// Methods for beans/properties
		for (String beanMethodName : new String[] { "getTemplateName",
				"getEscapedHtml", "getUnescapedHtml", "getNullBean", "getBean",
				"getBeanProperty", "getBeanArray" }) {
			SectionTask beanMethodTask = classWork.addSectionTask(
					beanMethodName, beanMethodName);
			beanMethodTask.getTaskObject("OBJECT");
		}

		// List
		SectionTask getList = classWork.addSectionTask("getList", "getList");
		getList.getTaskObject("OBJECT");
		getList.getTaskObject(HttpSession.class.getName());
		SectionTask listArrayIterator = classWork.addSectionTask(
				"ListArrayIterator", "ListArrayIterator");
		listArrayIterator.getTaskObject("ARRAY");
		SectionTask list = templateWork.addSectionTask("List", "List");
		list.getTaskObject("SERVER_HTTP_CONNECTION");
		list.getTaskObject("HTTP_APPLICATION_LOCATION");
		list.getTaskObject("OBJECT");

		// Tail
		SectionTask tail = templateWork.addSectionTask("Tail", "Tail");
		tail.getTaskObject("SERVER_HTTP_CONNECTION");
		tail.getTaskObject("HTTP_APPLICATION_LOCATION");

		// nextTask link URL continuation
		SectionWork nextTaskContinuationWork = expected.addSectionWork(
				"HTTP_URL_CONTINUATION_nextTask",
				HttpUrlContinuationWorkSource.class.getName());
		nextTaskContinuationWork.addSectionTask(
				"HTTP_URL_CONTINUATION_nextTask",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Handle nextTask task
		SectionTask nextTaskMethod = classWork.addSectionTask("nextTask",
				"nextTask");
		nextTaskMethod.getTaskObject("OBJECT");
		nextTaskMethod.getTaskObject(ServerHttpConnection.class.getName());

		// submit link URL continuation
		SectionWork submitContinuationWork = expected.addSectionWork(
				"HTTP_URL_CONTINUATION_submit",
				HttpUrlContinuationWorkSource.class.getName());
		submitContinuationWork.addSectionTask("HTTP_URL_CONTINUATION_submit",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Handle submit task
		SectionTask submitMethod = classWork.addSectionTask("submit", "submit");
		submitMethod.getTaskObject("OBJECT");
		submitMethod.getTaskObject(ServerHttpConnection.class.getName());

		// Route non-method link
		SectionWork nonMethodContinuationWork = expected.addSectionWork(
				"HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.class.getName());
		nonMethodContinuationWork.addSectionTask(
				"HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Extra task
		SectionTask doInternalFlow = classWork.addSectionTask("doInternalFlow",
				"doInternalFlow");
		doInternalFlow.getTaskObject("OBJECT");
		doInternalFlow.getTaskObject(Integer.class.getName());
		doInternalFlow.getTaskObject(Connection.class.getName());
		doInternalFlow.getTaskObject(ServerHttpConnection.class.getName());

		// Managed Object Sources
		expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName()).addProperty(
				SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				TemplateLogic.class.getName());
		expected.addSectionManagedObjectSource("managedObject",
				ClassManagedObjectSource.class.getName()).addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				RowBean.class.getName());

		// Validate type
		SectionLoaderUtil.validateSection(expected,
				HttpTemplateSectionSource.class, this.getClass(),
				"Template.ofp", HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				TemplateLogic.class.getName(),
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");
	}

	/**
	 * Ensure find methods with Data suffix.
	 */
	public void testTypeWithDataSuffix() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil
				.createSectionDesigner(HttpTemplateSectionSource.class);

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
		expected.addSectionOutput(IOException.class.getName(),
				IOException.class.getName(), true);

		// Objects
		expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());
		expected.addSectionObject(HttpApplicationLocation.class.getName(),
				HttpApplicationLocation.class.getName());
		expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		expected.addSectionObject(HttpSession.class.getName(),
				HttpSession.class.getName());

		// Managed Object Sources
		expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName()).addProperty(
				SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				TemplateDataLogic.class.getName());

		// Initial, Template and Class work
		SectionWork initialWork = expected.addSectionWork("INITIAL",
				HttpTemplateInitialWorkSource.class.getName());
		SectionWork templateWork = expected.addSectionWork("TEMPLATE",
				HttpTemplateWorkSource.class.getName());
		SectionWork classWork = expected.addSectionWork("WORK",
				ClassSectionSource.class.getName());

		// Initial task
		SectionTask initial = initialWork.addSectionTask("_INITIAL_TASK_",
				HttpTemplateInitialWorkSource.TASK_NAME);
		initial.getTaskObject("SERVER_HTTP_CONNECTION");
		initial.getTaskObject("HTTP_APPLICATION_LOCATION");
		initial.getTaskObject("REQUEST_STATE");
		initial.getTaskObject("HTTP_SESSION");
		initial.getTaskFlow("RENDER");

		// Template
		SectionTask getTemplate = classWork.addSectionTask("getTemplateData",
				"getTemplateData");
		getTemplate.getTaskObject("OBJECT");
		SectionTask template = templateWork.addSectionTask("template",
				"template");
		template.getTaskObject("SERVER_HTTP_CONNECTION");
		template.getTaskObject("HTTP_APPLICATION_LOCATION");
		template.getTaskObject("OBJECT");

		// Message
		SectionTask message = classWork.addSectionTask("getMessage",
				"getMessage");
		message.getTaskObject("OBJECT");

		// Section
		SectionTask getSection = classWork.addSectionTask("getSectionData",
				"getSectionData");
		getSection.getTaskObject("OBJECT");
		SectionTask section = templateWork.addSectionTask("section", "section");
		section.getTaskObject("SERVER_HTTP_CONNECTION");
		section.getTaskObject("HTTP_APPLICATION_LOCATION");
		initial.getTaskObject("REQUEST_STATE");
		section.getTaskObject("OBJECT");

		// Description
		SectionTask description = classWork.addSectionTask("getDescription",
				"getDescription");
		description.getTaskObject("OBJECT");

		// External flow
		SectionTask doExternalFlow = classWork.addSectionTask(
				"requiredForIntegration", "requiredForIntegration");
		doExternalFlow.getTaskObject("OBJECT");

		// Validate type
		SectionLoaderUtil.validateSection(expected,
				HttpTemplateSectionSource.class, this.getClass(),
				"TemplateData.ofp",
				HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				TemplateDataLogic.class.getName(),
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");
	}

	/**
	 * Ensure can use {@link HttpTemplateSectionSource} without a logic class.
	 */
	public void testTypeWithNoLogicClass() {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil
				.createSectionDesigner(HttpTemplateSectionSource.class);

		// Input (for HTTP Template rending)
		expected.addSectionInput("renderTemplate", null);

		// Outputs
		expected.addSectionOutput("output", null, false);
		expected.addSectionOutput("nonMethodLink", null, false);
		expected.addSectionOutput("doExternalFlow", null, false);
		expected.addSectionOutput(IOException.class.getName(),
				IOException.class.getName(), true);

		// Objects
		expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());
		expected.addSectionObject(HttpApplicationLocation.class.getName(),
				HttpApplicationLocation.class.getName());
		expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		expected.addSectionObject(HttpSession.class.getName(),
				HttpSession.class.getName());

		// Add the no logic class (with internal task)
		SectionWork classWork = expected.addSectionWork("WORK",
				ClassSectionSource.class.getName());
		SectionTask getTemplate = classWork.addSectionTask("notIncludedInput",
				"notIncluded");
		getTemplate.getTaskObject("OBJECT");

		// Initial and Template work
		SectionWork initialWork = expected.addSectionWork("INITIAL",
				HttpTemplateInitialWorkSource.class.getName());
		SectionWork templateWork = expected.addSectionWork("TEMPLATE",
				HttpTemplateWorkSource.class.getName());

		// Initial task
		SectionTask initial = initialWork.addSectionTask("_INITIAL_TASK_",
				HttpTemplateInitialWorkSource.TASK_NAME);
		initial.getTaskObject("SERVER_HTTP_CONNECTION");
		initial.getTaskObject("HTTP_APPLICATION_LOCATION");
		initial.getTaskObject("REQUEST_STATE");
		initial.getTaskObject("HTTP_SESSION");
		initial.getTaskFlow("RENDER");

		// Section
		SectionTask section = templateWork.addSectionTask("Section", "Section");
		section.getTaskObject("SERVER_HTTP_CONNECTION");
		section.getTaskObject("HTTP_APPLICATION_LOCATION");

		// nonMethodLink URL continuation
		SectionWork nonMethodContinuationWork = expected.addSectionWork(
				"HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.class.getName());
		nonMethodContinuationWork.addSectionTask(
				"HTTP_URL_CONTINUATION_nonMethodLink",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// doExternalFlow URL continuation
		SectionWork doExternalFlowContinuationWork = expected.addSectionWork(
				"HTTP_URL_CONTINUATION_doExternalFlow",
				HttpUrlContinuationWorkSource.class.getName());
		doExternalFlowContinuationWork.addSectionTask(
				"HTTP_URL_CONTINUATION_doExternalFlow",
				HttpUrlContinuationWorkSource.TASK_NAME);

		// Managed Object Sources
		expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName()).addProperty(
				SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				NoLogicClass.class.getName());

		// Validate type
		SectionLoaderUtil.validateSection(expected,
				HttpTemplateSectionSource.class, this.getClass(),
				"NoLogicTemplate.ofp",
				HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI, "uri");
	}

	/**
	 * Ensure that all links do exist.
	 */
	public void testUnknownLinkSecured() {

		CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Obtain the template location
		String templatePath = this.getPackageRelativePath(this.getClass())
				+ "/NotExistLinkTemplate.ofp";

		// Record errors
		issues.addIssue(LocationType.SECTION, templatePath, null, null,
				"Link 'LINK' does not exist on template /uri");

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI)
				.setValue("/uri");
		properties.addProperty(
				HttpTemplateWorkSource.PROPERTY_LINK_SECURE_PREFIX + "LINK")
				.setValue(String.valueOf(true));

		// Test
		this.replayMockObjects();
		loader.loadSectionType(HttpTemplateSectionSource.class, templatePath,
				properties);
		this.verifyMockObjects();
	}

	/**
	 * Section method may not be annotated with {@link NextTask}.
	 */
	public void testNoNextTaskAnnotationForSectionMethod() {

		CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Obtain the template location
		String templatePath = this.getPackageRelativePath(this.getClass())
				+ "/NextTaskErrorTemplate.ofp";

		// Record errors
		issues.addIssue(LocationType.SECTION, templatePath, AssetType.TASK,
				"GETSECTION",
				"Template bean method 'getSection' must not be annotated with NextTask");

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME)
				.setValue(NextTaskErrorLogic.class.getName());
		properties.addProperty(HttpTemplateSectionSource.PROPERTY_TEMPLATE_URI)
				.setValue("uri");

		// Test
		this.replayMockObjects();
		loader.loadSectionType(HttpTemplateSectionSource.class, templatePath,
				properties);
		this.verifyMockObjects();
	}

}