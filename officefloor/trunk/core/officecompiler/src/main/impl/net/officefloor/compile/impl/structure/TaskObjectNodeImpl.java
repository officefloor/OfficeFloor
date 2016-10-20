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

import net.officefloor.compile.impl.object.ObjectDependencyTypeImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.DependentObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
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

	@Override
	public boolean isInitialised() {
		// TODO implement Node.isInitialised
		throw new UnsupportedOperationException(
				"TODO implement Node.isInitialised");

	}

	@Override
	public void initialise() {
		// TODO implement TaskObjectNode.initialise
		throw new UnsupportedOperationException(
				"TODO implement TaskObjectNode.initialise");

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
	public ObjectDependencyType loadObjectDependencyType(TypeContext typeContext) {

		// Obtain the task type
		TaskType<?, ?, ?> taskType = this.taskNode.loadTaskType(typeContext);
		if (taskType == null) {
			return null;
		}

		// Obtain the task object type
		TaskObjectType<?> object = null;
		for (TaskObjectType<?> objectType : taskType.getObjectTypes()) {
			if (this.objectName.equals(objectType.getObjectName())) {
				object = objectType;
			}
		}
		if (object == null) {
			this.context.getCompilerIssues().addIssue(
					this,
					TaskObjectNode.TYPE + " does not have object "
							+ this.objectName);
			return null;
		}

		// Obtain the type information
		String dependencyType = object.getObjectType().getName();
		String typeQualifier = object.getTypeQualifier();

		// Obtain the dependent object type
		DependentObjectType dependentObjectType = null;
		if (!this.isParameter) {

			// Not parameter, so must obtain dependent object
			DependentObjectNode dependentObjectNode = LinkUtil
					.retrieveFurtherestTarget(this, DependentObjectNode.class,
							this.context.getCompilerIssues());
			if (dependentObjectNode == null) {
				this.context.getCompilerIssues().addIssue(
						this,
						TaskObjectNode.TYPE + " " + this.objectName
								+ " is not linked to a "
								+ DependentObjectNode.TYPE);
				return null;
			}

			// Obtain the dependent object type
			dependentObjectType = dependentObjectNode
					.loadDependentObjectType(typeContext);
		}

		// Create and return the type
		return new ObjectDependencyTypeImpl(this.objectName, dependencyType,
				typeQualifier, this.isParameter, dependentObjectType);
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
		return LinkUtil.linkObjectNode(this, node,
				this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}