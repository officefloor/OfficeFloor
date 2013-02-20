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

import net.officefloor.autowire.supplier.SupplierLoader;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link OfficeFloorSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSourceContextImpl extends SourceContextImpl implements
		OfficeFloorSourceContext {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

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
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public OfficeFloorSourceContextImpl(boolean isLoadingType,
			String officeFloorLocation, PropertyList propertyList,
			NodeContext nodeContext) {
		super(isLoadingType, nodeContext.getSourceContext(),
				new PropertyListSourceProperties(propertyList));
		this.officeFloorLocation = officeFloorLocation;
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
	public ManagedObjectType<?> loadManagedObjectType(
			ManagedObjectSource<?, ?> managedObjectSource,
			PropertyList properties) {

		// Load the managed object type
		ManagedObjectLoader managedObjectLoader = this.context
				.getManagedObjectLoader(LocationType.OFFICE_FLOOR,
						this.officeFloorLocation, "loadManagedObjectType");
		ManagedObjectType<?> managedObjectType = managedObjectLoader
				.loadManagedObjectType(managedObjectSource, properties);

		// Ensure have the managed object type
		if (managedObjectType == null) {
			throw new LoadTypeError(ManagedObjectType.class,
					managedObjectSource.getClass().getName());
		}

		// Return the managed object type
		return managedObjectType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(
			String managedObjectSourceClassName, PropertyList properties) {

		// Obtain the managed object source class
		Class managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(managedObjectSourceClassName,
						LocationType.OFFICE_FLOOR, this.officeFloorLocation,
						"loadManagedObjectType");

		// Ensure have the managed object source class
		if (managedObjectSourceClass == null) {
			throw new LoadTypeError(ManagedObjectType.class,
					managedObjectSourceClassName);
		}

		// Load the managed object type
		ManagedObjectLoader managedObjectLoader = this.context
				.getManagedObjectLoader(LocationType.OFFICE_FLOOR,
						this.officeFloorLocation, "loadManagedObjectType");
		ManagedObjectType<?> managedObjectType = managedObjectLoader
				.loadManagedObjectType(managedObjectSourceClass, properties);

		// Ensure have the managed object type
		if (managedObjectType == null) {
			throw new LoadTypeError(ManagedObjectType.class,
					managedObjectSourceClassName);
		}

		// Return the managed object type
		return managedObjectType;
	}

	@Override
	public boolean isInputManagedObject(ManagedObjectType<?> managedObjectType) {

		// Obtain the managed object loader
		ManagedObjectLoader managedObjectLoader = this.context
				.getManagedObjectLoader(LocationType.OFFICE_FLOOR,
						this.officeFloorLocation, "loadManagedObjectType");

		// Return whether should be an input managed object
		return managedObjectLoader.isInputManagedObject(managedObjectType);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SupplierType loadSupplierType(String supplierSourceClassName,
			PropertyList properties) {

		// Obtain the supplier source class
		Class supplierSourceClass = this.context.getSupplierSourceClass(
				supplierSourceClassName, this.officeFloorLocation,
				"loadSupplierType");

		// Ensure have the supplier source class
		if (supplierSourceClass == null) {
			throw new LoadTypeError(SupplierType.class, supplierSourceClassName);
		}

		// Load the supplier type
		SupplierLoader supplierLoader = this.context.getSupplierLoader(
				this.officeFloorLocation, "loadSupplierType");
		SupplierType supplierType = supplierLoader.loadSupplierType(
				supplierSourceClass, properties);

		// Ensure have the supplier type
		if (supplierType == null) {
			throw new LoadTypeError(SupplierType.class, supplierSourceClassName);
		}

		// Return the supplier type
		return supplierType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OfficeType loadOfficeType(String officeSourceClassName,
			String location, PropertyList properties) {

		// Obtain the office source class
		Class officeSourceClass = this.context.getOfficeSourceClass(
				officeSourceClassName, this.officeFloorLocation,
				"loadOfficeType");

		// Ensure have the office source class
		if (officeSourceClass == null) {
			throw new LoadTypeError(OfficeType.class, officeSourceClassName);
		}

		// Load the office type
		OfficeLoader officeLoader = this.context.getOfficeLoader();
		OfficeType officeType = officeLoader.loadOfficeType(officeSourceClass,
				location, properties);

		// Ensure have the office type
		if (officeType == null) {
			throw new LoadTypeError(OfficeType.class, officeSourceClassName);
		}

		// Return the office type
		return officeType;
	}

	@Override
	public OfficeType loadOfficeType(OfficeSource officeSource,
			String location, PropertyList properties) {

		// Load the office type
		OfficeLoader officeLoader = this.context.getOfficeLoader();
		OfficeType officeType = officeLoader.loadOfficeType(officeSource,
				location, properties);

		// Ensure have the office type
		if (officeType == null) {
			throw new LoadTypeError(OfficeType.class, officeSource.getClass()
					.getName());
		}

		// Return the office type
		return officeType;
	}

}