/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.model.impl.repository.xml;

import java.io.Reader;
import java.io.StringWriter;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.configuration.XmlFileConfigurationContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link XmlFileConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlConfigurationContextTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load the {@link ConfigurationItem} instances contained in the XML
	 * file without labels.
	 */
	public void testXmlConfigurationWithoutLabels() throws Exception {

		// Create the XML configuration context
		ConfigurationContext context = new XmlFileConfigurationContext(this.getClass(),
				"XmlConfigurationContextWithoutLabels.xml");

		// Validate the first configuration item
		ConfigurationItem one = context.getConfigurationItem("one", null);
		this.validateConfigurationItem(one, "one", "<one name=\"value\">first</one>");

		// Validate the second configuration item
		ConfigurationItem two = context.getConfigurationItem("two", null);
		this.validateConfigurationItem(two, "two", "<two><element attribute=\"something\">another</element></two>");
	}

	/**
	 * Ensure can load the {@link ConfigurationItem} instances contained in the XML
	 * file with labels.
	 */
	public void testXmlConfigurationWithLabels() throws Exception {

		// Create the XML configuration context
		ConfigurationContext context = new XmlFileConfigurationContext(this.getClass(),
				"XmlConfigurationContextWithLabels.xml");

		// Validate the first configuration item
		ConfigurationItem one = context.getConfigurationItem("label-one", null);
		this.validateConfigurationItem(one, "label-one", "<one name=\"value\">first</one>");

		// Validate the second configuration item
		ConfigurationItem two = context.getConfigurationItem("label-two", null);
		this.validateConfigurationItem(two, "label-two",
				"<two><element attribute=\"something\">another</element></two>");
	}

	/**
	 * Ensure that can do tag replacement before parsing the XML.
	 */
	public void testXmlConfigurationTagReplace() throws Exception {

		// Create the XML configuration context
		XmlFileConfigurationContext context = new XmlFileConfigurationContext(this.getClass(),
				"XmlConfigurationContextTagReplace.xml");

		// Do tag replacement
		context.addProperty("TAG", "VALUE");

		// Validate the tag replacement
		ConfigurationItem item = context.getConfigurationItem("tag", null);
		this.validateConfigurationItem(item, "tag", "<tag>VALUE</tag>");
	}

	/**
	 * Validates the {@link ConfigurationItem}.
	 * 
	 * @param configurationItem {@link ConfigurationItem} to validate.
	 * @param expectedLocation  Expected location.
	 * @param expectedContent   Expected content.
	 * @throws Exception If fails to obtain {@link ConfigurationItem} details.
	 */
	private void validateConfigurationItem(ConfigurationItem configurationItem, String expectedLocation,
			String expectedContent) throws Exception {

		// Obtain the configuration item content
		StringWriter writer = new StringWriter();
		Reader reader = configurationItem.getReader();
		for (int value = reader.read(); value != -1; value = reader.read()) {
			writer.write(value);
		}

		// Ensure the content matches
		String content = writer.toString();
		assertTextEquals("Incorrect content", expectedContent, content.trim());
	}
}
