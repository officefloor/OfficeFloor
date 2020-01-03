package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Writes the XML for an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlWriter {

	/**
	 * Writes the XML for the input object.
	 * 
	 * @param object
	 *            Object to have XML written for it.
	 * @param output
	 *            Output to write the XML.
	 * @throws XmlMarshallException
	 *             If fails to write the object into XML.
	 */
	void writeXml(Object object, XmlOutput output) throws XmlMarshallException;

}
