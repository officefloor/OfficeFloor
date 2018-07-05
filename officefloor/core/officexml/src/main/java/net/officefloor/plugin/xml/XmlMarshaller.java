/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.xml;

/**
 * Contract to marshall XML from an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface XmlMarshaller {

	/**
	 * Marshalls the source object as XML to the output.
	 * 
	 * @param source
	 *            Object to marshall into XML.
	 * @param output
	 *            Output to send the XML.
	 * @throws XmlMarshallException
	 *             If fails to marshall source.
	 */
	void marshall(Object source, XmlOutput output) throws XmlMarshallException;
}
