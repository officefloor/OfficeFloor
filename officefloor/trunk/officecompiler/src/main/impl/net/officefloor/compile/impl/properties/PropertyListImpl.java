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
package net.officefloor.compile.impl.properties;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;

/**
 * Implementation of the {@link PropertyList}.
 * 
 * @author Daniel
 */
public class PropertyListImpl implements PropertyList {

	/**
	 * List of {@link Property} instances.
	 */
	private final List<Property> properties = new LinkedList<Property>();

	/*
	 * ================== PropertyList ======================================
	 */

	@Override
	public List<Property> getPropertyList() {
		return this.properties;
	}

	@Override
	public Property addProperty(String name, String label) {
		Property property = new PropertyImpl(name, label);
		this.properties.add(property);
		return property;
	}

	@Override
	public Property addProperty(String name) {
		return this.addProperty(name, name);
	}

}