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
package net.officefloor.eclipse.wizard.managedfunctionsource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;
import net.officefloor.model.section.PropertyModel;

/**
 * Instance of a {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionNamespaceInstance {

	/**
	 * Name of this {@link FunctionNamespaceModel}.
	 */
	private final String namespaceName;

	/**
	 * {@link ManagedFunctionWorkSource} class name.
	 */
	private final String managedFunctionSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link FunctionNamespaceModel}.
	 */
	private final FunctionNamespaceModel namespaceModel;

	/**
	 * {@link FunctionNamespaceType}.
	 */
	private final FunctionNamespaceType namespaceType;

	/**
	 * {@link ManagedFunctionType} selected.
	 */
	private final ManagedFunctionType<?, ?>[] functionTypes;

	/**
	 * Names of the selected {@link ManagedFunctionType} instances.
	 */
	private final String[] functionTypeNames;

	/**
	 * Mapping of {@link ManagedFunctionType} name to
	 * {@link ManagedFunctionModel} name.
	 */
	private final Map<String, String> managedFunctionNameMapping;

	/**
	 * Mapping of {@link ManagedFunctionObjectType} name to
	 * {@link ManagedFunctionObjectModel} name for a particular
	 * {@link ManagedFunctionModel} name.
	 */
	private final Map<String, Map<String, String>> functionObjectNameMappingForManagedFunction;

	/**
	 * Mapping of {@link ManagedFunctionFlowType} name to
	 * {@link FunctionFlowModel} name for a particular {@link FunctionModel}
	 * name.
	 */
	private final Map<String, Map<String, String>> functionFlowNameMappingForFunction;

	/**
	 * Mapping of {@link ManagedFunctionEscalationType} name to
	 * {@link FunctionEscalationModel} name for a particular
	 * {@link FunctionModel} name.
	 */
	private final Map<String, Map<String, String>> functionEscalationTypeMappingForFunction;

	/**
	 * Initiate for public use.
	 * 
	 * @param namespaceModel
	 *            {@link FunctionNamespaceModel}.
	 */
	public FunctionNamespaceInstance(FunctionNamespaceModel namespaceModel) {
		this.namespaceModel = namespaceModel;
		this.namespaceName = this.namespaceModel.getFunctionNamespaceName();
		this.managedFunctionSourceClassName = this.namespaceModel.getManagedFunctionSourceClassName();
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		for (PropertyModel property : this.namespaceModel.getProperties()) {
			this.propertyList.addProperty(property.getName()).setValue(property.getValue());
		}
		this.namespaceType = null;
		this.functionTypes = null;
		this.managedFunctionNameMapping = null;
		this.functionObjectNameMappingForManagedFunction = null;
		this.functionFlowNameMappingForFunction = null;
		this.functionEscalationTypeMappingForFunction = null;

		// Create the list of managed function type names
		List<String> managedFunctionNames = new LinkedList<String>();
		for (ManagedFunctionModel managedFunction : namespaceModel.getManagedFunctions()) {
			managedFunctionNames.add(managedFunction.getManagedFunctionName());
		}
		this.functionTypeNames = managedFunctionNames.toArray(new String[0]);
	}

	/**
	 * Initiate from {@link ManagedFunctionSourceInstance}.
	 * 
	 * @param namespaceName
	 *            Name of the {@link FunctionNamespaceModel}.
	 * @param managedFunctionSourceClassName
	 *            {@link ManagedFunctionSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param namespaceType
	 *            {@link FunctionNamespaceType}.
	 * @param functionTypes
	 *            {@link ManagedFunctionType} selected.
	 * @param managedFunctionNameMapping
	 *            Mapping of {@link ManagedFunctionType} name to
	 *            {@link ManagedFunctionModel} name.
	 * @param functionObjectNameMappingForManagedFunction
	 *            Mapping of {@link ManagedFunctionObjectType} name to
	 *            {@link ManagedFunctionObjectModel} name for a particular
	 *            {@link ManagedFunctionModel} name.
	 * @param functionFlowNameMappingForFunction
	 *            Mapping of {@link ManagedFunctionFlowType} name to
	 *            {@link FlowFlowModel} name for a particular {@link FlowModel}
	 *            name.
	 * @param functionEscalationTypeMappingForFunction
	 *            Mapping of {@link ManagedFunctionEscalationType} name to
	 *            {@link FunctionEscalationModel} name for a particular
	 *            {@link FunctionModel} name.
	 */
	FunctionNamespaceInstance(String namespaceName, String managedFunctionSourceClassName, PropertyList propertyList,
			FunctionNamespaceType namespaceType, ManagedFunctionType<?, ?>[] functionTypes,
			Map<String, String> managedFunctionNameMapping,
			Map<String, Map<String, String>> functionObjectNameMappingForManagedFunction,
			Map<String, Map<String, String>> functionFlowNameMappingForFunction,
			Map<String, Map<String, String>> functionEscalationTypeMappingForFunction) {
		this.namespaceName = namespaceName;
		this.managedFunctionSourceClassName = managedFunctionSourceClassName;
		this.propertyList = propertyList;
		this.namespaceModel = null;
		this.namespaceType = namespaceType;
		this.functionTypes = functionTypes;
		this.managedFunctionNameMapping = managedFunctionNameMapping;
		this.functionObjectNameMappingForManagedFunction = functionObjectNameMappingForManagedFunction;
		this.functionFlowNameMappingForFunction = functionFlowNameMappingForFunction;
		this.functionEscalationTypeMappingForFunction = functionEscalationTypeMappingForFunction;

		// Create the listing of managed function type names
		this.functionTypeNames = new String[this.functionTypes.length];
		for (int i = 0; i < this.functionTypeNames.length; i++) {
			this.functionTypeNames[i] = this.functionTypes[i].getFunctionName();
		}
	}

	/**
	 * Obtains the name of the {@link FunctionNamespaceModel}.
	 * 
	 * @return Name of the {@link FunctionNamespaceModel}.
	 */
	public String getFunctionNamespaceName() {
		return this.namespaceName;
	}

	/**
	 * Obtains the {@link ManagedFunctionSource} class name.
	 * 
	 * @return {@link ManagedFunctionSource} class name.
	 */
	public String getManagedFunctionSourceClassName() {
		return this.managedFunctionSourceClassName;
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
	 * Obtains the {@link FunctionNamespaceModel}.
	 * 
	 * @return {@link FunctionNamespaceModel} if instantiated by
	 *         <code>public</code> constructor or <code>null</code> if from
	 *         {@link ManagedFunctionSourceInstance}.
	 */
	FunctionNamespaceModel getFunctionNamespaceModel() {
		return this.namespaceModel;
	}

	/**
	 * Obtains the {@link FunctionNamespaceType}.
	 * 
	 * @return {@link FunctionNamespaceType} if obtained from
	 *         {@link ManagedFunctionSourceInstance} or <code>null</code> if
	 *         initiated by <code>public</code> constructor.
	 */
	public FunctionNamespaceType getFunctionNamespaceType() {
		return this.namespaceType;
	}

	/**
	 * Obtains the {@link ManagedFunctionType} instances.
	 * 
	 * @return {@link ManagedFunctionType} instances if obtained from
	 *         {@link ManagedFunctionSourceInstance} or <code>null</code> if
	 *         initiated by <code>public</code> constructor.
	 */
	public ManagedFunctionType<?, ?>[] getManagedFunctionTypes() {
		return this.functionTypes;
	}

	/**
	 * Obtains the names of the {@link ManagedFunctionType} instances being used
	 * on the {@link FunctionNamespaceType}.
	 * 
	 * @return Names of the {@link ManagedFunctionType} instances being used on
	 *         the {@link FunctionNamespaceType}.
	 */
	public String[] getManagedFunctionTypeNames() {
		return this.functionTypeNames;
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionType} name to
	 * {@link ManagedFunctionModel} name.
	 * 
	 * @return Mapping of {@link ManagedFunctionType} name to
	 *         {@link ManagedFunctionModel} name.
	 */
	public Map<String, String> getManagedFunctionNameMapping() {
		return this.managedFunctionNameMapping;
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionObjectType} name to
	 * {@link ManagedFunctionObjectModel} name for a particular
	 * {@link ManagedFunctionModel} name.
	 * 
	 * @return Mapping of {@link ManagedFunctionObjectType} name to
	 *         {@link ManagedFunctionObjectModel} name for a particular
	 *         {@link ManagedFunctionModel} name.
	 */
	public Map<String, Map<String, String>> getFunctionObjectNameMappingForManagedFunction() {
		return this.functionObjectNameMappingForManagedFunction;
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionFlowType} name to
	 * {@link FunctionFlowModel} name for a particular {@link FunctionModel}
	 * name.
	 * 
	 * @return Mapping of {@link ManagedFunctionFlowType} name to
	 *         {@link FunctionFlowModel} name for a particular
	 *         {@link FunctionModel} name.
	 */
	public Map<String, Map<String, String>> getFunctionFlowNameMappingForFunction() {
		return this.functionFlowNameMappingForFunction;
	}

	/**
	 * Obtains the mapping of {@link ManagedFunctionEscalationType} name to
	 * {@link FunctionEscalationModel} name for a particular
	 * {@link FunctionModel} name.
	 * 
	 * @return Mapping of {@link ManagedFunctionEscalationType} name to
	 *         {@link FunctionEscalationModel} name for a particular
	 *         {@link FunctionModel} name.
	 */
	public Map<String, Map<String, String>> getFunctionEscalationTypeMappingForFunction() {
		return this.functionEscalationTypeMappingForFunction;
	}

}