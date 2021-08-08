/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.managedobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.structure.PropertyNode;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.IssueTarget;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectSourceContextImpl.ManagedObjectFunctionDependencyImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagingOfficeBuilderImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ManagedObjectLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectLoaderImpl implements ManagedObjectLoader, IssueTarget {

	/**
	 * {@link Node} requiring the {@link ManagedObject}.
	 */
	private final Node node;

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext nodeContext;

	/**
	 * Instantiate.
	 * 
	 * @param node        {@link Node} requiring the {@link ManagedObject}.
	 * @param officeNode  {@link OfficeNode}. May be <code>null</code> if not
	 *                    loading within {@link OfficeNode}.
	 * @param nodeContext {@link NodeContext}.
	 */
	public ManagedObjectLoaderImpl(Node node, OfficeNode officeNode, NodeContext nodeContext) {
		this.node = node;
		this.officeNode = officeNode;
		this.nodeContext = nodeContext;
	}

	/*
	 * ===================== ManagedObjectLoader ==============================
	 */

	@Override
	public <D extends Enum<D>, H extends Enum<H>, MS extends ManagedObjectSource<D, H>> PropertyList loadSpecification(
			Class<MS> managedObjectSourceClass) {

		// Instantiate the managed object source
		ManagedObjectSource<D, H> managedObjectSource = CompileUtil.newInstance(managedObjectSourceClass,
				ManagedObjectSource.class, this.node, this.nodeContext.getCompilerIssues());
		if (managedObjectSource == null) {
			return null; // failed to instantiate
		}

		// Return the specification
		return this.loadSpecification(managedObjectSource);
	}

	/**
	 * Loads the {@link PropertyList} specification.
	 * 
	 * @param <D>                 Dependency key.
	 * @param <H>                 Flow key.
	 * @param managedObjectSource {@link ManagedObjectSource}.
	 * @return {@link PropertyList} specification or <code>null</code> if issue.
	 */
	@Override
	public <D extends Enum<D>, H extends Enum<H>> PropertyList loadSpecification(
			ManagedObjectSource<D, H> managedObjectSource) {

		// Obtain the specification
		ManagedObjectSourceSpecification specification;
		try {
			specification = managedObjectSource.getSpecification();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ManagedObjectSourceSpecification.class.getSimpleName() + " from "
					+ managedObjectSource.getClass().getName(), ex);
			return null; // failed to obtain
		}

		// Ensure have specification
		if (specification == null) {
			this.addIssue("No " + ManagedObjectSourceSpecification.class.getSimpleName() + " returned from "
					+ managedObjectSource.getClass().getName());
			return null; // no specification obtained
		}

		// Obtain the properties
		ManagedObjectSourceProperty[] managedObjectSourceProperties;
		try {
			managedObjectSourceProperties = specification.getProperties();
		} catch (Throwable ex) {
			this.addIssue("Failed to obtain " + ManagedObjectSourceProperty.class.getSimpleName() + " instances from "
					+ ManagedObjectSourceSpecification.class.getSimpleName() + " for "
					+ managedObjectSource.getClass().getName(), ex);
			return null; // failed to obtain properties
		}

		// Load the managed object source properties into a property list
		PropertyList propertyList = new PropertyListImpl();
		if (managedObjectSourceProperties != null) {
			for (int i = 0; i < managedObjectSourceProperties.length; i++) {
				ManagedObjectSourceProperty mosProperty = managedObjectSourceProperties[i];

				// Ensure have the managed object source property
				if (mosProperty == null) {
					this.addIssue(ManagedObjectSourceProperty.class.getSimpleName() + " " + i + " is null from "
							+ ManagedObjectSourceSpecification.class.getSimpleName() + " for "
							+ managedObjectSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property name
				String name;
				try {
					name = mosProperty.getName();
				} catch (Throwable ex) {
					this.addIssue("Failed to get name for " + ManagedObjectSourceProperty.class.getSimpleName() + " "
							+ i + " from " + ManagedObjectSourceSpecification.class.getSimpleName() + " for "
							+ managedObjectSource.getClass().getName(), ex);
					return null; // must have complete property details
				}
				if (CompileUtil.isBlank(name)) {
					this.addIssue(ManagedObjectSourceProperty.class.getSimpleName() + " " + i
							+ " provided blank name from " + ManagedObjectSourceSpecification.class.getSimpleName()
							+ " for " + managedObjectSource.getClass().getName());
					return null; // must have complete property details
				}

				// Obtain the property label
				String label;
				try {
					label = mosProperty.getLabel();
				} catch (Throwable ex) {
					this.addIssue("Failed to get label for " + ManagedObjectSourceProperty.class.getSimpleName() + " "
							+ i + " (" + name + ") from " + ManagedObjectSourceSpecification.class.getSimpleName()
							+ " for " + managedObjectSource.getClass().getName(), ex);
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
		MS managedObjectSource = CompileUtil.newInstance(managedObjectSourceClass, ManagedObjectSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (managedObjectSource == null) {
			return null; // failed to instantiate
		}

		// Load and return the managed object type
		return this.loadManagedObjectType(managedObjectSource, propertyList);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>> ManagedObjectType<D> loadManagedObjectType(
			ManagedObjectSource<D, F> managedObjectSource, PropertyList propertyList) {

		// Obtain qualified name
		String qualifiedName = this.node.getQualifiedName();

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.nodeContext.overrideProperties(this.node, qualifiedName,
				this.officeNode, propertyList);

		// Create the managed object source context to initialise
		String officeName = null;
		ManagingOfficeConfiguration<F> managingOffice = new ManagingOfficeBuilderImpl<F>(officeName);
		OfficeConfiguration office = new OfficeBuilderImpl(officeName);
		String namespaceName = null; // stops the name spacing
		String[] additionalProfiles = this.nodeContext.additionalProfiles(this.officeNode);
		ManagedObjectSourceContextImpl<F> sourceContext = new ManagedObjectSourceContextImpl<F>(qualifiedName, true,
				namespaceName, managingOffice, additionalProfiles,
				new PropertyListSourceProperties(overriddenProperties), this.nodeContext.getRootSourceContext(),
				managingOffice.getBuilder(), office.getBuilder(), new Object());

		// Initialise the managed object source and obtain meta-data
		ManagedObjectSourceMetaData<D, F> metaData;
		try {
			// Initialise the managed object source
			metaData = managedObjectSource.init(sourceContext);

		} catch (AbstractSourceError ex) {
			ex.addIssue(this);
			return null; // must have property

		} catch (Throwable ex) {
			this.addIssue("Failed to init", ex);
			return null; // must initialise
		}

		// Ensure have meta-data
		if (metaData == null) {
			this.addIssue("Returned null " + ManagedObjectSourceMetaData.class.getSimpleName());
			return null; // must have meta-data
		}

		// Ensure handle any issue in interacting with meta-data
		Class<?> objectType;
		ManagedObjectDependencyType<D>[] dependencyTypes;
		ManagedObjectFlowType<F>[] flowTypes;
		ManagedObjectExecutionStrategyType[] strategyTypes;
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
				this.addIssue("No " + ManagedObject.class.getSimpleName() + " type provided");
				return null; // must have managed object type
			}
			if (!ManagedObject.class.isAssignableFrom(managedObjectType)) {
				this.addIssue(ManagedObject.class.getSimpleName() + " class must implement "
						+ ManagedObject.class.getName() + " (class=" + managedObjectType.getName() + ")");
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

			// Obtain the execution strategy types
			strategyTypes = this.getExecutionStrategyTypes(metaData);
			if (strategyTypes == null) {
				return null; // issue in getting execution strategy types
			}

			// Obtain the supported extension interfaces
			extensionInterfaces = this.getExtensionInterfaces(metaData);
			if (extensionInterfaces == null) {
				return null; // issue in getting extension interfaces
			}

		} catch (Throwable ex) {
			this.addIssue("Exception from " + managedObjectSource.getClass().getName(), ex);
			return null; // must be successful with meta-data
		}

		// Ensure flows of functions are configured
		if (!this.ensureFunctionFlowsLinked(office)) {
			return null; // issue in flow configuration of functions
		}

		// Determine if input
		boolean isInput = flowTypes.length > 0;

		// Obtain the team types (ensuring functions have names)
		ManagedObjectTeamType[] teamTypes = this.getTeamsEnsuringHaveFunctionNames(office);
		if (teamTypes == null) {
			return null; // issue getting team types
		}

		// Filter out flows already linked to functions
		ManagedObjectFlowType<F>[] unlinkedFlowTypes = this.filterLinkedProcesses(flowTypes, managingOffice, office);
		if (unlinkedFlowTypes == null) {
			return null; // issue in filter meta-data flow types
		}

		// Obtain the function dependencies
		ManagedObjectFunctionDependencyImpl[] functionDependencies = sourceContext
				.getManagedObjectFunctionDependencies();
		ManagedObjectFunctionDependencyType[] functionDependencyTypes = Arrays.asList(functionDependencies).stream()
				.map((functionDependency) -> new ManagedObjectFunctionDependencyTypeImpl(
						functionDependency.getFunctionDependencyName(), functionDependency.getFunctionObjectType(),
						functionDependency.getFunctionObjectTypeQualifier()))
				.toArray(ManagedObjectFunctionDependencyType[]::new);

		// Ensure no duplicate name
		Set<String> functionDependencyNames = new HashSet<>();
		for (ManagedObjectFunctionDependencyType functionDependencyType : functionDependencyTypes) {
			String functionDependencyName = functionDependencyType.getFunctionObjectName();
			if (functionDependencyNames.contains(functionDependencyName)) {
				this.addIssue("Two ManagedObjectFunctionDependency instances added by same name '"
						+ functionDependencyName + "'");
				return null; // can not carry on
			}
			functionDependencyNames.add(functionDependencyName);
		}

		// Ensure no name clash with meta-data
		for (ManagedObjectDependencyType<?> dependencyType : dependencyTypes) {
			String dependencyName = dependencyType.getDependencyName();
			if (functionDependencyNames.contains(dependencyName)) {
				this.addIssue(
						"Name clash '" + dependencyName + "' between meta-data dependency and function dependency");
				return null; // can not carry on
			}
		}

		// Sort the function dependency types (by name)
		Arrays.sort(functionDependencyTypes, (a, b) -> a.getFunctionObjectName().compareTo(b.getFunctionObjectName()));

		// Create and return the managed object type
		return new ManagedObjectTypeImpl<D>(objectType, isInput, dependencyTypes, functionDependencyTypes,
				unlinkedFlowTypes, teamTypes, strategyTypes, extensionInterfaces);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(
			String managedObjectSourceName, Class<MS> managedObjectSourceClass, PropertyList propertyList) {

		// Create an instance of the managed object source
		MS managedObjectSource = CompileUtil.newInstance(managedObjectSourceClass, ManagedObjectSource.class, this.node,
				this.nodeContext.getCompilerIssues());
		if (managedObjectSource == null) {
			return null; // failed to instantiate
		}

		// Return the loaded managed object source type
		return this.loadOfficeFloorManagedObjectSourceType(managedObjectSourceName, managedObjectSource, propertyList);
	}

	@Override
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(
			String managedObjectSourceName, MS managedObjectSource, PropertyList propertyList) {

		// Load the specification
		PropertyList properties = this.loadSpecification(managedObjectSource);
		if (properties == null) {
			return null;
		}

		// Load the values onto the properties
		// Note: create additional optional properties as needed
		for (Property property : propertyList) {
			properties.getOrAddProperty(property.getName()).setValue(property.getValue());
		}

		// Create and return the managed object source type
		return new OfficeFloorManagedObjectSourceTypeImpl(managedObjectSourceName,
				PropertyNode.constructPropertyNodes(properties));
	}

	/**
	 * Filters out any {@link ManagedObjectFlowType} instances of the
	 * {@link ManagedObjectSourceMetaData} that are linked to an added
	 * {@link ManagedFunction}.
	 * 
	 * @param metaDataFlows  {@link ManagedObjectFlowType} instances defining linked
	 *                       processes for the {@link ManagedObjectSourceMetaData}.
	 * @param managingOffice {@link ManagingOfficeConfiguration}.
	 * @param office         {@link OfficeConfiguration}.
	 * @return Filtered {@link ManagedObjectFlowType}.
	 */
	@SuppressWarnings("unchecked")
	private <F extends Enum<F>> ManagedObjectFlowType<F>[] filterLinkedProcesses(
			ManagedObjectFlowType<F>[] metaDataFlows, ManagingOfficeConfiguration<F> managingOffice,
			OfficeConfiguration office) {

		// Determine if required to do filtering
		if (metaDataFlows.length == 0) {
			return metaDataFlows; // no flows to filter
		}
		ManagedObjectFlowConfiguration<F>[] linkedFlows = managingOffice.getFlowConfiguration();
		if ((linkedFlows == null) || (linkedFlows.length == 0)) {
			return metaDataFlows; // no flows linked
		}

		// Required to filter, therefore determine if linking by keys/indexes
		F firstLinkKey = metaDataFlows[0].getKey();
		Class<?> keyClass = (firstLinkKey != null ? firstLinkKey.getClass() : null);

		// Create the filtered list of flows
		for (int i = 0; i < linkedFlows.length; i++) {
			ManagedObjectFlowConfiguration<F> linkedFlow = linkedFlows[i];

			// Ensure have linked flow configuration
			if (linkedFlow == null) {
				continue; // flow index may not be mapped
			}

			// Obtain the link details
			F key = linkedFlow.getFlowKey();
			int index = (key != null ? key.ordinal() : i);

			// Ensure correctly keyed or indexed
			if (key == null) {
				// Ensure should be indexed
				if (keyClass != null) {
					this.addIssue(ManagedObjectFlowMetaData.class.getSimpleName()
							+ " requires linking by keys (not indexes)");
					return null; // linked by index but keyed
				}

				// Ensure the index is in range
				if ((index < 0) || (index >= metaDataFlows.length)) {
					this.addIssue(ManagedObjectFlowMetaData.class.getSimpleName() + " does not define index (index="
							+ index + ")");
					return null;
				}

			} else {
				// Ensure should be keyed
				if (keyClass == null) {
					this.addIssue(ManagedObjectFlowMetaData.class.getSimpleName()
							+ " requires linking by indexes (not keys)");
					return null; // linked by key but indexed
				}

				// Ensure a valid key class
				if (!keyClass.isInstance(key)) {
					this.addIssue("Link key does not match type for " + ManagedObjectFlowMetaData.class.getSimpleName()
							+ " (meta-data key type=" + keyClass.getName() + ", link key type="
							+ key.getClass().getName() + ", link key=" + key + ")");
					return null; // invalid key
				}
			}

			// Obtain details of function being linked too
			String linkLabel = "linked process " + index + " (key=" + (key != null ? key.toString() : "<indexed>");
			ManagedFunctionReference link = linkedFlow.getManagedFunctionReference();
			String linkFunctionName = (link != null ? link.getFunctionName() : null);

			// Ensure have function name
			if (CompileUtil.isBlank(linkFunctionName)) {
				this.addIssue("Must provide function name for " + linkLabel + ")");
				return null;
			}

			// Ensure the linked function was added
			if (!this.isFunctionAdded(linkFunctionName, office)) {
				this.addIssue("Unknown function for " + linkLabel + ", link function=" + linkFunctionName + ")");
				return null;
			}

			// Filter out meta-data flow by setting it to null in the array
			metaDataFlows[index] = null;
		}

		// Create the list of filtered flows
		List<ManagedObjectFlowType<F>> filteredFlows = new ArrayList<ManagedObjectFlowType<F>>(metaDataFlows.length);
		for (int i = 0; i < metaDataFlows.length; i++) {
			if (metaDataFlows[i] != null) {
				// Add meta-data flow as not filtered out
				filteredFlows.add(metaDataFlows[i]);
			}
		}

		// Return the filtered list of flows
		return filteredFlows.stream().toArray(ManagedObjectFlowType[]::new);
	}

	/**
	 * Provides issue if the {@link Flow} of the {@link ManagedFunction} instances
	 * added are not linked.
	 * 
	 * @param office {@link OfficeConfiguration}.
	 * @return <code>true</code> if all {@link Flow} instances of all the
	 *         {@link ManagedFunction} instances are configured. Otherwise,
	 *         <code>false</code> with issues reported.
	 */
	private boolean ensureFunctionFlowsLinked(OfficeConfiguration office) {

		// Obtain the flows instigated by the functions
		for (ManagedFunctionConfiguration<?, ?> function : office.getManagedFunctionConfiguration()) {
			FlowConfiguration<?>[] flows = function.getFlowConfiguration();
			for (int i = 0; i < flows.length; i++) {
				FlowConfiguration<?> flow = flows[i];

				// Obtain the flow details
				String functionName = function.getFunctionName();
				String flowName = flow.getFlowName();
				String flowLabel = "function=" + functionName + ", flow=" + flowName;

				// Obtain linked task details
				ManagedFunctionReference link = flow.getInitialFunction();
				String linkFunctionName = (link == null ? null : link.getFunctionName());

				// Determine if linked to function
				if (!CompileUtil.isBlank(linkFunctionName)) {

					// Ensure the function is added
					if (!this.isFunctionAdded(linkFunctionName, office)) {
						this.addIssue("Unknown function being linked (" + flowLabel + ", link function="
								+ linkFunctionName + ")");
						return false; // invalid
					}
				}
			}
		}

		// As here, valid
		return true;
	}

	/**
	 * Obtains the {@link ManagedObjectTeamType} instances ensuring all added
	 * {@link ManagedFunction} instances have names.
	 * 
	 * @param office {@link OfficeConfiguration}.
	 * @return {@link ManagedObjectTeamType} instances.
	 */
	private ManagedObjectTeamType[] getTeamsEnsuringHaveFunctionNames(OfficeConfiguration office) {

		// Ensure all added functions valid (and collect the set of team names)
		Set<String> teamNames = new HashSet<String>();
		for (ManagedFunctionConfiguration<?, ?> function : office.getManagedFunctionConfiguration()) {

			// Ensure have function name
			String functionName = function.getFunctionName();
			if (CompileUtil.isBlank(functionName)) {
				this.addIssue("Function added without a name");
				return null; // must have function name
			}

			// Register the possible team
			String teamName = function.getResponsibleTeamName();
			if (!CompileUtil.isBlank(teamName)) {
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
	 * Indicates if the {@link ManagedFunction} was added to the {@link Office}.
	 * 
	 * @param functionName {@link ManagedFunction} name.
	 * @param office       {@link OfficeConfiguration}.
	 * @return <code>true</code> if {@link ManagedFunction} added to the
	 *         {@link Office}.
	 */
	private boolean isFunctionAdded(String functionName, OfficeConfiguration office) {

		// Determine if the task is added to the office
		for (ManagedFunctionConfiguration<?, ?> function : office.getManagedFunctionConfiguration()) {
			if (functionName.equals(function.getFunctionName())) {
				// Function added to office
				return true;
			}
		}

		// If at this point, function not added to the office
		return false;
	}

	/**
	 * Obtains the {@link ManagedObjectDependencyType} instances from the
	 * {@link ManagedObjectSourceMetaData}.
	 * 
	 * @param metaData {@link ManagedObjectSourceMetaData}.
	 * @return {@link ManagedObjectDependencyType} instances.
	 */
	@SuppressWarnings("unchecked")
	private <D extends Enum<D>> ManagedObjectDependencyType<D>[] getManagedObjectDependencyTypes(
			ManagedObjectSourceMetaData<D, ?> metaData) {

		// Obtain the dependency meta-data
		ManagedObjectDependencyType<D>[] dependencyTypes;
		Class<?> dependencyKeys = null;
		ManagedObjectDependencyMetaData<D>[] dependencyMetaDatas = metaData.getDependencyMetaData();
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
					this.addIssue(
							"Null " + ManagedObjectDependencyMetaData.class.getSimpleName() + " for dependency " + i);
					return null; // missing met-data
				}

				// Obtain details for dependency
				String label = dependencyMetaData.getLabel();
				D key = dependencyMetaData.getKey();
				String dependencyLabel = "dependency " + i + " (key=" + (key == null ? "<indexed>" : key.toString())
						+ ", label=" + (CompileUtil.isBlank(label) ? "<no label>" : label) + ")";

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
										+ dependencyKeys.getName() + ", " + key.getClass().getName() + ")");
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

				// Obtain the annotations
				Object[] annotations = dependencyMetaData.getAnnotations();

				// Create and add the dependency type
				dependencyTypes[i] = new ManagedObjectDependencyTypeImpl<D>(index, type, typeQualifier, annotations,
						key, label);
			}
		}

		// Validate have all the dependencies
		if (dependencyKeys == null) {
			// Determine if indexed or no dependencies
			dependencyKeys = (dependencyTypes.length == 0 ? None.class : Indexed.class);
		} else {
			// Ensure exactly one dependency per key
			Set<?> keys = new HashSet<Object>(Arrays.asList(dependencyKeys.getEnumConstants()));
			for (ManagedObjectDependencyType<D> dependencyType : dependencyTypes) {
				D key = dependencyType.getKey();
				if (!keys.contains(key)) {
					this.addIssue("Must have exactly one dependency per key (key=" + key + ")");
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
				this.addIssue("Missing dependency meta-data (keys=" + msg.toString() + ")");
				return null; // must have meta-data for each key
			}
		}

		// Ensure the dependency types are in index order
		Arrays.sort(dependencyTypes, new Comparator<ManagedObjectDependencyType<D>>() {
			@Override
			public int compare(ManagedObjectDependencyType<D> a, ManagedObjectDependencyType<D> b) {
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
	 * @param metaData {@link ManagedObjectSourceMetaData}.
	 * @return {@link ManagedObjectFlowType} instances.
	 */
	@SuppressWarnings("unchecked")
	private <F extends Enum<F>> ManagedObjectFlowType<F>[] getManagedObjectFlowTypes(
			ManagedObjectSourceMetaData<?, F> metaData) {

		// Obtain the flow meta-data
		ManagedObjectFlowType<F>[] flowTypes;
		Class<?> flowKeys = null;
		ManagedObjectFlowMetaData<F>[] flowMetaDatas = metaData.getFlowMetaData();
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
					this.addIssue("Null " + ManagedObjectFlowMetaData.class.getSimpleName() + " for flow " + i);
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
								this.addIssue("Meta-data flows identified by different key types (" + flowKeys.getName()
										+ ", " + key.getClass().getName() + ")");
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
				flowTypes[i] = new ManagedObjectFlowTypeImpl<F>(index, type, key, label);
			}
		}

		// Validate have all the dependencies
		if (flowKeys == null) {
			// Determine if indexed or no dependencies
			flowKeys = (flowTypes.length == 0 ? None.class : Indexed.class);
		} else {
			// Ensure exactly one flow per key
			Set<?> keys = new HashSet<Object>(Arrays.asList(flowKeys.getEnumConstants()));
			for (ManagedObjectFlowType<F> flowType : flowTypes) {
				F key = flowType.getKey();
				if (!keys.contains(key)) {
					this.addIssue("Must have exactly one flow per key (key=" + key + ")");
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
				this.addIssue("Missing flow meta-data (keys=" + msg.toString() + ")");
				return null; // must have meta-data for each key
			}
		}

		// Ensure the flow types are in index order
		Arrays.sort(flowTypes, new Comparator<ManagedObjectFlowType<F>>() {
			@Override
			public int compare(ManagedObjectFlowType<F> a, ManagedObjectFlowType<F> b) {
				return a.getIndex() - b.getIndex();
			}
		});

		// Return the flow types
		return flowTypes;
	}

	/**
	 * Obtains the {@link ManagedObjectExecutionStrategyType} instances.
	 * 
	 * @param metaData {@link ManagedObjectSourceMetaData}.
	 * @return {@link ManagedObjectExecutionStrategyType} instances.
	 */
	private ManagedObjectExecutionStrategyType[] getExecutionStrategyTypes(ManagedObjectSourceMetaData<?, ?> metaData) {

		// Obtain the execution strategies
		ManagedObjectExecutionStrategyType[] strategyTypes;
		ManagedObjectExecutionMetaData[] executionMetaDatas = metaData.getExecutionMetaData();
		if (executionMetaDatas == null) {
			// No strategies
			strategyTypes = new ManagedObjectExecutionStrategyType[0];

		} else {
			// Load the execution strategies
			strategyTypes = new ManagedObjectExecutionStrategyType[executionMetaDatas.length];
			for (int i = 0; i < strategyTypes.length; i++) {
				ManagedObjectExecutionMetaData executionMetaData = executionMetaDatas[i];

				// Ensure have execution meta-data
				if (executionMetaData == null) {
					this.addIssue("Null " + ManagedObjectExecutionMetaData.class.getSimpleName()
							+ " for execution strategy " + i);
					return null; // missing met-data
				}

				// Obtain the execution details
				String executionStrategyName = executionMetaData.getLabel();
				if (CompileUtil.isBlank(executionStrategyName)) {
					executionStrategyName = String.valueOf(i);
				}

				// Add the execution strategy type
				strategyTypes[i] = new ManagedObjectExecutionStrategyTypeImpl(executionStrategyName);
			}
		}

		// Return the exeuction strategy types
		return strategyTypes;
	}

	/**
	 * Obtains the extension interfaces supported by the {@link ManagedObject}.
	 * 
	 * @param metaData {@link ManagedObjectSourceMetaData}.
	 * @return Extension interfaces.
	 */
	private Class<?>[] getExtensionInterfaces(ManagedObjectSourceMetaData<?, ?> metaData) {

		// Obtain the extension interface meta-data
		Class<?>[] extensionInterfaces;
		ManagedObjectExtensionMetaData<?>[] eiMetaDatas = metaData.getExtensionInterfacesMetaData();
		if (eiMetaDatas == null) {
			// No extension interfaces supported
			extensionInterfaces = new Class[0];

		} else {
			// Obtain the extension interfaces supported
			extensionInterfaces = new Class[eiMetaDatas.length];
			for (int i = 0; i < extensionInterfaces.length; i++) {
				ManagedObjectExtensionMetaData<?> eiMetaData = eiMetaDatas[i];

				// Ensure have the interface meta-data
				if (eiMetaData == null) {
					this.addIssue("Null extension interface meta-data");
					return null; // must have meta-data
				}

				// Obtain the extension interface type
				Class<?> eiType = eiMetaData.getExtensionType();
				if (eiType == null) {
					this.addIssue("Null extension interface type");
					return null; // must have type
				}

				// Ensure an extension factory
				if (eiMetaData.getExtensionFactory() == null) {
					this.addIssue("No extension factory (type=" + eiType.getName() + ")");
					return null; // must have factory
				}

				// Load the extension interface
				extensionInterfaces[i] = eiType;
			}

		}

		// Return the supported extension interfaces
		return extensionInterfaces;
	}

	/*
	 * ================== IssueTarget ==================
	 */

	@Override
	public void addIssue(String issueDescription) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription);
	}

	@Override
	public void addIssue(String issueDescription, Throwable cause) {
		this.nodeContext.getCompilerIssues().addIssue(this.node, issueDescription, cause);
	}

}
