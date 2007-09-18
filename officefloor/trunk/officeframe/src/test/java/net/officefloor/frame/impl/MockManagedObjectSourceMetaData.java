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
package net.officefloor.frame.impl;

import java.util.EnumMap;
import java.util.Map;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.impl.ManagedObjectDependencyMetaDataImpl;

/**
 * Mock
 * {@link net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData}.
 * 
 * @author Daniel
 */
public class MockManagedObjectSourceMetaData<D extends Enum<D>, H extends Enum<H>>
		implements ManagedObjectSourceMetaData<D, H> {

	/**
	 * {@link Class} of the {@link ManagedObject}.
	 */
	protected final Class managedObjectClass;

	/**
	 * Class of object being managed.
	 */
	protected final Class objectClass;

	/**
	 * Dependency keys.
	 */
	protected final Class<D> dependencyKeys;

	/**
	 * Dependency meta-data.
	 */
	protected final Map<D, ManagedObjectDependencyMetaData> dependencyMetaData;

	/**
	 * Handler keys.
	 */
	protected final Class<H> handlerKeys;

	/**
	 * Handler meta-data.
	 */
	protected final Map<H, Class> handlerMetaData;

	/**
	 * Initiate from the {@link ManagedObject}.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject}.
	 */
	public MockManagedObjectSourceMetaData(ManagedObject managedObject) {
		this.managedObjectClass = managedObject.getClass();
		this.objectClass = Object.class;
		this.dependencyKeys = null;
		this.dependencyMetaData = null;
		this.handlerKeys = null;
		this.handlerMetaData = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param managedObjectClass
	 *            Class of the {@link ManagedObject}.
	 * @param objectClass
	 *            Class of the object being managed.
	 */
	public MockManagedObjectSourceMetaData(Class managedObjectClass,
			Class objectClass, Class<D> dependencyKeys,
			Map<D, Class> dependencyClasses, Class<H> handlerKeys,
			Map<H, Class> handlerClasses) {
		this.managedObjectClass = managedObjectClass;
		this.objectClass = objectClass;

		// Load the dependency meta-data
		this.dependencyKeys = dependencyKeys;
		if (this.dependencyKeys == null) {
			this.dependencyMetaData = null;
		} else {
			this.dependencyMetaData = new EnumMap<D, ManagedObjectDependencyMetaData>(
					this.dependencyKeys);
			for (D key : this.dependencyKeys.getEnumConstants()) {
				this.dependencyMetaData.put(key,
						new ManagedObjectDependencyMetaDataImpl(
								dependencyClasses.get(key)));
			}
		}

		// Load the handler meta-data
		this.handlerKeys = handlerKeys;
		if (this.handlerKeys == null) {
			this.handlerMetaData = null;
		} else {
			this.handlerMetaData = new EnumMap<H, Class>(this.handlerKeys);
			for (H key : this.handlerKeys.getEnumConstants()) {
				this.handlerMetaData.put(key, handlerClasses.get(key));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getManagedObjectClass()
	 */
	public Class getManagedObjectClass() {
		return this.managedObjectClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getObjectClass()
	 */
	public Class getObjectClass() {
		return this.objectClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyKeys()
	 */
	public Class<D> getDependencyKeys() {
		return this.dependencyKeys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getDependencyMetaData(D)
	 */
	public ManagedObjectDependencyMetaData getDependencyMetaData(D key) {
		return this.dependencyMetaData.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerKeys()
	 */
	public Class<H> getHandlerKeys() {
		return this.handlerKeys;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getHandlerType(H)
	 */
	public Class getHandlerType(H key) {
		return this.handlerMetaData.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData#getExtensionInterfacesMetaData()
	 */
	public ManagedObjectExtensionInterfaceMetaData[] getExtensionInterfacesMetaData() {
		// TODO Auto-generated method stub
		return null;
	}

}
