package net.officefloor.plugin.xml.marshall.translate;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Contract to translate an object value to an string value for XML.
 * 
 * @author Daniel Sagenschneider
 */
public interface Translator {

	/**
	 * Translates the object value into a string for XML.
	 * 
	 * @param object
	 *            Object to be translated.
	 * @return String for XML.
	 * @throws XmlMarshallException
	 *             If fails to translate object.
	 */
	String translate(Object object) throws XmlMarshallException;
}
