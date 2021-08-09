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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.PropertyModel;

/**
 * Abstract functionality to test refactoring the {@link FunctionNamespaceModel}
 * to a {@link FunctionNamespaceType} via the {@link SectionChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractRefactorNamespaceTest extends AbstractSectionChangesTestCase {

	/**
	 * {@link FunctionNamespaceModel} to refactor.
	 */
	private FunctionNamespaceModel namespaceModel;

	/**
	 * Name to refactor the {@link FunctionNamespaceModel} to have.
	 */
	private String namespaceName;

	/**
	 * {@link ManagedFunctionSource} class name to refactor the
	 * {@link FunctionNamespaceModel} to have.
	 */
	private String managedFunctionSourceClassName;

	/**
	 * {@link PropertyList} to refactor the {@link FunctionNamespaceModel} to
	 * have.
	 */
	private PropertyList properties = null;

	/**
	 * Mapping of {@link ManagedFunctionType} name to
	 * {@link ManagedFunctionModel} name.
	 */
	private final Map<String, String> namespaceFunctionNameMapping = new HashMap<String, String>();

	/**
	 * Mapping for a {@link ManagedFunctionModel} of the
	 * {@link ManagedFunctionObjectType} name to the
	 * {@link ManagedFunctionObjectModel} name.
	 */
	private final Map<String, Map<String, String>> managedFunctionToObjectNameMapping = new HashMap<String, Map<String, String>>();

	/**
	 * Mapping for a {@link FunctionModel} of the
	 * {@link ManagedFunctionFlowType} name to the {@link FunctionFlowModel}
	 * name.
	 */
	private final Map<String, Map<String, String>> functionToFlowNameMapping = new HashMap<String, Map<String, String>>();

	/**
	 * Mapping for a {@link FunctionModel} of the
	 * {@link ManagedFunctionEscalationType} name to the
	 * {@link FunctionEscalationModel} name.
	 */
	private final Map<String, Map<String, String>> functionToEscalationTypeMapping = new HashMap<String, Map<String, String>>();

	/**
	 * Listing of {@link ManagedFunctionType} names included on the
	 * {@link FunctionNamespaceModel}.
	 */
	private String[] functions = null;

	/**
	 * Initiate for specific setup per test.
	 */
	public AbstractRefactorNamespaceTest() {
		super(true);
	}

	@Override
	protected void setUp() throws Exception {

		// Setup the model
		super.setUp();

		// Obtain namespace model and specify details from it
		this.namespaceModel = this.model.getFunctionNamespaces().get(0);
		this.namespaceName = this.namespaceModel.getFunctionNamespaceName();
		this.managedFunctionSourceClassName = this.namespaceModel.getManagedFunctionSourceClassName();

	}

	/**
	 * Flags to refactor the name of the {@link FunctionNamespaceModel}.
	 * 
	 * @param namespaceName
	 *            New name for the {@link FunctionNamespaceModel}.
	 */
	protected void refactor_namespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	/**
	 * Flags to refactor the {@link ManagedFunctionSource} class name for the
	 * {@link FunctionNamespaceModel}.
	 * 
	 * @param managedFunctionSourceClassName
	 *            New {@link ManagedFunctionSource} class name for the
	 *            {@link FunctionNamespaceModel}.
	 */
	protected void refactor_managedFunctionSourceClassName(String managedFunctionSourceClassName) {
		this.managedFunctionSourceClassName = managedFunctionSourceClassName;
	}

	/**
	 * Flags to refactor the {@link PropertyModel} instances for the
	 * {@link FunctionNamespaceModel}.
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
	 * Maps the {@link ManagedFunctionType} to the {@link ManagedFunctionModel}.
	 * 
	 * @param managedFunctionName
	 *            Name of the {@link ManagedFunctionType}.
	 * @param managedFunctionModelName
	 *            Name of the {@link ManagedFunctionModel}.
	 */
	protected void refactor_mapFunction(String functionTypeName, String managedFunctionModelName) {
		this.namespaceFunctionNameMapping.put(functionTypeName, managedFunctionModelName);
	}

	/**
	 * Maps the {@link ManagedFunctionObjectType} name to the
	 * {@link ManagedFunctionObjectModel} name for a
	 * {@link ManagedFunctionModel}.
	 * 
	 * @param managedFunctionName
	 *            Name of the {@link ManagedFunctionModel}.
	 * @param objectTypeName
	 *            Name of the {@link ManagedFunctionObjectType}.
	 * @param managedFunctionObjectName
	 *            Name of the {@link ManagedFunctionObjectModel}.
	 */
	protected void refactor_mapObject(String managedFunctionName, String objectTypeName,
			String managedFunctionObjectName) {
		this.map(managedFunctionName, objectTypeName, managedFunctionObjectName,
				this.managedFunctionToObjectNameMapping);
	}

	/**
	 * Maps the {@link ManagedFunctionFlowType} name to the
	 * {@link FunctionFlowModel} name for a {@link FunctionModel}.
	 * 
	 * @param functionName
	 *            Name of the {@link FunctionModel}.
	 * @param flowTypeName
	 *            Name of the {@link ManagedFunctionFlowType}.
	 * @param functionFlowName
	 *            Name of the {@link FunctionFlowModel}.
	 */
	protected void refactor_mapFlow(String functionName, String flowTypeName, String functionFlowName) {
		this.map(functionName, flowTypeName, functionFlowName, this.functionToFlowNameMapping);
	}

	/**
	 * Maps the {@link ManagedFunctionEscalationType} name to the
	 * {@link FunctionEscalationModel} name for the {@link FunctionModel}.
	 * 
	 * @param functionName
	 *            Name of the {@link FunctionModel}.
	 * @param escalationTypeName
	 *            Name of the {@link ManagedFunctionEscalationType}.
	 * @param functionEscalationName
	 *            Name of the {@link FunctionEscalationModel}.
	 */
	protected void refactor_mapEscalation(String functionName, String escalationTypeName,
			String functionEscalationName) {
		this.map(functionName, escalationTypeName, functionEscalationName, this.functionToEscalationTypeMapping);
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
	private void map(String a, String b, String c, Map<String, Map<String, String>> map) {

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
	 * Specifies the names of the {@link ManagedFunctionType} instances to
	 * include on the {@link FunctionNamespaceModel}.
	 * 
	 * @param functionNames
	 *            Names of the {@link ManagedFunctionType} instances to include
	 *            on the {@link FunctionNamespaceModel}.
	 */
	protected void refactor_includeFunctions(String... functionNames) {
		this.functions = functionNames;
	}

	/**
	 * Convenience method to do refactoring with a simple
	 * {@link FunctionNamespaceType}.
	 */
	protected void doRefactor() {
		this.doRefactor((FunctionNamespaceType) null);
	}

	/**
	 * Convenience method to do refactoring and validates applying and
	 * reverting.
	 * 
	 * @param namespaceTypeConstructor
	 *            {@link NamespaceTypeConstructor}.
	 */
	protected void doRefactor(NamespaceTypeConstructor namespaceTypeConstructor) {

		// Construct the namespace type
		FunctionNamespaceType namespaceType = this.constructNamespaceType(namespaceTypeConstructor);

		// Do the refactoring
		this.doRefactor(namespaceType);
	}

	/**
	 * Does the refactoring and validates applying and reverting.
	 * 
	 * @param namespaceType
	 *            {@link FunctionNamespaceType}.
	 */
	protected void doRefactor(FunctionNamespaceType namespaceType) {

		// Ensure have a namespace type
		if (namespaceType == null) {
			// Create simple namespace type
			namespaceType = this.constructNamespaceType(new NamespaceTypeConstructor() {
				@Override
				public void construct(NamespaceTypeContext context) {
					// Simple namespace type
				}
			});
		}

		// Create the property list
		PropertyList propertyList = this.properties;
		if (propertyList == null) {
			// Not refactoring properties, so take from namespace model
			propertyList = new PropertyListImpl();
			for (PropertyModel property : this.namespaceModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(property.getValue());
			}
		}

		// Create the listing of functions
		String[] functionNames = this.functions;
		if (functionNames == null) {
			// Not refactoring functions, so take from namespace model
			List<String> functionNameList = new LinkedList<String>();
			for (ManagedFunctionModel managedFunction : this.namespaceModel.getManagedFunctions()) {
				functionNameList.add(managedFunction.getManagedFunctionName());
			}
			functionNames = functionNameList.toArray(new String[0]);
		}

		// Create the change to refactor
		Change<FunctionNamespaceModel> change = this.operations.refactorFunctionNamespace(this.namespaceModel,
				this.namespaceName, this.managedFunctionSourceClassName, propertyList, namespaceType,
				this.namespaceFunctionNameMapping, this.managedFunctionToObjectNameMapping,
				this.functionToFlowNameMapping, this.functionToEscalationTypeMapping, functionNames);

		// Asset the refactoring changes
		this.assertChange(change, this.namespaceModel, "Refactor namespace", true);
	}

}
