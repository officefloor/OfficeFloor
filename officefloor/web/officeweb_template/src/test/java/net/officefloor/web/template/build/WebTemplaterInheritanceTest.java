/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.build;

import java.io.StringReader;
import java.util.function.Consumer;

import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.template.parse.ParsedTemplateSection;
import net.officefloor.web.template.parse.WebTemplateParser;
import net.officefloor.web.template.section.WebTemplateSectionSource;

/**
 * Tests inheriting {@link ParsedTemplateSection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplaterInheritanceTest extends OfficeFrameTestCase {

	/**
	 * Tests simple inheritance.
	 */
	public void testSimpleInheritance() {
		this.doTest(new String[] { "template", "override", "footer" }, new String[] { "template", ":override" },
				"p:template", "c:override", "p:footer");
	}

	/**
	 * Ensure can override with introduced sections.
	 */
	public void testOverrideWithIntroducedSections() {
		this.doTest(new String[] { "start", "override", "end" }, new String[] { ":override", "introduced" }, "p:start",
				"c:override", "c:introduced", "p:end");
	}

	/**
	 * Ensure introduced section does not already exist from inheritance.
	 */
	public void testOverrideWithIntroducedSectionExisting() throws Exception {
		this.doInvalid(new String[] { "start", "override", "introduced", "end" },
				new String[] { ":override", "introduced" }, (issues) -> {
					issues.recordCaptureIssues(false);
					issues.recordCaptureIssues(false);
					issues.recordCaptureIssues(false);
					issues.recordCaptureIssues(false);
					issues.recordCaptureIssues(false);
					issues.recordIssue("OFFICE./child", SectionNodeImpl.class,
							"Section 'introduced' already exists by inheritance and not flagged for overriding (with ':' prefix)");
					issues.recordIssue("OFFICE", OfficeNodeImpl.class, "Failure loading OfficeSectionType from source "
							+ WebTemplateSectionSource.class.getName());
				});
	}

	/**
	 * Ensure that first section is not overridden unless provided with override
	 * prefix. The default first child section is considered a comment section, as
	 * typical overriding should occur within the body.
	 */
	public void testDefaultFirstSectionIsCommentSection() {
		this.doTest(new String[] { "template" }, new String[] { "template" }, "p:template");
	}

	/**
	 * Ensure can override the default first section.
	 */
	public void testOverrideDefaultFirstSection() {
		this.doTest(new String[] { "template" }, new String[] { ":template" }, "c:template");
	}

	/**
	 * Ensure issue as first section is not an override.
	 */
	public void testFirstSectionNotOverriding() throws Exception {
		this.doInvalid(new String[] { "override" }, new String[] { "override" }, (issues) -> {
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordIssue("OFFICE./child", SectionNodeImpl.class,
					"Section 'override' can not be introduced, as no previous override section (section prefixed with ':') to identify where to inherit");
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Failure loading OfficeSectionType from source " + WebTemplateSectionSource.class.getName());
		});
	}

	/**
	 * Ensure issue if section not overriding.
	 */
	public void testSectionNotOverriding() throws Exception {
		this.doInvalid(new String[] { "template" }, new String[] { "template", ":override" }, (issues) -> {
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordCaptureIssues(false);
			issues.recordIssue("OFFICE./child", SectionNodeImpl.class,
					"No inherited section exists for overriding by section 'override'");
			issues.recordIssue("OFFICE", OfficeNodeImpl.class,
					"Failure loading OfficeSectionType from source " + WebTemplateSectionSource.class.getName());
		});
	}

	/**
	 * Ensure that allow parent template to be re-ordered without requiring changes
	 * to child inheritance. This allows parent sections to be out of order to the
	 * child overriding sections.
	 */
	public void testOverrideInDifferentOrder() {
		this.doTest(new String[] { "first", "third", "second", "fourth" },
				new String[] { ":second", "introduced", ":third", "another" }, "p:first", "c:third", "c:another",
				"c:second", "c:introduced", "p:fourth");
	}

	/**
	 * Ensure ignore comment section.
	 */
	public void testIgnoreCommentSection() {
		this.doTest(new String[] { "!", "start", "!", "override", "!", "end", "!" },
				new String[] { "!", ":override", "!", "introduced", "!" }, "p:start", "c:override", "c:introduced",
				"p:end");
	}

	/**
	 * Ensure can continue to match if parent has override sections.
	 */
	public void testGrandChildOverride() {
		this.doTest(new String[] { "template", ":override", "footer" },
				new String[] { "template", ":override", "introduced" }, "p:template", "c:override", "c:introduced",
				"p:footer");
	}

	/**
	 * Undertakes the inheritance test.
	 * 
	 * @param parentSectionNames Parent {@link ParsedTemplateSection} names.
	 * @param childSectionNames  Child {@link ParsedTemplateSection} names.
	 * @param resultingSections  Resulting {@link ParsedTemplateSection} contents.
	 *                           Allows to distinguish between parent and child by
	 *                           same name for inheritance.
	 */
	private void doTest(String[] parentSectionNames, String[] childSectionNames, String... resultingSections) {

		// Create the content
		String parentContent = createMockTemplateContent(parentSectionNames, "p:");
		String childContent = createMockTemplateContent(childSectionNames, "c:");

		// Create the server
		Closure<MockHttpServer> server = new Closure<>();
		OfficeFloor officeFloor;
		try {
			WebCompileOfficeFloor compile = new WebCompileOfficeFloor();
			compile.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
			compile.web((context) -> {
				WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(
						context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());
				WebTemplate parent = templater.addTemplate(false, "/parent", new StringReader(parentContent));
				WebTemplate child = templater.addTemplate(false, "/child", new StringReader(childContent));
				child.setSuperTemplate(parent);
				templater.informWebArchitect();
			});
			officeFloor = compile.compileAndOpenOfficeFloor();
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Request child template for inheritance
		MockHttpResponse response = server.value.send(MockHttpServer.mockRequest("/child"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure correct content from inheritance
		String expected = createExpectedTemplateResult(resultingSections);
		assertEquals("Incorrect resulting content", expected, response.getEntity(null));

		// Ensure close OfficeFloor
		try {
			officeFloor.closeOfficeFloor();
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Ensure can override the default first section.
	 */
	public void testOverrideDefaultFirstSectionOfGrandParent() {
		this.doTest(new String[] { "template" }, new String[] { ":template" }, new String[] { ":template" },
				"c:template");
	}

	/**
	 * Ensure can override the default first section.
	 */
	public void testParentOverrideGrandParent() {
		this.doTest(new String[] { "template", "override" }, new String[] { ":override", "section" },
				new String[] { ":section" }, "g:template p:override c:section");
	}

	/**
	 * Undertakes grand parent inheritance test.
	 * 
	 * @param grandParentSectionNames Grand parent {@link ParsedTemplateSection}
	 *                                names.
	 * @param parentSectionNames      Parent {@link ParsedTemplateSection} names.
	 * @param childSectionNames       Child {@link ParsedTemplateSection} names.
	 * @param resultingSections       Resulting {@link ParsedTemplateSection}
	 *                                contents. Allows to distinguish between parent
	 *                                and child by same name for inheritance.
	 */
	private void doTest(String[] grandParentSectionNames, String[] parentSectionNames, String[] childSectionNames,
			String... resultingSections) {

		// Create the content
		String grandParentContent = createMockTemplateContent(grandParentSectionNames, "g:");
		String parentContent = createMockTemplateContent(parentSectionNames, "p:");
		String childContent = createMockTemplateContent(childSectionNames, "c:");

		// Create the server
		Closure<MockHttpServer> server = new Closure<>();
		OfficeFloor officeFloor;
		try {
			WebCompileOfficeFloor compile = new WebCompileOfficeFloor();
			compile.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
			compile.web((context) -> {
				WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(
						context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());
				WebTemplate grandParnt = templater.addTemplate(false, "/grandparent",
						new StringReader(grandParentContent));
				WebTemplate parent = templater.addTemplate(false, "/parent", new StringReader(parentContent));
				WebTemplate child = templater.addTemplate(false, "/child", new StringReader(childContent));
				parent.setSuperTemplate(grandParnt);
				child.setSuperTemplate(parent);
				templater.informWebArchitect();
			});
			officeFloor = compile.compileAndOpenOfficeFloor();
		} catch (Exception ex) {
			throw fail(ex);
		}

		// Request child template for inheritance
		MockHttpResponse response = server.value.send(MockHttpServer.mockRequest("/child"));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure correct content from inheritance
		String expected = createExpectedTemplateResult(resultingSections);
		assertEquals("Incorrect resulting content", expected, response.getEntity(null));

		// Ensure close OfficeFloor
		try {
			officeFloor.closeOfficeFloor();
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Undertakes test for invalid configuration.
	 * 
	 * @param parentSectionNames Parent {@link ParsedTemplateSection} names.
	 * @param childSectionNames  Child {@link ParsedTemplateSection} names.
	 * @param configureIssues    {@link Consumer} to configure the
	 *                           {@link MockCompilerIssues}.
	 */
	private void doInvalid(String[] parentSectionNames, String[] childSectionNames,
			Consumer<MockCompilerIssues> configureIssues) throws Exception {

		// Create the content
		String parentContent = createMockTemplateContent(parentSectionNames, "p:");
		String childContent = createMockTemplateContent(childSectionNames, "c:");

		// Provide mock issues
		MockCompilerIssues issues = new MockCompilerIssues(this);
		configureIssues.accept(issues);
		this.replayMockObjects();

		// Create the server
		Closure<MockHttpServer> server = new Closure<>();
		WebCompileOfficeFloor compile = new WebCompileOfficeFloor();
		compile.getOfficeFloorCompiler().setCompilerIssues(issues);
		compile.mockHttpServer((mockHttpServer) -> server.value = mockHttpServer);
		compile.web((context) -> {
			WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(context.getWebArchitect(),
					context.getOfficeArchitect(), context.getOfficeSourceContext());
			WebTemplate parent = templater.addTemplate(false, "/parent", new StringReader(parentContent));
			WebTemplate child = templater.addTemplate(false, "/child", new StringReader(childContent));
			child.setSuperTemplate(parent);
			templater.informWebArchitect();
		});
		OfficeFloor officeFloor = compile.compileOfficeFloor();
		assertNull("Should not load OfficeFloor", officeFloor);

		// Ensure appropriate issues raised
		this.verifyMockObjects();
	}

	/**
	 * Creates the mock {@link WebTemplate} content.
	 * 
	 * @param sectionNames  Names of the {@link ParsedTemplateSection} instances.
	 * @param contentPrefix Prefix for the {@link ParsedTemplateSection} content.
	 * @return Mock {@link WebTemplate} content.
	 */
	private static String createMockTemplateContent(String[] sectionNames, String contentPrefix) {

		// Create the mock template content
		StringBuilder content = new StringBuilder();
		boolean isFirstSection = true;
		for (String sectionName : sectionNames) {

			// Provide section separator if not the default template section
			if ((isFirstSection) && (WebTemplateParser.DEFAULT_FIRST_SECTION_NAME.equals(sectionName))) {
				// Do not include section for default first section
			} else {
				content.append("<!-- {");
				content.append(sectionName);
				content.append("} -->");
			}
			isFirstSection = false;

			// Provide content for section
			content.append(contentPrefix
					+ (sectionName.startsWith(":") ? sectionName.substring(":".length()) : sectionName) + " ");
		}

		// Return the mock template content
		return content.toString();
	}

	/**
	 * Creates the expected {@link WebTemplate} result.
	 * 
	 * @param sections Section content expected to be rendered.
	 * @return Expected {@link WebTemplate} result.
	 */
	private static String createExpectedTemplateResult(String[] sections) {

		// Create the resulting template
		StringBuilder content = new StringBuilder();
		for (String section : sections) {
			content.append(section + " ");
		}

		// Return the template
		return content.toString();
	}

}
