/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.xml.marshall.tree;

import java.lang.reflect.Method;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Abstract {@link net.officefloor.plugin.xml.marshall.tree.XmlMapping} to aid
 * in mapping.
 * 
 * @author Daniel
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
