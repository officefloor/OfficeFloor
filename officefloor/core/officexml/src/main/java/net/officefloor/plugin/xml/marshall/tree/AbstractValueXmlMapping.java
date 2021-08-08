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

package net.officefloor.plugin.xml.marshall.tree;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;
import net.officefloor.plugin.xml.marshall.translate.Translator;

/**
 * Abstract {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping} to aid
 * in writing an attribute/value.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractValueXmlMapping extends AbstractXmlMapping {

	/**
	 * Name of attribute/value.
	 */
	protected final String tagName;

	/**
	 * {@link Translator} to translate the return value of the method to a
	 * string value for XML.
	 */
	protected final Translator translator;

	/**
	 * Indicates whether the substitution of special characters of XML should
	 * not occur.
	 */
	protected final boolean isUseRaw;

	/**
	 * Initiate with items to write an attribute/value.
	 * 
	 * @param tagName
	 *            Name of attribute/value.
	 * @param getMethod
	 *            Method to obtain value from object.
	 * @param translator
	 *            Translates the object to string value for XML.
	 * @param isUseRaw
	 *            Indicates not to substitute special characters of XML and use
	 *            raw translated value.
	 */
	public AbstractValueXmlMapping(String tagName, Method getMethod,
			Translator translator, boolean isUseRaw) {
		super(getMethod);

		// Store state
		this.tagName = tagName;
		this.translator = translator;
		this.isUseRaw = isUseRaw;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.AbstractXmlMapping#writeXml(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void writeXml(Object value, XmlOutput output)
			throws XmlMarshallException {

		// Translate value to string
		String stringValue = this.translator.translate(value);

		// Transform for XML if required
		if (!this.isUseRaw) {
			stringValue = XmlMarshallerUtil.transformValueForXml(stringValue);
		}

		// Write the XML
		this.writeXml(this.tagName, stringValue, output);
	}

	/**
	 * Writes the XML.
	 * 
	 * @param tagName
	 *            Tag name for writing XML snippet.
	 * @param value
	 *            Value of XML snippet.
	 * @param output
	 *            Output to send the XML snippet.
	 * @throws XmlMarshallException
	 *             If fails to write XML.
	 */
	protected abstract void writeXml(String tagName, String value,
			XmlOutput output) throws XmlMarshallException;
}
