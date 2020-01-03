package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Mapping of object to XML.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMapping {

	/**
	 * Maps the object into XML.
	 * 
	 * @param object
	 *            Object to map into XML.
	 * @param output
	 *            Output to send the XML.
	 * @throws XmlMarshallException
	 *             If fails to map object into XML.
	 */
	void map(Object object, XmlOutput output) throws XmlMarshallException;

	/**
	 * Obtains the {@link XmlWriter} for this mapping.
	 * 
	 * @return {@link XmlWriter} for this mapping.
	 */
	XmlWriter getWriter();
}
