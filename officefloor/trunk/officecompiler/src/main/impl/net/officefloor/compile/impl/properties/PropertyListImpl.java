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
import java.util.Properties;

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
	public Property addProperty(String name, String label) {
		Property property = new PropertyImpl(name, label);
		this.properties.add(property);
		return property;
	}

	@Override
	public Property addProperty(String name) {
		return this.addProperty(name, name);
	}

	@Override
	public List<Property> getPropertyList() {
		return this.properties;
	}

	@Override
	public String[] getPropertyNames() {
		// Create the listing of property names
		String[] names = new String[this.properties.size()];
		int nameIndex = 0;
		for (Property property : this.properties) {
			names[nameIndex++] = property.getName();
		}
		return names;
	}

	@Override
	public Property getProperty(String name) {

		// Ensure have property name
		if (name == null) {
			return null; // no property by null name
		}

		// Find the first matching property
		for (Property property : this.properties) {
			if (name.equals(property.getName())) {
				return property; // found property
			}
		}

		// No matching property if at this point
		return null;
	}

	@Override
	public Properties getProperties() {
		// Create the properties.
		// This is done in reverse order to ensure first properties override.
		Properties utilProperties = new Properties();
		for (int i = this.properties.size() - 1; i >= 0; i--) {
			Property property = this.properties.get(i);
			utilProperties.setProperty(property.getName(), property.getValue());
		}
		return utilProperties;
	}

}