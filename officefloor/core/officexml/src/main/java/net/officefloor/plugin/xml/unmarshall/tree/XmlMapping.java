package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Mapping of XML element/attribute to either a value/new object on a target
 * object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMapping {

	/**
	 * Starts the load of the value/object to the target object based on the
	 * current context and state of unmarshalling.
	 * 
	 * @param state
	 *            Current state of XML unmarshalling.
	 * @param elementName
	 *            Name of element/attribute being mapped.
	 * @throws XmlMarshallException
	 *             If fail to load XML mapping.
	 */
	void startMapping(XmlState state, String elementName)
			throws XmlMarshallException;

	/**
	 * Ends the load of the value/object to the target object based on the
	 * current context and state of unmarshalling.
	 * 
	 * @param state
	 *            state of XML unmarshalling.
	 * @param value
	 *            Value of the element/attribute.
	 * @throws XmlMarshallException
	 *             If fail to load XML mapping.
	 */
	void endMapping(XmlState state, String value) throws XmlMarshallException;
}
