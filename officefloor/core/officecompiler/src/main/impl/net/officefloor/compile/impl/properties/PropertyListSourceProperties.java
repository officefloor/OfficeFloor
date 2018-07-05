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
package net.officefloor.compile.impl.properties;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;

/**
 * {@link SourceProperties} initialised from a {@link PropertyList}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyListSourceProperties extends SourcePropertiesImpl {

	/**
	 * Initiate with {@link Property} instances within the {@link PropertyList}.
	 * 
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public PropertyListSourceProperties(PropertyList properties) {
		if (properties != null) {
			for (Property property : properties) {
				this.addProperty(property.getName(), property.getValue());
			}
		}
	}

}