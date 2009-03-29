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
package net.officefloor.compile.impl.work;

import java.util.Arrays;
import java.util.Comparator;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.TaskEscalationType;
import net.officefloor.compile.spi.work.TaskFlowType;
import net.officefloor.compile.spi.work.TaskObjectType;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkLoader;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.compile.spi.work.source.WorkSourceProperty;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkUnknownPropertyError;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;

/**
 * {@link WorkLoader} implementation.
 * 
 * @author Daniel
 */
public class WorkLoaderImpl implements WorkLoader {

	/**
	 * Location of the {@link DeskModel}.
	 */
	private final String deskLocation;

	/**
	 * Name of the {@link Work}.
	 */
	private final String workName;

	/**
	 * Initiate.
	 * 
	 * @param deskLocation
	 *            Location of the {@link DeskModel}.
	 * @param workName
	 *            Name of the {@link Work}.
	 */
	public WorkLoaderImpl(String deskLocation, String workName) {
		this.deskLocation = deskLocation;
		this.workName = workName;
	}

	/*
	 * ====================== WorkLoader ====================================
	 */

	@Override
	public <W extends Work, WS extends WorkSource<W>> PropertyList loadSpecification(
			Class<WS> workSourceClass, CompilerIssues issues) {

		// Instantiate the work source
		WorkSource<W> workSource = CompileUtil.newInstance(workSourceClass,
				WorkSource.class, LocationType.DESK, this.deskLocation,
				AssetType.WORK, this.workName, issues);
		if (workSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		WorkSourceSpecification specification;
		try {
			specification = workSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ WorkSourceSpecification.class.getSimpleName() + " from "
					+ workSourceClass.getName(), ex, issues);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + WorkSourceSpecification.class.getSimpleName()
					+ " returned from " + workSourceClass.getName(), issues);
			return null; // no specification obtained
		}

		// Obtain the properties
		WorkSourceProperty[] workProperties;
		try {
			workProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ WorkSourceProperty.class.getSimpleName()
					+ " instances from "
					+ WorkSourceSpecification.class.getSimpleName() + " for "
					+ workSourceClass.getName(), ex, issues);
			return null; // failed to obtain properties
		}

		// Load the work properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (workProperties != null) {
			for (int i = 0; i < workProperties.length; i++) {
				WorkSourceProperty workProperty = workProperties[i];

				// Ensure have the work property
				if (workProperty == null) {
					this.addIssue(WorkSourceProperty.class.getSimpleName()
							+ " " + i + " is null from "
							+ WorkSourceSpecification.class.getSimpleName()
							+ " for " + workSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = workProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for "
							+ WorkSourceProperty.class.getSimpleName() + " "
							+ i + " from "
							+ WorkSourceSpecification.class.getSimpleName()
							+ " for " + workSourceClass.getName(), ex, issues);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(WorkSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ WorkSourceSpecification.class.getSimpleName()
							+ " for " + workSourceClass.getName(), issues);
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = workProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for "
							+ WorkSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from "
							+ WorkSourceSpecification.class.getSimpleName()
							+ " for " + workSourceClass.getName(), ex, issues);
					return null; // must have complete property details
				}

				// Add to the properties
				propertyList.addProperty(name, label);
			}
		}

		// Return the property list
		return propertyList;
	}

	@Override
	public <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWork(
			Class<WS> workSourceClass, PropertyList propertyList,
			ClassLoader classLoader, CompilerIssues issues) {

		// Instantiate the work source
		WorkSource<W> workSource = CompileUtil.newInstance(workSourceClass,
				WorkSource.class, LocationType.DESK, this.deskLocation,
				AssetType.WORK, this.workName, issues);
		if (workSource == null) {
			return null; // failed to instantiate
		}

		// Create the work source context
		WorkSourceContext context = new WorkSourceContextImpl(propertyList,
				classLoader);

		// Create the work type builder
		WorkTypeImpl<W> workType = new WorkTypeImpl<W>();

		try {
			// Source the work type
			workSource.sourceWork(workType, context);

		} catch (WorkUnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknonwnPropertyName()
					+ "' for " + WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName(), issues);
			return null; // must have property

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + WorkType.class.getSimpleName()
					+ " definition from " + WorkSource.class.getSimpleName()
					+ " " + workSourceClass.getName(), ex, issues);
			return null; // must be successful
		}

		// Ensure has a work factory
		if (workType.getWorkFactory() == null) {
			this.addIssue("No " + WorkFactory.class.getSimpleName()
					+ " provided by " + WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName(), issues);
			return null; // must have complete type
		}

		// Ensure has at least one task
		TaskType<W, ?, ?>[] taskTypes = workType.getTaskTypes();
		if (taskTypes.length == 0) {
			this.addIssue("No " + TaskType.class.getSimpleName()
					+ " definitions provided by "
					+ WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName(), issues);
			return null; // must have complete type
		}

		// Ensure the task definitions are valid
		for (int i = 0; i < taskTypes.length; i++) {
			if (!this.isValidTaskType(taskTypes[i], i, workSourceClass, issues)) {
				return null; // must be completely valid type
			}
		}

		// Return the work type
		return workType;
	}

	/**
	 * Determines if the input {@link TaskType} is valid.
	 * 
	 * @param taskType
	 *            {@link TaskType} to validate.
	 * @param taskIndex
	 *            Index of the {@link TaskType} on the {@link WorkType}.
	 * @param workSourceClass
	 *            {@link WorkSource} providing the {@link WorkType}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return <code>true</code> if {@link TaskType} is valid.
	 */
	private <W extends Work, M extends Enum<M>, F extends Enum<F>> boolean isValidTaskType(
			TaskType<W, M, F> taskType, int taskIndex,
			Class<?> workSourceClass, CompilerIssues issues) {

		// Ensure has name
		String taskName = taskType.getTaskName();
		if (CompileUtil.isBlank(taskName)) {
			this.addTaskIssue("No task name provided for", issues, taskIndex,
					taskName, workSourceClass);
			return false; // must have complete type
		}

		// Ensure has task factory manufacturer
		if (taskType.getTaskFactoryManufacturer() == null) {
			this.addTaskIssue("No "
					+ TaskFactoryManufacturer.class.getSimpleName()
					+ " provided for", issues, taskIndex, taskName,
					workSourceClass);
			return false; // must have complete type
		}

		// Obtain the object keys class (taking into account Indexed)
		Class<M> objectKeysClass = taskType.getObjectKeyClass();
		if ((objectKeysClass != null)
				&& (!objectKeysClass.equals(Indexed.class))) {
			// Is valid by keys
			if (!this.isValidObjects(taskType, objectKeysClass, taskIndex,
					taskName, workSourceClass, issues)) {
				return false; // must be valid
			}
		} else {
			// Is valid by indexes
			if (!this.isValidObjects(taskType, taskIndex, taskName,
					workSourceClass, issues)) {
				return false; // must be valid
			}
		}

		// Validate common details for objects
		for (TaskObjectType<M> objectType : taskType.getObjectTypes()) {

			// Must have names for objects
			String objectName = objectType.getObjectName();
			if (CompileUtil.isBlank(objectName)) {
				this.addTaskIssue("No object name on", issues, taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}

			// Must have object types
			if (objectType.getObjectType() == null) {
				this.addTaskIssue("No object type provided for object "
						+ objectName + " on", issues, taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}
		}

		// Obtain the flow keys class (taking into account Indexed)
		Class<F> flowKeysClass = taskType.getFlowKeyClass();
		if ((flowKeysClass != null) && (!flowKeysClass.equals(Indexed.class))) {
			// Is valid by keys
			if (!this.isValidFlows(taskType, flowKeysClass, taskIndex,
					taskName, workSourceClass, issues)) {
				return false; // must be valid
			}
		} else {
			// Is valid by indexes
			if (!this.isValidFlows(taskType, taskIndex, taskName,
					workSourceClass, issues)) {
				return false; // must be valid
			}
		}

		// Validate common details for flows
		for (TaskFlowType<F> flowType : taskType.getFlowTypes()) {

			// Must have names for flows
			if (CompileUtil.isBlank(flowType.getFlowName())) {
				this.addTaskIssue("No flow name on", issues, taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}
		}

		// Validate the escalations
		for (TaskEscalationType escalationType : taskType.getEscalationTypes()) {

			// Must have escalation type
			if (escalationType.getEscalationType() == null) {
				this.addTaskIssue("No escalation type on", issues, taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}

			// Must have names for escalations
			if (CompileUtil.isBlank(escalationType.getEscalationName())) {
				this.addTaskIssue("No escalation name on", issues, taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link TaskObjectType} instances are valid given an
	 * {@link Enum} providing the dependent {@link Object} keys.
	 * 
	 * @param taskType
	 *            {@link TaskType}.
	 * @param objectKeysClass
	 *            {@link Enum} providing the keys.
	 * @param taskIndex
	 *            Index of the {@link TaskType} on the {@link WorkType}.
	 * @param taskName
	 *            Name of the {@link TaskType}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return <code>true</code> if {@link TaskObjectType} instances are valid.
	 */
	private <M extends Enum<M>> boolean isValidObjects(
			TaskType<?, M, ?> taskType, Class<M> objectKeysClass,
			int taskIndex, String taskName, Class<?> workSourceClass,
			CompilerIssues issues) {

		// Obtain the keys are sort by ordinal just to be sure
		M[] keys = objectKeysClass.getEnumConstants();
		Arrays.sort(keys, new Comparator<M>() {
			@Override
			public int compare(M a, M b) {
				return a.ordinal() - b.ordinal();
			}
		});

		// Validate the task object types
		TaskObjectType<M>[] objectTypes = taskType.getObjectTypes();
		for (int i = 0; i < keys.length; i++) {
			M key = keys[i];
			TaskObjectType<M> objectType = (objectTypes.length > i ? objectTypes[i]
					: null);

			// Ensure object type for the key
			if (objectType == null) {
				this.addTaskIssue("No " + TaskObjectType.class.getSimpleName()
						+ " provided for key " + key + " on", issues,
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure have key identifying the object
			M typeKey = objectType.getKey();
			if (typeKey == null) {
				this.addTaskIssue("No key provided for an object on", issues,
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the key is of correct type
			if (!objectKeysClass.isInstance(typeKey)) {
				this.addTaskIssue("Incorrect key type ("
						+ typeKey.getClass().getName()
						+ ") provided for an object on", issues, taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure is the correct expected key
			if (key != typeKey) {
				this.addIssue("Incorrect object key (" + typeKey
						+ ") as was expecting " + key + " on", issues);
				return false; // not valid
			}

			// Ensure the index matches the ordinal for the key
			int index = objectType.getIndex();
			if (index != key.ordinal()) {
				this.addIssue("Index (" + index
						+ ") of object does match ordinal of key (" + key
						+ ") on", issues);
				return false; // not valid
			}
		}

		// Ensure there are no addition objects than keys
		if (objectTypes.length > keys.length) {
			this.addTaskIssue("More objects than keys on", issues, taskIndex,
					taskName, workSourceClass);
			return false; // not valid
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link TaskObjectType} instances are valid given they
	 * are {@link Indexed}.
	 * 
	 * @param taskType
	 *            {@link TaskType}.
	 * @param taskIndex
	 *            Index of the {@link TaskType} on the {@link WorkType}.
	 * @param taskName
	 *            Name of the {@link TaskType}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return <code>true</code> if {@link TaskObjectType} instances are valid.
	 */
	private boolean isValidObjects(TaskType<?, ?, ?> taskType, int taskIndex,
			String taskName, Class<?> workSourceClass, CompilerIssues issues) {

		// Validate the task object types
		TaskObjectType<?>[] objectTypes = taskType.getObjectTypes();
		for (int i = 0; i < objectTypes.length; i++) {
			TaskObjectType<?> objectType = objectTypes[i];

			// Ensure no key on object
			Enum<?> key = objectType.getKey();
			if (key != null) {
				this.addTaskIssue(
						"Objects are not keyed but object has key on", issues,
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the index is correct
			if (objectType.getIndex() != i) {
				this.addTaskIssue("Object indexes are out of order on", issues,
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link TaskFlowType} instances are valid given an
	 * {@link Enum} providing the instigated {@link Flow} keys.
	 * 
	 * @param taskType
	 *            {@link TaskType}.
	 * @param objectKeysClass
	 *            {@link Enum} providing the keys.
	 * @param taskIndex
	 *            Index of the {@link TaskType} on the {@link WorkType}.
	 * @param taskName
	 *            Name of the {@link TaskType}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return <code>true</code> if {@link TaskFlowType} instances are valid.
	 */
	private <F extends Enum<F>> boolean isValidFlows(
			TaskType<?, ?, F> taskType, Class<F> flowKeysClass, int taskIndex,
			String taskName, Class<?> workSourceClass, CompilerIssues issues) {

		// Obtain the keys are sort by ordinal just to be sure
		F[] keys = flowKeysClass.getEnumConstants();
		Arrays.sort(keys, new Comparator<F>() {
			@Override
			public int compare(F a, F b) {
				return a.ordinal() - b.ordinal();
			}
		});

		// Validate the task flow types
		TaskFlowType<F>[] flowTypes = taskType.getFlowTypes();
		for (int i = 0; i < keys.length; i++) {
			F key = keys[i];
			TaskFlowType<F> flowType = (flowTypes.length > i ? flowTypes[i]
					: null);

			// Ensure flow type for the key
			if (flowType == null) {
				this.addTaskIssue("No " + TaskFlowType.class.getSimpleName()
						+ " provided for key " + key + " on", issues,
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure have key identifying the object
			F typeKey = flowType.getKey();
			if (typeKey == null) {
				this.addTaskIssue("No key provided for a flow on", issues,
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the key is of correct type
			if (!flowKeysClass.isInstance(typeKey)) {
				this.addTaskIssue("Incorrect key type ("
						+ typeKey.getClass().getName()
						+ ") provided for a flow on", issues, taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure is the correct expected key
			if (key != typeKey) {
				this.addIssue("Incorrect flow key (" + typeKey
						+ ") as was expecting " + key + " on", issues);
				return false; // not valid
			}

			// Ensure the index matches the ordinal for the key
			int index = flowType.getIndex();
			if (index != key.ordinal()) {
				this.addIssue("Index (" + index
						+ ") of flow does match ordinal of key (" + key
						+ ") on", issues);
				return false; // not valid
			}
		}

		// Ensure there are no addition flows than keys
		if (flowTypes.length > keys.length) {
			this.addTaskIssue("More flows than keys on", issues, taskIndex,
					taskName, workSourceClass);
			return false; // not valid
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link TaskFlowType} instances are valid given they
	 * are {@link Indexed}.
	 * 
	 * @param taskType
	 *            {@link TaskType}.
	 * @param taskIndex
	 *            Index of the {@link TaskType} on the {@link WorkType}.
	 * @param taskName
	 *            Name of the {@link TaskType}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @return <code>true</code> if {@link TaskFlowType} instances are valid.
	 */
	private boolean isValidFlows(TaskType<?, ?, ?> taskType, int taskIndex,
			String taskName, Class<?> workSourceClass, CompilerIssues issues) {

		// Validate the task flow types
		TaskFlowType<?>[] flowTypes = taskType.getFlowTypes();
		for (int i = 0; i < flowTypes.length; i++) {
			TaskFlowType<?> flowType = flowTypes[i];

			// Ensure no key on flow
			Enum<?> key = flowType.getKey();
			if (key != null) {
				this.addTaskIssue("Flows are not keyed but flow has key on",
						issues, taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the index is correct
			if (flowType.getIndex() != i) {
				this.addTaskIssue("Flow indexes are out of order on", issues,
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}
		}

		// If here then valid
		return true;
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 * @param taskIndex
	 *            Index of the {@link Task}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 */
	private void addTaskIssue(String issueDescription, CompilerIssues issues,
			int taskIndex, String taskName, Class<?> workSourceClass) {
		issues.addIssue(LocationType.DESK, this.deskLocation, AssetType.WORK,
				this.workName, issueDescription + " "
						+ TaskType.class.getSimpleName() + " definition "
						+ taskIndex
						+ (taskName == null ? "" : " (" + taskName + ")")
						+ " by " + WorkSource.class.getSimpleName() + " "
						+ workSourceClass.getName());
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, CompilerIssues issues) {
		issues.addIssue(LocationType.DESK, this.deskLocation, AssetType.WORK,
				this.workName, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	private void addIssue(String issueDescription, Throwable cause,
			CompilerIssues issues) {
		issues.addIssue(LocationType.DESK, this.deskLocation, AssetType.WORK,
				this.workName, issueDescription, cause);
	}

}