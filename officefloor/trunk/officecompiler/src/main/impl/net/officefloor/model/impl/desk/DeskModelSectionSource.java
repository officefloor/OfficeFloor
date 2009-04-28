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
package net.officefloor.model.impl.desk;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskOperations;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link SectionSource} for a {@link DeskModel}.
 * 
 * @author Daniel
 */
public class DeskModelSectionSource extends AbstractSectionSource implements
		SectionSourceService {

	/*
	 * =================== SectionSourceService ===============================
	 */

	@Override
	public String getSectionSourceAlias() {
		return "DESK";
	}

	@Override
	public Class<? extends SectionSource> getSectionSourceClass() {
		return this.getClass();
	}

	/*
	 * ================= SectionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceSection(SectionDesigner designer,
			SectionSourceContext context) throws Exception {

		// Obtain the configuration to the desk
		ConfigurationItem configuration = context.getConfiguration(context
				.getSectionLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find desk '"
					+ context.getSectionLocation() + "'");
		}

		// Retrieve the desk model
		DeskModel desk = new DeskRepositoryImpl(new ModelRepositoryImpl())
				.retrieveDesk(configuration);

		// Add the external flows as outputs, keeping registry of the outputs
		Map<String, SectionOutput> sectionOutputs = new HashMap<String, SectionOutput>();
		for (ExternalFlowModel extFlow : desk.getExternalFlows()) {

			// Determine if escalation only (only has task escalation connected)
			boolean isEscalationOnly = ((extFlow.getPreviousTasks().size() == 0)
					&& (extFlow.getTaskFlows().size() == 0) && (extFlow
					.getTaskEscalations().size() > 0));

			// Add the section output and register
			String sectionOutputName = extFlow.getExternalFlowName();
			SectionOutput sectionOutput = designer.addSectionOutput(
					sectionOutputName, extFlow.getArgumentType(),
					isEscalationOnly);
			sectionOutputs.put(sectionOutputName, sectionOutput);
		}

		// Add the external managed objects as objects
		for (ExternalManagedObjectModel extMo : desk
				.getExternalManagedObjects()) {
			designer.addSectionObject(extMo.getExternalManagedObjectName(),
					extMo.getObjectType());
		}

		// Add the works, keeping registry of the tasks
		Map<String, SectionTask> tasks = new HashMap<String, SectionTask>();
		for (WorkModel workModel : desk.getWorks()) {

			// Add the work
			SectionWork work = designer.addSectionWork(workModel.getWorkName(),
					workModel.getWorkSourceClassName());
			for (PropertyModel property : workModel.getProperties()) {
				work.addProperty(property.getName(), property.getValue());
			}

			// Add the tasks for the work
			for (WorkTaskModel workTaskModel : workModel.getWorkTasks()) {
				for (WorkTaskToTaskModel conn : workTaskModel.getTasks()) {
					TaskModel taskModel = conn.getTask();
					if (taskModel != null) {
						// Add the task for the work and register
						String taskName = taskModel.getTaskName();
						SectionTask task = work.addSectionTask(taskName,
								taskModel.getWorkTaskName());
						tasks.put(taskName, task);
					}
				}
			}
		}

		// Link the flows for the task (as all tasks/external flows registered)
		for (TaskModel taskModel : desk.getTasks()) {

			// Obtain the task for the task model
			String taskName = taskModel.getTaskName();
			SectionTask task = tasks.get(taskName);
			if (task == null) {
				continue; // task not linked to work
			}

			// Link in the flows for the task
			for (TaskFlowModel taskFlowModel : taskModel.getTaskFlows()) {

				// Obtain the task flow
				String flowName = taskFlowModel.getFlowName();
				TaskFlow taskFlow = task.getTaskFlow(flowName);

				// Determine the instigation strategy
				FlowInstigationStrategyEnum instigationStrategy = null;

				// Determine if link flow to another task
				SectionTask linkedTask = null;
				TaskFlowToTaskModel flowToTask = taskFlowModel.getTask();
				if (flowToTask != null) {
					TaskModel linkedTaskModel = flowToTask.getTask();
					if (linkedTaskModel != null) {
						// Obtain the linked task and instigation strategy
						linkedTask = tasks.get(linkedTaskModel.getTaskName());
						instigationStrategy = this
								.getFlowInstatigationStrategy(flowToTask
										.getLinkType(), designer, taskName,
										flowName);
					}
				}
				if (linkedTask != null) {
					// Link the flow to its task
					designer.link(taskFlow, linkedTask, instigationStrategy);
				}

				// Determine if link flow to external flow
				SectionOutput linkedSectionOutput = null;
				TaskFlowToExternalFlowModel flowToExtFlow = taskFlowModel
						.getExternalFlow();
				if (flowToExtFlow != null) {
					ExternalFlowModel linkedExtFlow = flowToExtFlow
							.getExternalFlow();
					if (linkedExtFlow != null) {
						// Obtain the linked flow and instigation strategy
						linkedSectionOutput = sectionOutputs.get(linkedExtFlow
								.getExternalFlowName());
						instigationStrategy = this
								.getFlowInstatigationStrategy(flowToExtFlow
										.getLinkType(), designer, taskName,
										flowName);
					}
				}
				if (linkedSectionOutput != null) {
					// Link the flow to section output
					designer.link(taskFlow, linkedSectionOutput,
							instigationStrategy);
				}
			}

			// Determine if link task to next task
			SectionTask nextTask = null;
			TaskToNextTaskModel taskToNextTask = taskModel.getNextTask();
			if (taskToNextTask != null) {
				TaskModel nextTaskModel = taskToNextTask.getNextTask();
				if (nextTaskModel != null) {
					nextTask = tasks.get(nextTaskModel.getTaskName());
				}
			}
			if (nextTask != null) {
				// Link the task to its next task
				designer.link(task, nextTask);
			}

			// Determine if link task to next external flow
			SectionOutput nextSectionOutput = null;
			TaskToNextExternalFlowModel taskToNextExtFlow = taskModel
					.getNextExternalFlow();
			if (taskToNextExtFlow != null) {
				ExternalFlowModel nextExtFlow = taskToNextExtFlow
						.getNextExternalFlow();
				if (nextExtFlow != null) {
					nextSectionOutput = sectionOutputs.get(nextExtFlow
							.getExternalFlowName());
				}
			}
			if (nextSectionOutput != null) {
				// Link the task to its next section output
				designer.link(task, nextSectionOutput);
			}
		}

		// Add the public tasks as inputs and link to tasks
		for (TaskModel task : desk.getTasks()) {
			if (task.getIsPublic()) {

				// Obtain the work task
				WorkTaskModel workTask = null;
				WorkTaskToTaskModel conn = task.getWorkTask();
				if (conn != null) {
					workTask = conn.getWorkTask();
				}
				if (workTask == null) {
					designer.addIssue("Task not linked to a work task",
							AssetType.TASK, task.getTaskName());
					continue; // must have work task
				}

				// Determine the parameter type from the work task
				String parameterType = null;
				for (WorkTaskObjectModel taskObject : workTask.getTaskObjects()) {
					if (taskObject.getIsParameter()) {
						// TODO handle two parameters to work for a desk
						parameterType = taskObject.getObjectType();
					}
				}

				// Add the section input and register
				String taskName = task.getTaskName();
				SectionInput sectionInput = designer.addSectionInput(taskName,
						parameterType);

				// Obtain the section task and link input to task
				SectionTask sectionTask = tasks.get(taskName);
				designer.link(sectionInput, sectionTask);
			}
		}

	}

	/**
	 * Obtains the {@link FlowInstigationStrategyEnum}.
	 * 
	 * @param instigationStrategyName
	 *            Name identifying the {@link FlowInstigationStrategyEnum}.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param taskName
	 *            Name of the {@link Task} for reporting issues.
	 * @param flowName
	 *            Name of the {@link TaskFlow} for reporting issues.
	 * @return {@link FlowInstigationStrategyEnum}.
	 */
	private FlowInstigationStrategyEnum getFlowInstatigationStrategy(
			String instigationStrategyName, SectionDesigner designer,
			String taskName, String flowName) {

		// Obtain the flow instigation strategy
		if (DeskOperations.SEQUENTIAL_LINK.equals(instigationStrategyName)) {
			return FlowInstigationStrategyEnum.SEQUENTIAL;
		} else if (DeskOperations.PARALLEL_LINK.equals(instigationStrategyName)) {
			return FlowInstigationStrategyEnum.PARALLEL;
		} else if (DeskOperations.ASYNCHRONOUS_LINK
				.equals(instigationStrategyName)) {
			return FlowInstigationStrategyEnum.ASYNCHRONOUS;
		}

		// Unknown flow instigation strategy if at this point
		designer.addIssue("Unknown flow instigation strategy '"
				+ instigationStrategyName + "' for flow " + flowName,
				AssetType.TASK, taskName);
		return null;
	}
}