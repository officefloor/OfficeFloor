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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.spi.section.ManagedObjectFlow;

/**
 * {@link ManagedObjectFlowNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFlowNodeImpl implements ManagedObjectFlowNode {

	/**
	 * Name of this {@link ManagedObjectFlow}.
	 */
	private final String managedObjectFlowName;

	/**
	 * Parent {@link ManagedObjectSourceNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSource;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectFlowName
	 *            Name of this {@link ManagedObjectFlow}.
	 * @param managedObjectSource
	 *            Parent {@link ManagedObjectSourceNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectFlowNodeImpl(String managedObjectFlowName,
			ManagedObjectSourceNode managedObjectSource, NodeContext context) {
		this.managedObjectFlowName = managedObjectFlowName;
		this.managedObjectSource = managedObjectSource;
		this.context = context;
	}

	/*
	 * =================== Node =============================
	 */

	@Override
	public String getNodeName() {
		return this.managedObjectFlowName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.managedObjectSource;
	}

	/*
	 * =================== ManagedObjectFlow =============================
	 */

	@Override
	public String getManagedObjectFlowName() {
		return this.managedObjectFlowName;
	}

	/*
	 * =================== LinkFlowNode =================================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			// Office floor managed object flow
			this.context.getCompilerIssues().addIssue(
					this,
					"Managed object source flow " + this.managedObjectFlowName
							+ " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedFlowNode = node;
		return true;
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

}