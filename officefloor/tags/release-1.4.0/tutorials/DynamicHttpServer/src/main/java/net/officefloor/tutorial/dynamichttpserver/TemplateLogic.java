/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.tutorial.dynamichttpserver;

import java.util.LinkedList;
import java.util.List;

/**
 * Example logic for the template.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	public Time getTime() {
		return new Time();
	}

	public Property[] getSystemProperties() {
		List<Property> properties = new LinkedList<Property>();
		for (String name : System.getProperties().stringPropertyNames()) {
			String value = System.getProperty(name);
			properties.add(new Property(name, value));
		}
		return properties.toArray(new Property[properties.size()]);
	}

}
// END SNIPPET: example