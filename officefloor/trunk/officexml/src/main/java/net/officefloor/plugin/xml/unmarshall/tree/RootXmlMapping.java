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
package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping} that puts the
 * root object into context.
 * 
 * @author Daniel
 */
public class RootXmlMapping implements XmlMapping {

	/**
	 * {@link XmlContext} of the root target object.
	 */
	protected final XmlContext rootObjectContext;

	/**
	 * Initiate with {@link XmlContext} for the root target object.
	 * 
	 * @param rootObjectContext
	 *            {@link XmlContext} for the root target object.
	 */
	public RootXmlMapping(XmlContext rootObjectContext) {
		// Store state
		this.rootObjectContext = rootObjectContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#startMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {
		// Push root context into scope
		state.pushContext(elementName, state.getCurrentTargetObject(),
				this.rootObjectContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.unmarshall.tree.XmlMapping#endMapping(net.officefloor.plugin.xml.unmarshall.tree.XmlState,
	 *      java.lang.String)
	 */
	public void endMapping(XmlState state, String value)
			throws XmlMarshallException {
		// Pop the context happens on closing tag of pushed element name
	}

}
