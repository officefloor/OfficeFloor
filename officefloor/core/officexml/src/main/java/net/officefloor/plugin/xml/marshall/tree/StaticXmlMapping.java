package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Provides static mapping.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticXmlMapping implements XmlMapping, XmlWriter {

	/**
	 * Static XML snippet for this mapping.
	 */
	protected final String xmlSnippet;

	/**
	 * Initiate with the static XML snippet.
	 * 
	 * @param xmlSnippet
	 *            Static XML snippet.
	 */
	public StaticXmlMapping(String xmlSnippet) {
		this.xmlSnippet = xmlSnippet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#map(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void map(Object object, XmlOutput output)
			throws XmlMarshallException {
		// Write the XML
		this.writeXml(object, output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlMapping#getWriter()
	 */
	public XmlWriter getWriter() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.marshall.tree.XmlWriter#writeXml(java.lang.Object,
	 *      net.officefloor.plugin.xml.XmlOutput)
	 */
	public void writeXml(Object object, XmlOutput output)
			throws XmlMarshallException {
		// Output the static xml
		XmlMarshallerUtil.writeXml(this.xmlSnippet, output);
	}

}
