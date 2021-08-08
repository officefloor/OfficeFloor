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

package net.officefloor.plugin.xml.unmarshall.flat;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlUnmarshaller;
import net.officefloor.plugin.xml.unmarshall.load.DynamicValueLoader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML unmarshaller to load a flat object.
 * 
 * @author Daniel Sagenschneider
 */
public class FlatXmlUnmarshaller implements XmlUnmarshaller {

    /**
     * Parses the XML.
     */
    protected final SAXParser parser;

    /**
     * Handler to load the XML to the target object.
     */
    protected final HandlerImpl handler;

    /**
     * Initiate with meta-data to unmarshall the XML.
     * 
     * @param metaData
     *            Meta-data to unmarshall XML.
     * @throws XmlMarshallException
     *             Should fail to initiate instance.
     */
    public FlatXmlUnmarshaller(FlatXmlUnmarshallerMetaData metaData)
            throws XmlMarshallException {
        // Store state
        this.handler = new HandlerImpl(metaData);

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
     * @see net.officefloor.plugin.xml.XmlUnmarshaller#unmarshall(java.io.Reader,
     *      java.lang.Object)
     */
    public void unmarshall(InputStream xml, Object target)
            throws XmlMarshallException {

        // Set the target object to load it
        this.handler.setTargetObject(target);

        // Load XML values
        try {
            parser.parse(xml, this.handler);
        } catch (SAXException ex) {
            // Propagate failure
            throw new XmlMarshallException(ex.getMessage(),
                    (ex.getCause() == null ? ex : ex.getCause()));
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
    protected static class HandlerImpl extends DefaultHandler {

        /**
         * Separator of the attribute and element name for loading.
         */
        protected static final char ATTRIBUTE_SEPARATOR = '&';

        /**
         * Meta-data to load the target object.
         */
        protected final FlatXmlUnmarshallerMetaData metaData;

        /**
         * Target object to load XML values on.
         */
        protected Object targetObject = null;

        /**
         * Element value.
         */
        protected String elementValue = null;

        /**
         * Initiate with meta-data.
         * 
         * @param metaData
         *            Meta-data for unmarshalling the XML.
         */
        public HandlerImpl(FlatXmlUnmarshallerMetaData metaData) {
            // Initiate state
            this.metaData = metaData;
        }

        /**
         * Sets the target object.
         * 
         * @param targetObject
         *            Target object to have values loaded to it.
         */
        public void setTargetObject(Object targetObject) {
            this.targetObject = targetObject;
        }

        /**
         * Handles loading the attribute value on the target value.
         */
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            // Load the attribute values
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    // Obtain the load reference name
                    String referenceElement = qName + ATTRIBUTE_SEPARATOR
                            + attributes.getQName(i);

                    // Obtain the value
                    String value = attributes.getValue(i);

                    // Load the value
                    this.loadValue(referenceElement, value);
                }
            }
        }

        /**
         * Handles obtain the value for the element.
         */
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            // Obtain value
            this.elementValue = String.valueOf(ch, start, length);
        }

        /**
         * Handles loading the value onto the target value.
         */
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            // Load value
            this.loadValue(qName, this.elementValue);
        }

        /**
         * Loads the value to the target object.
         * 
         * @param referenceElement
         *            Reference element.
         * @param value
         *            Value to be loaded for the reference element.
         * @throws SAXException
         *             Should fail to load the value to the target object.
         */
        private void loadValue(String referenceElement, String value)
                throws SAXException {
            // Obtain Value Loader
            DynamicValueLoader valueLoader = this.metaData
                    .getValueLoader(referenceElement);

            // Load value if have loader
            if (valueLoader != null) {
                try {
                    valueLoader.loadValue(this.targetObject, value);
                } catch (XmlMarshallException ex) {
                    // Propagate failure
                    throw new SAXException(ex);
                }
            }
        }
    }

}
