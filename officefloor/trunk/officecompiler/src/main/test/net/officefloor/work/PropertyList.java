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
package net.officefloor.work;

import java.util.LinkedList;
import java.util.List;

/**
 * Helpful object to construct a name/value pair list.
 * 
 * @author Daniel
 */
public class PropertyList {

	/**
	 * Property listing in name/value pairing.
	 */
	private List<String> properties = new LinkedList<String>();

	/**
	 * Adds a property.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param value
	 *            Value of the property.
	 */
	public void addProperty(String name, String value) {
		this.properties.add(name);
		this.properties.add(value);
	}

	/**
	 * Obtains the properties as a name/value pairing array.
	 * 
	 * @return Name/value pairing array.
	 */
	public String[] getNameValuePairs() {
		return this.properties.toArray(new String[0]);
	}

}
