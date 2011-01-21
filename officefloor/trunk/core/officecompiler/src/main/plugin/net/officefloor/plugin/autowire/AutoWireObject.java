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
package net.officefloor.plugin.autowire;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Object for configuring auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObject extends AutoWireProperties {

	/**
	 * {@link ManagedObjectSource} class.
	 */
	private final Class<?> managedObjectSourceClass;

	/**
	 * {@link ManagedObjectSourceWirer}.
	 */
	private final ManagedObjectSourceWirer wirer;

	/**
	 * Object types for linking this a dependency.
	 */
	private final Class<?>[] objectTypes;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param properties
	 *            {@link PropertyList} for the {@link ManagedObjectSource}.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer}.
	 * @param objectTypes
	 *            Object types that the {@link ManagedObjectSource} is to
	 *            provide auto-wiring.
	 */
	public AutoWireObject(Class<?> managedObjectSourceClass,
			PropertyList properties, ManagedObjectSourceWirer wirer,
			Class<?>... objectTypes) {
		super(properties);
		this.managedObjectSourceClass = managedObjectSourceClass;
		this.wirer = wirer;
		this.objectTypes = objectTypes;
	}

	/**
	 * Obtains the {@link ManagedObjectSource} class.
	 * 
	 * @return {@link ManagedObjectSource} class.
	 */
	public Class<?> getManagedObjectSourceClass() {
		return this.managedObjectSourceClass;
	}

	/**
	 * Obtains the {@link ManagedObjectSourceWirer}.
	 * 
	 * @return {@link ManagedObjectSourceWirer}.
	 */
	public ManagedObjectSourceWirer getManagedObjectSourceWirer() {
		return this.wirer;
	}

	/**
	 * Obtains the Object types that the {@link ManagedObjectSource} is to
	 * provide auto-wiring.
	 * 
	 * @return Object types that the {@link ManagedObjectSource} is to provide
	 *         auto-wiring.
	 */
	public Class<?>[] getObjectTypes() {
		return this.objectTypes;
	}

}