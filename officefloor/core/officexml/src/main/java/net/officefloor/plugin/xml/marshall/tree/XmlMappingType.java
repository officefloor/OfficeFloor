package net.officefloor.plugin.xml.marshall.tree;

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
	 * Indicates the attributes for an element.
	 */
	ATTRIBUTES,

	/**
	 * A particular attribute for an element.
	 */
	ATTRIBUTE,

	/**
	 * Value of an object to be contained in an element.
	 */
	VALUE,

	/**
	 * Specific object that parents other elements.
	 */
	OBJECT,

	/**
	 * Generic object that has mappings specific to its sub-type implementation.
	 */
	TYPE,

	/**
	 * Collection of objects.
	 */
	COLLECTION,

	/**
	 * Specifies the type of object within a {@link #TYPE} or
	 * {@link #COLLECTION}.
	 */
	ITEM,

	/**
	 * Enables referencing other mappings. Mainly useful for recursive mappings.
	 */
	REFERENCE
}
