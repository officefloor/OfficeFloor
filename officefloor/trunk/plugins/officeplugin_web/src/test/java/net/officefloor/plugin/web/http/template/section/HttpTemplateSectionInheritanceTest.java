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

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSectionImpl;

/**
 * Tests inheriting {@link HttpTemplateSection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionInheritanceTest extends OfficeFrameTestCase {

	/**
	 * {@link SectionDesigner}.
	 */
	private final SectionDesigner designer = this
			.createMock(SectionDesigner.class);

	/**
	 * Tests simple inheritance.
	 */
	public void testSimpleInheritance() {
		this.doTest(new String[] { "template", "override", "footer" },
				new String[] { "template", ":override" }, "p:template",
				"c:override", "p:footer");
	}

	/**
	 * Ensure can override with introduced sections.
	 */
	public void testOverrideWithIntroducedSections() {
		this.doTest(new String[] { "start", "override", "end" }, new String[] {
				":override", "introduced" }, "p:start", "c:override",
				"c:introduced", "p:end");
	}

	/**
	 * Ensure introduced section does not already exist from inheritance.
	 */
	public void testOverrideWithIntroducedSectionExisting() {
		this.designer
				.addIssue(
						"Section 'introduced' already exists by inheritance and not flagged for overriding (with ':' prefix)",
						null, null);
		this.doTest(new String[] { "start", "override", "introduced", "end" },
				new String[] { ":override", "introduced" }, "p:start",
				"c:override", "p:introduced", "p:end");
	}

	/**
	 * Ensure that first section is not overridden unless provided with override
	 * prefix. The default first child section is considered a comment section,
	 * as typical overriding should occur within the body.
	 */
	public void testDefaultFirstSectionIsCommentSection() {
		this.doTest(new String[] { "template" }, new String[] { "template" },
				"p:template");
	}

	/**
	 * Ensure can override the default first section.
	 */
	public void testOverrideDefaultFirstSection() {
		this.doTest(new String[] { "template" }, new String[] { ":template" },
				"c:template");
	}

	/**
	 * Ensure issue as first section is not an override.
	 */
	public void testFirstSectionNotOverriding() {
		this.designer
				.addIssue(
						"Section 'override' can not be introduced, as no previous override section (section prefixed with ':') to identify where to inherit",
						null, null);
		this.doTest(new String[] { "override" }, new String[] { "override" },
				"p:override");
	}

	/**
	 * Ensure issue if section not overriding.
	 */
	public void testSectionNotOverriding() {
		this.designer
				.addIssue(
						"No inherited section exists for overriding by section 'override'",
						null, null);
		this.doTest(new String[] { "template" }, new String[] { "template",
				":override" }, "p:template");
	}

	/**
	 * Ensure that allow parent template to be re-ordered without requiring
	 * changes to child inheritance. This allows parent sections to be out of
	 * order to the child overriding sections.
	 */
	public void testOverrideInDifferentOrder() {
		this.doTest(new String[] { "first", "third", "second", "fourth" },
				new String[] { ":second", "introduced", ":third", "another" },
				"p:first", "c:third", "c:another", "c:second", "c:introduced",
				"p:fourth");
	}

	/**
	 * Ensure ignore comment section.
	 */
	public void testIgnoreCommentSection() {
		this.doTest(new String[] { "!", "start", "!", "override", "!", "end",
				"!" },
				new String[] { "!", ":override", "!", "introduced", "!" },
				"p:start", "c:override", "c:introduced", "p:end");
	}

	/**
	 * Ensure can continue to match if parent has override sections.
	 */
	public void testGrandChildOverride() {
		this.doTest(new String[] { "template", ":override", "footer" },
				new String[] { "template", ":override", "introduced" },
				"p:template", "c:override", "c:introduced", "p:footer");
	}

	/**
	 * Undertakes the inheritance test.
	 * 
	 * @param parentSectionNames
	 *            Parent {@link HttpTemplateSection} names.
	 * @param childSectionNames
	 *            Child {@link HttpTemplateSection} names.
	 * @param resultingSections
	 *            Resulting {@link HttpTemplateSection} contents. Allows to
	 *            distinguish between parent and child by same name for
	 *            inheritance.
	 */
	private void doTest(String[] parentSectionNames,
			String[] childSectionNames, String... resultingSections) {

		// Create the list of parent and child sections
		HttpTemplateSection[] parentSections = createHttpTemplateSections(
				parentSectionNames, "p:");
		HttpTemplateSection[] childSections = createHttpTemplateSections(
				childSectionNames, "c:");

		// Filter out section comments
		parentSections = HttpTemplateSectionSource
				.filterCommentHttpTemplateSections(parentSections);
		childSections = HttpTemplateSectionSource
				.filterCommentHttpTemplateSections(childSections);

		// Test
		this.replayMockObjects();

		// Undertake the inheritance
		HttpTemplateSection[] inheritedSections = HttpTemplateSectionSource
				.inheritHttpTemplateSections(parentSections, childSections,
						this.designer);

		// Verify
		this.verifyMockObjects();

		// Verify the sections
		assertEquals("Incorrect number of resulting sections",
				resultingSections.length, inheritedSections.length);
		for (int i = 0; i < resultingSections.length; i++) {
			assertEquals("Incorrect section " + i, resultingSections[i],
					inheritedSections[i].getRawSectionContent());
		}

		// Construct the expected inherited template content
		StringBuilder expectedTemplateContent = new StringBuilder();
		for (String resultingSection : resultingSections) {

			// Obtain the section name
			String sectionName = resultingSection.split(":")[1];

			// Append the section details
			expectedTemplateContent.append("<!-- {" + sectionName + "} -->"
					+ resultingSection);
		}

		// Ensure the reconstructed template content is as expected
		String reconstructedTemplateContent = HttpTemplateSectionSource
				.reconstructHttpTemplateContent(inheritedSections);
		assertEquals("Incorrect reconstructed inherited template content",
				expectedTemplateContent.toString(),
				reconstructedTemplateContent);
	}

	/**
	 * Creates the list of {@link HttpTemplateSection} instances.
	 * 
	 * @param sectionNames
	 *            Names of the {@link HttpTemplateSection} instances.
	 * @param contentPrefix
	 *            Prefix for the {@link HttpTemplateSection} content.
	 * @return {@link HttpTemplateSection} instances.
	 */
	private static HttpTemplateSection[] createHttpTemplateSections(
			String[] sectionNames, String contentPrefix) {

		// Create the list of sections
		HttpTemplateSection[] sections = new HttpTemplateSection[sectionNames.length];
		for (int i = 0; i < sections.length; i++) {
			String sectionName = sectionNames[i];
			String sectionContent = contentPrefix
					+ (sectionName.startsWith(":") ? sectionName.substring(":"
							.length()) : sectionName);
			sections[i] = new HttpTemplateSectionImpl(sectionName,
					sectionContent, null);
		}

		// Return the list of sections
		return sections;
	}

}