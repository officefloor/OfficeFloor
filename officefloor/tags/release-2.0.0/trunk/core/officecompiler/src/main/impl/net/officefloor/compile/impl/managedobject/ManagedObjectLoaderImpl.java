/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.compile.impl.managedobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagingOfficeBuilderImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.WorkConfiguration;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * {@link ManagedObjectLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectLoaderImpl implements ManagedObjectLoader {

	/**
	 * {@link LocationType}.
	 */
	private LocationType locationType;

	/**
	 * Location.
	 */
	private final String location;

	/**
	 * Name of the {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Initiate for building.
	 * 
	 * @param locationType
	 *            {@link LocationType}.
	 * @param location
	 *            Location.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public ManagedObjectLoaderImpl(LocationType locationType, String location,
			String managedObjectName, NodeContext nodeContext) {
		this.locationType = locationType;
		this.location = location;
		this.managedObjectName = managedObjectName;
		this.nodeContext = nodeContext;
	}

	/**
	 * Initiate from {@link OfficeFloorCompiler}.
	 * 
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public ManagedObjectLoaderImpl(NodeContext nodeContext) {
		this(null, null, null, nodeContext);
	}

	/*
	 * ===================== ManagedObjectLoader ==============================
	 */

	@Override
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> PropertyList loadSpecification(
			Class<MS> managedObjectSourceClass) {

		// Instantiate the managed object source
		ManagedObjectSource<D, H> managedObjectSource = CompileUtil
				.newInstance(managedObjectSourceClass,
						ManagedObjectSource.class, this.locationType,
						this.location, AssetType.MANAGED_OBJECT,
						this.managedObjectName,
						this.nodeContext.getCompilerIssues());
		if (managedObjectSource == null) {
			return null; // failed to instantiate
		}

		// Obtain the specification
		ManagedObjectSourceSpecification specification;
		try {
			specification = managedObjectSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ ManagedObjectSourceSpecification.class.getSimpleName()
					+ " from " + managedObjectSourceClass.getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No "
					+ ManagedObjectSourceSpecification.class.getSimpleName()
					+ " returned from " + managedObjectSourceClass.getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		ManagedObjectSourceProperty[] managedObjectSourceProperties;
		try {
			managedObjectSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain "
					+ ManagedObjectSourceProperty.class.getSimpleName()
					+ " instances from "
					+ ManagedObjectSourceSpecification.class.getSimpleName()
					+ " for " + managedObjectSourceClass.getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the managed object source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (managedObjectSourceProperties != null) {
			for (int i = 0; i < managedObjectSourceProperties.length; i++) {
				ManagedObjectSourceProperty mosProperty = managedObjectSourceProperties[i];

				// Ensure have the managed object source property
				if (mosProperty == null) {
					this.addIssue(ManagedObjectSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " is null from "
							+ ManagedObjectSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ managedObjectSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mosProperty.getName();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get name for "
									+ ManagedObjectSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " from "
									+ ManagedObjectSourceSpecification.class
											.getSimpleName() + " for "
									+ managedObjectSourceClass.getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(ManagedObjectSourceProperty.class
							.getSimpleName()
							+ " "
							+ i
							+ " provided blank name from "
							+ ManagedObjectSourceSpecification.class
									.getSimpleName()
							+ " for "
							+ managedObjectSourceClass.getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mosProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue(
							"Failed to get label for "
									+ ManagedObjectSourceProperty.class
											.getSimpleName()
									+ " "
									+ i
									+ " ("
									+ name
									+ ") from "
									+ ManagedObjectSourceSpecification.class
											.getSimpleName() + " for "
									+ managedObjectSourceClass.getName(), ex);
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
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectType<D> loadManagedObjectType(
			Class<MS> managedObjectSourceClass, PropertyList propertyList) {

		// Create an instance of the managed object source
		MS managedObjectSource = CompileUtil.newInstance(
				managedObjectSourceClass, ManagedObjectSource.class,
				this.locationType, this.location, AssetType.MANAGED_OBJECT,
				this.managedObjectName, this.nodeContext.getCompilerIssues());
		if (managedObjectSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the managed object type
		return this.loadManagedObjectType(managedObjectSource, propertyList);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>> ManagedObjectType<D> loadManagedObjectType(
			ManagedObjectSource<D, F> managedObjectSource,
			PropertyList propertyList) {

		// Create the managed object source context to initialise
		String officeName = null;
		ManagingOfficeConfiguration<F> managingOffice = new ManagingOfficeBuilderImpl<F>(
				officeName);
		OfficeConfiguration office = new OfficeBuilderImpl(officeName);
		String namespaceName = null; // stops the name spacing
		ManagedObjectSourceContext<F> sourceContext = new ManagedObjectSourceContextImpl<F>(
				namespaceName, new PropertyListSourceProperties(propertyList),
				this.nodeContext.getSourceContext(),
				managingOffice.getBuilder(), office.getBuilder());

		try {
			// Initialise the managed object source
			managedObjectSource.init(sourceContext);

		} catch (UnknownPropertyError ex) {
			this.addIssue("Missing property '" + ex.getUnknownPropertyName()
					+ "'");
			return null; // must have property

		} catch (UnknownClassError ex) {
			this.addIssue("Can not load class '" + ex.getUnknownClassName()
					+ "'");
			return null; // must have class

		} catch (UnknownResourceError ex) {
			this.addIssue("Can not obtain resource at location '"
					+ ex.getUnknownResourceLocation() + "'");
			return null; // must have resource

		} catch (Throwable ex) {
			this.addIssue("Failed to init", ex);
			return null; // must initialise
		}

		// Obtain the meta-data
		ManagedObjectSourceMetaData<D, F> metaData;
		try {
			metaData = managedObjectSource.getMetaData();
		} catch (Throwable ex) {
			this.addIssue(
					"Failed to get "
							+ ManagedObjectSourceMetaData.class.getSimpleName(),
					ex);
			return null; // must have meta-data
		}
		if (metaData == null) {
			this.addIssue("Returned null "
					+ ManagedObjectSourceMetaData.class.getSimpleName());
			return null; // must have meta-data
		}

		// Ensure handle any issue in interacting with meta-data
		Class<?> objectType;
		ManagedObjectDependencyType<D>[] dependencyTypes;
		ManagedObjectFlowType<F>[] flowTypes;
		Class<?>[] extensionInterfaces;
		try {

			// Obtain the object class
			objectType = metaData.getObjectClass();
			if (objectType == null) {
				this.addIssue("No Object type provided");
				return null; // must have object type
			}

			// Ensure Managed Object class defined and valid
			Class<?> managedObjectType = metaData.getManagedObjectClass();
			if (managedObjectType == null) {
				this.addIssue("No " + ManagedObject.class.getSimpleName()
						+ " type provided");
				return null; // must have managed object type
			}
			if (!ManagedObject.class.isAssignableFrom(managedObjectType)) {
				this.addIssue(ManagedObject.class.getSimpleName()
						+ " class must implement "
						+ ManagedObject.class.getName() + " (class="
						+ managedObjectType.getName() + ")");
				return null; // must have valid type
			}

			// Obtain the dependency types
			dependencyTypes = this.getManagedObjectDependencyTypes(metaData);
			if (dependencyTypes == null) {
				return null; // issue in getting dependency types
			}

			// Obtain the flow types
			flowTypes = this.getManagedObjectFlowTypes(metaData);
			if (flowTypes == null) {
				return null; // issue in getting flow types
			}

			// Obtain the supported extension interfaces
			extensionInterfaces = this.getExtensionInterfaces(metaData);
			if (extensionInterfaces == null) {
				return null; // issue in getting extension interfaces
			}

		} catch (Throwable ex) {
			this.addIssue("Exception from "
					+ managedObjectSource.getClass().getName(), ex);
			return null; // must be successful with meta-data
		}

		// Obtain the team types (ensuring work and tasks have names)
		ManagedObjectTeamType[] teamTypes = this
				.getTeamsEnsuringHaveWorkAndTaskNames(office);
		if (teamTypes == null) {
			return null; // issue getting team types
		}

		// Filter out linked processes already linked to tasks
		ManagedObjectFlowType<F>[] unlinkedFlowTypes = this
				.filterLinkedProcesses(flowTypes, managingOffice, office);
		if (unlinkedFlowTypes == null) {
			return null; // issue in filter meta-data flow types
		}

		// Obtain the flows instigated by the tasks
		ManagedObjectFlowType<?>[] taskFlows = this.getTaskFlows(office);
		if (taskFlows == null) {
			return null; // issue getting task flow types
		}

		// Create the combined listing of flows
		List<ManagedObjectFlowType<?>> moFlowTypes = new LinkedList<ManagedObjectFlowType<?>>();
		moFlowTypes.addAll(Arrays.asList(unlinkedFlowTypes));
		moFlowTypes.addAll(Arrays.asList(taskFlows));

		// Create and return the managed object type
		return new ManagedObjectTypeImpl<D>(objectType, dependencyTypes,
				moFlowTypes.toArray(new ManagedObjectFlowType[0]), teamTypes,
				extensionInterfaces);
	}

	@Override
	public boolean isInputManagedObject(ManagedObjectType<?> managedObjectType) {

		// Input if flows to link, as can share reference with other inputs
		if (managedObjectType.getFlowTypes().length > 0) {
			return true;
		}

		// Input if private tasks (indicated by team) and needs dependencies
		if ((managedObjectType.getTeamTypes().length > 0)
				&& (managedObjectType.getDependencyTypes().length > 0)) {
			return true;
		}

		// As here, not an input managed object
		return false;
	}

	/**
	 * Filters out any {@link ManagedObjectFlowType} instances of the
	 * {@link ManagedObjectSourceMetaData} that are linked to an added
	 * {@link Task}.
	 * 
	 * @param metaDataFlows
	 *            {@link ManagedObjectFlowType} instances defining linked
	 *            processes for the {@link ManagedObjectSourceMetaData}.
	 * @param managingOffice
	 *            {@link ManagingOfficeConfiguration}.
	 * @param office
	 *            {@link OfficeConfiguration}.
	 * @return Filtered {@link ManagedObjectFlowType}.
	 */
	private <F extends Enum<F>> ManagedObjectFlowType<F>[] filterLinkedProcesses(
			ManagedObjectFlowType<F>[] metaDataFlows,
			ManagingOfficeConfiguration<F> managingOffice,
			OfficeConfiguration office) {

		// Determine if required to do filtering
		if (metaDataFlows.length == 0) {
			return metaDataFlows; // no flows to filter
		}
		ManagedObjectFlowConfiguration<F>[] linkedFlows = managingOffice
				.getFlowConfiguration();
		if ((linkedFlows == null) || (linkedFlows.length == 0)) {
			return metaDataFlows; // no flows linked
		}

		// Required to filter, therefore determine if linking by keys/indexes
		F firstLinkKey = metaDataFlows[0].getKey();
		Class<?> keyClass = (firstLinkKey != null ? firstLinkKey.getClass()
				: null);

		// Create the filtered list of flows
		for (int i = 0; i < linkedFlows.length; i++) {
			ManagedObjectFlowConfiguration<F> linkedFlow = linkedFlows[i];

			// Ensure have linked flow configuration
			if (linkedFlow == null) {
				this.addIssue("Null "
						+ ManagedObjectFlowConfiguration.class.getSimpleName()
						+ " for flow index " + i);
				return null; // must have configuration
			}

			// Obtain the link details
			F key = linkedFlow.getFlowKey();
			int index = (key != null ? key.ordinal() : i);

			// Ensure correctly keyed or indexed
			if (key == null) {
				// Ensure should be indexed
				if (keyClass != null) {
					this.addIssue(ManagedObjectFlowMetaData.class
							.getSimpleName()
							+ " requires linking by keys (not indexes)");
					return null; // linked by index but keyed
				}

				// Ensure the index is in range
				if ((index < 0) || (index >= metaDataFlows.length)) {
					this.addIssue(ManagedObjectFlowMetaData.class
							.getSimpleName()
							+ " does not define index (index="
							+ index + ")");
					return null;
				}

			} else {
				// Ensure should be keyed
				if (keyClass == null) {
					this.addIssue(ManagedObjectFlowMetaData.class
							.getSimpleName()
							+ " requires linking by indexes (not keys)");
					return null; // linked by key but indexed
				}

				// Ensure a valid key class
				if (!keyClass.isInstance(key)) {
					this.addIssue("Link key does not match type for "
							+ ManagedObjectFlowMetaData.class.getSimpleName()
							+ " (meta-data key type=" + keyClass.getName()
							+ ", link key type=" + key.getClass().getName()
							+ ", link key=" + key + ")");
					return null; // invalid key
				}
			}

			// Obtain details of task being linked too
			String linkLabel = "linked process " + index + " (key="
					+ (key != null ? key.toString() : "<indexed>");
			TaskNodeReference link = linkedFlow.getTaskNodeReference();
			String linkWorkName = (link != null ? link.getWorkName() : null);
			String linkTaskName = (link != null ? link.getTaskName() : null);

			// Ensure have work name
			if (CompileUtil.isBlank(linkWorkName)) {
				this.addIssue("Must provide work name for " + linkLabel + ")");
				return null;
			}

			// Ensure have task name
			if (CompileUtil.isBlank(linkTaskName)) {
				this.addIssue("Must provide task name for " + linkLabel + ")");
				return null;
			}

			// Ensure the linked task was added
			if (!this.isTaskAdded(linkWorkName, linkTaskName, office)) {
				this.addIssue("Unknown task for " + linkLabel + ", link work="
						+ linkWorkName + ", link task=" + linkTaskName + ")");
				return null;
			}

			// Filter out meta-data flow by setting it to null in the array
			metaDataFlows[index] = null;
		}

		// Create the list of filtered flows
		List<ManagedObjectFlowType<F>> filteredFlows = new ArrayList<ManagedObjectFlowType<F>>(
				metaDataFlows.length);
		for (int i = 0; i < metaDataFlows.length; i++) {
			if (metaDataFlows[i] != null) {
				// Add meta-data flow as not filtered out
				filteredFlows.add(metaDataFlows[i]);
			}
		}

		// Return the filtered list of flows
		return CompileUtil.toArray(filteredFlows, new ManagedObjectFlowType[0]);
	}

	/**
	 * Obtains the {@link ManagedObjectFlowType} instigated from the added
	 * {@link Task} instances.
	 * 
	 * @param office
	 *            {@link OfficeConfiguration}.
	 * @return {@link ManagedObjectFlowType} instances.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ManagedObjectFlowType<?>[] getTaskFlows(OfficeConfiguration office) {

		// Obtain the flows instigated by the tasks
		List<ManagedObjectFlowType<?>> taskFlows = new LinkedList<ManagedObjectFlowType<?>>();
		for (WorkConfiguration<?> work : office.getWorkConfiguration()) {
			for (TaskConfiguration<?, ?, ?> task : work.getTaskConfiguration()) {
				TaskFlowConfiguration<?>[] flows = task.getFlowConfiguration();
				for (int i = 0; i < flows.length; i++) {
					TaskFlowConfiguration<?> flow = flows[i];

					// Obtain the flow details
					String workName = work.getWorkName();
					String taskName = task.getTaskName();
					String flowName = flow.getFlowName();
					String flowLabel = "work=" + workName + ", task="
							+ taskName + ", flow=" + flowName;

					// Ensure the flow has instigation strategy
					if (flow.getInstigationStrategy() == null) {
						this.addIssue("No instigation strategy for flow ("
								+ flowLabel + ")");
						return null; // must have instigation strategy
					}

					// Obtain linked task details
					TaskNodeReference link = flow.getInitialTask();
					String linkWorkName = (link == null ? null : link
							.getWorkName());
					String linkTaskName = (link == null ? null : link
							.getTaskName());
					Class<?> argumentType = (link == null ? null : link
							.getArgumentType());

					// Determine if linked or required flow
					if (CompileUtil.isBlank(linkTaskName)) {
						// Ensure no work as required flow
						if (!CompileUtil.isBlank(linkWorkName)) {
							this.addIssue("No task name for flow (" + flowLabel
									+ ", link work=" + linkWorkName + ")");
							return null; // no task name then no work name
						}

						// Obtain the flow key and index
						Enum<?> flowKey = flow.getKey();
						int flowIndex = (flowKey == null ? i : flowKey
								.ordinal());

						// Create and add the required flow
						taskFlows.add(new ManagedObjectFlowTypeImpl(workName,
								taskName, flowIndex, argumentType, flowKey,
								flowName));

					} else {
						// Linking to a task and determine if same work
						if (CompileUtil.isBlank(linkWorkName)) {
							// Same work
							linkWorkName = workName;
						}

						// Ensure the task is added
						if (!this.isTaskAdded(linkWorkName, linkTaskName,
								office)) {
							this.addIssue("Unknown task being linked ("
									+ flowLabel + ", link work=" + linkWorkName
									+ ", link task=" + linkTaskName + ")");
							return null; // must link to added task
						}
					}
				}
			}
		}

		// Return the required flows by tasks
		return taskFlows.toArray(new ManagedObjectFlowType[0]);
	}

	/**
	 * Obtains the {@link ManagedObjectTeamType} instances ensuring all added
	 * {@link Work} and {@link Task} instances have names.
	 * 
	 * @param office
	 *            {@link OfficeConfiguration}.
	 * @return {@link ManagedObjectTeamType} instances.
	 */
	private ManagedObjectTeamType[] getTeamsEnsuringHaveWorkAndTaskNames(
			OfficeConfiguration office) {

		// Ensure all added tasks are valid (and collect the set of team names)
		Set<String> teamNames = new HashSet<String>();
		for (WorkConfiguration<?> work : office.getWorkConfiguration()) {

			// Ensure have work name
			String workName = work.getWorkName();
			if (CompileUtil.isBlank(workName)) {
				this.addIssue("Work added without a name");
				return null; // must have work name
			}

			// Ensure have tasks for the work
			TaskConfiguration<?, ?, ?>[] tasks = work.getTaskConfiguration();
			if (tasks.length == 0) {
				this.addIssue("No tasks added for work (work=" + workName + ")");
				return null; // must have at least one task
			}

			// Ensure the tasks are valid
			for (TaskConfiguration<?, ?, ?> task : tasks) {

				// Ensure have task name
				String taskName = task.getTaskName();
				if (CompileUtil.isBlank(taskName)) {
					this.addIssue("Task added without a name (work=" + workName
							+ ")");
					return null; // must have task name
				}

				// Ensure have the team name
				String teamName = task.getOfficeTeamName();
				if (CompileUtil.isBlank(teamName)) {
					this.addIssue("Must specify team for task (work="
							+ workName + ", task=" + taskName + ")");
					return null; // must have team name
				}

				// Register the team
				teamNames.add(teamName);
			}
		}

		// Create the listing of teams sorted by name
		String[] sortedTeamNames = teamNames.toArray(new String[0]);
		Arrays.sort(sortedTeamNames);
		ManagedObjectTeamType[] teamTypes = new ManagedObjectTeamType[sortedTeamNames.length];
		for (int i = 0; i < teamTypes.length; i++) {
			teamTypes[i] = new ManagedObjectTeamTypeImpl(sortedTeamNames[i]);
		}

		// Return the teams
		return teamTypes;
	}

	/**
	 * Indicates if the {@link Task} was added to the {@link Office}.
	 * 
	 * @param workName
	 *            {@link Work} name.
	 * @param taskName
	 *            {@link Task} name.
	 * @param office
	 *            {@link OfficeConfiguration}.
	 * @return <code>true</code> if {@link Task} added to the {@link Office}.
	 */
	private boolean isTaskAdded(String workName, String taskName,
			OfficeConfiguration office) {

		// Determine if the task is added to the office
		for (WorkConfiguration<?> work : office.getWorkConfiguration()) {
			if (workName.equals(work.getWorkName())) {
				for (TaskConfiguration<?, ?, ?> task : work
						.getTaskConfiguration()) {
					if (taskName.equals(task.getTaskName())) {
						// Task added to office
						return true;
					}
				}
			}
		}

		// If at this point, task not added to the office
		return false;
	}

	/**
	 * Obtains the {@link ManagedObjectDependencyType} instances from the
	 * {@link ManagedObjectSourceMetaData}.
	 * 
	 * @param metaData
	 *            {@link ManagedObjectSourceMetaData}.
	 * @return {@link ManagedObjectDependencyType} instances.
	 */
	@SuppressWarnings("unchecked")
	private <D extends Enum<D>> ManagedObjectDependencyType<D>[] getManagedObjectDependencyTypes(
			ManagedObjectSourceMetaData<D, ?> metaData) {

		// Obtain the dependency meta-data
		ManagedObjectDependencyType<D>[] dependencyTypes;
		Class<?> dependencyKeys = null;
		ManagedObjectDependencyMetaData<D>[] dependencyMetaDatas = metaData
				.getDependencyMetaData();
		if (dependencyMetaDatas == null) {
			// No dependencies
			dependencyTypes = new ManagedObjectDependencyType[0];

		} else {
			// Load the dependencies
			dependencyTypes = new ManagedObjectDependencyType[dependencyMetaDatas.length];
			for (int i = 0; i < dependencyTypes.length; i++) {
				ManagedObjectDependencyMetaData<D> dependencyMetaData = dependencyMetaDatas[i];

				// Ensure have dependency meta-data
				if (dependencyMetaData == null) {
					this.addIssue("Null "
							+ ManagedObjectDependencyMetaData.class
									.getSimpleName() + " for dependency " + i);
					return null; // missing met-data
				}

				// Obtain details for dependency
				String label = dependencyMetaData.getLabel();
				D key = dependencyMetaData.getKey();
				String dependencyLabel = "dependency " + i + " (key="
						+ (key == null ? "<indexed>" : key.toString())
						+ ", label="
						+ (CompileUtil.isBlank(label) ? "<no label>" : label)
						+ ")";

				// Determine if the first dependency
				if (i == 0) {
					// First dependency, so load details
					dependencyKeys = (key == null ? null : key.getClass());
				} else {
					// Another dependency that must adhere to previous
					boolean isIndexKeyMix;
					if (dependencyKeys == null) {
						// Dependencies expected to be indexed
						isIndexKeyMix = (key != null);

					} else {
						// Dependencies expected to be keyed
						isIndexKeyMix = (key == null);
						if (!isIndexKeyMix) {
							// Ensure the key is valid
							if (!dependencyKeys.isInstance(key)) {
								this.addIssue("Dependencies identified by different key types ("
										+ dependencyKeys.getName()
										+ ", "
										+ key.getClass().getName() + ")");
								return null; // mismatched keys
							}
						}
					}
					if (isIndexKeyMix) {
						this.addIssue("Dependencies mixing keys and indexes");
						return null; // can not mix indexing/keying
					}
				}

				// Obtain the type required for the dependency
				Class<?> type = dependencyMetaData.getType();
				if (type == null) {
					this.addIssue("No type for " + dependencyLabel);
					return null; // must have type
				}

				// Obtain the type qualifier
				String typeQualifier = dependencyMetaData.getTypeQualifier();

				// Determine the index for the dependency
				int index = (key != null ? key.ordinal() : i);

				// Create and add the dependency type
				dependencyTypes[i] = new ManagedObjectDependencyTypeImpl<D>(
						index, type, typeQualifier, key, label);
			}
		}

		// Validate have all the dependencies
		if (dependencyKeys == null) {
			// Determine if indexed or no dependencies
			dependencyKeys = (dependencyTypes.length == 0 ? None.class
					: Indexed.class);
		} else {
			// Ensure exactly one dependency per key
			Set<?> keys = new HashSet<Object>(Arrays.asList(dependencyKeys
					.getEnumConstants()));
			for (ManagedObjectDependencyType<D> dependencyType : dependencyTypes) {
				D key = dependencyType.getKey();
				if (!keys.contains(key)) {
					this.addIssue("Must have exactly one dependency per key (key="
							+ key + ")");
					return null; // must be one dependency per key
				}
				keys.remove(key);
			}
			if (keys.size() > 0) {
				StringBuilder msg = new StringBuilder();
				boolean isFirst = true;
				for (Object key : keys) {
					if (!isFirst) {
						msg.append(", ");
					}
					isFirst = false;
					msg.append(key.toString());
				}
				this.addIssue("Missing dependency meta-data (keys="
						+ msg.toString() + ")");
				return null; // must have meta-data for each key
			}
		}

		// Ensure the dependency types are in index order
		Arrays.sort(dependencyTypes,
				new Comparator<ManagedObjectDependencyType<D>>() {
					@Override
					public int compare(ManagedObjectDependencyType<D> a,
							ManagedObjectDependencyType<D> b) {
						return a.getIndex() - b.getIndex();
					}
				});

		// Return the dependency types
		return dependencyTypes;
	}

	/**
	 * Obtains the {@link ManagedObjectFlowType} instances from the
	 * {@link ManagedObjectSourceMetaData}.
	 * 
	 * @param metaData
	 *            {@link ManagedObjectSourceMetaData}.
	 * @return {@link ManagedObjectFlowType} instances.
	 */
	@SuppressWarnings("unchecked")
	private <F extends Enum<F>> ManagedObjectFlowType<F>[] getManagedObjectFlowTypes(
			ManagedObjectSourceMetaData<?, F> metaData) {

		// Obtain the flow meta-data
		ManagedObjectFlowType<F>[] flowTypes;
		Class<?> flowKeys = null;
		ManagedObjectFlowMetaData<F>[] flowMetaDatas = metaData
				.getFlowMetaData();
		if (flowMetaDatas == null) {
			// No dependencies
			flowTypes = new ManagedObjectFlowType[0];

		} else {
			// Load the dependencies
			flowTypes = new ManagedObjectFlowType[flowMetaDatas.length];
			for (int i = 0; i < flowTypes.length; i++) {
				ManagedObjectFlowMetaData<F> flowMetaData = flowMetaDatas[i];

				// Ensure have flow meta-data
				if (flowMetaData == null) {
					this.addIssue("Null "
							+ ManagedObjectFlowMetaData.class.getSimpleName()
							+ " for flow " + i);
					return null; // missing met-data
				}

				// Obtain details for flow
				String label = flowMetaData.getLabel();
				F key = flowMetaData.getKey();

				// Determine if the first flow
				if (i == 0) {
					// First flow, so load details
					flowKeys = (key == null ? null : key.getClass());
				} else {
					// Another flow that must adhere to previous
					boolean isIndexKeyMix;
					if (flowKeys == null) {
						// Dependencies expected to be indexed
						isIndexKeyMix = (key != null);

					} else {
						// Dependencies expected to be keyed
						isIndexKeyMix = (key == null);
						if (!isIndexKeyMix) {
							// Ensure the key is valid
							if (!flowKeys.isInstance(key)) {
								this.addIssue("Meta-data flows identified by different key types ("
										+ flowKeys.getName()
										+ ", "
										+ key.getClass().getName() + ")");
								return null; // mismatched keys
							}
						}
					}
					if (isIndexKeyMix) {
						this.addIssue("Meta-data flows mixing keys and indexes");
						return null; // can not mix indexing/keying
					}
				}

				// Obtain the argument type to the flow
				// (may be null for no argument)
				Class<?> type = flowMetaData.getArgumentType();

				// Determine the index for the flow
				int index = (key != null ? key.ordinal() : i);

				// Create and add the flow type
				flowTypes[i] = new ManagedObjectFlowTypeImpl<F>(index, type,
						key, label);
			}
		}

		// Validate have all the dependencies
		if (flowKeys == null) {
			// Determine if indexed or no dependencies
			flowKeys = (flowTypes.length == 0 ? None.class : Indexed.class);
		} else {
			// Ensure exactly one flow per key
			Set<?> keys = new HashSet<Object>(Arrays.asList(flowKeys
					.getEnumConstants()));
			for (ManagedObjectFlowType<F> flowType : flowTypes) {
				F key = flowType.getKey();
				if (!keys.contains(key)) {
					this.addIssue("Must have exactly one flow per key (key="
							+ key + ")");
					return null; // must be one flow per key
				}
				keys.remove(key);
			}
			if (keys.size() > 0) {
				StringBuilder msg = new StringBuilder();
				boolean isFirst = true;
				for (Object key : keys) {
					if (!isFirst) {
						msg.append(", ");
					}
					isFirst = false;
					msg.append(key.toString());
				}
				this.addIssue("Missing flow meta-data (keys=" + msg.toString()
						+ ")");
				return null; // must have meta-data for each key
			}
		}

		// Ensure the flow types are in index order
		Arrays.sort(flowTypes, new Comparator<ManagedObjectFlowType<F>>() {
			@Override
			public int compare(ManagedObjectFlowType<F> a,
					ManagedObjectFlowType<F> b) {
				return a.getIndex() - b.getIndex();
			}
		});

		// Return the flow types
		return flowTypes;
	}

	/**
	 * Obtains the extension interfaces supported by the {@link ManagedObject}.
	 * 
	 * @param metaData
	 *            {@link ManagedObjectSourceMetaData}.
	 * @return Extension interfaces.
	 */
	private Class<?>[] getExtensionInterfaces(
			ManagedObjectSourceMetaData<?, ?> metaData) {

		// Obtain the extension interface meta-data
		Class<?>[] extensionInterfaces;
		ManagedObjectExtensionInterfaceMetaData<?>[] eiMetaDatas = metaData
				.getExtensionInterfacesMetaData();
		if (eiMetaDatas == null) {
			// No extension interfaces supported
			extensionInterfaces = new Class[0];

		} else {
			// Obtain the extension interfaces supported
			extensionInterfaces = new Class[eiMetaDatas.length];
			for (int i = 0; i < extensionInterfaces.length; i++) {
				ManagedObjectExtensionInterfaceMetaData<?> eiMetaData = eiMetaDatas[i];

				// Ensure have the interface meta-data
				if (eiMetaData == null) {
					this.addIssue("Null extension interface meta-data");
					return null; // must have meta-data
				}

				// Obtain the extension interface type
				Class<?> eiType = eiMetaData.getExtensionInterfaceType();
				if (eiType == null) {
					this.addIssue("Null extension interface type");
					return null; // must have type
				}

				// Ensure an extension factory
				if (eiMetaData.getExtensionInterfaceFactory() == null) {
					this.addIssue("No extension interface factory (type="
							+ eiType.getName() + ")");
					return null; // must have factory
				}

				// Load the extension interface
				extensionInterfaces[i] = eiType;
			}

		}

		// Return the supported extension interfaces
		return extensionInterfaces;
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.locationType,
				this.location, AssetType.MANAGED_OBJECT,
				this.managedObjectName, issueDescription);
	}

	/**
	 * Adds an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 * @param cause
	 *            Cause of the issue.
	 */
	private void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.locationType,
				this.location, AssetType.MANAGED_OBJECT,
				this.managedObjectName, issueDescription, cause);
	}

}