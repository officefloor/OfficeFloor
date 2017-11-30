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
package net.officefloor.web.template.section;

import java.io.FileReader;
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
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.template.parse.ParsedTemplate;
import net.officefloor.web.template.parse.WebTemplateParser;
import net.officefloor.web.template.section.TemplateLogic.RowBean;
import net.officefloor.web.template.section.WebTemplateSectionSource.NoLogicClass;
import net.officefloor.web.template.section.WebTemplateSectionSource.WebTemplateArrayIteratorManagedFunctionSource;
import net.officefloor.web.template.section.WebTemplateSectionSource.WebTemplateInitialManagedFunctionSource;
import net.officefloor.web.template.section.WebTemplateSectionSource.WebTemplateManagedFunctionSource;

/**
 * Tests the {@link WebTemplateSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(WebTemplateSectionSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() throws IOException {

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
		expected.addSectionInput("nextFunction", null)
				.addAnnotation(new WebTemplateLinkAnnotation(false, "next", new HttpMethod[0]));
		expected.addSectionInput("submit", null)
				.addAnnotation(new WebTemplateLinkAnnotation(false, "submit", new HttpMethod[0]));
		expected.addSectionInput("doInternalFlow", Integer.class.getName());
		expected.addSectionInput("notRenderTemplateAfter", null)
				.addAnnotation(new WebTemplateLinkAnnotation(false, "notRenderTemplateAfter", new HttpMethod[0]));
		expected.addSectionInput("nonMethodLink", null)
				.addAnnotation(new WebTemplateLinkAnnotation(false, "nonMethodLink", new HttpMethod[0]));

		// Outputs
		expected.addSectionOutput("doExternalFlow", String.class.getName(), false);
		expected.addSectionOutput("nonMethodLink", null, false);
		expected.addSectionOutput("redirectToTemplate", null, false);
		expected.addSectionOutput(SQLException.class.getName(), SQLException.class.getName(), true);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		SectionObject doFlowParameter = expected.addSectionObject("HttpQueryParameter_doFlow-java.lang.String",
				String.class.getName());
		doFlowParameter.setTypeQualifier("HttpQueryParameter_doFlow");
		SectionObject connection = expected.addSectionObject(Connection.class.getName(), Connection.class.getName());
		SectionObject httpConnection = expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());
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

		// Obtain the parsed content
		ParsedTemplate parsedTemplate = WebTemplateParser
				.parse(new FileReader(this.findFile(this.getClass(), "Template.ofp")));

		// Initial, Template and Class namespace
		SectionFunctionNamespace initialNamespace = expected.addSectionFunctionNamespace("INITIAL",
				new WebTemplateInitialManagedFunctionSource(
						new WebTemplateInitialFunction(false, null, null, null, '+')));
		SectionFunctionNamespace templateNamespace = expected.addSectionFunctionNamespace("TEMPLATE",
				new WebTemplateManagedFunctionSource(false, parsedTemplate,
						ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET, '+'));
		templateNamespace.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "Template.ofp"));
		templateNamespace.addProperty(WebTemplateSectionSource.PROPERTY_BEAN_PREFIX + "template",
				TemplateLogic.class.getName());
		templateNamespace.addProperty(WebTemplateSectionSource.PROPERTY_BEAN_PREFIX + "List", RowBean.class.getName());
		SectionFunctionNamespace classNamespace = expected.addSectionFunctionNamespace("RENDER",
				SectionClassManagedFunctionSource.class.getName());
		classNamespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, TemplateLogic.class.getName());
		SectionFunctionNamespace iteratorNamespace = expected.addSectionFunctionNamespace("ListArrayIterator",
				new WebTemplateArrayIteratorManagedFunctionSource(RowBean.class));

		// Initial function
		SectionFunction initial = initialNamespace.addSectionFunction("_INITIAL_FUNCTION_",
				WebTemplateInitialManagedFunctionSource.FUNCTION_NAME);
		expected.link(initial.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
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

		// Handle nextFunction function
		SectionFunction nextFunctionMethod = classNamespace.addSectionFunction("nextFunction", "nextFunction");
		expected.link(nextFunctionMethod.getFunctionObject("OBJECT"), sectionMo);
		expected.link(nextFunctionMethod.getFunctionObject(ServerHttpConnection.class.getName()), httpConnection);

		// Handle submit function
		SectionFunction submitMethod = classNamespace.addSectionFunction("submit", "submit");
		expected.link(submitMethod.getFunctionObject("HttpQueryParameter_doFlow-java.lang.String"), sectionMo);
		expected.link(submitMethod.getFunctionObject("OBJECT"), sectionMo);
		expected.link(submitMethod.getFunctionObject(ServerHttpConnection.class.getName()), httpConnection);

		// Extra function
		SectionFunction doInternalFlow = classNamespace.addSectionFunction("doInternalFlow", "doInternalFlow");
		expected.link(doInternalFlow.getFunctionObject("OBJECT"), sectionMo);
		doInternalFlow.getFunctionObject(Integer.class.getName()).flagAsParameter();
		expected.link(doInternalFlow.getFunctionObject(Connection.class.getName()), connection);
		expected.link(doInternalFlow.getFunctionObject(ServerHttpConnection.class.getName()), httpConnection);

		// Handle not render template after function
		SectionFunction notRenderTemplateAfterMethod = classNamespace.addSectionFunction("notRenderTemplateAfter",
				"notRenderTemplateAfter");
		expected.link(notRenderTemplateAfterMethod.getFunctionObject("OBJECT"), sectionMo);
		expected.link(notRenderTemplateAfterMethod.getFunctionObject(ServerHttpConnection.class.getName()),
				httpConnection);

		// Validate type
		SectionLoaderUtil.validateSection(expected, WebTemplateSectionSource.class, "/template",
				WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION,
				this.getFileLocation(this.getClass(), "Template.ofp"), WebTemplateSectionSource.PROPERTY_CLASS_NAME,
				TemplateLogic.class.getName());
	}

	/**
	 * Ensure find methods with Data suffix.
	 */
	public void testTypeWithDataSuffix() throws IOException {

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
		expected.addSectionOutput("redirectToTemplate", null, false);
		expected.addSectionOutput("doExternalFlow", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		SectionObject httpConnection = expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());

		// Managed Object Sources
		SectionManagedObjectSource sectionMos = expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName());
		sectionMos.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				TemplateDataLogic.class.getName());
		SectionManagedObject sectionMo = sectionMos.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);

		// Obtain the parsed template
		ParsedTemplate parsedTemplate = WebTemplateParser
				.parse(new FileReader(this.findFile(this.getClass(), "TemplateData.ofp")));

		// Initial, Template and Class namespace
		SectionFunctionNamespace initialNamespace = expected.addSectionFunctionNamespace("INITIAL",
				new WebTemplateInitialManagedFunctionSource(
						new WebTemplateInitialFunction(false, null, null, null, '+')));
		SectionFunctionNamespace templateNamespace = expected.addSectionFunctionNamespace("TEMPLATE",
				new WebTemplateManagedFunctionSource(false, parsedTemplate,
						ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET, '+'));
		templateNamespace.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_CONTENT,
				SectionLoaderUtil.getClassPathLocation(this.getClass(), "TemplateData.ofp"));
		templateNamespace.addProperty(WebTemplateSectionSource.PROPERTY_BEAN_PREFIX + "template",
				TemplateDataLogic.class.getName());
		templateNamespace.addProperty(WebTemplateSectionSource.PROPERTY_BEAN_PREFIX + "section",
				TemplateDataLogic.class.getName());
		SectionFunctionNamespace classNamespace = expected.addSectionFunctionNamespace("RENDER",
				SectionClassManagedFunctionSource.class.getName());
		classNamespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				TemplateDataLogic.class.getName());

		// Initial function
		SectionFunction initial = initialNamespace.addSectionFunction("_INITIAL_FUNCTION_",
				WebTemplateInitialManagedFunctionSource.FUNCTION_NAME);
		expected.link(initial.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
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
		expected.link(section.getFunctionObject("OBJECT"), sectionMo);

		// Description
		SectionFunction description = classNamespace.addSectionFunction("getDescription", "getDescription");
		expected.link(description.getFunctionObject("OBJECT"), sectionMo);

		// External flow
		SectionFunction doExternalFlow = classNamespace.addSectionFunction("requiredForIntegration",
				"requiredForIntegration");
		expected.link(doExternalFlow.getFunctionObject("OBJECT"), sectionMo);

		// Validate type
		SectionLoaderUtil.validateSection(expected, WebTemplateSectionSource.class, "/path",
				WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION,
				this.getFileLocation(this.getClass(), "TemplateData.ofp"), WebTemplateSectionSource.PROPERTY_CLASS_NAME,
				TemplateDataLogic.class.getName());
	}

	/**
	 * Ensure can use {@link WebTemplateSectionSource} without a logic class.
	 */
	public void testTypeWithNoLogicClass() throws IOException {

		// Create the expected type
		SectionDesigner expected = SectionLoaderUtil.createSectionDesigner();

		// Input (for HTTP Template rending)
		expected.addSectionInput("doExternalFlow", null)
				.addAnnotation(new WebTemplateLinkAnnotation(false, "doExternalFlow", new HttpMethod[0]));
		expected.addSectionInput("nonMethodLink", null)
				.addAnnotation(new WebTemplateLinkAnnotation(false, "nonMethodLink", new HttpMethod[0]));
		expected.addSectionInput("renderTemplate", null);

		// Outputs
		expected.addSectionOutput("nonMethodLink", null, false);
		expected.addSectionOutput("doExternalFlow", null, false);
		expected.addSectionOutput("redirectToTemplate", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		SectionObject httpConnection = expected.addSectionObject(ServerHttpConnection.class.getName(),
				ServerHttpConnection.class.getName());

		// Managed Object Sources
		SectionManagedObjectSource sectionMos = expected.addSectionManagedObjectSource("OBJECT",
				SectionClassManagedObjectSource.class.getName());
		sectionMos.addProperty(SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, NoLogicClass.class.getName());
		SectionManagedObject sectionMo = sectionMos.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);

		// Add the no logic class (with internal function)
		SectionFunctionNamespace classNamespace = expected.addSectionFunctionNamespace("RENDER",
				SectionClassManagedFunctionSource.class.getName());
		classNamespace.addProperty(SectionClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME,
				NoLogicClass.class.getName());
		SectionFunction getTemplate = classNamespace.addSectionFunction("notIncludedInput", "notIncludedInput");
		expected.link(getTemplate.getFunctionObject("OBJECT"), sectionMo);

		// Obtain the parsed template
		ParsedTemplate parsedTemplate = WebTemplateParser
				.parse(new FileReader(this.findFile(this.getClass(), "NoLogicTemplate.ofp")));

		// Initial and Template namespace
		SectionFunctionNamespace initialNamespace = expected.addSectionFunctionNamespace("INITIAL",
				new WebTemplateInitialManagedFunctionSource(
						new WebTemplateInitialFunction(false, null, null, null, '+')));
		SectionFunctionNamespace templateNamspace = expected.addSectionFunctionNamespace("TEMPLATE",
				new WebTemplateManagedFunctionSource(false, parsedTemplate,
						ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET, '+'));

		// Initial function
		SectionFunction initial = initialNamespace.addSectionFunction("_INITIAL_FUNCTION_",
				WebTemplateInitialManagedFunctionSource.FUNCTION_NAME);
		expected.link(initial.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);
		initial.getFunctionFlow("RENDER");

		// Section
		SectionFunction section = templateNamspace.addSectionFunction("Section", "Section");
		expected.link(section.getFunctionObject("SERVER_HTTP_CONNECTION"), httpConnection);

		// Validate type
		SectionLoaderUtil.validateSection(expected, WebTemplateSectionSource.class, "/path",
				WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION,
				this.getFileLocation(this.getClass(), "NoLogicTemplate.ofp"));
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
		issues.recordIssue("<type>", SectionNodeImpl.class, "Link 'LINK' does not exist on template /path");

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION).setValue(templatePath);
		properties.addProperty(WebTemplateSectionSource.PROPERTY_LINK_SECURE_PREFIX + "LINK")
				.setValue(String.valueOf(true));

		// Test
		this.replayMockObjects();
		loader.loadSectionType(WebTemplateSectionSource.class, "/path", properties);
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
		expected.addSectionOutput("redirectToTemplate", null, false);
		expected.addSectionOutput(IOException.class.getName(), IOException.class.getName(), true);

		// Objects
		expected.addSectionObject(ServerHttpConnection.class.getName(), ServerHttpConnection.class.getName());

		// Ensure correct type (and no link secure unknown issue)
		SectionLoaderUtil.validateSectionType(expected, WebTemplateSectionSource.class, "/path",
				WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION, childTemplatePath,
				WebTemplateSectionSource.PROPERTY_INHERITED_TEMPLATES_COUNT, "1",
				WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION + ".0", parentTemplatePath,
				WebTemplateSectionSource.PROPERTY_LINK_SECURE_PREFIX + "link", String.valueOf(true));
	}

	/**
	 * Ensure have section method if section requires bean.
	 */
	public void testMissingSectionMethodOnTemplateLogic() {
		this.doMissingSectionMethodTest(MissingSectionTemplateLogic.class, "Missing method 'getsection' on class "
				+ MissingSectionTemplateLogic.class.getName() + " to provide bean for template /path");
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
		this.doMissingSectionMethodTest(null, "Must provide template logic class for template /path");
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
		issues.recordIssue("<type>", SectionNodeImpl.class, issueDescription);

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION).setValue(templatePath);
		if (templateLogicClass != null) {
			properties.addProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME).setValue(templateLogicClass.getName());
		}

		// Test
		this.replayMockObjects();
		loader.loadSectionType(WebTemplateSectionSource.class, "/path", properties);
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
		issues.recordIssue("<type>", SectionNodeImpl.class,
				"Template bean method 'getSection' (function GETSECTION) must not be annotated with @NextFunction (next function is always rendering template section)");

		// Create loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		SectionLoader loader = compiler.getSectionLoader();

		// Create the properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(WebTemplateSectionSource.PROPERTY_TEMPLATE_LOCATION).setValue(templatePath);
		properties.addProperty(WebTemplateSectionSource.PROPERTY_CLASS_NAME)
				.setValue(NextFunctionErrorLogic.class.getName());

		// Test
		this.replayMockObjects();
		loader.loadSectionType(WebTemplateSectionSource.class, "/path", properties);
		this.verifyMockObjects();
	}

	public static class NextFunctionErrorLogic {

		/**
		 * Section method with disallowed {@link NextFunction}.
		 * 
		 * @return Should not be called as invalid to have {@link NextFunction}
		 *         annotation.
		 */
		@NextFunction("NotAllowed")
		public Object getSection() {
			return null;
		}
	}

}