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
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskManagedObjectDependencyModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToDeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.desk.DeskManagedObjectSourceFlowToFunctionModel;
import net.officefloor.model.desk.DeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskManagedObjectToDeskManagedObjectSourceModel;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.FunctionEscalationModel;
import net.officefloor.model.desk.FunctionEscalationToExternalFlowModel;
import net.officefloor.model.desk.FunctionEscalationToFunctionModel;
import net.officefloor.model.desk.FunctionFlowModel;
import net.officefloor.model.desk.FunctionFlowToExternalFlowModel;
import net.officefloor.model.desk.FunctionFlowToFunctionModel;
import net.officefloor.model.desk.FunctionModel;
import net.officefloor.model.desk.FunctionNamespaceModel;
import net.officefloor.model.desk.FunctionToNextExternalFlowModel;
import net.officefloor.model.desk.FunctionToNextFunctionModel;
import net.officefloor.model.desk.ManagedFunctionModel;
import net.officefloor.model.desk.ManagedFunctionObjectModel;
import net.officefloor.model.desk.ManagedFunctionObjectToDeskManagedObjectModel;
import net.officefloor.model.desk.ManagedFunctionObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.ManagedFunctionToFunctionModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.impl.change.AbstractChange;
import net.officefloor.model.impl.change.DisconnectChange;
import net.officefloor.model.impl.change.NoChange;

/**
 * {@link DeskChanges} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class DeskChangesImpl implements DeskChanges {

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
	 * {@link DeskModel}.
	 */
	private final DeskModel desk;

	/**
	 * Initiate.
	 *
	 * @param desk
	 *            {@link DeskModel}.
	 */
	public DeskChangesImpl(DeskModel desk) {
		this.desk = desk;
	}

	/**
	 * Sorts the {@link FunctionNamespaceModel} instances.
	 */
	protected void sortNamespaceModels() {
		sortNamespaceModels(this.desk.getFunctionNamespaces());
	}

	/**
	 * Sorts the {@link FunctionModel} instances.
	 */
	protected void sortFunctionModels() {
		sortFunctionModels(this.desk.getFunctions());
	}

	/**
	 * Sorts the {@link ExternalFlowModel} instances.
	 */
	protected void sortExternalFlows() {
		sortExternalFlows(this.desk.getExternalFlows());
	}

	/**
	 * Sorts the {@link ExternalManagedObjectModel} instances.
	 */
	protected void sortExternalManagedObjects() {
		sortExternalManagedObjects(this.desk.getExternalManagedObjects());
	}

	/**
	 * Creates a {@link ManagedFunctionModel} for a {@link ManagedFunctionType}.
	 *
	 * @param managedFunctionType
	 *            {@link ManagedFunctionType}.
	 * @return {@link ManagedFunctionkModel} for the
	 *         {@link ManagedFunctionType}.
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
			DeskChangesImpl.this.desk.removeFunction(function);
			functionList.add(function);
		}
	}

	/*
	 * ==================== DeskOperations =================================
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
			ManagedFunctionModel managedFunction = DeskChangesImpl.this.createManagedFunctionModel(managedFunctionType);
			namespace.addManagedFunction(managedFunction);
		}

		// Ensure managed function models in sorted order
		DeskChangesImpl.sortManagedFunctionModels(namespace.getManagedFunctions());

		// Return the change to add the work
		return new AbstractChange<FunctionNamespaceModel>(namespace, "Add function namespace " + namespaceName) {
			@Override
			public void apply() {
				// Add the namespace (ensuring in sorted order)
				DeskChangesImpl.this.desk.addFunctionNamespace(namespace);
				DeskChangesImpl.this.sortNamespaceModels();
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk.removeFunctionNamespace(namespace);
			}
		};
	}

	@Override
	public Change<FunctionNamespaceModel> removeFunctionNamespace(final FunctionNamespaceModel workModel) {

		// Ensure the work is on the desk
		boolean isOnDesk = false;
		for (FunctionNamespaceModel work : this.desk.getFunctionNamespaces()) {
			if (work == workModel) {
				isOnDesk = true;
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove
			return new NoChange<FunctionNamespaceModel>(workModel,
					"Remove work " + workModel.getFunctionNamespaceName(),
					"FunctionNamespace " + workModel.getFunctionNamespaceName() + " not on desk");
		}

		// Return change to remove the work
		return new AbstractChange<FunctionNamespaceModel>(workModel,
				"Remove work " + workModel.getFunctionNamespaceName()) {

			/**
			 * {@link FunctionModel} instances associated to
			 * {@link FunctionNamespaceModel}.
			 */
			private FunctionModel[] functions;

			/**
			 * {@link ConnectionModel} instances associated to the
			 * {@link FunctionNamespaceModel}.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {

				// Remove connections to work and its functions
				List<ConnectionModel> connectionList = new LinkedList<ConnectionModel>();
				for (ManagedFunctionModel managedFunction : workModel.getManagedFunctions()) {
					DeskChangesImpl.this.removeManagedFunctionConnections(managedFunction, connectionList);
				}
				this.connections = connectionList.toArray(new ConnectionModel[0]);

				// Remove the associated functions (storing for revert)
				List<FunctionModel> functionList = new LinkedList<FunctionModel>();
				for (ManagedFunctionModel managedFunction : workModel.getManagedFunctions()) {
					DeskChangesImpl.this.removeManagedFunction(managedFunction, functionList);
				}
				this.functions = functionList.toArray(new FunctionModel[0]);

				// Remove the work
				DeskChangesImpl.this.desk.removeFunctionNamespace(workModel);
			}

			@Override
			public void revert() {
				// Add the namespace (ensuring in sorted order)
				DeskChangesImpl.this.desk.addFunctionNamespace(workModel);
				DeskChangesImpl.this.sortNamespaceModels();

				// Add the functions (in reverse order, ensuring sorted)
				for (int i = (this.functions.length - 1); i >= 0; i--) {
					DeskChangesImpl.this.desk.addFunction(this.functions[i]);
				}
				DeskChangesImpl.this.sortFunctionModels();

				// Reconnect connections
				for (ConnectionModel connection : this.connections) {
					connection.connect();
				}
			}
		};
	}

	@Override
	public Change<FunctionNamespaceModel> renameFunctionNamespace(final FunctionNamespaceModel workModel,
			final String newFunctionNamespaceName) {

		// Ensure the work is on the desk
		boolean isOnDesk = false;
		for (FunctionNamespaceModel work : this.desk.getFunctionNamespaces()) {
			if (work == workModel) {
				isOnDesk = true;
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove
			return new NoChange<FunctionNamespaceModel>(workModel,
					"Rename work " + workModel.getFunctionNamespaceName() + " to " + newFunctionNamespaceName,
					"FunctionNamespace " + workModel.getFunctionNamespaceName() + " not on desk");
		}

		// Store the old name for reverting
		final String oldFunctionNamespaceName = workModel.getFunctionNamespaceName();

		// Return change to rename work
		return new AbstractChange<FunctionNamespaceModel>(workModel,
				"Rename work " + workModel.getFunctionNamespaceName() + " to " + newFunctionNamespaceName) {
			@Override
			public void apply() {
				// Rename and ensure namespace in sorted order
				workModel.setFunctionNamespaceName(newFunctionNamespaceName);
				DeskChangesImpl.this.sortNamespaceModels();
			}

			@Override
			public void revert() {
				// Revert to old name, ensuring namespace sorted
				workModel.setFunctionNamespaceName(oldFunctionNamespaceName);
				DeskChangesImpl.this.sortNamespaceModels();
			}
		};
	}

	@Override
	public Change<FunctionNamespaceModel> refactorFunctionNamespace(final FunctionNamespaceModel namespaceModel,
			final String workName, final String managedFunctionSourceClassName, PropertyList properties,
			FunctionNamespaceType namespaceType, Map<String, String> managedFunctionNameMapping,
			Map<String, Map<String, String>> managedFunctionToObjectNameMapping,
			Map<String, Map<String, String>> functionToFlowNameMapping,
			Map<String, Map<String, String>> functionToEscalationTypeMapping, String... functionNames) {

		// Create the list to contain all refactor changes
		final List<Change<?>> refactor = new LinkedList<Change<?>>();

		// ------------ Details of FunctionNamespaceModel ------------

		// Add change to rename the work
		final String existingNamespaceName = namespaceModel.getFunctionNamespaceName();
		refactor.add(new AbstractChange<FunctionNamespaceModel>(namespaceModel, "Rename namespace") {
			@Override
			public void apply() {
				namespaceModel.setFunctionNamespaceName(workName);
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
					? new ManagedFunctionModel(managedFunctionName) : findManagedFunction);
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
							DeskChangesImpl.this.removeManagedFunctionObjectConnections(functionObject, connList);
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
							? new FunctionFlowModel(flowName, flowKeyName, argumentTypeName) : findFunctionFlow);
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
								DeskChangesImpl.this.removeFunctionFlowConnections(functionFlow, connList);
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
							? new FunctionEscalationModel(escalationTypeName) : findFunctionEscalation);
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
								DeskChangesImpl.this.removeFunctionEscalationConnections(functionEscalation, connList);
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
						DeskChangesImpl.this.removeManagedFunctionConnections(managedFunction, connList);
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
						DeskChangesImpl.this.removeManagedFunction(managedFunction, this.functions);
					}

					@Override
					public void revert() {
						// Add back the function models
						for (FunctionModel function : this.functions) {
							DeskChangesImpl.this.desk.addFunction(function);
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
		final ManagedFunctionModel managedFunction = DeskChangesImpl.this.createManagedFunctionModel(functionType);

		// Return the add managed function change
		return new AbstractChange<ManagedFunctionModel>(managedFunction, "Add managed function " + functionName) {
			@Override
			public void apply() {
				// Add managed function (ensuring managed functions sorted)
				namespaceModel.addManagedFunction(managedFunction);
				DeskChangesImpl.sortManagedFunctionModels(namespaceModel.getManagedFunctions());
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

		// Ensure managed function on work
		boolean isOnWork = false;
		for (ManagedFunctionModel managedFunctionModel : namespace.getManagedFunctions()) {
			if (managedFunctionModel == managedFunction) {
				isOnWork = true;
			}
		}
		if (!isOnWork) {
			// Managed function not on work
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
				DeskChangesImpl.this.removeManagedFunctionConnections(managedFunction, connList);
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the functions of the managed function
				List<FunctionModel> functionList = new LinkedList<FunctionModel>();
				DeskChangesImpl.this.removeManagedFunction(managedFunction, functionList);
				this.functions = functionList.toArray(new FunctionModel[0]);

				// Remove the managed function
				namespace.removeManagedFunction(managedFunction);
			}

			@Override
			public void revert() {
				// Add the managed function (ensuring sorted)
				namespace.addManagedFunction(managedFunction);
				DeskChangesImpl.sortManagedFunctionModels(namespace.getManagedFunctions());

				// Add the functions (in reverse order, ensuring sorted)
				for (int i = (this.functions.length - 1); i >= 0; i--) {
					DeskChangesImpl.this.desk.addFunction(this.functions[i]);
				}
				DeskChangesImpl.this.sortFunctionModels();

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

		// Ensure the managed function is on the desk and obtain its namespace
		FunctionNamespaceModel namespace = null;
		for (FunctionNamespaceModel namespaceModel : this.desk.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunctionModel : namespaceModel.getManagedFunctions()) {
				if (managedFunctionModel == managedFunction) {
					// On the desk
					namespace = namespaceModel;
				}
			}
		}
		if (namespace == null) {
			// Managed function not on desk so can not add function
			return new NoChange<FunctionModel>(function, "Add function " + functionName,
					"Managed function " + managedFunction.getManagedFunctionName() + " not on desk");
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
				DeskChangesImpl.this.desk.addFunction(function);
				DeskChangesImpl.this.sortFunctionModels();

				// Connect managed function to the function (ensuring ordering)
				conn.connect();
				DeskChangesImpl.sortManagedFunctionToFunctionConnections(managedFunction.getFunctions());
			}

			@Override
			public void revert() {
				// Disconnect managed function from the function
				conn.remove();

				// Remove function (should maintain ordering)
				DeskChangesImpl.this.desk.removeFunction(function);
			}
		};
	}

	@Override
	public Change<FunctionModel> removeFunction(final FunctionModel function) {

		// Ensure the function is on the desk
		boolean isOnDesk = false;
		for (FunctionModel functionModel : this.desk.getFunctions()) {
			if (function == functionModel) {
				isOnDesk = true; // function on desk
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove it
			return new NoChange<FunctionModel>(function, "Remove function " + function.getFunctionName(),
					"Function " + function.getFunctionName() + " not on desk");
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
				DeskChangesImpl.this.removeFunctionConnections(function, connList);

				// Remove connection to managed function
				ManagedFunctionToFunctionModel managedFunctionConn = function.getManagedFunction();
				if (managedFunctionConn != null) {
					managedFunctionConn.remove();
					connList.add(managedFunctionConn);
				}

				// Store for revert
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the function (should maintain order)
				DeskChangesImpl.this.desk.removeFunction(function);
			}

			@Override
			public void revert() {
				// Add function back in (ensuring order)
				DeskChangesImpl.this.desk.addFunction(function);
				DeskChangesImpl.this.sortFunctionModels();

				// Reconnect connections
				for (ConnectionModel conn : this.connections) {
					conn.connect();
				}

				// Ensure managed function connections sorted by function name
				DeskChangesImpl.sortManagedFunctionToFunctionConnections(
						function.getManagedFunction().getManagedFunction().getFunctions());
			}
		};
	}

	@Override
	public Change<FunctionModel> renameFunction(final FunctionModel function, final String newFunctionName) {

		// Ensure the function is on the desk
		boolean isOnDesk = false;
		for (FunctionModel functionModel : DeskChangesImpl.this.desk.getFunctions()) {
			if (function == functionModel) {
				isOnDesk = true; // function on desk
			}
		}
		if (!isOnDesk) {
			// Can not remove function as not on desk
			return new NoChange<FunctionModel>(function,
					"Rename function " + function.getFunctionName() + " to " + newFunctionName,
					"Function " + function.getFunctionName() + " not on desk");
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
				DeskChangesImpl.this.sortFunctionModels();
				DeskChangesImpl.sortManagedFunctionToFunctionConnections(
						function.getManagedFunction().getManagedFunction().getFunctions());
			}

			@Override
			public void revert() {
				// Revert to old function name (ensuring ordering)
				function.setFunctionName(oldFunctionName);
				DeskChangesImpl.this.sortFunctionModels();
				DeskChangesImpl.sortManagedFunctionToFunctionConnections(
						function.getManagedFunction().getManagedFunction().getFunctions());
			}
		};
	}

	@Override
	public Change<ManagedFunctionObjectModel> setObjectAsParameter(boolean isParameter,
			final ManagedFunctionObjectModel functionObject) {

		// Ensure the function object on the desk
		boolean isOnDesk = false;
		for (FunctionNamespaceModel work : DeskChangesImpl.this.desk.getFunctionNamespaces()) {
			for (ManagedFunctionModel managedFunction : work.getManagedFunctions()) {
				for (ManagedFunctionObjectModel functionObjectModel : managedFunction.getManagedFunctionObjects()) {
					if (functionObject == functionObjectModel) {
						isOnDesk = true; // on the desk
					}
				}
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not set as parameter
			return new NoChange<ManagedFunctionObjectModel>(functionObject,
					"Set function object " + functionObject.getObjectName() + " as "
							+ (isParameter ? "a parameter" : "an object"),
					"Function object " + functionObject.getObjectName() + " not on desk");
		}

		// Return the appropriate change
		if (isParameter) {
			// Return change to set as parameter
			final ManagedFunctionObjectToExternalManagedObjectModel conn = functionObject.getExternalManagedObject();
			return new AbstractChange<ManagedFunctionObjectModel>(functionObject,
					"Set function object " + functionObject.getObjectName() + " as a parameter") {
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
					"Set function object " + functionObject.getObjectName() + " as an object") {

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

		// Ensure function on desk
		boolean isOnDesk = false;
		for (FunctionModel functionModel : DeskChangesImpl.this.desk.getFunctions()) {
			if (function == functionModel) {
				isOnDesk = true; // function on desk
			}
		}
		if (!isOnDesk) {
			// Function not on desk so can not make public
			return new NoChange<FunctionModel>(function,
					"Set function " + function.getFunctionName() + (isPublic ? " public" : " private"),
					"Function " + function.getFunctionName() + " not on desk");
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
	public Change<ExternalFlowModel> addExternalFlow(String externalFlowName, String argumentType) {

		// Create the external flow
		final ExternalFlowModel externalFlow = new ExternalFlowModel(externalFlowName, argumentType);

		// Return change to add external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow, "Add external flow " + externalFlowName) {
			@Override
			public void apply() {
				// Add external flow (ensuring ordering)
				DeskChangesImpl.this.desk.addExternalFlow(externalFlow);
				DeskChangesImpl.this.sortExternalFlows();
			}

			@Override
			public void revert() {
				// Remove external flow (should maintain order)
				DeskChangesImpl.this.desk.removeExternalFlow(externalFlow);
			}
		};
	}

	@Override
	public Change<ExternalFlowModel> removeExternalFlow(final ExternalFlowModel externalFlow) {

		// Ensure external flow on desk
		boolean isOnDesk = false;
		for (ExternalFlowModel externalFlowModel : DeskChangesImpl.this.desk.getExternalFlows()) {
			if (externalFlow == externalFlowModel) {
				isOnDesk = true; // on the desk
			}
		}
		if (!isOnDesk) {
			// No change as external flow not on desk
			return new NoChange<ExternalFlowModel>(externalFlow,
					"Remove external flow " + externalFlow.getExternalFlowName(),
					"External flow " + externalFlow.getExternalFlowName() + " not on desk");
		}

		// Return change to remove external flow
		return new AbstractChange<ExternalFlowModel>(externalFlow,
				"Remove external flow " + externalFlow.getExternalFlowName()) {

			/**
			 * {@link ConnectionModel} instances.
			 */
			private ConnectionModel[] connections;

			@Override
			public void apply() {
				// Remove the connections to the external flows
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				for (FunctionFlowToExternalFlowModel conn : new ArrayList<FunctionFlowToExternalFlowModel>(
						externalFlow.getFunctionFlows())) {
					conn.remove();
					connList.add(conn);
				}
				for (FunctionToNextExternalFlowModel conn : new ArrayList<FunctionToNextExternalFlowModel>(
						externalFlow.getPreviousFunctions())) {
					conn.remove();
					connList.add(conn);
				}
				for (FunctionEscalationToExternalFlowModel conn : new ArrayList<FunctionEscalationToExternalFlowModel>(
						externalFlow.getFunctionEscalations())) {
					conn.remove();
					connList.add(conn);
				}
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the external flow (should maintain order)
				DeskChangesImpl.this.desk.removeExternalFlow(externalFlow);
			}

			@Override
			public void revert() {
				// Add the external flow back (ensure ordering)
				DeskChangesImpl.this.desk.addExternalFlow(externalFlow);
				DeskChangesImpl.this.sortExternalFlows();

				// Reconnect connections
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
		return new AbstractChange<ExternalFlowModel>(externalFlow, "Rename external flow to " + newExternalFlowName) {
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
		final ExternalManagedObjectModel externalMo = new ExternalManagedObjectModel(externalManagedObjectName,
				objectType);

		// Return the change to add external managed object
		return new AbstractChange<ExternalManagedObjectModel>(externalMo,
				"Add external managed object " + externalManagedObjectName) {
			@Override
			public void apply() {
				// Add external managed object (ensure ordering)
				DeskChangesImpl.this.desk.addExternalManagedObject(externalMo);
				DeskChangesImpl.this.sortExternalManagedObjects();
			}

			@Override
			public void revert() {
				// Remove external managed object (should maintain order)
				DeskChangesImpl.this.desk.removeExternalManagedObject(externalMo);
			}
		};
	}

	@Override
	public Change<ExternalManagedObjectModel> removeExternalManagedObject(
			final ExternalManagedObjectModel externalManagedObject) {

		// Ensure external managed object on desk
		boolean isOnDesk = false;
		for (ExternalManagedObjectModel externalManagedObjectModel : DeskChangesImpl.this.desk
				.getExternalManagedObjects()) {
			if (externalManagedObject == externalManagedObjectModel) {
				isOnDesk = true; // on the desk
			}
		}
		if (!isOnDesk) {
			// Not on desk so can not remove it
			return new NoChange<ExternalManagedObjectModel>(externalManagedObject,
					"Remove external managed object " + externalManagedObject.getExternalManagedObjectName(),
					"External managed object " + externalManagedObject.getExternalManagedObjectName() + " not on desk");
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
				// Remove the connections to the external managed object
				List<ConnectionModel> connList = new LinkedList<ConnectionModel>();
				for (ManagedFunctionObjectToExternalManagedObjectModel conn : new ArrayList<ManagedFunctionObjectToExternalManagedObjectModel>(
						externalManagedObject.getManagedFunctionObjects())) {
					conn.remove();
					connList.add(conn);
				}
				this.connections = connList.toArray(new ConnectionModel[0]);

				// Remove the external managed object (should maintain order)
				DeskChangesImpl.this.desk.removeExternalManagedObject(externalManagedObject);
			}

			@Override
			public void revert() {
				// Add back the external managed object (ensure ordering)
				DeskChangesImpl.this.desk.addExternalManagedObject(externalManagedObject);
				DeskChangesImpl.this.sortExternalManagedObjects();

				// Reconnect connections
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
				"Rename external managed object to " + newExternalManagedObjectName) {
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
	public Change<DeskManagedObjectSourceModel> addDeskManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties, long timeout,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addDeskManagedObjectSource)

		// Create the managed object source
		final DeskManagedObjectSourceModel managedObjectSource = new DeskManagedObjectSourceModel(
				managedObjectSourceName, managedObjectSourceClassName, managedObjectType.getObjectClass().getName(),
				String.valueOf(timeout));
		for (Property property : properties) {
			managedObjectSource.addProperty(new PropertyModel(property.getName(), property.getValue()));
		}

		// Add the flows for the managed object source
		for (ManagedObjectFlowType<?> flow : managedObjectType.getFlowTypes()) {
			managedObjectSource.addDeskManagedObjectSourceFlow(
					new DeskManagedObjectSourceFlowModel(flow.getFlowName(), flow.getArgumentType().getName()));
		}

		// Return the change to add the managed object source
		return new AbstractChange<DeskManagedObjectSourceModel>(managedObjectSource, "Add managed object source") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk.addDeskManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk.removeDeskManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceModel> removeDeskManagedObjectSource(
			final DeskManagedObjectSourceModel managedObjectSource) {

		// TODO test this method (removeDeskManagedObjectSource)

		// Return change to remove the managed object source
		return new AbstractChange<DeskManagedObjectSourceModel>(managedObjectSource, "Remove managed object source") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk.removeDeskManagedObjectSource(managedObjectSource);
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk.addDeskManagedObjectSource(managedObjectSource);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectSourceModel> renameDeskManagedObjectSource(
			final DeskManagedObjectSourceModel managedObjectSource, final String newManagedObjectSourceName) {

		// TODO test this method (renameDeskManagedObjectSource)

		// Obtain the old managed object source name
		final String oldManagedObjectSourceName = managedObjectSource.getDeskManagedObjectSourceName();

		// Return change to rename the managed object source
		return new AbstractChange<DeskManagedObjectSourceModel>(managedObjectSource,
				"Rename managed object source to " + newManagedObjectSourceName) {
			@Override
			public void apply() {
				managedObjectSource.setDeskManagedObjectSourceName(newManagedObjectSourceName);
			}

			@Override
			public void revert() {
				managedObjectSource.setDeskManagedObjectSourceName(oldManagedObjectSourceName);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> addDeskManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope, DeskManagedObjectSourceModel managedObjectSource,
			ManagedObjectType<?> managedObjectType) {

		// TODO test this method (addDeskManagedObject)

		// Create the managed object
		final DeskManagedObjectModel managedObject = new DeskManagedObjectModel(managedObjectName,
				getManagedObjectScope(managedObjectScope));

		// Add the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependency : managedObjectType.getDependencyTypes()) {
			managedObject.addDeskManagedObjectDependency(new DeskManagedObjectDependencyModel(
					dependency.getDependencyName(), dependency.getDependencyType().getName()));
		}

		// Create connection to the managed object source
		final DeskManagedObjectToDeskManagedObjectSourceModel conn = new DeskManagedObjectToDeskManagedObjectSourceModel();
		conn.setDeskManagedObject(managedObject);
		conn.setDeskManagedObjectSource(managedObjectSource);

		// Return change to add the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject, "Add managed object") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk.addDeskManagedObject(managedObject);
				conn.connect();
			}

			@Override
			public void revert() {
				conn.remove();
				DeskChangesImpl.this.desk.removeDeskManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> removeDeskManagedObject(final DeskManagedObjectModel managedObject) {

		// TODO test this method (removeDeskManagedObject)

		// Return change to remove the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject, "Remove managed object") {
			@Override
			public void apply() {
				DeskChangesImpl.this.desk.removeDeskManagedObject(managedObject);
			}

			@Override
			public void revert() {
				DeskChangesImpl.this.desk.addDeskManagedObject(managedObject);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> renameDeskManagedObject(final DeskManagedObjectModel managedObject,
			final String newManagedObjectName) {

		// TODO test this method (renameDeskManagedObject)

		// Obtain the old managed object name
		final String oldManagedObjectName = managedObject.getDeskManagedObjectName();

		// Return change to rename the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject,
				"Rename managed object to " + newManagedObjectName) {
			@Override
			public void apply() {
				managedObject.setDeskManagedObjectName(newManagedObjectName);
			}

			@Override
			public void revert() {
				managedObject.setDeskManagedObjectName(oldManagedObjectName);
			}
		};
	}

	@Override
	public Change<DeskManagedObjectModel> rescopeDeskManagedObject(final DeskManagedObjectModel managedObject,
			final ManagedObjectScope newManagedObjectScope) {

		// TODO test this method (rescopeDeskManagedObject)

		// Obtain the new scope text
		final String newScope = getManagedObjectScope(newManagedObjectScope);

		// OBtain the old managed object scope
		final String oldScope = managedObject.getManagedObjectScope();

		// Return change to re-scope the managed object
		return new AbstractChange<DeskManagedObjectModel>(managedObject, "Rescope managed object to " + newScope) {
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
	public Change<ManagedFunctionObjectToDeskManagedObjectModel> linkManagedFunctionObjectToDeskManagedObject(
			ManagedFunctionObjectModel managedFunctionObject, DeskManagedObjectModel managedObject) {

		// TODO test this method (linkManagedFunctionObjectToDeskManagedObject)

		// Create the connection
		final ManagedFunctionObjectToDeskManagedObjectModel conn = new ManagedFunctionObjectToDeskManagedObjectModel();
		conn.setManagedFunctionObject(managedFunctionObject);
		conn.setDeskManagedObject(managedObject);

		// Return change to add connection
		return new AbstractChange<ManagedFunctionObjectToDeskManagedObjectModel>(conn, "Connect") {
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
	public Change<ManagedFunctionObjectToDeskManagedObjectModel> removeManagedFunctionObjectToDeskManagedObject(
			final ManagedFunctionObjectToDeskManagedObjectModel managedFunctionObjectToManagedObject) {

		// TODO test this method
		// (removeManagedFunctionObjectToDeskManagedObject)

		// Return change to remove connection
		return new AbstractChange<ManagedFunctionObjectToDeskManagedObjectModel>(managedFunctionObjectToManagedObject,
				"Remove") {
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
	public Change<DeskManagedObjectSourceFlowToFunctionModel> linkDeskManagedObjectSourceFlowToFunction(
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow, FunctionModel function) {

		// TODO test this method (linkDeskManagedObjectSourceFlowToFunction)

		// Create the connection
		final DeskManagedObjectSourceFlowToFunctionModel conn = new DeskManagedObjectSourceFlowToFunctionModel();
		conn.setDeskManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setFunction(function);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectSourceFlowToFunctionModel>(conn, "Connect") {
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
	public Change<DeskManagedObjectSourceFlowToFunctionModel> removeDeskManagedObjectSourceFlowToFunction(
			final DeskManagedObjectSourceFlowToFunctionModel managedObjectSourceFlowToFunction) {

		// TODO test this method (removeDeskManagedObjectSourceFlowToFunction)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectSourceFlowToFunctionModel>(managedObjectSourceFlowToFunction,
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
	public Change<DeskManagedObjectSourceFlowToExternalFlowModel> linkDeskManagedObjectSourceFlowToExternalFlow(
			DeskManagedObjectSourceFlowModel managedObjectSourceFlow, ExternalFlowModel externalFlow) {

		// TODO test this method (linkDeskManagedObjectSourceFlowToExternalFlow)

		// Create the connection
		final DeskManagedObjectSourceFlowToExternalFlowModel conn = new DeskManagedObjectSourceFlowToExternalFlowModel();
		conn.setDeskManagedObjectSourceFlow(managedObjectSourceFlow);
		conn.setExternalFlow(externalFlow);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectSourceFlowToExternalFlowModel>(conn, "Connect") {
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
	public Change<DeskManagedObjectSourceFlowToExternalFlowModel> removeDeskManagedObjectSourceFlowToExternalFlow(
			final DeskManagedObjectSourceFlowToExternalFlowModel managedObjectSourceFlowToExternalFlow) {

		// TODO test (removeDeskManagedObjectSourceFlowToExternalFlow)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectSourceFlowToExternalFlowModel>(managedObjectSourceFlowToExternalFlow,
				"Remove") {
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
	public Change<DeskManagedObjectDependencyToDeskManagedObjectModel> linkDeskManagedObjectDependencyToDeskManagedObject(
			DeskManagedObjectDependencyModel dependency, DeskManagedObjectModel managedObject) {

		// TODO test (linkDeskManagedObjectDependencyToDeskManagedObject)

		// Create the connection
		final DeskManagedObjectDependencyToDeskManagedObjectModel conn = new DeskManagedObjectDependencyToDeskManagedObjectModel();
		conn.setDeskManagedObjectDependency(dependency);
		conn.setDeskManagedObject(managedObject);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectDependencyToDeskManagedObjectModel>(conn, "Connect") {
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
	public Change<DeskManagedObjectDependencyToDeskManagedObjectModel> removeDeskManagedObjectDependencyToDeskManagedObject(
			final DeskManagedObjectDependencyToDeskManagedObjectModel dependencyToManagedObject) {

		// TODO test (removeDeskManagedObjectDependencyToDeskManagedObject)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectDependencyToDeskManagedObjectModel>(dependencyToManagedObject,
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
	public Change<DeskManagedObjectDependencyToExternalManagedObjectModel> linkDeskManagedObjectDependencyToExternalManagedObject(
			DeskManagedObjectDependencyModel dependency, ExternalManagedObjectModel externalManagedObject) {

		// TODO test (linkDeskManagedObjectDependencyToExternalManagedObject)

		// Create the connection
		final DeskManagedObjectDependencyToExternalManagedObjectModel conn = new DeskManagedObjectDependencyToExternalManagedObjectModel();
		conn.setDeskManagedObjectDependency(dependency);
		conn.setExternalManagedObject(externalManagedObject);

		// Return change to add connection
		return new AbstractChange<DeskManagedObjectDependencyToExternalManagedObjectModel>(conn, "Connect") {
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
	public Change<DeskManagedObjectDependencyToExternalManagedObjectModel> removeDeskManagedObjectDependencyToExternalManagedObject(
			final DeskManagedObjectDependencyToExternalManagedObjectModel dependencyToExternalManagedObject) {

		// TODO test (removeDeskManagedObjectDependencyToExternalManagedObject)

		// Return change to remove connection
		return new AbstractChange<DeskManagedObjectDependencyToExternalManagedObjectModel>(
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

}