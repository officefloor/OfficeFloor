/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.template.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;

/**
 * Tests the {@link HttpTemplateParser}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpTemplateParserTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load static content.
	 */
	public void testStatic() throws Exception {
		this.doTest("Static");
	}

	/**
	 * Ensure can load static sections.
	 */
	public void testSections() throws Exception {
		this.doTest("Sections");
	}

	/**
	 * Ensures able to name the first section, other than it being 'template'.
	 */
	public void testFirstSectionNamed() throws Exception {
		this.doTest("FirstSectionNamed");
	}

	/**
	 * Ensure can load bean content.
	 */
	public void testBean() throws Exception {
		this.doTest("Bean");
	}

	/**
	 * Ensure can load multiple beans.
	 */
	public void testMultipleBeans() throws Exception {
		this.doTest("MultipleBeans");
	}

	/**
	 * Ensure can list beans (provide section to be repeated for each bean
	 * listed).
	 */
	public void testList() throws Exception {
		this.doTest("List");
	}

	/**
	 * Does the test.
	 *
	 * @param templateFileName
	 *            Name of template file.
	 */
	private void doTest(String templateFileName) throws Exception {

		// Obtain unmarshaller to expected content
		File unmarshallerConfigFile = this.findFile(this.getClass(),
				"UnmarshallConfiguration.xml");
		XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory
				.getInstance()
				.createUnmarshaller(new FileInputStream(unmarshallerConfigFile));

		// Obtain the expected content
		File expectedFile = this.findFile(this.getClass(), templateFileName
				+ ".xml");
		TemplateConfig expectedTemplate = new TemplateConfig();
		unmarshaller.unmarshall(new FileInputStream(expectedFile),
				expectedTemplate);

		// Load the template
		File templateFile = this.findFile(this.getClass(), templateFileName
				+ ".ofp");
		HttpTemplate template = new HttpTemplateParserImpl()
				.parse(new StringReader(this.getFileContents(templateFile)));

		// Ensure template is as expected
		HttpTemplateSection[] sections = template.getSections();
		assertEquals("Incorrect number of sections", expectedTemplate.sections
				.size(), sections.length);
		for (int s = 0; s < sections.length; s++) {
			TemplateSectionConfig expectedSection = expectedTemplate.sections
					.get(s);
			HttpTemplateSection section = sections[s];

			// Ensure details of section correct
			assertEquals("Incorrect name for section " + s,
					expectedSection.name, section.getSectionName());

			// Ensure section is as expected
			assertEquals("Incorrect number of content for section " + s,
					expectedSection.contents.size(),
					section.getContent().length);
			HttpTemplateSectionContent[] contents = section.getContent();
			for (int c = 0; c < contents.length; c++) {
				TemplateSectionContentConfig expectedContent = expectedSection.contents
						.get(c);
				HttpTemplateSectionContent content = contents[c];

				// Handle based on type of content expected
				if (expectedContent instanceof StaticTemplateSectionContentConfig) {
					// Static content
					assertTrue("Section " + s + " content " + c
							+ " is expected to be static",
							content instanceof StaticHttpTemplateSectionContent);
					StaticTemplateSectionContentConfig expectedStaticContent = (StaticTemplateSectionContentConfig) expectedContent;
					StaticHttpTemplateSectionContent staticContent = (StaticHttpTemplateSectionContent) content;
					assertTextEquals("Incorrect content for section " + s
							+ " content " + c, expectedStaticContent.content
							.trim(), staticContent.getStaticContent().trim());

				} else if (expectedContent instanceof ReferenceTemplateSectionContentConfig) {
					// Referenced content
					assertTrue(
							"Section " + s + " content " + c
									+ " is expected to be reference",
							content instanceof ReferenceHttpTemplateSectionContent);
					ReferenceTemplateSectionContentConfig expectedReferenceContent = (ReferenceTemplateSectionContentConfig) expectedContent;
					ReferenceHttpTemplateSectionContent referenceContent = (ReferenceHttpTemplateSectionContent) content;
					assertEquals("Incorrect reference key for section " + s
							+ " content " + c, expectedReferenceContent.key,
							referenceContent.getKey());

				} else {
					// Unknown content
					fail("Unkonwn content type "
							+ expectedContent.getClass().getName());
				}
			}
		}
	}

}