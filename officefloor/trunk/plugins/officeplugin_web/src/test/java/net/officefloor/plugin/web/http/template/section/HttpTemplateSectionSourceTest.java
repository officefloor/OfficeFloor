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

package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.SectionClassManagedObjectSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
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
				HttpTemplateSectionSource.PROPERTY_LINK_TASK_NAME_PREFIX,
				"Link service Task name prefix");
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
		expected.addSectionObject(HttpSession.class.getName(),
				HttpSession.class.getName());
		expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());

		// Template and Class work
		SectionWork templateWork = expected.addSectionWork("TEMPLATE",
				HttpTemplateWorkSource.class.getName());
		SectionWork classWork = expected.addSectionWork("WORK",
				ClassSectionSource.class.getName());

		// Template
		SectionTask getTemplate = classWork.addSectionTask("getTemplate",
				"getTemplate");
		getTemplate.getTaskObject("OBJECT");
		SectionTask template = templateWork.addSectionTask("template",
				"template");
		template.getTaskObject("SERVER_HTTP_CONNECTION");
		template.getTaskObject("OBJECT");

		// List
		SectionTask getList = classWork.addSectionTask("getList", "getList");
		getList.getTaskObject("OBJECT");
		getList.getTaskObject(HttpSession.class.getName());
		SectionTask listArrayIterator = classWork.addSectionTask(
				"ListArrayIterator", "ListArrayIterator");
		listArrayIterator.getTaskObject("ARRAY");
		SectionTask list = templateWork.addSectionTask("List", "List");
		list.getTaskObject("SERVER_HTTP_CONNECTION");
		list.getTaskObject("OBJECT");

		// Tail
		SectionTask tail = templateWork.addSectionTask("Tail", "Tail");
		tail.getTaskObject("SERVER_HTTP_CONNECTION");

		// Additional bean method being task
		SectionTask templateName = classWork.addSectionTask("getTemplateName",
				"getTemplateName");
		templateName.getTaskObject("OBJECT");

		// Route nextTask link
		templateWork.addSectionTask("LINK_nextTask", "nextTask");

		// Handle nextTask task
		SectionTask nextTaskMethod = classWork.addSectionTask("nextTask",
				"nextTask");
		nextTaskMethod.getTaskObject("OBJECT");
		nextTaskMethod.getTaskObject(ServerHttpConnection.class.getName());

		// Route submit link
		templateWork.addSectionTask("LINK_submit", "submit");

		// Handle submit task
		SectionTask submitMethod = classWork.addSectionTask("submit", "submit");
		submitMethod.getTaskObject("OBJECT");
		submitMethod.getTaskObject(ServerHttpConnection.class.getName());

		// Route non-method link
		templateWork.addSectionTask("LINK_nonMethodLink", "nonMethodLink");

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
				HttpTemplateSectionSource.PROPERTY_LINK_TASK_NAME_PREFIX,
				"LINK_");
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

		// Add the no logic class (with internal task)
		SectionWork classWork = expected.addSectionWork("WORK",
				ClassSectionSource.class.getName());
		SectionTask getTemplate = classWork.addSectionTask("notIncludedInput",
				"notIncluded");
		getTemplate.getTaskObject("OBJECT");

		// Template work
		SectionWork templateWork = expected.addSectionWork("TEMPLATE",
				HttpTemplateWorkSource.class.getName());

		// Section
		SectionTask section = templateWork.addSectionTask("Section", "Section");
		section.getTaskObject("SERVER_HTTP_CONNECTION");

		// Links
		templateWork.addSectionTask("LINK_nonMethodLink", "nonMethodLink");
		templateWork.addSectionTask("LINK_doExternalFlow", "doExternalFlow");

		// Managed Object Sources
		expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName()).addProperty(
				SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				NoLogicClass.class.getName());

		// Validate type
		SectionLoaderUtil.validateSection(expected,
				HttpTemplateSectionSource.class, this.getClass(),
				"NoLogicTemplate.ofp",
				HttpTemplateSectionSource.PROPERTY_LINK_TASK_NAME_PREFIX,
				"LINK_");
	}

}