/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.DutyNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TaskTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link TaskNode} implementation.
 * 
 * @author Daniel Sagenschneider
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
	 * Location of {@link OfficeSection} containing this {@link SectionTask}.
	 */
	private final String sectionLocation;

	/**
	 * {@link WorkNode} containing this {@link TaskNode}.
	 */
	private final WorkNode workNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

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
	 * Listing of {@link OfficeDuty} instances to do before this
	 * {@link OfficeTask}.
	 */
	private final List<DutyNode> preTaskDuties = new LinkedList<DutyNode>();

	/**
	 * Listing of {@link OfficeDuty} instances to do after this
	 * {@link OfficeDuty}.
	 */
	private final List<DutyNode> postTaskDuties = new LinkedList<DutyNode>();

	/**
	 * Listing of {@link OfficeGovernance} instances providing
	 * {@link Governance} over this {@link Task}.
	 */
	private final List<GovernanceNode> governances = new LinkedList<GovernanceNode>();

	/**
	 * Flag indicating if the context of the {@link Office} for this
	 * {@link OfficeTask} has been loaded.
	 */
	private boolean isOfficeContextLoaded = false;

	/**
	 * Location of {@link DeployedOffice} containing this {@link OfficeTask}.
	 */
	private String officeLocation;

	/**
	 * {@link TaskTeam} responsible for this {@link OfficeTask}.
	 */
	private OfficeTeamNode teamResponsible = null;

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
	 * @param workNode
	 *            {@link WorkNode} containing this {@link TaskNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TaskNodeImpl(String taskName, String taskTypeName,
			String sectionLocation, WorkNode workNode, NodeContext context) {
		this.taskName = taskName;
		this.taskTypeName = taskTypeName;
		this.sectionLocation = sectionLocation;
		this.workNode = workNode;
		this.context = context;
	}

	/**
	 * Adds an issue regarding the {@link OfficeSection} being built.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(LocationType.SECTION,
				this.sectionLocation, AssetType.TASK, this.taskName,
				issueDescription);
	}

	/*
	 * ========================== TaskNode ===================================
	 */

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;

		// Flag all task objects within office context
		for (TaskObjectNode object : this.taskObjects.values()) {
			object.addOfficeContext(officeLocation);
		}

		// Create the team responsible
		this.teamResponsible = new OfficeTeamNodeImpl("Team for task "
				+ this.taskName, this.officeLocation, this.context);
		this.isOfficeContextLoaded = true;
	}

	@Override
	public WorkNode getWorkNode() {
		return this.workNode;
	}

	@Override
	public TaskType<?, ?, ?> getTaskType() {

		// Obtain the work type
		WorkType<?> workType = this.workNode.getWorkType();
		if (workType == null) {
			return null; // must have work type for task
		}

		// Find the task type for this task node
		for (TaskType<?, ?, ?> type : workType.getTaskTypes()) {
			if (this.taskTypeName.equals(type.getTaskName())) {
				// Found the type for this task
				return type;
			}
		}

		// As here, did not find corresponding task type
		return null;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildTask(WorkBuilder<?> workBuilder) {

		// Obtain the task factory for this task
		TaskType<?, ?, ?> taskType = this.getTaskType();
		if (taskType == null) {
			this.addIssue("Can not find task type '" + this.taskTypeName + "'");
			return; // must have task type
		}

		// Obtain the office team for the task
		OfficeTeam officeTeam = LinkUtil.retrieveTarget(this.teamResponsible,
				OfficeTeam.class, "Team for task " + this.taskName,
				LocationType.SECTION, this.sectionLocation, AssetType.TASK,
				this.taskName, this.context.getCompilerIssues());
		if (officeTeam == null) {
			return; // must have team for the task
		}

		// Build the task
		TaskFactory taskFactory = taskType.getTaskFactory();
		TaskBuilder taskBuilder = workBuilder.addTask(this.taskName,
				taskFactory);
		taskBuilder.setTeam(officeTeam.getOfficeTeamName());

		// Add differentiator (if available)
		Object differentiator = taskType.getDifferentiator();
		if (differentiator != null) {
			taskBuilder.setDifferentiator(differentiator);
		}

		// Build the flows
		TaskFlowType<?>[] flowTypes = taskType.getFlowTypes();
		for (int flowIndex = 0; flowIndex < flowTypes.length; flowIndex++) {
			TaskFlowType<?> flowType = flowTypes[flowIndex];

			// Obtain type details for linking
			String flowName = flowType.getFlowName();
			Enum<?> flowKey = flowType.getKey();
			Class<?> argumentType = flowType.getArgumentType();

			// Obtain the linked task for the flow
			TaskFlowNode flowNode = this.taskFlows.get(flowName);
			TaskNode linkedTask = LinkUtil.retrieveTarget(flowNode,
					TaskNode.class, "Flow " + flowName, LocationType.SECTION,
					this.sectionLocation, AssetType.TASK, this.taskName,
					this.context.getCompilerIssues());
			if (linkedTask == null) {
				continue; // must have linked task
			}

			// Obtain configured details for linking
			String linkedTaskName = linkedTask.getOfficeTaskName();
			FlowInstigationStrategyEnum instigationStrategy = flowNode
					.getFlowInstigationStrategy();
			if (instigationStrategy == null) {
				this.addIssue("No instigation strategy provided for flow "
						+ flowName);
				continue; // must have instigation strategy
			}

			// Determine if same work
			WorkNode linkedWork = linkedTask.getWorkNode();
			if (this.workNode == linkedWork) {
				// Link to task on same work
				if (flowKey != null) {
					taskBuilder.linkFlow(flowKey, linkedTaskName,
							instigationStrategy, argumentType);
				} else {
					taskBuilder.linkFlow(flowIndex, linkedTaskName,
							instigationStrategy, argumentType);
				}
			} else {
				// Link to task on different work
				String linkedWorkName = linkedWork.getQualifiedWorkName();
				if (flowKey != null) {
					taskBuilder.linkFlow(flowKey, linkedWorkName,
							linkedTaskName, instigationStrategy, argumentType);
				} else {
					taskBuilder.linkFlow(flowIndex, linkedWorkName,
							linkedTaskName, instigationStrategy, argumentType);
				}
			}
		}

		// Build the next task
		if (this.linkedFlowNode != null) {
			// Have next task so link to it
			TaskNode nextTask = LinkUtil.retrieveTarget(this, TaskNode.class,
					"Next task ", LocationType.SECTION, this.sectionLocation,
					AssetType.TASK, this.taskName,
					this.context.getCompilerIssues());
			if (nextTask != null) {

				// Obtain next details for linking
				String nextTaskName = nextTask.getOfficeTaskName();
				Class<?> argumentType = taskType.getReturnType();

				// Determine if linked task is on the same work
				WorkNode nextWork = nextTask.getWorkNode();
				if (this.workNode == nextWork) {
					// Link to next task on same work
					taskBuilder.setNextTaskInFlow(nextTaskName, argumentType);
				} else {
					// Link to next task on different work
					String nextWorkName = nextWork.getQualifiedWorkName();
					taskBuilder.setNextTaskInFlow(nextWorkName, nextTaskName,
							argumentType);
				}
			}
		}

		// Build the objects
		TaskObjectType<?>[] objectTypes = taskType.getObjectTypes();
		for (int objectIndex = 0; objectIndex < objectTypes.length; objectIndex++) {
			TaskObjectType<?> objectType = objectTypes[objectIndex];

			// Obtain the type details for linking
			String objectName = objectType.getObjectName();
			Enum<?> objectKey = objectType.getKey();
			Class<?> objectClass = objectType.getObjectType();

			// Obtain the object node for the task object
			TaskObjectNode objectNode = this.taskObjects.get(objectName);

			// Determine if the object is a parameter
			if ((objectNode != null) && (objectNode.isParameter())) {
				// Link as parameter
				if (objectKey != null) {
					taskBuilder.linkParameter(objectKey, objectClass);
				} else {
					taskBuilder.linkParameter(objectIndex, objectClass);
				}
				continue; // linked as a parameter
			}

			// Obtain the managed object for the object
			BoundManagedObjectNode linkedManagedObject = LinkUtil
					.retrieveTarget(objectNode, BoundManagedObjectNode.class,
							"Object " + objectName, LocationType.SECTION,
							this.sectionLocation, AssetType.TASK,
							this.taskName, this.context.getCompilerIssues());
			if (linkedManagedObject == null) {
				continue; // must have linked managed object
			}

			// Link task object to managed object
			String linkedManagedObjectName = linkedManagedObject
					.getBoundManagedObjectName();
			if (objectKey != null) {
				taskBuilder.linkManagedObject(objectKey,
						linkedManagedObjectName, objectClass);
			} else {
				taskBuilder.linkManagedObject(objectIndex,
						linkedManagedObjectName, objectClass);
			}
		}

		// Build the escalations
		TaskEscalationType[] escalationTypes = taskType.getEscalationTypes();
		for (int i = 0; i < escalationTypes.length; i++) {
			TaskEscalationType escalationType = escalationTypes[i];

			// Obtain the type details for linking
			Class<? extends Throwable> escalationClass = escalationType
					.getEscalationType();
			String escalationName = escalationClass.getName();

			// Obtain the linked task for the escalation
			TaskFlowNode escalationNode = this.taskEscalations
					.get(escalationName);
			TaskNode linkedTask = LinkUtil.findTarget(escalationNode,
					TaskNode.class, "Escalation " + escalationName,
					LocationType.SECTION, this.sectionLocation, AssetType.TASK,
					this.taskName, this.context.getCompilerIssues());
			if (linkedTask != null) {
				// Obtain the configuration details for linking
				String linkedTaskName = linkedTask.getOfficeTaskName();

				// Determine if same work
				WorkNode linkedWork = linkedTask.getWorkNode();
				if (this.workNode == linkedWork) {
					// Link to task on same work
					taskBuilder.addEscalation(escalationClass, linkedTaskName);
				} else {
					// Link to task on different work
					String linkedWorkName = linkedWork.getQualifiedWorkName();
					taskBuilder.addEscalation(escalationClass, linkedWorkName,
							linkedTaskName);
				}
			} else {
				// Ensure the escalation is propagated to the office
				boolean isEscalatedToOffice = false;
				SectionOutputNode sectionOutputNode = LinkUtil
						.findFurtherestTarget(escalationNode,
								SectionOutputNode.class, "Escalation "
										+ escalationName, LocationType.SECTION,
								this.sectionLocation, AssetType.TASK,
								this.taskName, this.context.getCompilerIssues());
				if (sectionOutputNode != null) {
					// Determine if object of top level section (the office)
					SectionNode sectionNode = sectionOutputNode
							.getSectionNode();
					isEscalatedToOffice = (sectionNode.getParentSectionNode() == null);
				}
				if (!isEscalatedToOffice) {
					// Escalation must be propagated to the office
					this.context
							.getCompilerIssues()
							.addIssue(
									LocationType.SECTION,
									this.sectionLocation,
									AssetType.TASK,
									this.taskName,
									"Escalation "
											+ escalationClass.getName()
											+ " not handled by a Task nor propagated to the Office");
				}
			}
		}

		// Build the pre task administration
		for (DutyNode preDuty : this.preTaskDuties) {
			preDuty.buildPreTaskAdministration(workBuilder, taskBuilder);
		}

		// Build the post task administration
		for (DutyNode postDuty : this.postTaskDuties) {
			postDuty.buildPostTaskAdministration(workBuilder, taskBuilder);
		}

		// Build the governance (first inherited then specific for task)
		SectionNode section = this.workNode.getSectionNode();
		GovernanceNode[] sectionGovernances = section.getGoverningGovernances();
		for (GovernanceNode governance : sectionGovernances) {
			taskBuilder.addGovernance(governance.getOfficeGovernanceName());
		}
		for (GovernanceNode governance : this.governances) {
			taskBuilder.addGovernance(governance.getOfficeGovernanceName());
		}
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
					this.sectionLocation, this.context);
			this.taskFlows.put(taskFlowName, flow);
		}
		return flow;
	}

	@Override
	public TaskObject getTaskObject(String taskObjectName) {
		// Obtain and return the task object for the name
		TaskObjectNode object = this.taskObjects.get(taskObjectName);
		if (object == null) {
			// Create the task object
			object = new TaskObjectNodeImpl(this, taskObjectName,
					this.sectionLocation, this.context);
			if (this.isOfficeContextLoaded) {
				// Add the office context to the task
				object.addOfficeContext(this.officeLocation);
			}

			// Add the task object
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
					this.sectionLocation, this.context);
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
			this.context.getCompilerIssues().addIssue(LocationType.SECTION,
					this.sectionLocation, null, null,
					"Task " + this.taskName + " linked more than once");
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
		ObjectDependency[] dependencies = this.taskObjects.values().toArray(
				new ObjectDependency[0]);
		Arrays.sort(dependencies, new Comparator<ObjectDependency>() {
			@Override
			public int compare(ObjectDependency a, ObjectDependency b) {
				return a.getObjectDependencyName().compareTo(
						b.getObjectDependencyName());
			}
		});
		return dependencies;
	}

	@Override
	public TaskTeam getTeamResponsible() {

		// Ensure have office context
		if (!this.isOfficeContextLoaded) {
			throw new IllegalStateException("Office context has not been added");
		}

		// Return the team responsible
		return this.teamResponsible;
	}

	@Override
	public void addPreTaskDuty(OfficeDuty duty) {

		// Ensure duty node
		if (!(duty instanceof DutyNode)) {
			this.addIssue("Invalid duty: " + duty + " ["
					+ (duty == null ? null : duty.getClass().getName()) + "]");
			return; // can not add duty
		}
		DutyNode dutyNode = (DutyNode) duty;

		// Add the pre task duty
		this.preTaskDuties.add(dutyNode);
	}

	@Override
	public void addPostTaskDuty(OfficeDuty duty) {

		// Ensure duty node
		if (!(duty instanceof DutyNode)) {
			this.addIssue("Invalid duty: " + duty + " ["
					+ (duty == null ? null : duty.getClass().getName()) + "]");
			return; // can not add duty
		}
		DutyNode dutyNode = (DutyNode) duty;

		// Add the post task duty
		this.postTaskDuties.add(dutyNode);
	}

	@Override
	public void addGovernance(OfficeGovernance governance) {

		// Ensure governance node
		if (!(governance instanceof GovernanceNode)) {
			this.addIssue("Invalid governance: "
					+ governance
					+ " ["
					+ (governance == null ? null : governance.getClass()
							.getName()) + "]");
			return; // can not add governance
		}
		GovernanceNode governanceNode = (GovernanceNode) governance;

		// Add the governance
		this.governances.add(governanceNode);
	}

}