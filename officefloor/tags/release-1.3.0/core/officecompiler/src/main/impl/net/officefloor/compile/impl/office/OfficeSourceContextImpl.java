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

package net.officefloor.compile.impl.office;

import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.ConfigurationContextPropagateError;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceContextImpl extends SourcePropertiesImpl implements
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
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public OfficeSourceContextImpl(String officeLocation,
			PropertyList propertyList, NodeContext nodeContext) {
		super(new PropertyListSourceProperties(propertyList));
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
	public ConfigurationItem getConfiguration(String location) {
		try {
			return this.context.getConfigurationContext().getConfigurationItem(
					location);
		} catch (Throwable ex) {
			// Propagate failure to office loader
			throw new ConfigurationContextPropagateError(location, ex);
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.context.getClassLoader();
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
				.loadAdministrator(administratorSourceClass, properties);

		// Ensure have the administrator type
		if (administratorType == null) {
			throw new LoadTypeError(AdministratorType.class,
					administratorSourceClassName);
		}

		// Return the administrator type
		return administratorType;
	}
}