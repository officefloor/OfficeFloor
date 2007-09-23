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

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Writes the XML for an object.
 * 
 * @author Daniel
 */
public interface XmlWriter {

	/**
	 * Writes the XML for the input object.
	 * 
	 * @param object
	 *            Object to have XML written for it.
	 * @param output
	 *            Output to write the XML.
	 * @throws XmlMarshallException
	 *             If fails to write the object into XML.
	 */
	void writeXml(Object object, XmlOutput output) throws XmlMarshallException;

}
