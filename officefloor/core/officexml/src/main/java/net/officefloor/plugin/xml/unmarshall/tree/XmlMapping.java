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
package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.plugin.xml.XmlMarshallException;

/**
 * Mapping of XML element/attribute to either a value/new object on a target
 * object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMapping {

	/**
	 * Starts the load of the value/object to the target object based on the
	 * current context and state of unmarshalling.
	 * 
	 * @param state
	 *            Current state of XML unmarshalling.
	 * @param elementName
	 *            Name of element/attribute being mapped.
	 * @throws XmlMarshallException
	 *             If fail to load XML mapping.
	 */
	void startMapping(XmlState state, String elementName)
			throws XmlMarshallException;

	/**
	 * Ends the load of the value/object to the target object based on the
	 * current context and state of unmarshalling.
	 * 
	 * @param state
	 *            state of XML unmarshalling.
	 * @param value
	 *            Value of the element/attribute.
	 * @throws XmlMarshallException
	 *             If fail to load XML mapping.
	 */
	void endMapping(XmlState state, String value) throws XmlMarshallException;
}
