/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
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
