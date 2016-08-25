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
package net.officefloor.compile.internal.structure;

/**
 * Node within the compilation tree.
 *
 * @author Daniel Sagenschneider
 */
public interface Node {

	/**
	 * Name used for {@link Node} when loaded as type.
	 */
	static final String TYPE_NAME = "TYPE";

	/**
	 * Obtains the name of the {@link Node}.
	 * 
	 * @return Name of the {@link Node}.
	 */
	String getNodeName();

	/**
	 * Obtains the location of the {@link Node}.
	 * 
	 * @return Location of the {@link Node}. May be <code>null</code> if
	 *         {@link Node} does not support a location.
	 */
	String getLocation();

	/**
	 * Obtains the {@link Node} containing this {@link Node}.
	 * 
	 * @return {@link Node} containing this {@link Node}.
	 */
	Node getParentNode();

}