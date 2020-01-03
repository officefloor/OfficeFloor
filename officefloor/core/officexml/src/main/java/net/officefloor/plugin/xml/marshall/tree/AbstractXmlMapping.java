package net.officefloor.plugin.xml.marshall.tree;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Abstract {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping} to aid
 * in mapping.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractXmlMapping implements XmlMapping, XmlWriter {

	/**
	 * Method to obtain value from object to map to XML.
	 */
	protected final Method getMethod;

	/**
	 * Initiate with method to obtain value to map.
	 * 
	 * @param getMethod
	 *            Method to obtain value to be mapped.
	 */
	public AbstractXmlMapping(Method getMethod) {
		// Store state
		this.getMethod = getMethod;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#map(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void map(Object object, XmlOutput output)
			throws XmlMarshallException {

		// Obtain the value from the object
		Object value = XmlMarshallerUtil.getReturnValue(object, this.getMethod);

		// Write the XML
		this.writeXml(value, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#getWriter()
	 */
	public XmlWriter getWriter() {
		return this;
	}

}
