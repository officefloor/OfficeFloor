package net.officefloor.plugin.xml.unmarshall.tree;

/**
 * Type of the
 * {@link net.officefloor.plugin.xml.marshall.tree.XmlMappingMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public enum XmlMappingType {

	/**
	 * Root mapping.
	 */
	ROOT,

	/**
	 * Value to loaded onto an object.
	 */
	VALUE,

	/**
	 * A new child object.
	 */
	OBJECT,

	/**
	 * Reference to another mapping.
	 */
	REFERENCE,

	/**
	 * Static value loaded onto an object.
	 */
	STATIC
}
