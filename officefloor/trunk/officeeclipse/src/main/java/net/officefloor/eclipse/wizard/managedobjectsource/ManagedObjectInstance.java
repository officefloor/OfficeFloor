/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.wizard.managedobjectsource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Instance of a {@link ManagedObject}.
 * 
 * @author Daniel
 */
public class ManagedObjectInstance {

	/**
	 * Name of this {@link ManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * {@link ManagedObjectSource} class name.
	 */
	private final String managedObjectSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link ManagedObjectType}.
	 */
	private final ManagedObjectType<?> managedObjectType;

	/**
	 * Default timeout for the {@link ManagedObject}
	 */
	private final long defaultTimeout;

	/**
	 * Initiate for public use.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectSourceClassName
	 *            {@link ManagedObjectSource} class name.
	 * @param defaultTimeout
	 *            Default timeout for the {@link ManagedObject}
	 */
	public ManagedObjectInstance(String managedObjectName,
			String managedObjectSourceClassName, long defaultTimeout) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.managedObjectType = null;
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * Initiate from {@link ManagedObjectSourceInstance}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectSourceClassName
	 *            {@link ManagedObjectSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param managedObjectType
	 *            {@link ManagedObjectType}.
	 * @param defaultTimeout
	 *            Default timeout for the {@link ManagedObject}
	 */
	ManagedObjectInstance(String managedObjectName,
			String managedObjectSourceClassName, PropertyList propertyList,
			ManagedObjectType<?> managedObjectType, long defaultTimeout) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.propertyList = propertyList;
		this.managedObjectType = managedObjectType;
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * Obtains the name of the {@link ManagedObject}.
	 * 
	 * @return Name of the {@link ManagedObject}.
	 */
	public String getManagedObjectName() {
		return this.managedObjectName;
	}

	/**
	 * Obtains the {@link ManagedObjectSource} class name.
	 * 
	 * @return {@link ManagedObjectSource} class name.
	 */
	public String getManagedObjectSourceClassName() {
		return this.managedObjectSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertylist() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link ManagedObjectType}.
	 * 
	 * @return {@link ManagedObjectType} if obtained from
	 *         {@link ManagedObjectSourceInstance} or <code>null</code> if
	 *         initiated by <code>public</code> constructor.
	 */
	public ManagedObjectType<?> getManagedObjectType() {
		return this.managedObjectType;
	}

	/**
	 * Returns the default timeout for the {@link ManagedObject}.
	 * 
	 * @return Default timeout for the {@link ManagedObject}.
	 */
	public long getDefaultTimeout() {
		return this.defaultTimeout;
	}
}