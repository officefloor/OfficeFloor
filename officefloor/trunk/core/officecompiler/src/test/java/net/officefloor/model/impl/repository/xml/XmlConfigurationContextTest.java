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
package net.officefloor.model.impl.repository.xml;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.xml.XmlConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Tests the {@link XmlConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlConfigurationContextTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load the {@link ConfigurationItem} instances contained in the
	 * XML file without labels.
	 */
	public void testXmlConfigurationWithoutLabels() throws Exception {

		// Create the XML configuration context
		ConfigurationContext context = new XmlConfigurationContext(this,
				"XmlConfigurationContextWithoutLabels.xml");

		// Validate the first configuration item
		ConfigurationItem one = context.getConfigurationItem("one");
		this.validateConfigurationItem(one, "one",
				"<one name=\"value\">first</one>");

		// Validate the second configuration item
		ConfigurationItem two = context.getConfigurationItem("two");
		this
				.validateConfigurationItem(two, "two",
						"<two><element attribute=\"something\">another</element></two>");
	}

	/**
	 * Ensure can load the {@link ConfigurationItem} instances contained in the
	 * XML file with labels.
	 */
	public void testXmlConfigurationWithLabels() throws Exception {

		// Create the XML configuration context
		ConfigurationContext context = new XmlConfigurationContext(this,
				"XmlConfigurationContextWithLabels.xml");

		// Validate the first configuration item
		ConfigurationItem one = context.getConfigurationItem("label-one");
		this.validateConfigurationItem(one, "label-one",
				"<one name=\"value\">first</one>");

		// Validate the second configuration item
		ConfigurationItem two = context.getConfigurationItem("label-two");
		this
				.validateConfigurationItem(two, "label-two",
						"<two><element attribute=\"something\">another</element></two>");
	}

	/**
	 * Ensure that can do tag replacement before parsing the XML.
	 */
	public void testXmlConfigurationTagReplace() throws Exception {

		// Create the XML configuration context
		XmlConfigurationContext context = new XmlConfigurationContext(this,
				"XmlConfigurationContextTagReplace.xml");

		// Do tag replacement
		context.addTag("TAG", "VALUE");

		// Validate the tag replacement
		ConfigurationItem item = context.getConfigurationItem("tag");
		this.validateConfigurationItem(item, "tag", "<tag>VALUE</tag>");
	}

	/**
	 * Validates the {@link ConfigurationItem}.
	 * 
	 * @param configurationItem
	 *            {@link ConfigurationItem} to validate.
	 * @param expectedLocation
	 *            Expected location.
	 * @param expectedContent
	 *            Expected content.
	 * @throws Exception
	 *             If fails to obtain {@link ConfigurationItem} details.
	 */
	private void validateConfigurationItem(ConfigurationItem configurationItem,
			String expectedLocation, String expectedContent) throws Exception {

		// Ensure correct location
		assertEquals("Incorrect location", expectedLocation, configurationItem
				.getLocation());

		// Obtain the configuration item content
		StringWriter writer = new StringWriter();
		Reader reader = new InputStreamReader(configurationItem
				.getConfiguration());
		for (int value = reader.read(); value != -1; value = reader.read()) {
			writer.write(value);
		}

		// Ensure the content matches
		String content = writer.toString();
		assertTextEquals("Incorrect content", expectedContent, content.trim());
	}
}