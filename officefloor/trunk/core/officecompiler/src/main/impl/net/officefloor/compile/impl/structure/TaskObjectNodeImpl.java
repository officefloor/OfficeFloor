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

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.frame.api.execute.Task;

/**
 * {@link TaskObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskObjectNodeImpl implements TaskObjectNode {

	/**
	 * Name of this {@link TaskObject}.
	 */
	private final String objectName;

	/**
	 * {@link TaskNode} containing this {@link TaskObjectNode}.
	 */
	private final TaskNode taskNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Flag indicating if this {@link TaskObject} is a parameter to the
	 * {@link Task}.
	 */
	private boolean isParameter = false;

	/**
	 * Initiate.
	 * 
	 * @param objectName
	 *            Name of this {@link TaskObject}.
	 * @param taskNode
	 *            {@link TaskNode} containing this {@link TaskObjectNode}. F * @param
	 *            context {@link NodeContext}.
	 */
	public TaskObjectNodeImpl(String objectName, TaskNode taskNode,
			NodeContext context) {
		this.objectName = objectName;
		this.taskNode = taskNode;
		this.context = context;
	}

	/*
	 * ==================== Node ============================
	 */

	@Override
	public String getNodeName() {
		return this.objectName;
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
		return this.taskNode;
	}

	/*
	 * ==================== TaskObjectNode ============================
	 */

	@Override
	public boolean isParameter() {
		return this.isParameter;
	}

	/*
	 * ================= ObjectDependencyNode =========================
	 */

	@Override
	public ObjectDependencyType loadObjectDependencyType() {
		// TODO implement TaskObjectNode.loadObjectDependencyType
		throw new UnsupportedOperationException(
				"TODO implement TaskObjectNode.loadObjectDependencyType");

	}

	/*
	 * ===================== TaskObject ===============================
	 */

	@Override
	public String getTaskObjectName() {
		return this.objectName;
	}

	@Override
	public void flagAsParameter() {
		this.isParameter = true;
	}

	/*
	 * ===================== LinkObjectNode ===========================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {

		// Ensure not already linked
		if (this.linkedObjectNode != null) {
			this.context.getCompilerIssues()
					.addIssue(
							this,
							"Task object " + this.objectName
									+ " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedObjectNode = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}