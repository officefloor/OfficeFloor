/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml.marshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;
import net.officefloor.plugin.xml.XmlOutput;

/**
 * Mapping of object to XML.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMapping {

	/**
	 * Maps the object into XML.
	 * 
	 * @param object
	 *            Object to map into XML.
	 * @param output
	 *            Output to send the XML.
	 * @throws XmlMarshallException
	 *             If fails to map object into XML.
	 */
	void map(Object object, XmlOutput output) throws XmlMarshallException;

	/**
	 * Obtains the {@link XmlWriter} for this mapping.
	 * 
	 * @return {@link XmlWriter} for this mapping.
	 */
	XmlWriter getWriter();
}
