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
package net.officefloor.compile.impl.office;

import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;

/**
 * {@link OfficeSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceContextImpl extends SourceContextImpl implements
		OfficeSourceContext {

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType
	 *            Indicates if loading type.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public OfficeSourceContextImpl(boolean isLoadingType,
			String officeLocation, PropertyList propertyList,
			NodeContext nodeContext) {
		super(isLoadingType, nodeContext.getSourceContext(),
				new PropertyListSourceProperties(propertyList));
		this.officeLocation = officeLocation;
		this.context = nodeContext;
	}

	/*
	 * ================= OfficeLoaderContext ================================
	 */

	@Override
	public String getOfficeLocation() {
		return this.officeLocation;
	}

	@Override
	public PropertyList createPropertyList() {
		return this.context.createPropertyList();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(
			String managedObjectSourceClassName, PropertyList properties) {

		// Obtain the managed object source class
		Class managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(managedObjectSourceClassName,
						LocationType.OFFICE, this.officeLocation,
						"loadManagedObjectType");

		// Ensure have the managed object source class
		if (managedObjectSourceClass == null) {
			throw new LoadTypeError(ManagedObjectType.class,
					managedObjectSourceClassName);
		}

		// Load the managed object type
		ManagedObjectLoader managedObjectLoader = this.context
				.getManagedObjectLoader(LocationType.OFFICE,
						this.officeLocation, "loadManagedObjectType");
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GovernanceType<?, ?> loadGovernanceType(
			String governanceSourceClassName, PropertyList properties) {

		// Obtain the governance source class
		Class governanceSourceClass = this.context.getGovernanceSourceClass(
				governanceSourceClassName, this.officeLocation,
				"loadAdministratorType");

		// Ensure have the governance source class
		if (governanceSourceClass == null) {
			throw new LoadTypeError(GovernanceType.class,
					governanceSourceClassName);
		}

		// Load the governance type
		GovernanceLoader governanceLoader = this.context.getGovernanceLoader(
				this.officeLocation, "loadGovernanceType");
		GovernanceType<?, ?> governanceType = governanceLoader
				.loadGovernanceType(governanceSourceClass, properties);

		// Ensure have the governance type
		if (governanceType == null) {
			throw new LoadTypeError(GovernanceType.class,
					governanceSourceClassName);
		}

		// Return the governance type
		return governanceType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AdministratorType<?, ?> loadAdministratorType(
			String administratorSourceClassName, PropertyList properties) {

		// Obtain the administrator source class
		Class administratorSourceClass = this.context
				.getAdministratorSourceClass(administratorSourceClassName,
						this.officeLocation, "loadAdministratorType");

		// Ensure have the administrator source class
		if (administratorSourceClass == null) {
			throw new LoadTypeError(AdministratorType.class,
					administratorSourceClassName);
		}

		// Load the administrator type
		AdministratorLoader administratorLoader = this.context
				.getAdministratorLoader(this.officeLocation,
						"loadAdministratorType");
		AdministratorType<?, ?> administratorType = administratorLoader
				.loadAdministratorType(administratorSourceClass, properties);

		// Ensure have the administrator type
		if (administratorType == null) {
			throw new LoadTypeError(AdministratorType.class,
					administratorSourceClassName);
		}

		// Return the administrator type
		return administratorType;
	}

}