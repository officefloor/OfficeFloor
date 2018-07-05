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
package net.officefloor.compile.internal.structure;

/**
 * {@link LinkObjectNode} that can be linked to another {@link LinkObjectNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkObjectNode extends Node {

	/**
	 * Links the input {@link LinkObjectNode} to this {@link LinkObjectNode}.
	 * 
	 * @param node
	 *            {@link LinkObjectNode} to link to this {@link LinkObjectNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkObjectNode(LinkObjectNode node);

	/**
	 * Obtains the {@link LinkObjectNode} linked to this {@link LinkObjectNode}.
	 * 
	 * @return {@link LinkObjectNode} linked to this {@link LinkObjectNode}.
	 */
	LinkObjectNode getLinkedObjectNode();

}