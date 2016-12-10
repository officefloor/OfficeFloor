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
package net.officefloor.eclipse.wizard.worksource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;

/**
 * Instance of a {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkInstance {

	/**
	 * Name of this {@link Work}.
	 */
	private final String workName;

	/**
	 * {@link WorkSource} class name.
	 */
	private final String workSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link WorkModel}.
	 */
	private final WorkModel workModel;

	/**
	 * {@link WorkType}.
	 */
	private final WorkType<?> workType;

	/**
	 * {@link TaskType} selected.
	 */
	private final TaskType<?, ?, ?>[] taskTypes;

	/**
	 * Names of the selected {@link TaskType} instances.
	 */
	private final String[] taskTypeNames;

	/**
	 * Mapping of {@link TaskType} name to {@link WorkTaskModel} name.
	 */
	private final Map<String, String> workTaskNameMapping;

	/**
	 * Mapping of {@link TaskObjectType} name to {@link WorkTaskObjectModel}
	 * name for a particular {@link WorkTaskModel} name.
	 */
	private final Map<String, Map<String, String>> taskObjectNameMappingForWorkTask;

	/**
	 * Mapping of {@link TaskFlowType} name to {@link TaskFlowModel} name for a
	 * particular {@link TaskModel} name.
	 */
	private final Map<String, Map<String, String>> taskFlowNameMappingForTask;

	/**
	 * Mapping of {@link TaskEscalationType} name to {@link TaskEscalationModel}
	 * name for a particular {@link TaskModel} name.
	 */
	private final Map<String, Map<String, String>> taskEscalationTypeMappingForTask;

	/**
	 * Initiate for public use.
	 * 
	 * @param workModel
	 *            {@link WorkModel}.
	 */
	public WorkInstance(WorkModel workModel) {
		this.workModel = workModel;
		this.workName = this.workModel.getWorkName();
		this.workSourceClassName = this.workModel.getWorkSourceClassName();
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		for (PropertyModel property : this.workModel.getProperties()) {
			this.propertyList.addProperty(property.getName()).setValue(
					property.getValue());
		}
		this.workType = null;
		this.taskTypes = null;
		this.workTaskNameMapping = null;
		this.taskObjectNameMappingForWorkTask = null;
		this.taskFlowNameMappingForTask = null;
		this.taskEscalationTypeMappingForTask = null;

		// Create the list of task type names
		List<String> workTaskNames = new LinkedList<String>();
		for (WorkTaskModel workTask : workModel.getWorkTasks()) {
			workTaskNames.add(workTask.getWorkTaskName());
		}
		this.taskTypeNames = workTaskNames.toArray(new String[0]);
	}

	/**
	 * Initiate from {@link WorkSourceInstance}.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workSourceClassName
	 *            {@link WorkSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param workType
	 *            {@link WorkType}.
	 * @param taskTypes
	 *            {@link TaskType} selected.
	 * @param workTaskNameMapping
	 *            Mapping of {@link TaskType} name to {@link WorkTaskModel}
	 *            name.
	 * @param taskObjectNameMappingForWorkTask
	 *            Mapping of {@link TaskObjectType} name to
	 *            {@link WorkTaskObjectModel} name for a particular
	 *            {@link WorkTaskModel} name.
	 * @param taskFlowNameMappingForTask
	 *            Mapping of {@link TaskFlowType} name to {@link TaskFlowModel}
	 *            name for a particular {@link TaskModel} name.
	 * @param taskEscalationTypeMappingForTask
	 *            Mapping of {@link TaskEscalationType} name to
	 *            {@link TaskEscalationModel} name for a particular
	 *            {@link TaskModel} name.
	 */
	WorkInstance(String workName, String workSourceClassName,
			PropertyList propertyList, WorkType<?> workType,
			TaskType<?, ?, ?>[] taskTypes,
			Map<String, String> workTaskNameMapping,
			Map<String, Map<String, String>> taskObjectNameMappingForWorkTask,
			Map<String, Map<String, String>> taskFlowNameMappingForTask,
			Map<String, Map<String, String>> taskEscalationTypeMappingForTask) {
		this.workName = workName;
		this.workSourceClassName = workSourceClassName;
		this.propertyList = propertyList;
		this.workModel = null;
		this.workType = workType;
		this.taskTypes = taskTypes;
		this.workTaskNameMapping = workTaskNameMapping;
		this.taskObjectNameMappingForWorkTask = taskObjectNameMappingForWorkTask;
		this.taskFlowNameMappingForTask = taskFlowNameMappingForTask;
		this.taskEscalationTypeMappingForTask = taskEscalationTypeMappingForTask;

		// Create the listing of task type names
		this.taskTypeNames = new String[this.taskTypes.length];
		for (int i = 0; i < this.taskTypeNames.length; i++) {
			this.taskTypeNames[i] = this.taskTypes[i].getTaskName();
		}
	}

	/**
	 * Obtains the name of the {@link Work}.
	 * 
	 * @return Name of the {@link Work}.
	 */
	public String getWorkName() {
		return this.workName;
	}

	/**
	 * Obtains the {@link WorkSource} class name.
	 * 
	 * @return {@link WorkSource} class name.
	 */
	public String getWorkSourceClassName() {
		return this.workSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertyList() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link WorkModel}.
	 * 
	 * @return {@link WorkModel} if instantiated by <code>public</code>
	 *         constructor or <code>null</code> if from
	 *         {@link WorkSourceInstance}.
	 */
	WorkModel getWorkModel() {
		return this.workModel;
	}

	/**
	 * Obtains the {@link WorkType}.
	 * 
	 * @return {@link WorkType} if obtained from {@link WorkSourceInstance} or
	 *         <code>null</code> if initiated by <code>public</code>
	 *         constructor.
	 */
	public WorkType<?> getWorkType() {
		return this.workType;
	}

	/**
	 * Obtains the {@link TaskType} instances.
	 * 
	 * @return {@link TaskType} instances if obtained from
	 *         {@link WorkSourceInstance} or <code>null</code> if initiated by
	 *         <code>public</code> constructor.
	 */
	public TaskType<?, ?, ?>[] getTaskTypes() {
		return this.taskTypes;
	}

	/**
	 * Obtains the names of the {@link TaskType} instances being used on the
	 * {@link WorkType}.
	 * 
	 * @return Names of the {@link TaskType} instances being used on the
	 *         {@link WorkType}.
	 */
	public String[] getTaskTypeNames() {
		return this.taskTypeNames;
	}

	/**
	 * Obtains the mapping of {@link TaskType} name to {@link WorkTaskModel}
	 * name.
	 * 
	 * @return Mapping of {@link TaskType} name to {@link WorkTaskModel} name.
	 */
	public Map<String, String> getWorkTaskNameMapping() {
		return this.workTaskNameMapping;
	}

	/**
	 * Obtains the mapping of {@link TaskObjectType} name to
	 * {@link WorkTaskObjectModel} name for a particular {@link WorkTaskModel}
	 * name.
	 * 
	 * @return Mapping of {@link TaskObjectType} name to
	 *         {@link WorkTaskObjectModel} name for a particular
	 *         {@link WorkTaskModel} name.
	 */
	public Map<String, Map<String, String>> getTaskObjectNameMappingForWorkTask() {
		return this.taskObjectNameMappingForWorkTask;
	}

	/**
	 * Obtains the mapping of {@link TaskFlowType} name to {@link TaskFlowModel}
	 * name for a particular {@link TaskModel} name.
	 * 
	 * @return Mapping of {@link TaskFlowType} name to {@link TaskFlowModel}
	 *         name for a particular {@link TaskModel} name.
	 */
	public Map<String, Map<String, String>> getTaskFlowNameMappingForTask() {
		return this.taskFlowNameMappingForTask;
	}

	/**
	 * Obtains the mapping of {@link TaskEscalationType} name to
	 * {@link TaskEscalationModel} name for a particular {@link TaskModel} name.
	 * 
	 * @return Mapping of {@link TaskEscalationType} name to
	 *         {@link TaskEscalationModel} name for a particular
	 *         {@link TaskModel} name.
	 */
	public Map<String, Map<String, String>> getTaskEscalationTypeMappingForTask() {
		return this.taskEscalationTypeMappingForTask;
	}

}