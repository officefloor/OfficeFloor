package net.officefloor.plugin.xml;

import java.io.InputStream;

/**
 * Contract to unmarshall XML onto an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlUnmarshaller {

    /**
     * Unmarshalls the input xml onto the input target object.
     * 
     * @param xml
     *            XML to unmarshall.
     * @param target
     *            Target object to load XML data onto.
     * @throws XmlMarshallException
     *             Should fail to load XML data onto the target object.
     */
    void unmarshall(InputStream xml, Object target) throws XmlMarshallException;
}