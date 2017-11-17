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
package net.officefloor.plugin.web.template.section;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.SectionClassManagedFunctionSource;
import net.officefloor.plugin.section.clazz.SectionClassManagedObjectSource;
import net.officefloor.plugin.web.template.WebTemplateManagedFunctionSource;
import net.officefloor.plugin.web.template.section.TemplateLogic.RowBean;
import net.officefloor.plugin.web.template.section.WebTemplateSectionSource.NoLogicClass;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpRequestState;

/**
 * Tests the {@link WebTemplateSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(WebTemplateSectionSource.class);
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
		expected.addSectionInput("nextFunction", null);
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

		// Initial, Template and Class namespace
		SectionFunctionNamespace initialNamespace = expected.addSectionFunctionNamespace("INITIAL",
				WebTemplateInitialManagedFunctionSource.class.getName());
		SectionFunctionNamespace templateNamespace = expected.addSectionFunctionNamespace("TEMPLATE",
				WebTemplateManagedFunctionSource.class.getName());
		templateNamespace.addProperty(WebTemplateManagedFunctionSource.PROPERTY_TEMPLATE_FILE,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "Template.ofp"));
		templateNamespace.addProperty(WebTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "template",
				TemplateLogic.class.getName());
		templateNamespace.addProperty(WebTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "List",
				RowBean.class.getName());
		SectionFunctionNamespace classNamespace = expected.addSectionFunctionNamespace("WORK",
				SectionClassManagedFunctionSource.class.getName());
		classNamespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, TemplateLogic.class.getName());
		SectionFunctionNamespace iteratorNamespace = expected.addSectionFunctionNamespace("ListArrayIterator",
				WebTemplateArrayIteratorManagedFunctionSource.class.getName());
		iteratorNamespace.addProperty(WebTemplateArrayIteratorManagedFunctionSource.PROPERTY_COMPONENT_TYPE_NAME,
				RowBean.class.getName());

		// Initial function
		SectionFunction initial = initialNamespace.addSectionFunction("_INITIAL_FUNCTION_",
				WebTemplateInitialManagedFunctionSource.FUNCTION_NAME);
		expected.link(initial.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(initial.getFunctionObject("REQUEST_STATE"), requestState);
		expected.link(initial.getFunctionObject("HTTP_SESSION"), httpSession);
		initial.getFunctionFlow("RENDER");

		// Template
		SectionFunction getTemplate = classNamespace.addSectionFunction("getTemplate", "getTemplate");
		expected.link(getTemplate.getFunctionObject("OBJECT"), sectionMo);
		SectionFunction template = templateNamespace.addSectionFunction("template", "template");
		expected.link(template.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(template.getFunctionObject("OBJECT"), sectionMo);

		// Methods for beans/properties
		for (String beanMethodName : new String[] { "getTemplateName", "getEscapedHtml", "getUnescapedHtml",
				"getNullBean", "getBean", "getBeanProperty", "getBeanArray" }) {
			SectionFunction beanMethodFunction = classNamespace.addSectionFunction(beanMethodName, beanMethodName);
			expected.link(beanMethodFunction.getFunctionObject("OBJECT"), sectionMo);
		}

		// List
		SectionFunction getList = classNamespace.addSectionFunction("getList", "getList");
		expected.link(getList.getFunctionObject("OBJECT"), sectionMo);
		expected.link(getList.getFunctionObject(HttpSession.class.getName()), httpSession);
		SectionFunction list = templateNamespace.addSectionFunction("List", "List");
		expected.link(list.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(list.getFunctionObject("OBJECT"), sectionMo);
		SectionFunction listArrayIterator = iteratorNamespace.addSectionFunction("ListArrayIterator", "iterate");
		listArrayIterator.getFunctionObject("ARRAY").flagAsParameter();

		// Tail
		SectionFunction tail = templateNamespace.addSectionFunction("Tail", "Tail");
		expected.link(tail.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);

		// nextFunction link URL continuation
		fail("TODO configure redirects via WebArchitect");
		// SectionFunctionNamespace nextFunctionContinuationNamespace =
		// expected.addSectionFunctionNamespace(
		// "HTTP_URL_CONTINUATION_nextFunction",
		// HttpUrlContinuationManagedFunctionSource.class.getName());
		// nextFunctionContinuationNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH,
		// "/next");
		// nextFunctionContinuationNamespace.addSectionFunction("HTTP_URL_CONTINUATION_nextFunction",
		// HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

		// Handle nextFunction function
		SectionFunction nextFunctionMethod = classNamespace.addSectionFunction("nextFunction", "nextFunction");
		expected.link(nextFunctionMethod.getFunctionObject("OBJECT"), sectionMo);
		expected.link(nextFunctionMethod.getFunctionObject(ServerHttpConnection.class.getName()), httpConnection);

		// submit link URL continuation
		fail("TODO configure redirects via WebArchitect");
		// SectionFunctionNamespace submitContinuationNamespace =
		// expected.addSectionFunctionNamespace(
		// "HTTP_URL_CONTINUATION_submit",
		// HttpUrlContinuationManagedFunctionSource.class.getName());
		// submitContinuationNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH,
		// "/submit");
		// submitContinuationNamespace.addSectionFunction("HTTP_URL_CONTINUATION_submit",
		// HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

		// Handle submit function
		SectionFunction submitMethod = classNamespace.addSectionFunction("submit", "submit");
		expected.link(submitMethod.getFunctionObject("OBJECT"), sectionMo);
		expected.link(submitMethod.getFunctionObject(ServerHttpConnection.class.getName()), httpConnection);

		// Route non-method link
		fail("TODO configure redirects via WebArchitect");
		// SectionFunctionNamespace nonMethodContinuationNamespace =
		// expected.addSectionFunctionNamespace(
		// "HTTP_URL_CONTINUATION_nonMethodLink",
		// HttpUrlContinuationManagedFunctionSource.class.getName());
		// nonMethodContinuationNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH,
		// "/nonMethod");
		// nonMethodContinuationNamespace.addSectionFunction("HTTP_URL_CONTINUATION_nonMethodLink",
		// HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

		// Extra function
		SectionFunction doInternalFlow = classNamespace.addSectionFunction("doInternalFlow", "doInternalFlow");
		expected.link(doInternalFlow.getFunctionObject("OBJECT"), sectionMo);
		doInternalFlow.getFunctionObject(Integer.class.getName()).flagAsParameter();
		expected.link(doInternalFlow.getFunctionObject(Connection.class.getName()), connection);
		expected.link(doInternalFlow.getFunctionObject(ServerHttpConnection.class.getName()), httpConnection);

		// Not render template after link
		fail("TODO configure redirects via WebArchitect");
		// SectionFunctionNamespace notRenderTemplateAfterNamespace =
		// expected.addSectionFunctionNamespace(
		// "HTTP_URL_CONTINUATION_notRenderTemplateAfter",
		// HttpUrlContinuationManagedFunctionSource.class.getName());
		// notRenderTemplateAfterNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH,
		// "/notRender");
		// notRenderTemplateAfterNamespace.addSectionFunction("HTTP_URL_CONTINUATION_notRenderTemplateAfter",
		// HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

		// Handle not render template after function
		SectionFunction notRenderTemplateAfterMethod = classNamespace.addSectionFunction("notRenderTemplateAfter",
				"notRenderTemplateAfter");
		expected.link(notRenderTemplateAfterMethod.getFunctionObject("OBJECT"), sectionMo);
		expected.link(notRenderTemplateAfterMethod.getFunctionObject(ServerHttpConnection.class.getName()),
				httpConnection);

		// Validate type
		SectionLoaderUtil.validateSection(expected, WebTemplateSectionSource.class, this.getClass(), "Template.ofp",
				WebTemplateSectionSource.PROPERTY_CLASS_NAME, TemplateLogic.class.getName());
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
		SectionObject requestState = expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		SectionObject httpSession = expected.addSectionObject(HttpSession.class.getName(), HttpSession.class.getName());

		// Managed Object Sources
		SectionManagedObjectSource sectionMos = expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName());
		sectionMos.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				TemplateDataLogic.class.getName());
		SectionManagedObject sectionMo = sectionMos.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);

		// Initial, Template and Class namespace
		SectionFunctionNamespace initialNamespace = expected.addSectionFunctionNamespace("INITIAL",
				WebTemplateInitialManagedFunctionSource.class.getName());
		SectionFunctionNamespace templateNamespace = expected.addSectionFunctionNamespace("TEMPLATE",
				WebTemplateManagedFunctionSource.class.getName());
		templateNamespace.addProperty(WebTemplateManagedFunctionSource.PROPERTY_TEMPLATE_FILE,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "TemplateData.ofp"));
		templateNamespace.addProperty(WebTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "template",
				TemplateDataLogic.class.getName());
		templateNamespace.addProperty(WebTemplateManagedFunctionSource.PROPERTY_BEAN_PREFIX + "section",
				TemplateDataLogic.class.getName());
		SectionFunctionNamespace classNamespace = expected.addSectionFunctionNamespace("WORK",
				SectionClassManagedFunctionSource.class.getName());
		classNamespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				TemplateDataLogic.class.getName());

		// Initial function
		SectionFunction initial = initialNamespace.addSectionFunction("_INITIAL_TASK_",
				WebTemplateInitialManagedFunctionSource.FUNCTION_NAME);
		expected.link(initial.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(initial.getFunctionObject("REQUEST_STATE"), requestState);
		expected.link(initial.getFunctionObject("HTTP_SESSION"), httpSession);
		initial.getFunctionFlow("RENDER");

		// Template
		SectionFunction getTemplate = classNamespace.addSectionFunction("getTemplateData", "getTemplateData");
		expected.link(getTemplate.getFunctionObject("OBJECT"), sectionMo);
		SectionFunction template = templateNamespace.addSectionFunction("template", "template");
		expected.link(template.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(template.getFunctionObject("OBJECT"), sectionMo);

		// Message
		SectionFunction message = classNamespace.addSectionFunction("getMessage", "getMessage");
		expected.link(message.getFunctionObject("OBJECT"), sectionMo);

		// Section
		SectionFunction getSection = classNamespace.addSectionFunction("getSectionData", "getSectionData");
		expected.link(getSection.getFunctionObject("OBJECT"), sectionMo);
		SectionFunction section = templateNamespace.addSectionFunction("section", "section");
		expected.link(section.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		initial.getFunctionObject("REQUEST_STATE");
		expected.link(section.getFunctionObject("OBJECT"), sectionMo);

		// Description
		SectionFunction description = classNamespace.addSectionFunction("getDescription", "getDescription");
		expected.link(description.getFunctionObject("OBJECT"), sectionMo);

		// External flow
		SectionFunction doExternalFlow = classNamespace.addSectionFunction("requiredForIntegration",
				"requiredForIntegration");
		expected.link(doExternalFlow.getFunctionObject("OBJECT"), sectionMo);

		// Validate type
		SectionLoaderUtil.validateSection(expected, WebTemplateSectionSource.class, this.getClass(), "TemplateData.ofp",
				WebTemplateSectionSource.PROPERTY_CLASS_NAME, TemplateDataLogic.class.getName());
	}

	/**
	 * Ensure can use {@link WebTemplateSectionSource} without a logic class.
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
		SectionObject requestState = expected.addSectionObject(HttpRequestState.class.getName(),
				HttpRequestState.class.getName());
		SectionObject httpSession = expected.addSectionObject(HttpSession.class.getName(), HttpSession.class.getName());

		// Managed Object Sources
		SectionManagedObjectSource sectionMos = expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName());
		sectionMos.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, NoLogicClass.class.getName());
		SectionManagedObject sectionMo = sectionMos.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);

		// Add the no logic class (with internal function)
		SectionFunctionNamespace classNamespace = expected.addSectionFunctionNamespace("WORK",
				SectionClassManagedFunctionSource.class.getName());
		classNamespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				NoLogicClass.class.getName());
		SectionFunction getTemplate = classNamespace.addSectionFunction("notIncludedInput", "notIncludedInput");
		expected.link(getTemplate.getFunctionObject("OBJECT"), sectionMo);

		// Initial and Template namespace
		SectionFunctionNamespace initialNamespace = expected.addSectionFunctionNamespace("INITIAL",
				WebTemplateInitialManagedFunctionSource.class.getName());
		SectionFunctionNamespace templateNamspace = expected.addSectionFunctionNamespace("TEMPLATE",
				WebTemplateManagedFunctionSource.class.getName());
		templateNamspace.addProperty(WebTemplateManagedFunctionSource.PROPERTY_TEMPLATE_FILE,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "NoLogicTemplate.ofp"));

		// Initial function
		SectionFunction initial = initialNamespace.addSectionFunction("_INITIAL_FUNCTION_",
				WebTemplateInitialManagedFunctionSource.FUNCTION_NAME);
		expected.link(initial.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		expected.link(initial.getFunctionObject("REQUEST_STATE"), requestState);
		expected.link(initial.getFunctionObject("HTTP_SESSION"), httpSession);
		initial.getFunctionFlow("RENDER");

		// Section
		SectionFunction section = templateNamspace.addSectionFunction("Section", "Section");
		expected.link(section.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);

		// nonMethodLink URL continuation
		fail("TODO configure redirects via WebArchitect");
		// SectionFunctionNamespace nonMethodContinuationNamespace =
		// expected.addSectionFunctionNamespace(
		// "HTTP_URL_CONTINUATION_nonMethodLink",
		// HttpUrlContinuationManagedFunctionSource.class.getName());
		// nonMethodContinuationNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH,
		// "nonMethod");
		// nonMethodContinuationNamespace.addSectionFunction("HTTP_URL_CONTINUATION_nonMethodLink",
		// HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

		// doExternalFlow URL continuation
		fail("TODO configure redirects via WebArchitect");
		// SectionFunctionNamespace doExternalFlowContinuationNamespace =
		// expected.addSectionFunctionNamespace(
		// "HTTP_URL_CONTINUATION_doExternalFlow",
		// HttpUrlContinuationManagedFunctionSource.class.getName());
		// doExternalFlowContinuationNamespace.addProperty(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH,
		// "doExternalFlow");
		// doExternalFlowContinuationNamespace.addSectionFunction("HTTP_URL_CONTINUATION_doExternalFlow",
		// HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME);

		// Validate type
		SectionLoaderUtil.validateSection(expected, WebTemplateSectionSource.class, this.getClass(),
				"NoLogicTemplate.ofp");
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
		properties.addProperty(WebTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "LINK")
				.setValue(String.valueOf(true));

		// Test
		this.replayMockObjects();
		loader.loadSectionType(WebTemplateSectionSource.class, templatePath, properties);
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
		expected.addSectionObject(HttpRequestState.class.getName(), HttpRequestState.class.getName());
		expected.addSectionObject(HttpSession.class.getName(), HttpSession.class.getName());

		// Ensure correct type (and no link secure unknown issue)
		SectionLoaderUtil.validateSectionType(expected, WebTemplateSectionSource.class, childTemplatePath,
				WebTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES, parentTemplatePath,
				WebTemplateManagedFunctionSource.PROPERTY_LINK_SECURE_PREFIX + "link", String.valueOf(true));
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
		if (templateLogicClass != null) {
			properties.addProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME).setValue(templateLogicClass.getName());
		}

		// Test
		this.replayMockObjects();
		loader.loadSectionType(WebTemplateSectionSource.class, templatePath, properties);
		this.verifyMockObjects();
	}

	/**
	 * Section method may not be annotated with {@link NextFunction}.
	 */
	public void testIllegalNextFunctionAnnotationForSectionMethod() {

		MockCompilerIssues issues = new MockCompilerIssues(this);

		// Obtain the template location
		String templatePath = this.getPackageRelativePath(this.getClass()) + "/NextFunctionErrorTemplate.ofp";

		// Record loading the work type
		issues.recordCaptureIssues(false);

		// Record errors
		issues.recordIssue("Type", SectionNodeImpl.class,
				"Template bean method 'getSection' (task GETSECTION) must not be annotated with NextFunction");

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME)
				.setValue(NextFunctionErrorLogic.class.getName());

		// Test
		this.replayMockObjects();
		loader.loadSectionType(WebTemplateSectionSource.class, templatePath, properties);
		this.verifyMockObjects();
	}

}