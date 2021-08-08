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

package net.officefloor.configuration.impl.configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.impl.ConfigurationContextImpl;
import net.officefloor.frame.api.source.ResourceSource;

/**
 * <p>
 * {@link ConfigurationContext} obtaining {@link ConfigurationItem} instances
 * from a single XML {@link File}.
 * <p>
 * This reduces the number of files required for unit testing.
 * 
 * @author Daniel Sagenschneider
 */
public class XmlFileConfigurationContext extends ConfigurationContextImpl
		implements PropertyConfigurable, ResourceSource {

	/**
	 * XML text instances for {@link ConfigurationItem} instances of this
	 * {@link XmlFileConfigurationContext}.
	 */
	private final Map<String, String> items = new HashMap<>();

	/**
	 * XML raw text before parsing into the {@link ConfigurationItem} instances.
	 */
	private String xmlText;

	/**
	 * Obtains the {@link ConfigurationContext} contained in the XML file found in
	 * the package of the offset object.
	 * 
	 * @param offsetClass       Offset {@link Class} identifying the package
	 *                          containing the XML file. This is typically the
	 *                          {@link TestCase} class.
	 * @param singleXmlFileName Name of the XML file.
	 * @throws Exception If fails to initialise.
	 */
	public XmlFileConfigurationContext(Class<?> offsetClass, String singleXmlFileName) throws Exception {
		super(null, null);

		// Obtain the location of the XML file
		String location = offsetClass.getPackage().getName().replace('.', '/') + "/" + singleXmlFileName;

		// Obtain the raw XML text from the resource
		InputStream inputStream = offsetClass.getClassLoader().getResourceAsStream(location);
		if (inputStream == null) {
			throw new FileNotFoundException("Can not find XML resource: " + location);
		}
		StringWriter writer = new StringWriter();
		Reader reader = new InputStreamReader(inputStream);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			writer.write(value);
		}
		this.xmlText = writer.toString();
	}

	/**
	 * Ensures the XML text has been parsed into {@link ConfigurationItem}
	 * instances.
	 * 
	 * @throws Exception If fails to parse.
	 */
	private void ensureParsedIntoConfigurationItems() throws IOException {

		// Determine if parsed
		if (this.xmlText == null) {
			return; // already parsed into configuration items
		}

		// Create the input source for the XML text
		InputSource inputSource = new InputSource(new StringReader(this.xmlText));

		try {
			// Parse the contents
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(inputSource, new XmlConfigurationHandler());
		} catch (SAXException | ParserConfigurationException ex) {
			throw new IOException(ex);
		}

		// Flag that now parsed
		this.xmlText = null;
	}

	/*
	 * ===================== PropertyConfigurable ========================
	 */

	@Override
	public void addProperty(String name, String value) {

		// Ensure not attempted to obtain configuration item
		if (this.xmlText == null) {
			throw new IllegalStateException("Can not replace tags after using context methods");
		}

		// Do the tag replacement
		this.xmlText = this.xmlText.replace("${" + name + "}", value);
	}

	/*
	 * ====================== ResourceSource =============================
	 */

	@Override
	public InputStream sourceResource(String location) {
		try {
			return this.getConfigurationSource().getConfigurationInputStream(location);
		} catch (Exception ex) {
			return null; // failed to obtain resource
		}
	}

	/*
	 * ====================== ConfigurationContext =============================
	 */

	@Override
	protected ConfigurationSource getConfigurationSource() {
		return (location) -> {
			this.ensureParsedIntoConfigurationItems();
			String xmlText = this.items.get(location);
			return (xmlText == null ? null : new ByteArrayInputStream(xmlText.getBytes()));
		};
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
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

			// Determine if the root element
			if (this.elementStack == null) {
				// Determine if using labels for locations (default is false)
				String isLabelsValue = attributes.getValue("item-labels");
				this.isLabels = Boolean
						.parseBoolean(CompileUtil.isBlank(isLabelsValue) ? Boolean.FALSE.toString() : isLabelsValue);

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
		public void characters(char[] ch, int start, int length) throws SAXException {
			// Write the characters to configuration
			if (this.configuration != null) {
				this.configuration.append(ch, start, length);
			}
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			// Do not ignore white spacing
			this.characters(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {

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
				XmlFileConfigurationContext.this.items.put(this.location, xmlText);
			}
		}
	}

}
