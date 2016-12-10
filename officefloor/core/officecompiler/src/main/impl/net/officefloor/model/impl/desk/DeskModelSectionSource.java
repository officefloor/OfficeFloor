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
package net.officefloor.model.impl.desk;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.SectionSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskManagedObjectDependencyModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToTaskModel;
import net.officefloor.model.desk.DeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskEscalationToExternalFlowModel;
import net.officefloor.model.desk.TaskEscalationToTaskModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskFlowToExternalFlowModel;
import net.officefloor.model.desk.TaskFlowToTaskModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.TaskToNextExternalFlowModel;
import net.officefloor.model.desk.TaskToNextTaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToDeskManagedObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskToTaskModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.inputstream.InputStreamConfigurationItem;
import net.officefloor.model.section.SectionManagedObjectModel;

/**
 * {@link SectionSource} for a {@link DeskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskModelSectionSource extends AbstractSectionSource implements
		SectionSourceService<DeskModelSectionSource> {

	/*
	 * =================== SectionSourceService ===============================
	 */

	@Override
	public String getSectionSourceAlias() {
		return "DESK";
	}

	@Override
	public Class<DeskModelSectionSource> getSectionSourceClass() {
		return DeskModelSectionSource.class;
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
		InputStream configuration = context.getResource(context
				.getSectionLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find desk '"
					+ context.getSectionLocation() + "'");
		}

		// Retrieve the desk model
		DeskModel desk = new DeskRepositoryImpl(new ModelRepositoryImpl())
				.retrieveDesk(new InputStreamConfigurationItem(configuration));

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

		// Add the external managed objects as objects, keeping registry of them
		Map<String, SectionObject> sectionObjects = new HashMap<String, SectionObject>();
		for (ExternalManagedObjectModel extMo : desk
				.getExternalManagedObjects()) {
			String sectionObjectName = extMo.getExternalManagedObjectName();
			SectionObject sectionObject = designer.addSectionObject(
					sectionObjectName, extMo.getObjectType());
			sectionObjects.put(sectionObjectName, sectionObject);
		}

		// Add the managed object sources, keeping registry of them
		Map<String, SectionManagedObjectSource> managedObjectSources = new HashMap<String, SectionManagedObjectSource>();
		for (DeskManagedObjectSourceModel mosModel : desk
				.getDeskManagedObjectSources()) {

			// Add the managed object source
			String mosName = mosModel.getDeskManagedObjectSourceName();
			SectionManagedObjectSource mos = designer
					.addSectionManagedObjectSource(mosName,
							mosModel.getManagedObjectSourceClassName());
			for (PropertyModel property : mosModel.getProperties()) {
				mos.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout
			String timeoutValue = mosModel.getTimeout();
			if (!CompileUtil.isBlank(timeoutValue)) {
				try {
					mos.setTimeout(Long.valueOf(timeoutValue));
				} catch (NumberFormatException ex) {
					designer.addIssue("Invalid timeout value: " + timeoutValue
							+ " for managed object source " + mosName);
				}
			}

			// Register the managed object source
			managedObjectSources.put(mosName, mos);
		}

		// Add the managed objects, keeping registry of them
		Map<String, SectionManagedObject> managedObjects = new HashMap<String, SectionManagedObject>();
		for (DeskManagedObjectModel moModel : desk.getDeskManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = moModel.getDeskManagedObjectName();
			ManagedObjectScope managedObjectScope = this.getManagedObjectScope(
					moModel.getManagedObjectScope(), designer,
					managedObjectName);

			// Obtain the managed object source for the managed object
			SectionManagedObjectSource moSource = null;
			DeskManagedObjectToDeskManagedObjectSourceModel moToSource = moModel
					.getDeskManagedObjectSource();
			if (moToSource != null) {
				DeskManagedObjectSourceModel moSourceModel = moToSource
						.getDeskManagedObjectSource();
				if (moSourceModel != null) {
					moSource = managedObjectSources.get(moSourceModel
							.getDeskManagedObjectSourceName());
				}
			}
			if (moSource == null) {
				continue; // must have managed object source
			}

			// Add the managed object and also register it
			SectionManagedObject managedObject = moSource
					.addSectionManagedObject(managedObjectName,
							managedObjectScope);
			managedObjects.put(managedObjectName, managedObject);
		}

		// Link managed object dependencies to managed objects/external objects
		for (DeskManagedObjectModel moModel : desk.getDeskManagedObjects()) {

			// Obtain the managed object
			SectionManagedObject managedObject = managedObjects.get(moModel
					.getDeskManagedObjectName());
			if (managedObject == null) {
				continue; // should always have
			}

			// Link dependencies to managed object/external object
			for (DeskManagedObjectDependencyModel dependencyModel : moModel
					.getDeskManagedObjectDependencies()) {

				// Obtain the dependency
				ManagedObjectDependency dependency = managedObject
						.getManagedObjectDependency(dependencyModel
								.getDeskManagedObjectDependencyName());

				// Link dependency to managed object
				SectionManagedObject linkedManagedObject = null;
				DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToMo = dependencyModel
						.getDeskManagedObject();
				if (dependencyToMo != null) {
					DeskManagedObjectModel linkedMoModel = dependencyToMo
							.getDeskManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel
								.getDeskManagedObjectName());
					}
				}
				if (linkedManagedObject != null) {
					// Link dependency to managed object
					designer.link(dependency, linkedManagedObject);
				}

				// Link dependency to external managed object
				SectionObject linkedObject = null;
				DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = dependencyModel
						.getExternalManagedObject();
				if (dependencyToExtMo != null) {
					ExternalManagedObjectModel extMoModel = dependencyToExtMo
							.getExternalManagedObject();
					if (extMoModel != null) {
						linkedObject = sectionObjects.get(extMoModel
								.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link dependency to external managed object
					designer.link(dependency, linkedObject);
				}
			}
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

			// Determine if an initial task for the work
			TaskModel initialTask = null;
			WorkToInitialTaskModel initialTaskConn = workModel.getInitialTask();
			if (initialTaskConn != null) {
				initialTask = initialTaskConn.getInitialTask();
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

						// Determine if the initial task
						if (taskModel == initialTask) {
							// Specify as the initial task
							work.setInitialTask(task);
						}
					}
				}
			}
		}

		// Link the flows/objects/escalations (as all links registered)
		for (TaskModel taskModel : desk.getTasks()) {

			// Obtain the task for the task model
			String taskName = taskModel.getTaskName();
			SectionTask task = tasks.get(taskName);
			if (task == null) {
				continue; // task not linked to work
			}

			// Obtain the work task for the task
			WorkTaskModel workTaskModel = null;
			WorkTaskToTaskModel workTaskToTask = taskModel.getWorkTask();
			if (workTaskToTask != null) {
				workTaskModel = workTaskToTask.getWorkTask();
			}
			if (workTaskModel != null) {
				// Link in the objects for the task
				for (WorkTaskObjectModel taskObjectModel : workTaskModel
						.getTaskObjects()) {

					// Obtain the task object
					String objectName = taskObjectModel.getObjectName();
					TaskObject taskObject = task.getTaskObject(objectName);

					// Determine if object is a parameter
					if (taskObjectModel.getIsParameter()) {
						taskObject.flagAsParameter();
						continue; // flagged as parameter
					}

					// Determine if link object to external managed object
					SectionObject linkedSectionObject = null;
					WorkTaskObjectToExternalManagedObjectModel objectToExtMo = taskObjectModel
							.getExternalManagedObject();
					if (objectToExtMo != null) {
						ExternalManagedObjectModel linkedExtMo = objectToExtMo
								.getExternalManagedObject();
						if (linkedExtMo != null) {
							// Obtain the linked section object
							linkedSectionObject = sectionObjects
									.get(linkedExtMo
											.getExternalManagedObjectName());
						}
					}
					if (linkedSectionObject != null) {
						// Link the object to its section object
						designer.link(taskObject, linkedSectionObject);
					}

					// Determine if link object to managed object
					SectionManagedObject linkedManagedObject = null;
					WorkTaskObjectToDeskManagedObjectModel objectToMo = taskObjectModel
							.getDeskManagedObject();
					if (objectToMo != null) {
						DeskManagedObjectModel linkedMo = objectToMo
								.getDeskManagedObject();
						if (linkedMo != null) {
							linkedManagedObject = managedObjects.get(linkedMo
									.getDeskManagedObjectName());
						}
					}
					if (linkedManagedObject != null) {
						// Link the object to its managed object
						designer.link(taskObject, linkedManagedObject);
					}
				}
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
								.getFlowInstatigationStrategy(
										flowToTask.getLinkType(), designer,
										taskName, flowName);
					}
				}
				if (linkedTask != null) {
					// Link the flow to its task
					designer.link(taskFlow, linkedTask, instigationStrategy);
					continue;
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
								.getFlowInstatigationStrategy(
										flowToExtFlow.getLinkType(), designer,
										taskName, flowName);
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

			// Link in the escalations for the task
			for (TaskEscalationModel taskEscalationModel : taskModel
					.getTaskEscalations()) {

				// Obtain the task escalation
				String escalationTypeName = taskEscalationModel
						.getEscalationType();
				TaskFlow taskEscalation = task
						.getTaskEscalation(escalationTypeName);

				// Determine if link escalation to another task
				SectionTask linkedTask = null;
				TaskEscalationToTaskModel escalationToTask = taskEscalationModel
						.getTask();
				if (escalationToTask != null) {
					TaskModel linkedTaskModel = escalationToTask.getTask();
					if (linkedTaskModel != null) {
						linkedTask = tasks.get(linkedTaskModel.getTaskName());
					}
				}
				if (linkedTask != null) {
					// Link the escalation to its task
					designer.link(taskEscalation, linkedTask,
							FlowInstigationStrategyEnum.PARALLEL);
				}

				// Determine if link escalation to section output
				SectionOutput linkedSectionOutput = null;
				TaskEscalationToExternalFlowModel escalationToExtFlow = taskEscalationModel
						.getExternalFlow();
				if (escalationToExtFlow != null) {
					ExternalFlowModel linkedExtFlow = escalationToExtFlow
							.getExternalFlow();
					if (linkedExtFlow != null) {
						linkedSectionOutput = sectionOutputs.get(linkedExtFlow
								.getExternalFlowName());
					}
				}
				if (linkedSectionOutput != null) {
					// Link the escalation to its section output
					designer.link(taskEscalation, linkedSectionOutput,
							FlowInstigationStrategyEnum.PARALLEL);
				}
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
					designer.addIssue("Task " + task.getTaskName()
							+ " not linked to a work task");
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

		// Link managed object source flows to tasks/section outputs
		for (DeskManagedObjectSourceModel mosModel : desk
				.getDeskManagedObjectSources()) {

			// Obtain the managed object source
			SectionManagedObjectSource mos = managedObjectSources.get(mosModel
					.getDeskManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have
			}

			// Link flows to tasks/section outputs
			for (DeskManagedObjectSourceFlowModel mosFlowModel : mosModel
					.getDeskManagedObjectSourceFlows()) {

				// Obtain the managed object source flow
				ManagedObjectFlow mosFlow = mos
						.getManagedObjectFlow(mosFlowModel
								.getDeskManagedObjectSourceFlowName());

				// Link managed object source flow to task
				SectionTask linkedTask = null;
				DeskManagedObjectSourceFlowToTaskModel flowToTask = mosFlowModel
						.getTask();
				if (flowToTask != null) {
					TaskModel taskModel = flowToTask.getTask();
					if (taskModel != null) {
						linkedTask = tasks.get(taskModel.getTaskName());
					}
				}
				if (linkedTask != null) {
					// Link managed object source flow to task
					designer.link(mosFlow, linkedTask);
				}

				// Link managed object source flow to external flow
				SectionOutput linkedOutput = null;
				DeskManagedObjectSourceFlowToExternalFlowModel flowToOutput = mosFlowModel
						.getExternalFlow();
				if (flowToOutput != null) {
					ExternalFlowModel extOutput = flowToOutput
							.getExternalFlow();
					if (extOutput != null) {
						linkedOutput = sectionOutputs.get(extOutput
								.getExternalFlowName());
					}
				}
				if (linkedOutput != null) {
					// Link managed object source flow to external flow
					designer.link(mosFlow, linkedOutput);
				}
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
		if (DeskChanges.SEQUENTIAL_LINK.equals(instigationStrategyName)) {
			return FlowInstigationStrategyEnum.SEQUENTIAL;
		} else if (DeskChanges.PARALLEL_LINK.equals(instigationStrategyName)) {
			return FlowInstigationStrategyEnum.PARALLEL;
		} else if (DeskChanges.ASYNCHRONOUS_LINK
				.equals(instigationStrategyName)) {
			return FlowInstigationStrategyEnum.ASYNCHRONOUS;
		}

		// Unknown flow instigation strategy if at this point
		designer.addIssue("Unknown flow instigation strategy '"
				+ instigationStrategyName + "' for flow " + flowName
				+ " of task " + taskName);
		return null;
	}

	/**
	 * Obtains the {@link ManagedObjectScope} from the managed object scope
	 * name.
	 * 
	 * @param managedObjectScope
	 *            Name of the {@link ManagedObjectScope}.
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param managedObjectName
	 *            Name of the {@link SectionManagedObjectModel}.
	 * @return {@link ManagedObjectScope} or <code>null</code> with issue
	 *         reported to the {@link SectionDesigner}.
	 */
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope,
			SectionDesigner designer, String managedObjectName) {

		// Obtain the managed object scope
		if (DeskChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (DeskChanges.THREAD_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (DeskChanges.WORK_MANAGED_OBJECT_SCOPE
				.equals(managedObjectScope)) {
			return ManagedObjectScope.WORK;
		}

		// Unknown scope if at this point
		designer.addIssue("Unknown managed object scope " + managedObjectScope
				+ " for managed object " + managedObjectName);
		return null;
	}

}