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

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.template.parse.ParsedTemplateSection;
import net.officefloor.web.template.parse.WebTemplateParserImpl;

/**
 * Tests inheriting {@link ParsedTemplateSection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateSectionInheritanceTest extends OfficeFrameTestCase {

	/**
	 * {@link SectionDesigner}.
	 */
	private final SectionDesigner designer = this.createMock(SectionDesigner.class);

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
	public void testOverrideWithIntroducedSectionExisting() {
		this.designer.addIssue(
				"Section 'introduced' already exists by inheritance and not flagged for overriding (with ':' prefix)");
		this.doTest(new String[] { "start", "override", "introduced", "end" },
				new String[] { ":override", "introduced" }, "p:start", "c:override", "p:introduced", "p:end");
	}

	/**
	 * Ensure that first section is not overridden unless provided with override
	 * prefix. The default first child section is considered a comment section,
	 * as typical overriding should occur within the body.
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
	public void testFirstSectionNotOverriding() {
		this.designer.addIssue(
				"Section 'override' can not be introduced, as no previous override section (section prefixed with ':') to identify where to inherit");
		this.doTest(new String[] { "override" }, new String[] { "override" }, "p:override");
	}

	/**
	 * Ensure issue if section not overriding.
	 */
	public void testSectionNotOverriding() {
		this.designer.addIssue("No inherited section exists for overriding by section 'override'");
		this.doTest(new String[] { "template" }, new String[] { "template", ":override" }, "p:template");
	}

	/**
	 * Ensure that allow parent template to be re-ordered without requiring
	 * changes to child inheritance. This allows parent sections to be out of
	 * order to the child overriding sections.
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
	 * @param parentSectionNames
	 *            Parent {@link ParsedTemplateSection} names.
	 * @param childSectionNames
	 *            Child {@link ParsedTemplateSection} names.
	 * @param resultingSections
	 *            Resulting {@link ParsedTemplateSection} contents. Allows to
	 *            distinguish between parent and child by same name for
	 *            inheritance.
	 */
	private void doTest(String[] parentSectionNames, String[] childSectionNames, String... resultingSections) {

		// Create the list of parent and child sections
		ParsedTemplateSection[] parentSections = createHttpTemplateSections(parentSectionNames, "p:");
		ParsedTemplateSection[] childSections = createHttpTemplateSections(childSectionNames, "c:");

		// Filter out section comments
		parentSections = WebTemplateSectionSource.filterCommentHttpTemplateSections(parentSections);
		childSections = WebTemplateSectionSource.filterCommentHttpTemplateSections(childSections);

		// Test
		this.replayMockObjects();

		// Undertake the inheritance
		ParsedTemplateSection[] inheritedSections = WebTemplateSectionSource.inheritParsedTemplateSections(parentSections,
				childSections, this.designer);

		// Verify
		this.verifyMockObjects();

		// Verify the sections
		assertEquals("Incorrect number of resulting sections", resultingSections.length, inheritedSections.length);
		for (int i = 0; i < resultingSections.length; i++) {
			assertEquals("Incorrect section " + i, resultingSections[i], inheritedSections[i].getRawSectionContent());
		}

		// Construct the expected inherited template content
		StringBuilder expectedTemplateContent = new StringBuilder();
		boolean isFirstSection = true;
		for (String resultingSection : resultingSections) {

			// Obtain the section name
			String sectionName = resultingSection.split(":")[1];

			// Append the section details
			if ((isFirstSection) && (WebTemplateParserImpl.DEFAULT_FIRST_SECTION_NAME.equals(sectionName))) {
				// Include only the content for default first section
				expectedTemplateContent.append(resultingSection);

			} else {
				expectedTemplateContent.append("<!-- {" + sectionName + "} -->" + resultingSection);
			}

			// No longer first section
			isFirstSection = false;
		}

		// Ensure the reconstructed template content is as expected
		String reconstructedTemplateContent = WebTemplateSectionSource
				.reconstructParsedTemplateContent(inheritedSections);
		assertEquals("Incorrect reconstructed inherited template content", expectedTemplateContent.toString(),
				reconstructedTemplateContent);
	}

	/**
	 * Creates the list of {@link ParsedTemplateSection} instances.
	 * 
	 * @param sectionNames
	 *            Names of the {@link ParsedTemplateSection} instances.
	 * @param contentPrefix
	 *            Prefix for the {@link ParsedTemplateSection} content.
	 * @return {@link ParsedTemplateSection} instances.
	 */
	private static ParsedTemplateSection[] createHttpTemplateSections(String[] sectionNames, String contentPrefix) {

		// Create the list of sections
		ParsedTemplateSection[] sections = new ParsedTemplateSection[sectionNames.length];
		for (int i = 0; i < sections.length; i++) {
			String sectionName = sectionNames[i];
			String sectionContent = contentPrefix
					+ (sectionName.startsWith(":") ? sectionName.substring(":".length()) : sectionName);
			sections[i] = new ParsedTemplateSection(sectionName, sectionContent, null);
		}

		// Return the list of sections
		return sections;
	}

}