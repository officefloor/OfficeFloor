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
