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

package net.officefloor.web.template.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.tree.TreeXmlUnmarshallerFactory;
import net.officefloor.server.http.HttpMethod;

/**
 * Tests the {@link WebTemplateParser}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateParserTest extends OfficeFrameTestCase {

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
			assertEquals("Incorrect cause",
					ParseException.class.getName() + ": No open Bean to close at line 3 column 31", ex.getMessage());
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
	 * Ensure can load link with configured {@link HttpMethod}.
	 */
	public void testLinkWithHttpMethod() {
		this.doTest();
	}

	/**
	 * Ensure can load link with multiple configured {@link HttpMethod}
	 * instances.
	 */
	public void testLinkWithMultipleHttpMethods() {
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
	 * @return {@link ParsedTemplate}.
	 */
	private ParsedTemplate doParse() throws IOException {

		// Obtain the template file name
		String templateFileName = this.getTemplateFileName();

		// Load the template
		File templateFile = this.findFile(this.getClass(), templateFileName + ".ofp");
		ParsedTemplate template = WebTemplateParser.parse(new StringReader(this.getFileContents(templateFile)));

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
			ParsedTemplate template = this.doParse();

			// Obtain unmarshaller to expected content
			File unmarshallerConfigFile = this.findFile(this.getClass(), "UnmarshallConfiguration.xml");
			XmlUnmarshaller unmarshaller = TreeXmlUnmarshallerFactory.getInstance()
					.createUnmarshaller(new FileInputStream(unmarshallerConfigFile));

			// Obtain the expected content
			File expectedFile = this.findFile(this.getClass(), templateFileName + ".xml");
			TemplateConfig expectedTemplate = new TemplateConfig();
			unmarshaller.unmarshall(new FileInputStream(expectedFile), expectedTemplate);

			// Ensure template is as expected
			ParsedTemplateSection[] sections = template.getSections();
			assertEquals("Incorrect number of sections", expectedTemplate.sections.size(), sections.length);
			for (int s = 0; s < sections.length; s++) {
				TemplateSectionConfig expectedSection = expectedTemplate.sections.get(s);
				ParsedTemplateSection section = sections[s];

				// Ensure details of section correct
				assertEquals("Incorrect name for section " + s, expectedSection.name, section.getSectionName());

				// Determine the section raw contents
				StringBuilder rawSectionContents = new StringBuilder();

				// Ensure section is as expected
				validateSectionOrBean(expectedSection.contents, section.getContent(), s, "", rawSectionContents);

				// Determine if raw section content is as expected
				assertEquals(
						"Incorrect raw section content for section " + expectedSection.name + "\n== Expected =======\n"
								+ rawSectionContents.toString() + "\n== Actual =========\n"
								+ section.getRawSectionContent() + "\n===================\n",
						transformContentForCompare(rawSectionContents.toString()),
						transformContentForCompare(section.getRawSectionContent()));
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
	 * Validates the {@link ParsedTemplateSection} and
	 * {@link BeanParsedTemplateSectionContent}.
	 * 
	 * @param expectedContents
	 *            Expected {@link TemplateSectionConfig} instances.
	 * @param actualContents
	 *            Actual {@link ParsedTemplateSectionContent}.
	 * @param sectionIndex
	 *            Index of the section being validated.
	 * @param beanPath
	 *            Path of the bean within the {@link ParsedTemplateSection}.
	 * @param rawSectionContents
	 *            {@link StringBuilder} to receive the expected raw section
	 *            contents.
	 */
	private static void validateSectionOrBean(List<TemplateSectionContentConfig> expectedContents,
			ParsedTemplateSectionContent[] actualContents, int sectionIndex, String beanPath,
			StringBuilder rawSectionContents) {

		// Ensure section/bean is as expected
		assertEquals("Incorrect number of content for section " + sectionIndex + " bean path '" + beanPath + "'",
				expectedContents.size(), actualContents.length);
		for (int c = 0; c < expectedContents.size(); c++) {
			TemplateSectionContentConfig expectedContent = expectedContents.get(c);
			ParsedTemplateSectionContent content = actualContents[c];

			// Obtain the message prefix
			String contentIdentifier = "Section " + sectionIndex + " Bean Path '" + beanPath + "' Content " + c;

			// Handle based on type of content expected
			if (expectedContent instanceof StaticTemplateSectionContentConfig) {
				// Static content
				assertTrue(contentIdentifier + " is expected to be static",
						content instanceof StaticParsedTemplateSectionContent);
				StaticTemplateSectionContentConfig expectedStaticContent = (StaticTemplateSectionContentConfig) expectedContent;
				StaticParsedTemplateSectionContent staticContent = (StaticParsedTemplateSectionContent) content;
				String expectedStaticText = (expectedStaticContent.content == null ? " "
						: expectedStaticContent.content);
				assertTextEquals("Incorrect content for " + contentIdentifier, expectedStaticText,
						staticContent.getStaticContent());

				// Include raw static content
				rawSectionContents.append(expectedStaticText);

			} else if (expectedContent instanceof BeanTemplateSectionContentConfig) {
				// Bean content
				assertTrue(contentIdentifier + " is expected to be bean",
						content instanceof BeanParsedTemplateSectionContent);
				BeanTemplateSectionContentConfig expectedBeanContent = (BeanTemplateSectionContentConfig) expectedContent;
				BeanParsedTemplateSectionContent beanContent = (BeanParsedTemplateSectionContent) content;
				String beanPropertyName = beanContent.getPropertyName();
				assertEquals("Incorrect bean property name for " + contentIdentifier, expectedBeanContent.beanName,
						beanPropertyName);

				// Include the raw bean open tag
				rawSectionContents.append(expectedBeanContent.getOpenTag());

				// Validate the bean content
				validateSectionOrBean(expectedBeanContent.contents, beanContent.getContent(), sectionIndex,
						beanPath + ("".equals(beanPath) ? "" : ".") + beanPropertyName, rawSectionContents);

				// Include the raw bean close tag
				rawSectionContents.append(expectedBeanContent.getCloseTag());

			} else if (expectedContent instanceof PropertyTemplateSectionContentConfig) {
				// Property content
				assertTrue(contentIdentifier + " is expected to be property",
						content instanceof PropertyParsedTemplateSectionContent);
				PropertyTemplateSectionContentConfig expectedPropertyContent = (PropertyTemplateSectionContentConfig) expectedContent;
				PropertyParsedTemplateSectionContent propertyContent = (PropertyParsedTemplateSectionContent) content;
				assertEquals("Incorrect property name for " + contentIdentifier, expectedPropertyContent.propertyName,
						propertyContent.getPropertyName());

				// Include the raw property
				rawSectionContents.append("${" + expectedPropertyContent.propertyName + "}");

			} else if (expectedContent instanceof LinkTemplateSectionContentConfig) {
				// Link content
				assertTrue(contentIdentifier + " is expected to be a link",
						content instanceof LinkParsedTemplateSectionContent);
				LinkTemplateSectionContentConfig expectedLinkContent = (LinkTemplateSectionContentConfig) expectedContent;
				LinkParsedTemplateSectionContent linkContent = (LinkParsedTemplateSectionContent) content;
				assertEquals("Incorrect link name for " + contentIdentifier, expectedLinkContent.name,
						linkContent.getName());

				// Include the raw link
				rawSectionContents.append("#{" + expectedLinkContent.name + "}");

			} else {
				// Unknown content
				fail("Unkonwn content type " + expectedContent.getClass().getName());
			}
		}
	}
}
