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
package net.officefloor.autowire.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Provides a singleton object.
 * 
 * @author Daniel Sagenschneider
 */
public class SingletonManagedObjectSource extends
		AbstractManagedObjectSource<None, None> implements ManagedObject,
		ExtensionInterfaceFactory<Object> {

	/**
	 * Singleton.
	 */
	private final Object object;

	/**
	 * Initiate.
	 * 
	 * @param object
	 *            Singleton object.
	 */
	public SingletonManagedObjectSource(Object object) {
		this.object = object;
	}

	/*
	 * ========================== ManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {

		// Obtain the object type
		Class<?> objectType = this.object.getClass();

		// Specify the object type
		context.setObjectClass(objectType);

		// Load the extension interfaces
		List<Class<?>> extensionInterfaces = new LinkedList<Class<?>>();
		this.loadAllExtensionInterfaces(objectType, extensionInterfaces);
		for (Class extensionInterface : extensionInterfaces) {
			context.addManagedObjectExtensionInterface(extensionInterface, this);
		}
	}

	@Override
	protected ManagedObject getManagedObject() {
		return this;
	}

	/*
	 * ============================ ManagedObject ============================
	 */

	@Override
	public Object getObject() {
		return this.object;
	}

	/*
	 * =================== ExtensionInterfaceFactory ======================
	 */

	@Override
	public Object createExtensionInterface(ManagedObject managedObject) {
		return ((SingletonManagedObjectSource) managedObject).object;
	}

	/**
	 * Loads all extension interfaces from type.
	 * 
	 * @param type
	 *            Type.
	 * @param interfaces
	 *            Listing of interfaces to be loaded.
	 */
	private void loadAllExtensionInterfaces(Class<?> type,
			List<Class<?>> interfaces) {

		// Determine if already loaded type
		if (interfaces.contains(type)) {
			return; // already loaded information for type
		}

		// Add type to prevent it further being loaded
		interfaces.add(type);

		// Load the interfaces from type
		for (Class<?> interfaceType : type.getInterfaces()) {
			this.loadAllExtensionInterfaces(interfaceType, interfaces);
		}

		// Load parent class types
		Class<?> parentType = type.getSuperclass();
		if (parentType != null) {
			this.loadAllExtensionInterfaces(parentType, interfaces);
		}
	}

}