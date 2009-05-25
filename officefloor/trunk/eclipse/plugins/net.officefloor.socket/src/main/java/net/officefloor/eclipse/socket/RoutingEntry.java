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
package net.officefloor.eclipse.socket;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.work.http.route.HttpRouteWorkSource;

/**
 * Entry in routing.
 * 
 * @author Daniel
 */
public class RoutingEntry {

	/**
	 * Name identifying this {@link RoutingEntry}.
	 */
	private String name;

	/**
	 * Pattern for matching to use this {@link RoutingEntry}.
	 */
	private String pattern;

	/**
	 * Obtains the name identifying this {@link RoutingEntry}.
	 * 
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Specifies the name.
	 * 
	 * @param name
	 *            Name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Obtains the pattern.
	 * 
	 * @return Pattern.
	 */
	public String getPattern() {
		return this.pattern;
	}

	/**
	 * Specifies the pattern.
	 * 
	 * @param pattern
	 *            Pattern.
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Loads the {@link Property} for this {@link RoutingEntry} to the
	 * {@link PropertyList}.
	 * 
	 * @param {@link PropertyList}.
	 */
	public void loadProperty(PropertyList propertyList) {

		// Only provide prefix if name provided
		String propertyName = ((this.name == null) || this.name.trim().length() == 0) ? ""
				: (HttpRouteWorkSource.ROUTE_PROPERTY_PREFIX + this.name);

		// Load the property
		Property property = propertyList.getProperty(propertyName);
		if (property == null) {
			property = propertyList.addProperty(propertyName);
		}
		property.setValue(this.pattern);
	}

}