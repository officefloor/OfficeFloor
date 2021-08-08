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

package net.officefloor.model.impl.section;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.DisconnectChange;
import net.officefloor.model.impl.change.NoChange;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.ExternalManagedObjectModel;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.section.FunctionEscalationToFunctionModel;
import net.officefloor.model.section.FunctionEscalationToSubSectionInputModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionFlowToExternalFlowModel;
import net.officefloor.model.section.FunctionFlowToFunctionModel;
import net.officefloor.model.section.FunctionFlowToSubSectionInputModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionToNextExternalFlowModel;
import net.officefloor.model.section.FunctionToNextFunctionModel;
import net.officefloor.model.section.FunctionToNextSubSectionInputModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.ManagedFunctionToFunctionModel;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionManagedObjectDependencyModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectDependencyToSectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToSubSectionInputModel;
import net.officefloor.model.section.SectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionManagedObjectToSectionManagedObjectSourceModel;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SubSectionInputModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectToSectionManagedObjectModel;
import net.officefloor.model.section.SubSectionOutputModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToFunctionModel;
import net.officefloor.model.section.SubSectionOutputToSubSectionInputModel;

/**
 * {@link SectionChanges} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class SectionChangesImpl implements SectionChanges {

	/**
	 * <p>
	 * Sorts the {@link FunctionNamespaceModel} instances.
	 * <p>
	 * This enable easier merging of configuration under SCM.
	 *
	 * @param namespaceModels
	 *            {@link FunctionNamespaceModel} instances.
	 */
	public static void sortNamespaceModels(List<FunctionNamespaceModel> namespaceModels) {
		Collections.sort(namespaceModels, new Comparator<FunctionNamespaceModel>() {
			@Override
			public int compare(FunctionNamespaceModel a, FunctionNamespaceModel b) {
				return a.getFunctionNamespaceName().compareTo(b.getFunctionNamespaceName());
			}
		});
	}

	/**
	 * <p>
	 * Sorts the {@link ManagedFunctionModel} instances.
	 * <p>
	 * This enables easier merging of configuration under SCM.
	 *
	 * @param managedFunctionModels
	 *            {@link ManagedFunctionModel} instances.
	 */
	public static void sortManagedFunctionModels(List<ManagedFunctionModel> managedFunctionModels) {
		Collections.sort(managedFunctionModels, new Comparator<ManagedFunctionModel>() {
			@Override
			public int compare(ManagedFunctionModel a, ManagedFunctionModel b) {
				return a.getManagedFunctionName().compareTo(b.getManagedFunctionName());
			}
		});
	}

	/**
	 * Sorts the {@link ManagedFunctionToFunctionModel} connections.
	 *
	 * @param managedFunctionToFunctionConnections
	 *            {@link ManagedFunctionToFunctionModel} instances.
	 */
	public static void sortManagedFunctionToFunctionConnections(
			List<ManagedFunctionToFunctionModel> managedFunctionToFunctionConnections) {
		Collections.sort(managedFunctionToFunctionConnections, new Comparator<ManagedFunctionToFunctionModel>() {
			@Override
			public int compare(ManagedFunctionToFunctionModel a, ManagedFunctionToFunctionModel b) {
				return a.getFunction().getFunctionName().compareTo(b.getFunction().getFunctionName());
			}
		});
	}

	/**
	 * <p>
	 * Sorts the {@link FunctionModel} instances.
	 * <p>
	 * This enable easier merging of configuration under SCM.
	 *
	 * @param functionModels
	 *            {@link FunctionModel} instances.
	 */
	public static void sortFunctionModels(List<FunctionModel> functionModels) {
		Collections.sort(functionModels, new Comparator<FunctionModel>() {
			@Override
			public int compare(FunctionModel a, FunctionModel b) {
				return a.getFunctionName().compareTo(b.getFunctionName());
			}
		});
	}

	/**
	 * Sorts the {@link SubSectionModel} instances.
	 *
	 * @param subSections
	 *            Listing of {@link SubSectionModel} instances to sort.
	 */
	public static void sortSubSections(List<SubSectionModel> subSections) {
		Collections.sort(subSections, new Comparator<SubSectionModel>() {
			@Override
			public int compare(SubSectionModel a, SubSectionModel b) {
				return a.getSubSectionName().compareTo(b.getSubSectionName());
			}
		});
	}

	/**
	 * <p>
	 * Sorts the {@link ExternalFlowModel} instances.
	 * <p>
	 * This enables easier merging of configuration under SCM.
	 *
	 * @param externalFlows
	 *            {@link ExternalFlowModel} instances.
	 */
	public static void sortExternalFlows(List<ExternalFlowModel> externalFlows) {
		Collections.sort(externalFlows, new Comparator<ExternalFlowModel>() {
			@Override
			public int compare(ExternalFlowModel a, ExternalFlowModel b) {
				return a.getExternalFlowName().compareTo(b.getExternalFlowName());
			}
		});
	}

	/**
	 * <p>
	 * Sorts the {@link ExternalManagedObjectModel} instances.
	 * <p>
	 * This enables easier merging of configuration under SCM.
	 *
	 * @param externalManagedObjects
	 *            {@link ExternalManagedObjectModel} instances.
	 */
	public static void sortExternalManagedObjects(List<ExternalManagedObjectModel> externalManagedObjects) {
		Collections.sort(externalManagedObjects, new Comparator<ExternalManagedObjectModel>() {
			@Override
			public int compare(ExternalManagedObjectModel a, ExternalManagedObjectModel b) {
				return a.getExternalManagedObjectName().compareTo(b.getExternalManagedObjectName());
			}
		});
	}

	/**
	 * Obtains the text name identifying the {@link ManagedObjectScope}.
	 *
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 * @return Text name for the {@link ManagedObjectScope}.
	 */
	public static String getManagedObjectScope(ManagedObjectScope scope) {

		// Ensure have scope
		if (scope == null) {
			return null;
		}

		// Return the text of the scope
		switch (scope) {
		case PROCESS:
			return PROCESS_MANAGED_OBJECT_SCOPE;
		case THREAD:
			return THREAD_MANAGED_OBJECT_SCOPE;
		case FUNCTION:
			return FUNCTION_MANAGED_OBJECT_SCOPE;
		default:
			throw new IllegalStateException("Unknown scope " + scope);
		}
	}

	/**
	 * {@link SectionModel} to be operated on.
	 */
	private final SectionModel section;

	/**
	 * Initiate.
	 *
	 * @param section
	 *            {@link SectionModel}.
	 */
	public SectionChangesImpl(SectionModel section) {
		this.section = section;
	}

	/**
	 * Sorts the {@link FunctionNamespaceModel} instances.
	 */
	protected void sortNamespaceModels() {
		sortNamespaceModels(this.section.getFunctionNamespaces());
	}

	/**
	 * Sorts the {@link FunctionModel} instances.
	 */
	protected void sortFunctionModels() {
		sortFunctionModels(this.section.getFunctions());
	}

	/**
	 * Sorts the {@link SubSectionModel} instances on the {@link SectionModel}.
	 */
	public void sortSubSections() {
		sortSubSections(this.section.getSubSections());
	}

	/**
	 * Sorts the {@link ExternalFlowModel} instances.
	 */
	protected void sortExternalFlows() {
		sortExternalFlows(this.section.getExternalFlows());
	}

	/**
	 * Sorts the {@link ExternalManagedObjectModel} instances.
	 */
	protected void sortExternalManagedObjects() {
		sortExternalManagedObjects(this.section.getExternalManagedObjects());
	}

	/**
	 * Creates a {@link ManagedFunctionModel} for a {@link ManagedFunctionType}.
	 *
	 * @param managedFunctionType
	 *            {@link ManagedFunctionType}.
	 * @return {@link ManagedFunctionkModel} for the {@link ManagedFunctionType}.
	 */
	private ManagedFunctionModel createManagedFunctionModel(ManagedFunctionType<?, ?> managedFunctionType) {

		// Create the managed function model
		ManagedFunctionModel managedFunction = new ManagedFunctionModel(managedFunctionType.getFunctionName());

		// Add the managed function object models
		for (ManagedFunctionObjectType<?> managedFunctionObjectType : managedFunctionType.getObjectTypes()) {
			Enum<?> key = managedFunctionObjectType.getKey();
			ManagedFunctionObjectModel managedFunctionObject = new ManagedFunctionObjectModel(
					managedFunctionObjectType.getObjectName(), (key == null ? null : key.name()),
					managedFunctionObjectType.getObjectType().getName(), false);
			managedFunction.addManagedFunctionObject(managedFunctionObject);
		}

		// Return the managed function model
		return managedFunction;
	}

	/**
	 * Removes the connections to the {@link FunctionModel} (except to its
	 * {@link ManagedFunctionModel}).
	 *
	 * @param function
	 *            {@link FunctionModel}.
	 * @param connectionList
	 *            Listing to add removed {@link ConnectionModel} instances.
	 */
	private void removeFunctionConnections(FunctionModel function, List<ConnectionModel> connectionList) {

		// Remove input connections (copy to stop concurrent)
		for (FunctionToNextFunctionModel conn : new ArrayList<FunctionToNextFunctionModel>(
				function.getPreviousFunctions())) {
			conn.remove();
			connectionList.add(conn);
		}
		for (FunctionFlowToFunctionModel conn : new ArrayList<FunctionFlowToFunctionModel>(
				function.getFunctionFlowInputs())) {
			conn.remove();
			connectionList.add(conn);
		}
		for (FunctionEscalationToFunctionModel conn : new ArrayList<FunctionEscalationToFunctionModel>(
				function.getFunctionEscalationInputs())) {
			conn.remove();
			connectionList.add(conn);
		}

		// Remove flow connections
		for (FunctionFlowModel flow : function.getFunctionFlows()) {
			this.removeFunctionFlowConnections(flow, connectionList);
		}

		// Remove next connections
		FunctionToNextFunctionModel connNextFunction = function.getNextFunction();
		if (connNextFunction != null) {
			connNextFunction.remove();
			connectionList.add(connNextFunction);
		}
		FunctionToNextExternalFlowModel connNextExtFlow = function.getNextExternalFlow();
		if (connNextExtFlow != null) {
			connNextExtFlow.remove();
			connectionList.add(connNextExtFlow);
		}

		// Remove escalation connections
		for (FunctionEscalationModel escalation : function.getFunctionEscalations()) {
			this.removeFunctionEscalationConnections(escalation, connectionList);
		}
	}

	/**
	 * Removes the connections to the {@link FunctionFlowModel}.
	 *
	 * @param functionFlow
	 *            {@link FunctionFlowModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeFunctionFlowConnections(FunctionFlowModel functionFlow, List<ConnectionModel> connectionList) {

		// Remove connection to function
		FunctionFlowToFunctionModel connFunction = functionFlow.getFunction();
		if (connFunction != null) {
			connFunction.remove();
			connectionList.add(connFunction);
		}

		// Remove connection to external flow
		FunctionFlowToExternalFlowModel connExtFlow = functionFlow.getExternalFlow();
		if (connExtFlow != null) {
			connExtFlow.remove();
			connectionList.add(connExtFlow);
		}
	}

	/**
	 * Removes the connections to the {@link FunctionEscalationModel}.
	 *
	 * @param functionEscalation
	 *            {@link FunctionEscalationModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeFunctionEscalationConnections(FunctionEscalationModel functionEscalation,
			List<ConnectionModel> connectionList) {

		// Remove connection to function
		FunctionEscalationToFunctionModel connFunction = functionEscalation.getFunction();
		if (connFunction != null) {
			connFunction.remove();
			connectionList.add(connFunction);
		}

		// Remove connection to external flow
		FunctionEscalationToExternalFlowModel connExtFlow = functionEscalation.getExternalFlow();
		if (connExtFlow != null) {
			connExtFlow.remove();
			connectionList.add(connExtFlow);
		}
	}

	/**
	 * Removes the connections to the {@link ManagedFunctionModel} and its
	 * associated {@link FunctionModel} instances.
	 *
	 * @param managedFunction
	 *            {@link ManagedFunctionModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeManagedFunctionConnections(ManagedFunctionModel managedFunction,
			List<ConnectionModel> connectionList) {

		// Remove object connections
		for (ManagedFunctionObjectModel managedFunctionObject : managedFunction.getManagedFunctionObjects()) {
			this.removeManagedFunctionObjectConnections(managedFunctionObject, connectionList);
		}

		// Remove function connections (copy to stop concurrent)
		for (ManagedFunctionToFunctionModel functionConn : new ArrayList<ManagedFunctionToFunctionModel>(
				managedFunction.getFunctions())) {
			FunctionModel function = functionConn.getFunction();
			this.removeFunctionConnections(function, connectionList);
		}
	}

	/**
	 * Removes the connections to the {@link ManagedFunctionObjectModel}.
	 *
	 * @param managedFunctionObject
	 *            {@link ManagedFunctionObjectModel}.
	 * @param connectionList
	 *            Listing to add the removed {@link ConnectionModel} instances.
	 */
	private void removeManagedFunctionObjectConnections(ManagedFunctionObjectModel managedFunctionObject,
			List<ConnectionModel> connectionList) {

		// Remove connection to external managed object
		ManagedFunctionObjectToExternalManagedObjectModel conn = managedFunctionObject.getExternalManagedObject();
		if (conn != null) {
			conn.remove();
			connectionList.add(conn);
		}
	}

	/**
	 * Removes the {@link FunctionModel} instances associated to the
	 * {@link ManagedFunctionModel}.
	 *
	 * @param managedFunction
	 *            {@link ManagedFunctionModel}.
	 * @param functionList
	 *            Listing to add the removed {@link FunctionModel} instances.
	 */
	private void removeManagedFunction(ManagedFunctionModel managedFunction, List<FunctionModel> functionList) {
		for (ManagedFunctionToFunctionModel conn : managedFunction.getFunctions()) {
			FunctionModel function = conn.getFunction();

			// Remove function and store for revert
			SectionChangesImpl.this.section.removeFunction(function);
			functionList.add(function);
		}
	}

	/*
	 * ====================== SectionOperations ============================
	 */

	@Override
	public Change<FunctionNamespaceModel> addFunctionNamespace(String namespaceName,
			String managedFunctionSourceClassName, PropertyList properties, FunctionNamespaceType namespaceType,
			String... managedFunctionNames) {

		// Create the namespace model for the namespace type
		final FunctionNamespaceModel namespace = new FunctionNamespaceModel(namespaceName,
				managedFunctionSourceClassName);

		// Add the properties to source the namespace again
		for (Property property : properties) {
			namespace.addProperty(new PropertyModel(property.getName(), property.getValue()));
		}

		// Create the set of managed function names to include
		Set<String> includeFunctionNames = new HashSet<String>();
		for (String functionName : managedFunctionNames) {
			includeFunctionNames.add(functionName);
		}

		// Add the managed function models
		for (ManagedFunctionType<?, ?> managedFunctionType : namespaceType.getManagedFunctionTypes()) {

			// Determine if include the function type
			String functionName = managedFunctionType.getFunctionName();
			if ((includeFunctionNames.size() > 0) && (!includeFunctionNames.contains(functionName))) {
				// Function to not be included
				continue;
			}

			// Create and add the managed function model
			ManagedFunctionModel managedFunction = SectionChangesImpl.this
					.createManagedFunctionModel(managedFunctionType);
			namespace.addManagedFunction(managedFunction);
		}

		// Ensure managed function models in sorted order
		SectionChangesImpl.sortManagedFunctionModels(namespace.getManagedFunctions());

		// Return the change to add the namespace
		return new AbstractChange<FunctionNamespaceModel>(namespace, "Add function namespace " + namespaceName) {
			@Override
			public void apply() {
				// Add the namespace (ensuring in sorted order)
				SectionChangesImpl.this.section.addFunctionNamespace(namespace);
				SectionChangesImpl.this.sortNamespaceModels();
			}

			@Override
			public void revert() {
				SectionChangesImpl.this.section.removeFunctionNamespace(namespace);
			}
		};
	}

	@Override
	public Change<FunctionNamespaceModel> removeFunctionNamespace(final FunctionNamespaceModel namespaceModel) {

		// Ensure the namespace is on the section
		boolean isOnSection = false;
		for (FunctionNamespaceModel namespace : this.section.getFunctionNamespaces()) {
			if (namespace == namespaceModel) {
				isOnSection = true;
			}
		}
		if (!isOnSection) {
			// Not in section so can not remove
			return new NoChange<FunctionNamespaceModel>(namespaceModel,
					"Remove namespace " + namespaceModel.getFunctionNamespaceName(),
					"Function namespace " + namespaceModel.getFunctionNamespaceName() + " not in section");
		}

		// Return change to remove the namespace
		return new AbstractChange<FunctionNamespaceModel>(namespaceModel,
				"Remove namespace " + namespaceModel.getFunctionNamespaceName()) {

			/**
			 * {@link FunctionModel} instances associated to {@link FunctionNamespaceModel}.
			 */
			private FunctionModel[] functions;

			/**
			 * {@link ConnectionModel} instances associated to the
			 * {@link FunctionNamespaceModel}.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove connections to namespace and its functions
				List<ConnectionModel> connectionList = new LinkedList<ConnectionModel>();
				for (ManagedFunctionModel managedFunction : namespaceModel.getManagedFunctions()) {
					SectionChangesImpl.this.removeManagedFunctionConnections(managedFunction, connectionList);
				}
				this.connections = connectionList.toArray(new ConnectionModel[0]);

				// Remove the associated functions (storing for revert)
				List<FunctionModel> functionList = new LinkedList<FunctionModel>();
				for (ManagedFunctionModel managedFunction : namespaceModel.getManagedFunctions()) {
					SectionChangesImpl.this.removeManagedFunction(managedFunction, functionList);
				}
				this.functions = functionList.toArray(new FunctionModel[0]);

				// Remove the namespace
				SectionChangesImpl.this.section.removeFunctionNamespace(namespaceModel);
			}

			@Override
			public void revert() {
				// Add the namespace (ensuring in sorted order)
				SectionChangesImpl.this.section.addFunctionNamespace(namespaceModel);
				SectionChangesImpl.this.sortNamespaceModels();

				// Add the functions (in reverse order, ensuring sorted)
				for (int i = (this.functions.length - 1); i >= 0; i--) {
					SectionChangesImpl.this.section.addFunction(this.functions[i]);
				}
				SectionChangesImpl.this.sortFunctionModels();

				// Reconnect connections
				for (ConnectionModel connection : this.connections) {
					connection.connect();
				}
			}
		};
	}

	@Override
	public Change<FunctionNamespaceModel> renameFunctionNamespace(final FunctionNamespaceModel namespaceModel,
			final String newFunctionNamespaceName) {

		// Ensure the namespace is on the section
		boolean isOnSection = false;
		for (FunctionNamespaceModel namespace : this.section.getFunctionNamespaces()) {
			if (namespace == namespaceModel) {
				isOnSection = true;
			}
		}
		if (!isOnSection) {
			// Not in section so can not remove
			return new NoChange<FunctionNamespaceModel>(namespaceModel,
					"Rename namespace " + namespaceModel.getFunctionNamespaceName() + " to " + newFunctionNamespaceName,
					"Function namespace " + namespaceModel.getFunctionNamespaceName() + " not in section");
		}

		// Store the old name for reverting
		final String oldFunctionNamespaceName = namespaceModel.getFunctionNamespaceName();

		// Return change to rename namespace
		return new AbstractChange<FunctionNamespaceModel>(namespaceModel,
				"Rename namespace " + namespaceModel.getFunctionNamespaceName() + " to " + newFunctionNamespaceName) {
			@Override
			public void apply() {
				// Rename and ensure namespace in sorted order
				namespaceModel.setFunctionNamespaceName(newFunctionNamespaceName);
				SectionChangesImpl.this.sortNamespaceModels();
			}

			@Override
			public void revert() {
				// Revert to old name, ensuring namespace sorted
				namespaceModel.setFunctionNamespaceName(oldFunctionNamespaceName);
				SectionChangesImpl.this.sortNamespaceModels();
			}
		};
	}

	@Override
	public Change<FunctionNamespaceModel> refactorFunctionNamespace(final FunctionNamespaceModel namespaceModel,
			final String namespaceName, final String managedFunctionSourceClassName, PropertyList properties,
			FunctionNamespaceType namespaceType, Map<String, String> managedFunctionNameMapping,
			Map<String, Map<String, String>> managedFunctionToObjectNameMapping,
			Map<String, Map<String, String>> functionToFlowNameMapping,
			Map<String, Map<String, String>> functionToEscalationTypeMapping, String... functionNames) {

		// Create the list to contain all refactor changes
		final List<Change<?>> refactor = new LinkedList<Change<?>>();

		// ------------ Details of FunctionNamespaceModel ------------

		// Add change to rename the namespace
		final String existingNamespaceName = namespaceModel.getFunctionNamespaceName();
		refactor.add(new AbstractChange<FunctionNamespaceModel>(namespaceModel, "Rename namespace") {
			@Override
			public void apply() {
				namespaceModel.setFunctionNamespaceName(namespaceName);
			}

			@Override
			public void revert() {
				namespaceModel.setFunctionNamespaceName(existingNamespaceName);
			}
		});

		// Add change for managed function class source name
		final String existingManagedFunctionSourceClassName = namespaceModel.getManagedFunctionSourceClassName();
		refactor.add(new AbstractChange<FunctionNamespaceModel>(namespaceModel, "Change ManagedFunctionSource class") {
			@Override
			public void apply() {
				namespaceModel.setManagedFunctionSourceClassName(managedFunctionSourceClassName);
			}

			@Override
			public void revert() {
				namespaceModel.setManagedFunctionSourceClassName(existingManagedFunctionSourceClassName);
			}
		});

		// Add change to the properties
		final List<PropertyModel> existingProperties = new ArrayList<PropertyModel>(namespaceModel.getProperties());
		final List<PropertyModel> newProperties = new LinkedList<PropertyModel>();
		for (Property property : properties) {
			newProperties.add(new PropertyModel(property.getName(), property.getValue()));
		}
		refactor.add(new AbstractChange<FunctionNamespaceModel>(namespaceModel, "Change namespace properties") {
			@Override
			public void apply() {
				for (PropertyModel property : existingProperties) {
					namespaceModel.removeProperty(property);
				}
				for (PropertyModel property : newProperties) {
					namespaceModel.addProperty(property);
				}
			}

			@Override
			public void revert() {
				for (PropertyModel property : newProperties) {
					namespaceModel.removeProperty(property);
				}
				for (PropertyModel property : existingProperties) {
					namespaceModel.addProperty(property);
				}
			}
		});

		// ------- ManagedFunctionModel / FunctionModel -------

		// Create the map of existing managed functions to their names
		Map<String, ManagedFunctionModel> existingManagedFunctions = new HashMap<String, ManagedFunctionModel>();
		for (ManagedFunctionModel managedFunction : namespaceModel.getManagedFunctions()) {
			existingManagedFunctions.put(managedFunction.getManagedFunctionName(), managedFunction);
		}

		// Create the set of functions to include
		Set<String> includeFunctionNames = new HashSet<String>(Arrays.asList(functionNames));

		// Refactor functions
		ManagedFunctionType<?, ?>[] functionTypes = namespaceType.getManagedFunctionTypes();
		List<ManagedFunctionModel> targetFunctionList = new LinkedList<ManagedFunctionModel>();
		for (int t = 0; t < functionTypes.length; t++) {
			ManagedFunctionType<?, ?> functionType = functionTypes[t];

			// Obtain the details of the function type
			final String managedFunctionName = functionType.getFunctionName();
			Class<?> returnClass = functionType.getReturnType();
			final String returnTypeName = (returnClass == null ? null : returnClass.getName());

			// Determine if include the function
			if ((includeFunctionNames.size() > 0) && (!(includeFunctionNames.contains(managedFunctionName)))) {
				continue; // function filtered from being included
			}

			// Obtain managed function for function type (may need to create)
			ManagedFunctionModel findManagedFunction = this.getExistingItem(managedFunctionName,
					managedFunctionNameMapping, existingManagedFunctions);
			final ManagedFunctionModel managedFunction = ((findManagedFunction == null)
					? new ManagedFunctionModel(managedFunctionName)
					: findManagedFunction);
			targetFunctionList.add(managedFunction);

			// Refactor details of managed function (and functions)
			final String existingManagedFunctionName = managedFunction.getManagedFunctionName();
			refactor.add(new AbstractChange<ManagedFunctionModel>(managedFunction, "Refactor managed function") {

				/**
				 * Existing return types for {@link FunctionModel} instances.
				 */
				private Map<FunctionModel, String> existingReturnTypes = new HashMap<FunctionModel, String>();

				@Override
				public void apply() {
					// Specify new function name
					managedFunction.setManagedFunctionName(managedFunctionName);
					for (ManagedFunctionToFunctionModel conn : managedFunction.getFunctions()) {
						FunctionModel function = conn.getFunction();
						function.setManagedFunctionName(managedFunctionName);
						this.existingReturnTypes.put(function, function.getReturnType());
						function.setReturnType(returnTypeName);
					}
				}

				@Override
				public void revert() {
					// Revert to existing function name
					managedFunction.setManagedFunctionName(existingManagedFunctionName);
					for (ManagedFunctionToFunctionModel conn : managedFunction.getFunctions()) {
						FunctionModel function = conn.getFunction();
						function.setManagedFunctionName(existingManagedFunctionName);
						function.setReturnType(this.existingReturnTypes.get(function));
					}
				}
			});

			// ---------- ManagedFunctionObjectModel ----------

			// Managed function to be refactored, so obtain object name mappings
			Map<String, String> objectTargetToExisting = managedFunctionToObjectNameMapping.get(managedFunctionName);
			if (objectTargetToExisting == null) {
				// Provide default empty map
				objectTargetToExisting = new HashMap<String, String>(0);
			}

			// Create map of existing managed function objects to their names
			Map<String, ManagedFunctionObjectModel> existingManagedFunctionObjects = new HashMap<String, ManagedFunctionObjectModel>();
			for (ManagedFunctionObjectModel managedFunctionObject : managedFunction.getManagedFunctionObjects()) {
				existingManagedFunctionObjects.put(managedFunctionObject.getObjectName(), managedFunctionObject);
			}

			// Obtain the objects in order as per type
			ManagedFunctionObjectType<?>[] objectTypes = functionType.getObjectTypes();
			final ManagedFunctionObjectModel[] targetObjectOrder = new ManagedFunctionObjectModel[objectTypes.length];
			for (int o = 0; o < objectTypes.length; o++) {
				ManagedFunctionObjectType<?> objectType = objectTypes[o];

				// Obtain the details of the object type
				final String objectName = objectType.getObjectName();
				Enum<?> objectKey = objectType.getKey();
				final String objectKeyName = (objectKey == null ? null : objectKey.name());
				Class<?> objectClass = objectType.getObjectType();
				final String objectTypeName = (objectClass == null ? null : objectClass.getName());

				// Obtain the object for object type (may need to create)
				ManagedFunctionObjectModel findManagedFunctionObject = this.getExistingItem(objectName,
						objectTargetToExisting, existingManagedFunctionObjects);
				final ManagedFunctionObjectModel managedFunctionObject = ((findManagedFunctionObject == null)
						? new ManagedFunctionObjectModel(objectName, objectKeyName, objectTypeName, false)
						: findManagedFunctionObject);
				targetObjectOrder[o] = managedFunctionObject;

				// Refactor details of object
				final String existingObjectName = managedFunctionObject.getObjectName();
				final String existingKeyName = managedFunctionObject.getKey();
				final String existingTypeName = managedFunctionObject.getObjectType();
				refactor.add(new AbstractChange<ManagedFunctionObjectModel>(managedFunctionObject,
						"Refactor managed function object") {
					@Override
					public void apply() {
						managedFunctionObject.setObjectName(objectName);
						managedFunctionObject.setKey(objectKeyName);
						managedFunctionObject.setObjectType(objectTypeName);
					}

					@Override
					public void revert() {
						managedFunctionObject.setObjectName(existingObjectName);
						managedFunctionObject.setKey(existingKeyName);
						managedFunctionObject.setObjectType(existingTypeName);
					}
				});
			}

			// Obtain the existing object order
			final ManagedFunctionObjectModel[] existingObjectOrder = managedFunction.getManagedFunctionObjects()
					.toArray(new ManagedFunctionObjectModel[0]);

			// Add changes to disconnect existing objects to be removed
			Set<ManagedFunctionObjectModel> targetObjects = new HashSet<ManagedFunctionObjectModel>(
					Arrays.asList(targetObjectOrder));
			for (ManagedFunctionObjectModel existingObject : existingObjectOrder) {
				if (!(targetObjects.contains(existingObject))) {
					// Add change to disconnect object
					final ManagedFunctionObjectModel functionObject = existingObject;
					refactor.add(new DisconnectChange<ManagedFunctionObjectModel>(existingObject) {
						@Override
						protected void populateRemovedConnections(List<ConnectionModel> connList) {
							SectionChangesImpl.this.removeManagedFunctionObjectConnections(functionObject, connList);
						}
					});
				}
			}

			// Add change to order the refactored objects
			refactor.add(
					new AbstractChange<ManagedFunctionModel>(managedFunction, "Refactor objects of managed function") {
						@Override
						public void apply() {
							// Remove existing objects, add target objects
							for (ManagedFunctionObjectModel object : existingObjectOrder) {
								managedFunction.removeManagedFunctionObject(object);
							}
							for (ManagedFunctionObjectModel object : targetObjectOrder) {
								managedFunction.addManagedFunctionObject(object);
							}
						}

						@Override
						public void revert() {
							// Remove the target objects, add back existing
							for (ManagedFunctionObjectModel object : targetObjectOrder) {
								managedFunction.removeManagedFunctionObject(object);
							}
							for (ManagedFunctionObjectModel object : existingObjectOrder) {
								managedFunction.addManagedFunctionObject(object);
							}
						}
					});

			// ---------------------- FunctionModel ------------------------

			// Refactor the functions of the managed function
			for (ManagedFunctionToFunctionModel managedFunctionToFunction : managedFunction.getFunctions()) {

				// Ensure have function for connection
				final FunctionModel function = managedFunctionToFunction.getFunction();
				if (function == null) {
					continue; // must have function
				}

				// Obtain details of function
				String functionName = function.getFunctionName();

				// --------------- FunctionFlowModel ------------------------

				// Function to be refactored, so obtain flow name mappings
				Map<String, String> flowTargetToExisting = functionToFlowNameMapping.get(functionName);
				if (flowTargetToExisting == null) {
					// Provide default empty map
					flowTargetToExisting = new HashMap<String, String>(0);
				}

				// Create the map of existing function flows to their names
				Map<String, FunctionFlowModel> existingFunctionFlows = new HashMap<String, FunctionFlowModel>();
				for (FunctionFlowModel functionFlow : function.getFunctionFlows()) {
					existingFunctionFlows.put(functionFlow.getFlowName(), functionFlow);
				}

				// Obtain the flows in order of type
				ManagedFunctionFlowType<?>[] flowTypes = functionType.getFlowTypes();
				final FunctionFlowModel[] targetFlowOrder = new FunctionFlowModel[flowTypes.length];
				for (int f = 0; f < targetFlowOrder.length; f++) {
					ManagedFunctionFlowType<?> flowType = flowTypes[f];

					// Obtain the details of the flow type
					final String flowName = flowType.getFlowName();
					Enum<?> flowKey = flowType.getKey();
					final String flowKeyName = (flowKey == null ? null : flowKey.name());
					Class<?> argumentType = flowType.getArgumentType();
					final String argumentTypeName = (argumentType == null ? null : argumentType.getName());

					// Obtain the flow for flow type (may need to create)
					FunctionFlowModel findFunctionFlow = this.getExistingItem(flowName, flowTargetToExisting,
							existingFunctionFlows);
					final FunctionFlowModel functionFlow = ((findFunctionFlow == null)
							? new FunctionFlowModel(flowName, flowKeyName, argumentTypeName)
							: findFunctionFlow);
					targetFlowOrder[f] = functionFlow;

					// Refactor details of flow
					final String existingFlowName = functionFlow.getFlowName();
					final String existingFlowKeyName = functionFlow.getKey();
					final String existingArgumentTypeName = functionFlow.getArgumentType();
					refactor.add(new AbstractChange<FunctionFlowModel>(functionFlow, "Refactor function flow") {
						@Override
						public void apply() {
							functionFlow.setFlowName(flowName);
							functionFlow.setKey(flowKeyName);
							functionFlow.setArgumentType(argumentTypeName);
						}

						@Override
						public void revert() {
							functionFlow.setFlowName(existingFlowName);
							functionFlow.setKey(existingFlowKeyName);
							functionFlow.setArgumentType(existingArgumentTypeName);
						}
					});
				}

				// Obtain the existing flow order
				final FunctionFlowModel[] existingFlowOrder = function.getFunctionFlows()
						.toArray(new FunctionFlowModel[0]);

				// Add changes to disconnect existing flows to be removed
				Set<FunctionFlowModel> targetFlows = new HashSet<FunctionFlowModel>(Arrays.asList(targetFlowOrder));
				for (FunctionFlowModel existingFunctionFlow : existingFlowOrder) {
					if (!(targetFlows.contains(existingFunctionFlow))) {
						// Add change to disconnect flow
						final FunctionFlowModel functionFlow = existingFunctionFlow;
						refactor.add(new DisconnectChange<FunctionFlowModel>(functionFlow) {
							@Override
							protected void populateRemovedConnections(List<ConnectionModel> connList) {
								SectionChangesImpl.this.removeFunctionFlowConnections(functionFlow, connList);
							}
						});
					}
				}

				// Add change to order the refactored flows
				refactor.add(new AbstractChange<FunctionModel>(function, "Refactor function flows") {
					@Override
					public void apply() {
						// Remove existing flows, add target flows
						for (FunctionFlowModel flow : existingFlowOrder) {
							function.removeFunctionFlow(flow);
						}
						for (FunctionFlowModel flow : targetFlowOrder) {
							function.addFunctionFlow(flow);
						}
					}

					@Override
					public void revert() {
						// Remove target flows, add back existing flows
						for (FunctionFlowModel flow : targetFlowOrder) {
							function.removeFunctionFlow(flow);
						}
						for (FunctionFlowModel flow : existingFlowOrder) {
							function.addFunctionFlow(flow);
						}
					}
				});

				// --------------- FunctionEscalationModel ------------------

				// Function to be refactored, so obtain escalation name mappings
				Map<String, String> escalationTargetToExisting = functionToEscalationTypeMapping.get(functionName);
				if (escalationTargetToExisting == null) {
					// Provide default empty map
					escalationTargetToExisting = new HashMap<String, String>(0);
				}

				// Create the map of existing function escalations to their
				// names
				Map<String, FunctionEscalationModel> existingFunctionEscalations = new HashMap<String, FunctionEscalationModel>();
				for (FunctionEscalationModel functionEscalation : function.getFunctionEscalations()) {
					existingFunctionEscalations.put(functionEscalation.getEscalationType(), functionEscalation);
				}

				// Obtain the escalations in order of type
				ManagedFunctionEscalationType[] escalationTypes = functionType.getEscalationTypes();
				final FunctionEscalationModel[] targetEscalationOrder = new FunctionEscalationModel[escalationTypes.length];
				for (int e = 0; e < targetEscalationOrder.length; e++) {
					ManagedFunctionEscalationType escalationType = escalationTypes[e];

					// Obtain details of the escalation type
					final String escalationTypeName = escalationType.getEscalationType().getName();

					// Obtain the escalation for escalation type (may create)
					FunctionEscalationModel findFunctionEscalation = this.getExistingItem(escalationTypeName,
							escalationTargetToExisting, existingFunctionEscalations);
					final FunctionEscalationModel functionEscalation = ((findFunctionEscalation == null)
							? new FunctionEscalationModel(escalationTypeName)
							: findFunctionEscalation);
					targetEscalationOrder[e] = functionEscalation;

					// Refactor details of escalation
					final String existingEscalationTypeName = functionEscalation.getEscalationType();
					refactor.add(new AbstractChange<FunctionEscalationModel>(functionEscalation,
							"Refactor function escalation") {
						@Override
						public void apply() {
							functionEscalation.setEscalationType(escalationTypeName);
						}

						@Override
						public void revert() {
							functionEscalation.setEscalationType(existingEscalationTypeName);
						}
					});
				}

				// Obtain the existing escalation order
				final FunctionEscalationModel[] existingEscalationOrder = function.getFunctionEscalations()
						.toArray(new FunctionEscalationModel[0]);

				// Add changes to disconnect existing escalations to be removed
				Set<FunctionEscalationModel> targetEscalations = new HashSet<FunctionEscalationModel>(
						Arrays.asList(targetEscalationOrder));
				for (FunctionEscalationModel existingEscalation : existingEscalationOrder) {
					if (!(targetEscalations.contains(existingEscalation))) {
						// Add change to disconnect escalation
						final FunctionEscalationModel functionEscalation = existingEscalation;
						refactor.add(new DisconnectChange<FunctionEscalationModel>(functionEscalation) {
							@Override
							protected void populateRemovedConnections(List<ConnectionModel> connList) {
								SectionChangesImpl.this.removeFunctionEscalationConnections(functionEscalation,
										connList);
							}
						});
					}
				}

				// Add change to order the refactored escalations
				refactor.add(new AbstractChange<FunctionModel>(function, "Refactor function escalations") {
					@Override
					public void apply() {
						// Remove existing escalations, add target escalations
						for (FunctionEscalationModel escalation : existingEscalationOrder) {
							function.removeFunctionEscalation(escalation);
						}
						for (FunctionEscalationModel escalation : targetEscalationOrder) {
							function.addFunctionEscalation(escalation);
						}
					}

					@Override
					public void revert() {
						// Remove target escalations, add back existing
						for (FunctionEscalationModel escalation : targetEscalationOrder) {
							function.removeFunctionEscalation(escalation);
						}
						for (FunctionEscalationModel escalation : existingEscalationOrder) {
							function.addFunctionEscalation(escalation);
						}
					}
				});
			}
		}

		// ------ ManagedFunctionModel / FunctionModel (continued) ------

		// Obtain the target managed function order
		final ManagedFunctionModel[] targetFunctionOrder = targetFunctionList.toArray(new ManagedFunctionModel[0]);

		// Obtain existing managed function order
		final ManagedFunctionModel[] existingFunctionOrder = namespaceModel.getManagedFunctions()
				.toArray(new ManagedFunctionModel[0]);

		// Add changes to disconnect existing functions to be removed
		Set<ManagedFunctionModel> targetFunctions = new HashSet<ManagedFunctionModel>(
				Arrays.asList(targetFunctionOrder));
		for (ManagedFunctionModel existingFunction : existingFunctionOrder) {
			if (!(targetFunctions.contains(existingFunction))) {
				final ManagedFunctionModel managedFunction = existingFunction;

				// Add change to disconnect managed function (and its functions)
				refactor.add(new DisconnectChange<ManagedFunctionModel>(managedFunction) {
					@Override
					protected void populateRemovedConnections(List<ConnectionModel> connList) {
						SectionChangesImpl.this.removeManagedFunctionConnections(managedFunction, connList);
					}
				});

				// Add change to remove functions of managed function
				refactor.add(new AbstractChange<ManagedFunctionModel>(managedFunction,
						"Remove functions of managed function") {

					/**
					 * Removed {@link FunctionModel} instances.
					 */
					private List<FunctionModel> functions;

					@Override
					public void apply() {
						this.functions = new LinkedList<FunctionModel>();
						SectionChangesImpl.this.removeManagedFunction(managedFunction, this.functions);
					}

					@Override
					public void revert() {
						// Add back the function models
						for (FunctionModel function : this.functions) {
							SectionChangesImpl.this.section.addFunction(function);
						}
					}
				});
			}
		}

		// Add change to order the new functions
		refactor.add(new AbstractChange<FunctionNamespaceModel>(namespaceModel, "Refactor functions of namespace") {
			@Override
			public void apply() {
				// Remove existing functions, add target functions
				for (ManagedFunctionModel function : existingFunctionOrder) {
					namespaceModel.removeManagedFunction(function);
				}
				for (ManagedFunctionModel function : targetFunctionOrder) {
					namespaceModel.addManagedFunction(function);
				}
			}

			@Override
			public void revert() {
				// Remove the target functions, add back existing
				for (ManagedFunctionModel function : targetFunctionOrder) {
					namespaceModel.removeManagedFunction(function);
				}
				for (ManagedFunctionModel function : existingFunctionOrder) {
					namespaceModel.addManagedFunction(function);
				}
			}
		});

		// Return change to do all the refactoring
		return new AbstractChange<FunctionNamespaceModel>(namespaceModel, "Refactor namespace") {
			@Override
			public void apply() {
				for (Change<?> change : refactor) {
					change.apply();
				}
			}

			@Override
			public void revert() {
				// Revert changes in reverse order as applied
				for (int i = (refactor.size() - 1); i >= 0; i--) {
					Change<?> change = refactor.get(i);
					change.revert();
				}
			}
		};
	}

	/**
	 * Obtains the existing item for the target name.
	 *
	 * @param targetItemName
	 *            Target item name.
	 * @param targetToExistingName
	 *            Mapping of target item name to existing item name.
	 * @param existingNameToItem
	 *            Mapping of existing item name to the existing item.
	 */
	private <T> T getExistingItem(String targetItemName, Map<String, String> targetToExistingName,
			Map<String, T> existingNameToItem) {

		// Obtain the existing item name
		String existingItemName = targetToExistingName.get(targetItemName);
		if (existingItemName != null) {
			// Have existing name, so return existing item by name
			return existingNameToItem.get(existingItemName);
		} else {
			// No existing name, so no existing item
			return null;
		}
	}

	@Override
	public <M extends Enum<M>, F extends Enum<F>> Change<ManagedFunctionModel> addManagedFunction(
			final FunctionNamespaceModel namespaceModel, ManagedFunctionType<M, F> functionType) {

		// Ensure the managed function is not already added
		String functionName = functionType.getFunctionName();
		for (ManagedFunctionModel managedFunction : namespaceModel.getManagedFunctions()) {
			if (functionName.equals(functionName)) {
				// Function already added
				return new NoChange<ManagedFunctionModel>(managedFunction, "Add managed function " + functionName,
						"Function " + functionName + " already added to namespace "
								+ namespaceModel.getFunctionNamespaceName());
			}
		}

		// Create the managed function model
		final ManagedFunctionModel managedFunction = SectionChangesImpl.this.createManagedFunctionModel(functionType);

		// Return the add managed function change
		return new AbstractChange<ManagedFunctionModel>(managedFunction, "Add managed function " + functionName) {
			@Override
			public void apply() {
				// Add managed function (ensuring managed functions sorted)
				namespaceModel.addManagedFunction(managedFunction);
				SectionChangesImpl.sortManagedFunctionModels(namespaceModel.getManagedFunctions());
			}

			@Override
			public void revert() {
				// Remove managed function (should already be sorted)
				namespaceModel.removeManagedFunction(managedFunction);
			}
		};
	}

	@Override
	public Change<ManagedFunctionModel> removeManagedFunction(final FunctionNamespaceModel namespace,
			final ManagedFunctionModel managedFunction) {

		// Ensure managed function on namespace
		boolean isOnNamespace = false;
		for (ManagedFunctionModel managedFunctionModel : namespace.getManagedFunctions()) {
			if (managedFunctionModel == managedFunction) {
				isOnNamespace = true;
			}
		}
		if (!isOnNamespace) {
			// Managed function not on namespace
			return new NoChange<ManagedFunctionModel>(managedFunction,
					"Remove managed function " + managedFunction.getManagedFunctionName(),
					"Managed function " + managedFunction.getManagedFunctionName() + " not on namespace "
							+ namespace.getFunctionNamespaceName());
		}

		// Return the remove managed function change
		return new AbstractChange<ManagedFunctionModel>(managedFunction,
				"Remove managed function " + managedFunction.getManagedFunctionName()) {

			/**
			 * Removed {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			/**
			 * Removed {@link FunctionModel} instances.
			 */
			private FunctionModel[] functions;

			@Override
			public void apply() {
				// Remove the connections
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				SectionChangesImpl.this.removeManagedFunctionConnections(managedFunction, connList);
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the functions of the managed function
				List<FunctionModel> functionList = new LinkedList<FunctionModel>();
				SectionChangesImpl.this.removeManagedFunction(managedFunction, functionList);
				this.functions = functionList.toArray(new FunctionModel[0]);

				// Remove the managed function
				namespace.removeManagedFunction(managedFunction);
			}

			@Override
			public void revert() {
				// Add the managed function (ensuring sorted)
				namespace.addManagedFunction(managedFunction);
				SectionChangesImpl.sortManagedFunctionModels(namespace.getManagedFunctions());

				// Add the functions (in reverse order, ensuring sorted)
				for (int i = (this.functions.length - 1); i >= 0; i--) {
					SectionChangesImpl.this.section.addFunction(this.functions[i]);
				}
				SectionChangesImpl.this.sortFunctionModels();

				// Reconnect connections
				for (ConnectionModel connection : this.connections) {
					connection.connect();
				}
			}
		};
	}

	@Override
	public <M extends Enum<M>, F extends Enum<F>> Change<FunctionModel> addFunction(String functionName,
			final ManagedFunctionModel managedFunction, ManagedFunctionType<M, F> functionType) {

		// Create the function model
		Class<?> returnType = functionType.getReturnType();
		final FunctionModel function = new FunctionModel(functionName, false, null,
				managedFunction.getManagedFunctionName(), (returnType != null ? returnType.getName() : null));
		for (ManagedFunctionFlowType<?> flowType : functionType.getFlowTypes()) {
			Enum<?> key = flowType.getKey();
			Class<?> argumentType = flowType.getArgumentType();
			FunctionFlowModel functionFlow = new FunctionFlowModel(flowType.getFlowName(),
					(key != null ? key.name() : null), (argumentType != null ? argumentType.getName() : null));
			function.addFunctionFlow(functionFlow);
		}
		for (ManagedFunctionEscalationType escalationType : functionType.getEscalationTypes()) {
			FunctionEscalationModel functionEscalation = new FunctionEscalationModel(
					escalationType.getEscalationType().getName());
			function.addFunctionEscalation(functionEscalation);
		}

		// Ensure the managed function is on the section and obtain its
		// namespace
		FunctionNamespaceModel namespace = null;
		for (FunctionNamespaceModel namespaceModel : this.section.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunctionModel : namespaceModel.getManagedFunctions()) {
				if (managedFunctionModel == managedFunction) {
					// On the section
					namespace = namespaceModel;
				}
			}
		}
		if (namespace == null) {
			// Managed function not in section so can not add function
			return new NoChange<FunctionModel>(function, "Add function " + functionName,
					"Managed function " + managedFunction.getManagedFunctionName() + " not in section");
		}

		// Specify the namespace name of the function (now that have namespace)
		function.setFunctionNamespaceName(namespace.getFunctionNamespaceName());

		// Ensure the managed function for the function type
		if (!managedFunction.getManagedFunctionName().equals(functionType.getFunctionName())) {
			// Not correct function type for the managed function
			return new NoChange<FunctionModel>(function, "Add function " + functionName,
					"Function type " + functionType.getFunctionName() + " does not match managed function "
							+ managedFunction.getManagedFunctionName());
		}

		// Create the connection from managed function to function
		final ManagedFunctionToFunctionModel conn = new ManagedFunctionToFunctionModel(function, managedFunction);

		// Return the change to add the function
		return new AbstractChange<FunctionModel>(function, "Add function " + functionName) {
			@Override
			public void apply() {
				// Add the function ensuring ordering
				SectionChangesImpl.this.section.addFunction(function);
				SectionChangesImpl.this.sortFunctionModels();

				// Connect managed function to the function (ensuring ordering)
				conn.connect();
				SectionChangesImpl.sortManagedFunctionToFunctionConnections(managedFunction.getFunctions());
			}

			@Override
			public void revert() {
				// Disconnect managed function from the function
				conn.remove();

				// Remove function (should maintain ordering)
				SectionChangesImpl.this.section.removeFunction(function);
			}
		};
	}

	@Override
	public Change<FunctionModel> removeFunction(final FunctionModel function) {

		// Ensure the function is on the section
		boolean isOnSection = false;
		for (FunctionModel functionModel : this.section.getFunctions()) {
			if (function == functionModel) {
				isOnSection = true; // function in section
			}
		}
		if (!isOnSection) {
			// Not in section so can not remove it
			return new NoChange<FunctionModel>(function, "Remove function " + function.getFunctionName(),
					"Function " + function.getFunctionName() + " not in section");
		}

		// Create change to remove the function
		return new AbstractChange<FunctionModel>(function, "Remove function " + function.getFunctionName()) {

			/**
			 * {@link ConnectionModel} instances removed.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {
				// Remove connections to the function
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				SectionChangesImpl.this.removeFunctionConnections(function, connList);

				// Remove connection to managed function
				ManagedFunctionToFunctionModel managedFunctionConn = function.getManagedFunction();
				if (managedFunctionConn != null) {
					managedFunctionConn.remove();
					connList.add(managedFunctionConn);
				}

				// Store for revert
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the function (should maintain order)
				SectionChangesImpl.this.section.removeFunction(function);
			}

			@Override
			public void revert() {
				// Add function back in (ensuring order)
				SectionChangesImpl.this.section.addFunction(function);
				SectionChangesImpl.this.sortFunctionModels();

				// Reconnect connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}

				// Ensure managed function connections sorted by function name
				SectionChangesImpl.sortManagedFunctionToFunctionConnections(
						function.getManagedFunction().getManagedFunction().getFunctions());
			}
		};
	}

	@Override
	public Change<FunctionModel> renameFunction(final FunctionModel function, final String newFunctionName) {

		// Ensure the function is on the section
		boolean isOnSection = false;
		for (FunctionModel functionModel : SectionChangesImpl.this.section.getFunctions()) {
			if (function == functionModel) {
				isOnSection = true; // function in section
			}
		}
		if (!isOnSection) {
			// Can not remove function as not in section
			return new NoChange<FunctionModel>(function,
					"Rename function " + function.getFunctionName() + " to " + newFunctionName,
					"Function " + function.getFunctionName() + " not in section");
		}

		// Maintain old function name for revert
		final String oldFunctionName = function.getFunctionName();

		// Return rename change
		return new AbstractChange<FunctionModel>(function,
				"Rename function " + oldFunctionName + " to " + newFunctionName) {
			@Override
			public void apply() {
				// Rename function (ensuring ordering)
				function.setFunctionName(newFunctionName);
				SectionChangesImpl.this.sortFunctionModels();
				ManagedFunctionToFunctionModel conn = function.getManagedFunction();
				if (conn != null) {
					SectionChangesImpl
							.sortManagedFunctionToFunctionConnections(conn.getManagedFunction().getFunctions());
				}
			}

			@Override
			public void revert() {
				// Revert to old function name (ensuring ordering)
				function.setFunctionName(oldFunctionName);
				SectionChangesImpl.this.sortFunctionModels();
				ManagedFunctionToFunctionModel conn = function.getManagedFunction();
				if (conn != null) {
					SectionChangesImpl
							.sortManagedFunctionToFunctionConnections(conn.getManagedFunction().getFunctions());
				}
			}
		};
	}

	@Override
	public Change<ManagedFunctionObjectModel> setObjectAsParameter(boolean isParameter,
			final ManagedFunctionObjectModel functionObject) {

		// Ensure the function object on the section
		boolean isOnSection = false;
		for (FunctionNamespaceModel namespace : SectionChangesImpl.this.section.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : namespace.getManagedFunctions()) {
				for (ManagedFunctionObjectModel functionObjectModel : managedFunction.getManagedFunctionObjects()) {
					if (functionObject == functionObjectModel) {
						isOnSection = true; // on the section
					}
				}
			}
		}
		if (!isOnSection) {
			// Not in section so can not set as parameter
			return new NoChange<ManagedFunctionObjectModel>(functionObject,
					"Set managed function object " + functionObject.getObjectName() + " as "
							+ (isParameter ? "a parameter" : "an object"),
					"Managed function object " + functionObject.getObjectName() + " not in section");
		}

		// Return the appropriate change
		if (isParameter) {
			// Return change to set as parameter
			final ManagedFunctionObjectToExternalManagedObjectModel conn = functionObject.getExternalManagedObject();
			return new AbstractChange<ManagedFunctionObjectModel>(functionObject,
					"Set managed function object " + functionObject.getObjectName() + " as a parameter") {
				@Override
				public void apply() {
					// Remove possible connection to external managed object
					if (conn != null) {
						conn.remove();
					}

					// Flag as parameter
					functionObject.setIsParameter(true);
				}

				@Override
				public void revert() {
					// Flag as object
					functionObject.setIsParameter(false);

					// Reconnect to possible external managed object
					if (conn != null) {
						conn.connect();
					}
				}
			};
		} else {
			// Return change to set as object
			return new AbstractChange<ManagedFunctionObjectModel>(functionObject,
					"Set managed function object " + functionObject.getObjectName() + " as an object") {

				@Override
				public void apply() {
					// Flag as object (no connection as parameter)
					functionObject.setIsParameter(false);
				}

				@Override
				public void revert() {
					// Flag back as parameter
					functionObject.setIsParameter(true);
				}

			};
		}
	}

	@Override
	public Change<FunctionModel> setFunctionAsPublic(final boolean isPublic, final FunctionModel function) {

		// Ensure function in section
		boolean isOnSection = false;
		for (FunctionModel functionModel : SectionChangesImpl.this.section.getFunctions()) {
			if (function == functionModel) {
				isOnSection = true; // function in section
			}
		}
		if (!isOnSection) {
			// Function not in section so can not make public
			return new NoChange<FunctionModel>(function,
					"Set function " + function.getFunctionName() + (isPublic ? " public" : " private"),
					"Function " + function.getFunctionName() + " not in section");
		}

		// Return the change
		return new AbstractChange<FunctionModel>(function,
				"Set function " + function.getFunctionName() + (isPublic ? " public" : " private")) {
			@Override
			public void apply() {
				// Specify public/private
				function.setIsPublic(isPublic);
			}

			@Override
			public void revert() {
				// Revert public/private
				function.setIsPublic(!isPublic);
			}
		};
	}

	@Override
	public Change<SubSectionModel> addSubSection(String subSectionName, String sectionSourceClassName,
			String sectionLocation, PropertyList properties, SectionType sectionType) {

		// Create the sub section model
		final SubSectionModel subSection = new SubSectionModel(subSectionName, sectionSourceClassName, sectionLocation);

		// Add the properties in the order defined
		for (Property property : properties) {
			subSection.addProperty(new PropertyModel(property.getName(), property.getValue()));
		}

		// Add the inputs ensuring ordering
		for (SectionInputType inputType : sectionType.getSectionInputTypes()) {
			subSection.addSubSectionInput(new SubSectionInputModel(inputType.getSectionInputName(),
					inputType.getParameterType(), false, null));
		}
		Collections.sort(subSection.getSubSectionInputs(), new Comparator<SubSectionInputModel>() {
			@Override
			public int compare(SubSectionInputModel a, SubSectionInputModel b) {
				return a.getSubSectionInputName().compareTo(b.getSubSectionInputName());
			}
		});

		// Add the outputs ensuring ordering
		for (SectionOutputType outputType : sectionType.getSectionOutputTypes()) {
			subSection.addSubSectionOutput(new SubSectionOutputModel(outputType.getSectionOutputName(),
					outputType.getArgumentType(), outputType.isEscalationOnly()));
		}
		Collections.sort(subSection.getSubSectionOutputs(), new Comparator<SubSectionOutputModel>() {
			@Override
			public int compare(SubSectionOutputModel a, SubSectionOutputModel b) {
				return a.getSubSectionOutputName().compareTo(b.getSubSectionOutputName());
			}
		});

		// Add the objects ensuring ordering
		for (SectionObjectType objectType : sectionType.getSectionObjectTypes()) {
			subSection.addSubSectionObject(
					new SubSectionObjectModel(objectType.getSectionObjectName(), objectType.getObjectType()));
		}
		Collections.sort(subSection.getSubSectionObjects(), new Comparator<SubSectionObjectModel>() {
			@Override
			public int compare(SubSectionObjectModel a, SubSectionObjectModel b) {
				return a.getSubSectionObjectName().compareTo(b.getSubSectionObjectName());
			}
		});

		// Create the change to add the sub section
		return new AbstractChange<SubSectionModel>(subSection, "Add sub section " + subSectionName) {
			@Override
			public void apply() {
				// Add the sub section (ensuring ordering)
				SectionChangesImpl.this.section.addSubSection(subSection);
				SectionChangesImpl.this.sortSubSections();
			}

			@Override
			public void revert() {
				// Remove the sub section (should maintain order)
				SectionChangesImpl.this.section.removeSubSection(subSection);
			}
		};
	}

	@Override
	public Change<SubSectionModel> removeSubSection(final SubSectionModel subSection) {

		// Ensure the sub section in the section
		boolean isInSection = false;
		for (SubSectionModel subSectionModel : this.section.getSubSections()) {
			if (subSection == subSectionModel) {
				isInSection = true;
			}
		}
		if (!isInSection) {
			// No change as not in section
			return new NoChange<SubSectionModel>(subSection, "Remove sub section " + subSection.getSubSectionName(),
					"Sub section " + subSection.getSubSectionName() + " not in section");
		}

		// Return the change to remove the sub section
		return new AbstractChange<SubSectionModel>(subSection, "Remove sub section " + subSection.getSubSectionName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections to the sub section
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				for (SubSectionInputModel input : subSection.getSubSectionInputs()) {
					for (SubSectionOutputToSubSectionInputModel conn : input.getSubSectionOutputs()) {
						conn.remove();
						connList.add(conn);
					}
				}
				for (SubSectionOutputModel output : subSection.getSubSectionOutputs()) {
					SubSectionOutputToSubSectionInputModel outputToInput = output.getSubSectionInput();
					if (outputToInput != null) {
						outputToInput.remove();
						connList.add(outputToInput);
					}
					SubSectionOutputToExternalFlowModel outputToExtFlow = output.getExternalFlow();
					if (outputToExtFlow != null) {
						outputToExtFlow.remove();
						connList.add(outputToExtFlow);
					}
				}
				for (SubSectionObjectModel object : subSection.getSubSectionObjects()) {
					SubSectionObjectToExternalManagedObjectModel conn = object.getExternalManagedObject();
					if (conn != null) {
						conn.remove();
						connList.add(conn);
					}
				}
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the sub section (should maintain order)
				SectionChangesImpl.this.section.removeSubSection(subSection);
			}

			@Override
			public void revert() {
				// Add the sub section (ensuring order)
				SectionChangesImpl.this.section.addSubSection(subSection);
				SectionChangesImpl.this.sortSubSections();

				// Reconnect the connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}
			}
		};
	}

	@Override
	public Change<SubSectionModel> renameSubSection(final SubSectionModel subSection, final String newSubSectionName) {

		// Ensure the sub section in the section
		boolean isInSection = false;
		for (SubSectionModel subSectionModel : this.section.getSubSections()) {
			if (subSection == subSectionModel) {
				isInSection = true;
			}
		}
		if (!isInSection) {
			// No change as not in section
			return new NoChange<SubSectionModel>(subSection,
					"Rename sub section " + subSection.getSubSectionName() + " to " + newSubSectionName,
					"Sub section " + subSection.getSubSectionName() + " not in section");
		}

		// Maintain the old sub section name
		final String oldSubSectionName = subSection.getSubSectionName();

		// Return change to rename the sub section
		return new AbstractChange<SubSectionModel>(subSection,
				"Rename sub section " + subSection.getSubSectionName() + " to " + newSubSectionName) {
			@Override
			public void apply() {
				// Rename the sub section (ensuring order)
				subSection.setSubSectionName(newSubSectionName);
				SectionChangesImpl.this.sortSubSections();
			}

			@Override
			public void revert() {
				// Revert to old name (ensuring order)
				subSection.setSubSectionName(oldSubSectionName);
				SectionChangesImpl.this.sortSubSections();
			}
		};
	}

	@Override
	public Change<SubSectionInputModel> setSubSectionInputPublic(final boolean isPublic, final String publicName,
			final SubSectionInputModel input) {

		// Ensure sub section input in the section
		boolean isInSection = false;
		for (SubSectionModel subSection : this.section.getSubSections()) {
			for (SubSectionInputModel inputModel : subSection.getSubSectionInputs()) {
				if (input == inputModel) {
					isInSection = true;
				}
			}
		}
		if (!isInSection) {
			// Not in section so can not change
			return new NoChange<SubSectionInputModel>(input,
					"Set sub section input " + input.getSubSectionInputName() + " " + (isPublic ? "public" : "private"),
					"Sub section input " + input.getSubSectionInputName() + " not in section");
		}

		// Maintain old values
		final boolean oldIsPublic = input.getIsPublic();
		final String oldPublicInputName = input.getPublicInputName();

		// Return the change to set input public/private
		return new AbstractChange<SubSectionInputModel>(input,
				"Set sub section input " + input.getSubSectionInputName() + " " + (isPublic ? "public" : "private")) {
			@Override
			public void apply() {
				// Set public/private (only provide name if public)
				input.setIsPublic(isPublic);
				input.setPublicInputName(isPublic ? publicName : null);
			}

			@Override
			public void revert() {
				// Revert to old values
				input.setIsPublic(oldIsPublic);
				input.setPublicInputName(oldPublicInputName);
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> addExternalFlow(String externalFlowName, String argumentType) {

		// Create the external flow
		final ExternalFlowModel externalFlow = new ExternalFlowModel(externalFlowName, argumentType);

		// Return the change to add the external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow, "Add external flow " + externalFlowName) {
			@Override
			public void apply() {
				// Add the external flow (ensuring ordering)
				SectionChangesImpl.this.section.addExternalFlow(externalFlow);
				SectionChangesImpl.this.sortExternalFlows();
			}

			@Override
			public void revert() {
				// Remove the external flow (should maintain order)
				SectionChangesImpl.this.section.removeExternalFlow(externalFlow);
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> removeExternalFlow(final ExternalFlowModel externalFlow) {

		// Ensure the external flow is in the section
		boolean isInSection = false;
		for (ExternalFlowModel externalFlowModel : this.section.getExternalFlows()) {
			if (externalFlow == externalFlowModel) {
				isInSection = true;
			}
		}
		if (!isInSection) {
			// No change as not in the section
			return new NoChange<ExternalFlowModel>(externalFlow,
					"Remove external flow " + externalFlow.getExternalFlowName(),
					"External flow " + externalFlow.getExternalFlowName() + " not in section");
		}

		// Return change to remove the external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow,
				"Remove external flow " + externalFlow.getExternalFlowName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections to the external flow
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				for (SubSectionOutputToExternalFlowModel conn : new ArrayList<SubSectionOutputToExternalFlowModel>(
						externalFlow.getSubSectionOutputs())) {
					conn.remove();
					connList.add(conn);
				}
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the external flow (should maintain order)
				SectionChangesImpl.this.section.removeExternalFlow(externalFlow);
			}

			@Override
			public void revert() {
				// Add the external flow (ensuring order)
				SectionChangesImpl.this.section.addExternalFlow(externalFlow);
				SectionChangesImpl.this.sortExternalFlows();

				// Reconnect the connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> renameExternalFlow(final ExternalFlowModel externalFlow,
			final String newExternalFlowName) {

		// TODO test this method (renameExternalFlow)

		// Obtain the old name
		final String oldExternalFlowName = externalFlow.getExternalFlowName();

		// Return change to rename the external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow, "Rename exernal flow to " + newExternalFlowName) {
			@Override
			public void apply() {
				externalFlow.setExternalFlowName(newExternalFlowName);
			}

			@Override
			public void revert() {
				externalFlow.setExternalFlowName(oldExternalFlowName);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> addExternalManagedObject(String externalManagedObjectName,
			String objectType) {

		// Create the external managed object
		final ExternalManagedObjectModel extMo = new ExternalManagedObjectModel(externalManagedObjectName, objectType);

		// Return change to add the external managed object
		return new AbstractChange<ExternalManagedObjectModel>(extMo,
				"Add external managed object " + externalManagedObjectName) {
			@Override
			public void apply() {
				// Add the external managed object (ensuring order)
				SectionChangesImpl.this.section.addExternalManagedObject(extMo);
				SectionChangesImpl.this.sortExternalManagedObjects();
			}

			@Override
			public void revert() {
				// Remove the external managed object (should maintain order)
				SectionChangesImpl.this.section.removeExternalManagedObject(extMo);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject) {

		// Ensure the external managed object in the section
		boolean isInSection = false;
		for (ExternalManagedObjectModel externalMoModel : SectionChangesImpl.this.section.getExternalManagedObjects()) {
			if (externalManagedObject == externalMoModel) {
				isInSection = true;
			}
		}
		if (!isInSection) {
			// No change as not in section
			return new NoChange<ExternalManagedObjectModel>(externalManagedObject,
					"Remove external managed object " + externalManagedObject.getExternalManagedObjectName(),
					"External managed object " + externalManagedObject.getExternalManagedObjectName()
							+ " not in section");
		}

		// Return change to remove the external managed object
		return new AbstractChange<ExternalManagedObjectModel>(externalManagedObject,
				"Remove external managed object " + externalManagedObject.getExternalManagedObjectName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove the connections
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				for (SubSectionObjectToExternalManagedObjectModel conn : new ArrayList<SubSectionObjectToExternalManagedObjectModel>(
						externalManagedObject.getSubSectionObjects())) {
					conn.remove();
					connList.add(conn);
				}
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the external managed object (should maintain order)
				SectionChangesImpl.this.section.removeExternalManagedObject(externalManagedObject);
			}

			@Override
			public void revert() {
				// Add the external managed object (ensuring order)
				SectionChangesImpl.this.section.addExternalManagedObject(externalManagedObject);
				SectionChangesImpl.this.sortExternalManagedObjects();

				// Reconnect the connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> renameExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject, final String newExternalManagedObjectName) {

		// TODO test this method (renameExternalManagedObject)

		// Obtain the old name
		final String oldExternalManagedObjectName = externalManagedObject.getExternalManagedObjectName();

		// Return change to rename the external managed object
		return new AbstractChange<ExternalManagedObjectModel>(externalManagedObject,
				"Rename exernal managed object to " + newExternalManagedObjectName) {
			@Override
			public void apply() {
				externalManagedObject.setExternalManagedObjectName(newExternalManagedObjectName);
			}

			@Override
			public void revert() {
				externalManagedObject.setExternalManagedObjectName(oldExternalManagedObjectName);
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceModel> addSectionManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties, long timeout,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addSectionManagedObjectSource)

		// Create the managed object source
		final SectionManagedObjectSourceModel managedObjectSource = new SectionManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSourceClassName, managedObjectType.getObjectType().getName(),
				String.valueOf(timeout));
		for (Property property : properties) {
			managedObjectSource.addProperty(new PropertyModel(property.getName(), property.getValue()));
		}

		// Add the flows for the managed object source
		for (ManagedObjectFlowType<?> flow : managedObjectType.getFlowTypes()) {
			managedObjectSource.addSectionManagedObjectSourceFlow(
					new SectionManagedObjectSourceFlowModel(flow.getFlowName(), flow.getArgumentType().getName()));
		}

		// Return the change to add the managed object source
		return new AbstractChange<SectionManagedObjectSourceModel>(managedObjectSource, "Add managed object source") {
			@Override
			public void apply() {
				SectionChangesImpl.this.section.addSectionManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				SectionChangesImpl.this.section.removeSectionManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceModel> removeSectionManagedObjectSource(
			final SectionManagedObjectSourceModel managedObjectSource) {

		// TODO test this method (removeSectionManagedObjectSource)

		// Return change to remove the managed object source
		return new AbstractChange<SectionManagedObjectSourceModel>(managedObjectSource,
				"Remove managed object source") {
			@Override
			public void apply() {
				SectionChangesImpl.this.section.removeSectionManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				SectionChangesImpl.this.section.addSectionManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceModel> renameSectionManagedObjectSource(
			final SectionManagedObjectSourceModel managedObjectSource, final String newManagedObjectSourceName) {

		// TODO test this method (renameSectionManagedObjectSource)

		// Obtain the old managed object source name
		final String oldManagedObjectSourceName = managedObjectSource.getSectionManagedObjectSourceName();

		// Return change to rename the managed object source
		return new AbstractChange<SectionManagedObjectSourceModel>(managedObjectSource,
				"Rename managed object source to " + newManagedObjectSourceName) {
			@Override
			public void apply() {
				managedObjectSource.setSectionManagedObjectSourceName(newManagedObjectSourceName);
			}

			@Override
			public void revert() {
				managedObjectSource.setSectionManagedObjectSourceName(oldManagedObjectSourceName);
			}
		};
	}

	@Override
	public Change<SectionManagedObjectModel> addSectionManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope, SectionManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addSectionManagedObject)

		// Create the managed object
		final SectionManagedObjectModel managedObject = new SectionManagedObjectModel(managedObjectName,
				getManagedObjectScope(managedObjectScope));

		// Add the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependency : managedObjectType.getDependencyTypes()) {
			managedObject.addSectionManagedObjectDependency(new SectionManagedObjectDependencyModel(
					dependency.getDependencyName(), dependency.getDependencyType().getName()));
		}

		// Create connection to the managed object source
		final SectionManagedObjectToSectionManagedObjectSourceModel conn = new SectionManagedObjectToSectionManagedObjectSourceModel();
		conn.setSectionManagedObject(managedObject);
		conn.setSectionManagedObjectSource(managedObjectSource);

		// Return change to add the managed object
		return new AbstractChange<SectionManagedObjectModel>(managedObject, "Add managed object") {
			@Override
			public void apply() {
				SectionChangesImpl.this.section.addSectionManagedObject(managedObject);
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
				SectionChangesImpl.this.section.removeSectionManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<SectionManagedObjectModel> removeSectionManagedObject(final SectionManagedObjectModel managedObject) {

		// TODO test this method (removeSectionManagedObject)

		// Return change to remove the managed object
		return new AbstractChange<SectionManagedObjectModel>(managedObject, "Remove managed object") {
			@Override
			public void apply() {
				SectionChangesImpl.this.section.removeSectionManagedObject(managedObject);
			}

			@Override
			public void revert() {
				SectionChangesImpl.this.section.addSectionManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<SectionManagedObjectModel> renameSectionManagedObject(final SectionManagedObjectModel managedObject,
			final String newManagedObjectName) {

		// TODO test this method (renameSectionManagedObject)

		// Obtain the old managed object name
		final String oldManagedObjectName = managedObject.getSectionManagedObjectName();

		// Return change to rename the managed object
		return new AbstractChange<SectionManagedObjectModel>(managedObject,
				"Rename managed object to " + newManagedObjectName) {
			@Override
			public void apply() {
				managedObject.setSectionManagedObjectName(newManagedObjectName);
			}

			@Override
			public void revert() {
				managedObject.setSectionManagedObjectName(oldManagedObjectName);
			}
		};
	}

	@Override
	public Change<SectionManagedObjectModel> rescopeSectionManagedObject(final SectionManagedObjectModel managedObject,
			final ManagedObjectScope newManagedObjectScope) {

		// TODO test this method (rescopeSectionManagedObject)

		// Obtain the new scope text
		final String newScope = getManagedObjectScope(newManagedObjectScope);

		// OBtain the old managed object scope
		final String oldScope = managedObject.getManagedObjectScope();

		// Return change to re-scope the managed object
		return new AbstractChange<SectionManagedObjectModel>(managedObject, "Rescope managed object to " + newScope) {
			@Override
			public void apply() {
				managedObject.setManagedObjectScope(newScope);
			}

			@Override
			public void revert() {
				managedObject.setManagedObjectScope(oldScope);
			}
		};
	}

	@Override
	public Change<ManagedFunctionObjectToExternalManagedObjectModel> linkManagedFunctionObjectToExternalManagedObject(
			ManagedFunctionObjectModel managedFunctionObject, ExternalManagedObjectModel externalManagedObject) {

		// TODO test this method
		// (linkManagedFunctionObjectToExternalManagedObject)

		// Create the connection
		final ManagedFunctionObjectToExternalManagedObjectModel conn = new ManagedFunctionObjectToExternalManagedObjectModel();
		conn.setManagedFunctionObject(managedFunctionObject);
		conn.setExternalManagedObject(externalManagedObject);

		// Return the change
		return new AbstractChange<ManagedFunctionObjectToExternalManagedObjectModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<ManagedFunctionObjectToExternalManagedObjectModel> removeManagedFunctionObjectToExternalManagedObject(
			final ManagedFunctionObjectToExternalManagedObjectModel objectToExternalManagedObject) {

		// TODO test this method
		// (removeManagedFunctionObjectToExternalManagedObject)

		// Return change to remove object to external managed object
		return new AbstractChange<ManagedFunctionObjectToExternalManagedObjectModel>(objectToExternalManagedObject,
				"Remove object to external managed object") {
			@Override
			public void apply() {
				objectToExternalManagedObject.remove();
			}

			@Override
			public void revert() {
				objectToExternalManagedObject.connect();
			}
		};
	}

	@Override
	public Change<ManagedFunctionObjectToSectionManagedObjectModel> linkManagedFunctionObjectToSectionManagedObject(
			ManagedFunctionObjectModel managedFunctionObject, SectionManagedObjectModel managedObject) {

		// TODO test this method
		// (linkManagedFunctionObjectToSectionManagedObject)

		// Create the connection
		final ManagedFunctionObjectToSectionManagedObjectModel conn = new ManagedFunctionObjectToSectionManagedObjectModel();
		conn.setManagedFunctionObject(managedFunctionObject);
		conn.setSectionManagedObject(managedObject);

		// Return change to add connection
		return new AbstractChange<ManagedFunctionObjectToSectionManagedObjectModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<ManagedFunctionObjectToSectionManagedObjectModel> removeManagedFunctionObjectToSectionManagedObject(
			final ManagedFunctionObjectToSectionManagedObjectModel managedFunctionObjectToManagedObject) {

		// TODO test this method
		// (removeManagedFunctionObjectToSectionManagedObject)

		// Return change to remove connection
		return new AbstractChange<ManagedFunctionObjectToSectionManagedObjectModel>(
				managedFunctionObjectToManagedObject, "Remove") {
			@Override
			public void apply() {
				managedFunctionObjectToManagedObject.remove();
			}

			@Override
			public void revert() {
				managedFunctionObjectToManagedObject.connect();
			}
		};
	}

	@Override
	public Change<FunctionFlowToFunctionModel> linkFunctionFlowToFunction(FunctionFlowModel functionFlow,
			FunctionModel function, boolean isSpawnThreadState) {

		// TODO test this method (linkFunctionFlowToFunction)

		// Create the connection
		final FunctionFlowToFunctionModel conn = new FunctionFlowToFunctionModel();
		conn.setFunctionFlow(functionFlow);
		conn.setFunction(function);
		conn.setIsSpawnThreadState(isSpawnThreadState);

		// Return the change
		return new AbstractChange<FunctionFlowToFunctionModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<FunctionFlowToFunctionModel> removeFunctionFlowToFunction(
			final FunctionFlowToFunctionModel functionFlowToFunction) {

		// TODO test this method (removeFunctionFlowToFunction)

		// Return the change
		return new AbstractChange<FunctionFlowToFunctionModel>(functionFlowToFunction, "Remove") {
			@Override
			public void apply() {
				functionFlowToFunction.remove();
			}

			@Override
			public void revert() {
				functionFlowToFunction.connect();
			}
		};
	}

	@Override
	public Change<FunctionFlowToExternalFlowModel> linkFunctionFlowToExternalFlow(FunctionFlowModel functionFlow,
			ExternalFlowModel externalFlow, boolean isSpawnThreadState) {

		// TODO test this method (linkFunctionFlowToExternalFlow)

		// Create the connection
		final FunctionFlowToExternalFlowModel conn = new FunctionFlowToExternalFlowModel();
		conn.setFunctionFlow(functionFlow);
		conn.setExternalFlow(externalFlow);
		conn.setIsSpawnThreadState(isSpawnThreadState);

		// Return the change
		return new AbstractChange<FunctionFlowToExternalFlowModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<FunctionFlowToExternalFlowModel> removeFunctionFlowToExternalFlow(
			final FunctionFlowToExternalFlowModel functionFlowToExternalFlow) {

		// TODO test this method (removeFunctionFlowToExternalFlow)

		// Return the change
		return new AbstractChange<FunctionFlowToExternalFlowModel>(functionFlowToExternalFlow, "Remove") {
			@Override
			public void apply() {
				functionFlowToExternalFlow.remove();
			}

			@Override
			public void revert() {
				functionFlowToExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<FunctionToNextFunctionModel> linkFunctionToNextFunction(FunctionModel function,
			FunctionModel nextFunction) {

		// TODO test this method (linkFunctionToNextFunction)

		// Create the connection
		final FunctionToNextFunctionModel conn = new FunctionToNextFunctionModel();
		conn.setPreviousFunction(function);
		conn.setNextFunction(nextFunction);

		// Return the change
		return new AbstractChange<FunctionToNextFunctionModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<FunctionToNextFunctionModel> removeFunctionToNextFunction(
			final FunctionToNextFunctionModel functionToNextFunction) {

		// TODO test this method (removeFunctionToNextFunctionModel)

		// Return the change
		return new AbstractChange<FunctionToNextFunctionModel>(functionToNextFunction, "Remove") {
			@Override
			public void apply() {
				functionToNextFunction.remove();
			}

			@Override
			public void revert() {
				functionToNextFunction.connect();
			}
		};
	}

	@Override
	public Change<FunctionToNextExternalFlowModel> linkFunctionToNextExternalFlow(FunctionModel function,
			ExternalFlowModel nextExternalFlow) {

		// TODO test this method (linkFunctionToNextExternalFlow)

		// Create the connection
		final FunctionToNextExternalFlowModel conn = new FunctionToNextExternalFlowModel();
		conn.setPreviousFunction(function);
		conn.setNextExternalFlow(nextExternalFlow);

		// Return the change
		return new AbstractChange<FunctionToNextExternalFlowModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<FunctionToNextExternalFlowModel> removeFunctionToNextExternalFlow(
			final FunctionToNextExternalFlowModel functionToNextExternalFlow) {

		// TODO test this method (removeFunctionToNextExternalFlow)

		// Return the change
		return new AbstractChange<FunctionToNextExternalFlowModel>(functionToNextExternalFlow, "Remove") {
			@Override
			public void apply() {
				functionToNextExternalFlow.remove();
			}

			@Override
			public void revert() {
				functionToNextExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<FunctionEscalationToFunctionModel> linkFunctionEscalationToFunction(
			FunctionEscalationModel functionEscalation, FunctionModel function) {

		// TODO test this method (linkFunctionEscalationToFunction)

		// Create the connection
		final FunctionEscalationToFunctionModel conn = new FunctionEscalationToFunctionModel();
		conn.setEscalation(functionEscalation);
		conn.setFunction(function);

		// Return the change
		return new AbstractChange<FunctionEscalationToFunctionModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<FunctionEscalationToFunctionModel> removeFunctionEscalationToFunction(
			final FunctionEscalationToFunctionModel functionEscalationToFunction) {

		// TODO test this method (removeFunctionEscalationToFunction)

		// Return the change
		return new AbstractChange<FunctionEscalationToFunctionModel>(functionEscalationToFunction, "Remove") {
			@Override
			public void apply() {
				functionEscalationToFunction.remove();
			}

			@Override
			public void revert() {
				functionEscalationToFunction.connect();
			}
		};
	}

	@Override
	public Change<FunctionEscalationToExternalFlowModel> linkFunctionEscalationToExternalFlow(
			FunctionEscalationModel functionEscalation, ExternalFlowModel externalFlow) {

		// TODO test this method (linkFunctionEscalationToExternalFlow)

		// Create the connection
		final FunctionEscalationToExternalFlowModel conn = new FunctionEscalationToExternalFlowModel();
		conn.setFunctionEscalation(functionEscalation);
		conn.setExternalFlow(externalFlow);

		// Return the change
		return new AbstractChange<FunctionEscalationToExternalFlowModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<FunctionEscalationToExternalFlowModel> removeFunctionEscalationToExternalFlow(
			final FunctionEscalationToExternalFlowModel functionEscalationToExternalFlow) {

		// TODO test this method (removeFunctionEscalationToExternalFlow)

		// Return the change
		return new AbstractChange<FunctionEscalationToExternalFlowModel>(functionEscalationToExternalFlow, "Remove") {
			@Override
			public void apply() {
				functionEscalationToExternalFlow.remove();
			}

			@Override
			public void revert() {
				functionEscalationToExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceFlowToFunctionModel> linkSectionManagedObjectSourceFlowToFunction(
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow, FunctionModel function) {

		// TODO test this method (linkSectionManagedObjectSourceFlowToFunction)

		// Create the connection
		final SectionManagedObjectSourceFlowToFunctionModel conn = new SectionManagedObjectSourceFlowToFunctionModel();
		conn.setSectionManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setFunction(function);

		// Return change to add connection
		return new AbstractChange<SectionManagedObjectSourceFlowToFunctionModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceFlowToFunctionModel> removeSectionManagedObjectSourceFlowToFunction(
			final SectionManagedObjectSourceFlowToFunctionModel managedObjectSourceFlowToFunction) {

		// TODO test this method
		// (removeSectionManagedObjectSourceFlowToFunction)

		// Return change to remove connection
		return new AbstractChange<SectionManagedObjectSourceFlowToFunctionModel>(managedObjectSourceFlowToFunction,
				"Remove") {
			@Override
			public void apply() {
				managedObjectSourceFlowToFunction.remove();
			}

			@Override
			public void revert() {
				managedObjectSourceFlowToFunction.connect();
			}
		};
	}

	@Override
	public Change<SubSectionObjectToExternalManagedObjectModel> linkSubSectionObjectToExternalManagedObject(
			SubSectionObjectModel subSectionObject, ExternalManagedObjectModel externalManagedObject) {

		// TODO test this method (linkSubSectionObjectToExternalManagedObject)

		// Create the connection
		final SubSectionObjectToExternalManagedObjectModel conn = new SubSectionObjectToExternalManagedObjectModel();
		conn.setSubSectionObject(subSectionObject);
		conn.setExternalManagedObject(externalManagedObject);

		// Return change to add connection
		return new AbstractChange<SubSectionObjectToExternalManagedObjectModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SubSectionObjectToExternalManagedObjectModel> removeSubSectionObjectToExternalManagedObject(
			final SubSectionObjectToExternalManagedObjectModel subSectionObjectToExternalManagedObject) {

		// TODO test this method (removeSubSectionObjectToExternalManagedObject)

		// Create change to remove connection
		return new AbstractChange<SubSectionObjectToExternalManagedObjectModel>(subSectionObjectToExternalManagedObject,
				"Remove") {
			@Override
			public void apply() {
				subSectionObjectToExternalManagedObject.remove();
			}

			@Override
			public void revert() {
				subSectionObjectToExternalManagedObject.connect();
			}
		};
	}

	@Override
	public Change<SubSectionObjectToSectionManagedObjectModel> linkSubSectionObjectToSectionManagedObject(
			SubSectionObjectModel subSectionObject, SectionManagedObjectModel managedObject) {

		// TODO test this method (linkSubSectionObjectToSectionManagedObject)

		// Create the connection
		final SubSectionObjectToSectionManagedObjectModel conn = new SubSectionObjectToSectionManagedObjectModel();
		conn.setSubSectionObject(subSectionObject);
		conn.setSectionManagedObject(managedObject);

		// Return change to add connection
		return new AbstractChange<SubSectionObjectToSectionManagedObjectModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SubSectionObjectToSectionManagedObjectModel> removeSubSectionObjectToSectionManagedObject(
			final SubSectionObjectToSectionManagedObjectModel subSectionObjectToManagedObject) {

		// TODO test this method (removeSubSectionObjectToSectionManagedObject)

		// Return change to remove connection
		return new AbstractChange<SubSectionObjectToSectionManagedObjectModel>(subSectionObjectToManagedObject,
				"Remove") {
			@Override
			public void apply() {
				subSectionObjectToManagedObject.remove();
			}

			@Override
			public void revert() {
				subSectionObjectToManagedObject.connect();
			}
		};
	}

	@Override
	public Change<SubSectionOutputToSubSectionInputModel> linkSubSectionOutputToSubSectionInput(
			SubSectionOutputModel subSectionOutput, SubSectionInputModel subSectionInput) {

		// TODO test this method (linkSubSectionOutputToSubSectionInput)

		// Create the connection
		final SubSectionOutputToSubSectionInputModel conn = new SubSectionOutputToSubSectionInputModel();
		conn.setSubSectionOutput(subSectionOutput);
		conn.setSubSectionInput(subSectionInput);

		// Return change to add connection
		return new AbstractChange<SubSectionOutputToSubSectionInputModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SubSectionOutputToSubSectionInputModel> removeSubSectionOutputToSubSectionInput(
			final SubSectionOutputToSubSectionInputModel subSectionOutputToSubSectionInput) {

		// TODO test this method (removeSubSectionOutputToSubSectionInputModel)

		// Create change to remove connection
		return new AbstractChange<SubSectionOutputToSubSectionInputModel>(subSectionOutputToSubSectionInput, "Remove") {
			@Override
			public void apply() {
				subSectionOutputToSubSectionInput.remove();
			}

			@Override
			public void revert() {
				subSectionOutputToSubSectionInput.connect();
			}
		};
	}

	@Override
	public Change<SubSectionOutputToExternalFlowModel> linkSubSectionOutputToExternalFlow(
			SubSectionOutputModel subSectionOutput, ExternalFlowModel externalFlow) {

		// TODO test this method (linkSubSectionOutputToExternalFlow)

		// Create the connection
		final SubSectionOutputToExternalFlowModel conn = new SubSectionOutputToExternalFlowModel();
		conn.setSubSectionOutput(subSectionOutput);
		conn.setExternalFlow(externalFlow);

		// Return change to add connection
		return new AbstractChange<SubSectionOutputToExternalFlowModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SubSectionOutputToExternalFlowModel> removeSubSectionOutputToExternalFlow(
			final SubSectionOutputToExternalFlowModel subSectionOutputToExternalFlow) {

		// TODO test this method (removeSubSectionOutputToExternalFlow)

		// Create change to remove connection
		return new AbstractChange<SubSectionOutputToExternalFlowModel>(subSectionOutputToExternalFlow, "Remove") {
			@Override
			public void apply() {
				subSectionOutputToExternalFlow.remove();
			}

			@Override
			public void revert() {
				subSectionOutputToExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceFlowToSubSectionInputModel> linkSectionManagedObjectSourceFlowToSubSectionInput(
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow, SubSectionInputModel subSectionInput) {

		// TODO test (linkSectionManagedObjectSourceFlowToSubSectionInput)

		// Create the connection
		final SectionManagedObjectSourceFlowToSubSectionInputModel conn = new SectionManagedObjectSourceFlowToSubSectionInputModel();
		conn.setSectionManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setSubSectionInput(subSectionInput);

		// Return change to add the connection
		return new AbstractChange<SectionManagedObjectSourceFlowToSubSectionInputModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceFlowToSubSectionInputModel> removeSectionManagedObjectSourceFlowToSubSectionInput(
			final SectionManagedObjectSourceFlowToSubSectionInputModel managedObjectSourceFlowToSubSectionInput) {

		// TODO test (removeSectionManagedObjectSourceFlowToSubSectionInput)

		// Return change to remove the connection
		return new AbstractChange<SectionManagedObjectSourceFlowToSubSectionInputModel>(
				managedObjectSourceFlowToSubSectionInput, "Remove") {
			@Override
			public void apply() {
				managedObjectSourceFlowToSubSectionInput.remove();
			}

			@Override
			public void revert() {
				managedObjectSourceFlowToSubSectionInput.connect();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceFlowToExternalFlowModel> linkSectionManagedObjectSourceFlowToExternalFlow(
			SectionManagedObjectSourceFlowModel managedObjectSourceFlow, ExternalFlowModel externalFlow) {

		// TODO test (linkSectionManagedObjectSourceFlowToExternalFlow)

		// Create the connection
		final SectionManagedObjectSourceFlowToExternalFlowModel conn = new SectionManagedObjectSourceFlowToExternalFlowModel();
		conn.setSectionManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setExternalFlow(externalFlow);

		// Return change to add the connection
		return new AbstractChange<SectionManagedObjectSourceFlowToExternalFlowModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectSourceFlowToExternalFlowModel> removeSectionManagedObjectSourceFlowToExternalFlow(
			final SectionManagedObjectSourceFlowToExternalFlowModel managedObjectSourceFlowToExternalFlow) {

		// TODO test (removeSectionManagedObjectSourceFlowToExternalFlow)

		// Return change to remove the connection
		return new AbstractChange<SectionManagedObjectSourceFlowToExternalFlowModel>(
				managedObjectSourceFlowToExternalFlow, "Remove") {
			@Override
			public void apply() {
				managedObjectSourceFlowToExternalFlow.remove();
			}

			@Override
			public void revert() {
				managedObjectSourceFlowToExternalFlow.connect();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectDependencyToSectionManagedObjectModel> linkSectionManagedObjectDependencyToSectionManagedObject(
			SectionManagedObjectDependencyModel dependency, SectionManagedObjectModel managedObject) {

		// TODO test (linkSectionManagedObjectDependencyToSectionManagedObject)

		// Create the connection
		final SectionManagedObjectDependencyToSectionManagedObjectModel conn = new SectionManagedObjectDependencyToSectionManagedObjectModel();
		conn.setSectionManagedObjectDependency(dependency);
		conn.setSectionManagedObject(managedObject);

		// Return change to add the connection
		return new AbstractChange<SectionManagedObjectDependencyToSectionManagedObjectModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectDependencyToSectionManagedObjectModel> removeSectionManagedObjectDependencyToSectionManagedObject(
			final SectionManagedObjectDependencyToSectionManagedObjectModel dependencyToManagedObject) {

		// TODO (removeSectionManagedObjectDependencyToSectionManagedObject)

		// Return change to remove the connection
		return new AbstractChange<SectionManagedObjectDependencyToSectionManagedObjectModel>(dependencyToManagedObject,
				"Remove") {
			@Override
			public void apply() {
				dependencyToManagedObject.remove();
			}

			@Override
			public void revert() {
				dependencyToManagedObject.connect();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectDependencyToExternalManagedObjectModel> linkSectionManagedObjectDependencyToExternalManagedObject(
			SectionManagedObjectDependencyModel dependency, ExternalManagedObjectModel externalManagedObject) {

		// TODO test (linkSectionManagedObjectDependencyToExternalManagedObject)

		// Create connection
		final SectionManagedObjectDependencyToExternalManagedObjectModel conn = new SectionManagedObjectDependencyToExternalManagedObjectModel();
		conn.setSectionManagedObjectDependency(dependency);
		conn.setExternalManagedObject(externalManagedObject);

		// Return change to add connection
		return new AbstractChange<SectionManagedObjectDependencyToExternalManagedObjectModel>(conn, "Connect") {
			@Override
			public void apply() {
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
			}
		};
	}

	@Override
	public Change<SectionManagedObjectDependencyToExternalManagedObjectModel> removeSectionManagedObjectDependencyToExternalManagedObject(
			final SectionManagedObjectDependencyToExternalManagedObjectModel dependencyToExternalManagedObject) {

		// TODO (removeSectionManagedObjectDependencyToExternalManagedObject)

		// Return change to remove the connection
		return new AbstractChange<SectionManagedObjectDependencyToExternalManagedObjectModel>(
				dependencyToExternalManagedObject, "Remove") {
			@Override
			public void apply() {
				dependencyToExternalManagedObject.remove();
			}

			@Override
			public void revert() {
				dependencyToExternalManagedObject.connect();
			}
		};
	}

	@Override
	public Change<SubSectionOutputToFunctionModel> linkSubSectionOutputToFunction(
			SubSectionOutputModel subSectionOutput, FunctionModel function) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Change<SubSectionOutputToFunctionModel> removeSubSectionOutputToFunction(
			SubSectionOutputToFunctionModel subSectionOutputToFunction) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Change<FunctionFlowToSubSectionInputModel> linkFunctionFlowToSubSectionInput(FunctionFlowModel functionFlow,
			SubSectionInputModel subSectionInput, boolean isSpawnThreadState) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Change<FunctionFlowToSubSectionInputModel> removeFunctionFlowToSubSectionInput(
			FunctionFlowToSubSectionInputModel functionFlowToSubSectionInput) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Change<FunctionToNextSubSectionInputModel> linkFunctionToNextSubSectionInput(FunctionModel function,
			SubSectionInputModel nextSubSectionInput) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Change<FunctionToNextSubSectionInputModel> removeFunctionToNextSubSectionInput(
			FunctionToNextSubSectionInputModel functionToNextSubSectionInput) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Change<FunctionEscalationToSubSectionInputModel> linkFunctionEscalationToSubSectionInput(
			FunctionEscalationModel functionEscalation, SubSectionInputModel subSectionInput) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	@Override
	public Change<FunctionEscalationToSubSectionInputModel> removeFunctionEscalationToSubSectionInput(
			FunctionEscalationToSubSectionInputModel functionEscalationToSubSectionInput) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

}
