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
package net.officefloor.plugin.web.http.template.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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
	public void testStatic() {
		this.doTest();
	}

	/**
	 * Ensure can load static sections.
	 */
	public void testSections() {
		this.doTest();
	}
	
	/**
	 * Ensures able to name the first section, other than it being 'template'.
	 */
	public void testFirstSectionNamed() {
		this.doTest();
	}

	/**
	 * Ensure can load override sections.
	 */
	public void testOverrideSection() {
		this.doTest();
	}
	
	/**
	 * Ensure can have comment sections.
	 */
	public void testCommentSection() {
		this.doTest();
	}

	/**
	 * Ensure can load property content.
	 */
	public void testProperty() {
		this.doTest();
	}

	/**
	 * Ensure can load property content that is a reference.
	 */
	public void testPropertyReference() {
		this.doTest();
	}

	/**
	 * Ensure can load multiple properties.
	 */
	public void testMultipleProperties() {
		this.doTest();
	}

	/**
	 * Ensures able to load bean content.
	 */
	public void testBean() {
		this.doTest();
	}

	/**
	 * Ensure able to load bean with property.
	 */
	public void testBeanProperty() {
		this.doTest();
	}

	/**
	 * Ensure able to load mixed bean content.
	 */
	public void testBeanMixedContent() {
		this.doTest();
	}

	/**
	 * Ensure able to load tree of beans.
	 */
	public void testBeanTree() {
		this.doTest();
	}

	/**
	 * Ensure can put bean tags within comment. Allows for hiding from raw HTML
	 * rendering.
	 */
	public void testBeanWithinComment() {
		this.doTest();
	}

	/**
	 * Ensure that section closes any open beans.
	 */
	public void testSectionClosesBean() {
		this.doTest();
	}

	/**
	 * Ensure that EOF closes any open beans.
	 */
	public void testEofClosesBean() {
		this.doTest();
	}

	/**
	 * Ensure that there is an error on attempting to close a non-open bean.
	 */
	public void testErrorOnClosingNonOpenBean() {
		try {
			this.doParse();
			fail("Should not successfully parse template");
		} catch (IOException ex) {
			assertEquals("Incorrect cause", ParseException.class.getName()
					+ ": No open Bean to close at line 3 column 31",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can load link content.
	 */
	public void testLink() {
		this.doTest();
	}

	/**
	 * Ensure can load multiple links.
	 */
	public void testMultipleLinks() {
		this.doTest();
	}

	/**
	 * Ensure can list beans (provide section to be repeated for each bean
	 * listed).
	 */
	public void testList() {
		this.doTest();
	}

	/**
	 * Obtains the template file name.
	 * 
	 * @return Template file name.
	 */
	private String getTemplateFileName() {

		// Obtain the template file name
		String testName = this.getName();
		String templateFileName = testName.substring("test".length());

		// Return the template file name
		return templateFileName;
	}

	/**
	 * Undertakes parsing the template.
	 * 
	 * @return {@link HttpTemplate}.
	 */
	private HttpTemplate doParse() throws IOException {

		// Obtain the template file name
		String templateFileName = this.getTemplateFileName();

		// Load the template
		File templateFile = this.findFile(this.getClass(), templateFileName
				+ ".ofp");
		HttpTemplate template = new HttpTemplateParserImpl(new StringReader(
				this.getFileContents(templateFile))).parse();

		// Return the template
		return template;
	}

	/**
	 * Does the test.
	 */
	private void doTest() {
		try {

			// Obtain the template file name
			String templateFileName = this.getTemplateFileName();

			// Parse the template
			HttpTemplate template = this.doParse();

			// Obtain unmarshaller to expected content
			File unmarshallerConfigFile = this.findFile(this.getClass(),
					"UnmarshallConfiguration.xml");
			XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory
					.getInstance().createUnmarshaller(
							new FileInputStream(unmarshallerConfigFile));

			// Obtain the expected content
			File expectedFile = this.findFile(this.getClass(), templateFileName
					+ ".xml");
			TemplateConfig expectedTemplate = new TemplateConfig();
			unmarshaller.unmarshall(new FileInputStream(expectedFile),
					expectedTemplate);

			// Ensure template is as expected
			HttpTemplateSection[] sections = template.getSections();
			assertEquals("Incorrect number of sections",
					expectedTemplate.sections.size(), sections.length);
			for (int s = 0; s < sections.length; s++) {
				TemplateSectionConfig expectedSection = expectedTemplate.sections
						.get(s);
				HttpTemplateSection section = sections[s];

				// Ensure details of section correct
				assertEquals("Incorrect name for section " + s,
						expectedSection.name, section.getSectionName());

				// Determine the section raw contents
				StringBuilder rawSectionContents = new StringBuilder();

				// Ensure section is as expected
				validateSectionOrBean(expectedSection.contents,
						section.getContent(), s, "", rawSectionContents);

				// Determine if raw section content is as expected
				assertEquals(
						"Incorrect raw section content for section "
								+ expectedSection.name
								+ "\n== Expected =======\n"
								+ rawSectionContents.toString()
								+ "\n== Actual =========\n"
								+ section.getRawSectionContent()
								+ "\n===================\n",
						transformContentForCompare(rawSectionContents
								.toString()),
						transformContentForCompare(section
								.getRawSectionContent()));
			}

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Transforms the content for use in comparison.
	 * 
	 * @param rawContent
	 *            Raw content to be transformed for comparing.
	 * @return Transformed content for use in comparison.
	 */
	private static String transformContentForCompare(String rawContent) {
		// Ignore carriage returns and mark line feeds
		return rawContent.replace("\r", "").replace('\n', '|');
	}

	/**
	 * Validates the {@link HttpTemplateSection} and
	 * {@link BeanHttpTemplateSectionContent}.
	 * 
	 * @param expectedContents
	 *            Expected {@link TemplateSectionConfig} instances.
	 * @param actualContents
	 *            Actual {@link HttpTemplateSectionContent}.
	 * @param sectionIndex
	 *            Index of the section being validated.
	 * @param beanPath
	 *            Path of the bean within the {@link HttpTemplateSection}.
	 * @param rawSectionContents
	 *            {@link StringBuilder} to receive the expected raw section
	 *            contents.
	 */
	private static void validateSectionOrBean(
			List<TemplateSectionContentConfig> expectedContents,
			HttpTemplateSectionContent[] actualContents, int sectionIndex,
			String beanPath, StringBuilder rawSectionContents) {

		// Ensure section/bean is as expected
		assertEquals("Incorrect number of content for section " + sectionIndex
				+ " bean path '" + beanPath + "'", expectedContents.size(),
				actualContents.length);
		for (int c = 0; c < expectedContents.size(); c++) {
			TemplateSectionContentConfig expectedContent = expectedContents
					.get(c);
			HttpTemplateSectionContent content = actualContents[c];

			// Obtain the message prefix
			String contentIdentifier = "Section " + sectionIndex
					+ " Bean Path '" + beanPath + "' Content " + c;

			// Handle based on type of content expected
			if (expectedContent instanceof StaticTemplateSectionContentConfig) {
				// Static content
				assertTrue(contentIdentifier + " is expected to be static",
						content instanceof StaticHttpTemplateSectionContent);
				StaticTemplateSectionContentConfig expectedStaticContent = (StaticTemplateSectionContentConfig) expectedContent;
				StaticHttpTemplateSectionContent staticContent = (StaticHttpTemplateSectionContent) content;
				String expectedStaticText = (expectedStaticContent.content == null ? " "
						: expectedStaticContent.content);
				assertTextEquals("Incorrect content for " + contentIdentifier,
						expectedStaticText, staticContent.getStaticContent());

				// Include raw static content
				rawSectionContents.append(expectedStaticText);

			} else if (expectedContent instanceof BeanTemplateSectionContentConfig) {
				// Bean content
				assertTrue(contentIdentifier + " is expected to be bean",
						content instanceof BeanHttpTemplateSectionContent);
				BeanTemplateSectionContentConfig expectedBeanContent = (BeanTemplateSectionContentConfig) expectedContent;
				BeanHttpTemplateSectionContent beanContent = (BeanHttpTemplateSectionContent) content;
				String beanPropertyName = beanContent.getPropertyName();
				assertEquals("Incorrect bean property name for "
						+ contentIdentifier, expectedBeanContent.beanName,
						beanPropertyName);

				// Include the raw bean open tag
				rawSectionContents.append(expectedBeanContent.getOpenTag());

				// Validate the bean content
				validateSectionOrBean(expectedBeanContent.contents,
						beanContent.getContent(), sectionIndex,
						beanPath + ("".equals(beanPath) ? "" : ".")
								+ beanPropertyName, rawSectionContents);

				// Include the raw bean close tag
				rawSectionContents.append(expectedBeanContent.getCloseTag());

			} else if (expectedContent instanceof PropertyTemplateSectionContentConfig) {
				// Property content
				assertTrue(contentIdentifier + " is expected to be property",
						content instanceof PropertyHttpTemplateSectionContent);
				PropertyTemplateSectionContentConfig expectedPropertyContent = (PropertyTemplateSectionContentConfig) expectedContent;
				PropertyHttpTemplateSectionContent propertyContent = (PropertyHttpTemplateSectionContent) content;
				assertEquals(
						"Incorrect property name for " + contentIdentifier,
						expectedPropertyContent.propertyName,
						propertyContent.getPropertyName());

				// Include the raw property
				rawSectionContents.append("${"
						+ expectedPropertyContent.propertyName + "}");

			} else if (expectedContent instanceof LinkTemplateSectionContentConfig) {
				// Link content
				assertTrue(contentIdentifier + " is expected to be a link",
						content instanceof LinkHttpTemplateSectionContent);
				LinkTemplateSectionContentConfig expectedLinkContent = (LinkTemplateSectionContentConfig) expectedContent;
				LinkHttpTemplateSectionContent linkContent = (LinkHttpTemplateSectionContent) content;
				assertEquals("Incorrect link name for " + contentIdentifier,
						expectedLinkContent.name, linkContent.getName());

				// Include the raw link
				rawSectionContents
						.append("#{" + expectedLinkContent.name + "}");

			} else {
				// Unknown content
				fail("Unkonwn content type "
						+ expectedContent.getClass().getName());
			}
		}
	}
}