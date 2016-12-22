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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.TaskEscalationModel;
import net.officefloor.model.desk.TaskFlowModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;

/**
 * Abstract functionality to test refactoring the {@link WorkModel} to a
 * {@link FunctionNamespaceType} via the {@link DeskChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRefactorWorkTest extends
		AbstractDeskChangesTestCase {

	/**
	 * {@link WorkModel} to refactor.
	 */
	private WorkModel workModel;

	/**
	 * Name to refactor the {@link WorkModel} to have.
	 */
	private String workName;

	/**
	 * {@link ManagedFunctionSource} class name to refactor the {@link WorkModel} to have.
	 */
	private String workSourceClassName;

	/**
	 * {@link PropertyList} to refactor the {@link WorkModel} to have.
	 */
	private PropertyList properties = null;

	/**
	 * Mapping of {@link ManagedFunctionType} name to {@link WorkTaskModel} name.
	 */
	private final Map<String, String> workTaskNameMapping = new HashMap<String, String>();

	/**
	 * Mapping for a {@link WorkTaskModel} of the {@link ManagedFunctionObjectType} name to
	 * the {@link WorkTaskObjectModel} name.
	 */
	private final Map<String, Map<String, String>> workTaskToObjectNameMapping = new HashMap<String, Map<String, String>>();

	/**
	 * Mapping for a {@link TaskModel} of the {@link ManagedFunctionFlowType} name to the
	 * {@link TaskFlowModel} name.
	 */
	private final Map<String, Map<String, String>> taskToFlowNameMapping = new HashMap<String, Map<String, String>>();

	/**
	 * Mapping for a {@link TaskModel} of the {@link ManagedFunctionEscalationType} name to
	 * the {@link TaskEscalationModel} name.
	 */
	private final Map<String, Map<String, String>> taskToEscalationTypeMapping = new HashMap<String, Map<String, String>>();

	/**
	 * Listing of {@link ManagedFunctionType} names included on the {@link WorkModel}.
	 */
	private String[] tasks = null;

	/**
	 * Initiate for specific setup per test.
	 */
	public AbstractRefactorWorkTest() {
		super(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.model.impl.AbstractChangesTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {

		// Setup the model
		super.setUp();

		// Obtain work model and specify details from it
		this.workModel = this.model.getWorks().get(0);
		this.workName = this.workModel.getWorkName();
		this.workSourceClassName = this.workModel.getWorkSourceClassName();

	}

	/**
	 * Flags to refactor the name of the {@link WorkModel}.
	 * 
	 * @param workName
	 *            New name for the {@link WorkModel}.
	 */
	protected void refactor_workName(String workName) {
		this.workName = workName;
	}

	/**
	 * Flags to refactor the {@link ManagedFunctionSource} class name for the
	 * {@link WorkModel}.
	 * 
	 * @param workSourceClassName
	 *            New {@link ManagedFunctionSource} class name for the {@link WorkModel}.
	 */
	protected void refactor_workSourceClassName(String workSourceClassName) {
		this.workSourceClassName = workSourceClassName;
	}

	/**
	 * Flags to refactor the {@link PropertyModel} instances for the
	 * {@link WorkModel}.
	 * 
	 * @param name
	 *            {@link PropertyModel} name.
	 * @param value
	 *            {@link PropertyModel} value.
	 */
	protected void refactor_addProperty(String name, String value) {
		// Lazy create the property list
		if (this.properties == null) {
			this.properties = new PropertyListImpl();
		}

		// Add the property
		this.properties.addProperty(name).setValue(value);
	}

	/**
	 * Maps the {@link ManagedFunctionType} to the {@link WorkTaskModel}.
	 * 
	 * @param taskTypeName
	 *            Name of the {@link ManagedFunctionType}.
	 * @param workTaskModelName
	 *            Name of the {@link WorkTaskModel}.
	 */
	protected void refactor_mapTask(String taskTypeName,
			String workTaskModelName) {
		this.workTaskNameMapping.put(taskTypeName, workTaskModelName);
	}

	/**
	 * Maps the {@link ManagedFunctionObjectType} name to the {@link WorkTaskObjectModel}
	 * name for a {@link WorkTaskModel}.
	 * 
	 * @param workTaskName
	 *            Name of the {@link WorkTaskModel}.
	 * @param objectTypeName
	 *            Name of the {@link ManagedFunctionObjectType}.
	 * @param workTaskObjectName
	 *            Name of the {@link WorkTaskObjectModel}.
	 */
	protected void refactor_mapObject(String workTaskName,
			String objectTypeName, String workTaskObjectName) {
		this.map(workTaskName, objectTypeName, workTaskObjectName,
				this.workTaskToObjectNameMapping);
	}

	/**
	 * Maps the {@link ManagedFunctionFlowType} name to the {@link TaskFlowModel} name for
	 * a {@link TaskModel}.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskModel}.
	 * @param flowTypeName
	 *            Name of the {@link ManagedFunctionFlowType}.
	 * @param taskFlowName
	 *            Name of the {@link TaskFlowModel}.
	 */
	protected void refactor_mapFlow(String taskName, String flowTypeName,
			String taskFlowName) {
		this.map(taskName, flowTypeName, taskFlowName,
				this.taskToFlowNameMapping);
	}

	/**
	 * Maps the {@link ManagedFunctionEscalationType} name to the
	 * {@link TaskEscalationModel} name for the {@link TaskModel}.
	 * 
	 * @param taskName
	 *            Name of the {@link TaskModel}.
	 * @param escalationTypeName
	 *            Name of the {@link ManagedFunctionEscalationType}.
	 * @param taskEscalationName
	 *            Name of the {@link TaskEscalationModel}.
	 */
	protected void refactor_mapEscalation(String taskName,
			String escalationTypeName, String taskEscalationName) {
		this.map(taskName, escalationTypeName, taskEscalationName,
				this.taskToEscalationTypeMapping);
	}

	/**
	 * Maps in the values.
	 * 
	 * @param a
	 *            First value.
	 * @param b
	 *            Second value.
	 * @param c
	 *            Third value.
	 * @param map
	 *            Map to have values loaded.
	 */
	private void map(String a, String b, String c,
			Map<String, Map<String, String>> map) {

		// Obtain map by a
		Map<String, String> aMap = map.get(a);
		if (aMap == null) {
			aMap = new HashMap<String, String>();
			map.put(a, aMap);
		}

		// Load b and c to aMap
		aMap.put(b, c);
	}

	/**
	 * Specifies the names of the {@link ManagedFunctionType} instances to include on the
	 * {@link WorkModel}.
	 * 
	 * @param taskNames
	 *            Names of the {@link ManagedFunctionType} instances to include on the
	 *            {@link WorkModel}.
	 */
	protected void refactor_includeTasks(String... taskNames) {
		this.tasks = taskNames;
	}

	/**
	 * Convenience method to do refactoring with a simple {@link FunctionNamespaceType}.
	 */
	protected void doRefactor() {
		this.doRefactor((FunctionNamespaceType<?>) null);
	}

	/**
	 * Convenience method to do refactoring and validates applying and
	 * reverting.
	 * 
	 * @param workTypeConstructor
	 *            {@link WorkTypeConstructor}.
	 */
	protected void doRefactor(WorkTypeConstructor workTypeConstructor) {

		// Construct the work type
		FunctionNamespaceType<?> workType = this.constructWorkType(workTypeConstructor);

		// Do the refactoring
		this.doRefactor(workType);
	}

	/**
	 * Does the refactoring and validates applying and reverting.
	 * 
	 * @param workType
	 *            {@link FunctionNamespaceType}.
	 */
	protected void doRefactor(FunctionNamespaceType<?> workType) {

		// Ensure have a work type
		if (workType == null) {
			// Create simple work type
			workType = this.constructWorkType(new WorkTypeConstructor() {
				@Override
				public void construct(WorkTypeContext context) {
					// Simple work type
				}
			});
		}

		// Create the property list
		PropertyList propertyList = this.properties;
		if (propertyList == null) {
			// Not refactoring properties, so take from work model
			propertyList = new PropertyListImpl();
			for (PropertyModel property : this.workModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(
						property.getValue());
			}
		}

		// Create the listing of tasks
		String[] taskNames = this.tasks;
		if (taskNames == null) {
			// Not refactoring tasks, so take from work model
			List<String> taskNameList = new LinkedList<String>();
			for (WorkTaskModel workTask : this.workModel.getWorkTasks()) {
				taskNameList.add(workTask.getWorkTaskName());
			}
			taskNames = taskNameList.toArray(new String[0]);
		}

		// Create the change to refactor
		Change<WorkModel> change = this.operations.refactorWork(this.workModel,
				this.workName, this.workSourceClassName, propertyList,
				workType, this.workTaskNameMapping,
				this.workTaskToObjectNameMapping, this.taskToFlowNameMapping,
				this.taskToEscalationTypeMapping, taskNames);

		// Asset the refactoring changes
		this.assertChange(change, this.workModel, "Refactor work", true);
	}

}