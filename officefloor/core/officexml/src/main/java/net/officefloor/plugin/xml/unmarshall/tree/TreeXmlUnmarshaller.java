/*-
 * #%L
 * OfficeXml
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

package net.officefloor.plugin.xml.unmarshall.tree;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.translate.TranslatorRegistry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implementation of {@link net.officefloor.plugin.xml.XmlUnmarshaller} that is
 * capable of unmarshalling a tree structure of objects from XML.
 * 
 * @author Daniel Sagenschneider
 */
public class TreeXmlUnmarshaller implements XmlUnmarshaller {

	/**
	 * Separator of the attribute and element name for loading.
	 */
	protected static final String ATTRIBUTE_SEPARATOR = "@";

	/**
	 * Parses the XML.
	 */
	protected final SAXParser parser;

	/**
	 * Handler to load the XML to the target object.
	 */
	protected final HandlerImpl handler;

	/**
	 * Initiate with details to unmarshall the XML.
	 * 
	 * @param metaData           Meta-data of the mappings.
	 * @param translatorRegistry Registry of translators.
	 * @param referenceRegistry  Registry of referenced {@link XmlMapping}
	 *                           instances.
	 * @throws XmlMarshallException If fail to configure.
	 */
	public TreeXmlUnmarshaller(XmlMappingMetaData metaData, TranslatorRegistry translatorRegistry,
			ReferencedXmlMappingRegistry referenceRegistry) throws XmlMarshallException {

		// Obtain the target object class
		String targetObjectClassName = metaData.getLoadObjectClassName();
		Class<?> targetObjectClass;
		try {
			targetObjectClass = Class.forName(targetObjectClassName);
		} catch (ClassNotFoundException ex) {
			throw new XmlMarshallException("Can not find class of target object '" + targetObjectClassName + "'", ex);
		}

		// Initiate handler
		this.handler = new HandlerImpl(new XmlState(new XmlContext(targetObjectClass, metaData.getElementName(),
				metaData.getLoadObjectConfiguration(), translatorRegistry, referenceRegistry)));

		// Create the parser
		try {
			this.parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception ex) {
			// Propagate failure
			throw new XmlMarshallException("Failure to create a SAX parser");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.plugin.xml.XmlUnmarshaller#unmarshall(java.io.InputStream ,
	 * java.lang.Object)
	 */
	public void unmarshall(InputStream xml, Object target) throws XmlMarshallException {
		// Set the target object to load it
		this.handler.setTargetObject(target);

		// Load XML values
		try {
			parser.parse(xml, this.handler);
		} catch (UnmarshallException ex) {
			// Propagate failure
			throw ex.getXmlMarshallException();
		} catch (SAXException ex) {
			// Propagate failure
			throw new XmlMarshallException(ex.getMessage(), (ex.getCause() == null ? ex : ex.getCause()));
		} catch (IOException ex) {
			// Propagate failure
			throw new XmlMarshallException(ex.getMessage(), ex);
		} finally {
			// Disassociate target object after loading
			this.handler.setTargetObject(null);
		}
	}

	/**
	 * Handler to load XML values onto target object.
	 */
	public static class HandlerImpl extends DefaultHandler {

		/**
		 * State of the XML unmarshalling.
		 */
		protected final XmlState state;

		/**
		 * Element value.
		 */
		protected String elementValue = null;

		/**
		 * Initiate with state of XML unmarshalling.
		 * 
		 * @param state State of XML unmarshalling.
		 */
		public HandlerImpl(XmlState state) {
			// Store state
			this.state = state;
		}

		/**
		 * Sets the target object.
		 * 
		 * @param targetObject Target object to have values loaded to it.
		 */
		public void setTargetObject(Object targetObject) {
			this.state.setTargetObject(targetObject);
		}

		/**
		 * Handles loading the attribute value on the target value.
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			try {
				// Obtain the element xml mapping for element
				ElementXmlMapping mapping = this.state.getCurrentContext().getElementXmlMapping(qName);
				if (mapping != null) {

					// Start the mapping of the element
					XmlMapping elementMapping = mapping.getElementXmlMapping();
					if (elementMapping != null) {
						elementMapping.startMapping(this.state, qName);
					}

					// Map the static mappings for element
					List<XmlMapping> staticMappings = mapping.getStaticXmlMappings();
					if (staticMappings != null) {
						for (XmlMapping staticMapping : staticMappings) {
							// Do the mapping
							staticMapping.startMapping(this.state, qName);
							staticMapping.endMapping(this.state, qName);
						}
					}

					// Map the attributes
					AttributeXmlMappings attributeMappings = mapping.getAttributeXmlMappings();
					if (attributeMappings != null) {

						// Process the attributes
						if (attributes != null) {
							for (int i = 0; i < attributes.getLength(); i++) {

								// Obtain the name of the attribute
								String attributeName = attributes.getQName(i);

								// Obtain the xml mapping for attribute
								XmlMapping attributeMapping = attributeMappings.getXmlMapping(attributeName);

								if (attributeMapping != null) {
									// Do the mapping
									attributeMapping.startMapping(this.state, attributeName);
									attributeMapping.endMapping(this.state, attributes.getValue(i));
								}
							}
						}
					}
				}
			} catch (XmlMarshallException ex) {
				// Propagate
				throw new UnmarshallException(ex);
			}
		}

		/**
		 * Handles obtain the value for the element.
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			// Obtain value
			this.elementValue = String.valueOf(ch, start, length);
		}

		/**
		 * Handles loading the value onto the target value.
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			try {
				// Check if to pop context
				String endElementName = this.state.getEndElementName();
				if ((endElementName != null) && (endElementName.equals(qName))) {
					// Pop context as ending element of context
					this.state.popContext();
				}

				// End xml mapping for element if exists
				ElementXmlMapping mapping = this.state.getCurrentContext().getElementXmlMapping(qName);
				if (mapping != null) {
					XmlMapping elementMapping = mapping.getElementXmlMapping();
					if (elementMapping != null) {
						// End the mapping
						elementMapping.endMapping(this.state, this.elementValue);
					}
				}

			} catch (XmlMarshallException ex) {
				// Propagate
				throw new UnmarshallException(ex);
			}
		}
	}
}

/**
 * Allows propagation of failures during parsing to handle.
 * 
 * @author Daniel Sagenschneider
 */
class UnmarshallException extends SAXException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Cause of unmarshalling failure.
	 */
	protected XmlMarshallException cause;

	/**
	 * Initiate with cause.
	 * 
	 * @param cause Cause.
	 */
	public UnmarshallException(XmlMarshallException cause) {
		super(cause);
		this.cause = cause;
	}

	/**
	 * Obtains the {@link XmlMarshallException} to propagate.
	 * 
	 * @return {@link XmlMarshallException} to propagate.
	 */
	public XmlMarshallException getXmlMarshallException() {
		return this.cause;
	}
}
