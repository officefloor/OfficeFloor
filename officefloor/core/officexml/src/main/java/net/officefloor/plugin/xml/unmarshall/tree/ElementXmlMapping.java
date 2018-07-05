/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.unmarshall.tree;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains the {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping}
 * instance for a particular element and the
 * {@link net.officefloor.plugin.xml.unmarshall.tree.AttributeXmlMappings} for
 * the attributes of the element.
 * 
 * @author Daniel Sagenschneider
 */
public class ElementXmlMapping {

	/**
	 * {@link XmlMapping} of the element.
	 */
	protected XmlMapping elementXmlMapping = null;

	/**
	 * {@link XmlMapping} instances of the attributes of the element.
	 */
	protected AttributeXmlMappings attributeXmlMappings = null;

	/**
	 * List of static {@link XmlMapping} instances for the element.
	 */
	protected List<XmlMapping> staticXmlMappings = null;

	/**
	 * Obtains the {@link XmlMapping} for the element.
	 * 
	 * @return {@link XmlMapping} for the element or <code>null</code> if
	 *         there is no {@link XmlMapping} for the element.
	 */
	public XmlMapping getElementXmlMapping() {
		return this.elementXmlMapping;
	}

	/**
	 * Obtains the {@link AttributeXmlMappings} for the attributes of the
	 * element.
	 * 
	 * @return {@link AttributeXmlMappings} for the attribute or
	 *         <code>null</code> if there are no {@link AttributeXmlMappings}
	 *         for the element.
	 */
	public AttributeXmlMappings getAttributeXmlMappings() {
		return this.attributeXmlMappings;
	}

	/**
	 * Specifies the {@link XmlMapping} for the element.
	 * 
	 * @param mapping
	 *            {@link XmlMapping} for the element.
	 */
	protected void setElementXmlMapping(XmlMapping mapping) {
		this.elementXmlMapping = mapping;
	}

	/**
	 * Adds a {@link XmlMapping} for an attribute of the element.
	 * 
	 * @param attributeName
	 *            Attribute name.
	 * @param mapping
	 *            {@link XmlMapping} for the attribute.
	 */
	protected void addAttributeXmlMapping(String attributeName,
			XmlMapping mapping) {

		// Lazy load the attributes
		if (this.attributeXmlMappings == null) {
			this.attributeXmlMappings = new AttributeXmlMappings();
		}

		// Add the attribute mapping
		this.attributeXmlMappings.addXmlMapping(attributeName, mapping);
	}

	/**
	 * Obtains the list of static {@link XmlMapping} instances for the element.
	 * 
	 * @return {@link List} of {@link XmlMapping}.
	 */
	public List<XmlMapping> getStaticXmlMappings() {
		return this.staticXmlMappings;
	}

	/**
	 * Adds a static {@link XmlMapping} to the element.
	 * 
	 * @param mapping
	 *            Static {@link XmlMapping}.
	 */
	public void addStaticXmlMappings(XmlMapping mapping) {
		// Lazy load
		if (this.staticXmlMappings == null) {
			this.staticXmlMappings = new LinkedList<XmlMapping>();
		}
		this.staticXmlMappings.add(mapping);
	}
}
