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

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping} instances for
 * attributes of a particular element.
 * 
 * @author Daniel Sagenschneider
 */
public class AttributeXmlMappings {

	/**
	 * Mappings of attribute to appropriate {@link XmlMapping}.
	 */
	protected final Map<String, XmlMapping> mappings = new HashMap<String, XmlMapping>();

	/**
	 * Obtains the {@link XmlMapping} for the input attribute.
	 * 
	 * @param attributeName
	 *            Attribute name.
	 * @return {@link XmlMapping} for the attribute or <code>null</code> if
	 *         there is no mapping.
	 */
	public XmlMapping getXmlMapping(String attributeName) {
		return this.mappings.get(attributeName);
	}

	/**
	 * Adds an {@link XmlMapping} for the attribute.
	 * 
	 * @param attributeName
	 *            Attribute name.
	 * @param mapping
	 *            {@link XmlMapping} for the attribute.
	 */
	protected void addXmlMapping(String attributeName, XmlMapping mapping) {
		this.mappings.put(attributeName, mapping);
	}

}
