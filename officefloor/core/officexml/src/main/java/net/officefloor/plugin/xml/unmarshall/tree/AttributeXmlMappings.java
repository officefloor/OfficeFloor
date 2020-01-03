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
