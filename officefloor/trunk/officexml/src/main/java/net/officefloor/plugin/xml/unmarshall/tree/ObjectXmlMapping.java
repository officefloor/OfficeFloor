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
import net.officefloor.plugin.xml.unmarshall.load.ObjectLoader;

/**
 * {@link net.officefloor.plugin.xml.unmarshall.tree.XmlMapping} that loads
 * object onto target object.
 * 
 * @author Daniel
 */
public class ObjectXmlMapping implements XmlMapping {

	/**
	 * Loads the object onto the target object.
	 */
	protected final ObjectLoader loader;

	/**
	 * {@link XmlContext} of the object to be loaded onto the target object.
	 */
	protected final XmlContext loadObjectContext;

	/**
	 * Initiate with loader and {@link XmlContext} for the object loaded.
	 * 
	 * @param loader
	 *            Loads the object onto the target object.
	 * @param loadObjectXmlContext
	 *            {@link XmlContext} for this {@link XmlMapping}.
	 */
	public ObjectXmlMapping(ObjectLoader loader, XmlContext loadObjectContext) {
		// Store state
		this.loader = loader;
		this.loadObjectContext = loadObjectContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.XmlMapping#startMapping(net.officefloor.plugin.xml.tree.XmlState,
	 *      java.lang.String)
	 */
	public void startMapping(XmlState state, String elementName)
			throws XmlMarshallException {

		// Load object onto target object
		Object loadedObject = this.loader.loadObject(state
				.getCurrentTargetObject());

		// Push new context
		state.pushContext(elementName, loadedObject, this.loadObjectContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.xml.tree.XmlMapping#endMapping(net.officefloor.plugin.xml.tree.XmlState,
	 *      java.lang.String)
	 */
	public void endMapping(XmlState state, String value)
			throws XmlMarshallException {
		// Pop the context happens on closing tag of pushed element name
	}

}
