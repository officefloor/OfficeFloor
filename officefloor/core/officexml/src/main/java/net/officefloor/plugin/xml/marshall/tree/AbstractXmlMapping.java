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

/**
 * Abstract {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping} to aid
 * in mapping.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractXmlMapping implements XmlMapping, XmlWriter {

	/**
	 * Method to obtain value from object to map to XML.
	 */
	protected final Method getMethod;

	/**
	 * Initiate with method to obtain value to map.
	 * 
	 * @param getMethod
	 *            Method to obtain value to be mapped.
	 */
	public AbstractXmlMapping(Method getMethod) {
		// Store state
		this.getMethod = getMethod;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#map(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void map(Object object, XmlOutput output)
			throws XmlMarshallException {

		// Obtain the value from the object
		Object value = XmlMarshallerUtil.getReturnValue(object, this.getMethod);

		// Write the XML
		this.writeXml(value, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#getWriter()
	 */
	public XmlWriter getWriter() {
		return this;
	}

}
