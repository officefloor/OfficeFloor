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

package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.configuration.impl.ConfigurationSourceContextImpl;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link OfficeFloorSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSourceContextImpl extends ConfigurationSourceContextImpl
		implements OfficeFloorSourceContext, OfficeFloorExtensionContext {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloorNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType       Indicates if loading type.
	 * @param officeFloorLocation Location of the {@link OfficeFloor}.
	 * @param additionalProfiles  Additional profiles.
	 * @param propertyList        {@link PropertyList}.
	 * @param officeFloorNode     {@link OfficeFloorNode}.
	 * @param nodeContext         {@link NodeContext}.
	 */
	public OfficeFloorSourceContextImpl(boolean isLoadingType, String officeFloorLocation, String[] additionalProfiles,
			PropertyList propertyList, OfficeFloorNode officeFloorNode, NodeContext nodeContext) {
		super(officeFloorNode.getNodeName(), isLoadingType, nodeContext.getRootSourceContext(), additionalProfiles,
				new PropertyListSourceProperties(propertyList));
		this.officeFloorLocation = officeFloorLocation;
		this.officeFloorNode = officeFloorNode;
		this.context = nodeContext;
	}

	/*
	 * =============== OfficeFloorLoaderContext ============================
	 */

	@Override
	public String getOfficeFloorLocation() {
		return this.officeFloorLocation;
	}

	@Override
	public PropertyList createPropertyList() {
		return this.context.createPropertyList();
	}

	@Override
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the managed object type
					ManagedObjectSourceNode mosNode = this.context
							.createManagedObjectSourceNode(managedObjectSourceName, this.officeFloorNode);
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(mosNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSource, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the managed object source class
					ManagedObjectSourceNode mosNode = this.context
							.createManagedObjectSourceNode(managedObjectSourceName, this.officeFloorNode);
					Class managedObjectSourceClass = this.context
							.getManagedObjectSourceClass(managedObjectSourceClassName, mosNode);
					if (managedObjectSourceClass == null) {
						return null;
					}

					// Load and return the managed object type
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(mosNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSourceClass, properties);
				});
	}

	@Override
	public InitialSupplierType loadSupplierType(String supplierName, SupplierSource supplierSource,
			PropertyList properties) {
		return CompileUtil.loadType(InitialSupplierType.class, supplierSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the supplier type
					SupplierNode supplierNode = this.context.createSupplierNode(supplierName, this.officeFloorNode);
					SupplierLoader supplierLoader = this.context.getSupplierLoader(supplierNode, true);
					return supplierLoader.loadInitialSupplierType(supplierSource, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public InitialSupplierType loadSupplierType(String supplierName, String supplierSourceClassName,
			PropertyList properties) {
		return CompileUtil.loadType(InitialSupplierType.class, supplierSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the supplier source class
					SupplierNode supplierNode = this.context.createSupplierNode(supplierName, this.officeFloorNode);
					Class supplierSourceClass = this.context.getSupplierSourceClass(supplierSourceClassName,
							supplierNode);
					if (supplierSourceClass == null) {
						return null;
					}

					// Load and return the supplier type
					SupplierLoader supplierLoader = this.context.getSupplierLoader(supplierNode, true);
					return supplierLoader.loadInitialSupplierType(supplierSourceClass, properties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OfficeType loadOfficeType(String officeName, String officeSourceClassName, String location,
			PropertyList properties) {
		return CompileUtil.loadType(OfficeType.class, officeSourceClassName, this.context.getCompilerIssues(), () -> {

			// Obtain the office source class
			OfficeNode officeNode = this.context.createOfficeNode(officeName, this.officeFloorNode);
			Class officeSourceClass = this.context.getOfficeSourceClass(officeSourceClassName, officeNode);
			if (officeSourceClass == null) {
				return null;
			}

			// Load and return the office type
			OfficeLoader officeLoader = this.context.getOfficeLoader(officeNode);
			return officeLoader.loadOfficeType(officeSourceClass, location, properties);
		});
	}

	@Override
	public OfficeType loadOfficeType(String officeName, OfficeSource officeSource, String location,
			PropertyList properties) {
		return CompileUtil.loadType(OfficeType.class, officeSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Load and return the office type
					OfficeNode officeNode = this.context.createOfficeNode(officeName, this.officeFloorNode);
					OfficeLoader officeLoader = this.context.getOfficeLoader(officeNode);
					return officeLoader.loadOfficeType(officeSource, location, properties);
				});
	}

}
