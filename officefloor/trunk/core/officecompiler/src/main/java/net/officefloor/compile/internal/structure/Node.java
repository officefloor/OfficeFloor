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
	 * Obtains the name of the {@link Node}.
	 * 
	 * @return Name of the {@link Node}.
	 */
	String getNodeName();

	/**
	 * Obtains the {@link Node} type.
	 * 
	 * @return {@link Node} type.
	 */
	String getNodeType();

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

	/**
	 * Indicates if the {@link Node} has been initialised. {@link Node}
	 * instances should only be initialised once. Initialising the {@link Node}
	 * twice is an issue.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

}