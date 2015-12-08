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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.frame.spi.source.ResourceSource;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ReadOnlyConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>
 * {@link ConfigurationContext} obtaining {@link ConfigurationItem} instances
 * from a single XML file.
 * <p>
 * This reduces the number of files required for unit testing.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlConfigurationContext implements ConfigurationContext,
		ResourceSource {

	/**
	 * Location of this {@link XmlConfigurationContext}.
	 */
	private final String location;

	/**
	 * {@link ConfigurationItem} instances for this
	 * {@link XmlConfigurationContext}.
	 */
	private final Map<String, ConfigurationItem> items = new HashMap<String, ConfigurationItem>();

	/**
	 * XML raw text before parsing into the {@link ConfigurationItem} instances.
	 */
	private String xmlText;

	/**
	 * Obtains the {@link ConfigurationContext} contained in the XML file found
	 * in the package of the offset object.
	 * 
	 * @param offsetObject
	 *            Offset object identifying the package containing the XML file.
	 *            This is typically the {@link TestCase} class.
	 * @param singleXmlFileName
	 *            Name of the XML file.
	 * @throws Exception
	 *             If fails to initialise.
	 */
	public XmlConfigurationContext(Object offsetObject, String singleXmlFileName)
			throws Exception {

		// Obtain the location of the XML file
		this.location = offsetObject.getClass().getPackage().getName()
				.replace('.', '/')
				+ "/" + singleXmlFileName;

		// Obtain the raw XML text from the resource
		InputStream inputStream = offsetObject.getClass().getClassLoader()
				.getResourceAsStream(this.location);
		if (inputStream == null) {
			throw new FileNotFoundException("Can not find XML resource: "
					+ this.location);
		}
		StringWriter writer = new StringWriter();
		Reader reader = new InputStreamReader(inputStream);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			writer.write(value);
		}
		this.xmlText = writer.toString();
	}

	/**
	 * Adds tag replacement of the raw XML text before it is parsed and divided
	 * into {@link ConfigurationItem} instances.
	 * 
	 * @param tagName
	 *            Name of the tag. This will replace <code>${tagName}</code>
	 *            text with the tag replace value.
	 * @param tagReplaceValue
	 *            Value to replace the tag with.
	 */
	public void addTag(String tagName, String tagReplaceValue) {

		// Ensure not attempted to obtain configuration item
		if (this.xmlText == null) {
			throw new IllegalStateException(
					"Can not replace tags after using context methods");
		}

		// Do the tag replacement
		this.xmlText = this.xmlText.replace("${" + tagName + "}",
				tagReplaceValue);
	}

	/**
	 * Ensures the XML text has been parsed into {@link ConfigurationItem}
	 * instances.
	 * 
	 * @throws Exception
	 *             If fails to parse.
	 */
	private void ensureParsedIntoConfigurationItems() throws Exception {

		// Determine if parsed
		if (this.xmlText == null) {
			return; // already parsed into configuration items
		}

		// Create the input source for the XML text
		InputSource inputSource = new InputSource(
				new StringReader(this.xmlText));

		// Parse the contents
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(inputSource, new XmlConfigurationHandler());

		// Flag that now parsed
		this.xmlText = null;
	}

	/*
	 * ====================== ResourceSource =============================
	 */

	@Override
	public InputStream sourceResource(String location) {
		try {
			ConfigurationItem item = this.getConfigurationItem(location);
			return (item == null ? null : item.getConfiguration());
		} catch (Exception ex) {
			return null; // failed to obtain resource
		}
	}

	/*
	 * ====================== ConfigurationContext =============================
	 */

	@Override
	public String getLocation() {
		return this.location;
	}

	@Override
	public ConfigurationItem getConfigurationItem(String relativeLocation)
			throws Exception {
		this.ensureParsedIntoConfigurationItems();
		return this.items.get(relativeLocation);
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public ConfigurationItem createConfigurationItem(String relativeLocation,
			InputStream configuration) throws Exception {
		throw new ReadOnlyConfigurationException("Can not create "
				+ ConfigurationItem.class.getSimpleName()
				+ " instances within a "
				+ XmlConfigurationContext.class.getSimpleName());
	}

	@Override
	public void deleteConfigurationItem(String relativeLocation)
			throws Exception, ReadOnlyConfigurationException {
		throw new ReadOnlyConfigurationException("Can not delete "
				+ ConfigurationItem.class.getSimpleName()
				+ " instances within a "
				+ XmlConfigurationContext.class.getSimpleName());
	}

	/**
	 * {@link DefaultHandler} to read in the XML configuration.
	 */
	private class XmlConfigurationHandler extends DefaultHandler {

		/**
		 * Location of the current {@link ConfigurationItem} being read in.
		 */
		private String location = null;

		/**
		 * XML text of the current {@link ConfigurationItem} being read in.
		 */
		private StringBuilder configuration = null;

		/**
		 * Flag indicating if label elements being used to provide locations.
		 */
		private boolean isLabels;

		/**
		 * Element stack to indicate when {@link ConfigurationItem} read in.
		 */
		private Deque<String> elementStack;

		/*
		 * ================== DefaultHandler ==============================
		 */

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {

			// Determine if the root element
			if (this.elementStack == null) {
				// Determine if using labels for locations (default is false)
				String isLabelsValue = attributes.getValue("item-labels");
				this.isLabels = Boolean.parseBoolean(CompileUtil
						.isBlank(isLabelsValue) ? Boolean.FALSE.toString()
						: isLabelsValue);

				// Specify element stack and ignore root element
				this.elementStack = new LinkedList<String>();
				return; // ignore root element
			}

			// Determine if new configuration
			if (this.configuration == null) {
				// New configuration
				this.location = name;
				this.configuration = new StringBuilder();

				// Do not include element if using labels
				if (this.isLabels) {
					return; // element to only provide location, not content
				}
			}

			// Add the element
			this.elementStack.push(name);

			// Write the element details to configuration
			this.configuration.append("<");
			this.configuration.append(name);
			for (int i = 0; i < attributes.getLength(); i++) {
				this.configuration.append(" ");
				this.configuration.append(attributes.getQName(i));
				this.configuration.append("=\"");
				this.configuration.append(attributes.getValue(i));
				this.configuration.append("\"");
			}
			this.configuration.append(">");
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			// Write the characters to configuration
			if (this.configuration != null) {
				this.configuration.append(ch, start, length);
			}
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			// Do not ignore white spacing
			this.characters(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {

			// Determine if reading in configuration
			if (this.elementStack.isEmpty()) {
				return; // not reading in configuration
			}

			// Pop the name
			this.elementStack.pop();

			// Write the configuration
			this.configuration.append("</");
			this.configuration.append(name);
			this.configuration.append(">");

			// Determine if last element of configuration
			if (this.elementStack.isEmpty()) {
				// Last element, so have configuration
				String xmlText = this.configuration.toString();
				this.configuration = null;

				// Add the configuration item
				XmlConfigurationContext.this.items.put(this.location,
						new XmlConfigurationItem(this.location, xmlText,
								XmlConfigurationContext.this));
			}
		}
	}

}