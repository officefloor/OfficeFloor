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
package net.officefloor.compile.impl.work;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkSourceProperty;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.compile.work.TaskFlowType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.compile.work.TaskType;
import net.officefloor.compile.work.WorkLoader;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link WorkLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkLoaderImpl implements WorkLoader {

	/**
	 * {@link Node} requiring the {@link Work}.
	 */
	private final Node node;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate for building.
	 * 
	 * @param node
	 *            {@link Node} requiring the {@link Work}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public WorkLoaderImpl(Node node, NodeContext nodeContext) {
		this.node = node;
		this.nodeContext = nodeContext;
	}

	/*
	 * ====================== WorkLoader ====================================
	 */

	@Override
	public <W extends Work, WS extends WorkSource<W>> PropertyList loadSpecification(
			Class<WS> workSourceClass) {

		// Instantiate the work source
		WorkSource<W> workSource = CompileUtil.newInstance(workSourceClass,
				WorkSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (workSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		WorkSourceSpecification specification;
		try {
			specification = workSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ WorkSourceSpecification.class.getSimpleName()
							+ " from " + workSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + WorkSourceSpecification.class.getSimpleName()
					+ " returned from " + workSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		WorkSourceProperty[] workProperties;
		try {
			workProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to obtain "
							+ WorkSourceProperty.class.getSimpleName()
							+ " instances from "
							+ WorkSourceSpecification.class.getSimpleName()
							+ " for " + workSourceClass.getName(), ex);
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
							+ " for " + workSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = workProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ WorkSourceProperty.class.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ WorkSourceSpecification.class
											.getSimpleName() + " for "
									+ workSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(WorkSourceProperty.class.getSimpleName()
							+ " " + i + " provided blank name from "
							+ WorkSourceSpecification.class.getSimpleName()
							+ " for " + workSourceClass.getName());
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
							+ " for " + workSourceClass.getName(), ex);
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
	public <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWorkType(
			Class<WS> workSourceClass, PropertyList propertyList) {

		// Instantiate the work source
		WorkSource<W> workSource = CompileUtil.newInstance(workSourceClass,
				WorkSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (workSource == null) {
			return null; // failed to instantiate
		}

		// Create the work source context
		WorkSourceContext context = new WorkSourceContextImpl(true,
				propertyList, this.nodeContext);

		// Create the work type builder
		WorkTypeImpl<W> workType = new WorkTypeImpl<W>();

		try {
			// Source the work type
			workSource.sourceWork(workType, context);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "' for " + WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName());
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "' for " + WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName());
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '"
					+ ex.getUnknownResourceLocation() + "' for "
					+ WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName());
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + WorkType.class.getSimpleName()
					+ " definition from " + WorkSource.class.getSimpleName()
					+ " " + workSourceClass.getName(), ex);
			return null; // must be successful
		}

		// Ensure has a work factory
		if (workType.getWorkFactory() == null) {
			this.addIssue("No " + WorkFactory.class.getSimpleName()
					+ " provided by " + WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName());
			return null; // must have complete type
		}

		// Ensure has at least one task
		TaskType<W, ?, ?>[] taskTypes = workType.getTaskTypes();
		if (taskTypes.length == 0) {
			this.addIssue("No " + TaskType.class.getSimpleName()
					+ " definitions provided by "
					+ WorkSource.class.getSimpleName() + " "
					+ workSourceClass.getName());
			return null; // must have complete type
		}

		// Determine if duplicate task names
		if (this.isDuplicateNaming(
				taskTypes,
				(taskType) -> taskType.getTaskName(),
				"Two or more "
						+ TaskType.class.getSimpleName()
						+ " definitions with the same name (${NAME}) provided by "
						+ WorkSource.class.getSimpleName() + " "
						+ workSourceClass.getName())) {
			return null; // must have valid type
		}

		// Ensure the task definitions are valid
		for (int i = 0; i < taskTypes.length; i++) {
			if (!this.isValidTaskType(taskTypes[i], i, workSourceClass)) {
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
	 * @return <code>true</code> if {@link TaskType} is valid.
	 */
	private <W extends Work, M extends Enum<M>, F extends Enum<F>> boolean isValidTaskType(
			TaskType<W, M, F> taskType, int taskIndex, Class<?> workSourceClass) {

		// Ensure has name
		String taskName = taskType.getTaskName();
		if (CompileUtil.isBlank(taskName)) {
			this.addTaskIssue("No task name provided for", taskIndex, taskName,
					workSourceClass);
			return false; // must have complete type
		}

		// Ensure has task factory
		if (taskType.getTaskFactory() == null) {
			this.addTaskIssue("No " + TaskFactory.class.getSimpleName()
					+ " provided for", taskIndex, taskName, workSourceClass);
			return false; // must have complete type
		}

		// Obtain the object keys class (taking into account Indexed)
		Class<M> objectKeysClass = taskType.getObjectKeyClass();
		if ((objectKeysClass != null)
				&& (!objectKeysClass.equals(Indexed.class))) {
			// Is valid by keys
			if (!this.isValidObjects(taskType, objectKeysClass, taskIndex,
					taskName, workSourceClass)) {
				return false; // must be valid
			}
		} else {
			// Is valid by indexes
			if (!this.isValidObjects(taskType, taskIndex, taskName,
					workSourceClass)) {
				return false; // must be valid
			}
		}

		// Validate common details for objects
		TaskObjectType<M>[] objectTypes = taskType.getObjectTypes();
		for (TaskObjectType<M> objectType : objectTypes) {

			// Must have names for objects
			String objectName = objectType.getObjectName();
			if (CompileUtil.isBlank(objectName)) {
				this.addTaskIssue("No object name on", taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}

			// Must have object types
			if (objectType.getObjectType() == null) {
				this.addTaskIssue("No object type provided for object "
						+ objectName + " on", taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}
		}

		// Validate no duplicate object names
		if (this.isDuplicateNaming(objectTypes, (item) -> item.getObjectName(),
				this.getTaskIssueDescription("Two or more "
						+ TaskObjectType.class.getSimpleName()
						+ " definitions with the same name (${NAME}) for",
						taskIndex, taskName, workSourceClass))) {
			return false; // must have valid type
		}

		// Obtain the flow keys class (taking into account Indexed)
		Class<F> flowKeysClass = taskType.getFlowKeyClass();
		if ((flowKeysClass != null) && (!flowKeysClass.equals(Indexed.class))) {
			// Is valid by keys
			if (!this.isValidFlows(taskType, flowKeysClass, taskIndex,
					taskName, workSourceClass)) {
				return false; // must be valid
			}
		} else {
			// Is valid by indexes
			if (!this.isValidFlows(taskType, taskIndex, taskName,
					workSourceClass)) {
				return false; // must be valid
			}
		}

		// Validate common details for flows
		TaskFlowType<F>[] flowTypes = taskType.getFlowTypes();
		for (TaskFlowType<F> flowType : flowTypes) {

			// Must have names for flows
			if (CompileUtil.isBlank(flowType.getFlowName())) {
				this.addTaskIssue("No flow name on", taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}
		}

		// Validate no duplicate flow names
		if (this.isDuplicateNaming(flowTypes,
				new NameExtractor<TaskFlowType<F>>() {
					@Override
					public String extractName(TaskFlowType<F> item) {
						return item.getFlowName();
					}
				}, this.getTaskIssueDescription("Two or more "
						+ TaskFlowType.class.getSimpleName()
						+ " definitions with the same name (${NAME}) for",
						taskIndex, taskName, workSourceClass))) {
			return false; // must have valid type
		}

		// Validate the escalations
		TaskEscalationType[] escalationTypes = taskType.getEscalationTypes();
		for (TaskEscalationType escalationType : escalationTypes) {

			// Must have escalation type
			if (escalationType.getEscalationType() == null) {
				this.addTaskIssue("No escalation type on", taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}

			// Must have names for escalations
			if (CompileUtil.isBlank(escalationType.getEscalationName())) {
				this.addTaskIssue("No escalation name on", taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}
		}

		// Validate no duplicate escalation names
		if (this.isDuplicateNaming(escalationTypes,
				new NameExtractor<TaskEscalationType>() {
					@Override
					public String extractName(TaskEscalationType item) {
						return item.getEscalationName();
					}
				}, this.getTaskIssueDescription("Two or more "
						+ TaskEscalationType.class.getSimpleName()
						+ " definitions with the same name (${NAME}) for",
						taskIndex, taskName, workSourceClass))) {
			return false; // must have valid type
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
	 * @return <code>true</code> if {@link TaskObjectType} instances are valid.
	 */
	private <M extends Enum<M>> boolean isValidObjects(
			TaskType<?, M, ?> taskType, Class<M> objectKeysClass,
			int taskIndex, String taskName, Class<?> workSourceClass) {

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
						+ " provided for key " + key + " on", taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure have key identifying the object
			M typeKey = objectType.getKey();
			if (typeKey == null) {
				this.addTaskIssue("No key provided for an object on",
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the key is of correct type
			if (!objectKeysClass.isInstance(typeKey)) {
				this.addTaskIssue("Incorrect key type ("
						+ typeKey.getClass().getName()
						+ ") provided for an object on", taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}

			// Ensure is the correct expected key
			if (key != typeKey) {
				this.addIssue("Incorrect object key (" + typeKey
						+ ") as was expecting " + key + " on");
				return false; // not valid
			}

			// Ensure the index matches the ordinal for the key
			int index = objectType.getIndex();
			if (index != key.ordinal()) {
				this.addIssue("Index (" + index
						+ ") of object does match ordinal of key (" + key
						+ ") on");
				return false; // not valid
			}
		}

		// Ensure there are no addition objects than keys
		if (objectTypes.length > keys.length) {
			this.addTaskIssue("More objects than keys on", taskIndex, taskName,
					workSourceClass);
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
	 * @return <code>true</code> if {@link TaskObjectType} instances are valid.
	 */
	private boolean isValidObjects(TaskType<?, ?, ?> taskType, int taskIndex,
			String taskName, Class<?> workSourceClass) {

		// Validate the task object types
		TaskObjectType<?>[] objectTypes = taskType.getObjectTypes();
		for (int i = 0; i < objectTypes.length; i++) {
			TaskObjectType<?> objectType = objectTypes[i];

			// Ensure no key on object
			Enum<?> key = objectType.getKey();
			if (key != null) {
				this.addTaskIssue(
						"Objects are not keyed but object has key on",
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the index is correct
			if (objectType.getIndex() != i) {
				this.addTaskIssue("Object indexes are out of order on",
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}
		}

		// If here then valid
		return true;
	}

	/**
	 * Determines that the {@link TaskFlowType} instances are valid given an
	 * {@link Enum} providing the instigated {@link JobSequence} keys.
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
	 * @return <code>true</code> if {@link TaskFlowType} instances are valid.
	 */
	private <F extends Enum<F>> boolean isValidFlows(
			TaskType<?, ?, F> taskType, Class<F> flowKeysClass, int taskIndex,
			String taskName, Class<?> workSourceClass) {

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
						+ " provided for key " + key + " on", taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure have key identifying the object
			F typeKey = flowType.getKey();
			if (typeKey == null) {
				this.addTaskIssue("No key provided for a flow on", taskIndex,
						taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the key is of correct type
			if (!flowKeysClass.isInstance(typeKey)) {
				this.addTaskIssue("Incorrect key type ("
						+ typeKey.getClass().getName()
						+ ") provided for a flow on", taskIndex, taskName,
						workSourceClass);
				return false; // not valid
			}

			// Ensure is the correct expected key
			if (key != typeKey) {
				this.addIssue("Incorrect flow key (" + typeKey
						+ ") as was expecting " + key + " on");
				return false; // not valid
			}

			// Ensure the index matches the ordinal for the key
			int index = flowType.getIndex();
			if (index != key.ordinal()) {
				this.addIssue("Index (" + index
						+ ") of flow does match ordinal of key (" + key
						+ ") on");
				return false; // not valid
			}
		}

		// Ensure there are no addition flows than keys
		if (flowTypes.length > keys.length) {
			this.addTaskIssue("More flows than keys on", taskIndex, taskName,
					workSourceClass);
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
	 * @return <code>true</code> if {@link TaskFlowType} instances are valid.
	 */
	private boolean isValidFlows(TaskType<?, ?, ?> taskType, int taskIndex,
			String taskName, Class<?> workSourceClass) {

		// Validate the task flow types
		TaskFlowType<?>[] flowTypes = taskType.getFlowTypes();
		for (int i = 0; i < flowTypes.length; i++) {
			TaskFlowType<?> flowType = flowTypes[i];

			// Ensure no key on flow
			Enum<?> key = flowType.getKey();
			if (key != null) {
				this.addTaskIssue("Flows are not keyed but flow has key on",
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}

			// Ensure the index is correct
			if (flowType.getIndex() != i) {
				this.addTaskIssue("Flow indexes are out of order on",
						taskIndex, taskName, workSourceClass);
				return false; // not valid
			}
		}

		// If here then valid
		return true;
	}

	/**
	 * <p>
	 * Determines if there are duplicate item names.
	 * <p>
	 * Duplicate names are reported as issues.
	 * 
	 * @param items
	 *            Items to be checked for unique naming.
	 * @param extractor
	 *            {@link NameExtractor}.
	 * @param issueDescription
	 *            {@link CompilerIssues} description if duplicate name
	 * @return <code>true</code> if there are duplicate item names.
	 */
	private <N> boolean isDuplicateNaming(N[] items,
			NameExtractor<N> extractor, String issueDescription) {

		// Determine if duplicate name
		boolean isDuplicateName = false;
		Set<String> checkedNames = new HashSet<String>();
		for (N item : items) {
			String name = extractor.extractName(item);
			if (name != null) {

				// Ignore if name already checked (stops repetitive issues)
				if (checkedNames.contains(name)) {
					continue; // already checked
				}

				// Determine if name occurs more than once
				int nameCount = 0;
				for (N check : items) {
					String checkName = extractor.extractName(check);
					if (name.equals(checkName)) {
						nameCount++;
					}
				}
				if (nameCount > 1) {

					// More than one item with name so duplicate name
					isDuplicateName = true;

					// Duplicate name so prepare message
					String reportMessage = issueDescription.replace("${NAME}",
							name);

					// Report the issue
					this.addIssue(reportMessage);
				}

				// Name checked
				checkedNames.add(name);
			}
		}

		// Return whether duplicate names
		return isDuplicateName;
	}

	/**
	 * Extracts the name from the object.
	 */
	private static interface NameExtractor<N> {

		/**
		 * Extracts the particular name from the item.
		 * 
		 * @param item
		 *            Item to have name extracted.
		 * @return
		 */
		String extractName(N item);
	}

	/**
	 * Obtains the {@link Task} issue description.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param taskIndex
	 *            Index of the {@link Task}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @return {@link Task} issue description.
	 */
	private String getTaskIssueDescription(String issueDescription,
			int taskIndex, String taskName, Class<?> workSourceClass) {
		return issueDescription + " " + TaskType.class.getSimpleName()
				+ " definition " + taskIndex
				+ (taskName == null ? "" : " (" + taskName + ")") + " by "
				+ WorkSource.class.getSimpleName() + " "
				+ workSourceClass.getName();
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param taskIndex
	 *            Index of the {@link Task}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 */
	private void addTaskIssue(String issueDescription, int taskIndex,
			String taskName, Class<?> workSourceClass) {
		this.nodeContext.getCompilerIssues().addIssue(
				this.node,
				this.getTaskIssueDescription(issueDescription, taskIndex,
						taskName, workSourceClass));
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node,
				issueDescription, cause);
	}

}