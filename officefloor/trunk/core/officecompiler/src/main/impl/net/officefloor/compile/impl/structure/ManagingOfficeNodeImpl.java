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

import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;

/**
 * {@link ManagingOfficeNode} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ManagingOfficeNodeImpl implements ManagingOfficeNode {

	/**
	 * Parent {@link ManagedObjectSourceNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSourceNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 *
	 * @param managedObjectSource
	 *            Parent {@link ManagedObjectSourceNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagingOfficeNodeImpl(ManagedObjectSourceNode managedObjectSource,
			NodeContext context) {
		this.managedObjectSourceNode = managedObjectSource;
		this.context = context;
	}

	/*
	 * ======================== Node ===============================
	 */

	@Override
	public String getNodeName() {
		return "Managing Office for " + managedObjectSourceNode.TYPE + " "
				+ this.managedObjectSourceNode.getManagedObjectSourceName();
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
		return this.managedObjectSourceNode;
	}

	/*
	 * ================== LinkOfficeNode ===============================
	 */

	/**
	 * Linked {@link LinkOfficeNode}.
	 */
	private LinkOfficeNode linkedOfficeNode;

	@Override
	public boolean linkOfficeNode(LinkOfficeNode node) {

		// Ensure not already linked
		if (this.linkedOfficeNode != null) {
			this.context.getCompilerIssues().addIssue(
					this,
					"Managing office for managed object source "
							+ this.managedObjectSourceNode
									.getManagedObjectSourceName()
							+ " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedOfficeNode = node;
		return true;
	}

	@Override
	public LinkOfficeNode getLinkedOfficeNode() {
		return this.linkedOfficeNode;
	}

}