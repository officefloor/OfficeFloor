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

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.UnknownType;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link TaskObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TaskObjectNodeImpl implements TaskObjectNode {

	/**
	 * {@link TaskNode} containing this {@link TaskObjectNode}.
	 */
	private final TaskNode taskNode;

	/**
	 * Name of this {@link TaskObject}.
	 */
	private final String objectName;

	/**
	 * Location of the {@link OfficeSection} containing this {@link TaskObject}.
	 */
	private final String sectionLocation;

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
	 * Flags whether within the {@link Office} context.
	 */
	private boolean isInOfficeContext = false;

	/**
	 * Location of the {@link Office} containing this
	 * {@link OfficeSectionObject}.
	 */
	private String officeLocation;

	/**
	 * Initiate.
	 * 
	 * @param taskNode
	 *            {@link TaskNode} containing this {@link TaskObjectNode}.
	 * @param objectName
	 *            Name of this {@link TaskObject}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link TaskObject}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TaskObjectNodeImpl(TaskNode taskNode, String objectName,
			String sectionLocation, NodeContext context) {
		this.taskNode = taskNode;
		this.objectName = objectName;
		this.sectionLocation = sectionLocation;
		this.context = context;
	}

	/*
	 * ==================== TaskObjectNode ============================
	 */

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;
		this.isInOfficeContext = true;
	}

	@Override
	public boolean isParameter() {
		return this.isParameter;
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
	 * ======================= ObjectDependency ===========================
	 */

	@Override
	public String getObjectDependencyName() {
		// Return the object name
		return this.objectName;
	}

	@Override
	public Class<?> getObjectDependencyType() {

		// Obtain the task object type
		TaskObjectType<?> objectType = this.getTaskObjectType();
		if (objectType == null) {
			return UnknownType.class; // unable to obtain object type
		}

		// Return the type
		return objectType.getObjectType();
	}

	@Override
	public String getObjectDependencyTypeQualifier() {

		// Obtain the task object type
		TaskObjectType<?> objectType = this.getTaskObjectType();
		if (objectType == null) {
			return null; // unable to obtain object type
		}

		// Return the type qualifier
		return objectType.getTypeQualifier();
	}

	/**
	 * Obtains the {@link TaskObjectType} for this {@link TaskObjectNode}.
	 * 
	 * @return {@link TaskObjectType} for this {@link TaskObjectNode}. May be
	 *         <code>null</code> if can not obtain.
	 */
	private TaskObjectType<?> getTaskObjectType() {

		// Ensure in office context
		if (!this.isInOfficeContext) {
			throw new IllegalStateException("Must be in office context");
		}

		// Obtain the task type for this task node
		TaskType<?, ?, ?> taskType = this.taskNode.getTaskType();
		if (taskType == null) {
			return null; // must have task type containing object type
		}

		// Find the corresponding object type for this task object
		for (TaskObjectType<?> objectType : taskType.getObjectTypes()) {
			if (this.objectName.equals(objectType.getObjectName())) {
				// Found the object type, so return the type
				return objectType;
			}
		}

		// As here, did not find the object type
		return null;
	}

	@Override
	public DependentManagedObject getDependentManagedObject() {

		// Ensure in office context
		if (!this.isInOfficeContext) {
			throw new IllegalStateException("Must be in office context");
		}

		// No dependent if a parameter
		if (this.isParameter) {
			return null;
		}

		// Return the retrieved dependent managed object
		return LinkUtil.retrieveTarget(this, DependentManagedObject.class,
				"TaskObject " + this.objectName, LocationType.OFFICE,
				this.officeLocation, null, null,
				this.context.getCompilerIssues());
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
							LocationType.SECTION,
							this.sectionLocation,
							null,
							null,
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