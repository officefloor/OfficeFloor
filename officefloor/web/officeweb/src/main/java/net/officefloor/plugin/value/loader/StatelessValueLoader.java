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
package net.officefloor.plugin.value.loader;

import java.util.Map;

/**
 * Loads a value onto the Object graph.
 * 
 * @author Daniel Sagenschneider
 */
public interface StatelessValueLoader {

	/**
	 * Loads the value onto the object graph.
	 * 
	 * @param object
	 *            Root object of the graph to have the value loaded.
	 * @param name
	 *            Full property name.
	 * @param nameIndex
	 *            Index into property name to identify particular property name
	 *            for next stringed property to load.
	 * @param value
	 *            Property value.
	 * @param state
	 *            State of loading values to the Object graph.
	 * @throws Exception
	 *             If fails to load the value.
	 */
	void loadValue(Object object, String name, int nameIndex, String value,
			Map<PropertyKey, Object> state) throws Exception;

}