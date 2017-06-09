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
package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link OfficeFloorSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSourceContextImpl extends SourceContextImpl
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
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param officeFloorNode
	 *            {@link OfficeFloorNode}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public OfficeFloorSourceContextImpl(boolean isLoadingType, String officeFloorLocation, PropertyList propertyList,
			OfficeFloorNode officeFloorNode, NodeContext nodeContext) {
		super(isLoadingType, nodeContext.getRootSourceContext(), new PropertyListSourceProperties(propertyList));
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

					// Obtain the override properties
					PropertyList overrideProperties = this.context.overrideProperties(this.officeFloorNode,
							managedObjectSourceName, properties);

					// Load and return the managed object type
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(this.officeFloorNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSource, overrideProperties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName,
			String managedObjectSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(ManagedObjectType.class, managedObjectSourceClassName,
				this.context.getCompilerIssues(), () -> {

					// Obtain the managed object source class
					Class managedObjectSourceClass = this.context
							.getManagedObjectSourceClass(managedObjectSourceClassName, this.officeFloorNode);
					if (managedObjectSourceClass == null) {
						return null;
					}

					// Obtain the override properties
					PropertyList overrideProperties = this.context.overrideProperties(this.officeFloorNode,
							managedObjectSourceName, properties);

					// Load and return the managed object type
					ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(this.officeFloorNode);
					return managedObjectLoader.loadManagedObjectType(managedObjectSourceClass, overrideProperties);
				});
	}

	@Override
	public boolean isInputManagedObject(ManagedObjectType<?> managedObjectType) {

		// Obtain the managed object loader
		ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(this.officeFloorNode);

		// Return whether should be an input managed object
		return managedObjectLoader.isInputManagedObject(managedObjectType);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SupplierType loadSupplierType(String supplierName, String supplierSourceClassName, PropertyList properties) {
		return CompileUtil.loadType(SupplierType.class, supplierSourceClassName, this.context.getCompilerIssues(),
				() -> {

					// Obtain the supplier source class
					Class supplierSourceClass = this.context.getSupplierSourceClass(supplierSourceClassName,
							this.officeFloorNode);
					if (supplierSourceClass == null) {
						return null;
					}

					// Obtain the override properties
					PropertyList overrideProperties = this.context.overrideProperties(this.officeFloorNode,
							supplierName, properties);

					// Load and return the supplier type
					SupplierLoader supplierLoader = this.context.getSupplierLoader(this.officeFloorNode);
					return supplierLoader.loadSupplierType(supplierSourceClass, overrideProperties);
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OfficeType loadOfficeType(String officeName, String officeSourceClassName, String location,
			PropertyList properties) {
		return CompileUtil.loadType(OfficeType.class, officeSourceClassName, this.context.getCompilerIssues(), () -> {

			// Obtain the office source class
			Class officeSourceClass = this.context.getOfficeSourceClass(officeSourceClassName, this.officeFloorNode);
			if (officeSourceClass == null) {
				return null;
			}

			// Obtain the override properties
			PropertyList overrideProperties = this.context.overrideProperties(this.officeFloorNode, officeName,
					properties);

			// Load and return the office type
			OfficeLoader officeLoader = this.context.getOfficeLoader(this.officeFloorNode);
			return officeLoader.loadOfficeType(officeSourceClass, location, overrideProperties);
		});
	}

	@Override
	public OfficeType loadOfficeType(String officeName, OfficeSource officeSource, String location,
			PropertyList properties) {
		return CompileUtil.loadType(OfficeType.class, officeSource.getClass().getName(),
				this.context.getCompilerIssues(), () -> {

					// Obtain the override properties
					PropertyList overrideProperties = this.context.overrideProperties(this.officeFloorNode, officeName,
							properties);

					// Load and return the office type
					OfficeLoader officeLoader = this.context.getOfficeLoader(this.officeFloorNode);
					return officeLoader.loadOfficeType(officeSource, location, overrideProperties);
				});
	}

}