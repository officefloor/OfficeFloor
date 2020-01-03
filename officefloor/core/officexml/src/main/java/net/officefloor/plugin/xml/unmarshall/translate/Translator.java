package net.officefloor.plugin.xml.unmarshall.translate;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Contract to translate the XML string value to specific typed object.
 * 
 * @author Daniel Sagenschneider
 */
public interface Translator {

    /**
     * Translates the XML string value to specific typed object.
     * 
     * @param value
     *            XML string value.
     * @return Specific type object translated from the input XML string value.
     * @throws XmlMarshallException
     *             Should there be a failure to translate the value.
     */
    Object translate(String value) throws XmlMarshallException;

}