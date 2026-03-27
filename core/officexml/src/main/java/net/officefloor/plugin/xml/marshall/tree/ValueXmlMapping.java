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
 * Writes a XML value with value sourced from object.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueXmlMapping extends AbstractValueXmlMapping {

	/**
	 * From super.
	 * 
	 * @param tagName
	 *            Name of the XML tag.
	 * @param getMethod
	 *            {@link Method} to obtain the value.
	 * @param translator
	 *            {@link Translator}.
	 * @param isUseRaw
	 *            Indicates to use raw.
	 */
	public ValueXmlMapping(String tagName, Method getMethod,
			Translator translator, boolean isUseRaw) {
		super(tagName, getMethod, translator, isUseRaw);
	}

	@Override
	protected void writeXml(String tagName, String value, XmlOutput output)
			throws XmlMarshallException {

		// Write value based on value being null
		XmlMarshallerUtil.writeXml("<", output);
		XmlMarshallerUtil.writeXml(tagName, output);
		if (value == null) {
			// Value null
			XmlMarshallerUtil.writeXml("/>", output);
		} else {
			// Value supplied
			XmlMarshallerUtil.writeXml(">", output);
			XmlMarshallerUtil.writeXml(value, output);
			XmlMarshallerUtil.writeXml("</", output);
			XmlMarshallerUtil.writeXml(tagName, output);
			XmlMarshallerUtil.writeXml(">", output);
		}
	}

}
