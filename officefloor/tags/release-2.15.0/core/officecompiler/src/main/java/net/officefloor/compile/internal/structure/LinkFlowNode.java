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
 * {@link LinkFlowNode} that can be linked to another {@link LinkFlowNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkFlowNode {

	/**
	 * Links the input {@link LinkFlowNode} to this {@link LinkFlowNode}.
	 * 
	 * @param node
	 *            {@link LinkFlowNode} to link to this {@link LinkFlowNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkFlowNode(LinkFlowNode node);

	/**
	 * Obtains the {@link LinkFlowNode} linked to this {@link LinkFlowNode}.
	 * 
	 * @return {@link LinkFlowNode} linked to this {@link LinkFlowNode}.
	 */
	LinkFlowNode getLinkedFlowNode();

}