/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;

/**
 * {@link TaskNode} implementation.
 * 
 * @author Daniel
 */
public class TaskNodeImpl implements TaskNode {

	/**
	 * Name of this {@link SectionTask}.
	 */
	private final String taskName;

	/**
	 * Name of the {@link TaskType} for this {@link SectionTask}.
	 */
	private final String taskTypeName;

	/**
	 * Location of the {@link OfficeSection} containing this {@link TaskNode}.
	 */
	private final String sectionLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * {@link TaskFlowNode} instances by their {@link TaskFlow} names.
	 */
	private final Map<String, TaskFlowNode> taskFlows = new HashMap<String, TaskFlowNode>();

	/**
	 * {@link TaskObjectNode} instances by their {@link TaskObject} names.
	 */
	private final Map<String, TaskObjectNode> taskObjects = new HashMap<String, TaskObjectNode>();

	/**
	 * {@link TaskFlowNode} instances by their {@link TaskFlow} names.
	 */
	private final Map<String, TaskFlowNode> taskEscalations = new HashMap<String, TaskFlowNode>();

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of this {@link SectionTask}.
	 * @param taskTypeName
	 *            Name of the {@link TaskType} for this {@link SectionTask}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link TaskNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public TaskNodeImpl(String taskName, String taskTypeName,
			String sectionLocation, CompilerIssues issues) {
		this.taskName = taskName;
		this.taskTypeName = taskTypeName;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/**
	 * Adds an issue regarding the {@link TaskNode} being built.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.issues.addIssue(LocationType.SECTION, this.sectionLocation,
				AssetType.TASK, this.taskName, issueDescription);
	}

	/*
	 * ====================== SectionTask =============================
	 */

	@Override
	public String getSectionTaskName() {
		return this.taskName;
	}

	@Override
	public TaskFlow getTaskFlow(String taskFlowName) {
		// Obtain and return the task flow for the name
		TaskFlowNode flow = this.taskFlows.get(taskFlowName);
		if (flow == null) {
			// Add the task flow
			flow = new TaskFlowNodeImpl(taskFlowName, false,
					this.sectionLocation, this.issues);
			this.taskFlows.put(taskFlowName, flow);
		}
		return flow;
	}

	@Override
	public TaskObject getTaskObject(String taskObjectName) {
		// Obtain and return the task object for the name
		TaskObjectNode object = this.taskObjects.get(taskObjectName);
		if (object == null) {
			// Add the task object
			object = new TaskObjectNodeImpl(taskObjectName,
					this.sectionLocation, this.issues);
			this.taskObjects.put(taskObjectName, object);
		}
		return object;
	}

	@Override
	public TaskFlow getTaskEscalation(String taskEscalationName) {
		// Obtain and return the task escalation for the name
		TaskFlowNode escalation = this.taskEscalations.get(taskEscalationName);
		if (escalation == null) {
			// Add the task escalation
			escalation = new TaskFlowNodeImpl(taskEscalationName, true,
					this.sectionLocation, this.issues);
			this.taskEscalations.put(taskEscalationName, escalation);
		}
		return escalation;
	}

	/*
	 * ==================== LinkFlowNode ==================================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			this.issues.addIssue(LocationType.SECTION, this.sectionLocation,
					null, null, "Task " + this.taskName
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

	/*
	 * ========================== OfficeTask ===========================
	 */

	@Override
	public String getOfficeTaskName() {
		return this.taskName;
	}

	@Override
	public ObjectDependency[] getObjectDependencies() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeTask.getObjectDependencies");
	}

	@Override
	public void setTeamResponsible(OfficeTeam team) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeTask.setTeamResponsible");
	}

	@Override
	public void addPreTaskDuty(OfficeDuty duty) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeTask.addPreTaskDuty");
	}

	@Override
	public void addPostTaskDuty(OfficeDuty duty) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeTask.addPostTaskDuty");
	}

}